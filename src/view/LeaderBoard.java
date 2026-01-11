package view;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import model.History;
import model.SysData;

public class LeaderBoard {

    private JFrame frame;
    private Runnable onCloseCallback;
    private java.util.List<History> allHistories;
    private Map<String, PlayerStats> playerStatsMap;

    public LeaderBoard() {
        this(null);
    }

    public LeaderBoard(Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
        this.allHistories = SysData.getAllHistories();
        this.playerStatsMap = new HashMap<>();
        aggregatePlayerStats();
        initialize();
    }

    public void show() {
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    // Aggregate statistics by player name
    private void aggregatePlayerStats() {
        for (History h : allHistories) {
            String playerName = h.getUsername() != null && !h.getUsername().isEmpty() 
                ? h.getUsername() 
                : h.getPlayer1Name();
            
            playerStatsMap.putIfAbsent(playerName, new PlayerStats(playerName));
            PlayerStats stats = playerStatsMap.get(playerName);
            
            stats.addGame(h);
        }
    }

    private void initialize() {
        frame = new JFrame("LeaderBoard");
        frame.setSize(1030, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(true);
        // set app icon for taskbar and window
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadAppIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (onCloseCallback != null) {
                    onCloseCallback.run();
                }
            }
        });

        // Background gradient panel
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
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        frame.setContentPane(bg);

        // Header panel with title and controls
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Title
        JLabel title = new JLabel("LeaderBoard - Top 5", SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(title);

        // Control panel: Sort dropdown - positioned below title (centered)
        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setForeground(new Color(0, 200, 170));
        sortLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        controlPanel.add(sortLabel);

        String[] sortOptions = {
            "Most Wins",
            "Most Games Played",
            "Highest Win Ratio",
            "Highest Average Score",
            "Total Time Played",
            "Highest Question Accuracy",
            "Most Surprises Triggered"
        };

        JComboBox<String> sortDropdown = new JComboBox<>(sortOptions);
        sortDropdown.setFont(new Font("Tahoma", Font.PLAIN, 12));
        sortDropdown.setBackground(new Color(25, 60, 55));
        sortDropdown.setForeground(new Color(0, 200, 170));
        sortDropdown.setFocusable(false);
        sortDropdown.setPreferredSize(new Dimension(220, 30));
        controlPanel.add(sortDropdown);

        headerPanel.add(controlPanel);
        bg.add(headerPanel, BorderLayout.NORTH);

        // Cards panel - horizontal layout for pedestal (centered, no scroll)
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        cardsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Function to load cards in pedestal style (centered)
        Runnable loadCards = () -> {
            cardsPanel.removeAll();
            java.util.List<PlayerStats> topPlayers = getTopPlayers(sortDropdown.getSelectedIndex());
            int selectedCategory = sortDropdown.getSelectedIndex();
            
            // Add top spacing
            cardsPanel.add(Box.createVerticalStrut(20));
            
            // Pedestal container - horizontal layout (centered)
            JPanel pedestalPanel = new JPanel();
            pedestalPanel.setLayout(new BoxLayout(pedestalPanel, BoxLayout.X_AXIS));
            pedestalPanel.setOpaque(false);
            pedestalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
            pedestalPanel.setPreferredSize(new Dimension(1050, 400));
            pedestalPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Arrange players: 5, 2, 1 (tallest in center), 3, 4
            int[] order = {4, 1, 0, 2, 3}; // indices for positions
            int[] heights = {200, 240, 300, 260, 220}; // heights for each position
            int[] topPositions = {80, 40, 0, 30, 60}; // top padding for each position
            
            for (int i = 0; i < Math.min(5, topPlayers.size()); i++) {
                int playerIndex = order[i];
                if (playerIndex < topPlayers.size()) {
                    PlayerStats player = topPlayers.get(playerIndex);
                    int rank = playerIndex + 1;
                    JPanel pedestal = createPedestalCard(player, rank, heights[i], selectedCategory);
                    pedestalPanel.add(Box.createHorizontalStrut(8));
                    pedestalPanel.add(pedestal);
                }
            }
            pedestalPanel.add(Box.createHorizontalGlue());
            
            cardsPanel.add(pedestalPanel);
            cardsPanel.add(Box.createVerticalStrut(30));
            cardsPanel.add(Box.createVerticalGlue());
            cardsPanel.revalidate();
            cardsPanel.repaint();
        };

        // Initial load
        loadCards.run();

        // Add listener for dropdown changes
        sortDropdown.addActionListener(e -> loadCards.run());

        bg.add(cardsPanel, BorderLayout.CENTER);

        // Back button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        backBtn.setBackground(new Color(180, 50, 50));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 100, 100), 2));
        backBtn.setPreferredSize(new Dimension(100, 35));
        backBtn.addActionListener(e -> close());

        buttonPanel.add(backBtn);
        bg.add(buttonPanel, BorderLayout.SOUTH);
    }

    private java.util.List<PlayerStats> getTopPlayers(int sortType) {
        java.util.List<PlayerStats> players = new ArrayList<>(playerStatsMap.values());

        switch (sortType) {
            case 0: // Most Wins
                players.sort((a, b) -> Integer.compare(b.wins, a.wins));
                break;
            case 1: // Most Games Played
                players.sort((a, b) -> Integer.compare(b.totalGames, a.totalGames));
                break;
            case 2: // Highest Win Ratio
                players.sort((a, b) -> Double.compare(b.getWinRatio(), a.getWinRatio()));
                break;
            case 3: // Highest Average Score
                players.sort((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()));
                break;
            case 4: // Total Time Played
                players.sort((a, b) -> Long.compare(b.totalDurationSeconds, a.totalDurationSeconds));
                break;
            case 5: // Highest Question Accuracy
                players.sort((a, b) -> Double.compare(b.getQuestionAccuracy(), a.getQuestionAccuracy()));
                break;
            case 6: // Most Surprises Triggered
                players.sort((a, b) -> Integer.compare(b.totalSurprises, a.totalSurprises));
                break;
            default:
                players.sort((a, b) -> Integer.compare(b.wins, a.wins));
        }

        // Return top 5
        return players.stream().limit(5).toList();
    }
    private JPanel createPlayerCard(PlayerStats player, int rank) {
        // This method is kept for compatibility but not used in pedestal mode
        return new JPanel();
    }

    private JPanel createPedestalCard(PlayerStats player, int rank, int height, int sortCategory) {
        // Create a vertical pedestal card for horizontal display
        JPanel pedestal = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Rank-based color
                Color bgColor;
                Color borderColor;
                if (rank == 1) {
                    bgColor = new Color(80, 80, 40);
                    borderColor = new Color(255, 215, 0); // Gold
                } else if (rank == 2) {
                    bgColor = new Color(60, 70, 75);
                    borderColor = new Color(192, 192, 192); // Silver
                } else if (rank == 3) {
                    bgColor = new Color(75, 55, 40);
                    borderColor = new Color(205, 127, 50); // Bronze
                } else if (rank == 4) {
                    bgColor = new Color(80, 40, 40);
                    borderColor = new Color(255, 100, 100); // Red
                } else {
                    bgColor = new Color(40, 80, 60);
                    borderColor = new Color(100, 255, 130); // Green
                }

                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        pedestal.setLayout(new BoxLayout(pedestal, BoxLayout.Y_AXIS));
        pedestal.setOpaque(false);
        pedestal.setMaximumSize(new Dimension(180, height));
        pedestal.setPreferredSize(new Dimension(180, height));
        pedestal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Rank label (large)
        JLabel rankLabel = new JLabel("#" + rank);
        rankLabel.setForeground(new Color(255, 215, 0));
        rankLabel.setFont(new Font("Tahoma", Font.BOLD, 56));
        rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pedestal.add(rankLabel);

        // Player name
        JLabel nameLabel = new JLabel(player.playerName);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pedestal.add(Box.createVerticalStrut(8));
        pedestal.add(nameLabel);

        pedestal.add(Box.createVerticalStrut(12));

        // Category-specific details
        JLabel categoryLabel = new JLabel();
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        categoryLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
        
        JLabel valueLabel = new JLabel();
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setFont(new Font("Tahoma", Font.BOLD, 15));

        switch (sortCategory) {
            case 0: // Most Wins
                categoryLabel.setText("Wins");
                categoryLabel.setForeground(new Color(100, 255, 150));
                valueLabel.setText(String.valueOf(player.wins));
                valueLabel.setForeground(new Color(100, 255, 150));
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(3));
                pedestal.add(valueLabel);
                break;
            case 1: // Most Games Played
                categoryLabel.setText("Games");
                categoryLabel.setForeground(new Color(150, 200, 255));
                valueLabel.setText(String.valueOf(player.totalGames));
                valueLabel.setForeground(new Color(150, 200, 255));
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(3));
                pedestal.add(valueLabel);
                break;
            case 2: // Highest Win Ratio
                categoryLabel.setText("Win Rate");
                categoryLabel.setForeground(new Color(255, 200, 100));
                valueLabel.setText(String.format("%.1f%%", player.getWinRatio() * 100));
                valueLabel.setForeground(new Color(255, 200, 100));
                JLabel gamesInfoLabel = new JLabel("(" + player.totalGames + " games)");
                gamesInfoLabel.setForeground(new Color(200, 200, 100));
                gamesInfoLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
                gamesInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(valueLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(gamesInfoLabel);
                break;
            case 3: // Highest Average Score
                categoryLabel.setText("Avg Score");
                categoryLabel.setForeground(new Color(150, 220, 200));
                valueLabel.setText(String.format("%.0f", player.getAverageScore()));
                valueLabel.setForeground(new Color(150, 220, 200));
                JLabel gameCountLabel = new JLabel("(" + player.totalGames + " games)");
                gameCountLabel.setForeground(new Color(120, 200, 180));
                gameCountLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
                gameCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(valueLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(gameCountLabel);
                break;
            case 4: // Total Time Played
                categoryLabel.setText("Time Played");
                categoryLabel.setForeground(new Color(200, 150, 255));
                String timeFormatted = formatSeconds(player.totalDurationSeconds);
                valueLabel.setText(timeFormatted);
                valueLabel.setForeground(new Color(200, 150, 255));
                JLabel timeUnitLabel = new JLabel("(HH:MM:SS)");
                timeUnitLabel.setForeground(new Color(180, 130, 235));
                timeUnitLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
                timeUnitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(valueLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(timeUnitLabel);
                break;
            case 5: // Highest Question Accuracy
                categoryLabel.setText("Accuracy");
                categoryLabel.setForeground(new Color(100, 200, 255));
                valueLabel.setText(String.format("%.1f%%", player.getQuestionAccuracy() * 100));
                valueLabel.setForeground(new Color(100, 200, 255));
                JLabel qAttemptLabel = new JLabel(String.valueOf(player.totalCorrectQuestions) + "/" + String.valueOf(player.totalQuestionsAnswered));
                qAttemptLabel.setForeground(new Color(100, 180, 255));
                qAttemptLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
                qAttemptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(valueLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(qAttemptLabel);
                break;
            case 6: // Most Surprises Triggered
                categoryLabel.setText("Surprises");
                categoryLabel.setForeground(new Color(255, 200, 100));
                valueLabel.setText(String.valueOf(player.totalSurprises));
                valueLabel.setForeground(new Color(255, 200, 100));
                JLabel posNegLabel = new JLabel(String.format("+%d/-%d", player.totalPositiveSurprises, player.totalNegativeSurprises));
                posNegLabel.setForeground(new Color(235, 180, 100));
                posNegLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
                posNegLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(valueLabel);
                pedestal.add(Box.createVerticalStrut(2));
                pedestal.add(posNegLabel);
                break;
            default:
                categoryLabel.setText("Wins");
                categoryLabel.setForeground(new Color(100, 255, 150));
                valueLabel.setText(String.valueOf(player.wins));
                valueLabel.setForeground(new Color(100, 255, 150));
                pedestal.add(categoryLabel);
                pedestal.add(Box.createVerticalStrut(3));
                pedestal.add(valueLabel);
        }

        pedestal.add(Box.createVerticalGlue());

        return pedestal;
    }

    private String formatSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, secs);
    }

    // Inner class to hold aggregated player statistics
    static class PlayerStats {
        String playerName;
        int totalGames = 0;
        int wins = 0;
        int losses = 0;
        int totalScore = 0;
        long totalDurationSeconds = 0;
        int totalMinesHit = 0;
        int totalQuestionsAnswered = 0;
        int totalCorrectQuestions = 0;
        int totalSurprises = 0;
        int totalPositiveSurprises = 0;
        int totalNegativeSurprises = 0;

        PlayerStats(String name) {
            this.playerName = name;
        }

        void addGame(History h) {
            totalGames++;
            if (h.isWin()) {
                wins++;
            } else {
                losses++;
            }
            totalScore += h.getFinalScore();
            totalDurationSeconds += h.getDurationSeconds();
            totalMinesHit += h.getMinesHit();
            totalQuestionsAnswered += h.getQuestionsAnswered();
            totalCorrectQuestions += h.getCorrectQuestions();
            totalSurprises += h.getSurprisesTriggered();
            totalPositiveSurprises += h.getPositiveSurprises();
            totalNegativeSurprises += h.getNegativeSurprises();
        }

        double getWinRatio() {
            return totalGames > 0 ? (double) wins / totalGames : 0;
        }

        double getAverageScore() {
            return totalGames > 0 ? (double) totalScore / totalGames : 0;
        }

        double getQuestionAccuracy() {
            return totalQuestionsAnswered > 0 ? (double) totalCorrectQuestions / totalQuestionsAnswered : 0;
        }

        public int getSurprisesTriggered() {
            return totalSurprises;
        }
    }
}
