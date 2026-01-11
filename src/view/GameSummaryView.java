package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;


public class GameSummaryView {

    public interface SummaryController {
        void onPlayAgain();
        void onBackToMenu();
    }

    private final JFrame frame;

    public GameSummaryView(SummaryController controller,
                           String player1Name,
                           String player2Name,
                           String difficulty,
                           boolean win,
                           int finalScore,
                           int durationSeconds,
                           int minesHit,
                           int questionsAnswered,
                           int correctQuestions,
                           int wrongQuestions,
                           int surprisesTriggered,
                           int positiveSurprises,
                           int negativeSurprises,
                           int livesRemaining) {

        frame = new JFrame("Game Summary");
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // set app icon for taskbar and window
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadAppIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }

        JPanel main = new JPanel(new BorderLayout(0, 15));
        main.setBackground(new Color(15, 25, 30));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.setContentPane(main);

        JLabel title = new JLabel(win ? "Victory!" : "Game Over", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 26));
        title.setForeground(win ? new Color(0, 255, 150) : new Color(255, 120, 120));
        main.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(0, 2, 10, 8));
        center.setOpaque(false);

        addRow(center, "Player 1:", player1Name);
        addRow(center, "Player 2:", player2Name);
        addRow(center, "Difficulty:", difficulty);
        addRow(center, "Final Score:", String.valueOf(finalScore));
        addRow(center, "Time (sec):", String.valueOf(durationSeconds));
        addRow(center, "Lives Left:", String.valueOf(livesRemaining));
        addRow(center, "Mines Hit:", String.valueOf(minesHit));
        addRow(center, "Questions Answered:", String.valueOf(questionsAnswered));
        addRow(center, "Correct Questions:", String.valueOf(correctQuestions));
        addRow(center, "Wrong Questions:", String.valueOf(wrongQuestions));
        addRow(center, "Surprises Triggered:", String.valueOf(surprisesTriggered));
        addRow(center, "Positive Surprises:", String.valueOf(positiveSurprises));
        addRow(center, "Negative Surprises:", String.valueOf(negativeSurprises));

        main.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
        buttons.setOpaque(false);

        JButton playAgain = createButton("Play Again");
        playAgain.addActionListener(e -> {
            frame.dispose();
            controller.onPlayAgain();
        });

        JButton backMenu = createButton("Back to Menu");
        backMenu.addActionListener(e -> {
            frame.dispose();
            controller.onBackToMenu();
        });

        buttons.add(playAgain);
        buttons.add(backMenu);
        main.add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.BOLD, 14));
        l.setForeground(new Color(170, 220, 200));

        JLabel v = new JLabel(value);
        v.setFont(new Font("Tahoma", Font.PLAIN, 14));
        v.setForeground(Color.WHITE);

        panel.add(l);
        panel.add(v);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn.setForeground(new Color(220, 235, 230));
        btn.setBackground(new Color(20, 80, 60));
        btn.setBorder(new LineBorder(new Color(51, 102, 51), 2, true));
        btn.setFocusPainted(false);
        return btn;
    }

    public void show() {
        frame.setVisible(true);
    }
}