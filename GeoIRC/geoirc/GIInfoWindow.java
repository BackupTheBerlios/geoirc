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
    
    public GIInfoWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        TreeModel model
    )
    {
        super( display_manager, settings_manager, title );
        tree = new JTree( model );
        createScrollPane( tree );
        selectFrameAndAssociatedButtons();
    }
}
