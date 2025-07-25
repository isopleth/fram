package fram.rotation;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used by the unit tests to check image rotations
 *
 * @author Jason Leake
 */
public class RotationCounter {

    private static final Map<Orientation, Integer> rotationCounts = new HashMap<>();

    /**
     * Reset all counts
     */
    public static void reset() {
        rotationCounts.clear();
    }

    /**
     * Increment count for image rotation
     *
     * @param imageOrientation image orientation
     */
    public static void bump(Orientation imageOrientation) {
        if (rotationCounts.containsKey(imageOrientation)) {
            rotationCounts.put(imageOrientation, 
                    rotationCounts.get(imageOrientation) + 1);
        } else {
            rotationCounts.put(imageOrientation, 1);
        }
    }

    /**
     * Get a string describing the rotation counts
     *
     * @return counts as a string - just used for testing
     */
    static public String getRotationCounts() {
        final var counts = new int[Orientation.values().length];
        int index = 0;
        for (var orientation : Orientation.values()) {
            if (rotationCounts.containsKey(orientation)) {
                counts[index++] = rotationCounts.get(orientation);
            } else {
                counts[index++] = 0;
            }
        }
        return makeRotationCounts(counts);
    }

    /**
     * Generate a string containing rotation counts as specified
     *
     * @param counts array of counts
     * @return counts as a string - just used for testing
     */
    static public String makeRotationCounts(int[] counts) {
        if (counts.length != Orientation.values().length) {
            return "bad length";
        }
        var returnValue = "";
        int index = 0;
        for (var orientation : Orientation.values()) {
            if (index > 0) {
                returnValue += " ";
            }
            returnValue += orientation.name().toLowerCase() + " ";
            returnValue += Integer.toString(counts[index++]);

        }
        return returnValue;
    }

}
