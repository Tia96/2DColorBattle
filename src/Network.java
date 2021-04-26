import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Network {
    public Socket[] sockets;
    private BufferedReader[] ins;
    private PrintWriter[] outs;
    private List<Deque<String>> messageBoxes;

    Network(Socket[] sockets) throws IOException {
        this.sockets = sockets;
        this.ins = new BufferedReader[sockets.length];
        this.outs = new PrintWriter[sockets.length];
        for (int i = 0; i < sockets.length; ++i) {
            ins[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
            outs[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
        }

        for (int i = 0; i < sockets.length; ++i) {
            messageBoxes.add(new ArrayDeque<String>());
        }
    }

    public void startReceiveMessage(int id) {
        new Thread(() -> {
            while (true) {
                String str = "";
                try {
                    str = ins[id].readLine();
                    while (!ins[id].ready()) ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(id + " " + str);
                messageBoxes.get(id).push(str);
            }
        }).start();
    }

    public String getMessage(int id) {
        return messageBoxes.get(id).pop();
    }

    public void sendAll(String str) {
        for (int i = 0; i < sockets.length; ++i) {
            final int id = i;
            new Thread(() -> outs[id].println(str)).start();
        }
    }
}
