package com.scc;

import com.scc.dao.DatabaseConnection;
import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.Admin;
import com.scc.ui.Customer;
import com.scc.ui.FrontDesk;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {
        // Shows which database file is used and makes sure the users table exists
        DatabaseConnection.initialize();

        boolean running = true;

        while (running) {
            System.out.println("\n===========================================");
            System.out.println(" GYM MEMBERSHIP MANAGEMENT ");
            System.out.println("===========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2 or 3.");
                    break;
            }
        }
    }

    //////////////////////////////////////////////////////////////
    // LOGIN
    //////////////////////////////////////////////////////////////

    private static void login() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        User user = userDao.login(email, password);

        if (user == null) {
            System.out.println("\nInvalid email or password.");
            return;
        }

        String role = user.roles().toUpperCase();

        // Only ACTIVE accounts may log in. New accounts start as PENDING.
        if (!user.status().equalsIgnoreCase("ACTIVE")) {
            System.out.println("\nYour account is PENDING. Please wait for approval before logging in.");
            return;
        }

        // Send each role to its own dashboard
        switch (role) {
            case "ADMIN":
                new Admin(sc).showMenu(user);
                break;
            case "FRONT DESK":
                new FrontDesk(sc).showMenu(user);
                break;
            case "CUSTOMER":
                new Customer(sc).showMenu(user);
                break;
            default:
                System.out.println("\nUnknown role: " + user.roles());
                break;
        }
    }

    //////////////////////////////////////////////////////////////
    // REGISTER
    //////////////////////////////////////////////////////////////

    private static void register() {
        System.out.println("\n=== REGISTER ===");
        System.out.print("Enter the new username: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter the new email: ");
        String email = sc.nextLine().trim();
        System.out.print("Enter the password: ");
        String password = sc.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("Name, email and password are required.");
            return;
        }

        if (userDao.emailExists(email, -1)) {
            System.out.println("That email is already registered.");
            return;
        }

        System.out.print("Select Role [1] FRONT DESK | [2] ADMIN | [3] CUSTOMER: ");
        String roles;
        switch (sc.nextLine().trim()) {
            case "1":
                roles = "FRONT DESK";
                break;
            case "2":
                roles = "ADMIN";
                break;
            case "3":
                roles = "CUSTOMER";
                break;
            default:
                roles = null;
                break;
        }

        if (roles == null) {
            System.out.println("Invalid role selection");
            return;
        }

        if (userDao.registerUser(name, email, password, roles)) {
            // If there is no active admin yet, the first admin approves themselves
            // so that someone can log in and approve everyone else.
            if (roles.equals("ADMIN") && userDao.activateIfFirstAdmin(email)) {
                System.out.println("You are the first admin, so your account is already ACTIVE. You can log in now.");
            }
        }
    }
}