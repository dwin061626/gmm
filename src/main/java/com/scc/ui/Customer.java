package com.scc.ui;

import com.scc.dao.PaymentDao;
import com.scc.dao.PaymentDao.Payment;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Customer {

    private final Scanner scanner;
    private final UserDao userDao;
    private final PaymentDao paymentDao = new PaymentDao();

    public Customer(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void display(User customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=========================================");
            System.out.println("           CUSTOMER DASHBOARD");
            System.out.println("=========================================");
            System.out.println("Welcome, " + customer.name());
            System.out.println("1. View Subscription Plans");
            System.out.println("2. Apply Subscription");
            System.out.println("3. Logout");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n1. Regular - " + FrontDesk.REGULAR_PRICE + " per month");
                    System.out.println("2. Premium - " + FrontDesk.PREMIUM_PRICE + " per month");
                    break;
                case "2":
                    applySubscription(customer);
                    break;
                case "3":
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-3.");
            }
        }
    }

    private void applySubscription(User customer) {
        System.out.println("\n1. Regular - " + FrontDesk.REGULAR_PRICE);
        System.out.println("2. Premium - " + FrontDesk.PREMIUM_PRICE);
        System.out.print("Choose plan: ");
        String planChoice = scanner.nextLine().trim();

        String plan;
        double price;
        if (planChoice.equals("1")) {
            plan = "Regular";
            price = FrontDesk.REGULAR_PRICE;
        } else if (planChoice.equals("2")) {
            plan = "Premium";
            price = FrontDesk.PREMIUM_PRICE;
        } else {
            System.out.println("Invalid plan.");
            return;
        }

        System.out.print("Enter payment amount: ");
        double paid = Double.parseDouble(scanner.nextLine().trim());

        if (paid < price) {
            System.out.println("Insufficient payment.");
            return;
        }

        Payment p = paymentDao.processPayment(customer, plan, price, paid);
        if (p == null) {
            System.out.println("Payment failed.");
            return;
        }

        System.out.println("\n----- RECEIPT -----");
        System.out.println("Plan        : " + p.plan());
        System.out.println("Fee         : " + p.fee());
        System.out.println("Paid        : " + p.paid());
        System.out.println("Change      : " + (p.paid() - p.fee()));
        System.out.println("Valid Until : " + p.expDate());
    }
}