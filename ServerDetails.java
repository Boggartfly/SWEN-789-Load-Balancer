import java.io.IOException;
import java.util.Observable;
import java.util.TreeMap;
import java.util.TreeSet;

public class ServerDetails extends Observable {

    private TreeMap<Integer,Integer> serverConn;

    private int totalConnections;

    private float maxConnections;

    private TreeSet<Integer> serversOnNetwork;

    public int maxServerConnections;

    public ServerDetails(int maxServerConnections) {

        serversOnNetwork = new TreeSet<Integer>();
        serversOnNetwork.add(8180);
        serversOnNetwork.add(8280);
        serversOnNetwork.add(8380);
        serversOnNetwork.add(8480);
        serversOnNetwork.add(8580);
        serversOnNetwork.add(8680);

        serverConn = new TreeMap<Integer, Integer>();
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

    public void decTotalConnections() {
        this.totalConnections -= 1;

    }

    public void incTotalConnections() {
        this.totalConnections += 1;

    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public void decPortConnections(int port) {
        int count = serverConn.containsKey(port) ? serverConn.get(port) : 0;
        serverConn.put(port, count - 1);
        decTotalConnections();

        this.announceChange();
    }

    public void incPortConnections(int port) {
        int count = serverConn.containsKey(port) ? serverConn.get(port) : 0;
        serverConn.put(port, count + 1);
        incTotalConnections();

        this.announceChange();

    }

    public TreeMap<Integer, Integer> getNumberOfConnections() {
        return serverConn;
    }

    public void scaleUp() {
        int count = 0;
        int portsUp = 0;

        for (int i:
             serversOnNetwork) {
            if (!serverConn.containsKey(i)){
                serverConn.put(i, 0);
                try {
                    Runtime.getRuntime().exec("/Users/kirtanasuresh/Documents" +
                            "/Scripts/" +
                            "start_instance_" + i +
                            ".sh");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                portsUp++;
                count++;
                if (count == 2){
                    break;
                }
            }
        }

        maxConnections = maxConnections + (maxServerConnections * portsUp);

    }


    public void scaleDown() {
        int count = 0;
        int portsDown = 0;

        for (int i:
                serversOnNetwork) {
            if(serverConn.containsKey(i)) {
                if (serverConn.get(i) == 0) {
                    try {
                        Runtime.getRuntime().exec("/Users/kirtanasuresh/Documents" +
                                "/Scripts/" +
                                "stop_instance_" + i +
                                ".sh");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    portsDown++;
                    count++;
                    serverConn.remove(i);
                    if (count == 2) {
                        break;
                    }
                }
            }
        }

        maxConnections = maxConnections - (maxServerConnections * portsDown);
    }


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


    private void announceChange() {
        super.setChanged();
        super.notifyObservers();
    }

}
