/*
 * Server.java
 *
 * Created on June 22, 2003, 11:59 PM
 */

package geoirc;

import geoirc.util.Util;
import java.awt.Component;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.*;

/**
 *
 * @author  Pistos
 */
public class Server
    extends RemoteMachine
    implements GeoIRCConstants
{
    protected ServerReader server_reader;
    protected Vector channels;
    protected boolean listening_to_channels;
    protected String current_nick;
    protected InfoManager info_manager;
    
    public Server(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        SoundManager sound_manager,
        InfoManager info_manager,
        String hostname,
        String port
    )
    {
        super( parent, display_manager, settings_manager, sound_manager, hostname, port );
        
        listening_to_channels = false;
        server_reader = null;
        channels = new Vector();
        current_nick = "";
        this.info_manager = info_manager;
    }
    
    // Returns whether a connection has been established.
    public boolean connect( String nick_to_use )
    {
        try
        {
            socket = new Socket( hostname, port );
            if( socket != null )
            {
                server_reader = new ServerReader( 
                    new BufferedReader(
                        new InputStreamReader(
                            socket.getInputStream()
                        )
                    )
                );
                out = new PrintWriter( socket.getOutputStream(), true );
                
                server_reader.start();

                out.println( "PASS ooga7" );
                out.println( "NICK " + nick_to_use );
                out.println(
                    "USER "
                    + settings_manager.getString( "/personal/ident/username", "Pistos" )
                    + " x x :Pi Gi" );
                
                // TODO: We'll setup some way to find out when the server
                // acknowledges that our nick is okay to use,
                // and when it sends us the "in use" error right away once
                // we sign on.
                current_nick = nick_to_use;
            }
        }
        catch( UnknownHostException e )
        {
            display_manager.printlnDebug( "Unknown host: " + hostname );
            display_manager.printlnDebug( e.getMessage() );
        }
        catch( IOException e )
        {
            display_manager.printlnDebug( e.getMessage() );
        }
        
        boolean connected = isConnected();
        if( connected )
        {
            restoreChannels();
            listening_to_channels = true;
        }
                
        return isConnected();
    }

    public void addChannel( String channel_name )
    {
        if( isConnected() )
        {
            Channel channel = new Channel( this, channel_name );
            channels.add( channel );
            info_manager.addChannel( channel );
        }
        if( listening_to_channels )
        {
            recordChannels();
        }
    }
    
    public boolean removeChannel( String channel_name )
    {
        boolean removed = false;
        Channel channel = null;
        int n = channels.size();
        for( int i = 0; i < n; i++ )
        {
            channel = (Channel) channels.elementAt( i );
            if( channel.getName().equals( channel_name ) )
            {
                channels.remove( channel );
                info_manager.removeChannel( channel );
                removed = true;
                if( listening_to_channels )
                {
                    recordChannels();
                }
                break;
            }
        }
        
        return removed;
    }
    
    public Channel [] getChannels()
    {
        Channel [] retval = new Channel [ 0 ];
        return (Channel []) channels.toArray( retval );
    }

    protected void recordChannels()
    {
        String server_id = geoirc.getRemoteMachineID( this );
        String nodepath = "/connections/" + server_id + "/channels/";
        settings_manager.removeNode( nodepath );
        
        int n = channels.size();
        Channel channel;
        String i_str;
        
        for( int i = 0; i < n; i++ )
        {
            i_str = Integer.toString( i );
            channel = (Channel) channels.elementAt( i );
            
            settings_manager.putString(
                nodepath + i_str + "/name",
                channel.getName()
            );
        }
    }
    
    protected void restoreChannels()
    {
        String server_id = geoirc.getRemoteMachineID( this );
        int i = 0;
        String i_str;
        
        String channel_name;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            channel_name = settings_manager.getString(
                "/connections/" + server_id + "/channels/" + i_str + "/name",
                ""
            );
            if( channel_name.equals( "" ) )
            {
                // No more channels stored in the settings.
                break;
            }
            
            geoirc.execute( CMDS[ CMD_SEND_RAW ] + " JOIN " + channel_name );
            
            i++;
        }
    }
    
    public String getCurrentNick()
    {
        return current_nick;
    }

    /* ******************************************************************** */
    
    protected class ServerReader
        extends java.lang.Thread
        implements GeoIRCConstants
    {
        protected BufferedReader in;

        // No default constructor.
        private ServerReader() { }

        public ServerReader(
            BufferedReader in
        )
        {
            this.in = in;
        }

        public void run()
        {
            if( ( in == null ) || ( display_manager == null ) )
            {
                return;
            }

            String line = "";
            while( line != null )
            {
                interpretLine( line );
                try
                {
                    line = in.readLine();
                }
                catch( IOException e )
                {
                    display_manager.printlnDebug( e.getMessage() );
                }
            }
        }

        protected String getNick( String nick_and_username_and_host )
        {
            return nick_and_username_and_host.substring( 1, nick_and_username_and_host.indexOf( "!" ) );
        }

        protected void interpretLine( String line )
        {
            String [] tokens = Util.tokensToArray( line );
            if( tokens != null )
            {
                String qualities = Server.this.toString();
                
                if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_JOIN ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ].substring( 1 );  // Remove leading colon.
                    String text = nick + " joined " + channel + ".";
                    qualities += " " + channel
                        + " from=" + nick
                        + " " + FILTER_SPECIAL_CHAR + "join";
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                    
                    if( nick.equals( current_nick ) )
                    {
                        addChannel( channel );
                    }
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_NICK ] ) )
                {
                    String old_nick = getNick( tokens[ 0 ] );
                    String new_nick = tokens[ 2 ].substring( 1 );  // Remove leading colon.
                    if( old_nick.equals( current_nick ) )
                    {
                        // Server acknowledged and allowed our nick change.
                        current_nick = new_nick;
                    }
                    else
                    {
                        // Someone else's nick changed.
                        
                        String text = old_nick + " is now known as " + new_nick + ".";
                        qualities += " from=" + new_nick
                            + " " + FILTER_SPECIAL_CHAR + "nick";
                        display_manager.println(
                            GeoIRC.getATimeStamp(
                                settings_manager.getString( "/gui/format/timestamp", "" )
                            ) + text,
                            qualities
                        );
                        sound_manager.check( text, qualities );
                    }
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_NOTICE ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String text = Util.stringArrayToString( tokens, 3 );
                    text = text.substring( 1 );  // Remove leading colon.
                    qualities += " " + FILTER_SPECIAL_CHAR + "notice";

                    if( text.charAt( 0 ) == CTCP_MARKER )
                    {
                        // CTCP message.
                        
                        qualities += " " + FILTER_SPECIAL_CHAR + "ctcp";
                        
                        String ctcp_message = text.substring(
                            1,
                            text.lastIndexOf( CTCP_MARKER )
                        );
                        
                        int space_index = ctcp_message.indexOf( " " );
                        String command_name;
                        String arg_string;
                        if( space_index > -1 )
                        {
                            command_name = ctcp_message.substring( 0, space_index );
                            arg_string = ctcp_message.substring( space_index + 1 );
                        }
                        else
                        {
                            command_name = ctcp_message;
                            arg_string = null;
                        }
                        String [] args = Util.tokensToArray( arg_string );

                        int command_id = UNKNOWN_CTCP_CMD;
                        for( int i = 0; i < CTCP_CMDS.length; i++ )
                        {
                            if( command_name.equals( CTCP_CMDS[ i ] ) )
                            {
                                command_id = i;
                                break;
                            }
                        }

                        text = "Received CTCP " + CTCP_CMDS[ command_id ]
                            + " from " + nick;
                    }
                    else
                    {
                        text = "-" + nick + "- " + text;
                    }

                    String timestamp = GeoIRC.getATimeStamp(
                        settings_manager.getString( "/gui/format/timestamp", "" )
                    );
                    qualities += " " + tokens[ 2 ]
                        + " from=" + nick;

                    display_manager.println(
                        timestamp + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_PART ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ];
                    String message = Util.stringArrayToString( tokens, 3 ).substring( 1 );  // remove leading colon
                    String text = nick + " left " + channel + " (" + message + ").";
                    qualities += " " + channel
                        + " from=" + nick
                        + " " + FILTER_SPECIAL_CHAR + "part";
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                    
                    if( nick.equals( current_nick ) )
                    {
                        removeChannel( channel );
                    }
                }
                else if( tokens[ 0 ].equals( IRCMSGS[ IRCMSG_PING ] ) )
                {
                    send( "PONG GeoIRC" );
                    display_manager.printlnDebug( "PONG sent to " + Server.this.toString() );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_PRIVMSG ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String text = Util.stringArrayToString( tokens, 3 );
                    text = text.substring( 1 );  // Remove leading colon.

                    if(
                        ( text.charAt( 0 ) == (char) 1 )
                        && ( text.substring( 1, 7 ).equals( "ACTION" ) )
                    )
                    {
                        text = "* " + nick + text.substring( 7, text.length() - 1 );
                        qualities += " " + FILTER_SPECIAL_CHAR + "action";
                    }
                    else if( text.charAt( 0 ) == CTCP_MARKER )
                    {
                        // CTCP message.
                        
                        qualities += " " + FILTER_SPECIAL_CHAR + "ctcp";
                        
                        String ctcp_message = text.substring(
                            1,
                            text.lastIndexOf( CTCP_MARKER )
                        );
                        
                        int space_index = ctcp_message.indexOf( " " );
                        String command_name;
                        String arg_string;
                        if( space_index > -1 )
                        {
                            command_name = ctcp_message.substring( 0, space_index );
                            arg_string = ctcp_message.substring( space_index + 1 );
                        }
                        else
                        {
                            command_name = ctcp_message;
                            arg_string = null;
                        }
                        String [] args = Util.tokensToArray( arg_string );

                        int command_id = UNKNOWN_CTCP_CMD;
                        for( int i = 0; i < CTCP_CMDS.length; i++ )
                        {
                            if( command_name.equals( CTCP_CMDS[ i ] ) )
                            {
                                command_id = i;
                                break;
                            }
                        }

                        text = "Received CTCP " + CTCP_CMDS[ command_id ]
                            + " from " + nick;
                        
                        switch( command_id )
                        {
                            case CTCP_CMD_SOURCE:
                                send(
                                    IRCMSGS[ IRCMSG_NOTICE ] + " "
                                    + nick + " :"
                                    + CTCP_MARKER 
                                    + CTCP_CMDS[ CTCP_CMD_SOURCE ]
                                    + " http://geoirc.berlios.de"
                                    + CTCP_MARKER
                                );
                                break;
                            case CTCP_CMD_USERINFO:
                                send(
                                    IRCMSGS[ IRCMSG_NOTICE ] + " "
                                    + nick + " :"
                                    + CTCP_MARKER 
                                    + CTCP_CMDS[ CTCP_CMD_USERINFO ]
                                    + " "
                                    + settings_manager.getString(
                                        "/personal/ctcp/userinfo", ""
                                    )
                                    + CTCP_MARKER
                                );
                                break;
                            case CTCP_CMD_VERSION:
                                send(
                                    IRCMSGS[ IRCMSG_NOTICE ] + " "
                                    + nick + " :"
                                    + CTCP_MARKER 
                                    + CTCP_CMDS[ CTCP_CMD_VERSION ]
                                    + " GeoIRC/" + GEOIRC_VERSION + " "
                                    + settings_manager.getString(
                                        "/personal/ctcp/version", ""
                                    )
                                    + CTCP_MARKER
                                );
                                break;
                            default:
                                text = "Unknown CTCP command from " + nick + ": "
                                    + ctcp_message;
                                break;
                        }
                    }
                    else
                    {
                        text = "<" + nick + "> " + text;
                    }

                    String timestamp = GeoIRC.getATimeStamp(
                        settings_manager.getString( "/gui/format/timestamp", "" )
                    );
                    qualities += " " + tokens[ 2 ]
                        + " from=" + nick;

                    display_manager.println(
                        timestamp + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_QUIT ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String message = Util.stringArrayToString( tokens, 2 ).substring( 1 );  // remove leading colon
                    String text = nick + " has quit (" + message + ").";
                    qualities += " from=" + nick
                        + " " + FILTER_SPECIAL_CHAR + "quit";
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_TOPIC ] ) )
                {
                    String channel = tokens[ 3 ];
                    String topic = Util.stringArrayToString( tokens, 4 ).substring( 1 );  // remove leading colon
                    qualities += " " + FILTER_SPECIAL_CHAR + "topic"
                        + " " + channel;
                    String text = "The topic for " + channel + " is: " + topic;
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_TOPIC_SETTER ] ) )
                {
                    String channel = tokens[ 3 ];
                    String setter = tokens[ 4 ];
                    String time_str = tokens[ 5 ];
                    long time_in_seconds;
                    String time = "an unknown date and time";
                    try
                    {
                        time_in_seconds = Integer.parseInt( time_str );
                        DateFormat df = DateFormat.getDateTimeInstance(
                            DateFormat.LONG, DateFormat.LONG
                        );
                        time = df.format( new Date( time_in_seconds * 1000 ) );
                    } catch( NumberFormatException e ) { }
                    
                    qualities += " " + FILTER_SPECIAL_CHAR + "topicsetter"
                        + " " + channel;
                    String text = "The topic for " + channel + " was set by "
                        + setter + " on " + time;
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }

            }

            display_manager.println( line, Server.this.toString() );
        }
    }
}
