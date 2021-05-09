import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class SnapShot {
    private final Network network;
    public int player_num;
    public Player[] players;
    public int gameLevel = 1;
    private final Type type = new TypeToken<HashMap<String, String>>() {
    }.getType();
    public StringBuilder updateStage = new StringBuilder();
    public int[][] conArea;
    public int[][] invArea;

    public static class Player {
        public final int ID;
        public Vector2 position;
        public List<Vector2> inv_positions = new ArrayList<>();
        public boolean invading = false;
        public double radius = 5.0;
        public int color = 2;

        Player(int id, Vector2 pos, double radius, int color) {
            this.ID = id;
            this.position = pos;
            this.radius = radius;
            this.color = color;
        }
    }

    SnapShot(Network network) {
        this.network = network;
        this.player_num = network.sockets.length;
        players = new Player[player_num];

        conArea = new int[480][];
        invArea = new int[480][];
        for (int i = 0; i < 480; ++i) {
            conArea[i] = new int[640];
            invArea[i] = new int[640];
        }

        Random rand = new Random();
        for (int i = 0; i < player_num; ++i) {
            double pi = 2 * Math.PI / player_num * i;
            Vector2 pos = new Vector2(320 + 200.0 * Math.cos(pi), 240 + 200.0 * Math.sin(pi));
            System.out.println("Debug: " + pos.getX() + " " + pos.getY());
            players[i] = new Player(i, pos, 5.0, rand.nextInt(3) + 1);
            for (int y = (int) pos.getY() - 25; y <= pos.getY() + 25; ++y) {
                for (int x = (int) pos.getX() - 25; x <= pos.getX() + 25; ++x) {
                    if (Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2) <= 25 * 25) conArea[y][x] = i;
                }
            }
        }
    }

    public void rollbackPlayer(int id, int back) {
        Gson gson = new Gson();
        String data = network.getMessage(id, back);
        Map<String, String> map = gson.fromJson(data, type);
        String str = map.get("Position");
        players[id].position = GameHelper.StringToVector2(str.substring(0, str.length() - 1));
    }

    public void getSnapShot() {
        if (gameLevel == 2) {
            Gson gson = new Gson();
            for (Player player : players) {
                String data = network.getMessage(player.ID, 0);
                if (data.equals("")) return;
                Map<String, String> map = gson.fromJson(data, type);
                String str = map.get("Position");
                player.position = GameHelper.StringToVector2(str.substring(0, str.length() - 1));
            }
        }
    }

    public void sendSnapShot(Double time) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Time", time.toString());
        map.put("GameLevel", Integer.toString(gameLevel));

        StringBuilder position = new StringBuilder();
        for (Player player : players) {
            String x = String.format("%.1f", player.position.getX()), y = String.format("%.1f", player.position.getY());
            position.append(x).append(",").append(y).append(";");
        }
        map.put("Position", position.toString());

        Gson gson = new Gson();
        String sendData = gson.toJson(map);

        network.sendAll(sendData);
    }

    public void sendGameStart() throws InterruptedException {
        HashMap<String, String> map = new HashMap<>();
        Gson gson = new Gson();

        for (int time = 5; time >= 0; --time) {
            map.put("GameLevel", "1");
            map.put("CountDown", Integer.toString(time));
            String sendData = gson.toJson(map);
            network.sendAll(sendData);
            Thread.sleep(1000);
        }
    }

    public void sendFirstData() {
        HashMap<String, String> map = new HashMap<>();
        Gson gson = new Gson();

        for (Player player : players) {
            map.put("ID", Integer.toString(player.ID));
            map.put("Player_num", Integer.toString(player_num));

            StringBuilder position = new StringBuilder(), color = new StringBuilder();
            for (Player player_tmp : players) {
                String x = String.format("%.1f", player_tmp.position.getX()), y = String.format("%.1f", player_tmp.position.getY());
                position.append(x).append(",").append(y).append(";");
                color.append(player_tmp.color).append(";");
            }
            map.put("Position", position.toString());
            map.put("Color", color.toString());
            String sendData = gson.toJson(map);
            network.send(player.ID, sendData);
        }
    }
}
