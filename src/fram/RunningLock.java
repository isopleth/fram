package fram;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

/**
 * See if the program is already running.  Uses a lock file
 *
 * @author Jason Leake
 */
class RunningLock {

    private boolean myLock = false;
    private final File lockFile = new File("fram.lock");

    /**
     * Constructor
     */
    public RunningLock() {

	// Delete lock file if it is more than a month old.  Probably means program crashed.
	if (lockFile.exists()) {
            var oneMonthAgo = new Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000));
            if (new Date(lockFile.lastModified()).before(oneMonthAgo)) {
                System.out.println("Deleting lock file as it is more than a month old");
		if (!delete()) {
		    System.out.println("Could not delete lock file");
		}
	    }
	}
	    
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

   /**
    * Delete lock
    * @return success or failure
    */
    boolean delete() {
        return lockFile.delete();
    }

}
