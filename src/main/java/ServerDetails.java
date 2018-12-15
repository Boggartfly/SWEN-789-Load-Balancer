package main.java;

import java.io.IOException;
import java.util.Observable;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Handles server details
 *
 * @author Kirtana Suresh @ RIT SE
 */
public class ServerDetails extends Observable {

    /**
     * contains servers that are up, with their respective number of
     * client connections
     */
    private TreeMap<Integer,Integer> serverConn;

    /**
     * the number of clients connected
     */
    private int totalConnections;

    /**
     * the maximum client connections that can be handled. Decided by the
     * number of servers up
     */
    private float maxConnections;

    /**
     * the servers configured on the network
     */
    private TreeSet<Integer> serversOnNetwork;

    /**
     * the number of connections a single server can handle
     */
    private int maxServerConnections;

    /**
     * initialise
     * @param maxServerConnections the number of connections a single server can handle
     */
    public ServerDetails(int maxServerConnections) {

        //6 web servers on the network, excluding load balancer
        serversOnNetwork = new TreeSet<Integer>();
        serversOnNetwork.add(8180);
        serversOnNetwork.add(8280);
        serversOnNetwork.add(8480);
        serversOnNetwork.add(8580);
        serversOnNetwork.add(8680);

        serverConn = new TreeMap<>();
        serverConn.put(8180, 0);

        this.maxServerConnections = maxServerConnections;

        this.maxConnections = serverConn.size() * maxServerConnections;

    }

    public int getServersOnNetworkSize() {
        return serversOnNetwork.size();
    }

    public float getMaxConnections() {
        return maxConnections;
    }

    private void decTotalConnections() {
        this.totalConnections -= 1;

    }

    private void incTotalConnections() {
        this.totalConnections += 1;

    }

    public int getTotalConnections() {
        return totalConnections;
    }

    /**
     * if a client exits, remove the connection from the web server
     * @param port web server client is connected to
     */
    public void decPortConnections(int port) {
        int count = serverConn.containsKey(port) ? serverConn.get(port) : 0;
        serverConn.put(port, count - 1);
        decTotalConnections();

        this.announceChange();
    }

    /**
     * if a client connects, add the connection to the web server
     * @param port web server client is connected to
     */
    public void incPortConnections(int port) {
        int count = serverConn.getOrDefault(port, 0);
        serverConn.put(port, count + 1);
        incTotalConnections();

        this.announceChange();

    }

    public TreeMap<Integer, Integer> getNumberOfConnections() {
        return serverConn;
    }

    /**
     * starts 1 server everytime 75% of total capacity is reached
     */
    public void scaleUp() {
        int count = 0;
        int portsUp = 0;

        for (int i:
             serversOnNetwork) {
            if (!serverConn.containsKey(i)){
                serverConn.put(i, 0);
                try {
                    if(System.getProperty("os.name").equals("Mac OS X")){
                    Runtime.getRuntime().exec("src/Scripts/" +
                            "start_instance_" + i +
                            ".sh");} else {
                    Runtime.getRuntime().exec("src/Scripts/" +
                            "stop_instance_" + i +
                            ".bat");}
                } catch (IOException e) {
                    e.printStackTrace();
                }

                portsUp++;
                count++;
                if (count == 1){
                    break;
                }
            }
        }

        maxConnections = maxConnections + (maxServerConnections * portsUp);
    }


    /**
     * removes 1 unused server everytime 18.75% of total capacity is used
     */
    public void scaleDown() {
        int count = 0;
        int portsDown = 0;

        for (int i:
                serversOnNetwork) {
            if(serverConn.containsKey(i)) {
                if (serverConn.get(i) == 0) {
                    try {
                        if(System.getProperty("os.name").equals("Mac OS X")){
                        Runtime.getRuntime().exec("src/Scripts/" +
                                "stop_instance_" + i +
                                ".sh");} else {
                        Runtime.getRuntime().exec("src/Scripts/" +
                                "stop_instance_" + i +
                                ".bat");}
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    portsDown++;
                    count++;
                    serverConn.remove(i);
                    if (count == 1) {
                        break;
                    }
                }
            }
        }

        maxConnections = maxConnections - (maxServerConnections * portsDown);
    }


    /**
     * checks for available port/web server
     * @return port of the webserver which has space to accomodate a client
     */
    public int whichPortAvailable(){

        int port = 0;
        for (Integer i:
                serverConn.keySet()) {
            int j = serverConn.get(i);
            if (1 <= j && j < maxServerConnections){
                port = i;
                break;
            } else if (j == 0) {
                port = i;
                break;
            } else {
                continue;
            }
        }
        return port;
    }


    /**
     * announce changes made to the server, to all observers
     */
    private void announceChange() {
        super.setChanged();
        super.notifyObservers();
    }

}
