package view;

import java.awt.*;
import javax.swing.*;

public class LoginView {

    public interface LoginController {
        void onLogin(String username, String password);
        void onExit();
    }

    private final JFrame frame;
    private final JTextField userField;
    private final JPasswordField passField;

    public LoginView(LoginController controller) {
        frame = new JFrame("Login");
        frame.setBounds(100, 100, 600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
<<<<<<< Updated upstream
=======
        // set app icon for taskbar and window
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadAppIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }
>>>>>>> Stashed changes

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
        bg.setBounds(0, 0, 600, 500);
        bg.setLayout(null);
        frame.setContentPane(bg);

        // Main container
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setBorder(null);
        card.setBounds(75, 80, 450, 350);
        card.setLayout(null);
        bg.add(card);

        // Title
        JLabel title = new JLabel("Minesweeper Login");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 32));
        title.setBounds(0, 20, card.getWidth(), 50);
        card.add(title);

        // Username label
        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(new Color(170, 220, 200));
        userLbl.setFont(new Font("Tahoma", Font.PLAIN, 14));
        userLbl.setBounds(50, 90, 120, 30);
        card.add(userLbl);

        // Username field
        userField = new JTextField();
        userField.setBounds(50, 120, 350, 40);
        userField.setOpaque(false);
        userField.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 51), 2));
        userField.setForeground(new Color(220, 235, 230));
        userField.setCaretColor(new Color(220, 235, 230));
        card.add(userField);

        // Password label
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(new Color(170, 220, 200));
        passLbl.setFont(new Font("Tahoma", Font.PLAIN, 14));
        passLbl.setBounds(50, 170, 120, 30);
        card.add(passLbl);

        // Password field
        passField = new JPasswordField();
        passField.setBounds(50, 200, 350, 40);
        passField.setOpaque(false);
        passField.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 51), 2));
        passField.setForeground(new Color(220, 235, 230));
        passField.setCaretColor(new Color(220, 235, 230));
        card.add(passField);

        // Buttons
        RoundedButton exitBtn = new RoundedButton("Cancel");
        exitBtn.setBounds(50, 270, 150, 50);
        exitBtn.setBackground(new Color(120, 30, 30));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFont(new Font("Tahoma", Font.PLAIN, 14));
        exitBtn.addActionListener(e -> controller.onExit());
        card.add(exitBtn);

        RoundedButton loginBtn = new RoundedButton("Login");
        loginBtn.setBounds(250, 270, 150, 50);
        loginBtn.setBackground(new Color(20, 120, 70));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Tahoma", Font.PLAIN, 14));
        loginBtn.addActionListener(e ->
                controller.onLogin(
                        userField.getText().trim(),
                        new String(passField.getPassword())
                )
        );
        card.add(loginBtn);

        frame.setVisible(false);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }

    private static class RoundedButton extends JButton {
        private static final int ARC = 16;
        RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getBackground();
            if (bg == null) bg = new Color(40, 60, 55, 220);
            if (getModel().isArmed()) {
                bg = bg.darker();
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        public boolean isOpaque() { return false; }
    }
}