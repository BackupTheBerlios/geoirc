/*
 * SoundManager.java
 *
 * Created on July 7, 2003, 8:30 AM
 */

package geoirc;

import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class SoundManager implements GeoIRCConstants
{
    protected Vector triggers;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    
    // No default constructor
    private SoundManager() { }
    
    /** Creates a new instance of SoundManager */
    public SoundManager(
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        triggers = new Vector();
        
        int i = 0;
        String i_str;
        String filter;
        String regexp;
        String filename;
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            filename = settings_manager.getString(
                "/sound/triggers/" + i_str + "/filename",
                ""
            );
            if( filename.equals( "" ) )
            {
                // No more triggers stored in the settings.
                break;
            }
            
            regexp = settings_manager.getString(
                "/sound/triggers/" + i_str + "/regexp",
                ""
            );
            filter = settings_manager.getString(
                "/sound/triggers/" + i_str + "/filter",
                ""
            );
            
            addTrigger( filter, regexp, filename );
            
            i++;
        }
    }
    
    public void check( String message, String qualities )
    {
        int n = triggers.size();
        SoundTrigger st;
        for( int i = 0; i < n; i++ )
        {
            st = (SoundTrigger) triggers.elementAt( i );
            st.check( message, qualities );
        }
    }
    
    protected boolean addTrigger( String filter, String regexp, String sound_file )
    {
        boolean success = true;
        SoundTrigger st;
        try
        {
            st = new SoundTrigger( display_manager, filter, regexp, sound_file );
            triggers.add( st );
        }
        catch( java.util.regex.PatternSyntaxException e )
        {
            success = false;
        }
        return success;
    }
}
