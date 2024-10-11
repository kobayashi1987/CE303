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

            do {
                System.out.println("Enter a command (or 'exit' to quit): ");
                command = scanner.nextLine();

                writer.println(command);  // Send the command to the server

                if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting...");
                    break;
                }

                // Receive and display the server's response
                String response = reader.readLine();
                System.out.println("Server response: " + response);
            } while (true);

        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}