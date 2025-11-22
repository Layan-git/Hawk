package model;

public class Questions {
    private int id;
    private String text;
    private int difficulty;
    private String optA, optB, optC, optD;
    private String correctAnswer;

    public Questions(int id, String text, int difficulty, String a, String b, String c, String d, String ans) {
        this.id = id;
        this.text = text;
        this.difficulty = difficulty;
        this.optA = a;
        this.optB = b;
        this.optC = c;
        this.optD = d;
        this.correctAnswer = ans;
    }

    // This method prints the question in a readable format
    public void printNicely() {
        System.out.println("------------------------------------------------");
        System.out.println("Q" + id + ": " + text + " (Difficulty: " + difficulty + ")");
        System.out.println("   A) " + optA);
        System.out.println("   B) " + optB);
        System.out.println("   C) " + optC);
        System.out.println("   D) " + optD);
        System.out.println("   The Answer Is : "+correctAnswer+".");
        System.out.println("------------------------------------------------");
    }
}