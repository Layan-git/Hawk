package view;

import controller.Main.MainMenuController;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenu {

    private JFrame frame;
    private final MainMenuController controller;

    public static void main(String[] args) {
        // Launches the main menu (entry point for the whole game UI)
        EventQueue.invokeLater(controller.Main::new);
    }

    /**
     * @wbp.parser.entryPoint
     */
    public MainMenu(MainMenuController controller) {
        this.controller = controller;
        initialize();
    }

    // make window visible
    public void show() {
        frame.setVisible(true);
    }
    // hide window but don't destroy it
    public void close() {
        frame.setVisible(false);
    }

    private void initialize() {
        int W = 1024;
        int H = 768;
        frame = new JFrame("Minesweeper V2");
        frame.setBounds(100, 100, W, H);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // background with gif (will loop forever by default)
        JPanel bg = new JPanel() {
            private final Image bgImage;
            {
                ImageIcon icon = new ImageIcon(MainMenu.class.getResource("/resources/bg.gif"));
                // GIF loops forever by default in Swing
                bgImage = icon.getImage();
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        bg.setBounds(0, 0, W, H);
        bg.setLayout(null);
        frame.setContentPane(bg);

        // Side panel with opacity gradient - solid on left, fades to transparent on right
        JPanel sidePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
                // Horizontal gradient from left (opaque) to right (transparent)
                Color leftColor = new Color(20, 45, 35, 255);      // Opaque charcoal with green tint on left
                Color rightColor = new Color(20, 45, 35, 0);       // Transparent on right
                GradientPaint gp = new GradientPaint(0, 0, leftColor, getWidth(), 0, rightColor);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidePanel.setBounds(0, 0, 280, H);
        sidePanel.setOpaque(false);  // Important: allow background to show through
        bg.add(sidePanel);
        sidePanel.setLayout(null);

        // Place icon at the top of side panel
        javax.swing.JLabel iconLabel = new javax.swing.JLabel(new ImageIcon(MainMenu.class.getResource("/resources/nerd_icon_shwompy.png")));
        iconLabel.setBounds((280 - 64) / 2, 30, 64, 64);
        sidePanel.add(iconLabel);

        // Place game title on side panel
        JLabel title = new JLabel("MINESWEEPER V2");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(10, 100, 260, 44);
        sidePanel.add(title);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));

        // Place subtitle on side panel
        JLabel subtitle = new JLabel("THINK BEFORE YOU CLICK");
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setBounds(10, 150, 260, 20);
        sidePanel.add(subtitle);
        subtitle.setForeground(new Color(170, 220, 200));
        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 12));

        JButton startBtn = new JButton("Start Game");
        startBtn.setBounds(20, 300, 240, 40);
        sidePanel.add(startBtn);
        styleMenuButton(startBtn);

        JButton historyBtn = new JButton("History");
        historyBtn.setBounds(20, 350, 240, 40);
        sidePanel.add(historyBtn);
        styleMenuButton(historyBtn);

        JButton manageBtn = new JButton("Manage Questions");
        manageBtn.setBounds(20, 400, 240, 40);
        sidePanel.add(manageBtn);
        styleMenuButton(manageBtn);
        manageBtn.addActionListener(e -> controller.openManageQuestions());
        
        JButton howBtn = new JButton("How to Play");
        howBtn.setBounds(20, 450, 240, 40);
        sidePanel.add(howBtn);
        styleMenuButton(howBtn);

        // Settings button in top right corner with cog icon
        JButton settingsBtn = new JButton();
        settingsBtn.setBounds(W - 75, 10, 50, 50);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.setBorderPainted(false);
        
        // Load gears icon with better quality
        try {
            File gearsFile = new File("src/resources/gears.png");
            if (!gearsFile.exists()) {
                gearsFile = new File("resources/gears.png");
            }
            if (gearsFile.exists()) {
                java.awt.image.BufferedImage gearsImage = ImageIO.read(gearsFile);
                // Use SCALE_AREA_AVERAGING for better quality
                Image scaledImage = gearsImage.getScaledInstance(50, 50, Image.SCALE_AREA_AVERAGING);
                settingsBtn.setIcon(new ImageIcon(scaledImage));
                settingsBtn.setRolloverIcon(new ImageIcon(
                    gearsImage.getScaledInstance(52, 52, Image.SCALE_AREA_AVERAGING)
                ));
            }
        } catch (Exception e) {
            System.err.println("Could not load cog icon: " + e.getMessage());
            settingsBtn.setText("⚙");
            settingsBtn.setFont(new Font("Tahoma", Font.PLAIN, 24));
        }
        
        bg.add(settingsBtn);
        // Don't style it with menu button styling for icon buttons
        settingsBtn.setOpaque(false);
        styleMenuButton(settingsBtn);

        // --- BEHAVIOR: hook up menu actions to controller here ---

        howBtn.addActionListener(e -> controller.openHowToPlay());
        manageBtn.addActionListener(e -> controller.openManageQuestions());
        historyBtn.addActionListener(e -> controller.openHistory());
        startBtn.addActionListener(e -> controller.startGame());
        settingsBtn.addActionListener(e -> controller.openSettings());

        // set window/taskbar icon (may need adjustment for IDE resource path)
        frame.setIconImage(new ImageIcon("/resources/nerd_icon_shwompy.png").getImage());   
    }

    // all menu buttons use same simple style with hover effect
    private void styleMenuButton(JButton b) {
        b.setForeground(new Color(220, 235, 230));
        b.setFont(new Font("Tahoma", Font.PLAIN, 16));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);

        // Add hover effect - text turns green
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(new Color(0, 255, 100)); // Bright green
                b.repaint(); // Force repaint to avoid text ghosting
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(new Color(220, 235, 230)); // Back to light color
                b.repaint(); // Force repaint to avoid text ghosting
            }
        });
    }
}
