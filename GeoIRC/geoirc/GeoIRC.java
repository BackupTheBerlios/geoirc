/*
 * GeoIRC.java
 *
 * Created on June 21, 2003, 11:12 AM
 */

package geoirc;

import com.l2fprod.gui.plaf.skin.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.LinkedList;
import java.util.prefs.*;
import java.util.StringTokenizer;
import java.util.Vector;
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
    protected boolean listening_to_servers;

    protected LinkedList input_history;
    protected int input_history_pointer;
    protected boolean input_saved;
    
    protected InputMap input_map;
    protected ActionMap action_map;
    
    protected String current_nick;
    protected RemoteMachine current_remote_machine;

    /* **************************************************************** */
    
    public GeoIRC()
    {
        this( DEFAULT_SETTINGS_FILEPATH );
    }
    
    public GeoIRC( String settings_filepath )
    {
        listening_to_servers = false;
        
        settings_manager = new SettingsManager(
            display_manager,
            ( settings_filepath == null ) ? DEFAULT_SETTINGS_FILEPATH : settings_filepath
        );
        settings_manager.loadSettingsFromXML();
        
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
            getContentPane(), menu_bar, settings_manager
        );
        display_manager.printlnDebug( skin_errors );
        
        // Read settings.
        
        current_nick = settings_manager.getString( "/personal/nick1", "GeoIRC_User" );
        
        // Map input (keystrokes, mouseclicks, etc.)
        
        setupKeyMapping( NO_MODIFIER_KEYS, KeyEvent.VK_UP );
        setupKeyMapping( NO_MODIFIER_KEYS, KeyEvent.VK_DOWN );
        setupKeyMapping( ALT, KeyEvent.VK_RIGHT );
        setupKeyMapping( ALT, KeyEvent.VK_LEFT );
        
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
        action_map.put(
            stroke_text,
            new GIAction( settings_manager.getString( "/keyboard/" + stroke_text, "default" ), this )
        );
    }
            
    // Returns the Server created.
    protected Server addServer( String hostname, String port )
    {
        Server s = new Server( this, display_manager, settings_manager, hostname, port );
        remote_machines.add( s );
        if( listening_to_servers )
        {
            recordConnections();
        }
        current_remote_machine = s;
        //GITextWindow window = display_manager.addServerWindow( s );
        
        return s;
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
                s.connect( current_nick );
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
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        input_field = new javax.swing.JTextField();
        menu_bar = new javax.swing.JMenuBar();
        file_menu = new javax.swing.JMenu();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
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

        addToInputHistory( text );
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
        if( thief instanceof JTextPane )
        {
            SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
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
    
    public int execute( String command )
    {
        if( ( command == null ) || ( command.equals( "" ) ) )
        {
            return CommandExecutor.EXEC_BAD_COMMAND;
        }
        
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
            case CMD_JOIN:
                if( args != null )
                {
                    //Server s = (Server) display_manager.getSelectedRemoteMachine();
                    if( current_remote_machine instanceof Server )
                    {
                        Server s = (Server) current_remote_machine;
                        if( s != null )
                        {
                            GITextWindow window = display_manager.addChannelWindow( s, args[ 0 ] );
                            if( window != null )
                            {
                                execute( CMDS[ CMD_SEND_RAW ] + " " + command );
                                // TODO: Check if the channel was actually joined...
                                s.addChannel( args[ 0 ] );
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
                    String [] channels = s.getChannels();
                    for( int i = 0; i < channels.length; i++ )
                    {
                        display_manager.printlnDebug( channels[ i ] );
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
                    //display_manager.addServerWindow( s );
                    display_manager.addTextWindow( s.toString(), s.toString() );
                    s.connect( current_nick );
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
                    current_nick = args[ 0 ];
                    
                    execute(
                        CMDS[ CMD_SEND_RAW ]
                        + " NICK "
                        + args[ 0 ]
                    );
                }
                else
                {
                    display_manager.printlnDebug( "Current nick: " + current_nick );
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
                            s.removeChannel( args[ 0 ] );
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
                if( input_history_pointer < input_history.size() - 1 )
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
            case CMD_SEND_RAW:
                {
                    //Server s = (Server) display_manager.getSelectedRemoteMachine();
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
                                text = "* " + current_nick + text.substring( 7, text.length() - 1 );
                            }
                            else
                            {
                                text = "<" + current_nick + "> " + text;
                            }
                            display_manager.println(
                                text,
                                s.toString() + " " + args[ 1 ]
                            );
                        }
                        result = CommandExecutor.EXEC_SUCCESS;
                    }
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
