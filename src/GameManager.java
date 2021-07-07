import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

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
        snapshot.level2StartTime = System.currentTimeMillis();

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
        if (snapshot.gameLevel == 2) {
            updateStage();
            collidePositions();
            calcPlayerScore();
            updateRestTime();
            updateGameLevel();
        }
    }

    private Vector2 searchInside(Vector2 center, int id) {
        for (int y = (int) (center.getY() - 20); y < center.getY() + 20; ++y) {
            for (int x = (int) (center.getX() - 20); x < center.getX() + 20; ++x) {
                if (snapshot.conArea[y][x] == id) continue;

                int cnt = 0;
                for (int w = x - 1; w >= 0; --w) {
                    if (snapshot.conArea[y][w] == id) {
                        ++cnt;
                        break;
                    }
                }
                for (int w = x + 1; w < 640; ++w) {
                    if (snapshot.conArea[y][w] == id) {
                        ++cnt;
                        break;
                    }
                }
                for (int h = y - 1; h >= 0; --h) {
                    if (snapshot.conArea[h][x] == id) {
                        ++cnt;
                        break;
                    }
                }
                for (int h = y + 1; h < 480; ++h) {
                    if (snapshot.conArea[h][x] == id) {
                        ++cnt;
                        break;
                    }
                }
                if (cnt == 4) return new Vector2(x, y);
            }
        }
        return new Vector2(-1, -1);
    }

    private void fillInside(Vector2 base, int id) {
        final int[] dx = {0, 1, 0, -1};
        final int[] dy = {1, 0, -1, 0};
        Deque<Vector2> que = new ArrayDeque<>();
        que.push(base);
        while (!que.isEmpty()) {
            Vector2 p = que.pop();
            int x = (int) p.getX(), y = (int) p.getY();
            if (x < 0 || x >= 640 || y < 0 || y >= 480 || snapshot.conArea[y][x] == id) continue;
            snapshot.conArea[y][x] = id;
            for (int i = 0; i < 4; ++i) {
                que.push(new Vector2(x + dx[i], y + dy[i]));
            }
        }
    }

    private void updateStage() {
        if (snapshot.gameLevel == 2) {
            for (SnapShot.Player player : snapshot.players) {
                boolean invading = true;
                for (int y = (int) player.position.getY(); y <= player.position.getY() + player.radius * 2; ++y) {
                    for (int x = (int) player.position.getX(); x <= player.position.getX() + player.radius * 2; ++x) {
                        if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                        if (GameHelper.isCollideWithCircleAndRect(new Vector2(player.position.getX() + player.radius, player.position.getY() + player.radius), player.radius, new Vector2(x, y), new Vector2(1, 1))) {
                            if (snapshot.conArea[y][x] == player.ID) invading = false;
                        }
                    }
                }

                if (invading) {
                    for (int y = (int) player.position.getY(); y <= player.position.getY() + player.radius * 2; ++y) {
                        for (int x = (int) player.position.getX(); x <= player.position.getX() + player.radius * 2; ++x) {
                            if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                            if (GameHelper.isCollideWithCircleAndRect(new Vector2(player.position.getX() + player.radius, player.position.getY() + player.radius), player.radius, new Vector2(x, y), new Vector2(1, 1))) {
                                snapshot.invArea[y][x] = player.ID;
                            }

                        }
                    }

                    if (!player.invading) {
                        for (int y = (int) player.pre_position.getY(); y <= player.pre_position.getY() + player.radius * 2; ++y) {
                            for (int x = (int) player.pre_position.getX(); x <= player.pre_position.getX() + player.radius * 2; ++x) {
                                if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                                if (GameHelper.isCollideWithCircleAndRect(new Vector2(player.pre_position.getX() + player.radius, player.pre_position.getY() + player.radius), player.radius, new Vector2(x, y), new Vector2(1, 1))) {
                                    snapshot.invArea[y][x] = player.ID;
                                }
                            }
                        }
                    }

                    player.invading = true;
                }

                if (player.invading && !invading) {
                    for (int y = (int) player.position.getY(); y <= player.position.getY() + player.radius * 2; ++y) {
                        for (int x = (int) player.position.getX(); x <= player.position.getX() + player.radius * 2; ++x) {
                            if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                            if (GameHelper.isCollideWithCircleAndRect(new Vector2(player.position.getX() + player.radius, player.position.getY() + player.radius), player.radius, new Vector2(x, y), new Vector2(1, 1))) {
                                snapshot.invArea[y][x] = player.ID;
                            }

                        }
                    }

                    for (int y = 0; y < 480; ++y) {
                        for (int x = 0; x < 640; ++x) {
                            if (snapshot.invArea[y][x] == player.ID) {
                                snapshot.invArea[y][x] = -1;
                                snapshot.conArea[y][x] = player.ID;
                            }
                        }
                    }
                    fillInside(searchInside(player.position, player.ID), player.ID);

                    player.invading = false;
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

    private void calcPlayerScore() {
        int[] area = new int[snapshot.player_num];
        for (int y = 0; y < 480; ++y) {
            for (int x = 0; x < 640; ++x) {
                int id = snapshot.conArea[y][x];
                if (id != -1) ++area[id];
            }
        }
        for (int i = 0; i < snapshot.player_num; ++i) {
            snapshot.players[i].score = (double)area[i] / (640 * 480) * 100;
        }
    }

    private void updateRestTime() {
        snapshot.leftTime = snapshot.gameDuration - (System.currentTimeMillis() - snapshot.level2StartTime) / 1000;
    }

    private void updateGameLevel() {
        if (snapshot.leftTime <= 0) snapshot.gameLevel = 3;
    }
}
