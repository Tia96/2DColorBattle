import java.io.*;
import java.net.Socket;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private static Network network;
    private static SnapShot snapshot;

    private boolean gameLoop;
    private final double targetFPS = 20.0;


    private GameManager() {
    }

    public static GameManager getInstance(Socket[] sockets) throws IOException {
        INSTANCE.gameLoop = true;
        network = new Network(sockets);
        snapshot = new SnapShot(network);

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
            //System.out.println("fps: " + fpsCounter.getFPS());
        }
    }

    private void updateWorld() {
        collidePositions();
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
