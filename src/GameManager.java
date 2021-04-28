import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private static Network network;
    private static SnapShot snapshot;

    private boolean gameLoop;
    private final double targetFPS = 20.0;

    private int[][] stage;

    private GameManager() {
    }

    public static GameManager getInstance(Socket[] sockets) throws IOException {
        INSTANCE.gameLoop = true;
        network = new Network(sockets);
        snapshot = new SnapShot(network);

        INSTANCE.stage = new int[480][];
        for (int i = 0; i < 480; ++i) {
            INSTANCE.stage[i] = new int[640];
        }
        return INSTANCE;
    }

    private void init() {
    }

    public void start() throws InterruptedException {
        init();
        snapshot.sendFirstData();
        snapshot.sendGameStart();
        for (int id = 0; id < network.sockets.length; ++id) {
            network.startReceiveMessage(id);
        }
        snapshot.gameLevel = 2;

        FPSCounter fpsCounter = new FPSCounter();
        fpsCounter.start();

        double targetFPSTime = 1000 / targetFPS;
        while (INSTANCE.gameLoop) {
            long startTime = System.currentTimeMillis();
            fpsCounter.count_frame();

            snapshot.getSnapShot();
            updateWorld();
            snapshot.sendSnapShot(0.0);

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime < targetFPSTime) Thread.sleep((long) (targetFPSTime - elapsedTime));
            System.out.println("fps: " + fpsCounter.getFPS());
        }
    }

    private void updateWorld() {
        updateStage();
        collidePositions();
    }

    private void updateStage() {
        if (snapshot.gameLevel == 2) {
            for (int id = 0; id < snapshot.player_num; ++id) {
                SnapShot.Player player = snapshot.players[id];
                int left = (int) player.position.getX();
                int right = (int) Math.ceil(left + 2 * player.radius);
                int top = (int) player.position.getY();
                int bottom = (int) Math.ceil(top + 2 * player.radius);
                for (int y = top; y <= bottom; ++y) {
                    for (int x = left; x <= right; ++x) {
                        if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                        if (stage[y][x] == player.color) continue;

                        if (GameHelper.isCollideWithCircleAndRect(new Vector2(left + player.radius, top + player.radius), player.radius, new Vector2(x, y), new Vector2(1, 1))) {
                            stage[y][x] = player.color;
                        }
                    }
                }

            }
        }
    }

    private void collidePositions() {
        for (int id1 = 0; id1 < snapshot.player_num; ++id1) {
            for (int id2 = id1 + 1; id2 < snapshot.player_num; ++id2) {
                SnapShot.Player player1 = snapshot.players[id1], player2 = snapshot.players[id2];
                int back = 1;
                while (GameHelper.isCollideWith2Circles(player1.position, player1.radius, player2.position, player2.radius)) {
                    System.out.println("Collide");
                    snapshot.rollbackPlayer(id1, back);
                    snapshot.rollbackPlayer(id2, back);
                    ++back;
                }
            }
        }
    }
}
