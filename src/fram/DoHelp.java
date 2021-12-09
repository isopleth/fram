package fram;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Display help text
 *
 * @author Jason Leake
 */
class DoHelp {

    private static final String NAME = "resources/description.txt";

    /**
     * Display the contents of the help file
     */
    public void help() {
        final var stream = getClass().getResourceAsStream(NAME);
        if (stream == null) {
            System.out.println("File not located: " + getClass().getResource(NAME));
        } else {
            try {
                final var input = new Scanner(stream);

                while (input.hasNextLine()) {
                    System.out.println(input.nextLine());
                }
            } catch (Exception e) {
                System.out.println("Scanner error");
            }
        }
    }
}
