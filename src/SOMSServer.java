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
    private static final String CREDENTIALS_FILE = "credentials.json";  // New file for storing user credentials
    private static JSONObject database;
    private static JSONObject credentials;  // JSON object to hold credentials
    private static final AtomicInteger clientCounter = new AtomicInteger(1); // Unique ID generator
    private static JSONArray loggedInClients = new JSONArray();  // List of currently logged-in clients

    public static void main(String[] args) {
        loadDatabase();  // Load the JSON database at the beginning
        loadCredentials();  // Load the credentials at the beginning

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

    // Load the credentials from a separate JSON file
    private static void loadCredentials() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(CREDENTIALS_FILE)));
            credentials = new JSONObject(content);
        } catch (IOException ex) {
            System.out.println("Error loading credentials: " + ex.getMessage());
            credentials = new JSONObject();
            credentials.put("users", new JSONArray());
        }
    }

    // Save the credentials to the JSON file
    private static void saveCredentials() {
        try (FileWriter file = new FileWriter(CREDENTIALS_FILE)) {
            file.write(credentials.toString(4));  // Pretty print JSON
            file.flush();
        } catch (IOException ex) {
            System.out.println("Error saving credentials: " + ex.getMessage());
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
        int clientId = -1;
        String userID = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            clientId = clientCounter.getAndIncrement();  // Generate a unique client ID
            writer.println("Welcome! Your Client ID: " + clientId);

            // Prompt for user ID and password
            writer.println("Enter your userID: ");
            userID = reader.readLine();
            writer.println("Enter your password: ");
            String password = reader.readLine();

            // Check if the user is already registered
            JSONObject existingUser = getCredential(userID);
            if (existingUser != null) {
                if (existingUser.getString("password").equals(password)) {
                    writer.println("Login successful! Welcome back.");

                    // Only show top sellers if the user is a customer
                    JSONObject user = getUserByID(userID);
                    if (user.getString("role").equals("customer")) {
                        writer.println("Top 5 Sellers (by completed sales transactions_FROM SERVER SIDE):");
                        showTopSellers(writer);  // Send top sellers to the client
                    }

                } else {
                    writer.println("Invalid password. Connection closed.");
                    socket.close();
                    return;
                }
            } else {
                // If user does not exist, register as a new user
                writer.println("No account found. Registering as a new user.");
                writer.println("Are you a Customer or Seller? (Enter 'customer' or 'seller')");
                String role = reader.readLine().trim().toLowerCase();  // Read user role

                if (!role.equals("customer") && !role.equals("seller")) {
                    writer.println("Invalid role. Please enter 'customer' or 'seller'.");
                    socket.close();
                    return;
                }

                // Register new user with the chosen role
                registerNewUser(userID, password, clientId, writer, role);

                // Confirm registration and proceed with customer flow
                writer.println("Registration complete. You are registered as a " + role + "." + " from server side");

                // Only show top sellers if the new user is a customer
                if (role.equals("customer")) {
                    writer.println("Top 5 Sellers (by completed sales transactions from SERVER SIDE):");
                    showTopSellers(writer);  // Send top sellers to the client
                }
            }

            // Retrieve the user details to check their role
            JSONObject user = getUserByID(userID);
            String role = user.getString("role");

            // Add client info to the logged-in clients list
            JSONObject loggedInClient = new JSONObject();
            loggedInClient.put("clientId", clientId);
            loggedInClient.put("userID", userID);
            loggedInClient.put("role", role);
            loggedInClients.put(loggedInClient);

            // Display on server that a client has connected
            System.out.println("Client connected: ID = " + clientId + ", UserID = " + userID);
            System.out.println("Currently online clients: " + loggedInClients.length());

            // Main interaction loop for commands
            String command;
            if (role.equals("customer")) {
                do {
                    writer.println("Enter a command (view credits, buy, sell, view items, top up, view history, view clients, exit): ");
                    command = reader.readLine();

                    if (command == null) {  // Handle unexpected disconnection
                        break;  // Exit the loop if the client disconnects unexpectedly
                    }

                    // JSONObject user = getUserByID(userID);  // Get the user object from the database

                    switch (command.toLowerCase()) {
                        case "view credits":

                            writer.println("Your current credits: " + user.getInt("credits"));
                            break;

                        case "view items":
                            writer.println("Available items:");
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

                        // Additional commands here...
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
                                transaction.put("buyer", userID);
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
                                newItem.put("seller", userID);
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
                                if (transaction.getString("buyer").equals(userID)) {
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
                                        ", Username: " + client.getString("userID") +
                                        ", Role: " + client.getString("role"));
                            }
                            writer.println("");  // Send an empty line to indicate the end of the list
                            break;
                        // newly added code ends here

                        case "exit":
                            writer.println("Goodbye!");
                            loggedInClients = removeLoggedInClient(clientId);  // Remove the client from the list
                            socket.close();

                            // Display on server that a client has disconnected
                            System.out.println("Client disconnected: ID = " + clientId + ", UserID = " + userID);
                            System.out.println("Currently online clients: " + loggedInClients.length());
                            return;

                        default:
                            writer.println("Invalid command.");
                            break;
                    }

                } while (!command.equalsIgnoreCase("exit"));
            } else if (role.equals("seller")) {
                do {
                    writer.println("Enter a command (sell, view items, view history, view clients, exit): ");
                    command = reader.readLine();
                    if (command == null) break;  // Handle unexpected disconnection

                    switch (command.toLowerCase()) {

                        case "sell":
                            //writer.println("Enter item name to sell: ");
                            String itemName = reader.readLine();  // Get item name
                            //writer.println("Enter price per item: ");
                            String priceInput = reader.readLine();  // Get price input
                            //writer.println("Enter quantity: ");
                            String quantityInput = reader.readLine();  // Get quantity input

                            try {
                                int price = Integer.parseInt(priceInput);
                                int quantity = Integer.parseInt(quantityInput);

                                // Add item to the database
                                JSONObject newItem = new JSONObject();
                                newItem.put("itemName", itemName);
                                newItem.put("price", price);
                                newItem.put("quantity", quantity);
                                newItem.put("seller", user.getString("username"));
                                database.getJSONArray("items").put(newItem);

                                saveDatabase();  // Save the updated item database
                                writer.println("Item listed for sale successfully.");

                            } catch (NumberFormatException e) {
                                writer.println("Invalid input for price or quantity. Please enter numeric values.");
                            }
                            break;

                        case "view items":
                            writer.println("Items you are selling:");
                            JSONArray items = database.getJSONArray("items");
                            boolean hasItems = false;

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                if (item.getString("seller").equals(user.getString("username"))) {
                                    writer.println("Item: " + item.getString("itemName") +
                                            " - Price: " + item.getInt("price") +
                                            " - Quantity: " + item.getInt("quantity"));
                                    hasItems = true;
                                }
                            }
                            if (!hasItems) {
                                writer.println("You are not selling any items.");
                            }
                            writer.println("");  // End of item list
                            break;

                        case "view history":
                            writer.println("Your transaction history:");
                            JSONArray transactions = database.getJSONArray("transactions");
                            boolean hasTransactions = false;

                            for (int i = 0; i < transactions.length(); i++) {
                                JSONObject transaction = transactions.getJSONObject(i);
                                if (transaction.getString("seller").equals(user.getString("username"))) {
                                    writer.println("Item: " + transaction.getString("itemName") +
                                            ", Quantity: " + transaction.getInt("quantity") +
                                            ", Buyer: " + transaction.getString("buyer") +
                                            ", Date: " + transaction.getString("date") +
                                            ", Status: " + transaction.getString("status"));
                                    hasTransactions = true;
                                }
                            }
                            if (!hasTransactions) {
                                writer.println("You have no transaction history.");
                            }
                            writer.println("");  // End of transaction history
                            break;

                        case "view clients":
                            writer.println("Currently logged-in clients:");
                            for (int i = 0; i < loggedInClients.length(); i++) {
                                JSONObject client = loggedInClients.getJSONObject(i);
                                writer.println("Client ID: " + client.getInt("clientId") +
                                        ", Username: " + client.getString("userID") +
                                        ", Role: " + client.getString("role"));
                            }
                            writer.println("");  // End of client list
                            break;

                        case "exit":
                            writer.println("Goodbye!");
                            loggedInClients = removeLoggedInClient(clientId);  // Remove the client from the list
                            socket.close();
                            System.out.println("Client disconnected: ID = " + clientId + ", UserID = " + userID);
                            System.out.println("Currently online clients: " + loggedInClients.length());
                            return;

                        default:
                            writer.println("Invalid command. Please try again.");
                            break;
                    }

                } while (!command.equalsIgnoreCase("exit"));
            }
            writer.println("Goodbye!");

        } catch (IOException ex) {
            System.out.println("Error in client interaction: " + ex.getMessage());
        }
    }

    // Register a new user with a specific role (Customer or Seller)
    private static void registerNewUser(String userID, String password, int clientId, PrintWriter writer, String role) {
        // Add new credentials
        JSONObject newCredential = new JSONObject();
        newCredential.put("userID", userID);
        newCredential.put("password", password);
        credentials.getJSONArray("users").put(newCredential);
        saveCredentials();

        // Add new user data to the main database with role and initial 1000 credits for customers
        JSONObject newUser = new JSONObject();
        newUser.put("clientId", clientId);
        newUser.put("username", userID);
        newUser.put("role", role);
        newUser.put("credits", role.equals("customer") ? 1000 : 0);  // Initial 1000 credits for customers
        newUser.put(role.equals("customer") ? "purchaseHistory" : "transactionHistory", new JSONArray());
        database.getJSONArray("users").put(newUser);
        saveDatabase();

        writer.println("Registration complete. You are registered as a " + role + ".");

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
        //writer.println("Top 5 Sellers (by completed sales transactions_FROM INSIDE SHOW TOP SELLER FUNCTION):");
        for (int i = 0; i < Math.min(5, sellerList.size()); i++) {
            JSONObject seller = sellerList.get(i);
            writer.println((i + 1) + ". Seller: " + seller.getString("seller") +
                    " - Completed Transactions: " + seller.getInt("transactions"));
        }
        writer.println("");  // End of top sellers list
    }

    // Retrieve a user's credentials by userID from the credentials file
    private static JSONObject getCredential(String userID) {
        JSONArray users = credentials.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getString("userID").equals(userID)) {
                return user;
            }
        }
        return null;
    }

    // Retrieve a user by their userID from the main database
    private static JSONObject getUserByID(String userID) {
        JSONArray users = database.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getString("username").equals(userID)) {
                return user;
            }
        }
        return null;
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
}


















