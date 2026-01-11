package controller;

import java.awt.*;
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
        JDialog dialog = new JDialog();
        dialog.setTitle("How to Play");
        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        // Background panel with gradient
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
        
        // Title
        JLabel title = new JLabel("How to Play", SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        bg.add(title, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        
        // Instructions with icons and styling
        String[] instructions = {
            "Left-click a cell to reveal: Mine, Number, Empty, Question, or Surprise",
            "Right-click to flag a suspected mine: +1 point if correct, -3 points if wrong",
            "Mines (-1 life) - Flag to prevent damage and gain +1 point without penalty",
            "Numbers (1-8): Show adjacent mines. Reveal +1 pt, flag -3 pts",
            "Questions: Answer within 30 seconds - Correct: points/lives. Incorrect/Timeout: lose points/lives",
            "Surprises: 50/50 random outcome - Gain or lose points and lives",
            "Metal Detector (5s, Max 3 uses): Reveals hidden mines. Crosshair turns red over mines, green over safe cells",
            "Safety Net (Max 3 uses): Protects from one mine hit. Use strategically to survive",
            "Stabilizer (Auto, Max 1 use): Last life + mine = auto Hard question. Answer correctly to survive and disable mine",
            "Momentum: Consecutive safe cell reveals build streaks. Tier 1 (5+ safe): +1 bonus point per safe cell. Tier 2 (15+ safe): +2 bonus points per safe cell.",
            "Win: Reveal all cells on your board. Lose: All lives run out.",
            "Cooperative Play: Both players share lives and score. Work together to complete both boards before lives run out"
        };
        
        String[] iconPaths = {
            "/resources/left_click.png",   // Left-click icon
            "/resources/falg.png",         // Flag icon
            "/resources/bomb.png",         // Mine icon
            "/resources/numbers.png",      // Numbers icon
            "/resources/question.png",     // Question cells icon
            "/resources/gift.png",         // Surprise cells icon
            "/resources/metaldetector.png",// Metal Detector shop item
            "/resources/net.png",          // Safety Net shop item
            "/resources/defibrillator.png",// Stabilizer mechanic
            "/resources/electric_bolt.png", // Momentum icon
            "/resources/falg.png",         // Win/Loss conditions
            "/resources/handshake.png"     // Cooperative play
        };
        
        // Calculate dialog width based on longest instruction text
        FontMetrics fm = new JLabel().getFontMetrics(new Font("Tahoma", Font.PLAIN, 13));
        int maxWidth = 0;
        for (String instruction : instructions) {
            int width = fm.stringWidth(instruction);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        int dialogWidth = maxWidth + 250; // 250px buffer (icon + margins + padding)
        int dialogHeight = 750;
        dialog.setSize(dialogWidth, dialogHeight);
        
        for (int i = 0; i < instructions.length; i++) {
            JPanel instructionPanel = new JPanel();
            instructionPanel.setOpaque(false);
            instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.X_AXIS));
            instructionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            instructionPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            JLabel iconLabel = new JLabel();
            java.awt.image.BufferedImage icon = model.ResourceLoader.loadImage(iconPaths[i]);
            if (icon != null) {
                Image scaledIcon = icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledIcon));
            } else {
                iconLabel.setText("[" + (i+1) + "]");
                iconLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
            }
            iconLabel.setForeground(new Color(0, 200, 170));
            iconLabel.setPreferredSize(new Dimension(40, 32));
            iconLabel.setMaximumSize(new Dimension(40, 32));
            instructionPanel.add(iconLabel);
            
            instructionPanel.add(Box.createHorizontalStrut(10));
            
            JLabel textLabel = new JLabel("<html><b>" + (i+1) + ".</b> " + instructions[i] + "</html>");
            textLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
            textLabel.setForeground(Color.WHITE);
            // Calculate width for this specific instruction to prevent wrapping
            int instructionWidth = fm.stringWidth(instructions[i]) + 40; // +40 for numbering
            instructionPanel.setMaximumSize(new Dimension(instructionWidth + 70, Integer.MAX_VALUE));
            textLabel.setMaximumSize(new Dimension(instructionWidth, Short.MAX_VALUE));
            textLabel.setAlignmentY(Component.TOP_ALIGNMENT);
            instructionPanel.add(textLabel);
            
            contentPanel.add(instructionPanel);
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(new Color(8, 45, 40));
        scrollPane.getVerticalScrollBar().setForeground(new Color(0, 200, 170));
        bg.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JButton closeBtn = new JButton("Understood");
        closeBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        closeBtn.setBackground(new Color(0, 150, 120));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 170), 2));
        closeBtn.setPreferredSize(new Dimension(120, 40));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        bg.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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

