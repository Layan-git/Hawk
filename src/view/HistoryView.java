package view;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.History;
import model.SysData;

public class HistoryView {

    private final JFrame frame;
    private final JTable table;
    private final String currentUser;
    private final boolean isAdmin;

    public HistoryView(String currentUser, boolean isAdmin) {
        this.currentUser = currentUser;
        this.isAdmin = isAdmin;
        frame = new JFrame("Game History");
        frame.setSize(1200, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);

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
        bg.setBounds(0, 0, 1200, 600);
        bg.setLayout(null);
        frame.setContentPane(bg);

        // Main container
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setBorder(null);
        card.setBounds(30, 30, 1140, 540);
        card.setLayout(new BorderLayout(10, 10));
        bg.add(card);

        // Title
        JLabel title = new JLabel("Game History");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        card.add(title, BorderLayout.NORTH);

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
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        table.setBackground(new Color(30, 40, 50));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 80, 100));
        table.getTableHeader().setBackground(new Color(20, 30, 40));
        table.getTableHeader().setForeground(new Color(0, 200, 170));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);
        table.getColumnModel().getColumn(8).setPreferredWidth(140);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBackground(new Color(20, 30, 40));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 51), 2));
        card.add(scroll, BorderLayout.CENTER);

        loadData();

        // Button panel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        RoundedButton closeBtn = new RoundedButton("Close");
        closeBtn.setBackground(new Color(120, 30, 30));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> frame.dispose());
        bottom.add(closeBtn);
        card.add(bottom, BorderLayout.SOUTH);
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