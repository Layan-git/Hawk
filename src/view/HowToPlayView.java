package view;

import java.awt.*;
import javax.swing.*;

public class HowToPlayView {
    
    public void show() {
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
            "Right-click to flag a suspected mine: +1 point if correct, -3 points if wrong, remove flag with right-click .flag's ammount = number of bombs on the board.",
            "Mines (-1 life) - Flag to prevent damage and gain +1 point without penalty",
            "Numbers (1-8): Show adjacent mines. Reveal +1 pt, flag -3 pts",
            "Questions: Answer within 30 seconds - Correct: points/lives. Incorrect/Timeout: lose points/lives",
            "Surprises: 50/50 random outcome - Gain or lose points and lives",
            "Metal Detector (5s, Max 3 uses): Reveals hidden mines. Crosshair turns red over mines, green over safe cells",
            "Safety Net (Max 3 uses): Protects from one mine hit. Use strategically to survive",
            "Stabilizer (EXTREME ONLY, Max 1 use): Hit mine on last life? Answer a HARD question to disable it. Correct = survive, Incorrect = game over",
            "Momentum (EXTREME ONLY): Consecutive safe cell reveals build streaks. Every cell interaction counts. Tier 1 (5+ cells): +1 bonus. Tier 2 (15+ cells): +2 bonus ,Icon Color changes accordingly.",
            "Win: Reveal all cells on your board. Lose: All lives run out",
            "Cooperative Play: Both players share lives, score and flags. Work together to complete both boards before lives run out"
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
}
