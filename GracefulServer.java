import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GracefulServer
{
    private static final int PORT = 8080;
    private static boolean running = true; // control flag

    private static void main(String[] args)
    {
        // add a shutdown hook to detect when the server is stopped
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting Down gracefully...");
            running = false; //update flag to stop accepting new connections
        }));

        try(ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.println("Server started on port " + PORT);

            while(running)
            {
                try
                {
                    Socket clientSocket = serverSocket.accept(); // Accept connections
                    System.out.println("Connection recieved from " + clientSocket.getInetAddress());

                    // close client socket after logging
                    clientSocket.close();
                }
                catch (IOException e)
                {
                    if(running) // ignore
                    {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        }catch (IOException e)
        {
            System.err.println("Server failed to start: " + e.getMessage());
        }
    System.out.println("Server has stopped");
    }

}
