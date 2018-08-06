package fram;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * See if the program is already running.  Uses a lock file
 *
 * @author Jason Leake
 */
class RunningLock {

    private boolean myLock = false;
    private File lockFile = new File("fram.lock");

    /**
     * Constructor
     */
    public RunningLock() {
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
                lockFile.deleteOnExit();
                myLock = true;
            } catch (IOException ex) {
                Logger.getLogger(RunningLock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Return true if the program is already running
     * @return true if the program is already running, false if this is the
     * only copy running
     */
   public boolean alreadyLocked() {
        return !myLock;
    }

    void delete() {
        lockFile.delete();
    }

}
