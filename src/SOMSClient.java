import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SOMSClient {
    private static final String HOST = "localhost";  // Server address (use correct address if needed)
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

            // Initial interaction with server to enter username
            System.out.println(reader.readLine());  // "Welcome! Your Client ID is:"
            System.out.println(reader.readLine());  // "Enter your username:"

            String username = scanner.nextLine();
            writer.println(username);  // Send username to server

            // Server response: Check if new user and prompt role
            String serverResponse = reader.readLine();
            System.out.println(serverResponse);

            if (serverResponse.contains("Customer or Seller")) {
                String role = scanner.nextLine();
                writer.println(role);  // Send chosen role to server
            }

            // Main interaction loop for commands
            while (true) {
                serverResponse = reader.readLine();  // Server asks for next command
                System.out.println(serverResponse);

                // Get user command input
                command = scanner.nextLine();
                writer.println(command);  // Send command to server

//                if (command.equalsIgnoreCase("exit")) {
//                    System.out.println(reader.readLine());  // "Goodbye!"
//                    break;
//                }

                // New Exit Case Client Side
                if (command.equalsIgnoreCase("exit")) {
                    System.out.println(reader.readLine()); // Goodbye! message from server
                    socket.close();
                    System.out.println("Client disconnected.");
                    System.exit(0);
                    break;
                }


                switch (command.toLowerCase()) {
                    case "view credits":
                        // The server will return the current credit balance
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);
                        break;

                    case "view items":
                        // The server will return a list of available items
                        System.out.println("Available items:");
                        while (true) {
                            serverResponse = reader.readLine();
                            if (serverResponse == null || serverResponse.equals("")) {
                                break;  // Exit the loop when the server sends an empty line or null
                            }
                            System.out.println(serverResponse);  // Print each item line
                        }
                        break;

                    case "buy":
                        // Prompt the user to enter the item name and quantity to purchase
                        System.out.println("Enter item name: ");
                        String itemName = scanner.nextLine();  // Get item name from user
                        writer.println(itemName);  // Send item name to server

                        // Wait for the server to prompt for quantity
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);  // Should print "Enter quantity"

                        // Enter the quantity
                        System.out.println("Enter quantity: ");
                        String quantityStr = scanner.nextLine();  // Get quantity from user
                        writer.println(quantityStr);  // Send quantity to server

                        // Now read the final response from the server regarding the transaction success or failure
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);

                        // Now, the server will immediately send the next command prompt
                        serverResponse = reader.readLine();
                        System.out.println(serverResponse);  // New command prompt
                        break;


                    case "sell":
                        // Input item details for selling
                        System.out.println("Enter item name to sell: ");
                        itemName = scanner.nextLine();
                        writer.println(itemName);  // Send item name to server

                        System.out.println("Enter price per item: ");
                        String priceStr = scanner.nextLine();
                        writer.println(priceStr);  // Send price to server

                        System.out.println("Enter quantity: ");
                        quantityStr = scanner.nextLine();
                        writer.println(quantityStr);  // Send quantity to server

                        // Server response on success or failure of listing the item
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);
                        break;

                    case "top up":
                        // Prompt for the top-up amount
                        System.out.println("Enter amount to top up: ");
                        String topUpAmount = scanner.nextLine();
                        writer.println(topUpAmount);  // Send top-up amount to server

                        // Wait for the server's confirmation message about the new credit balance
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);  // Should print the new balance
                        break;


                    case "view history":
                        // Server will return the transaction history
                        System.out.println("Your transaction history:");
                        while (true) {
                            serverResponse = reader.readLine();
                            if (serverResponse == null || serverResponse.equals("")) {
                                break;  // Exit loop when empty or null is received
                            }
                            System.out.println(serverResponse);  // Print each transaction line
                        }
                        break;

                    // newly added code "View Clients" added here
                    case "view clients":
                        System.out.println("Currently logged-in clients:");
                        while (true) {
                            serverResponse = reader.readLine();
                            if (serverResponse == null || serverResponse.equals("")) {
                                break;
                            }
                            System.out.println(serverResponse);
                        }
                        break;
                     // newly added code "View Clients" added here

                    case "exit":
                        writer.println("exit");
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);  // Goodbye message from server
                        socket.close();  // Close the client socket
                        System.out.println("Client disconnected.");
                        System.exit(0);  // Exit the program
                        break;

                    default:
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);
                        break;
                }
            }

        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}