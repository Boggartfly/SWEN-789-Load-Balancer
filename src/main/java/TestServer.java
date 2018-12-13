package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Sets up the server
 *
 * @author Kirtana Suresh @ RIT SE
 */
public class TestServer implements Observer {

    /**
     * Server details
     */
    private ServerDetails serverDetails;

    /**
     * If load increases above it, scale up
     */
    private final double LOAD_FACTOR = 0.75;

    /**
     * initialises Test Server
     * @param maxServerConnections the number of connections a single server
     *                             can handle
     */
    public TestServer(int maxServerConnections) {
        this.serverDetails = new ServerDetails(maxServerConnections);
        initializeView();
    }

    /**
     * Observe changes made to server details
     */
    private void initializeView() {
        serverDetails.addObserver(this);
    }

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) {

        int maxServerConnections;
        if (args.length == 0){
            System.out.println("By default, load balancer assigns maximum of " +
                    "10 connections to each server");
            maxServerConnections = 10;
        } else {
            maxServerConnections = Integer.parseInt(args[0]);
        }

        TestServer testServer = new TestServer(maxServerConnections);
        testServer.runServer(maxServerConnections);

    }

    /**
     * Starts the load balancer
     * @param maxServerConnections the number of connections a single server
     *                             can handle
     */
    private void runServer(int maxServerConnections) {

        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("The load balancer is running.");
            int port = 8180;

            //only 6 servers available on network
            //if each server can only handle 10 connections for example, the
            // total connections that can be made are 60
            int omgTooManyConnections =
                    maxServerConnections * serverDetails.getServersOnNetworkSize();


            while (serverDetails.getTotalConnections() != omgTooManyConnections) {

                Socket clientSocket = serverSocket.accept();

                //will run till client disconnects from server
                new ClientHandler(clientSocket, port).start();
                serverDetails.incPortConnections(port);

                //check if ports need to be scaled

                if (serverDetails.getTotalConnections() / serverDetails.getMaxConnections() >= LOAD_FACTOR){
                    System.out.println("scaling up");
                    serverDetails.scaleUp();
                } else if (serverDetails.getTotalConnections() / serverDetails.getMaxConnections() < (LOAD_FACTOR / 4) ){
                    System.out.println("scaling down");
                    serverDetails.scaleDown();
                }
                //which port to send next client to
                port = serverDetails.whichPortAvailable();

            }

            System.out.println("Too many client connections, server can't " +
                    "handle load");

        } catch (Exception e){
            System.out.println("Server going down");
            e.printStackTrace();
        } finally {
            System.out.println("load balancer is down");
        }

    }


    /**
     * This method is called whenever the observed object is changed. An
     * application calls an {@code Observable} object's
     * {@code notifyObservers} method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the {@code notifyObservers}
     */
    public void update(Observable o, Object arg) {
        assert o == this.serverDetails: "Unexpected subject of observation";

        System.out.println( "port connections: " + this.serverDetails.getNumberOfConnections() );
        System.out.println( "total connections: " + this.serverDetails.getTotalConnections());
    }


    /**
     * INTERNAL CLASS TO HANDLE CLIENT CONNECTIONS
     */
    class ClientHandler extends Thread{

        /**
         * client socket
         */
        private Socket socket;

        /**
         * web server the client is assigned to by the load balancer
         */
        private Integer port;

        /**
         * initialise variables
         * @param socket client socket
         * @param port assigned web server
         */
        public ClientHandler(Socket socket, Integer port) {
            this.socket = socket;
            this.port = port;
        }


        /**
         * Starts client thread, will run till client disconnects from web
         * server
         */
        @Override
        public void run() {
            try {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String inputLine;

                //OLD CODE/////
                /*while ((inputLine = in.readLine()) != null) {
                    //do nothing
                }

                if ((inputLine = in.readLine()) == null){
                    throw new SocketException("client exited");
                }*/


                //NEW CODE/////
                //read input from client
                inputLine = in.readLine();
                String[] requestParam = inputLine.split(" ");

                PrintWriter out = new PrintWriter(socket.getOutputStream(),
                        true);

                //send html output from the webserver client is assigned to
                StringBuilder urlData = urlReader(port);
                out.write(urlData.toString());

                while ((inputLine = in.readLine()) != null) {
                    //do nothing
                }

                if ((inputLine = in.readLine()) == null){
                    throw new SocketException("client exited");
                }



            } catch (SocketException e){
                //remove the client from its assigned web server
                serverDetails.decPortConnections(port);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }


        /**
         * This method reads from the url and returns html content to client
         * @param port the port from where the html response is sent to the
         *             client
         * @return the html webpage in StringBuilder format
         */
        private StringBuilder urlReader (int port){
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            try {

                URL url = new URL("http://localhost:" + port + "/");
                br = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;

                sb = new StringBuilder();

                while ((line = br.readLine()) != null) {

                    sb.append(line);
                    sb.append(System.lineSeparator());
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb;
        }
    }


}

