package fram;

import java.util.Date;

/**
 * Get date as a string
 *
 * @author Jason Leake
 */
class DateAndTimeNow {

    /**
     * Get the current date, and append newline to return string
     *
     * @return current date
     */
    public static String getNewline() {
        return get() + "\n";
    }

    /**
     * Get the current date
     *
     * @return current date
     */
    public static String get() {
        return new Date().toString();
    }
}
