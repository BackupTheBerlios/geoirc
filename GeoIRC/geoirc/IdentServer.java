// Java Ident 0.1.0a
// A simple Ident server for IRC clients.
// By Sunfire: Keith L. Sunfire@Sunfire.ma.cx
// Finished Thursday, July 10, 2003, 6:09AM CST.

package geoirc;

import java.io.*; // Needed for Input/Output streams. (BufferedReader, DataOutputStream)
import java.net.*; // Needed for sockets. (Socket, ServerSocket)

public class IdentServer extends Thread implements GeoIRCConstants
{
    
    protected String user_id;
    protected int local_ident_port;
    protected String os_string;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected I18nManager i18n_manager;
    
    // No default constructor.
    private IdentServer() { }

    public IdentServer(
        SettingsManager settings_manager,
        DisplayManager display_manager,
        I18nManager i18n_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        
        this.user_id = settings_manager.getString( "/personal/ident/username", "Pistos" );
        this.local_ident_port = DEFAULT_IDENT_PORT;
        this.os_string = settings_manager.getString( "/personal/ident/os", "Unknown" );
    }

    public void run()
    {

        ServerSocket Ident_Sock = null;
        
        Socket Ident_Stream = null;
        // The socket that will be used to establish a connection with the remote
        // incoming connection.

        DataOutputStream out = null;
        BufferedReader in = null; 

        try
        {
            // Just keep on serving ident requests...
            while( GOD_IS_GOOD )
            {
                Ident_Sock = new ServerSocket( local_ident_port ); 

                //Ident_Sock.setSoTimeout( 30000 );
                // This basically tells the server to wait 30 seconds for an incoming connection,
                // if nothing connects by then, then nothings going to, and no ident will be 
                // performed.
                // If this line is commented out, it will never time out.

                Ident_Stream = Ident_Sock.accept(); 
                // this basically tells the ident server to listen for a connection.
                // The thread will block until a connection request comes in.

                out = new DataOutputStream( Ident_Stream.getOutputStream() ); 
                in = new BufferedReader(
                    new InputStreamReader( Ident_Stream.getInputStream() )
                ); 

                String Streamed_Data;

                while( ( Streamed_Data = in.readLine() ) != null )
                {
                    // This enters a loop that basically doesn't close unless the remote socket is closed.

                    if( Streamed_Data.indexOf( "," ) != -1 )
                    {
                        // This waits for the client to send an ident-request
                        // RFC 1413 (Ident-request): <local-port>, <remote-port> <EOL>

                        String local_port = Streamed_Data.substring( 0, Streamed_Data.indexOf(",") );
                        String remote_port = Streamed_Data.substring( Streamed_Data.indexOf(",") + 1, Streamed_Data.length() );

                        out.writeBytes(
                            local_port.trim()
                            + ", " + remote_port.trim()
                            + " : USERID : "
                            + os_string
                            + " : " + user_id
                            + "\r\n"
                        );
                        display_manager.printlnDebug(
                            i18n_manager.getString( "ident sent" )
                        );
                        // Now according to the protocol, this is all the Ident server really needs to send unless you're using a different
                        // operating system other then specified on RFC 931. I don't think anyone will not specify UNIX or WINDOWS anyways.
                        // By the way, this sends ident response.
                        // RFC 1413 Ident Responce: <local-port>, <remote-port> ":" "USERID" ":" <os_string> ":" <user-id> <EOL>

                        break;
                    }

                }

                out.close();
                in.close();
                Ident_Stream.close();
                Ident_Sock.close();
            }
        }
        catch( IOException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "io exception 5" )
            );
            display_manager.printlnDebug( e.getMessage() );
        }
        
    }

}