package fram.filesystem;

import fram.Cache;
import fram.CheckProgramNeedsRunning;
import fram.Configuration;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process the files
 *
 * @author Jason Leake
 */
public class ProcessFiles {

    private final Configuration theConfiguration;
    private Walker walker;
    private CheckProgramNeedsRunning checker = null;

    /**
     * Constructor
     *
     * @param configuration Configuration data
     */
    public ProcessFiles(Configuration configuration) {
        theConfiguration = configuration;
    }

    /**
     * Run the processing
     */
    public void run() {
        boolean runTheMainProgramCode = false;
        if (theConfiguration.checkFirst()) {
            try {
                checker = new CheckProgramNeedsRunning(theConfiguration);
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
                deleteExistingFiles(theConfiguration.getOutputDirectory());
                Cache cache = null;
                if (theConfiguration.getCache()) {
                    cache = new Cache();
                }
                walker = new Walker(theConfiguration, cache);
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
        DeleteTree deleteTree = new DeleteTree(outputDirectory);
        deleteTree.process();
        System.out.println(String.format("Deleted %d files", deleteTree.getCount()));
    }

    /**
     * Return CheckProgramNeedsRunning object, for unit tests
     *
     * @return CheckProgramNeedsRunning object
     */
    public CheckProgramNeedsRunning getChecker() {
        return checker;
    }

}
