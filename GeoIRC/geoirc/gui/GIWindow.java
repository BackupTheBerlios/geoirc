/*
 * GIWindow.java
 *
 * Created on July 12, 2003, 1:16 AM
 */

package geoirc.gui;

import geoirc.SettingsManager;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.text.*;
import org.jscroll.*;
import org.jscroll.widgets.*;

/**
 *
 * @author  Pistos
 */
public class GIWindow
    extends JScrollInternalFrame
    implements geoirc.GeoIRCConstants, java.awt.event.MouseListener
{
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected Container pane;
    protected GIPaneWrapper gipw;
    protected GIFrameWrapper gifw;
    
    // No default constructor
    private GIWindow() { }

    public GIWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        PaneVector panes,
        java.util.Vector frames
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
        if( getComponentCount() > 1 )
        {
            // A wild guess that component 1 is the title bar.
            getComponent( 1 ).addMouseListener( this );
        }
        addMouseListener( this );
        
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;

        pane = null;
        gifw = new GIFrameWrapper( this );
        
        gipw = new GIPaneWrapper(
            settings_manager,
            display_manager,
            getContentPane(),
            "Content Pane",
            CHILD_CONTENT_PANE
        );
        gipw.setFrame( gifw );
        
        frames.add( gifw );
        panes.add( gipw );
        
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
        return gipw;
    }
    
    public void setPaneWrapper( GIPaneWrapper gipw )
    {
        this.gipw = gipw;
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
    
    public boolean activateFirstTextOrConsolePane()
    {
        boolean activated = false;
        if( gipw != null )
        {
            GIPaneWrapper gitpw = gipw.getFirstTextOrConsolePaneWrapper();
            if( gitpw != null )
            {
                gitpw.activate();
                activated = true;
            }
        }
        return activated;
    }
    
    public void mouseClicked( MouseEvent e ) { }
    public void mouseEntered( MouseEvent e ) { }
    public void mouseExited( MouseEvent e ) { }
    public void mousePressed( MouseEvent e ) { }
    public void mouseReleased( MouseEvent e )
    {
        activateFirstTextOrConsolePane();
    }
    
}
