package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
    public int myID;

    public static class Player {
        public Point2D previous_position;
        public Point2D position;
        public double radius = 5.0;
        public int color = 2;
    }

    SnapShot() {
        this.network = new Network();

        network.connect().subscribeOn(Schedulers.newThread()).subscribe(result -> {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Map<String, String> map = gson.fromJson(result, type);
            gameLevel = 1;
            myID = Integer.parseInt(map.get("ID"));
            player_num = Integer.parseInt(map.get("Player_num"));
            players = new Player[player_num];
            for (int i = 0; i < player_num; ++i) {
                players[i] = new Player();
            }
            String str = map.get("Position");
            players[myID].position = GameHelper.StringToPoint2D(str.substring(0, str.length() - 1));
            System.out.println("Connect: " + network.socket + " ID: " + myID);
            network.startReceiveMessage();
        }, Throwable::printStackTrace);

        gameLevel = 0;
        stage = new int[480][];
        for (int y = 0; y < 480; ++y) {
            for (int x = 0; x < 640; ++x) {
                stage[y] = new int[640];
            }
        }
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
        } else if (gameLevel == 2) {
            String[] str = map.get("Position").split(";");
            if (str.length != player_num) System.out.println("ERROR! NOT MATCHING PLAYER NUM!");
            for (int i = 0; i < player_num; ++i) {
                players[i].previous_position = players[i].position;
                players[i].position = GameHelper.StringToPoint2D(str[i]);
            }
        }
    }

    public void sendSnapShot(Double time, Point2D position) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Time", time.toString());

        String pos_str = position.getX() + "," + position.getY() + ";";
        map.put("Position", pos_str);

        Gson gson = new Gson();
        String sendData = gson.toJson(map);
        network.send(sendData);
    }
}
