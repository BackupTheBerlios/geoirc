/*
 * DCCConnection.java
 *
 * Created on September 10, 2003, 10:28 AM
 */

package geoirc;

import geoirc.gui.DisplayManager;
import geoirc.util.Util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
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
    protected GeoIRC geoirc;
    protected ServerSocket listening_socket;
    protected Socket socket;
    protected int listening_port;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected I18nManager i18n_manager;
    protected PrintWriter text_out;
    protected BufferedReader text_in;
    protected BufferedOutputStream out;
    protected BufferedInputStream in;
    protected boolean closed;
    protected String offeree_nick;
    protected String user_nick;
    protected String remote_ip;
    protected String qualities;
    protected int type;
    protected BufferedInputStream file;
    
    private DCCConnection() { }
    
    public DCCConnection(
        GeoIRC parent,
        SettingsManager settings_manager,
        DisplayManager display_manager,
        I18nManager i18n_manager,
        int type,
        String offeree_nick,
        String user_nick,
        BufferedInputStream file
    ) throws IOException
    {
        geoirc = parent;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        this.type = type;
        this.offeree_nick = offeree_nick;
        this.user_nick = user_nick;
        this.file = file;
        
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
        text_out = null;
        text_in = null;
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
        if( text_out != null )
        {
            text_out.println( line );
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
            
            switch( type )
            {
                case DCC_CHAT:
                    text_out = new PrintWriter( socket.getOutputStream(), true );
                    text_in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                    break;
                case DCC_SEND:
                    out = new BufferedOutputStream( socket.getOutputStream() );
                    in = new BufferedInputStream( socket.getInputStream() );
                    break;
            }
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
            
            switch( type )
            {
                case DCC_CHAT:
                {
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
                            ( ( line = text_in.readLine() ) != null )
                            && ( ! closed )
                        )
                        {
                            display_manager.println(
                                GeoIRC.getATimeStamp(
                                    settings_manager.getString( "/gui/format/timestamp", "" )
                                ) + "<" + offeree_nick + "> " + line,
                                qualities
                            );
                            geoirc.checkAgainstTriggers( line, qualities );
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
                    break;
                }
                case DCC_SEND:
                {
                    try
                    {
                        int character = 0;
                        int bytes_sent = 0;
                        int packet_size = settings_manager.getInt(
                            "/dcc/file transfers/packet size",
                            DEFAULT_PACKET_SIZE
                        );
                        file.mark( packet_size + 1 );
                        
                        while(
                            ( ( character = file.read() ) != -1 )
                            && ( ! closed )
                        )
                        {
                            out.write( character );
                            bytes_sent++;
                            if( ( bytes_sent % packet_size == 0 ) /*|| ( in.available() > 0 )*/ )
                            {
                                out.flush();
                                
                                // Wait for remote acknowledgement of bytes sent so far.
                                byte [] b_bytes_received = new byte[ 4 ];
                                int four_bytes = in.read( b_bytes_received, 0, 4 );
                                if( four_bytes < 4 )
                                {
                                    throw new IOException(
                                        i18n_manager.getString( "bad ack" )
                                    );
                                }
                                int i_bytes_received = Util.networkByteOrderToInt( b_bytes_received );
                                if( i_bytes_received < bytes_sent )
                                {
                                    /*
                                    file.reset();
                                    long num_skipped = file.skip( bytes_sent - i_bytes_received );
                                    if( num_skipped != (long) (bytes_sent - i_bytes_received) )
                                    {
                                        throw new IOException(
                                            i18n_manager.getString( "bytes lost" )
                                        );
                                    }
                                    bytes_sent -= packet_size - num_skipped;
                                     */
                                }
                                else
                                {
                                    file.mark( packet_size + 1 );
                                }
                            }
                        }
                        
                        out.flush();
                    }
                    catch( IOException e )
                    {
                        Util.printException(
                            display_manager,
                            e,
                            i18n_manager.getString( "io exception 11", new Object [] { offeree_nick } )
                        );
                    }
                    break;
                }
            }
        }
        
        try
        {
            if( text_in != null ) { text_in.close(); }
            if( text_out != null ) { text_out.close(); }
            if( in != null ) { in.close(); }
            if( out != null ) { out.close(); }
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
