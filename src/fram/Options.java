package fram;

/**
 * Structure holding command line options
 *
 * @author Jason Leake
 */
class Options {

    public boolean verbose;
    public boolean check;
    public boolean showFilename;
    public boolean norotate;
    public boolean showDate;
    public boolean nodirectory;
    public boolean cache;

    /**
     * Constructor
     *
     * @param inOptions default options setting
     */
    public Options(Options inOptions) {
        verbose = inOptions.verbose;
        cache = inOptions.cache;
        check = inOptions.check;
        showFilename = inOptions.showFilename;
        norotate = inOptions.norotate;
        showDate = inOptions.showDate;
        nodirectory = inOptions.nodirectory;
    }

    /**
     * Default constructor
     */
    public Options() {
        verbose = false;
        check = false;
        showFilename = false;
        norotate = false;
        showDate = false;
        nodirectory = false;
        cache = false;
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
        System.out.print(pad(arg, 20));
        if (arg.equalsIgnoreCase("--verbose")) {
            verbose = true;
            found = true;
            System.out.println("   Describe what is happening");
        } else if (arg.equalsIgnoreCase("--cache")) {
            cache = true;
            found = true;
            System.out.println("   Use cache");
        } else if (arg.equalsIgnoreCase("--check")) {
            check = true;
            found = true;
            System.out.println("   Ony run main processing if number of input files changed");
        } else if (arg.equalsIgnoreCase("--norotate")) {
            norotate = true;
            found = true;
            System.out.println("   Don't rotate output files according to their metadata");
        } else if (arg.equalsIgnoreCase("--showfilenames")) {
            showFilename = true;
            found = true;
            System.out.println("   Annotate images with input filename");
        } else if (arg.equalsIgnoreCase("--date")) {
            showDate = true;
            found = true;
            System.out.println("   Annotate images with date in image file");
        } else if (arg.equalsIgnoreCase("--nodirectory")) {
            nodirectory = true;
            found = true;
            System.out.println("   Do not annotate images with directory name");
        } else if (arg.startsWith("--")) {
            System.out.println("Unrecognised option " + arg);
        }
        else {
            System.out.println();
        }
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

}
