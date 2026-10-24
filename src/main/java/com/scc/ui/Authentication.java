package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {

    private final Scanner scanner;
    private final UserDao userDao;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n===========================================");
            System.out.println(" GYM MEMBERSHIP MANAGEMENT ");
            System.out.println("===========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

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
            }
        }
    }

    private void login() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.login(email, password);

        if (user == null) {
            System.out.println("\nInvalid email or password.");
            return;
        }

        if (!user.status().equalsIgnoreCase("APPROVED")) {
            System.out.println("\nYour account is PENDING. Please wait for approval before logging in.");
            return;
        }

        routeByRole(user);
    }

    // Sends each logged-in user to their role's menu, sharing the same Scanner and UserDao
    private void routeByRole(User user) {
        String role = user.roles().toUpperCase();

        if (role.equals("ADMIN")) {
            new Admin(scanner, userDao).display(user);
        } else if (role.equals("FRONT DESK")) {
            new FrontDesk(scanner, userDao).display(user);
        } else if (role.equals("CUSTOMER")) {
            new Customer(scanner, userDao).display(user);
        } else {
            System.out.println("Unknown role: " + user.roles());
        }
    }

    private void register() {
        System.out.println("\n=== REGISTER ===");
        System.out.print("Enter the new username: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter the new email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter the password: ");
        String password = scanner.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("Name, email and password are required.");
            return;
        }

        if (userDao.emailExists(email, -1)) {
            System.out.println("That email is already registered.");
            return;
        }

        System.out.print("Select Role [1] FRONT DESK | [2] ADMIN | [3] CUSTOMER: ");
        String roleChoice = scanner.nextLine().trim();

        String roles;
        if (roleChoice.equals("1")) {
            roles = "FRONT DESK";
        } else if (roleChoice.equals("2")) {
            roles = "ADMIN";
        } else if (roleChoice.equals("3")) {
            roles = "CUSTOMER";
        } else {
            System.out.println("Invalid role selection");
            return;
        }

        if (userDao.registerUser(name, email, password, roles)) {
            // The first admin approves themselves so someone can approve everyone else
            if (roles.equals("ADMIN") && userDao.activateIfFirstAdmin(email)) {
                System.out.println("You are the first admin, so your account is already APPROVED. You can log in now.");
            }
        }
    }
}