package model;

public class Questions {

    // basic question data (id + text + difficulty level)
    private int id;
    private String text;
    private int difficulty;           // 1/2/3/4

    // 4 options for the question
    private String optA, optB, optC, optD;

    // stores which option is correct (e.g. "A" / "B" / "C" / "D")
    private String correctAnswer;

    // full constructor to build a complete question in one go
    public Questions(int id, String text, int difficulty,
                     String a, String b, String c, String d, String ans) {
        this.id = id;
        this.text = text;
        this.difficulty = difficulty;
        this.optA = a;
        this.optB = b;
        this.optC = c;
        this.optD = d;
        this.correctAnswer = ans;
    }

    // This method prints the question in a readable format (good for debug / console)
    public void printNicely() {
        System.out.println("------------------------------------------------");
        System.out.println("Q" + id + ": " + text + " (Difficulty: " + difficulty + ")");
        System.out.println("   A) " + optA);
        System.out.println("   B) " + optB);
        System.out.println("   C) " + optC);
        System.out.println("   D) " + optD);
        System.out.println("   The Answer Is : " + correctAnswer + ".");
        System.out.println("------------------------------------------------");
    }

    // ------------- Getters & Setters (simple data access) -------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getOptA() {
        return optA;
    }

    public void setOptA(String optA) {
        this.optA = optA;
    }

    public String getOptB() {
        return optB;
    }

    public void setOptB(String optB) {
        this.optB = optB;
    }

    public String getOptC() {
        return optC;
    }

    public void setOptC(String optC) {
        this.optC = optC;
    }

    public String getOptD() {
        return optD;
    }

    public void setOptD(String optD) {
        this.optD = optD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
