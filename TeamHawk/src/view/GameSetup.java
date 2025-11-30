package view;

import controller.Main.GameSetupController;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

public class GameSetup {

    private JFrame frame;
    private GameSetupController controller;

    private JTextField player1Field;
    private JTextField player2Field;
    private JRadioButton easyBtn;
    private JRadioButton mediumBtn;
    private JRadioButton hardBtn;

    public GameSetup(GameSetupController controller) {
        this.controller = controller;
        initialize();
    }

    public void show() { frame.setVisible(true); }
    public void close() { frame.setVisible(false); }

    private void initialize() {
        int W = 1024;
        int H = 768;

        frame = new JFrame("Game Setup");
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

        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color d1 = new Color(15, 25, 30);
                Color d2 = new Color(15, 35, 45);
                GradientPaint gp = new GradientPaint(0, 0, d1, getWidth(), getHeight(), d2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            }
        };
        card.setBorder(new LineBorder(new Color(51, 102, 51), 4, true));
        card.setBounds(200, 110, 624, 520);
        card.setLayout(null);
        bg.add(card);
        

     // Difficulty descriptions – accurate per your game rules
        JLabel easyLabel = new JLabel("Easy: 9×9 board");
        easyLabel.setForeground(Color.WHITE);
        easyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        easyLabel.setBounds(60, 402, 160, 12);
        card.add(easyLabel);

        JLabel medLabel = new JLabel("Medium: 13×13 board");
        medLabel.setForeground(Color.WHITE);
        medLabel.setHorizontalAlignment(SwingConstants.CENTER);
        medLabel.setBounds(230, 402, 160, 12);
        card.add(medLabel);

        JLabel hardLabel = new JLabel("Hard: 16×16 board");
        hardLabel.setForeground(Color.WHITE);
        hardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hardLabel.setBounds(404, 402, 160, 12);
        card.add(hardLabel);
       
        
        JLabel title = new JLabel("Game Setup");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setBounds(0, 88, 624, 40);
        card.add(title);

        JLabel sub = new JLabel("Configure your game settings");
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        sub.setForeground(new Color(170, 220, 200));
        sub.setFont(new Font("Tahoma", Font.PLAIN, 14));
        sub.setBounds(0, 128, 624, 20);
        card.add(sub);

        JLabel p1 = new JLabel("Player 1");
        p1.setForeground(new Color(170, 220, 200));
        p1.setBounds(60, 170, 200, 20);
        card.add(p1);

        player1Field = new JTextField("");
        player1Field.setBounds(60, 195, 504, 36);
        player1Field.setOpaque(false);
        player1Field.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(0, 102, 51), null));
        player1Field.setForeground(new Color(140, 160, 160));
        player1Field.setCaretColor(new Color(220, 235, 230));
        player1Field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (player1Field.getText().equals(" Enter Player 1 username")) {
                    player1Field.setText("");
                    player1Field.setForeground(new Color(220, 235, 230));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (player1Field.getText().equals("")) {
                    player1Field.setText(" Enter Player 1 username");
                    player1Field.setForeground(new Color(140, 160, 160));
                }
            }
        });
        card.add(player1Field);

        JLabel p2 = new JLabel("Player 2");
        p2.setForeground(new Color(170, 220, 200));
        p2.setBounds(60, 240, 200, 20);
        card.add(p2);

        player2Field = new JTextField("");
        player2Field.setBounds(60, 265, 504, 36);
        player2Field.setOpaque(false);
        player2Field.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(0, 102, 51), null));
        player2Field.setForeground(new Color(140, 160, 160));
        player2Field.setCaretColor(new Color(220, 235, 230));
        player2Field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (player2Field.getText().equals(" Enter Player 2 username")) {
                    player2Field.setText("");
                    player2Field.setForeground(new Color(220, 235, 230));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (player2Field.getText().equals("")) {
                    player2Field.setText(" Enter Player 2 username");
                    player2Field.setForeground(new Color(140, 160, 160));
                }
            }
        });
        card.add(player2Field);

        JLabel diffLbl = new JLabel("Select Difficulty");
        diffLbl.setForeground(new Color(170, 220, 200));
        diffLbl.setBounds(60, 312, 200, 20);
        card.add(diffLbl);

        ButtonGroup grp = new ButtonGroup();
        easyBtn = new JRadioButton("Easy");
        easyBtn.setHorizontalAlignment(SwingConstants.CENTER);
        mediumBtn = new JRadioButton("Medium");
        mediumBtn.setHorizontalAlignment(SwingConstants.CENTER);
        hardBtn = new JRadioButton("Hard");
        hardBtn.setHorizontalAlignment(SwingConstants.CENTER);
        easyBtn.setBounds(60, 336, 160, 60);
        mediumBtn.setBounds(232, 336, 160, 60);
        hardBtn.setBounds(404, 336, 160, 60);
        java.awt.Color green = new java.awt.Color(20, 120, 70);
        java.awt.Color orange = new java.awt.Color(200, 120, 0);
        java.awt.Color red = new java.awt.Color(160, 45, 45);
        easyBtn.setForeground(java.awt.Color.white);
        mediumBtn.setForeground(java.awt.Color.white);
        hardBtn.setForeground(java.awt.Color.white);
        easyBtn.setBackground(green);
        mediumBtn.setBackground(orange);
        hardBtn.setBackground(red);
        easyBtn.setOpaque(true);
        mediumBtn.setOpaque(true);
        hardBtn.setOpaque(true);
        easyBtn.setFocusPainted(false);
        mediumBtn.setFocusPainted(false);
        hardBtn.setFocusPainted(false);
        grp.add(easyBtn);
        grp.add(mediumBtn);
        grp.add(hardBtn);
        card.add(easyBtn);
        card.add(mediumBtn);
        card.add(hardBtn);
        mediumBtn.setSelected(true);

        JButton cancelBtn = new JButton("Cancel");
        styleTextOnly(cancelBtn);
        cancelBtn.setBounds(60, 440, 120, 36);
        cancelBtn.addActionListener(e -> controller.backToMenu());
        card.add(cancelBtn);

        JButton startBtn = new JButton("Start Game");
        styleTextOnly(startBtn);
        startBtn.setBounds(444, 440, 120, 36);
        startBtn.addActionListener(e -> controller.confirmStart(
                player1Field.getText(),
                player2Field.getText(),
                selectedDifficulty()));
        card.add(startBtn);
    }

    private model.Board.Difficulty selectedDifficulty() {
        if (easyBtn.isSelected()) return model.Board.Difficulty.EASY;
        if (hardBtn.isSelected()) return model.Board.Difficulty.HARD;
        return model.Board.Difficulty.MEDIUM;
    }

    private void styleTextOnly(JButton b) {
        b.setForeground(new Color(220, 235, 230));
        b.setFont(new Font("Tahoma", Font.PLAIN, 16));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
    }
}
