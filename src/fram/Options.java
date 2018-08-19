package fram;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure holding command line options
 *
 * @author Jason Leake
 */
class Options {

    enum Option {
        VERBOSE, CHECK, SHOW_FILENAME, NO_ROTATE_IMAGES, SHOW_DATE,
        NO_DIRECTORY_NAME, CACHE, MINIMUM_WIDTH;
        private static final Map<String, Option> keywords = new HashMap<>();

        static {
            // These must all be lower case
            keywords.put("--verbose", VERBOSE);
            keywords.put("--check", CHECK);
            keywords.put("--showfilenames", SHOW_FILENAME);
            keywords.put("--norotate", NO_ROTATE_IMAGES);
            keywords.put("--date", SHOW_DATE);
            keywords.put("--nodirectory", NO_DIRECTORY_NAME);
            keywords.put("--cache", CACHE);
            keywords.put("--minimumwidth", MINIMUM_WIDTH);

        }

        private String description() {
            switch (this) {
                case VERBOSE:
                    return "Describe what is happening";
                case CHECK:
                    return "Only run main processing if number of input files changed";
                case SHOW_FILENAME:
                    return "Annotate images with input filename";
                case NO_ROTATE_IMAGES:
                    return "Don't rotate output files according to their metadata";
                case SHOW_DATE:
                case NO_DIRECTORY_NAME:
                    return "Do not annotate images with directory name";
                case CACHE:
                    return "Use cache";
                case MINIMUM_WIDTH:
                    return "Specify minimum width for image";
                default:
                    return "Unknown enum!";
            }
        }

        /**
         * Convert keyword to option enumeration
         *
         * @param keyword keyword to check
         * @return option enumeration, or null if there isn't one
         */
        public static Option keywordToOption(String keyword) {
            keyword = keyword.toLowerCase();
            if (keywords.containsKey(keyword)) {
                return keywords.get(keyword);
            }
            return null;
        }
    }

    private final Map<Option, Integer> options = new HashMap<>();

    /**
     * Constructor
     *
     */
    public Options() {
        options.put(Option.VERBOSE, 0);
        options.put(Option.CHECK, 0);
        options.put(Option.SHOW_FILENAME, 0);
        options.put(Option.NO_ROTATE_IMAGES, 0);
        options.put(Option.SHOW_DATE, 0);
        options.put(Option.NO_DIRECTORY_NAME, 1);
        options.put(Option.CACHE, 0);
        options.put(Option.MINIMUM_WIDTH, 2048);
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

        if (keyword.startsWith("--")) {
            int numericValue = 1;
            if (keyword.contains("=")) {
                String value = arg.replaceAll("^.*=", "");
                if (value.equalsIgnoreCase("T")) {
                    value = "1";

                } else if (value.equalsIgnoreCase("F")) {
                    value = "0";
                }

                try {
                    numericValue = Integer.parseInt(value);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid option value " + value);
                    numericValue = 0;

                }
            }
            Option option = Option.keywordToOption(keyword);
            if (option != null) {
                System.out.print("   " + option.description());
                options.put(option, numericValue);
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
     * Get the specified option value. 1 or 0 for binary options, or a number
     * for ones which hold a numeric value.
     *
     * @param option the option
     * @return value
     */
    public int get(Option option) {
        if (options.containsKey(option)) {
            return options.get(option);

        }
        return 0;
    }
}
