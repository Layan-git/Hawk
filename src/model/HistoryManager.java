package model;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * HistoryManager handles reading and writing game history to/from a CSV file.
 * CSV format: DateTime,Player1,Player2,Difficulty,Win,Score,Duration,LivesRemaining
 */
public class HistoryManager {
    
    private static final String CSV_HEADER = "DateTime,Player1,Player2,Username,Difficulty,Win,Score,Duration,MinesHit,QuestionsAnswered,CorrectQuestions,WrongQuestions,SurprisesTriggered,PositiveSurprises,NegativeSurprises,LivesRemaining";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    static {
        // Ensure CSV file exists with header
        initializeCSV();
    }
    
    /**
     * Get the History CSV file path, handling both development and JAR deployment
     */
    private static String getHistoryCSVPath() {
        return ResourceLoader.getHistoryCSVPath();
    }
    
    /**
     * Initialize the CSV file if it doesn't exist
     */
    private static void initializeCSV() {
        try {
            String csvPath = getHistoryCSVPath();
            Path filePath = Paths.get(csvPath);
            
            // Ensure parent directories exist
            Files.createDirectories(filePath.getParent());
            
            // If file doesn't exist, create it with header
            if (!Files.exists(filePath)) {
                Files.write(filePath, CSV_HEADER.getBytes());
            }
        } catch (IOException e) {
            System.err.println("Error initializing History CSV: " + e.getMessage());
        }
    }
    
    /**
     * Write a game history record to the CSV file
     */
    public static void writeHistory(History history) {
        if (history == null) return;
        
        try {
            String csvPath = getHistoryCSVPath();
            Path filePath = Paths.get(csvPath);
            
            System.out.println("Writing history to: " + filePath.toAbsolutePath());
            
            // Ensure parent directory exists
            Files.createDirectories(filePath.getParent());
            
            // Ensure file exists before appending
            if (!Files.exists(filePath)) {
                System.out.println("History file does not exist, creating with header...");
                Files.write(filePath, CSV_HEADER.getBytes());
            }
            
            String csvLine = convertHistoryToCSV(history);
            Files.write(
                    filePath,
                    (csvLine + "\n").getBytes(),
                    StandardOpenOption.APPEND
            );
            System.out.println("History saved successfully!");
        } catch (IOException e) {
            System.err.println("Error writing history to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Read all game histories from the CSV file.
     * First tries to load from bundled History.csv in JAR, then falls back to user directory.
     */
    public static List<History> readAllHistories() {
        List<History> histories = new ArrayList<>();
        
        // First try to load from classpath (bundled History.csv in JAR)
        InputStream csvStream = ResourceLoader.getResourceAsStream("/csvFiles/History.csv");
        if (csvStream != null) {
            loadHistoriesFromStream(csvStream, histories);
        }
        
        // Then load from user directory (writable location)
        try {
            String csvPath = getHistoryCSVPath();
            Path filePath = Paths.get(csvPath);
            
            if (Files.exists(filePath)) {
                List<String> lines = Files.readAllLines(filePath);
                
                // Skip header row
                for (int i = 1; i < lines.size(); i++) {
                    History history = parseCSVLine(lines.get(i));
                    if (history != null && !histories.contains(history)) {
                        histories.add(history);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading history from CSV: " + e.getMessage());
        }
        
        return histories;
    }
    
    /**
     * Load histories from an InputStream (JAR classpath resources)
     */
    private static void loadHistoriesFromStream(InputStream is, List<History> histories) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("DateTime,")) {
                    continue; // Skip header
                }
                
                History history = parseCSVLine(line);
                if (history != null) {
                    histories.add(history);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading histories from stream: " + e.getMessage());
        }
    }
    
    /**
     * Convert a History object to CSV format
     */
    private static String convertHistoryToCSV(History history) {
        StringBuilder sb = new StringBuilder();
        
        // DateTime
        String dateTime = history.getDateTime() != null 
                ? history.getDateTime().format(DATE_FORMATTER) 
                : "";
        sb.append(escapeCSV(dateTime)).append(",");
        
        // Player1, Player2, Username
        sb.append(escapeCSV(history.getPlayer1Name())).append(",");
        sb.append(escapeCSV(history.getPlayer2Name())).append(",");
        sb.append(escapeCSV(history.getUsername())).append(",");
        
        // Difficulty
        sb.append(escapeCSV(history.getDifficulty())).append(",");
        
        // Win (true/false)
        sb.append(history.isWin()).append(",");
        
        // Score, Duration, MinesHit, QuestionsAnswered, CorrectQuestions, WrongQuestions, SurprisesTriggered, 
        // PositiveSurprises, NegativeSurprises, LivesRemaining
        sb.append(history.getFinalScore()).append(",");
        sb.append(history.getDurationSeconds()).append(",");
        sb.append(history.getMinesHit()).append(",");
        sb.append(history.getQuestionsAnswered()).append(",");
        sb.append(history.getCorrectQuestions()).append(",");
        sb.append(history.getWrongQuestions()).append(",");
        sb.append(history.getSurprisesTriggered()).append(",");
        sb.append(history.getPositiveSurprises()).append(",");
        sb.append(history.getNegativeSurprises()).append(",");
        sb.append(history.getLivesRemaining());
        
        return sb.toString();
    }
    
    /**
     * Parse a CSV line to History object
     */
    private static History parseCSVLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] parts = parseCSVFields(line);
            
            if (parts.length < 16) {
                return null; // Invalid line
            }
            
            History history = new History();
            
            // DateTime
            if (!parts[0].isEmpty()) {
                history.setDateTime(LocalDateTime.parse(parts[0], DATE_FORMATTER));
            }
            
            // Player names
            history.setPlayer1Name(parts[1]);
            history.setPlayer2Name(parts[2]);
            
            // Username
            history.setUsername(parts[3]);
            
            // Difficulty
            history.setDifficulty(parts[4]);
            
            // Win
            history.setWin(Boolean.parseBoolean(parts[5]));
            
            // Score, Duration, MinesHit, QuestionsAnswered, CorrectQuestions, WrongQuestions, 
            // SurprisesTriggered, PositiveSurprises, NegativeSurprises, LivesRemaining
            history.setFinalScore(Integer.parseInt(parts[6]));
            history.setDurationSeconds(Long.parseLong(parts[7]));
            history.setMinesHit(Integer.parseInt(parts[8]));
            history.setQuestionsAnswered(Integer.parseInt(parts[9]));
            history.setCorrectQuestions(Integer.parseInt(parts[10]));
            history.setWrongQuestions(Integer.parseInt(parts[11]));
            history.setSurprisesTriggered(Integer.parseInt(parts[12]));
            history.setPositiveSurprises(Integer.parseInt(parts[13]));
            history.setNegativeSurprises(Integer.parseInt(parts[14]));
            history.setLivesRemaining(Integer.parseInt(parts[15]));
            
            return history;
        } catch (Exception e) {
            System.err.println("Error parsing CSV line: " + line + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse CSV fields respecting quoted values
     */
    private static String[] parseCSVFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }
    
    /**
     * Escape CSV fields that contain commas or quotes
     */
    private static String escapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\""); // Escape quotes
            return "\"" + value + "\"";
        }
        
        return value;
    }
}
