package view;

import controller.IGameBoardController;
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
import model.GameObserver;
import model.ResourceLoader;

public class GameBoardView implements GameObserver {

    // Timer fields
    private Timer gameTimer;
    private int elapsedSeconds = 0;
    private JLabel timerLabel;

    private JFrame frame;
    private final IGameBoardController controller;

    private JPanel boardPanel1;
    private JPanel boardPanel2;
    private JPanel infoPanelRef;  // Reference to info panel for border updates
    private JPanel centerSidebarPanelRef;  // Reference to sidebar panel for border updates

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
    private JLabel tierIconLabel;  // For tier1-3 icons based on momentum
    private JPanel momentumPanel;
    private JPanel shopPanel;
    private JLabel shopStatusLabel;
    private JLabel metalDetectorTimerLabel;

    private final int boardSize;
    
    // Character icons
    private static final String[] CHARACTER_ICON_PATHS = {
        "/resources/cool.png", "/resources/smile.png", "/resources/artum.png",
        "/resources/wizard.png", "/resources/superhero.png", "/resources/Dragonfly.png"
    };
    private BufferedImage player1CharIcon;
    private BufferedImage player2CharIcon;
    private JLabel player1CharLabel;
    private JLabel player2CharLabel;
    private JLabel stabilizerLabel;
    private JButton stabilizerBtnRef;
    private BufferedImage stabilizerIcon;
    private JLabel flagsRemainingLabel;
    private static final int CHARACTER_DISPLAY_SIZE = 40;
    
    // Store board references for metal detector
    private model.Board board1;
    private model.Board board2;
    private model.Board.Difficulty gameDifficulty;
    
    // Health bar icon
    private BufferedImage healthIcon;
    private BufferedImage emptyHeartIcon;
    private static final int ICON_SIZE = 24;
    private int maxLives = 0;  // Track max lives to display empty hearts for lost lives
    
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
    
    // Stabilizer state tracking
    private boolean stabilizerAvailable = false;

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
    
    // Difficulty-based colors (matching GameSetup radio buttons)
    private static final Color EASY_COLOR = new Color(20, 120, 70);
    private static final Color MEDIUM_COLOR = new Color(200, 120, 0);
    private static final Color HARD_COLOR = new Color(160, 45, 45);
    private static final Color EXTREME_COLOR = new Color(100, 50, 150);
    
    private Border difficultyInactiveP1;
    private Border difficultyInactiveP2;
    private Border difficultyActiveP1;
    private Border difficultyActiveP2;

    // ---------- ctor ----------

    /**
     * @wbp.parser.entryPoint
     */
    public GameBoardView(IGameBoardController controller, String player1Name, String player2Name, int boardSize, int player1CharIndex, int player2CharIndex) {
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
        frame.setSize(1800, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        // set app icon for taskbar and window
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadAppIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }

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

        gbc.gridx = 2;
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
        gbc.insets = new Insets(0, 0, 0, 0);  // Remove insets for board row to eliminate gap
        player1Container = new JPanel(new BorderLayout(0, 8));
        player1Container.setOpaque(false);
        player1Container.setFocusable(false);
        player1Container.setFocusTraversalPolicyProvider(false);
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
        board1Wrapper.setOpaque(true);
        board1Wrapper.setBackground(new Color(20, 20, 20));
        board1Wrapper.setFocusable(false);
        board1Wrapper.setFocusTraversalPolicyProvider(false);

        boardPanel1 = new JPanel(new GridLayout(boardSize, boardSize, 1, 1));
        boardPanel1.setBackground(new Color(20, 20, 20));
        boardPanel1.setBorder(new LineBorder(new Color(30, 30, 30), 2));
        boardPanel1.setFocusable(false);

        GridBagConstraints innerGbc = new GridBagConstraints();
        innerGbc.fill = GridBagConstraints.BOTH;
        innerGbc.insets = new Insets(0, 0, 0, 0);
        // For HARD difficulty, use fixed sizing. For others, use flexible sizing
        if (gameDifficulty == model.Board.Difficulty.HARD) {
            innerGbc.weightx = 0.0;
            innerGbc.weighty = 0.0;
        } else {
            innerGbc.weightx = 1.0;
            innerGbc.weighty = 1.0;
        }
        board1Wrapper.add(boardPanel1, innerGbc);

        // Set fixed size for HARD, flexible for others
        if (gameDifficulty == model.Board.Difficulty.HARD) {
            board1Wrapper.setPreferredSize(new Dimension(520, 520));
            board1Wrapper.setMinimumSize(new Dimension(520, 520));
            board1Wrapper.setMaximumSize(new Dimension(520, 520));
        }

        player1Container.add(board1Wrapper, BorderLayout.CENTER);
        // create all buttons for player 1 and hook up mouse events to controller
        initializeBoardButtons(boardPanel1, cellButtons1, 1);
        centerPanel.add(player1Container, gbc);

        // Center sidebar panel with control buttons (between the two boards)
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.0;  // Don't expand sidebar
        gbc.insets = new Insets(0, 20, 0, 20);  // Equal padding on both sides
        
        JPanel centerSidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't paint background - make it transparent
                super.paintComponent(g);
            }
        };
        centerSidebarPanel.setLayout(new BoxLayout(centerSidebarPanel, BoxLayout.Y_AXIS));
        centerSidebarPanel.setOpaque(false);
        centerSidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0, 200, 255), 3, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)  // Reduced top/bottom padding from 10 to 5
        ));
        centerSidebarPanel.setPreferredSize(new Dimension(80, 0));
        centerSidebarPanelRef = centerSidebarPanel;  // Store reference for border updates

        // Pause button in center sidebar
        JButton sidebarPauseBtn = new JButton("Pause");
        if (pauseIcon != null) {
            Image scaledPauseIcon = pauseIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            sidebarPauseBtn.setIcon(new ImageIcon(scaledPauseIcon));
        }
        sidebarPauseBtn.setFont(new Font("Tahoma", Font.BOLD, 10));
        sidebarPauseBtn.setForeground(new Color(220, 235, 230));
        sidebarPauseBtn.setBackground(new Color(100, 60, 40));
        sidebarPauseBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        sidebarPauseBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        sidebarPauseBtn.setFocusPainted(false);
        sidebarPauseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPauseBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(150, 100, 80), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        sidebarPauseBtn.setPreferredSize(new Dimension(60, 55));
        sidebarPauseBtn.setMaximumSize(new Dimension(60, 55));
        sidebarPauseBtn.addActionListener(e -> controller.pauseGame());
        centerSidebarPanel.add(sidebarPauseBtn);
        centerSidebarPanel.add(Box.createVerticalStrut(15));

        // Exit button in center sidebar
        JButton sidebarExitBtn = new JButton("Exit");
        if (exitIcon != null) {
            Image scaledExitIcon = exitIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            sidebarExitBtn.setIcon(new ImageIcon(scaledExitIcon));
        }
        sidebarExitBtn.setFont(new Font("Tahoma", Font.BOLD, 10));
        sidebarExitBtn.setForeground(new Color(220, 235, 230));
        sidebarExitBtn.setBackground(new Color(100, 40, 40));
        sidebarExitBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        sidebarExitBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        sidebarExitBtn.setFocusPainted(false);
        sidebarExitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarExitBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(150, 80, 80), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        sidebarExitBtn.setPreferredSize(new Dimension(60, 55));
        sidebarExitBtn.setMaximumSize(new Dimension(60, 55));
        sidebarExitBtn.addActionListener(e -> controller.quitToMenu());
        centerSidebarPanel.add(sidebarExitBtn);
        centerSidebarPanel.add(Box.createVerticalStrut(20));  // Space before stabilizer

        // Stabilizer button in center sidebar (bottom - doesn't appear in all difficulties)
        JButton sidebarStabilizerBtn = new JButton();
        if (stabilizerIcon != null) {
            Image scaledStabilizer = stabilizerIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            sidebarStabilizerBtn.setIcon(new ImageIcon(scaledStabilizer));
        }
        sidebarStabilizerBtn.setPreferredSize(new Dimension(60, 50));
        sidebarStabilizerBtn.setMaximumSize(new Dimension(60, 50));
        sidebarStabilizerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarStabilizerBtn.setBorderPainted(true);
        sidebarStabilizerBtn.setBorder(new LineBorder(new Color(100, 200, 200), 2, true));
        sidebarStabilizerBtn.setContentAreaFilled(false);
        sidebarStabilizerBtn.setFocusPainted(false);
        sidebarStabilizerBtn.setVisible(false);
        sidebarStabilizerBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        sidebarStabilizerBtn.addActionListener(e -> showStabilizerInfoDialog());
        stabilizerBtnRef = sidebarStabilizerBtn;
        centerSidebarPanel.add(sidebarStabilizerBtn);
        centerSidebarPanel.add(Box.createVerticalStrut(20));  // Space before flags display
        
        // Flags remaining display
        JPanel flagsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        flagsPanel.setLayout(new BoxLayout(flagsPanel, BoxLayout.Y_AXIS));
        flagsPanel.setOpaque(false);
        flagsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        flagsPanel.setMaximumSize(new Dimension(70, 70));
        
        // Flag icon
        JButton flagIconBtn = new JButton();
        if (flagIcon != null) {
            Image scaledFlagIcon = flagIcon.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            flagIconBtn.setIcon(new ImageIcon(scaledFlagIcon));
        }
        flagIconBtn.setPreferredSize(new Dimension(50, 40));
        flagIconBtn.setMaximumSize(new Dimension(50, 40));
        flagIconBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        flagIconBtn.setContentAreaFilled(false);
        flagIconBtn.setBorderPainted(false);
        flagIconBtn.setFocusPainted(false);
        flagIconBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        flagIconBtn.setEnabled(false);
        flagsPanel.add(flagIconBtn);
        
        // Flags remaining label
        flagsRemainingLabel = new JLabel("Flags: 0");
        flagsRemainingLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        flagsRemainingLabel.setForeground(Color.WHITE);
        flagsRemainingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        flagsPanel.add(flagsRemainingLabel);
        
        centerSidebarPanel.add(flagsPanel);
        
        centerPanel.add(centerSidebarPanel, gbc);

        // Player 2 board + labels (same layout on right side)
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 10, 0, 10);
        player2Container = new JPanel(new BorderLayout(0, 8));
        player2Container.setOpaque(false);
        player2Container.setFocusable(false);
        player2Container.setFocusTraversalPolicyProvider(false);
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
        board2Wrapper.setOpaque(true);
        board2Wrapper.setBackground(new Color(20, 20, 20));
        board2Wrapper.setFocusable(false);
        board2Wrapper.setFocusTraversalPolicyProvider(false);

        boardPanel2 = new JPanel(new GridLayout(boardSize, boardSize, 1, 1));
        boardPanel2.setBackground(new Color(20, 20, 20));
        boardPanel2.setBorder(new LineBorder(new Color(30, 30, 30), 2));
        boardPanel2.setFocusable(false);
        board2Wrapper.add(boardPanel2, innerGbc);

        // Set fixed size for HARD, flexible for others
        if (gameDifficulty == model.Board.Difficulty.HARD) {
            board2Wrapper.setPreferredSize(new Dimension(520, 520));
            board2Wrapper.setMinimumSize(new Dimension(520, 520));
            board2Wrapper.setMaximumSize(new Dimension(520, 520));
        }

        player2Container.add(board2Wrapper, BorderLayout.CENTER);
        // same for player 2 board
        initializeBoardButtons(boardPanel2, cellButtons2, 2);

        centerPanel.add(player2Container, gbc);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // bottom panel with shop, controls, and status
        JPanel bottomWrapper = new JPanel(new BorderLayout(0, 5));
        bottomWrapper.setOpaque(false);
        
        // Shop panel (top of bottom section)
        createShopPanel();
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
        shopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        shopPanel.setOpaque(false);
        
        // Momentum Multiplier display - EXTREME difficulty only
        momentumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        momentumPanel.setOpaque(false);
        
        // Add tier icon (tier1-3.png based on momentum)
        tierIconLabel = new JLabel();
        java.awt.image.BufferedImage tierIcon = model.ResourceLoader.loadImage("/resources/tier1.png");
        if (tierIcon != null) {
            Image scaledTier = tierIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            tierIconLabel.setIcon(new ImageIcon(scaledTier));
        }
        momentumPanel.add(tierIconLabel);
        
        JLabel momentumTitle = new JLabel("Momentum:");
        momentumTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
        momentumTitle.setForeground(new Color(255, 215, 0));
        momentumPanel.add(momentumTitle);
        
        momentumLabel = new JLabel("0 streak");
        momentumLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        momentumLabel.setForeground(Color.WHITE);
        momentumPanel.add(momentumLabel);
        
        // Note: momentum panel will be added conditionally in setBoards() based on difficulty
        // shopPanel.add(momentumPanel);
        
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

        // Use GridBagLayout for better centering control
        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(1000, 90));
        panel.setBorder(new LineBorder(new Color(0, 200, 255), 3, true));
        infoPanelRef = panel;  // Store reference for later border updates

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;  // Center items vertically

        // Score panel - LEFT
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel scorePanel = createInfoItem("Score:", "0", new Color(0, 255, 128));
        scoreLabel = (JLabel) scorePanel.getComponent(1);
        panel.add(scorePanel, gbc);

        // Lives panel - MIDDLE
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 5, 0, 5);  // Closer spacing
        JPanel livesPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        livesPanelWrapper.setOpaque(false);
        JLabel livesTitle = new JLabel("Lives:");
        livesTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
        livesTitle.setForeground(new Color(100, 255, 100));
        livesPanelWrapper.add(livesTitle);
        
        livesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        livesPanel.setOpaque(false);
        livesPanelWrapper.add(livesPanel);
        panel.add(livesPanelWrapper, gbc);

        // Timer label - RIGHT
        gbc.gridx = 2;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 5, 0, 0);  // Closer spacing
        timerLabel = new JLabel("Time: 0:00");
        timerLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        timerLabel.setForeground(new Color(0, 200, 255));
        panel.add(timerLabel, gbc);
        
        // Stabilizer icon display as button - right center with equal weight
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 10, 0, 10);
        JButton stabilizerBtn = new JButton();
        if (stabilizerIcon != null) {
            Image scaledStabilizer = stabilizerIcon.getScaledInstance(CHARACTER_DISPLAY_SIZE, CHARACTER_DISPLAY_SIZE, Image.SCALE_SMOOTH);
            stabilizerBtn.setIcon(new ImageIcon(scaledStabilizer));
        }
        stabilizerBtn.setPreferredSize(new Dimension(CHARACTER_DISPLAY_SIZE + 10, CHARACTER_DISPLAY_SIZE + 10));
        stabilizerBtn.setBorderPainted(true);
        stabilizerBtn.setBorder(new LineBorder(new Color(100, 200, 200), 2, true));
        stabilizerBtn.setContentAreaFilled(false);
        stabilizerBtn.setFocusPainted(false);
        stabilizerBtn.setVisible(false);  // Initially hidden, shown only for EXTREME
        stabilizerBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        stabilizerBtn.addActionListener(e -> showStabilizerInfoDialog());
        stabilizerLabel = new JLabel();  // Keep for compatibility with other methods
        panel.add(stabilizerBtn, gbc);
        
        // Store button reference for state updates
        stabilizerBtnRef = stabilizerBtn;

        return panel;
    }

    // Load the health icon from resources
    private void loadHealthIcon() {
        try {
            // Load filled heart icon
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
            
            // Load empty heart icon
            java.net.URL emptyIconUrl = GameBoardView.class.getResource("/resources/emptyheart.png");
            if (emptyIconUrl != null) {
                emptyHeartIcon = ImageIO.read(emptyIconUrl);
            } else {
                // Fallback to file system (for IDE)
                String emptyIconPath = ResourceLoader.getResourcePath("/resources/emptyheart.png");
                if (emptyIconPath != null && !emptyIconPath.isEmpty()) {
                    emptyHeartIcon = ImageIO.read(new File(emptyIconPath));
                } else {
                    System.err.println("Empty heart icon not found");
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load health icons: " + e.getMessage());
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
            pauseIcon = loadIcon("/resources/pause-button.png");
            exitIcon = loadIcon("/resources/exit.png");
            stabilizerIcon = loadIcon("/resources/defibrillator.png");
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
    
    public void hideTimer() {
        timerLabel.setVisible(false);
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
                player2Container.setBorder(INACTIVE_BORDER_P2);            }
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
                // revealed number cell (1â€“8)
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
            // hidden and not flagged â€“ default covered look
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
        
        // Add icon for each life remaining (filled hearts)
        for (int i = 0; i < lives; i++) {
            if (healthIcon != null) {
                // Create a scaled version of the filled heart icon
                Image scaledImage = healthIcon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
                livesPanel.add(iconLabel);
            }
        }
        
        // Add icons for lost lives (empty hearts)
        for (int i = lives; i < maxLives; i++) {
            if (emptyHeartIcon != null) {
                // Create a scaled version of the empty heart icon
                Image scaledImage = emptyHeartIcon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
                livesPanel.add(iconLabel);
            } else {
                // Fallback to text if icon can't be loaded
                JLabel textLabel = new JLabel("ðŸ–¤");  // Using empty heart emoji
                textLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
                textLabel.setForeground(new Color(100, 100, 100));
                livesPanel.add(textLabel);
            }
        }
        
        livesPanel.revalidate();
        livesPanel.repaint();
    }
    
    // Set the maximum lives to display empty hearts for lost lives
    public void setMaxLives(int maxLives) {
        this.maxLives = maxLives;
    }

    public void updateFlagsRemaining(int flags) {
        flagsRemainingLabel.setText("Flags: " + flags);
    }

    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    // small info popup used e.g. for â€œwrong turnâ€, â€œpauseâ€, etc.
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

    // older dialog helper, now you mostly use the controllerâ€™s custom dialogs
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
        // Only show momentum for EXTREME difficulty
        if (board1 != null && board1.getDifficulty() == model.Board.Difficulty.EXTREME) {
            momentumLabel.setText(streak + " streak - " + tierDescription);
            momentumLabel.setVisible(true);
            
            // Update tier icon based on momentum tier
            // Determine which tier based on consecutive safe cells count (streak)
            int tierLevel = 1;  // Default to tier1
            if (streak >= 15) {
                tierLevel = 3;  // Tier 2 (using tier3.png for Tier 2)
            } else if (streak >= 5) {
                tierLevel = 2;  // Tier 1 (using tier2.png for Tier 1)
            } else {
                tierLevel = 1;  // No tier (tier1.png)
            }
            
            // Load and set the appropriate tier icon
            java.awt.image.BufferedImage tierIcon = model.ResourceLoader.loadImage("/resources/tier" + tierLevel + ".png");
            if (tierIcon != null && tierIconLabel != null) {
                Image scaledTier = tierIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                tierIconLabel.setIcon(new ImageIcon(scaledTier));
                tierIconLabel.repaint();  // Force repaint to ensure icon updates
            }
        } else if (momentumLabel != null) {
            momentumLabel.setVisible(false);
        }
    }
    
    // Reset momentum tier icon to tier1 when a mine is hit
    public void resetMomentumTierIcon() {
        if (tierIconLabel != null) {
            java.awt.image.BufferedImage tierIcon = model.ResourceLoader.loadImage("/resources/tier1.png");
            if (tierIcon != null) {
                Image scaledTier = tierIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                tierIconLabel.setIcon(new ImageIcon(scaledTier));
            }
        }
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
            metalDetectorTimerLabel.setText(String.format("Metal Detector: %.1fs", secondsRemaining));
        } else {
            metalDetectorTimerLabel.setText("");
        }
    }
    
    public void setBoards(model.Board b1, model.Board b2) {
        this.board1 = b1;
        this.board2 = b2;
        
        // Update info panel border based on difficulty
        if (b1 != null) {
            model.Board.Difficulty difficulty = b1.getDifficulty();
            
            // Initialize stabilizer display for EXTREME difficulty
            if (difficulty == model.Board.Difficulty.EXTREME) {
                setStabilizerAvailable();
            }
            
            // Only add momentum panel for EXTREME difficulty
            if (difficulty == model.Board.Difficulty.EXTREME) {
                // Add momentum panel if not already added
                if (momentumPanel.getParent() == null) {
                    // Find the shop panel and add momentum panel to it
                    // We need to add it at the right position in the hierarchy
                    for (Component comp : shopPanel.getComponents()) {
                        if (comp instanceof JLabel && "SHOP".equals(((JLabel) comp).getText())) {
                            // Insert momentum panel before SHOP title
                            int index = shopPanel.getComponentZOrder(comp);
                            shopPanel.add(momentumPanel, index);
                            shopPanel.add(Box.createHorizontalStrut(20), index + 1);
                            break;
                        }
                    }
                    if (momentumPanel.getParent() == null) {
                        // Fallback: just add at the beginning
                        shopPanel.add(momentumPanel, 0);
                        shopPanel.add(Box.createHorizontalStrut(20), 1);
                    }
                }
            }
            
            Color borderColor;
            switch (difficulty) {
                case EASY:
                    borderColor = EASY_COLOR;
                    break;
                case MEDIUM:
                    borderColor = MEDIUM_COLOR;
                    break;
                case HARD:
                    borderColor = HARD_COLOR;
                    break;
                case EXTREME:
                    borderColor = EXTREME_COLOR;
                    break;
                default:
                    borderColor = new Color(0, 200, 255);  // Default cyan
            }
            if (infoPanelRef != null) {
                infoPanelRef.setBorder(new LineBorder(borderColor, 3, true));
            }
            if (centerSidebarPanelRef != null) {
                centerSidebarPanelRef.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(borderColor, 3, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }
        }
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
    // Mock main (optional) â€“ lets you open the board view without the rest of the game
    /**
     * Get the board panel for a specific player
     * @param playerNum 1 for player 1, 2 for player 2
     * @return The board panel component
     */
    public JPanel getBoardPanel(int playerNum) {
        return (playerNum == 1) ? boardPanel1 : boardPanel2;
    }
    
    /**
     * Get the board size (number of cells per row/col)
     */
    public int getBoardSize() {
        return boardSize;
    }

    // -------------------------------
    // GameObserver implementation
    // -------------------------------
    
    /**
     * Called when the game state is updated (Observer pattern).
     * Updates the score and lives display in the UI.
     * 
     * @param score the current score
     * @param lives the current number of lives
     */
    @Override
    public void onGameUpdated(int score, int lives) {
        if (scoreLabel != null) {
            scoreLabel.setText(String.valueOf(score));
        }
        // Lives are displayed using updateLives method with icons
        // We can update the lives panel here if needed
        if (livesPanel != null) {
            updateLives(lives);
        }
    }

    /**
     * Updates the stabilizer display to show it's available (lit up with full color)
     */
    public void setStabilizerAvailable() {
        if (stabilizerBtnRef == null || stabilizerIcon == null) return;
        
        stabilizerAvailable = true;
        stabilizerBtnRef.setVisible(true);
        // Display the icon at full brightness
        Image scaledStabilizer = stabilizerIcon.getScaledInstance(CHARACTER_DISPLAY_SIZE, CHARACTER_DISPLAY_SIZE, Image.SCALE_SMOOTH);
        stabilizerBtnRef.setIcon(new ImageIcon(scaledStabilizer));
    }

    /**
     * Updates the stabilizer display to show it's been used (grayed out)
     */
    public void setStabilizerUsed() {
        if (stabilizerBtnRef == null || stabilizerIcon == null) return;
        
        stabilizerAvailable = false;
        // Create a grayed out version of the icon
        java.awt.image.BufferedImage grayedIcon = new java.awt.image.BufferedImage(
            stabilizerIcon.getWidth(), 
            stabilizerIcon.getHeight(), 
            java.awt.image.BufferedImage.TYPE_INT_RGB
        );
        
        // Convert to grayscale
        for (int y = 0; y < stabilizerIcon.getHeight(); y++) {
            for (int x = 0; x < stabilizerIcon.getWidth(); x++) {
                int rgb = stabilizerIcon.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Calculate grayscale using luminance formula
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // Reduce brightness by 50%
                gray = gray / 2;
                
                int grayRgb = (a << 24) | (gray << 16) | (gray << 8) | gray;
                grayedIcon.setRGB(x, y, grayRgb);
            }
        }
        
        Image scaledGrayed = grayedIcon.getScaledInstance(CHARACTER_DISPLAY_SIZE, CHARACTER_DISPLAY_SIZE, Image.SCALE_SMOOTH);
        stabilizerBtnRef.setIcon(new ImageIcon(scaledGrayed));
    }

    /**
     * Shows the stabilizer information dialog when the button is clicked
     */
    private void showStabilizerInfoDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Stabilizer");
        dialog.setModal(true);
        dialog.setSize(500, 370);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
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
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Icon and title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        if (stabilizerIcon != null) {
            Image scaledIcon = stabilizerIcon.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
            titlePanel.add(iconLabel);
        }
        JLabel titleLabel = new JLabel("STABILIZER");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 200, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Status
        JLabel statusLabel = new JLabel(stabilizerAvailable ? "Status: AVAILABLE" : "Status: USED");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        statusLabel.setForeground(stabilizerAvailable ? new Color(0, 255, 128) : new Color(200, 100, 100));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Description - create wrapper to ensure left alignment
        JPanel descPanel = new JPanel();
        descPanel.setOpaque(false);
        descPanel.setLayout(new BorderLayout());
        JLabel descLabel = new JLabel("<html>" +
            "1. The Stabilizer is a special feature available only in EXTREME difficulty.<br><br>" +
            "2. When you hit a mine on your LAST LIFE, instead of losing immediately,<br>" +
            "you get one chance to answer a HARD question.<br><br>" +
            "3. If you answer correctly, the mine is disabled and flagged!<br>" +
            "If you answer incorrectly, you lose the game.<br><br>" +
            "4. You can only use the Stabilizer ONCE per game." +
            "</html>");
        descLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        descLabel.setForeground(Color.WHITE);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setHorizontalAlignment(SwingConstants.LEFT);
        descPanel.add(descLabel, BorderLayout.WEST);
        mainPanel.add(descPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        closeBtn.setBackground(new Color(100, 150, 200));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(150, 180, 220), 2, true),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());
        mainPanel.add(closeBtn);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                IGameBoardController mock = new IGameBoardController() {
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
