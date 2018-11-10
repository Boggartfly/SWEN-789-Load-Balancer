import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WebClient {
    private static BufferedReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        String serverAddress = "localhost";
        Socket socket = null;
        try {
            socket = new Socket(serverAddress, 8080);

        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

            JEditorExp jEditorExp = new JEditorExp();

            String line = in.readLine();
            jEditorExp.displayPage("http://localhost:" + line + "/");

            while (true) {
                String input = in.readLine();
                out.println("you said" + input);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.println("BYE");
        }
    }
}
