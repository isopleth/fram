package fram.filesystem;

import fram.Cache;
import fram.CheckProgramNeedsRunning;
import fram.CmdLineOptions;
import fram.CmdLineOptions.OptionEnum;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process the files
 *
 * @author Jason Leake
 */
public class ProcessFiles {

    private final CmdLineOptions commandLine;
    private Walker walker;
    private CheckProgramNeedsRunning checker = null;

    /**
     * Constructor
     *
     * @param configuration Configuration data
     */
    public ProcessFiles(CmdLineOptions configuration) {
        commandLine = configuration;
    }

    /**
     * Run the processing
     */
    public void run() {
        var runTheMainProgramCode = false;
        if (commandLine.isSet(OptionEnum.CHECK)) {
            try {
                checker = new CheckProgramNeedsRunning(commandLine);
                if (checker.changed()) {
                    runTheMainProgramCode = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(ProcessFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            runTheMainProgramCode = true;
        }

        if (runTheMainProgramCode) {
            try {
                deleteExistingFiles(commandLine.outputDirectoryRoot);
                Cache cache = null;
                if (commandLine.isSet(OptionEnum.CACHE)) {
                    cache = new Cache();
                }
                walker = new Walker(commandLine, cache);
                walker.process(true);
                walker.writeExclusionListFile();
                walker.process(false);
                if (cache != null) {
                    cache.clean();
                    cache.close();
                }
                System.out.println(String.format("Copied %d files, skipped %d files\n",
                        getCopyCount(), getSkippedCount()));
            } catch (IOException ex) {
                Logger.getLogger(ProcessFiles.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (checker != null) {
                checker.update();
            }
        }
    }

    /**
     * Get the number of files copied to the output directory
     *
     * @return number of files
     */
    public int getCopyCount() {
        if (walker != null) {
            return walker.getCopyCount();
        } else {
            return 0;
        }
    }

    /**
     * Get the number of files not copied to the output directory
     *
     * @return number of files
     */
    public int getSkippedCount() {
        if (walker != null) {
            return walker.getSkippedCount();
        } else {
            return 0;
        }
    }

    /**
     * Get the number of directories not copied to the output directory
     *
     * @return number of files
     */
    public int getSkippedDirCount() {
        if (walker != null) {
            return walker.getSkippedDirCount();
        } else {
            return 0;
        }
    }

    /**
     * Delete existing image files
     *
     * @param outputDirectorydirectory containing image files to delete
     */
    private void deleteExistingFiles(String outputDirectory) {
        System.out.println("Deleting old files");
        final var deleteTree = new DeleteTree(outputDirectory);
        deleteTree.process();
        System.out.println(String.format("Deleted %d files", deleteTree.getCount()));
    }

    /**
     * Return CheckProgramNeedsRunning object, for unit tests
     *
     * @return CheckProgramNeedsRunning object
     */
    public CheckProgramNeedsRunning getChecker() {
        if (checker == null) {
            System.out.println("Returning an empty checker");
        }
        return checker;
    }

}
