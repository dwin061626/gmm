package com.scc;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner (System.in);
    private static final UserDao userDao = new UserDao();
    public static void main(String[] args){

        System.out.println("\n===========================================");
        System.out.println(" GYM MEMBERSHIP MANAGEMENT ");
        System.out.println("===========================================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

    switch(choice){

        case "1":
            break;
        case "2":
            System.out.print("Enter the new username: ");
            String user = sc.nextLine();
            System.out.print("Enter the new email: ");
            String email = sc.nextLine();
            System.out.print("Enter the password: ");
            String password = sc.nextLine();

            System.out.print("Select Role [1] FRONT DESK | [2] ADMIN | [3] CUSTOMER: ");
            int roleChoice = sc.nextInt();
            String roles;
        switch (roleChoice){

            case 1:
                roles = "FRONT DESK";
                break;
            case 2:
                roles = "ADMIN";
                break;
            case 3:
                roles = "CUSTOMER";
                break;
            default:
                roles = null;
        }
        if(roles != null){
            userDao.registerUser(user, email, password, roles);

        }else {

            System.out.println("Invalid role selection");
        }

            break;


    }

    }
}
