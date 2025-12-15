package view;

import controller.Main.MainMenuController;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Settings {

    private JFrame frame;
    private final MainMenuController controller;

    public Settings(MainMenuController controller) {
        this.controller = controller;
        initialize();
    }

    public void show() {
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
    }

    private void initialize() {
        int W = 1024;
        int H = 768;
        frame = new JFrame("Settings");
        frame.setBounds(100, 100, W, H);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // background with gif
        JPanel bg = new JPanel() {
            private final Image bgImage;
            {
                ImageIcon icon = new ImageIcon(Settings.class.getResource("/resources/bg.gif"));
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

        // Settings panel
        JPanel settingsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color c1 = new Color(20, 45, 35, 220);
                g2.setColor(c1);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        settingsPanel.setBounds((W - 400) / 2, (H - 300) / 2, 400, 300);
        bg.add(settingsPanel);
        settingsPanel.setLayout(null);

        // Title
        JLabel title = new JLabel("SETTINGS");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 20, 400, 40);
        settingsPanel.add(title);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));

        // Settings label placeholder
        JLabel settingsLabel = new JLabel("Settings Coming Soon...");
        settingsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        settingsLabel.setBounds(0, 100, 400, 30);
        settingsPanel.add(settingsLabel);
        settingsLabel.setForeground(new Color(170, 220, 200));
        settingsLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // Back button
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(150, 220, 100, 40);
        settingsPanel.add(backBtn);
        styleButton(backBtn);
        backBtn.addActionListener(e -> {
            close();
            controller.showMainMenu();
        });

        // set window/taskbar icon
        frame.setIconImage(new ImageIcon("/resources/bomb.png").getImage());
    }

    private void styleButton(JButton b) {
        b.setForeground(new Color(220, 235, 230));
        b.setFont(new Font("Tahoma", Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);

        // Add hover effect - text turns green
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(new Color(0, 255, 100));
                b.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(new Color(220, 235, 230));
                b.repaint();
            }
        });
    }
}
