/*
 * GIExternalWindow.java
 *
 * Created on October 28, 2003, 4:17 PM
 */

package geoirc.gui;

import geoirc.SettingsManager;

import java.awt.Container;
//import java.awt.event.MouseEvent;
import javax.swing.JFrame;

/**
 *
 * @author  Pistos
 */
public class GIExternalWindow
    extends JFrame
    implements geoirc.GeoIRCConstants//, java.awt.event.MouseListener
{
    
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected Container pane;
    protected GIPaneWrapper gipw;
    protected GIFrameWrapper gifw;
    
    // No default constructor
    private GIExternalWindow() { }

    public GIExternalWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        PaneVector panes,
        java.util.Vector frames
    )
    {
        super();
        setResizable( true );
        setTitle( title );
        setDefaultCloseOperation( javax.swing.WindowConstants.DISPOSE_ON_CLOSE );
        
        addWindowListener( display_manager );
        addComponentListener( display_manager );
        //addMouseListener( this );
        
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;

        pane = null;
        
        gipw = new GIPaneWrapper(
            settings_manager,
            display_manager,
            getContentPane(),
            "External Content Pane",
            EXTERNAL_CONTENT_PANE
        );
        
        panes.add( gipw );
        
        gifw = new GIFrameWrapper( this );
        frames.add( gifw );
        gipw.setFrame( gifw );
        
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
    
    public void setFrameWrapper( GIFrameWrapper gifw )
    {
        this.gifw = gifw;
    }
    
    public GIFrameWrapper getFrameWrapper()
    {
        return gifw;
    }
    
    public void selectFrame()
    {
        setVisible( true );
    }
    
    public boolean activateFirstTextPane()
    {
        boolean activated = false;
        if( gipw != null )
        {
            GIPaneWrapper gitpw = gipw.getFirstTextPaneWrapper();
            if( gitpw != null )
            {
                gitpw.activate();
                activated = true;
            }
        }
        return activated;
    }
    
    /*
    public void mouseClicked( MouseEvent e ) { }
    public void mouseEntered( MouseEvent e ) { }
    public void mouseExited( MouseEvent e ) { }
    public void mousePressed( MouseEvent e ) { }
    public void mouseReleased( MouseEvent e )
    {
        activateFirstTextPane();
    }
     */
}
