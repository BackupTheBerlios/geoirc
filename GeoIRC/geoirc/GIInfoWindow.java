/*
 * GIInfoWindow.java
 *
 * Created on July 12, 2003, 1:12 AM
 */

package geoirc;

import javax.swing.*;
import javax.swing.tree.*;
import org.jscroll.*;
import org.jscroll.widgets.*;

/**
 *
 * @author  Pistos
 */
public class GIInfoWindow extends GIWindow
{
    protected JTree tree;
    String path;  // server-channel-user path
    
    public GIInfoWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        String path
    )
    {
        super( display_manager, settings_manager, title );
        this.path = path;
        tree = null;
        selectFrameAndAssociatedButtons();
    }
    
    public boolean isActive()
    {
        return ( tree != null );
    }
    
    public void activate( TreeModel model )
    {
        tree = new JTree( model );
        createScrollPane( tree );
    }
    
    public void deactivate()
    {
        destroyScrollPane();
        tree = null;
    }
    
    public String getPath()
    {
        return path;
    }
}
