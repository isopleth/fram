package fram;

import java.io.File;
import java.util.logging.Logger;

/**
 * Copies .jpg files onto a memory stick for displaying on a photo frame device.
 *
 * Version history: 
 * 
 * 1.000 - Original
 *
 * 1.001 - Cache and unit tests know about
 *
 * 1.002 - Allow user to set minimum image size
 *
 * @author Jason Leake
 */
public class Fram {

    private static final String VERSION = "1.002";
    private static final Logger logger = Logger.getLogger(Fram.class.getName());
    private ProcessFiles processFiles;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Fram().runProgram(args);
    }

    /**
     * This is the main entry point. It returns a boolean to make testing easier
     *
     * @param args
     * @return true if successful
     */
    public boolean runProgram(String[] args) {
        System.out.println("Program version " + VERSION);
        // This is maintained for the unit tests which use it
        RotationCounter.reset();
        // Guard against two instances of the same program running out of
        // the same directory at the same time
        RunningLock lock = new RunningLock();
        if (lock.alreadyLocked()) {
            System.out.println("Already running");
            return false;
        }
        Options options = new Options();
        // Parse command line
        String input = "";
        String output = "";
        System.out.println();
        try {
            for (String arg : args) {

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

            if (output.isEmpty()) {
                System.out.println("No output directory specified");
                new DoHelp().help();
                return false;
            } else {
                return runMainProgram(input, output, options);
            }
        } finally {
            lock.delete();
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
        ElapsedTime timer = new ElapsedTime();
        System.out.println("Starting at " + DateAndTimeNow.get());

        Configuration configuration = new Configuration();
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
