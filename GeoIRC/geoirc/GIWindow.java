/*
 * GIWindow.java
 *
 * Created on July 12, 2003, 1:16 AM
 */

package geoirc;

import java.awt.Color;
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
    protected JComponent pane;
    
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
    
    public void addPane( JComponent pane )
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
    
    public void removePane( JComponent pane )
    {
        getContentPane().remove( pane );
        pane_type = NO_PANE;
        this.pane = null;
    }
    
    public int getPaneType()
    {
        return pane_type;
    }
    
    /**
     * @return either the data pane of this window, or the SplitPane.
     */
    public JComponent getPane()
    {
        JComponent retval = pane;
        if( ( pane != null ) && ( pane.getParent() instanceof JSplitPane ) )
        {
            retval = (JSplitPane) pane.getParent();
        }
        return retval;
    }
    
    public JComponent getActualPane()
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
    
    public void setTitle( String new_title )
    {
        super.setTitle( new_title );
        if( pane != null )
        {
            if( pane instanceof GIPane )
            {
                ((GIPane) pane).setTitle( new_title );
            }
        }
        JToggleButton button = getAssociatedButton();
        if( button != null )
        {
            button.setText( new_title );
        }
    }
    
    public void setButtonColor( Color colour )
    {
        setButtonColour( colour );
    }
    public void setButtonColour( Color colour )
    {
        JToggleButton button = getAssociatedButton();
        if( button != null )
        {
            button.setForeground( colour );
        }
    }
}
