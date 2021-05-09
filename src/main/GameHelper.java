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

    //長方形の基準座標は左上
    public static boolean isCollideWithCircleAndRect(Point2D circle, double radius, Point2D rect, Point2D size) {
        double xc = circle.getX();
        double yc = circle.getY();
        double x1 = rect.getX();
        double x2 = rect.getX() + size.getX();
        double y1 = rect.getY();
        double y2 = rect.getY() + size.getY();
        boolean a = (xc > x1) && (xc < x2) && (yc > y1 - radius) && (yc < y2 + radius);
        boolean b = (xc > x1 - radius) && (xc < x2 + radius) && (yc > y1) && (yc < y2);
        boolean c = (Math.pow(xc - x1, 2) + Math.pow(yc - y1, 2) < Math.pow(radius, 2));
        boolean d = (Math.pow(xc - x1, 2) + Math.pow(yc - y2, 2) < Math.pow(radius, 2));
        boolean e = (Math.pow(xc - x2, 2) + Math.pow(yc - y1, 2) < Math.pow(radius, 2));
        boolean f = (Math.pow(xc - x2, 2) + Math.pow(yc - y2, 2) < Math.pow(radius, 2));
        return a || b || c || d || e || f;
    }
}
