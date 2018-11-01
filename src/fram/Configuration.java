package fram;

import static fram.Options.Option.CACHE;
import static fram.Options.Option.CHECK;
import static fram.Options.Option.MINIMUM_WIDTH;
import static fram.Options.Option.NO_DIRECTORY_NAME;
import static fram.Options.Option.NO_ROTATE_IMAGES;
import static fram.Options.Option.SHOW_DATE;
import static fram.Options.Option.SHOW_FILENAME;
import static fram.Options.Option.VERBOSE;
import static fram.Options.Option.REMOVE_BORDER;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stored the configuration for the program run
 *
 * @author Jason Leake
 */
public class Configuration {

    private File inputDirectory;
    private File outputDirectory;
    private StringBuilder errorMessage = new StringBuilder();
    private Options options;

    /**
     * Check that the configuration is OK
     *
     * @return true if configuration is OK
     */
    public boolean ok() {
        return (inputDirectory != null
                || inputDirectory.exists()
                && inputDirectory.canRead()
                && outputDirectory.exists()
                && outputDirectory.canWrite()
                && outputDirectory != null);

    }

    /**
     * Set the input directory
     *
     * @param selectedDirectory input directory
     * @return true if input directory set OK
     */
    public boolean setInputDirectory(File selectedDirectory) {

        inputDirectory = selectedDirectory;
        if (!inputDirectory.exists()) {
            errorMessage.append(String.format("Input directory %s does not exist\n",
                    inputDirectory.getAbsolutePath()));
            return false;
        }
        if (!inputDirectory.canRead()) {
            errorMessage.append(String.format("Input directory %s unreadable\n",
                    inputDirectory.getAbsolutePath()));
            return false;
        }
        return true;
    }

    /**
     * Set the output directory
     *
     * @param selectedDirectory input directory
     * @return true if output directory set OK
     */
    public boolean setOutputDirectory(File selectedDirectory) {
        outputDirectory = selectedDirectory;
        if (!inputDirectory.exists()) {
            errorMessage.append(String.format("Output directory %s does not exist\n",
                    inputDirectory.getAbsolutePath()));
            return false;
        }
        if (!outputDirectory.canWrite()) {
            errorMessage.append("Output directory ").append(selectedDirectory).append(" unwriteable\n");
            return false;
        }
        return true;
    }

    /**
     * Get any error message, and clear it
     *
     * @return error message
     */
    public String getErrorMessage() {
        String message = errorMessage.toString();
        errorMessage = new StringBuilder();
        return message;
    }

    /**
     * Get input directory
     *
     * @return input directory
     * @throws IOException
     */
    public String getInputDirectory() throws IOException {
        return inputDirectory.getCanonicalPath();
    }

    /**
     * Get output directory
     *
     * @return output directory
     * @throws IOException
     */
    public String getOutputDirectory() throws IOException {
        return outputDirectory.getCanonicalPath();
    }

    /**
     * Get output directory
     *
     * @return output directory
     * @throws IOException
     */
    public String getOutputDirectory(String subdirectory) throws IOException {
        return Paths.get(outputDirectory.getCanonicalPath(), subdirectory).toString();
    }

    /**
     * Get output directory
     *
     * @return output directory
     * @throws IOException
     */
    public Path getOutputPath() {
        return Paths.get(outputDirectory.getPath());
    }

    /**
     * Get input directory
     *
     * @return input directory
     * @throws IOException
     */
    public Path getInputPath() {
        return Paths.get(inputDirectory.getPath());
    }

    /**
     * Return verbose mode flag
     *
     * @return true if verbose mode, false if quiet mode
     */
    public boolean isVerboseMode() {
        return options.get(VERBOSE) != 0;
    }

    /**
     * Get the state of the flag which enables or disables checking if the
     * program needs to run
     *
     * @return flag value
     */
    public boolean checkFirst() {
        return options.get(CHECK) != 0;
    }

    /**
     * Interrogate flag for adding filename text to image file
     *
     * @return true if flag set, false if not
     */
    public boolean getShowFilename() {
        return options.get(SHOW_FILENAME) != 0;
    }

    /**
     * Get setting of rotate flag
     *
     * @return true if flag is set
     */
    public boolean doNotRotateImages() {
        return options.get(NO_ROTATE_IMAGES) != 0;
    }

    /**
     * Get setting of whether date of photo is to be show
     *
     * @return true if date is to be shown
     */
    public boolean getShowDate() {
        return options.get(SHOW_DATE) != 0;
    }

    /**
     * Get setting of directory flag
     *
     * @return true if directory is to be shown
     */
    public boolean annotateImageWithDirectory() {
        return options.get(NO_DIRECTORY_NAME) != 0;
    }

    /**
     * Return true if caching enabled
     *
     * @return true if cache is enabled
     */
    public boolean getCache() {
        return options.get(CACHE) != 0;
    }

    /**
     * Get minimum output image width
     *
     * @return minimum width in pixels
     */
    public int getMinimumWidth() {
        return options.get(MINIMUM_WIDTH);
    }

    /**
     * Set the options to the specified set of options
     *
     * @param newOptions the new options
     * @return true always, as always successful
     */
    boolean setOptions(Options newOptions) {
        options = newOptions;
        return true;
    }

    /**
     * Remove border around photos
     * @return true if enabled
     */
    public boolean removeBorder() {
        return options.get(REMOVE_BORDER) != 0;
    }

}
