/*
 * GIPane.java
 *
 * Created on July 16, 2003, 11:20 PM
 */

package geoirc.gui;

import geoirc.SettingsManager;

import java.awt.Container;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;

/**
 *
 * @author  Pistos
 */
public class GIPane extends JScrollPane implements java.awt.event.MouseListener
{
    protected String title;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected GIPaneWrapper gipw;
    
    // No default constructor
    private GIPane() { }

    public GIPane(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        java.awt.Component contents
    )
    {
        super( contents );
        
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;
        this.title = title;
        gipw = null;
        
        addComponentListener( display_manager );
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle( String new_title )
    {
        title = new_title;
    }
    
    public void setPaneWrapper( GIPaneWrapper gipw )
    {
        this.gipw = gipw;
    }
    
    public void mouseClicked( MouseEvent e ) { }
    public void mouseEntered( MouseEvent e ) { }
    public void mouseExited( MouseEvent e ) { }
    public void mousePressed( MouseEvent e ) { }
    public void mouseReleased( MouseEvent e )
    {
        gipw.activate();
    }
    
}
