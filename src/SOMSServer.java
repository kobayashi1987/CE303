import java.io.*;
import java.net.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


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
                System.out.println("New client connected");
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

            // If items list is empty, add some default items
            if (database.getJSONArray("items").length() == 0) {
                addDefaultItems();
            }
        } catch (IOException ex) {
            System.out.println("Error loading database: " + ex.getMessage());
            database = new JSONObject();
            database.put("users", new JSONArray());
            database.put("items", new JSONArray());
            database.put("transactions", new JSONArray());
            addDefaultItems();  // Add default items if database is empty
        }
    }

    // Add some default items to the item list
    private static void addDefaultItems() {
        JSONArray items = database.getJSONArray("items");

        JSONObject item1 = new JSONObject();
        item1.put("itemName", "Laptop");
        item1.put("price", 1000);
        item1.put("quantity", 5);
        item1.put("seller", "seller1");

        JSONObject item2 = new JSONObject();
        item2.put("itemName", "Headphones");
        item2.put("price", 150);
        item2.put("quantity", 10);
        item2.put("seller", "seller1");

        JSONObject item3 = new JSONObject();
        item3.put("itemName", "Smartphone");
        item3.put("price", 800);
        item3.put("quantity", 3);
        item3.put("seller", "seller1");

        items.put(item1);
        items.put(item2);
        items.put(item3);

        saveDatabase();  // Save the updated database with the new items
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

            // newly added code stars here:
            int clientId = clientCounter.getAndIncrement();  // Generate a unique client ID
            writer.println("Welcome! Your Client ID: " + clientId);
            // newly added code ends here
            writer.println("Enter your username: ");
            String username = reader.readLine();
            System.out.println("New user joint: " + username); // To show new user joined
            System.out.println("now total user number is: " + getTotalUsers());  // To show total user number

            // Check if user exists or add new user
            JSONObject user = getUser(username);
            if (user == null) {
                writer.println("You are a new user. Are you a Customer or Seller?");
                String role = reader.readLine();
                user = new JSONObject();
                user.put("username", username);
                user.put("role", role.toLowerCase());
                user.put("credits", 1000);  // Give default credits
                user.put(role.equals("customer") ? "purchaseHistory" : "transactionHistory", new JSONArray());
                database.getJSONArray("users").put(user);
                saveDatabase();  // Save the new user to the database
            } else {
                writer.println("Welcome back, " + username + " (" + user.getString("role") + ")");
            }

            // Main interaction loop
            String command;
            do {
                writer.println("Enter a command (view credits, buy, sell, view items, top up, view history, exit): ");
                command = reader.readLine();

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
                            writer.println("Enter item name to sell: ");
                            String itemName = reader.readLine();
                            writer.println("Enter price per item: ");
                            int price = Integer.parseInt(reader.readLine());
                            writer.println("Enter quantity: ");
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

                    case "exit":
                        writer.println("Goodbye!");
                        socket.close();  // Close the socket and end the client connection
                        break;  // Exit the loop and close the connection properly

                    default:
                        writer.println("Invalid command.");
                }

            } while (!command.equalsIgnoreCase("exit"));

        } catch (IOException ex) {
            System.out.println("Error in client interaction: " + ex.getMessage());
        }
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

}