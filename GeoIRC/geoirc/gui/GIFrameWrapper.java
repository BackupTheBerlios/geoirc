/*
 * GIFrameWrapper.java
 *
 * Created on October 25, 2003, 12:24 AM
 */

package geoirc.gui;

import geoirc.GeoIRC;
import geoirc.gui.GIWindow;
import java.awt.Container;

/**
 * A wrapper for GIWindows and GeoIRC frames, such that they can be treated
 * as if they were subclasses of a super class.
 *
 * @author  Pistos
 */
public class GIFrameWrapper implements geoirc.GeoIRCConstants
{
    protected Container frame;
    protected int type;
    
    private GIFrameWrapper() { }
    
    public GIFrameWrapper( Container frame )
    {
        this.frame = frame;
        type = UNKNOWN_FRAME_TYPE;
        if( frame instanceof GeoIRC )
        {
            type = GEOIRC_FRAME;
        }
        else if( frame instanceof GIWindow )
        {
            type = GIWINDOW_FRAME;
        }
    }
    
    public Container getFrame()
    {
        return frame;
    }
    
    public int getType()
    {
        return type;
    }
    
    public Container getContentPane()
    {
        Container retval = null;
        switch( type )
        {
            case GEOIRC_FRAME:
                retval = ((GeoIRC) frame).getContentPane();
                break;
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).getContentPane();
                break;
        }
        
        return retval;
    }
    
    public void add( Container c )
    {
        switch( type )
        {
            case GEOIRC_FRAME:
                ((GeoIRC) frame).getContentPane().add( c );
                break;
            case GIWINDOW_FRAME:
                ((GIWindow) frame).addPane( c );
                break;
        }
    }
    
    public String getTitle()
    {
        String retval = null;
        switch( type )
        {
            case GEOIRC_FRAME:
                retval = ((GeoIRC) frame).getTitle();
                break;
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).getTitle();
                break;
        }
        
        return retval;
    }
    
    public void setTitle( String title )
    {
        switch( type )
        {
            case GEOIRC_FRAME:
                ((GeoIRC) frame).setTitle( title );
                break;
            case GIWINDOW_FRAME:
                ((GIWindow) frame).setTitle( title );
                break;
        }
    }
}
