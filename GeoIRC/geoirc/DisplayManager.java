/*
 * DisplayManager.java
 *
 * Created on June 24, 2003, 3:38 PM
 */

package geoirc;

import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.WindowStateListener;
import java.awt.event.WindowEvent;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import org.jscroll.*;
import org.jscroll.widgets.JScrollInternalFrame;

/**
 *
 * @author  Pistos
 */
public class DisplayManager
    implements
        InternalFrameListener,
        GeoIRCConstants,
        ComponentListener
{
    protected SettingsManager settings_manager;
    protected Vector windows;
    protected JScrollDesktopPane desktop_pane;
    protected JInternalFrame last_activated_frame;
    protected boolean listening;
    
    protected int last_added_frame_x;
    protected int last_added_frame_y;
    protected static final int MAX_NEW_WINDOW_X = 500;
    protected static final int MAX_NEW_WINDOW_Y = 400;
    protected static final int MIN_NEW_WINDOW_X = 10;
    protected static final int MIN_NEW_WINDOW_Y = 10;
    protected static final int NEW_WINDOW_X_INCREMENT = 20;
    protected static final int NEW_WINDOW_Y_INCREMENT = 20;
    protected static final int DEFAULT_WINDOW_WIDTH = 700;
    protected static final int DEFAULT_WINDOW_HEIGHT = 500;
    
    // No default constructor
    private DisplayManager() { }
    
    public DisplayManager(
        Container content_pane,
        JMenuBar menu_bar,
        SettingsManager settings_manager
    )
    {
        listening = false;
        
        this.settings_manager = settings_manager;
        windows = new Vector();
        
        desktop_pane = new JScrollDesktopPane( menu_bar );
        content_pane.add( desktop_pane );
        
        restoreDesktopState();
        
        /*
        if( getDebugWindow() == null )
        {
            addTextWindow( "Debug", "debug" );
        }
         */
        
        printlnDebug( "GeoIRC started." );
        
        last_activated_frame = null;
        last_added_frame_x = 0;
        last_added_frame_y = 0;
    }
    
    public void beginListening()
    {
        listening = true;
    }
    
    /* @return null if no window accepts debug quality messages
     * otherwise returns the first GITextWindow which does.
     */
    protected GITextWindow getDebugWindow()
    {
        int n = windows.size();
        GITextWindow debug_window = null;
        JInternalFrame jif;
        for( int i = 0; i < n; i++ )
        {
            jif = (JInternalFrame) windows.elementAt( i );
            if( jif instanceof GITextWindow )
            {
                GITextWindow gitw = (GITextWindow) jif;
                if( gitw.accepts( "debug" ) )
                {
                    debug_window = gitw;
                    break;
                }
            }
        }
        
        return debug_window;
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
        GITextWindow text_window = new GITextWindow( this, settings_manager, title, filter, rm );

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
        text_window.setBounds( last_added_frame_x, last_added_frame_y, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT );
        
        printlnDebug( Integer.toString( windows.size() ) + " windows" );
        
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
        println( line, "debug" );
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
    
    protected void addWindowToVector( JInternalFrame jif )
    {
        windows.add( jif );
        jif.addComponentListener( this );
    }
    
    /* ************************************************************
     * Listener Implementations
     */
    
    public void internalFrameActivated( InternalFrameEvent e )
    {
        last_activated_frame = e.getInternalFrame();
    }
    
    public void internalFrameClosed(InternalFrameEvent e)
    {
        windows.remove( e.getSource() );
        if( listening )
        {
            recordDesktopState();
        }
    }
    public void internalFrameClosing(InternalFrameEvent e) {    }
    public void internalFrameDeactivated(InternalFrameEvent e) {    }
    public void internalFrameDeiconified(InternalFrameEvent e) {    }
    public void internalFrameIconified(InternalFrameEvent e) {    }
    public void internalFrameOpened(InternalFrameEvent e)
    {
        JInternalFrame jif = (JInternalFrame) e.getSource();
        jif.addComponentListener( this );
        windows.add( jif );
        if( listening )
        {
            recordDesktopState();
        }
    }

    public void componentHidden(java.awt.event.ComponentEvent e) { }
    public void componentShown(java.awt.event.ComponentEvent e) { }
    public void componentMoved(java.awt.event.ComponentEvent e)
    {
        if( listening )
        {
            recordDesktopState();
        }
    }
    public void componentResized(java.awt.event.ComponentEvent e)
    {
        if( listening )
        {
            recordDesktopState();
        }
    }
    
    /* ************************************************************ */
    
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
    
    protected void recordDesktopState()
    {
        settings_manager.removeNode( "/gui/desktop/" );
        int n = windows.size();
        JInternalFrame frame;
        String i_str;
        for( int i = 0; i < n; i++ )
        {
            frame = (JInternalFrame) windows.elementAt( i );
            i_str = Integer.toString( i );
            
            settings_manager.putString(
                "/gui/desktop/" + i_str + "/type", 
                frame.getClass().toString()
            );
            
            settings_manager.putString(
                "/gui/desktop/" + i_str + "/title",
                frame.getTitle()
            );
            settings_manager.putInt(
                "/gui/desktop/" + i_str + "/x",
                frame.getX()
            );
            settings_manager.putInt(
                "/gui/desktop/" + i_str + "/y",
                frame.getY()
            );
            settings_manager.putInt(
                "/gui/desktop/" + i_str + "/height",
                frame.getHeight()
            );
            settings_manager.putInt(
                "/gui/desktop/" + i_str + "/width",
                frame.getWidth()
            );
            
            if( frame instanceof GITextWindow )
            {
                settings_manager.putString(
                    "/gui/desktop/" + i_str + "/filter",
                    ((GITextWindow) frame).getFilter()
                );
            }
        }
    }

    protected void restoreDesktopState()
    {
        int i = 0;
        String i_str;
        
        String type;
        String title;
        int x;
        int y;
        int width;
        int height;
        String filter;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            type = settings_manager.getString(
                "/gui/desktop/" + i_str + "/type", 
                ""
            );
            
            if( type.equals( "" ) )
            {
                // No more windows stored in the settings.
                break;
            }
            
            type = type.substring( type.lastIndexOf( "." ) + 1 );
            
            title = settings_manager.getString(
                "/gui/desktop/" + i_str + "/title",
                "GeoIRC"
            );
            
            x = settings_manager.getInt(
                "/gui/desktop/" + i_str + "/x",
                MIN_NEW_WINDOW_X
            );
            y = settings_manager.getInt(
                "/gui/desktop/" + i_str + "/y",
                MIN_NEW_WINDOW_Y
            );
            height = settings_manager.getInt(
                "/gui/desktop/" + i_str + "/height",
                DEFAULT_WINDOW_HEIGHT
            );
            width = settings_manager.getInt(
                "/gui/desktop/" + i_str + "/width",
                DEFAULT_WINDOW_WIDTH
            );
            
            if( type.equals( "GITextWindow" ) )
            {
                filter = settings_manager.getString(
                    "/gui/desktop/" + i_str + "/filter",
                    ""
                );
                
                GITextWindow gitw = addTextWindow( title, filter );
                gitw.setBounds( x, y, width, height );
            }
            else
            {
                // Huh?  Unknown window type.
                printlnDebug( "Unknown window type in settings." );
            }
            
            i++;
        }
    }    
    
}
