package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Client connects to server. It needs to exit on its own. Server will not
 * disconnect it
 *
 * @author Kirtana Suresh @ RIT SE
 */
class TestClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            String serverInput;
            while ((serverInput = in.readLine()) != null) {
                //do nothing
            }


        } catch (IOException e){
            e.printStackTrace();
        }
    }
}