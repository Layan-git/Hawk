package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class History implements Serializable {

    private static final long serialVersionUID = 1L;

    // --------- Fields ---------

    private LocalDateTime dateTime;     // when the game ended (date + time)

    private String player1Name;         // name shown on left board
    private String player2Name;         // name shown on right board

    // e.g. "Easy", "Medium", "Hard" – stored as text for display
    private String difficulty;

    // true = players won, false = they lost
    private boolean win;

    // final shared score for this match
    private int finalScore;

    // how long the whole game took (in seconds)
    private long durationSeconds;

    // stats from the game – can be shown in history / stats screen
    private int minesHit;
    private int questionsAnswered;
    private int correctQuestions;
    private int wrongQuestions;
    private int surprisesTriggered;
    private int positiveSurprises;
    private int negativeSurprises;
    private int livesRemaining;

    // who was logged in when playing
    private String username;

    // --------- Constructors ---------

    public History() {
        // empty constructor for frameworks / file loading if needed
    }

    // full constructor when we want to save one finished game snapshot
    public History(LocalDateTime dateTime,
                   String player1Name,
                   String player2Name,
                   String difficulty,
                   boolean win,
                   int finalScore,
                   long durationSeconds,
                   int minesHit,
                   int questionsAnswered,
                   int correctQuestions,
                   int wrongQuestions,
                   int surprisesTriggered,
                   int positiveSurprises,
                   int negativeSurprises,
                   int livesRemaining,
                   String username) {
        this.dateTime = dateTime;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.difficulty = difficulty;
        this.win = win;
        this.finalScore = finalScore;
        this.durationSeconds = durationSeconds;
        this.minesHit = minesHit;
        this.questionsAnswered = questionsAnswered;
        this.correctQuestions = correctQuestions;
        this.wrongQuestions = wrongQuestions;
        this.surprisesTriggered = surprisesTriggered;
        this.positiveSurprises = positiveSurprises;
        this.negativeSurprises = negativeSurprises;
        this.livesRemaining = livesRemaining;
        this.username = username;
    }

    // --------- Getters and Setters ---------

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

    public int getMinesHit() {
        return minesHit;
    }

    public void setMinesHit(int minesHit) {
        this.minesHit = minesHit;
    }

    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(int questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }

    public int getCorrectQuestions() {
        return correctQuestions;
    }

    public void setCorrectQuestions(int correctQuestions) {
        this.correctQuestions = correctQuestions;
    }

    public int getWrongQuestions() {
        return wrongQuestions;
    }

    public void setWrongQuestions(int wrongQuestions) {
        this.wrongQuestions = wrongQuestions;
    }

    public int getSurprisesTriggered() {
        return surprisesTriggered;
    }

    public void setSurprisesTriggered(int surprisesTriggered) {
        this.surprisesTriggered = surprisesTriggered;
    }

    public int getPositiveSurprises() {
        return positiveSurprises;
    }

    public void setPositiveSurprises(int positiveSurprises) {
        this.positiveSurprises = positiveSurprises;
    }

    public int getNegativeSurprises() {
        return negativeSurprises;
    }

    public void setNegativeSurprises(int negativeSurprises) {
        this.negativeSurprises = negativeSurprises;
    }

    public int getLivesRemaining() {
        return livesRemaining;
    }

    public void setLivesRemaining(int livesRemaining) {
        this.livesRemaining = livesRemaining;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
