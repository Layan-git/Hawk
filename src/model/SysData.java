package model;

import java.io.*;
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
    
    // All game histories in memory
    private static final List<History> historyList = new ArrayList<>();

    // For game runtime: track asked question IDs per difficulty (1..4)
    private static final Map<Integer, Set<Integer>> askedQuestionIds = new HashMap<>();

    static {
        // init asked sets
        for (int d = 1; d <= 4; d++) {
            askedQuestionIds.put(d, new HashSet<>());
        }
        // Load from classpath if available
        InputStream csvStream = ResourceLoader.getResourceAsStream("/csvFiles/Questions.csv");
        if (csvStream != null) {
            loadQuestionsFromStream(csvStream);
        } else {
            loadQuestions();
        }
    }

    // ---------------- Loading & Saving ----------------

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

                // We know we should have exactly 8 CSV columns:
                // ID, Question, Difficulty, A, B, C, D, Correct
                // Question may be quoted and contain commas, so we must parse quotes properly.
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

                // Skip the two initial header lines and the real header
                if (line.equals("Questions") ||
                    line.equals("Questions,,,,,,,") ||
                    line.startsWith("ID,Question,")) {
                    continue;
                }

                // We know we should have exactly 8 CSV columns:
                // ID, Question, Difficulty, A, B, C, D, Correct
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
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                inQuotes = !inQuotes; // toggle quote state
            } else if (ch == ',' && !inQuotes) {
                // end of field
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        // last field
        result.add(current.toString());

        return result.toArray(new String[0]);
    }



    // Write the current in-memory list back to CSV
    public static void saveQuestions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_PATH))) {
            // header
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
            pw.flush(); // Ensure data is written to disk
        } catch (IOException e) {
            System.err.println("ERROR saving questions to: " + CSV_PATH);
            e.printStackTrace();
        }
    }

    // very simple escaping (avoid commas breaking CSV)
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(",", " ");
    }

    // ---------------- Public accessors ----------------
<<<<<<< Updated upstream
=======

    // Reload all questions from CSV (useful when questions have been updated externally)
    public static void reloadQuestionsFromCSV() {
        questionList.clear();
        loadQuestions();
    }
>>>>>>> Stashed changes

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

    // Call at start of each new game - clear asked questions for fresh game
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
    
    // ----------------  History Management ----------------
    
<<<<<<< Updated upstream
    // Add a game history record
    public static void addHistory(History history) {
        if (history != null) {
            historyList.add(history);
        }
    }
    
    // Get all game histories
=======
    // Add a game history record and save to CSV
    public static void addHistory(History history) {
        if (history != null) {
            historyList.add(history);
            HistoryManager.writeHistory(history);
        }
    }
    
    // Reload all histories from CSV (useful when history has been updated externally)
    public static void reloadHistoriesFromCSV() {
        historyList.clear();
        historyList.addAll(HistoryManager.readAllHistories());
    }
    
    // Get all game histories (loads from CSV if list is empty, otherwise returns cached list)
>>>>>>> Stashed changes
    public static List<History> getAllHistories() {
        if (historyList.isEmpty()) {
            historyList.addAll(HistoryManager.readAllHistories());
        }
        return new ArrayList<>(historyList);
    }
    
    // Get histories for a specific user
    public static List<History> getHistoriesForUser(String username) {
        List<History> userHistories = new ArrayList<>();
        if (username != null) {
            for (History h : historyList) {
                if (username.equalsIgnoreCase(h.getUsername())) {
                    userHistories.add(h);
                }
            }
        }
        return userHistories;
    }
<<<<<<< Updated upstream
=======
    
    // ----------------  Authentication ----------------
    
    // Verify login credentials
    public static boolean verifyLogin(String username, String password) {
        // Simple authentication: username "admin" with password "admin"
        // Modify this logic based on your actual authentication requirements
        return "admin".equalsIgnoreCase(username) && "admin".equals(password);
    }
>>>>>>> Stashed changes
}
