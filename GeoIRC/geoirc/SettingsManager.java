/*
 * SettingsManager.java
 *
 * Created on July 2, 2003, 4:53 PM
 */

package geoirc;

/**
 *
 * @author  livesNbox
 * @author  Pistos
 */

    import geoirc.*;
    import java.io.*;
    import java.util.prefs.*;
    
public class SettingsManager {
    
    private static Preferences root = Preferences.userNodeForPackage(GeoIRC.class);
    /*
    private static final String SETTINGS_FILE_PATH = "";
    private static final String SETTINGS_FILE_NAME = "settings.xml";    
     */
    protected String filepath;
    protected DisplayManager displayMgr = null;

    // No default constructor.
    private SettingsManager() { }
    
    public SettingsManager( DisplayManager newDisplayMgr, String filepath )
    {
        displayMgr = newDisplayMgr;
        this.filepath = filepath;
    }
    
    public boolean loadSettingsFromXML()
    {
        InputStream is = null;
        boolean success = true;
        try {
            is = new BufferedInputStream(new FileInputStream( filepath ));
        } catch (FileNotFoundException e) {
            displayMgr.printlnDebug( "File not found: '" + filepath + "'." );
            success = false;
        }
        
        try {
            root.importPreferences(is);
        } catch (InvalidPreferencesFormatException e) {
            displayMgr.printlnDebug( "Invalid format in '" + filepath + "'; cannot import settings." );
            success = false;
        } catch (IOException e) {
            displayMgr.printlnDebug("I/O problem while trying to load settings from '" + filepath + "'.");
            success = false;
        }
        
        return success;
    }
    
    public boolean saveSettingsToXML()
    {
        boolean success = true;
        
        /*
        while (myPreferences.parent() != null)
        {
            myPreferences = myPreferences.parent();
        }
         */
        
        try {
            root.exportSubtree(new FileOutputStream( filepath ));
        } catch (IOException e) {
            displayMgr.printlnDebug("I/O problem while trying to save settings to '" + filepath + "'.");
            success = false;
        } catch (BackingStoreException e) {
            displayMgr.printlnDebug("Backing Store problem while trying to save settings to '" + filepath + "'.");
            success = false;
        }
        
        return success;
    }
    
    protected String getNodePath( String path )
    {
        String nodepath = "";
        
        int index = path.lastIndexOf( "/" );
        if( index > -1 )
        {
            // We use substring from 1 to remove the leading slash.
            nodepath = path.substring( 1, index );
        }
        
        return nodepath;
    }
    
    protected String getKey( String path )
    {
        String key = path;
        int index = path.lastIndexOf( "/" );
        if( index > -1 )
        {
            // We add 1 in order to exclude the slash.
            key = path.substring( index + 1 );
        }
        
        return key;
    }
    
    /* For all the following get methods,
     * the path is specified as a node path, ending with the key desired.
     *
     * e.g. /keyboard/ctrl/x
     * will retrieve the /keyboard/ctrl node, and get the x key from it.
     */
    
    public String get( String path, String default_ ) { return getString( path, default_ ); }
    public String getString( String path, String default_ )
    {
        return root.node( getNodePath( path ) ).get( getKey( path ), default_ );
    }
    
    public int getInt( String path, int default_ )
    {
        return root.node( getNodePath( path ) ).getInt( getKey( path ), default_ );
    }

    public boolean getBoolean( String path, boolean default_ )
    {
        return root.node( getNodePath( path ) ).getBoolean( getKey( path ), default_ );
    }
}
