package view;

import model.Questions;
import model.SysData;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class QuestionsManager {

    private JFrame frame;
    private JTable table;
    private QuestionsTableModel tableModel;

    public QuestionsManager() {
        initialize();
    }

    public void show() {
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
    }

    private void initialize() {
        int W = 900;
        int H = 600;
        frame = new JFrame("Questions Manager");
        frame.setBounds(150, 150, W, H);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // background gradient panel similar to other views
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
        bg.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.setContentPane(bg);

        JLabel title = new JLabel("Questions Manager", SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        bg.add(title, BorderLayout.NORTH);

        // center: table in scroll pane
        tableModel = new QuestionsTableModel(SysData.getAllQuestions());
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        bg.add(scrollPane, BorderLayout.CENTER);

        // bottom: buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton removeBtn = new JButton("Remove");
        JButton saveBtn = new JButton("Save to CSV");
        JButton closeBtn = new JButton("Close");

        styleButton(addBtn);
        styleButton(editBtn);
        styleButton(removeBtn);
        styleButton(saveBtn);
        styleButton(closeBtn);

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(closeBtn);

        bg.add(buttonPanel, BorderLayout.SOUTH);

        // button actions
        addBtn.addActionListener(e -> onAddQuestion());
        editBtn.addActionListener(e -> onEditQuestion());
        removeBtn.addActionListener(e -> onRemoveQuestion());
        saveBtn.addActionListener(e -> onSave());
        closeBtn.addActionListener(e -> frame.dispose());
    }

    private void styleButton(JButton b) {
        b.setForeground(new Color(220, 235, 230));
        b.setFont(new Font("Tahoma", Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(true);
        b.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 120)));
    }

    // ----------------- CRUD handlers -----------------

    private void onAddQuestion() {
        Questions q = showQuestionEditDialog(null);
        if (q != null) {
            SysData.getAllQuestions().add(q);
            tableModel.fireTableDataChanged();
        }
    }

    private void onEditQuestion() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Select a question to edit.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Questions existing = SysData.getAllQuestions().get(row);
        Questions updated = showQuestionEditDialog(existing);
        if (updated != null) {
            // values are already changed inside existing object
            tableModel.fireTableRowsUpdated(row, row);
        }
    }

    private void onRemoveQuestion() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Select a question to remove.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Delete selected question?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SysData.getAllQuestions().remove(row);
            tableModel.fireTableDataChanged();
        }
    }

    private void onSave() {
        SysData.saveQuestions();
        JOptionPane.showMessageDialog(frame, "Questions saved to CSV.", "Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Dialog used for both add and edit
    private Questions showQuestionEditDialog(Questions existing) {
        JDialog dialog = new JDialog(frame, true);
        dialog.setTitle(existing == null ? "Add Question" : "Edit Question");
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new GridLayout(8, 2, 5, 5));

        JTextField idField = new JTextField();
        idField.setEditable(false);
        JTextField textField = new JTextField();
        JTextField diffField = new JTextField();
        JTextField aField = new JTextField();
        JTextField bField = new JTextField();
        JTextField cField = new JTextField();
        JTextField dField = new JTextField();
        JTextField ansField = new JTextField();

        dialog.add(new JLabel("ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Question:"));
        dialog.add(textField);
        dialog.add(new JLabel("Difficulty (1-4):"));
        dialog.add(diffField);
        dialog.add(new JLabel("Option A:"));
        dialog.add(aField);
        dialog.add(new JLabel("Option B:"));
        dialog.add(bField);
        dialog.add(new JLabel("Option C:"));
        dialog.add(cField);
        dialog.add(new JLabel("Option D:"));
        dialog.add(dField);
        dialog.add(new JLabel("Correct Answer (A/B/C/D):"));
        dialog.add(ansField);

        if (existing != null) {
            idField.setText(String.valueOf(existing.getId()));
            textField.setText(existing.getText());
            diffField.setText(String.valueOf(existing.getDifficulty()));
            aField.setText(existing.getOptA());
            bField.setText(existing.getOptB());
            cField.setText(existing.getOptC());
            dField.setText(existing.getOptD());
            ansField.setText(existing.getCorrectAnswer());
        } else {
            idField.setText(String.valueOf(SysData.getNextQuestionId()));
        }

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        styleButton(okBtn);
        styleButton(cancelBtn);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(okBtn);
        bottom.add(cancelBtn);

        // wrap main form + bottom
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(dialog.getContentPane(), BorderLayout.CENTER);
        wrapper.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(wrapper);

        final Questions[] result = new Questions[1];

        okBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String text = textField.getText().trim();
                int diff = Integer.parseInt(diffField.getText().trim());
                String a = aField.getText().trim();
                String b = bField.getText().trim();
                String c = cField.getText().trim();
                String d = dField.getText().trim();
                String ans = ansField.getText().trim().toUpperCase();

                if (!(ans.equals("A") || ans.equals("B") || ans.equals("C") || ans.equals("D"))) {
                    JOptionPane.showMessageDialog(dialog, "Correct answer must be A, B, C, or D.",
                            "Invalid Answer", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (diff < 1 || diff > 4) {
                    JOptionPane.showMessageDialog(dialog, "Difficulty must be between 1 and 4.",
                            "Invalid Difficulty", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (existing == null) {
                    result[0] = new Questions(id, text, diff, a, b, c, d, ans);
                } else {
                    existing.setText(text);
                    existing.setDifficulty(diff);
                    existing.setOptA(a);
                    existing.setOptB(b);
                    existing.setOptC(c);
                    existing.setOptD(d);
                    existing.setCorrectAnswer(ans);
                    result[0] = existing;
                }

                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID and Difficulty must be numbers.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });

        dialog.setVisible(true);
        return result[0];
    }

    // ----------------- Table model -----------------

    private static class QuestionsTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID", "Question", "Difficulty", "A", "B", "C", "D", "Correct"
        };
        private final List<Questions> data;

        public QuestionsTableModel(List<Questions> data) {
            this.data = data;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Questions q = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> q.getId();
                case 1 -> q.getText();
                case 2 -> q.getDifficulty();
                case 3 -> q.getOptA();
                case 4 -> q.getOptB();
                case 5 -> q.getOptC();
                case 6 -> q.getOptD();
                case 7 -> q.getCorrectAnswer();
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // editing via dialog only
        }
    }
}
