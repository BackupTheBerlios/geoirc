/*
 * GIFrameWrapper.java
 *
 * Created on October 25, 2003, 12:24 AM
 */

package geoirc.gui;

import geoirc.GeoIRC;
import geoirc.gui.GIWindow;
import java.awt.Container;
import java.beans.PropertyVetoException;

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
        else if( frame instanceof GIExternalWindow )
        {
            type = GIEXTERNALWINDOW_FRAME;
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
            case GIEXTERNALWINDOW_FRAME:
                retval = ((GIExternalWindow) frame).getContentPane();
                break;
        }
        
        return retval;
    }
    
    public void addPane( Container c )
    {
        switch( type )
        {
            case GEOIRC_FRAME:
                ((GeoIRC) frame).getContentPane().add( c );
                break;
            case GIWINDOW_FRAME:
                ((GIWindow) frame).addPane( c );
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).addPane( c );
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
            case GIEXTERNALWINDOW_FRAME:
                retval = ((GIExternalWindow) frame).getTitle();
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
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).setTitle( title );
                break;
        }
    }
    
    public boolean close() throws PropertyVetoException
    {
        boolean success = false;
        
        switch( type )
        {
            case GIWINDOW_FRAME:
                ((GIWindow) frame).setClosed( true );
                success = true;
                break;
        }
        
        return success;
    }
    
    public void activate()
    {
        switch( type )
        {
            case GIWINDOW_FRAME:
                ((GIWindow) frame).selectFrame();
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).selectFrame();
                break;
        }
    }
    
    public void maximize() throws PropertyVetoException
    {
        switch( type )
        {
            case GIWINDOW_FRAME:
                ((GIWindow) frame).setMaximum( true );
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).setExtendedState( java.awt.Frame.MAXIMIZED_BOTH );
                break;
        }
    }
    
    public void minimize() throws PropertyVetoException
    {
        switch( type )
        {
            case GIWINDOW_FRAME:
                ((GIWindow) frame).setIcon( true );
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).setExtendedState( java.awt.Frame.ICONIFIED );
                break;
        }
    }
    
    public void restore() throws PropertyVetoException
    {
        switch( type )
        {
            case GIWINDOW_FRAME:
                if( isMaximized() )
                {
                    ((GIWindow) frame).setMaximum( false );
                }
                else if( isMinimized() )
                {
                    ((GIWindow) frame).setIcon( false );
                }
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).setExtendedState( java.awt.Frame.NORMAL );
                break;
        }
    }
    
    public void setSize( int width, int height ) throws PropertyVetoException
    {
        switch( type )
        {
            case GIWINDOW_FRAME:
                ((GIWindow) frame).setSize( width, height );
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).setSize( width, height );
                break;
        }
    }
    
    public void setLocation( int x, int y ) throws PropertyVetoException
    {
        switch( type )
        {
            case GIWINDOW_FRAME:
                ((GIWindow) frame).setSize( x, y );
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).setLocation( x, y );
                break;
        }
    }
    
    public void setBounds( int x, int y, int width, int height ) throws PropertyVetoException
    {
        switch( type )
        {
            case GIWINDOW_FRAME:
                ((GIWindow) frame).setBounds( x, y, width, height );
                break;
            case GIEXTERNALWINDOW_FRAME:
                ((GIExternalWindow) frame).setBounds( x, y, width, height );
                break;
        }
    }
    
    public boolean isMinimized()
    {
        boolean retval = false;
        switch( type )
        {
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).isIcon();
                break;
            case GIEXTERNALWINDOW_FRAME:
                retval = (((GIExternalWindow) frame).getExtendedState() & java.awt.Frame.ICONIFIED) > 0;
                break;
        }
        return retval;
    }
    
    public boolean isMaximized()
    {
        boolean retval = false;
        switch( type )
        {
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).isMaximum();
                break;
            case GIEXTERNALWINDOW_FRAME:
                retval = (((GIExternalWindow) frame).getExtendedState() & java.awt.Frame.MAXIMIZED_BOTH) > 0;
                break;
        }
        return retval;
    }
    
    /**
     * @return -1 on error.
     */
    public int getX()
    {
        int retval = -1;
        switch( type )
        {
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).getX();
                break;
            case GIEXTERNALWINDOW_FRAME:
                retval = ((GIExternalWindow) frame).getX();
                break;
        }
        return retval;
    }
    /**
     * @return -1 on error.
     */
    public int getY()
    {
        int retval = -1;
        switch( type )
        {
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).getY();
                break;
            case GIEXTERNALWINDOW_FRAME:
                retval = ((GIExternalWindow) frame).getY();
                break;
        }
        return retval;
    }
    /**
     * @return -1 on error.
     */
    public int getHeight()
    {
        int retval = -1;
        switch( type )
        {
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).getHeight();
                break;
            case GIEXTERNALWINDOW_FRAME:
                retval = ((GIExternalWindow) frame).getHeight();
                break;
        }
        return retval;
    }
    /**
     * @return -1 on error.
     */
    public int getWidth()
    {
        int retval = -1;
        switch( type )
        {
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).getWidth();
                break;
            case GIEXTERNALWINDOW_FRAME:
                retval = ((GIExternalWindow) frame).getWidth();
                break;
        }
        return retval;
    }
    
    public boolean isSelected()
    {
        boolean retval = false;
        switch( type )
        {
            case GIWINDOW_FRAME:
                retval = ((GIWindow) frame).isSelected();
                break;
        }
        return retval;
    }
}
