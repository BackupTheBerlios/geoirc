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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.*;
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
    protected HashSet users;
    protected Hashtable variables;
    
    public Server(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        SoundManager sound_manager,
        InfoManager info_manager,
        Hashtable variables,
        String hostname,
        String port
    )
    {
        super( parent, display_manager, settings_manager, sound_manager, hostname, port );
        
        listening_to_channels = false;
        server_reader = null;
        channels = new Vector();
        users = new HashSet();
        current_nick = "";
        this.info_manager = info_manager;
        this.variables = variables;
    }
    
    // Returns whether a connection has been established.
    public boolean connect( String nick_to_use )
    {
        try
        {
            if( socket != null )
            {
                socket.close();
            }
            
            socket = new Socket( hostname, port );
            if( socket != null )
            {
                if( server_reader != null )
                {
                    while( server_reader.isAlive() )
                    {
                        try {
                            Thread.sleep( DELAY_FOR_SERVER_READER_DEATH );
                        } catch( InterruptedException e ) { }
                    }
                }
                
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
            info_manager.addRemoteMachine( this );
        }
                
        return isConnected();
    }
    
    public void addChannel( String channel_name )
    {
        if( isConnected() )
        {
            Channel channel = new Channel( this, channel_name, info_manager, settings_manager );
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

    public void recordChannels()
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
            
            geoirc.setCurrentRemoteMachine( this );
            geoirc.execute( CMDS[ CMD_SEND_RAW ] + " JOIN " + channel_name );
            
            i++;
        }
    }
    
    public String getCurrentNick()
    {
        return current_nick;
    }

    protected Channel getChannelByName( String name )
    {
        int n = channels.size();
        Channel retval = null;
        Channel c;
        for( int i = 0; i < n; i++ )
        {
            c = (Channel) channels.elementAt( i );
            if( c.getName().equals( name ) )
            {
                retval = c;
                break;
            }
        }

        return retval;
    }

    protected boolean acknowledgeNickChange( String old_nick, String new_nick )
    {
        boolean changed = false;
        Iterator it = users.iterator();
        User u;
        while( it.hasNext() )
        {
            u = (User) it.next();
            if( u.getNick().equals( old_nick ) )
            {
                u.setNick( new_nick );
                changed = true;
                break;
            }
        }
        
        return changed;
    }

    /* Searches the memberships of all channels, returning the first
     * User object which matches.
     */
    protected User getUserByNick( String nick )
    {
        Iterator it = users.iterator();
        User retval = null;
        User user;
        while( it.hasNext() )
        {
            user = (User) it.next();
            if( user.getNick().equals( nick ) )
            {
                retval = user;
                break;
            }
        }

        return retval;
    }

    protected User addMember( String nick )
    {
        User u = new User( nick );
        if( users.contains( u ) )
        {
            Iterator it = users.iterator();
            while( it.hasNext() )
            {
                u = (User) it.next();
                if( u.getNick().equals( nick ) )
                {
                    break;
                }
            }
        }
        else
        {
            users.add( u );
        }
        
        return u;
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
            while( ( line != null ) && ( isConnected() ) )
            {
                try
                {
                    interpretLine( line );
                }
                catch( NullPointerException e )
                {
                    Util.printException(
                        display_manager,
                        e,
                        "NullPointerException in interpretLine.\nProblem line: " + line
                    );
                }
                
                try
                {
                    line = in.readLine();
                }
                catch( IOException e )
                {
                    if( ! isConnected() && ( ! closed ) )
                    {
                        display_manager.printlnDebug( "Connection to " + Server.this.toString() + " lost." );
                        Util.printException(
                            display_manager,
                            e,
                            "I/O error while reading from server " + Server.this.toString()
                        );
                    }
                }
            }
            
            if( ! isConnected() )
            {
                display_manager.printlnDebug( "No longer connected to " + Server.this.toString() );
                if( ! closed )
                {
                    display_manager.printlnDebug( "Attempting to reconnect..." );
                    connect( current_nick );
                }
            }
        }

        protected String getNick( String nick_and_username_and_host )
        {
            return nick_and_username_and_host.substring( 1, nick_and_username_and_host.indexOf( "!" ) );
        }
        
        public Vector handleNamesList( String namlist )
        {
            Vector list_members = new Vector();
            
            User user;
            String [] nicks = Util.tokensToArray( namlist );
            for( int i = 0; i < nicks.length; i++ )
            {
                user = getUserByNick( nicks[ i ] );
                if( user == null )
                {
                    user = new User( nicks[ i ] );
                    users.add( user );
                }
                list_members.add( user );
            }

            //users.addAll( list_members );
            
            return list_members;
        }
        
        protected String extractLastURL( String text )
        {
            String url = null;
            
            String url_regexp = settings_manager.getString(
                "/misc/url regexp",
                "\\b(http://.+)\\b"
            );
            
            Matcher matcher = Pattern.compile( url_regexp ).matcher( text );
            
            if( matcher.groupCount() > 0 )
            {
                while( matcher.find() )
                {
                    url = matcher.group();

                    variables.put(
                        VARS[ VAR_LAST_URL ],
                        url
                    );
                }
            }
            
            return url;
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
                    else
                    {
                        Channel chan_obj = getChannelByName( channel );
                        if( chan_obj != null )
                        {
                            User user = addMember( nick );
                            chan_obj.addMember( user );
                        }
                    }
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_NICK ] ) )
                {
                    String old_nick = getNick( tokens[ 0 ] );
                    String new_nick = tokens[ 2 ].substring( 1 );  // Remove leading colon.
                    User user = getUserByNick( old_nick );
                    if( user != null )
                    {
                        user.setNick( new_nick );
                    }

                    Channel c;
                    if( old_nick.equals( current_nick ) )
                    {
                        // Server acknowledged and allowed our nick change.
                        current_nick = new_nick;
                        
                        int n = channels.size();
                        for( int i = 0; i < n; i++ )
                        {
                            c = (Channel) channels.elementAt( i );
                            qualities += " " + c.getName();
                            info_manager.acknowledgeNickChange( c );
                        }
                        
                        qualities += " " + FILTER_SPECIAL_CHAR + "self";
                    }
                    else
                    {
                        // Someone else's nick changed.
                        
                        int n = channels.size();
                        for( int i = 0; i < n; i++ )
                        {
                            c = (Channel) channels.elementAt( i );
                            if( c.nickIsPresent( new_nick ) )
                            {
                                qualities += " " + c.getName();
                                info_manager.acknowledgeNickChange( c );
                            }
                        }
                    }
                    
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
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_NOTICE ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String text = Util.stringArrayToString( tokens, 3 );
                    text = text.substring( 1 );  // Remove leading colon.
                    
                    extractLastURL( text );
                    
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
                    String message = Util.stringArrayToString( tokens, 3 );
                    if( message != null )
                    {
                        message = message.substring( 1 );  // remove leading colon
                        extractLastURL( message );
                    }
                    
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
                    else
                    {
                        Channel chan_obj = getChannelByName( channel );
                        if( chan_obj != null )
                        {
                            chan_obj.removeMember( nick );
                        }
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

                    extractLastURL( text );
                    
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
                        
                        if( command_id != UNKNOWN_CTCP_CMD )
                        {
                            text = "Received CTCP " + CTCP_CMDS[ command_id ]
                                + " from " + nick;
                        }
                        
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

                    extractLastURL( message );
                    
                    String text = nick + " has quit (" + message + ").";
                    qualities += " from=" + nick
                        + " " + FILTER_SPECIAL_CHAR + "quit";
                    
                    if( nick.equals( current_nick ) )
                    {
                        geoirc.execute(
                            CMDS[ CMD_DISCONNECT ] + " "
                            + geoirc.getRemoteMachineID( Server.this )
                        );
                    }
                    else
                    {
                        Channel channel = null;
                        int n = channels.size();
                        User user = null;
                        for( int i = 0; i < n; i++ )
                        {
                            channel = (Channel) channels.elementAt( i );
                            if( channel.nickIsPresent( nick ) )
                            {
                                qualities += " " + channel.getName();
                                user = channel.removeMember( nick );
                            }
                        }
                        if( user != null )
                        {
                            users.remove( user );
                        }
                    }
                    
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    sound_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_ENDOFNAMES ] ) )
                {
                    /* Example:
                    :calvino.freenode.net 366 GeoIRC_User #geoirc :End of /NAMES list.
                     */
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_NAMREPLY ] ) )
                {
                    /* Example:
                    :calvino.freenode.net 353 GeoIRC_User = #geoirc :GeoIRC_User GeoBot Fluff @ChanServ 
                     */
                    Channel channel = getChannelByName( tokens[ 4 ] );
                    if( channel != null )
                    {
                        String namlist = Util.stringArrayToString( tokens, 5 );
                        namlist = namlist.substring( 1 );  // remove leading colon
                        Vector v = handleNamesList( namlist );
                        channel.setChannelMembership( v );
                    }
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_TOPIC ] ) )
                {
                    String channel = tokens[ 3 ];
                    String topic = Util.stringArrayToString( tokens, 4 ).substring( 1 );  // remove leading colon
                    
                    extractLastURL( topic );
                    
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
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_WELCOME ] ) )
                {
                    /* Perhaps our suggested nick is longer than the maximum
                     * nick length allowed by the server, and was truncated.
                     * We shall use the welcome message to help us identify
                     * the nick that the server knows us by.
                     */
                    current_nick = tokens[ 2 ];
                    String message = Util.stringArrayToString( tokens, 3 );
                }
            }

            display_manager.println( line, Server.this.toString() );
        }
    }
}
