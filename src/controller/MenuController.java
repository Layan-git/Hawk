package controller;

import javax.swing.*;
import view.HistoryView;
import view.LeaderBoard;
import view.LoginView;
import view.MainMenu;
import view.QuestionsManager;
import view.Settings;

public class MenuController implements IMainMenuController {
    
    private MainMenu mainMenu;
    private final GameFlowController gameFlowController;
    private String currentUser;
    private boolean isAdmin;
    
    public MenuController(GameFlowController gameFlowController, String currentUser, boolean isAdmin) {
        this.gameFlowController = gameFlowController;
        this.currentUser = currentUser;
        this.isAdmin = isAdmin;
    }
    
    public void setMainMenu(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }
    
    public void setUserInfo(String user, boolean admin) {
        this.currentUser = user;
        this.isAdmin = admin;
    }
    
    @Override
    public void startGame() {
        gameFlowController.showSetup(mainMenu);
    }

    @Override
    public void openHistory() {
        HistoryView historyView = new HistoryView(currentUser, isAdmin);
        historyView.show();
    }

    @Override
    public void openLeaderBoard() {
        LeaderBoard leaderboard = new LeaderBoard();
        leaderboard.show();
    }

    @Override
    public void openManageQuestions() {
        // Show login dialog to authenticate before allowing access
        final LoginView[] loginViewHolder = new LoginView[1];

        LoginView.LoginController loginController = new LoginView.LoginController() {
            @Override
            public void onLogin(String username, String password) {
                if (username == null || username.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Please enter a username.",
                            "Login",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Simple auth: "admin" is required to manage questions
                if (!"admin".equalsIgnoreCase(username)) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Only admin can manage questions.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Close the login dialog
                if (loginViewHolder[0] != null) {
                    loginViewHolder[0].close();
                }
                
                // Show questions manager with callback to close it when done
                // Main menu stays open behind it
                final QuestionsManager[] qmHolder = new QuestionsManager[1];
                QuestionsManager qm = new QuestionsManager(() -> {
                    // Callback when QuestionsManager is closed - just close the questions manager
                    if (qmHolder[0] != null) {
                        qmHolder[0].close();
                    }
                });
                qmHolder[0] = qm;
                qm.show();
            }

            @Override
            public void onExit() {
                if (loginViewHolder[0] != null) {
                    loginViewHolder[0].close();
                }
            }
        };

        LoginView loginView = new LoginView(loginController);
        loginViewHolder[0] = loginView;
        loginView.show();
    }

    @Override
    public void openHowToPlay() {
        JOptionPane.showMessageDialog(
                null,
                "How to Play Minesweeper:\n\n" +
                        "1. Left-click to reveal a cell\n" +
                        "2. Right-click to flag a suspected mine\n" +
                        "3. Avoid hitting mines\n" +
                        "4. Answer questions to gain points\n" +
                        "5. Use special items strategically",
                "How to Play",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void openSettings() {
        Settings settingsView = new Settings(this);
        settingsView.show();
    }

    @Override
    public void showMainMenu() {
        if (mainMenu != null) {
            mainMenu.show();
        }
    }

    @Override
    public void exit() {
        System.exit(0);
    }
}
