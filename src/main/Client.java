package main;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static java.lang.Thread.sleep;

public class Client {
    private static final int PORT = 60040;

    public static void main(String[] args) throws IOException {
        InetAddress addr = InetAddress.getByName("localhost");
        System.out.println("address = " + addr);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(addr, PORT), 0);
            System.out.println("socket = " + socket);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            Thread.sleep(5000);
            System.out.println("closing: " + socket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
