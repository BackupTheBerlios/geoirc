/*
 * RemoteMachine.java
 *
 * Created on June 25, 2003, 4:22 PM
 */

package geoirc;

import geoirc.util.Util;
import java.io.*;
import java.net.Socket;

/**
 *
 * @author  Pistos
 */
public class RemoteMachine implements GeoIRCConstants
{
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected I18nManager i18n_manager;
    protected String hostname;
    protected int port;
    protected GeoIRC geoirc;

    protected RemoteMachineReader reader;

    protected Socket socket;
    protected PrintWriter out;
    
    protected boolean closed;
    protected boolean reset;
    
    // No default constructor.
    private RemoteMachine() { }

    public RemoteMachine(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        I18nManager i18n_manager,
        String hostname,
        String port
    )
    {
        int i_port = DEFAULT_PORT;
        
        try
        {
            i_port = Integer.parseInt( port );
        }
        catch( NumberFormatException e )
        {
            // On error, keep default port.
        }

        this.hostname = hostname;
        this.port = i_port;
        this.display_manager = display_manager;
        this.settings_manager = settings_manager;
        this.i18n_manager = i18n_manager;
        geoirc = parent;
        
        socket = null;
        out = null;
        closed = false;
        reset = false;
        reader = null;
    }
    
    public void close()
    {
        closed = true;
        if( socket != null )
        {
            try
            {
                socket.close();
            }
            catch( IOException e )
            {
                Util.printException(
                    display_manager, e,
                    i18n_manager.getString( "io exception 7", new Object [] { toString() } )
                );
            }
        }
    }
    
    public boolean isConnected()
    {
        return (
            ( socket != null )
            && socket.isConnected()
            && ( ! socket.isClosed() )
        );
    }
    
    // Sends a line out to the server, including newline.
    public void send( String text )
    {
        if( out != null )
        {
            out.println( text );
        }
    }
    
    public String toString()
    {
        return ( hostname /* + Integer.toString( port ) */ );
    }
    
    public String getHostname()
    {
        return hostname;
    }
    
    public int getPort()
    {
        return port;
    }
}
