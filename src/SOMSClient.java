//import java.io.*;
//import java.net.*;
//import java.util.Scanner;
//
//public class SOMSClient {
//    private static final String HOST = "localhost";  // Update to the correct host if necessary
//    private static final int PORT = 12345;
//
//    public static void main(String[] args) {
//        try (Socket socket = new Socket(HOST, PORT)) {
//            System.out.println("Connected to the server");
//
//            OutputStream output = socket.getOutputStream();
//            PrintWriter writer = new PrintWriter(output, true);
//            InputStream input = socket.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//
//            Scanner scanner = new Scanner(System.in);
//            String serverResponse;
//
//            // Client prompts for userID and password
//            System.out.println(reader.readLine());  // "Welcome! Your Client ID: ..."
//            System.out.println(reader.readLine());  // "Enter your userID: "
//            String userID = scanner.nextLine();
//            writer.println(userID);  // Send userID to server
//
//            System.out.println(reader.readLine());  // "Enter your password: "
//            String password = scanner.nextLine();
//            writer.println(password);  // Send password to server
//
//            // Login response from the server (successful or failure)
//            serverResponse = reader.readLine();
//            System.out.println("Server response: " + serverResponse);
//            if (serverResponse.contains("Invalid")) {
//                System.out.println("Login failed. Exiting...");
//                return;
//            }
//
//            // Read and display top 5 sellers
//            System.out.println("Top 5 Sellers (by completed sales transactions):");
//            while (true) {
//                serverResponse = reader.readLine();  // Read each seller from the server
//                if (serverResponse == null || serverResponse.trim().isEmpty()) {
//                    break;  // Exit loop when no more sellers are sent
//                }
//                System.out.println(serverResponse);
//            }
//
//            // If login is successful, the client proceeds with available commands
//            while (true) {
//                serverResponse = reader.readLine();  // Server prompts for a command
//                System.out.println(serverResponse);
//
//                String command = scanner.nextLine();
//                writer.println(command);  // Send command to server
//
//                if (command.equalsIgnoreCase("exit")) {
//                    serverResponse = reader.readLine();  // "Goodbye!"
//                    System.out.println("Server response: " + serverResponse);
//                    System.out.println("Client disconnected.");
//                    break;
//                }
//
//                switch (command.toLowerCase()) {
//                    case "view credits":
//                        serverResponse = reader.readLine();
//                        System.out.println("Server response: " + serverResponse);
//                        break;
//
//                    case "view items":
//                        System.out.println("Available items:");
//                        while (true) {
//                            serverResponse = reader.readLine();
//                            if (serverResponse == null || serverResponse.trim().isEmpty()) {
//                                break;
//                            }
//                            System.out.println(serverResponse);
//                        }
//                        break;
//
//                    case "buy":
//                        System.out.println("Enter item name: ");
//                        String itemName = scanner.nextLine();
//                        writer.println(itemName);  // Send item name
//
//                        serverResponse = reader.readLine();
//                        System.out.println("Server response: " + serverResponse);  // Server asks for quantity
//
//                        System.out.println("Enter quantity: ");
//                        String quantity = scanner.nextLine();
//                        writer.println(quantity);  // Send quantity
//
//                        serverResponse = reader.readLine();  // Purchase response
//                        System.out.println("Server response11: " + serverResponse);
//
//                        serverResponse = reader.readLine();
//                        System.out.println("Server response: " + serverResponse);  // Server asks for command
//                        break;
//
//                    case "sell":
//                        System.out.println("Enter item name to sell: ");
//                        itemName = scanner.nextLine();
//                        writer.println(itemName);  // Send item name
//
//                        System.out.println("Enter price per item: ");
//                        String price = scanner.nextLine();
//                        writer.println(price);  // Send price
//
//                        System.out.println("Enter quantity: ");
//                        quantity = scanner.nextLine();
//                        writer.println(quantity);  // Send quantity
//
//                        serverResponse = reader.readLine();  // Sell response
//                        System.out.println("Server response: " + serverResponse);
//                        break;
//
//                    case "top up":
//                        System.out.println("Enter amount to top up: ");
//                        String topUpAmount = scanner.nextLine();
//                        writer.println(topUpAmount);  // Send top-up amount
//
//                        serverResponse = reader.readLine();
//                        System.out.println("Server response: " + serverResponse);
//                        break;
//
//                    case "view history":
//                        System.out.println("Your transaction history:");
//                        while (true) {
//                            serverResponse = reader.readLine();
//                            if (serverResponse == null || serverResponse.trim().isEmpty()) {
//                                break;
//                            }
//                            System.out.println(serverResponse);
//                        }
//                        break;
//
//                    // Add the "view clients" case in SOMSClient.java
//                    case "view clients":
//                        System.out.println("Currently logged-in clients:");
//
//                        // Read and display each line from the server about the logged-in clients
//                        while (true) {
//                            serverResponse = reader.readLine();  // Read each line sent by the server
//                            if (serverResponse == null || serverResponse.trim().isEmpty()) {
//                                break;  // Break when an empty line or null is received, indicating the end of the client list
//                            }
//                            System.out.println(serverResponse);  // Print each line (client details)
//                        }
//                        break;
//
//                    case "exit":
//                        writer.println("exit");
//                        serverResponse = reader.readLine();
//                        System.out.println("Server response: " + serverResponse);  // Goodbye message from server
//                        socket.close();  // Close the client socket
//                        System.out.println("Client disconnected.");
//                        System.exit(0);  // Exit the program
//                        break;
//
//                    default:
//                        serverResponse = reader.readLine();
//                        System.out.println("Server response: " + serverResponse);
//                        break;
//                }
//            }
//
//        } catch (IOException ex) {
//            System.out.println("Client exception: " + ex.getMessage());
//            ex.printStackTrace();
//        }
//    }
//}



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
            System.out.println("Server response123: " + serverResponse);


            if (serverResponse.contains("Login successful") && !userID.contains("seller")) {
                while (true) {
                    serverResponse = reader.readLine();  // Read each seller from the server
                    if (serverResponse == null || serverResponse.trim().isEmpty()) {
                        break;  // Exit loop when no more sellers are sent
                    }
                    System.out.println(serverResponse);
                }

            } else if (serverResponse.contains("Registering as a new user.")) {
                System.out.println(reader.readLine());  // "Enter your role: "
                String role = scanner.nextLine();
                writer.println(role);  // Send role to server
                serverResponse = reader.readLine();  // Server sends command prompt
                System.out.println("serviceResponse102 _ inside else if of CLIENT SIDE:" + serverResponse);
                //Display Top 5 Sellers if the user is a Customer
                //System.out.println("Top 5 Sellers (by completed sales transactions_FROM CLIENT SIDE):");
                if (role.equalsIgnoreCase("Customer")) {
                    System.out.println("Top 5 Sellers (by completed sales transactions) FROM CLIENT SIDE Else IF:");
                    while (true) {
                        serverResponse = reader.readLine();  // Read each seller from the server
                        if (serverResponse == null || serverResponse.trim().isEmpty()) {
                            break;  // Exit loop when no more sellers are sent
                        }
                        System.out.println(serverResponse);
                    }
                }
            }

            if (serverResponse.contains("Invalid")) {
                System.out.println("Login failed. Exiting...");
                return;
            }

            // Role-specific menu based on user type (Customer or Seller)
            while (true) {
                serverResponse = reader.readLine();  // Server sends command prompt
                System.out.println("serviceResponse101_After the IF BLOCK_FROM ClIENT SIDE:" + serverResponse);


                String command = scanner.nextLine();
                writer.println(command);  // Send command to server

                if (command.equalsIgnoreCase("exit")) {
                    serverResponse = reader.readLine();  // "Goodbye!"
                    System.out.println("Server response: " + serverResponse);
                    System.out.println("Client disconnected.");
                    break;
                }

                // Handle responses for customer commands
                if (command.equalsIgnoreCase("view credits")) {
                    serverResponse = reader.readLine();
                    System.out.println("Server response: " + serverResponse);

                } else if (command.equalsIgnoreCase("buy")) {
                    System.out.println("Enter item name: ");
                    String itemName = scanner.nextLine();
                    writer.println(itemName);  // Send item name to server

                    serverResponse = reader.readLine();  // Server asks for quantity
                    System.out.println("Server response: " + serverResponse);

                    System.out.println("Enter quantity: ");
                    String quantity = scanner.nextLine();
                    writer.println(quantity);  // Send quantity to server

                    serverResponse = reader.readLine();  // Purchase confirmation
                    System.out.println("Server response: " + serverResponse);

                    serverResponse = reader.readLine();  // Get new command prompt
                    System.out.println("Server response: " + serverResponse);

                } else if (command.equalsIgnoreCase("view items")) {
                    System.out.println("Available items:");
                    while (true) {
                        serverResponse = reader.readLine();
                        if (serverResponse == null || serverResponse.trim().isEmpty()) {
                            break;
                        }
                        System.out.println(serverResponse);
                    }

                } else if (command.equalsIgnoreCase("top up")) {
                    System.out.println("Enter amount to top up: ");
                    String topUpAmount = scanner.nextLine();
                    writer.println(topUpAmount);  // Send top-up amount

                    serverResponse = reader.readLine();
                    System.out.println("Server response: " + serverResponse);

                } else if (command.equalsIgnoreCase("view history")) {
                    System.out.println("Your transaction history:");
                    while (true) {
                        serverResponse = reader.readLine();
                        if (serverResponse == null || serverResponse.trim().isEmpty()) {
                            break;
                        }
                        System.out.println(serverResponse);
                    }

                } else if (command.equalsIgnoreCase("view clients")) {
                    System.out.println("Currently logged-in clients:");
                    while (true) {
                        serverResponse = reader.readLine();
                        if (serverResponse == null || serverResponse.trim().isEmpty()) {
                            break;
                        }
                        System.out.println(serverResponse);
                    }

                } else if (command.equalsIgnoreCase("sell")) {
                    System.out.println("Enter item name to sell_FROM CLIENT: ");
                    String itemName = scanner.nextLine();
                    writer.println(itemName);  // Send item name

                    System.out.println("Enter price per item_FROM CLIENT: ");
                    String price = scanner.nextLine();
                    writer.println(price);  // Send price

                    System.out.println("Enter quantity_FROM CLIENT: ");
                    String quantity = scanner.nextLine();
                    writer.println(quantity);  // Send quantity

                    serverResponse = reader.readLine();  // Confirm item listed for sale
                    System.out.println("Server response: " + serverResponse);

                } else {
                    serverResponse = reader.readLine();
                    System.out.println("Server response: " + serverResponse);
                }
            }

        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}