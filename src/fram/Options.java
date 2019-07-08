package fram;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure holding command line options
 *
 * @author Jason Leake
 */
public class Options {

    private static final boolean DEBUG = false;
    
    public enum Option {
        VERBOSE, CHECK, SHOW_INDEX, SHOW_FILENAME, NO_ROTATE_IMAGES, SHOW_DATE,
        NO_DIRECTORY_NAME, REMOVE_BORDER, CACHE, MINIMUM_WIDTH;
    };

    /**
     * This inner class maps the command line string to the option enumeration,
     * and the option enumeration to the textual description of the option along
     * with the default value if it is not specified.
     */
    private static class KnownOptions {

        private final static Map<String, Option> options = new HashMap<>();
        private final static Map<Option, String> descriptions = new HashMap<>();
        private final static Map<Option, Integer> defaultValue = new HashMap<>();
        private final static Map<Option, Boolean> defaultPresent = new HashMap<>();

        /**
         * This is for options that carry a numeric value
         *
         * @param optionString command line option
         * @param option option enumeration
         * @param description text description of option
         * @param defaultVal default value
         */
        private static void put(String optionString,
                Option option,
                String description,
                Integer defaultVal) {
            descriptions.put(option, description);
            options.put(optionString.toLowerCase(), option);
            defaultPresent.put(option, true);
            defaultValue.put(option, defaultVal);
        }

        /**
         * This is for options that are present or not present
         *
         * @param optionString command line option
         * @param option option enumeration
         * @param description text description of option
         * @param defaultPres true if default is that it is present
         */
        private static void put(String optionString,
                Option option,
                String description,
                Boolean defaultPres) {
            descriptions.put(option, description);
            options.put(optionString.toLowerCase(), option);
            defaultPresent.put(option, defaultPres);
        }

        /**
         * Get the text description of the option
         *
         * @param option the option
         * @return text description of the option
         */
        public final String getDescription(Option option) {
            if (descriptions.containsKey(option)) {
                return descriptions.get(option);
            } else {
                // Oops
                return "No description for option " + option + "!";
            }
        }

        public final Option translateOptionString(String optionString) {
            optionString = optionString.toLowerCase();
            if (options.containsKey(optionString)) {
                return options.get(optionString);
            }
            return null;
        }

        /**
         * Check if the default value is to be used for the specified option
         * 
         * @param option option to check
         * @return true if the default value is to be used
         */
        private Boolean getDefaultPresent(Option option) {
            if (defaultPresent.containsKey(option)) {
                return defaultPresent.get(option);
            }
            System.err.println("getDefaultPresent - unknown option " + option);
            return false;
        }

        /**
         * Get the default value for the specified option
         * @param option the option to get the value for
         * @return default value for option
         */
        private Integer getDefaultValue(Option option) {
            if (defaultValue.containsKey(option)) {
                return defaultValue.get(option);
            }
            System.err.println("getDefaultValue- unknown option " + option);
            return 0;
        }

        static private final String DESCR_VERBOSE = "Describe what is happening";
        static private final String DESCR_CHECK = "Only run main processing if number of input files changed";
        static private final String DESCR_SHOW_FILENAME = "Annotate images with input filename";
        static private final String DESCR_NO_DIRECTORY_NAME = "Don't show directory name in annotation";
        static private final String DESCR_SHOW_INDEX = "Show output file index number";
        static private final String DESCR_NO_ROTATE = "Don't rotate output files according to their metadata";
        static private final String DESCR_SHOW_DATE = "Show date that photo was taken or scanned";
        static private final String DESCR_CACHE = "Use cache";
        static private final String DESCR_MIN_WIDTH = "Specify minimum width for image";
        static private final String DESCR_REMOVE_BORDER = "Remove any white border around images";

        static {
            // These are the command line options that are recognised
            put("--cache", Option.CACHE, DESCR_CACHE, false);
            put("--check", Option.CHECK, DESCR_CHECK, false);
            put("--date", Option.SHOW_DATE, DESCR_SHOW_DATE, false);
            put("--minimumWidth", Option.MINIMUM_WIDTH, DESCR_MIN_WIDTH, 5656);
            put("--noDirectory", Option.NO_DIRECTORY_NAME, DESCR_NO_DIRECTORY_NAME, true);
            put("--noRotate", Option.NO_ROTATE_IMAGES, DESCR_NO_ROTATE, false);
            put("--removeBorder", Option.REMOVE_BORDER, DESCR_REMOVE_BORDER, false);
            put("--showFilename", Option.SHOW_FILENAME, DESCR_SHOW_FILENAME, false);
            put("--showIndex", Option.SHOW_INDEX, DESCR_SHOW_INDEX, false);
            put("--verbose", Option.VERBOSE, DESCR_VERBOSE, false);
        }

    }

    static private final KnownOptions KNOWN_OPTIONS = new KnownOptions();

    // These are the settings of the options
    private final Map<Option, Boolean> optionSetting = new HashMap<>();
    private final Map<Option, Integer> optionValues = new HashMap<>();

    /**
     * Constructor
     *
     */
    public Options() {
        // Set to default settings
        for (Option option : Option.values()) {
            optionSetting.put(option, KNOWN_OPTIONS.getDefaultPresent(option));
            if (optionValues.containsKey(option)) {
                optionValues.put(option, KNOWN_OPTIONS.getDefaultValue(option));
            }
        }
    }

    /**
     * Parse command line option
     *
     * @param arg argument to parse
     * @return true if the argument was recognised, false if not or it wasn't an
     * option
     */
    boolean parseOption(String arg) {
        boolean found = false;

        System.out.print(pad(arg, 30));
        String keyword = arg.replaceAll("=.*$", "");

        boolean optionPresent = false;
        Integer numericValue = null;
        if (keyword.startsWith("--")) {
            // Contains a value, which can be the actual value or a T/F
            if (arg.contains("=")) {
                String value = arg.replaceAll("^.*=", "");
                if (value.equalsIgnoreCase("T")) {
                    optionPresent = true;
                } else if (value.equalsIgnoreCase("F")) {
                    optionPresent = false;
                } else {
                    try {
                        numericValue = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid option value " + value);
                        numericValue = 0;
                    }
                }
            } else {
                optionPresent = true;
            }

            Option option = KNOWN_OPTIONS.translateOptionString(keyword);
            if (option != null) {
                System.out.print("   " + KNOWN_OPTIONS.getDescription(option));
                optionSetting.put(option, optionPresent);
                if (numericValue != null) {
                    optionValues.put(option, numericValue);
                }
                found = true;
            } else {
                System.out.print("    Unrecognised option " + arg);
            }
        }

        System.out.println();
        return found;
    }

    /**
     * Pad string
     *
     * @param arg string to pad
     * @param desiredLength length to pad to
     * @return padded string
     */
    private String pad(String arg, int desiredLength) {
        String output = arg;
        desiredLength = Math.abs(desiredLength);

        while (output.length() < desiredLength) {
            output += " ";
        }
        return output;
    }

    /**
     * Get the specified option logical value
     *
     * @param option the option
     * @return true if the option is set, false if it is not
     */
    public boolean isSet(Option option) {
        boolean set =  optionSetting.get(option);
        if (DEBUG && optionSetting.get(Option.VERBOSE)) {
            System.out.println(option + " value is " + set);
        }
        return set;
    }

    /**
     * Get the numeric value of an option
     *
     * @param option option to get value for
     * @return option value
     */
    int getValue(Option option) {
        if (!optionValues.containsKey(option)) {
            // Option value not present, so return the default value
            return KNOWN_OPTIONS.getDefaultValue(option);
        }
        return optionValues.get(option);
    }
    
    /**
     * Check internal consistency of options
     * 
     * @return true if options consistent, false on error
     */
    boolean checkOptionsConsistent() {
        if (isSet(Option.CACHE) && isSet(Option.SHOW_INDEX)) {
            // Show index disables the cache because the images generated have
            // to contain the index number for this particular run, and these
            // will be different in other runs since they are randomly generated
             optionSetting.put(Option.CACHE, false);
            System.out.println("Clearing --cache because --showIndex is present");
        }
        return true;
    }
    
}
