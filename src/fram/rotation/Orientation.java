package fram.rotation;

/**
 * Enum for image rotation
 *
 * @author Jason Leake
 */
public enum Orientation {
    NONE,
    CLOCKWISE,
    ANTICLOCKWISE,
    MIRROR_LEFT_RIGHT,
    UPSIDE_DOWN,
    MIRROR_TOP_BOTTOM,
    CLOCKWISE_AND_MIRROR,
    ANTICLOCKWISE_AND_MIRROR,
    UNDEFINED;

    static public Orientation convertValue(int value) {
        // Only process some orientations. This is the full set, but many seem to be
        // used when there is no non-normal orientation
        // 1	top	left side
        // 2	top	right side
        // 3	bottom	right side
        // 4	bottom	left side
        // 5	left side	top
        // 6	right side	top
        // 7	right side	bottom
        // 8	left side	bottom
        //
        //   1        2       3      4         5            6           7          8
        //
        //888888  888888      88  88      8888888888  88                  88  8888888888
        //88          88      88  88      88  88      88  88          88  88      88  88
        //8888      8888    8888  8888    88          8888888888  8888888888          88
        //88          88      88  88
        //88          88  888888  888888
        //
        switch (value) {
            case 1:
                return Orientation.NONE;
            case 2:
                return Orientation.MIRROR_LEFT_RIGHT;
            case 3:
                return Orientation.UPSIDE_DOWN;
            case 4:
                return Orientation.MIRROR_TOP_BOTTOM;
            case 5:
                return Orientation.CLOCKWISE_AND_MIRROR;
            case 6:
                return Orientation.ANTICLOCKWISE;
            case 7:
                return Orientation.ANTICLOCKWISE_AND_MIRROR;
            case 8:
                return Orientation.CLOCKWISE;
            default:
                return Orientation.UNDEFINED;
        }
    }

}
