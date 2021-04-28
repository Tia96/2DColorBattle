package main;

import io.reactivex.rxjava3.core.Observable;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class Network {
    private static final int PORT = 60040;
    public Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Deque<String> messageBox = new ArrayDeque<>();

    public Observable<String> connect() {
        return Observable.create(emitter -> {
            while (true) {
                try {
                    InetAddress addr = InetAddress.getByName("localhost");
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(addr, PORT), 0);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    String result = in.readLine();
                    emitter.onNext(result);
                    break;
                } catch (IOException e) {
                    System.out.println("Connecting ...");
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
                System.out.println("in: " + str);
                messageBox.push(str);
            }
        }).start();
    }

    public String getMessage() {
        if (messageBox.size() == 0) return "";
        return messageBox.getFirst();
    }

    public void send(String str) {
        new Thread(() -> {
            out.println(str);
            System.out.println("out: " + " " + str);
        }
        ).start();
    }
}
