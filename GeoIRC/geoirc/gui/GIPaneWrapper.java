/*
 * GIPaneWrapper.java
 *
 * Created on October 24, 2003, 12:05 PM
 */

package geoirc.gui;

import geoirc.GeoIRC;
import java.awt.Container;

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
    protected Container window;
    protected GIPaneWrapper parent;
    
    // docking position in split pane parent (if any)
    protected int split_rank;
    
    private GIPaneWrapper() { }
    
    public GIPaneWrapper( Container pane, String title, int type )
    {
        this.pane = pane;
        this.title = title;
        this.type = type;
        window = null;
        split_rank = SPLIT_NOT_SPLIT_MEMBER;
        parent = null;
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
    
    public Container getWindow()
    {
        return window;
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
    
    public void setParent( GIPaneWrapper parent )
    {
        this.parent = parent;
    }
    
    public void setSplitRank( int split_rank )
    {
        this.split_rank = split_rank;
    }
    
    public boolean setWindow( Container window )
    {
        boolean success = false;
        
        if( window instanceof GeoIRC )
        {
            this.title = ((GeoIRC) window).getTitle();
            this.window = window;
            success = true;
        }
        else if( window instanceof GIWindow )
        {
            this.title = ((GIWindow) window).getTitle();
            this.window = window;
            success = true;
        }
        
        return success;
    }
    
    public void setTitle( String new_title )
    {
        title = new_title;
        if( ( type == CHILD_CONTENT_PANE ) && ( window != null ) )
        {
            ((GIWindow) window).setTitle( new_title );
        }
    }
    
    public String toString()
    {
        return title;
    }
    
    public void applySettings()
    {
        switch( type )
        {
            case TEXT_PANE:
                ((GITextPane) pane).applySettings();
                break;
        }
    }
    
}
