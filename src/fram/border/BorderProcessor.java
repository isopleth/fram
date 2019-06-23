package fram.border;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is responsible for removing white borders around images.
 *
 * @author Jason Leake
 */
public class BorderProcessor {

    private final BufferedImage theImage;

    private ArrayList<BorderChecker> rowColours = new ArrayList<>();
    private ArrayList<BorderChecker> columnColours = new ArrayList<>();

    /**
     * constructor
     *
     * @param image
     */
    public BorderProcessor(BufferedImage image) {
        theImage = image;

        // Test each vertical linescan to see if it could be a border
        for (int x = image.getMinX(); x < image.getMinX() + image.getWidth(); x++) {
            BorderChecker borderchecker = new BorderChecker();
            for (int y = image.getMinY(); y < image.getMinY() + image.getHeight(); y++) {
                borderchecker.add(image.getRGB(x, y));
                if (borderchecker.definitelyNotBorder()) {
                    break;
                }
            }
            columnColours.add(borderchecker);
        }

        // Test each horizontal linescan to see if it could be a border
        for (int y = image.getMinY(); y < image.getMinY() + image.getHeight(); y++) {
            BorderChecker borderchecker = new BorderChecker();
            for (int x = image.getMinX(); x < image.getMinX() + image.getWidth(); x++) {
                borderchecker.add(image.getRGB(x, y));
                if (borderchecker.definitelyNotBorder()) {
                    break;
                }
            }
            rowColours.add(borderchecker);
        }

    }

    /**
     * Check if the image has a border around it
     *
     * @return true if the image has a border round it
     */
    public boolean hasBorder() {
        // Check the row
        for (BorderChecker borderchecker : rowColours) {
            if (borderchecker.isBorder()) {
                System.out.println("Has a border");
                return true;
            } else {
                break;
            }
        }

        Collections.reverse(rowColours);
        for (BorderChecker borderchecker : rowColours) {
            if (borderchecker.isBorder()) {
                System.out.println("Has a border");
                return true;
            } else {
                break;
            }
        }

        // Check the column
        for (BorderChecker bordercheker : columnColours) {
            if (bordercheker.isBorder()) {
                System.out.println("Has a border");
                return true;
            } else {
                break;
            }
        }
        Collections.reverse(columnColours);
        for (BorderChecker borderchecker : columnColours) {
            if (borderchecker.isBorder()) {
                System.out.println("Has a border");
                return true;
            } else {
                break;
            }
        }

        //System.out.println("No border");
        return false;
    }

    public BufferedImage removeBorder() {
        return theImage;
    }

}
