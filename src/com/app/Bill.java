package com.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bill class representing sales transactions and receipts.
 */
public class Bill {
    private String billId;
    private LocalDateTime dateTime;
    private int medicineId;
    private String medicineName;
    private int quantitySold;
    private double unitPrice;
    private double totalAmount;
    private String customerName;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Bill(String billId, LocalDateTime dateTime, String customerName, int medicineId, String medicineName, int quantitySold, double unitPrice) {
        this.billId = billId;
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now();
        this.customerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName.trim() : "Walk-in Customer";
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice * quantitySold;
    }

    public String getBillId() {
        return billId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Prints an attractive, formatted receipt to console.
     */
    public void printReceipt() {
        System.out.println();
        System.out.println("========================================================");
        System.out.println("                 PHARMACY SALES INVOICE                 ");
        System.out.println("========================================================");
        System.out.printf(" Invoice No   : %s%n", billId);
        System.out.printf(" Date & Time  : %s%n", dateTime.format(FORMATTER));
        System.out.printf(" Customer     : %s%n", customerName);
        System.out.println("--------------------------------------------------------");
        System.out.printf(" %-10s %-20s %-8s %-10s%n", "Med ID", "Medicine Name", "Qty", "Price (Rs)");
        System.out.println("--------------------------------------------------------");
        System.out.printf(" %-10d %-20s %-8d Rs.%-8.2f%n", medicineId, medicineName, quantitySold, unitPrice);
        System.out.println("--------------------------------------------------------");
        System.out.printf(" TOTAL AMOUNT :                                Rs.%.2f%n", totalAmount);
        System.out.println("========================================================");
        System.out.println("         Thank you for your purchase! Get well soon!    ");
        System.out.println("========================================================");
        System.out.println();
    }

    /**
     * CSV line representation for sales file.
     * Format: billId,dateTime,customerName,medicineId,medicineName,quantitySold,unitPrice,totalAmount
     */
    public String toFileString() {
        return billId + "," + dateTime.format(FORMATTER) + "," + customerName + "," + medicineId + "," + medicineName + "," + quantitySold + "," + String.format("%.2f", unitPrice) + "," + String.format("%.2f", totalAmount);
    }

    public static Bill fromFileString(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }
        String[] parts = line.split(",");
        if (parts.length != 8) {
            return null;
        }
        try {
            String billId = parts[0].trim();
            LocalDateTime dt = LocalDateTime.parse(parts[1].trim(), FORMATTER);
            String customerName = parts[2].trim();
            int medId = Integer.parseInt(parts[3].trim());
            String medName = parts[4].trim();
            int qty = Integer.parseInt(parts[5].trim());
            double price = Double.parseDouble(parts[6].trim());
            return new Bill(billId, dt, customerName, medId, medName, qty, price);
        } catch (Exception e) {
            return null;
        }
    }
}
