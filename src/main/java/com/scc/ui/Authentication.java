package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {

    // Shared Scanner and UserDao passed in from Main
    private final Scanner scanner;
    private final UserDao userDao;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }



    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=================GYM MEMBERSHIP MANAGEMENT==================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Select: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    System.out.println("->Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("->Invalid option. Please choose 1, 2 or 3.");
            }
        }
    }



    private void login() {
        System.out.println("\n===========================LOGIN============================");

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Check the email and password
        User user = userDao.login(email, password);
        if (user == null) {
            System.out.println("->Invalid email or password.");
            return;
        }

        // Only APPROVED accounts can log in
        if (!user.status().equalsIgnoreCase("APPROVED")) {
            System.out.println("->Your account is PENDING. Please wait for approval before logging in.");
            return;
        }

        routeByRole(user);
    }



    // Sends each logged-in user to their role's menu
    private void routeByRole(User user) {
        String role = user.roles().toUpperCase();

        if (role.equals("ADMIN")) {
            new Admin(scanner, userDao).display(user);
        } else if (role.equals("FRONT DESK")) {
            new FrontDesk(scanner, userDao).display(user);
        } else if (role.equals("CUSTOMER")) {
            new Customer(scanner, userDao).display(user);
        } else {
            System.out.println("->Unknown role: " + user.roles());
        }
    }



    private void register() {
        System.out.println("\n==========================REGISTER==========================");

        // Ask for the details
        System.out.print("Enter the new username: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter the new email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter the password: ");
        String password = scanner.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("->Name, email and password are required.");
            return;
        }

        // No duplicate emails
        if (userDao.emailExists(email, -1)) {
            System.out.println("->That email is already registered.");
            return;
        }

        // Pick the role (the admin is a fixed account, so it can't be registered)
        System.out.print("Select Role [1] FRONT DESK | [2] CUSTOMER: ");
        String roleChoice = scanner.nextLine().trim();

        String roles;
        if (roleChoice.equals("1")) {
            roles = "FRONT DESK";
        } else if (roleChoice.equals("2")) {
            roles = "CUSTOMER";
        } else {
            System.out.println("->Invalid role selection.");
            return;
        }

        // Save as PENDING (waits for admin approval)
        userDao.registerUser(name, email, password, roles);
    }
}