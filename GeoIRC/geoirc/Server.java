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
import javax.swing.*;

/**
 *
 * @author  Pistos
 */
public class Server extends RemoteMachine
{
    protected ServerReader server_reader;
    
    public Server(
        GeoIRC parent,
        DisplayManager display_manager,
        String hostname,
        String port
    )
    {
        super( parent, display_manager, hostname, port );
        
        server_reader = null;
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
                out.println( "USER PistosGI x x :Pi Gi" );
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
    
}
