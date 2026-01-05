package view;

import controller.Main.GameSetupController;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import model.ResourceLoader;

public class GameSetup {

    private JFrame frame;
    private final GameSetupController controller;

    private JTextField player1Field;
    private JTextField player2Field;
    private JRadioButton easyBtn;
    private JRadioButton mediumBtn;
    private JRadioButton hardBtn;
    
    // Character selection
    private static final String[] CHARACTER_ICONS = {
        "/resources/bomb.png", "/resources/gift.png", "/resources/net.png",
        "/resources/metaldetector.png", "/resources/question.png", "/resources/exit.png"
    };
    private static final int NUM_CHARACTERS = 6;
    private BufferedImage[] characterImages = new BufferedImage[NUM_CHARACTERS];
    private JButton[][] characterButtons = new JButton[2][NUM_CHARACTERS];
    private int player1SelectedChar = -1;
    private int player2SelectedChar = -1;

    // this screen only collects names + difficulty, then notifies controller
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
        
        // Load character icons
        loadCharacterIcons();

        // background panel with gradient (visual only)
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

        // container for controls — make it transparent so everything sits on the bg
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setBorder(null);
        card.setBounds(120, 80, 784, 600);
        card.setLayout(null);
        bg.add(card);
        // Title at top-center
        JLabel title = new JLabel("Game Type: Classic");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setBounds(0, 12, card.getWidth(), 36);
        card.add(title);

        // Left player area (top-left)
        JPanel leftArea = new JPanel(null);
        leftArea.setOpaque(false);
        leftArea.setBounds(24, 64, 340, 240);
        card.add(leftArea);

        JLabel p1 = new JLabel("Player 1");
        p1.setForeground(new Color(170, 220, 200));
        p1.setBounds(8, 0, 200, 20);
        leftArea.add(p1);

        player1Field = new JTextField("");
        player1Field.setBounds(8, 26, 324, 36);
        player1Field.setOpaque(false);
        player1Field.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(0, 102, 51), null));
        player1Field.setForeground(new Color(140, 160, 160));
        player1Field.setCaretColor(new Color(220, 235, 230));
        player1Field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (player1Field.getText().equals(" Enter Player 1 username")) {
                    player1Field.setText("");
                    player1Field.setForeground(new Color(220, 235, 230));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (player1Field.getText().equals("")) {
                    player1Field.setText(" Enter Player 1 username");
                    player1Field.setForeground(new Color(140, 160, 160));
                }
            }
        });
        leftArea.add(player1Field);

        // 6 character icon buttons under player1 (2 rows of 3)
        int icW = 96; int icH = 56; int gap = 8;
        for (int c = 0; c < NUM_CHARACTERS; c++) {
            final int charIndex = c;
            RoundedButton ib = new RoundedButton("");
            int row = c / 3;
            int col = c % 3;
            ib.setBounds(8 + col * (icW + gap), 76 + row * (icH + gap), icW, icH);
            if (characterImages[c] != null) {
                Image scaledImage = characterImages[c].getScaledInstance(icW - 8, icH - 8, Image.SCALE_SMOOTH);
                ib.setIcon(new ImageIcon(scaledImage));
            }
            ib.setFocusPainted(false);
            ib.setBorder(new LineBorder(new Color(80, 150, 120), 2));
            ib.addActionListener(e -> selectCharacter(0, charIndex, ib));
            characterButtons[0][c] = ib;
            leftArea.add(ib);
        }

        // Right player area (top-right)
        JPanel rightArea = new JPanel(null);
        rightArea.setOpaque(false);
        rightArea.setBounds(card.getWidth() - 364, 64, 340, 240);
        card.add(rightArea);

        JLabel p2 = new JLabel("Player 2");
        p2.setForeground(new Color(170, 220, 200));
        p2.setBounds(8, 0, 200, 20);
        rightArea.add(p2);

        player2Field = new JTextField("");
        player2Field.setBounds(8, 26, 324, 36);
        player2Field.setOpaque(false);
        player2Field.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(0, 102, 51), null));
        player2Field.setForeground(new Color(140, 160, 160));
        player2Field.setCaretColor(new Color(220, 235, 230));
        player2Field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (player2Field.getText().equals(" Enter Player 2 username")) {
                    player2Field.setText("");
                    player2Field.setForeground(new Color(220, 235, 230));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (player2Field.getText().equals("")) {
                    player2Field.setText(" Enter Player 2 username");
                    player2Field.setForeground(new Color(140, 160, 160));
                }
            }
        });
        rightArea.add(player2Field);

        for (int c = 0; c < NUM_CHARACTERS; c++) {
            final int charIndex = c;
            RoundedButton ib = new RoundedButton("");
            int row = c / 3;
            int col = c % 3;
            ib.setBounds(8 + col * (icW + gap), 76 + row * (icH + gap), icW, icH);
            if (characterImages[c] != null) {
                Image scaledImage = characterImages[c].getScaledInstance(icW - 8, icH - 8, Image.SCALE_SMOOTH);
                ib.setIcon(new ImageIcon(scaledImage));
            }
            ib.setFocusPainted(false);
            ib.setBorder(new LineBorder(new Color(100, 120, 180), 2));
            ib.addActionListener(e -> selectCharacter(1, charIndex, ib));
            characterButtons[1][c] = ib;
            rightArea.add(ib);
        }

        // Difficulty area (middle, full-width)
        JLabel diffLbl = new JLabel("Select Difficulty");
        diffLbl.setForeground(new Color(170, 220, 200));
        diffLbl.setBounds(0, 320, card.getWidth(), 20);
        diffLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(diffLbl);

        // radio buttons inside colored menu panels with descriptions
        ButtonGroup grp = new ButtonGroup();
        easyBtn = new JRadioButton("Easy");
        mediumBtn = new JRadioButton("Medium");
        hardBtn = new JRadioButton("Hard");
        easyBtn.setHorizontalAlignment(SwingConstants.CENTER);
        mediumBtn.setHorizontalAlignment(SwingConstants.CENTER);
        hardBtn.setHorizontalAlignment(SwingConstants.CENTER);
        java.awt.Color green = new java.awt.Color(20, 120, 70);
        java.awt.Color orange = new java.awt.Color(200, 120, 0);
        java.awt.Color red = new java.awt.Color(160, 45, 45);

        int w = 160, h = 80;
        JPanel easyContainer = new JPanel(null);
        easyContainer.setBounds(120, 344, w, h);
        easyContainer.setBackground(green);
        easyContainer.setOpaque(true);
        easyContainer.putClientProperty("baseColor", green);
        card.add(easyContainer);

        JPanel medContainer = new JPanel(null);
        medContainer.setBounds(312, 344, w, h);
        medContainer.setBackground(orange);
        medContainer.setOpaque(true);
        medContainer.putClientProperty("baseColor", orange);
        card.add(medContainer);

        JPanel hardContainer = new JPanel(null);
        hardContainer.setBounds(504, 344, w, h);
        hardContainer.setBackground(red);
        hardContainer.setOpaque(true);
        hardContainer.putClientProperty("baseColor", red);
        card.add(hardContainer);

        // configure radios and add into containers
        easyBtn.setBounds((w-100)/2, 8, 100, 28);
        mediumBtn.setBounds((w-100)/2, 8, 100, 28);
        hardBtn.setBounds((w-100)/2, 8, 100, 28);
        easyBtn.setOpaque(false);
        mediumBtn.setOpaque(false);
        hardBtn.setOpaque(false);
        easyBtn.setForeground(Color.WHITE);
        mediumBtn.setForeground(Color.WHITE);
        hardBtn.setForeground(Color.WHITE);
        easyBtn.setFocusPainted(false);
        mediumBtn.setFocusPainted(false);
        hardBtn.setFocusPainted(false);

        // description labels inside each container
        JLabel easyInfo = new JLabel("9×9 board : 10 Bombs");
        easyInfo.setForeground(Color.WHITE);
        easyInfo.setHorizontalAlignment(SwingConstants.CENTER);
        easyInfo.setBounds(0, 44, w, 14);
        easyContainer.add(easyBtn);
        easyContainer.add(easyInfo);

        JLabel medInfo = new JLabel("13×13 board : 26 Bombs");
        medInfo.setForeground(Color.WHITE);
        medInfo.setHorizontalAlignment(SwingConstants.CENTER);
        medInfo.setBounds(0, 44, w, 14);
        medContainer.add(mediumBtn);
        medContainer.add(medInfo);

        JLabel hardInfo = new JLabel("16×16 board : 44 Bombs");
        hardInfo.setForeground(Color.WHITE);
        hardInfo.setHorizontalAlignment(SwingConstants.CENTER);
        hardInfo.setBounds(0, 44, w, 14);
        hardContainer.add(hardBtn);
        hardContainer.add(hardInfo);

        grp.add(easyBtn);
        grp.add(mediumBtn);
        grp.add(hardBtn);

        // selection visual: highlight selected panel
        easyBtn.addItemListener(e -> {
            if (easyBtn.isSelected()) updateSelectionVisuals(easyContainer, medContainer, hardContainer);
        });
        mediumBtn.addItemListener(e -> {
            if (mediumBtn.isSelected()) updateSelectionVisuals(medContainer, easyContainer, hardContainer);
        });
        hardBtn.addItemListener(e -> {
            if (hardBtn.isSelected()) updateSelectionVisuals(hardContainer, easyContainer, medContainer);
        });

        mediumBtn.setSelected(true);  // default difficulty
        // initialize visuals
        updateSelectionVisuals(medContainer, easyContainer, hardContainer);

        // bottom left cancel and bottom right start buttons
        RoundedButton cancelBtn = new RoundedButton("Cancel");
        cancelBtn.setBounds(40, 508, 140, 44);
        cancelBtn.setBackground(new Color(120, 30, 30));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> controller.backToMenu());
        card.add(cancelBtn);

        RoundedButton startBtn = new RoundedButton("Start Game");
        startBtn.setBounds( card.getBounds().width - 180, 508, 140, 44);
        startBtn.setBackground(new Color(20, 120, 70));
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> {
            if (player1SelectedChar >= 0 && player2SelectedChar >= 0) {
                controller.confirmStart(
                    player1Field.getText(),
                    player2Field.getText(),
                    selectedDifficulty(),
                    player1SelectedChar,
                    player2SelectedChar);
            }
        });
        card.add(startBtn);
    }

    // convert selected radio button into your Board.Difficulty enum
    private model.Board.Difficulty selectedDifficulty() {
        if (easyBtn.isSelected()) return model.Board.Difficulty.EASY;
        if (hardBtn.isSelected()) return model.Board.Difficulty.HARD;
        return model.Board.Difficulty.MEDIUM;
    }
    
    // Load character icons from resources
    private void loadCharacterIcons() {
        for (int i = 0; i < NUM_CHARACTERS; i++) {
            try {
                java.net.URL iconUrl = GameSetup.class.getResource(CHARACTER_ICONS[i]);
                if (iconUrl != null) {
                    characterImages[i] = ImageIO.read(iconUrl);
                } else {
                    String iconPath = ResourceLoader.getResourcePath(CHARACTER_ICONS[i]);
                    if (iconPath != null && !iconPath.isEmpty()) {
                        characterImages[i] = ImageIO.read(new File(iconPath));
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load character icon: " + CHARACTER_ICONS[i]);
            }
        }
    }
    
    // Handle character selection with visual feedback
    private void selectCharacter(int player, int charIndex, JButton btn) {
        if (player == 0) {
            // Player 1 selection
            if (charIndex == player2SelectedChar) {
                JOptionPane.showMessageDialog(frame, 
                    "Player 2 already selected this character!", 
                    "Invalid Selection", 
                    JOptionPane.ERROR_MESSAGE);
                return; // Can't select same character as player 2
            }
            player1SelectedChar = charIndex;
            // Update visual feedback
            for (int i = 0; i < NUM_CHARACTERS; i++) {
                if (i == charIndex) {
                    characterButtons[0][i].setBorder(new LineBorder(new Color(0, 255, 100), 4));
                } else {
                    characterButtons[0][i].setBorder(new LineBorder(new Color(80, 150, 120), 2));
                }
            }
        } else {
            // Player 2 selection
            if (charIndex == player1SelectedChar) {
                JOptionPane.showMessageDialog(frame, 
                    "Player 1 already selected this character!", 
                    "Invalid Selection", 
                    JOptionPane.ERROR_MESSAGE);
                return; // Can't select same character as player 1
            }
            player2SelectedChar = charIndex;
            // Update visual feedback
            for (int i = 0; i < NUM_CHARACTERS; i++) {
                if (i == charIndex) {
                    characterButtons[1][i].setBorder(new LineBorder(new Color(0, 255, 100), 4));
                } else {
                    characterButtons[1][i].setBorder(new LineBorder(new Color(100, 120, 180), 2));
                }
            }
        }
    }

    // shared style for text-only buttons at the bottom
    @SuppressWarnings("unused")
    private void styleTextOnly(JButton b) {
        b.setForeground(new Color(220, 235, 230));
        b.setFont(new Font("Tahoma", Font.PLAIN, 16));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
    }

    // update visual state for difficulty containers
    private void updateSelectionVisuals(JPanel selected, JPanel... others) {
        Color base = (Color) selected.getClientProperty("baseColor");
        if (base == null) base = selected.getBackground();
        selected.setBackground(base.brighter());
        selected.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
        selected.repaint();
        for (JPanel p : others) {
            Color b = (Color) p.getClientProperty("baseColor");
            if (b == null) b = p.getBackground();
            p.setBackground(b);
            p.setBorder(BorderFactory.createEmptyBorder());
            p.repaint();
        }
    }

    // simple rounded button used as placeholder and for Cancel/Start
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
