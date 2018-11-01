package fram.filesystem;

import fram.PhotoframeTreeValidator;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delete specified file tree
 *
 * @author Jason Leake
 */
class DeleteTree implements FileVisitor<Path> {

    private int count = 0;
    private final Path outputDirectory;

    /**
     * Constructor
     *
     * @param outputDir tree delete
     */
    DeleteTree(String outputDir) {
        outputDirectory = Paths.get(outputDir);
        System.out.println("Delete tree " + outputDir);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        count++;
        file.toFile().delete();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

        if (!dir.equals(outputDirectory)) {
            count++;
            dir.toFile().delete();
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Delete the tree
     */
    public void process() {
        try {
            if (new PhotoframeTreeValidator(outputDirectory).verify()) {
                Files.walkFileTree(outputDirectory, this);
            }
            else {
                System.out.println("NOT deleting output tree since not certain it is a photoframe tree");
            }
        } catch (IOException ex) {
            Logger.getLogger(DeleteTree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Return the number of files deleted
     *
     * @return number of files deleted
     */
    public int getCount() {
        return count;
    }

}
