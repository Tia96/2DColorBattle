package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;
import java.util.*;

public class SnapShot {
    private final Network network;
    public int player_num;
    public Player[] players;
    public int countDown;
    public int gameLevel = 0;
    public int myID;
    public int[][] conArea;
    public int[][] invArea;
    public double leftTime;

    public static class Player {
        public int ID;
        public Point2D pre_position;
        public Point2D position;
        public double radius;
        public Color color;
        public boolean invading;
        public double score;

        Player(int id, Point2D pos, double radius, int color) {
            this.ID = id;
            this.position = pos;
            this.radius = radius;
            this.invading = false;
            this.score = 0;

            this.color = switch (color) {
                case 0 -> Color.WHITE;
                case 1 -> Color.BLACK;
                case 2 -> Color.RED;
                case 3 -> Color.BLUE;
                default -> throw new IllegalStateException("Unexpected value: " + color);
            };
        }
    }

    SnapShot() {
        this.network = new Network();

        network.connect().subscribeOn(Schedulers.newThread()).subscribe(result -> {
            System.out.println("_in: " + result);
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Map<String, String> map = gson.fromJson(result, type);
            gameLevel = 1;
            myID = Integer.parseInt(map.get("ID"));
            player_num = Integer.parseInt(map.get("Player_num"));
            players = new Player[player_num];
            leftTime = Double.parseDouble(map.get("LeftTime"));

            String[] positions = map.get("Position").split(";");
            String[] colors = map.get("Color").split(";");
            for (int i = 0; i < player_num; ++i) {
                Point2D pos = GameHelper.StringToPoint2D(positions[i]);
                players[i] = new Player(i, pos, 5.0, Integer.parseInt(colors[i]));
                for (int y = (int) pos.getY() - 25; y <= pos.getY() + 25; ++y) {
                    for (int x = (int) pos.getX() - 25; x <= pos.getX() + 25; ++x) {
                        if ( Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2) <= 25 * 25) conArea[y][x] = i;
                    }
                }
            }

            System.out.println("Connect: " + network.socket + " ID: " + myID);
            network.startReceiveMessage();
        }, Throwable::printStackTrace);

        gameLevel = 0;
        conArea = new int[480][];
        invArea = new int[480][];
        for (int y = 0; y < 480; ++y) {
            conArea[y] = new int[640];
            invArea[y] = new int[640];
            for (int x = 0; x < 640; ++x) {
                conArea[y][x] = -1;
                invArea[y][x] = -1;
            }
        }
    }

    public void getSnapShot() {
        Gson gson = new Gson();
        String message = network.getMessage();
        if (message.equals("")) return;
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        Map<String, String> map = gson.fromJson(message, type);

        gameLevel = Integer.parseInt(map.get("GameLevel"));

        if (gameLevel == 1) {
            countDown = Integer.parseInt(map.get("CountDown"));
        } else if (gameLevel == 2) {
            String[] positions = map.get("Position").split(";");
            String[] scores = map.get("Score").split(";");
            for (int i = 0; i < player_num; ++i) {
                players[i].pre_position = players[i].position;
                players[i].position = GameHelper.StringToPoint2D(positions[i]);
                players[i].score = Double.parseDouble(scores[i]);
            }
            leftTime = Double.parseDouble(map.get("LeftTime"));
        }
    }

    public void sendSnapShot(Double time, Point2D position) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Time", time.toString());

        String pos_str = String.format("%.1f", position.getX()) + "," + String.format("%.1f", position.getY()) + ";";
        map.put("Position", pos_str);

        Gson gson = new Gson();
        String sendData = gson.toJson(map);
        network.sendMessage(sendData);
    }
}
