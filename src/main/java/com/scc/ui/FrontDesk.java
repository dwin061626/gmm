package com.scc.ui;

import com.scc.dao.PaymentDao;
import com.scc.dao.PaymentDao.Payment;
import com.scc.dao.PaymentDao.Summary;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.time.LocalDate;
import java.util.Scanner;

public class FrontDesk {

    public static final int REGULAR_PRICE = 700;
    public static final int PREMIUM_PRICE = 1500;

    private final Scanner scanner;
    private final UserDao userDao;
    private final PaymentDao paymentDao = new PaymentDao();

    public FrontDesk(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void display(User frontDesk) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=========================================");
            System.out.println("          FRONT DESK DASHBOARD");
            System.out.println("=========================================");
            System.out.println("Welcome, " + frontDesk.name());
            System.out.println("1. View Members");
            System.out.println("2. Process Payment / Activate Member");
            System.out.println("3. View Payment History");
            System.out.println("4. Income Summary");
            System.out.println("5. Logout");
            System.out.print("Choose an option (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewMembers();
                    break;
                case "2":
                    processPayment();
                    break;
                case "3":
                    viewPayments();
                    break;
                case "4":
                    incomeSummary();
                    break;
                case "5":
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-5.");
            }
        }
    }

    private void viewMembers() {
        System.out.println("\n--- MEMBERS ---");
        for (User u : userDao.getAllUsers()) {
            System.out.println(u);
        }
    }

    private void processPayment() {
        viewMembers();
        System.out.print("\nEnter Member ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        User member = userDao.getUserById(id);
        if (member == null) {
            System.out.println("Member not found.");
            return;
        }
        if (!member.roles().equalsIgnoreCase("CUSTOMER")) {
            System.out.println("This account is not a customer.");
            return;
        }

        System.out.println("1. Regular - " + REGULAR_PRICE);
        System.out.println("2. Premium - " + PREMIUM_PRICE);
        System.out.print("Choose plan: ");
        String planChoice = scanner.nextLine().trim();

        String plan;
        double price;
        if (planChoice.equals("1")) {
            plan = "Regular";
            price = REGULAR_PRICE;
        } else if (planChoice.equals("2")) {
            plan = "Premium";
            price = PREMIUM_PRICE;
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

        Payment p = paymentDao.processPayment(member, plan, price, paid);
        if (p == null) {
            System.out.println("Payment failed.");
            return;
        }

        System.out.println("\n----- RECEIPT -----");
        System.out.println("Name        : " + member.name());
        System.out.println("Plan        : " + p.plan());
        System.out.println("Fee         : " + p.fee());
        System.out.println("Paid        : " + p.paid());
        System.out.println("Change      : " + (p.paid() - p.fee()));
        System.out.println("Valid Until : " + p.expDate());
        System.out.println("Status      : ACTIVE");
    }

    private void viewPayments() {
        System.out.println("\n--- PAYMENT HISTORY ---");
        for (Payment p : paymentDao.getAllPayments()) {
            System.out.println(p.subId() + " | " + p.name() + " | " + p.plan()
                    + " | " + p.paid() + " | " + p.payDate() + " | " + p.expDate());
        }
    }

    private void incomeSummary() {
        String today = LocalDate.now().toString();
        String month = today.substring(0, 7);

        Summary day = paymentDao.getSummaryForDate(today);
        Summary mon = paymentDao.getSummaryForMonth(month);

        System.out.println("\n--- INCOME SUMMARY ---");
        System.out.println("Today      : " + day.count() + " payments, " + day.total());
        System.out.println("This Month : " + mon.count() + " payments, " + mon.total());
    }
}