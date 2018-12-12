package main.java;

import java.io.IOException;
import java.net.ServerSocket;

import java.net.Socket;

public class JmeterRespTest {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);

            System.out.println("server up");
            Socket client = serverSocket.accept();

            System.out.println("here");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
