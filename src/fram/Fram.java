package fram;

import fram.filesystem.ProcessFiles;
import fram.rotation.RotationCounter;
import java.io.File;
import java.util.Locale;

/**
 * Annotate photographs for display in a digital photoframe.
 * 
 * This is a program for preparing the files for a digital photo
 * frame. These devices show a stream of photographs loaded from a memory
 * stick, changing the photo every few seconds or minutes. The simpler
 * ones just read the memory stick and display the photos, whilst the
 * more complicated ones are based on tablets.
 * 
 * Version history:
 *
 * 1.000 - Original
 * 1.001 - Cache and unit tests know about
 * 1.002 - Allow user to set minimum image size
 * 1.003 - Add some changes for detecting borders around photos
 * 1.004 - Fix --showFilename, display command line
 * 1.005 - Add --showIndex
 * 1.006 - Clear --cache if --showIndex is set
 * 1.007 - Display heap size when program runs.  Start migration to JDK 11
 * 1.008 - Delete lock file if more than a month old
 * 1.009 - Tidying up the code
 *
 * @author Jason Leake
 * 
 * To compile and test the code:
 * 
 * ant test
 * 
 * Then check the output in test_data/testOutput
 */
public class Fram {

    private static final String VERSION = "1.009";
    private ProcessFiles processFiles;

    /**
     * The command line options
     */
    public Options options = new Options();

    /**
     * Entry point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Fram().runProgram(args);
    }

    /**
     * This is the main entry point. It returns a boolean to make testing easier
     *
     * @param args the command line arguments
     * @return true if successful
     */
    public boolean runProgram(String[] args) {
        System.out.println("Program version " + VERSION);

        System.out.println();
	System.out.println(String.format(Locale.ROOT, "Max heap size is %d bytes",
					 Runtime.getRuntime().maxMemory()));

        boolean first = true;
        for (String arg : args) {
            if (first) {
                System.out.print("Arguments:");
                first = false;
            }

            System.out.print(" ");
            System.out.print(arg);
        }
        System.out.println();

        // This is maintained for the unit tests which use it
        RotationCounter.reset();
        // Guard against two instances of the same program running out of
        // the same directory at the same time
        final var lock = new RunningLock();
        if (lock.alreadyLocked()) {
            System.out.println("Already running");
            return false;
        }
        // Parse command line
        String input = "";
        String output = "";
        System.out.println();

        try {
            for (var arg : args) {

                if (options.parseOption(arg)) {
                } else if (input.isEmpty()) {
                    input = arg;
                } else if (output.isEmpty()) {
                    output = arg;
                } else {
                    new DoHelp().help();
                    return false;
                }
            }

            // Check for internal consistency of options
            if (!options.checkOptionsConsistent()) {
                System.out.println("Terminating program");
                return false;
            }

            if (output.isEmpty()) {
                System.out.println("No output directory specified");
                new DoHelp().help();
                return false;
            }

            return runMainProgram(input, output, options);

        } finally {
            lock.deleteLock();
        }
    }

    /**
     * Run the processing.
     *
     * @param input input directory
     * @param output output directory
     * @param verbose flag indicating whether additional debug information is to
     * be displayed
     * @param check flag indicating whether program should check if it needs
     * running first
     * @param showIndex show index number on image
     */
    private boolean runMainProgram(String input, String output,
            Options options) {
        final var timer = new ElapsedTime();
        System.out.println();
        System.out.println("Starting at " + DateAndTimeNow.get());

        final var configuration = new Configuration();
        boolean ok = configuration.setInputDirectory(new File(input))
                && configuration.setOutputDirectory(new File(output))
                && configuration.setOptions(options);
        System.out.print(configuration.getErrorMessage());
        if (ok) {
            processFiles = new ProcessFiles(configuration);
            processFiles.run();
            System.out.println("Files copied:    " + processFiles.getCopyCount()
                    + "   Files skipped:    " + processFiles.getSkippedCount()
                    + "   Directory trees skipped:    " + processFiles.getSkippedDirCount());

            System.out.println("Rotations - " + RotationCounter.getRotationCounts());
            System.out.println("Finishing at " + DateAndTimeNow.get());
            timer.reportElapsedTime("Complete");
        } else {
            System.out.println("Configuration not OK");
        }
        return ok;
    }

    /**
     * Get file processor, for unit tests
     *
     * @return file processor
     */
    public ProcessFiles getProcessor() {
        return processFiles;
    }
}
