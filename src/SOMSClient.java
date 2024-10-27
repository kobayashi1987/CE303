
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SOMSClient {
    private static final String HOST = "localhost";  // Update to the correct host if necessary
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("Connected to the server");

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            Scanner scanner = new Scanner(System.in);
            String serverResponse;

            // Client prompts for userID and password
            System.out.println(reader.readLine());  // "Welcome! Your Client ID: ..."
            System.out.println(reader.readLine());  // "Enter your userID: "
            String userID = scanner.nextLine();
            writer.println(userID);  // Send userID to server

            System.out.println(reader.readLine());  // "Enter your password: "
            String password = scanner.nextLine();
            writer.println(password);  // Send password to server

            // Login response from the server (successful or failure)
            serverResponse = reader.readLine();
            System.out.println("Server response: 101  " + serverResponse);

            if (serverResponse.contains("Login successful") && !userID.contains("seller")) {
                // Display the top 5 sellers for customers, if applicable
                while (true) {
                    serverResponse = reader.readLine();
                    if (serverResponse == null || serverResponse.trim().isEmpty()) {
                        break;
                    }
                    System.out.println(serverResponse);
                }

            } else if (serverResponse.contains("Registering as a new user.")) {
                System.out.println(reader.readLine());  // "Enter your role: "
                String role = scanner.nextLine();
                writer.println(role);  // Send role to server

                serverResponse = reader.readLine();  // Server sends confirmation
                System.out.println("Server response: " + serverResponse);

                if (role.equalsIgnoreCase("customer")) {
                    System.out.println("Top 5 Sellers (by completed sales transactions):");
                    while (true) {
                        serverResponse = reader.readLine();
                        if (serverResponse == null || serverResponse.trim().isEmpty()) {
                            break;
                        }
                        System.out.println(serverResponse);
                    }
                }

            } else if (serverResponse.contains("Invalid")) {
                System.out.println("Login failed. Exiting...");
                return;
            }

            // Interaction loop based on user type (Customer or Seller)
            while (true) {
                serverResponse = reader.readLine();  // Server sends command prompt
                System.out.println("Server prompt: " + serverResponse);

                String command = scanner.nextLine();
                writer.println(command);  // Send command to server

                if (command.equalsIgnoreCase("exit")) {
                    serverResponse = reader.readLine();  // "Goodbye!"
                    System.out.println("Server response: " + serverResponse);
                    System.out.println("Client disconnected.");
                    break;
                }

                switch (command.toLowerCase()) {
                    case "view credits":
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);
                        break;

                    case "buy":
                        System.out.println("Enter item name: ");
                        String itemName = scanner.nextLine();
                        writer.println(itemName);  // Send item name to server

                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);  // Either prompt for quantity or item not found

                        if (serverResponse.contains("not found") || serverResponse.contains("Enter a command")) {
                            break;  // Exit the buy process if item is not found or if server returns to main menu
                        }

                        System.out.println("Enter quantity: ");
                        String quantity = scanner.nextLine();
                        writer.println(quantity);  // Send quantity to server

                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);  // Response for invalid quantity or stock availability

                        if (serverResponse.contains("Invalid quantity") || serverResponse.contains("stock")) {
                            break;  // Exit the buy process if quantity is invalid or insufficient stock
                        }

                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);  // Response for insufficient credits or success message

                        if (serverResponse.contains("Insufficient credits")) {
                            break;  // Exit if not enough credits
                        }

                        // Final success message and new command prompt
                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);  // Confirmation for purchase success and prompt for next command
                        break;

                    case "view items":
                        System.out.println("Available items:");
                        while (true) {
                            serverResponse = reader.readLine();
                            if (serverResponse == null || serverResponse.trim().isEmpty()) {
                                break;
                            }
                            System.out.println(serverResponse);
                        }
                        break;

                    case "top up":
                        System.out.println("Enter amount to top up: ");
                        String topUpAmount = scanner.nextLine();
                        writer.println(topUpAmount);  // Send top-up amount

                        serverResponse = reader.readLine();
                        System.out.println("Server response: " + serverResponse);
                        break;

                    case "view history":
                        System.out.println("Your transaction history:");
                        while (true) {
                            serverResponse = reader.readLine();
                            if (serverResponse == null || serverResponse.trim().isEmpty()) {
                                break;
                            }
                            System.out.println(serverResponse);
                        }
                        break;

                    case "view clients":
                        System.out.println("Currently logged-in clients:");
                        while (true) {
                            serverResponse = reader.readLine();
                            if (serverResponse == null || serverResponse.trim().isEmpty()) {
                                break;
                            }
                            System.out.println(serverResponse);
                        }
                        break;

                    case "sell":
                        System.out.println("Enter item name to sell: ");
                        itemName = scanner.nextLine();
                        writer.println(itemName);  // Send item name

                        System.out.println("Enter price per item: ");
                        String price = scanner.nextLine();
                        writer.println(price);  // Send price

                        System.out.println("Enter quantity: ");
                        quantity = scanner.nextLine();
                        writer.println(quantity);  // Send quantity

                        serverResponse = reader.readLine();  // Confirm item listed for sale
                        System.out.println("Server response: " + serverResponse);
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