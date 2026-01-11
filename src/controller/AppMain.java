package controller;

import javax.swing.*;

/**
 * AppMain is the entry point for the application.
 * It initializes GameFlowController with a default user and shows the main menu.
 */
public class AppMain {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize with default user (admin can be set, regular user by default)
            GameFlowController flowController = new GameFlowController("player", false);
            flowController.showMainMenu();
        });
    }
}
