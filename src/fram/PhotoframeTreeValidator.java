package fram;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks that this is a valid photoframe tree before deleting it. This is to
 * prevent putting the wrong directory name into the program from doing too much
 * damage.
 *
 * @author Jason Leake
 */
public class PhotoframeTreeValidator implements FileVisitor<Path> {

    private final Path outputDirectory;
    private boolean isPhotoframeTree;

    /**
     * Constructor
     *
     * @param outputDir proposed output directory for files
     */
    public PhotoframeTreeValidator(Path outputDir) {
        outputDirectory = outputDir;
        System.out.println(String.format("Check tree - %s is the proposed photoframe tree",
                outputDir));
    }

    /**
     * Check if this is a valid photoframe tree
     *
     * @return true if this tree doesn't contain anything which isn't a
     * non-photoframe file
     */
    public boolean verify() {
        try {
            isPhotoframeTree = true;
            Files.walkFileTree(outputDirectory, this);
            return isPhotoframeTree;
        } catch (IOException ex) {
            Logger.getLogger(PhotoframeTreeValidator.class.getName()).
                    log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Called just before any directory is visited. It does nothing.
     *
     * @param dir directory about to be visited
     * @param attrs directory attributes
     * @return CONTINUE always
     * @throws IOException This exception is never thrown
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, 
            BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Check file is valid
     *
     * @param file file to check
     * @param attrs file attributes
     * @return true if it is a valid file
     * @throws IOException on error
     */
    @Override
    public FileVisitResult visitFile(Path file, 
            BasicFileAttributes attrs) throws IOException {
        final var filename = file.getName(file.getNameCount() - 1).toString();
        var ok = false;
        if (filename.matches("\\d+\\.jpg")) {
            ok = true;
        } else if (filename.equals("Thumbs.db")) {
            ok = true;
        }
        if (!ok) {
            System.out.println(filename + " doesn't match pattern");
            isPhotoframeTree = false;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, 
            IOException exc) throws IOException {

        return FileVisitResult.CONTINUE;
    }

    /**
     * Called after each directory is visited. It does nothing.
     *
     * @param dir directory just visited
     * @param exc I/O exception that prevented the file from being visited, if any
     * @return FileVisitResult.CONTINUE always
     * @throws IOException This exception is never thrown
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, 
            IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
