/*
 * HighlightManager.java
 *
 * Created on July 9, 2003, 3:06 PM
 */

package geoirc;

import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class HighlightManager implements GeoIRCConstants
{
    
    protected Vector triggers;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    
    // No default constructor
    private HighlightManager() { }
    
    public HighlightManager(
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
        String format;
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            format = settings_manager.getString(
                "/gui/text windows/highlighting/" + i_str + "/format",
                ""
            );
            if( format.equals( "" ) )
            {
                // No more highlight rules stored in the settings.
                break;
            }
            
            regexp = settings_manager.getString(
                "/gui/text windows/highlighting/" + i_str + "/regexp",
                ""
            );
            filter = settings_manager.getString(
                "/gui/text windows/highlighting/" + i_str + "/filter",
                ""
            );
            
            addTrigger( filter, regexp, format );
            
            i++;
        }
    }
    
    public String highlight( String line, String qualities )
    {
        /* TODO: There's the problem that the highlighting rules
         * also apply to the raw formatting text, as well.  :(
         */
        
        int n = triggers.size();
        HighlightTrigger ht;
        String highlighted_line = line;
        for( int i = 0; i < n; i++ )
        {
            ht = (HighlightTrigger) triggers.elementAt( i );
            highlighted_line = ht.highlight( highlighted_line, qualities );
        }
        
        return highlighted_line;
    }
    
    protected boolean addTrigger( String filter, String regexp, String format )
    {
        boolean success = true;
        HighlightTrigger ht;
        try
        {
            ht = new HighlightTrigger( display_manager, filter, regexp, format );
            triggers.add( ht );
        }
        catch( java.util.regex.PatternSyntaxException e )
        {
            success = false;
        }
        return success;
    }
    
}
