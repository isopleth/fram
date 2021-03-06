package fram.filesystem;

import fram.rotation.RotationCounter;
import fram.rotation.Orientation;
import fram.border.BorderProcessor;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import fram.Cache;
import fram.Configuration;
import fram.ExifDateReader;
import fram.Hash;
import fram.ManipulateImage;
import fram.Options.Option;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * This class does file copying, including rotating and flipping the image.
 *
 * @author Jason Leake
 */
public class FileCopier {

    private Configuration theConfiguration;
    private SortedMap<Integer, Path> fileMap = new TreeMap<>();
    private BufferedWriter copyListFile;
    private static final Logger logger = Logger.getLogger(FileCopier.class.getName());
    private static final String COPY_LIST_FILE = "copy_list.txt";
    private final Cache theCache;
    private final static String CLASSNAME = FileCopier.class.getName();
    // This is used by the unit tests

    /**
     * Constructor
     *
     * @param configuration configuration settings
     * @param cache file cache
     * @param copylist true to log the file copy
     */
    public FileCopier(Configuration configuration, Cache cache, boolean copylist) {
        theCache = cache;
        theConfiguration = configuration;
        if (copylist) {
            try {
                copyListFile = new BufferedWriter(new FileWriter(COPY_LIST_FILE));
                System.out.println(String.format("Logging to %s", COPY_LIST_FILE));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                copyListFile = null;
            }
        } else {
            copyListFile = null;
        }
    }

    /**
     * Add another file to the list of files to be copied
     *
     * @param index index number to assign to file
     * @param file file to copy
     */
    public void addAnotherFile(int index, Path file) {
        fileMap.put(index, file);
    }

    /**
     * Compact the list of output files so that there are no gaps in the
     * sequence of names
     */
    public void compactOutputFiles() {
        SortedMap<Integer, Path> newMap = new TreeMap<>();
        int newIndex = 0;
        for (int index : fileMap.keySet()) {
            newMap.put(newIndex++, fileMap.get(index));
        }
        fileMap = newMap;
    }

    /**
     * Copy the next file on the copy list
     *
     * @return true if successful, false if list empty
     */
    public boolean copy() {
        if (fileMap.isEmpty()) {
            // Copying done. Log it if the copy list file is open
            if (copyListFile != null) {
                try {
                    copyListFile.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                copyListFile = null;
            }
            return false;
        } else {
            final int index = fileMap.firstKey();
            final Path photoframeFile = convertIndexToDest(index);
            final Path inputFile = fileMap.get(index);
            // If we are logging the filenames then do that
            if (copyListFile != null) {
                try {
                    String message = String.format("Copy %s to %s\n",
                            inputFile.toString(),
                            photoframeFile.toString());
                    copyListFile.write(message);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }

            File destinationDirectory = photoframeFile.getParent().toFile();
            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdir();
            }

            copyAndAnnotateFile(index, inputFile, photoframeFile);

            // Remove the entry from the map as we have processed it
            fileMap.remove(index);
        }
        return true;
    }

    /**
     * Copy the file, adding any annotation requested. Rotate the output image
     * if the metadata says this needs to be done
     *
     * @param photoframeOutputFile output file
     */
    private void copyAndAnnotateFile(int index,
            Path originalFile,
            Path photoframeOutputFile) {

        File outputFile = photoframeOutputFile.toFile();
        String hash = null;
        if (theCache != null) {
            try {
                hash = new Hash(theConfiguration).generate(originalFile.toFile());
                Path cachedFile = theCache.getCachedFile(originalFile, hash);
                if (cachedFile != null) {
                    // There is a cached file all ready so no need to process
                    // the original file
                    if (theConfiguration.isSet(Option.VERBOSE)) {
                        System.out.println("Copying cached file to " + outputFile);
                        copyFile(cachedFile, photoframeOutputFile);
                    }
                    return;
                }
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CLASSNAME).log(Level.SEVERE, null, ex);
            }
        }

        // Else cached files are not enabled, or this file isn't in the cache
        final double ANNOTATION_SIZE = 1.0 / 25.0;

        try {
            URL imagePath = originalFile.toFile().toURI().toURL();
            BufferedImage image = ImageIO.read(imagePath);

            // Rotate the image if necessary
            if (!theConfiguration.isSet(Option.NO_ROTATE_IMAGES)) {
                Orientation imageOrientation = getOrientation(originalFile);
                switch (imageOrientation) {

                    case NONE:
                        break;

                    case CLOCKWISE:
                        image = ManipulateImage.rotate(image, -1);
                        break;

                    case ANTICLOCKWISE:
                        image = ManipulateImage.rotate(image, 1);
                        break;

                    case UPSIDE_DOWN:
                        image = ManipulateImage.rotate(image, 2);
                        break;

                    case MIRROR_LEFT_RIGHT:
                        image = ManipulateImage.mirror(image);
                        break;

                    case MIRROR_TOP_BOTTOM:
                        image = ManipulateImage.rotate(ManipulateImage.mirror(
                                ManipulateImage.rotate(image, +1)), -1);
                        break;

                    case ANTICLOCKWISE_AND_MIRROR:
                        image = ManipulateImage.rotate(ManipulateImage.mirror(image), 1);
                        break;

                    case CLOCKWISE_AND_MIRROR:
                        image = ManipulateImage.rotate(ManipulateImage.mirror(image), -1);
                        break;
                }
                RotationCounter.bump(imageOrientation);
            }

            int width = image.getWidth();
            if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                image = ManipulateImage.make3ByteBgr(image);
            }

            // Remove any border around the image if necessary.  This is not
            // yet fully implemented
            if (theConfiguration.isSet(Option.REMOVE_BORDER)) {
                BorderProcessor borderProcessor = new BorderProcessor(image);
                if (borderProcessor.hasBorder()) {
                    image = borderProcessor.removeBorder();
                }

            }

            final int minimumWidth = theConfiguration.getMinimumWidth();
            if (width < minimumWidth) {
                image = ManipulateImage.resizeImage(image, minimumWidth);
            }

            Graphics2D graphics2d = image.createGraphics();

            double size = image.getHeight() * ANNOTATION_SIZE;

            if (theConfiguration.isSet(Option.NO_DIRECTORY_NAME)) {
                double xoffset = image.getHeight() * ANNOTATION_SIZE;
                double yoffset = image.getHeight() * ANNOTATION_SIZE;
                graphics2d.setFont(new Font("TimesRoman",
                        Font.PLAIN, (int) size));
                graphics2d.setColor(Color.red);
                // Add the name of the immediate containing directory to the image
                graphics2d.drawString(originalFile.getName(originalFile.
                        getNameCount() - 2).toString(),
                        (int) xoffset, (int) yoffset);
            }

            String indexText = "";
            if (theConfiguration.isSet(Option.SHOW_FILENAME)) {
                // Want debugging info on image
                indexText = originalFile.getFileName().toString();
            }

            if (theConfiguration.isSet(Option.SHOW_INDEX)) {
                if (!indexText.isBlank()) {
                    indexText += " ";
                }
                indexText += String.format("%06d", index);
            }

            if (theConfiguration.isSet(Option.SHOW_DATE)) {
                String theDate = new ExifDateReader(originalFile).getDate();
                if (!theDate.isEmpty()) {
                    if (!indexText.isBlank()) {
                        indexText += " ";
                    }
                    indexText += theDate;
                }
            }

            // Add text to the bottom of the image if required
            if (!indexText.isEmpty()) {
                double xoffset = image.getHeight() * ANNOTATION_SIZE;
                double yoffset = image.getHeight() * ANNOTATION_SIZE;
                double x = xoffset;
                double y = yoffset + image.getHeight() - (size * 2);
                graphics2d.setFont(new Font("TimesRoman", Font.PLAIN, (int) size / 2));
                graphics2d.drawString(indexText, (int) x, (int) y);
            }
            if (theConfiguration.isSet(Option.VERBOSE)) {
                System.out.println("Writing " + outputFile);
            }
            ImageIO.write(image, "jpg", outputFile);
            if (theCache != null) {
                theCache.cacheFile(originalFile, hash, outputFile);
            }
        } catch (IOException ex) {
            // If this fails then just copy the file
            System.out.println(photoframeOutputFile.toString() + ": " + ex);
            try {
                Files.copy(originalFile, photoframeOutputFile, REPLACE_EXISTING);
            } catch (IOException exc) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get Exif information indicating whether the image was rotated and what
     * the orientation is
     *
     * @param imagePath image path
     * @return rotation type
     */
    static public Orientation getOrientation(Path imagePath) {
        int value = 1; // Defaut to no rotation if orientation exif not present
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(imagePath.toFile());
            ExifIFD0Directory ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class
            );
            if (ifd0Directory != null && ifd0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                value = ifd0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

            }

        } catch (JpegProcessingException | IOException | MetadataException ex) {
            Logger.getLogger(CLASSNAME).log(Level.SEVERE, null, ex);
        }

        return Orientation.convertValue(value);
    }

    /**
     * Convert output file index number to a proper file path
     *
     * @param index index number
     * @return file path
     */
    private Path convertIndexToDest(int index) {
        String outputFilename = String.format("%06d.jpg", index);
        String subdirectory = getSubDirectory(index);
        try {
            return Paths.get(theConfiguration.getOutputDirectory(subdirectory),
                    outputFilename);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Generate the subdirectory name to put the file in.
     *
     * @param outputFilename filename
     * @return suitable subdirectory name for the file
     */
    private String getSubDirectory(int outputFilenameIndex) {
        Integer roundedNumber = outputFilenameIndex / 100 * 100;
        return String.format("%06d", roundedNumber);
    }

    /**
     * Copy the file without modifying it
     *
     * @param inputfile the input file
     * @param outputFile the output file
     * @return true if successful
     */
    public static boolean copyFile(Path inputfile, Path outputFile) {
        boolean copyOk = true;
        try {
            // Copy the the file to the local directory for speedy processing
            Path parent = outputFile.getParent();
            if (!parent.toFile().exists()) {
                parent.toFile().mkdirs();
            }
            Files.copy(inputfile, outputFile, REPLACE_EXISTING);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.err.println("(Input file \"" + inputfile.toString() + "\")");
            copyOk = false;
        }
        return copyOk;
    }

}
