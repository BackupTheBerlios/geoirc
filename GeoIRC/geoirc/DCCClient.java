/*
 * DCCClient.java
 *
 * Created on August 16, 2003, 4:20 PM
 */

package geoirc;

import geoirc.util.Util;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
        I18nManager i18n_manager,
        String host_ip,
        String port,
        int dcc_type,
        String user_nick,
        String remote_nick,
        String arg1,
        int filesize
    )
    {
        super( parent, display_manager, settings_manager, i18n_manager, host_ip, port );
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
                        String directory = 
                            settings_manager.getString(
                                "/dcc/file transfers/download directory",
                                "./downloads"
                            );
                        if( ! directory.endsWith( File.separator ) )
                        {
                            directory += File.separator;
                        }
                        
                        // Use only the filename if a full path is passed.
                        int index = arg1.lastIndexOf( "/" ) + 1;
                        index = arg1.lastIndexOf( "\\", index ) + 1;
                        String filename = arg1.substring( index );
                        problem = true;
                        if( ! filename.equals( "" ) )
                        {
                            File dir = new File( directory );
                            if( ! dir.exists() )
                            {
                                if( ! dir.mkdirs() )
                                {
                                    break;
                                }
                            }
                            
                            File file = new File( directory + filename );
                            int i = 0;
                            while( file.exists() )
                            {
                                i++;
                                file = new File( directory + filename + "." + Integer.toString( i ) );
                            }
                            
                            if( file.createNewFile() )
                            {
                                reader = new DCCSendReader(
                                    new BufferedInputStream(
                                        socket.getInputStream()
                                    ),
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
                        i18n_manager.getString( "dcc chat reader failure" )
                    );
                }
            }
        }
        catch( UnknownHostException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "unknown host", new Object [] { hostname } )
            );
            display_manager.printlnDebug( e.getMessage() );
        }
        catch( IOException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString(
                    "io exception 10",
                    new Object [] { arg1 }
                )
            );
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
                        i18n_manager.getString( "interpretline npe", new Object [] { line } )
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
                        i18n_manager.getString( "io exception 2", new Object [] { DCCClient.this.toString() } )
                    );
                    
                    if( ! isConnected() && ( ! closed ) )
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString(
                                "connection lost",
                                new Object [] { DCCClient.this.toString() }
                            )
                        );
                    }
                    
                    if( e.getMessage().equals( "Connection reset" ) )
                    {
                        display_manager.printlnDebug(
                            i18n_manager.getString(
                                "connection reset",
                                new Object [] { DCCClient.this.toString() }
                            )
                        );
                        reset = true;
                    }
                }
            }
            
            if( ( ! isConnected() ) || reset )
            {
                if( ! isConnected() )
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString(
                            "no longer connected",
                            new Object [] { DCCClient.this.toString() }
                        )
                    );
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
                    geoirc.checkAgainstTriggers( transformed_message[ MSG_TEXT ], transformed_message[ MSG_QUALITIES ] );
                    break;
            }
        }
        
    }
    
    /* ******************************************************************** */
    
    protected class DCCSendReader
        extends RemoteMachineReader
        implements GeoIRCConstants
    {
        protected BufferedInputStream in;
        protected java.io.OutputStream outstream;
        File file;
        int filesize;

        // No default constructor.
        private DCCSendReader() { }

        public DCCSendReader(
            BufferedInputStream in,
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
            BufferedOutputStream file_out = null;
            try
            {
                display_manager.printlnDebug(
                    i18n_manager.getString(
                        "dcc send starting",
                        new Object [] { file.getName() }
                    )
                );
                file_out = new BufferedOutputStream( new java.io.FileOutputStream( file ) );
                outstream = socket.getOutputStream();
                
                byte [] bytes_written_;
                while(
                    ( character != -1 )
                    && isConnected()
                    && ( ! reset )
                    && ( bytes_written < filesize )
                )
                {
                    try
                    {
                        if( in.available() == 0 )
                        {
                            // Report progress to sender, as per DCC SEND protocol.
                            
                            bytes_written_ = Util.intToNetworkByteOrder( bytes_written );
                            outstream.write( bytes_written_ );
                        }
                        character = in.read();

                        if( character != -1 )
                        {
                            file_out.write( character );
                            bytes_written++;
                        }
                    }
                    catch( IOException e )
                    {
                        Util.printException(
                            display_manager,
                            e,
                            i18n_manager.getString(
                                "io exception 2",
                                new Object [] { DCCClient.this.toString() }
                            )
                        );

                        if( ! isConnected() && ( ! closed ) )
                        {
                            display_manager.printlnDebug(
                                i18n_manager.getString(
                                    "connection lost",
                                    new Object [] { DCCClient.this.toString() }
                                )
                            );
                        }

                        if( e.getMessage().equals( "Connection reset" ) )
                        {
                            display_manager.printlnDebug(
                                i18n_manager.getString(
                                    "connection reset",
                                    new Object [] { DCCClient.this.toString() }
                                )
                            );
                            reset = true;
                        }
                    }
                }
                
                bytes_written_ = Util.intToNetworkByteOrder( bytes_written );
                outstream.write( bytes_written_ );

                if( ( ! isConnected() ) || reset )
                {
                    display_manager.printlnDebug(
                        i18n_manager.getString(
                            "no longer connected",
                            new Object [] { DCCClient.this.toString() }
                        )
                    );
                }
                
                file_out.flush();
                file_out.close();
                
                int percentage = (int) (( (double) bytes_written / (double) filesize ) * 100.0);
                display_manager.printlnDebug(
                    i18n_manager.getString(
                        "dcc send done",
                        new Object [] { file.getAbsolutePath(), new Integer( percentage ) }
                    )
                );
            }
            catch( IOException e )
            {
                Util.printException(
                    display_manager,
                    e,
                    i18n_manager.getString( "io exception 9", new Object [] { file.getAbsolutePath() } )
                );
            }
        }
        
        protected void interpretLine( int stage, String[] transformed_message_ ) { }
        
    }
}
