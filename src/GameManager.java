import java.io.*;
import java.net.Socket;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private Socket[] sockets;
    private BufferedReader[] ins;
    private PrintWriter[] outs;
    private Vector2[] positions;
    private boolean gameLoop;
    private final double targetFPS = 30.0;

    private GameManager() {
    }

    public static GameManager getInstance(Socket[] sockets) throws IOException {
        INSTANCE.gameLoop = true;
        INSTANCE.sockets = sockets;
        for (int i = 0; i < sockets.length; ++i) {
            INSTANCE.ins[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
            INSTANCE.outs[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
        }
        return INSTANCE;
    }

    private void init() {

    }

    public void start() throws InterruptedException {
        synchronizedStartGame();

        int frames = 0;
        long startTime = System.currentTimeMillis();
        double targetFPSTime = 1000 / targetFPS;
        while (INSTANCE.gameLoop) {
            long sT = System.currentTimeMillis();
            ++frames;



            long elapsedTime = System.currentTimeMillis() - sT;
            if (elapsedTime < targetFPSTime) Thread.sleep((long) (targetFPSTime - elapsedTime));
            System.out.println("fps: " + (double) frames / (System.currentTimeMillis() - startTime) * 1000);
        }
    }

    private void send(int ID, String str) {
        outs[ID].println(str);
    }

    private String receive(int ID) throws IOException {
        return ins[ID].readLine();
    }

    private void synchronizedStartGame() throws InterruptedException {
        for (int time = 5; time>= 0; ++time) {
            for (int i = 0; i < sockets.length; ++i) {
                send(i, Integer.toString(time));
            }
            Thread.sleep(1000);
        }
    }
}
