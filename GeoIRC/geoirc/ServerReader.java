/*
 * ServerReader.java
 *
 * Created on June 23, 2003, 11:02 PM
 */

package geoirc;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author  Pistos
 */
public class ServerReader
    extends java.lang.Thread
    implements GeoIRCConstants
{
    protected Server server;
    protected BufferedReader in;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    
    // No default constructor.
    private ServerReader() { }
    
    public ServerReader(
        Server parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        BufferedReader in
    )
    {
        this.in = in;
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;
        server = parent;
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
    
    protected void interpretLine( String line )
    {
        /*
         * Example PRIVMSGs:
         *
         * :Pistos!Pistos@CPE0050ba18d2d5-CM000039ed745e.cpe.net.cable.rogers.com PRIVMSG #geoirc :I'm in Canada.
         * :livesN[box]!Jumper2@207.0.237.192 PRIVMSG #geoirc :ah.        
         */
        
        String [] tokens = Util.tokensToArray( line );
        if( tokens != null )
        {
            if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_PRIVMSG ] ) )
            {
                String nick = tokens[ 0 ].substring( 1, tokens[ 0 ].indexOf( "!" ) );
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

                display_manager.println(
                    timestamp + text,
                    server.toString()
                    + " " + tokens[ 2 ]
                    + " from=" + nick
                );
            }
            else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_JOIN ] ) )
            {
                String nick = tokens[ 0 ].substring( 1, tokens[ 0 ].indexOf( "!" ) );
                String channel = tokens[ 2 ].substring( 1 );  // Remove leading colon.
                String text = nick + " joined " + channel + ".";
                display_manager.println(
                    GeoIRC.getATimeStamp(
                        settings_manager.getString( "/gui/format/timestamp", "" )
                    ) + text,
                    server.toString()
                    + " " + channel
                    + " from=" + nick
                    + " join"
                );
            }
            else if( tokens[ 1 ].equals( IRCMSGS[ IRCMSG_PART ] ) )
            {
                String nick = tokens[ 0 ].substring( 1, tokens[ 0 ].indexOf( "!" ) );
                String channel = tokens[ 2 ];
                String message = Util.stringArrayToString( tokens, 3 ).substring( 1 );  // remove leading colon
                String text = nick + " left " + channel + " (" + message + ").";
                display_manager.println(
                    GeoIRC.getATimeStamp(
                        settings_manager.getString( "/gui/format/timestamp", "" )
                    ) + text,
                    server.toString()
                    + " " + channel
                    + " from=" + nick
                    + " part"
                );
            }

        }
        
        display_manager.println( line, server.toString() );
    }
}
