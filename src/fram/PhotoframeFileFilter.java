package fram;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter for image file types. At the moment we just want jpegs.
 *
 * @author Jason Leake
 */
class PhotoframeFileFilter implements FilenameFilter {

    private final Pattern regularExpression = Pattern.compile("^\\d\\d\\d\\d\\d\\d.jpg$");

    /**
     * Test if this filename should be used
     * @param file file to check
     * @param string
     * @return true if this is a valid photoframe output filename
     */
    @Override
    public boolean accept(File file, String string) {
        Matcher matcher = regularExpression.matcher(string);
        return matcher.find();
    }
}
