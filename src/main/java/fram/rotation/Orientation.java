package fram.rotation;

import java.util.Map;

/**
 * Enum for image rotation
 *
 * @author Jason Leake
 */
public enum Orientation {
    /** Image is oriented correctly */
    NONE,
    /** Rotated 90 degrees clockwise */
    CLOCKWISE,
    /** Rotated 90 degrees anticlockwise */
    ANTICLOCKWISE,
    /** Mirrored along the vertical centreline */
    MIRROR_LEFT_RIGHT,
    /** Rotated 180 degrees */
    UPSIDE_DOWN,
    /** Mirrored along the horizontal centreline */
    MIRROR_TOP_BOTTOM,
    /** Rotated 90 degrees clockwise and then mirrored along horizontal centreline */
    CLOCKWISE_AND_MIRROR,
    /** Rotated 90 degrees anticlockwise and then mirrored along horizontal centreline */
    ANTICLOCKWISE_AND_MIRROR,
    /** Orientation is not know */
    UNDEFINED;


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
    static private final Map<Integer, Orientation> mappings = Map.ofEntries(Map.entry(1,  Orientation.NONE),
									    Map.entry(2,  Orientation.MIRROR_LEFT_RIGHT),
									    Map.entry(3,  Orientation.UPSIDE_DOWN),
									    Map.entry(4,  Orientation.MIRROR_TOP_BOTTOM),
									    Map.entry(5,  Orientation.CLOCKWISE_AND_MIRROR),
									    Map.entry(6,  Orientation.ANTICLOCKWISE),
									    Map.entry(7,  Orientation.ANTICLOCKWISE_AND_MIRROR),
									    Map.entry(8,  Orientation.CLOCKWISE));

    
    /**
     * Convert orientation exif data to Orientation enumeration
     *
     * @param value EXIF orientation value
     * @return corresponding orientation enumeration
     */
    static public Orientation convertValue(int value) {
	var orientation = mappings.get(value);
	if (orientation == null) {
	    orientation = Orientation.UNDEFINED;
	}
	return orientation;
    }

}
