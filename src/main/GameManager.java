package main;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private GraphicsContext g;
    private SnapShot snapshot;
    private final String fontPath = "resource/SourceHanSans-Normal.otf";
    private final long level1StartTime;

    private GameManager() {
        level1StartTime = System.currentTimeMillis();
    }

    public static GameManager getInstance(GraphicsContext g, SnapShot snapshot) {
        INSTANCE.g = g;
        INSTANCE.snapshot = snapshot;
        return INSTANCE;
    }

    public Point2D searchInside(Point2D center, int id) {
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
                if (cnt == 4) return new Point2D(x, y);
            }
        }
        return new Point2D(-1, -1);
    }

    public void fillInside(Point2D base, int id) {
        final int[] dx = {0, 1, 0, -1};
        final int[] dy = {1, 0, -1, 0};
        Deque<Point2D> que = new ArrayDeque<>();
        que.push(base);
        while (!que.isEmpty()) {
            Point2D p = que.pop();
            int x = (int) p.getX(), y = (int) p.getY();
            if (x < 0 || x >= 640 || y < 0 || y >= 480 || snapshot.conArea[y][x] == id) continue;
            snapshot.conArea[y][x] = id;
            for (int i = 0; i < 4; ++i) {
                que.push(new Point2D(x + dx[i], y + dy[i]));
            }
        }
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

        if (snapshot.gameLevel == 2) {
            for (SnapShot.Player player : snapshot.players) {
                for (Point2D position : player.piledPositions) {
                    boolean invading = true;
                    for (int y = (int) position.getY(); y <= position.getY() + player.radius * 2; ++y) {
                        for (int x = (int) position.getX(); x <= position.getX() + player.radius * 2; ++x) {
                            if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                            if (GameHelper.isCollideWithCircleAndRect(new Point2D(position.getX() + player.radius, position.getY() + player.radius), player.radius, new Point2D(x, y), new Point2D(1, 1))) {
                                if (snapshot.conArea[y][x] == player.ID) invading = false;
                            }
                        }
                    }

                    if (invading) {
                        for (int y = (int) position.getY(); y <= position.getY() + player.radius * 2; ++y) {
                            for (int x = (int) position.getX(); x <= position.getX() + player.radius * 2; ++x) {
                                if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                                if (GameHelper.isCollideWithCircleAndRect(new Point2D(position.getX() + player.radius, position.getY() + player.radius), player.radius, new Point2D(x, y), new Point2D(1, 1))) {
                                    snapshot.invArea[y][x] = player.ID;
                                }

                            }
                        }

                        if (!player.invading) {
                            for (int y = (int) player.pre_position.getY(); y <= player.pre_position.getY() + player.radius * 2; ++y) {
                                for (int x = (int) player.pre_position.getX(); x <= player.pre_position.getX() + player.radius * 2; ++x) {
                                    if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                                    if (GameHelper.isCollideWithCircleAndRect(new Point2D(player.pre_position.getX() + player.radius, player.pre_position.getY() + player.radius), player.radius, new Point2D(x, y), new Point2D(1, 1))) {
                                        snapshot.invArea[y][x] = player.ID;
                                    }
                                }
                            }
                        }

                        player.invading = true;
                    }

                    if (player.invading && !invading) {
                        for (int y = (int) position.getY(); y <= position.getY() + player.radius * 2; ++y) {
                            for (int x = (int) position.getX(); x <= position.getX() + player.radius * 2; ++x) {
                                if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                                if (GameHelper.isCollideWithCircleAndRect(new Point2D(position.getX() + player.radius, position.getY() + player.radius), player.radius, new Point2D(x, y), new Point2D(1, 1))) {
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
                        fillInside(searchInside(position, player.ID), player.ID);

                        player.invading = false;
                    }

                    player.pre_position = position;
                }
            }
        } else if (snapshot.gameLevel == 3) {
            if (snapshot.winner == -1) {
                int index = -1;
                for (int i = 0; i < snapshot.player_num; ++i) {
                    if (index == -1 || snapshot.players[i].score > snapshot.players[index].score) index = i;
                }
                snapshot.winner = index;
            }
        }
    }

    public void draw() {
        g.clearRect(0, 0, 640, 480);

        if (snapshot.gameLevel == 0) {
            g.setFill(Color.BLACK);
            g.setFont(new Font(fontPath, 30));
            g.fillText("Connecting", 100, 200);

            for (int i = 0; i < 3; ++i) {
                if (((System.currentTimeMillis() - level1StartTime) / 1000) % 3 >= i)
                    g.fillText(".", 260 + i * 20, 200);
            }
        } else if (snapshot.gameLevel == 1) {
            g.setFill(Color.BLACK);
            g.setFont(new Font(fontPath, 30));

            g.setFont(new Font(fontPath, 100));
            g.fillText(Integer.toString(snapshot.countDown), 300, 250);
        } else if (snapshot.gameLevel == 2) {
            double range = 1.0;
            for (int y = 0; y < 480 / range; ++y) {
                for (int x = 0; x < 640 / range; ++x) {
                    if (snapshot.conArea[(int) (y * range)][(int) (x * range)] != -1) {
                        int id = snapshot.conArea[(int) (y * range)][(int) (x * range)];
                        g.setFill(snapshot.players[id].color);
                        g.fillRect(x * range, y * range, range, range);
                        //g.setFill(new Color(snapshot.players[id].color.getRed(), snapshot.players[id].color.getGreen(), snapshot.players[id].color.getBlue(), 0.3));
                        //g.fillRect(x * range - range * 2, y * range - range * 2, range * 5, range * 5);
                    }
                    if (snapshot.invArea[(int) (y * range)][(int) (x * range)] != -1) {
                        int id = snapshot.invArea[(int) (y * range)][(int) (x * range)];
                        g.setFill(snapshot.players[id].color);
                        g.fillRect(x * range, y * range, range, range);
                        //g.setFill(new Color(snapshot.players[id].color.getRed(), snapshot.players[id].color.getGreen(), snapshot.players[id].color.getBlue(), 0.3));
                        //g.fillRect(x * range - range * 2, y * range - range * 2, range * 5, range * 5);
                    }
                }
            }

            for (SnapShot.Player player : snapshot.players) {
                g.setFill(player.color);
                g.fillOval(player.position.getX(), player.position.getY(), player.radius * 2, player.radius * 2);
                g.setFill(Color.BLACK);
                g.strokeOval(player.position.getX(), player.position.getY(), player.radius * 2, player.radius * 2);

                g.setFill(player.color);
                g.setFont(new Font(fontPath, 20));
                g.fillText(player.ID + ": " + player.score + "%", 10, 50 + 30 * player.ID);
            }

            g.setFill(Color.BLACK);
            g.setFont(new Font(fontPath, 20));
            g.fillText("Time: " + Math.max(snapshot.leftTime, 0.0), 100, 20);

        } else if (snapshot.gameLevel == 3) {
            g.setFill(snapshot.players[snapshot.winner].color);
            g.setFont(new Font(fontPath, 50));
            g.fillText("Player" + snapshot.players[snapshot.winner].ID + " WIN", 100, 200);
        }
    }
}
