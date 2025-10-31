package fram;

import java.io.IOException;
import static java.lang.System.exit;
import org.apache.commons.cli.help.HelpFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Structure holding command line options
 *
 * @author Jason Leake
 */
public class CmdLineOptions {

    public final String inputDirectoryRoot;
    public final String outputDirectoryRoot;
    public final String extraExclusionFile;

    private Set<OptionEnum> optionsSet = new HashSet<>();
    private static final boolean DEBUG = false;
    private Options options = new Options();
    private CommandLine cmd;
    int minimumWidth = 900;

    private void put(Options options, Map<Option, OptionEnum> optionMapping, Option option, OptionEnum optionEnum) {
        options.addOption(option);
        optionMapping.put(option, optionEnum);
    }

    public int getMinimumWidth() {
        return minimumWidth;
    }

    /**
     * Command line options
     * {@link #VERBOSE}
     * {@link #CHECK}
     * {@link #SHOW_INDEX}
     * {@link #SHOW_FILENAME}
     * {@link #NO_ROTATE_IMAGES}
     * {@link #SHOW_DATE}
     * {@link #NO_DIRECTORY_NAME}
     * {@link #REMOVE_BORDER}
     * {@link #CACHE}
     * {@link #MINIMUM_WIDTH}
     */
    public enum OptionEnum {
        /**
         * Verbose logging
         */
        VERBOSE,
        /**
         * Check if program needs rerunning before starting major processing
         */
        CHECK,
        /**
         * An additional image exclusion filename has been specified
         */
        EXTRA_EXCLUSION_FILE,
        /**
         * Print index number on each photo
         */
        SHOW_INDEX,
        /**
         * Print filename on each photo
         */
        SHOW_FILENAME,
        /**
         * Don't rotate any images
         */
        NO_ROTATE_IMAGES,
        /**
         * Show date metadata on each photo
         */
        SHOW_DATE,
        /**
         * Don't show directory name on photos
         */
        NO_DIRECTORY_NAME,
        /**
         * Remove borders from photos. This has not been properly implemented
         * yet.
         */
        REMOVE_BORDER,
        /**
         * Use cache for unchanged photos to reduce processing time
         */
        CACHE,
        /**
         * Override default minimum photo width
         */
        MINIMUM_WIDTH;
    };

    /**
     * Constructor
     *
     * @param args Command line arguments
     */
    public CmdLineOptions(String[] args) {

        String inputDirectory = "";
        String outputDirectory = "";
        String extraExclusionName = "";

        // These are the command line options that are recognised
        var cacheOption = Option.builder("c").longOpt("cache").desc("Use cache").get();
        var checkOption = Option.builder("e").longOpt("check").desc("Only run main processing if number of input files changed").get();
        var dateOption = Option.builder("d").longOpt("date").desc("Show date that photo was taken or scanned").get();
        var minimumWidthOption = Option.builder("m").longOpt("minimumWidth").desc("Specify minimum width for image").
                hasArg().argName("minimumWidth").get();
        var noDirectoryOption = Option.builder("n").longOpt("noDirectory").desc("Don't show directory name in annotation").get();
        var noRotateOption = Option.builder("d").longOpt("noRotate").desc("Don't rotate output files according to their metadata").get();
        var removeBorderOption = Option.builder("r").longOpt("removeBorder").desc("Remove any white border around images").get();
        var showFilenameOption = Option.builder("f").longOpt("showFilename").desc("Annotate images with input filename").get();
        var showIndexOption = Option.builder("i").longOpt("showIndex").desc("Show output file index number").get();
        var verboseOption = Option.builder("v").longOpt("verbose").desc("Describe what is happening").get();
        var helpOption = Option.builder("h").longOpt("help").desc("Show help").get();
        var extraExclusionFileOption = Option.builder("x").longOpt("extraExclusion").desc("Recognise extra directory exclusion files").
                hasArg().argName("exclusionFile").get();

        Map<Option, OptionEnum> optionMapping = new HashMap<>();

        put(options, optionMapping, cacheOption, OptionEnum.CACHE);
        put(options, optionMapping, checkOption, OptionEnum.CHECK);
        put(options, optionMapping, dateOption, OptionEnum.SHOW_DATE);
        put(options, optionMapping, extraExclusionFileOption, OptionEnum.EXTRA_EXCLUSION_FILE);
        put(options, optionMapping, minimumWidthOption, OptionEnum.MINIMUM_WIDTH);
        put(options, optionMapping, noDirectoryOption, OptionEnum.NO_DIRECTORY_NAME);
        put(options, optionMapping, noRotateOption, OptionEnum.NO_ROTATE_IMAGES);
        put(options, optionMapping, removeBorderOption, OptionEnum.REMOVE_BORDER);
        put(options, optionMapping, showFilenameOption, OptionEnum.SHOW_FILENAME);
        put(options, optionMapping, showIndexOption, OptionEnum.SHOW_INDEX);
        put(options, optionMapping, verboseOption, OptionEnum.VERBOSE);

        CommandLineParser parser = new DefaultParser();
        var formatter = HelpFormatter.builder().get();
        try {
            cmd = parser.parse(options, args);
            // Fetch options
            if (cmd.hasOption(helpOption)) {
                formatter.printHelp("fram", null, options, null, true);
            }

            options.getOptions().stream().filter(option -> (cmd.hasOption(option))).forEachOrdered(option -> {
                optionsSet.add(optionMapping.get(option));
            });

            String[] remainingArgs = cmd.getArgs();

            if (remainingArgs.length < 2) {
                System.out.println("Error: Two positional parameters are required.");
                formatter.printHelp("fram", null, options, null, true);
                exit(1);
            }

            if (remainingArgs.length < 2) {

                inputDirectory = "";
                outputDirectory = "";
            } else {
                inputDirectory = remainingArgs[0];
                outputDirectory = remainingArgs[1];
            }
            // Fetch remaining arguments
            for (String arg : cmd.getArgList()) {
                System.out.println("Additional argument: " + arg);
            }

            if (cmd.hasOption(minimumWidthOption)) {
                minimumWidth = Integer.parseInt(cmd.getParsedOptionValue(minimumWidthOption));
                if (minimumWidth < 50) {
                    minimumWidth = 50;
                }
            }

            if (cmd.hasOption(extraExclusionFileOption)) {
                extraExclusionName = cmd.getParsedOptionValue(extraExclusionFileOption);
            } else {
                extraExclusionName = "";
            }

        } catch (ParseException e) {
            System.out.println("Error parsing command-line arguments: " + e.getMessage());
            exit(1);
        } catch (IOException e) {
            System.out.println("I/O exception processing command-line arguments: " + e.getMessage());
            exit(1);
        } finally {
            inputDirectoryRoot = inputDirectory;
            outputDirectoryRoot = outputDirectory;
            extraExclusionFile = extraExclusionName;
        }

    }

    /**
     * Get the specified option logical value
     *
     * @param option the option
     * @return true if the option is set, false if it is not
     */
    public boolean isSet(OptionEnum option) {
        var set = optionsSet.contains(option);
        if (DEBUG && optionsSet.contains(OptionEnum.VERBOSE)) {
            System.out.println(option + " value is " + set);
        }
        return set;
    }

    /**
     * Check internal consistency of options
     *
     * @return true if options consistent, false on error
     */
    boolean checkOptionsConsistent() {
        if (isSet(OptionEnum.CACHE) && isSet(OptionEnum.SHOW_INDEX)) {
            // Show index disables the cache because the images generated have
            // to contain the index number for this particular run, and these
            // will be different in other runs since they are randomly generated

            optionsSet.remove(OptionEnum.CACHE);
            System.out.println("Clearing --cache because --showIndex is present");
        }
        return true;
    }

}
