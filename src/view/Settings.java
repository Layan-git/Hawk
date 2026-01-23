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
    private MainMenu mainMenu; // To refresh fonts on MainMenu

    public Settings(IMainMenuController controller) {
        this.controller = controller;
        initialize();
    }
    
    // Allow MenuController to pass MainMenu reference
    public void setMainMenu(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
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

        // Secret button
        JButton secretBtn = new JButton("SECRET");
        secretBtn.setBounds(10, 10, 60, 20);
        bg.add(secretBtn);
        secretBtn.setFont(new Font("Tahoma", Font.BOLD, 8));
        secretBtn.setForeground(Color.WHITE);
        secretBtn.setBackground(new Color(100, 50, 150, 80));
        secretBtn.setOpaque(true);
        secretBtn.setFocusPainted(false);
        secretBtn.setBorder(BorderFactory.createLineBorder(new Color(150, 100, 200, 100), 1));
        secretBtn.addActionListener(e -> {
            playSecretAnimation();
        });

        // Image button (top right) - REMOVED
        // JButton imageBtn = new JButton();
        // imageBtn.setBounds(W - 70, 10, 60, 20);
        // bg.add(imageBtn);
        // imageBtn.setForeground(Color.WHITE);
        // imageBtn.setBackground(new Color(50, 150, 100, 80));
        // imageBtn.setOpaque(true);
        // imageBtn.setFocusPainted(false);
        // imageBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 150, 100), 1));
        // imageBtn.addActionListener(e -> {
        //     openImageDialog();
        // });

        // Back button
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(150, 250, 200, 50);
        settingsPanel.add(backBtn);
        styleBackButton(backBtn);
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

    private void styleBackButton(JButton b) {
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Tahoma", Font.BOLD, 14));
        b.setBackground(new Color(0, 150, 120));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 170), 2));
        
        // Add hover effect
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0, 180, 150));
                b.setBorder(BorderFactory.createLineBorder(new Color(100, 255, 200), 2));
                b.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(0, 150, 120));
                b.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 170), 2));
                b.repaint();
            }
        });
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
    
    private void playSecretAnimation() {
        // Mute background music first
        AudioManager.getInstance().setMusicMuted(true);
        
        // Create a dialog to display the GIF
        JDialog dialog = new JDialog();
        dialog.setSize(400, 350);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setUndecorated(true);
        
        // Create panel with just the GIF
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(0, 0, 0, 0));
        
        // Load and display the animated GIF
        try {
            java.net.URL gifUrl = Settings.class.getResource("/resources/ss.gif");
            if (gifUrl != null) {
                ImageIcon animatedGif = new ImageIcon(gifUrl);
                JLabel gifLabel = new JLabel(animatedGif);
                mainPanel.add(gifLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            System.err.println("Could not load GIF: " + e.getMessage());
        }
        
        dialog.add(mainPanel);
        
        // Play the secret sound effect
        AudioManager.getInstance().playSoundEffect("ss.wav");
        
        // Show the dialog after a brief delay
        Timer showDialogTimer = new Timer(100, e -> dialog.setVisible(true));
        showDialogTimer.setRepeats(false);
        showDialogTimer.start();
        
        // Close dialog after 2 seconds
        Timer closeDialogTimer = new Timer(2000, e -> dialog.dispose());
        closeDialogTimer.setRepeats(false);
        closeDialogTimer.start();
    }
    
    private void openImageDialog() {
        // Create a dialog to display an image
        JDialog dialog = new JDialog();
        dialog.setTitle("Image");
        dialog.setSize(800, 600);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Create layered pane for image and text overlay
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setPreferredSize(new Dimension(800, 600));
        
        // Load and display the image
        try {
            java.net.URL imgUrl = Settings.class.getResource("/resources/loz.jpeg");
            if (imgUrl != null) {
                ImageIcon originalImg = new ImageIcon(imgUrl);
                // Scale image to fit window (leaving some padding)
                int maxWidth = 750;
                int maxHeight = 550;
                int imgWidth = originalImg.getIconWidth();
                int imgHeight = originalImg.getIconHeight();
                
                double scaleX = (double) maxWidth / imgWidth;
                double scaleY = (double) maxHeight / imgHeight;
                double scale = Math.min(scaleX, scaleY);
                
                int newWidth = (int) (imgWidth * scale);
                int newHeight = (int) (imgHeight * scale);
                
                ImageIcon scaledImg = new ImageIcon(originalImg.getImage().getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH));
                JLabel imgLabel = new JLabel(scaledImg);
                imgLabel.setBounds((800 - newWidth) / 2, (600 - newHeight) / 2, newWidth, newHeight);
                layeredPane.add(imgLabel, JLayeredPane.DEFAULT_LAYER);
                
                // Add text overlay
                JLabel textLabel = new JLabel("<html><center>hiiii layan<br>go listen to the songs<br>i sent you !!</center></html>");
                textLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
                textLabel.setForeground(new Color(255, 200, 0));
                textLabel.setBounds(50, 420, 700, 150);
                textLabel.setHorizontalAlignment(SwingConstants.CENTER);
                layeredPane.add(textLabel, JLayeredPane.PALETTE_LAYER);
            }
        } catch (Exception e) {
            System.err.println("Could not load image: " + e.getMessage());
        }
        
        dialog.add(layeredPane);
        dialog.setVisible(true);
    }
    
}

