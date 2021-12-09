package fram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Check if the main job needs running by seeing if the number of files has
 * changed
 *
 * @author Jason Leake
 */
final public class CheckProgramNeedsRunning implements FileVisitor<Path> {

    private final Configuration theConfiguration;
    private int count = 0;
    private String newText;
    private boolean changedFlag = false;
    private final String checkFile;

    /**
     * Constructor
     *
     * @param configuration program configuration - command line options etc
     * @throws java.io.IOException thrown if getInputDirectory() fails
     */
    public CheckProgramNeedsRunning(Configuration configuration) throws IOException {
        theConfiguration = configuration;
        checkFile = generateName(configuration.getInputDirectory());
    }

    /**
     * Find out if the media files have changed.
     *
     * @return true if the files have changed. It currently just checks if the
     * number of files has changed.
     */
    public boolean changed() {

        System.out.println("See if program needs to regenerate output files");
        try {
            Files.walkFileTree(theConfiguration.getInputPath(), this);
        } catch (IOException ex) {
            Logger.getLogger(CheckProgramNeedsRunning.class.getName()).log(Level.SEVERE, null, ex);
        }

        BufferedReader bufferedReader = null;
        String text = "";
        try {
            bufferedReader = new BufferedReader(new FileReader(checkFile));
            final var stringBuilder = new StringBuilder();
            var line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
            text = stringBuilder.toString();
        } catch (FileNotFoundException ex) {
            System.out.println(String.format("Check file %s not found", checkFile));
        } catch (IOException ex) {
            Logger.getLogger(CheckProgramNeedsRunning.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    Logger.getLogger(CheckProgramNeedsRunning.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (text.isEmpty()) {
            text = "(blank)";
        }
        System.out.println(String.format("old count is %s", text));

        newText = "count = " + count;

        System.out.println(String.format("new count is %s", newText));
        if (!newText.equals(text)) {
            System.out.println("Output files need to be regenerated");
            changedFlag = true;
        } else {
            System.out.println("Output files do not need to be regenerated");
        }
        return changedFlag;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
        count++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Write a new file count file if the total number of files in the tree has
     * changed.
     */
    public void update() {
        if (changedFlag) {
            try {
                final var out = new BufferedWriter(new FileWriter(checkFile));
                out.write(newText + "\n");
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Generate a name for the check file. There may be more than one instance
     * of the program run so we need a unique name. The name is generated from
     * the name of the input directory.
     *
     * @param inputDirectory input directory path
     * @return name corresponding to input directory
     */
    public static String generateName(String inputDirectory) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");

            md.update(inputDirectory.getBytes());
            final var digest = md.digest();
            final var sb = new StringBuilder();
            for (var b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return "check_" + sb.toString() + ".txt";
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CheckProgramNeedsRunning.class.getName()).log(Level.SEVERE, null, ex);
            return "check.txt";
        }
    }

    /**
     * Test if the objet has detected that the input directory has changed. Used
     * for unit tests.
     *
     * @return true if it has changed
     */
    public boolean getChangedFlag() {
        return changedFlag;
    }

}
