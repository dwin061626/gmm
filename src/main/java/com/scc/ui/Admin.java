package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class Admin {

    // Shared Scanner and UserDao passed in from Main
    private final Scanner scanner;
    private final UserDao userDao;

    public Admin(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    //////////////////////////////////////////////////////////////
    // ADMIN DASHBOARD
    //////////////////////////////////////////////////////////////

    public void display(User admin) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println("+----------------------------------------------------------+");
            System.out.println("|                     ADMIN DASHBOARD                      |");
            System.out.println("+----------------------------------------------------------+");
            System.out.printf("| %-56s |%n", "Welcome, " + admin.name());
            System.out.println("+----------------------------------------------------------+");
            System.out.println("| 1. View All Users                                        |");
            System.out.println("| 2. View Pending User Approvals                           |");
            System.out.println("| 3. Approve User Registration                             |");
            System.out.println("| 4. Add New User                                          |");
            System.out.println("| 5. Update User Role / Status                             |");
            System.out.println("| 6. Delete User Account                                   |");
            System.out.println("| 7. Logout                                                |");
            System.out.println("+----------------------------------------------------------+");
            System.out.print("Select: ");

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
                    updateUser(admin);
                    break;
                case "6":
                    deleteUser(admin);
                    break;
                case "7":
                    loggedIn = false;
                    break;
                default:
                    System.out.println("->Invalid option. Please choose 1-7.");
            }
        }
    }


    // Formatted table for any list of users, with a title above it
    private void displayUserTable(List<User> users, String title) {
        int left = (60 - title.length()) / 2;
        int right = 60 - title.length() - left;
        System.out.println("\n" + "=".repeat(left) + title + "=".repeat(right));

        if (users.isEmpty()) {
            System.out.println(" -> No records found.");
            return;
        }

        System.out.println("+-------+-----------------+--------------+------------+");
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("+-------+-----------------+--------------+------------+");

        for (User u : users) {
            System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n",
                    u.u_id(), u.name(), u.roles(), u.status());
        }

        System.out.println("+-------+-----------------+--------------+------------+");
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        displayUserTable(users, "ALL SYSTEM USERS");
    }

    // Shows the pending table and returns the pending list
    private List<User> viewPendingUsers() {
        List<User> pending = userDao.getPendingUsers();
        displayUserTable(pending, "PENDING APPROVALS");
        return pending;
    }



    private void approveUser() {
        List<User> pending = viewPendingUsers();
        if (pending.isEmpty()) {
            return;
        }

        while (true) {
            int id = readIntInput("\nEnter User ID to Approve (or 0 to cancel): ");

            // 0 cancels
            if (id == 0) {
                System.out.println("->Approval cancelled.");
                return;
            }

            // The ID must be in the pending list
            boolean inPending = false;
            for (User u : pending) {
                if (u.u_id() == id) {
                    inPending = true;
                }
            }
            if (!inPending) {
                System.out.println("->User ID " + id + " is not in the pending list. Try again or enter 0 to cancel.");
                continue;
            }

            // Approve
            if (userDao.approveUser(id)) {
                System.out.println("->USER ID: " + id + " APPROVED SUCCESSFULLY!");
            } else {
                System.out.println("->Approval failed.");
            }
            return;
        }
    }



    private void updateUser(User currentAdmin) {
        // Display all users in a table
        List<User> users = userDao.getAllUsers();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) {
            return;
        }

        // Pick the user (0 cancels)
        int userId = readIntInput("\nEnter selected ID to update (or 0 to cancel): ");

        if (userId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        // Self-edit restriction
        if (userId == currentAdmin.u_id()) {
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted");
            return;
        }

        // New role (keeps asking until valid, 0 cancels)
        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println(" ->Operation Cancelled.");
            return;
        }

        // New status (keeps asking until valid, 0 cancels)
        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println(" ->Operation Cancelled.");
            return;
        }

        // Save (only the role and status change). The DAO prints the result message.
        userDao.updateUserAndStatus(userId, newRole, newStatus);
    }



    // Keeps asking until a whole number is typed (letters never crash the program)
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("-> [ERROR] Please enter a valid numeric ID.");
            }
        }
    }

    // Keeps asking until a valid role is chosen (1, 2, 3 or the role name). Returns null if 0 is entered to cancel.
    private String readValidRole() {
        while (true) {
            System.out.print("Enter Role [1] FRONT DESK [2] ADMIN [3] CUSTOMER [0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equals("CANCEL")) {
                return null;
            }
            if (role.equals("1") || role.equals("FRONT DESK")) {
                return "FRONT DESK";
            }
            if (role.equals("2") || role.equals("ADMIN")) {
                return "ADMIN";
            }
            if (role.equals("3") || role.equals("CUSTOMER")) {
                return "CUSTOMER";
            }
            System.out.println("-> [ERROR] Invalid role. Must be 1 (FRONT DESK), 2 (ADMIN) or 3 (CUSTOMER).");
        }
    }

    // Keeps asking until a valid status is typed. Returns null if 0 is entered to cancel.
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING) [0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("-> [ERROR] Invalid status. Must be APPROVED or PENDING.");
        }
    }



    private void addUser() {
        System.out.println("\n========================ADD NEW USER========================");

        // Ask for the details
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Role [1] FRONT DESK | [2] ADMIN | [3] CUSTOMER: ");
        String roleChoice = scanner.nextLine().trim();

        // Convert the role number to a role name
        String role;
        if (roleChoice.equals("1")) {
            role = "FRONT DESK";
        } else if (roleChoice.equals("2")) {
            role = "ADMIN";
        } else if (roleChoice.equals("3")) {
            role = "CUSTOMER";
        } else {
            System.out.println("->Invalid role.");
            return;
        }

        // No duplicate emails
        if (userDao.emailExists(email, -1)) {
            System.out.println("->Email already exists.");
            return;
        }

        // Save (admin-created accounts need no approval)
        if (userDao.createUser(name, email, password, role, "APPROVED")) {
            System.out.println("->USER ADDED SUCCESSFULLY!");
        } else {
            System.out.println("->Failed to add user.");
        }
    }



    private void deleteUser(User admin) {
        viewAllUsers();

        // Pick the user
        int id = readIntInput("\nEnter User ID to Delete (or 0 to cancel): ");

        if (id == 0) {
            System.out.println("->Delete cancelled.");
            return;
        }

        // Admin cannot delete their own account
        if (id == admin.u_id()) {
            System.out.println("->You cannot delete your own account.");
            return;
        }

        // Delete
        if (userDao.deleteUser(id)) {
            System.out.println("->USER ID: " + id + " DELETED SUCCESSFULLY!");
        } else {
            System.out.println("->User not found or delete failed.");
        }
    }
}