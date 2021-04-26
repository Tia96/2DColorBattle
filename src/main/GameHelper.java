package main;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GameHelper {
    public static Map<KeyCode, Boolean> isKeyPushed = new HashMap<>();

    public static void keyPressedHandler(KeyEvent e) {
        isKeyPushed.put(e.getCode(), true);
    }

    public static void keyReleasedHandler(KeyEvent e) {
        isKeyPushed.put(e.getCode(), false);
    }

    static public Point2D StringToPoint2D (String str) {
        Double[] pos = Arrays.stream(str.split(",")).map(Double::parseDouble).toArray(Double[]::new);
        if (pos.length != 2) return new Point2D(-1, -1);
        else return new Point2D(pos[0], pos[1]);
    }
}
