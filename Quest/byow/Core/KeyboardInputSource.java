package byow.Core;

/**
 * Created by hug.
 */
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

public class KeyboardInputSource implements InputSource {
    private static final boolean PRINT_TYPED_KEYS = false;
    public KeyboardInputSource() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(0.5, 0.75, "Welcome To Your Quest");

        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(0.5, 0.55, "New Game (N)");
        StdDraw.text(0.5, 0.45, "Load Game (L)");
        StdDraw.text(0.5, 0.35, "Replay (R)");
        StdDraw.text(0.5, 0.25, "Quit (Q)");



    }

    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (PRINT_TYPED_KEYS) {
                    System.out.print(c);
                }
                return c;
            }
        }
    }

    public boolean possibleNextInput() {
        return true;
    }
}
