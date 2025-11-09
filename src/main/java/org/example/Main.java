package org.example;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final UserDao userDao = new UserDao();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
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
                        System.out.println("Exit...");
                        return;
                    default:
                        System.out.println("Wrong action!");
                }
            }
        } catch (Exception e) {
            System.out.println("Issue: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
        }
    }

    private static void printMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1. Create User");
        System.out.println("2. Find User by ID");
        System.out.println("3. Show all Users");
        System.out.println("4. Refresh User");
        System.out.println("5. Delete User");
        System.out.println("6. Exit");
        System.out.print("Choose action: ");
    }

    private static void createUser() {
        try {
            System.out.print("Type User Name: ");
            String name = scanner.nextLine();

            System.out.print("Type Hometown: ");
            String hometown = scanner.nextLine();

            System.out.print("Type age: ");
            int age = Integer.parseInt(scanner.nextLine());

            User user = new User(name, hometown, age);
            userDao.save(user);
            System.out.println("User created with an ID: " + user.getId());
        } catch (Exception e) {
            System.out.println("User creation issue: " + e.getMessage());
        }
    }

    private static void getUserById() {
        try {
            System.out.print("Type User ID: ");
            Long id = Long.parseLong(scanner.nextLine());

            User user = userDao.findById(id);
            if (user != null) {
                System.out.println("User found: " + user);
            } else {
                System.out.println("User not found!");
            }
        } catch (Exception e) {
            System.out.println("User finding issue: " + e.getMessage());
        }
    }

    private static void getAllUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("Users are found");
            } else {
                for (User user : users) {
                    System.out.println(user);
                }
            }
        } catch (Exception e) {
            System.out.println("Issue while getting User list: " + e.getMessage());
        }
    }

    private static void updateUser() {
        try {
            System.out.print("Type User ID: ");
            Long id = Long.parseLong(scanner.nextLine());

            User user = userDao.findById(id);
            if (user == null) {
                System.out.println("User not found!");
                return;
            }

            System.out.print("Type new Name: ");
            user.setName(scanner.nextLine());

            System.out.print("Type new Hometown: ");
            user.setHometown(scanner.nextLine());

            System.out.print("Type new Age: ");
            user.setAge(Integer.parseInt(scanner.nextLine()));

            userDao.update(user);
            System.out.println("User updated");
        } catch (Exception e) {
            System.out.println("Issue occured while updating User : " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("Type User ID for deletion: ");
            Long id = Long.parseLong(scanner.nextLine());

            userDao.delete(id);
            System.out.println("User deleted");
        } catch (Exception e) {
            System.out.println("Issue occured while deleting User: " + e.getMessage());
        }
    }
}