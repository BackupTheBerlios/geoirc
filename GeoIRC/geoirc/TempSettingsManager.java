/*
 * TempSettingsManager.java
 *
 * Created on July 2, 2003, 7:33 AM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public class TempSettingsManager
{
    
    /** Creates a new instance of TempSettingsManager */
    public TempSettingsManager()
    {
    }
    
    public Object getSetting( String path )
    {
        if( ( path == null ) || ( path.equals( "" ) ) )
        {
            return null;
        }
        
        if( path.equals( "/Keyboard/CTRL/TAB" ) )
        {
            return (Object) "nextwindow";
        }
        else if( path.equals( "/Keyboard/CTRL/SHIFT/TAB" ) )
        {
            return (Object) "nextwindow";
        }
        else if( path.equals( "/Nicks/Nick" ) )
        {
            return (Object) "Pistos|GeoIRC";
        }
        else if( path.equals( "" ) )
        {
        }
        else if( path.equals( "" ) )
        {
        }
        else if( path.equals( "" ) )
        {
        }
        else if( path.equals( "" ) )
        {
        }
    }
}
