/*
 * SettingsManager.java
 *
 * Created on July 2, 2003, 4:53 PM
 */

package geoirc;

/**
 *
 * @author  livesNbox
 */
    import geoirc.*;
    import java.io.*;
    import java.util.prefs.*;
    
public class SettingsManager {
    
    private static Preferences myPreferences = Preferences.userNodeForPackage(GeoIRC.class);
    private static final String SETTINGS_FILE_PATH = "";
    private static final String SETTINGS_FILE_NAME = "settings.xml";    
    protected DisplayManager displayMgr = null;
    /** Creates a new instance of SettingsManager */
    public SettingsManager(DisplayManager newDisplayMgr) {
        displayMgr = newDisplayMgr;
    }
    
    
    public void reloadXML(){
        InputStream is = null;
        
        try {
            is = new BufferedInputStream(new FileInputStream(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME));
        } catch (FileNotFoundException e) {
            displayMgr.printlnDebug("File Not Found:" + SETTINGS_FILE_PATH + SETTINGS_FILE_NAME + ".");
        }
        
        try {
            myPreferences.importPreferences(is);
        } catch (InvalidPreferencesFormatException e) {
            displayMgr.printlnDebug("Invalid Format in " + SETTINGS_FILE_PATH + SETTINGS_FILE_NAME + "; cannot import settings.");
        } catch (IOException e) {
            displayMgr.printlnDebug("IO Problem while trying to reload settings from " + SETTINGS_FILE_PATH + SETTINGS_FILE_NAME+ ".");
        }        
    }
    
    public void saveToXML(){
        while (myPreferences.parent() != null)
        {
            myPreferences = myPreferences.parent();
        }
        
        try {
            myPreferences.exportSubtree(new FileOutputStream(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME));
        } catch (IOException e) {
            displayMgr.printlnDebug("IO Problem while trying to save settings to " + SETTINGS_FILE_PATH + SETTINGS_FILE_NAME + ".");
        } catch (BackingStoreException e) {
            displayMgr.printlnDebug("Backing Store Problem while trying to save settings to " + SETTINGS_FILE_PATH + SETTINGS_FILE_NAME + ".");
        }
    }
}
