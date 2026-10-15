package com.app;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * ExpiryChecker handles date comparisons using LocalDate,
 * generates alerts for expired and expiring-soon medicines,
 * and identifies low-stock items.
 */
public class ExpiryChecker {
    public static final int EXPIRY_THRESHOLD_DAYS = 30;
    public static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * Filters medicines whose expiry date is strictly before today.
     */
    public List<Medicine> getExpiredMedicines(List<Medicine> medicines) {
        List<Medicine> expired = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Medicine m : medicines) {
            if (m.getExpiryDate().isBefore(today)) {
                expired.add(m);
            }
        }
        return expired;
    }

    /**
     * Filters medicines expiring within the next 30 days (from today).
     */
    public List<Medicine> getExpiringSoonMedicines(List<Medicine> medicines, int daysThreshold) {
        List<Medicine> expiringSoon = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(daysThreshold);

        for (Medicine m : medicines) {
            LocalDate exp = m.getExpiryDate();
            // Not expired yet, but on or before the deadline
            if (!exp.isBefore(today) && !exp.isAfter(deadline)) {
                expiringSoon.add(m);
            }
        }
        return expiringSoon;
    }

    /**
     * Filters medicines valid beyond the 30-day window.
     */
    public List<Medicine> getValidMedicines(List<Medicine> medicines) {
        List<Medicine> valid = new ArrayList<>();
        LocalDate deadline = LocalDate.now().plusDays(EXPIRY_THRESHOLD_DAYS);
        for (Medicine m : medicines) {
            if (m.getExpiryDate().isAfter(deadline)) {
                valid.add(m);
            }
        }
        return valid;
    }

    /**
     * Filters medicines with quantity <= threshold.
     */
    public List<Medicine> getLowStockMedicines(List<Medicine> medicines, int threshold) {
        List<Medicine> lowStock = new ArrayList<>();
        for (Medicine m : medicines) {
            if (m.getQuantity() <= threshold) {
                lowStock.add(m);
            }
        }
        return lowStock;
    }

    /**
     * Prints an alert summary upon login or application startup.
     */
    public void printStartupAlertSummary(List<Medicine> medicines) {
        LocalDate today = LocalDate.now();
        List<Medicine> expired = getExpiredMedicines(medicines);
        List<Medicine> expiringSoon = getExpiringSoonMedicines(medicines, EXPIRY_THRESHOLD_DAYS);
        List<Medicine> lowStock = getLowStockMedicines(medicines, LOW_STOCK_THRESHOLD);

        System.out.println();
        System.out.println("================================================================");
        System.out.println("                   SYSTEM HEALTH & INVENTORY ALERTS             ");
        System.out.printf(" Current Date: %s | Total Inventory Records: %d%n", today, medicines.size());
        System.out.println("================================================================");

        boolean hasAlert = false;
        if (!expired.isEmpty()) {
            System.out.printf(" [!] ATTENTION: %d medicine(s) are EXPIRED! (Sale prohibited)%n", expired.size());
            hasAlert = true;
        }
        if (!expiringSoon.isEmpty()) {
            System.out.printf(" [*] WARNING  : %d medicine(s) EXPIRING SOON (within %d days)%n", expiringSoon.size(), EXPIRY_THRESHOLD_DAYS);
            hasAlert = true;
        }
        if (!lowStock.isEmpty()) {
            System.out.printf(" [?] RESTOCK  : %d medicine(s) have LOW STOCK (<= %d units)%n", lowStock.size(), LOW_STOCK_THRESHOLD);
            hasAlert = true;
        }

        if (!hasAlert) {
            System.out.println(" [OK] All medicines are within valid dates and adequate stock levels.");
        }
        System.out.println("================================================================");
        System.out.println();
    }

    /**
     * Prints a detailed report for expired medicines.
     */
    public void printExpiredReport(List<Medicine> medicines) {
        List<Medicine> expired = getExpiredMedicines(medicines);
        LocalDate today = LocalDate.now();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                         EXPIRED MEDICINES REPORT                               ");
        System.out.printf(" Today: %s | Total Expired: %d%n", today, expired.size());
        System.out.println("--------------------------------------------------------------------------------");

        if (expired.isEmpty()) {
            System.out.println(" Great news! No expired medicines found in inventory.");
        } else {
            System.out.printf("%-6s | %-24s | %-6s | %-10s | %-12s | %-15s%n",
                    "ID", "Medicine Name", "Qty", "Price(Rs)", "Expiry Date", "Days Expired");
            System.out.println("--------------------------------------------------------------------------------");
            for (Medicine m : expired) {
                long daysPast = ChronoUnit.DAYS.between(m.getExpiryDate(), today);
                System.out.printf("%-6d | %-24s | %-6d | Rs.%-7.2f | %-12s | %d day(s) ago%n",
                        m.getId(), m.getName(), m.getQuantity(), m.getPrice(), m.getExpiryDate(), daysPast);
            }
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Prints a detailed report for medicines expiring within the specified days.
     */
    public void printExpiringSoonReport(List<Medicine> medicines, int days) {
        List<Medicine> expiringSoon = getExpiringSoonMedicines(medicines, days);
        LocalDate today = LocalDate.now();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("               MEDICINES EXPIRING WITHIN NEXT %d DAYS                           %n", days);
        System.out.printf(" Today: %s | Total Expiring Soon: %d%n", today, expiringSoon.size());
        System.out.println("--------------------------------------------------------------------------------");

        if (expiringSoon.isEmpty()) {
            System.out.println(" No medicines are expiring within the next " + days + " days.");
        } else {
            System.out.printf("%-6s | %-24s | %-6s | %-10s | %-12s | %-15s%n",
                    "ID", "Medicine Name", "Qty", "Price(Rs)", "Expiry Date", "Time Left");
            System.out.println("--------------------------------------------------------------------------------");
            for (Medicine m : expiringSoon) {
                long daysLeft = ChronoUnit.DAYS.between(today, m.getExpiryDate());
                String timeLeftStr = (daysLeft == 0) ? "Expires TODAY!" : daysLeft + " day(s) left";
                System.out.printf("%-6d | %-24s | %-6d | Rs.%-7.2f | %-12s | %-15s%n",
                        m.getId(), m.getName(), m.getQuantity(), m.getPrice(), m.getExpiryDate(), timeLeftStr);
            }
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Prints a report of medicines with low stock.
     */
    public void printLowStockReport(List<Medicine> medicines, int threshold) {
        List<Medicine> lowStock = getLowStockMedicines(medicines, threshold);

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("                 LOW STOCK REPORT (<= %d UNITS)                                %n", threshold);
        System.out.printf(" Total Low-Stock Medicines: %d%n", lowStock.size());
        System.out.println("--------------------------------------------------------------------------------");

        if (lowStock.isEmpty()) {
            System.out.println(" All medicine stock levels are above " + threshold + " units.");
        } else {
            System.out.printf("%-6s | %-24s | %-10s | %-10s | %-12s | %-10s%n",
                    "ID", "Medicine Name", "Current Qty", "Price(Rs)", "Expiry Date", "Action");
            System.out.println("--------------------------------------------------------------------------------");
            for (Medicine m : lowStock) {
                String action = (m.getQuantity() == 0) ? "OUT OF STOCK" : "REORDER SOON";
                System.out.printf("%-6d | %-24s | %-10d | Rs.%-7.2f | %-12s | %-10s%n",
                        m.getId(), m.getName(), m.getQuantity(), m.getPrice(), m.getExpiryDate(), action);
            }
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }
}
