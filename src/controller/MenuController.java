package controller;

import java.awt.*;
import javax.swing.*;
import view.HistoryView;
import view.HowToPlayView;
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
    private QuestionsManager questionsManager; // Store reference for font refresh
    
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
                    showStyledDialog(
                        "Login",
                        "Please enter a username.",
                        "warning"
                    );
                    return;
                }

                // Simple auth: "admin" is required to manage questions
                if (!"admin".equalsIgnoreCase(username)) {
                    showStyledDialog(
                        "Access Denied",
                        "Only admin can manage questions.",
                        "warning"
                    );
                    return;
                }

                // Close the login dialog
                if (loginViewHolder[0] != null) {
                    loginViewHolder[0].close();
                }
                
                // Show questions manager
                // Main menu stays open behind it
                questionsManager = new QuestionsManager();
                questionsManager.show();
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
        HowToPlayView howToPlayView = new HowToPlayView();
        howToPlayView.show();
    }

    @Override
    public void openSettings() {
        Settings settingsView = new Settings(this);
        settingsView.setMainMenu(mainMenu);
        settingsView.show();
    }

    @Override
    public void showMainMenu() {
        if (mainMenu != null) {
            mainMenu.show();
        }
    }
    
    // Refresh QuestionsManager fonts when custom font is toggled
    public void refreshQuestionsManagerFonts() {
        if (questionsManager != null) {
            questionsManager.refreshFonts();
        }
    }

    @Override
    public void exit() {
        System.exit(0);
    }
    
    // Styled dialog method using FontManager
    private void showStyledDialog(String title, String message, String type) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(450, 200);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color c1 = new Color(8, 45, 40);
                Color c2 = new Color(5, 80, 60);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(bg);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(type.equals("error") ? new Color(255, 100, 100) : new Color(255, 200, 100));
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        bg.add(titleLabel, BorderLayout.NORTH);
        
        JLabel msgLabel = new JLabel("<html>" + message + "</html>", SwingConstants.CENTER);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bg.add(msgLabel, BorderLayout.CENTER);
        
        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        okBtn.setBackground(new Color(0, 150, 120));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 170), 2));
        okBtn.setPreferredSize(new Dimension(100, 40));
        okBtn.addActionListener(e -> dialog.dispose());
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);
        bg.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}

