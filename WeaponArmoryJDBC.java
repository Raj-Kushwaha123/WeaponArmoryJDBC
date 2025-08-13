import java.sql.*;
import java.util.*;

public class WeaponArmoryJDBC {
    private static final String URL = "jdbc:mysql://localhost:3306/WeaponArmory";
    private static final String USER = "root";
    private static final String PASSWORD = "r1sh4v@jha";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\nMain Menu:");
                System.out.println("1. Display tables");
                System.out.println("2. Add a new weapon");
                System.out.println("3. Add a new attachment");
                System.out.println("4. Place order");
                System.out.println("5. Add a customer or employee");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int mainChoice = scanner.nextInt();
                scanner.nextLine();

                switch (mainChoice) {
                    case 1:
                        System.out.print("Enter table name to display: ");
                        String tableName = scanner.nextLine();
                        displayTable(conn, tableName);
                        break;
                    case 2:
                        addWeapon(conn, scanner);
                        break;
                    case 3:
                        addAttachment(conn, scanner);
                        break;
                    case 4:
                        placeOrder(conn, scanner);
                        break;
                    case 5:
                        addPersonnel(conn, scanner);
                        break;
                    case 6:
                        System.out.println("Exiting program.");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayTable(Connection conn, String tableName) {
        String query = "SELECT * FROM " + tableName;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addWeapon(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nAdd a new weapon:");
        System.out.print("Enter weapon name: ");
        String name = scanner.nextLine();

        String checkQuery = "SELECT * FROM Weapons WHERE name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int currentStock = rs.getInt("stock");
                System.out.println("Weapon already exists. Current stock: " + currentStock);
                System.out.print("Enter additional stock to add: ");
                int additionalStock = scanner.nextInt();
                scanner.nextLine();

                int newStock = currentStock + additionalStock;
                String updateQuery = "UPDATE Weapons SET stock = ? WHERE name = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, newStock);
                    updateStmt.setString(2, name);
                    updateStmt.executeUpdate();
                    System.out.println("Stock updated successfully!");
                }
            } else {
                System.out.print("Enter caliber: ");
                String caliber = scanner.nextLine();
                System.out.print("Enter price: ");
                double price = scanner.nextDouble();
                System.out.print("Enter stock: ");
                int stock = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Enter manufacturer: ");
                String manufacture = scanner.nextLine();

                String insertQuery = "INSERT INTO Weapons (name, caliber, price, stock, manufacture) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, caliber);
                    insertStmt.setDouble(3, price);
                    insertStmt.setInt(4, stock);
                    insertStmt.setString(5, manufacture);
                    insertStmt.executeUpdate();
                    System.out.println("Weapon added successfully!");
                }
            }
        }
    }

    private static void addAttachment(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nAdd a new attachment:");
        System.out.print("Enter attachment name: ");
        String name = scanner.nextLine();

        String checkQuery = "SELECT * FROM Attachments WHERE name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int currentStock = rs.getInt("stock");
                System.out.println("Attachment already exists. Current stock: " + currentStock);
                System.out.print("Enter additional stock to add: ");
                int additionalStock = scanner.nextInt();
                scanner.nextLine();

                int newStock = currentStock + additionalStock;
                String updateQuery = "UPDATE Attachments SET stock = ? WHERE name = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, newStock);
                    updateStmt.setString(2, name);
                    updateStmt.executeUpdate();
                    System.out.println("Stock updated successfully!");
                }
            } else {
                System.out.print("Enter price: ");
                double price = scanner.nextDouble();
                System.out.print("Enter stock: ");
                int stock = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Enter manufacturer: ");
                String manufacture = scanner.nextLine();

                String insertQuery = "INSERT INTO Attachments (name, price, stock, manufacture) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, name);
                    insertStmt.setDouble(2, price);
                    insertStmt.setInt(3, stock);
                    insertStmt.setString(4, manufacture);
                    insertStmt.executeUpdate();
                    System.out.println("Attachment added successfully!");

                    // Ask if this attachment fits any weapons
                    System.out.print("Does this attachment fit any weapons? (y/n): ");
                    String response = scanner.nextLine();
                    if (response.equalsIgnoreCase("y")) {
                        linkAttachmentToWeapons(conn, scanner, name);
                    }
                }
            }
        }
    }

    private static void linkAttachmentToWeapons(Connection conn, Scanner scanner, String attachmentName) throws SQLException {
        // Get the attachment ID
        int attachmentId = 0;
        String attachmentQuery = "SELECT attachment_id FROM Attachments WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(attachmentQuery)) {
            stmt.setString(1, attachmentName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                attachmentId = rs.getInt("attachment_id");
            } else {
                System.out.println("Error: Attachment not found.");
                return;
            }
        }

        // Display available weapons
        System.out.println("\nAvailable weapons:");
        String weaponsQuery = "SELECT weap_id, name FROM Weapons";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(weaponsQuery)) {
            while (rs.next()) {
                System.out.println(rs.getInt("weap_id") + ": " + rs.getString("name"));
            }
        }

        // Link to weapons
        while (true) {
            System.out.print("\nEnter weapon ID to link (or 0 to finish): ");
            int weaponId = scanner.nextInt();
            scanner.nextLine();

            if (weaponId == 0) {
                break;
            }

            // Check if the weapon exists
            String weaponCheckQuery = "SELECT name FROM Weapons WHERE weap_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(weaponCheckQuery)) {
                stmt.setInt(1, weaponId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Error: Weapon ID not found.");
                    continue;
                }
            }

            // Create the link
            String linkQuery = "INSERT INTO Weapon_Fits_Attachment (weap_id, attachment_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(linkQuery)) {
                stmt.setInt(1, weaponId);
                stmt.setInt(2, attachmentId);
                stmt.executeUpdate();
                System.out.println("Link created successfully!");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // Duplicate entry error
                    System.out.println("This attachment is already linked to this weapon.");
                } else {
                    throw e;
                }
            }
        }
    }

    private static void addPersonnel(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nAdd Personnel:");
        System.out.println("1. Add a customer");
        System.out.println("2. Add an employee");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        if (choice == 1) {
            // Add a customer
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter middle name (or press Enter to skip): ");
            String middleName = scanner.nextLine();
            if (middleName.isEmpty()) {
                middleName = null;
            }

            System.out.print("Enter last name: ");
            String lastName = scanner.nextLine();

            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine();

            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            System.out.print("Enter address: ");
            String address = scanner.nextLine();

            System.out.print("Enter date of birth (YYYY-MM-DD): ");
            String dobString = scanner.nextLine();
            java.sql.Date dob = java.sql.Date.valueOf(dobString);

            conn.setAutoCommit(false);

            try {
                // First insert the customer
                String insertCustomerQuery = "INSERT INTO Customers (first_name, middle_name, last_name, phone, email, address, dob) VALUES (?, ?, ?, ?, ?, ?, ?)";
                int customerId;

                try (PreparedStatement insertStmt = conn.prepareStatement(insertCustomerQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, firstName);
                    insertStmt.setString(2, middleName);
                    insertStmt.setString(3, lastName);
                    insertStmt.setString(4, phone);
                    insertStmt.setString(5, email);
                    insertStmt.setString(6, address);
                    insertStmt.setDate(7, dob);

                    int affectedRows = insertStmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating customer failed, no rows affected.");
                    }

                    try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            customerId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating customer failed, no ID obtained.");
                        }
                    }
                }

                // Now add license info
                System.out.print("Enter license expiry date (YYYY-MM-DD): ");
                String expiryDateString = scanner.nextLine();
                java.sql.Date expiryDate = java.sql.Date.valueOf(expiryDateString);

                String insertLicenseQuery = "INSERT INTO License (expiry_date, customer_id) VALUES (?, ?)";
                try (PreparedStatement licenseStmt = conn.prepareStatement(insertLicenseQuery)) {
                    licenseStmt.setDate(1, expiryDate);
                    licenseStmt.setInt(2, customerId);
                    licenseStmt.executeUpdate();
                }

                conn.commit();
                System.out.println("Customer added successfully! Customer ID: " + customerId);
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error adding customer. Operation rolled back.");
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

        } else if (choice == 2) {
            // Add an employee
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter middle name (or press Enter to skip): ");
            String middleName = scanner.nextLine();
            if (middleName.isEmpty()) {
                middleName = null;
            }

            System.out.print("Enter last name: ");
            String lastName = scanner.nextLine();

            System.out.print("Enter position: ");
            String position = scanner.nextLine();

            System.out.print("Enter salary: ");
            double salary = scanner.nextDouble();
            scanner.nextLine();  // Consume newline

            System.out.print("Enter hire date (YYYY-MM-DD): ");
            String hireDateString = scanner.nextLine();
            java.sql.Date hireDate = java.sql.Date.valueOf(hireDateString);

            String insertQuery = "INSERT INTO Employee (first_name, middle_name, last_name, position, salary, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, firstName);
                insertStmt.setString(2, middleName);
                insertStmt.setString(3, lastName);
                insertStmt.setString(4, position);
                insertStmt.setDouble(5, salary);
                insertStmt.setDate(6, hireDate);

                int affectedRows = insertStmt.executeUpdate();
                if (affectedRows > 0) {
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int employeeId = generatedKeys.getInt(1);
                        System.out.println("Employee added successfully! Employee ID: " + employeeId);
                    }
                }
            }
        } else {
            System.out.println("Invalid choice. Returning to main menu.");
        }
    }

    private static void placeOrder(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nPlace an order:");

        // Lists to track order items
        List<String> weaponNames = new ArrayList<>();
        List<Integer> weaponQuantities = new ArrayList<>();
        List<Integer> weaponIds = new ArrayList<>();
        List<Double> weaponPrices = new ArrayList<>();

        List<String> attachmentNames = new ArrayList<>();
        List<Integer> attachmentQuantities = new ArrayList<>();
        List<Integer> attachmentIds = new ArrayList<>();
        List<Double> attachmentPrices = new ArrayList<>();

        // Order weapons
        System.out.println("\nWeapons:");
        while (true) {
            System.out.print("Enter weapon name (or 'done' to continue to attachments): ");
            String name = scanner.nextLine();
            if (name.equalsIgnoreCase("done")) break;

            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();

            weaponNames.add(name);
            weaponQuantities.add(quantity);
        }

        // Order attachments
        System.out.println("\nAttachments:");
        while (true) {
            System.out.print("Enter attachment name (or 'done' to finish): ");
            String name = scanner.nextLine();
            if (name.equalsIgnoreCase("done")) break;

            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();

            attachmentNames.add(name);
            attachmentQuantities.add(quantity);
        }

        // If no items selected, return to main menu
        if (weaponNames.isEmpty() && attachmentNames.isEmpty()) {
            System.out.println("No items selected. Returning to main menu.");
            return;
        }

        // Get customer and employee info for the transaction
        System.out.print("Enter customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter employee ID: ");
        int employeeId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter payment method: ");
        String paymentMethod = scanner.nextLine();

        conn.setAutoCommit(false);
        boolean orderSuccess = true;
        double totalPrice = 0;

        try {
            // First check if all weapon orders can be fulfilled
            for (int i = 0; i < weaponNames.size(); i++) {
                String name = weaponNames.get(i);
                int quantity = weaponQuantities.get(i);

                String checkQuery = "SELECT weap_id, stock, price FROM Weapons WHERE name = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, name);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        int stock = rs.getInt("stock");
                        double price = rs.getDouble("price");
                        int weapId = rs.getInt("weap_id");

                        if (stock >= quantity) {
                            weaponIds.add(weapId);
                            weaponPrices.add(price);
                            totalPrice += quantity * price;
                        } else {
                            System.out.println("Not enough stock for weapon: " + name + ". Order canceled.");
                            orderSuccess = false;
                            break;
                        }
                    } else {
                        System.out.println("Weapon " + name + " not available. Order canceled.");
                        orderSuccess = false;
                        break;
                    }
                }
            }

            // Then check if all attachment orders can be fulfilled
            if (orderSuccess) {
                for (int i = 0; i < attachmentNames.size(); i++) {
                    String name = attachmentNames.get(i);
                    int quantity = attachmentQuantities.get(i);

                    String checkQuery = "SELECT attachment_id, stock, price FROM Attachments WHERE name = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setString(1, name);
                        ResultSet rs = checkStmt.executeQuery();

                        if (rs.next()) {
                            int stock = rs.getInt("stock");
                            double price = rs.getDouble("price");
                            int attachId = rs.getInt("attachment_id");

                            if (stock >= quantity) {
                                attachmentIds.add(attachId);
                                attachmentPrices.add(price);
                                totalPrice += quantity * price;
                            } else {
                                System.out.println("Not enough stock for attachment: " + name + ". Order canceled.");
                                orderSuccess = false;
                                break;
                            }
                        } else {
                            System.out.println("Attachment " + name + " not available. Order canceled.");
                            orderSuccess = false;
                            break;
                        }
                    }
                }
            }

            if (orderSuccess) {
                // Create transaction first
                String insertTransactionQuery = "INSERT INTO Transaction (total_amount, payment_method, emp_id, cust_id) VALUES (?, ?, ?, ?)";
                int transId;

                try (PreparedStatement transStmt = conn.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
                    transStmt.setDouble(1, totalPrice);
                    transStmt.setString(2, paymentMethod);
                    transStmt.setInt(3, employeeId);
                    transStmt.setInt(4, customerId);
                    transStmt.executeUpdate();

                    ResultSet generatedKeys = transStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        transId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating transaction failed, no ID obtained.");
                    }
                }

                // Process each weapon
                for (int i = 0; i < weaponIds.size(); i++) {
                    int weapId = weaponIds.get(i);
                    int quantity = weaponQuantities.get(i);

                    // Create order entry
                    String insertOrderQuery = "INSERT INTO Orders (quantity, trans_id) VALUES (?, ?)";
                    int orderId;

                    try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
                        orderStmt.setInt(1, quantity);
                        orderStmt.setInt(2, transId);
                        orderStmt.executeUpdate();

                        ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            orderId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating order failed, no ID obtained.");
                        }
                    }

                    // Link order to weapon in Orders_Weapons table
                    String insertOrderWeaponQuery = "INSERT INTO Orders_Weapons (order_id, weap_id) VALUES (?, ?)";
                    try (PreparedStatement orderWeaponStmt = conn.prepareStatement(insertOrderWeaponQuery)) {
                        orderWeaponStmt.setInt(1, orderId);
                        orderWeaponStmt.setInt(2, weapId);
                        orderWeaponStmt.executeUpdate();
                    }

                    // Update weapon stock
                    String updateStockQuery = "UPDATE Weapons SET stock = stock - ? WHERE weap_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateStockQuery)) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setInt(2, weapId);
                        updateStmt.executeUpdate();
                    }
                }

                // Process each attachment
                for (int i = 0; i < attachmentIds.size(); i++) {
                    int attachId = attachmentIds.get(i);
                    int quantity = attachmentQuantities.get(i);

                    // Create order entry
                    String insertOrderQuery = "INSERT INTO Orders (quantity, trans_id) VALUES (?, ?)";
                    int orderId;

                    try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
                        orderStmt.setInt(1, quantity);
                        orderStmt.setInt(2, transId);
                        orderStmt.executeUpdate();

                        ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            orderId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating order failed, no ID obtained.");
                        }
                    }

                    // Link order to attachment in Orders_Attachments table
                    String insertOrderAttachmentQuery = "INSERT INTO Orders_Attachments (order_id, attachment_id) VALUES (?, ?)";
                    try (PreparedStatement orderAttachmentStmt = conn.prepareStatement(insertOrderAttachmentQuery)) {
                        orderAttachmentStmt.setInt(1, orderId);
                        orderAttachmentStmt.setInt(2, attachId);
                        orderAttachmentStmt.executeUpdate();
                    }

                    // Update attachment stock
                    String updateStockQuery = "UPDATE Attachments SET stock = stock - ? WHERE attachment_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateStockQuery)) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setInt(2, attachId);
                        updateStmt.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("Transaction completed successfully! Transaction ID: " + transId);
                System.out.println("Total price: $" + totalPrice);

                // Print order summary
                System.out.println("\nOrder Summary:");
                if (!weaponNames.isEmpty()) {
                    System.out.println("Weapons:");
                    for (int i = 0; i < weaponNames.size(); i++) {
                        System.out.printf("  %s x%d: $%.2f\n",
                                weaponNames.get(i),
                                weaponQuantities.get(i),
                                weaponQuantities.get(i) * weaponPrices.get(i));
                    }
                }

                if (!attachmentNames.isEmpty()) {
                    System.out.println("Attachments:");
                    for (int i = 0; i < attachmentNames.size(); i++) {
                        System.out.printf("  %s x%d: $%.2f\n",
                                attachmentNames.get(i),
                                attachmentQuantities.get(i),
                                attachmentQuantities.get(i) * attachmentPrices.get(i));
                    }
                }

                System.out.printf("Total: $%.2f\n", totalPrice);
            } else {
                conn.rollback();
                System.out.println("Transaction canceled.");
            }
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error during transaction. All changes rolled back.");
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
