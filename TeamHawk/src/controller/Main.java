package controller;

import model.Board.Difficulty;
import view.MainMenu;
import view.GameSetup;

public class Main {

    public static interface MainMenuController {
        void startGame();
        void openHistory();
        void openManageQuestions();
        void openHowToPlay();
        void exit();
    }

    public static interface GameSetupController {
        void backToMenu();
        void confirmStart(String player1, String player2, Difficulty difficulty);
    }

    private MainMenu mainMenu;
    private GameSetup gameSetup;

    private final MainMenuController menuController = new MainMenuController() {
        public void startGame() { showSetup(); }
        public void openHistory() {}
        public void openManageQuestions() {}
        public void openHowToPlay() {}
        public void exit() { System.exit(0); }
    };

    private final GameSetupController setupController = new GameSetupController() {
        public void backToMenu() { 
        	if (gameSetup != null) gameSetup.close();
        	mainMenu.show();
        	}
        public void confirmStart(String p1, String p2, Difficulty d) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Starting " + d + " for " + p1 + " vs " + p2);
        }
    };

    public Main() {
        mainMenu = new MainMenu(menuController);
        mainMenu.show();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(Main::new);
    }

    private void showSetup() {
        if (gameSetup == null) {
            gameSetup = new GameSetup(setupController);
        }
        mainMenu.close();
        gameSetup.show();
    }
}
