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
    private Deque<String> messageBox = new ArrayDeque<>();

    public void connect(Integer gameLevel, Integer myID) {
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
            Integer gameLevel, myID;

            InnerClass(Integer tmp, Integer tmp2) {
                gameLevel = tmp;
                myID = tmp2;
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        InetAddress addr = InetAddress.getByName("localhost");
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(addr, PORT), 0);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        myID = 0;
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
                startReceiveMessage();
                gameLevel = 1;
            }
        }

        InnerClass i = new InnerClass(gameLevel, myID);
        new Thread(i).start();
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
