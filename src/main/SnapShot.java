package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.geometry.Point2D;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SnapShot {
    private final Network network;
    private int player_num;
    public Player[] players;
    public int[][] stage;
    public int countDown;
    public int gameLevel = 0;
    private int myID;

    public static class Player {
        public Point2D previous_position;
        public Point2D position;
        public double radius = 5.0;
        public int color = 2;
    }

    SnapShot() {
        this.network = new Network();
        network.connect(gameLevel, myID);

        player_num = 2;
        players = new Player[player_num];
        for (int i = 0; i < player_num; ++i) {
            players[i] = new Player();
        }

        stage = new int[480][];
        for (int y = 0; y < 480; ++y) {
            for (int x = 0; x < 640; ++x) {
                stage[y] = new int[640];
            }
        }
        gameLevel = 0;
    }

    public void getSnapShot() {
        Gson gson = new Gson();
        String data = network.getMessage();
        if (data.equals("")) return;
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        Map<String, String> map = gson.fromJson(data, type);

        gameLevel = Integer.parseInt(map.get("GameLevel"));

        if (gameLevel == 1) {
            countDown = Integer.parseInt(map.get("CountDown"));
        }

        else if (gameLevel == 2) {
            String[] str = map.get("Position").split(";");
            if (str.length != player_num) System.out.println("ERROR! NOT MATCHING PLAYER NUM!");
            for (int i = 0; i < player_num; ++i) {
                players[i].previous_position = players[i].position;
                players[i].position = GameHelper.StringToPoint2D(str[i]);
            }

            String stage_str = map.get("Stage");

        }
    }

    public void sendSnapShot(Double time, Point2D pos) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Time", time.toString());

        String position = pos.getX() + "," + pos.getY() + ";";
        map.put("Position", position);

        Gson gson = new Gson();
        String sendData = gson.toJson(map);
        network.send(sendData);
    }
}
