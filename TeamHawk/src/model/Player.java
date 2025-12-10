package model ;
 //this class is just for show now but will be used in future iterations
public class Player {

    // --------- Fields (attributes) ---------

    // Basic identity
    private String username;        // how we show the player in the UI / login
    private String password;        // simple password field (can be hashed later)
    private String id;              // optional unique id (db / file key)

    // Game statistics across all games (lifetime stats)
    private int gamesPlayed;
    private int gamesWon;
    private int totalScore;         // sum of scores from all finished games
    private int bestScore;          // best score reached in a single game

    // Question / answer statistics
    private int totalQuestionsAnswered;
    private int totalCorrectAnswers;
    private int totalWrongAnswers;

    // Surprise statistics
    private int totalSurprisesPositive; // how many good surprises this player got
    private int totalSurprisesNegative; // how many bad surprises this player got


    // --------- Constructors ---------

    /**
     * Constructor without ID.
     * useful when we don't care about a db id yet
     */
    public Player(String username, String password) {
        this(username, password, null);
    }

    /**
     * Full constructor.
     * here we start a “fresh” player with zeroed stats
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

    // call this once per finished game to update win/loss + scores
    public void addGameResult(boolean win, int score) {
        gamesPlayed++;
        if (win) gamesWon++;
        totalScore += score;
        if (score > bestScore) bestScore = score;
    }

    // track a single answered question (right or wrong)
    public void recordQuestionResult(boolean correct) {
        totalQuestionsAnswered++;
        if (correct) totalCorrectAnswers++;
        else totalWrongAnswers++;
    }

    // track a single surprise outcome
    public void recordSurprise(boolean positive) {
        if (positive) totalSurprisesPositive++;
        else totalSurprisesNegative++;
    }

    // helper to get accuracy as a percentage-like value (0.0–1.0)
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
        // compact debug print for logs / console
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
