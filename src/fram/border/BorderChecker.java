package fram.border;

import java.awt.Color;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * This class checks if a linescan - vertical or horizontal - could be a border
 * It must meet the following criteria:
 *
 * 1. All of the pixels must be more or less white
 *
 * @author Jason Leake
 */
class BorderChecker {

    DescriptiveStatistics[] colorStats = new DescriptiveStatistics[3];
    // Set false if this is definitely not part of a border
    boolean couldBeBorder = true;

    /**
     * constructor
     */
    public BorderChecker() {
        colorStats[REDI] = new DescriptiveStatistics();
        colorStats[BLUEI] = new DescriptiveStatistics();
        colorStats[GREENI] = new DescriptiveStatistics();
    }

    private static final int REDI = 0;
    private static final int BLUEI = 1;
    private static final int GREENI = 2;

    /**
     * Add the specified rgb values to the descriptive statistics. If they are
     * not close to white then mark the AverageColour object as not a border and
     * give up subsequent adding to the average, as a speed optimisation.
     *
     * @param rgb
     */
    void add(int rgb) {
        if (!couldBeBorder) {
            Color colour = new Color(rgb);
            colorStats[REDI].addValue(colour.getRed());
            colorStats[BLUEI].addValue(colour.getBlue());
            colorStats[GREENI].addValue(colour.getGreen());
            if (!whiteish(colour)) {
                couldBeBorder = false;
            }
        }
    }

    /**
     * Check if the specified pixel is sufficiently white to potentially be part
     * of a border
     *
     * @param colour colour of pixel
     * @return true if it could be part of a border
     */
    private boolean whiteish(Color colour) {
        return (colour.getRed() > 200) && (colour.getBlue() > 200) && (colour.getGreen() > 200);
    }

    private Color getMedian() {
        return new Color((int) colorStats[REDI].getPercentile(0.5),
                (int) colorStats[GREENI].getPercentile(0.5),
                (int) colorStats[BLUEI].getPercentile(0.5));
    }

    /**
     * Return true if this linescan is the correct colour to be a border. It
     * might still just be part of the image, depending upon whether it is on
     * the edge of the picture or not - this class doesn't handle that.
     *
     * @return true if it potentially is a border
     */
    boolean isBorder() {
        // If there is some reason it is not a border then immediately return
        // false
        if (!couldBeBorder) {
            return false;
        }
        // Otherwise check the median colour to see if it is reasonably white
        Color median = getMedian();
        return whiteish(median);
    }

    /**
     * Test if this linescan is definitely not a border
     *
     * @return true if it definitely isn't a border
     */
    boolean definitelyNotBorder() {
        return !couldBeBorder;
    }
}
