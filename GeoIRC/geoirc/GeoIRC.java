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

import com.l2fprod.gui.plaf.skin.*;
import geoirc.util.Util;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.text.*;
import org.jscroll.*;

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
        FocusListener
{
    protected static final int MAX_HISTORY_SIZE = 30;
    protected static final int MOST_RECENT_ENTRY = 0;
    
    protected Vector remote_machines;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected SoundManager sound_manager;
    protected AliasManager alias_manager;
    protected InfoManager info_manager;
    
    protected IdentServer ident_server;
    
    protected boolean listening_to_servers;

    protected LinkedList input_history;
    protected int input_history_pointer;
    protected boolean input_saved;
    
    protected InputMap input_map;
    protected ActionMap action_map;
    
    protected String preferred_nick;
    protected RemoteMachine current_remote_machine;

    /* **************************************************************** */
    
    public GeoIRC()
    {
        this( DEFAULT_SETTINGS_FILEPATH );
    }
    
    public GeoIRC( String settings_filepath )
    {
        System.out.println(
            "GeoIRC " + GEOIRC_VERSION
        );
        System.out.println(
            "Copyright (C) 2003 Alex Reyes (\"Pistos\")"
        );
        System.out.println(
            "This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version."
        );
        System.out.println(
            "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details."
        );
        
        
        listening_to_servers = false;
        
        // Settings.
        
        settings_manager = new SettingsManager(
            display_manager,
            ( settings_filepath == null ) ? DEFAULT_SETTINGS_FILEPATH : settings_filepath
        );
        settings_manager.loadSettingsFromXML();
        
        // Apply skin, if any specified.
        
        String skin1 = settings_manager.getString( "/gui/skin1", null );
        String skin2 = settings_manager.getString( "/gui/skin2", null );
        String skin_errors = "";
        
        try
        {
            Skin skin = null;

            if( ( skin1 != null ) && ( skin2 != null ) )
            {
                skin = new CompoundSkin(
                    SkinLookAndFeel.loadSkin( skin1 ),
                    SkinLookAndFeel.loadSkin( skin2 )
                );
            }
            else if( skin1 != null )
            {
                skin = SkinLookAndFeel.loadSkin( skin1 );
                skin_errors += "(No second skin specified.)\n";
            }
            else
            {
                skin_errors += "(No skins specified.)\n";
            }

            if( skin != null )
            {
                SkinLookAndFeel.setSkin( skin );
                UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
                UIManager.setLookAndFeel( new SkinLookAndFeel() );
                skin_errors += "Skin applied.\n";
            }
            else
            {
                skin_errors += "No skin applied.\n";
            }
        }
        catch( Exception e )
        {
            skin_errors += "Failed to apply skin.\n";
            if( skin1 != null ) { skin_errors += "(" + skin1 + ")\n"; }
            if( skin2 != null ) { skin_errors += "(" + skin2 + ")\n"; }
            skin_errors += e.getMessage() + "\n";
        }

        // Setup GUI.

        initComponents();

        input_field.grabFocus();
        input_field.addActionListener( this );
        input_field.setFont( new Font(
            settings_manager.getString( "/gui/input field/font face", "Lucida Console" ),
            Font.PLAIN,
            settings_manager.getInt( "/gui/input field/font size", 14 )
        ) );
        input_field.addFocusListener( this );
        
        // Un-map the Tab-related default mappings which have to do with focus traversal.
        input_field.setFocusTraversalKeysEnabled( false );
        
        input_map = input_field.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        action_map = input_field.getActionMap();
        
        input_history = new LinkedList();
        input_history_pointer = MOST_RECENT_ENTRY;
        input_saved = false;
        
        setFocusable( false );
        if( settings_manager.getBoolean( "/gui/start maximized", true ) == true )
        {
            setExtendedState( MAXIMIZED_BOTH );
        }
        
        this.setTitle( "GeoIRC" );
        
        display_manager = new DisplayManager(
            getContentPane(), menu_bar, settings_manager, input_field
        );
        display_manager.printlnDebug( skin_errors );
        
        info_manager = new InfoManager( settings_manager, display_manager );
        
        // Read settings.
        
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
        
        // Sound
        
        sound_manager = new SoundManager( settings_manager, display_manager );
        
        // Command aliases.
        
        alias_manager = new AliasManager( settings_manager, display_manager );
        
        // Ident server.
        
        ident_server = new IdentServer( settings_manager, display_manager );
        ident_server.start();
                
        // Restore connections, if any.
        
        current_remote_machine = null;
        remote_machines = new Vector();
        restoreConnections();
        if( ( current_remote_machine == null ) && ( remote_machines.size() > 0 ) )
        {
            current_remote_machine = (RemoteMachine) remote_machines.elementAt( 0 );
        }
        
        // Final miscellaneous initialization
        
        settings_manager.listenToPreferences();
        display_manager.beginListening();
        listening_to_servers = true;
        
        display_manager.printlnDebug(
            "GeoIRC " + GEOIRC_VERSION
        );
        display_manager.printlnDebug(
            "Copyright (C) 2003 Alex Reyes (\"Pistos\")"
        );
        display_manager.printlnDebug(
            "This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version."
        );
        display_manager.printlnDebug(
            "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details."
        );
        display_manager.printlnDebug(
            "----------------------\n"
        );
        
        // Open the curtains!

        show();
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
        
        //display_manager.printlnDebug( stroke_text );
        
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
    
    // Returns the Server created.
    protected Server addServer( String hostname, String port )
    {
        Server s = new Server( this, display_manager, settings_manager, sound_manager, info_manager, hostname, port );
        remote_machines.add( s );
        if( listening_to_servers )
        {
            recordConnections();
        }
        current_remote_machine = s;
        
        return s;
    }
    
    protected void removeServer( Server s )
    {
        remote_machines.remove( s );
        info_manager.removeRemoteMachine( s );
        if( current_remote_machine == s )
        {
            if( remote_machines.size() > 0 )
            {
                current_remote_machine = (RemoteMachine) remote_machines.elementAt( 0 );
            }
            else
            {
                current_remote_machine = null;
            }
        }
    }
    
    protected void recordConnections()
    {
        settings_manager.removeNode( "/connections/" );
        int n = remote_machines.size();
        RemoteMachine rm;
        String i_str;
        
        for( int i = 0; i < n; i++ )
        {
            i_str = Integer.toString( i );
            rm = (RemoteMachine) remote_machines.elementAt( i );
            
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
                if( s.connect( preferred_nick ) )
                {
                    info_manager.addRemoteMachine( s );
                }
            }
            else
            {
                // Huh?  Unrecognized RemoteMachine type.
                display_manager.printlnDebug( "Unknown remote machine type in settings." );
            }
            
            i++;
        }
    }
    
    public String getRemoteMachineID( RemoteMachine rm )
    {
        int id = remote_machines.indexOf( rm );
        String retval = null;
        if( id > -1 )
        {
            retval = Integer.toString( id );
        }
        
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
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        input_field = new javax.swing.JTextField();
        menu_bar = new javax.swing.JMenuBar();
        file_menu = new javax.swing.JMenu();

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        getContentPane().add(input_field, java.awt.BorderLayout.SOUTH);

        file_menu.setText("File");
        menu_bar.add(file_menu);

        setJMenuBar(menu_bar);

        pack();
    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    protected void addToInputHistory( String text )
    {
        input_history.addFirst( text );
        if( input_history.size() > MAX_HISTORY_SIZE )
        {
            input_history.removeLast();
        }
    }

    /* *********************************************************************
     *
     * Listener Implementations
     *
     * ********************************************************************* */
    
    // When the user presses enter in the text field, this method is called.
    public void actionPerformed( ActionEvent event )
    {
        String text = input_field.getText();
        if( ( text == null ) || ( text.equals( "" ) ) )
        {
            return;
        }

        if( input_saved )
        {
            input_history.set( input_history_pointer, text );
        }
        else
        {
            addToInputHistory( text );
        }
        input_history_pointer = MOST_RECENT_ENTRY;
        input_saved = false;
        
        /* What we do with this input depends on its nature.
         * 
         * If the text starts with a backslash, we send it (without the
         * backslash) to the server as-is (raw).
         *
         * If the text starts with a slash, we must interpret it as a command
         * alias.
         *
         * All other text is sent to the current channel or chat, if any.
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
        else
        {
            String channel = display_manager.getSelectedChannel();
            if( channel != null )
            {
                execute(
                    CMDS[ CMD_SEND_RAW ]
                    + " privmsg "
                    + channel + " :"
                    + text
                );
            }
        }
        
        // Clear the input field.
        input_field.setText( "" );
        
    }    

    public void focusGained( FocusEvent e ) { }
    public void focusLost( FocusEvent e )
    {
        Component thief = e.getOppositeComponent();
        if(
            ( thief instanceof com.l2fprod.gui.plaf.skin.SkinWindowButton )
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
        else if( thief != null )
        {
            display_manager.printlnDebug( "Focus stolen by: " 
            + thief.getClass().toString() );
        }
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
        
        int result = CommandExecutor.EXEC_GENERAL_FAILURE;
        int space_index = command.indexOf( " " );
        String command_name;
        String arg_string;
        if( space_index > -1 )
        {
            command_name = command.substring( 0, space_index ).toLowerCase();
            arg_string = command.substring( space_index + 1 );
        }
        else
        {
            command_name = command.toLowerCase();
            arg_string = null;
        }
        String [] args = Util.tokensToArray( arg_string );
        
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
            case CMD_CHANGE_SERVER:
                {
                    int id = -1;
                    boolean problem = false;
                    try
                    {
                        id = Integer.parseInt( args[ 0 ] );
                        if( ( id < 0 ) || ( id >= remote_machines.size() ) )
                        {
                            problem = true;
                        }
                        else
                        {
                            current_remote_machine = (RemoteMachine) remote_machines.elementAt( id );
                        }
                    }
                    catch( NumberFormatException e )
                    {
                        problem = true;
                    }
                    
                    if( problem )
                    {
                        display_manager.printlnDebug( "Invalid server id: '" + args[ 0 ] + "'" );
                        display_manager.printlnDebug( "Try /listservers" );
                    }
                }
                break;
            case CMD_COMPLETE_NICK:
                if( current_remote_machine instanceof Server )
                {
                    String input_line = input_field.getText();
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
                    
                    if( word != null )
                    {
                        Server s = (Server) current_remote_machine;
                        Channel channel = s.getChannelByName( display_manager.getSelectedChannel() );
                        if( channel != null )
                        {
                            String completed_nick = channel.completeNick( word );
                            if( right_space_pos > -1 )
                            {
                                input_line =
                                    input_line.substring( 0, left_space_pos + 1 )
                                    + completed_nick
                                    + input_line.substring( right_space_pos );
                            }
                            else
                            {
                                input_line =
                                    input_line.substring( 0, left_space_pos + 1 )
                                    + completed_nick;
                            }
                            input_field.setText( input_line );
                        }
                    }
                }
                break;
            case CMD_DOCK_WINDOW:
                if( ( args != null ) && ( args.length > 1 ) )
                {
                    int location = DOCK_NOWHERE;
                    int window_index = -1;
                    
                    for( int i = 0; i < DOCK_STR.length; i++ )
                    {
                        if( args[ 0 ].equals( DOCK_STR[ i ] ) )
                        {
                            location = i;
                            break;
                        }
                    }
                    
                    try
                    {
                        window_index = Integer.parseInt( args[ 1 ] );
                    }
                    catch( NumberFormatException e ) { }
                    
                    if( ( location != DOCK_NOWHERE ) && ( window_index > -1 ) )
                    {
                        if( display_manager.dock( location, window_index ) )
                        {
                            pack();
                            display_manager.printlnDebug( "Window docked." );
                        }
                        else
                        {
                            display_manager.printlnDebug( "Failed to dock window." );
                        }
                    }
                }
                else
                {
                    display_manager.printlnDebug(
                        "/dockwindow [t|r|b|l] [window id number]"
                    );
                }
                break;
            case CMD_FOCUS_ON_INPUT_FIELD:
                input_field.grabFocus();
                break;
            case CMD_HELP:
                {
                    display_manager.printlnDebug( "Built-in commands:" );
                    
                    String [] sa = (String []) CMDS.clone();
                    Arrays.sort( sa );
                    for( int i = 0; i < sa.length; i++ )
                    {
                        display_manager.printlnDebug( sa[ i ] );
                    }
                    
                    display_manager.printlnDebug( "Known aliases:" );
                    
                    sa = alias_manager.getAliases();
                    Arrays.sort( sa );
                    for( int i = 0; i < sa.length; i++ )
                    {
                        display_manager.printlnDebug( sa[ i ] );
                    }
                }
                break;
            case CMD_JOIN:
                if( args != null )
                {
                    if( current_remote_machine instanceof Server )
                    {
                        Server s = (Server) current_remote_machine;
                        if( s != null )
                        {
                            GITextWindow window = display_manager.addChannelWindow( s, args[ 0 ] );
                            if( window != null )
                            {
                                execute( CMDS[ CMD_SEND_RAW ] + " " + command );
                                result = CommandExecutor.EXEC_SUCCESS;
                            }
                        }
                    }
                    else
                    {
                        display_manager.printlnDebug( "First use /changeserver <server id>" );
                    }
                }
                break;
            case CMD_LIST_CHANNELS:
                if( current_remote_machine instanceof Server )
                {
                    Server s = (Server) current_remote_machine;
                    Channel [] channels = s.getChannels();
                    for( int i = 0; i < channels.length; i++ )
                    {
                        display_manager.printlnDebug( channels[ i ].toString() );
                    }
                }
                break;
            case CMD_LIST_FONTS:
                GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Font [] fonts = genv.getAllFonts();
                for( int i = 0; i < fonts.length; i++ )
                {
                    display_manager.printlnDebug( fonts[ i ].getName() + " -- " + fonts[ i ].getFontName() );
                }
                break;
            case CMD_LIST_SERVERS:
                {
                    int n = remote_machines.size();
                    for( int i = 0; i < n; i++ )
                    {
                        display_manager.printlnDebug(
                            Integer.toString( i ) + ": "
                            + ((RemoteMachine) remote_machines.elementAt( i )).toString()
                        );
                    }
                }
                break;
            case CMD_LIST_WINDOWS:
                display_manager.listWindows();
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
                    display_manager.addTextWindow( s.toString(), s.toString() );
                    if( s.connect( preferred_nick ) )
                    {
                        info_manager.addRemoteMachine( s );
                    }
                }
                else
                {
                    display_manager.printlnDebug( "/newserver <host> [port]" );
                }
                break;
            case CMD_NEW_TEXT_WINDOW:
                if( args != null )
                {
                    GITextWindow window = display_manager.addTextWindow( arg_string, arg_string );
                    if( window != null )
                    {
                        result = CommandExecutor.EXEC_SUCCESS;
                    }
                }

                break;
            case CMD_NEXT_HISTORY_ENTRY:
                if( input_history_pointer > MOST_RECENT_ENTRY )
                {
                    String input_text = input_field.getText();
                    input_history_pointer--;
                    input_field.setText( (String) input_history.get( input_history_pointer ) );
                }
                break;
            case CMD_NEXT_WINDOW:
                display_manager.switchToNextWindow( NEXT_WINDOW );
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
                    display_manager.printlnDebug( "/nick <new nickname>" );
                }
                break;
            case CMD_PART:
                if( args != null )
                {
                    if( current_remote_machine instanceof Server )
                    {
                        Server s = (Server) current_remote_machine;
                        if( s != null )
                        {
                            execute( CMDS[ CMD_SEND_RAW ] + " " + command );
                            result = CommandExecutor.EXEC_SUCCESS;
                        }
                    }
                    else
                    {
                        display_manager.printlnDebug( "First use /changeserver <server id>" );
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
            case CMD_PREVIOUS_WINDOW:
                display_manager.switchToNextWindow( PREVIOUS_WINDOW );
                break;
            case CMD_PRINT:
                if( arg_string != null )
                {
                    display_manager.println( arg_string, "debug" );
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
                    display_manager.printlnDebug( "/privmsg <nick/channel> <message>" );
                }
                break;
            case CMD_SEND_RAW:
                if( ( args != null ) && ( ! args.equals( "" ) ) )
                {
                    Server s = (Server) current_remote_machine;
                    if( s != null )
                    {
                        s.send( arg_string );
                        if( args[ 0 ].toUpperCase().equals( IRCMSGS[ IRCMSG_PRIVMSG ] ) )
                        {
                            String text = Util.stringArrayToString( args, 2 ).substring( 1 );
                            if(
                                ( text.charAt( 0 ) == (char) 1 )
                                && ( text.substring( 1, 7 ).equals( "ACTION" ) )
                            )
                            {
                                text = "* " + s.getCurrentNick() + text.substring( 7, text.length() - 1 );
                            }
                            else
                            {
                                text = "<" + s.getCurrentNick() + "> " + text;
                            }
                            display_manager.println(
                                getATimeStamp(
                                    settings_manager.getString(
                                        "/gui/format/timestamp", ""
                                    )
                                )
                                + text,
                                s.toString() + " " + args[ 1 ]
                                + " from=$self"
                            );
                        }
                        result = CommandExecutor.EXEC_SUCCESS;
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
                    JInternalFrame jif = display_manager.getSelectedFrame();
                    if( jif instanceof GITextWindow )
                    {
                        GITextWindow gitw = (GITextWindow) jif;
                        gitw.setFilter( arg_string );
                    }
                }
                break;
            case CMD_SET_TITLE:
                {
                    JInternalFrame jif = display_manager.getSelectedFrame();
                    if( jif instanceof GITextWindow )
                    {
                        GITextWindow gitw = (GITextWindow) jif;
                        gitw.setTitle( arg_string );
                    }
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
                    display_manager.printlnDebug( "/switchwindow <title regexp>" );
                }
                break;
            
            case UNKNOWN_COMMAND:
            default:
                display_manager.printlnDebug( "Invalid command name: " + command_name );
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
    private javax.swing.JMenu file_menu;
    private javax.swing.JTextField input_field;
    private javax.swing.JMenuBar menu_bar;
    // End of variables declaration//GEN-END:variables
    
}
