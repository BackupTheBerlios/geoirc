/*
 * SettingsManager.java
 *
 * Created on July 2, 2003, 4:53 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 * @author  netseeker
 */

import geoirc.gui.DisplayManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
    
public class SettingsManager
    implements XmlProcessable
{
    
    protected Document document;
    protected Element root;
    protected XMLOutputter xml_out;
    protected SAXBuilder xml_in;
    
    protected String filepath;
    protected DisplayManager displayMgr = null;
    protected boolean any_load_failure;

    // No default constructor.
    private SettingsManager() { }
    
    public SettingsManager( DisplayManager newDisplayMgr, String filepath )
    {
        displayMgr = newDisplayMgr;
        this.filepath = filepath;
        any_load_failure = false;
        xml_out = new XMLOutputter( "  ", true );
        xml_in = new SAXBuilder();
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
    
    
    public boolean loadSettingsFromXML()
    {
        return loadSettingsFromXML( filepath );
    }
    
    public boolean loadSettingsFromXML( String filepath )
    {
        InputStream is = null;
        boolean success = false;
        try
        {
            is = new BufferedInputStream( new FileInputStream( filepath ) );
            document = xml_in.build( is );
            root = document.getRootElement();
            is.close();
            success = true;
        }
        catch( JDOMException e )
        {
            e.printStackTrace();
        }
        catch( FileNotFoundException e )
        {
            printlnDebug( "File not found: '" + filepath + "'." );
        }
        catch( IOException e )
        {
            printlnDebug( "I/O problem while trying to load settings from '" + filepath + "'." );
        }
        
        if( success == false )
        {
            any_load_failure = true;
        }
        
        return success;
    }

    public boolean saveSettingsToXML()
    {
        return saveSettingsToXML( filepath );
    }

    public boolean saveSettingsToXML( String filepath )
    {
        boolean success = false;
        
        if( ! any_load_failure )
        {
            try {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream( filepath )); 
                xml_out.output( document, out );
                out.close();
                success = true;
            }
            catch( IOException e )
            {
                printlnDebug( "I/O problem while trying to save settings to '" + filepath + "'." );
            }
            catch (BackingStoreException e)
            {
                printlnDebug( "Backing Store problem while trying to save settings to '" + filepath + "'." );
            }
        }
        else
        {
            printlnDebug( "Settings not saved due to previous settings load failure." );
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
    
    /**
     * Creates the node if it does not exist.
     */
    protected Element getNode( String absolute_path )
    {
        if( absolute_path == null )
        {
            throw new NullPointerException();
        }
        
        if(
            ( ! absolute_path.startsWith( "/" ) )
            || ( ! absolute_path.endsWith( "/" ) )
            || ( absolute_path.length() < 2 )
        )
        {
            throw new IllegalArgumentException();
        }
        
        String path = absolute_path.substring( 1 );
        Element element = root;
        int index = path.indexOf( "/" );
        while( index > -1 )
        {
            element = element.getChild( path.substring( 0, index ) );
            path = path.substring( index + 1 );
            index = path.indexOf( "/" );
        }
        
        int index = relative_path.indexOf( "/" );
        if( index == -1 )
        {
            // We have the attribute name.
            return node.getAttributeValue( relative_path, default_ );
        }
        
        // Recurse down to the next child node.
        return getValue(
            node.getChild( relative_path.substring( 0, index ) ),
            relative_path.substring( index + 1 ),
            default_
        );
    }
    
    /* For all the following get methods,
     * the path is specified as a node path, ending with the key desired.
     *
     * e.g. /keyboard/ctrl/x
     * will retrieve the /keyboard/ctrl node,
     * and get the value associated with the key x.
     */
    
    public String get( String path, String default_ ) { return getString( path, default_ ); }
    public String getString( String path_, String default_ )
    {
        String path = path_;
        if( path.startsWith( "/" ) )
        {
            path.substring( 1 );
        }
        return getValue( root, path, default_ );
    }
    public int getInt( String path, int default_ )
    {
        String value = getString( path, Integer.toString( default_ ) );
        int retval = default_;
        try
        {
            retval = Integer.parseInt( value );
        } catch( NumberFormatException e ) { }
        
        return retval;
    }
    public boolean getBoolean( String path, boolean default_ )
    {
        String value = getString( path, Boolean.toString( default_ ) );
        Boolean bool = Boolean.valueOf( value );
        return bool.booleanValue();
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
        saveSettingsToXML();
    }
    public void putInt( String path, int value )
    {
        root.node( getNodePath( path ) ).putInt( getKey( path ), value );
        saveSettingsToXML();
    }
    public void putBoolean( String path, boolean value )
    {
        root.node( getNodePath( path ) ).putBoolean( getKey( path ), value );
        saveSettingsToXML();
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
            saveSettingsToXML();
        }
        catch( BackingStoreException e )
        {
            printlnDebug( e.getMessage() );
            success = false;
        }
        
        return success;
    }

    /*
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
     */
    
    /*
    public void printSettings( Preferences p_, int level )
    {
        Preferences p = root;
        if( p_ != null )
        {
            p = p_;
        }
        
        String ind = "";
        for( int i = 0; i < level; i++ )
        {
            ind += "  ";
        }
        
        printlnDebug( ind + p.name() );
        
        String [] keys;
        String [] children;
        try
        {
            keys = p.keys();
            for( int i = 0; i < keys.length; i++ )
            {
                printlnDebug( ind + keys[ i ] + ": " + p.get( keys[ i ], "?" ) );
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
     */

	/* (non-Javadoc)
	 * @see geoirc.XmlProcessable#getBuffer()
	 */
	public Object getBuffer()
	{
		return root;
	}
	
	public boolean nodeExists(String nodePath)
	{
		try
		{
			return root.nodeExists(getNodePath(nodePath));
		}
		catch (BackingStoreException e)
		{
			return false;
		}
	}
}
