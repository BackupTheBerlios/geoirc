/*
 * GIPane.java
 *
 * Created on July 16, 2003, 11:20 PM
 */

package geoirc;

import java.awt.Container;
import javax.swing.JScrollPane;

/**
 *
 * @author  Pistos
 */
public class GIPane extends JScrollPane
{
    String title;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    
    // No default constructor
    private GIPane() { }

    public GIPane(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        java.awt.Component contents
    )
    {
        super( contents );
        
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;
        this.title = title;
        
        addComponentListener( display_manager );
    }
    
    public String getTitle()
    {
        return title;
    }
}
