/*
 * MenuManager.java
 *
 * Created on November 4, 2003, 7:21 PM
 */

package geoirc.gui;

import geoirc.SettingsManager;

/**
 *
 * @author  Pistos
 */
public class MenuManager
{
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    
    /** Creates a new instance of MenuManager */
    public MenuManager(
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
    }
    
    public void showPopup( int context, Object param1, Object param2 )
    {
    }
}
