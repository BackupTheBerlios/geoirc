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
    
public class SettingsManager
    implements PreferenceChangeListener, NodeChangeListener
{
    
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
    
    protected void printlnDebug( String s )
    {
        if( displayMgr != null )
        {
            displayMgr.printlnDebug( s );
        }
        else
        {
            System.err.println( s );
        }
    }
    
    protected void listenToPreference( Preferences p )
    {
        p.addNodeChangeListener( this );
        p.addPreferenceChangeListener( this );
        
        String [] children;
        try
        {
            children = p.childrenNames();
            for( int i = 0; i < children.length; i++ )
            {
                listenToPreference( p.node( children[ i ] ) );
            }
        }
        catch( BackingStoreException e )
        {
            printlnDebug( e.getMessage() );
        }
    }
    
    public boolean loadSettingsFromXML()
    {
        InputStream is = null;
        boolean success = true;
        try {
            is = new BufferedInputStream(new FileInputStream( filepath ));
        } catch (FileNotFoundException e) {
            printlnDebug( "File not found: '" + filepath + "'." );
            success = false;
        }
        
        try {
            root.importPreferences(is);
        } catch (InvalidPreferencesFormatException e) {
            printlnDebug( "Invalid format in '" + filepath + "'; cannot import settings." );
            success = false;
        } catch (IOException e) {
            printlnDebug("I/O problem while trying to load settings from '" + filepath + "'.");
            success = false;
        }
        
        // Register as a listener for every preference node.
        listenToPreference( root );
        
        return success;
    }
    
    public boolean saveSettingsToXML()
    {
        boolean success = true;
        
        try {
            root.exportSubtree(new FileOutputStream( filepath ));
        } catch (IOException e) {
            printlnDebug("I/O problem while trying to save settings to '" + filepath + "'.");
            success = false;
        } catch (BackingStoreException e) {
            printlnDebug("Backing Store problem while trying to save settings to '" + filepath + "'.");
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
     * will retrieve the /keyboard/ctrl node,
     * and get the value associated with the key x.
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
    
    public void set( String path, String value ) { putString( path, value ); }
    public void setString( String path, String value ) { putString( path, value ); }
    public void setInt( String path, int value ) { putInt( path, value ); }
    public void setBoolean( String path, boolean value ) { putBoolean( path, value ); }
    public void put( String path, String value ) { putString( path, value ); }
    public void putString( String path, String value )
    {
        String val = ( value == null ) ? "" : value;
        root.node( getNodePath( path ) ).put( getKey( path ), val );
    }
    public void putInt( String path, int value )
    {
        root.node( getNodePath( path ) ).putInt( getKey( path ), value );
    }
    public void putBoolean( String path, boolean value )
    {
        root.node( getNodePath( path ) ).putBoolean( getKey( path ), value );
    }
    
    /* Call this method with a path that ends in a slash,
     * otherwise the last token in the path will be treated
     * as a key.
     */
    public boolean removeNode( String path )
    {
        boolean success = true;
        try
        {
            root.node( getNodePath( path ) ).removeNode();
        }
        catch( BackingStoreException e )
        {
            printlnDebug( e.getMessage() );
            success = false;
        }
        
        return success;
    }

    public void childAdded( NodeChangeEvent evt )
    {
        Preferences p = evt.getChild();
        try
        {
            if( p.nodeExists( "" ) )
            {
                p.addNodeChangeListener( this );
                p.addPreferenceChangeListener( this );
                saveSettingsToXML();
            }
        }
        catch( BackingStoreException e )
        {
            printlnDebug( e.getMessage() );
        }
    }    
    public void childRemoved( NodeChangeEvent evt ) { }
    public void preferenceChange( PreferenceChangeEvent evt )
    {
        saveSettingsToXML();
    }
    
    public void printSettings( Preferences p_, int level )
    {
        Preferences p = root;
        if( p_ != null )
        {
            p = p_;
        }
        
        String [] keys;
        String [] children;
        try
        {
            children = p.childrenNames();
            for( int i = 0; i < children.length; i++ )
            {
                listenToPreference( p.node( children[ i ] ) );
            }
        }
        catch( BackingStoreException e )
        {
            printlnDebug( e.getMessage() );
        }

        String ind = "";
        for( int i = 0; i < level; i++ )
        {
            ind += "  ";
        }
        
        try
        {
            keys = p.keys();
            for( int i = 0; i < keys.length; i++ )
            {
                printlnDebug( keys[ i ] + ": " + p.get( keys[ i ], "?" ) );
            }
            
            children = p.childrenNames();
            for( int i = 0; i < children.length; i++ )
            {
                printSettings( p.node( children[ i ] ), level + 1 );
            }
        }
        catch( BackingStoreException e )
        {
            printlnDebug( e.getMessage() );
        }
    }
}
