/*
 * GIExternalWindow.java
 *
 * Created on October 28, 2003, 4:17 PM
 */

package geoirc.gui;

import geoirc.SettingsManager;

import java.awt.Container;
import javax.swing.JFrame;

/**
 *
 * @author  Pistos
 */
public class GIExternalWindow extends JFrame
{
    
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected Container pane;
    protected GIPaneWrapper pane_wrapper;
    
    // No default constructor
    private GIExternalWindow() { }

    public GIExternalWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title
    )
    {
        super();
        setResizable( true );
        setTitle( title );
        
        addWindowListener( display_manager );
        addComponentListener( display_manager );
        
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;

        pane = null;
        pane_wrapper = null;
    }
    
    public void addPane( Container pane )
    {
        getContentPane().add( pane );
        this.pane = pane;
        pane.addComponentListener( display_manager );
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
    }
    
    public GIPaneWrapper getPaneWrapper()
    {
        return pane_wrapper;
    }
    
    public void setPaneWrapper( GIPaneWrapper pane_wrapper )
    {
        this.pane_wrapper = pane_wrapper;
    }
    
}
