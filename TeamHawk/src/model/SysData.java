package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList; // Using a list to store them is best practice

public class SysData {

    public static void main(String[] args) {
        // Ideally, put Questions.csv in your project root folder (outside src)
    	String csvFile = "src/csvFiles/Questions.csv";
        String line = "";
        String splitBy = ",";
        
        ArrayList<Questions> questionList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            // 1. SKIP THE HEADER
            // We read the first line but do nothing with it.
            br.readLine(); 

            // 2. Loop through the rest of the lines
            while ((line = br.readLine()) != null) {
                
                // Split the line by comma
                String[] data = line.split(splitBy);

                // 3. Parse the data (Handle the columns specifically)
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
    
                    // Create the object
                    Questions q = new Questions(id, text, difficulty, valA, valB, valC, valD, ans);
                    
                    // Add to list and Print
                    questionList.add(q);
                    q.printNicely();
                    
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Skipping a bad line: " + line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}