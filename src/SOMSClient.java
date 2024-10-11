import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SOMSClient {
    private static final String HOST = "localhost";  // Adjust this if needed
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("Connected to the server");

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            Scanner scanner = new Scanner(System.in);
            String command;

            // Read server's welcome message
            System.out.println(reader.readLine());

            // Send username
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            writer.println(username);

            // Handle new user case
            String serverResponse = reader.readLine();
            if (serverResponse.contains("Buyer or Seller")) {
                System.out.println(serverResponse);
                String role = scanner.nextLine();
                writer.println(role);
            }

            // Main interaction loop
            while (true) {
                serverResponse = reader.readLine();
                System.out.println(serverResponse);  // Display server's prompt

                command = scanner.nextLine();  // Get user input
                writer.println(command);  // Send command to server

                // Read server's response
                String response = reader.readLine();
                System.out.println("Server response: " + response);

                // Exit if command was "exit"
                if (command.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}