/*
 * GIWindow.java
 *
 * Created on July 12, 2003, 1:16 AM
 */

package geoirc.gui;

import geoirc.SettingsManager;

import java.awt.Color;
import java.awt.Container;
import javax.swing.*;
import javax.swing.text.*;
import org.jscroll.*;
import org.jscroll.widgets.*;

/**
 *
 * @author  Pistos
 */
public class GIWindow extends JScrollInternalFrame implements geoirc.GeoIRCConstants
{
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected Container pane;
    protected GIPaneWrapper pane_wrapper;
    protected GIFrameWrapper gifw;
    
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
        addComponentListener( display_manager );
        
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;

        pane = null;
        pane_wrapper = null;
        gifw = null;
        
        selectFrame();
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
    
    public void selectFrame()
    {
        try
        {
            setSelected( true );
            setIcon( false ); // select and de-iconify the frame
        } catch (java.beans.PropertyVetoException pve)
        {
            display_manager.printlnDebug( pve.getMessage() );
        }
        setVisible( true );
    }
    
    public void setFrameWrapper( GIFrameWrapper gifw )
    {
        this.gifw = gifw;
    }
    
    public GIFrameWrapper getFrameWrapper()
    {
        return gifw;
    }
}
