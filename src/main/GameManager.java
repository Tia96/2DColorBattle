package main;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private GraphicsContext g;
    private double x = 0, y = 0, radius = 25;
    private double x2 = 620, y2 = 460, radius2 = 20;
    private int horizon = 0, vertical = 0;
    private int horizon2 = 0, vertical2 = 0;
    private int[][] map;

    private GameManager() {
    }

    public static GameManager getInstance(GraphicsContext g) {
        INSTANCE.g = g;
        INSTANCE.map = new int[480][];
        for(int i = 0; i < 480; ++i) {
            INSTANCE.map[i] = new int[640];
        }
        return INSTANCE;
    }

    public void step() {
        horizon = 0;
        vertical = 0;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.UP, false)) vertical -= 1;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.DOWN, false)) vertical += 1;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.RIGHT, false)) horizon += 1;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.LEFT, false)) horizon -= 1;

        x += horizon * 4.6;
        y += vertical * 4.6;

        horizon2 = 0;
        vertical2 = 0;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.W, false)) vertical2 -= 1;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.S, false)) vertical2 += 1;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.D, false)) horizon2 += 1;
        if (GameHelper.isKeyPushed.getOrDefault(KeyCode.A, false)) horizon2 -= 1;

        x2 += horizon2 * 4.6;
        y2 += vertical2 * 4.6;

        if (GameHelper.isCollideWith2Circles(new Point2D(x + radius, y + radius), radius, new Point2D(x2 + radius2, y2 + radius2), radius2)) {
            x -= horizon * 4.6;
            y -= vertical * 4.6;
            x2 -= horizon2 * 4.6;
            y2 -= vertical2 * 4.6;
        }

        int left = (int)x;
        int right = (int)Math.ceil(x + 2 * radius);
        int top = (int)y;
        int bottom = (int)Math.ceil(y + 2 * radius);
        for (int i = top; i <= bottom; ++i) {
            for (int j = left; j <= right; ++j) {
                if (i < 0 || i >= 480 || j < 0 || j >= 640) continue;
                if(map[i][j] == 1) continue;

                if (GameHelper.isCollideWithCircleAndRect(new Point2D(x + radius, y + radius), radius, new Point2D(j, i), new Point2D(1, 1))){
                    map[i][j] = 1;
                }
            }
        }

        int left2 = (int)x2;
        int right2 = (int)Math.ceil(x2 + 2 * radius2);
        int top2 = (int)y2;
        int bottom2 = (int)Math.ceil(y2 + 2 * radius2);
        for (int i = top2; i <= bottom2; ++i) {
            for (int j = left2; j <= right2; ++j) {
                if (i < 0 || i >= 480 || j < 0 || j >= 640) continue;
                if(map[i][j] == -1) continue;

                if (GameHelper.isCollideWithCircleAndRect(new Point2D(x2 + radius2, y2 + radius2), radius2, new Point2D(j, i), new Point2D(1, 1))){
                    map[i][j] = -1;
                }
            }
        }
    }

    public void draw() {
        g.clearRect(0, 0, 640, 480);

        for (int i = 0; i < 480; ++i) {
            for (int j = 0; j < 640; ++j) {
                if (map[i][j] == 1) {
                    g.setFill(Color.PURPLE);
                    g.fillRect(j, i,1, 1);
                }
                if (map[i][j] == -1) {
                    g.setFill(Color.BLUE);
                    g.fillRect(j, i,1, 1);
                }
            }
        }

        g.setFill(Color.RED);
        g.fillOval(x, y, radius * 2, radius * 2);

        g.setFill(Color.YELLOW);
        g.fillOval(x2, y2, radius2 * 2, radius2 * 2);
    }
}
