/*
 * GIInfoPane.java
 *
 * Created on July 16, 2003, 11:18 PM
 */

package geoirc;

import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  Pistos
 */
public class GIInfoPane extends GIPane
{
    protected JTree tree;
    String path;  // server-channel-user path
    
    public GIInfoPane(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        GIWindow window,
        String title,
        String path
    )
    {
        super( display_manager, settings_manager, window, title, null );
        this.path = path;
        tree = null;
    }
    
    public boolean isActive()
    {
        return ( tree != null );
    }
    
    public void activate( javax.swing.tree.TreeModel model )
    {
        tree = new JTree( model );
        setViewportView( tree );
    }
    
    public void deactivate()
    {
        tree = null;
    }
    
    public String getPath()
    {
        return path;
    }
    
}
