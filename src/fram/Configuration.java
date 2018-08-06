package fram;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stored the configuration for the program run
 *
 * @author Jason Leake
 */
class Configuration {

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
        return options.verbose;
    }

    /**
     * Get the state of the flag which enables or disables checking if the
     * program needs to run
     *
     * @return flag value
     */
    public boolean checkFirst() {
        return options.check;
    }

    /**
     * Set the command line options
     *
     * @param options options
     * @return true always, indicating success
     */
    public boolean setOptions(Options inOptions) {
        options = new Options(inOptions);
        return true;
    }

    /**
     * Interrogate flag for adding filename text to image file
     *
     * @return true if flag set, false if not
     */
    public boolean getShowFilename() {
        return options.showFilename;
    }

    /**
     * Get setting of rotate flag
     *
     * @return true if flag is set
     */
    public boolean norotate() {
        return options.norotate;
    }

    /**
     * Get setting of whether date of photo is to be show
     *
     * @return true if date is to be shown
     */
    public boolean getShowDate() {
        return options.showDate;
    }

    /**
     * Get setting of directory flag
     *
     * @return true if directory is to be shown
     */
    public boolean annotateImageWithDirectory() {
        return !options.nodirectory;
    }

    /**
     * Return true if caching enabled
     *
     * @return true if cache is enabled
     */
    boolean getCache() {
        return options.cache;
    }

}
