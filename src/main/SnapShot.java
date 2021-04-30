package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.geometry.Point2D;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SnapShot {
    private final Network network;
    public int player_num;
    public Player[] players;
    public int countDown;
    public int gameLevel = 0;
    public int myID;
    public int[][] conArea;
    public int[][] invArea;

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
            players[myID - 1].position = GameHelper.StringToPoint2D(str.substring(0, str.length() - 1));

            String[] stages = map.get("Stage").split(";");
            for (String stage_str: stages) {
                Integer[] details = Arrays.asList(stage_str.split(",")).stream().map(Integer::parseInt).toArray(Integer[]::new);
                if (details.length != 4) System.out.println("ERROR! NOT MATCHING STAGE SHAPE");
                int x = details[1], y = details[2], color = details[3];
                conArea[y][x] = color;
            }

            System.out.println("Connect: " + network.socket + " ID: " + myID);
            network.startReceiveMessage();
        }, Throwable::printStackTrace);

        gameLevel = 0;
        conArea = new int[480][];
        invArea = new int[480][];
        for (int y = 0; y < 480; ++y) {
            for (int x = 0; x < 640; ++x) {
                conArea[y] = new int[640];
                invArea[y] = new int[640];
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
            if (positions.length != player_num) System.out.println("ERROR! NOT MATCHING PLAYER NUM!");
            for (int i = 0; i < player_num; ++i) {
                players[i].previous_position = players[i].position;
                players[i].position = GameHelper.StringToPoint2D(positions[i]);
            }

            if (!map.get("Stage").equals("")) {
                String[] stages = map.get("Stage").split(";");
                for (String stage_str : stages) {
                    Integer[] details = Arrays.asList(stage_str.split(",")).stream().map(Integer::parseInt).toArray(Integer[]::new);
                    if (details.length != 4) System.out.println("ERROR! NOT MATCHING STAGE SHAPE");
                    int areaType = details[0], x = details[1], y = details[2], color = details[3];
                    if (areaType == 0) {
                        conArea[y][x] = color;
                    } else {
                        invArea[y][x] = color;
                    }
                }
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
