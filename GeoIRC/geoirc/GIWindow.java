/*
 * GIWindow.java
 *
 * Created on July 12, 2003, 1:16 AM
 */

package geoirc;

import javax.swing.*;
import javax.swing.text.*;
import org.jscroll.*;
import org.jscroll.widgets.*;

/**
 *
 * @author  Pistos
 */
public class GIWindow extends JScrollInternalFrame
{
    protected JScrollPane scroll_pane;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    
    // No default constructor
    private GIWindow() { }

    public GIWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title
    )
    {
        super();
        setIconifiable( true );
        setClosable( true );
        setMaximizable( true );
        setResizable( true );
        setTitle( title );
        
        addInternalFrameListener( display_manager );
        
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;
        
    }
    
    protected void createScrollPane( java.awt.Component contents )
    {
        scroll_pane = new JScrollPane( contents );
        getContentPane().add( scroll_pane );
    }
    
    protected void destroyScrollPane()
    {
        getContentPane().remove( scroll_pane );
        scroll_pane = null;
    }
    
    public JScrollPane getScrollPane()
    {
        return scroll_pane;
    }
}
