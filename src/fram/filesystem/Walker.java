package fram.filesystem;

import fram.Cache;
import fram.Configuration;
import fram.DateAndTimeNow;
import fram.Options.Option;
import fram.OutputFileIndexGenerator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This walks the directory tree containing the files and processes them. It is
 * used twice - once to build a list of files to process and the second time it
 * actually processes them.
 *
 * @author Jason Leake
 */
class Walker implements FileVisitor<Path> {

    private boolean buildTheList;
    private boolean foundExcludeDirectoryFlag = false;
    private final Set<Path> excludeDirectoryList = new HashSet<>();
    private final Set<Path> excludeFileList = new HashSet<>();
    private final FileCopier fileCopier;

    private int copyCount = 0;
    private int skippedCount = 0;
    private int skippedDirCount = 0;
    private final OutputFileIndexGenerator outputFileIndexGenerator;
    private final Configuration theConfiguration;

    private final static String EXCLUDE_WHOLE_DIRECTORY_FILENAME
            = "_photoframe_exclude.txt";
    private final static String EXCLUDE_SPECIFIC_FILES_FILENAME
            = "_photoframe_exclude_list.txt";

    private final static String CLASSNAME = Walker.class.getName();

    /**
     * Constructor
     *
     * @param configuration command line options etc
     * @param cache file cache
     */
    public Walker(Configuration configuration, Cache cache) {
        theConfiguration = configuration;
        outputFileIndexGenerator = new OutputFileIndexGenerator();
        fileCopier = new FileCopier(configuration, 
                cache, configuration.isSet(Option.VERBOSE));
    }

    /**
     * Process the files
     *
     * @param buildTheList true to build the list of files to copy, false to
     * copy them
     * @throws IOException
     */
    public void process(boolean buildList) throws IOException {
        this.buildTheList = buildList;
        if (buildTheList) {
            excludeDirectoryList.clear();
            System.out.println("Scanning directory tree");
            Files.walkFileTree(theConfiguration.getInputPath(), this);
        } else {
            System.out.println("Copying files");
            Files.walkFileTree(theConfiguration.getInputPath(), this);
            fileCopier.compactOutputFiles();
            while (fileCopier.copy()) {
                if (++copyCount % 100 == 0) {
                    System.out.println(String.format("%d files copied at %s",
                            copyCount, DateAndTimeNow.getNewline()));
                }
            }
        }
    }

    /**
     * Called when a directory is about to be entered. Checks if the directory
     * is to be excluded.
     *
     * @param directory directory to check
     * @param attrs basic file attributes
     * @return CONTINUE always as always want to search whole tree to maintain
     * count of excluded files
     * @throws IOException
     */
    @Override
    public FileVisitResult preVisitDirectory(Path directory,
            BasicFileAttributes attrs) throws IOException {
        if (excludeDirectoryList.contains(directory)) {
            if (!buildTheList) {
                skippedDirCount++;
            }
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Action to perform when specified file is visited
     *
     * @param file the file
     * @param attrs file attributes
     * @return CONTINUE always
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (buildTheList) {
            // If we are building the list then ignore the file unless it is
            // one of the exclusion list files
            if (file.getFileName().toString().equals(EXCLUDE_WHOLE_DIRECTORY_FILENAME)) {
                // Add this directory and all its sub-directories
                Files.walkFileTree(file.getParent(), new DirectoryAdder(excludeDirectoryList));
            } else if (file.getFileName().toString().equals(EXCLUDE_SPECIFIC_FILES_FILENAME)) {
                readExcludefiles(file);
            }
        } else {
            // Otherwise process the file.  Ignore anything which isn't a .jpg
            String filename = file.getFileName().toString();
            if (filename.toLowerCase().endsWith(".jpg")) {
                if (foundExcludeDirectoryFlag) {
                    // Skipping all files in this directory
                    skippedCount++;
                } else if (excludeFileList.contains(file)) {
                    // Explicitly skipping this file
                    System.out.println(String.format("Skipped %s", file.toString()));
                    skippedCount++;
                } else {
                    fileCopier.addAnotherFile(outputFileIndexGenerator.getNextfilenameToUse(), file);
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Called when file access failed
     *
     * @param file filename
     * @param exc exception which occurred
     * @return what the search should do - CONTINUE always
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Called when directory visit has been completed
     *
     * @param directory directory being visited
     * @param exc exception
     * @return result of visit
     * @throws IOException
     */
    @Override
    public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
        if (foundExcludeDirectoryFlag) {
            System.out.println(String.format("Skipped directory %s", directory.toString()));
            foundExcludeDirectoryFlag = false;
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Read the files which need to be excluded from the files copied. This list
     * is help in the specified file. current directory
     *
     * @param file exclusion list
     */
    private void readExcludefiles(Path file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file.toFile()));
            String line = reader.readLine();
            while (line != null) {
                Path dir = file.getParent();
                Path skipFile = Paths.get(dir.toString(), line);
                excludeFileList.add(skipFile);
                System.out.println(String.format("Skip file %s", skipFile.toString()));
                line = reader.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(CLASSNAME).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(CLASSNAME).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get the number of files copied
     *
     * @return number of files copied
     */
    public int getCopyCount() {
        return copyCount;
    }

    /**
     * Get the number of files not copied to the output directory
     *
     * @return number of files
     */
    public int getSkippedCount() {
        return skippedCount;
    }

    /**
     * Get the number of directories not copied to the output directory
     *
     * @return number of directories
     */
    public int getSkippedDirCount() {
        return skippedDirCount;
    }

    /**
     * Write the exclusion list to a file
     */
    public void writeExclusionListFile() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("exclusion_list.txt"));
            SortedSet<Path> sortedSet = new TreeSet<>();
            sortedSet.addAll(excludeDirectoryList);
            for (Path path : sortedSet) {
                out.write("directory " + path.toString() + "\n");
            }

            sortedSet.clear();
            sortedSet.addAll(excludeFileList);
            for (Path path : sortedSet) {
                out.write("file " + path.toString() + "\n");
            }

            out.close();
        } catch (IOException e) {
        }

    }

}
