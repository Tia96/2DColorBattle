package main;

import io.reactivex.rxjava3.core.Observable;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Network {
    private static final String SETTING_FILE_PATH = "resources/settings.properties";
    private static final Properties properties = new Properties();

    private static int PORT;
    private static String HOSTNAME;
    public Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String[] messageBox;
    private int index = 0;

    Network() {
        try {
            properties.load(Files.newBufferedReader(Paths.get(SETTING_FILE_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PORT = Integer.parseInt(properties.getProperty("PORT", "60040"));
        HOSTNAME = properties.getProperty("HOSTNAME", "localhost");

        messageBox = new String[100];
        for (int i = 0; i < 100; ++i) {
            messageBox[i] = "";
        }
    }

    public Observable<String> connect() {
        return Observable.create(emitter -> {
            while (true) {
                try {
                    InetAddress addr = InetAddress.getByName(HOSTNAME);
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(addr, PORT), 0);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    while(!in.ready()) ;
                    String result = in.readLine();
                    emitter.onNext(result);
                    break;
                } catch (IOException e) {
                    System.out.println("Connecting to " + HOSTNAME + ": " + PORT);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
    }

    public void startReceiveMessage() {
        new Thread(() -> {
            while (true) {
                String str = "";
                try {
                    while (!in.ready()) ;
                    str = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("_in: " + str);
                messageBox[index] = str;
                index += 1;
                if (index == 100) index = 0;
            }
        }).start();
    }

    public String getMessage() {
        int idx = index - 1;
        if (idx < 0) idx += 100;
        return messageBox[idx];
    }

    public void sendMessage(String str) {
        new Thread(() -> {
            out.println(str);
            System.out.println("out: " + str);
        }
        ).start();
    }
}
