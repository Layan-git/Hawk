package view;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.History;
import model.SysData;

public class HistoryView {

    private final JFrame frame;
    private final String currentUser;
    private final boolean isAdmin;
    private JPanel cardsPanel;
    private JScrollPane scrollPane;

    public HistoryView(String currentUser, boolean isAdmin) {
        this.currentUser = currentUser;
        this.isAdmin = isAdmin;
        frame = new JFrame("Game History");
        frame.setSize(1400, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(true);
        // set app icon for taskbar and window
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadAppIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }
        frame.getContentPane().setLayout(new BorderLayout());

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
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        frame.setContentPane(bg);

        // Title
        JLabel title = new JLabel("Game History");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bg.add(title, BorderLayout.NORTH);

        // Left panel with sort on top and filter below
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        
        // Sort panel
        JPanel sortPanel = createSortPanel();
        leftPanel.add(sortPanel);
        
        // Filter panel
        JPanel filterPanel = createFilterPanel();
        leftPanel.add(filterPanel);
        leftPanel.add(Box.createVerticalGlue());
        
        bg.add(leftPanel, BorderLayout.WEST);

        // Cards panel with scroll
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);
        scrollPane.getVerticalScrollBar().setBackground(new Color(8, 45, 40));
        scrollPane.getVerticalScrollBar().setForeground(new Color(0, 200, 170));
        bg.add(scrollPane, BorderLayout.CENTER);

        loadData();

        // Button panel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        RoundedButton closeBtn = new RoundedButton("Close");
        closeBtn.setBackground(new Color(120, 30, 30));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> frame.dispose());
        bottom.add(closeBtn);
        bg.add(bottom, BorderLayout.SOUTH);
    }

    private List<History> allHistories = new ArrayList<>();  // Store all histories for filtering
    private JCheckBox winFilter, loseFilter;
    private JCheckBox easyFilter, mediumFilter, hardFilter, extremeFilter;
    
    // Sort variables
    private String currentSortBy = "Date";  // Default sort
    private boolean sortAscending = false;  // Default descending for date (newest first)
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 10, 5));
        filterPanel.setMaximumSize(new Dimension(180, 200));
        
        // Result filter
        JLabel resultLabel = new JLabel("Result:");
        resultLabel.setForeground(new Color(0, 200, 170));
        resultLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.add(resultLabel);
        
        winFilter = new JCheckBox("Wins");
        winFilter.setOpaque(false);
        winFilter.setForeground(new Color(100, 255, 150));
        winFilter.setSelected(true);
        winFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        winFilter.addActionListener(e -> loadData());
        filterPanel.add(winFilter);
        
        loseFilter = new JCheckBox("Losses");
        loseFilter.setOpaque(false);
        loseFilter.setForeground(new Color(255, 100, 100));
        loseFilter.setSelected(true);
        loseFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        loseFilter.addActionListener(e -> loadData());
        filterPanel.add(loseFilter);
        
        filterPanel.add(Box.createVerticalStrut(3));
        
        // Difficulty filter
        JLabel difficultyLabel = new JLabel("Difficulty:");
        difficultyLabel.setForeground(new Color(0, 200, 170));
        difficultyLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        difficultyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.add(difficultyLabel);
        
        easyFilter = new JCheckBox("Easy");
        easyFilter.setOpaque(false);
        easyFilter.setForeground(new Color(100, 200, 255));
        easyFilter.setSelected(true);
        easyFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        easyFilter.addActionListener(e -> loadData());
        filterPanel.add(easyFilter);
        
        mediumFilter = new JCheckBox("Medium");
        mediumFilter.setOpaque(false);
        mediumFilter.setForeground(new Color(255, 200, 100));
        mediumFilter.setSelected(true);
        mediumFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        mediumFilter.addActionListener(e -> loadData());
        filterPanel.add(mediumFilter);
        
        hardFilter = new JCheckBox("Hard");
        hardFilter.setOpaque(false);
        hardFilter.setForeground(new Color(255, 100, 100));
        hardFilter.setSelected(true);
        hardFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        hardFilter.addActionListener(e -> loadData());
        filterPanel.add(hardFilter);
        
        extremeFilter = new JCheckBox("Extreme");
        extremeFilter.setOpaque(false);
        extremeFilter.setForeground(new Color(200, 100, 255));
        extremeFilter.setSelected(true);
        extremeFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        extremeFilter.addActionListener(e -> loadData());
        filterPanel.add(extremeFilter);
        
        return filterPanel;
    }
    
    private JPanel createSortPanel() {
        JPanel sortPanel = new JPanel();
        sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.Y_AXIS));
        sortPanel.setOpaque(false);
        sortPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 5, 5));
        sortPanel.setMaximumSize(new Dimension(180, 70));
        
        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setForeground(new Color(0, 200, 170));
        sortLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        sortLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sortPanel.add(sortLabel);
        
        // Create dropdown menu
        String[] sortOptions = {
            "Date (Newest First)",
            "Date (Oldest First)",
            "Score (High to Low)",
            "Score (Low to High)",
            "Duration (Longest)",
            "Duration (Shortest)",
            "Difficulty (Easy to Hard)",
            "Difficulty (Hard to Easy)",
            "Result (Wins First)",
            "Result (Losses First)"
        };
        
        JComboBox<String> sortCombo = new JComboBox<>(sortOptions);
        sortCombo.setSelectedIndex(0);  // Default to Date (Newest First)
        sortCombo.setBackground(new Color(8, 45, 40));
        sortCombo.setForeground(Color.WHITE);
        sortCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sortCombo.setMaximumSize(new Dimension(180, 25));
        sortCombo.addActionListener(e -> {
            int selected = sortCombo.getSelectedIndex();
            handleSortSelection(selected);
        });
        
        sortPanel.add(sortCombo);
        sortPanel.add(Box.createVerticalStrut(10));
        
        return sortPanel;
    }
    
    private void handleSortSelection(int selection) {
        switch (selection) {
            case 0 -> { currentSortBy = "Date"; sortAscending = false; }
            case 1 -> { currentSortBy = "Date"; sortAscending = true; }
            case 2 -> { currentSortBy = "Score"; sortAscending = false; }
            case 3 -> { currentSortBy = "Score"; sortAscending = true; }
            case 4 -> { currentSortBy = "Duration"; sortAscending = false; }
            case 5 -> { currentSortBy = "Duration"; sortAscending = true; }
            case 6 -> { currentSortBy = "Difficulty"; sortAscending = true; }
            case 7 -> { currentSortBy = "Difficulty"; sortAscending = false; }
            case 8 -> { currentSortBy = "Result"; sortAscending = false; }
            case 9 -> { currentSortBy = "Result"; sortAscending = true; }
        }
        loadData();
    }

    private void loadData() {
        cardsPanel.removeAll();

        // reload from csv - gets any new games played
        SysData.reloadHistoriesFromCSV();
        allHistories = SysData.getAllHistories();
        
        // Filter the histories
        List<History> filteredHistories = new ArrayList<>();

        for (History h : allHistories) {
            // Check result filter
            boolean resultMatches = (h.isWin() && winFilter.isSelected()) || 
                                   (!h.isWin() && loseFilter.isSelected());
            if (!resultMatches) continue;
            
            // Check difficulty filter
            String difficulty = h.getDifficulty().toLowerCase();
            boolean diffMatches = (difficulty.equals("easy") && easyFilter.isSelected()) ||
                                 (difficulty.equals("medium") && mediumFilter.isSelected()) ||
                                 (difficulty.equals("hard") && hardFilter.isSelected()) ||
                                 (difficulty.equals("extreme") && extremeFilter.isSelected());
            if (!diffMatches) continue;
            
            filteredHistories.add(h);
        }
        
        // Sort the filtered histories
        sortHistories(filteredHistories);
        
        // Add sorted data as cards
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (History h : filteredHistories) {
            JPanel card = createHistoryCard(h, fmt);
            cardsPanel.add(card);
            cardsPanel.add(Box.createVerticalStrut(10));
        }
        
        cardsPanel.add(Box.createVerticalGlue());
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    private JPanel createHistoryCard(History h, DateTimeFormatter fmt) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = h.isWin() ? new Color(25, 70, 50) : new Color(70, 30, 30);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                g2.setColor(new Color(0, 200, 170));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setPreferredSize(new Dimension(800, 110));
        
        // Top row: Player names with "vs"
        JPanel topRow = new JPanel();
        topRow.setOpaque(false);
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        
        JLabel player1Label = new JLabel(h.getPlayer1Name());
        player1Label.setForeground(new Color(100, 200, 255));
        player1Label.setFont(new Font("Tahoma", Font.BOLD, 14));
        topRow.add(player1Label);
        
        topRow.add(Box.createHorizontalStrut(10));
        
        JLabel vsLabel = new JLabel("&");
        vsLabel.setForeground(Color.WHITE);
        vsLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        topRow.add(vsLabel);
        
        topRow.add(Box.createHorizontalStrut(10));
        
        JLabel player2Label = new JLabel(h.getPlayer2Name());
        player2Label.setForeground(new Color(100, 200, 255));
        player2Label.setFont(new Font("Tahoma", Font.BOLD, 14));
        topRow.add(player2Label);
        topRow.add(Box.createHorizontalGlue());
        
        card.add(topRow);
        
        // Middle row: Difficulty, Outcome
        JPanel middleRow = new JPanel();
        middleRow.setOpaque(false);
        middleRow.setLayout(new BoxLayout(middleRow, BoxLayout.X_AXIS));
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        
        Color diffColor = switch (h.getDifficulty().toLowerCase()) {
            case "easy" -> new Color(100, 200, 255);
            case "medium" -> new Color(255, 200, 100);
            case "hard" -> new Color(255, 100, 100);
            case "extreme" -> new Color(200, 100, 255);
            default -> Color.WHITE;
        };
        
        JLabel diffLabel = new JLabel("Difficulty: " + h.getDifficulty());
        diffLabel.setForeground(diffColor);
        diffLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        middleRow.add(diffLabel);
        middleRow.add(Box.createHorizontalStrut(40));
        
        String result = h.isWin() ? "WIN" : "LOSS";
        Color resultColor = h.isWin() ? new Color(100, 255, 150) : new Color(255, 100, 100);
        JLabel resultLabel = new JLabel(result);
        resultLabel.setForeground(resultColor);
        resultLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        middleRow.add(resultLabel);
        middleRow.add(Box.createHorizontalGlue());
        
        String dateTime = h.getDateTime() != null ? h.getDateTime().format(fmt) : "";
        JLabel dateLabel = new JLabel(dateTime);
        dateLabel.setForeground(new Color(0, 200, 170));
        dateLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        middleRow.add(dateLabel);
        
        card.add(middleRow);
        
        // Bottom row: Score, Lives, Duration
        JPanel bottomRow = new JPanel();
        bottomRow.setOpaque(false);
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        
        JLabel scoreLabel = new JLabel("Score: " + h.getFinalScore());
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bottomRow.add(scoreLabel);
        bottomRow.add(Box.createHorizontalStrut(40));
        
        JLabel livesLabel = new JLabel("Lives: " + h.getLivesRemaining());
        livesLabel.setForeground(Color.WHITE);
        livesLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bottomRow.add(livesLabel);
        bottomRow.add(Box.createHorizontalStrut(40));
        
        JLabel durationLabel = new JLabel("Duration: " + h.getDurationSeconds() + "s");
        durationLabel.setForeground(Color.WHITE);
        durationLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bottomRow.add(durationLabel);
        bottomRow.add(Box.createHorizontalGlue());
        
        card.add(bottomRow);
        
        return card;
    }
    
    private void sortHistories(List<History> histories) {
        switch (currentSortBy) {
            case "Date" -> {
                histories.sort((h1, h2) -> {
                    int cmp = h2.getDateTime().compareTo(h1.getDateTime());
                    return sortAscending ? -cmp : cmp;
                });
            }
            case "Score" -> {
                histories.sort((h1, h2) -> {
                    int cmp = Integer.compare(h1.getFinalScore(), h2.getFinalScore());
                    return sortAscending ? cmp : -cmp;
                });
            }
            case "Duration" -> {
                histories.sort((h1, h2) -> {
                    int cmp = Long.compare(h1.getDurationSeconds(), h2.getDurationSeconds());
                    return sortAscending ? cmp : -cmp;
                });
            }
            case "Difficulty" -> {
                histories.sort((h1, h2) -> {
                    int level1 = getDifficultyLevel(h1.getDifficulty());
                    int level2 = getDifficultyLevel(h2.getDifficulty());
                    int cmp = Integer.compare(level1, level2);
                    return sortAscending ? cmp : -cmp;
                });
            }
            case "Result" -> {
                histories.sort((h1, h2) -> {
                    boolean w1 = h1.isWin();
                    boolean w2 = h2.isWin();
                    int cmp = Boolean.compare(w1, w2);
                    return sortAscending ? cmp : -cmp;
                });
            }
        }
    }
    
    private int getDifficultyLevel(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 1;
            case "medium" -> 2;
            case "hard" -> 3;
            case "extreme" -> 4;
            default -> 0;
        };
    }

    public void show() {
        frame.setVisible(true);
    }

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
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
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