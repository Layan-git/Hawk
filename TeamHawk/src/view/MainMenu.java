package view;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

// Import your question loader if you want to connect it later
// import model.SysData; 

public class MainMenu {

    private JFrame frame;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainMenu window = new MainMenu();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainMenu() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        // 1. Setup the Main Frame
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 350); // Made slightly taller for buttons
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null); // Absolute layout for WindowBuilder

        // 2. Title Label
        JLabel lblTitle = new JLabel("Minesweeper Battle");
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblTitle.setBounds(73, 25, 286, 40);
        frame.getContentPane().add(lblTitle);

        // 3. "Start Game" Button
        JButton btnStart = new JButton("Start Game");
        btnStart.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnStart.setBounds(123, 90, 180, 40);
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: Code to open your Game Board goes here
                // Example: GameWindow game = new GameWindow();
                // game.setVisible(true);
                // frame.dispose(); // Close the menu
                
                JOptionPane.showMessageDialog(frame, "Game starting... (Connect your Board class here!)");
            }
        });
        frame.getContentPane().add(btnStart);

        // 4. "View Questions" Button (Connecting to your CSV work)
        JButton btnQuestions = new JButton("View Questions");
        btnQuestions.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnQuestions.setBounds(123, 150, 180, 40);
        btnQuestions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // You can call your SysData logic here to test the CSV reading
                // SysData.main(null); 
                JOptionPane.showMessageDialog(frame, "Check the Console for the loaded questions!");
            }
        });
        frame.getContentPane().add(btnQuestions);

        // 5. "Exit" Button
        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnExit.setBounds(123, 210, 180, 40);
        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Close the application
                System.exit(0);
            }
        });
        frame.getContentPane().add(btnExit);
    }
}