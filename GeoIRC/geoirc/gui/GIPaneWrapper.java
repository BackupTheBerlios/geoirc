/*
 * GIPaneWrapper.java
 *
 * Created on October 24, 2003, 12:05 PM
 */

package geoirc.gui;

import geoirc.GeoIRC;
import geoirc.SettingsManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JSplitPane;

/**
 * A wrapper class used to wrap content panes, split panes and GIPanes,
 * providing extra data fields for GeoIRC's use.
 *
 * @author  Pistos
 */
public class GIPaneWrapper implements geoirc.GeoIRCConstants
{
    protected Container pane;
    protected String title;
    protected int type;
    protected GIFrameWrapper frame;
    protected GIPaneWrapper parent;
    protected GIPaneBarButton button;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    
    // docking position in split pane parent (if any)
    protected int split_rank;
    
    private GIPaneWrapper() { }
    
    public GIPaneWrapper(
        SettingsManager settings_manager,
        DisplayManager display_manager,
        Container pane,
        String title,
        int type
    )
    {
        this.pane = pane;
        this.title = title;
        this.type = type;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        frame = null;
        split_rank = SPLIT_NOT_SPLIT_MEMBER;
        parent = null;
        button = null;
    }
    
    public Container getPane()
    {
        return pane;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public int getType()
    {
        return type;
    }
    
    public GIFrameWrapper getFrame()
    {
        if( frame != null )
        {
            return frame;
        }
        else
        {
            if( parent != null )
            {
                return parent.getFrame();
            }
            else
            {
                return frame;  // null
            }
        }
    }
    
    public int getSplitRank()
    {
        return split_rank;
    }
    
    /**
     * @return the index in panes of the containment parent of this wrapper's pane;
     * NO_PARENT if the parent is not found in panes.
     */
    public int getParentIndex( java.util.Vector panes )
    {
        int index = NO_PARENT;
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            if( parent == panes.elementAt( i ) )
            {
                index = i;
                break;
            }
        }
        
        return index;
    }
    
    public GIPaneWrapper getParent()
    {
        return parent;
    }
    
    public GIPaneBarButton getAssociatedButton()
    {
        return button;
    }
    
    public void setAssociatedButton( GIPaneBarButton button )
    {
        this.button = button;
    }
    
    public void setParent( GIPaneWrapper parent )
    {
        this.parent = parent;
    }
    
    public void setSplitRank( int split_rank )
    {
        this.split_rank = split_rank;
    }
    
    public void setFrame( GIFrameWrapper frame )
    {
        this.title = frame.getTitle();
        this.frame = frame;
    }
    
    public void setTitle( String new_title )
    {
        title = new_title;
        if( frame != null )
        {
            switch( type )
            {
                case CHILD_CONTENT_PANE:
                case EXTERNAL_CONTENT_PANE:
                    frame.setTitle( new_title );
                    break;
            }
        }
    }
    
    public String toString()
    {
        String retval =
            "(" + new Integer( pane.hashCode() ).toString() + ") "
            + "(t" + new Integer( type ).toString() + ") ";
        if( parent != null )
        {
            retval += "(p" + new Integer( parent.getPane().hashCode() ).toString() + ") ";
        }
        if( pane.getParent() != null )
        {
            retval += "(pp" + new Integer( pane.getParent().hashCode() ).toString() + ") ";
        }
        retval += title;
        
        return retval;
            
    }
    
    public void applySettings()
    {
        switch( type )
        {
            case TEXT_PANE:
            case CONSOLE_PANE:
                ((GITextPane) pane).applySettings();
                break;
        }
    }
    
    public void activate()
    {
        if( button != null )
        {
            button.setSelected( true );
            int [] rgb = geoirc.util.Util.getRGB(
                settings_manager.getString(
                    "/gui/normal_button_colour",
                    "000000"
                )
            );
            Color colour = new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
            button.setForeground( colour );
        }
        GIFrameWrapper gifw = getFrame();
        if( gifw != null )
        {
            gifw.activate();
        }
        display_manager.paneActivated( this );
    }
    
    public void highlightButton()
    {
        if( button != null )
        {
            int [] rgb = geoirc.util.Util.getRGB(
                settings_manager.getString(
                    "/gui/new_content_button_colour",
                    "ff0000"
                )
            );
            Color colour = new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
            button.setForeground( colour );
        }
    }
    
    public GIPaneWrapper getFirstTextOrConsolePaneWrapper()
    {
        return getFirstTextOrConsolePaneWrapper( pane );
    }
    
    protected GIPaneWrapper getFirstTextOrConsolePaneWrapper( Component component )
    {
        GIPaneWrapper retval = null;
        if( component != null )
        {
            if( component instanceof JSplitPane )
            {
                JSplitPane split_pane = (JSplitPane) component;
                retval = getFirstTextOrConsolePaneWrapper( split_pane.getTopComponent() );
                if( retval == null )
                {
                    retval = getFirstTextOrConsolePaneWrapper( split_pane.getBottomComponent() );
                }
            }
            else if( component instanceof GITextPane )
            {
                retval = ((GITextPane) component).getPaneWrapper();
            }
            else if( component instanceof GIConsolePane )
            {
                retval = ((GIConsolePane) component).getPaneWrapper();
            }
            else if(
                ( component instanceof GIInfoPane )
                || ( component instanceof org.jscroll.JScrollDesktopPane )
            )
            {
                // Ignore
            }
            else // content pane
            {
                Container container = (Container) component;
                if( container.getComponentCount() > 0 )
                {
                    retval = getFirstTextOrConsolePaneWrapper( container.getComponent( 0 ) );
                }
            }
        }
        
        return retval;
    }
}
