package com.scc.ui;

import com.scc.dao.PaymentDao;
import com.scc.dao.PaymentDao.Payment;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Customer {

    private final Scanner sc;
    private final UserDao userDao = new UserDao();
    private final PaymentDao paymentDao = new PaymentDao();

    // Share the Scanner from Main so two Scanners don't fight over System.in
    public Customer(Scanner sc) {
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

    //////////////////////////////////////////////////////////////
    // VIEW SUBSCRIPTION
    //////////////////////////////////////////////////////////////

    private void viewSubscription(User loggedInUser) {
        clearScreen();
        System.out.println("========== SUBSCRIPTION PLANS ==========\n");
        System.out.println("1. Regular  - " + FrontDesk.REGULAR_PRICE + " per month");
        System.out.println("2. Premium  - " + FrontDesk.PREMIUM_PRICE + " per month");
        System.out.println("\n----------------------------------------");
        System.out.println("Both plans are valid for 1 month only.");

        // Show the customer's current account status (fresh from the database)
        User fresh = userDao.getUserById(loggedInUser.u_id());
        if (fresh != null) {
            System.out.println("\nYour account status: " + fresh.status());
        }
    }

    //////////////////////////////////////////////////////////////
    // APPLY SUBSCRIPTION
    //////////////////////////////////////////////////////////////

    private void applySubscription(User loggedInUser) {
        clearScreen();
        System.out.println("========== APPLY SUBSCRIPTION ==========\n");

        // Re-read the user so we see the latest status
        User me = userDao.getUserById(loggedInUser.u_id());
        if (me == null) {
            System.out.println("Your account could not be found.");
            return;
        }

        System.out.println("Applying as: " + me.name() + " (" + me.email() + ")");

        System.out.println("\nChoose Subscription Plan:");
        System.out.println("1. Regular  (" + FrontDesk.REGULAR_PRICE + ")");
        System.out.println("2. Premium  (" + FrontDesk.PREMIUM_PRICE + ")");
        System.out.print("Choice: ");
        int subChoice = readInt();

        String plan;
        double price;
        if (subChoice == 1) {
            plan = "Regular";
            price = FrontDesk.REGULAR_PRICE;
        } else if (subChoice == 2) {
            plan = "Premium";
            price = FrontDesk.PREMIUM_PRICE;
        } else {
            System.out.println("\nInvalid choice! Default set to Regular.");
            plan = "Regular";
            price = FrontDesk.REGULAR_PRICE;
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

        // Save the payment (the account stays ACTIVE)
        Payment p = paymentDao.processPayment(me, plan, price, payment);
        if (p == null) {
            System.out.println("\nTransaction failed. Nothing was saved.");
            return;
        }

        clearScreen();
        System.out.println("=========== PAYMENT RECEIPT ===========\n");
        System.out.println("Name            : " + me.name());
        System.out.println("Email           : " + me.email());
        System.out.println("Subscription    : " + p.plan());
        System.out.printf("Monthly Fee     : %.2f%n", p.fee());
        System.out.printf("Amount Paid     : %.2f%n", p.paid());
        System.out.printf("Change          : %.2f%n", p.paid() - p.fee());
        System.out.println("Valid Until     : " + p.expDate());
        System.out.println("----------------------------------------");
        System.out.println("Status          : ACTIVE");
        System.out.println("========================================");
    }

    //////////////////////////////////////////////////////////////
    // CUSTOMER DASHBOARD
    //////////////////////////////////////////////////////////////

    public void showMenu(User loggedInUser) {
        while (true) {
            clearScreen();
            System.out.println("=================================");
            System.out.println("         USER DASHBOARD");
            System.out.println("=================================");
            System.out.println("Welcome, " + loggedInUser.name() + "\n");

            System.out.println("1. View Subscription");
            System.out.println("2. Apply Subscription");
            System.out.println("3. Logout");
            System.out.print("\nChoice: ");

            switch (readInt()) {
                case 1 -> {
                    viewSubscription(loggedInUser);
                    pause();
                }
                case 2 -> {
                    applySubscription(loggedInUser);
                    pause();
                }
                case 3 -> {
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