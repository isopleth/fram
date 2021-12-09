package fram;

import fram.Options.Option;
import static fram.Options.Option.MINIMUM_WIDTH;
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
            errorMessage.append(
                    String.format("Input directory %s does not exist\n",
                            inputDirectory.getAbsolutePath()));
            return false;
        }
        if (!inputDirectory.canRead()) {
            errorMessage.append(
                    String.format("Input directory %s unreadable\n",
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
        if (!outputDirectory.exists()) {
            errorMessage.append("Output directory ").append(selectedDirectory).
                    append(" does not exist\n");
            return false;
        }
        if (!outputDirectory.canWrite()) {
            errorMessage.append("Output directory ").append(selectedDirectory).
                    append(" unwriteable\n");
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
        final var message = errorMessage.toString();
        errorMessage.setLength(0);
        return message;
    }

    /**
     * Get input directory
     *
     * @return input directory
     * @throws IOException thrown if inputDirectory.getCanonicalPath fails
     */
    public String getInputDirectory() throws IOException {
        return inputDirectory.getCanonicalPath();
    }

    /**
     * Get output directory
     *
     * @return output directory
     * @throws IOException thrown if inputDirectory.getCanonicalPath fails
     */
    public String getOutputDirectory() throws IOException {
        return outputDirectory.getCanonicalPath();
    }

    /**
     * Get output directory
     *
     * @param subdirectory subdirectory name
     * @return output directory corresponding full path name
     * @throws IOException Thrown if outputDirectory.getCanonicalPath fails
     */
    public String getOutputDirectory(String subdirectory) throws IOException {
        return Paths.get(outputDirectory.getCanonicalPath(),
                subdirectory).toString();
    }

    /**
     * Get output directory
     *
     * @return output directory
     */
    public Path getOutputPath() {
        return Paths.get(outputDirectory.getPath());
    }

    /**
     * Get input directory
     *
     * @return input directory
     */
    public Path getInputPath() {
        return Paths.get(inputDirectory.getPath());
    }

    /**
     * Get minimum output image width
     *
     * @return minimum width in pixels
     */
    public int getMinimumWidth() {
        return options.getValue(MINIMUM_WIDTH);
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
     * Test if specified option is set/enabled
     *
     * @param option option to test
     * @return true if option set, false if not set
     */
    public boolean isSet(Option option) {
        return options.isSet(option);
    }

}
