/*
 * DCCConnection.java
 *
 * Created on September 10, 2003, 10:28 AM
 */

package geoirc;

import geoirc.util.Util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 *
 * @author  Pistos
 */
public class DCCConnection extends Thread implements GeoIRCConstants
{
    protected ServerSocket listening_socket;
    protected Socket socket;
    protected int listening_port;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected TriggerManager trigger_manager;
    protected I18nManager i18n_manager;
    protected PrintWriter out;
    protected BufferedReader in;
    protected boolean closed;
    protected String offeree_nick;
    protected String user_nick;
    protected String remote_ip;
    protected String qualities;
    
    private DCCConnection() { }
    
    public DCCConnection(
        SettingsManager settings_manager,
        DisplayManager display_manager,
        TriggerManager trigger_manager,
        I18nManager i18n_manager,
        String offeree_nick,
        String user_nick
    ) throws IOException
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.trigger_manager = trigger_manager;
        this.i18n_manager = i18n_manager;
        this.offeree_nick = offeree_nick;
        this.user_nick = user_nick;
        
        int min_port = settings_manager.getInt(
            "/dcc/lowest port",
            DEFAULT_LOWEST_DCC_PORT
        );
        int max_port = settings_manager.getInt(
            "/dcc/highest port",
            DEFAULT_HIGHEST_DCC_PORT
        );
        int extent = max_port - min_port + 1;
        listening_port = (new Random()).nextInt( extent ) + min_port;
        
        socket = null;
        out = null;
        in = null;
        closed = false;
        remote_ip = null;
        qualities = null;
        listening_socket = new ServerSocket( listening_port );
    }
    
    public int listen()
    {
        super.start();
        
        return listening_port;
    }
    
    public int getListeningPort()
    {
        return listening_port;
    }
    
    public void println( String line )
    {
        if( out != null )
        {
            out.println( line );
        }
    }
    
    public void close()
    {
        closed = true;
    }
    
    public void run()
    {
        try
        {
            socket = listening_socket.accept();
            listening_socket.close();
            out = new PrintWriter( socket.getOutputStream(), true );
            in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        }
        catch( IOException e )
        {
            Util.printException(
                display_manager,
                e,
                i18n_manager.getString( "could not dcc connect" )
            );
        }
        
        if( socket != null )
        {
            remote_ip = socket.getInetAddress().getHostAddress();
            qualities =
                "dcc=" + remote_ip + " "
                + FILTER_SPECIAL_CHAR + "dccchat";
            String filter = 
                "dcc=" + remote_ip + " and "
                + FILTER_SPECIAL_CHAR + "dccchat";
            display_manager.addTextWindow( filter, filter );
            String line;
            try
            {
                while(
                    ( ( line = in.readLine() ) != null )
                    && ( ! closed )
                )
                {
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + "<" + offeree_nick + "> " + line,
                        qualities
                    );
                    trigger_manager.check( line, qualities );
                }
            }
            catch( IOException e )
            {
                Util.printException(
                    display_manager,
                    e,
                    i18n_manager.getString( "io exception 3", new Object [] { offeree_nick } )
                );
            }
        }
        
        try
        {
            in.close();
            out.close();
            socket.close();
        }
        catch( IOException e )
        {
            Util.printException(
                display_manager,
                e,
                i18n_manager.getString( "io exception 4", new Object [] { offeree_nick } )
            );
        }
        
    }
    
    public String toString()
    {
        return
            offeree_nick
            + "@" + remote_ip
            + (
                ( socket != null )
                ? " (" + i18n_manager.getString( "connected" ) + ")"
                : " (" + i18n_manager.getString( "connection inactive" ) + ")"
            );
    }
    
    public String getUserNick()
    {
        return user_nick;
    }
    
    public String getQualities()
    {
        return qualities;
    }
    
    public String getRemoteIPString()
    {
        return remote_ip;
    }
}
