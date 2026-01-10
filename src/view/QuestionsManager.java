package view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.Questions;
import model.ResourceLoader;
import model.SysData;

public class QuestionsManager {

    private JFrame frame;
    private JPanel cardsPanel;
    private JScrollPane scrollPane;
    private Runnable onCloseCallback;
    private List<Questions> questions;

    public QuestionsManager() {
        this(null);
    }

    public QuestionsManager(Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
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

    private void initialize() {
        frame = new JFrame("Questions Manager");
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(true);
        // set app icon for taskbar and window
        java.awt.image.BufferedImage icon = ResourceLoader.loadAppIcon();
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

        // background gradient panel
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

        JLabel title = new JLabel("Questions Manager", SwingConstants.CENTER);
        title.setForeground(new Color(0, 200, 170));
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bg.add(title, BorderLayout.NORTH);

        // Cards panel with scroll
        // Reload questions from CSV to ensure we have the latest data
        SysData.reloadQuestionsFromCSV();
        questions = SysData.getAllQuestions();
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
        
        loadCards();
        bg.add(scrollPane, BorderLayout.CENTER);

        // bottom: buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton addBtn = new JButton("Add Question");
        JButton saveBtn = new JButton("Save to CSV");
        JButton closeBtn = new JButton("Back");

        // Add Question - Blue
        addBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        addBtn.setBackground(new Color(50, 100, 200));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 255), 2));
        addBtn.setPreferredSize(new Dimension(130, 35));
        
        // Save to CSV - Green
        saveBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        saveBtn.setBackground(new Color(50, 180, 80));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 255, 130), 2));
        saveBtn.setPreferredSize(new Dimension(130, 35));
        
        // Back/Close - Red
        closeBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        closeBtn.setBackground(new Color(180, 50, 50));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 100, 100), 2));
        closeBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(addBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(closeBtn);

        bg.add(buttonPanel, BorderLayout.SOUTH);

        // button actions
        addBtn.addActionListener(e -> onAddQuestion());
        saveBtn.addActionListener(e -> onSave());
        closeBtn.addActionListener(e -> frame.dispose());
    }
    
    private void loadCards() {
        cardsPanel.removeAll();
        for (int i = 0; i < questions.size(); i++) {
            Questions q = questions.get(i);
            JPanel card = createQuestionCard(q, i);
            cardsPanel.add(card);
            cardsPanel.add(Box.createVerticalStrut(10));
        }
        cardsPanel.add(Box.createVerticalGlue());
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    private JPanel createQuestionCard(Questions q, int index) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(25, 60, 55));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                g2.setColor(new Color(0, 200, 170));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setPreferredSize(new Dimension(800, 140));
        
        // Top row: Question text
        JPanel topRow = new JPanel();
        topRow.setOpaque(false);
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel qLabel = new JLabel("Q" + (index + 1) + ": " + q.getText());
        qLabel.setForeground(new Color(0, 200, 170));
        qLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        topRow.add(qLabel);
        topRow.add(Box.createHorizontalGlue());
        
        card.add(topRow);
        
        // Middle row: Options
        JPanel middleRow = new JPanel();
        middleRow.setOpaque(false);
        middleRow.setLayout(new BoxLayout(middleRow, BoxLayout.X_AXIS));
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JLabel optionsLabel = new JLabel("<html>A: " + q.getOptA() + " | B: " + q.getOptB() + " | C: " + q.getOptC() + " | D: " + q.getOptD() + "</html>");
        optionsLabel.setForeground(Color.WHITE);
        optionsLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        middleRow.add(optionsLabel);
        middleRow.add(Box.createHorizontalGlue());
        
        card.add(middleRow);
        
        // Bottom row: Difficulty, Answer, and Action Buttons
        JPanel bottomRow = new JPanel();
        bottomRow.setOpaque(false);
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        Color diffColor = switch (q.getDifficulty()) {
            case 1 -> new Color(100, 200, 255);
            case 2 -> new Color(255, 200, 100);
            case 3 -> new Color(255, 150, 100);
            case 4 -> new Color(255, 100, 100);
            default -> Color.WHITE;
        };
        
        String diffText = switch (q.getDifficulty()) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            case 3 -> "Hard";
            case 4 -> "Very Hard";
            default -> "Unknown";
        };
        
        JLabel diffLabel = new JLabel("Difficulty: " + diffText);
        diffLabel.setForeground(diffColor);
        diffLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        bottomRow.add(diffLabel);
        bottomRow.add(Box.createHorizontalStrut(20));
        
        JLabel ansLabel = new JLabel("Answer: " + q.getCorrectAnswer());
        ansLabel.setForeground(new Color(100, 255, 150));
        ansLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        bottomRow.add(ansLabel);
        bottomRow.add(Box.createHorizontalGlue());
        
        // Edit button
        JButton editBtn = new JButton("Edit");
        editBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        editBtn.setBackground(new Color(0, 150, 120));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 170), 2));
        editBtn.setPreferredSize(new Dimension(80, 30));
        editBtn.addActionListener(e -> onEditQuestion(index));
        bottomRow.add(editBtn);
        bottomRow.add(Box.createHorizontalStrut(8));
        
        // Delete button
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        deleteBtn.setBackground(new Color(120, 30, 30));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 100, 100), 2));
        deleteBtn.setPreferredSize(new Dimension(80, 30));
        deleteBtn.addActionListener(e -> onDeleteQuestion(index));
        bottomRow.add(deleteBtn);
        
        card.add(bottomRow);
        
        return card;
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

    private void onAddQuestion() {
        Questions q = showQuestionEditDialog(null);
        if (q != null) {
            // Add directly to SysData's question list
            SysData.getAllQuestions().add(q);
            // save right away - persist to csv
            SysData.saveQuestions();
            // Refresh local reference and UI
            questions = SysData.getAllQuestions();
            loadCards();
        }
    }

    private void onEditQuestion(int index) {
        Questions existing = questions.get(index);
        Questions updated = showQuestionEditDialog(existing);
        if (updated != null) {
            // save right away - persist to csv
            SysData.saveQuestions();
            loadCards();
        }
    }

    private void onDeleteQuestion(int index) {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Delete this question?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Remove from SysData's question list
            SysData.getAllQuestions().remove(index);
            // save right away - persist to csv
            SysData.saveQuestions();
            // Refresh local reference and UI
            questions = SysData.getAllQuestions();
            loadCards();
        }
    }

    private void onSave() {
        // Ensure the local questions list is synced with SysData before saving
        // (in case any changes were made that aren't reflected in SysData)
        SysData.saveQuestions();
        JOptionPane.showMessageDialog(frame, "Questions saved to CSV.", "Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Dialog used for both add and edit
    private Questions showQuestionEditDialog(Questions existing) {
        JDialog dialog = new JDialog(frame, true);
        dialog.setTitle(existing == null ? "Add Question" : "Edit Question");
        dialog.setSize(700, 650);
        dialog.setLocationRelativeTo(frame);
        
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
        dialog.setContentPane(bg);
        
        // Title
        JLabel titleLabel = new JLabel(existing == null ? "Add New Question" : "Edit Question");
        titleLabel.setForeground(new Color(0, 200, 170));
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bg.add(titleLabel, BorderLayout.NORTH);

        JTextField idField = new JTextField();
        idField.setEditable(false);
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setRows(4);
        textArea.setFont(new Font("Tahoma", Font.PLAIN, 12));
        textArea.setBackground(new Color(20, 40, 40));
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(new Color(0, 200, 170));
        
        JTextField diffField = new JTextField();
        JTextField aField = new JTextField();
        JTextField bField = new JTextField();
        JTextField cField = new JTextField();
        JTextField dField = new JTextField();
        JTextField ansField = new JTextField();
        
        // Style text fields
        JTextField[] fields = {idField, diffField, aField, bField, cField, dField, ansField};
        for (JTextField field : fields) {
            field.setBackground(new Color(20, 40, 40));
            field.setForeground(Color.WHITE);
            field.setCaretColor(new Color(0, 200, 170));
            field.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 120), 1));
            field.setFont(new Font("Tahoma", Font.PLAIN, 12));
        }

        // Form panel with GridBagLayout for better control
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ID (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel idLabel = new JLabel("ID:");
        idLabel.setForeground(new Color(0, 200, 170));
        idLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(idLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(idField, gbc);
        
        // Question (larger text area)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel qLabel = new JLabel("Question:");
        qLabel.setForeground(new Color(0, 200, 170));
        qLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(qLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane qScroll = new JScrollPane(textArea);
        qScroll.setOpaque(false);
        qScroll.getViewport().setOpaque(false);
        qScroll.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 120), 1));
        formPanel.add(qScroll, gbc);
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Difficulty
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel diffLabel = new JLabel("Difficulty (1=Easy, 2=Medium, 3=Hard, 4=Expert):");
        diffLabel.setForeground(new Color(0, 200, 170));
        diffLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(diffLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(diffField, gbc);
        
        // Options
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        JLabel aLabel = new JLabel("Option A:");
        aLabel.setForeground(new Color(0, 200, 170));
        aLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(aLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(aField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        JLabel bLabel = new JLabel("Option B:");
        bLabel.setForeground(new Color(0, 200, 170));
        bLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(bLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(bField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.0;
        JLabel cLabel = new JLabel("Option C:");
        cLabel.setForeground(new Color(0, 200, 170));
        cLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(cLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(cField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0.0;
        JLabel dLabel = new JLabel("Option D:");
        dLabel.setForeground(new Color(0, 200, 170));
        dLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(dLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(dField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.weightx = 0.0;
        JLabel ansLabel = new JLabel("Correct Answer (A/B/C/D):");
        ansLabel.setForeground(new Color(0, 200, 170));
        ansLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        formPanel.add(ansLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(ansField, gbc);
        
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getVerticalScrollBar().setBackground(new Color(8, 45, 40));
        formScroll.getVerticalScrollBar().setForeground(new Color(0, 200, 170));
        bg.add(formScroll, BorderLayout.CENTER);

        if (existing != null) {
            idField.setText(String.valueOf(existing.getId()));
            textArea.setText(existing.getText());
            diffField.setText(String.valueOf(existing.getDifficulty()));
            aField.setText(existing.getOptA());
            bField.setText(existing.getOptB());
            cField.setText(existing.getOptC());
            dField.setText(existing.getOptD());
            ansField.setText(existing.getCorrectAnswer());
        } else {
            idField.setText(String.valueOf(SysData.getNextQuestionId()));
        }

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        
        // Style OK button
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        okBtn.setBackground(new Color(0, 150, 120));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 170), 2));
        okBtn.setPreferredSize(new Dimension(100, 35));
        
        // Style Cancel button
        cancelBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        cancelBtn.setBackground(new Color(80, 40, 40));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 100, 100), 2));
        cancelBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        bg.add(buttonPanel, BorderLayout.SOUTH);

        final Questions[] result = new Questions[1];

        okBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String text = textArea.getText().trim();
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
}
