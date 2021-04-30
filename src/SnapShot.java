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
        public int ID;
        public Vector2 position;
        public List<Vector2> inv_positions = new ArrayList<>();
        public boolean invading = false;
        public double radius = 5.0;
        public int color = 2;
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
            players[i] = new Player();
            players[i].position = new Vector2(rand.nextInt(640), rand.nextInt(480));

            Vector2 position = players[i].position;
            for (int y = (int) position.getY() - 50; y <= position.getY() + 50; ++y) {
                for (int x = (int) position.getX() - 50; x <= position.getX() + 50; ++x) {
                    if (y < 0 || y >= 480 || x < 0 || x >= 640) continue;
                    conArea[y][x] = i + 1;
                    updateStage.append("0,").append(x).append(",").append(y).append(",").append(players[i].color).append(";");
                }
            }

            players[i].ID = i + 1;
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
            for (int id = 0; id < player_num; ++id) {
                String data = network.getMessage(id, 0);
                if (data.equals("")) return;
                Map<String, String> map = gson.fromJson(data, type);
                String str = map.get("Position");
                players[id].position = GameHelper.StringToVector2(str.substring(0, str.length() - 1));
            }
        }
    }

    public void sendSnapShot(Double time) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Time", time.toString());
        map.put("GameLevel", Integer.toString(gameLevel));

        StringBuilder position = new StringBuilder();
        for (int id = 0; id < player_num; ++id) {
            position.append(players[id].position.getX()).append(",").append(players[id].position.getY()).append(";");
        }
        map.put("Position", position.toString());

        map.put("Stage", updateStage.toString());

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

        for (int id = 0; id < player_num; ++id) {
            map.put("ID", Integer.toString(id + 1));
            map.put("Player_num", Integer.toString(player_num));

            String position = players[id].position.getX() + "," + players[id].position.getY() + ";";
            map.put("Position", position);
            map.put("Stage", updateStage.toString());
            String sendData = gson.toJson(map);
            network.send(id, sendData);
        }
    }
}
