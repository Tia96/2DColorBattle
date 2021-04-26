import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {
    public final int PORT = 60040;
    private List<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
        server.connect();
        server.start();
    }

    private void connect() throws IOException {
        System.out.println("Server accepting");
        ServerSocket s = new ServerSocket(PORT);
        while(sockets.size() < 2){
            Socket socket = s.accept();
            System.out.println("Connect: " + socket);
            sockets.add(socket);
        }
    }

    private void start() throws IOException, InterruptedException {
        GameManager gameManager = GameManager.getInstance((Socket[]) sockets.toArray());
        gameManager.start();
    }
}
