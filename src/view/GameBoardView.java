package view;

import controller.Main.GameBoardController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import model.Cell;
import model.ResourceLoader;

public class GameBoardView {

    // Timer fields
    private Timer gameTimer;
    private int elapsedSeconds = 0;
    private JLabel timerLabel;

    private JFrame frame;
    private final GameBoardController controller;

    private JPanel boardPanel1;
    private JPanel boardPanel2;

    private final JButton[][] cellButtons1;
    private final JButton[][] cellButtons2;

    private JLabel player1NameLabel;
    private JLabel player2NameLabel;
    private JLabel scoreLabel;
    private JPanel livesPanel;  // Changed from JLabel to JPanel for icon-based display
    @SuppressWarnings("unused")
    private JLabel currentTurnLabel;
    private JLabel statusLabel;
    // Timer label is added to info panel
    // ...existing code...

    // Containers + mines-left
    private JPanel player1Container;
    private JPanel player2Container;
    private JLabel player1MinesLeftLabel;
    private JLabel player2MinesLeftLabel;
    
    // Shop UI elements
    private JButton safetyNetButton;
    private JButton metalDetectorButton;
    private JLabel momentumLabel;
    private JLabel shopStatusLabel;
    private JLabel metalDetectorTimerLabel;

    private final int boardSize;
    
    // Character icons
    private static final String[] CHARACTER_ICON_PATHS = {
        "/resources/bomb.png", "/resources/gift.png", "/resources/net.png",
        "/resources/metaldetector.png", "/resources/question.png", "/resources/exit.png"
    };
    private BufferedImage player1CharIcon;
    private BufferedImage player2CharIcon;
    private JLabel player1CharLabel;
    private JLabel player2CharLabel;
    private static final int CHARACTER_DISPLAY_SIZE = 40;
    
    // Store board references for metal detector
    private model.Board board1;
    private model.Board board2;
    
    // Health bar icon
    private BufferedImage healthIcon;
    private static final int ICON_SIZE = 24;
    
    // Game cell icons
    private BufferedImage bombIcon;
    private BufferedImage flagIcon;
    private BufferedImage questionIcon;
    private BufferedImage surpriseIcon;
    private BufferedImage metalDetectorIcon;
    private BufferedImage safetyNetIcon;
    private BufferedImage pauseIcon;
    private BufferedImage exitIcon;
    private static final int CELL_ICON_SIZE = 32;
    private static final int QUESTION_ICON_SIZE = 24;
    private static final int BUTTON_ICON_SIZE = 20;

    // Colors
    private static final Color COLOR_HIDDEN = new Color(60, 80, 95);
    private static final Color COLOR_MINE = new Color(220, 50, 50);
    private static final Color COLOR_SURPRISE = new Color(180, 100, 255);
    private static final Color COLOR_SURPRISE_DARK = new Color(120, 60, 200);  // darker S after pass
    private static final Color COLOR_QUESTION = new Color(255, 215, 0);
    private static final Color COLOR_QUESTION_ATTEMPTED = new Color(200, 170, 0);
    private static final Color COLOR_SAFE = new Color(140, 200, 140);
    private static final Color COLOR_FLAGGED = new Color(90, 110, 130);

    // Neon theme
    private static final Color NEON_GREEN = new Color(57, 255, 20); // P1
    private static final Color NEON_ORANGE = new Color(255, 140, 0); // P2
    private static final Color DIM_COLOR = new Color(10, 20, 30);
    private static final Color DIM_BORDER_P1_COL = new Color(51, 153, 102, 100);
    private static final Color DIM_BORDER_P2_COL = new Color(153, 102, 51, 100);
    private static final Border INACTIVE_BORDER_P1 = new LineBorder(DIM_BORDER_P1_COL, 4, true);
    private static final Border INACTIVE_BORDER_P2 = new LineBorder(DIM_BORDER_P2_COL, 4, true);
    private static final Border ACTIVE_BORDER_P1_NEON = new LineBorder(NEON_GREEN, 5, true);
    private static final Border ACTIVE_BORDER_P2_NEON = new LineBorder(NEON_ORANGE, 5, true);

    // ---------- ctor ----------

    /**
     * @wbp.parser.entryPoint
     */
    public GameBoardView(GameBoardController controller, String player1Name, String player2Name, int boardSize, int player1CharIndex, int player2CharIndex) {
        this.controller = controller;
        // if boardSize is 0 we default to 9x9
        this.boardSize = (boardSize == 0) ? 9 : boardSize;
        this.cellButtons1 = new JButton[this.boardSize][this.boardSize];
        this.cellButtons2 = new JButton[this.boardSize][this.boardSize];
        loadCharacterIcons(player1CharIndex, player2CharIndex);
        initialize(player1Name, player2Name);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }

    // ---------- UI init ----------

    private void initialize(String p1Name, String p2Name) {
        frame = new JFrame("Minesweeper - Two Player Game");
        frame.setSize(1600, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));

        // Load all icons before creating UI
        loadHealthIcon();
        loadGameIcons();

        // mainPanel just draws the gradient background for the whole game
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = new Color(8, 45, 40);
                Color c2 = new Color(5, 80, 60);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(0, 15));
        frame.setContentPane(mainPanel);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        topWrapper.add(createInfoPanel(), BorderLayout.CENTER);
        mainPanel.add(topWrapper, BorderLayout.NORTH);

        // Timer setup
        setupGameTimer();

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 10, 0, 10);

        // Character icons header - appears above the boards, outside the neon borders
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        JPanel p1CharHeaderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        p1CharHeaderPanel.setOpaque(false);
        player1CharLabel = new JLabel();
        if (player1CharIcon != null) {
            Image scaledChar = player1CharIcon.getScaledInstance(CHARACTER_DISPLAY_SIZE, CHARACTER_DISPLAY_SIZE, Image.SCALE_SMOOTH);
            player1CharLabel.setIcon(new ImageIcon(scaledChar));
        }
        p1CharHeaderPanel.add(player1CharLabel);
        centerPanel.add(p1CharHeaderPanel, gbc);

        gbc.gridx = 1;
        JPanel p2CharHeaderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        p2CharHeaderPanel.setOpaque(false);
        player2CharLabel = new JLabel();
        if (player2CharIcon != null) {
            Image scaledChar = player2CharIcon.getScaledInstance(CHARACTER_DISPLAY_SIZE, CHARACTER_DISPLAY_SIZE, Image.SCALE_SMOOTH);
            player2CharLabel.setIcon(new ImageIcon(scaledChar));
        }
        p2CharHeaderPanel.add(player2CharLabel);
        centerPanel.add(p2CharHeaderPanel, gbc);

        // Reset weighty for board containers
        gbc.weighty = 1.0;

        // Player 1 board + labels
        gbc.gridx = 0;
        gbc.gridy = 1;
        player1Container = new JPanel(new BorderLayout(0, 8));
        player1Container.setOpaque(false);
        // border color will change on active turn
        player1Container.setBorder(INACTIVE_BORDER_P1);

        player1NameLabel = new JLabel(p1Name + "'s Board", SwingConstants.CENTER);
        player1NameLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        player1NameLabel.setForeground(Color.WHITE);
        player1Container.add(player1NameLabel, BorderLayout.NORTH);

        player1MinesLeftLabel = new JLabel("Mines Left: ?", SwingConstants.CENTER);
        player1MinesLeftLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        player1MinesLeftLabel.setForeground(Color.LIGHT_GRAY);
        player1Container.add(player1MinesLeftLabel, BorderLayout.SOUTH);

        JPanel board1Wrapper = new JPanel(new GridBagLayout());
        board1Wrapper.setOpaque(false);

        boardPanel1 = new JPanel(new GridLayout(boardSize, boardSize, 1, 1));
        boardPanel1.setBackground(new Color(20, 20, 20));
        boardPanel1.setBorder(new LineBorder(new Color(30, 30, 30), 2));

        GridBagConstraints innerGbc = new GridBagConstraints();
        innerGbc.fill = GridBagConstraints.BOTH;
        innerGbc.weightx = 1.0;
        innerGbc.weighty = 1.0;
        board1Wrapper.add(boardPanel1, innerGbc);

        player1Container.add(board1Wrapper, BorderLayout.CENTER);
        // create all buttons for player 1 and hook up mouse events to controller
        initializeBoardButtons(boardPanel1, cellButtons1, 1);
        centerPanel.add(player1Container, gbc);

        // Player 2 board + labels (same layout on right side)
        gbc.gridx = 1;
        gbc.gridy = 1;
        player2Container = new JPanel(new BorderLayout(0, 8));
        player2Container.setOpaque(false);
        player2Container.setBorder(INACTIVE_BORDER_P2);

        player2NameLabel = new JLabel(p2Name + "'s Board", SwingConstants.CENTER);
        player2NameLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        player2NameLabel.setForeground(Color.WHITE);
        player2Container.add(player2NameLabel, BorderLayout.NORTH);

        player2MinesLeftLabel = new JLabel("Mines Left: ?", SwingConstants.CENTER);
        player2MinesLeftLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        player2MinesLeftLabel.setForeground(Color.LIGHT_GRAY);
        player2Container.add(player2MinesLeftLabel, BorderLayout.SOUTH);

        JPanel board2Wrapper = new JPanel(new GridBagLayout());
        board2Wrapper.setOpaque(false);

        boardPanel2 = new JPanel(new GridLayout(boardSize, boardSize, 1, 1));
        boardPanel2.setBackground(new Color(20, 20, 20));
        boardPanel2.setBorder(new LineBorder(new Color(30, 30, 30), 2));
        board2Wrapper.add(boardPanel2, innerGbc);

        player2Container.add(board2Wrapper, BorderLayout.CENTER);
        // same for player 2 board
        initializeBoardButtons(boardPanel2, cellButtons2, 2);

        centerPanel.add(player2Container, gbc);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // bottom panel with shop, controls, and status
        JPanel bottomWrapper = new JPanel(new BorderLayout(0, 5));
        bottomWrapper.setOpaque(false);
        
        // Shop panel (top of bottom section)
        JPanel shopPanel = createShopPanel();
        bottomWrapper.add(shopPanel, BorderLayout.NORTH);

        // Controls panel with status text (pause/quit buttons moved to HUD)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setOpaque(false);

        statusLabel = new JLabel("Game Started!");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        statusLabel.setForeground(new Color(0, 255, 150));
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(statusLabel);

        bottomWrapper.add(bottomPanel, BorderLayout.CENTER);
        mainPanel.add(bottomWrapper, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(1200, 750));
        // start visuals assuming player 1 goes first
        updateTurnVisuals(1);
    }
    
    // Create the shop panel with buy buttons and status
    private JPanel createShopPanel() {
        JPanel shopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        shopPanel.setOpaque(false);
        
        // Momentum Multiplier display
        JPanel momentumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        momentumPanel.setOpaque(false);
        JLabel momentumTitle = new JLabel("Momentum:");
        momentumTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
        momentumTitle.setForeground(new Color(255, 215, 0));
        momentumPanel.add(momentumTitle);
        
        momentumLabel = new JLabel("0 streak");
        momentumLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        momentumLabel.setForeground(Color.WHITE);
        momentumPanel.add(momentumLabel);
        shopPanel.add(momentumPanel);
        
        shopPanel.add(Box.createHorizontalStrut(20));
        
        // Shop title
        JLabel shopTitle = new JLabel("SHOP");
        shopTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
        shopTitle.setForeground(new Color(0, 255, 200));
        shopPanel.add(shopTitle);
        
        // Safety Net button
        safetyNetButton = new JButton("Safety Net (10 pts)");
        if (safetyNetIcon != null) {
            Image scaledNetIcon = safetyNetIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            safetyNetButton.setIcon(new ImageIcon(scaledNetIcon));
        }
        safetyNetButton.setFont(new Font("Tahoma", Font.BOLD, 12));
        safetyNetButton.setForeground(new Color(220, 235, 230));
        safetyNetButton.setBackground(new Color(40, 100, 80));
        safetyNetButton.setFocusPainted(false);
        safetyNetButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(80, 150, 120), 2, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        safetyNetButton.setEnabled(false); // disabled by default
        shopPanel.add(safetyNetButton);
        
        // Metal Detector button
        metalDetectorButton = new JButton("Metal Detector (15 pts)");
        if (metalDetectorIcon != null) {
            Image scaledDetectorIcon = metalDetectorIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            metalDetectorButton.setIcon(new ImageIcon(scaledDetectorIcon));
        }
        metalDetectorButton.setFont(new Font("Tahoma", Font.BOLD, 12));
        metalDetectorButton.setForeground(new Color(220, 235, 230));
        metalDetectorButton.setBackground(new Color(60, 80, 120));
        metalDetectorButton.setFocusPainted(false);
        metalDetectorButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 120, 180), 2, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        metalDetectorButton.setEnabled(false); // disabled by default
        shopPanel.add(metalDetectorButton);
        
        // Shop status label (shows active items)
        shopStatusLabel = new JLabel("");
        shopStatusLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        shopStatusLabel.setForeground(new Color(255, 255, 100));
        shopPanel.add(shopStatusLabel);
        
        // Metal Detector timer display
        metalDetectorTimerLabel = new JLabel("");
        metalDetectorTimerLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        metalDetectorTimerLabel.setForeground(new Color(255, 100, 100));
        shopPanel.add(metalDetectorTimerLabel);
        
        return shopPanel;
    }

    private JPanel createInfoPanel() {
        // Load health icon
        loadHealthIcon();
        
        // top bar that shows score, lives and current turn
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                        0, 0,
                        new Color(15, 25, 30),
                        getWidth(), getHeight(),
                        new Color(15, 35, 45)
                ));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            }
        };

        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 20));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(1000, 90));

        JPanel scorePanel = createInfoItem("Score:", "0", new Color(0, 255, 128));
        scoreLabel = (JLabel) scorePanel.getComponent(1);
        panel.add(scorePanel);

        // Lives panel with icons instead of text
        JPanel livesPanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        livesPanelWrapper.setOpaque(false);
        JLabel livesTitle = new JLabel("Lives:");
        livesTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
        livesTitle.setForeground(new Color(100, 255, 100));
        livesPanelWrapper.add(livesTitle);
        
        livesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        livesPanel.setOpaque(false);
        livesPanelWrapper.add(livesPanel);
        panel.add(livesPanelWrapper);

        // Timer label
        timerLabel = new JLabel("Time: 0:00");
        timerLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        timerLabel.setForeground(new Color(0, 200, 255));
        panel.add(timerLabel);
        
        // Add separator
        panel.add(Box.createHorizontalStrut(30));
        
        // Pause button
        JButton pauseBtn = new JButton("Pause");
        if (pauseIcon != null) {
            Image scaledPauseIcon = pauseIcon.getScaledInstance(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, Image.SCALE_SMOOTH);
            pauseBtn.setIcon(new ImageIcon(scaledPauseIcon));
        }
        pauseBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        pauseBtn.setForeground(new Color(220, 235, 230));
        pauseBtn.setBackground(new Color(100, 60, 40));
        pauseBtn.setFocusPainted(false);
        pauseBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(150, 100, 80), 2, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        pauseBtn.addActionListener(e -> controller.pauseGame());
        panel.add(pauseBtn);
        
        // Exit button
        JButton exitBtn = new JButton("Exit");
        if (exitIcon != null) {
            Image scaledExitIcon = exitIcon.getScaledInstance(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, Image.SCALE_SMOOTH);
            exitBtn.setIcon(new ImageIcon(scaledExitIcon));
        }
        exitBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        exitBtn.setForeground(new Color(220, 235, 230));
        exitBtn.setBackground(new Color(100, 40, 40));
        exitBtn.setFocusPainted(false);
        exitBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(150, 80, 80), 2, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        exitBtn.addActionListener(e -> controller.quitToMenu());
        panel.add(exitBtn);

        return panel;
    }

    // Load the health icon from resources
    private void loadHealthIcon() {
        try {
            // Try classpath first (for JAR)
            java.net.URL iconUrl = GameBoardView.class.getResource("/resources/poisoned_hardcore_full.png");
            if (iconUrl != null) {
                healthIcon = ImageIO.read(iconUrl);
            } else {
                // Fallback to file system (for IDE)
                String iconPath = ResourceLoader.getResourcePath("/resources/poisoned_hardcore_full.png");
                if (iconPath != null && !iconPath.isEmpty()) {
                    healthIcon = ImageIO.read(new File(iconPath));
                } else {
                    System.err.println("Health icon not found");
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load health icon: " + e.getMessage());
        }
    }

    // Load all game cell icons from resources
    private void loadGameIcons() {
        try {
            bombIcon = loadIcon("/resources/bomb.png");
            flagIcon = loadIcon("/resources/falg.png");
            questionIcon = loadIcon("/resources/question.png");
            surpriseIcon = loadIcon("/resources/gift.png");
            metalDetectorIcon = loadIcon("/resources/metaldetector.png");
            safetyNetIcon = loadIcon("/resources/net.png");
            pauseIcon = loadIcon("/resources/gears.png");
            exitIcon = loadIcon("/resources/exit.png");
        } catch (Exception e) {
            System.err.println("Could not load game icons: " + e.getMessage());
        }
    }
    
    // Load character icons for both players
    private void loadCharacterIcons(int player1CharIndex, int player2CharIndex) {
        try {
            if (player1CharIndex >= 0 && player1CharIndex < CHARACTER_ICON_PATHS.length) {
                player1CharIcon = loadIcon(CHARACTER_ICON_PATHS[player1CharIndex]);
            }
            if (player2CharIndex >= 0 && player2CharIndex < CHARACTER_ICON_PATHS.length) {
                player2CharIcon = loadIcon(CHARACTER_ICON_PATHS[player2CharIndex]);
            }
        } catch (Exception e) {
            System.err.println("Could not load character icons: " + e.getMessage());
        }
    }
    
    // Helper method to load a single icon
    private BufferedImage loadIcon(String resourcePath) throws IOException {
        // Try classpath first (for JAR)
        java.net.URL iconUrl = GameBoardView.class.getResource(resourcePath);
        if (iconUrl != null) {
            return ImageIO.read(iconUrl);
        }
        
        // Fallback to file system (for IDE)
        String iconPath = ResourceLoader.getResourcePath(resourcePath);
        if (iconPath != null && !iconPath.isEmpty()) {
            return ImageIO.read(new File(iconPath));
        }
        
        System.err.println("Icon not found: " + resourcePath);
        return null;
    }

    // Timer setup and control
    private void setupGameTimer() {
        gameTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            int min = elapsedSeconds / 60;
            int sec = elapsedSeconds % 60;
            timerLabel.setText(String.format("Time: %d:%02d", min, sec));
        });
        elapsedSeconds = 0;
        timerLabel.setText("Time: 0:00");
        gameTimer.start();
    }

    public void pauseTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    public void resumeTimer() {
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    public void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void resetTimer() {
        elapsedSeconds = 0;
        timerLabel.setText("Time: 0:00");
    }

    private JPanel createInfoItem(String title, String value, Color valueColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
        titleLabel.setForeground(new Color(170, 220, 200));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Tahoma", Font.BOLD, 26));
        valueLabel.setForeground(valueColor);

        panel.add(titleLabel);
        panel.add(valueLabel);

        return panel;
    }

    // create one grid of buttons and wire left/right click to the controller
    private void initializeBoardButtons(JPanel boardPanel, JButton[][] buttons, int playerNum) {
        int fontSize = switch (boardSize) {
            case 9 -> 20;
            case 13 -> 16;
            case 16 -> 14;
            default -> 16;
        };

        int cellSize = 40; // fixed size for all cells
        Dimension cellDim = new Dimension(cellSize, cellSize);

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                final int r = row;
                final int c = col;

                JButton btn = new JButton();
                btn.setBackground(COLOR_HIDDEN);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Tahoma", Font.BOLD, fontSize));
                btn.setFocusPainted(false);
                btn.setBorder(new LineBorder(new Color(40, 50, 60), 2));
                btn.setPreferredSize(cellDim);
                btn.setMinimumSize(cellDim);
                btn.setMaximumSize(cellDim);

                // mouse listener forwards clicks to controller, plus some UI rules
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            controller.onCellClick(playerNum, r, c);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            String text = btn.getText();
                            if (text != null && text.endsWith("*")) {
                                return;
                            }
                            controller.onCellRightClick(playerNum, r, c);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (btn.isEnabled()) {
                            // Check if metal detector is active and cell is a mine
                            if (isMetalDetectorActive()) {
                                model.Board currentBoard = (playerNum == 1) ? board1 : board2;
                                if (currentBoard != null) {
                                    model.Cell cell = currentBoard.getCell(r, c);
                                    if (cell.isMine() && cell.isHidden()) {
                                        // Red border for mine
                                        btn.setBorder(new LineBorder(new Color(255, 50, 50), 3));
                                        btn.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                                        return;
                                    }
                                }
                            }
                            btn.setBorder(new LineBorder(new Color(100, 200, 150), 2));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (btn.isEnabled()) {
                            btn.setBorder(new LineBorder(new Color(40, 50, 60), 2));
                            btn.setCursor(Cursor.getDefaultCursor());
                        }
                    }
                });

                buttons[row][col] = btn;
                boardPanel.add(btn);
            }
        }

        // Set fixed size for the board panel itself
        int boardPixelSize = boardSize * cellSize;
        Dimension boardDim = new Dimension(boardPixelSize, boardPixelSize);
        boardPanel.setPreferredSize(boardDim);
        boardPanel.setMinimumSize(boardDim);
        boardPanel.setMaximumSize(boardDim);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(new Color(220, 235, 230));
        btn.setFont(new Font("Tahoma", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(20, 80, 60));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 102, 51), 2, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        return btn;
    }

    // ---------- Public update methods ----------

    // called by controller when hidden-mine count changes
    public void updateMinesLeft(int playerNum, int count) {
        if (playerNum == 1) {
            player1MinesLeftLabel.setText("Mines Left: " + count);
        } else {
            player2MinesLeftLabel.setText("Mines Left: " + count);
        }
    }

    // switch active board highlight + dim the other one
    public void updateTurnVisuals(int currentTurn) {
        switch (currentTurn) {
            case 1 -> {
                player1Container.setBorder(ACTIVE_BORDER_P1_NEON);
                player1NameLabel.setForeground(NEON_GREEN);
                player1MinesLeftLabel.setForeground(Color.WHITE);
                boardPanel1.setBackground(new Color(20, 20, 20));

                player2Container.setBorder(INACTIVE_BORDER_P2);
                player2NameLabel.setForeground(Color.LIGHT_GRAY);
                player2MinesLeftLabel.setForeground(Color.LIGHT_GRAY);
                boardPanel2.setBackground(DIM_COLOR);
            }
            case 2 -> {
                player2Container.setBorder(ACTIVE_BORDER_P2_NEON);
                player2NameLabel.setForeground(NEON_ORANGE);
                player2MinesLeftLabel.setForeground(Color.WHITE);
                boardPanel2.setBackground(new Color(20, 20, 20));

                player1Container.setBorder(INACTIVE_BORDER_P1);
                player1NameLabel.setForeground(Color.LIGHT_GRAY);
                player1MinesLeftLabel.setForeground(Color.LIGHT_GRAY);
                boardPanel1.setBackground(DIM_COLOR);
            }
            default -> {
                // currentTurn == 0 or invalid -> no active board (game ended)
                player1Container.setBorder(INACTIVE_BORDER_P1);
                player2Container.setBorder(INACTIVE_BORDER_P2);
            }
        }
    }

    // main method that redraws a single cell according to Cell state + type
    public void updateCell(int playerNum, int row, int col, Cell cell, String cellTypeLabel) {
        JButton btn = (playerNum == 1) ? cellButtons1[row][col] : cellButtons2[row][col];

        if (cell.isRevealed()) {
            if (cell.isMine()) {
                // revealed mine (by click or final reveal) - use bomb icon
                btn.setText("");
                if (bombIcon != null) {
                    Image scaledImage = bombIcon.getScaledInstance(CELL_ICON_SIZE, CELL_ICON_SIZE, Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(scaledImage));
                } else {
                    btn.setText("M");
                }
                btn.setBackground(COLOR_MINE);
                btn.setForeground(Color.WHITE);
                btn.setEnabled(false);
                // Prevent graying out when disabled
                UIManager.put("Button.disabledText", Color.WHITE);
                btn.setDisabledIcon(btn.getIcon());
            } else if (cell.isQuestion()) {
                // yellow question; after attempt we darken and disable it
                btn.setText("");
                if (cell.isQuestionAttempted()) {
                    if (questionIcon != null) {
                        Image scaledImage = questionIcon.getScaledInstance(QUESTION_ICON_SIZE, QUESTION_ICON_SIZE, Image.SCALE_SMOOTH);
                        btn.setIcon(new ImageIcon(scaledImage));
                    } else {
                        btn.setText("Q");
                    }
                    btn.setBackground(COLOR_QUESTION_ATTEMPTED);
                    btn.setEnabled(false);
                } else {
                    if (questionIcon != null) {
                        Image scaledImage = questionIcon.getScaledInstance(QUESTION_ICON_SIZE, QUESTION_ICON_SIZE, Image.SCALE_SMOOTH);
                        btn.setIcon(new ImageIcon(scaledImage));
                    } else {
                        btn.setText("Q");
                    }
                    btn.setBackground(COLOR_QUESTION);
                    btn.setEnabled(true);
                }
                btn.setForeground(Color.BLACK);
            } else if (cell.isSurprise()) {
                // surprise cell behavior depends on pass/used state
                btn.setText("");
                if (surpriseIcon != null) {
                    Image scaledImage = surpriseIcon.getScaledInstance(CELL_ICON_SIZE, CELL_ICON_SIZE, Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(scaledImage));
                } else {
                    btn.setText("S");
                }
                if (cell.isReadyForSurprise()) {
                    // surprise already activated -> normal color but disabled
                    btn.setBackground(COLOR_SURPRISE);
                    btn.setEnabled(false);
                } else if (cell.isSurprisePassed()) {
                    // player passed turn on this surprise -> darker but still clickable
                    btn.setBackground(COLOR_SURPRISE_DARK);
                    btn.setEnabled(true);
                } else {
                    // just revealed and not passed/used yet
                    btn.setBackground(COLOR_SURPRISE);
                    btn.setEnabled(true);
                }
                btn.setForeground(Color.WHITE);
            } else if (cellTypeLabel != null && !cellTypeLabel.isEmpty()) {
                // revealed number cell (1–8)
                btn.setIcon(null);
                btn.setText(cellTypeLabel);
                btn.setBackground(COLOR_SAFE);
                try {
                    int num = Integer.parseInt(cellTypeLabel);
                    btn.setForeground(getNumberColor(num));   // different colors per number
                } catch (NumberFormatException ex) {
                    btn.setForeground(Color.BLACK);
                }
                btn.setEnabled(false);
            } else {
                // revealed empty cell (no mines around)
                btn.setIcon(null);
                btn.setText("");
                btn.setBackground(new Color(200, 220, 200));
                btn.setEnabled(false);
            }
            btn.setBorder(new LineBorder(new Color(30, 30, 30), 1));
        } else if (cell.isFlagged()) {
            // hidden + flagged -> show icon based on content
            if (cell.isMine()) {
                // mine flagged (safety net protection) -> show bomb icon
                btn.setText("");
                if (bombIcon != null) {
                    Image scaledImage = bombIcon.getScaledInstance(CELL_ICON_SIZE, CELL_ICON_SIZE, Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(scaledImage));
                } else {
                    btn.setText("M");
                }
            } else {
                // non-mine flagged -> show flag icon
                btn.setText("");
                if (flagIcon != null) {
                    Image scaledImage = flagIcon.getScaledInstance(CELL_ICON_SIZE, CELL_ICON_SIZE, Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(scaledImage));
                } else {
                    btn.setText("F");
                }
            }
            btn.setBackground(COLOR_FLAGGED);
            btn.setForeground(Color.YELLOW);
            btn.setEnabled(true);
            btn.setBorder(new LineBorder(new Color(40, 50, 60), 2));
        } else {
            // hidden and not flagged – default covered look
            btn.setIcon(null);
            btn.setText("");
            btn.setBackground(COLOR_HIDDEN);
            btn.setForeground(Color.WHITE);
            btn.setEnabled(true);
            btn.setBorder(new LineBorder(new Color(40, 50, 60), 2));
        }
    }

    // called after a question is answered to lock that Q visually
    public void markQuestionAttempted(int playerNum, int row, int col, Cell cell) {
        JButton btn = (playerNum == 1) ? cellButtons1[row][col] : cellButtons2[row][col];
        btn.setText("");
        if (questionIcon != null) {
            Image scaledImage = questionIcon.getScaledInstance(QUESTION_ICON_SIZE, QUESTION_ICON_SIZE, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaledImage));
        } else {
            btn.setText("Q");
        }
        btn.setBackground(COLOR_QUESTION_ATTEMPTED);
        btn.setForeground(Color.BLACK);
        btn.setEnabled(false);
        btn.setBorder(new LineBorder(new Color(30, 30, 30), 1));
    }

    public void updateScore(int score) {
        scoreLabel.setText(String.valueOf(score));
    }

    public void updateLives(int lives) {
        // Clear existing icons
        livesPanel.removeAll();
        
        // Add icon for each life remaining
        for (int i = 0; i < lives; i++) {
            if (healthIcon != null) {
                // Create a scaled version of the icon
                Image scaledImage = healthIcon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
                livesPanel.add(iconLabel);
            } else {
                // Fallback to text if icon can't be loaded
                JLabel textLabel = new JLabel("❤");
                textLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
                textLabel.setForeground(new Color(220, 50, 50));
                livesPanel.add(textLabel);
            }
        }
        
        livesPanel.revalidate();
        livesPanel.repaint();
    }

    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    // small info popup used e.g. for “wrong turn”, “pause”, etc.
    public void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // map number of neighbors to a classic Minesweeper color
    private Color getNumberColor(int num) {
        return switch (num) {
            case 1 -> Color.BLUE;
            case 2 -> new Color(0, 128, 0);
            case 3 -> Color.RED;
            case 4 -> new Color(0, 0, 128);
            case 5 -> new Color(128, 0, 0);
            case 6 -> new Color(0, 128, 128);
            case 8 -> Color.GRAY;
            default -> Color.BLACK;
        };
    }

    // older dialog helper, now you mostly use the controller’s custom dialogs
    public int showQuestionChoiceDialog() {
        Object[] options = { "Pass Turn", "Answer Question" };
        return JOptionPane.showOptionDialog(
                frame,
                "You uncovered a Question Cell!\nWhat do you want to do?",
                "Question Cell",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );
    }    
    // -------------------------------
    // Shop UI update methods
    // -------------------------------
    
    public void updateMomentumDisplay(int streak, String tierDescription) {
        momentumLabel.setText(streak + " streak - " + tierDescription);
    }
    
    public void updateShopButtons(int currentScore, boolean safetyNetActive, boolean metalDetectorActive, int safetyNetPurchases, int metalDetectorPurchases) {
        // Enable/disable Safety Net button
        if (safetyNetActive) {
            safetyNetButton.setText("Safety Net (ACTIVE)");
            safetyNetButton.setEnabled(false);
            safetyNetButton.setBackground(new Color(80, 200, 80));
        } else if (safetyNetPurchases >= 3) {
            safetyNetButton.setText("Safety Net (MAX)");
            safetyNetButton.setEnabled(false);
            safetyNetButton.setBackground(new Color(80, 80, 80));
        } else if (currentScore >= 10) {
            safetyNetButton.setText(String.format("Safety Net (10 pts) [%d/3]", safetyNetPurchases));
            safetyNetButton.setEnabled(true);
            safetyNetButton.setBackground(new Color(40, 100, 80));
        } else {
            safetyNetButton.setText(String.format("Safety Net (10 pts) [%d/3]", safetyNetPurchases));
            safetyNetButton.setEnabled(false);
            safetyNetButton.setBackground(new Color(80, 80, 80));
        }
        
        // Metal Detector button
        if (metalDetectorPurchases >= 3) {
            metalDetectorButton.setText("Metal Detector (MAX)");
            metalDetectorButton.setEnabled(false);
            metalDetectorButton.setBackground(new Color(80, 80, 80));
        } else if (metalDetectorActive) {
            metalDetectorButton.setText(String.format("Metal Detector (15 pts) [%d/3]", metalDetectorPurchases));
            metalDetectorButton.setEnabled(false);
            metalDetectorButton.setBackground(new Color(120, 160, 255));
        } else if (currentScore >= 15) {
            metalDetectorButton.setText(String.format("Metal Detector (15 pts) [%d/3]", metalDetectorPurchases));
            metalDetectorButton.setEnabled(true);
            metalDetectorButton.setBackground(new Color(60, 80, 120));
        } else {
            metalDetectorButton.setText(String.format("Metal Detector (15 pts) [%d/3]", metalDetectorPurchases));
            metalDetectorButton.setEnabled(false);
            metalDetectorButton.setBackground(new Color(80, 80, 80));
        }
    }
    
    public void updateShopStatus(String status) {
        shopStatusLabel.setText(status);
    }
    
    public void updateMetalDetectorTimer(double secondsRemaining) {
        if (secondsRemaining > 0) {
            metalDetectorTimerLabel.setText(String.format("⏱️ %.1fs", secondsRemaining));
        } else {
            metalDetectorTimerLabel.setText("");
        }
    }
    
    public void setBoards(model.Board b1, model.Board b2) {
        this.board1 = b1;
        this.board2 = b2;
    }
    
    private boolean metalDetectorActiveFlag = false;
    
    private boolean isMetalDetectorActive() {
        return metalDetectorActiveFlag;
    }
    
    public void setMetalDetectorActive(boolean active) {
        this.metalDetectorActiveFlag = active;
    }
    
    public void setShopButtonListeners(Runnable onSafetyNet, Runnable onMetalDetector) {
        safetyNetButton.addActionListener(e -> onSafetyNet.run());
        metalDetectorButton.addActionListener(e -> onMetalDetector.run());
    }
    
    // Store a reference to check metal detector status
    private java.util.function.BooleanSupplier metalDetectorChecker;
    private java.util.function.BiFunction<Integer, Integer, Boolean> mineChecker;
    
    public void setMetalDetectorChecker(java.util.function.BooleanSupplier isActive,
                                        java.util.function.BiFunction<Integer, Integer, Boolean> isMineAt) {
        this.metalDetectorChecker = isActive;
        this.mineChecker = isMineAt;
    }
    
    public JButton getSafetyNetButton() {
        return safetyNetButton;
    }
    
    public JButton getMetalDetectorButton() {
        return metalDetectorButton;
    }
    // Mock main (optional) – lets you open the board view without the rest of the game
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                GameBoardController mock = new GameBoardController() {
                    @Override
                    public void onCellClick(int p, int r, int c) {}
                    @Override
                    public void onCellRightClick(int p, int r, int c) {}
                    @Override
                    public void pauseGame() {}
                    @Override
                    public void quitToMenu() {}
                };
                GameBoardView window = new GameBoardView(mock, "P1", "P2", 9, 0, 1);
                window.show();
            } catch (Exception e) {
                // Suppress stack trace for demo purposes
                System.err.println("Error: " + e.getMessage());
            }
        });
    }
}
