import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory class manages the collection of medicines, coordinates
 * search, add, update, delete, sell operations, and persists data via FileHandler.
 */
public class Inventory {
    private List<Medicine> medicines;
    private FileHandler fileHandler;
    private int billCounter = 1000;

    public Inventory(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.medicines = fileHandler.loadMedicines();
        // Initialize bill counter based on existing bills count if available
        List<Bill> existingBills = fileHandler.loadBills();
        this.billCounter = 1000 + existingBills.size();
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }

    /**
     * Adds a new medicine. Prevents duplicate ID.
     */
    public boolean addMedicine(Medicine medicine) {
        if (medicine == null) {
            System.out.println("Cannot add null medicine.");
            return false;
        }

        if (searchById(medicine.getId()) != null) {
            System.out.printf("Error: Medicine with ID %d already exists!%n", medicine.getId());
            return false;
        }

        medicines.add(medicine);
        boolean saved = fileHandler.saveMedicines(medicines);
        if (saved) {
            System.out.printf("Success: Medicine '%s' (ID: %d) added successfully.%n", medicine.getName(), medicine.getId());
        }
        return saved;
    }

    /**
     * Searches for a medicine by its unique ID.
     */
    public Medicine searchById(int id) {
        for (Medicine m : medicines) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    /**
     * Searches for medicines by name (case-insensitive substring match).
     */
    public List<Medicine> searchByName(String keyword) {
        List<Medicine> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return results;
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        for (Medicine m : medicines) {
            if (m.getName().toLowerCase().contains(lowerKeyword)) {
                results.add(m);
            }
        }
        return results;
    }

    /**
     * Updates quantity of a medicine by ID.
     */
    public boolean updateQuantity(int id, int newQuantity) {
        Medicine med = searchById(id);
        if (med == null) {
            System.out.printf("Error: Medicine with ID %d not found.%n", id);
            return false;
        }
        if (newQuantity < 0) {
            System.out.println("Error: Quantity cannot be negative.");
            return false;
        }
        med.setQuantity(newQuantity);
        boolean saved = fileHandler.saveMedicines(medicines);
        if (saved) {
            System.out.printf("Success: Updated quantity for '%s' to %d units.%n", med.getName(), newQuantity);
        }
        return saved;
    }

    /**
     * Updates unit price of a medicine by ID.
     */
    public boolean updatePrice(int id, double newPrice) {
        Medicine med = searchById(id);
        if (med == null) {
            System.out.printf("Error: Medicine with ID %d not found.%n", id);
            return false;
        }
        if (newPrice < 0.0) {
            System.out.println("Error: Price cannot be negative.");
            return false;
        }
        med.setPrice(newPrice);
        boolean saved = fileHandler.saveMedicines(medicines);
        if (saved) {
            System.out.printf("Success: Updated price for '%s' to Rs.%.2f.%n", med.getName(), newPrice);
        }
        return saved;
    }

    /**
     * Updates expiry date of a medicine by ID.
     */
    public boolean updateExpiryDate(int id, LocalDate newDate) {
        Medicine med = searchById(id);
        if (med == null) {
            System.out.printf("Error: Medicine with ID %d not found.%n", id);
            return false;
        }
        if (newDate == null) {
            System.out.println("Error: Expiry date cannot be null.");
            return false;
        }
        med.setExpiryDate(newDate);
        boolean saved = fileHandler.saveMedicines(medicines);
        if (saved) {
            System.out.printf("Success: Updated expiry date for '%s' to %s.%n", med.getName(), newDate);
        }
        return saved;
    }

    /**
     * Deletes a medicine by ID.
     */
    public boolean deleteMedicine(int id) {
        Medicine med = searchById(id);
        if (med == null) {
            System.out.printf("Error: Medicine with ID %d not found.%n", id);
            return false;
        }
        medicines.remove(med);
        boolean saved = fileHandler.saveMedicines(medicines);
        if (saved) {
            System.out.printf("Success: Medicine '%s' (ID: %d) deleted from inventory.%n", med.getName(), id);
        }
        return saved;
    }

    /**
     * Adjusts stock quantity (can be positive to restock, or negative to deduct).
     * Prevents stock from becoming negative.
     */
    public boolean adjustStock(int id, int delta) {
        Medicine med = searchById(id);
        if (med == null) {
            System.out.printf("Error: Medicine with ID %d not found.%n", id);
            return false;
        }
        int updatedQty = med.getQuantity() + delta;
        if (updatedQty < 0) {
            System.out.printf("Error: Cannot reduce stock below 0. Current stock is %d, requested deduction is %d.%n",
                    med.getQuantity(), Math.abs(delta));
            return false;
        }
        med.setQuantity(updatedQty);
        boolean saved = fileHandler.saveMedicines(medicines);
        if (saved) {
            System.out.printf("Success: Stock for '%s' updated from %d to %d units.%n",
                    med.getName(), (updatedQty - delta), updatedQty);
        }
        return saved;
    }

    /**
     * Sells a medicine and generates a bill:
     * 1. Validates medicine exists
     * 2. Validates medicine is not expired
     * 3. Validates sufficient stock
     * 4. Reduces stock and saves to medicines.txt
     * 5. Creates and saves bill to bills.txt
     */
    public Bill sellMedicine(int id, int quantityToSell, String customerName) {
        Medicine med = searchById(id);
        if (med == null) {
            System.out.printf("Error: Medicine with ID %d not found!%n", id);
            return null;
        }

        // Expiry check: Prohibit selling expired medicine
        if (med.isExpired()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.printf(" CRITICAL ERROR: CANNOT SELL EXPIRED MEDICINE!%n");
            System.out.printf(" Medicine '%s' (ID: %d) expired on %s.%n", med.getName(), med.getId(), med.getExpiryDate());
            System.out.println(" Please discard or return this batch immediately.");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return null;
        }

        // Expiring soon warning (allowed with alert)
        if (med.isExpiringSoon(30)) {
            System.out.printf(" [Notice] Medicine '%s' is expiring soon (%s). Sale permitted.%n", med.getName(), med.getExpiryDate());
        }

        // Stock quantity check
        if (quantityToSell <= 0) {
            System.out.println("Error: Quantity to sell must be greater than 0.");
            return null;
        }

        if (med.getQuantity() < quantityToSell) {
            System.out.printf("Error: Insufficient stock for '%s'. Available: %d units, Requested: %d units.%n",
                    med.getName(), med.getQuantity(), quantityToSell);
            return null;
        }

        // Deduct stock
        med.setQuantity(med.getQuantity() - quantityToSell);
        fileHandler.saveMedicines(medicines);

        // Generate bill
        billCounter++;
        String billId = "INV-" + billCounter;
        Bill bill = new Bill(billId, LocalDateTime.now(), customerName, med.getId(), med.getName(), quantityToSell, med.getPrice());
        fileHandler.saveBill(bill);

        return bill;
    }

    /**
     * Prints a formatted tabular view of the given medicines list.
     */
    public void printMedicineTable(List<Medicine> list) {
        System.out.println("\n------------------------------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-24s | %-8s | %-12s | %-12s | %-16s%n",
                "ID", "Medicine Name", "Quantity", "Price (Rs)", "Expiry Date", "Status");
        System.out.println("------------------------------------------------------------------------------------------------------");

        if (list == null || list.isEmpty()) {
            System.out.println(" No medicine records to display.");
        } else {
            for (Medicine m : list) {
                System.out.printf("%-6d | %-24s | %-8d | Rs.%-9.2f | %-12s | %-16s%n",
                        m.getId(), m.getName(), m.getQuantity(), m.getPrice(), m.getExpiryDate(), m.getStatusDescription());
            }
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
        System.out.printf(" Total Records: %d%n%n", (list != null ? list.size() : 0));
    }

    /**
     * Prints all medicines in inventory.
     */
    public void viewAllMedicines() {
        printMedicineTable(medicines);
    }
}
