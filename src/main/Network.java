package main;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class Network {
    private static final int PORT = 60040;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Deque<String> messageBox = new ArrayDeque<String>();

    public void connect(Integer gameLevel) {
//        new Thread(() -> {
//            try {
//                InetAddress addr = InetAddress.getByName("localhost");
//                socket = new Socket();
//                socket.connect(new InetSocketAddress(addr, PORT), 0);
//                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("Connect: " + socket);
//            gameLevel = 1;
//        }).start();

        class InnerClass implements Runnable {
            Integer tmp;
            InnerClass(Integer tmp) {this.tmp = tmp;}

            @Override
            public void run() {
                while (true) {
                    try {
                        InetAddress addr = InetAddress.getByName("localhost");
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(addr, PORT), 0);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
                System.out.println("Connect: " + socket);
                tmp = 1;
            }
        }

        InnerClass i = new InnerClass(gameLevel);
        new Thread(i).start();
    }

    public void startReceiveMessage() {
        new Thread(() -> {
            while (true) {
                String str = "";
                try {
                    str = in.readLine();
                    while (!in.ready()) ;
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
        return messageBox.pop();
    }
}
