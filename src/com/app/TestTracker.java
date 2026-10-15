package com.app;

import com.app.*;
import java.time.LocalDate;
import java.util.List;

public class TestTracker {
    public static void main(String[] args) {
        System.out.println("--- Starting Automated Verification Tests ---");

        FileHandler fh = new FileHandler();
        Inventory inv = new Inventory(fh);
        ExpiryChecker checker = new ExpiryChecker();

        List<Medicine> all = inv.getMedicines();
        System.out.println("Total loaded medicines: " + all.size());
        assert all.size() >= 8 : "Expected at least 8 starter medicines";

        // Test 1: Search by ID
        Medicine para = inv.searchById(101);
        assert para != null : "Medicine 101 Paracetamol should exist";
        System.out.println("Test 1 Passed: Search by ID -> " + para.getName());

        // Test 2: Search by Name
        List<Medicine> found = inv.searchByName("paracetamol");
        assert !found.isEmpty() : "Search for 'paracetamol' should return results";
        System.out.println("Test 2 Passed: Search by Name -> found " + found.size() + " matches");

        // Test 3: Expiry Check
        List<Medicine> expired = checker.getExpiredMedicines(all);
        System.out.println("Expired medicines count: " + expired.size());
        assert !expired.isEmpty() : "Should have expired medicines in sample";
        System.out.println("Test 3 Passed: Expiry checker identified expired medicines");

        // Test 4: Expiring Soon
        List<Medicine> expiringSoon = checker.getExpiringSoonMedicines(all, 30);
        System.out.println("Expiring soon count (<= 30 days): " + expiringSoon.size());
        assert !expiringSoon.isEmpty() : "Should have expiring soon medicines";
        System.out.println("Test 4 Passed: Expiring soon identified");

        // Test 5: Low stock
        List<Medicine> lowStock = checker.getLowStockMedicines(all, 10);
        System.out.println("Low stock count (<= 10 units): " + lowStock.size());
        assert !lowStock.isEmpty() : "Should have low stock medicines";
        System.out.println("Test 5 Passed: Low stock identified");

        // Test 6: Selling Expired Medicine should be blocked
        Medicine expMed = inv.searchById(104); // Cough syrup expired on 2026-09-30
        assert expMed.isExpired() : "Medicine 104 should be expired";
        Bill failedBill = inv.sellMedicine(104, 1, "Test Customer");
        assert failedBill == null : "Selling expired medicine must return null and be blocked";
        System.out.println("Test 6 Passed: Expired medicine sale was successfully blocked!");

        // Test 7: Selling Valid Medicine
        Medicine validMed = inv.searchById(102); // Amoxicillin: Qty 25, Price 12.50
        int initialQty = validMed.getQuantity();
        Bill bill = inv.sellMedicine(102, 5, "Sachin");
        assert bill != null : "Bill should be generated";
        assert bill.getTotalAmount() == 5 * 12.50 : "Total should be 62.50";
        assert validMed.getQuantity() == initialQty - 5 : "Quantity should be deducted";
        System.out.println("Test 7 Passed: Valid sale generated bill with total Rs." + bill.getTotalAmount());
        bill.printReceipt();

        // Test 8: Reloading from file
        Inventory reloadedInv = new Inventory(new FileHandler());
        Medicine reloadedMed = reloadedInv.searchById(102);
        assert reloadedMed.getQuantity() == initialQty - 5 : "Persisted quantity should match after reload";
        System.out.println("Test 8 Passed: Inventory changes persisted and reloaded from medicines.txt successfully!");

        System.out.println("\n>>> ALL TESTS PASSED SUCCESSFULLY! <<<");
    }
}
