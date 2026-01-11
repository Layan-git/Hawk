package controller;

import java.awt.*;
import javax.swing.*;
import model.Board;
import view.GameSetup;

public class SetupController implements IGameSetupController {
    
    private GameSetup gameSetup;
    private final GameFlowController gameFlowController;
    
    public SetupController(GameFlowController gameFlowController) {
        this.gameFlowController = gameFlowController;
    }
    
    public void setGameSetup(GameSetup gameSetup) {
        this.gameSetup = gameSetup;
    }
    
    @Override
    public void backToMenu() {
        if (gameSetup != null) gameSetup.close();
        gameFlowController.showMainMenu();
    }

    @Override
    public void confirmStart(String p1, String p2, Board.Difficulty d, int p1CharIndex, int p2CharIndex) {
        // Validate icons are selected (charIndex should not be -1 or same)
        if (p1CharIndex < 0) {
            showStyledErrorDialog(
                    "Missing Player 1 Icon",
                    "Player 1 must select an icon!",
                    "/resources/gears.png"
            );
            return;
        }
        
        if (p2CharIndex < 0) {
            showStyledErrorDialog(
                    "Missing Player 2 Icon",
                    "Player 2 must select an icon!",
                    "/resources/gears.png"
            );
            return;
        }
        
        // Validate that both players have chosen different icons
        if (p1CharIndex == p2CharIndex) {
            showStyledErrorDialog(
                    "Invalid Icon Selection",
                    "Both players must choose different icons!\nPlease select unique icons for each player.",
                    "/resources/gears.png"
            );
            return;
        }
        
        // Use default names if empty
        String player1Name = (p1 == null || p1.trim().isEmpty()) ? "Player 1" : p1;
        String player2Name = (p2 == null || p2.trim().isEmpty()) ? "Player 2" : p2;
        
        gameFlowController.startGameBoard(player1Name, player2Name, d, p1CharIndex, p2CharIndex);
    }
    
    private void showStyledErrorDialog(String title, String message, String iconPath) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(450, 220);
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
        titleLabel.setForeground(new Color(255, 100, 100));
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        
        // Try to load and display icon
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadImage(iconPath);
        if (icon != null) {
            Image scaledIcon = icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledIcon));
            titleLabel.setIconTextGap(10);
        }
        bg.add(titleLabel, BorderLayout.NORTH);
        
        JLabel msgLabel = new JLabel("<html>" + message + "</html>", SwingConstants.CENTER);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        bg.add(msgLabel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        okBtn.setBackground(new Color(200, 100, 100));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 150, 150), 2));
        okBtn.setPreferredSize(new Dimension(90, 35));
        okBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(okBtn);
        bg.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}
