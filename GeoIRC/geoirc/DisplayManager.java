/*
 * DisplayManager.java
 *
 * Created on June 24, 2003, 3:38 PM
 */

package geoirc;

import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowStateListener;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreeModel;
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
        ComponentListener,
        KeyListener
{
    protected SettingsManager settings_manager;
    protected StyleManager style_manager;
    protected HighlightManager highlight_manager;
    protected Vector windows;
    protected JScrollDesktopPane desktop_pane;
    protected JInternalFrame last_activated_frame;
    protected JTextField input_field;
    protected boolean listening;
    protected Vector inactive_info_windows;
    
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
        SettingsManager settings_manager,
        JTextField input_field
    )
    {
        listening = false;
        
        this.settings_manager = settings_manager;
        style_manager = new StyleManager( settings_manager, this );
        highlight_manager = new HighlightManager( settings_manager, this );

        windows = new Vector();
        inactive_info_windows = new Vector();
        
        desktop_pane = new JScrollDesktopPane( menu_bar );
        content_pane.add( desktop_pane );
        this.input_field = input_field;

        // Re-map the Tab-related default mappings which have to do with focus traversal.
        desktop_pane.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            Collections.EMPTY_SET
        );
        desktop_pane.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, 
            Collections.EMPTY_SET
        );
        
        restoreDesktopState();
        
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
    
    protected void addNewWindow( GIWindow window )
    {
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
        desktop_pane.add( window, last_added_frame_x, last_added_frame_y );
        window.setBounds(
            last_added_frame_x,
            last_added_frame_y,
            DEFAULT_WINDOW_WIDTH,
            DEFAULT_WINDOW_HEIGHT
        );
    }

    protected GITextWindow addTextWindow( String title )
    {
        return addTextWindow( title, null );
    }

    public GITextWindow addTextWindow( String title, String filter )
    {
        String actual_title = title;
        if( actual_title == null )
        {
            actual_title = "";
        }
        GITextWindow text_window = new GITextWindow(
            this, settings_manager, style_manager, title, filter
        );

        addNewWindow( text_window );
        
        return text_window;
    }
    
    public GIInfoWindow addInfoWindow( String title, String path )
    {
        String actual_title = title;
        if( actual_title == null )
        {
            actual_title = "";
        }
        GIInfoWindow info_window = new GIInfoWindow(
            this, settings_manager, title, path
        );

        addNewWindow( info_window );
        inactive_info_windows.add( info_window );
        
        return info_window;
    }
    
    public GITextWindow addChannelWindow( Server s, String channel_name )
    {
        if( s == null )
        {
            return null;
        }
           
        return addTextWindow(
            channel_name + " on " + s.toString(),
            s.toString() + " AND " + channel_name
        );
    }
    
    public void activateInfoWindows( String path, TreeModel model )
    {
        int n = inactive_info_windows.size();
        GIInfoWindow giiw;
        for( int i = 0; i < n; i++ )
        {
            giiw = (GIInfoWindow) inactive_info_windows.elementAt( i );
            if( giiw.getPath().equals( path ) )
            {
                giiw.activate( model );
                inactive_info_windows.remove( i );
            }
        }
    }
    
    public void deactivateInfoWindows( String path )
    {
        int n = windows.size();
        GIWindow window;
        for( int i = 0; i < n; i++ )
        {
            window = (GIWindow) windows.elementAt( i );
            if( window instanceof GIInfoWindow )
            {
                GIInfoWindow giiw = (GIInfoWindow) window;
                if( giiw.getPath().equals( path ) )
                {
                    giiw.deactivate();
                    inactive_info_windows.add( giiw );
                }
            }
        }
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
                    tw.appendLine( highlight_manager.highlight( line, qualities ) );
                }
            }
        }
    }
    
    public JInternalFrame getSelectedFrame()
    {
        return desktop_pane.getSelectedFrame();
    }
    
    public String getSelectedChannel()
    {
        GITextWindow tw;
        String retval = null;
        JInternalFrame jif = last_activated_frame;
        if( jif == null )
        {
            jif = getSelectedFrame();
        }

        if( jif instanceof GITextWindow )
        {
            String filter = ((GITextWindow) jif).getFilter();
            if( filter != null )
            {
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

    public void keyPressed( KeyEvent e )
    {
    }
    
    public void keyReleased( KeyEvent e )
    {
    }
    
    public void keyTyped( KeyEvent e )
    {
        // The user is accidentally typing into a window.
        // Redirect them to the input field.
        
        String mods = e.getModifiersExText( e.getModifiersEx() );
        if( mods.equals( "" ) || mods.equals( "Shift" ) ) 
        {
            String character = "" + e.getKeyChar();

            input_field.grabFocus();
            input_field.setText(
                input_field.getText()
                + character
            );
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
    
    public boolean switchToWindow( String regexp )
    {
        boolean success = false;
        
        if( regexp != null )
        {
            JScrollInternalFrame jif;
            int n = windows.size();
            JInternalFrame start = getSelectedFrame();
            
            for( int i = 0; i < n; i++ )
            {
                jif = (JScrollInternalFrame) windows.elementAt( i );
                if( jif != start )
                {
                    if( java.util.regex.Pattern.matches( regexp, jif.getTitle() ) )
                    {
                        jif.selectFrameAndAssociatedButtons();
                        success = true;
                        break;
                    }
                }
            }
        }
        
        return success;
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
            
            int state = GI_NORMAL;
            if( frame.isMaximum() )
            {
                state = GI_MAXIMIZED;
            }
            else if( frame.isIcon() )
            {
                state = GI_MINIMIZED;
            }
            settings_manager.putInt(
                "/gui/desktop/" + i_str + "/state",
                state
            );
            
            settings_manager.putBoolean(
                "/gui/desktop/" + i_str + "/selected",
                frame.isSelected()
            );
            
            if( frame instanceof GITextWindow )
            {
                settings_manager.putString(
                    "/gui/desktop/" + i_str + "/filter",
                    ((GITextWindow) frame).getFilter()
                );
            }
            else if( frame instanceof GIInfoWindow )
            {
                settings_manager.putString(
                    "/gui/desktop/" + i_str + "/path",
                    ((GIInfoWindow) frame).getPath()
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
        int state;
        boolean is_selected;
        
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
            state = settings_manager.getInt(
                "/gui/desktop/" + i_str + "/state",
                GI_NORMAL
            );
            is_selected = settings_manager.getBoolean(
                "/gui/desktop/" + i_str + "/selected",
                false
            );
            
            boolean type_known = true;
            GIWindow frame = null;
            if( type.equals( "GITextWindow" ) )
            {
                String filter = settings_manager.getString(
                    "/gui/desktop/" + i_str + "/filter",
                    ""
                );
                
                GITextWindow gitw = addTextWindow( title, filter );
                frame = gitw;
            }
            else if( type.equals( "GIInfoWindow" ) )
            {
                String path = settings_manager.getString(
                    "/gui/desktop/" + i_str + "/path",
                    "/"
                );
                
                GIInfoWindow giiw = addInfoWindow( title, path );
                frame = giiw;
            }
            else
            {
                // Huh?  Unknown window type.
                type_known = false;
                printlnDebug( "Unknown window type in settings." );
            }
            
            if( type_known )
            {
                frame.setBounds( x, y, width, height );
                try
                {
                    switch( state )
                    {
                        case GI_MAXIMIZED:
                            frame.setMaximum( true );
                            break;
                        case GI_MINIMIZED:
                            frame.setIcon( true );
                            break;
                        case GI_NORMAL:
                        default:
                            frame.setMaximum( false );
                            break;
                    }
                    frame.setSelected( is_selected );
                }
                catch( java.beans.PropertyVetoException e )
                {
                    // Do nothing about this error.
                }
            }
            
            i++;
        }
    }    
    
}
