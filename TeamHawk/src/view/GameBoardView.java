package view;

import controller.Main.GameBoardController;
import model.Cell;

import javax.swing.*;
import javax.swing.border.Border;
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

    // Containers + mines-left
    private JPanel player1Container;
    private JPanel player2Container;
    private JLabel player1MinesLeftLabel;
    private JLabel player2MinesLeftLabel;

    private int boardSize;

    // Colors
    private static final Color COLOR_HIDDEN   = new Color(60, 80, 95);
    private static final Color COLOR_MINE     = new Color(220, 50, 50);
    private static final Color COLOR_SURPRISE = new Color(180, 100, 255);
    private static final Color COLOR_QUESTION = new Color(255, 215, 0);
    private static final Color COLOR_SAFE     = new Color(140, 200, 140);
    private static final Color COLOR_FLAGGED  = new Color(90, 110, 130);

    // Neon theme
    private static final Color NEON_GREEN  = new Color(57, 255, 20);   // P1
    private static final Color NEON_ORANGE = new Color(255, 140, 0);   // P2
    private static final Color DIM_COLOR   = new Color(10, 20, 30);

    private static final Color DIM_BORDER_P1_COL = new Color(51, 153, 102, 100);
    private static final Color DIM_BORDER_P2_COL = new Color(153, 102, 51, 100);

    private static final Border INACTIVE_BORDER_P1     = new LineBorder(DIM_BORDER_P1_COL, 4, true);
    private static final Border INACTIVE_BORDER_P2     = new LineBorder(DIM_BORDER_P2_COL, 4, true);
    private static final Border ACTIVE_BORDER_P1_NEON  = new LineBorder(NEON_GREEN, 5, true);
    private static final Border ACTIVE_BORDER_P2_NEON  = new LineBorder(NEON_ORANGE, 5, true);

    // ---------- ctor ----------

    /**
     * @wbp.parser.entryPoint
     */
    public GameBoardView(GameBoardController controller, String player1Name, String player2Name, int boardSize) {
        this.controller = controller;
        this.boardSize = (boardSize == 0) ? 9 : boardSize;

        this.cellButtons1 = new JButton[this.boardSize][this.boardSize];
        this.cellButtons2 = new JButton[this.boardSize][this.boardSize];

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

        // Background gradient
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

        // Top info bar
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        topWrapper.add(createInfoPanel(), BorderLayout.CENTER);
        mainPanel.add(topWrapper, BorderLayout.NORTH);

        // Center – two boards
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 10, 0, 10);

        // Player 1
        gbc.gridx = 0;
        gbc.gridy = 0;

        player1Container = new JPanel(new BorderLayout(0, 8));
        player1Container.setOpaque(false);
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

        initializeBoardButtons(boardPanel1, cellButtons1, 1);
        centerPanel.add(player1Container, gbc);

        // Player 2
        gbc.gridx = 1;

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

        initializeBoardButtons(boardPanel2, cellButtons2, 2);
        centerPanel.add(player2Container, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setOpaque(false);

        JButton pauseBtn = createStyledButton("Pause Game");
        pauseBtn.addActionListener(e -> controller.pauseGame());
        bottomPanel.add(pauseBtn);

        JButton quitBtn = createStyledButton("Quit to Menu");
        quitBtn.addActionListener(e -> controller.quitToMenu());
        bottomPanel.add(quitBtn);

        statusLabel = new JLabel("Game Started!");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        statusLabel.setForeground(new Color(0, 255, 150));
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(statusLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(1200, 700));

        updateTurnVisuals(1);
    }

    private JPanel createInfoPanel() {
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

        JPanel livesPanel = createInfoItem("Lives:", "10", new Color(100, 255, 100));
        livesLabel = (JLabel) livesPanel.getComponent(1);
        panel.add(livesPanel);

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
        int fontSize = switch (boardSize) {
            case 9  -> 20;
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
                btn.setBorder(new LineBorder(new Color(40, 50, 60), 2));

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
                            btn.setBorder(new LineBorder(new Color(100, 200, 150), 2));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (btn.isEnabled()) {
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
        return btn;
    }

    // ---------- Public update methods ----------

    public void updateMinesLeft(int playerNum, int count) {
        if (playerNum == 1) {
            player1MinesLeftLabel.setText("Mines Left: " + count);
        } else {
            player2MinesLeftLabel.setText("Mines Left: " + count);
        }
    }

    public void updateTurnVisuals(int currentTurn) {
        if (currentTurn == 1) {
            player1Container.setBorder(ACTIVE_BORDER_P1_NEON);
            player1NameLabel.setForeground(NEON_GREEN);
            player1MinesLeftLabel.setForeground(Color.WHITE);
            boardPanel1.setBackground(new Color(20, 20, 20));

            player2Container.setBorder(INACTIVE_BORDER_P2);
            player2NameLabel.setForeground(Color.LIGHT_GRAY);
            player2MinesLeftLabel.setForeground(Color.LIGHT_GRAY);
            boardPanel2.setBackground(DIM_COLOR);
        } else {
            player2Container.setBorder(ACTIVE_BORDER_P2_NEON);
            player2NameLabel.setForeground(NEON_ORANGE);
            player2MinesLeftLabel.setForeground(Color.WHITE);
            boardPanel2.setBackground(new Color(20, 20, 20));

            player1Container.setBorder(INACTIVE_BORDER_P1);
            player1NameLabel.setForeground(Color.LIGHT_GRAY);
            player1MinesLeftLabel.setForeground(Color.LIGHT_GRAY);
            boardPanel1.setBackground(DIM_COLOR);
        }
    }

    public void updateCell(int playerNum, int row, int col, Cell cell, String cellTypeLabel) {
        JButton btn = (playerNum == 1) ? cellButtons1[row][col] : cellButtons2[row][col];

        if (cell.isRevealed()) {
            if (cell.isMine()) {
                btn.setText("M");
                btn.setBackground(COLOR_MINE);
                btn.setForeground(Color.WHITE);
            } else if (cell.isQuestion()) {
                btn.setText("Q");
                btn.setBackground(COLOR_QUESTION);
                btn.setForeground(Color.BLACK);
            } else if (cell.isSurprise()) {
                btn.setText("S");
                btn.setBackground(COLOR_SURPRISE);
                btn.setForeground(Color.WHITE);
            } else if (cellTypeLabel != null && !cellTypeLabel.isEmpty()) {
                // number cell
                btn.setText(cellTypeLabel);
                btn.setBackground(COLOR_SAFE);
                try {
                    int num = Integer.parseInt(cellTypeLabel);
                    btn.setForeground(getNumberColor(num));
                } catch (NumberFormatException ex) {
                    btn.setForeground(Color.BLACK);
                }
            } else {
                // empty revealed
                btn.setText("");
                btn.setBackground(new Color(200, 220, 200));
            }

            btn.setEnabled(false);
            btn.setBorder(new LineBorder(new Color(30, 30, 30), 1));
        } else if (cell.isFlagged()) {
            btn.setText("F");
            btn.setBackground(COLOR_FLAGGED);
            btn.setForeground(Color.YELLOW);
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
                options[1] // default = Answer Question
        );
    }
    // ---------- Mock main for WindowBuilder ----------

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                GameBoardController mock = new GameBoardController() {
                    public void onCellClick(int p, int r, int c) {}
                    public void onCellRightClick(int p, int r, int c) {}
                    public void pauseGame() {}
                    public void quitToMenu() {}
                };
                GameBoardView window = new GameBoardView(mock, "P1", "P2", 9);
                window.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
