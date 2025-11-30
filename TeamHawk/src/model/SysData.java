package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList; // Using a list to store them is best practice

public class SysData {

    public static void main(String[] args) {
        // path to the CSV file (right now inside src/csvFiles)
        String csvFile = "src/csvFiles/Questions.csv";
        String line = "";
        String splitBy = ",";   // simple comma-separated file
        
        // this will hold all questions we load from the file
        ArrayList<Questions> questionList = new ArrayList<>();

        // try-with-resources: closes the reader automatically
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            // 1. SKIP THE HEADER
            // read the first line (column titles) and ignore it
            br.readLine(); 

            // 2. Loop through the rest of the lines
            while ((line = br.readLine()) != null) {
                
                // Split the line by comma into columns
                String[] data = line.split(splitBy);

                // CSV Structure: ID(0), Question(1), Diff(2), A(3), B(4), C(5), D(6), Ans(7)
                try {
                    int id = Integer.parseInt(data[0]);       // Convert ID to int
                    String text = data[1];
                    int difficulty = Integer.parseInt(data[2]); // Convert Diff to int
                    String valA = data[3];
                    String valB = data[4];
                    String valC = data[5];
                    String valD = data[6];
                    String ans = data[7];
    
                    // Create the question object from one CSV row
                    Questions q = new Questions(id, text, difficulty, valA, valB, valC, valD, ans);
                    
                    // Add to list and print to console for now (debug)
                    questionList.add(q);
                    q.printNicely();
                    
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // if a line is broken or has wrong numbers we just skip it
                    System.out.println("Skipping a bad line: " + line);
                }
            }

        } catch (IOException e) {
            // file not found / read error will show full stack trace
            e.printStackTrace();
        }
    }
}
