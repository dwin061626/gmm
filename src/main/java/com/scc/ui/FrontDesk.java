package com.scc.ui;

import com.scc.dao.PaymentDao;
import com.scc.dao.PaymentDao.Payment;
import com.scc.dao.PaymentDao.Summary;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class FrontDesk {

    public static final int REGULAR_PRICE = 700;
    public static final int PREMIUM_PRICE = 1500;

    private final Scanner sc;
    private final UserDao userDao = new UserDao();
    private final PaymentDao paymentDao = new PaymentDao();

    // Share the Scanner from Main so two Scanners don't fight over System.in
    public FrontDesk(Scanner sc) {
        this.sc = sc;
    }

    //////////////////////////////////////////////////////////////
    // HELPERS
    //////////////////////////////////////////////////////////////

    // Safe integer input (returns -1 on invalid input)
    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {
        }
    }

    private void pause() {
        System.out.print("\nPress Enter to continue . . . ");
        sc.nextLine();
    }

    private void printMembers() {
        System.out.printf("| %-5s | %-20s | %-25s | %-12s | %-8s |%n",
                "ID", "NAME", "EMAIL", "ROLE", "STATUS");
        System.out.println("|-------|----------------------|---------------------------|"
                + "--------------|----------|");

        for (User u : userDao.getAllUsers()) {
            System.out.printf("| %-5d | %-20s | %-25s | %-12s | %-8s |%n",
                    u.u_id(), u.name(), u.email(), u.roles(), u.status());
        }
    }

    //////////////////////////////////////////////////////////////
    // PROCESS PAYMENT / ACTIVATE MEMBERSHIP
    //////////////////////////////////////////////////////////////

    private void processPayment() {
        clearScreen();
        System.out.println("=== PROCESS PAYMENT / ACTIVATE MEMBERSHIP ===\n");
        printMembers();

        System.out.print("\nEnter Member ID: ");
        int id = readInt();

        User target = userDao.getUserById(id);
        if (target == null) {
            System.out.printf("%nMember ID %d not found.%n", id);
            return;
        }

        // Front desk can only handle customers, not Admin/Front Desk accounts
        if (!"CUSTOMER".equalsIgnoreCase(target.roles())) {
            System.out.println("\nThis account is not a member. Action not allowed.");
            return;
        }

        System.out.println("\nMember : " + target.name());
        System.out.println("Current Status : " + target.status());

        System.out.println("\nSelect Plan:");
        System.out.println("1. Regular  (" + REGULAR_PRICE + ")");
        System.out.println("2. Premium  (" + PREMIUM_PRICE + ")");
        System.out.print("Choice: ");
        int planChoice = readInt();

        String plan;
        double price;
        if (planChoice == 1) {
            plan = "Regular";
            price = REGULAR_PRICE;
        } else if (planChoice == 2) {
            plan = "Premium";
            price = PREMIUM_PRICE;
        } else {
            System.out.println("\nInvalid plan! Transaction cancelled.");
            return;
        }

        System.out.printf("%nSubscription Fee: %.2f%n", price);
        System.out.print("Enter Payment Amount: ");
        double payment;
        try {
            payment = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid amount! Transaction cancelled.");
            return;
        }

        if (payment < price) {
            System.out.println("\nInsufficient Payment! Transaction Cancelled.");
            return;
        }

        // Save payment + activate member in one transaction
        Payment p = paymentDao.processPayment(target, plan, price, payment);
        if (p == null) {
            System.out.println("\nTransaction failed. Nothing was saved.");
            return;
        }

        System.out.println("\n\n=========== PAYMENT RECEIPT ===========\n");
        System.out.println("Name            : " + target.name());
        System.out.println("Email           : " + target.email());
        System.out.println("Subscription    : " + p.plan());
        System.out.printf("Monthly Fee     : %.2f%n", p.fee());
        System.out.printf("Amount Paid     : %.2f%n", p.paid());
        System.out.printf("Change          : %.2f%n", p.paid() - p.fee());
        System.out.println("Payment Date    : " + p.payDate());
        System.out.println("Valid Until     : " + p.expDate());
        System.out.println("----------------------------------------");
        System.out.println("Status          : ACTIVE");
        System.out.println("========================================");
    }

    //////////////////////////////////////////////////////////////
    // VIEW PAYMENT HISTORY
    //////////////////////////////////////////////////////////////

    private void viewPayments() {
        clearScreen();
        System.out.println("=== PAYMENT HISTORY ===");

        List<Payment> payments = paymentDao.getAllPayments();
        if (payments.isEmpty()) {
            System.out.println("\nNo payment records found.");
            return;
        }

        System.out.printf("%n| %-5s | %-5s | %-20s | %-9s | %-9s | %-10s | %-10s |%n",
                "SUB", "UID", "NAME", "PLAN", "PAID", "DATE", "EXPIRY");
        System.out.println("|-------|-------|----------------------|-----------|-----------|"
                + "------------|------------|");

        for (Payment p : payments) {
            System.out.printf("| %-5d | %-5d | %-20s | %-9s | %-9.2f | %-10s | %-10s |%n",
                    p.subId(), p.uId(), p.name(), p.plan(), p.paid(), p.payDate(), p.expDate());
        }
    }

    //////////////////////////////////////////////////////////////
    // INCOME SUMMARY (TODAY + THIS MONTH)
    //////////////////////////////////////////////////////////////

    private void incomeSummary() {
        clearScreen();
        System.out.println("=== INCOME SUMMARY ===");

        String today = LocalDate.now().toString();
        String month = today.substring(0, 7);

        Summary day = paymentDao.getSummaryForDate(today);
        Summary mon = paymentDao.getSummaryForMonth(month);

        System.out.println("\nDate  : " + today);
        System.out.println("--------------------------------------");
        System.out.println("Today's Payments     : " + day.count());
        System.out.printf("Today's Income       : %.2f%n", day.total());
        System.out.println("--------------------------------------");
        System.out.println("This Month's Payments: " + mon.count());
        System.out.printf("This Month's Income  : %.2f%n", mon.total());
    }

    //////////////////////////////////////////////////////////////
    // FRONT DESK DASHBOARD
    //////////////////////////////////////////////////////////////

    public void showMenu(User loggedInUser) {
        while (true) {
            clearScreen();
            System.out.println("=================================");
            System.out.println("        FRONT DESK DASHBOARD");
            System.out.println("=================================");
            System.out.println("Welcome, " + loggedInUser.name() + "\n");

            System.out.println("1. View Members");
            System.out.println("2. Process Payment / Activate Member");
            System.out.println("3. View Payment History");
            System.out.println("4. Income Summary");
            System.out.println("5. Logout");
            System.out.print("\nChoice: ");
            int choice = readInt();

            switch (choice) {
                case 1 -> {
                    clearScreen();
                    System.out.println("=== MEMBER LIST ===\n");
                    printMembers();
                    pause();
                }
                case 2 -> {
                    processPayment();
                    pause();
                }
                case 3 -> {
                    viewPayments();
                    pause();
                }
                case 4 -> {
                    incomeSummary();
                    pause();
                }
                case 5 -> {
                    return;
                }
                default -> {
                    System.out.println("\nInvalid choice!");
                    pause();
                }
            }
        }
    }
}