package fram;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 *
 * @author user
 */
public class ManipulateImage {

    /**
     * Mirror the image along the vertical centreline asix
     *
     * @param image the input image
     * @return the modified image
     */
    static BufferedImage mirror(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width * 2, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D gb = (Graphics2D) newImage.getGraphics();
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-width, 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        newImage = op.filter(image, null);
        gb.drawImage(newImage, 0, 0, null);
        return newImage;
    }

    /**
     * Resize image so that it has width of newWidth
     *
     * @param image
     * @param newWidth new image width in pixels
     * @return resized image
     */
    static BufferedImage resizeImage(BufferedImage image, int newWIdth) {
 
        int newHeight = image.getHeight() * newWIdth / image.getWidth();
        BufferedImage resizedImage = new BufferedImage(newWIdth, newHeight, image.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, newWIdth, newHeight, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return resizedImage;
    }

    /**
     * Rotate the image by the specified angle in right angles
     *
     * @param image input image
     * @param angle number of right angles to rotate it
     * @return rotated image
     */
    static BufferedImage rotate(BufferedImage image, int rightAngles) {
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
            newImage = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_3BYTE_BGR);
        } else {
            // Even number of right angles - no change in aspect ratio
            newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D graphics = (Graphics2D) newImage.getGraphics();
        graphics.rotate(sign * Math.toRadians(rightAngles * 90), newImage.getWidth() / 2, newImage.getHeight() / 2);
        graphics.translate((newImage.getWidth() - image.getWidth()) / 2, (newImage.getHeight() - image.getHeight()) / 2);
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return newImage;
    }

    /**
     * Convert image to 3 byte blue-green-red format
     *
     * @param image
     * @return new image
     */
    static BufferedImage make3ByteBgr(BufferedImage image) {
        // Convert monochrome etc images to 3 byte BGR
        BufferedImage newImage = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = (Graphics2D) newImage.getGraphics();
        graphics.drawImage(image, 0, 0, image.getHeight(), image.getWidth(), null);
        graphics.dispose();
        return newImage;
    }

}