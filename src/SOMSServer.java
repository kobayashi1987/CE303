import java.io.*;
import java.net.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class SOMSServer {
    private static final int PORT = 12345;
    private static final String DATABASE_FILE = "soms_database.json";
    private static JSONObject database;
    private static final AtomicInteger clientCounter = new AtomicInteger(1); // Unique ID generator
    private static JSONArray loggedInClients = new JSONArray();  // List of currently logged-in clients

    public static void main(String[] args) {
        loadDatabase();  // Load the JSON database at the beginning

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                handleClient(socket);
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Load the database from the JSON file
    private static void loadDatabase() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(DATABASE_FILE)));
            database = new JSONObject(content);
        } catch (IOException ex) {
            System.out.println("Error loading database: " + ex.getMessage());
            database = new JSONObject();
            database.put("users", new JSONArray());
            database.put("items", new JSONArray());
            database.put("transactions", new JSONArray());
        }
    }

    // Save the database to the JSON file
    private static void saveDatabase() {
        try (FileWriter file = new FileWriter(DATABASE_FILE)) {
            file.write(database.toString(4));  // Pretty print JSON
            file.flush();
        } catch (IOException ex) {
            System.out.println("Error saving database: " + ex.getMessage());
        }
    }

    // Handle the client connection and interaction
    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            int clientId = clientCounter.getAndIncrement();  // Generate a unique client ID
            writer.println("Welcome! Your Client ID: " + clientId);
            writer.println("Enter your username: ");
            String username = reader.readLine();

            // Check if user exists or add new user
            JSONObject user = getUser(username);
            // If user doesn't exist, register them
            if (user == null) {
                writer.println("You are a new user. Registering with 1000 credits.");
                user = new JSONObject();
                user.put("clientId", clientId);  // Assign client ID
                user.put("username", username);
                user.put("role", "customer");  // Default role is customer for new users
                user.put("credits", 1000);  // New users start with 0 credits
                user.put("purchaseHistory", new JSONArray());  // No history for new users
                database.getJSONArray("users").put(user);
                saveDatabase();  // Save the new user to the database
                writer.println("Registration complete. Your starting credits are 1000.");
                // Show the command options immediately after registration
                writer.println("Enter a command (view credits, buy, view items, top up, view history, view clients, exit): ");
            } else {
                writer.println("Welcome back, " + username + " (" + user.getString("role") + ")");
            }

            // Add client info to the logged-in clients list
            JSONObject loggedInClient = new JSONObject();
            loggedInClient.put("clientId", clientId);
            loggedInClient.put("username", username);
            loggedInClient.put("role", user.getString("role"));
            loggedInClients.put(loggedInClient);

            // Display on server that a client has connected
            System.out.println("Client connected: ID = " + clientId + ", Username = " + username);
            System.out.println("Currently online clients: " + loggedInClients.length());

             //Show the top 5 sellers by number of transactions upon login
             showTopSellers(writer);

            // Main interaction loop
            String command;
            do {
                //writer.println("Enter a command (view credits, buy, sell, view items, top up, view history, exit): ");
                // newly added code starts here:
                if (user.getString("role").equals("customer")) {
                    writer.println("Enter a command (view credits, buy, view items, top up, view history, view clients, exit): ");
                } else {
                    writer.println("Enter a command (sell, view items, view history, view clients, exit): ");
                }
                // newly added code ends here
                command = reader.readLine();

                switch (command.toLowerCase()) {
                    case "view credits":
                        writer.println("Your current credits: " + user.getInt("credits"));
                        break;

                    case "view items":
                        //writer.println("Available items:");
                        JSONArray items = database.getJSONArray("items");
                        if (items.length() == 0) {
                            writer.println("No items available.");
                        } else {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                writer.println(item.getString("itemName") + " - Price: " + item.getInt("price") +
                                        ", Quantity: " + item.getInt("quantity"));
                            }
                        }
                        writer.println("");  // Send an empty line to indicate end of the item list
                        break;

                    case "buy":
                        if (user.getString("role").equals("customer")) {
                            // writer.println("Enter item to buy: ");  // Prompt for item name
                            String itemName = reader.readLine();  // Read item name from client
                            writer.println("You are going to buy: " + itemName);  // Prompt for item name

                            // Check if item exists in the inventory
                            JSONObject item = findItem(itemName);
                            if (item == null) {
                                writer.println("Item not found.");
                                break;
                            }

                            //writer.println("Enter quantity: ");  // Prompt for quantity
                            int quantity = Integer.parseInt(reader.readLine());  // Read quantity from client
                            writer.println("The quantity you buy is: " + quantity); // Prompt for quantity

                            // Check if the requested quantity is available
                            if (quantity > item.getInt("quantity")) {
                                writer.println("Not enough stock available.");
                                break;
                            }

                            // Calculate total price
                            int totalPrice = item.getInt("price") * quantity;

                            // Check if the user has enough credits to make the purchase
                            if (user.getInt("credits") < totalPrice) {
                                writer.println("Insufficient credits to complete the purchase.");
                                break;
                            }

                            // Deduct credits from the buyer and reserve the purchase (transaction remains pending)
                            user.put("credits", user.getInt("credits") - totalPrice);
                            item.put("quantity", item.getInt("quantity") - quantity);  // Deduct the item quantity

                            // Create a new pending transaction
                            JSONObject transaction = new JSONObject();
                            transaction.put("itemName", itemName);
                            transaction.put("quantity", quantity);
                            transaction.put("buyer", username);
                            transaction.put("seller", item.getString("seller"));
                            transaction.put("status", "pending");
                            transaction.put("date", new Date().toString());

                            database.getJSONArray("transactions").put(transaction);  // Add transaction to the database
                            saveDatabase();  // Save the updated data
                            writer.println("Purchase successful. Money reserved. Waiting for seller to fulfill the order.");
                        } else {
                            writer.println("You are not a customer.");
                        }
                        // Now send a new command prompt after the purchase is successful
                        writer.println("Enter a command (view credits, buy, sell, view items, top up, view history, exit): ");
                        break;

                    case "sell":
                        if (user.getString("role").equals("seller")) {
                            //writer.println("Enter item name to sell: ");
                            String itemName = reader.readLine();
                            // writer.println("Enter price per item: ");
                            int price = Integer.parseInt(reader.readLine());
                            //writer.println("Enter quantity: ");
                            int quantity = Integer.parseInt(reader.readLine());

                            JSONObject newItem = new JSONObject();
                            newItem.put("itemName", itemName);
                            newItem.put("price", price);
                            newItem.put("quantity", quantity);
                            newItem.put("seller", username);
                            database.getJSONArray("items").put(newItem);

                            writer.println("Item listed for sale.");
                            saveDatabase();
                        } else {
                            writer.println("You are not a seller.");
                        }
                        break;

                    case "top up":
                        // writer.println("Enter amount to top up: ");  // Prompt for top-up amount
                        int topUpAmount = Integer.parseInt(reader.readLine());  // Read top-up amount from client

                        // Update user's credit balance
                        user.put("credits", user.getInt("credits") + topUpAmount);
                        writer.println("Credits topped up. New balance: " + user.getInt("credits"));
                        saveDatabase();  // Save the updated data
                        break;

                    case "view history":
                        // If the user is a customer, show their purchase history
                        JSONArray transactions = database.getJSONArray("transactions");
                        JSONArray history = new JSONArray();

                        for (int i = 0; i < transactions.length(); i++) {
                            JSONObject transaction = transactions.getJSONObject(i);
                            if (transaction.getString("buyer").equals(username)) {
                                history.put(transaction);  // Add to the customer's history
                            }
                        }

                        if (history.length() == 0) {
                            writer.println("No history available.");
                        } else {
                            for (int i = 0; i < history.length(); i++) {
                                JSONObject transaction = history.getJSONObject(i);
                                writer.println("Item: " + transaction.getString("itemName") +
                                        ", Quantity: " + transaction.getInt("quantity") +
                                        ", Date: " + transaction.getString("date") +
                                        ", Status: " + transaction.getString("status"));
                            }
                        }
                        writer.println("");  // Send empty line to indicate end of history
                        break;

                    // newly added code starts here:
                    case "view clients":
                        writer.println("Currently logged-in clients:");
                        for (int i = 0; i < loggedInClients.length(); i++) {
                            JSONObject client = loggedInClients.getJSONObject(i);
                            writer.println("Client ID: " + client.getInt("clientId") +
                                    ", Username: " + client.getString("username") +
                                    ", Role: " + client.getString("role"));
                        }
                        writer.println("");  // End of client list
                        break;
                    // newly added code ends here

//                    case "exit":
//                        writer.println("Goodbye!");
//                        loggedInClients = removeLoggedInClient(clientId);  // Remove the client from the list
//                        socket.close();  // Close the socket and end the client connection
//                        break;  // Exit the loop and close the connection properly

                    // newly added Case "exit" starts here:
                    case "exit":
                        writer.println("Goodbye!");
                        loggedInClients = removeLoggedInClient(clientId);  // Remove the client from the list
                        socket.close();

                        // Display on server that a client has disconnected
                        System.out.println("Client disconnected: ID = " + clientId + ", Username = " + username);
                        System.out.println("Currently online clients: " + loggedInClients.length());
                        return;
                    // newly added Case "exit" ends here

                    default:
                        writer.println("Invalid command.");
                }

            } while (!command.equalsIgnoreCase("exit"));

        } catch (IOException ex) {
            System.out.println("Error in client interaction: " + ex.getMessage());
        }
    }

    // Remove a client from the logged-in clients list when they exit
    private static JSONArray removeLoggedInClient(int clientId) {
        JSONArray updatedClients = new JSONArray();
        for (int i = 0; i < loggedInClients.length(); i++) {
            JSONObject client = loggedInClients.getJSONObject(i);
            if (client.getInt("clientId") != clientId) {
                updatedClients.put(client);
            }
        }
        return updatedClients;
    }

    // Find user in the JSON database
    private static JSONObject getUser(String username) {
        JSONArray users = database.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getString("username").equals(username)) {
                return user;
            }
        }
        return null;
    }

    // Find item in the JSON database
    private static JSONObject findItem(String itemName) {
        JSONArray items = database.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.getString("itemName").equals(itemName)) {
                return item;
            }
        }
        return null;
    }


    // Add a method to get the total number of users
    private static int getTotalUsers() {
        return database.getJSONArray("users").length();
    }


    // Show the top 5 sellers based on number of completed transactions (sales fulfilled)
    private static void showTopSellers(PrintWriter writer) {
        JSONArray transactions = database.getJSONArray("transactions");
        // Count the number of completed transactions per seller
        JSONObject sellerTransactionCount = new JSONObject();

        // Iterate over all transactions and only count those that are "fulfilled"
        for (int i = 0; i < transactions.length(); i++) {
            JSONObject transaction = transactions.getJSONObject(i);
            String seller = transaction.getString("seller");
            String status = transaction.getString("status");

            // Count only fulfilled (completed) transactions
            if (status.equals("pending")) {
                if (!sellerTransactionCount.has(seller)) {
                    sellerTransactionCount.put(seller, 0);
                }
                sellerTransactionCount.put(seller, sellerTransactionCount.getInt(seller) + 1);
            }
        }

        // Create a list of sellers to sort by completed transactions
        List<JSONObject> sellerList = new ArrayList<>();
        sellerTransactionCount.keySet().forEach(seller -> {
            JSONObject sellerObj = new JSONObject();
            sellerObj.put("seller", seller);
            sellerObj.put("transactions", sellerTransactionCount.getInt(seller));
            sellerList.add(sellerObj);
        });

        // Sort sellers by number of completed transactions in descending order
        sellerList.sort(Comparator.comparingInt(s -> -s.getInt("transactions")));

        // Display the top 5 sellers
        writer.println("Top 5 Sellers (by completed sales transactions):");
        for (int i = 0; i < Math.min(5, sellerList.size()); i++) {
            JSONObject seller = sellerList.get(i);
            writer.println((i + 1) + ". Seller: " + seller.getString("seller") +
                    " - Completed Transactions: " + seller.getInt("transactions"));
        }
        writer.println("");  // End of top sellers list
    }

}