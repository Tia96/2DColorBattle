import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server {
    private static final String SETTING_FILE_PATH = "resources/settings.properties";
    private static final Properties properties = new Properties();

    public static int PORT;
    private final List<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            properties.load(Files.newBufferedReader(Paths.get(SETTING_FILE_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PORT = Integer.parseInt(properties.getProperty("PORT", "60040"));

        Server server = new Server();
        server.connect();
        server.start();
    }

    private void connect() throws IOException {
        System.out.println("Server accepting. PORT: " + PORT);
        ServerSocket s = new ServerSocket(PORT);
        while (sockets.size() < 2) {
            Socket socket = s.accept();
            System.out.println("Connect: " + socket);
            sockets.add(socket);
        }
    }

    private void start() throws IOException, InterruptedException {
        GameManager gameManager = GameManager.getInstance(sockets.toArray(new Socket[]{}));
        gameManager.start();
    }
}
