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
    protected GIFrameWrapper frame;
    protected GIPaneWrapper parent;
    
    // docking position in split pane parent (if any)
    protected int split_rank;
    
    private GIPaneWrapper() { }
    
    public GIPaneWrapper( Container pane, String title, int type )
    {
        this.pane = pane;
        this.title = title;
        this.type = type;
        frame = null;
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
    
    public GIFrameWrapper getFrame()
    {
        return frame;
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
    
    public void setFrame( GIFrameWrapper frame )
    {
        this.title = frame.getTitle();
        this.frame = frame;
    }
    
    public void setTitle( String new_title )
    {
        title = new_title;
        if( ( type == CHILD_CONTENT_PANE ) && ( frame != null ) )
        {
            frame.setTitle( new_title );
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
                ((GITextPane) pane).applySettings();
                break;
        }
    }
    
}
