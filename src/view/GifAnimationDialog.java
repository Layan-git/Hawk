package view;

import model.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * GifAnimationDialog displays animated GIFs in a modal dialog.
 * Uses ImageIcon's built-in GIF animation support.
 */
public class GifAnimationDialog extends JDialog {
    private Timer closeTimer;
    private boolean autoCloseEnabled = false;
    
    /**
     * Create a GIF animation dialog with default centered positioning
     */
    public GifAnimationDialog(String gifResourceName, int width, int height, Runnable onComplete) {
        this(gifResourceName, width, height, onComplete, -1, -1);
    }
    
    /**
     * Create a GIF animation dialog with specific position
     * @param gifResourceName Name of GIF file (e.g., "win.gif")
     * @param width Dialog width
     * @param height Dialog height
     * @param onComplete Callback when dialog closes (can be null)
     * @param x X position (-1 for center), y Y position (-1 for center)
     */
    public GifAnimationDialog(String gifResourceName, int width, int height, Runnable onComplete, int x, int y) {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(true);  // Make it modal to be in foreground
        setSize(width, height);
        
        // Set position
        if (x >= 0 && y >= 0) {
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);  // Center on screen
        }
        
        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);  // Ensure it's on top
        
        // Create label for GIF display
        JLabel gifLabel = new JLabel();
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gifLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Load and display GIF
        ImageIcon gif = loadGif(gifResourceName);
        if (gif != null) {
            gifLabel.setIcon(gif);
        } else {
            gifLabel.setText("Animation failed to load");
        }
        
        add(gifLabel, BorderLayout.CENTER);
        
        // Set background to semi-transparent if you want
        getContentPane().setBackground(new Color(0, 0, 0, 50));
        
        // Add window listener to call callback on close
        if (onComplete != null) {
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    onComplete.run();
                }
            });
        }
    }
    
    /**
     * Load GIF from resources using ResourceLoader
     */
    private ImageIcon loadGif(String gifResourceName) {
        try {
            // Try loading from resources directory
            InputStream is = ResourceLoader.getResourceAsStream("resources/" + gifResourceName);
            
            if (is == null) {
                System.err.println("GIF not found in resources: " + gifResourceName);
                return null;
            }
            
            // Read all bytes
            byte[] bytes = is.readAllBytes();
            is.close();
            
            // Create ImageIcon from bytes - this handles GIF animation automatically
            ImageIcon icon = new ImageIcon(bytes);
            return icon;
            
        } catch (IOException e) {
            System.err.println("Error loading GIF: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Auto-close the dialog after specified milliseconds
     */
    public void autoCloseAfter(int delayMs) {
        autoCloseEnabled = true;
        closeTimer = new Timer(delayMs, e -> {
            dispose();
        });
        closeTimer.setRepeats(false);
        closeTimer.start();
    }
    
    @Override
    public void dispose() {
        if (closeTimer != null) {
            closeTimer.stop();
        }
        super.dispose();
    }
}

