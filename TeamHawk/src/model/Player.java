// Add "package ..." here if your project uses packages.
package model ;

public class Player {

    // --------- Fields (attributes) ---------

    // Basic identity
    private String username;        // e.g. "Player 1" or real name
    private String password;        // password for login
    private String id;              // optional unique id (can be null)

    // Game statistics across all games
    private int gamesPlayed;
    private int gamesWon;
    private int totalScore;         // sum of scores from all games
    private int bestScore;          // highest score in a single game

    // Question / answer statistics
    private int totalQuestionsAnswered;
    private int totalCorrectAnswers;
    private int totalWrongAnswers;

    // Surprise statistics
    private int totalSurprisesPositive;
    private int totalSurprisesNegative;


    // --------- Constructors ---------

    /**
     * Constructor without ID.
     */
    public Player(String username, String password) {
        this(username, password, null);
    }

    /**
     * Full constructor.
     */
    public Player(String username, String password, String id) {
        this.username = username;
        this.password = password;
        this.id = id;

        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.totalScore = 0;
        this.bestScore = 0;
        this.totalQuestionsAnswered = 0;
        this.totalCorrectAnswers = 0;
        this.totalWrongAnswers = 0;
        this.totalSurprisesPositive = 0;
        this.totalSurprisesNegative = 0;
    }


    // --------- Business methods (logic) ---------

    public void addGameResult(boolean win, int score) {
        gamesPlayed++;
        if (win) gamesWon++;
        totalScore += score;
        if (score > bestScore) bestScore = score;
    }

    public void recordQuestionResult(boolean correct) {
        totalQuestionsAnswered++;
        if (correct) totalCorrectAnswers++;
        else totalWrongAnswers++;
    }

    public void recordSurprise(boolean positive) {
        if (positive) totalSurprisesPositive++;
        else totalSurprisesNegative++;
    }

    public double getAnswerAccuracy() {
        if (totalQuestionsAnswered == 0) return 0.0;
        return (double) totalCorrectAnswers / totalQuestionsAnswered;
    }


    // --------- Getters and Setters ---------

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public void setTotalQuestionsAnswered(int totalQuestionsAnswered) {
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(int totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public int getTotalWrongAnswers() {
        return totalWrongAnswers;
    }

    public void setTotalWrongAnswers(int totalWrongAnswers) {
        this.totalWrongAnswers = totalWrongAnswers;
    }

    public int getTotalSurprisesPositive() {
        return totalSurprisesPositive;
    }

    public void setTotalSurprisesPositive(int totalSurprisesPositive) {
        this.totalSurprisesPositive = totalSurprisesPositive;
    }

    public int getTotalSurprisesNegative() {
        return totalSurprisesNegative;
    }

    public void setTotalSurprisesNegative(int totalSurprisesNegative) {
        this.totalSurprisesNegative = totalSurprisesNegative;
    }


    // --------- Utility ---------

    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + '\'' +
                ", id='" + id + '\'' +
                ", gamesPlayed=" + gamesPlayed +
                ", gamesWon=" + gamesWon +
                ", totalScore=" + totalScore +
                ", bestScore=" + bestScore +
                ", questionsAnswered=" + totalQuestionsAnswered +
                ", correctAnswers=" + totalCorrectAnswers +
                ", wrongAnswers=" + totalWrongAnswers +
                ", positiveSurprises=" + totalSurprisesPositive +
                ", negativeSurprises=" + totalSurprisesNegative +
                '}';
    }
}