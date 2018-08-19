package fram;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test basic operation
 *
 * @author Jason Leake
 */
public class FramTest {

    private final String inputDirectory;
    private final String outputDirectory;
    private boolean testHasBeenRun = false;

    public FramTest() {
        inputDirectory = System.getProperty("user.dir") + File.separator
                + "test_data" + File.separator + "testInput";
        outputDirectory = System.getProperty("user.dir") + File.separator
                + "test_data" + File.separator + "testOutput";
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        File outputDir = new File(outputDirectory);
        if (outputDir.exists()) {
            delete(outputDir);
        }
        assertTrue(String.format("Create %s", outputDir.getAbsolutePath()),
                outputDir.mkdirs());

        if (testHasBeenRun) {
            assertTrue("Delete output directory", delete(outputDir));

            String runDirectory = System.getProperty("user.dir");
            File copyList = new File(runDirectory, "copy_list.txt");
            File exclusionList = new File(runDirectory, "exclusion_list.txt");
            assertTrue("delete copy list - " + copyList.getAbsolutePath(),
                    copyList.delete());
            assertTrue("delete exclusion list - " + exclusionList.getAbsolutePath(),
                    exclusionList.delete());

            File cacheDirectory = new File("framcache");
            assertTrue("cache directory", delete(cacheDirectory));
        }
    }

    @After
    public void tearDown() {

    }

    /**
     * Delete a file or directory
     *
     * @param fileToDelete file (or directory) to delete
     * @return true if successful, false if not
     */
    boolean delete(File fileToDelete) {
        if (fileToDelete.isDirectory()) {
            for (File file : fileToDelete.listFiles()) {
                if (!delete(file)) {
                    return false;
                }
            }
        }
        if (!fileToDelete.delete()) {
            System.out.println("Failed to delete file: " + fileToDelete);
            return false;
        }
        return true;
    }

    /**
     * Run the main method of the program
     *
     * @param args arguments to pass to method
     */
    private void run(String[] args) {
        try {
            assertTrue("Run program", new Fram().runProgram(args));
            File outputDir = new File(outputDirectory);
            // Check that the newly created directory exists
            File createdDir = new File(outputDir, "000000");
            assertTrue(String.format("%s created", createdDir.getAbsolutePath()),
                    createdDir.exists());
            assertTrue(String.format("%s is a directory", createdDir.getAbsolutePath()),
                    createdDir.isDirectory());
            File[] listOfFiles = createdDir.listFiles();

            // Check all of the expected output files are present.
            Set<String> expectedfiles = new HashSet<>();
            for (int index = 0; index < 10; index++) {
                expectedfiles.add(String.format("%06d.jpg", index));
            }

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    assertTrue(String.format("%s exists", file.getName()),
                            expectedfiles.contains(file.getName()));
                    expectedfiles.remove(file.getName());
                }
            }
            assertTrue("No missing files", expectedfiles.isEmpty());

            // Now remove the output files next time we run a test
            testHasBeenRun = true;
        } catch (Exception ex) {
            assertTrue(ex.getLocalizedMessage(), false);
            ex.printStackTrace();
        }
    }

    /**
     * check the rotations are as expected
     *
     * @param counts array of expected rotation for each orientation change
     */
    private void checkRotations(int[] counts) {
        String actualRotations = RotationCounter.getRotationCounts();
        String expectedRotations = RotationCounter.makeRotationCounts(counts);
        if (!actualRotations.equals(expectedRotations)) {
            System.out.println("Actual: " + actualRotations);
            System.out.println("Expected: " + expectedRotations);

        }
        assertTrue("Rotations as expected", actualRotations.equals(expectedRotations));

    }

    /**
     * Print title of test in slightly fancy style
     *
     * @param title title of test
     */
    private void announce(String title) {
        System.out.println();
        System.out.println();
        for (int count = 0; count < title.length(); count++) {
            System.out.print("-");

        }
        System.out.println();
        System.out.println(title);
        for (int count = 0; count < title.length(); count++) {
            System.out.print("-");

        }
        System.out.println();
    }

    /**
     * Print minor announcement in slightly fancy style
     *
     * @param title title of test
     */
    private void subAnnounce(String title) {
        System.out.println();
        System.out.println(title);
        for (int count = 0; count < title.length(); count++) {
            System.out.print("-");

        }
        System.out.println();
    }

    /**
     * Test with cache enabled
     */
    @Test
    public void testCache() {
        announce("test cache");
        Cache.deleteCache();
        subAnnounce("Run program with caching but no cache");
        run(new String[]{inputDirectory, outputDirectory, "--verbose", "--cache"});
        int[] counts = {9, 0, 0, 0, 1, 0, 0, 0, 0};
        checkRotations(counts);
        subAnnounce("Run program with caching but cache exists");
        run(new String[]{inputDirectory, outputDirectory, "--verbose", "--cache"});
        // Nothing will be rotated because the files all come from the cache
        int[] counts1 = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        checkRotations(counts1);
        subAnnounce("Test cache clean");
        announce("test cache clean");
        Cache cache = new Cache();
        cache.createTestFile();
        cache.createTestEntry();
        cache.clean();
        cache.close();
    }

    /**
     * Test without cache enabled
     */
    @Test
    public void testNoCache() {
        announce("test no cache");
        // Run program without caching
        run(new String[]{inputDirectory, outputDirectory, "--verbose"});
        int[] counts = {9, 0, 0, 0, 1, 0, 0, 0, 0};
        checkRotations(counts);
    }

    /**
     * Test checking if the output directory needs regenerating
     */
    @Test
    public void testCheckOption() {
        announce("test check output directory needs regenerating");
        subAnnounce("Delete check file, if it exists");
        String name = CheckProgramNeedsRunning.generateName(inputDirectory);
        File checkFile = new File(System.getProperty("user.dir"), name);
        checkFile.delete();

        subAnnounce("Run program. Files will be regenerated since nothing to compare against");
        Fram fram = new Fram();
        fram.runProgram(new String[]{inputDirectory, outputDirectory, "--verbose", "--check"});
        assertTrue("Renegerated files", fram.getProcessor().getChecker().getChangedFlag());

        subAnnounce("Run program again. Should not need regenerating");
        fram = new Fram();
        fram.runProgram(new String[]{inputDirectory, outputDirectory, "--verbose", "--check"});
        assertFalse("Renegerated files", fram.getProcessor().getChecker().getChangedFlag());
    }

    /**
     * Test checking if the output directory needs regenerating
     */
    @Test
    public void testMinimumWidthOption() {
        announce("Test \"minimum width\" option");
        subAnnounce("Delete check file, if it exists");
        String name = CheckProgramNeedsRunning.generateName(inputDirectory);
        File checkFile = new File(System.getProperty("user.dir"), name);
        checkFile.delete();

        subAnnounce("Run program. Files will be regenerated since nothing to compare against");
        Fram fram = new Fram();
        fram.runProgram(new String[]{inputDirectory, outputDirectory, "--verbose",
            "--check", "--minimumwidth=1024"});
        assertTrue("Renegerated files", fram.getProcessor().getChecker().getChangedFlag());

        checkFile.delete();
        subAnnounce("Run program again. Should be regenerated");
        fram = new Fram();
        fram.runProgram(new String[]{inputDirectory, outputDirectory, "--verbose",
            "--check", "--minimumwidth=4024"});
        assertTrue("Renegerated files", fram.getProcessor().getChecker().getChangedFlag());
    }

}
