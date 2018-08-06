package fram;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

/**
 * Add all directories - the current one, and below the current one to a set
 *
 * @author Jason Leake
 */
class DirectoryAdder implements FileVisitor<Path> {

    private final Set<Path> excludeDirectoryList;

    /**
     * Constructor
     *
     * @param excludeDirectoryList directory list
     */
    public DirectoryAdder(Set<Path> excludeList) {
        excludeDirectoryList = excludeList;
    }

    /**
     *
     * @param directory
     * @param bfa
     * @return CONTINUE always
     * @throws IOException
     */
    @Override
    public FileVisitResult preVisitDirectory(Path directory,
            BasicFileAttributes bfa) throws IOException {
        excludeDirectoryList.add(directory);
        System.out.println(String.format("Skip directory %s", directory.toFile().getAbsolutePath()));
        return CONTINUE;
    }

    /**
     * Called for each file visited - does nothing
     *
     * @param file path to file
     * @param bfa basic file attributes
     * @return CONTINUE always
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes bfa) throws IOException {
        return CONTINUE;
    }

    /**
     * Called for each file visited if the visit fails - does nothing
     *
     * @param file path to file
     * @param bfa basic file attributes
     * @return CONTINUE always
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFileFailed(Path file,
            IOException ioe) throws IOException {
        return CONTINUE;
    }

    /**
     * Called after a directory has been visited
     *
     * @param file path to file
     * @param bfa basic file attributes
     * @return CONTINUE always
     * @throws IOException
     */
    @Override
    public FileVisitResult postVisitDirectory(Path directory,
            IOException ioe) throws IOException {
        return CONTINUE;
    }

}
