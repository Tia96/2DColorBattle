package main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private GameManager gameManager;
    private GraphicsContext graphic;
    private final FPSCounter fpsCounter = new FPSCounter();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 640, 480, Color.WHITE);
        Canvas cvs = new Canvas(640, 480);
        root.getChildren().add(cvs);
        scene.setOnKeyPressed(GameHelper::keyPressedHandler);
        scene.setOnKeyReleased(GameHelper::keyReleasedHandler);
        graphic = cvs.getGraphicsContext2D();

        stage.setTitle("2DColorBattle");
        stage.setScene(scene);
        stage.show();

        SnapShot snapshot = new SnapShot();
        gameManager = GameManager.getInstance(graphic, snapshot);

        fpsCounter.start();
        new Animation().start();
    }

    private class Animation extends AnimationTimer {
        @Override
        public void handle(long now) {
            fpsCounter.count_frame();

            gameManager.step();
            gameManager.draw();

            graphic.setFill(Color.BLACK);
            graphic.setFont(new Font("resources/SourceHanSansJP-Normal.otf", 20));
            graphic.fillText("fps: " + fpsCounter.getFPS(), 10, 20);
        }
    }
}
