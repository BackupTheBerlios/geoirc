/*
 * DisplayManager.java
 *
 * Created on June 24, 2003, 3:38 PM
 */

package geoirc;

import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import org.jscroll.*;
import org.jscroll.widgets.JScrollInternalFrame;

/**
 *
 * @author  Pistos
 */
public class DisplayManager implements InternalFrameListener
{
    
    protected GITextWindow debug_window;
    protected Vector windows;
    protected JScrollDesktopPane desktop_pane;
    protected JInternalFrame last_activated_frame;
    
    protected int last_added_frame_x;
    protected int last_added_frame_y;
    protected static final int MAX_NEW_WINDOW_X = 500;
    protected static final int MAX_NEW_WINDOW_Y = 400;
    protected static final int MIN_NEW_WINDOW_X = 10;
    protected static final int MIN_NEW_WINDOW_Y = 10;
    protected static final int NEW_WINDOW_X_INCREMENT = 20;
    protected static final int NEW_WINDOW_Y_INCREMENT = 20;
    
    // No default constructor
    private DisplayManager() { }
    
    public DisplayManager( Container content_pane, JMenuBar menu_bar ) {
        windows = new Vector();
        
        desktop_pane = new JScrollDesktopPane( menu_bar );
        content_pane.add( desktop_pane );
        
        debug_window = null;
        debug_window = addTextWindow( "Debug" );
        
        debug_window.appendLine( "GeoIRC started." );
        
        last_activated_frame = null;
        last_added_frame_x = 0;
        last_added_frame_y = 0;
    }

    protected GITextWindow addTextWindow( String title )
    {
        return addTextWindow( title, null, null );
    }

    public GITextWindow addTextWindow( String title, String filter )
    {
        return addTextWindow( title, filter, null );
    }
    
    // Returns the GITextWindow created.
    protected GITextWindow addTextWindow( String title, String filter, RemoteMachine rm )
    {
        String actual_title = title;
        if( actual_title == null )
        {
            actual_title = "";
        }
        GITextWindow text_window = new GITextWindow( this, title, filter, rm );

        if( last_added_frame_x < MAX_NEW_WINDOW_X )
        {
            last_added_frame_x += NEW_WINDOW_X_INCREMENT;
        }
        else
        {
            last_added_frame_x = MIN_NEW_WINDOW_X;
        }
        if( last_added_frame_y < MAX_NEW_WINDOW_Y )
        {
            last_added_frame_y += NEW_WINDOW_Y_INCREMENT;
        }
        else
        {
            last_added_frame_y = MIN_NEW_WINDOW_Y;
        }
        desktop_pane.add( text_window, last_added_frame_x, last_added_frame_y );
        text_window.setBounds( last_added_frame_x, last_added_frame_y, 700, 500 );
        
        windows.add( text_window );
        
        return text_window;
    }
    
    public GITextWindow addServerWindow( Server s )
    {
        return addTextWindow( s.toString(), s.toString(), s );
    }
    
    public GITextWindow addChannelWindow( Server s, String channel_name )
    {
        if( s == null )
        {
            return null;
        }
           
        return addTextWindow(
            channel_name + " on " + s.toString(),
            s.toString() + " AND " + channel_name,
            s
        );
    }
    
    public void printlnDebug( String line )
    {
        debug_window.appendLine( line );
    }
    
    public void println( String line, String qualities )
    {
        int n = windows.size();
        GITextWindow tw;
        Object window;
        for( int i = 0; i < n; i++ )
        {
            tw = null;
            window = windows.elementAt( i );
            if( window instanceof GITextWindow )
            {
                tw = (GITextWindow) windows.elementAt( i );
                if( tw.accepts( qualities ) )
                {
                    tw.appendLine( line );
                }
            }
        }
    }
    
    public JInternalFrame getSelectedFrame()
    {
        return desktop_pane.getSelectedFrame();
    }
    
    public RemoteMachine getSelectedRemoteMachine()
    {
        RemoteMachine retval;
        
        if( last_activated_frame instanceof GITextWindow )
        {
            retval = ((GITextWindow) last_activated_frame).getAssociatedMachine();
        }
        else
        {
            retval = null;
        }
        
        return retval;
    }
    
    public String getSelectedChannel()
    {
        int n = windows.size();
        GITextWindow tw;
        Object window;
        String retval = null;

        for( int i = 0; i < n; i++ )
        {
            window = windows.elementAt( i );
            if( window instanceof GITextWindow )
            {
                String filter = ((GITextWindow) window).getFilter();
                if( filter == null )
                {
                    continue;
                }
                
                // Search for a channel in this filter.
                int pound_index = filter.indexOf( "#" );
                if( pound_index > -1 )
                {
                    int space_index = filter.indexOf( " ", pound_index );
                    if( space_index > -1 )
                    {
                        retval = filter.substring( pound_index, space_index );
                    }
                    else
                    {
                        retval = filter.substring( pound_index );
                    }
                    break;
                }
            }
        }
        
        return retval;
    }
    
    public JInternalFrame getLastActivated()
    {
        return last_activated_frame;
    }
    
    public void internalFrameActivated( InternalFrameEvent e )
    {
        last_activated_frame = e.getInternalFrame();
    }
    
    public void internalFrameClosed(InternalFrameEvent e) {    }
    public void internalFrameClosing(InternalFrameEvent e) {    }
    public void internalFrameDeactivated(InternalFrameEvent e) {    }
    public void internalFrameDeiconified(InternalFrameEvent e) {    }
    public void internalFrameIconified(InternalFrameEvent e) {    }
    public void internalFrameOpened(InternalFrameEvent e) {    }

    public boolean switchToNextWindow( boolean previous )
    {
        if( ( windows == null ) || ( windows.size() < 2 ) )
        {
            return false;
        }
        
        JScrollInternalFrame jif;
        JScrollInternalFrame next_window = (JScrollInternalFrame) windows.elementAt( 0 );
        int n = windows.size();
        for( int i = 0; i < n; i++ )
        {
            jif = (JScrollInternalFrame) windows.elementAt( i );
            if( jif == last_activated_frame )
            {
                int next_index = i + ( previous ? -1 : 1 );
                if( next_index == n )
                {
                    next_index = 0;
                }
                else if( next_index == -1 )
                {
                    next_index = n - 1;
                }
                
                next_window = (JScrollInternalFrame) windows.elementAt( next_index );
                
                break;
            }
        }
        
        next_window.selectFrameAndAssociatedButtons();
        
        return true;
    }
}
