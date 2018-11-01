package fram;

import java.util.HashSet;
import java.util.Set;

/**
 * Get the next output filename
 *
 * @author Jason Leake
 */
public class OutputFileIndexGenerator {

    private final Set<Integer> usedIndices = new HashSet<>();
    private int totalFiles = 0;
    private final int MAX_FILES = 1_000_000;

    /**
     * Get next filename to use
     *
     * @return next filename
     */
    public int getNextfilenameToUse() {

        if (totalFiles++ >= MAX_FILES) {
            System.out.println("Cannot handle more than a million files");
            System.exit(0);
        }

        int number;
        // Randomize the order of the photographs
        int randomNumber = getRandom();
        int countdown = 30;
        while (usedIndices.contains(randomNumber)) {
            if (countdown-- == 0) {

                while (usedIndices.contains(randomNumber)) {
                    randomNumber++;
                    if (randomNumber >= MAX_FILES) {
                        randomNumber = 0;
                    }
                }
            } else {
                randomNumber = getRandom();
            }
        }
        usedIndices.add(randomNumber);
        number = randomNumber;

        return number;
    }

    /**
     * Get a random integer in the range 0->max number of files supported
     *
     * @return random integer
     */
    private int getRandom() {
        return (int) (Math.random() * MAX_FILES);
    }

}
