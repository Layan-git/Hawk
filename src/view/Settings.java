package view;

import controller.IMainMenuController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.AudioManager;
import model.ResourceLoader;

public class Settings {

    private JFrame frame;
    private final IMainMenuController controller;

    public Settings(IMainMenuController controller) {
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
                Image img = null;
                // Load from classpath resources
                java.net.URL bgUrl = Settings.class.getResource("/resources/bg.gif");
                if (bgUrl != null) {
                    img = new ImageIcon(bgUrl).getImage();
                } else {
                    // Fallback for development
                    String bgPath = ResourceLoader.getResourcePath("/resources/bg.gif");
                    if (bgPath != null) {
                        try {
                            img = ImageIO.read(new File(bgPath));
                        } catch (Exception e) {
                            System.err.println("Could not load background image: " + e.getMessage());
                        }
                    }
                }
                bgImage = img;
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
        settingsPanel.setBounds((W - 500) / 2, (H - 350) / 2, 500, 350);
        bg.add(settingsPanel);
        settingsPanel.setLayout(null);

        // Title
        JLabel title = new JLabel("SETTINGS");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 20, 500, 40);
        settingsPanel.add(title);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));

        // Music button
        JButton musicBtn = new JButton("MUSIC");
        musicBtn.setBounds(50, 80, 180, 50);
        settingsPanel.add(musicBtn);
        updateMusicButtonState(musicBtn);
        musicBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        musicBtn.setFocusPainted(false);
        musicBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        musicBtn.addActionListener(e -> {
            boolean currentlyMuted = AudioManager.getInstance().isMusicMuted();
            AudioManager.getInstance().setMusicMuted(!currentlyMuted);
            updateMusicButtonState(musicBtn);
        });

        // Sound Effects button
        JButton soundBtn = new JButton("SOUND");
        soundBtn.setBounds(270, 80, 180, 50);
        settingsPanel.add(soundBtn);
        updateSoundButtonState(soundBtn);
        soundBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        soundBtn.setFocusPainted(false);
        soundBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        soundBtn.addActionListener(e -> {
            boolean currentlyMuted = AudioManager.getInstance().isSoundEffectsMuted();
            AudioManager.getInstance().setSoundEffectsMuted(!currentlyMuted);
            updateSoundButtonState(soundBtn);
        });

        // Back button
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(150, 250, 200, 50);
        settingsPanel.add(backBtn);
        styleButton(backBtn);
        backBtn.addActionListener(e -> {
            close();
            controller.showMainMenu();
        });

        // set window/taskbar icon
        try {
            java.net.URL iconUrl = Settings.class.getResource("/resources/nerd_icon_shwompy.png");
            if (iconUrl != null) {
                frame.setIconImage(new ImageIcon(iconUrl).getImage());
            } else {
                String iconPath = ResourceLoader.getResourcePath("/resources/nerd_icon_shwompy.png");
                if (iconPath != null) {
                    frame.setIconImage(ImageIO.read(new File(iconPath)));
                }
            }
        } catch (Exception e) {
            System.err.println("Could not set window icon: " + e.getMessage());
        }
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
    
    /**
     * Update music button color and text based on mute state
     */
    private void updateMusicButtonState(JButton btn) {
        if (AudioManager.getInstance().isMusicMuted()) {
            // Music is MUTED (OFF)
            btn.setBackground(new Color(200, 50, 50));
            btn.setForeground(Color.WHITE);
        } else {
            // Music is ON
            btn.setBackground(new Color(50, 150, 100));
            btn.setForeground(Color.WHITE);
        }
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
    }
    
    /**
     * Update sound button color and text based on mute state
     */
    private void updateSoundButtonState(JButton btn) {
        if (AudioManager.getInstance().isSoundEffectsMuted()) {
            // Sound is MUTED (OFF)
            btn.setBackground(new Color(200, 50, 50));
            btn.setForeground(Color.WHITE);
        } else {
            // Sound is ON
            btn.setBackground(new Color(50, 150, 100));
            btn.setForeground(Color.WHITE);
        }
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
    }
}

