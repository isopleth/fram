package fram;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Set of static methods for manipulating images
 *
 * @author Jason Leake
 */
public class ManipulateImage {

    /**
     * Mirror the image along the vertical centre line axis
     *
     * @param image the input image
     * @return the modified image
     */
    public static BufferedImage mirror(BufferedImage image) {
        final var width = image.getWidth();
        final var height = image.getHeight();
	var newImage = new BufferedImage(width * 2, height,
					       BufferedImage.TYPE_3BYTE_BGR);
        final var gb = (Graphics2D) newImage.getGraphics();
        final var tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-width, 0);
        final var op = new AffineTransformOp(tx,
					     AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        newImage = op.filter(image, null);
        gb.drawImage(newImage, 0, 0, null);
        return newImage;
    }

    /**
     * Resize image so that it has width of newWidth
     *
     * @param image input image
     * @param newWidth new image width in pixels
     * @return resized output image
     */
    public static BufferedImage resizeImage(BufferedImage image, int newWidth) {

        final var newHeight = image.getHeight() * newWidth / image.getWidth();
        final var resizedImage = new BufferedImage(newWidth, newHeight, image.getType());
        final var graphics2d = resizedImage.createGraphics();
        graphics2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        graphics2d.dispose();
        graphics2d.setComposite(AlphaComposite.Src);
        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return resizedImage;
    }

    /**
     * Rotate the image by the specified angle in right angles
     *
     * @param image input image
     * @param rightAngles number of right angles to rotate it
     * @return rotated image
     */
    public static BufferedImage rotate(BufferedImage image, int rightAngles) {
        float sign = 1;
        if (rightAngles < 0) {
            rightAngles = -rightAngles;
            sign = -1;
        }
        while (rightAngles >= 4) {
            rightAngles = rightAngles - 4;
        }
        BufferedImage newImage;
        if ((rightAngles & 1) == 1) {
            // Odd number of right angles - aspect ratio changes
            newImage = new BufferedImage(image.getHeight(), image.getWidth(), 
                    BufferedImage.TYPE_3BYTE_BGR);
        } else {
            // Even number of right angles - no change in aspect ratio
            newImage = new BufferedImage(image.getWidth(), image.getHeight(), 
                    BufferedImage.TYPE_3BYTE_BGR);
        }
        final var graphics = (Graphics2D) newImage.getGraphics();
        graphics.rotate(sign * Math.toRadians(rightAngles * 90), 
                newImage.getWidth() / 2, newImage.getHeight() / 2);
        graphics.translate((newImage.getWidth() - image.getWidth()) / 2, 
                (newImage.getHeight() - image.getHeight()) / 2);
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return newImage;
    }

    /**
     * Convert image to 3 byte blue-green-red format
     *
     * @param image image to read for conversion
     * @return new converted image
     */
    public static BufferedImage make3ByteBgr(BufferedImage image) {
        // Convert monochrome etc images to 3 byte BGR
        final var newImage = new BufferedImage(image.getHeight(), 
					       image.getWidth(), BufferedImage.TYPE_3BYTE_BGR);
        final var graphics = (Graphics2D) newImage.getGraphics();
        graphics.drawImage(image, 0, 0, image.getHeight(), image.getWidth(), null);
        graphics.dispose();
        return newImage;
    }

}
