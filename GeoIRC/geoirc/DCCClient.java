/*
 * DCCClient.java
 *
 * Created on August 16, 2003, 4:20 PM
 */

package geoirc;

import geoirc.util.Util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author  Pistos
 */
public class DCCClient extends RemoteMachine implements GeoIRCConstants
{
    protected int dcc_type;
    protected ScriptInterface script_interface;
    protected String user_nick;
    protected String remote_nick;
    
    public DCCClient(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        TriggerManager trigger_manager,
        ScriptInterface script_interface,
        String host_ip,
        String port,
        int dcc_type,
        String user_nick,
        String remote_nick
    )
    {
        super( parent, display_manager, settings_manager, trigger_manager, host_ip, port );
        this.script_interface = script_interface;
        this.dcc_type = dcc_type;
        this.remote_nick = remote_nick;
        this.user_nick = user_nick;
    }
    
    // Returns whether a connection has been established.
    public boolean connect()
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
                
                switch( dcc_type )
                {
                    case DCC_CHAT:
                        reader = new DCCChatReader( 
                            new BufferedReader(
                                new InputStreamReader(
                                    socket.getInputStream()
                                )
                            )
                        );
                        break;
                    case DCC_SEND:
                        break;
                }
                out = new PrintWriter( socket.getOutputStream(), true );
                
                if( reader != null )
                {
                    reader.start();
                }
                else
                {
                    display_manager.printlnDebug(
                        "Failed to create DCCChatReader."
                    );
                }
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
        
        return isConnected();
    }
    
    public String getUserNick()
    {
        return user_nick;
    }
    
    protected class DCCChatReader
        extends RemoteMachineReader
        implements GeoIRCConstants
    {
        protected BufferedReader in;

        // No default constructor.
        private DCCChatReader() { }

        public DCCChatReader(
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
            while( ( line != null ) && ( isConnected() ) && ( ! reset ) )
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
                        "I/O error while reading from ip " + DCCClient.this.toString()
                    );
                    
                    if( ! isConnected() && ( ! closed ) )
                    {
                        display_manager.printlnDebug( "Connection to " + DCCClient.this.toString() + " lost." );
                    }
                    
                    if( e.getMessage().equals( "Connection reset" ) )
                    {
                        display_manager.printlnDebug( "Connection to " + DCCClient.this.toString() + " reset." );
                        reset = true;
                    }
                }
            }
            
            if( ( ! isConnected() ) || reset )
            {
                if( ! isConnected() )
                {
                    display_manager.printlnDebug( "No longer connected to " + DCCClient.this.toString() );
                }
                
                /*
                if( ! closed )
                {
                    display_manager.printlnDebug( "Attempting to reconnect..." );
                    connect();
                }
                 */
            }
        }
        
        protected void interpretLine( String line )
        {
            String qualities =
                DCCClient.this.toString() + " "
                + FILTER_SPECIAL_CHAR + "dccchat";
            display_manager.println(
                GeoIRC.getATimeStamp(
                    settings_manager.getString( "/gui/format/timestamp", "" )
                ) + "<" + remote_nick + "> " + line,
                qualities
            );
            trigger_manager.check( line, qualities );
            script_interface.onRaw( line, DCCClient.this.toString() );
        }
        
    }    
}
