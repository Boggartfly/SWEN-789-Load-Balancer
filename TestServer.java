import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;

/**
 *
 */
public class TestServer implements Observer {

    private ServerDetails serverDetails;

    public TestServer(int maxServerConnections) {
        this.serverDetails = new ServerDetails(maxServerConnections);
        initializeView();
    }

    private void initializeView() {
        serverDetails.addObserver(this);
    }

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) {

        TestServer testServer = new TestServer(Integer.parseInt(args[0]));
        testServer.runServer(Integer.parseInt(args[0]));

    }

    private void runServer(int maxServerConnections) {

        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("The load balancer is running.");
            int port = 8180;
            int omgTooManyConnections =
                    maxServerConnections * serverDetails.getServersOnNetworkSize();

            while (serverDetails.getTotalConnections() != omgTooManyConnections) {

                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, port).start();
                serverDetails.incPortConnections(port);
                //check if ports need to be scaled
                if (serverDetails.getTotalConnections() / serverDetails.getMaxConnections() >= 0.75){
                    serverDetails.scaleUp();
                } else if (serverDetails.getTotalConnections() / serverDetails.getMaxConnections() <= 0.375){
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
     * INTERNAL CLASS
     */
    class ClientHandler extends Thread{

        private Socket socket;

        private Integer port;

        public ClientHandler(Socket socket, Integer port) {
            this.socket = socket;
            this.port = port;
        }


        @Override
        public void run() {
            try {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    //do nothing
                }

                if ((inputLine = in.readLine()) == null){
                    throw new SocketException("client exited");
                }

            } catch (SocketException e){
                serverDetails.decPortConnections(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

