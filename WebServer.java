import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple
 * chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 */
public class WebServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 8080;

    /**
     * The set of all names of clients in the load balancer.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * List of all available ports
     */
    private static int[] allPorts = {8180, 8280};

    /**
     * Keeps track of client connections to web page
     */
    private static int numberOfClients = 0;

    /**
     * Keeps track of number of connections to each port
     */
    private static int portConnectionCount = 0;

    /**
     *
     */
    private static Integer port = 0;

    /**
     * <Port, Number of Connections in each port>
     */
    private static HashMap<Integer, Integer> portConnections = new HashMap<Integer, Integer>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The load balancer is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {

                numberOfClients++;

                if (portConnectionCount == 10){
                    portConnectionCount = 0;
                } else {
                    portConnectionCount++;
                }

                port = allPorts[numberOfClients/10];
                System.out.println(port);

                if(portConnections.containsKey(port)){
                    Integer oldValue = portConnections.get(port);
                    portConnections.put(port, ++oldValue);
                } else {
                    portConnections.put(port,
                            portConnectionCount);
                }

                System.out.println(numberOfClients +" "+ portConnectionCount);

                System.out.println(portConnections.keySet());
                System.out.println(portConnections.values());

                new ClientHandle(listener.accept()).start();

                System.out.println("after thread");
                System.out.println(numberOfClients +" "+ portConnectionCount);

                System.out.println(portConnections.keySet());
                System.out.println(portConnections.values());

            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class ClientHandle extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public ClientHandle(Socket socket) {
            this.socket = socket;

            this.name = port.toString()+ portConnectionCount;
            names.add(name);
            System.out.println(name);

        }


        /**
         * Services this thread's client by displaying a web page fom a
         * particular server.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                writers.add(out);

                out.println(port.toString());

                while (true) {
                    String input = in.readLine();
                    if("BYE".equals(input)){
                        break;
                    }
                }


            } catch (IOException e) {
                System.out.println(e);
            } finally {

                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                System.out.println("in finally block");
                if (name != null) {
                    names.remove(name);
                    numberOfClients--;
                    portConnectionCount--;
                    Integer oldValue = portConnections.get(port);
                    portConnections.put(port, --oldValue);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
