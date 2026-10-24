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
                    updateUser();
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

    //////////////////////////////////////////////////////////////
    // VIEW USERS
    //////////////////////////////////////////////////////////////

    // Formatted table for any list of users
    private void printUserTable(List<User> users) {
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
        System.out.println("\n======================ALL SYSTEM USERS======================");

        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("->No users found.");
        } else {
            printUserTable(users);
        }
    }

    // Shows the pending table and returns the pending list
    private List<User> viewPendingUsers() {
        System.out.println("\n=====================PENDING APPROVALS======================");

        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("->No pending registrations.");
        } else {
            printUserTable(pending);
        }
        return pending;
    }

    //////////////////////////////////////////////////////////////
    // APPROVE USER
    //////////////////////////////////////////////////////////////

    private void approveUser() {
        List<User> pending = viewPendingUsers();
        if (pending.isEmpty()) {
            return;
        }

        while (true) {
            System.out.print("\nEnter User ID to Approve (or 0 to cancel): ");
            String input = scanner.nextLine().trim();

            // Must be a number
            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("->Please enter a valid number.");
                continue;
            }

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

    //////////////////////////////////////////////////////////////
    // ADD USER
    //////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////
    // UPDATE USER ROLE / STATUS
    //////////////////////////////////////////////////////////////

    private void updateUser() {
        viewAllUsers();

        // Pick the user
        System.out.print("\nEnter User ID to Update (or 0 to cancel): ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        if (id == 0) {
            System.out.println("->Update cancelled.");
            return;
        }

        User u = userDao.getUserById(id);
        if (u == null) {
            System.out.println("->User not found.");
            return;
        }

        System.out.println("(Press Enter to keep the current value)");

        // Role
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
            System.out.println("->Invalid role.");
            return;
        }

        // Status
        System.out.print("New Status [1] APPROVED | [2] PENDING [" + u.status() + "]: ");
        String statusChoice = scanner.nextLine().trim();
        String status = u.status();
        if (statusChoice.equals("1")) {
            status = "APPROVED";
        } else if (statusChoice.equals("2")) {
            status = "PENDING";
        } else if (!statusChoice.isEmpty()) {
            System.out.println("->Invalid status.");
            return;
        }

        // Save (name, email and password stay the same)
        User updated = new User(u.u_id(), u.name(), u.email(), u.password(), role, status);
        if (userDao.updateUser(updated)) {
            System.out.println("->USER ID: " + id + " UPDATED SUCCESSFULLY!");
        } else {
            System.out.println("->Update failed.");
        }
    }

    //////////////////////////////////////////////////////////////
    // DELETE USER
    //////////////////////////////////////////////////////////////

    private void deleteUser(User admin) {
        viewAllUsers();

        // Pick the user
        System.out.print("\nEnter User ID to Delete (or 0 to cancel): ");
        int id = Integer.parseInt(scanner.nextLine().trim());

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