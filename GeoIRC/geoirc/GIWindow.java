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
public class GIWindow extends JScrollInternalFrame implements GeoIRCConstants
{
    protected JScrollPane scroll_pane;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected int pane_type;
    protected GIPane pane;
    
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

        pane_type = NO_PANE;
        pane = null;
        
        selectFrameAndAssociatedButtons();
    }
    
    public void addPane( GIPane pane )
    {
        if( pane instanceof GIInfoPane )
        {
            pane_type = INFO_PANE;
        }
        else if( pane instanceof GITextPane )
        {
            pane_type = TEXT_PANE;
        }
        getContentPane().add( pane );
        this.pane = pane;
    }
    
    public void removePane( GIPane pane )
    {
        getContentPane().remove( pane );
        pane_type = NO_PANE;
        this.pane = null;
    }
    
    public int getPaneType()
    {
        return pane_type;
    }
    
    public GIPane getPane()
    {
        return pane;
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
