/*
 * GeoIRC
 * An Internet Relay Chat client.
 * Copyright (C) 2003 Alex Reyes ("Pistos")
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA;
 * or visit http://www.gnu.org/licenses/gpl.html .
 *
 * ******************************************
 *
 * GeoIRC.java
 *
 * Created on June 21, 2003, 11:12 AM
 *
 */

package geoirc;

import geoirc.conf.SettingsDialog;
import geoirc.gui.*;
import geoirc.util.Util;

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jscroll.components.ResizableToolBar;

/**
 *
 * @author  Pistos
 */
public class GeoIRC
    extends javax.swing.JFrame
    implements
        GeoIRCConstants,
        ActionListener,
        CommandExecutor,
        FocusListener,
        ComponentListener,
        WindowListener,
        MouseListener,
        DCCAgent,
        InputFieldOwner
{
    protected LinkedHashSet remote_machines;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected TriggerManager trigger_manager;
    protected AliasManager alias_manager;
    protected InfoManager info_manager;
    protected VariableManager variable_manager;
    protected LogManager log_manager;
    protected StyleManager style_manager;
    protected HighlightManager highlight_manager;
    protected I18nManager i18n_manager;
    protected PythonScriptInterface python_script_interface;
    protected TclScriptInterface tcl_script_interface;
    
    protected Vector tcl_procs;
    
    protected Hashtable processes;
    protected Hashtable audio_clips;
    protected Vector dcc_requests;
    protected Vector dcc_offers;
    
    protected IdentServer ident_server;
    
    protected RemoteMachine current_rm;

    protected LinkedList input_history;
    protected int input_history_pointer;
    protected boolean input_saved;
    protected boolean listening_to_connections;
    
    protected InputMap input_map;
    protected ActionMap action_map;
    
    protected String preferred_nick;
    
    protected Set conversation_words;
    
    protected boolean mouse_button_depressed;
    protected boolean use_skinning;
    
    protected ResizableToolBar pane_bar;
    
    /* **************************************************************** */
    
    public GeoIRC()
    {
        this( DEFAULT_SETTINGS_FILEPATH );
    }
    
    public GeoIRC( String settings_filepath )
    {
        listening_to_connections = false;
        use_skinning = false;
        
        // Settings.
        
        settings_manager = new SettingsManager(
            display_manager,
            ( settings_filepath == null ) ? DEFAULT_SETTINGS_FILEPATH : settings_filepath
        );
        settings_manager.loadSettingsFromXML();

        i18n_manager = new I18nManager( settings_manager );

        System.out.println( i18n_manager.getString( "version string", new Object [] { GEOIRC_VERSION } ) );
        System.out.println( i18n_manager.getString( "copyright" ) );
        System.out.println( i18n_manager.getString( "gpl1" ) );
        System.out.println( i18n_manager.getString( "gpl2" ) );
        
        variable_manager = new VariableManager();
        
        // Setup GUI.

        initComponents();
        
        restoreMainFrameState();
        setFocusable( false );
        
        this.setTitle( BASE_GEOIRC_TITLE );
        
        addComponentListener( this );
        addWindowListener( this );

        input_field.grabFocus();
        input_field.addActionListener( this );
        input_field.addFocusListener( this );
        input_field.addMouseListener( this );

        pane_bar = new ResizableToolBar( MINIMUM_PANE_BAR_BUTTON_WIDTH, MAXIMUM_PANE_BAR_BUTTON_WIDTH );
        getContentPane().add( pane_bar, java.awt.BorderLayout.NORTH );
        
        // Disable Ctrl-V, so that we can use our own Ctrl-V handler so
        // we can paste multiple lines in the input_field.
        InputMap map = input_field.getInputMap();
        KeyStroke pastekeystroke = KeyStroke.getKeyStroke( KeyEvent.VK_V, InputEvent.CTRL_MASK );
        while( map != null )
        {
            map.remove( pastekeystroke );
            map = map.getParent();
        }
        
        // Un-map the Tab-related default mappings which have to do with focus traversal.
        input_field.setFocusTraversalKeysEnabled( false );
        
        input_map = input_field.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        action_map = input_field.getActionMap();
        
        input_history = new LinkedList();
        input_history_pointer = MOST_RECENT_ENTRY;
        input_saved = false;

        display_manager = new DisplayManager(
            this, menu_bar, pane_bar, settings_manager, variable_manager, i18n_manager, input_field
        );

        applySettings();
        
        display_manager.restoreDesktopState();
        
        info_manager = new InfoManager( settings_manager, display_manager, i18n_manager );
        
        display_manager.printlnDebug( i18n_manager.getString( "version string", new Object [] { GEOIRC_VERSION } ) );
        display_manager.printlnDebug( i18n_manager.getString( "copyright" ) );
        display_manager.printlnDebug( i18n_manager.getString( "gpl1" ) );
        display_manager.printlnDebug( i18n_manager.getString( "gpl2" ) );
        display_manager.printlnDebug(
            "----------------------\n"
        );
        
        // Scripting.
        
        initializeScriptingInterfaces();
        
        // Ident server.
        
        ident_server = new IdentServer( settings_manager, display_manager, i18n_manager );
        ident_server.start();
                
        // Restore connections, if any.
        
        conversation_words = java.util.Collections.synchronizedSet( new java.util.HashSet() );
        current_rm = null;
        remote_machines = new LinkedHashSet();
        restoreConnections();
        
        // Final miscellaneous initialization
        
        settings_manager.listenToPreferences();
        display_manager.beginListening();
        listening_to_connections = true;
        processes = new Hashtable();
        dcc_requests = new Vector();
        dcc_offers = new Vector();
        audio_clips = new Hashtable();
        mouse_button_depressed = false;
        
        // Open the curtains!

        show();
    }
    
    /**
     * This method is used to apply the settings of the SettingsManager at
     * startup, as well as to apply them after the user has changed them via
     * the settings GUI.
     *
     * @return a string describing any errors that occurred when trying to apply the specified skin(s)
     */
    public void applySettings()
    {
        i18n_manager = new I18nManager( settings_manager );
        
        // Apply skin, if any specified.
        String skin1 = settings_manager.getString( "/gui/skin1", null );
        String skin2 = settings_manager.getString( "/gui/skin2", null );

        try
        {
            Class.forName("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
            SkinManager skin_manager = new SkinManager();
            skin_manager.applySkin(skin1, skin2, i18n_manager);
            display_manager.printlnDebug( skin_manager.getSkinMessages() );
            use_skinning = true;
            /*
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
             */
        }
        catch (ClassNotFoundException e)
        {
            display_manager.printlnDebug( "SkinLF library not found. If you want to use skins install SkinLF from http://www.l2fprod.com." );
            use_skinning = false;
        }
        /*
        catch( Exception e )
        {
            System.err.println( "Urg." );
            e.printStackTrace();
        }
         */

        // GUI
        
        input_field.setFont( new Font(
            settings_manager.getString( "/gui/input field/font face", "Lucida Console" ),
            Font.PLAIN,
            settings_manager.getInt( "/gui/input field/font size", 14 )
        ) );
        int [] rgb = Util.getRGB( settings_manager.getString( "/gui/input field/foreground colour", DEFAULT_INPUT_FIELD_FOREGROUND ) );
        input_field.setForeground( new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] ) );
        rgb = Util.getRGB( settings_manager.getString( "/gui/input field/background colour", DEFAULT_INPUT_FIELD_BACKGROUND ) );
        input_field.setBackground( new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] ) );
        
        // Misc settings
        
        preferred_nick = settings_manager.getString( "/personal/nick1", "GeoIRC_User" );
        
        // Map input (keystrokes, mouseclicks, etc.)
        
        setupFullKeyMapping( KeyEvent.VK_F1 );
        setupFullKeyMapping( KeyEvent.VK_F2 );
        setupFullKeyMapping( KeyEvent.VK_F3 );
        setupFullKeyMapping( KeyEvent.VK_F4 );
        setupFullKeyMapping( KeyEvent.VK_F5 );
        setupFullKeyMapping( KeyEvent.VK_F6 );
        setupFullKeyMapping( KeyEvent.VK_F7 );
        setupFullKeyMapping( KeyEvent.VK_F8 );
        setupFullKeyMapping( KeyEvent.VK_F9 );
        setupFullKeyMapping( KeyEvent.VK_F10 );
        setupFullKeyMapping( KeyEvent.VK_F11 );
        setupFullKeyMapping( KeyEvent.VK_F12 );
        setupFullKeyMapping( KeyEvent.VK_UP );
        setupFullKeyMapping( KeyEvent.VK_DOWN );
        setupFullKeyMapping( KeyEvent.VK_LEFT );
        setupFullKeyMapping( KeyEvent.VK_RIGHT );
        setupFullKeyMapping( KeyEvent.VK_PAGE_UP );
        setupFullKeyMapping( KeyEvent.VK_PAGE_DOWN );
        setupFullKeyMapping( KeyEvent.VK_HOME );
        setupFullKeyMapping( KeyEvent.VK_END );
        setupFullKeyMapping( KeyEvent.VK_INSERT );
        setupFullKeyMapping( KeyEvent.VK_DELETE );
        setupFullKeyMapping( KeyEvent.VK_ESCAPE );
        setupFullKeyMapping( KeyEvent.VK_TAB );
        setupFullKeyMapping( KeyEvent.VK_A );
        setupFullKeyMapping( KeyEvent.VK_B );
        setupFullKeyMapping( KeyEvent.VK_C );
        setupFullKeyMapping( KeyEvent.VK_D );
        setupFullKeyMapping( KeyEvent.VK_E );
        setupFullKeyMapping( KeyEvent.VK_F );
        setupFullKeyMapping( KeyEvent.VK_G );
        setupFullKeyMapping( KeyEvent.VK_H );
        setupFullKeyMapping( KeyEvent.VK_I );
        setupFullKeyMapping( KeyEvent.VK_J );
        setupFullKeyMapping( KeyEvent.VK_K );
        setupFullKeyMapping( KeyEvent.VK_L );
        setupFullKeyMapping( KeyEvent.VK_M );
        setupFullKeyMapping( KeyEvent.VK_N );
        setupFullKeyMapping( KeyEvent.VK_O );
        setupFullKeyMapping( KeyEvent.VK_P );
        setupFullKeyMapping( KeyEvent.VK_Q );
        setupFullKeyMapping( KeyEvent.VK_R );
        setupFullKeyMapping( KeyEvent.VK_S );
        setupFullKeyMapping( KeyEvent.VK_T );
        setupFullKeyMapping( KeyEvent.VK_U );
        setupFullKeyMapping( KeyEvent.VK_V );
        setupFullKeyMapping( KeyEvent.VK_W );
        setupFullKeyMapping( KeyEvent.VK_X );
        setupFullKeyMapping( KeyEvent.VK_Y );
        setupFullKeyMapping( KeyEvent.VK_Z );
        setupFullKeyMapping( KeyEvent.VK_1 );
        setupFullKeyMapping( KeyEvent.VK_2 );
        setupFullKeyMapping( KeyEvent.VK_3 );
        setupFullKeyMapping( KeyEvent.VK_4 );
        setupFullKeyMapping( KeyEvent.VK_5 );
        setupFullKeyMapping( KeyEvent.VK_6 );
        setupFullKeyMapping( KeyEvent.VK_7 );
        setupFullKeyMapping( KeyEvent.VK_8 );
        setupFullKeyMapping( KeyEvent.VK_9 );
        setupFullKeyMapping( KeyEvent.VK_0 );

        // Managers
        
        style_manager = new StyleManager( settings_manager, display_manager, i18n_manager );
        highlight_manager = new HighlightManager( settings_manager, display_manager, i18n_manager );
        log_manager = new LogManager( settings_manager, display_manager, i18n_manager );
        display_manager.setLogManager( log_manager );
        trigger_manager = new TriggerManager( this, settings_manager, display_manager, i18n_manager );
        alias_manager = new AliasManager( settings_manager, display_manager, variable_manager );
        
        display_manager.applySettings();
    }
    
    protected void initializeScriptingInterfaces()
    {
        python_script_interface = null;
        
        try
        {
            Class python_script_interface_class = Class.forName( "geoircscripting.PythonScriptInterfaceClass" );
            Class [] arg_template =
                new Class []
                { 
                    CommandExecutor.class,
                    SettingsManager.class,
                    DisplayManager.class,
                    VariableManager.class,
                    I18nManager.class
                };
            Constructor constructor = python_script_interface_class.getConstructor( arg_template );
            python_script_interface = (PythonScriptInterface) constructor.newInstance(
                new Object [] {
                    this, settings_manager, display_manager, variable_manager, i18n_manager,
                }
            );

            display_manager.printlnDebug( i18n_manager.getString( "python inited" ) );
        }
        catch( Exception e )
        {
            Util.printException( display_manager, e, i18n_manager.getString( "python init error" ) );
        }
        
        tcl_script_interface = null;
        tcl_procs = null;
        
        try
        {
            Class tcl_script_interface_class = Class.forName( "geoircscripting.TclScriptInterfaceClass" );
            Class [] arg_template =
                new Class []
                { 
                    CommandExecutor.class,
                    SettingsManager.class,
                    DisplayManager.class,
                    VariableManager.class,
                    I18nManager.class,
                    Vector.class
                };
            Constructor constructor = tcl_script_interface_class.getConstructor( arg_template );
            tcl_script_interface = (TclScriptInterface) constructor.newInstance(
                new Object [] {
                    this, settings_manager, display_manager, variable_manager, i18n_manager, tcl_procs
                }
            );
            tcl_procs = new Vector();
            display_manager.printlnDebug( i18n_manager.getString( "tcl inited" ) );
        }
        catch( Exception e )
        {
            Util.printException( display_manager, e, i18n_manager.getString( "tcl init error" ) );
        }
    }
    
    /* ********************************************************************* */
    
    protected void setupKeyMapping( int modifiers, int keycode )
    {
        int java_modifiers;
        
        switch( modifiers )
        {
            case SHIFT:
                java_modifiers = InputEvent.SHIFT_DOWN_MASK;
                break;
            case CTRL:
                java_modifiers = InputEvent.CTRL_DOWN_MASK;
                break;
            case CTRL+SHIFT:
                java_modifiers = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
                break;
            case ALT:
                java_modifiers = InputEvent.ALT_DOWN_MASK;
                break;
            case ALT+SHIFT:
                java_modifiers = InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
                break;
            case CTRL+ALT:
                java_modifiers = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
                break;
            case CTRL+ALT+SHIFT:
                java_modifiers = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
                break;
            case NO_MODIFIER_KEYS:
            default:
                java_modifiers = 0;
                break;
        }
        
        String stroke_text =
            InputEvent.getModifiersExText( java_modifiers ) + "|"
            + KeyEvent.getKeyText( keycode );
        
        input_map.put(
            KeyStroke.getKeyStroke( keycode, java_modifiers ),
            stroke_text
        );
        String command = settings_manager.getString( "/keyboard/" + stroke_text, "" );
        if( ! command.equals( "" ) )
        {
            action_map.put( stroke_text, new GIAction( command, this ) );
        }
    }
    
    protected void setupFullKeyMapping( int keycode )
    {
        setupKeyMapping( NO_MODIFIER_KEYS, keycode );
        setupKeyMapping( CTRL, keycode );
        setupKeyMapping( ALT, keycode );
        setupKeyMapping( SHIFT, keycode );
        setupKeyMapping( CTRL+ALT, keycode );
        setupKeyMapping( ALT+SHIFT, keycode );
        setupKeyMapping( CTRL+SHIFT, keycode );
        setupKeyMapping( CTRL+ALT+SHIFT, keycode );
    }
    
    public StyleManager getStyleManager()
    {
        return style_manager;
    }
    
    public HighlightManager getHighlightManager()
    {
        return highlight_manager;
    }
    
    public ActionMap getActionMap()
    {
        return this.action_map;
    }
    
    // Returns the Server created.
    protected Server addServer( String hostname, String port )
    {
        Server s = new Server(
            this, display_manager, settings_manager, info_manager, variable_manager,
            i18n_manager, conversation_words, hostname, port
        );
        addRemoteMachine( s );
        
        return s;
    }
    
    public DCCClient addDCCClient(
        String hostname,
        String port,
        int type,
        String user_nick,
        String remote_nick,
        String arg1,
        int filesize
    )
    {
        DCCClient dcc_client = new DCCClient(
            this,
            display_manager,
            settings_manager,
            i18n_manager,
            hostname,
            port,
            type,
            user_nick,
            remote_nick,
            arg1,
            filesize
        );
        addRemoteMachine( dcc_client );
        
        if( type == DCC_CHAT )
        {
            display_manager.addTextWindow(
                remote_nick + " @ " + dcc_client.toString(),
                dcc_client.toString()
            );
        }
        
        return dcc_client;
    }
    
    protected void addRemoteMachine( RemoteMachine rm )
    {
        remote_machines.add( rm );
        recordConnections();
    }
    
    protected void removeRemoteMachine( RemoteMachine rm )
    {
        remote_machines.remove( rm );
        info_manager.removeRemoteMachine( rm );
        recordConnections();
    }
    
    public void recordConnections()
    {
        if( listening_to_connections )
        {
            settings_manager.removeNode( "/connections/" );
            Iterator it = remote_machines.iterator();
            RemoteMachine rm;
            String i_str;
            int i = 0;

            while( it.hasNext() )
            {
                i_str = Integer.toString( i );
                rm = (RemoteMachine) it.next();

                if( rm instanceof DCCClient )
                {
                    continue;
                }

                settings_manager.putString(
                    "/connections/" + i_str + "/type",
                    rm.getClass().toString()
                );
                settings_manager.putString(
                    "/connections/" + i_str + "/hostname",
                    rm.getHostname()
                );
                settings_manager.putInt(
                    "/connections/" + i_str + "/port",
                    rm.getPort()
                );

                if( rm instanceof Server )
                {
                    Server s = (Server) rm;
                    s.recordChannels();
                }
                
                i++;
            }
        }
    }
    
    protected void restoreConnections()
    {
        int i = 0;
        String i_str;
        
        String type;
        String hostname;
        int port;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            type = settings_manager.getString(
                "/connections/" + i_str + "/type",
                ""
            );
            if( type.equals( "" ) )
            {
                // No more connections stored in the settings.
                break;
            }
            type = type.substring( type.lastIndexOf( "." ) + 1 );

            hostname = settings_manager.getString(
                "/connections/" + i_str + "/hostname",
                ""
            );
            port = settings_manager.getInt(
                "/connections/" + i_str + "/port",
                RemoteMachine.DEFAULT_PORT
            );
            
            if( type.equals( "Server") )
            {
                Server s = addServer( hostname, Integer.toString( port ) );
                s.connect( preferred_nick );
            }
            else
            {
                // Huh?  Unrecognized RemoteMachine type.
                display_manager.printlnDebug( i18n_manager.getString( "unknown rm type" ) );
            }
            
            i++;
        }
    }
    
    protected void recordMainFrameState()
    {
        settings_manager.putInt(
            "/gui/main frame x",
            getX()
        );
        settings_manager.putInt(
            "/gui/main frame y",
            getY()
        );
        settings_manager.putInt(
            "/gui/main frame width",
            getWidth()
        );
        settings_manager.putInt(
            "/gui/main frame height",
            getHeight()
        );
        
        int state = getExtendedState();
        settings_manager.putInt(
            "/gui/main frame state",
            state
        );
    }
    
    protected void restoreMainFrameState()
    {
        int state = settings_manager.getInt(
            "/gui/main frame state",
            Frame.MAXIMIZED_BOTH
        );
        setExtendedState( state );
        
        if( state != Frame.MAXIMIZED_BOTH )
        {
            int x = settings_manager.getInt( "/gui/main frame x", 50 );
            int y = settings_manager.getInt( "/gui/main frame y", 50 );
            int width = settings_manager.getInt( "/gui/main frame width", 600 );
            int height = settings_manager.getInt( "/gui/main frame height", 400 );
            setBounds( x, y, width, height );
        }                        
    }
    
    public String getRemoteMachineID( RemoteMachine rm )
    {
        Iterator it = remote_machines.iterator();
        RemoteMachine rm_;
        int index = 0;
        while( it.hasNext() )
        {
            rm_ = (RemoteMachine) it.next();
            if( rm_ == rm )
            {
                break;
            }
            index++;
        }
        
        String retval = null;
        if( index < remote_machines.size() )
        {
            retval = Integer.toString( index );
        }
        
        return retval;
    }
    
    public boolean setCurrentRemoteMachine( String rm_name )
    {
        RemoteMachine rm;
        boolean was_set = false;
        Iterator it = remote_machines.iterator();
        
        while( it.hasNext() )
        {
            rm = (RemoteMachine) it.next();
            if( rm.getHostname().equals( rm_name ) )
            {
                current_rm = rm;
                was_set = true;
                break;
            }
        }
        
        return was_set;
    }
    
    public void setCurrentRemoteMachine( RemoteMachine rm )
    {
        current_rm = rm;
    }
    
    public RemoteMachine getRemoteMachineByIDString( String id )
    {
        int server_id = -1;
        RemoteMachine retval = null;

        try
        {
            server_id = Integer.parseInt( id );
            if( ( server_id >= 0 ) && ( server_id < remote_machines.size() ) )
            {
                Iterator it = remote_machines.iterator();
                int i = 0;
                while( it.hasNext() )
                {
                    retval = (RemoteMachine) it.next();
                    if( i == server_id )
                    {
                        break;
                    }
                    i++;
                }
            }
        } catch( NumberFormatException e ) { }
        
        return retval;
    }
    
    public static String getATimeStamp( String pattern )
    {
        // http://java.sun.com/docs/books/tutorial/i18n/format/datepattern.html

        String timestamp = "";
        SimpleDateFormat formatter;
        if( ! pattern.equals( "" ) )
        {
            formatter = new SimpleDateFormat( pattern );
            timestamp = formatter.format( new Date() ) + " ";
        }
        
        return timestamp;
    }

    protected void addToInputHistory( String text )
    {
        input_history.addFirst( text );
        if( input_history.size() > MAX_HISTORY_SIZE )
        {
            input_history.removeLast();
        }
    }

    public int addDCCRequest( String [] args, String remote_nick )
    {
        int index = dcc_requests.size();
        dcc_requests.add( new DCCRequest( args, this, remote_nick, settings_manager ) );
        return index;
    }
    public String [] removeDCCRequest( int index )
    {
        return (String []) dcc_requests.remove( index );
    }

    protected void insertCharAtCaret( char char_to_insert )
    {
        String input_line = input_field.getText();
        int pos = input_field.getCaretPosition();
        input_line =
            input_line.substring( 0, pos )
            + char_to_insert
            + input_line.substring( pos );
        input_field.setText( input_line );
    }
    
    /**
     * As opposed to an extended (multiline-capable) paste.
     */
    public void regularPaste( String text_to_paste )
    {
        int selection_start = input_field.getCaretPosition();
        int selection_end = selection_start;

        if( input_field.getSelectedText() != null )
        {
            selection_start = input_field.getSelectionStart();
            selection_end = input_field.getSelectionEnd();
        }
        
        input_field.replaceSelection( text_to_paste );
        int new_caret_pos = selection_start + text_to_paste.length();
        input_field.setCaretPosition( new_caret_pos );
        input_field.setSelectionStart( new_caret_pos );
        input_field.setSelectionEnd( new_caret_pos );
    }

    public int getFloodAllowance()
    {
        return settings_manager.getInt(
            "/misc/paste flood/allowance",
            DEFAULT_PASTE_FLOOD_ALLOWANCE
        );
    }
    
    public int getFloodDelay()
    {
        return settings_manager.getInt(
            "/misc/paste flood/delay",
            DEFAULT_PASTE_FLOOD_DELAY
        );
    }
    
    public void checkAgainstTriggers( String message, String qualities )
    {
        trigger_manager.check( message, qualities );
    }
    
    /**
     * Passes a message through the scripting engines, and returns it as
     * modified by the scripting engines.
     */
    public String [] onRaw( String line, String qualities )
    {
        String [] transformed_message = new String[ 2 ];
        transformed_message[ MSG_TEXT ] = line;
        transformed_message[ MSG_QUALITIES ] = qualities;
        
        if( python_script_interface != null )
        {
            transformed_message = python_script_interface.onRaw(
                transformed_message[ MSG_TEXT ], transformed_message[ MSG_QUALITIES ]
            );
        }
        
        if( tcl_script_interface != null )
        {
            transformed_message = tcl_script_interface.onRaw(
                transformed_message[ MSG_TEXT ], transformed_message[ MSG_QUALITIES ]
            );
        }
        
        return transformed_message;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        input_field = new javax.swing.JTextField();
        menu_bar = new javax.swing.JMenuBar();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        getContentPane().add(input_field, java.awt.BorderLayout.SOUTH);

        setJMenuBar(menu_bar);

        pack();
    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    /* *********************************************************************
     *
     * Listener Implementations
     *
     * ********************************************************************* */
    
    // When the user presses enter in the input field, this method is called.
    public void actionPerformed( ActionEvent event )
    {
        useInputField();
    }
    public void useInputField()
    {
        String text = input_field.getText();
        
        if( ( text == null ) || ( text.equals( "" ) ) )
        {
            return;
        }

        input_history_pointer = MOST_RECENT_ENTRY;
        if( input_saved )
        {
            input_history.set( input_history_pointer, text );
        }
        else
        {
            addToInputHistory( text );
        }
        input_saved = false;

        if( python_script_interface != null )
        {
            text = python_script_interface.onInput( text );
        }
        if( tcl_script_interface != null )
        {
            text = tcl_script_interface.onInput( text );
        }

        /* What we do with this input depends on its nature.
         * 
         * If the text starts with a backslash, we send it (without the
         * backslash) to the server as-is (raw).
         *
         * If the text starts with a slash, we must interpret it as a command
         * alias.
         *
         * All other text is sent to the current channel, chat, or process,
         * if any.
         */
        
        if( text.charAt( 0 ) == '\\' )
        {
            text = text.substring( 1 );
            execute( CMDS[ CMD_SEND_RAW ] + " " + text );
        }
        else if( text.charAt( 0 ) == '/' )
        {
            text = text.substring( 1 );
            execute( text );
        }
        else if( current_rm instanceof DCCClient )
        {
            display_manager.println(
                getATimeStamp(
                    settings_manager.getString(
                        "/gui/format/timestamp", ""
                    )
                )
                + "<" + ((DCCClient) current_rm).getUserNick() + "> " + text,
                current_rm.toString() + " "
                + FILTER_SPECIAL_CHAR + "dccchat"
                + " from=" + FILTER_SPECIAL_CHAR + "self"
            );
            
            execute( CMDS[ CMD_SEND_RAW ] + " " + text );
        }
        else
        {
            String channel = display_manager.getSelectedChannel();
            if( channel != null )
            {
                // Send to a channel.
                
                execute(
                    CMDS[ CMD_SEND_RAW ]
                    + " privmsg "
                    + channel + " :"
                    + text
                );
            }
            else
            {
                GIPaneWrapper gipw = display_manager.getSelectedTextPane();
                boolean handled = false;
                if( gipw != null )
                {
                    GITextPane gitp = (GITextPane) gipw.getPane();
                    String filter = gitp.getFilter();

                    if( filter.matches( ".*from=%self.*" ) )
                    {
                        // Determine the other party in this query conversation
                        // based on the filter.

                        Pattern p = Pattern.compile( "from=([^%]\\S*)" );
                        Matcher m = p.matcher( filter );
                        if( m.find() )
                        {
                            String recipient = m.group( 1 );
                            execute(
                                CMDS[ CMD_SEND_RAW ]
                                + " privmsg "
                                + recipient + " :"
                                + text
                            );
                            handled = true;
                        }
                    }
                }
                    
                if( ! handled )
                {
                
                    String process_id = display_manager.getSelectedProcess();
                    if( process_id != null )
                    {
                        // Send to a process.

                        process_id = process_id.substring( process_id.indexOf( "=" ) + 1 );
                        Integer pid;
                        try
                        {
                            pid = new Integer( Integer.parseInt( process_id ) );
                            GIProcess gip = (GIProcess) processes.get( pid );
                            gip.println( text );
                        }
                        catch( NumberFormatException e )
                        {
                            display_manager.printlnDebug(
                                i18n_manager.getString( "bad process id", new Object [] { process_id } )
                            );
                        }
                    }
                    else
                    {
                        String dcc_conn = display_manager.getSelectedDCCConnection();
                        String dcc_ip = null;
                        if( dcc_conn != null )
                        {
                            dcc_ip = dcc_conn.substring( "dcc=".length() );
                        }
                        if( dcc_ip != null )
                        {
                            // Send to a dcc connection.

                            DCCConnection dcc;

                            for( int i = 0, n = dcc_offers.size(); i < n; i++ )
                            {
                                dcc = (DCCConnection) dcc_offers.elementAt( i );
                                if( dcc.getRemoteIPString().equals( dcc_ip ) )
                                {
                                    display_manager.println(
                                        getATimeStamp(
                                            settings_manager.getString(
                                                "/gui/format/timestamp", ""
                                            )
                                        )
                                            + "<" + dcc.getUserNick() + "> " + text,
                                        dcc.getQualities()
                                            + " from=" + FILTER_SPECIAL_CHAR + "self"
                                    );
                                    dcc.println( text );
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Clear the input field.
        input_field.setText( "" );
        input_field.setCaretPosition( 0 );
    }

    public void focusGained( FocusEvent e ) { }
    public void focusLost( FocusEvent e )
    {
        if( ! mouse_button_depressed )
        {
            Component thief = e.getOppositeComponent();
        
            if(
                ( use_skinning == true )
                && ( thief instanceof JButton )
            )
            {
                SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Thread.sleep( 300 );  // milliseconds
                            } catch( InterruptedException e ) { }
                            input_field.grabFocus();
                        }
                    }
                );
            }
            else
            {
                boolean is_in_settings = false;
                Component c = thief;
                while( c != null )
                {
                    if( c instanceof SettingsDialog )
                    {
                        is_in_settings = true;
                        break;
                    }
                    c = c.getParent();
                }
                
                if( ! is_in_settings )
                {
                    input_field.grabFocus();
                }
            }
            /*
            else if( thief != null )
            {
                display_manager.printlnDebug( "Focus stolen by: " 
                + thief.getClass().toString() );
            }
             */
        }
    }

    public void componentHidden(ComponentEvent e) { }
    public void componentShown(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e)
    {
        recordMainFrameState();
    }
    
    public void componentResized(ComponentEvent e)
    {
        recordMainFrameState();
    }

    public void windowActivated( WindowEvent e )
    {
        variable_manager.setInt( "lines_unread", 0 );
        setTitle( BASE_GEOIRC_TITLE );
    }
    public void windowClosed( WindowEvent e ) { }
    public void windowClosing( WindowEvent e ) { }
    public void windowDeactivated( WindowEvent e ) { }
    public void windowDeiconified( WindowEvent e ) { }
    public void windowIconified( WindowEvent e ) { }
    public void windowOpened( WindowEvent e ) { }

    public void mouseClicked( MouseEvent e ) { }
    public void mouseEntered( MouseEvent e ) { }
    public void mouseExited( MouseEvent e ) { }
    public void mousePressed( MouseEvent e )
    {
        mouse_button_depressed = true;
    }
    public void mouseReleased( MouseEvent e )
    {
        mouse_button_depressed = false;
        
        Component component = e.getComponent();
        if( component instanceof JTextPane )
        {
            JTextPane text_pane = (JTextPane) component;
            text_pane.copy();
        }
        
        //if( e.getButton() == MouseEvent.BUTTON3 )
        if( javax.swing.SwingUtilities.isMiddleMouseButton( e ) )
        {
            //input_field.paste();
            execute( CMDS[ CMD_EXTENDED_PASTE ] );
        }
        
        input_field.grabFocus();
    }
    
    /* *********************************************************************
     *
     * @param command   the command to execute
     *
     * @return a result code.  @see geoirc.CommandExecutor
     *
     */
    
    public int execute( String command_ )
    {
        if( ( command_ == null ) || ( command_.equals( "" ) ) )
        {
            return CommandExecutor.EXEC_BAD_COMMAND;
        }
        
        String command = alias_manager.expand( command_ );
        command = variable_manager.replaceAll( command );
        
        int result = CommandExecutor.EXEC_GENERAL_FAILURE;
        int space_index = command.indexOf( " " );
        String command_name;
        String arg_string = null;
        String [] args = null;
        if( space_index > -1 )
        {
            command_name = command.substring( 0, space_index ).toLowerCase();
            arg_string = command.substring( space_index + 1 );
            args = Util.tokensToArray( arg_string );
            if( args == null )
            {
                arg_string = null;
            }
        }
        else
        {
            command_name = command.toLowerCase();
        }
        
        int command_id = UNKNOWN_COMMAND;
        for( int i = 0; i < CMDS.length; i++ )
        {
            if( command_name.equals( CMDS[ i ] ) )
            {
                command_id = i;
                break;
            }
        }
        
        switch( command_id )
        {
            case CMD_ACCEPT_DCC_REQUEST:
                {
                    boolean problem = true;
                    
                    if( args != null )
                    {
                        try
                        {
                            int index = Integer.parseInt( args[ 0 ] );
                            if( ( index >= 0 ) && ( index < dcc_requests.size() ) )
                            {
                                DCCRequest request = (DCCRequest) dcc_requests.elementAt( index );
                                request.accept( preferred_nick );
                                problem = false;
                            }
                        }
                        catch( NumberFormatException e ) { }
                    }
                    
                    if( problem )
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_LIST_DCC_REQUESTS ]
                        );
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_ACCEPT_DCC_REQUEST ]
                            + " <dcc request index>"
                        );
                    }
                }
                break;
            case CMD_ACTION:
                {
                    String channel = display_manager.getSelectedChannel();
                    if( channel != null )
                    {
                        execute(
                            CMDS[ CMD_SEND_RAW ]
                            + " PRIVMSG "
                            + channel
                            + " :" + Character.toString( (char) 1 )
                            + "ACTION "
                            + arg_string
                            + Character.toString( (char) 1 )
                        );
                    }
                }
                break;
            case CMD_CHAR_BOLD:
                insertCharAtCaret( MIRC_BOLD_CONTROL_CHAR );
                break;
            case CMD_CHAR_COLOUR:
                insertCharAtCaret( MIRC_COLOUR_CONTROL_CHAR );
                break;
            case CMD_CHAR_ITALIC:
                insertCharAtCaret( MIRC_ITALIC_CONTROL_CHAR );
                break;
            case CMD_CHAR_NORMAL:
                insertCharAtCaret( MIRC_NORMAL_CONTROL_CHAR );
                break;
            case CMD_CHAR_UNDERLINE:
                insertCharAtCaret( MIRC_UNDERLINE_CONTROL_CHAR );
                break;
            case CMD_CLEAR_INPUT_FIELD:
                input_field.setText( null );
                break;
            case CMD_CLEAR_INPUT_HISTORY:
                input_history = new LinkedList();
                input_history_pointer = MOST_RECENT_ENTRY;
                break;
            case CMD_CLEAR_WINDOW:
                if( arg_string != null )
                {
                    try
                    {
                        int index = Integer.parseInt( args[ 0 ] );
                        display_manager.clearTextPane( index );
                    }
                    catch( NumberFormatException e )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_WINDOWS ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_CLEAR_WINDOW ]
                            + " [window id number]" );
                    }
                }
                else
                {
                    display_manager.clearTextPane( -1 );
                }
                break;
            case CMD_CLOSE_WINDOW:
                /*
                if( arg_string != null )
                {
                    try
                    {
                        int index = Integer.parseInt( args[ 0 ] );
                        display_manager.closeWindow( index );
                    }
                    catch( NumberFormatException e )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_WINDOWS ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_CLOSE_WINDOW ]
                            + " [window id number]" );
                    }
                }
                else
                {
                    display_manager.closeWindow( -1 );
                }
                 */
                break;
            case CMD_COMPLETE_NICK:
                {
                    String input_line = input_field.getText();
                    if( input_line.length() > 0 )
                    {
                        int caret_pos = input_field.getCaretPosition();
                        int left_space_pos;
                        if( caret_pos > 0 )
                        {
                            left_space_pos = input_line.lastIndexOf( " ", caret_pos - 1 );
                        }
                        else
                        {
                            left_space_pos = -1;
                        }
                        int right_space_pos = input_line.indexOf( " ", caret_pos );
                        String word = null;
                        if( right_space_pos > -1 )
                        {
                            word = input_line.substring( left_space_pos + 1, right_space_pos );
                        }
                        else
                        {
                            word = input_line.substring( left_space_pos + 1 );
                        }
                        
                        
                        if( ( word != null ) && ( word.length() > 0 ) )
                        {
                            String replacement_text = "";
                            
                            if( word.charAt( 0 ) == '/' )
                            {
                                // Command completion.
                                
                                String completion = Util.completeFrom(
                                    word.substring( 1 ),
                                    CMDS,
                                    display_manager
                                );
                                int error = Util.getLastErrorCode();
                                switch( error )
                                {
                                    case COMPLETE_NONE_FOUND:
                                        display_manager.printlnDebug(
                                            i18n_manager.getString( "no command" )
                                        );
                                        break;
                                    case COMPLETE_ONE_FOUND:
                                        replacement_text = "/" + completion + " ";
                                        break;
                                    case COMPLETE_MORE_THAN_ONE_FOUND:
                                        replacement_text = "/" + completion;
                                        break;
                                }
                            }
                            else if( current_rm instanceof Server )
                            {
                                Server s = (Server) current_rm;
                                
                                if( word.charAt( 0 ) == '#' )
                                {
                                    // Channel name completion
                                    
                                    Channel [] channels = s.getChannels();
                                    String [] channel_names = new String [ channels.length ];
                                    for( int i = 0; i < channels.length; i++ )
                                    {
                                        channel_names[ i ] = channels[ i ].getName();
                                    }
                                    
                                    String completion = Util.completeFrom(
                                        word, channel_names, display_manager
                                    );
                                    int error = Util.getLastErrorCode();
                                    switch( error )
                                    {
                                        case COMPLETE_NONE_FOUND:
                                            display_manager.printlnDebug(
                                                i18n_manager.getString( "no channel" )
                                            );
                                            break;
                                        case COMPLETE_ONE_FOUND:
                                        case COMPLETE_MORE_THAN_ONE_FOUND:
                                            replacement_text = completion;
                                            break;
                                    }
                                }
                                else
                                {
                                    // Nick completion

                                    Channel channel = s.getChannelByName( display_manager.getSelectedChannel() );
                                    if( channel != null )
                                    {
                                        replacement_text = channel.completeNick( word, (left_space_pos == -1) );
                                    }

                                    if( replacement_text.equals( word ) )
                                    {
                                        // If no nick found, search for conversation words.

                                        String [] words = new String[ 0 ];
                                        words = (String []) conversation_words.toArray( words );
                                        for( int i = 0; i < words.length; i++ )
                                        {
                                            if( words[ i ].toLowerCase().startsWith( word.toLowerCase() ) )
                                            {
                                                replacement_text = words[ i ];
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if( replacement_text != "" )
                            {
                                if( right_space_pos > -1 )
                                {
                                    input_line =
                                        input_line.substring( 0, left_space_pos + 1 )
                                        + replacement_text
                                        + input_line.substring( right_space_pos );
                                }
                                else
                                {
                                    input_line =
                                        input_line.substring( 0, left_space_pos + 1 )
                                        + replacement_text;
                                }
                                input_field.setText( input_line );
                            }
                        }
                    }
                }
                break;
            case CMD_CONNECT:
                {
                    Server s = null;
                    
                    if( arg_string != null )
                    {
                        RemoteMachine rm = getRemoteMachineByIDString( args[ 0 ] );
                        
                        if( rm instanceof Server )
                        {
                            s = (Server) rm;
                        }
                        else
                        {
                            display_manager.printlnDebug( i18n_manager.getString( "bad machine id", new Object[] { args[ 0 ] } ) );
                            display_manager.printlnDebug(
                                "Try /"
                                + CMDS[ CMD_LIST_CONNECTIONS ]
                            );
                        }
                    }
                    else
                    {
                        if( current_rm instanceof Server )
                        {
                            s = (Server) current_rm;
                        }
                        else
                        {
                            display_manager.printlnDebug(
                                i18n_manager.getString( "go to server window" )
                            );
                        }
                    }
                    
                    if( s != null )
                    {
                        s.connect( preferred_nick );
                    }
                }
                break;
            case CMD_DCC_CHAT:
            case CMD_DCC_SEND:
                if( args != null )
                {
                    if( current_rm instanceof Server )
                    {
                        Server s = (Server) current_rm;
                    
                        try
                        {
                            InetAddress addr = InetAddress.getLocalHost();
                            String addr_str = addr.getHostAddress();
                            if( addr_str != "127.0.0.1" )
                            {
                                try
                                {
                                    DCCConnection dcc = null;
                                    File f = null;
                                    switch( command_id )
                                    {
                                        case CMD_DCC_CHAT:
                                            dcc = new DCCConnection(
                                                this,
                                                settings_manager,
                                                display_manager,
                                                i18n_manager,
                                                DCC_CHAT,
                                                args[ 0 ],
                                                s.getCurrentNick(),
                                                null
                                            );
                                            break;
                                        case CMD_DCC_SEND:
                                        {
                                            if( args.length < 2 )
                                            {
                                                display_manager.printlnDebug(
                                                    "/"
                                                    + CMDS[ CMD_DCC_SEND ]
                                                    + " <nick> <file>" );
                                                break;
                                            }
                                            f = new File( Util.stringArrayToString( args, 1 ) );
                                            if( ! f.exists() )
                                            {
                                                display_manager.printlnDebug(
                                                    i18n_manager.getString(
                                                        "nonexistent file",
                                                        new Object [] { f.getPath() }
                                                    )
                                                );
                                                break;
                                            }
                                            
                                            BufferedInputStream bis = new BufferedInputStream(
                                                new FileInputStream( f ),
                                                settings_manager.getInt(
                                                    "/dcc/file transfers/packet size",
                                                    DEFAULT_PACKET_SIZE
                                                )
                                            );
                                            dcc = new DCCConnection(
                                                this,
                                                settings_manager,
                                                display_manager,
                                                i18n_manager,
                                                DCC_SEND,
                                                args[ 0 ],
                                                s.getCurrentNick(),
                                                bis
                                            );
                                            break;
                                        }
                                    }
                                    
                                    if( dcc != null )
                                    {
                                        dcc_offers.add( dcc );
                                        int port = dcc.listen();

                                        switch( command_id )
                                        {
                                            case CMD_DCC_CHAT:
                                                execute(
                                                    CMDS[ CMD_SEND_RAW ]
                                                    + " PRIVMSG "
                                                    + args[ 0 ]
                                                    + " :\001DCC CHAT chat "
                                                    + Util.get32BitAddressString( addr_str ) + " "
                                                    + Integer.toString( port )
                                                    + "\001"
                                                );
                                                break;
                                            case CMD_DCC_SEND:
                                                execute(
                                                    CMDS[ CMD_SEND_RAW ]
                                                    + " PRIVMSG "
                                                    + args[ 0 ]
                                                    + " :\001DCC SEND "
                                                    + f.getName() + " "
                                                    + Util.get32BitAddressString( addr_str ) + " "
                                                    + Integer.toString( port ) + " "
                                                    + Long.toString( f.length() )
                                                    + "\001"
                                                );
                                                break;
                                        }
                                    }
                                }
                                catch( IOException e )
                                {
                                    display_manager.printlnDebug(
                                        i18n_manager.getString( "dcc chat offer failed" )
                                    );
                                }
                            }
                            else
                            {
                                display_manager.printlnDebug(
                                    i18n_manager.getString( "unknown local ip" )
                                );
                            }
                        }
                        catch( UnknownHostException e )
                        {
                            Util.printException(
                                display_manager,
                                e,
                                i18n_manager.getString( "unknown local ip" )
                            );
                        }
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString( "go to server window" )
                        );
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        "/"
                        + CMDS[ command_id ]
                        + " <nick>" );
                }
                break;
            case CMD_DISABLE_COLOUR_CODES:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        GITextPane gitp = (GITextPane) gipw.getPane();
                        gitp.setPaintMIRCCodes( false );
                        display_manager.printlnDebug(
                            i18n_manager.getString(
                                "codes disabled",
                                new Object [] { gipw.getTitle() }
                            )
                        );
                    }
                }
                break;
            case CMD_DISCONNECT:
                {
                    RemoteMachine rm = null;
                    
                    if( arg_string != null )
                    {
                        rm = getRemoteMachineByIDString( args[ 0 ] );
                        
                        if( rm == null )
                        {
                            display_manager.printlnDebug(
                                i18n_manager.getString(
                                    "bad machine id",
                                    new Object [] { args[ 0 ] }
                                )
                            );
                            display_manager.printlnDebug(
                                "Try /"
                                + CMDS[ CMD_LIST_CONNECTIONS ]
                            );
                        }
                    }
                    else
                    {
                        if( current_rm == null )
                        {
                            display_manager.printlnDebug( i18n_manager.getString( "go to rm window" ) );
                        }
                        else
                        {
                            rm = current_rm;
                        }
                    }
                    
                    if( rm != null )
                    {
                        rm.close();
                        removeRemoteMachine( rm );
                        if( rm instanceof Server )
                        {
                            display_manager.closeWindows( rm.toString() + " and %raw and not %printed" );
                        }
                    }
                }
                break;
            case CMD_DOCK_PANE:
                if( ( args != null ) && ( args.length > 1 ) )
                {
                    int location = DOCK_NOWHERE;
                    int pane_index = -1;
                    int partner_index = DESKTOP_PANE_INDEX;
                    
                    for( int i = 0; i < DOCK_STR.length; i++ )
                    {
                        if( args[ 1 ].equals( DOCK_STR[ i ] ) )
                        {
                            location = i;
                            break;
                        }
                    }
                    
                    try
                    {
                        pane_index = Integer.parseInt( args[ 0 ] );
                        if( args.length > 2 )
                        {
                            partner_index = Integer.parseInt( args[ 2 ] );
                        }
                    }
                    catch( NumberFormatException e ) { }
                    
                    if( ( location != DOCK_NOWHERE ) && ( pane_index > -1 ) )
                    {
                        if( display_manager.dock( location, pane_index, partner_index ) )
                        {
                            display_manager.printlnDebug( i18n_manager.getString( "docked" ) );
                        }
                        else
                        {
                            display_manager.printlnDebug( i18n_manager.getString( "dock failed" ) );
                        }
                    }
                }
                else
                {
                    display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_PANES ] );
                    display_manager.printlnDebug(
                        "/"
                        + CMDS[ CMD_DOCK_PANE ]
                        + " <pane id number> <t|r|b|l> [partner pane]" );
                }
                break;
            case CMD_ENABLE_COLOUR_CODES:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        GITextPane gitp = (GITextPane) gipw.getPane();
                        gitp.setPaintMIRCCodes( true );
                        display_manager.printlnDebug(
                            i18n_manager.getString(
                                "codes enabled",
                                new Object [] { gipw.getTitle() }
                            )
                        );
                    }
                }
                break;
            case CMD_EXEC:
            case CMD_EXEC2:
            case CMD_EXEC_WITH_WINDOW:
                if( arg_string != null )
                {
                    try
                    {
                        GIProcess gip = new GIProcess(
                            display_manager, i18n_manager, processes, arg_string, this, command_id
                        );
                    }
                    catch( IOException e )
                    {
                        Util.printException(
                            display_manager, e,
                            i18n_manager.getString( "io exception 1" )
                        );
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        "/"
                        + CMDS[ command_id ]
                        + " <program to execute, with any arguments>"
                    );
                }
                break;
            case CMD_EXEC_PY_METHOD:
                if( python_script_interface != null )
                {
                    if( args.length > 0 )
                    {
                        python_script_interface.execMethod( args );
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_EXEC_PY_METHOD ]
                            + " <Python method name> [arguments]"
                        );
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString( "python not loaded" )
                    );
                }
                break;
            case CMD_EXEC_TCL:
                if( tcl_script_interface != null )
                {
                    if( arg_string != null )
                    {
                        tcl_script_interface.eval( arg_string );
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_EXEC_TCL ]
                            + " <Tcl code>"
                        );
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString( "tcl not loaded" )
                    );
                }
                break;
            case CMD_EXIT:
                {
                    String quit_message;
                    if( arg_string != null )
                    {
                        quit_message = arg_string;
                    }
                    else
                    {
                        quit_message = settings_manager.getString(
                            "/misc/quit message",
                            "http://geoirc.berlios.de"
                        );
                    }
                    Iterator it = remote_machines.iterator();
                    RemoteMachine rm;
                    while( it.hasNext() )
                    {
                        rm = (RemoteMachine) it.next();
                        rm.send( "QUIT :" + quit_message );
                    }
                    // Do we need a more graceful termination?  :)
                    System.exit( 0 );
                }
                break;
            case CMD_EXTENDED_PASTE:
                try
                {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable contents = clipboard.getContents( null );
                    String text = (String) contents.getTransferData( DataFlavor.stringFlavor );
                    
                    MultilinePaster mp = new MultilinePaster( this, text );
                    mp.start();
                }
                catch( UnsupportedFlavorException e ) { }
                catch( IOException e ) { }
                break;
            case CMD_FOCUS_ON_INPUT_FIELD:
                input_field.grabFocus();
                break;
            case CMD_HELP:
                {
                    display_manager.printlnDebug( i18n_manager.getString( "commands" ) );
                    
                    String [] sa = (String []) CMDS.clone();
                    Arrays.sort( sa );
                    for( int i = 0; i < sa.length; i++ )
                    {
                        display_manager.printlnDebug( sa[ i ] );
                    }
                    
                    display_manager.printlnDebug( i18n_manager.getString( "aliases" ) );
                    
                    sa = alias_manager.getAliases();
                    Arrays.sort( sa );
                    for( int i = 0; i < sa.length; i++ )
                    {
                        display_manager.printlnDebug( sa[ i ] );
                    }
                }
                break;
            case CMD_HIDE_QUALITIES:
                display_manager.setShowQualities( false );
                break;
            case CMD_JOIN:
                if( args != null )
                {
                    if( current_rm instanceof Server )
                    {
                        Server s = (Server) current_rm;
                        GIWindow window = display_manager.addChannelWindow( s, args[ 0 ] );
                        if( window != null )
                        {
                            execute( CMDS[ CMD_SEND_RAW ] + " " + command );
                            result = CommandExecutor.EXEC_SUCCESS;
                        }
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString( "go to server window" )
                        );
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        "/" + CMDS[ CMD_JOIN ]
                        + " <channel to join>"
                    );
                }
                break;
            case CMD_KILL_PROCESS:
                {
                    boolean problem = true;
                    
                    if( args != null )
                    {
                        Integer pid = null;
                        try
                        {
                            pid = new Integer( Integer.parseInt( args[ 0 ] ) );
                            GIProcess gip = (GIProcess) processes.get( pid );
                            gip.destroy();
                            problem = false;
                        }
                        catch( NumberFormatException e ) { }
                    }
                    
                    if( problem )
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_LIST_PROCESSES ]
                        );
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_KILL_PROCESS ]
                            + " <GeoIRC-internal process id>"
                        );
                    }
                }
                break;
            case CMD_LIST_CHANNELS:
                {
                    if( current_rm instanceof Server )
                    {
                        Server s = (Server) current_rm;
                        Channel [] channels = s.getChannels();
                        for( int i = 0; i < channels.length; i++ )
                        {
                            display_manager.printlnDebug( channels[ i ].toString() );
                        }
                    }
                }
                break;
            case CMD_LIST_CONNECTIONS:
                {
                    Iterator it = remote_machines.iterator();
                    RemoteMachine rm;
                    String current_marker;
                    int i = 0;
                    while( it.hasNext() )
                    {
                        rm = (RemoteMachine) it.next();
                        if( rm == current_rm )
                        {
                            current_marker = " ("
                                + i18n_manager.getString( "current rm" )
                                + ")";
                        }
                        else
                        {
                            current_marker = "";
                        }
                        display_manager.printlnDebug(
                            Integer.toString( i ) + ": "
                            + rm.toString() + current_marker
                        );
                        i++;
                    }
                }
                break;
            case CMD_LIST_DCC_OFFERS:
                {
                    DCCConnection offer;
                    for( int i = 0, n = dcc_offers.size(); i < n; i++ )
                    {
                        offer = (DCCConnection) dcc_offers.elementAt( i );
                        display_manager.printlnDebug(
                            Integer.toString( i ) + ": "
                            + offer.toString()
                        );
                    }
                }
                break;
            case CMD_LIST_DCC_REQUESTS:
                {
                    DCCRequest request;
                    for( int i = 0, n = dcc_requests.size(); i < n; i++ )
                    {
                        request = (DCCRequest) dcc_requests.elementAt( i );
                        display_manager.printlnDebug(
                            Integer.toString( i ) + ": "
                            + request.toString()
                        );
                    }
                }
                break;
            case CMD_LIST_DOCKED_WINDOWS:
                display_manager.listDockedPanes();
                break;
            case CMD_LIST_FONTS:
                GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Font [] fonts = genv.getAllFonts();
                for( int i = 0; i < fonts.length; i++ )
                {
                    display_manager.printlnDebug( fonts[ i ].getName() + " -- " + fonts[ i ].getFontName() );
                }
                break;
            case CMD_LIST_LOGS:
                log_manager.listLogs();
                break;
            case CMD_LIST_MEMBERS:
                if( current_rm instanceof Server )
                {
                    Server s = (Server) current_rm;
                    Channel channel = s.getChannelByName( display_manager.getSelectedChannel() );
                    if( channel != null )
                    {
                        User [] members = channel.getMembers();
                        for( int i = 0; i < members.length; i++ )
                        {
                            display_manager.printlnDebug(
                                Integer.toString( i ) + ": "
                                + members[ i ].getNick()
                            );
                        }
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString( "go to server window" )
                    );
                }
                break;
            case CMD_LIST_PANES:
                display_manager.listPanes();
                break;
            case CMD_LIST_PROCESSES:
                {
                    GIProcess gip;
                    Integer pid;
                    for( Enumeration keys = processes.keys(); keys.hasMoreElements(); )
                    {
                        pid = (Integer) keys.nextElement();
                        gip = (GIProcess) processes.get( pid );
                        display_manager.printlnDebug(
                            gip.toString()
                        );
                    }
                }
                break;
            case CMD_LIST_WINDOWS:
                display_manager.listWindows();
                break;
            case CMD_LOAD_PY:
                if( python_script_interface != null )
                {
                    if( arg_string != null )
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString( "loading", new Object [] { arg_string } )
                        );
                        python_script_interface.evalFile( arg_string );
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_LOAD_PY ]
                            + " <Python script filename>"
                        );
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString( "python not loaded" )
                    );
                }
                break;
            case CMD_LOAD_TCL:
                if( tcl_script_interface != null )
                {
                    if( arg_string != null )
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString( "loading", new Object [] { arg_string } )
                        );
                        tcl_script_interface.evalFile( arg_string );
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_LOAD_TCL ]
                            + " <Tcl script filename>"
                        );
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString( "tcl not loaded" )
                    );
                }
                break;
            case CMD_LOG:
                if( arg_string != null )
                {
                    int index1 = arg_string.indexOf( COMMAND_ARGUMENT_SEPARATOR_CHAR );
                    int index2 = arg_string.lastIndexOf( COMMAND_ARGUMENT_SEPARATOR_CHAR );
                    
                    String filename = null;
                    String filter = "";
                    String regexp = ".*";
                    
                    if( index1 > -1 )
                    {
                        filename = arg_string.substring( 0, index1 );
                        try
                        {
                            if( index2 != index1 )
                            {
                                // Regexp specified.
                                filter = arg_string.substring( index1 + 1, index2 );
                                regexp = arg_string.substring( index2 + 1 );
                            }
                            else
                            {
                                // No regexp specified.
                                filter = arg_string.substring( index1 + 1 );
                            }
                        }
                        catch( StringIndexOutOfBoundsException e ) { }
                    }
                    else
                    {
                        filename = arg_string;
                        
                        // Assign the filter of the current window, if any.
                        // No regexp specified.
                        
                        GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                        if( gipw != null )
                        {
                            GITextPane gitp = (GITextPane) gipw.getPane();
                            filter = gitp.getFilter();
                        }
                    }
                    
                    log_manager.addLogger( filter, regexp, filename );
                }
                else
                {
                    display_manager.printlnDebug(
                        "/" + CMDS[ CMD_LOG ] +
                        " <filename>[;filter[;regexp]]"
                    );
                }
                break;
            case CMD_MAXIMIZE_WINDOW:
                if( arg_string != null )
                {
                    try
                    {
                        int index = Integer.parseInt( args[ 0 ] );
                        display_manager.maximizeWindow( index );
                    }
                    catch( NumberFormatException e )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_WINDOWS ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_MAXIMIZE_WINDOW ]
                            + " [window id number]" );
                    }
                }
                else
                {
                    display_manager.maximizeWindow( -1 );
                }
                break;
            case CMD_MINIMIZE_WINDOW:
                if( arg_string != null )
                {
                    try
                    {
                        int index = Integer.parseInt( args[ 0 ] );
                        display_manager.minimizeWindow( index );
                    }
                    catch( NumberFormatException e )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_WINDOWS ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_MINIMIZE_WINDOW ]
                            + " [window id number]" );
                    }
                }
                else
                {
                    display_manager.minimizeWindow( -1 );
                }
                break;
            case CMD_NEW_INFO_WINDOW:
                {
                    String path = arg_string;
                    if( path == null )
                    {
                        path = "/";
                    }
                    GIWindow window = display_manager.addInfoWindow( "Info", path );
                    if( window != null )
                    {
                        info_manager.activateInfoPanes( path );
                        
                        result = CommandExecutor.EXEC_SUCCESS;
                    }
                }
                break;
            case CMD_NEW_SERVER:
            case CMD_SERVER:
                if( args != null )
                {
                    String host = args[ 0 ];
                    
                    int i_port = 6667;
                    String port = "6667";
                    if( args.length > 1 )
                    {
                        try
                        {
                            i_port = Integer.parseInt( args[ 1 ] );
                            port = args[ 1 ];
                        }
                        catch( NumberFormatException e )
                        {
                            // Ignore exception and use default port.
                        }
                    }
                    Server s = addServer( host, port );
                    display_manager.addTextWindow(
                        s.toString() + " and %raw and not %printed",
                        s.toString() + " and %raw and not %printed"
                    );
                    s.connect( preferred_nick );
                }
                else
                {
                    display_manager.printlnDebug(
                        "/"
                        + CMDS[ CMD_NEW_SERVER ]
                        + " <host> [port]" );
                }
                break;
            case CMD_NEW_TEXT_WINDOW:
            case CMD_NEW_WINDOW:
                if( args != null )
                {
                    GIWindow window = display_manager.addTextWindow( arg_string, arg_string );
                    if( window != null )
                    {
                        result = CommandExecutor.EXEC_SUCCESS;
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        "/" + CMDS[ CMD_NEW_WINDOW ] + " <filter>"
                    );
                }
                break;
            case CMD_NEXT_HISTORY_ENTRY:
                if( input_history_pointer > MOST_RECENT_ENTRY )
                {
                    input_history_pointer--;
                    input_field.setText( (String) input_history.get( input_history_pointer ) );
                }
                break;
            case CMD_NEXT_PANE:
                display_manager.switchToNextPane( NEXT_PANE );
                break;
            case CMD_NICK:
                if( args != null )
                {
                    execute(
                        CMDS[ CMD_SEND_RAW ]
                        + " NICK "
                        + args[ 0 ]
                    );
                }
                else
                {
                    display_manager.printlnDebug(
                        "/"
                        + CMDS[ CMD_NICK ]
                        + " <new nickname>" );
                }
                break;
            case CMD_NUDGE_DOWN:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        ((GITextPane) gipw.getPane()).nudgeDown();
                    }
                }
                break;
            case CMD_NUDGE_UP:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        ((GITextPane) gipw.getPane()).nudgeUp();
                    }
                }
                break;
            case CMD_OPEN_SETTINGS:
                new geoirc.conf.SettingsDialog( settings_manager, display_manager ).setVisible( true );
                break;
            case CMD_PAGE_DOWN:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        ((GITextPane) gipw.getPane()).pageDown();
                    }
                }
                break;
            case CMD_PAGE_UP:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        ((GITextPane) gipw.getPane()).pageUp();
                    }
                }
                break;
            case CMD_PART:
                if( current_rm instanceof Server )
                {
                    String channels = display_manager.getSelectedChannel();
                    String part_message = null;

                    if( args != null )
                    {
                        if( args[ 0 ].charAt( 0 ) == '#' )
                        {
                            channels = args[ 0 ];
                            if( args.length > 1 )
                            {
                                part_message = Util.stringArrayToString( args, 1 );
                            }
                        }
                        else
                        {
                            part_message = arg_string;
                        }
                    }

                    Server s = (Server) current_rm;
                    execute(
                        CMDS[ CMD_SEND_RAW ] + " PART " + channels
                        + (
                            ( part_message != null )
                            ? ( " :" + part_message )
                            : ""
                        )
                    );
                    result = CommandExecutor.EXEC_SUCCESS;
                }
                else
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString( "go to server window" )
                    );
                }
                break;
            case CMD_PLAY:
                if( args != null )
                {
                    String sound_file = arg_string;
                    java.net.URL url = null;
                    AudioClip clip = null;
                    
                    if( audio_clips.contains( sound_file ) )
                    {
                        clip = (AudioClip) audio_clips.get( sound_file );
                    }
                    else
                    {
                        try
                        {
                            url = new File( sound_file ).toURL(); 
                            clip = java.applet.Applet.newAudioClip( url );
                            audio_clips.put( sound_file, clip );
                        }
                        catch ( java.net.MalformedURLException e )
                        {
                            display_manager.printlnDebug( e.getMessage() );
                            display_manager.printlnDebug(
                                i18n_manager.getString(
                                    "audio load failure",
                                    new Object [] { sound_file }
                                )
                            );
                        }
                    }
                    
                    if( clip != null )
                    {
                        clip.play();
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString(
                                "audio load failure",
                                new Object [] { sound_file }
                            )
                        );
                    }
                    
                }
                break;
            case CMD_POSITION_WINDOW:
                {
                    boolean problem = true;
                    if( args != null )
                    {
                        try
                        {
                            if( args.length >= 3 )
                            {
                                // Window index specified.

                                display_manager.positionWindow(
                                    Integer.parseInt( args[ 0 ] ),
                                    Integer.parseInt( args[ 1 ] ),
                                    Integer.parseInt( args[ 2 ] )
                                );
                                problem = false;
                            }
                            else if( args.length == 2 )
                            {
                                // No window index specified; assume current window.
                                
                                display_manager.positionWindow(
                                    -1,
                                    Integer.parseInt( args[ 0 ] ),
                                    Integer.parseInt( args[ 1 ] )
                                );
                                problem = false;
                            }
                        } catch( NumberFormatException e ) { }
                    }
                    
                    if( problem )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_WINDOWS ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_POSITION_WINDOW ]
                            + " [window id number] <width in pixels> <height in pixels>" );
                    }
                }
                break;
            case CMD_PREVIOUS_HISTORY_ENTRY:
                if(
                    ( input_history_pointer < input_history.size() - 1 )
                    || ( input_history.size() == 1 )
                )
                {
                    String input_text = input_field.getText();
                    if( input_saved )
                    {
                        if( input_history_pointer == MOST_RECENT_ENTRY )
                        {
                            input_history.set( input_history_pointer, input_text );
                        }
                    }
                    else
                    {
                        addToInputHistory( input_text );
                        input_saved = true;
                    }
                    input_history_pointer++;
                    input_field.setText( (String) input_history.get( input_history_pointer ) );
                }
                break;
            case CMD_PREVIOUS_PANE:
                display_manager.switchToNextPane( PREVIOUS_PANE );
                break;
            case CMD_PRINT:
                if( arg_string != null )
                {
                    int index = arg_string.indexOf( COMMAND_ARGUMENT_SEPARATOR_CHAR );
                    String text = arg_string;
                    String qualities = FILTER_SPECIAL_CHAR + "debug";
                    if( ( index > -1 ) && ( index < arg_string.length() - 1 ) )
                    {
                        text = arg_string.substring( index + 1 );
                        qualities = arg_string.substring( 0, index );
                    }
                    display_manager.println( text, qualities );
                }
                else
                {
                    display_manager.printlnDebug(
                        "/" + CMDS[ CMD_PRINT ]
                        + " [qualities;]<text>"
                    );
                }
                break;
            case CMD_PRINT_ACTIVE:
                if( arg_string != null )
                {
                    display_manager.printlnToActiveTextPane( arg_string );
                }
                break;
            case CMD_PRINT_DEBUG:
                if( arg_string != null )
                {
                    display_manager.printlnDebug( arg_string );
                }
                break;
            case CMD_PRIVMSG:
            case CMD_MSG:
                if( ( args != null ) || ( args.length < 2 ) )
                {
                    String dest = args[ 0 ];
                    String message = Util.stringArrayToString( args, 1 );
                    execute(
                        CMDS[ CMD_SEND_RAW ]
                        + " PRIVMSG "
                        + dest
                        + " :"
                        + message
                    );
                }
                else
                {
                    display_manager.printlnDebug(
                        "/"
                        + CMDS[ CMD_PRIVMSG ]
                        + " <nick/channel> <message>" );
                }
                break;
            case CMD_QUIT:
                if( current_rm instanceof Server )
                {
                    String quit_message = arg_string;

                    Server s = (Server) current_rm;
                    execute(
                        CMDS[ CMD_SEND_RAW ] + " QUIT "
                        + (
                            ( quit_message != null )
                            ? ( " :" + quit_message )
                            : ""
                        )
                    );
                    result = CommandExecutor.EXEC_SUCCESS;
                }
                else
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString( "go to server window" )
                    );
                }
                break;
            case CMD_REJECT_DCC_REQUEST:
                {
                    boolean problem = true;

                    if( args != null )
                    {
                        try
                        {
                            int index = Integer.parseInt( args[ 0 ] );
                            dcc_requests.remove( index );
                        }
                        catch( NumberFormatException e ) { }
                    }

                    if( problem )
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_LIST_DCC_REQUESTS ]
                        );
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_REJECT_DCC_REQUEST ]
                            + " <dcc request index>"
                        );
                    }
                }
                break;
            case CMD_REMOVE_LOG:
                {
                    boolean problem = true;
                    
                    if( arg_string != null )
                    {
                        int index;
                        try
                        {
                            index = Integer.parseInt( arg_string );
                            log_manager.removeLogger( index );
                            problem = false;
                        }
                        catch( NumberFormatException e ) { }
                    }
                    
                    if( problem )
                    {
                        display_manager.printlnDebug(
                            "/" + CMDS[ CMD_REMOVE_LOG ]
                            + " <index of log to remove>"
                        );
                    }
                }
                break;
            case CMD_RESET_SCRIPT_ENVIRONMENT:
                initializeScriptingInterfaces();
                break;
            case CMD_RESTORE_WINDOW:
                if( arg_string != null )
                {
                    try
                    {
                        int index = Integer.parseInt( args[ 0 ] );
                        display_manager.restoreWindow( index );
                    }
                    catch( NumberFormatException e )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_WINDOWS ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_RESTORE_WINDOW ]
                            + " [window id number]" );
                    }
                }
                else
                {
                    display_manager.restoreWindow( -1 );
                }
                break;
            case CMD_SEND_RAW:
            case CMD_QUOTE:
            case CMD_RAW:
                if( ( args != null ) && ( ! args.equals( "" ) ) )
                {
                    if( current_rm != null )
                    {
                        current_rm.send( arg_string );

                        if( current_rm instanceof Server )
                        {
                            Server s = (Server) current_rm;
                            User u = s.getUserObject();
                            if( u != null )
                            {
                                u.noteActivity();
                            }
                            if( args[ 0 ].toUpperCase().equals( IRCMSGS[ IRCMSG_PRIVMSG ] ) )
                            {
                                if( u != null )
                                {
                                    s.noteActivity( args[ 1 ], u );
                                }
                                
                                // Create a query window, if not already existent.
                                
                                if( args[ 1 ].charAt( 0 ) != '#' )
                                {
                                    String nick = args[ 1 ];
                                    String query_window_title = Util.getQueryWindowFilter( nick );

                                    if(
                                        display_manager.getTextPaneByTitle(
                                            query_window_title
                                        ) == null
                                    )
                                    {
                                        display_manager.addTextWindow(
                                            query_window_title, query_window_title
                                        );
                                    }
                                }

                                String text = Util.stringArrayToString( args, 2 ).substring( 1 );
                                String qualities =
                                    s.toString() + " " + args[ 1 ]
                                    + " from=" + FILTER_SPECIAL_CHAR + "self"
                                    + " " + FILTER_SPECIAL_CHAR + "privmsg";
                                
                                if(
                                    ( text.length() > 0 )
                                    && ( text.charAt( 0 ) == (char) 1 )
                                    && ( text.substring( 1, 7 ).equals( "ACTION" ) )
                                )
                                {
                                    text = s.getPadded( "* " + s.getCurrentNick() ) + text.substring( 7, text.length() - 1 );
                                    qualities += " " + FILTER_SPECIAL_CHAR + "action "
                                        + FILTER_SPECIAL_CHAR + "ctcp";
                                }
                                else
                                {
                                    text = s.getPadded( "<" + s.getCurrentNick() + ">" ) + " " + text;
                                }
                                
                                display_manager.println(
                                    getATimeStamp(
                                        settings_manager.getString(
                                            "/gui/format/timestamp", ""
                                        )
                                    )
                                    + text,
                                    qualities
                                );
                            }
                        }

                        result = CommandExecutor.EXEC_SUCCESS;
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString( "go to server window" )
                        );
                    }
                }
                break;
            case CMD_SET:
                // TODO: Do some validity checking.
                {
                    String path;
                    String value;
                    int eq_index = arg_string.indexOf( "=" );
                    if( ( eq_index > 0 ) && ( eq_index < arg_string.length() ) )
                    {
                        path = arg_string.substring( 0, eq_index ).trim();
                        value = arg_string.substring( eq_index + 1 );
                    }
                }
                break;
            case CMD_SET_FILTER:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        ((GITextPane) gipw.getPane()).setFilter( arg_string );
                    }
                }
                break;
            case CMD_SET_TITLE:
                {
                    GIPaneWrapper gipw = (GIPaneWrapper) display_manager.getSelectedTextPane();
                    if( gipw != null )
                    {
                        ((GITextPane) gipw.getPane()).setTitle( arg_string );
                    }
                }
                break;
            case CMD_SIZE_WINDOW:
                {
                    boolean problem = true;
                    if( args != null )
                    {
                        try
                        {
                            if( args.length >= 3 )
                            {
                                // Window index specified.

                                display_manager.sizeWindow(
                                    Integer.parseInt( args[ 0 ] ),
                                    Integer.parseInt( args[ 1 ] ),
                                    Integer.parseInt( args[ 2 ] )
                                );
                                problem = false;
                            }
                            else if( args.length == 2 )
                            {
                                // No window index specified; assume current window.
                                
                                display_manager.sizeWindow(
                                    -1,
                                    Integer.parseInt( args[ 0 ] ),
                                    Integer.parseInt( args[ 1 ] )
                                );
                                problem = false;
                            }
                        } catch( NumberFormatException e ) { }
                    }
                    
                    if( problem )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_WINDOWS ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_SIZE_WINDOW ]
                            + " [window id number] <width in pixels> <height in pixels>" );
                    }
                }
                break;
            case CMD_SHOW_QUALITIES:
                {
                    boolean setting = true;
                    if( args != null )
                    {
                        setting = ( ! args[ 0 ].equals( "0" ) );
                    }
                    display_manager.setShowQualities( setting );
                }
                break;
            case CMD_SWITCH_WINDOW:
            case CMD_CHANGE_WINDOW:
                if( ( args != null ) && ( ! args.equals( "" ) ) )
                {
                    display_manager.switchToWindow( arg_string );
                }
                else
                {
                    display_manager.printlnDebug(
                        "/"
                        + CMDS[ CMD_SWITCH_WINDOW ]
                        + " <title regexp>" );
                }
                break;
            case CMD_TEST:
                // For testing/debugging purposes.
                pack();
                break;
            case CMD_TOPIC:
                {
                    String channel = display_manager.getSelectedChannel();
                    if( channel != null )
                    {
                        execute(
                            CMDS[ CMD_SEND_RAW ]
                            + " TOPIC "
                            + channel
                            + (
                                ( arg_string != null )
                                ? " :" + arg_string
                                : ""
                            )
                        );
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString( "go to channel window" )
                        );
                    }
                }
                break;
            case CMD_UNDOCK_PANE:
            case CMD_FLOAT_PANE:
                {
                    boolean problem = true;
                    
                    if( args != null )
                    {
                        int index;
                        try
                        {
                            index = Integer.parseInt( args[ 0 ] );
                            display_manager.undock( index );
                            problem = false;
                        } catch( NumberFormatException e ) { }
                    }
                    
                    if( problem )
                    {
                        display_manager.printlnDebug( "/" + CMDS[ CMD_LIST_PANES ] );
                        display_manager.printlnDebug(
                            "/"
                            + CMDS[ CMD_UNDOCK_PANE ]
                            + " <docked pane index>" );
                    }
                }
                break;
            
            case UNKNOWN_COMMAND:
            default:
                display_manager.printlnDebug(
                    i18n_manager.getString(
                        "bad command",
                        new Object [] { command_name }
                    )
                );
                result = CommandExecutor.EXEC_BAD_COMMAND;
                break;
        }

        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main( String args[] )
    {
        String settings_filepath = null;
        
        try
        {
            for( int i = 0; i < args.length; i++ )
            {
                if(
                    ( args[ i ].equals( "-c" ) )
                    || ( args[ i ].equals( "--config" ) )
                )
                {
                    i++;
                    if( i == args.length )
                    {
                        throw new BadArgumentsException( "settings filepath expected" );
                    }

                    settings_filepath = args[ i ];
                }
            }
        }
        catch( BadArgumentsException e )
        {
            System.err.println( e.getMessage() );
            System.out.println( "Usage:" );
            System.out.println( "java -classpath skinlf.jar;. geoirc.GeoIRC [options]" );
            System.out.println( "\t-c, --config\t<settings filepath>" );
        }
        
        GeoIRC geoirc = new GeoIRC( settings_filepath );
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField input_field;
    private javax.swing.JMenuBar menu_bar;
    // End of variables declaration//GEN-END:variables
    
}
