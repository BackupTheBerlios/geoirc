/*
 * RemoteMachine.java
 *
 * Created on June 25, 2003, 4:22 PM
 */

package geoirc;

import java.io.*;
import java.net.Socket;

/**
 *
 * @author  Pistos
 */
public class RemoteMachine
{
    public static final int DEFAULT_PORT = 6667;

    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected SoundManager sound_manager;
    protected String hostname;
    protected int port;
    protected GeoIRC geoirc;
    
    protected Socket socket;
    protected PrintWriter out;
    
    
    // No default constructor.
    private RemoteMachine() { }

    public RemoteMachine(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        SoundManager sound_manager,
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
        this.sound_manager = sound_manager;
        geoirc = parent;
        
        socket = null;
        out = null;
        
    }
    
    public boolean isConnected()
    {
        return ( ( socket != null ) && socket.isConnected() );
    }
    
    // Sends a line out to the server, including newline.
    public void send( String text )
    {
        out.println( text );
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
