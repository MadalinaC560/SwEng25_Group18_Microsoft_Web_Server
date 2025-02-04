import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class SimpleServer{
    private static final int PORT = 8080; // port the server will listen from
    private static boolean running = true; // Server status flag

    public static void main(String[] args)
    {
        try(ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.println("Server stated on port " + PORT);
            while(running)
            {
                Socket clientSocket = serverSocket.accept(); // accept incoming connection
                System.out.println("Connection recieved from " + clientSocket.getInetAddress());

                clientSocket.close(); // Close client sock after logging
            }
        }
        catch (IOException e)
        {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
// How it works:
// The ServerSocket will listen on the port 8080
// When a client conects, it logs the connections
// It closes the client socket after recieving the connection

}
