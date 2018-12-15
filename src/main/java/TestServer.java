package main.java;

import java.io.*;
import java.net.*;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

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
     * Headers for HTTP response
     */
    private static final String OUTPUT_HEADERS = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: ";

    /**
     * Header end for HTTP response
     */
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";

    /**
     * Stores the client connections in queue until serviced
     */
    private ConcurrentLinkedQueue<Socket> clients;


    /**
     * Initialises Test Server
     *
     * @param maxServerConnections the number of connections a single server
     *                             can handle
     */
    public TestServer(int maxServerConnections) {
        this.serverDetails = new ServerDetails(maxServerConnections);
        this.clients = new ConcurrentLinkedQueue<>();
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
        if (args.length == 0) {
            System.out.println("By default, load balancer assigns maximum of " +
                    "100 connections to each server");
            maxServerConnections = 100;
        } else {
            maxServerConnections = Integer.parseInt(args[0]);
        }

        TestServer testServer = new TestServer(maxServerConnections);
        testServer.runServer(maxServerConnections);

    }

    /**
     * Starts the load balancer
     *
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


            //Thread that handles incoming client connections and puts them
            // in a queue
            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        clients.add(serverSocket.accept());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

            //Handle the client connections by popping them from the queue.
            while (true) {

                if (serverDetails.getTotalConnections() != omgTooManyConnections
                        && clients.size() > 0) {

                    //which port to send next client to
                    port = serverDetails.whichPortAvailable();


                    //hand over the web page from respective web server port,
                    // to client
                    new ClientHandler(clients.poll(), port).start();
                    serverDetails.incPortConnections(port);


                    //check if ports need to be scaled
                    if (serverDetails.getTotalConnections() / serverDetails.getMaxConnections() >= LOAD_FACTOR) {
                        System.out.println("scaling up");
                        serverDetails.scaleUp();
                    } else if (serverDetails.getTotalConnections() / serverDetails.getMaxConnections() < (LOAD_FACTOR / 4)) {
                        System.out.println("scaling down");
                        serverDetails.scaleDown();
                    }

                }

            }

        } catch (Exception e) {
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
        assert o == this.serverDetails : "Unexpected subject of observation";

        System.out.println();
        System.out.println("port connections: " + this.serverDetails.getNumberOfConnections());
        System.out.println("total connections: " + this.serverDetails.getTotalConnections());
    }


    /**
     * INTERNAL CLASS TO HANDLE CLIENT CONNECTIONS
     */
    class ClientHandler extends Thread {

        /**
         * client clientSocket
         */
        private Socket clientSocket;

        /**
         * web server the client is assigned to by the load balancer
         */
        private Integer port;


        /**
         * initialise variables
         *
         * @param clientSocket client clientSocket
         * @param port   assigned web server
         */
        public ClientHandler(Socket clientSocket, Integer port) {
            this.clientSocket = clientSocket;
            this.port = port;
        }


        /**
         * Starts client thread, will run till response is sent back to client
         */
        @Override
        public void run() {
            try {

                //read client input
                InputStream in = clientSocket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty()) {
                        break;
                    }
                }

                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(
                                new BufferedOutputStream(clientSocket.getOutputStream()),
                                "UTF-8")
                );

                //sleep for 5 secs in case server is being brought up
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //send html output from the webserver client is assigned to
                StringBuilder urlData = urlReader(port);

                out.write(OUTPUT_HEADERS + urlData.length() + OUTPUT_END_OF_HEADERS + urlData);
                out.flush();
                out.close();

            } catch (SocketException e) {
                e.getMessage();
            } catch (IOException e) {
                e.getMessage();
            } catch (Exception e){
                e.getMessage();
            } finally {
                try {
                    clientSocket.close();
                    //remove the client from its assigned web server
                    serverDetails.decPortConnections(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        /**
         * This method reads from the url and returns html content to client
         *
         * @param port the port from where the html response is sent to the
         *             client
         * @return the html webpage in StringBuilder format
         */
        private StringBuilder urlReader(int port) {
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
                e.getMessage();
            } catch (IOException e) {
                e.getMessage();
            }catch (Exception e){e.getMessage();
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

