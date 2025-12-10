package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SysData {

    private static final String CSV_PATH = "src/csvFiles/Questions.csv";

    // All questions
    private static final List<Questions> allQuestions = new ArrayList<>();

    // Questions grouped by difficulty 1..4
    private static final Map<Integer, List<Questions>> questionsByDifficulty = new HashMap<>();

    // Track asked question IDs per difficulty (per game, will be cleared from Main)
    private static final Map<Integer, Set<Integer>> askedQuestionIds = new HashMap<>();

    static {
        loadQuestionsFromCsv();
    }

    private static void loadQuestionsFromCsv() {
        String line;
        String splitBy = ",";

        for (int d = 1; d <= 4; d++) {
            questionsByDifficulty.put(d, new ArrayList<>());
            askedQuestionIds.put(d, new HashSet<>());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
            // skip header
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(splitBy);
                try {
                    int id = Integer.parseInt(data[0].trim());
                    String text = data[1].trim();
                    int difficulty = Integer.parseInt(data[2].trim());
                    String valA = data[3].trim();
                    String valB = data[4].trim();
                    String valC = data[5].trim();
                    String valD = data[6].trim();
                    String ans = data[7].trim();

                    Questions q = new Questions(id, text, difficulty, valA, valB, valC, valD, ans);
                    allQuestions.add(q);

                    List<Questions> bucket = questionsByDifficulty.get(difficulty);
                    if (bucket != null) {
                        bucket.add(q);
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Skipping a bad line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Called at start of each new game to clear seen questions
    public static void resetAskedQuestions() {
        for (int d = 1; d <= 4; d++) {
            askedQuestionIds.get(d).clear();
        }
    }

    public static int getTotalQuestions(int difficulty) {
        List<Questions> list = questionsByDifficulty.get(difficulty);
        return (list == null) ? 0 : list.size();
    }

    public static int getRemainingQuestions(int difficulty) {
        List<Questions> list = questionsByDifficulty.get(difficulty);
        Set<Integer> asked = askedQuestionIds.get(difficulty);
        if (list == null || asked == null) return 0;
        return list.size() - asked.size();
    }

    public static Questions getRandomQuestion(int difficulty) {
        List<Questions> list = questionsByDifficulty.get(difficulty);
        Set<Integer> asked = askedQuestionIds.get(difficulty);
        if (list == null || list.isEmpty()) return null;

        // collect unused
        List<Questions> unused = new ArrayList<>();
        for (Questions q : list) {
            if (!asked.contains(q.getId())) {
                unused.add(q);
            }
        }
        if (unused.isEmpty()) return null;

        Questions chosen = unused.get(new Random().nextInt(unused.size()));
        asked.add(chosen.getId());
        return chosen;
    }
}
