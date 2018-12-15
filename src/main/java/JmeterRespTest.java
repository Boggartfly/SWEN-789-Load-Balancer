package main.java;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
/**
 * Testing file for JMeter responses
 *
 * @author Kirtana Suresh @ RIT SE
 * @author Parth Sane @ RIT SE
 */
public class JmeterRespTest {

    public static void main(String[] args) {

            try {
                ServerSocket serverSock = new ServerSocket(8080);
                //noinspection InfiniteLoopStatement
                while(true) {
                    Socket sock = serverSock.accept();

                    InputStream sis = sock.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(sis));
                    String request = br.readLine();
                    String[] requestParam = request.split(" ");
                    System.out.println("Request Headers: "+Arrays.toString(requestParam));
                    PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

                    // Response is written as HTML
                    out.write("<html>\n" + "<head><title>Test Page</title></head><body>Hello World!</body></html>");
                    br.close();
                    out.close();
                    sock.close();
                }
            } catch (IOException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                // Just press stop in intellij to stop server
            }

    }
}
