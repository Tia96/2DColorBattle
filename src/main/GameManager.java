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

    private GameManager() {
    }

    public static GameManager getInstance(GraphicsContext g, SnapShot snapshot) {
        INSTANCE.g = g;
        INSTANCE.snapshot = snapshot;
        return INSTANCE;
    }

    public void step() {
        if (snapshot.gameLevel == 2) {
            Point2D position = snapshot.players[snapshot.myID].position;
            int horizon = 0, vertical = 0;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.UP, false)) vertical -= 1;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.DOWN, false)) vertical += 1;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.RIGHT, false)) horizon += 1;
            if (GameHelper.isKeyPushed.getOrDefault(KeyCode.LEFT, false)) horizon -= 1;

            position = position.add(horizon * 4.6, vertical * 4.6);
            snapshot.sendSnapShot(0.0, position);
        }
        snapshot.getSnapShot();
    }

    public void draw() {
        //g.clearRect(0, 0, 640, 480);

        if (snapshot.gameLevel == 1) {
            g.setFill(Color.BLACK);
            g.fillText("COUNTDOWN " + snapshot.countDown, 100, 100);
        } else if (snapshot.gameLevel > 1) {
            for (int id = 0; id < player_num; ++id) {
                Color color;
                SnapShot.Player player = snapshot.players[id];
                color = switch (player.color) {
                    case 0 -> Color.WHITE;
                    case 1 -> Color.BLACK;
                    case 2 -> Color.RED;
                    case 3 -> Color.BLUE;
                    default -> throw new IllegalStateException("Unexpected value: " + player.color);
                };
                g.setFill(color);

                int size = 100;
                Point2D[] inner_positions = new Point2D[size];
                for (int i = 0; i < size - 1; ++i)
                    inner_positions[i] = player.previous_position.add((player.position.subtract(player.previous_position)).multiply(1.0 / (size - 1) * i));
                inner_positions[size - 1] = player.position;

                for (Point2D position : inner_positions) {
                    g.fillOval(position.getX(), position.getY(), player.radius * 2, player.radius * 2);
                }
            }
        }
    }
}
