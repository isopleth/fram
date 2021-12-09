package fram;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read EXIF data from image file
 *
 * @author Jason Leake
 */
public class ExifDateReader {

    private final Path file;

    /**
     * Constructor
     *
     * @param localCopyOfFile path of local copy of the file
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
        var date = "";

        try {

            final var metadata = JpegMetadataReader.readMetadata(file.toFile());
            final var directories = metadata.getDirectories();
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
                final var fields = date.split(":");
                if (fields.length >= 3) {
                    // Day of month has hour at the end of it
                    final var splitString = fields[2].split(" ");
                    var dayOfMonth = "";
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

    
    private static final Map<String, String> months = Map.ofEntries(Map.entry("1", "Jan"),
								    Map.entry("2", "Feb"),
								    Map.entry("3", "Mar"),
								    Map.entry("4", "Apr"),
								    Map.entry("5", "May"),
								    Map.entry("6", "Jun"),
								    Map.entry("7", "Jul"),
								    Map.entry("8", "Aug"),
								    Map.entry("9", "Sep"),
								    Map.entry("10", "Oct"),
								    Map.entry("11", "Nov"),
								    Map.entry("12", "Dec"));
    
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
	var monthName = months.get(field);
	if (monthName == null) {
	   monthName = "?" + field + "?";
	}
	return monthName;
    }
}
