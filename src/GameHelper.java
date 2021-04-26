import java.util.Arrays;

public class GameHelper {
    static public Vector2 StringToVector2 (String str) {
        Double[] pos = Arrays.stream(str.split(",")).map(Double::parseDouble).toArray(Double[]::new);
        if (pos.length != 2) return new Vector2(-1, -1);
        else return new Vector2(pos[0], pos[1]);
    }

    //長方形の基準座標は左上
    public static boolean isCollideWithCircleAndRect(Vector2 circle, double radius, Vector2 rect, Vector2 size) {
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

    public static boolean isCollideWith2Circles(Vector2 circle1, double radius1, Vector2 circle2, double radius2) {
        double dist = Math.sqrt(Math.pow(circle1.getX() - circle2.getX(), 2) + Math.pow(circle1.getY() - circle2.getY(), 2));
        return radius1 + radius2 >= dist;
    }
}
