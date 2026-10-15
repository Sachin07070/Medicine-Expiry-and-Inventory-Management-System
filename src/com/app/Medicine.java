package com.app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Medicine class representing individual medicine records.
 * Fields: id, name, quantity, price, expiryDate.
 */
public class Medicine {
    private int id;
    private String name;
    private int quantity;
    private double price;
    private LocalDate expiryDate;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Default constructor
    public Medicine() {
    }

    // Parameterized constructor
    public Medicine(int id, String name, int quantity, double price, LocalDate expiryDate) {
        if (id <= 0) {
            throw new IllegalArgumentException("Medicine ID must be a positive integer.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be empty.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (price < 0.0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date cannot be null.");
        }

        this.id = id;
        this.name = name.trim();
        this.quantity = quantity;
        this.price = price;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters with validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Medicine ID must be a positive integer.");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be empty.");
        }
        this.name = name.trim();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0.0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date cannot be null.");
        }
        this.expiryDate = expiryDate;
    }

    // Status helper methods
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int daysThreshold) {
        LocalDate today = LocalDate.now();
        return !isExpired() && !expiryDate.isAfter(today.plusDays(daysThreshold));
    }

    public boolean isLowStock(int minThreshold) {
        return quantity <= minThreshold;
    }

    public String getStatusDescription() {
        if (isExpired()) {
            return "EXPIRED";
        } else if (isExpiringSoon(30)) {
            return "EXPIRING SOON";
        } else if (isLowStock(10)) {
            return "LOW STOCK";
        } else {
            return "VALID / IN STOCK";
        }
    }

    /**
     * Converts medicine object into a CSV string for file storage.
     * Format: id,name,quantity,price,expiryDate
     */
    public String toFileString() {
        return id + "," + name + "," + quantity + "," + String.format("%.2f", price) + "," + expiryDate.format(DATE_FORMATTER);
    }

    /**
     * Creates a Medicine object from a CSV line.
     */
    public static Medicine fromFileString(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }

        String[] parts = line.split(",");
        if (parts.length != 5) {
            System.err.println("Skipping malformed line in data file: " + line);
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            int quantity = Integer.parseInt(parts[2].trim());
            double price = Double.parseDouble(parts[3].trim());
            LocalDate expiryDate = LocalDate.parse(parts[4].trim(), DATE_FORMATTER);

            return new Medicine(id, name, quantity, price, expiryDate);
        } catch (Exception e) {
            System.err.println("Error parsing medicine line: '" + line + "'. Cause: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("Medicine[ID=%d, Name='%s', Qty=%d, Price=Rs.%.2f, Expiry=%s, Status=%s]",
                id, name, quantity, price, expiryDate.format(DATE_FORMATTER), getStatusDescription());
    }
}
