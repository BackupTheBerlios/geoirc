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
import java.util.Hashtable;
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
    protected boolean listening;

    protected JTextField input_field;
    
    protected Container content_pane;
    protected JScrollDesktopPane desktop_pane;
    
    protected Vector windows;
    protected JInternalFrame last_activated_frame;
    protected int last_added_frame_x;
    protected int last_added_frame_y;
    
    protected Vector inactive_info_panes;
    protected Vector active_info_panes;
    protected Vector docked_panes;
    protected Vector undocked_panes;
    protected Vector panes;
    
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
        inactive_info_panes = new Vector();
        active_info_panes = new Vector();
        docked_panes = new Vector();
        undocked_panes = new Vector();
        panes = new Vector();
        
        this.content_pane = content_pane;
        desktop_pane = new JScrollDesktopPane( menu_bar );
        content_pane.add( desktop_pane );
        this.input_field = input_field;
        
        restoreDesktopState();
        
        last_activated_frame = null;
        last_added_frame_x = 0;
        last_added_frame_y = 0;
    }
    
    public void beginListening()
    {
        listening = true;
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

    protected GIWindow addTextWindow( String title )
    {
        return addTextWindow( title, null );
    }
    
    protected GITextPane addTextPane( String title, String filter )
    {
        GITextPane gitp = new GITextPane(
            this, settings_manager, style_manager, title, filter
        );
        panes.add( gitp );
        return gitp;
    }

    public GIWindow addTextWindow( String title, String filter )
    {
        String actual_title = title;
        if( actual_title == null )
        {
            actual_title = "";
        }
        GIWindow text_window = new GIWindow(
            this, settings_manager, actual_title
        );
        GITextPane gitp = addTextPane( actual_title, filter );
        undocked_panes.add( gitp );
        text_window.addPane( gitp );
        addNewWindow( text_window );
        
        return text_window;
    }
    
    protected GIInfoPane addInfoPane( String title, String path )
    {
        GIInfoPane giip = new GIInfoPane(
            this, settings_manager, title, path
        );
        panes.add( giip );
        return giip;
    }
    
    public GIWindow addInfoWindow( String title, String path )
    {
        String actual_title = title;
        if( actual_title == null )
        {
            actual_title = "";
        }
        GIWindow info_window = new GIWindow( this, settings_manager, actual_title );
        GIInfoPane giip = addInfoPane( actual_title, path );
        info_window.addPane( giip );
        undocked_panes.add( giip );

        addNewWindow( info_window );
        inactive_info_panes.add( giip );
        
        return info_window;
    }
    
    public GIWindow addChannelWindow( Server s, String channel_name )
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
    
    public void activateInfoPanes( String path, TreeModel model )
    {
        int n = inactive_info_panes.size();
        GIInfoPane giip;
        for( int i = 0; i < n; i++ )
        {
            giip = (GIInfoPane) inactive_info_panes.elementAt( i );
            if( giip.getPath().equals( path ) )
            {
                giip.activate( model );
                inactive_info_panes.remove( i );
                active_info_panes.add( giip );
            }
        }
    }
    
    public void deactivateInfoPanes( String path )
    {
        int n = active_info_panes.size();
        GIInfoPane giip;
        for( int i = 0; i < n; i++ )
        {
            giip = (GIInfoPane) active_info_panes.elementAt( i );
            if( giip.getPath().equals( path ) )
            {
                giip.deactivate();
                active_info_panes.remove( i );
                inactive_info_panes.add( giip );
            }
        }
    }
    
    public void printlnDebug( String line )
    {
        println( line, "debug" );
    }
    
    public void println( String line, String qualities )
    {
        if( line == null )
        {
            return;
        }
        
        int n = panes.size();
        GITextPane text_pane;
        GIPane pane;
        for( int i = 0; i < n; i++ )
        {
            text_pane = null;
            pane = (GIPane) panes.elementAt( i );
            if( pane instanceof GITextPane )
            {
                text_pane = (GITextPane) pane;
                if( text_pane.accepts( qualities ) )
                {
                    text_pane.appendLine( highlight_manager.highlight( line, qualities ) );
                }
            }
        }
    }
    
    public GIWindow getSelectedFrame()
    {
        return (GIWindow) desktop_pane.getSelectedFrame();
    }
    
    public String getSelectedChannel()
    {
        GIPane pane;
        String retval = null;
        GIWindow giw = (GIWindow) last_activated_frame;
        if( giw == null )
        {
            giw = getSelectedFrame();
        }

        if( giw != null )
        {
            pane = giw.getPane();
            if( pane instanceof GITextPane )
            {
                String filter = ((GITextPane) pane).getFilter();
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
        }
        
        return retval;
    }

    public JInternalFrame getLastActivated()
    {
        return last_activated_frame;
    }
    
    public boolean dock( int location, int window )
    {
        boolean success = false;
        
        if( ( windows != null )
            && ( windows.size() > 0 )
            && ( window < windows.size() )
        )
        {
            GIWindow giw = (GIWindow) windows.elementAt( window );
            GIPane pane = giw.getPane();
            
            if( dock( location, pane ) )
            {
                success = true;
                giw.dispose();
            }
        }

        return success;
    }
    
    protected boolean dock( int location, GIPane pane )
    {
        boolean success = false;
        
        if(
            ( location == DOCK_TOP )
            || ( location == DOCK_BOTTOM )
            || ( location == DOCK_LEFT )
            || ( location == DOCK_RIGHT )
        )
        {
            JSplitPane split_pane = null;
            Container desktop_container = desktop_pane.getParent();
            boolean was_primary = true;
            if( desktop_container instanceof JSplitPane )
            {
                JSplitPane sp = (JSplitPane) desktop_container;
                was_primary = ( sp.getTopComponent() == desktop_pane );
            }
            desktop_container.remove( desktop_pane );
            switch( location )
            {
                case DOCK_TOP:
                    {
                        split_pane = new JSplitPane(
                            JSplitPane.VERTICAL_SPLIT,
                            pane,
                            desktop_pane
                        );
                        split_pane.setDividerLocation( DEFAULT_DOCK_WEIGHT );
                    }
                    break;
                case DOCK_RIGHT:
                    {
                        split_pane = new JSplitPane(
                            JSplitPane.HORIZONTAL_SPLIT,
                            desktop_pane, 
                            pane
                        );
                        split_pane.setDividerLocation( 1.0 - DEFAULT_DOCK_WEIGHT );
                    }
                    break;
                case DOCK_BOTTOM:
                    {
                        split_pane = new JSplitPane(
                            JSplitPane.VERTICAL_SPLIT,
                            desktop_pane, 
                            pane
                        );
                        split_pane.setDividerLocation( 1.0 - DEFAULT_DOCK_WEIGHT );
                    }
                    break;
                case DOCK_LEFT:
                    {
                        split_pane = new JSplitPane(
                            JSplitPane.HORIZONTAL_SPLIT,
                            pane,
                            desktop_pane
                        );
                        split_pane.setDividerLocation( DEFAULT_DOCK_WEIGHT );
                    }
                    break;
                default:
                    break;
            }

            if( desktop_container instanceof JSplitPane )
            {
                JSplitPane sp = (JSplitPane) desktop_container;
                if( was_primary )
                {
                    sp.setTopComponent( split_pane );
                }
                else
                {
                    sp.setBottomComponent( split_pane );
                }
            }
            else
            {
                desktop_container.add( split_pane );
            }

            docked_panes.add( pane );
            undocked_panes.remove( pane );
            
            success = true;
        }
        
        return success;
    }
    
    public void undock( int pane_index )
    {
        if( ( pane_index < 0 ) || ( pane_index >= panes.size() ) )
        {
            return;
        }
        
        GIPane pane = (GIPane) docked_panes.elementAt( pane_index );
        if( ! ( pane.getParent() instanceof JSplitPane ) )
        {
            return;
        }
        
        JSplitPane split_pane = (JSplitPane) pane.getParent();
        Container split_pane_parent = split_pane.getParent();
        Component other_component = null;
        
        if( split_pane.getTopComponent() == pane )
        {
            other_component = split_pane.getBottomComponent();
        }
        else
        {
            other_component = split_pane.getTopComponent();
        }
        
        /* Replace the split pane which housed the pane we're undocking with
         * the 'partner component' of the pane.
         *
         * e.g. if a JSplitPane, A, contained some Component B, and the
         * pane we are undocking, C, then we want to replace A with B.
         */
        
        if( split_pane_parent instanceof JSplitPane )
        {
            JSplitPane parental_split_pane = (JSplitPane) split_pane_parent;
            if( parental_split_pane.getTopComponent() == split_pane )
            {
                parental_split_pane.setTopComponent( other_component );
            }
            else
            {
                parental_split_pane.setBottomComponent( other_component );
            }
        }
        else
        {
            split_pane_parent.remove( split_pane );
            split_pane_parent.add( other_component );
        }
        
        GIWindow window = new GIWindow( this, settings_manager, pane.getTitle() );
        undocked_panes.add( pane );
        docked_panes.remove( pane );
        window.addPane( pane );
        addNewWindow( window );
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
        GIWindow window = (GIWindow) e.getSource();
        GIPane pane = window.getPane();
        
        if( ! docked_panes.contains( pane ) )
        {
            panes.remove( pane );
            undocked_panes.remove( pane );
            active_info_panes.remove( pane );
            inactive_info_panes.remove( pane );
        }
        
        windows.remove( window );
        
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
        //jif.addComponentListener( this );
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
        
        int n = panes.size();
        String i_str;
        GIPane pane;
        String setting_path;
        
        for( int i = 0; i < n; i++ )
        {
            pane = (GIPane) panes.elementAt( i );
            i_str = Integer.toString( i );
            
            setting_path = "/gui/desktop/panes/" + i_str;
            
            settings_manager.putString(
                setting_path + "/type",
                pane.getClass().toString()
            );
            settings_manager.putString(
                setting_path + "/title",
                pane.getTitle()
            );
            
            if( pane instanceof GITextPane )
            {
                settings_manager.putString(
                    setting_path + "/filter",
                    ((GITextPane) pane).getFilter()
                );
            }
            else if( pane instanceof GIInfoPane )
            {
                settings_manager.putString(
                    setting_path + "/path",
                    ((GIInfoPane) pane).getPath()
                );
            }
        }
        
        n = windows.size();
        GIWindow giw;
        for( int i = 0; i < n; i++ )
        {
            giw = (GIWindow) windows.elementAt( i );
            i_str = Integer.toString( i );
            
            setting_path = "/gui/desktop/windows/" + i_str;
            
            settings_manager.putString(
                setting_path + "/title",
                giw.getTitle()
            );
            settings_manager.putInt(
                setting_path + "/x",
                giw.getX()
            );
            settings_manager.putInt(
                setting_path + "/y",
                giw.getY()
            );
            settings_manager.putInt(
                setting_path + "/height",
                giw.getHeight()
            );
            settings_manager.putInt(
                setting_path + "/width",
                giw.getWidth()
            );
            
            int state = GI_NORMAL;
            if( giw.isMaximum() )
            {
                state = GI_MAXIMIZED;
            }
            else if( giw.isIcon() )
            {
                state = GI_MINIMIZED;
            }
            settings_manager.putInt(
                setting_path + "/state",
                state
            );
            
            settings_manager.putBoolean(
                setting_path + "/selected",
                giw.isSelected()
            );
            
            settings_manager.putInt(
                setting_path + "/pane",
                panes.indexOf( giw.getPane() )
            );
            
        }
        
        int location;
        JSplitPane split_pane;
        int orientation;
        n = docked_panes.size();
        for( int i = 0; i < n; i++ )
        {
            pane = (GIPane) docked_panes.elementAt( i );
            setting_path = "/gui/desktop/docked panes/" + Integer.toString( i );

            settings_manager.putInt(
                setting_path + "/pane", 
                panes.indexOf( pane )
            );
            
            split_pane = (JSplitPane) pane.getParent();
            orientation = split_pane.getOrientation();
            location = DOCK_NOWHERE;
            if( orientation == JSplitPane.VERTICAL_SPLIT )
            {
                if( split_pane.getTopComponent() == pane )
                {
                    location = DOCK_TOP;
                }
                else
                {
                    location = DOCK_BOTTOM;
                }
            }
            else if( orientation == JSplitPane.HORIZONTAL_SPLIT )
            {
                if( split_pane.getTopComponent() == pane )
                {
                    location = DOCK_LEFT;
                }
                else
                {
                    location = DOCK_RIGHT;
                }
            }
            
            settings_manager.putInt(
                setting_path + "/location",
                location
            );
            settings_manager.putInt(
                setting_path + "/divider location",
                split_pane.getDividerLocation()
            );
        }
    }

    protected void restoreDesktopState()
    {
        int i;
        String i_str;
        
        String type;
        String title;
        String setting_path;
        int pane_type;
        
        Hashtable filters = new Hashtable();
        Hashtable paths = new Hashtable();
        Vector pane_types = new Vector();
        Vector pane_titles = new Vector();
        
        // Panes
        
        i = 0;
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            setting_path = "/gui/desktop/panes/" + i_str;
            
            type = settings_manager.getString( setting_path + "/type", "" );
            
            if( type.equals( "" ) )
            {
                break;
            }
            
            pane_titles.add( settings_manager.getString( setting_path + "/title", "" ) );
            
            if( type.equals( "class geoirc.GIInfoPane" ) )
            {
                paths.put(
                    i_str,
                    settings_manager.getString(
                        setting_path + "/path",
                        ""
                    )
                );
                pane_types.add( new Integer( INFO_PANE ) );
            }
            else if( type.equals( "class geoirc.GITextPane" ) )
            {
                filters.put(
                    i_str,
                    settings_manager.getString(
                        setting_path + "/filter",
                        ""
                    )
                );
                pane_types.add( new Integer( TEXT_PANE ) );
            }
            
            i++;
        }

        // Windows
        
        int pane_index;
        int x;
        int y;
        int width;
        int height;
        int state;
        boolean is_selected;

        i = 0;
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            setting_path = "/gui/desktop/windows/" + i_str;
            
            title = settings_manager.getString(
                setting_path + "/title",
                ""
            );
            
            if( title.equals( "" ) )
            {
                // No more windows stored in the settings.
                break;
            }
            
            x = settings_manager.getInt(
                setting_path + "/x",
                MIN_NEW_WINDOW_X
            );
            y = settings_manager.getInt(
                setting_path + "/y",
                MIN_NEW_WINDOW_Y
            );
            height = settings_manager.getInt(
                setting_path + "/height",
                DEFAULT_WINDOW_HEIGHT
            );
            width = settings_manager.getInt(
                setting_path + "/width",
                DEFAULT_WINDOW_WIDTH
            );
            state = settings_manager.getInt(
                setting_path + "/state",
                GI_NORMAL
            );
            is_selected = settings_manager.getBoolean(
                setting_path + "/selected",
                false
            );
            pane_index = settings_manager.getInt(
                setting_path + "/pane",
                -1
            );
            
            pane_type = ( (Integer) pane_types.elementAt( pane_index ) ).intValue();
            
            GIWindow giw = null;
            if( pane_type == TEXT_PANE )
            {
                giw = addTextWindow(
                    title,
                    (String) filters.get( Integer.toString( pane_index ) )
                );
            }
            else if( pane_type == INFO_PANE )
            {
                giw = addInfoWindow(
                    title,
                    (String) paths.get( Integer.toString( pane_index ) )
                );
            }
            else
            {
                // Huh?  Unknown pane type.
                printlnDebug( "Unknown pane type in settings." );
            }
            
            if( giw != null )
            {
                giw.setBounds( x, y, width, height );
                try
                {
                    switch( state )
                    {
                        case GI_MAXIMIZED:
                            giw.setMaximum( true );
                            break;
                        case GI_MINIMIZED:
                            giw.setIcon( true );
                            break;
                        case GI_NORMAL:
                        default:
                            giw.setMaximum( false );
                            break;
                    }
                    giw.setSelected( is_selected );
                }
                catch( java.beans.PropertyVetoException e )
                {
                    // Do nothing about this error.
                }
            }
            
            i++;
        }
        
        // Docked panes

        int location;
        int divider_location;
        
        i = 0;
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            setting_path = "/gui/desktop/docked panes/" + Integer.toString( i );
            
            pane_index = settings_manager.getInt(
                setting_path + "/pane",
                -1
            );
            
            if( pane_index == -1 )
            {
                // No more panes stored in the settings.
                break;
            }
            
            location = settings_manager.getInt(
                setting_path + "/location",
                DOCK_NOWHERE
            );
            divider_location = settings_manager.getInt(
                setting_path + "/divider location",
                DEFAULT_DIVIDER_LOCATION
            );

            pane_type = ( (Integer) pane_types.elementAt( pane_index ) ).intValue();
            
            GIPane gip = null;
            if( pane_type == TEXT_PANE )
            {
                gip = addTextPane(
                    (String) pane_titles.elementAt( pane_index ),
                    (String) filters.get( Integer.toString( pane_index ) )
                );
            }
            else if( pane_type == INFO_PANE )
            {
                gip = addInfoPane(
                    (String) pane_titles.elementAt( pane_index ),
                    (String) paths.get( Integer.toString( pane_index ) )
                );
                inactive_info_panes.add( gip );
            }
            else
            {
                // Huh?  Unknown pane type.
                printlnDebug( "Unknown pane type in settings." );
            }
            
            // Dock the pane.
            
            if( gip != null )
            {
                dock( location, gip );
                ( (JSplitPane) gip.getParent() ).setDividerLocation( divider_location );
            }
            
            i++;
        }        
    }    
    
    public void listWindows()
    {
        GIWindow giw;
        for( int i = 0, n = windows.size(); i < n; i++ )
        {
            giw = (GIWindow) windows.elementAt( i );
            printlnDebug( Integer.toString( i ) + ": " + giw.getTitle() );
        }
    }
    
    public void listDockedPanes()
    {
        GIPane pane;
        for( int i = 0, n = docked_panes.size(); i < n; i++ )
        {
            pane = (GIPane) docked_panes.elementAt( i );
            printlnDebug(
                Integer.toString( i ) + ": "
                + pane.getTitle()
            );
        }
    }
}
