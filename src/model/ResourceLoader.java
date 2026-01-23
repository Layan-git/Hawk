package model;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * Utility class for loading resources (CSV files, icons, GIFs) that works both in
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
        // Ensure path starts with /
        String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        
        // Try to load from classpath first (works in JAR and IDE)
        InputStream is = ResourceLoader.class.getResourceAsStream(normalizedPath);
        if (is != null) {
            return is;
        }
        
        // Fallback for development: try relative file paths
        try {
            // Remove leading slash if present
            String path = normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath;
            
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
     * Load an image from resources using classpath or file fallback.
     * Works in both IDE and JAR environments.
     * 
     * @param resourcePath The resource path (e.g., "/resources/icon.png")
     * @return BufferedImage or null if not found
     */
    public static BufferedImage loadImage(String resourcePath) {
        try {
            InputStream is = getResourceAsStream(resourcePath);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                is.close();
                return img;
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + resourcePath + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * Get the URL to a resource (preferred for ImageIcon loading in JAR).
     * 
     * @param resourcePath The resource path (e.g., "/resources/icon.png")
     * @return URL or null if not found
     */
    public static URL getResourceURL(String resourcePath) {
        String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        return ResourceLoader.class.getResource(normalizedPath);
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
            String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
            URL url = ResourceLoader.class.getResource(normalizedPath);
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
     * Get the absolute path to the CSV file (Questions.csv).
     * Priority: development src/csvFiles > user directory (for new questions)
     * 
     * @return Path to Questions.csv file
     */
    public static String getCSVPath() {
        // Priority 1: Development mode - use src/csvFiles (bundled questions)
        String devPath = "src/csvFiles/Questions.csv";
        File devFile = new File(devPath);
        if (devFile.exists()) {
            return devFile.getAbsolutePath();
        }
        
        // Priority 2: Try just csvFiles/Questions.csv (in case working directory is set to src)
        String altPath = "csvFiles/Questions.csv";
        File altFile = new File(altPath);
        if (altFile.exists()) {
            return altFile.getAbsolutePath();
        }
        
        // Priority 3: Try resource path
        String resourcePath = getResourcePath("/csvFiles/Questions.csv");
        if (resourcePath != null) {
            return resourcePath;
        }
        
        // Priority 4: User home directory (fallback)
        String userHome = System.getProperty("user.home");
        String hawkDir = new File(userHome, ".hawk").getAbsolutePath();
        return new File(hawkDir, "Questions.csv").getAbsolutePath();
    }

    /**
     * Get the absolute path to the History CSV file.
     * Priority: development src/csvFiles > user directory (for new games)
     * 
     * @return Path to History.csv file
     */
    public static String getHistoryCSVPath() {
        // Priority 1: Development mode - use src/csvFiles (bundled history)
        String devPath = "src/csvFiles/History.csv";
        File devFile = new File(devPath);
        if (devFile.exists()) {
            return devFile.getAbsolutePath();
        }
        
        // Priority 2: Try just csvFiles/History.csv (in case working directory is set to src)
        String altPath = "csvFiles/History.csv";
        File altFile = new File(altPath);
        if (altFile.exists()) {
            return altFile.getAbsolutePath();
        }
        
        // Priority 3: User home directory (writable location for new games, used in JAR)
        String userHome = System.getProperty("user.home");
        String hawkDir = new File(userHome, ".hawk").getAbsolutePath();
        return new File(hawkDir, "History.csv").getAbsolutePath();
    }

    /**
     * Load app icon image from resources for window icons
     * @return BufferedImage of the app icon, or null if not found
     */
    public static BufferedImage loadAppIcon() {
        return loadImage("/resources/nerd_icon_shwompy.png");
    }
}
