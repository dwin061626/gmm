package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Admin {

    private final Scanner sc;
    private final UserDao userDao = new UserDao();

    // Share the Scanner from Main so two Scanners don't fight over System.in
    public Admin(Scanner sc) {
        this.sc = sc;
    }

    //////////////////////////////////////////////////////////////
    // HELPERS
    //////////////////////////////////////////////////////////////

    // Safe integer input (returns -1 on invalid input)
    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {
        }
    }

    private void pause() {
        System.out.print("\nPress Enter to continue . . . ");
        sc.nextLine();
    }

    private void printUsers() {
        System.out.printf("| %-5s | %-20s | %-25s | %-12s | %-8s |%n",
                "ID", "NAME", "EMAIL", "ROLE", "STATUS");
        System.out.println("|-------|----------------------|---------------------------|"
                + "--------------|----------|");

        for (User u : userDao.getAllUsers()) {
            System.out.printf("| %-5d | %-20s | %-25s | %-12s | %-8s |%n",
                    u.u_id(), u.name(), u.email(), u.roles(), u.status());
        }
    }

    // Returns the chosen role, or null if the choice is invalid
    private String chooseRole(String prompt) {
        System.out.println("\n" + prompt);
        System.out.println("1. FRONT DESK");
        System.out.println("2. ADMIN");
        System.out.println("3. CUSTOMER");
        System.out.print("Choice: ");
        return switch (readInt()) {
            case 1 -> "FRONT DESK";
            case 2 -> "ADMIN";
            case 3 -> "CUSTOMER";
            default -> null;
        };
    }

    // Returns the chosen status, or null if the choice is invalid
    private String chooseStatus(String prompt) {
        System.out.println("\n" + prompt);
        System.out.println("1. ACTIVE");
        System.out.println("2. PENDING");
        System.out.print("Choice: ");
        return switch (readInt()) {
            case 1 -> "ACTIVE";
            case 2 -> "PENDING";
            default -> null;
        };
    }

    //////////////////////////////////////////////////////////////
    // MANAGE USERS (CRUD)
    //////////////////////////////////////////////////////////////

    private void createUser() {
        clearScreen();
        System.out.println("=== CREATE USER ===\n");

        String name = readLine("Enter Name: ");
        String email = readLine("Enter Email: ");
        String password = readLine("Enter Password: ");

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("\nName, email and password are required.");
            return;
        }

        String role = chooseRole("Select Role:");
        if (role == null) {
            System.out.println("\nInvalid role selection. User not created.");
            return;
        }

        if (userDao.emailExists(email, -1)) {
            System.out.println("\nUser already exists! Cannot create duplicate.");
            return;
        }

        // Admin-created accounts need no approval, so they start ACTIVE
        if (userDao.createUser(name, email, password, role, "ACTIVE")) {
            System.out.println("\nUser created successfully!");
        } else {
            System.out.println("\nFailed to create user.");
        }
    }

    private void updateUser() {
        clearScreen();
        System.out.println("=== UPDATE USER ===\n");
        printUsers();

        System.out.print("\nEnter User ID to Update: ");
        int id = readInt();

        User current = userDao.getUserById(id);
        if (current == null) {
            System.out.printf("%nUser ID %d not found.%n", id);
            return;
        }

        System.out.println("\n(Leave blank to keep the current value)\n");

        String name = readLine("Enter New Name [" + current.name() + "]: ");
        if (name.isEmpty()) name = current.name();

        String email = readLine("Enter New Email [" + current.email() + "]: ");
        if (email.isEmpty()) email = current.email();

        String password = readLine("Enter New Password [unchanged]: ");
        if (password.isEmpty()) password = current.password();

        if (!email.equalsIgnoreCase(current.email()) && userDao.emailExists(email, id)) {
            System.out.println("\nThat email is already used by another user. Update cancelled.");
            return;
        }

        String role = current.roles();
        System.out.print("\nChange role? Current: " + role + " (y/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            String chosen = chooseRole("Select New Role:");
            if (chosen == null) {
                System.out.println("\nInvalid role selection. Update cancelled.");
                return;
            }
            role = chosen;
        }

        String status = current.status();
        System.out.print("\nChange status? Current: " + status + " (y/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            String chosen = chooseStatus("Select New Status:");
            if (chosen == null) {
                System.out.println("\nInvalid status selection. Update cancelled.");
                return;
            }
            status = chosen;
        }

        User updated = new User(id, name, email, password, role, status);
        if (userDao.updateUser(updated)) {
            System.out.println("\nUser updated successfully!");
        } else {
            System.out.println("\nFailed to update user.");
        }
    }

    private void deleteUser(User loggedInUser) {
        clearScreen();
        System.out.println("=== DELETE USER ===\n");
        printUsers();

        System.out.print("\nEnter User ID to Delete: ");
        int id = readInt();

        User target = userDao.getUserById(id);
        if (target == null) {
            System.out.printf("%nUser ID %d not found.%n", id);
            return;
        }

        if (id == loggedInUser.u_id()) {
            System.out.println("\nYou cannot delete the account you are logged in with.");
            return;
        }

        System.out.print("\nDelete " + target.name() + " (" + target.roles() + ")? (y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("\nDelete cancelled.");
            return;
        }

        if (userDao.deleteUser(id)) {
            System.out.println("\nUser deleted successfully!");
        } else {
            System.out.println("\nFailed to delete user.");
        }
    }

    private void manageUsers(User loggedInUser) {
        while (true) {
            clearScreen();
            System.out.println("=== MANAGE USERS ===\n");
            System.out.println("1. Create User");
            System.out.println("2. View Users");
            System.out.println("3. Update User");
            System.out.println("4. Delete User");
            System.out.println("5. Back");
            System.out.print("\nChoice: ");

            switch (readInt()) {
                case 1 -> {
                    createUser();
                    pause();
                }
                case 2 -> {
                    clearScreen();
                    System.out.println("=== USER LIST ===\n");
                    printUsers();
                    pause();
                }
                case 3 -> {
                    updateUser();
                    pause();
                }
                case 4 -> {
                    deleteUser(loggedInUser);
                    pause();
                }
                case 5 -> {
                    return;
                }
                default -> {
                    System.out.println("\nInvalid choice!");
                    pause();
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////
    // MANAGE SUBSCRIPTION
    //////////////////////////////////////////////////////////////

    private void manageSubscription() {
        clearScreen();
        System.out.println("=== MANAGE SUBSCRIPTION ===\n");

        System.out.println("AVAILABLE PLANS:");
        System.out.println("---------------------------------");
        System.out.println("1. Regular  - " + FrontDesk.REGULAR_PRICE + " per month");
        System.out.println("2. Premium  - " + FrontDesk.PREMIUM_PRICE + " per month");
        System.out.println("---------------------------------");
        System.out.println("\nNote: Prices are fixed in system.");

        pause();
    }

    //////////////////////////////////////////////////////////////
    // ADMIN MAIN MENU
    //////////////////////////////////////////////////////////////

    public void showMenu(User loggedInUser) {
        while (true) {
            clearScreen();
            System.out.println("=================================");
            System.out.println("        ADMIN DASHBOARD");
            System.out.println("=================================");
            System.out.println("Welcome, " + loggedInUser.name() + "\n");

            System.out.println("1. Manage Users");
            System.out.println("2. Manage Subscription");
            System.out.println("3. Logout");
            System.out.print("\nChoice: ");

            switch (readInt()) {
                case 1 -> manageUsers(loggedInUser);
                case 2 -> manageSubscription();
                case 3 -> {
                    return;
                }
                default -> {
                    System.out.println("\nInvalid choice!");
                    pause();
                }
            }
        }
    }
}