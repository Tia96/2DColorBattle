import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Network {
    public Socket[] sockets;
    private BufferedReader[] ins;
    private PrintWriter[] outs;
    private List<Deque<String>> messageBoxes = new ArrayList<>();

    Network(Socket[] sockets) throws IOException {
        this.sockets = sockets;
        this.ins = new BufferedReader[sockets.length];
        this.outs = new PrintWriter[sockets.length];
        for (int i = 0; i < sockets.length; ++i) {
            ins[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
            outs[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
        }

        for (int i = 0; i < sockets.length; ++i) {
            messageBoxes.add(new ArrayDeque<>());
        }
    }

    public void startReceiveMessage(int id) {
        new Thread(() -> {
            while (true) {
                String str = "";
                try {
                    while (!ins[id].ready()) ;
                    str = ins[id].readLine();
                    System.out.println("in: " + "ID: " + id + " " + str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messageBoxes.get(id).push(str);
            }
        }).start();
    }

    public String getMessage(int id) {
        if (messageBoxes.get(id).size() == 0) return "";
        return messageBoxes.get(id).getFirst();
    }

    public void sendAll(String str) {
        for (int i = 0; i < sockets.length; ++i) {
            final int id = i;
            new Thread(() -> {
                outs[id].println(str);
                System.out.println("out: " + "ID: " + id + " " + str);
            }
            ).start();
        }
    }
}
