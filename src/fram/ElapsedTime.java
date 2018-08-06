package fram;

/**
 * Elapsed time
 *
 * @author Jason Leake
 */
class ElapsedTime {

    private final long startTime;

    /**
     * Constructor - set start time
     */
    public ElapsedTime() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Output the elapsed time since the object was constructed
     */
    public void reportElapsedTime(String description) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("%s - elapsed time %d seconds",
                description,
                (int) (Math.round((float) elapsedTime) / 1000.0)));
    }

}
