import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public final int PORT = 60040;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.connect();
    }

    private void connect() throws IOException {
        try (ServerSocket s = new ServerSocket(PORT)) {
            System.out.println("Started: " + s);
            try (Socket socket = s.accept()) {
                System.out.println("Connection accepted: " + socket);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                GameManager gameManager = GameManager.getInstance();
//                    String str = in.readLine();
//                    if(str.equals("END")) break;
//                    System.out.println("Echoing: " + str);
//                    out.println(str);
                System.out.println("yes");
                gameManager.init();
                gameManager.start();
                System.out.println("closing: " + socket);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
