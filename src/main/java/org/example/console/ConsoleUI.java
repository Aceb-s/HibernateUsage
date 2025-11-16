package org.example.console;

import org.example.service.UserService;
import org.example.entity.User;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

@Log
public class ConsoleUI {
    private final UserService userService;
    private final Scanner scanner;

    public ConsoleUI() {
        this.userService = new UserService();  // Теперь работает!
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        log.info("Console UI started");

        while (true) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createUser();
                    break;
                case "2":
                    getUserById();
                    break;
                case "3":
                    getAllUsers();
                    break;
                case "4":
                    updateUser();
                    break;
                case "5":
                    deleteUser();
                    break;
                case "6":
                    log.info("Console UI terminated by user");
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option! Please choose 1-6.");
                    log.warning("Invalid menu option selected: " + choice);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1. Create User");
        System.out.println("2. Find User by ID");
        System.out.println("3. List All Users");
        System.out.println("4. Update User");
        System.out.println("5. Delete User");
        System.out.println("6. Exit");
        System.out.print("Choose an option (1-6): ");
    }

    private void createUser() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            System.out.print("Enter hometown: ");
            String hometown = scanner.nextLine();

            System.out.print("Enter age: ");
            int age = Integer.parseInt(scanner.nextLine());

            User user = userService.createUser(name, hometown, age);
            System.out.println("User created successfully with ID: " + user.getId());

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid age format!");
            log.warning("Invalid age format input");
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            log.log(Level.SEVERE, "Error in createUser: " + e.getMessage(), e);
        }
    }

    private void getUserById() {
        try {
            System.out.print("Enter user ID: ");
            Long id = Long.parseLong(scanner.nextLine());

            userService.getUserById(id).ifPresentOrElse(
                    user -> System.out.println("User found: " + user),
                    () -> System.out.println("User not found with ID: " + id)
            );

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID format!");
            log.warning("Invalid ID format input");
        } catch (Exception e) {
            System.out.println("Error finding user: " + e.getMessage());
            log.log(Level.SEVERE, "Error in getUserById: " + e.getMessage(), e);
        }
    }

    private void getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                users.forEach(System.out::println);
            }

        } catch (Exception e) {
            System.out.println("Error retrieving users: " + e.getMessage());
            log.log(Level.SEVERE, "Error in getAllUsers: " + e.getMessage(), e);
        }
    }

    private void updateUser() {
        try {
            System.out.print("Enter user ID to update: ");
            Long id = Long.parseLong(scanner.nextLine());

            userService.getUserById(id).ifPresentOrElse(
                    user -> {
                        System.out.println("Current user: " + user);

                        System.out.print("Enter new name (press Enter to keep current): ");
                        String name = scanner.nextLine();

                        System.out.print("Enter new hometown (press Enter to keep current): ");
                        String hometown = scanner.nextLine();

                        System.out.print("Enter new age (press Enter to keep current): ");
                        String ageInput = scanner.nextLine();

                        Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

                        userService.updateUser(id,
                                name.isEmpty() ? null : name,
                                hometown.isEmpty() ? null : hometown,
                                age);

                        System.out.println("User updated successfully!");
                    },
                    () -> System.out.println("User not found with ID: " + id)
            );

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format!");
            log.warning("Invalid number format in updateUser");
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            log.log(Level.SEVERE, "Error in updateUser: " + e.getMessage(), e);
        }
    }

    private void deleteUser() {
        try {
            System.out.print("Enter user ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine());

            userService.deleteUser(id);
            System.out.println("User deleted successfully!");

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID format!");
            log.warning("Invalid ID format in deleteUser");
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            log.log(Level.SEVERE, "Error in deleteUser: " + e.getMessage(), e);
        }
    }

    public void close() {
        scanner.close();
        log.info("Console UI closed");
    }
}
