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

    public static class Player {
        public Point2D position;
        public int radius;
        public int color;
    }

    SnapShot() {
        this.network = new Network();
        network.connect(gameLevel);

        player_num = 2;
        players = new Player[player_num];
        gameLevel = 0;
    }

    public void getSnapShot() {
        Gson gson = new Gson();
        String data = network.getMessage();
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
                players[i].position = GameHelper.StringToPoint2D(str[i]);
            }

            String stage_str = map.get("Stage");
            for (int y = 0; y < 480; ++y) {
                for (int x = 0; x < 640; ++x) {
                    stage[y][x] = Character.getNumericValue(stage_str.charAt(y * 640 + x));
                }
            }
        }
    }

    public void sendSnapShot(Double time, Point2D pos) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Time", time.toString());

        String position = pos.getX() + "," + pos.getY() + ";";
        map.put("Position", position);

        //network.send(map)
    }
}
