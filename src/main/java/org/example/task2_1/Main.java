package org.example.task2_1;

import org.example.task2_1.dao.UserDAO;
import org.example.task2_1.dao.UserDAOImpl;
import org.example.task2_1.model.User;
import org.example.task2_1.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final UserDAO userDao = new UserDAOImpl();

    static void main(String[] args) {
        logger.info("Starting user-service application");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean exit = false;
            while (!exit) {
                printMenu();
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1": createUser(scanner); break;
                    case "2": listUsers(); break;
                    case "3": getUser(scanner); break;
                    case "4": updateUser(scanner); break;
                    case "5": deleteUser(scanner); break;
                    case "0": exit = true; break;
                    default: System.out.println("Wrong choice. Try again");
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected application error", e);
            System.out.println("Error: " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
            logger.info("Application stopped");
        }
    }

    private static void printMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1) Create user");
        System.out.println("2) List all users");
        System.out.println("3) Get user by id");
        System.out.println("4) Update user");
        System.out.println("5) Delete user");
        System.out.println("0) Exit");
        System.out.print("Choose action: ");
    }

    private static void createUser(Scanner scanner) {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Age (number) or empty: ");
            String ageStr = scanner.nextLine().trim();
            Integer age = ageStr.isEmpty() ? null : Integer.parseInt(ageStr);

            User user = new User(name, email, age, LocalDateTime.now());
            userDao.create(user);
            System.out.println("Created user: " + user);
        } catch (Exception e) {
            logger.error("Failed to create user", e);
            System.out.println("Failed to create user: " + e.getMessage());
        }
    }

    private static void listUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }
            users.forEach(System.out::println);
        } catch (Exception e) {
            logger.error("Failed to list users", e);
            System.out.println("Failed to list users: " + e.getMessage());
        }
    }

    private static void getUser(Scanner scanner) {
        try {
            System.out.print("ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<User> userOpt = userDao.findById(id);
            userOpt.ifPresentOrElse(
                    System.out::println,
                    () -> System.out.println("No users found with id = " + id)
            );
        } catch (Exception e) {
            logger.error("Failed to get user", e);
            System.out.println("Failed to get user: " + e.getMessage());
        }
    }

    private static void updateUser(Scanner scanner) {
        try {
            System.out.print("User id to update: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<User> userOpt = userDao.findById(id);
            if (userOpt.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            User user = userOpt.get();
            System.out.println("Current user: " + user);

            System.out.print("New name (Enter to keep current): ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) user.setName(name);

            System.out.print("New email (Enter to keep current): ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) user.setEmail(email);

            System.out.print("New age (Enter to keep current): ");
            String ageStr = scanner.nextLine().trim();
            if (!ageStr.isEmpty()) user.setAge(Integer.parseInt(ageStr));

            userDao.update(user);
            System.out.println("User: " + user + " has been updated." );
        } catch (Exception e) {
            logger.error("Failed to update user", e);
            System.out.println("Failed to update user: " + e.getMessage());
        }
    }

    private static void deleteUser(Scanner scanner) {
        try {
            System.out.print("User id to delete: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            boolean deleted = userDao.delete(id);
            if (deleted) System.out.println("User has been deleted.");
            else System.out.println("User has not been found, nothing to delete.");
        } catch (Exception e) {
            logger.error("Failed to delete user", e);
            System.out.println("Failed to delete user: " + e.getMessage());
        }
    }
}
