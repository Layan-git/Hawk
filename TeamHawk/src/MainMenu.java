package view;

import controller.Main.MainMenuController;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * MainMenu is the main window of the Minesweeper V2 application.
 * <p>
 * It displays the title, logo, and main navigation buttons:
 * Start Game, History, Manage Questions, and How to Play.
 * <br>
 * The class uses a {@link MainMenuController} to handle user actions.
 */
public class MainMenu {

    /**
     * The main frame (window) of the application.
     */
    private JFrame frame;

    /**
     * Status label shown at the bottom-right (e.g., "Ready to Play").
     */
    private JLabel statusRight;

    /**
     * Label showing the version text at the bottom-left of the side panel.
     */
    private JLabel versionLeft;

    /**
     * Controller that handles actions initiated from the Main Menu.
     */
    private MainMenuController controller;

    /**
     * Application entry point.
     * <p>
     * This method starts the application on the Event Dispatch Thread
     * to ensure thread-safe Swing operations.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(controller.Main::new);
    }

    /**
     * Constructs a new {@code MainMenu} with the given controller.
     *
     * @param controller the controller responsible for handling menu actions
     */
    public MainMenu(MainMenuController controller) {
        this.controller = controller;
        initialize();
    }

    /**
     * Shows the main menu window.
     */
    public void show() {
        frame.setVisible(true);
    }

    /**
     * Hides (but does not dispose) the main menu window.
     */
    public void close() {
        frame.setVisible(false);
    }

    /**
     * Initializes all UI components, layouts, and styles of the main menu.
     * <p>
     * This includes:
     * <ul>
     *   <li>Main frame configuration</li>
     *   <li>Background gradient panel</li>
     *   <li>Side panel with title, icon and menu buttons</li>
     *   <li>Status and version labels</li>
     *   <li>Action listeners wired to the controller</li>
     * </ul>
     */
    private void initialize() {
        int W = 1024;
        int H = 768;

        // Main application frame
        frame = new JFrame("Minesweeper V2");
        frame.setBounds(100, 100, W, H);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

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
        bg.setBounds(0, 0, W, H);
        bg.setLayout(null);
        frame.setContentPane(bg);

        // Status label at the bottom-right of the window
        statusRight = new JLabel("Ready to Play");
        statusRight.setHorizontalAlignment(SwingConstants.RIGHT);
        statusRight.setForeground(new Color(0, 200, 90));
        statusRight.setFont(new Font("Tahoma", Font.PLAIN, 12));
        statusRight.setBounds(W - 200, H - 70, 150, 20);
        bg.add(statusRight);

        // Side panel with reversed gradient (menu area)
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color cMain1 = new Color(8, 45, 40);
                Color cMain2 = new Color(5, 80, 60);
                GradientPaint gpRev = new GradientPaint(0, 0, cMain2, getWidth(), getHeight(), cMain1);
                g2.setPaint(gpRev);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBounds(40, 0, 300, 743);
        bg.add(panel);
        panel.setLayout(null);

        // Game title label
        JLabel title = new JLabel("MINESWEEPER V2");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 89, 300, 44);
        panel.add(title);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 30));

        // Game icon (bomb)
        JLabel iconLabel = new JLabel(new ImageIcon(MainMenu.class.getResource("/resources/bomb.png")));
        iconLabel.setBounds(107, 15, 64, 64);
        panel.add(iconLabel);

        // Subtitle / slogan
        JLabel subtitle = new JLabel("THINK BEFORE YOU CLICK");
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setBounds(0, 143, 300, 20);
        panel.add(subtitle);
        subtitle.setForeground(new Color(170, 220, 200));
        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // "Start Game" button
        JButton startBtn = new JButton("Start Game");
        startBtn.setBounds(36, 345, 220, 40);
        panel.add(startBtn);
        styleMenuButton(startBtn);

        // "History" button
        JButton historyBtn = new JButton("History");
        historyBtn.setBounds(36, 395, 220, 40);
        panel.add(historyBtn);
        styleMenuButton(historyBtn);

        // "Manage Questions" button
        JButton manageBtn = new JButton("Manage Questions");
        manageBtn.setBounds(36, 445, 220, 40);
        panel.add(manageBtn);
        styleMenuButton(manageBtn);

        // "How to Play" button
        JButton howBtn = new JButton("How to Play");
        howBtn.setBounds(36, 495, 220, 40);
        panel.add(howBtn);
        styleMenuButton(howBtn);

        // Version label at the bottom of the side panel
        versionLeft = new JLabel("Version 1.0");
        versionLeft.setBounds(10, 701, 200, 20);
        panel.add(versionLeft);
        versionLeft.setForeground(new Color(120, 160, 150));
        versionLeft.setFont(new Font("Tahoma", Font.PLAIN, 12));

        // Wire buttons to controller actions
        howBtn.addActionListener(e -> controller.openHowToPlay());
        manageBtn.addActionListener(e -> controller.openManageQuestions());
        historyBtn.addActionListener(e -> controller.openHistory());
        startBtn.addActionListener(e -> controller.startGame());

        // Set the frame icon (application icon)
        frame.setIconImage(new ImageIcon("/resources/bomb.png").getImage());
    }

    /**
     * Applies common visual styling to main menu buttons.
     * <p>
     * This method sets font, foreground color and removes default button
     * decorations (border, content fill, focus paint) to achieve a
     * flat, clean look that integrates with the gradient background.
     *
     * @param b the button to be styled
     */
    private void styleMenuButton(JButton b) {
        b.setForeground(new Color(220, 235, 230));
        b.setFont(new Font("Tahoma", Font.PLAIN, 16));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
    }

}
