/*
 * PaneVector.java
 *
 * Created on October 26, 2003, 2:18 PM
 */

package geoirc.gui;

import org.jscroll.components.ResizableToolBar;

/**
 *
 * @author  Pistos
 */
public class PaneVector extends java.util.Vector implements geoirc.GeoIRCConstants
{
    protected ResizableToolBar pane_bar;
    protected DisplayManager display_manager;
    
    private PaneVector() { }
    
    public PaneVector( DisplayManager display_manager, ResizableToolBar pane_bar )
    {
        this.pane_bar = pane_bar;
        this.display_manager = display_manager;
    }
    
    public boolean add( GIPaneWrapper gipw )
    {
        switch( gipw.getType() )
        {
            case TEXT_PANE:
            case INFO_PANE:
            {
                GIPaneBarButton gipbb = new GIPaneBarButton( gipw );
                pane_bar.add( gipbb );
                break;
            }
        }
        return super.add( gipw );
    }
    
    public boolean remove( GIPaneWrapper gipw )
    {
        switch( gipw.getType() )
        {
            case TEXT_PANE:
                display_manager.resetLastTextPaneSearched();
                // Drop through...
            case INFO_PANE:
            {
                GIPaneBarButton gipbb = gipw.getAssociatedButton();
                pane_bar.remove( gipbb );
                break;
            }
        }
        return super.remove( gipw );
    }
}
