package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Admin {

    private final Scanner scanner;
    private final UserDao userDao;

    public Admin(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=========================================");
            System.out.println("            ADMIN DASHBOARD");
            System.out.println("=========================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewPendingUsers();
                    break;
                case "3":
                    approveUser();
                    break;
                case "4":
                    addUser();
                    break;
                case "5":
                    updateUser();
                    break;
                case "6":
                    deleteUser(admin);
                    break;
                case "7":
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-7.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL USERS ---");
        for (User u : userDao.getAllUsers()) {
            System.out.println(u);
        }
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING USERS ---");
        boolean found = false;
        for (User u : userDao.getAllUsers()) {
            if (u.status().equalsIgnoreCase("PENDING")) {
                System.out.println(u);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No pending users.");
        }
    }

    private void approveUser() {
        viewPendingUsers();
        System.out.print("\nEnter User ID to approve: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        User u = userDao.getUserById(id);
        if (u == null) {
            System.out.println("User not found.");
            return;
        }

        User approved = new User(u.u_id(), u.name(), u.email(), u.password(), u.roles(), "ACTIVE");
        if (userDao.updateUser(approved)) {
            System.out.println(u.name() + " is now ACTIVE.");
        } else {
            System.out.println("Approval failed.");
        }
    }

    private void addUser() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Role [1] FRONT DESK | [2] ADMIN | [3] CUSTOMER: ");
        String roleChoice = scanner.nextLine().trim();

        String role;
        if (roleChoice.equals("1")) {
            role = "FRONT DESK";
        } else if (roleChoice.equals("2")) {
            role = "ADMIN";
        } else if (roleChoice.equals("3")) {
            role = "CUSTOMER";
        } else {
            System.out.println("Invalid role.");
            return;
        }

        if (userDao.emailExists(email, -1)) {
            System.out.println("Email already exists.");
            return;
        }

        if (userDao.createUser(name, email, password, role, "ACTIVE")) {
            System.out.println("User added.");
        } else {
            System.out.println("Failed to add user.");
        }
    }

    private void updateUser() {
        viewAllUsers();
        System.out.print("\nEnter User ID to update: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        User u = userDao.getUserById(id);
        if (u == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("(Press Enter to keep the current value)");

        System.out.print("New Name [" + u.name() + "]: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = u.name();
        }

        System.out.print("New Email [" + u.email() + "]: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            email = u.email();
        }
        if (userDao.emailExists(email, id)) {
            System.out.println("Email already used by another user.");
            return;
        }

        System.out.print("New Password [unchanged]: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            password = u.password();
        }

        System.out.print("New Role [1] FRONT DESK | [2] ADMIN | [3] CUSTOMER [" + u.roles() + "]: ");
        String roleChoice = scanner.nextLine().trim();
        String role = u.roles();
        if (roleChoice.equals("1")) {
            role = "FRONT DESK";
        } else if (roleChoice.equals("2")) {
            role = "ADMIN";
        } else if (roleChoice.equals("3")) {
            role = "CUSTOMER";
        } else if (!roleChoice.isEmpty()) {
            System.out.println("Invalid role.");
            return;
        }

        System.out.print("New Status [1] ACTIVE | [2] PENDING [" + u.status() + "]: ");
        String statusChoice = scanner.nextLine().trim();
        String status = u.status();
        if (statusChoice.equals("1")) {
            status = "ACTIVE";
        } else if (statusChoice.equals("2")) {
            status = "PENDING";
        } else if (!statusChoice.isEmpty()) {
            System.out.println("Invalid status.");
            return;
        }

        User updated = new User(u.u_id(), name, email, password, role, status);
        if (userDao.updateUser(updated)) {
            System.out.println("User updated.");
        } else {
            System.out.println("Update failed.");
        }
    }

    private void deleteUser(User admin) {
        viewAllUsers();
        System.out.print("\nEnter User ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        if (id == admin.u_id()) {
            System.out.println("You cannot delete your own account.");
            return;
        }

        if (userDao.deleteUser(id)) {
            System.out.println("User deleted.");
        } else {
            System.out.println("User not found or delete failed.");
        }
    }
}