import java.io.*;
import java.net.*;

public class SOMSServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();  // Accept client connection
                System.out.println("New client connected");

                // Create a new thread or process to handle client interactions
                handleClient(socket);
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String clientInput;
            // Interact with client until they disconnect or send an "exit" command
            while ((clientInput = reader.readLine()) != null) {
                System.out.println("Received from client: " + clientInput);

                if (clientInput.equalsIgnoreCase("exit")) {
                    writer.println("Goodbye!");
                    System.out.println("Client disconnected");
                    break;
                }

                // Respond to client commands (this is where you implement more functionalities)
                writer.println("Server received: " + clientInput);
            }
        } catch (IOException ex) {
            System.out.println("Error in client interaction: " + ex.getMessage());
        }
    }
}