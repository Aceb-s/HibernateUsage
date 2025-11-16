package org.example;

import org.example.console.ConsoleUI;
import org.example.config.HibernateUtil;
import lombok.extern.java.Log;

@Log
public class Main {
    public static void main(String[] args) {
        log.info("User Service application started");

        try {
            ConsoleUI consoleUI = new ConsoleUI();
            consoleUI.start();
        } catch (Exception e) {
            log.severe("Application error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
            log.info("User Service application stopped");
        }
    }
}
