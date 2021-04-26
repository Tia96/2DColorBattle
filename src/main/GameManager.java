package main;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private GraphicsContext g;
    private SnapShot snapshot;

    private int player_num = 2;
    private Point2D position = new Point2D(0, 0);

    private GameManager() {
    }

    public static GameManager getInstance(GraphicsContext g, SnapShot snapshot) {
        INSTANCE.g = g;
        INSTANCE.snapshot = snapshot;
        return INSTANCE;
    }

    public void step() {
        if (snapshot.gameLevel >= 1) {
            int horizon = 0, vertical = 0;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.UP, false)) vertical -= 1;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.DOWN, false)) vertical += 1;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.RIGHT, false)) horizon += 1;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.LEFT, false)) horizon -= 1;

            position.add(horizon * 4.6, vertical * 4.6);
            snapshot.sendSnapShot(0.0, position);
            snapshot.getSnapShot();
        }
    }

    public void draw() {
        g.clearRect(0, 0, 640, 480);

        if (snapshot.gameLevel == 1) {
            g.setFill(Color.BLACK);
            g.fillText("COUNTDOWN " + snapshot.countDown, 100, 100);
        } else if (snapshot.gameLevel > 1) {
            for (int id = 0; id < player_num; ++id) {
                Color color;
                SnapShot.Player player = snapshot.players[id];
                switch (player.color) {
                    case 0:
                        color = Color.BLACK;
                        break;
                    case 1:
                        color = Color.WHITE;
                        break;
                    case 2:
                        color = Color.RED;
                        break;
                    case 3:
                        color = Color.BLUE;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + player.color);
                }
                g.setFill(color);
                g.fillOval(player.position.getX(), player.position.getY(), player.radius * 2, player.radius * 2);
            }

            for (int i = 0; i < 480; ++i) {
                for (int j = 0; j < 640; ++j) {
                    Color color;
                    switch (snapshot.stage[i][j]) {
                        case 0:
                            color = Color.BLACK;
                            break;
                        case 1:
                            color = Color.WHITE;
                            break;
                        case 2:
                            color = Color.RED;
                            break;
                        case 3:
                            color = Color.BLUE;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + snapshot.stage[i][j]);
                    }
                    g.setFill(color);
                    g.fillRect(j, i, 1, 1);
                }
            }
        }
    }
}
