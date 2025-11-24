package model;

import java.time.LocalDateTime;

public class History {

    private LocalDateTime dateTime;     // when the game ended
    private String player1Name;
    private String player2Name;
    private String difficulty;          // "Easy", "Medium", "Hard"
    private boolean win;                // true = victory, false = defeat
    private int finalScore;             // final game score
    private long durationSeconds;       // how long the game took

    // --------- Constructors ---------

    public History() {
        // empty constructor if needed for serialization
    }

    public History(LocalDateTime dateTime,
                   String player1Name,
                   String player2Name,
                   String difficulty,
                   boolean win,
                   int finalScore,
                   long durationSeconds) {

        this.dateTime = dateTime;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.difficulty = difficulty;
        this.win = win;
        this.finalScore = finalScore;
        this.durationSeconds = durationSeconds;
    }

    // --------- Getters & Setters ---------

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    // --------- Utility ---------

    @Override
    public String toString() {
        return "History{" +
                "dateTime=" + dateTime +
                ", player1Name='" + player1Name + '\'' +
                ", player2Name='" + player2Name + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", win=" + win +
                ", finalScore=" + finalScore +
                ", durationSeconds=" + durationSeconds +
                '}';
    }
}