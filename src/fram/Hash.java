package fram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculate the hash of a file
 *
 * @author Jason Leake
 */
public class Hash {

    private static MessageDigest digest = null;
    private static String CLASSNAME = Hash.class.getName();

    Hash() throws NoSuchAlgorithmException {
        if (digest == null) {
            digest = MessageDigest.getInstance("SHA-256");
        } else {
            digest.reset();
        }
    }

    /**
     * Compute the hash, from the file contents AND the filename
     *
     * @param inputFile input file to process
     * @return its hash
     */
    public String smoke(File inputFile) {
        String output;
        int read;
        byte[] buffer = new byte[8192];
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputFile);
            digest.reset();
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            // Add in the filename
            byte[] fileNameChars = inputFile.getAbsolutePath().getBytes();
            digest.update(fileNameChars, 0, fileNameChars.length);
            // Add in the image orientation exif data as if this is changed then
            // the output file needs to be regenerated
            int ordinal = FileCopier.getRotation(inputFile.toPath()).ordinal();
            ByteBuffer byteBuffer = ByteBuffer.allocate(4).putInt(ordinal);
            byte[] orientationArray = byteBuffer.array();
            digest.update(orientationArray, 0, orientationArray.length);

            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(1, hash);
            output = bigInt.toString(16);
            while (output.length() < 32) {
                output = "0" + output;
            }
        } catch (IOException ex) {
            Logger.getLogger(CLASSNAME).log(Level.SEVERE, null, ex);
            ex.printStackTrace(System.err);
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(CLASSNAME).log(Level.SEVERE, null, ex);
                }
            }
        }

        return output;
    }
}
