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
    protected boolean expecting_pong;
    
    public Server(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        TriggerManager trigger_manager,
        InfoManager info_manager,
        VariableManager variable_manager,
        I18nManager i18n_manager,
        Set conversation_words,
        String hostname,
        String port
    )
    {
        super( parent, display_manager, settings_manager, trigger_manager, i18n_manager, hostname, port );
        
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
                /*
                if( reader != null )
                {
                    while( reader.isAlive() )
                    {
                        try {
                            Thread.sleep( DELAY_FOR_SERVER_READER_DEATH );
                        } catch( InterruptedException e ) { }
                    }
                }
                 */
                
                socket.setSoTimeout(
                    settings_manager.getInt( "/misc/server timeout", DEFAULT_SERVER_TIMEOUT )
                );
                
                current_nick = nick_to_use;
                
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
                    + settings_manager.getString( "/personal/ident/username", "geoircuser" )
                    + " x x :Pi Gi" );
                
            }
        }
        catch( UnknownHostException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "unknown host", new Object [] { hostname } )
            );
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
            Channel channel = new Channel( this, channel_name, info_manager, settings_manager, display_manager, i18n_manager );
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
            boolean timed_out = false;
            expecting_pong = false;
            while( ( line != null ) && isConnected() && ( ! reset ) )
            {
                try
                {
                    if( ! timed_out )
                    {
                        interpretLine( STAGE_SCRIPTING, new String [] { line, Server.this.toString() } );
                    }
                }
                catch( NullPointerException e )
                {
                    Util.printException(
                        display_manager,
                        e,
                        i18n_manager.getString( "interpretline npe", new Object [] { line } )
                    );
                }
                
                try
                {
                    /*
                    while( ! in.ready() )
                    {
                        if( ! isConnected() )
                        {
                            disconnected = true;
                            break;
                        }
                        try
                        {
                            Thread.sleep( SERVER_READER_POLLING_INTERVAL );
                        } catch( InterruptedException e ) { }
                    }
                     */
                    
                    timed_out = false;
                    try
                    {
                        line = in.readLine();
                    }
                    catch( java.io.InterruptedIOException e )
                    {
                        timed_out = true;
                        if( expecting_pong )
                        {
                            throw new IOException( i18n_manager.getString( "ping timeout" ) );
                        }
                        else
                        {
                            expecting_pong = true;
                            send( "PING GeoIRC" );
                        }
                    }
                }
                catch( IOException e )
                {
                    Util.printException(
                        display_manager,
                        e,
                        i18n_manager.getString( "io exception 8", new Object [] { Server.this.toString() } )
                    );
                    
                    if( ! isConnected() && ( ! closed ) )
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString(
                                "connection lost",
                                new Object [] { Server.this.toString() }
                            )
                        );
                    }
                    
                    if( e.getMessage().equals( "Connection reset" ) )
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString(
                                "connection reset",
                                new Object [] { Server.this.toString() }
                            )
                        );
                        reset = true;
                    }
                }
            }
            
            if( ( ! isConnected() ) || reset )
            {
                //geoirc.recordConnections();
                
                display_manager.printlnDebug(
                    i18n_manager.getString(
                        "no longer connected",
                        new Object [] { Server.this.toString() }
                    )
                );
                listening_to_channels = false;
                channels = new Vector();
                info_manager.removeRemoteMachine( Server.this );
                
                if( ! closed )
                {
                    display_manager.printlnDebug( i18n_manager.getString( "attempting reconnect" ) );
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
                    display_manager.printlnDebug(
                        i18n_manager.getString( "filter error", new Object [] { filter } )
                    );
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
        
        protected void interpretLine( int stage, String [] transformed_message_ )
        {
            String [] transformed_message = transformed_message_;
            
            int windows_printed_to = 0;
            String [] tokens = Util.tokensToArray( transformed_message[ MSG_TEXT ] );
            if( tokens == null )
            {
                return;
            }
            
            String irc_code = "";
            if( tokens.length > 1 )
            {
                irc_code = tokens[ 1 ];
            }
            
            String code_string;

            try
            {
                Integer.parseInt( irc_code );
                code_string = " ncode=" + irc_code;
            }
            catch( NumberFormatException e )
            {
                code_string = " acode=" + irc_code;
            }

            if( irc_code.equals( IRCMSGS[ IRCMSG_ERR_NICKNAMEINUSE ] ) )
            {
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " ncode="
                                + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            int nick_index = 1;
                            String another_nick;
                            while( GOD_IS_GOOD )
                            {
                                another_nick = settings_manager.getString(
                                    "/personal/nick" + Integer.toString( nick_index ),
                                    ""
                                );

                                if( another_nick.equals( current_nick ) )
                                {
                                    nick_index++;
                                    another_nick = settings_manager.getString(
                                        "/personal/nick" + Integer.toString( nick_index ),
                                        ""
                                    );
                                    if( another_nick.equals( "" ) )
                                    {
                                        another_nick = current_nick;
                                        display_manager.printlnDebug(
                                            i18n_manager.getString( "no more nicks" )
                                        );
                                    }
                                    break;
                                }
                                else if( another_nick.equals( "" ) )
                                {
                                    // Current nick is not any of the nicks in the settings.
                                    current_nick = settings_manager.getString(
                                        "/personal/nick1",
                                        ""
                                    );
                                    break;
                                }

                                nick_index++;
                            }

                            if( ! another_nick.equals( "" ) )
                            {
                                send( "NICK " + another_nick );
                            }
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_JOIN ] ) ) && ( tokens.length > 2 ) )
            {
                String nick = getNick( tokens[ 0 ] );
                String channel = tokens[ 2 ].toLowerCase();
                if( channel.charAt( 0 ) == ':' )
                {
                    // Remove leading colon.
                    channel = channel.substring( 1 );
                }

                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " " + channel
                                + " from=" + nick
                                + " " + FILTER_SPECIAL_CHAR + "join"
                                + " acode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String text;
                            String padded_nick = getPadded( nick );
                            String user_string = "";
                            
                            if(
                                settings_manager.getBoolean(
                                    "/gui/format/complete join message", false
                                ) == true
                            )
                            {                    
                                user_string = " (" + getUserNameAndHost( tokens[ 0 ] ) + ")";
                            }
                            text = i18n_manager.getString(
                                "join",
                                new Object [] { padded_nick, nick, user_string, channel }
                            );

                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );

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
                                        i18n_manager.getString(
                                            "no channel object", new Object [] { channel }
                                        )
                                    );
                                }
                            }
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_KICK ] ) ) && ( tokens.length > 3 ) )
            {
                // :kez!kez@modem-302.bear.dialup.pol.co.uk KICK #GeoShell GeoBot :kez
                
                String kicker = getNick( tokens[ 0 ] );
                String channel = tokens[ 2 ].toLowerCase();
                String nick = tokens[ 3 ];
                String message = Util.stringArrayToString( tokens, 4 );
                
                if( ( message != null ) && ( message.charAt( 0 ) == ':' ) )
                {
                    message = message.substring( 1 );  // remove leading colon
                }
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " " + channel
                                + " from=" + kicker
                                + " victim=" + nick
                                + " " + FILTER_SPECIAL_CHAR + "kick"
                                + " acode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String text;
                            String padded_kicker = getPadded( kicker );
                            text = i18n_manager.getString(
                                "kick",
                                new Object [] { padded_kicker, kicker, nick, channel, message }
                            );

                            User user = getUserByNick( nick );
                            if( user != null )
                            {
                                user.noteActivity();
                            }

                            if( message != null )
                            {
                                extractVariables( message, transformed_message[ MSG_QUALITIES ] );
                            }

                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );

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
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_MODE ] ) ) && ( tokens.length > 2 ) )
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
                        }
                        String channel = channel_or_nick;
                        String polarity = tokens[ 3 ].substring( 0, 1 );
                        String mode = tokens[ 3 ].substring( 1, 2 );
                        String arg = tokens[ 4 ];
                        Channel c = Server.this.getChannelByName( channel );

                        switch( stage )
                        {
                            case STAGE_SCRIPTING:
                            {
                                String qualities = transformed_message[ MSG_QUALITIES ]
                                    + " " + channel
                                    + " from=" + nick
                                    + " " + FILTER_SPECIAL_CHAR + "mode"
                                    + " mode=" + mode
                                    + " polarity=" + polarity
                                    + " acode=" + irc_code;

                                if(
                                    ( mode.equals( MODE_OP ) )
                                    || ( mode.equals( MODE_HALFOP ) )
                                    || ( mode.equals( MODE_VOICE ) )
                                )
                                {
                                    qualities += " recipient=" + nick;
                                }

                                transformed_message = geoirc.onRaw(
                                    transformed_message[ MSG_TEXT ],
                                    qualities
                                );
                            }
                            break;
                            case STAGE_PROCESSING:
                            {
                                String text = null;
                                if( user != null )
                                {
                                    user.noteActivity();
                                }
                                String padded_nick = getPadded( nick );

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
                                            text = i18n_manager.getString(
                                                "chanop give",
                                                new Object [] { padded_nick, nick, channel, arg }
                                            );
                                        }
                                        else if( polarity.equals( "-" ) )
                                        {
                                            recipient_user.removeModeFlag( c, MODE_OP );
                                            c.acknowledgeUserChange( user );
                                            c.acknowledgeUserChange( recipient_user );
                                            text = i18n_manager.getString(
                                                "chanop take",
                                                new Object [] { padded_nick, nick, channel, arg }
                                            );
                                        }
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
                                            text = i18n_manager.getString(
                                                "halfop give",
                                                new Object [] { padded_nick, nick, channel, arg }
                                            );
                                        }
                                        else if( polarity.equals( "-" ) )
                                        {
                                            recipient_user.removeModeFlag( c, MODE_HALFOP );
                                            c.acknowledgeUserChange( user );
                                            c.acknowledgeUserChange( recipient_user );
                                            text = i18n_manager.getString(
                                                "halfop take",
                                                new Object [] { padded_nick, nick, channel, arg }
                                            );
                                        }
                                    }
                                }
                                else if( mode.equals( "p" ) )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "private channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "nonprivate channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                }
                                else if( mode.equals( "s" ) )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "secret channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "nonsecret channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                }
                                else if( mode.equals( "i" ) )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "invite channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "noninvite channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                }
                                else if( mode.equals( "t" ) )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "topic chanop",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "topic all",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                }
                                else if( mode.equals( "n" ) )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "members only",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "not members only",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                }
                                else if( mode.equals( "m" ) )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "moderated channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "unmoderated channel",
                                            new Object [] { padded_nick, nick, channel }
                                        );
                                    }
                                }
                                else if( mode.equals( "b" ) )
                                {
                                    if( polarity.equals( "+" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "ban",
                                            new Object [] { padded_nick, nick, arg, channel }
                                        );
                                    }
                                    else if( polarity.equals( "-" ) )
                                    {
                                        text = i18n_manager.getString(
                                            "unban",
                                            new Object [] { padded_nick, nick, arg, channel }
                                        );
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
                                            text = i18n_manager.getString(
                                                "voice give",
                                                new Object [] { padded_nick, nick, channel, arg }
                                            );
                                        }
                                        else if( polarity.equals( "-" ) )
                                        {
                                            recipient_user.removeModeFlag( c, MODE_VOICE );
                                            c.acknowledgeUserChange( user );
                                            c.acknowledgeUserChange( recipient_user );
                                            text = i18n_manager.getString(
                                                "voice take",
                                                new Object [] { padded_nick, nick, channel, arg }
                                            );
                                        }
                                    }
                                }

                                windows_printed_to += display_manager.println(
                                    GeoIRC.getATimeStamp(
                                        settings_manager.getString( "/gui/format/timestamp", "" )
                                    ) + text,
                                    transformed_message[ MSG_QUALITIES ]
                                );
                                trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                            }
                            break;
                        }
                    }
                    catch( ArrayIndexOutOfBoundsException e )
                    {
                    }
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_NICK ] ) ) && ( tokens.length > 2 ) )
            {
                String old_nick = getNick( tokens[ 0 ] );
                String new_nick = tokens[ 2 ].substring( 1 );  // Remove leading colon.
                User user = null;
                if( stage == STAGE_PROCESSING )
                {
                    user = getUserByNick( old_nick );
                    if( user != null )
                    {
                        user.setNick( new_nick );
                        user.noteActivity();
                    }
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
                        switch( stage )
                        {
                            case STAGE_SCRIPTING:
                                transformed_message[ MSG_QUALITIES ] += " " + c.getName();
                                break;
                            case STAGE_PROCESSING:
                                c.acknowledgeUserChange( user );
                                break;
                        }
                    }

                    if( stage == STAGE_SCRIPTING )
                    {
                        transformed_message[ MSG_QUALITIES ] += " " + FILTER_SPECIAL_CHAR + "self";
                    }
                }
                else
                {
                    // Someone else's nick changed.

                    int n = channels.size();
                    for( int i = 0; i < n; i++ )
                    {
                        c = (Channel) channels.elementAt( i );
                        String nick_to_check = "";
                        switch( stage )
                        {
                            case STAGE_SCRIPTING:
                                nick_to_check = old_nick;
                                break;
                            case STAGE_PROCESSING:
                                nick_to_check = new_nick;
                                break;
                        }
                        if( c.nickIsPresent( nick_to_check ) )
                        {
                            switch( stage )
                            {
                                case STAGE_SCRIPTING:
                                    transformed_message[ MSG_QUALITIES ] += " " + c.getName();
                                    break;
                                case STAGE_PROCESSING:
                                    c.acknowledgeUserChange( user );
                                    break;
                            }
                        }
                    }
                }

                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " from=" + new_nick
                                + " " + FILTER_SPECIAL_CHAR + "nick"
                                + " acode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String text;
                            String padded_nick = getPadded( old_nick );
                            text = i18n_manager.getString(
                                "nick change",
                                new Object [] { padded_nick, old_nick, new_nick }
                            );
                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_NOTICE ] ) ) && ( tokens.length > 3 ) )
            {
                String nick = getNick( tokens[ 0 ] );
                String text = Util.stringArrayToString( tokens, 3 );
                text = text.substring( 1 );  // Remove leading colon.

                if( stage == STAGE_SCRIPTING )
                {
                    transformed_message[ MSG_QUALITIES ] +=
                        " " + FILTER_SPECIAL_CHAR + "notice"
                        + " " + tokens[ 2 ]
                        + " from=" + nick;
                }
                
                boolean notices_in_active = settings_manager.getBoolean(
                    "/gui/notices in active window",
                    true
                );
                
                User user = getUserByNick( nick );
                if( user != null )
                {
                    user.noteActivity();

                    if( tokens[ 2 ].equals( current_nick ) )
                    {
                        // Notice to GeoIRC user.

                        switch( stage )
                        {
                            case STAGE_SCRIPTING:
                                transformed_message[ MSG_QUALITIES ] += " " + FILTER_SPECIAL_CHAR + "self";
                                break;
                            case STAGE_PROCESSING:
                                if( ! notices_in_active )
                                {
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
                                }
                                break;
                        }
                    }

                    if( stage == STAGE_PROCESSING )
                    {
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
                }

                if( 
                    ( text.length() > 0 )
                    && ( text.charAt( 0 ) == CTCP_MARKER )
                )
                {
                    // CTCP message.

                    switch( stage )
                    {
                        case STAGE_SCRIPTING:
                            transformed_message[ MSG_QUALITIES ] += " " + FILTER_SPECIAL_CHAR + "ctcp";
                            break;
                        case STAGE_PROCESSING:
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

                                text = i18n_manager.getString(
                                    "received ctcp",
                                    new Object [] { nick, ctcp_message }
                                );
                            }
                            break;
                    }
                }
                else
                {
                    if( stage == STAGE_PROCESSING )
                    {
                        text = "-" + nick + "- " + text;
                    }
                }

                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                            + " acode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String timestamp = GeoIRC.getATimeStamp(
                                settings_manager.getString( "/gui/format/timestamp", "" )
                            );

                            extractVariables( text, transformed_message[ MSG_QUALITIES ] );
                            if( notices_in_active )
                            {
                                windows_printed_to++;
                                display_manager.printlnToActiveTextPane( timestamp + text );
                            }
                            else
                            {
                                windows_printed_to += display_manager.println(
                                    timestamp + text,
                                    transformed_message[ MSG_QUALITIES ]
                                );
                            }
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_PART ] ) ) && ( tokens.length > 2 ) )
            {
                String nick = getNick( tokens[ 0 ] );
                String channel = tokens[ 2 ].toLowerCase();
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " " + channel
                                + " from=" + nick
                                + " " + FILTER_SPECIAL_CHAR + "part"
                                + " acode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
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

                            String padded_nick = getPadded( nick );
                            String text = i18n_manager.getString(
                                "part",
                                new Object [] { padded_nick, nick, channel, message }
                            );

                            if( message != null )
                            {
                                extractVariables( message, transformed_message[ MSG_QUALITIES ] );
                            }

                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );

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
                        break;
                }
            }
            else if( tokens[ 0 ].equals( IRCMSGS[ IRCMSG_PING ] ) )
            {
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                            + " " + FILTER_SPECIAL_CHAR + "pong"
                            + " acode=" + irc_code
                            + " from=" + FILTER_SPECIAL_CHAR + "self"
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String pong_arg = "GeoIRC";
                            if( tokens.length > 1 )
                            {
                                pong_arg = tokens[ 1 ];
                            }
                            send( "PONG " + pong_arg );
                            display_manager.println(
                                i18n_manager.getString(
                                    "pong sent",
                                    new Object [] { Server.this.toString() }
                                ),
                                transformed_message[ MSG_QUALITIES ]
                            );
                        }
                        break;
                }
            }
            else if( irc_code.equals( IRCMSGS[ IRCMSG_PONG ] ) )
            {
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                            + " " + FILTER_SPECIAL_CHAR + "pong"
                            + " acode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                    {
                        expecting_pong = false;
                        String text = i18n_manager.getString(
                            "pong received",
                            new Object [] { Server.this.toString() }
                        );
                        windows_printed_to += display_manager.println(
                            text,
                            transformed_message[ MSG_QUALITIES ]
                        );
                        trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        break;
                    }
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_PRIVMSG ] ) ) && ( tokens.length > 3 ) )
            {
                String nick = getNick( tokens[ 0 ] );
                if( stage == STAGE_SCRIPTING )
                {
                    transformed_message[ MSG_QUALITIES ]
                        += " " + FILTER_SPECIAL_CHAR + "privmsg"
                        + " " + tokens[ 2 ]
                        + " from=" + nick
                        + " acode=" + irc_code;
                }
                String text = Util.stringArrayToString( tokens, 3 );
                text = text.substring( 1 );  // Remove leading colon.
                String [] words = null;

                User user = getUserByNick( nick );
                if( user != null )
                {
                    switch( stage )
                    {
                        case STAGE_SCRIPTING:
                            if( tokens[ 2 ].equals( current_nick ) )
                            {
                                transformed_message[ MSG_QUALITIES ]
                                    += " " + FILTER_SPECIAL_CHAR + "self";
                            }
                            break;
                        case STAGE_PROCESSING:
                            {
                                user.noteActivity();
                                
                                if( tokens[ 2 ].equals( current_nick ) )
                                {
                                    // Message to GeoIRC user.

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
                            break;
                    }
                }
                else if( stage == STAGE_PROCESSING )
                {
                    display_manager.printlnDebug(
                        "Warning: User '" + nick + "' not in user list for "
                        + Server.this.toString()
                    );
                }
                
                String padded_nick = getPadded( nick );

                if( text.length() > 0 )
                {
                    if(
                        ( text.charAt( 0 ) == CTCP_MARKER )
                        && ( text.charAt( text.length() - 1 ) == CTCP_MARKER )
                    )
                    {
                        // CTCP message.

                        if( stage == STAGE_SCRIPTING )
                        {
                            transformed_message[ MSG_QUALITIES ]
                                += " " + FILTER_SPECIAL_CHAR + "ctcp";
                        }

                        if( text.substring( 1, 7 ).equals( "ACTION" ) )
                        {
                            // For legacy reasons, we'll treat ACTIONs in a different way.

                            switch( stage )
                            {
                                case STAGE_SCRIPTING:
                                    transformed_message[ MSG_QUALITIES ]
                                        += " " + FILTER_SPECIAL_CHAR + "action";
                                    break;
                                case STAGE_PROCESSING:
                                    {
                                        String text2 = Util.stringArrayToString( tokens, 4 );
                                        words = Util.tokensToArray(
                                            text2.substring( 0, text2.length() - 1 )
                                        );
                                        text =
                                            getPadded( "* " + nick )
                                            + text.substring( 7, text.length() - 1 );
                                    }
                                    break;
                            }
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

                            if( ( command_id != UNKNOWN_CTCP_CMD ) && ( stage == STAGE_PROCESSING ) )
                            {
                                text = i18n_manager.getString(
                                    "got ctcp",
                                    new Object [] { CTCP_CMDS[ command_id ], nick }
                                );
                            }

                            switch( stage )
                            {
                                case STAGE_SCRIPTING:
                                    switch( command_id )
                                    {
                                        case CTCP_CMD_PAGE:
                                            transformed_message[ MSG_QUALITIES ]
                                                += " " + FILTER_SPECIAL_CHAR + "page";
                                            break;
                                    }
                                    break;
                                case STAGE_PROCESSING:
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
                                        case CTCP_CMD_DCC:
                                            try
                                            {
                                                geoirc.addDCCRequest( args, nick );
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
                                        case CTCP_CMD_PING:
                                            send(
                                                IRCMSGS[ IRCMSG_NOTICE ] + " "
                                                + nick + " :"
                                                + CTCP_MARKER 
                                                + CTCP_CMDS[ CTCP_CMD_PING ]
                                                + " " + arg_string
                                                + CTCP_MARKER
                                            );
                                            break;
                                        default:
                                            text = i18n_manager.getString(
                                                "unknown ctcp",
                                                new Object [] { nick, ctcp_message }
                                            );
                                            break;
                                    }
                                    break;
                            }
                        }
                    }
                    else if( stage == STAGE_PROCESSING )
                    {
                        words = Util.tokensToArray( text );
                        text = getPadded( "<" + nick + ">" ) + " " + text;
                    }
                }
                else if( stage == STAGE_PROCESSING )
                {
                    text = getPadded( "<" + nick + ">" );
                }

                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
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

                            extractVariables( text, transformed_message[ MSG_QUALITIES ] );

                            windows_printed_to += display_manager.println(
                                timestamp + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        }
                        break;
                }
            }
            else if( irc_code.equals( IRCMSGS[ IRCMSG_QUIT ] ) )
            {
                String nick = getNick( tokens[ 0 ] );
                String message = null;
                String text = "";
                
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message[ MSG_QUALITIES ]
                            += " from=" + nick
                            + " " + FILTER_SPECIAL_CHAR + "quit"
                            + " acode=" + irc_code;
                        if( ! nick.equals( current_nick ) )
                        {
                            Channel channel = null;
                            for( int i = 0, n = channels.size(); i < n; i++ )
                            {
                                channel = (Channel) channels.elementAt( i );
                                if( channel.nickIsPresent( nick ) )
                                {
                                    transformed_message[ MSG_QUALITIES ] += " " + channel.getName();
                                }
                            }
                        }
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                        );
                        break;
                    case STAGE_PROCESSING:
                        message = Util.stringArrayToString( tokens, 2 ).substring( 1 );  // remove leading colon
                        String padded_nick = getPadded( nick );
                        text = i18n_manager.getString(
                            "quit",
                            new Object [] { padded_nick, nick, message }
                        );

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
                                    user = channel.removeMember( nick );
                                }
                            }
                            if( user != null )
                            {
                                users.remove( user );
                            }
                        }

                        extractVariables( message, transformed_message[ MSG_QUALITIES ] );
                        windows_printed_to += display_manager.println(
                            GeoIRC.getATimeStamp(
                                settings_manager.getString( "/gui/format/timestamp", "" )
                            ) + text,
                            transformed_message[ MSG_QUALITIES ]
                        );
                        trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        break;
                }
            }
            else if( irc_code.equals( IRCMSGS[ IRCMSG_RPL_ENDOFNAMES ] ) )
            {
                /* Example:
                :calvino.freenode.net 366 GeoIRC_User #geoirc :End of /NAMES list.
                 */
                if( stage == STAGE_SCRIPTING )
                {
                    transformed_message = geoirc.onRaw(
                        transformed_message[ MSG_TEXT ],
                        transformed_message[ MSG_QUALITIES ]
                        + " ncode=" + irc_code
                    );
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_RPL_NAMREPLY ] ) ) && ( tokens.length > 5 ) )
            {
                /* Example:
                :calvino.freenode.net 353 GeoIRC_User = #geoirc :GeoIRC_User GeoBot Fluff @ChanServ 
                 */
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                            + " ncode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            Channel channel = getChannelByName( tokens[ 4 ] );
                            if( channel != null )
                            {
                                String namlist = Util.stringArrayToString( tokens, 5 );
                                namlist = namlist.substring( 1 );  // remove leading colon
                                Vector v = handleNamesList( channel, namlist );
                                channel.addToChannelMembership( v );
                            }
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_RPL_NOTOPIC ] ) ) && ( tokens.length > 3 ) )
            {
                String channel = tokens[ 3 ].toLowerCase();

                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " " + FILTER_SPECIAL_CHAR + "topic"
                                + " " + channel
                                + " ncode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String text = i18n_manager.getString(
                                "no topic",
                                new Object [] { channel }
                            );

                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_RPL_TOPIC ] ) ) && ( tokens.length > 3 ) )
            {
                String channel = tokens[ 3 ].toLowerCase();
                
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " " + FILTER_SPECIAL_CHAR + "topic"
                                + " " + channel
                                + " ncode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String topic = Util.stringArrayToString( tokens, 4 ).substring( 1 );  // remove leading colon
                            String text = i18n_manager.getString(
                                "topic",
                                new Object [] { channel, topic }
                            );

                            extractVariables( topic, transformed_message[ MSG_QUALITIES ] );
                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_RPL_TOPIC_SETTER ] ) ) && ( tokens.length > 3 ) )
            {
                String channel = tokens[ 3 ].toLowerCase();
                
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " " + FILTER_SPECIAL_CHAR + "topicsetter"
                                + " " + channel
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String setter = tokens[ 4 ];
                            String time_str = tokens[ 5 ];
                            long time_in_seconds;
                            String time = i18n_manager.getString( "unknown datetime" );
                            try
                            {
                                time_in_seconds = Integer.parseInt( time_str );
                                DateFormat df = DateFormat.getDateTimeInstance(
                                    DateFormat.LONG, DateFormat.LONG, i18n_manager.getLocale()
                                );
                                time = df.format( new Date( time_in_seconds * 1000 ) );
                            } catch( NumberFormatException e ) { }

                            String text = i18n_manager.getString(
                                "topic setter",
                                new Object [] { channel, setter, time }
                            );
                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_TOPIC ] ) ) && ( tokens.length > 2 ) )
            {
                String nick = getNick( tokens[ 0 ] );
                String channel = tokens[ 2 ].toLowerCase();
                
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                                + " " + FILTER_SPECIAL_CHAR + "topic"
                                + " " + channel
                                + " from=" + nick
                        );
                        break;
                    case STAGE_PROCESSING:
                        {
                            String topic = Util.stringArrayToString( tokens, 3 ).substring( 1 );  // remove leading colon

                            String padded_nick = getPadded( nick );
                            String text = i18n_manager.getString(
                                "topic change",
                                new Object [] { padded_nick, nick, channel, topic }
                            );

                            extractVariables( topic, transformed_message[ MSG_QUALITIES ] );
                            windows_printed_to += display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + text,
                                transformed_message[ MSG_QUALITIES ]
                            );
                            trigger_manager.check( text, transformed_message[ MSG_QUALITIES ] );
                        }
                        break;
                }
            }
            else if( ( irc_code.equals( IRCMSGS[ IRCMSG_WELCOME ] ) ) && ( tokens.length > 2 ) )
            {
                switch( stage )
                {
                    case STAGE_SCRIPTING:
                        transformed_message = geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                            + " ncode=" + irc_code
                        );
                        break;
                    case STAGE_PROCESSING:
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
                        //String message = Util.stringArrayToString( tokens, 3 );
                        break;
                }
            }
            else if( stage == STAGE_SCRIPTING )
            {
                // This IRC message is not [yet] handled by GeoIRC.
                
                transformed_message = geoirc.onRaw(
                    transformed_message[ MSG_TEXT ],
                    transformed_message[ MSG_QUALITIES ]
                    + code_string + irc_code
                );
            }

            switch( stage )
            {
                case STAGE_SCRIPTING:
                    interpretLine( STAGE_PROCESSING, transformed_message );
                    break;
                case STAGE_PROCESSING:
                    display_manager.println(
                        transformed_message_[ MSG_TEXT ],
                        Server.this.toString() + " "
                            + FILTER_SPECIAL_CHAR + "raw"
                            + code_string
                            + (
                                ( windows_printed_to > 0 )
                                ? ( " " + FILTER_SPECIAL_CHAR + "printed" )
                                : ""
                            )
                    );
                    break;
            }
        }
    }
    
}
