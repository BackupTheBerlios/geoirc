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
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
    
public class SettingsManager
    implements XmlProcessable, GeoIRCConstants
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
        xml_out.setTextTrim( true );
        xml_out.setTrimAllWhite( true );
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
        }
        else
        {
            printlnDebug( "Settings not saved due to previous settings load failure." );
        }
        
        return success;
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
     * Also creates any nodes if they do not exist.
     * absolute_path may end in an attribute name, or a slash.
     */
    public Element getNode( String absolute_path )
    {
        return getNode( absolute_path, CREATE_NODES );
    }
    
    /**
     * Returns a List of all nodes matching the path.
     */
    public List getNodes( String absolute_path, boolean create_nodes )
    {
        Element first_node = getNode( absolute_path, create_nodes );
        if( first_node == null )
        {
            return null;
        }
        
        Element parent = first_node.getParent();
        return parent.getChildren( first_node.getName() );
    }
    
    /**
     * @return the Element denoted by the absolute_path, or null if
     * create_nodes is false, and the node doesn't exist.
     */
    protected Element getNode( String absolute_path, boolean create_nodes )
    {
        if( absolute_path == null )
        {
            throw new NullPointerException();
        }
        
        if(
            ( ! absolute_path.startsWith( "/" ) )
            || ( absolute_path.length() < 2 )
        )
        {
            throw new IllegalArgumentException();
        }
        
        String path = absolute_path.substring( 1 );
        Element element = root;
        Element child_element;
        int index = path.indexOf( "/" );
        String node_name;
        while( index > -1 )
        {
            node_name = path.substring( 0, index );
            
            // Convert from pre-0.3.5a node reference format.
            node_name = convertFromOldXMLFormat( node_name );
            
            child_element = element.getChild( node_name );
            if( child_element == null )
            {
                if( create_nodes )
                {
                    child_element = new Element( node_name );
                    element.addContent( child_element );
                }
                else
                {
                    return null;
                }
            }
            element = child_element;
            path = path.substring( index + 1 );
            index = path.indexOf( "/" );
        }
        
        return element;
    }
    
    protected String convertFromOldXMLFormat( String token )
    {
        String retval = token;
        retval = retval.replace( ' ', '_' );
        char first_char = token.charAt( 0 );
        if( ( first_char >= '0' ) && ( first_char <= '9' ) )
        {
            retval = "_" + retval;
        }
        
        return retval;
    }
    
    /**
     * Also creates any nodes and the attribute if they do not exist.
     */
    protected Attribute getAttribute( String absolute_path, String new_value )
    {
        Element element = getNode( absolute_path );
        String attribute_name = convertFromOldXMLFormat( getKey( absolute_path ) );
        Attribute attribute = element.getAttribute( attribute_name );
        if( attribute == null )
        {
            element = element.setAttribute( attribute_name, new_value );
            attribute = element.getAttribute( attribute_name );
        }
        
        return attribute;
    }
    
    /* For all the following get methods,
     * the path is specified as a node path, ending with the key desired.
     *
     * e.g. /keyboard/ctrl/x
     * will retrieve the /keyboard/ctrl node,
     * and get the value of attribute x.
     */
    
    public String get( String path, String default_ ) { return getString( path, default_ ); }
    public String getString( String path, String default_ )
    {
        return getAttribute( path, default_ ).getValue();
    }
    public int getInt( String path, int default_ )
    {
        int retval = default_;
        try
        {
            retval = getAttribute( path, Integer.toString( default_ ) ).getIntValue();
        } catch( DataConversionException e ) { }
        return retval;
    }
    public boolean getBoolean( String path, boolean default_ )
    {
        boolean retval = default_;
        try
        {
            retval = getAttribute( path, Boolean.toString( default_ ) ).getBooleanValue();
        } catch( DataConversionException e ) { }
        return retval;
    }
    
    public void set( String path, String value ) { put( path, value ); }
    public void setString( String path, String value ) { putString( path, value ); }
    public void setInt( String path, int value ) { putInt( path, value ); }
    public void setBoolean( String path, boolean value ) { putBoolean( path, value ); }
    public void put( String path, String value )
    {
        String val = ( value == null ) ? "" : value;
        Attribute attribute = getAttribute( path, val );
        attribute.setValue( val );
        saveSettingsToXML();
    }
    public void putString( String path, String value )
    {
        put( path, value );
    }
    public void putInt( String path, int value )
    {
        put( path, Integer.toString( value ) );
    }
    public void putBoolean( String path, boolean value )
    {
        put( path, Boolean.toString( value ) );
    }
    
    /* Call this method with a path that ends in a slash,
     * otherwise the last token in the path will be treated
     * as a key.
     */
    public boolean removeNode( String absolute_path )
    {
        boolean success = true;
        getNode( absolute_path ).detach();
        saveSettingsToXML();
        return success;
    }
    
    public boolean nodeExists( String absolute_path )
    {
        return( getNode( absolute_path, DONT_CREATE_NODES ) != null );
    }

	/* (non-Javadoc)
	 * @see geoirc.XmlProcessable#getBuffer()
	 */
	public Element getBuffer()
	{
		return root;
	}
}
