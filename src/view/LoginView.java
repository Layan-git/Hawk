package view;

import javax.swing.*;
import java.awt.*;

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
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        main.setBackground(new Color(15, 25, 30));
        frame.setContentPane(main);

        JLabel title = new JLabel("Minesweeper Login", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 22));
        title.setForeground(new Color(0, 200, 255));
        main.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 2, 8, 8));
        center.setOpaque(false);

        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(Color.WHITE);
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.WHITE);

        userField = new JTextField();
        passField = new JPasswordField();

        center.add(userLbl);
        center.add(userField);
        center.add(passLbl);
        center.add(passField);

        main.add(center, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Login");
        JButton exitBtn = new JButton("Exit");

        loginBtn.addActionListener(e ->
                controller.onLogin(
                        userField.getText().trim(),
                        new String(passField.getPassword())
                )
        );
        exitBtn.addActionListener(e -> controller.onExit());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(exitBtn);
        bottom.add(loginBtn);
        main.add(bottom, BorderLayout.SOUTH);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void close() {
        frame.dispose();
    }
}
