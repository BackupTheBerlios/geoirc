/*
 * DisplayManager.java
 *
 * Created on June 24, 2003, 3:38 PM
 */

package geoirc;

import geoirc.conf.SettingsDialog;
import geoirc.util.BadExpressionException;
import geoirc.util.BoolExpEvaluator;
import geoirc.util.JMenuHelper;
import geoirc.util.Util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;

import org.jscroll.JScrollDesktopPane;
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
    protected VariableManager variable_manager;
    protected LogManager log_manager;
    protected I18nManager i18n_manager;
    protected GeoIRC geoirc;
    protected boolean listening;

    protected JTextField input_field;
    
    protected JScrollDesktopPane desktop_pane;
    
    protected Vector windows;
    protected JInternalFrame last_activated_frame;
    protected int last_added_frame_x;
    protected int last_added_frame_y;
    protected GITextPane last_activated_text_pane;
    
    protected Vector inactive_info_panes;
    protected Vector active_info_panes;
    protected Vector docked_panes;
    protected Vector undocked_panes;
    protected Vector panes;
    
    protected boolean show_qualities;  // for debugging purposes
    
    protected DefaultTreeCellRenderer cell_renderer;
    
       
    // No default constructor
    private DisplayManager() { }
    
    public DisplayManager(
        GeoIRC parent,
        JMenuBar menu_bar,
        SettingsManager settings_manager,
        VariableManager variable_manager,
        I18nManager i18n_manager,
        JTextField input_field
    )
    {
        geoirc = parent;
        listening = false;
        
        this.settings_manager = settings_manager;
        this.variable_manager = variable_manager;
        this.i18n_manager = i18n_manager;
        log_manager = null;

        windows = new Vector();
        inactive_info_panes = new Vector();
        active_info_panes = new Vector();
        docked_panes = new Vector();
        undocked_panes = new Vector();
        panes = new Vector();
        
        desktop_pane = new JScrollDesktopPane( settings_manager, menu_bar );
		JMenu settings_menu = JMenuHelper.addMenuBarItem(menu_bar, "_Settings");
		ActionListener actionProcCmd = new OpenSettingsDialogListener(settings_manager, this);
		JMenuHelper.addMenuItem(settings_menu, "_Options", actionProcCmd);
        
        geoirc.getContentPane().add( desktop_pane );
        this.input_field = input_field;
        
        try
        {
            cell_renderer = new GITreeCellRenderer();
        }
        catch( FileNotFoundException e )
        {
            printlnDebug( i18n_manager.getString( "icons missing" ) );
            cell_renderer = new DefaultTreeCellRenderer();
        }
        
        last_activated_frame = null;
        last_added_frame_x = 0;
        last_added_frame_y = 0;
        last_activated_text_pane = null;
        
        show_qualities = false;
    }
    
    public void beginListening()
    {
        listening = true;
    }
    
    public void applySettings()
    {
        GIPane gip = null;
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            gip = (GIPane) panes.elementAt( i );
            if( gip instanceof GITextPane )
            {
                ((GITextPane) gip).applySettings();
            }
        }
    }
    
    public StyleManager getStyleManager()
    {
        return geoirc.getStyleManager();
    }
    
    public GeoIRC getGeoIRCInstance()
    {
        return this.geoirc;
    }
    
    public void setShowQualities( boolean setting )
    {
        show_qualities = setting;
    }
    
    public void setLogManager( LogManager log_manager )
    {
        this.log_manager = log_manager;
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
            geoirc, this, settings_manager, i18n_manager, title, filter
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
            s.toString() + " and " + channel_name
        );
    }
    
    public void activateInfoPanes( String path, TreeModel model )
    {
        GIInfoPane giip;
        for( int i = 0; i < inactive_info_panes.size(); i++ )
        {
            giip = (GIInfoPane) inactive_info_panes.elementAt( i );
            if( giip.getPath().equals( path ) )
            {
                giip.activate( model );
                inactive_info_panes.remove( i );
                i--;
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
    
    /**
     * Closes all windows whose filter matches the given filter.
     */
    public void closeWindows( String filter )
    {
        if( windows != null )
        {
            Vector wins = (Vector) windows.clone();
            GIWindow giw;
            for( int i = 0, n = wins.size(); i < n; i++ )
            {
                giw = (GIWindow) wins.elementAt( i );
                if( giw.getPaneType() == TEXT_PANE )
                {
                    GITextPane gip = (GITextPane) giw.getPane();
                    if( gip.getFilter().equals( filter ) )
                    {
                        try
                        {
                            giw.setClosed( true );
                        }
                        catch( java.beans.PropertyVetoException e )
                        {
                            Util.printException(
                                this, e,
                                i18n_manager.getString( "window closure failure 1", new Object [] { giw.getTitle() } )
                            );
                        }
                    }
                }
            }
        }
    }
    
    public GIWindow getWindowByIndex( int index )
    {
        GIWindow giw;
        try
        {
            giw = (GIWindow) windows.elementAt( index );
        }
        catch( ArrayIndexOutOfBoundsException e )
        {
            giw = getSelectedFrame();
        }
        return giw;
    }
    
    public GITextPane getTextPaneByTitle( String title )
    {
        GIPane gip;
        GITextPane retval = null;
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            gip = (GIPane) panes.elementAt( i );
            if( gip instanceof GITextPane )
            {
                GITextPane gitp = (GITextPane) gip;
                if( gitp.getTitle().equals( title ) )
                {
                    retval = gitp;
                    break;
                }
            }
        }
        
        return retval;
    }
    
    public void closeWindow( int index )
    {
        GIWindow giw = getWindowByIndex( index );
        if( giw != null )
        {
            try
            {
                giw.setClosed( true );
            }
            catch( PropertyVetoException e )
            {
                Util.printException(
                    this,
                    e,
                    i18n_manager.getString(
                        "window closure failure 2",
                        new Object [] { new Integer( index ) }
                    )
                );
            }
        }
    }
    
    public void maximizeWindow( int index )
    {
        GIWindow giw = getWindowByIndex( index );
        if( giw != null )
        {
            try
            {
                giw.setMaximum( true );
            }
            catch( PropertyVetoException e )
            {
                Util.printException(
                    this,
                    e,
                    i18n_manager.getString(
                        "maximize failure",
                        new Object [] { new Integer( index ) }
                    )
                );
            }
        }
    }
    
    public void minimizeWindow( int index )
    {
        GIWindow giw = getWindowByIndex( index );
        if( giw != null )
        {
            try
            {
                giw.setIcon( true );
            }
            catch( PropertyVetoException e )
            {
                Util.printException(
                    this,
                    e,
                    i18n_manager.getString(
                        "minimize failure",
                        new Object [] { new Integer( index ) }
                    )
                );
            }
        }
    }
    
    public void restoreWindow( int index )
    {
        GIWindow giw = getWindowByIndex( index );
        if( giw != null )
        {
            try
            {
                if( giw.isMaximum() )
                {
                    giw.setMaximum( false );
                }
                else if( giw.isIcon() )
                {
                    giw.setIcon( false );
                }
            }
            catch( PropertyVetoException e )
            {
                Util.printException(
                    this,
                    e,
                    i18n_manager.getString(
                        "restoration failure",
                        new Object [] { new Integer( index ) }
                    )
                );
            }
        }
    }
    
    public void sizeWindow( int index, int width_, int height_ )
    {
        GIWindow giw = getWindowByIndex( index );
        if( giw != null )
        {
            int width = Util.fitInt( width_, WINDOW_MINIMUM_WIDTH, WINDOW_MAXIMUM_WIDTH );
            int height = Util.fitInt( height_, WINDOW_MINIMUM_HEIGHT, WINDOW_MAXIMUM_HEIGHT );
            try
            {
                giw.setMaximum( false );
                giw.setIcon( false );
                giw.setSize( width, height );
            }
            catch( PropertyVetoException e )
            {
                Util.printException(
                    this,
                    e,
                    i18n_manager.getString(
                        "size failure",
                        new Object [] { new Integer( index ) }
                    )
                );
            }
        }
    }
    
    public void positionWindow( int index, int x_, int y_ )
    {
        GIWindow giw = getWindowByIndex( index );
        if( giw != null )
        {
            int x = Util.fitInt( x_, -1000, 5000 );
            int y = Util.fitInt( y_, -1000, 5000 );
            try
            {
                giw.setMaximum( false );
                giw.setIcon( false );
                giw.setLocation( x, y );
            }
            catch( PropertyVetoException e )
            {
                Util.printException(
                    this,
                    e,
                    i18n_manager.getString(
                        "position failure",
                        new Object [] { new Integer( index ) }
                    )
                );
            }
        }
    }
    
    public boolean clearTextWindow( int index )
    {
        boolean success = false;
        GIWindow giw = getWindowByIndex( index );
        
        if( giw != null )
        {
            if( giw.getPaneType() == TEXT_PANE )
            {
                GITextPane gitp = (GITextPane) giw.getPane();
                gitp.clearDocument();
                success = true;
            }
        }
        
        return success;
    }
    
    public DefaultTreeCellRenderer getCellRenderer()
    {
        return cell_renderer;
    }
    
    protected void appendAndHighlightLine(
        GITextPane text_pane,
        String line,
        String qualities
    )
    {
        int offset = text_pane.getDocumentLength();
        int len = text_pane.appendLine( line ).length();
        geoirc.getHighlightManager().highlight(
            text_pane, 
            offset, 
            len,
            qualities
        );
    }
    
    public int printlnDebug( String line )
    {
        return println( line, FILTER_SPECIAL_CHAR + "debug" );
    }
    
    public void printlnToActiveTextPane( String line )
    {
        if( ( line != null ) && ( last_activated_text_pane != null ) )
        {
            appendAndHighlightLine(
                last_activated_text_pane, line, ""
            );
        }
    }
    
    /**
     * @return the number of windows that the line was printed in
     */
    public int println( String line, String qualities )
    {
        if( line == null )
        {
            return 0;
        }
        
        boolean result = false;
        String prefilter = settings_manager.getString(
            "/misc/global pre-filter",
            ""
        );
        try
        {
            if( ! BoolExpEvaluator.evaluate( prefilter, qualities ) )
            {
                return 1;
            }
        }
        catch( BadExpressionException e )
        {
            printlnDebug(
                i18n_manager.getString(
                    "filter error",
                    new Object [] { prefilter }
                )
            );
            printlnDebug( e.getMessage() );
        }

        
        if( show_qualities )
        {
            line = "[" + qualities + "] " + line;
        }
        
        int windows_printed_to = 0;
        
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
                    appendAndHighlightLine( text_pane, line, qualities );
                    windows_printed_to++;
                }
            }
        }
        
        if( ! geoirc.isActive() )
        {
            int lines_unread = variable_manager.incrementInt( "lines_unread" );
            geoirc.setTitle(
                "(" + Integer.toString( lines_unread ) + ") "
                + BASE_GEOIRC_TITLE
            );
        }
        
        if( log_manager != null )
        {
            log_manager.log( line, qualities );
        }
        
        return windows_printed_to;
    }
    
    public GIWindow getSelectedFrame()
    {
        return (GIWindow) desktop_pane.getSelectedFrame();
    }

    public String getSelectedChannel()
    {
        return getSelectedByPrefix( "#" );
    }
    
    public String getSelectedProcess()
    {
        return getSelectedByPrefix( "process=" );
    }
    
    public String getSelectedDCCConnection()
    {
        return getSelectedByPrefix( "dcc=" );
    }
    
    public String getSelectedByPrefix( String prefix )
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
                    int prefix_index = filter.indexOf( prefix );
                    if( prefix_index > -1 )
                    {
                        int space_index = filter.indexOf( " ", prefix_index );
                        if( space_index > -1 )
                        {
                            retval = filter.substring( prefix_index, space_index );
                        }
                        else
                        {
                            retval = filter.substring( prefix_index );
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
        return dock( location, window, DESKTOP_PANE );
    }
    
    public boolean dock( int location, int window, int host_window )
    {
        boolean success = false;
        
        if(
            ( windows != null )
            && ( windows.size() > 0 )
            && ( window >= 0 )
            && ( window < windows.size() )
            && ( host_window < windows.size() )
        )
        {
            GIWindow giw = (GIWindow) windows.elementAt( window );
            GIPane pane = giw.getPane();
            JComponent partner_pane_window = desktop_pane;
            if( host_window >= 0 )
            {
                partner_pane_window = ((GIWindow) windows.elementAt( host_window )).getPane();
            }
            
            if( dock( location, pane, partner_pane_window ) )
            {
                success = true;
                giw.dispose();
            }
        }

        return success;
    }
    
    protected boolean dock( int location, GIPane pane, JComponent partner )
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
            
            // partner_container: The Container holding the partner
            Container partner_container = partner.getParent();
            
            // was_primary: In which of the two panes in the SplitPane is the partner?
            boolean was_primary = true;
            if( partner_container instanceof JSplitPane )
            {
                JSplitPane sp = (JSplitPane) partner_container;
                was_primary = ( sp.getTopComponent() == partner );
            }
            
            partner_container.remove( partner );
            
            switch( location )
            {
                case DOCK_TOP:
                    {
                        split_pane = new JSplitPane(
                            JSplitPane.VERTICAL_SPLIT,
                            pane,
                            partner
                        );
                        split_pane.setDividerLocation( DEFAULT_DOCK_WEIGHT );
                    }
                    break;
                case DOCK_RIGHT:
                    {
                        split_pane = new JSplitPane(
                            JSplitPane.HORIZONTAL_SPLIT,
                            partner, 
                            pane
                        );
                        split_pane.setDividerLocation( 1.0 - DEFAULT_DOCK_WEIGHT );
                    }
                    break;
                case DOCK_BOTTOM:
                    {
                        split_pane = new JSplitPane(
                            JSplitPane.VERTICAL_SPLIT,
                            partner, 
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
                            partner
                        );
                        split_pane.setDividerLocation( DEFAULT_DOCK_WEIGHT );
                    }
                    break;
                default:
                    break;
            }

            if( partner_container instanceof JSplitPane )
            {
                JSplitPane sp = (JSplitPane) partner_container;
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
                partner_container.add( split_pane );
            }

            docked_panes.add( pane );
            undocked_panes.remove( pane );
            
            success = true;
        }
        
        return success;
    }
    
    public void undock( int pane_index )
    {
        if( ( pane_index < 0 ) || ( pane_index >= docked_panes.size() ) )
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
    
    public void highlightButton( GIPane pane )
    {
        GIWindow window;
        for( int i = 0, n = windows.size(); i < n; i++ )
        {
            window = (GIWindow) windows.elementAt( i );
            if( window.getPane() == pane )
            {
                JToggleButton button = window.getAssociatedButton();
                if( button != null )
                {
                    int [] rgb = Util.getRGB(
                        settings_manager.getString(
                            "/gui/new content button colour",
                            "ff0000"
                        )
                    );
                    Color colour = new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
                    button.setForeground( colour );
                }
                break;
            }
        }
    }
    
    /* ************************************************************
     * Listener Implementations
     */
    
    public void internalFrameActivated( InternalFrameEvent e )
    {
        last_activated_frame = e.getInternalFrame();
        GIWindow giw = (GIWindow) e.getSource();
        JToggleButton button = giw.getAssociatedButton();
        if( button != null )
        {
            button.setForeground( DEFAULT_WINDOW_BUTTON_FOREGROUND_COLOUR );
        }
        
        GIPane pane = giw.getPane();
        if( pane instanceof GITextPane )
        {
            last_activated_text_pane = (GITextPane) pane;
            
            String filter = ((GITextPane) pane).getFilter();
            if( filter != null )
            {
                // Search for a server name in this filter.
                Matcher m = Pattern.compile( "\\S+\\.\\S+\\.\\S+" ).matcher( filter );
                if( m.find() )
                {
                    String server_name = m.group();
                    geoirc.setCurrentRemoteMachine( server_name );
                }
                else
                {
                    m = Pattern.compile( "\\d+\\.\\d+\\.\\d+\\.\\d+" ).matcher( filter );
                    if( m.find() )
                    {
                        String ip_address = m.group();
                        geoirc.setCurrentRemoteMachine( ip_address );
                    }
                }
            }
        }
    }
    
    public void internalFrameClosed( InternalFrameEvent e )
    {
        GIWindow window = (GIWindow) e.getSource();
        GIPane pane = window.getPane();
        
        if( last_activated_frame == window )
        {
            last_activated_frame = null;
        }
        if( last_activated_text_pane == pane )
        {
            last_activated_text_pane = null;
        }
        
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
        
        String mods = KeyEvent.getModifiersExText( e.getModifiersEx() );
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
        Component partner = null;
        GIPane pane2;
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
                    partner = split_pane.getBottomComponent();
                }
                else
                {
                    location = DOCK_BOTTOM;
                    partner = split_pane.getTopComponent();
                }
            }
            else if( orientation == JSplitPane.HORIZONTAL_SPLIT )
            {
                if( split_pane.getTopComponent() == pane )
                {
                    location = DOCK_LEFT;
                    partner = split_pane.getBottomComponent();
                }
                else
                {
                    location = DOCK_RIGHT;
                    partner = split_pane.getTopComponent();
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
            
            settings_manager.putInt(
                setting_path + "/partner",
                panes.indexOf( partner )
            );
        }
    }

    public void restoreDesktopState()
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
            
            if( pane_index > -1 )
            {
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
                    printlnDebug( i18n_manager.getString( "unknown pane" ) );
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
                printlnDebug( i18n_manager.getString( "unknown pane" ) );
            }
            
            // Dock the pane.
            
            if( gip != null )
            {
                int partner_index = settings_manager.getInt(
                    setting_path + "/partner",
                    DESKTOP_PANE
                );
                JComponent partner = desktop_pane;
                if( partner_index >= 0 )
                {
                    partner = (JComponent) panes.elementAt( partner_index );
                }
                dock( location, gip, partner );
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
    
    public void openSettingsDialog()
    {
        SettingsDialog dlg = new SettingsDialog(settings_manager, this);
        dlg.setVisible( true );
    }

	class OpenSettingsDialogListener implements java.awt.event.ActionListener
	{
		XmlProcessable settings_manager;
		DisplayManager display_manager;
		
		OpenSettingsDialogListener(XmlProcessable settings_manager, DisplayManager display_manager)
		{
			this.settings_manager = settings_manager;
			this.display_manager = display_manager;
		}
		public void actionPerformed(ActionEvent e)
		{
            display_manager.openSettingsDialog();
		}
	}   
}
