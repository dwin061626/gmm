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

    // Fixed monthly prices
    public static final int REGULAR_PRICE = 700;
    public static final int PREMIUM_PRICE = 1500;

    // Shared Scanner and UserDao passed in from Main
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
            System.out.println();
            System.out.println("+----------------------------------------------------------+");
            System.out.println("|                   FRONT DESK DASHBOARD                   |");
            System.out.println("+----------------------------------------------------------+");
            System.out.printf("| %-56s |%n", "Welcome, " + frontDesk.name());
            System.out.println("+----------------------------------------------------------+");
            System.out.println("| 1. View Members                                          |");
            System.out.println("| 2. Process Payment / Activate Member                     |");
            System.out.println("| 3. View Payment History                                  |");
            System.out.println("| 4. Income Summary                                        |");
            System.out.println("| 5. Logout                                                |");
            System.out.println("+----------------------------------------------------------+");
            System.out.print("Select: ");

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
                    System.out.println("->Invalid option. Please choose 1-5.");
            }
        }
    }



    private void printMemberTable(List<User> users) {
        System.out.println("+-------+-----------------+--------------+------------+");
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("+-------+-----------------+--------------+------------+");

        for (User u : users) {
            System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n",
                    u.u_id(), u.name(), u.roles(), u.status());
        }

        System.out.println("+-------+-----------------+--------------+------------+");
    }

    private void viewMembers() {
        System.out.println("\n========================MEMBER LIST=========================");

        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("->No members found.");
        } else {
            printMemberTable(users);
        }
    }



    private void processPayment() {
        viewMembers();

        // 1. Pick the member
        System.out.print("\nEnter Member ID (or 0 to cancel): ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("->Invalid input. Please enter a number.");
            return;
        }

        if (id == 0) {
            System.out.println("->Payment cancelled.");
            return;
        }

        User member = userDao.getUserById(id);
        if (member == null) {
            System.out.println("->Member not found.");
            return;
        }
        if (!member.roles().equalsIgnoreCase("CUSTOMER")) {
            System.out.println("->This account is not a customer.");
            return;
        }

        // 2. Pick the plan
        System.out.println("\n========================SELECT PLAN=========================");
        System.out.println("1. Regular - " + REGULAR_PRICE);
        System.out.println("2. Premium - " + PREMIUM_PRICE);
        System.out.print("Select: ");
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
            System.out.println("->Invalid plan.");
            return;
        }

        // 3. Take the payment
        System.out.print("Enter Payment Amount: ");
        double paid;
        try {
            paid = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("->Invalid amount. Payment cancelled.");
            return;
        }

        if (paid < price) {
            System.out.println("->Insufficient payment.");
            return;
        }

        // 4. Save the payment and approve the member
        Payment p = paymentDao.processPayment(member, plan, price, paid);
        if (p == null) {
            System.out.println("->Payment failed.");
            return;
        }

        // 5. Receipt
        System.out.println("\n======================PAYMENT RECEIPT=======================");
        System.out.println("Name        : " + member.name());
        System.out.println("Plan        : " + p.plan());
        System.out.printf("Fee         : %.2f%n", p.fee());
        System.out.printf("Paid        : %.2f%n", p.paid());
        System.out.printf("Change      : %.2f%n", p.paid() - p.fee());
        System.out.println("Valid Until : " + p.expDate());
        System.out.println("Status      : APPROVED");
        System.out.println("->PAYMENT PROCESSED SUCCESSFULLY!");
    }



    private void viewPayments() {
        System.out.println("\n======================PAYMENT HISTORY=======================");

        List<Payment> payments = paymentDao.getAllPayments();
        if (payments.isEmpty()) {
            System.out.println("->No payment records found.");
            return;
        }

        System.out.println("+-------+-------+-----------------+----------+-----------+------------+------------+");
        System.out.printf("| %-5s | %-5s | %-15s | %-8s | %-9s | %-10s | %-10s |%n",
                "SUB", "UID", "NAME", "PLAN", "PAID", "DATE", "EXPIRY");
        System.out.println("+-------+-------+-----------------+----------+-----------+------------+------------+");

        for (Payment p : payments) {
            System.out.printf("| %-5s | %-5s | %-15s | %-8s | %-9s | %-10s | %-10s |%n",
                    p.subId(), p.uId(), p.name(), p.plan(),
                    String.format("%.2f", p.paid()), p.payDate(), p.expDate());
        }

        System.out.println("+-------+-------+-----------------+----------+-----------+------------+------------+");
    }



    private void incomeSummary() {
        String today = LocalDate.now().toString();
        String month = today.substring(0, 7);

        Summary day = paymentDao.getSummaryForDate(today);
        Summary mon = paymentDao.getSummaryForMonth(month);

        System.out.println("\n=======================INCOME SUMMARY=======================");
        System.out.println("+----------------+----------+--------------+");
        System.out.printf("| %-14s | %-8s | %-12s |%n", "PERIOD", "PAYMENTS", "INCOME");
        System.out.println("+----------------+----------+--------------+");
        System.out.printf("| %-14s | %-8s | %-12s |%n", "Today", day.count(), String.format("%.2f", day.total()));
        System.out.printf("| %-14s | %-8s | %-12s |%n", "This Month", mon.count(), String.format("%.2f", mon.total()));
        System.out.println("+----------------+----------+--------------+");
    }
}