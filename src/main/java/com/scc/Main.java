package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize database
        DatabaseInitializer.initialize();

        // 2. Instantiate DAOs & utilities (one shared copy each)
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();
        userDao.defaultAdminGenerator();

        // 3. Delegate to Authentication
        new Authentication(scanner, userDao).start();

        scanner.close();
    }
}