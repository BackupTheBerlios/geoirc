/*
 * Server.java
 *
 * Created on June 22, 2003, 11:59 PM
 */

package geoirc;

import geoirc.util.BadExpressionException;
import geoirc.util.BoolExpEvaluator;
import geoirc.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author  Pistos
 */
public class Server
    extends RemoteMachine
    implements GeoIRCConstants
{
    protected Vector channels;
    protected InfoManager info_manager;
    protected VariableManager variable_manager;
    protected HashSet users;
    protected Set conversation_words;
    
    protected int current_nick_width;
    protected boolean listening_to_channels;
    protected String current_nick;
    
    
    public Server(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        TriggerManager trigger_manager,
        InfoManager info_manager,
        VariableManager variable_manager,
        Set conversation_words,
        String hostname,
        String port
    )
    {
        super( parent, display_manager, settings_manager, trigger_manager, hostname, port );
        
        listening_to_channels = false;
        channels = new Vector();
        users = new HashSet();
        current_nick = "";
        this.info_manager = info_manager;
        this.variable_manager = variable_manager;
        this.conversation_words = conversation_words;
        current_nick_width = 0;
    }
    
    public void connect( String nick_to_use )
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
                if( reader != null )
                {
                    while( reader.isAlive() )
                    {
                        try {
                            Thread.sleep( DELAY_FOR_SERVER_READER_DEATH );
                        } catch( InterruptedException e ) { }
                    }
                }
                
                reader = new ServerReader( 
                    new BufferedReader(
                        new InputStreamReader(
                            socket.getInputStream()
                        )
                    )
                );
                out = new PrintWriter( socket.getOutputStream(), true );
                
                reader.start();

                out.println( "PASS ooga7" );
                out.println( "NICK " + nick_to_use );
                out.println(
                    "USER "
                    + settings_manager.getString( "/personal/ident/username", "pistos" )
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
        
        reset = false;
    }
    
    public void addChannel( String channel_name )
    {
        if( isConnected() )
        {
            Channel channel = new Channel( this, channel_name, info_manager, settings_manager, display_manager );
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
        Channel retval = null;
        if( ( name != null ) && ( ! name.equals( "" ) ) )
        {
            int n = channels.size();
            Channel c;
            for( int i = 0; i < n; i++ )
            {
                c = (Channel) channels.elementAt( i );
                if( c.getName().equals( name.toLowerCase() ) )
                {
                    retval = c;
                    break;
                }
            }
        }

        return retval;
    }
    
    public void noteActivity( String channel_name, User user )
    {
        Channel c = getChannelByName( channel_name );
        if( c != null )
        {
            c.acknowledgeUserChange( user );
        }
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
                for ( Iterator cit = channels.iterator(); cit.hasNext(); )
                {
                    ((Channel)cit.next()).acknowledgeUserChange(u);
                }
                break;
            }
        }
        
        return changed;
    }
    
    /**
     * @return the User object for the GeoIRC user
     */
    public User getUserObject()
    {
        return getUserByNick( current_nick );
    }

    /* Searches the server membership, returning the first
     * User object which matches.
     */
    protected User getUserByNick( String nick )
    {
        User retval = null;
        
        if( nick != null )
        {
            Iterator it = users.iterator();
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
        }

        return retval;
    }

    protected User addMember( Channel originating_channel, String nick )
    {
        User user = new User( originating_channel, nick );
        User u;
        if( users.contains( user ) )
        {
            Iterator it = users.iterator();
            while( it.hasNext() )
            {
                u = (User) it.next();
                if( u.equals( user ) )
                {
                    user = u;
                    user.addModeChars( originating_channel, nick );
                    break;
                }
            }
        }
        else
        {
            users.add( user );
        }
        
        return user;
    }
    
    public String getPadded( String text )
    {
        current_nick_width = (
            ( current_nick_width < text.length() )
            ? text.length()
            : current_nick_width
        );

        int max_nick_width = settings_manager.getInt(
            "/gui/format/maximum nick width",
            DEFAULT_MAXIMUM_NICK_WIDTH
        );
        if( current_nick_width > max_nick_width )
        {
            current_nick_width = max_nick_width;
        }
        return Util.getPadding( " ", current_nick_width - text.length() ) + text;
    }

    /* ******************************************************************** */
    
    protected class ServerReader
        extends RemoteMachineReader
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
            while( ( line != null ) && isConnected() && ( ! reset ) )
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
                    Util.printException(
                        display_manager,
                        e,
                        "I/O error while reading from server " + Server.this.toString()
                    );
                    
                    if( ! isConnected() && ( ! closed ) )
                    {
                        display_manager.printlnDebug( "Connection to " + Server.this.toString() + " lost." );
                    }
                    
                    if( e.getMessage().equals( "Connection reset" ) )
                    {
                        display_manager.printlnDebug( "Connection to " + Server.this.toString() + " reset." );
                        reset = true;
                    }
                }
            }
            
            if( ( ! isConnected() ) || reset )
            {
                geoirc.recordConnections();
                
                if( ! isConnected() )
                {
                    display_manager.printlnDebug( "No longer connected to " + Server.this.toString() );
                }
                
                if( ! closed )
                {
                    display_manager.printlnDebug( "Attempting to reconnect..." );
                    connect( current_nick );
                }
            }
        }

        protected String getNick( String nick_and_username_and_host )
        {
            String retval = null;
            
            if( nick_and_username_and_host != null )
            {
                int index = nick_and_username_and_host.indexOf( "!" );
                if( index > -1 )
                {
                    retval = nick_and_username_and_host.substring( 1, index );
                }
            }
            
            return retval;
        }
        
        protected String getUserNameAndHost( String nick_and_username_and_host )
        {
            String retval = null;
            
            if( nick_and_username_and_host != null )
            {
                int index = nick_and_username_and_host.indexOf( "!" );
                if( index > -1 )
                {                    
                    retval = nick_and_username_and_host.substring( index + 1 );
                }
            }
            
            return retval;            
        }
        
        public Vector handleNamesList( Channel originating_channel, String namlist )
        {
            Vector list_members = new Vector();
            
            User user;
            String [] nicks = Util.tokensToArray( namlist );
            for( int i = 0; i < nicks.length; i++ )
            {
                user = addMember( originating_channel, nicks[ i ] );
                list_members.add( user );
            }

            return list_members;
        }
        
        protected void extractVariables( String text, String qualities )
        {
            String variable;
            String filter;
            String regexp;
            int i = 0;
            String path;
            
            while( GOD_IS_GOOD )
            {
                path = "/variables/captured/" + Integer.toString( i );

                variable = settings_manager.getString( path + "/name", "" );
                if( variable.equals( "" ) )
                {
                    // No more variables stored in the settings.
                    break;
                }
                
                filter = settings_manager.getString( path + "/filter", "" );
                
                boolean passed = false;
                try
                {
                    passed = BoolExpEvaluator.evaluate( filter, qualities );
                }
                catch( BadExpressionException e )
                {
                    display_manager.printlnDebug( "Filter evaluation error for filter '" + filter + "'" );
                    display_manager.printlnDebug( e.getMessage() );
                }
                
                if( passed )
                {
                    regexp = settings_manager.getString( path + "/regexp", ".*" );

                    Matcher matcher = Pattern.compile( regexp ).matcher( text );

                    if( matcher.groupCount() > 0 )
                    {
                        while( matcher.find() )
                        {
                            variable_manager.setString( variable, matcher.group( 1 ) );
                        }
                    }
                }
                
                i++;
            }
        }
        
        protected void interpretLine( String line )
        {
            String [] transformed_message =
                geoirc.onRaw( line, Server.this.toString() );
            
            String [] tokens = Util.tokensToArray( transformed_message[ 0 ] );
            int windows_printed_to = 0;
            if( tokens != null )
            {
                String qualities = transformed_message[ 1 ];
                
                if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_JOIN ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ].toLowerCase();
                    if( channel.charAt( 0 ) == ':' )
                    {
                        // Remove leading colon.
                        channel = channel.substring( 1 );
                    }
                    String text = getPadded( nick );                                        
                    //show dns username and host?
                    if( settings_manager.getBoolean("/gui/format/complete join message", false) == true )
                    {                    
                        text += " (" + getUserNameAndHost( tokens[ 0 ] ) + ")";
                    }
                    text += " has joined " + channel + ".";
                    
                    qualities += " " + channel
                        + " from=" + nick
                        + " " + FILTER_SPECIAL_CHAR + "join";
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                    
                    if( nick.equals( current_nick ) )
                    {
                        addChannel( channel );
                    }
                    else
                    {
                        Channel chan_obj = getChannelByName( channel );
                        if( chan_obj != null )
                        {
                            User user = addMember( chan_obj, nick );
                            chan_obj.addMember( user );
                        }
                        else
                        {
                            display_manager.printlnDebug(
                                "Warning: No associated channel object for '"
                                + channel + "'."
                            );
                        }
                    }
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_KICK ] ) )
                {
                    // :kez!kez@modem-302.bear.dialup.pol.co.uk KICK #GeoShell GeoBot :kez
                    String kicker = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ].toLowerCase();
                    String nick = tokens[ 3 ];
                    String message = Util.stringArrayToString( tokens, 4 );
                    if( message != null )
                    {
                        message = message.substring( 1 );  // remove leading colon
                    }
                    User user = getUserByNick( nick );
                    if( user != null )
                    {
                        user.noteActivity();
                    }
                    
                    String text = getPadded( kicker ) + " has kicked " + nick + " from " + channel + " (" + message + ").";
                    qualities += " " + channel
                        + " from=" + kicker
                        + " victim=" + nick
                        + " " + FILTER_SPECIAL_CHAR + "kick";
                    
                    if( message != null )
                    {
                        extractVariables( message, qualities );
                    }
                    
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                    
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
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_MODE ] ) )
                {
                    // For now, this only handles single argument MODE commands.
                    
                    String channel_or_nick = tokens[ 2 ];
                    
                    if( channel_or_nick.charAt( 0 ) == '#' )
                    {
                        try
                        {
                            String nick = getNick( tokens[ 0 ] );
                            User user = null;
                            if( nick == null )
                            {
                                // No nick; it could be the server itself doing the action.
                                nick = tokens[ 0 ];
                            }
                            else
                            {
                                user = getUserByNick( nick );
                                if( user != null )
                                {
                                    user.noteActivity();
                                }
                            }
                            String channel = channel_or_nick;
                            String polarity = tokens[ 3 ].substring( 0, 1 );
                            String mode = tokens[ 3 ].substring( 1, 2 );
                            String arg = tokens[ 4 ];
                            Channel c = Server.this.getChannelByName( channel );

                            String text = null;
                            qualities += " " + channel
                                + " from=" + nick
                                + " " + FILTER_SPECIAL_CHAR + "mode"
                                + " mode=" + mode
                                + " polarity=" + polarity;

                            if( mode.equals( MODE_OP ) )
                            {
                                User recipient_user = getUserByNick( arg );
                                if( recipient_user != null )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        recipient_user.addModeFlag( c, MODE_OP );
                                        c.acknowledgeUserChange( user );
                                        c.acknowledgeUserChange( recipient_user );
                                        text = getPadded( nick ) + " has given channel operator privileges for "
                                            + channel + " to " + arg + ".";
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        recipient_user.removeModeFlag( c, MODE_OP );
                                        c.acknowledgeUserChange( user );
                                        c.acknowledgeUserChange( recipient_user );
                                        text = getPadded( nick ) + " has taken channel operator privileges for "
                                            + channel + " from " + arg + ".";
                                    }

                                    qualities += " recipient=" + nick;
                                }
                            }
                            else if( mode.equals( MODE_HALFOP ) )
                            {
                                User recipient_user = getUserByNick( arg );
                                if( recipient_user != null )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        recipient_user.addModeFlag( c, MODE_HALFOP );
                                        c.acknowledgeUserChange( user );
                                        c.acknowledgeUserChange( recipient_user );
                                        text = getPadded( nick ) + " has given half operator privileges for "
                                            + channel + " to " + arg + ".";
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        recipient_user.removeModeFlag( c, MODE_HALFOP );
                                        c.acknowledgeUserChange( user );
                                        c.acknowledgeUserChange( recipient_user );
                                        text = getPadded( nick ) + " has taken half operator privileges for "
                                            + channel + " from " + arg + ".";
                                    }

                                    qualities += " recipient=" + nick;
                                }
                            }
                            else if( mode.equals( "p" ) )
                            {
                                if( polarity.equals( "+" ) )
                                {
                                    text = getPadded( nick ) + " has made " + channel
                                        + " a private channel.";
                                }
                                else if( polarity.equals( "-" ) )
                                {
                                    text = getPadded( nick ) + " has removed the private status of "
                                        + channel + ".";
                                }
                            }
                            else if( mode.equals( "s" ) )
                            {
                                if( polarity.equals( "+" ) )
                                {
                                    text = getPadded( nick ) + " has made " + channel
                                        + " a secret channel.";
                                }
                                else if( polarity.equals( "-" ) )
                                {
                                    text = getPadded( nick ) + " has removed the secret status of "
                                        + channel + ".";
                                }
                            }
                            else if( mode.equals( "i" ) )
                            {
                                if( polarity.equals( "+" ) )
                                {
                                    text = getPadded( nick ) + " has made " + channel
                                        + " invite-only.";
                                }
                                else if( polarity.equals( "-" ) )
                                {
                                    text = getPadded( nick ) + " has removed the invite-only status of "
                                        + channel + ".";
                                }
                            }
                            else if( mode.equals( "t" ) )
                            {
                                if( polarity.equals( "+" ) )
                                {
                                    text = getPadded( nick ) + " has made the topic of " + channel
                                        + " settable only by channel operators.";
                                }
                                else if( polarity.equals( "-" ) )
                                {
                                    text = getPadded( nick ) + " has made the topic of " + channel
                                        + " settable by anyone.";
                                }
                            }
                            else if( mode.equals( "n" ) )
                            {
                                if( polarity.equals( "+" ) )
                                {
                                    text = getPadded( nick ) + " has blocked messages to  " + channel
                                        + " from people who are not in the channel.";
                                }
                                else if( polarity.equals( "-" ) )
                                {
                                    text = getPadded( nick ) + " has allowed messages to  " + channel
                                        + " from people who are not in the channel.";
                                }
                            }
                            else if( mode.equals( "m" ) )
                            {
                                if( polarity.equals( "+" ) )
                                {
                                    text = getPadded( nick ) + " has made " + channel
                                        + " a moderated channel.";
                                }
                                else if( polarity.equals( "-" ) )
                                {
                                    text = getPadded( nick ) + " has made " + channel
                                        + " an unmoderated channel.";
                                }
                            }
                            else if( mode.equals( "b" ) )
                            {
                                if( polarity.equals( "+" ) )
                                {
                                    text = getPadded( nick ) + " has added the ban " + arg + " for "
                                        + channel + ".";
                                }
                                else if( polarity.equals( "-" ) )
                                {
                                    text = getPadded( nick ) + " has lifted the ban " + arg + " for "
                                        + channel + ".";
                                }
                            }
                            else if( mode.equals( MODE_VOICE ) )
                            {
                                User recipient_user = getUserByNick( arg );
                                if( recipient_user != null )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        recipient_user.addModeFlag( c, MODE_VOICE );
                                        c.acknowledgeUserChange( user );
                                        c.acknowledgeUserChange( recipient_user );
                                        text = getPadded( nick ) + " has given voice in "
                                            + channel + " to " + arg + ".";
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        recipient_user.removeModeFlag( c, MODE_VOICE );
                                        c.acknowledgeUserChange( user );
                                        c.acknowledgeUserChange( recipient_user );
                                        text = getPadded( nick ) + " has taken voice in "
                                            + channel + " from " + arg + ".";
                                    }

                                    qualities += " recipient=" + nick;
                                }
                            }

                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                qualities
                            );
                            trigger_manager.check( text, qualities );
                        }
                        catch( ArrayIndexOutOfBoundsException e )
                        {
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
                        user.noteActivity();
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
                            c.acknowledgeUserChange( user );
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
                                c.acknowledgeUserChange( user );
                                //info_manager.acknowledgeNickChange( c );
                            }
                        }
                    }
                    
                    String text = getPadded( old_nick ) + " is now known as " + new_nick + ".";
                    qualities += " from=" + new_nick
                        + " " + FILTER_SPECIAL_CHAR + "nick";
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_NOTICE ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String text = Util.stringArrayToString( tokens, 3 );
                    text = text.substring( 1 );  // Remove leading colon.
                    
                    User user = getUserByNick( nick );
                    if( user != null )
                    {
                        user.noteActivity();

                        if( tokens[ 2 ].equals( current_nick ) )
                        {
                            // Notice to GeoIRC user.
                            
                            String query_window_title = Util.getQueryWindowFilter( nick );
                            
                            if(
                                display_manager.getTextPaneByTitle(
                                    query_window_title
                                ) == null
                            )
                            {
                                display_manager.addTextWindow(
                                    query_window_title,
                                    query_window_title
                                );
                            }
                            
                            qualities += " " + FILTER_SPECIAL_CHAR + "self";
                        }
                        
                        Channel c;
                        int n = channels.size();
                        for( int i = 0; i < n; i++ )
                        {
                            c = (Channel) channels.elementAt( i );
                            if( c.isMember( user ) )
                            {
                                c.acknowledgeUserChange( user );
                            }
                        }
                    }
                    
                    qualities += " " + FILTER_SPECIAL_CHAR + "notice";

                    if( 
                        ( text.length() > 0 )
                        && ( text.charAt( 0 ) == CTCP_MARKER )
                    )
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

                    extractVariables( text, qualities );
                    windows_printed_to += display_manager.println(
                        timestamp + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_PART ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ].toLowerCase();
                    String message = Util.stringArrayToString( tokens, 3 );
                    if( message != null )
                    {
                        message = message.substring( 1 );  // remove leading colon
                    }
                    User user = getUserByNick( nick );
                    if( user != null )
                    {
                        user.noteActivity();
                    }
                    
                    String text = getPadded( nick ) + " has left " + channel + " (" + message + ").";
                    qualities += " " + channel
                        + " from=" + nick
                        + " " + FILTER_SPECIAL_CHAR + "part";
                    
                    if( message != null )
                    {
                        extractVariables( message, qualities );
                    }
                    
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                    
                    if( nick.equals( current_nick ) )
                    {
                        removeChannel( channel );
                        display_manager.closeWindows( Server.this.toString() + " and " + channel );
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
                    String pong_arg = "GeoIRC";
                    if( tokens.length > 1 )
                    {
                        pong_arg = tokens[ 1 ];
                    }
                    send( "PONG " + pong_arg );
                    display_manager.println(
                        "PONG sent to " + Server.this.toString(),
                        Server.this.toString() + " "
                        + FILTER_SPECIAL_CHAR + "pong"
                    );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_PRIVMSG ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String text = Util.stringArrayToString( tokens, 3 );
                    text = text.substring( 1 );  // Remove leading colon.
                    qualities += " " + FILTER_SPECIAL_CHAR + "privmsg";
                    String [] words = null;
                    
                    User user = getUserByNick( nick );
                    if( user != null )
                    {
                        user.noteActivity();
                        
                        if( tokens[ 2 ].equals( current_nick ) )
                        {
                            // Message to GeoIRC user.
                            
                            String query_window_title = 
                                FILTER_SPECIAL_CHAR + "self and "
                                + "from=" + nick
                                + " or "
                                + nick + " and "
                                + "from=" + FILTER_SPECIAL_CHAR + "self";
                            
                            if(
                                display_manager.getTextPaneByTitle(
                                    query_window_title
                                ) == null
                            )
                            {
                                display_manager.addTextWindow(
                                    query_window_title,
                                    query_window_title
                                );
                            }
                            
                            qualities += " " + FILTER_SPECIAL_CHAR + "self";
                        }
                        else
                        {
                            Channel c = Server.this.getChannelByName( tokens[ 2 ] );
                            if( c != null )
                            {
                                c.acknowledgeUserChange( user );
                            }
                        }
                    }
                    else
                    {
                        display_manager.printlnDebug(
                            "Warning: User '" + nick + "' not in user list for "
                            + Server.this.toString()
                        );
                    }

                    if( text.length() > 0 )
                    {
                        if(
                            ( text.charAt( 0 ) == CTCP_MARKER )
                            && ( text.charAt( text.length() - 1 ) == CTCP_MARKER )
                        )
                        {
                            // CTCP message.
                            
                            qualities += " " + FILTER_SPECIAL_CHAR + "ctcp";
                            
                            if( text.substring( 1, 7 ).equals( "ACTION" ) )
                            {
                                // For legacy reasons, we'll treat ACTIONs in a different way.
                                
                                String text2 = Util.stringArrayToString( tokens, 4 );
                                words = Util.tokensToArray(
                                    text2.substring( 0, text2.length() - 1 )
                                );
                                text =
                                    getPadded( "* " + nick )
                                    + text.substring( 7, text.length() - 1 );
                                qualities += " " + FILTER_SPECIAL_CHAR + "action";
                            }
                            else
                            {
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
                                    case CTCP_CMD_PAGE:
                                        qualities += " " + FILTER_SPECIAL_CHAR + "page";
                                        break;
                                    case CTCP_CMD_DCC:
                                        try
                                        {
                                            geoirc.addDCCChatRequest( args, nick );
                                        }
                                        catch( ArrayIndexOutOfBoundsException e )
                                        {
                                            // Someone tried to send us an invalid DCC command.
                                        }
                                        catch( NumberFormatException e )
                                        {
                                            // Bad long integer...
                                        }
                                        break;
                                    default:
                                        text = "Unknown CTCP command from " + nick + ": "
                                            + ctcp_message;
                                        break;
                                }
                            }
                        }
                        else
                        {
                            words = Util.tokensToArray( text );
                            text = getPadded( "<" + nick + ">" ) + " " + text;
                        }
                    }
                    else
                    {
                        text = getPadded( "<" + nick + ">" );
                    }
                    
                    if( words != null )
                    {
                        for( int i = 0; i < words.length; i++ )
                        {
                            if(
                                words[ i ].length() >= settings_manager.getInt(
                                    "/misc/word memory/minimum word length",
                                    DEFAULT_MINIMUM_WORD_LENGTH
                                )
                            )
                            {
                                conversation_words.add( words[ i ] );
                            }
                        }
                    }
                    
                    String timestamp = GeoIRC.getATimeStamp(
                        settings_manager.getString( "/gui/format/timestamp", "" )
                    );
                    qualities += " " + tokens[ 2 ]
                        + " from=" + nick;
                    
                    extractVariables( text, qualities );

                    windows_printed_to += display_manager.println(
                        timestamp + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_QUIT ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String message = Util.stringArrayToString( tokens, 2 ).substring( 1 );  // remove leading colon

                    String text = getPadded( nick ) + " has quit (" + message + ").";
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
                    
                    extractVariables( message, qualities );
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
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
                        Vector v = handleNamesList( channel, namlist );
                        //channel.setChannelMembership( v );
                        channel.addToChannelMembership( v );
                    }
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_NOTOPIC ] ) )
                {
                    String channel = tokens[ 3 ].toLowerCase();
                    
                    qualities += " " + FILTER_SPECIAL_CHAR + "topic"
                        + " " + channel;
                    String text = channel + " has no topic set.";
                    
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_TOPIC ] ) )
                {
                    String channel = tokens[ 3 ].toLowerCase();
                    String topic = Util.stringArrayToString( tokens, 4 ).substring( 1 );  // remove leading colon
                    
                    qualities += " " + FILTER_SPECIAL_CHAR + "topic"
                        + " " + channel;
                    String text = "The topic of " + channel + " is: " + topic;
                    
                    extractVariables( topic, qualities );
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_RPL_TOPIC_SETTER ] ) )
                {
                    String channel = tokens[ 3 ].toLowerCase();
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
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_TOPIC ] ) )
                {
                    String nick = getNick( tokens[ 0 ] );
                    String channel = tokens[ 2 ].toLowerCase();
                    String topic = Util.stringArrayToString( tokens, 3 ).substring( 1 );  // remove leading colon
                    
                    qualities += " " + FILTER_SPECIAL_CHAR + "topic"
                        + " " + channel
                        + " from=" + nick;
                    String text = getPadded( nick ) + " has changed the topic for " + channel + " to: " + topic;
                    
                    extractVariables( topic, qualities );
                    windows_printed_to += display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + text,
                        qualities
                    );
                    trigger_manager.check( text, qualities );
                }
                else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_WELCOME ] ) )
                {
                    if( ! listening_to_channels )
                    {
                        restoreChannels();
                        listening_to_channels = true;
                        info_manager.addRemoteMachine( Server.this );
                    }
                    
                    /* Perhaps our suggested nick is longer than the maximum
                     * nick length allowed by the server, and was truncated.
                     * We shall use the welcome message to help us identify
                     * the nick that the server knows us by.
                     */
                    current_nick = tokens[ 2 ];
                    String message = Util.stringArrayToString( tokens, 3 );
                }
            }

            display_manager.println(
                transformed_message[ 0 ],
                transformed_message[ 1 ] + " "
                    + FILTER_SPECIAL_CHAR + "raw"
                    + (
                        ( windows_printed_to > 0 )
                        ? ( " " + FILTER_SPECIAL_CHAR + "printed" )
                        : ""
                    )
            );
        }
    }
}
