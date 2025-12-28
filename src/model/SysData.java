package model;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SysData {

    private static String CSV_PATH = null;

    static {
        // Initialize CSV_PATH to work in both IDE and JAR
        CSV_PATH = ResourceLoader.getCSVPath();
    }

    // All questions in memory
    private static final List<Questions> questionList = new ArrayList<>();

    // For game runtime: track asked question IDs per difficulty (1..4)
    private static final Map<Integer, Set<Integer>> askedQuestionIds = new HashMap<>();

    // --------------- History storage ---------------

    // All finished games in memory
    private static final List<History> historyList = new ArrayList<>();

    // CSV file path for histories
    private static final String HISTORY_CSV_PATH =
            ResourceLoader.getResourcePath("/csvFiles/history.csv");

    static {
        // init asked sets
        for (int d = 1; d <= 4; d++) {
            askedQuestionIds.put(d, new HashSet<>());
        }
        // Load questions from classpath if available
        InputStream csvStream = ResourceLoader.getResourceAsStream("/csvFiles/Questions.csv");
        if (csvStream != null) {
            loadQuestionsFromStream(csvStream);
        } else {
            loadQuestions();
        }

        // Load histories from CSV if present
        loadHistories();
    }

    // ---------------- Loading & Saving questions ----------------

    public static void loadQuestions() {
        questionList.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Skip the two initial header lines and the real header
                if (line.equals("Questions") ||
                    line.equals("Questions,,,,,,,") ||
                    line.startsWith("ID,Question,")) {
                    continue;
                }

                String[] fields = parseCsvLine(line);
                if (fields.length != 8) {
                    System.out.println("Skipping bad line (expected 8 fields): " + line);
                    continue;
                }

                try {
                    int id = Integer.parseInt(fields[0].trim());
                    String text = fields[1].trim();
                    int difficulty = Integer.parseInt(fields[2].trim());
                    String optA = fields[3].trim();
                    String optB = fields[4].trim();
                    String optC = fields[5].trim();
                    String optD = fields[6].trim();
                    String correct = fields[7].trim();

                    Questions q = new Questions(id, text, difficulty, optA, optB, optC, optD, correct);
                    questionList.add(q);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping bad line (parse error): " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load questions from an InputStream (works with JAR classpath resources)
    private static void loadQuestionsFromStream(InputStream is) {
        questionList.clear();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equals("Questions") ||
                    line.equals("Questions,,,,,,,") ||
                    line.startsWith("ID,Question,")) {
                    continue;
                }

                String[] fields = parseCsvLine(line);
                if (fields.length != 8) {
                    System.out.println("Skipping bad line (expected 8 fields): " + line);
                    continue;
                }

                try {
                    int id = Integer.parseInt(fields[0].trim());
                    String text = fields[1].trim();
                    int difficulty = Integer.parseInt(fields[2].trim());
                    String optA = fields[3].trim();
                    String optB = fields[4].trim();
                    String optC = fields[5].trim();
                    String optD = fields[6].trim();
                    String correct = fields[7].trim();

                    Questions q = new Questions(id, text, difficulty, optA, optB, optC, optD, correct);
                    questionList.add(q);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping bad line (parse error): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load questions from stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Simple CSV parser: handles quoted fields with commas and unquoted fields
    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                inQuotes = !inQuotes; // toggle quote state
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    // Write the current in-memory list back to CSV
    public static void saveQuestions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_PATH))) {
            pw.println("ID,Question,Difficulty,A,B,C,D,Correct Answer");
            for (Questions q : questionList) {
                pw.printf("%d,%s,%d,%s,%s,%s,%s,%s%n",
                        q.getId(),
                        escape(q.getText()),
                        q.getDifficulty(),
                        escape(q.getOptA()),
                        escape(q.getOptB()),
                        escape(q.getOptC()),
                        escape(q.getOptD()),
                        q.getCorrectAnswer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // very simple escaping (avoid commas breaking CSV)
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(",", " ");
    }

    private static String safeCsv(String s) {
        if (s == null) return "";
        return s.replace(",", " ");
    }

    // ---------------- Public accessors (questions) ----------------

    public static List<Questions> getAllQuestions() {
        return questionList;
    }

    public static int getNextQuestionId() {
        int max = 0;
        for (Questions q : questionList) {
            if (q.getId() > max) {
                max = q.getId();
            }
        }
        return max + 1;
    }

    // ---------------- Game-time question usage ----------------

    // Call at start of each new game
    public static void resetAskedQuestions() {
        for (int d = 1; d <= 4; d++) {
            askedQuestionIds.get(d).clear();
        }
    }

    // Total questions with given difficulty in CSV
    public static int getTotalQuestions(int difficulty) {
        int count = 0;
        for (Questions q : questionList) {
            if (q.getDifficulty() == difficulty) {
                count++;
            }
        }
        return count;
    }

    // Remaining (not yet asked) questions for difficulty
    public static int getRemainingQuestions(int difficulty) {
        Set<Integer> asked = askedQuestionIds.get(difficulty);
        if (asked == null) return 0;

        int total = 0;
        int askedHere = 0;
        for (Questions q : questionList) {
            if (q.getDifficulty() == difficulty) {
                total++;
                if (asked.contains(q.getId())) {
                    askedHere++;
                }
            }
        }
        return total - askedHere;
    }

    // Get a random not-yet-asked question for difficulty, mark it as asked
    public static Questions getRandomQuestion(int difficulty) {
        List<Questions> candidates = new ArrayList<>();
        Set<Integer> asked = askedQuestionIds.get(difficulty);
        if (asked == null) return null;

        for (Questions q : questionList) {
            if (q.getDifficulty() == difficulty && !asked.contains(q.getId())) {
                candidates.add(q);
            }
        }
        if (candidates.isEmpty()) return null;

        Questions chosen = candidates.get(new java.util.Random().nextInt(candidates.size()));
        asked.add(chosen.getId());
        return chosen;
    }

    // ---------------- History methods (CSV) ----------------

    private static void loadHistories() {
        historyList.clear();
        if (HISTORY_CSV_PATH == null) return;
        File f = new File(HISTORY_CSV_PATH);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 16) continue;

                int idx = 0;
                String dateTimeStr = parts[idx++].trim();
                String p1 = parts[idx++].trim();
                String p2 = parts[idx++].trim();
                String diff = parts[idx++].trim();
                boolean win = Boolean.parseBoolean(parts[idx++].trim());
                int finalScore = Integer.parseInt(parts[idx++].trim());
                long duration = Long.parseLong(parts[idx++].trim());
                int minesHit = Integer.parseInt(parts[idx++].trim());
                int qAns = Integer.parseInt(parts[idx++].trim());
                int qCorrect = Integer.parseInt(parts[idx++].trim());
                int qWrong = Integer.parseInt(parts[idx++].trim());
                int surp = Integer.parseInt(parts[idx++].trim());
                int posSurp = Integer.parseInt(parts[idx++].trim());
                int negSurp = Integer.parseInt(parts[idx++].trim());
                int lives = Integer.parseInt(parts[idx++].trim());
                String username = parts[idx++].trim();

                LocalDateTime dt = LocalDateTime.parse(dateTimeStr);

                History h = new History(dt, p1, p2, diff, win, finalScore,
                        duration, minesHit, qAns, qCorrect, qWrong,
                        surp, posSurp, negSurp, lives, username);
                historyList.add(h);
            }
        } catch (Exception e) {
            System.err.println("Failed to load histories CSV: " + e.getMessage());
        }
    }

    private static void saveHistories() {
        if (HISTORY_CSV_PATH == null) return;
        File f = new File(HISTORY_CSV_PATH);
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("dateTime,player1,player2,difficulty,win,finalScore," +
                    "durationSeconds,minesHit,questionsAnswered,correctQuestions," +
                    "wrongQuestions,surprisesTriggered,positiveSurprises," +
                    "negativeSurprises,livesRemaining,username");
            for (History h : historyList) {
                pw.printf("%s,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s%n",
                        h.getDateTime(),
                        safeCsv(h.getPlayer1Name()),
                        safeCsv(h.getPlayer2Name()),
                        safeCsv(h.getDifficulty()),
                        Boolean.toString(h.isWin()),
                        h.getFinalScore(),
                        h.getDurationSeconds(),
                        h.getMinesHit(),
                        h.getQuestionsAnswered(),
                        h.getCorrectQuestions(),
                        h.getWrongQuestions(),
                        h.getSurprisesTriggered(),
                        h.getPositiveSurprises(),
                        h.getNegativeSurprises(),
                        h.getLivesRemaining(),
                        safeCsv(h.getUsername()));
            }
        } catch (Exception e) {
            System.err.println("Failed to save histories CSV: " + e.getMessage());
        }
    }

    // Add one finished game and persist
    public static void addHistory(History h) {
        if (h == null) return;
        historyList.add(h);
        saveHistories();
    }

    // Read‑only access to all histories (for History/Leaderboard screen)
    public static List<History> getAllHistories() {
        return new ArrayList<>(historyList);
    }
}
