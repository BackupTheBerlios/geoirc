/*
 * DCCClient.java
 *
 * Created on August 16, 2003, 4:20 PM
 */

package geoirc;

import geoirc.util.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
    protected String user_nick;
    protected String remote_nick;
    protected String arg1;
    protected int filesize;
    
    public DCCClient(
        GeoIRC parent,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        TriggerManager trigger_manager,
        String host_ip,
        String port,
        int dcc_type,
        String user_nick,
        String remote_nick,
        String arg1,
        int filesize
    )
    {
        super( parent, display_manager, settings_manager, trigger_manager, host_ip, port );
        this.dcc_type = dcc_type;
        this.remote_nick = remote_nick;
        this.user_nick = user_nick;
        this.arg1 = arg1;
        this.filesize = filesize;
    }
    
    // Returns whether a connection has been established.
    public boolean connect()
    {
        boolean problem = false;
        
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
                        out = new PrintWriter( socket.getOutputStream(), true );
                        break;
                    case DCC_SEND:
                    {
                        File file = new File( arg1 );
                        String filename = file.getName();
                        problem = true;
                        if( ! filename.equals( "" ) )
                        {
                            int i = 0;
                            while( file.exists() )
                            {
                                i++;
                                file = new File( filename + "." + Integer.toString( i ) );
                            }

                            if( file.createNewFile() )
                            {
                                reader = new DCCSendReader(
                                    new BufferedReader( new InputStreamReader(
                                        socket.getInputStream()
                                    ) ),
                                    file, 
                                    filesize
                                );
                                problem = false;
                            }
                            out = null;
                        }
                        break;
                    }
                }
                
                if( ( reader != null ) && ( ! problem ) )
                {
                    reader.start();
                }
                else
                {
                    display_manager.printlnDebug(
                        "Failed to create DCC stream reader."
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

    /* ******************************************************************** */
    
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
                    interpretLine(
                        STAGE_SCRIPTING,
                        new String [] {
                            line,
                            DCCClient.this.toString() + " " + FILTER_SPECIAL_CHAR + "dccchat"
                        }
                    );
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
            }
        }
        
        protected void interpretLine( int stage, String [] transformed_message )
        {
            switch( stage )
            {
                case STAGE_SCRIPTING:
                    interpretLine(
                        STAGE_PROCESSING,
                        geoirc.onRaw(
                            transformed_message[ MSG_TEXT ],
                            transformed_message[ MSG_QUALITIES ]
                        )
                    );
                    break;
                case STAGE_PROCESSING:
                    display_manager.println(
                        GeoIRC.getATimeStamp(
                            settings_manager.getString( "/gui/format/timestamp", "" )
                        ) + "<" + remote_nick + "> " + transformed_message[ MSG_TEXT ],
                        transformed_message[ MSG_QUALITIES ]
                    );
                    trigger_manager.check( transformed_message[ MSG_TEXT ], transformed_message[ MSG_QUALITIES ] );
                    break;
            }
        }
        
    }
    
    /* ******************************************************************** */
    
    protected class DCCSendReader
        extends RemoteMachineReader
        implements GeoIRCConstants
    {
        protected BufferedReader in;
        File file;
        int filesize;

        // No default constructor.
        private DCCSendReader() { }

        public DCCSendReader(
            BufferedReader in,
            File file,
            int filesize
        )
        {
            this.in = in;
            this.file = file;
            this.filesize = filesize;
        }

        public void run()
        {
            if(
                ( in == null )
                || ( file == null )
                || ( display_manager == null )
            )
            {
                return;
            }

            int character = 0;
            int bytes_written = 0;
            BufferedWriter file_out = null;
            try
            {
                file_out = new BufferedWriter( new FileWriter( file ) );
                
                while( ( character != -1 ) && ( isConnected() ) && ( ! reset ) )
                {
                    try
                    {
                        character = in.read();

                        if( character != -1 )
                        {
                            file_out.write( character );
                        }
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
                }
                
                file_out.flush();
                file_out.close();
            }
            catch( IOException e )
            {
                Util.printException(
                    display_manager, e,
                    "Error during open of or write to " + file.getAbsolutePath()
                );
            }
        }
        
        protected void interpretLine(int stage, String[] transformed_message_) {
        }
        
    }
}
