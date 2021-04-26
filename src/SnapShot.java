import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SnapShot {
    private final Network network;
    public int player_num;
    public Player[] players;

    public static class Player {
        public Vector2 position;
        public int radius;
        public int color;
    }

    SnapShot(Network network) {
        this.network = network;
        this.player_num = network.sockets.length;
        players = new Player[player_num];
    }

    public void getSnapShot() {
        Gson gson = new Gson();
        for (int id = 0; id < player_num; ++id) {
            String data = network.getMessage(id);
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Map<String, String> map = gson.fromJson(data, type);
            String str = map.get("Position");
            players[id].position = GameHelper.StringToVector2(str.substring(0, str.length() - 1));
        }
    }

    public void sendSnapShot(Double time, int gameLevel, int[][] stage) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Time", time.toString());
        map.put("Player_num", Integer.toString(player_num));

        StringBuilder position = new StringBuilder();
        for (int id = 0; id < player_num; ++id) {
            position.append(players[id].position.getX());
            position.append(",");
            position.append(players[id].position.getY());
            position.append(";");
        }
        map.put("Position", position.toString());

        StringBuilder stage_str = new StringBuilder();
        for (int y = 0; y < stage.length; ++y) {
            for (int x = 0; x < stage[0].length; ++x) {
                stage_str.append(stage[y][x]);
            }
        }
        map.put("Stage", stage_str.toString());

        Gson gson = new Gson();
        String sendData = gson.toJson(map);
        System.out.println("Send: " + sendData);

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
}
