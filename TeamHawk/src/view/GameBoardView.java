package view;

import controller.Main.GameBoardController;
import model.Cell;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameBoardView {
    private JFrame frame;
    private GameBoardController controller;
    
    private JPanel boardPanel1;
    private JPanel boardPanel2;
    private JButton[][] cellButtons1;
    private JButton[][] cellButtons2;
    
    private JLabel player1NameLabel;
    private JLabel player2NameLabel;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel currentTurnLabel;
    private JLabel statusLabel;
    
    private int boardSize;
    
    // Cell type colors
    private static final Color COLOR_HIDDEN = new Color(60, 80, 95);
    private static final Color COLOR_QUESTION = new Color(255, 200, 50);
    private static final Color COLOR_MINE = new Color(220, 50, 50);
    private static final Color COLOR_SURPRISE = new Color(180, 100, 255);
    private static final Color COLOR_SAFE = new Color(140, 200, 140);
    private static final Color COLOR_FLAGGED = new Color(90, 110, 130);
    
    /**
     * @wbp.parser.entryPoint
     */
    public GameBoardView(GameBoardController controller, String player1Name, String player2Name, int boardSize) {
        this.controller = controller;
        this.boardSize = boardSize;
        this.cellButtons1 = new JButton[boardSize][boardSize];
        this.cellButtons2 = new JButton[boardSize][boardSize];
        initialize(player1Name, player2Name);
    }
    
    public void show() {
        frame.setVisible(true);
    }
    
    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }
    
    private void initialize(String p1Name, String p2Name) {
        frame = new JFrame("Minesweeper - Two Player Game");
        frame.setSize(1600, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        
        // Main background panel
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
        
        // ==================== TOP INFO PANEL ====================
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        
        JPanel infoPanel = createInfoPanel();
        topWrapper.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(topWrapper, BorderLayout.NORTH);
        
        // ==================== CENTER - BOARDS ====================
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 10, 0, 10);
        
        // Player 1 Board Container
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        JPanel player1Container = new JPanel(new BorderLayout(0, 8));
        player1Container.setOpaque(false);
        player1Container.setBorder(new LineBorder(new Color(51, 153, 102), 4, true));
        
        player1NameLabel = new JLabel(p1Name + "'s Board", SwingConstants.CENTER);
        player1NameLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        player1NameLabel.setForeground(Color.WHITE);
        player1NameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        player1Container.add(player1NameLabel, BorderLayout.NORTH);
        
        // Board panel with responsive GridLayout
        JPanel board1Wrapper = new JPanel(new GridBagLayout());
        board1Wrapper.setOpaque(false);
        board1Wrapper.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        
        boardPanel1 = new JPanel(new GridLayout(boardSize, boardSize, 1, 1));
        boardPanel1.setBackground(new Color(20, 20, 20));
        boardPanel1.setBorder(new LineBorder(new Color(30, 30, 30), 2));
        
        GridBagConstraints innerGbc = new GridBagConstraints();
        innerGbc.fill = GridBagConstraints.BOTH;
        innerGbc.weightx = 1.0;
        innerGbc.weighty = 1.0;
        board1Wrapper.add(boardPanel1, innerGbc);
        
        player1Container.add(board1Wrapper, BorderLayout.CENTER);
        initializeBoardButtons(boardPanel1, cellButtons1, 1);
        
        centerPanel.add(player1Container, gbc);
        
        // Player 2 Board Container
        gbc.gridx = 1;
        
        JPanel player2Container = new JPanel(new BorderLayout(0, 8));
        player2Container.setOpaque(false);
        player2Container.setBorder(new LineBorder(new Color(153, 102, 51), 4, true));
        
        player2NameLabel = new JLabel(p2Name + "'s Board", SwingConstants.CENTER);
        player2NameLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        player2NameLabel.setForeground(Color.WHITE);
        player2NameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        player2Container.add(player2NameLabel, BorderLayout.NORTH);
        
        JPanel board2Wrapper = new JPanel(new GridBagLayout());
        board2Wrapper.setOpaque(false);
        board2Wrapper.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        
        boardPanel2 = new JPanel(new GridLayout(boardSize, boardSize, 1, 1));
        boardPanel2.setBackground(new Color(20, 20, 20));
        boardPanel2.setBorder(new LineBorder(new Color(30, 30, 30), 2));
        
        board2Wrapper.add(boardPanel2, innerGbc);
        
        player2Container.add(board2Wrapper, BorderLayout.CENTER);
        initializeBoardButtons(boardPanel2, cellButtons2, 2);
        
        centerPanel.add(player2Container, gbc);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // ==================== BOTTOM - CONTROLS ====================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 20, 20));
        
        JButton pauseBtn = createStyledButton("Pause Game");
        pauseBtn.addActionListener(e -> controller.pauseGame());
        bottomPanel.add(pauseBtn);
        
        JButton quitBtn = createStyledButton("Quit to Menu");
        quitBtn.addActionListener(e -> controller.quitToMenu());
        bottomPanel.add(quitBtn);
        
        bottomPanel.add(Box.createHorizontalStrut(30));
        
        statusLabel = new JLabel("Game Started!");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        statusLabel.setForeground(new Color(0, 255, 150));
        bottomPanel.add(statusLabel);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Center frame and make it resizable
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(1200, 700));
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = new Color(15, 25, 30);
                Color c2 = new Color(15, 35, 45);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            }
        };
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 20));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(1000, 90));
        
        // Score
        JPanel scorePanel = createInfoItem("Score:", "0", new Color(0, 255, 128));
        scoreLabel = (JLabel) scorePanel.getComponent(1);
        panel.add(scorePanel);
        
        // Lives
        JPanel livesPanel = createInfoItem("Lives:", "10", new Color(100, 255, 100));
        livesLabel = (JLabel) livesPanel.getComponent(1);
        panel.add(livesPanel);
        
        // Current Turn
        JPanel turnPanel = createInfoItem("Current Turn:", "Player 1", new Color(255, 215, 0));
        currentTurnLabel = (JLabel) turnPanel.getComponent(1);
        panel.add(turnPanel);
        
        return panel;
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
    
    private void initializeBoardButtons(JPanel boardPanel, JButton[][] buttons, int playerNum) {
        // Dynamic font size based on board size
        int fontSize = switch (boardSize) {
            case 9 -> 20;
            case 13 -> 16;
            case 16 -> 14;
            default -> 16;
        };
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                final int r = row;
                final int c = col;
                
                JButton btn = new JButton();
                btn.setBackground(COLOR_HIDDEN);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Tahoma", Font.BOLD, fontSize));
                btn.setFocusPainted(false);
                
                // FIXED: Use constant 2px border - only change color on hover
                btn.setBorder(new LineBorder(new Color(40, 50, 60), 2));
                btn.setMargin(new Insets(0, 0, 0, 0));
                
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            controller.onCellClick(playerNum, r, c);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            controller.onCellRightClick(playerNum, r, c);
                        }
                    }
                    
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (btn.isEnabled()) {
                            // FIXED: Keep same border thickness, just change color
                            btn.setBorder(new LineBorder(new Color(100, 200, 150), 2));
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (btn.isEnabled()) {
                            // FIXED: Restore original border color, same thickness
                            btn.setBorder(new LineBorder(new Color(40, 50, 60), 2));
                        }
                    }
                });
                
                buttons[row][col] = btn;
                boardPanel.add(btn);
            }
        }
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
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(30, 100, 75));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(20, 80, 60));
            }
        });
        
        return btn;
    }
    
    // ==================== PUBLIC UPDATE METHODS ====================
    
    public void updateCell(int playerNum, int row, int col, Cell cell, String cellTypeLabel) {
        JButton btn = (playerNum == 1) ? cellButtons1[row][col] : cellButtons2[row][col];
        
        int fontSize = switch (boardSize) {
            case 9 -> 20;
            case 13 -> 16;
            case 16 -> 14;
            default -> 16;
        };
        
        if (cell.isRevealed()) {
            if (cell.isMine()) {
                btn.setText("M");
                btn.setBackground(COLOR_MINE);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Tahoma", Font.BOLD, fontSize + 2));
            } else if (cellTypeLabel != null && !cellTypeLabel.isEmpty()) {
                switch (cellTypeLabel) {
                    case "Q":
                        btn.setText("Q");
                        btn.setBackground(COLOR_QUESTION);
                        btn.setForeground(Color.BLACK);
                        btn.setFont(new Font("Tahoma", Font.BOLD, fontSize + 2));
                        break;
                    case "S":
                        btn.setText("S");
                        btn.setBackground(COLOR_SURPRISE);
                        btn.setForeground(Color.WHITE);
                        btn.setFont(new Font("Tahoma", Font.BOLD, fontSize + 2));
                        break;
                    default:
                        btn.setText(cellTypeLabel);
                        btn.setBackground(COLOR_SAFE);
                        btn.setForeground(getNumberColor(Integer.parseInt(cellTypeLabel)));
                        btn.setFont(new Font("Tahoma", Font.BOLD, fontSize));
                }
            } else if (cell.getNeighborMines() > 0) {
                btn.setText(String.valueOf(cell.getNeighborMines()));
                btn.setBackground(COLOR_SAFE);
                btn.setForeground(getNumberColor(cell.getNeighborMines()));
                btn.setFont(new Font("Tahoma", Font.BOLD, fontSize));
            } else {
                btn.setText("");
                btn.setBackground(new Color(200, 220, 200));
            }
            btn.setEnabled(false);
            btn.setBorder(new LineBorder(new Color(30, 30, 30), 1));
        } else if (cell.isFlagged()) {
            btn.setText("F");
            btn.setBackground(COLOR_FLAGGED);
            btn.setForeground(Color.YELLOW);
            btn.setFont(new Font("Tahoma", Font.BOLD, fontSize + 2));
            btn.setEnabled(true);
        } else {
            btn.setText("");
            btn.setBackground(COLOR_HIDDEN);
            btn.setEnabled(true);
        }
    }
    
    public void updateScore(int score) {
        scoreLabel.setText(String.valueOf(score));
    }
    
    public void updateLives(int lives) {
        livesLabel.setText(String.valueOf(lives));
        if (lives <= 3) {
            livesLabel.setForeground(new Color(255, 50, 50));
        } else if (lives <= 5) {
            livesLabel.setForeground(new Color(255, 150, 50));
        } else {
            livesLabel.setForeground(new Color(100, 255, 100));
        }
    }
    
    public void updateCurrentTurn(String playerName) {
        currentTurnLabel.setText(playerName);
    }
    
    public void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    public void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showQuestionDialog(String question, String[] options) {
        JOptionPane.showOptionDialog(
            frame,
            question,
            "Answer the Question",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
    }
    
    private Color getNumberColor(int num) {
        return switch (num) {
            case 1 -> new Color(0, 0, 255);
            case 2 -> new Color(0, 128, 0);
            case 3 -> new Color(255, 0, 0);
            case 4 -> new Color(0, 0, 128);
            case 5 -> new Color(128, 0, 0);
            case 6 -> new Color(0, 128, 128);
            case 7 -> new Color(0, 0, 0);
            case 8 -> new Color(128, 128, 128);
            default -> Color.BLACK;
        };
    }
}
