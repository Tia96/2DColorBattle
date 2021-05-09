import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {
    public Socket[] sockets;
    private final BufferedReader[] ins;
    private final PrintWriter[] outs;
    private final List<String[]> messageBoxes = new ArrayList<>();
    private final List<Integer> indexes = new ArrayList<>();

    Network(Socket[] sockets) throws IOException {
        this.sockets = sockets;
        this.ins = new BufferedReader[sockets.length];
        this.outs = new PrintWriter[sockets.length];
        for (int i = 0; i < sockets.length; ++i) {
            ins[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
            outs[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
        }

        for (int i = 0; i < sockets.length; ++i) {
            messageBoxes.add(new String[100]);
            for (int j = 0; j < 100; ++j) messageBoxes.get(i)[j] = "";
            indexes.add(0);
        }
    }

    public void startReceiveMessage(int id) {
        new Thread(() -> {
            while (true) {
                String str = "";
                try {
                    while (!ins[id].ready()) ;
                    str = ins[id].readLine();
                    System.out.println("_in: " + "ID: " + id + " " + str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messageBoxes.get(id)[indexes.get(id)] = str;
                indexes.set(id, indexes.get(id) + 1);
                if (indexes.get(id) == 100) indexes.set(id, 0);
            }
        }).start();
    }

    public String getMessage(int id, int back) {
        int index = indexes.get(id) - 1 - back;
        if (index < 0) index += 100;
        return messageBoxes.get(id)[index];
    }

    public void send(int id, String str) {
        new Thread(() -> {
            outs[id].println(str);
            System.out.println("out: " + "ID: " + id + " " + str);
        }
        ).start();
    }

    public void sendAll(String str) {
        for (int id = 0; id < sockets.length; ++id) {
            send(id, str);
        }
    }
}
