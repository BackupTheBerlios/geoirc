/*
 * Server.java
 *
 * Created on June 22, 2003, 11:59 PM
 */

package geoirc;

//import java.awt.Container;
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
    }
    
    // Returns whether a connection has been established.
    public boolean connect( String nick )
    {
        try
        {
            socket = new Socket( hostname, port );
            if( socket != null )
            {
                server_reader = new ServerReader( 
                    this,
                    display_manager,
                    settings_manager,
                    sound_manager,
                    new BufferedReader(
                        new InputStreamReader(
                            socket.getInputStream()
                        )
                    )
                );
                out = new PrintWriter( socket.getOutputStream(), true );
                
                server_reader.start();

                out.println( "PASS ooga7" );
                out.println( "NICK " + nick );
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
    
}
