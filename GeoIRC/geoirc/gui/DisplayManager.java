/*
 * DisplayManager.java
 *
 * Created on June 24, 2003, 3:38 PM
 */

package geoirc.gui;

import geoirc.GeoIRC;
import geoirc.I18nManager;
import geoirc.LogManager;
import geoirc.Server;
import geoirc.SettingsManager;
import geoirc.VariableManager;
import geoirc.XmlProcessable;

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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
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
        geoirc.GeoIRCConstants,
        ComponentListener,
        KeyListener,
        WindowListener
{
    protected SettingsManager settings_manager;
    protected VariableManager variable_manager;
    protected LogManager log_manager;
    protected I18nManager i18n_manager;
    protected GeoIRC geo_irc;
    protected boolean restoring;

    protected JTextField input_field;
    
    protected JScrollDesktopPane desktop_pane;
    
    protected GIFrameWrapper last_activated_frame;
    protected int last_added_frame_x;
    protected int last_added_frame_y;
    protected GITextPane last_activated_text_pane;
    
    protected Vector inactive_info_panes;
    protected Vector active_info_panes;
    protected PaneVector panes;
    protected Vector frames;
    
    protected GIPaneWrapper last_activated_pane;
    protected GIFrameWrapper geoirc_gifw;
    
    protected boolean show_qualities;  // for debugging purposes
    
    protected DefaultTreeCellRenderer cell_renderer;
    
    protected JMenuBar menu_bar;
    
    // No default constructor
    private DisplayManager() { }
    
    public DisplayManager(
        GeoIRC parent,
        JMenuBar menu_bar,
        org.jscroll.components.ResizableToolBar pane_bar,
        SettingsManager settings_manager,
        VariableManager variable_manager,
        I18nManager i18n_manager,
        JTextField input_field
    )
    {
        geo_irc = parent;
        restoring = true;
        
        this.settings_manager = settings_manager;
        this.variable_manager = variable_manager;
        this.i18n_manager = i18n_manager;
        this.menu_bar = menu_bar;
        log_manager = null;

        inactive_info_panes = new Vector();
        active_info_panes = new Vector();
        panes = new PaneVector( this, pane_bar );
        frames = new Vector();

        GIPaneWrapper gipw = new GIPaneWrapper(
            settings_manager,
            this,
            geo_irc.getContentPane(),
            "GeoIRC Content Pane",
            GEOIRC_CONTENT_PANE
        );
        geoirc_gifw = new GIFrameWrapper( geo_irc );
        gipw.setFrame( geoirc_gifw );
        panes.add( gipw );
        
        JMenu settings_menu = JMenuHelper.addMenuBarItem(menu_bar, "_Settings");
        ActionListener actionProcCmd = new OpenSettingsDialogListener(settings_manager, this);
        JMenuHelper.addMenuItem(settings_menu, "_Options", actionProcCmd);
        
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
        last_activated_pane = null;
        
        show_qualities = false;
    }
    
    public void applySettings()
    {
        GIPaneWrapper gipw = null;
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            gipw = (GIPaneWrapper) panes.elementAt( i );
            gipw.applySettings();
        }
    }
    
    public StyleManager getStyleManager()
    {
        return geo_irc.getStyleManager();
    }
    
    public GeoIRC getGeoIRCInstance()
    {
        return this.geo_irc;
    }
    
    public void setShowQualities( boolean setting )
    {
        show_qualities = setting;
    }
    
    public void setLogManager( LogManager log_manager )
    {
        this.log_manager = log_manager;
    }

    public DefaultTreeCellRenderer getCellRenderer()
    {
        return cell_renderer;
    }
    
    /*********************************************************************
     *
     * Window and Pane Functions
     *
     */
    
    protected void addNewWindow( GIWindow window )
    {
        last_added_frame_x += NEW_WINDOW_X_INCREMENT;
        if( last_added_frame_x + DEFAULT_WINDOW_WIDTH > desktop_pane.getWidth() - 30 )
        {
            last_added_frame_x = MIN_NEW_WINDOW_X;
        }
        last_added_frame_y += NEW_WINDOW_Y_INCREMENT;
        if( last_added_frame_y + DEFAULT_WINDOW_HEIGHT > desktop_pane.getHeight() - 30 )
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
        
        if( ! restoring )
        {
            recordDesktopState();
        }
    }

    protected GIFrameWrapper addTextWindow( String title )
    {
        return addTextWindow( title, null );
    }
    
    protected GIPaneWrapper addTextPane( String title, String filter )
    {
        GITextPane gitp = new GITextPane(
            geo_irc, this, settings_manager, i18n_manager, title, filter
        );
        GIPaneWrapper gipw = new GIPaneWrapper( settings_manager, this, gitp, title, TEXT_PANE );
        gitp.setPaneWrapper( gipw );
        panes.add( gipw );
        last_activated_pane = gipw;
        paneActivated( gipw );
        last_activated_text_pane = (GITextPane) gipw.getPane();
        return gipw;
    }

    public GIFrameWrapper addTextWindow( String title, String filter )
    {
        return addTextWindow( title, filter, INTERNAL_WINDOW );
    }
    
    public GIFrameWrapper addTextWindow( String title, String filter, int window_type )
    {
        String actual_title = title;
        if( actual_title == null )
        {
            actual_title = "";
        }
        GIPaneWrapper gipw = addTextPane( actual_title, filter );
        GIFrameWrapper gifw = null;
        switch( window_type )
        {
            case INTERNAL_WINDOW:
            {
                GIWindow text_window = new GIWindow(
                    this, settings_manager, actual_title, panes, frames
                );
                text_window.addPane( gipw.getPane() );
                gipw.setParent( text_window.getPaneWrapper() );
                addNewWindow( text_window );
                gifw = new GIFrameWrapper( text_window );
                text_window.setFrameWrapper( gifw );
                break;
            }
            case EXTERNAL_WINDOW:
            {
                GIExternalWindow text_window = new GIExternalWindow(
                    this, settings_manager, actual_title, panes, frames
                );
                text_window.addPane( gipw.getPane() );
                gipw.setParent( text_window.getPaneWrapper() );
                gifw = new GIFrameWrapper( text_window );
                text_window.setFrameWrapper( gifw );
                break;
            }
        }
        
        return gifw;
    }
    
    protected GIPaneWrapper addInfoPane( String title, String path )
    {
        GIInfoPane giip = new GIInfoPane(
            this, settings_manager, title, path
        );
        GIPaneWrapper gipw = new GIPaneWrapper( settings_manager, this, giip, title, INFO_PANE );
        giip.setPaneWrapper( gipw );
        panes.add( gipw );
        last_activated_pane = gipw;
        paneActivated( gipw );
        return gipw;
    }
    
    public GIFrameWrapper addInfoWindow( String title, String path )
    {
        return addInfoWindow( title, path, INTERNAL_WINDOW );
    }
    
    public GIFrameWrapper addInfoWindow( String title, String path, int window_type )
    {
        String actual_title = title;
        if( actual_title == null )
        {
            actual_title = "";
        }
        GIPaneWrapper gipw = addInfoPane( actual_title, path );
        GIFrameWrapper gifw = null;
        switch( window_type )
        {
            case INTERNAL_WINDOW:
            {
                GIWindow info_window = new GIWindow(
                    this, settings_manager, actual_title, panes, frames
                );
                info_window.addPane( gipw.getPane() );
                gipw.setParent( info_window.getPaneWrapper() );
                addNewWindow( info_window );
                gifw = new GIFrameWrapper( info_window );
                info_window.setFrameWrapper( gifw );
                break;
            }
            case EXTERNAL_WINDOW:
            {
                GIExternalWindow info_window = new GIExternalWindow(
                    this, settings_manager, actual_title, panes, frames
                );
                info_window.addPane( gipw.getPane() );
                gipw.setParent( info_window.getPaneWrapper() );
                gifw = new GIFrameWrapper( info_window );
                info_window.setFrameWrapper( gifw );
                break;
            }
        }
        inactive_info_panes.add( gipw.getPane() );
        
        return gifw;
    }
    
    public GIFrameWrapper addChannelWindow( Server s, String channel_name )
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
    
    public boolean dockByUserIndex( int location, int pane_user_index, int partner_user_index )
    {
        return dock(
            location,
            userIndexToTrueIndex( pane_user_index ),
            (
                partner_user_index == NO_PARTNER_USER_INDEX_SPECIFIED
                ? DESKTOP_PANE_INDEX
                : userIndexToTrueIndex( partner_user_index )
            )
        );
    }
    
    /**
     * Replace partner with a JSplitPane, and place pane and partner in the split pane.
     *
     * @return true on success, false otherwise
     */
    public boolean dock( int location, int pane_index, int partner_index )
    {
        boolean success = false;
        
        if(
            ( location == DOCK_TOP )
            || ( location == DOCK_BOTTOM )
            || ( location == DOCK_LEFT )
            || ( location == DOCK_RIGHT )
        )
        {
            if(
                pane_index != partner_index
                && isUserPane( pane_index, EXCLUDE_SPLIT_PANES )
                && (
                    isUserPane( partner_index, EXCLUDE_SPLIT_PANES )
                    || partner_index == DESKTOP_PANE_INDEX
                )
            )
            {
            
                GIPaneWrapper gipw = (GIPaneWrapper) panes.elementAt( pane_index );
                GIPaneWrapper partner_gipw = (GIPaneWrapper) panes.elementAt( partner_index );
                Container pane = gipw.getPane();
                Container partner = partner_gipw.getPane();
                
                if( gipw.getParent().getType() == SPLIT_PANE )
                {
                    // The pane we're docking is already docked somewhere;
                    // undock it before docking it into the new parent pane.
                    undock( gipw );
                }

                JSplitPane split_pane = null;

                // partner_container: The Container holding the partner
                GIPaneWrapper partner_container_gipw = partner_gipw.getParent();
                GIFrameWrapper frame = null;
                Container partner_container = null;
                boolean was_primary = true;
                if( partner_container_gipw != null )
                {
                    partner_container = partner_container_gipw.getPane();
                    // If the partner was itself a member of a split pane,
                    // was_primary: In which of the two panes in the SplitPane is the partner?
                    if( partner_container instanceof JSplitPane )
                    {
                        JSplitPane sp = (JSplitPane) partner_container;
                        was_primary = ( sp.getTopComponent() == partner );
                    }
                    partner_container = partner_container_gipw.getPane();
                    partner_container.remove( partner );
                }
                else
                {
                    frame = partner_gipw.getFrame();
                }

                GIPaneWrapper pane_container_gipw = gipw.getParent();
                if( pane_container_gipw != null )
                {
                    pane_container_gipw.getPane().remove( pane );
                    switch( pane_container_gipw.getType() )
                    {
                        case CHILD_CONTENT_PANE:
                        case EXTERNAL_CONTENT_PANE:
                        {
                            // We just removed the only content of a window.
                            // So, we should delete the window, too!
                            GIFrameWrapper gifw = pane_container_gipw.getFrame();
                            try
                            {
                                gifw.close();
                            }
                            catch( PropertyVetoException e )
                            {
                                Util.printException(
                                    this, e,
                                    i18n_manager.getString(
                                        "window closure failure 1", new Object [] { gifw.getTitle() }
                                    )
                                );
                            }
                            break;
                        }
                    }
                }

                // Create a new split pane.

                switch( location )
                {
                    case DOCK_TOP:
                        {
                            split_pane = new JSplitPane(
                                JSplitPane.VERTICAL_SPLIT,
                                pane,
                                partner
                            );
                            split_pane.setDividerLocation( 100 );
                            gipw.setSplitRank( SPLIT_PRIMARY );
                            partner_gipw.setSplitRank( SPLIT_SECONDARY );
                        }
                        break;
                    case DOCK_RIGHT:
                        {
                            split_pane = new JSplitPane(
                                JSplitPane.HORIZONTAL_SPLIT,
                                partner, 
                                pane
                            );
                            split_pane.setDividerLocation( 100 );
                            gipw.setSplitRank( SPLIT_SECONDARY );
                            partner_gipw.setSplitRank( SPLIT_PRIMARY );
                        }
                        break;
                    case DOCK_BOTTOM:
                        {
                            split_pane = new JSplitPane(
                                JSplitPane.VERTICAL_SPLIT,
                                partner, 
                                pane
                            );
                            split_pane.setDividerLocation( 100 );
                            gipw.setSplitRank( SPLIT_SECONDARY );
                            partner_gipw.setSplitRank( SPLIT_PRIMARY );
                        }
                        break;
                    case DOCK_LEFT:
                        {
                            split_pane = new JSplitPane(
                                JSplitPane.HORIZONTAL_SPLIT,
                                pane,
                                partner
                            );
                            split_pane.setDividerLocation( 100 );
                            gipw.setSplitRank( SPLIT_PRIMARY );
                            partner_gipw.setSplitRank( SPLIT_SECONDARY );
                        }
                        break;
                    default:
                        break;
                }

                GIPaneWrapper split_gipw = new GIPaneWrapper( settings_manager, this, split_pane, "Split Pane", SPLIT_PANE );
                if( partner_container_gipw != null )
                {
                    split_gipw.setParent( partner_container_gipw );
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
                }
                else
                {
                    frame.addPane( split_pane );
                    split_gipw.setParent( partner_gipw );
                }
                panes.add( split_gipw );
                gipw.setParent( split_gipw );
                partner_gipw.setParent( split_gipw );
                
                recordDesktopState();
                
                if( partner_index == DESKTOP_PANE_INDEX )
                {
                    /*
                    geo_irc.recordMainFrameState();
                    geo_irc.pack();
                    geo_irc.restoreMainFrameState();
                     */
                    geoirc_gifw.repaint();
                }
                else
                {
                    gipw.getFrame().repaint();
                }

                success = true;
            }
        }
        
        return success;
    }
    
    public void undockByUserIndex( int pane_user_index )
    {
        undock( userIndexToTrueIndex( pane_user_index ) );
    }
    
    public boolean undock( int pane_index )
    {
        boolean success = false;
        
        if(
            ( pane_index >= 0 )
            && ( pane_index < panes.size() )
            && isUserPane( pane_index, EXCLUDE_SPLIT_PANES )
        )
        {
        
            GIPaneWrapper gipw = (GIPaneWrapper) panes.elementAt( pane_index );
            if( undock( gipw ) )
            {
                GIWindow window = new GIWindow( this, settings_manager, gipw.getTitle(), panes, frames );
                window.addPane( gipw.getPane() );
                gipw.setParent( window.getPaneWrapper() );
                addNewWindow( window );

                recordDesktopState();
                
                success = true;
            }
        }
        
        return success;
    }
    
    protected boolean undock( GIPaneWrapper gipw )
    {
        boolean success = false;
        
        GIPaneWrapper parent_gipw = gipw.getParent();
        if( parent_gipw.getType() == SPLIT_PANE )
        {
            panes.remove( parent_gipw );

            Container pane = gipw.getPane();
            JSplitPane split_pane = (JSplitPane) parent_gipw.getPane();
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
            GIPaneWrapper partner_gipw = getPaneWrapperByPane( other_component );

            gipw.setSplitRank( SPLIT_NOT_SPLIT_MEMBER );
            partner_gipw.setSplitRank( SPLIT_NOT_SPLIT_MEMBER );

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
                    partner_gipw.setSplitRank( SPLIT_PRIMARY );
                }
                else
                {
                    parental_split_pane.setBottomComponent( other_component );
                    partner_gipw.setSplitRank( SPLIT_SECONDARY );
                }
            }
            else
            {
                split_pane_parent.remove( split_pane );
                split_pane_parent.add( other_component );
                if( split_pane_parent instanceof GeoIRC )
                {
                    /*
                    geo_irc.recordMainFrameState();
                    geo_irc.pack();
                    geo_irc.restoreMainFrameState();
                     */
                    geoirc_gifw.repaint();
                }
            }
            GIPaneWrapper spp_gipw = getPaneWrapperByPane( split_pane_parent );
            partner_gipw.setParent( spp_gipw );
            
            success = true;
        }
        
        return success;
    }
    
    public GIFrameWrapper getFrameByIndex( int index )
    {
        GIFrameWrapper gifw;
        try
        {
            gifw = (GIFrameWrapper) frames.elementAt( index );
        }
        catch( ArrayIndexOutOfBoundsException e )
        {
            gifw = (GIFrameWrapper) getSelectedFrame();
        }
            
        return gifw;
    }
    
    public GIPaneWrapper getPaneByIndex( int index )
    {
        GIPaneWrapper retval = null;
        try
        {
            retval = (GIPaneWrapper) panes.elementAt( index );
        } catch( ArrayIndexOutOfBoundsException e ) { }
        
        return retval;
    }
    
    public GIPaneWrapper getPaneWrapperByPane( Component pane )
    {
        GIPaneWrapper retval = null;
        GIPaneWrapper gipw;
        
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            gipw = (GIPaneWrapper) panes.elementAt( i );
            if( gipw.getPane() == pane )
            {
                retval = gipw;
                break;
            }
        }
        
        return retval;
    }
    
    public int getPaneIndexByPaneWrapper( GIPaneWrapper gipw )
    {
        return panes.indexOf( gipw );
    }
    
    public GITextPane getTextPaneByTitle( String title )
    {
        GIPaneWrapper gipw;
        GITextPane retval = null;
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            gipw = (GIPaneWrapper) panes.elementAt( i );
            if( gipw.getType() == TEXT_PANE )
            {
                if( gipw.getTitle().equals( title ) )
                {
                    retval = (GITextPane) gipw.getPane();
                    break;
                }
            }
        }
        
        return retval;
    }
    
    /**
     * @return an array of Strings containing the titles of the user panes.
     */
    public String [] getPaneTitles()
    {
        GIPaneWrapper gipw;
        Vector titles = new Vector();
        String [] retval = new String[ 0 ];
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            gipw = (GIPaneWrapper) panes.elementAt( i );
            if( isUserPane( i, EXCLUDE_SPLIT_PANES ) )
            {
                titles.add( gipw.getTitle() );
            }
        }
        retval = (String []) titles.toArray( retval );
        return retval;
    }
    
    public void closePaneByUserIndex( int user_index )
    {
        try
        {
            closePane( userIndexToTrueIndex( user_index ) );
        } catch( PropertyVetoException e ) { }
    }
    
    protected void closePane( int index ) throws PropertyVetoException
    {
        GIPaneWrapper gipw = getPaneByIndex( index );
        
        if( gipw == null )
        {
            gipw = last_activated_pane;
        }
        
        if( gipw != null )
        {
            if( gipw.getParent().getType() == SPLIT_PANE )
            {
                // The pane we're docking is already docked somewhere;
                // undock it before docking it into the new parent pane.
                undock( index );
            }

            gipw.getFrame().close();
        }
    }
    
    /**
     * Closes all text panes whose filter matches the given filter.
     */
    public void closePanes( String filter )
    {
        if( panes != null )
        {
            Vector panes_ = (Vector) panes.clone();
            GIPaneWrapper gipw;
            for( int i = 0, n = panes_.size(); i < n; i++ )
            {
                gipw = (GIPaneWrapper) panes_.elementAt( i );
                if( gipw.getType() == TEXT_PANE )
                {
                    GITextPane gitp = (GITextPane) gipw.getPane();
                    if( gitp.getFilter().equals( filter ) )
                    {
                        try
                        {
                            closePane( i );
                        }
                        catch( PropertyVetoException e )
                        {
                            Util.printException(
                                this, e,
                                i18n_manager.getString(
                                    "window closure failure 1",
                                    new Object [] { gipw.getTitle() }
                                )
                            );
                        }
                    }
                }
            }
        }
    }
    
    public void closeWindow( int index )
    {
        GIFrameWrapper gifw = getFrameByIndex( index );
        if( gifw != null )
        {
            try
            {
                gifw.close();
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
    
    // TODO: closePane( int index )
    
    public void maximizeWindow( int index )
    {
        GIFrameWrapper gifw = getFrameByIndex( index );
        if( gifw != null )
        {
            try
            {
                gifw.maximize();
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
        GIFrameWrapper gifw = getFrameByIndex( index );
        if( gifw != null )
        {
            try
            {
                gifw.minimize();
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
        GIFrameWrapper gifw = getFrameByIndex( index );
        if( gifw != null )
        {
            try
            {
                gifw.restore();
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
        GIFrameWrapper gifw = getFrameByIndex( index );
        if( gifw != null )
        {
            int width = Util.fitInt( width_, WINDOW_MINIMUM_WIDTH, WINDOW_MAXIMUM_WIDTH );
            int height = Util.fitInt( height_, WINDOW_MINIMUM_HEIGHT, WINDOW_MAXIMUM_HEIGHT );
            try
            {
                gifw.restore();
                gifw.setSize( width, height );
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
        GIFrameWrapper gifw = getFrameByIndex( index );
        if( gifw != null )
        {
            int x = Util.fitInt( x_, -1000, 5000 );
            int y = Util.fitInt( y_, -1000, 5000 );
            try
            {
                gifw.restore();
                gifw.setLocation( x, y );
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
    
    public boolean clearTextPane( int index )
    {
        boolean success = false;
        GIPaneWrapper gipw = getPaneByIndex( userIndexToTrueIndex( index ) );
        
        if( gipw == null )
        {
            gipw = last_activated_pane;
        }
        
        if( gipw != null )
        {
            if( gipw.getType() == TEXT_PANE )
            {
                GITextPane gitp = (GITextPane) gipw.getPane();
                gitp.clearDocument();
                success = true;
            }
        }
        
        return success;
    }
    
    public boolean switchToNextPane( boolean previous )
    {
        if( ( panes == null ) || ( panes.size() < 4 ) )
        {
            return false;
        }
        
        GIPaneWrapper gipw;
        GIPaneWrapper next_pane = null;
        for( int i = 2, n = panes.size(); i < n; i++ )
        {
            gipw = (GIPaneWrapper) panes.elementAt( i );
            if( gipw == last_activated_pane )
            {
                int motion = ( previous ? -1 : 1 );
                int next_index = i + motion;
                while( next_index != i )
                {
                    if( next_index == n )
                    {
                        next_index = 2;
                    }
                    else if( next_index < 2 )
                    {
                        next_index = n - 1;
                    }
                    if( isUserPane( next_index, EXCLUDE_SPLIT_PANES ) )
                    {
                        break;
                    }
                    next_index += motion;
                }
                
                next_pane = (GIPaneWrapper) panes.elementAt( next_index );
                break;
            }
        }
        
        if( next_pane != null )
        {
            next_pane.activate();
        }
        
        return true;
    }
    
    public boolean switchToNextWindow( boolean previous )
    {
        if( ( frames == null ) || ( frames.size() < 2 ) )
        {
            return false;
        }
        
        GIFrameWrapper gifw;
        GIFrameWrapper next_frame = null;
        for( int i = 0, n = frames.size(); i < n; i++ )
        {
            gifw = (GIFrameWrapper) frames.elementAt( i );
            if( gifw == last_activated_frame )
            {
                int motion = ( previous ? -1 : 1 );
                int next_index = i + motion;
                if( next_index == n )
                {
                    next_index = 0;
                }
                else if( next_index < 0 )
                {
                    next_index = n - 1;
                }
                next_frame = (GIFrameWrapper) frames.elementAt( next_index );
                break;
            }
        }
        
        if( next_frame != null )
        {
            next_frame.activate();
        }
        
        return true;
    }
    
    public boolean activatePane( String regexp )
    {
        boolean success = false;
        
        int n = panes.size();
        if( ( regexp != null ) && ( n > 0 ) )
        {
            GIPaneWrapper gipw;
            int i = getPaneIndexByPaneWrapper( last_activated_pane );
            int end = i;
            
            while( GOD_IS_GOOD )
            {
                i++;
                if( i == n )
                {
                    i = 0;
                }
                if( i == end )
                {
                    break;
                }
                
                gipw = (GIPaneWrapper) panes.elementAt( i );
                switch( gipw.getType() )
                {
                    case TEXT_PANE:
                    case INFO_PANE:
                        if( java.util.regex.Pattern.matches( regexp, gipw.getTitle() ) )
                        {
                            gipw.activate();
                            success = true;
                        }
                        break;
                }
                if( success )
                {
                    break;
                }
            }
        }
        
        return success;
    }
    
    public boolean setActivePane( int index )
    {
        boolean success = false;
        
        try
        {
            GIPaneWrapper gipw = (GIPaneWrapper) panes.elementAt( index );
            last_activated_pane = gipw;
            gipw.activate();
            success = true;
        }
        catch( ArrayIndexOutOfBoundsException e ) { }
        
        return success;
    }
    
    public boolean setActivePaneByUserIndex( int user_index )
    {
        return setActivePane( userIndexToTrueIndex( user_index ) );
    }
    
    /************************************************************************
     *
     * Printing Functions
     *
     */
    
    protected void appendAndHighlightLine(
        GITextPane text_pane,
        String line,
        String qualities
    )
    {
        int offset = text_pane.getDocumentLength();
        int len = text_pane.appendLine( line ).length();
        geo_irc.getHighlightManager().highlight(
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
        GIPaneWrapper pane;
        for( int i = 0; i < n; i++ )
        {
            text_pane = null;
            pane = (GIPaneWrapper) panes.elementAt( i );
            if( pane.getType() == TEXT_PANE )
            {
                text_pane = (GITextPane) pane.getPane();
                if( text_pane.accepts( qualities ) )
                {
                    appendAndHighlightLine( text_pane, line, qualities );
                    windows_printed_to++;
                }
            }
        }
        
        if( ! geo_irc.isActive() )
        {
            int lines_unread = variable_manager.incrementInt( "lines_unread" );
            geo_irc.setTitle(
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
    
    /**********************************************************************
     *
     * getSelected Functions
     *
     */
    
    public GIPaneWrapper getSelectedPane()
    {
        return last_activated_pane;
    }
    
    public GIFrameWrapper getSelectedFrame()
    {
        return last_activated_frame;
    }
    
    public GIPaneWrapper getSelectedTextPane()
    {
        GIPaneWrapper retval = null;
        
        if( last_activated_pane != null )
        {
            if( last_activated_pane.getType() == TEXT_PANE )
            {
                retval = last_activated_pane;
            }
        }
        
        return retval;
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
        String retval = null;
        
        GITextPane gitp = last_activated_text_pane;
        if( gitp != null )
        {
            String filter = gitp.getFilter();
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
        
        return retval;
    }
    
    public void paneActivated( GIPaneWrapper gipw )
    {
        last_activated_pane = gipw;
        if( gipw.getType() == TEXT_PANE )
        {
            GITextPane gitp = (GITextPane) gipw.getPane();
            last_activated_text_pane = gitp;
            
            String filter = gitp.getFilter();
            if( filter != null )
            {
                // Search for a server name in this filter.
                Matcher m = Pattern.compile( "\\S+\\.\\S+\\.\\S+" ).matcher( filter );
                if( m.find() )
                {
                    String server_name = m.group();
                    geo_irc.setCurrentRemoteMachine( server_name );
                }
                else
                {
                    m = Pattern.compile( "\\d+\\.\\d+\\.\\d+\\.\\d+" ).matcher( filter );
                    if( m.find() )
                    {
                        String ip_address = m.group();
                        geo_irc.setCurrentRemoteMachine( ip_address );
                    }
                }
            }
            
            
        }
    }
    
    public void resetLastTextPaneSearched()
    {
        geo_irc.resetLastTextPaneSearched();
    }
    
    /* ************************************************************
     * Listener Implementations
     */
    
    public void internalFrameActivated( InternalFrameEvent e )
    {
        GIWindow giw = (GIWindow) e.getInternalFrame();
        last_activated_frame = giw.getFrameWrapper();
        giw.activateFirstTextPane();
    }
    
    public void internalFrameClosed( InternalFrameEvent e )
    {
        GIWindow window = (GIWindow) e.getSource();
        GIFrameWrapper gifw = window.getFrameWrapper();

        if( last_activated_frame == gifw )
        {
            last_activated_frame = null;
        }
        
        GIPaneWrapper pw;
        for( int i = 0; i < panes.size(); i++ )
        {
            pw = (GIPaneWrapper) panes.elementAt( i );
            Container pw_pane = pw.getPane();
            if( window.isAncestorOf( pw_pane ) )
            {
                panes.remove( pw );
                i--;
                active_info_panes.remove( pw_pane );
                inactive_info_panes.remove( pw_pane );
            }
        }
        
        frames.remove( gifw );
        
        if( ! restoring )
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
        if( ! restoring )
        {
            recordDesktopState();
        }
    }

    public void componentHidden(java.awt.event.ComponentEvent e) { }
    public void componentShown(java.awt.event.ComponentEvent e) { }
    public void componentMoved(java.awt.event.ComponentEvent e)
    {
        /*
        if( ! restoring )
        {
            recordDesktopState();
        }
         */
    }
    public void componentResized(java.awt.event.ComponentEvent e)
    {
        if( ! restoring )
        {
            if( e.getComponent() instanceof GIPane )
            {
                recordDesktopState();
            }
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
    
    public void windowActivated( WindowEvent e )
    {
        GIExternalWindow giew = (GIExternalWindow) e.getSource();
        last_activated_frame = giew.getFrameWrapper();
        giew.activateFirstTextPane();
    }
    
    public void windowClosed( WindowEvent e )
    {
        GIExternalWindow window = (GIExternalWindow) e.getSource();
        GIFrameWrapper gifw = window.getFrameWrapper();

        if( last_activated_frame == gifw )
        {
            last_activated_frame = null;
        }
        
        GIPaneWrapper pw;
        for( int i = 0; i < panes.size(); i++ )
        {
            pw = (GIPaneWrapper) panes.elementAt( i );
            Container pw_pane = pw.getPane();
            if( window.isAncestorOf( pw_pane ) )
            {
                panes.remove( pw );
                i--;
                active_info_panes.remove( pw_pane );
                inactive_info_panes.remove( pw_pane );
            }
        }
        
        frames.remove( gifw);
        
        if( ! restoring )
        {
            recordDesktopState();
        }
    }
    
    public void windowClosing(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowOpened( WindowEvent e )
    {
        if( ! restoring )
        {
            recordDesktopState();
        }
    }
    
    /* ************************************************************ */
    
    protected void recordDesktopState()
    {
        settings_manager.removeNode( "/gui/desktop/" );
        
        int n = panes.size();
        String i_str;
        GIPaneWrapper pane;
        String setting_path;
        int parent_pane_index;
        
        for( int i = 0; i < n; i++ )
        {
            pane = (GIPaneWrapper) panes.elementAt( i );
            i_str = Integer.toString( i );
            
            setting_path = "/gui/desktop/panes/" + i_str;
            
            settings_manager.putInt(
                setting_path + "/type",
                pane.getType()
            );
            settings_manager.putString(
                setting_path + "/title",
                pane.getTitle()
            );
            
            parent_pane_index = pane.getParentIndex( panes );
            
            settings_manager.putInt(
                setting_path + "/parent",
                parent_pane_index
            );

            settings_manager.putInt(
                setting_path + "/split rank",
                pane.getSplitRank()
            );
            
            switch( pane.getType() )
            {
                case TEXT_PANE:
                    settings_manager.putString(
                        setting_path + "/filter",
                        ((GITextPane) pane.getPane()).getFilter()
                    );
                    break;
                    
                case INFO_PANE:
                    settings_manager.putString(
                        setting_path + "/path",
                        ((GIInfoPane) pane.getPane()).getPath()
                    );
                    break;
                
                case CHILD_CONTENT_PANE:
                case EXTERNAL_CONTENT_PANE:
                {
                    /* Store the settings for the window associated with this
                     * content pane.
                     */
                    
                    GIFrameWrapper gifw = (GIFrameWrapper) pane.getFrame();
                    settings_manager.putInt(
                        setting_path + "/x",
                        gifw.getX()
                    );
                    settings_manager.putInt(
                        setting_path + "/y",
                        gifw.getY()
                    );
                    settings_manager.putInt(
                        setting_path + "/height",
                        gifw.getHeight()
                    );
                    settings_manager.putInt(
                        setting_path + "/width",
                        gifw.getWidth()
                    );

                    int state = GI_NORMAL;
                    if( gifw.isMaximized() )
                    {
                        state = GI_MAXIMIZED;
                    }
                    else if( gifw.isMinimized() )
                    {
                        state = GI_MINIMIZED;
                    }
                    settings_manager.putInt(
                        setting_path + "/state",
                        state
                    );

                    settings_manager.putBoolean(
                        setting_path + "/selected",
                        gifw.isSelected()
                    );
                    
                    break;
                }
                
                case SPLIT_PANE:
                {
                    JSplitPane split_pane = (JSplitPane) pane.getPane();
                    int orientation = split_pane.getOrientation();
                    settings_manager.putInt(
                        setting_path + "/orientation",
                        orientation
                    );
                    
                    settings_manager.putInt(
                        setting_path + "/divider location",
                        split_pane.getDividerLocation()
                    );
                        
                    break;
                }
            }
        }
        
        
    }

    public void restoreDesktopState()
    {
        // Panes
        
        int i = 0;
        String i_str;
        String setting_path;
        int num_panes;
        
        int type;
        String title;
        int split_rank;
        int x;
        int y;
        int width;
        int height;
        int state;
        boolean is_selected;
        int orientation;
        int divider_location;
        String filter;
        String path;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            setting_path = "/gui/desktop/panes/" + i_str;
            
            type = settings_manager.getInt( setting_path + "/type", NO_PANE_TYPE );
            
            if( type == NO_PANE_TYPE )
            {
                break;
            }

            title = settings_manager.getString( setting_path + "/title", "" );
            
            split_rank = settings_manager.getInt(
                setting_path + "/split rank", SPLIT_NOT_SPLIT_MEMBER
            );
            
            switch( type )
            {
                case CHILD_CONTENT_PANE:
                case EXTERNAL_CONTENT_PANE:
                {
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
                    
                    GIFrameWrapper gifw = null;
                    switch( type )
                    {
                        case CHILD_CONTENT_PANE:
                        {
                            GIWindow giw = new GIWindow( this, settings_manager, title, panes, frames );
                            desktop_pane.add( giw, x, y );
                            gifw = new GIFrameWrapper( giw );
                            break;
                        }
                        case EXTERNAL_CONTENT_PANE:
                        {
                            GIExternalWindow giew = new GIExternalWindow( this, settings_manager, title, panes, frames );
                            gifw = new GIFrameWrapper( giew );
                            break;
                        }
                    }
                    
                    try
                    {
                        gifw.setBounds( x, y, width, height );
                        switch( state )
                        {
                            case GI_MAXIMIZED:
                                gifw.maximize();
                                break;
                            case GI_MINIMIZED:
                                gifw.minimize();
                                break;
                            case GI_NORMAL:
                            default:
                                gifw.restore();
                                break;
                        }
                        //gifw.activate();
                    }
                    catch( java.beans.PropertyVetoException e )
                    {
                        // Do nothing about this error.
                    }

                    break;
                }
                case SPLIT_PANE:
                {
                    orientation = settings_manager.getInt(
                        setting_path + "/orientation",
                        UNKNOWN_ORIENTATION
                    );
                    divider_location = settings_manager.getInt(
                        setting_path + "/divider location",
                        DEFAULT_DIVIDER_LOCATION
                    );
                    
                    if( orientation != UNKNOWN_ORIENTATION )
                    {
                        JSplitPane split_pane = new JSplitPane( orientation );
                        split_pane.setDividerLocation( divider_location );
                        GIPaneWrapper split_gipw = new GIPaneWrapper( settings_manager, this, split_pane, title, type );
                        split_gipw.setSplitRank( split_rank );
                        panes.add( split_gipw );
                    }
                    else
                    {
                        printlnDebug( i18n_manager.getString( "unknown orientation" ) );
                    }
                    
                    break;
                }   
                case INFO_PANE:
                    path = settings_manager.getString(
                        setting_path + "/path",
                        ""
                    );
                    if( ! path.equals( "" ) )
                    {
                        GIPaneWrapper gipw = addInfoPane( title, path );
                        gipw.setSplitRank( split_rank );
                        inactive_info_panes.add( gipw.getPane() );
                    }
                    break;
                case TEXT_PANE:
                    filter = settings_manager.getString(
                        setting_path + "/filter",
                        ""
                    );
                    if( ! filter.equals( "" ) )
                    {
                        GIPaneWrapper gipw = addTextPane( title, filter );
                        gipw.setSplitRank( split_rank );
                    }
                    break;
                case DESKTOP_PANE:
                {
                    desktop_pane = new JScrollDesktopPane( settings_manager, menu_bar );
                    GIPaneWrapper dgipw = new GIPaneWrapper( settings_manager, this, desktop_pane, "GeoIRC Desktop Pane", DESKTOP_PANE );
                    dgipw.setSplitRank( split_rank );
                    panes.add( dgipw );
                    break;
                }
            }
            
            i++;
        }
        
        num_panes = i;
        
        // Put panes inside their parent panes.
        
        int parent_index;
        GIPaneWrapper gipw;
        GIPaneWrapper parent_gipw;
        for( i = 0; i < num_panes; i++ )
        {
            i_str = Integer.toString( i );
            setting_path = "/gui/desktop/panes/" + i_str;
            
            parent_index = settings_manager.getInt(
                setting_path + "/parent",
                NO_PARENT
            );
            
            if( parent_index != NO_PARENT )
            {
                gipw = (GIPaneWrapper) panes.elementAt( i );
                parent_gipw = (GIPaneWrapper) panes.elementAt( parent_index );
                switch( parent_gipw.getType() )
                {
                    case SPLIT_PANE:
                    {
                        split_rank = gipw.getSplitRank();
                        JSplitPane split_pane = (JSplitPane) parent_gipw.getPane();
                        switch( split_rank )
                        {
                            case SPLIT_PRIMARY:
                               split_pane.setTopComponent( gipw.getPane() );
                               break;
                            case SPLIT_SECONDARY:
                               split_pane.setBottomComponent( gipw.getPane() );
                               break;
                        }
                        gipw.setParent( parent_gipw );
                        break;
                    }
                    case GEOIRC_CONTENT_PANE:
                        geo_irc.getContentPane().add( gipw.getPane() );
                        gipw.setFrame( geoirc_gifw );
                        gipw.setParent( parent_gipw );
                        break;
                    case CHILD_CONTENT_PANE:
                    case EXTERNAL_CONTENT_PANE:
                    {
                        GIFrameWrapper gifw = (GIFrameWrapper) parent_gipw.getFrame();
                        gifw.addPane( gipw.getPane() );
                        gipw.setParent( parent_gipw );
                        break;
                    }
                    default:
                        printlnDebug(
                            i18n_manager.getString(
                                "bad parent",
                                new Object [] { new Integer( i ), new Integer( gipw.getType() ) }
                            )
                        );
                        break;
                }
            }
        }
        
        restoring = false;
    }    
    
    public boolean isUserPane( int index, boolean include_split_panes )
    {
        boolean retval = false;
        
        switch( ((GIPaneWrapper) panes.elementAt( index )).getType() )
        {
            case TEXT_PANE:
            case INFO_PANE:
                retval = true;
                break;
            case SPLIT_PANE:
                retval = include_split_panes;
                break;
        }
        
        return retval;
    }
    
    /**
     * @return -1 if true_index does not point to a user pane
     */
    public int trueIndexToUserIndex( int true_index )
    {
        int user_index = -1;
        
        if( ( true_index >= 0 ) && ( true_index < panes.size() ) )
        {
            if( isUserPane( true_index, EXCLUDE_SPLIT_PANES ) )
            {
                int user_panes_counted = 0;
                for( int i = 0; i < true_index; i++ )
                {
                    if( isUserPane( i, EXCLUDE_SPLIT_PANES ) )
                    {
                        user_panes_counted++;
                    }
                }
                user_index = user_panes_counted + 1;
            }
        }
        
        return user_index;
    }
    
    /**
     * @return -1 if user_index is out of bounds
     */
    public int userIndexToTrueIndex( int user_index )
    {
        int true_index = -1;
        
        int user_panes_counted = 0;
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            if( isUserPane( i, EXCLUDE_SPLIT_PANES ) )
            {
                user_panes_counted++;
                if( user_panes_counted == user_index )
                {
                    true_index = i;
                    break;
                }
            }
        }
        
        return true_index;
    }
    
    public void listPanes()
    {
        listPanes( true );
    }
    public void listPanes( boolean users_point_of_view )
    {
        GIPaneWrapper gipw;
        int user_index;
        for( int i = 0, n = panes.size(); i < n; i++ )
        {
            if( users_point_of_view )
            {
                if( isUserPane( i, EXCLUDE_SPLIT_PANES ) )
                {
                    gipw = (GIPaneWrapper) panes.elementAt( i );
                    user_index = trueIndexToUserIndex( i );
                    printlnDebug( Integer.toString( user_index ) + ": " + gipw.toString() );
                }
            }
            else
            {
                gipw = (GIPaneWrapper) panes.elementAt( i );
                printlnDebug( Integer.toString( i ) + ": " + gipw.toString() );
            }
        }
    }
    
    public void listWindows()
    {
        GIFrameWrapper gifw;
        for( int i = 0, n = frames.size(); i < n; i++ )
        {
            gifw = (GIFrameWrapper) frames.elementAt( i );
            printlnDebug( Integer.toString( i ) + ": " + gifw.toString() );
        }
    }
    
    public MenuManager getMenuManager()
    {
        return geo_irc.getMenuManager();
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

