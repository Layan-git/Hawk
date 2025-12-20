package model;

import java.io.*;
import java.net.URL;

/**
 * Utility class for loading resources (CSV files, icons) that works both in
 * development (with IDE) and when running as a JAR file.
 */
public class ResourceLoader {

    /**
     * Load a resource file as an InputStream.
     * Works in both IDE and JAR environments.
     * 
     * @param resourcePath The resource path (e.g., "/csvFiles/Questions.csv" or "/resources/icon.png")
     * @return InputStream of the resource, or null if not found
     */
    public static InputStream getResourceAsStream(String resourcePath) {
        // Try to load from classpath first (works in JAR and IDE)
        InputStream is = ResourceLoader.class.getResourceAsStream(resourcePath);
        if (is != null) {
            return is;
        }
        
        // Fallback for development: try relative file paths
        try {
            // Remove leading slash if present
            String path = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            
            // Try from project root
            File file = new File(path);
            if (file.exists()) {
                return new FileInputStream(file);
            }
            
            // Try from src directory
            file = new File("src/" + path);
            if (file.exists()) {
                return new FileInputStream(file);
            }
        } catch (FileNotFoundException e) {
            // Continue to return null
        }
        
        return null;
    }

    /**
     * Get the absolute path to a resource file.
     * Works in IDE development. For JAR, returns null as classpath resources aren't filesystem paths.
     * 
     * @param resourcePath The resource path (e.g., "/resources/icon.png")
     * @return Absolute file path, or null if not found or running from JAR
     */
    public static String getResourcePath(String resourcePath) {
        try {
            URL url = ResourceLoader.class.getResource(resourcePath);
            if (url != null && url.getProtocol().equals("file")) {
                return new File(url.toURI()).getAbsolutePath();
            }
        } catch (Exception e) {
            // Continue to fallback
        }
        
        // Fallback for development
        String path = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        
        File file = new File(path);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        
        file = new File("src/" + path);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        
        return null;
    }

    /**
     * Get the absolute path to the CSV file.
     * Handles both development and JAR deployment scenarios.
     * 
     * @return Path to Questions.csv file
     */
    public static String getCSVPath() {
        String path = getResourcePath("/csvFiles/Questions.csv");
        if (path != null) {
            return path;
        }
        
        // Last resort: create/use Questions.csv in current working directory
        return "Questions.csv";
    }
}
