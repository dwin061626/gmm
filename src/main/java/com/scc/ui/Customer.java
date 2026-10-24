package com.scc.ui;

import com.scc.dao.PaymentDao;
import com.scc.dao.PaymentDao.Payment;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Customer {

    // Shared Scanner and UserDao passed in from Main
    private final Scanner scanner;
    private final UserDao userDao;
    private final PaymentDao paymentDao = new PaymentDao();

    public Customer(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    //////////////////////////////////////////////////////////////
    // CUSTOMER DASHBOARD
    //////////////////////////////////////////////////////////////

    public void display(User customer) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println("+----------------------------------------------------------+");
            System.out.println("|                    CUSTOMER DASHBOARD                    |");
            System.out.println("+----------------------------------------------------------+");
            System.out.printf("| %-56s |%n", "Welcome, " + customer.name());
            System.out.println("+----------------------------------------------------------+");
            System.out.println("| 1. View Subscription Plans                               |");
            System.out.println("| 2. Apply Subscription                                    |");
            System.out.println("| 3. Logout                                                |");
            System.out.println("+----------------------------------------------------------+");
            System.out.print("Select: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewPlans();
                    break;
                case "2":
                    applySubscription(customer);
                    break;
                case "3":
                    loggedIn = false;
                    break;
                default:
                    System.out.println("->Invalid option. Please choose 1-3.");
            }
        }
    }

    //////////////////////////////////////////////////////////////
    // VIEW PLANS
    //////////////////////////////////////////////////////////////

    private void viewPlans() {
        System.out.println("\n=====================SUBSCRIPTION PLANS=====================");
        System.out.println("+-----+------------+-----------------+");
        System.out.printf("| %-3s | %-10s | %-15s |%n", "NO", "PLAN", "PRICE / MONTH");
        System.out.println("+-----+------------+-----------------+");
        System.out.printf("| %-3s | %-10s | %-15s |%n", "1", "Regular", FrontDesk.REGULAR_PRICE);
        System.out.printf("| %-3s | %-10s | %-15s |%n", "2", "Premium", FrontDesk.PREMIUM_PRICE);
        System.out.println("+-----+------------+-----------------+");
    }

    //////////////////////////////////////////////////////////////
    // APPLY SUBSCRIPTION
    //////////////////////////////////////////////////////////////

    private void applySubscription(User customer) {
        System.out.println("\n=====================APPLY SUBSCRIPTION=====================");

        // 1. Pick the plan
        System.out.println("1. Regular - " + FrontDesk.REGULAR_PRICE);
        System.out.println("2. Premium - " + FrontDesk.PREMIUM_PRICE);
        System.out.print("Select: ");
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
            System.out.println("->Invalid plan.");
            return;
        }

        // 2. Take the payment
        System.out.print("Enter Payment Amount: ");
        double paid = Double.parseDouble(scanner.nextLine().trim());

        if (paid < price) {
            System.out.println("->Insufficient payment.");
            return;
        }

        // 3. Save the payment
        Payment p = paymentDao.processPayment(customer, plan, price, paid);
        if (p == null) {
            System.out.println("->Payment failed.");
            return;
        }

        // 4. Receipt
        System.out.println("\n======================PAYMENT RECEIPT=======================");
        System.out.println("Plan        : " + p.plan());
        System.out.printf("Fee         : %.2f%n", p.fee());
        System.out.printf("Paid        : %.2f%n", p.paid());
        System.out.printf("Change      : %.2f%n", p.paid() - p.fee());
        System.out.println("Valid Until : " + p.expDate());
        System.out.println("->SUBSCRIPTION APPLIED SUCCESSFULLY!");
    }
}