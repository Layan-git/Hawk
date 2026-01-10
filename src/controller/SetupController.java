package controller;

import javax.swing.JOptionPane;
import model.Board;
import view.GameSetup;

public class SetupController implements IGameSetupController {
    
    private GameSetup gameSetup;
    private final GameFlowController gameFlowController;
    
    public SetupController(GameFlowController gameFlowController) {
        this.gameFlowController = gameFlowController;
    }
    
    public void setGameSetup(GameSetup gameSetup) {
        this.gameSetup = gameSetup;
    }
    
    @Override
    public void backToMenu() {
        if (gameSetup != null) gameSetup.close();
        gameFlowController.showMainMenu();
    }

    @Override
    public void confirmStart(String p1, String p2, Board.Difficulty d, int p1CharIndex, int p2CharIndex) {
        // Validate icons are selected (charIndex should not be -1 or same)
        if (p1CharIndex < 0) {
            JOptionPane.showMessageDialog(
                    null,
                    "Player 1 must select an icon!",
                    "Missing Player 1 Icon",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        if (p2CharIndex < 0) {
            JOptionPane.showMessageDialog(
                    null,
                    "Player 2 must select an icon!",
                    "Missing Player 2 Icon",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Validate that both players have chosen different icons
        if (p1CharIndex == p2CharIndex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Both players must choose different icons!\nPlease select unique icons for each player.",
                    "Invalid Icon Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Use default names if empty
        String player1Name = (p1 == null || p1.trim().isEmpty()) ? "Player 1" : p1;
        String player2Name = (p2 == null || p2.trim().isEmpty()) ? "Player 2" : p2;
        
        gameFlowController.startGameBoard(player1Name, player2Name, d, p1CharIndex, p2CharIndex);
    }
}
