import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//import com.webserver.util.Logger;

public class GracefulServer
{
    private static final int PORT = 8080;
    private boolean running = true; // control flag
    private final ServerSocket serverSocket;

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(PORT);
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        Logger.info("Server started on port " + PORT);

        while(running) {
            try {
                Socket clientSocket = serverSocket.accept();
                Logger.info("Connection received from " + clientSocket.getInetAddress());

                // Create and use ConnectionHandler instead of directly closing socket
                ConnectionHandler handler = new ConnectionHandler(clientSocket);
                handler.handle();

            } catch (IOException e) {
                if(running) {
                    Logger.error("Error accepting connection", e);
                }
            }
        }

        Logger.info("Server has stopped");
    }

    public void stop() {
        Logger.info("Shutting down gracefully...");
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            Logger.error("Error closing server socket", e);
        }
    }

    private static void main(String[] args)
    {
        try {
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            Logger.error("Server failed to start", e);
        }

    }

}
