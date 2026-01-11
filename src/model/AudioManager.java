package model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

/**
 * AudioManager handles background music and sound effects
 */
public class AudioManager {
    private static AudioManager instance;
    private Clip musicClip;
    private boolean musicMuted = false;
    private boolean soundEffectsMuted = false;
    private float musicVolume = 1.0f;
    private static final float EFFECT_VOLUME_DB = -15.0f; // Sound effects volume in dB
    
    // Cache for pre-loaded sound effect data to reduce latency
    private Map<String, byte[]> soundCache = new HashMap<>();
    
    private AudioManager() {
    }
    
    /**
     * Get singleton instance
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * Pre-load a sound effect into cache for faster playback
     */
    public void preloadSoundEffect(String soundFileName) {
        if (soundCache.containsKey(soundFileName)) {
            return; // Already cached
        }
        
        try {
            InputStream is = ResourceLoader.getResourceAsStream("resources/" + soundFileName);
            if (is == null) {
                System.err.println("Cannot preload - Sound file not found: " + soundFileName);
                return;
            }
            
            byte[] audioData = is.readAllBytes();
            is.close();
            soundCache.put(soundFileName, audioData);
        } catch (IOException e) {
            System.err.println("Error preloading sound effect: " + e.getMessage());
        }
    }
    
    /**
     * Load and play background music from resources (looping)
     */
    public void playBackgroundMusic(String musicFileName) {
        try {
            // Load music from resources
            InputStream is = ResourceLoader.getResourceAsStream("resources/" + musicFileName);
            if (is == null) {
                System.err.println("Music file not found: " + musicFileName);
                return;
            }
            
            // Read the entire stream into a byte array to support mark/reset
            byte[] audioData = is.readAllBytes();
            is.close();
            
            // Create a ByteArrayInputStream which supports mark/reset
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);
            
            // Close previous clip if it exists
            if (musicClip != null && musicClip.isRunning()) {
                musicClip.stop();
                musicClip.close();
            }
            
            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
            
            // Set volume for background music
            FloatControl volumeControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            if (!musicMuted) {
                // Set to -10dB (70% volume) for comfortable background music
                volumeControl.setValue(-10.0f);
            } else {
                volumeControl.setValue(Float.NEGATIVE_INFINITY);
            }
            
            // Loop forever
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio format: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error loading music file: " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Play a sound effect (non-looping, one-time sound)
     * Optimized for minimal delay - plays on separate thread but data is pre-loaded
     */
    public void playSoundEffect(String soundFileName) {
        // Don't play if sound effects are muted
        if (soundEffectsMuted) {
            return;
        }
        
        // Run on a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Get audio data (from cache if available, otherwise load)
                byte[] audioData = soundCache.get(soundFileName);
                if (audioData == null) {
                    // Load from file
                    InputStream is = ResourceLoader.getResourceAsStream("resources/" + soundFileName);
                    if (is == null) {
                        System.err.println("Sound effect file not found: " + soundFileName);
                        return;
                    }
                    
                    audioData = is.readAllBytes();
                    is.close();
                }
                
                // Create a ByteArrayInputStream which supports mark/reset
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);
                
                Clip effectClip = AudioSystem.getClip();
                effectClip.open(audioStream);
                
                // Set volume for sound effects
                FloatControl volumeControl = (FloatControl) effectClip.getControl(FloatControl.Type.MASTER_GAIN);
                
                // Adjust volume based on effect type
                if (soundFileName.contains("expolsion")) {
                    volumeControl.setValue(-25.0f); // 50% volume for explosion
                } else {
                    volumeControl.setValue(EFFECT_VOLUME_DB); // -15dB for other effects
                }
                
                // Play once
                effectClip.start();
                
                // Keep clip alive while playing (optional optimization)
                // The clip will close when playback finishes
                
            } catch (UnsupportedAudioFileException e) {
                System.err.println("Unsupported audio format for effect: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error loading sound effect: " + e.getMessage());
            } catch (LineUnavailableException e) {
                System.err.println("Audio line unavailable for effect: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error playing sound effect: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }
    
    /**
     * Pause background music
     */
    public void pauseBackgroundMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }
    
    /**
     * Resume background music
     */
    public void resumeBackgroundMusic() {
        if (musicClip != null && !musicClip.isRunning()) {
            musicClip.start();
        }
    }
    
    /**
     * Toggle music mute
     */
    public void toggleMusicMute() {
        setMusicMuted(!musicMuted);
    }
    
    /**
     * Set music mute state
     */
    public void setMusicMuted(boolean muted) {
        musicMuted = muted;
        if (musicClip != null) {
            try {
                FloatControl volumeControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                if (muted) {
                    volumeControl.setValue(Float.NEGATIVE_INFINITY);
                } else {
                    volumeControl.setValue(-10.0f);  // -10dB volume
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error adjusting music volume: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get music mute state
     */
    public boolean isMusicMuted() {
        return musicMuted;
    }
    
    /**
     * Toggle sound effects mute
     */
    public void toggleSoundEffectsMute() {
        setSoundEffectsMuted(!soundEffectsMuted);
    }
    
    /**
     * Set sound effects mute state
     */
    public void setSoundEffectsMuted(boolean muted) {
        soundEffectsMuted = muted;
    }
    
    /**
     * Get sound effects mute state
     */
    public boolean isSoundEffectsMuted() {
        return soundEffectsMuted;
    }
}
