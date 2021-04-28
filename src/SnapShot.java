import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SnapShot {
    private final Network network;
    public int player_num;
    public Player[] players;
    public int gameLevel = 1;
    private final Type type = new TypeToken<HashMap<String, String>>() {
    }.getType();

    public static class Player {
        public Vector2 position;
        public double radius = 5.0;
        public int color = 2;
    }

    SnapShot(Network network) {
        this.network = network;
        this.player_num = network.sockets.length;
        players = new Player[player_num];

        Random rand = new Random();
        for (int i = 0; i < player_num; ++i) {
            players[i] = new Player();
            players[i].position = new Vector2(rand.nextInt(640), rand.nextInt(480));
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
            position.append(players[id].position.getX());
            position.append(",");
            position.append(players[id].position.getY());
            position.append(";");
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

        for (int id = 0; id < player_num; ++id) {
            map.put("ID", Integer.toString(id));
            map.put("Player_num", Integer.toString(player_num));

            String position = players[id].position.getX() + "," + players[id].position.getY() + ";";
            map.put("Position", position);
            String sendData = gson.toJson(map);
            network.send(id, sendData);
        }
    }
}
