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
    
    public Server(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        SoundManager sound_manager,
        String hostname,
        String port
    )
    {
        super( parent, display_manager, settings_manager, sound_manager, hostname, port );
        
        listening_to_channels = false;
        server_reader = null;
        channels = new Vector();
        current_nick = "";
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

    public void addChannel( String channel )
    {
        if( isConnected() )
        {
            channels.add( channel );
        }
        if( listening_to_channels )
        {
            recordChannels();
        }
    }
    
    public boolean removeChannel( String channel )
    {
        boolean result = channels.remove( channel );
        if( listening_to_channels )
        {
            recordChannels();
        }
        return result;
    }
    
    public String [] getChannels()
    {
        String [] retval = new String [ 0 ];
        return (String []) channels.toArray( retval );
    }

    protected void recordChannels()
    {
        String server_id = geoirc.getRemoteMachineID( this );
        String nodepath = "/connections/" + server_id + "/channels/";
        settings_manager.removeNode( nodepath );
        
        int n = channels.size();
        String channel;
        String i_str;
        
        for( int i = 0; i < n; i++ )
        {
            i_str = Integer.toString( i );
            channel = (String) channels.elementAt( i );
            
            settings_manager.putString(
                nodepath + i_str + "/name",
                channel
            );
        }
    }
    
    protected void restoreChannels()
    {
        String server_id = geoirc.getRemoteMachineID( this );
        int i = 0;
        String i_str;
        
        String channel;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            channel = settings_manager.getString(
                "/connections/" + server_id + "/channels/" + i_str + "/name",
                ""
            );
            if( channel.equals( "" ) )
            {
                // No more channels stored in the settings.
                break;
            }
            
            geoirc.execute( CMDS[ CMD_SEND_RAW ] + " JOIN " + channel );
            // TODO: Check if the channel was actually joined.
            addChannel( channel );
            
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
                if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_JOIN ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ].substring( 1 );  // Remove leading colon.
                    String text = nick + " joined " + channel + ".";
                    String qualities = Server.this.toString()
                        + " " + channel
                        + " from=" + nick
                        + " join";
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
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
                        String qualities = Server.this.toString()
                            + " from=" + new_nick
                            + " nick";
                        display_manager.println(
                            GeoIRC.getATimeStamp(
                                settings_manager.getString( "/gui/format/timestamp", "" )
                            ) + text,
                            qualities
                        );
                        sound_manager.check( text, qualities );
                    }
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_PART ] ) )
                {
                    String nick  = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ];
                    String message = Util.stringArrayToString( tokens, 3 ).substring( 1 );  // remove leading colon
                    String text = nick + " left " + channel + " (" + message + ").";
                    String qualities = Server.this.toString()
                        + " " + channel
                        + " from=" + nick
                        + " part";
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }
                else if( tokens[ 0 ].equals( IRCMSGS[ IRCMSG_PING ] ) )
                {
                    send( "PONG GeoIRC" );
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
                    }
                    else
                    {
                        text = "<" + nick + "> " + text;
                    }

                    String timestamp = GeoIRC.getATimeStamp(
                        settings_manager.getString( "/gui/format/timestamp", "" )
                    );
                    String qualities = Server.this.toString()
                        + " " + tokens[ 2 ]
                        + " from=" + nick;

                    display_manager.println(
                        timestamp + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_QUIT ] ) )
                {
                    String nick  = getNick( tokens[ 0 ] );
                    String message = Util.stringArrayToString( tokens, 2 ).substring( 1 );  // remove leading colon
                    String text = nick + " has quit (" + message + ").";
                    String qualities = Server.this.toString()
                        + " from=" + nick
                        + " quit";
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
