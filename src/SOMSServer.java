import java.io.*;
import java.net.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

// json-20240303.jar : JSON JAR VERSION

public class SOMSServer {
    private static final int PORT = 12345;
    private static final String DATABASE_FILE = "soms_database.json";
    private static JSONObject database;

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
        } catch (IOException ex) {
            System.out.println("Error loading database: " + ex.getMessage());
            database = new JSONObject();
            database.put("users", new JSONArray());  // Initialize with an empty array
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

            writer.println("Welcome! Enter your username: ");
            String username = reader.readLine();

            // Check if user exists or add new user
            JSONObject user = getUser(username);
            if (user == null) {
                writer.println("You are a new user. Are you a Buyer or Seller?");
                String role = reader.readLine();
                user = new JSONObject();
                user.put("username", username);
                user.put("role", role.toLowerCase());
                user.put("credits", 100);  // Give default credits
                user.put("transactions", new JSONArray());
                database.getJSONArray("users").put(user);
                saveDatabase();  // Save the new user to the database
            } else {
                writer.println("Welcome back, " + username + " (" + user.getString("role") + ")");
            }

            // Main interaction loop
            String command;
            do {
                writer.println("Enter a command (view credits, buy, sell, exit): ");
                command = reader.readLine();

                switch (command.toLowerCase()) {
                    case "view credits":
                        writer.println("Your current credits: " + user.getInt("credits"));
                        break;

                    case "buy":
                        if (user.getString("role").equals("buyer")) {
                            writer.println("Enter item to buy: ");
                            String item = reader.readLine();
                            writer.println("Enter quantity: ");
                            int quantity = Integer.parseInt(reader.readLine());

                            // Simulate a purchase and deduct credits
                            int cost = quantity * 10;  // Assume each item costs 10 units
                            int credits = user.getInt("credits");
                            if (credits >= cost) {
                                user.put("credits", credits - cost);
                                writer.println("Purchase successful. Item: " + item + ", Quantity: " + quantity);
                                JSONObject transaction = new JSONObject();
                                transaction.put("item", item);
                                transaction.put("quantity", quantity);
                                transaction.put("cost", cost);
                                user.getJSONArray("transactions").put(transaction);
                                saveDatabase();
                            } else {
                                writer.println("Insufficient credits!");
                            }
                        } else {
                            writer.println("You are not a buyer.");
                        }
                        break;

                    case "sell":
                        if (user.getString("role").equals("seller")) {
                            writer.println("Enter item to sell: ");
                            String item = reader.readLine();
                            writer.println("Enter quantity: ");
                            int quantity = Integer.parseInt(reader.readLine());
                            writer.println("Item listed for sale. Item: " + item + ", Quantity: " + quantity);
                            // Seller logic can be expanded to track items sold, etc.
                        } else {
                            writer.println("You are not a seller.");
                        }
                        break;

                    case "exit":
                        writer.println("Goodbye!");
                        break;

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
}