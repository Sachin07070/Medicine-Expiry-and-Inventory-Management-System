package com.app;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class for Medicine Expiry & Inventory Tracker.
 * Manages user authentication, navigation menus, user inputs, and workflow execution.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASS = "admin123";

    private static FileHandler fileHandler;
    private static Inventory inventory;
    private static ExpiryChecker expiryChecker;

    public static void main(String[] args) {
        printBanner();

        // 1. Authentication
        if (!authenticateUser()) {
            System.out.println("\nAuthentication failed. Exiting application. Goodbye!");
            return;
        }

        // 2. Initialize application services
        fileHandler = new FileHandler();
        inventory = new Inventory(fileHandler);
        expiryChecker = new ExpiryChecker();

        // 3. Proactive Startup Alerts for Expired and Expiring-Soon Medicines
        expiryChecker.printStartupAlertSummary(inventory.getMedicines());

        // 4. Main Menu Loop
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter your choice (1-6): ");

            switch (choice) {
                case 1:
                    handleMedicineManagementMenu();
                    break;
                case 2:
                    handleExpiryCheckerMenu();
                    break;
                case 3:
                    handleStockManagementMenu();
                    break;
                case 4:
                    handleSellMedicine();
                    break;
                case 5:
                    handleSalesHistory();
                    break;
                case 6:
                    running = false;
                    System.out.println("\nAll inventory changes saved successfully to 'medicines.txt'.");
                    System.out.println("Thank you for using Medicine Expiry & Inventory Tracker!");
                    break;
                default:
                    System.out.println("Invalid option! Please select a number between 1 and 6.");
            }
        }
    }

    /**
     * Application Header Banner.
     */
    private static void printBanner() {
        System.out.println("================================================================================");
        System.out.println("                   MEDICINE EXPIRY & INVENTORY TRACKER                          ");
        System.out.println("                    Core Java & File Handling System                            ");
        System.out.println("================================================================================");
    }

    /**
     * User Login System.
     */
    private static boolean authenticateUser() {
        System.out.println("\n--- User Authentication ---");
        System.out.println("(Default Credentials -> Username: admin | Password: admin123)");
        int attempts = 3;

        while (attempts > 0) {
            System.out.print("Username: ");
            String user = scanner.nextLine().trim();
            System.out.print("Password: ");
            String pass = scanner.nextLine().trim();

            if (DEFAULT_USER.equalsIgnoreCase(user) && DEFAULT_PASS.equals(pass)) {
                System.out.println("\n>>> Login Successful! Welcome to Medicine Inventory Tracker, " + user + ".");
                return true;
            } else {
                attempts--;
                if (attempts > 0) {
                    System.out.printf("Invalid credentials! You have %d attempt(s) remaining.%n%n", attempts);
                }
            }
        }
        return false;
    }

    /**
     * Main Menu display.
     */
    private static void printMainMenu() {
        System.out.println("\n========================================================");
        System.out.println("                       MAIN MENU                        ");
        System.out.println("========================================================");
        System.out.println(" 1. Medicine Management (Add, View, Search, Update, Delete)");
        System.out.println(" 2. Expiry Checker (Expired & Expiring-Soon Alerts)");
        System.out.println(" 3. Stock Management (Quantity, Low-Stock Check, Restock)");
        System.out.println(" 4. Sell Medicine & Generate Invoice");
        System.out.println(" 5. View Sales & Billing History");
        System.out.println(" 6. Exit Application");
        System.out.println("========================================================");
    }

    // =========================================================================
    // MODULE 1: MEDICINE MANAGEMENT
    // =========================================================================
    private static void handleMedicineManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--------------------------------------------------------");
            System.out.println("                 MEDICINE MANAGEMENT                    ");
            System.out.println("--------------------------------------------------------");
            System.out.println(" 1. Add New Medicine");
            System.out.println(" 2. View All Medicines");
            System.out.println(" 3. Search Medicine by ID");
            System.out.println(" 4. Search Medicine by Name");
            System.out.println(" 5. Update Medicine Details");
            System.out.println(" 6. Delete Medicine");
            System.out.println(" 7. Return to Main Menu");
            System.out.println("--------------------------------------------------------");
            int choice = readInt("Enter choice (1-7): ");

            switch (choice) {
                case 1:
                    addNewMedicine();
                    break;
                case 2:
                    inventory.viewAllMedicines();
                    break;
                case 3:
                    searchMedicineById();
                    break;
                case 4:
                    searchMedicineByName();
                    break;
                case 5:
                    updateMedicineDetails();
                    break;
                case 6:
                    deleteMedicine();
                    break;
                case 7:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option! Please enter 1-7.");
            }
        }
    }

    private static void addNewMedicine() {
        System.out.println("\n--- Add New Medicine ---");
        int id;
        while (true) {
            id = readInt("Enter Medicine ID (e.g. 109): ");
            if (id <= 0) {
                System.out.println("ID must be greater than 0.");
                continue;
            }
            if (inventory.searchById(id) != null) {
                System.out.printf("A medicine with ID %d already exists. Please choose a different ID.%n", id);
                return;
            }
            break;
        }

        String name;
        while (true) {
            System.out.print("Enter Medicine Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                continue;
            }
            break;
        }

        int quantity = readInt("Enter Initial Quantity: ");
        while (quantity < 0) {
            System.out.println("Quantity cannot be negative.");
            quantity = readInt("Enter Initial Quantity: ");
        }

        double price = readDouble("Enter Unit Price (Rs): ");
        while (price < 0.0) {
            System.out.println("Price cannot be negative.");
            price = readDouble("Enter Unit Price (Rs): ");
        }

        LocalDate expiryDate = readDate("Enter Expiry Date (YYYY-MM-DD, e.g. 2027-12-31): ");

        Medicine newMed = new Medicine(id, name, quantity, price, expiryDate);
        inventory.addMedicine(newMed);
    }

    private static void searchMedicineById() {
        int id = readInt("\nEnter Medicine ID to search: ");
        Medicine med = inventory.searchById(id);
        if (med == null) {
            System.out.printf("No medicine found with ID %d.%n", id);
        } else {
            System.out.println("\nMedicine Found:");
            inventory.printMedicineTable(List.of(med));
        }
    }

    private static void searchMedicineByName() {
        System.out.print("\nEnter Medicine Name (or partial name) to search: ");
        String name = scanner.nextLine().trim();
        List<Medicine> results = inventory.searchByName(name);
        if (results.isEmpty()) {
            System.out.printf("No medicines found matching '%s'.%n", name);
        } else {
            System.out.printf("\nSearch Results for '%s':%n", name);
            inventory.printMedicineTable(results);
        }
    }

    private static void updateMedicineDetails() {
        int id = readInt("\nEnter Medicine ID to update: ");
        Medicine med = inventory.searchById(id);
        if (med == null) {
            System.out.printf("Medicine with ID %d not found.%n", id);
            return;
        }

        System.out.println("\nCurrent Medicine Details:");
        inventory.printMedicineTable(List.of(med));

        System.out.println("Select field to update:");
        System.out.println(" 1. Quantity");
        System.out.println(" 2. Price");
        System.out.println(" 3. Expiry Date");
        System.out.println(" 4. Cancel");
        int option = readInt("Enter choice (1-4): ");

        switch (option) {
            case 1:
                int newQty = readInt("Enter new Quantity: ");
                inventory.updateQuantity(id, newQty);
                break;
            case 2:
                double newPrice = readDouble("Enter new Price (Rs): ");
                inventory.updatePrice(id, newPrice);
                break;
            case 3:
                LocalDate newDate = readDate("Enter new Expiry Date (YYYY-MM-DD): ");
                inventory.updateExpiryDate(id, newDate);
                break;
            case 4:
                System.out.println("Update cancelled.");
                break;
            default:
                System.out.println("Invalid selection.");
        }
    }

    private static void deleteMedicine() {
        int id = readInt("\nEnter Medicine ID to delete: ");
        Medicine med = inventory.searchById(id);
        if (med == null) {
            System.out.printf("Medicine with ID %d not found.%n", id);
            return;
        }

        System.out.printf("Are you sure you want to delete '%s' (ID: %d)? (yes/no): ", med.getName(), id);
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            inventory.deleteMedicine(id);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // =========================================================================
    // MODULE 2: EXPIRY CHECKER
    // =========================================================================
    private static void handleExpiryCheckerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--------------------------------------------------------");
            System.out.println("                 EXPIRY CHECKER & ALERTS                ");
            System.out.println("--------------------------------------------------------");
            System.out.println(" 1. View Expired Medicines");
            System.out.println(" 2. View Medicines Expiring Soon (Next 30 Days)");
            System.out.println(" 3. View Medicines Expiring in Custom Days");
            System.out.println(" 4. System Health Summary");
            System.out.println(" 5. Return to Main Menu");
            System.out.println("--------------------------------------------------------");
            int choice = readInt("Enter choice (1-5): ");

            switch (choice) {
                case 1:
                    expiryChecker.printExpiredReport(inventory.getMedicines());
                    break;
                case 2:
                    expiryChecker.printExpiringSoonReport(inventory.getMedicines(), ExpiryChecker.EXPIRY_THRESHOLD_DAYS);
                    break;
                case 3:
                    int days = readInt("Enter threshold in days (e.g. 60): ");
                    if (days <= 0) {
                        System.out.println("Days must be positive.");
                    } else {
                        expiryChecker.printExpiringSoonReport(inventory.getMedicines(), days);
                    }
                    break;
                case 4:
                    expiryChecker.printStartupAlertSummary(inventory.getMedicines());
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option! Please enter 1-5.");
            }
        }
    }

    // =========================================================================
    // MODULE 3: STOCK MANAGEMENT
    // =========================================================================
    private static void handleStockManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--------------------------------------------------------");
            System.out.println("                    STOCK MANAGEMENT                    ");
            System.out.println("--------------------------------------------------------");
            System.out.println(" 1. View All Current Stock");
            System.out.println(" 2. View Low Stock Alert (<= 10 units)");
            System.out.println(" 3. Restock Medicine (Add Quantity)");
            System.out.println(" 4. Deduct Medicine Stock (Manual Adjustment)");
            System.out.println(" 5. Return to Main Menu");
            System.out.println("--------------------------------------------------------");
            int choice = readInt("Enter choice (1-5): ");

            switch (choice) {
                case 1:
                    inventory.viewAllMedicines();
                    break;
                case 2:
                    expiryChecker.printLowStockReport(inventory.getMedicines(), ExpiryChecker.LOW_STOCK_THRESHOLD);
                    break;
                case 3:
                    handleRestock(true);
                    break;
                case 4:
                    handleRestock(false);
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option! Please enter 1-5.");
            }
        }
    }

    private static void handleRestock(boolean isIncrease) {
        int id = readInt("\nEnter Medicine ID: ");
        Medicine med = inventory.searchById(id);
        if (med == null) {
            System.out.printf("Medicine with ID %d not found.%n", id);
            return;
        }

        System.out.printf("Current stock of '%s' is %d units.%n", med.getName(), med.getQuantity());
        String actionName = isIncrease ? "add (restock)" : "deduct";
        int amount = readInt("Enter quantity to " + actionName + ": ");
        if (amount <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return;
        }

        int delta = isIncrease ? amount : -amount;
        inventory.adjustStock(id, delta);
    }

    // =========================================================================
    // MODULE 4: SELL MEDICINE & INVOICE
    // =========================================================================
    private static void handleSellMedicine() {
        System.out.println("\n========================================================");
        System.out.println("                SELL MEDICINE & GENERATE BILL           ");
        System.out.println("========================================================");

        int id = readInt("Enter Medicine ID to sell: ");
        Medicine med = inventory.searchById(id);
        if (med == null) {
            System.out.printf("Medicine with ID %d not found!%n", id);
            return;
        }

        // Show medicine summary before sale
        System.out.printf("Selected: %s | Available Stock: %d | Price: Rs.%.2f | Expiry: %s%n",
                med.getName(), med.getQuantity(), med.getPrice(), med.getExpiryDate());

        // Fast expiry check feedback
        if (med.isExpired()) {
            System.out.println("\n[!] WARNING: This medicine is EXPIRED! Sales are strictly blocked.");
            return;
        }

        System.out.print("Enter Customer Name (leave blank for 'Walk-in Customer'): ");
        String customer = scanner.nextLine().trim();

        int qtyToSell = readInt("Enter Quantity to sell: ");
        if (qtyToSell <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return;
        }

        Bill bill = inventory.sellMedicine(id, qtyToSell, customer);
        if (bill != null) {
            bill.printReceipt();
            System.out.println("Sale successfully completed, stock deducted, and invoice saved to 'bills.txt'.");
        }
    }

    // =========================================================================
    // MODULE 5: BILLING HISTORY
    // =========================================================================
    private static void handleSalesHistory() {
        List<Bill> bills = fileHandler.loadBills();
        System.out.println("\n----------------------------------------------------------------------------------------------------------------");
        System.out.println("                                             SALES & BILLING HISTORY                                            ");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-19s | %-18s | %-6s | %-20s | %-6s | %-10s%n",
                "Bill ID", "Date & Time", "Customer", "MedID", "Medicine Name", "Qty", "Total (Rs)");
        System.out.println("----------------------------------------------------------------------------------------------------------------");

        if (bills.isEmpty()) {
            System.out.println(" No sales records found in history yet.");
        } else {
            double grandTotal = 0.0;
            for (Bill b : bills) {
                System.out.printf("%-12s | %-19s | %-18s | %-6d | %-20s | %-6d | Rs.%-8.2f%n",
                        b.getBillId(), b.getDateTime().format(Bill.FORMATTER), b.getCustomerName(),
                        b.getMedicineId(), b.getMedicineName(), b.getQuantitySold(), b.getTotalAmount());
                grandTotal += b.getTotalAmount();
            }
            System.out.println("----------------------------------------------------------------------------------------------------------------");
            System.out.printf(" Total Bills: %d | Grand Revenue: Rs.%.2f%n", bills.size(), grandTotal);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------\n");
    }

    // =========================================================================
    // INPUT UTILITIES
    // =========================================================================
    private static int readInt(String prompt) {
        while (true) {
        	
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid numeric value.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, Medicine.DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format! Please enter date in YYYY-MM-DD format (e.g. 2026-10-25).");
            }
        }
    }
}
