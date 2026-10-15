package com.app;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHandler manages reading and writing medicine data to 'medicines.txt'
 * and sales transaction history to 'bills.txt'.
 */
public class FileHandler {
    private static final String MEDICINE_FILE = "medicines.txt";
    private static final String BILLS_FILE = "bills.txt";

    public FileHandler() {
        // Ensure data file exists with starter sample data if new
        initializeMedicineFile();
    }

    /**
     * Creates medicines.txt with sample data if it does not already exist.
     */
    private void initializeMedicineFile() {
        File file = new File(MEDICINE_FILE);
        if (!file.exists()) {
            System.out.println("Data file '" + MEDICINE_FILE + "' not found. Creating with initial sample medicines...");
            List<Medicine> sampleMedicines = new ArrayList<>();
            // Creating starter data with different statuses (valid, expiring soon, expired, low stock)
            // Current date context: October 2026
            sampleMedicines.add(Medicine.fromFileString("101,Paracetamol,50,2.00,2026-10-25")); // Expiring soon (10 days)
            sampleMedicines.add(Medicine.fromFileString("102,Amoxicillin 500mg,25,12.50,2027-05-15")); // Valid
            sampleMedicines.add(Medicine.fromFileString("103,Ibuprofen 400mg,8,5.00,2027-02-10")); // Low stock (8 <= 10)
            sampleMedicines.add(Medicine.fromFileString("104,Cough Syrup 100ml,15,45.00,2026-09-30")); // Expired
            sampleMedicines.add(Medicine.fromFileString("105,Cetirizine 10mg,60,3.50,2026-11-05")); // Expiring soon (21 days)
            sampleMedicines.add(Medicine.fromFileString("106,Azithromycin 500mg,5,22.00,2026-09-15")); // Expired & low stock
            sampleMedicines.add(Medicine.fromFileString("107,Metformin 500mg,80,4.00,2027-12-31")); // Valid
            sampleMedicines.add(Medicine.fromFileString("108,Omeprazole 20mg,7,9.00,2027-08-20")); // Low stock

            saveMedicines(sampleMedicines);
        }
    }

    /**
     * Reads all medicine records from medicines.txt into an ArrayList.
     */
    public List<Medicine> loadMedicines() {
        List<Medicine> medicines = new ArrayList<>();
        File file = new File(MEDICINE_FILE);

        if (!file.exists()) {
            return medicines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                Medicine med = Medicine.fromFileString(line);
                if (med != null) {
                    medicines.add(med);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading medicines file: " + e.getMessage());
        }

        return medicines;
    }

    /**
     * Saves the entire medicine list back to medicines.txt.
     */
    public boolean saveMedicines(List<Medicine> medicines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEDICINE_FILE))) {
            writer.write("# Medicine Inventory Records (Format: ID,Name,Quantity,Price,ExpiryDate)");
            writer.newLine();
            for (Medicine med : medicines) {
                writer.write(med.toFileString());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving medicines to file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Appends a completed sale/bill to bills.txt for historical audit.
     */
    public boolean saveBill(Bill bill) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BILLS_FILE, true))) {
            writer.write(bill.toFileString());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving bill to history file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads bill history from bills.txt.
     */
    public List<Bill> loadBills() {
        List<Bill> bills = new ArrayList<>();
        File file = new File(BILLS_FILE);

        if (!file.exists()) {
            return bills;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                Bill bill = Bill.fromFileString(line);
                if (bill != null) {
                    bills.add(bill);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading bills file: " + e.getMessage());
        }

        return bills;
    }
}
