/*
 * TriggerManager.java
 *
 * Created on August 6, 2003, 12:03 PM
 */

package geoirc;

import geoirc.gui.DisplayManager;

import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class TriggerManager implements GeoIRCConstants
{
    protected Vector triggers;
    protected CommandExecutor executor;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected I18nManager i18n_manager;
    
    // No default constructor
    private TriggerManager() { }
    
    public TriggerManager(
        CommandExecutor executor,
        SettingsManager settings_manager,
        DisplayManager display_manager,
        I18nManager i18n_manager
    )
    {
        this.executor = executor;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        triggers = new Vector();
        
        int i = 0;
        String i_str;
        String filter;
        String regexp;
        String command;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            command = settings_manager.getString(
                "/triggers/" + i_str + "/command",
                ""
            );
            if( command.equals( "" ) )
            {
                // No more triggers stored in the settings.
                break;
            }
            
            regexp = settings_manager.getString(
                "/triggers/" + i_str + "/regexp",
                ""
            );
            filter = settings_manager.getString(
                "/triggers/" + i_str + "/filter",
                ""
            );
            
            addTrigger( filter, regexp, command );
            
            i++;
        }
    }
    
    public void check( String message, String qualities )
    {
        int n = triggers.size();
        Trigger trigger;
        for( int i = 0; i < n; i++ )
        {
            trigger = (Trigger) triggers.elementAt( i );
            trigger.check( message, qualities );
        }
    }
    
    protected boolean addTrigger( String filter, String regexp, String command )
    {
        boolean success = true;
        Trigger trigger;
        try
        {
            trigger = new Trigger( executor, display_manager, i18n_manager, filter, regexp, command );
            triggers.add( trigger );
        }
        catch( java.util.regex.PatternSyntaxException e )
        {
            success = false;
        }
        return success;
    }
}
