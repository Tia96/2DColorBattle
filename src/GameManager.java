import java.io.*;
import java.net.Socket;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private static Network network;
    private static SnapShot snapshot;

    private boolean gameLoop;
    private final double targetFPS = 20.0;

    private int[][] stage;
    private int gameLevel = 0;

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
        snapshot.sendGameStart();
        for (int id = 0; id < network.sockets.length; ++id) {
            network.startReceiveMessage(id);
        }

        int frames = 0;
        long startTime = System.currentTimeMillis();
        double targetFPSTime = 1000 / targetFPS;
        while (INSTANCE.gameLoop) {
            long sT = System.currentTimeMillis();
            ++frames;

            snapshot.getSnapShot();
            updateWorld();
            snapshot.sendSnapShot(0.0, gameLevel, stage);

            long elapsedTime = System.currentTimeMillis() - sT;
            if (elapsedTime < targetFPSTime) Thread.sleep((long) (targetFPSTime - elapsedTime));
            //System.out.println("fps: " + (double) frames / (System.currentTimeMillis() - startTime) * 1000);
        }
    }

    private void updateWorld() {
        drawStage();
    }

    private void drawStage() {
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

    private void collidePositions(Vector2[] positions) {

    }
}
