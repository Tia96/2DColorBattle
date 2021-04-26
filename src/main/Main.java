package main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private GameManager gameManager;
    private GraphicsContext graphic;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root, 640, 480, Color.WHITE);
        Canvas cvs = new Canvas(640, 480);
        root.getChildren().add(cvs);
        scene.setOnKeyPressed(GameHelper::keyPressedHandler);
        scene.setOnKeyReleased(GameHelper::keyReleasedHandler);
        graphic = cvs.getGraphicsContext2D();

        stage.setTitle("Splatoon2D");
        stage.setScene(scene);
        stage.show();

        SnapShot snapshot = new SnapShot();
        gameManager = GameManager.getInstance(graphic, snapshot);

        new Animation().start();
    }

    private class Animation extends AnimationTimer {
        private long startTime = 0;
        private int frames = 0;

        @Override
        public void handle(long now) {
            ++frames;
            if (startTime == 0) startTime = now;
            long elapseTime = (now - startTime) / 1000000;

            gameManager.step();
            gameManager.draw();

            graphic.setFill(Color.BLACK);
            graphic.fillText("fps: " + (float) frames / elapseTime * 1000, 20, 20);
        }
    }
}
