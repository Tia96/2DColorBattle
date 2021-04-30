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
            System.out.println("fps: " + fpsCounter.getFPS());
        }
    }

    private void updateWorld() {
        updateStage();
        collidePositions();
    }

    private void updateStage() {
        if (snapshot.gameLevel == 2) {
            snapshot.updateStage = new StringBuilder();

            for (int id = 0; id < snapshot.player_num; ++id) {
                SnapShot.Player player = snapshot.players[id];
                int left = (int) player.position.getX();
                int right = (int) Math.ceil(left + 2 * player.radius);
                int top = (int) player.position.getY();
                int bottom = (int) Math.ceil(top + 2 * player.radius);

                boolean invading = true;
                for (int y = top; y <= bottom; ++y) {
                    for (int x = left; x <= right; ++x) {
                        if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                        if (GameHelper.isCollideWithCircleAndRect(new Vector2(left + player.radius, top + player.radius), player.radius, new Vector2(x, y), new Vector2(1, 1))) {
                            if (snapshot.conArea[y][x] == player.ID) invading = false;
                        }
                    }
                }

                if (player.invading && !invading) {
                    int left_area = 640, right_area = 0, top_area = 480, bottom_area = 0;
                    for (Vector2 position : player.inv_positions) {
                        if (position.getX() < left_area) left_area = (int) position.getX();
                        if (position.getX() > right_area) right_area = (int) position.getX();
                        if (position.getY() < top_area) top_area = (int) position.getY();
                        if (position.getY() > bottom_area) bottom_area = (int) position.getY();
                    }
                    player.inv_positions.clear();

                    System.out.println("INVADING COMPLETED!!!!!!!!!!");
                    for (int y = top_area; y <= bottom_area; ++y) {
                        for (int x = left_area; x <= right_area; ++x) {
                            if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                            if (isInsideArea(id, x, y)) {
                                snapshot.conArea[y][x] = player.ID;
                                snapshot.updateStage.append("0,").append(x).append(",").append(y).append(",").append(player.color).append(";");
                            }
                        }
                    }

                    for (int y = top_area; y <= bottom_area; ++y) {
                        for (int x = left_area; x <= right_area; ++x) {
                            if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                            if (snapshot.invArea[y][x] == player.ID) {
                                snapshot.invArea[y][x] = 0;
                                snapshot.updateStage.append("1,").append(x).append(",").append(y).append(",").append(0).append(";");
                            }
                        }
                    }

                    player.invading = false;
                }

                if (player.invading) {
                    for (int y = top; y <= bottom; ++y) {
                        for (int x = left; x <= right; ++x) {
                            if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                            if (GameHelper.isCollideWithCircleAndRect(new Vector2(left + player.radius, top + player.radius), player.radius, new Vector2(x, y), new Vector2(1, 1))) {
                                snapshot.invArea[y][x] = player.ID;
                                snapshot.updateStage.append("1,").append(x).append(",").append(y).append(",").append(player.color).append(";");
                                snapshot.players[id].inv_positions.add(new Vector2(x, y));
                            }
                        }
                    }
                }
                if (!player.invading && invading) player.invading = true;
            }
        }
    }

    private boolean isInsideArea(int id, int x, int y) {
        int cnt = 0, c = snapshot.invArea[y][x];
        for (int nx = x + 1; nx < 640; ++nx) {
            if (snapshot.invArea[y][nx] == id && snapshot.invArea[y][nx - 1] != snapshot.invArea[y][nx]) ++cnt;
        }
        return cnt % 2 == 1;
    }

//    private int getDirection(int id, int x, int y, int direction) {
//        int ret = stage[y][x];
//        switch(direction) {
//            case 0:
//                for (int ny = y - 1; ny >= 0; --ny) if (stage[ny][x] != 0)
//        }
//    }

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
