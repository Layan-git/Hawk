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

public class MainMenu {

    private JFrame frame;
    private JLabel statusRight;
    private JLabel versionLeft;
    private MainMenuController controller;

    public static void main(String[] args) {
        EventQueue.invokeLater(controller.Main::new);
    }

    /**
     * @wbp.parser.entryPoint
     */
    public MainMenu(MainMenuController controller) {
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
        frame = new JFrame("Minesweeper V2");
        frame.setBounds(100, 100, W, H);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JPanel bg = new JPanel() {
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

        statusRight = new JLabel("Ready to Play");
        statusRight.setHorizontalAlignment(SwingConstants.RIGHT);
        statusRight.setForeground(new Color(0, 200, 90));
        statusRight.setFont(new Font("Tahoma", Font.PLAIN, 12));
        statusRight.setBounds(W - 200, H - 70, 150, 20);
        bg.add(statusRight);
                        
                        JPanel panel = new JPanel() {
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
                        
                                JLabel title = new JLabel("MINESWEEPER V2");
                                title.setHorizontalAlignment(SwingConstants.CENTER);
                                title.setBounds(0, 89, 300, 44);
                                panel.add(title);
                                title.setForeground(new Color(0, 200, 170));
                                title.setFont(new Font("Tahoma", Font.BOLD, 30));
                                
                                javax.swing.JLabel iconLabel = new javax.swing.JLabel(new ImageIcon(MainMenu.class.getResource("/resources/bomb.png")));
                                iconLabel.setBounds(107, 15, 64, 64);
                                panel.add(iconLabel);
                                
                                        JLabel subtitle = new JLabel("THINK BEFORE YOU CLICK");
                                        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
                                        subtitle.setBounds(0, 143, 300, 20);
                                        panel.add(subtitle);
                                        subtitle.setForeground(new Color(170, 220, 200));
                                        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 14));
                                        
                                                JButton startBtn = new JButton("Start Game");
                                                startBtn.setBounds(36, 345, 220, 40);
                                                panel.add(startBtn);
                                                styleMenuButton(startBtn);
                                                
                                                        JButton historyBtn = new JButton("History");
                                                        historyBtn.setBounds(36, 395, 220, 40);
                                                        panel.add(historyBtn);
                                                        styleMenuButton(historyBtn);
                                                        
                                                                JButton manageBtn = new JButton("Manage Questions");
                                                                manageBtn.setBounds(36, 445, 220, 40);
                                                                panel.add(manageBtn);
                                                                styleMenuButton(manageBtn);
                                                                
                                                                        JButton howBtn = new JButton("How to Play");
                                                                        howBtn.setBounds(36, 495, 220, 40);
                                                                        panel.add(howBtn);
                                                                        styleMenuButton(howBtn);
                                                                        
                                                                                versionLeft = new JLabel("Version 1.0");
                                                                                versionLeft.setBounds(10, 701, 200, 20);
                                                                                panel.add(versionLeft);
                                                                                versionLeft.setForeground(new Color(120, 160, 150));
                                                                                versionLeft.setFont(new Font("Tahoma", Font.PLAIN, 12));
                                                                        howBtn.addActionListener(e -> controller.openHowToPlay());
                                                                manageBtn.addActionListener(e -> controller.openManageQuestions());
                                                        historyBtn.addActionListener(e -> controller.openHistory());
                                                startBtn.addActionListener(e -> controller.startGame());
        
         frame.setIconImage(new ImageIcon("/resources/bomb.png").getImage());   
         }

    private void styleMenuButton(JButton b) {
        b.setForeground(new Color(220, 235, 230));
        b.setFont(new Font("Tahoma", Font.PLAIN, 16));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
    }

}
