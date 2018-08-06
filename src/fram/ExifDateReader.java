package fram;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read EXIF data from image file
 *
 * @author Jason Leake
 */
class ExifDateReader {

    private final Path file;

    /**
     * Constructor
     *
     * @param localCopyOfFile
     */
    public ExifDateReader(Path localCopyOfFile) {
        file = localCopyOfFile;
    }

    /**
     * Copy the next file on the copy list
     *
     * @return true if successful, false if list empty
     */
    public String getDate() {
        String date = "";

        try {

            Metadata metadata = JpegMetadataReader.readMetadata(file.toFile());
            Iterable<Directory> directories = metadata.getDirectories();
            for (Directory directory : directories) {

                if (directory != null) {
                    if (directory.containsTag(ExifIFD0Directory.TAG_DATETIME_ORIGINAL)) {
                        date = directory.getString(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
                    } else if (directory.containsTag(ExifIFD0Directory.TAG_DATETIME_DIGITIZED)) {
                        date = directory.getString(ExifIFD0Directory.TAG_DATETIME_DIGITIZED);
                    }
                }
            }
            if (date != null && !date.isEmpty()) {
                // format is yyyy:mm:dd hh:mm:ss
                String fields[] = date.split(":");
                if (fields.length >= 3) {
                    // Day of month has hour at the end of it
                    String splitString[] = fields[2].split(" ");
                    String dayOfMonth = "";
                    if (splitString.length > 1) {
                        dayOfMonth = splitString[0];
                    }
                    date = "" + dayOfMonth + " " + month(fields[1]) + " " + fields[0];

                }
            }

        } catch (JpegProcessingException | IOException ex) {
            Logger.getLogger(ExifDateReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return date;
    }

    /**
     * Convert date field to month name
     *
     * @param field
     * @return month name
     */
    private String month(String field) {
        if (field.startsWith("0")) {
            field = field.replaceFirst("0", "");
        }
        switch (field) {
            case "1":
                return "Jan";
            case "2":
                return "Feb";
            case "3":
                return "Mar";
            case "4":
                return "Apr";
            case "5":
                return "May";
            case "6":
                return "Jun";
            case "7":
                return "Jul";
            case "8":
                return "Aug";
            case "9":
                return "Sep";
            case "10":
                return "Oct";
            case "11":
                return "Nov";
            case "12":
                return "Dec";
            default:
                return "?" + field + "?";
        }
    }

}
