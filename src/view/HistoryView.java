package view;

import model.History;
import model.SysData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryView {

    private final JFrame frame;
    private final JTable table;
    private final String currentUser;
    private final boolean isAdmin;

    public HistoryView(String currentUser, boolean isAdmin) {
        this.currentUser = currentUser;
        this.isAdmin = isAdmin;
        frame = new JFrame("Game History");
        frame.setSize(900, 450); // wider
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.setBackground(new Color(15, 25, 30));
        frame.setContentPane(main);

        JLabel title = new JLabel("Game History", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setForeground(new Color(0, 200, 255));
        main.add(title, BorderLayout.NORTH);

        String[] columns = {
                "Date & Time",
                "Player 1",
                "Player 2",
                "Difficulty",
                "Result",
                "Score",
                "Time (sec)",
                "Mines Hit",
                "Questions Answered"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));

        // Column widths so last column is readable
        table.getColumnModel().getColumn(0).setPreferredWidth(130); // Date & Time
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Player 1
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Player 2
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Difficulty
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Result
        table.getColumnModel().getColumn(5).setPreferredWidth(60);  // Score
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Time (sec)
        table.getColumnModel().getColumn(7).setPreferredWidth(70);  // Mines Hit
        table.getColumnModel().getColumn(8).setPreferredWidth(140); // Questions Answered

        JScrollPane scroll = new JScrollPane(table);
        main.add(scroll, BorderLayout.CENTER);

        loadData();

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> frame.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(closeBtn);
        main.add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        List<History> histories = SysData.getAllHistories();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (History h : histories) {
            if (!isAdmin) {
                if (h.getUsername() == null ||
                        !h.getUsername().equalsIgnoreCase(currentUser)) {
                    continue;
                }
            }
            String dateTime = h.getDateTime() != null ? h.getDateTime().format(fmt) : "";
            String result = h.isWin() ? "Win" : "Lose";
            Object[] row = {
                    dateTime,
                    h.getPlayer1Name(),
                    h.getPlayer2Name(),
                    h.getDifficulty(),
                    result,
                    h.getFinalScore(),
                    h.getDurationSeconds(),
                    h.getMinesHit(),
                    h.getQuestionsAnswered()
            };
            model.addRow(row);
        }
    }

    public void show() {
        frame.setVisible(true);
    }
}
