/*
 * InputStreamReaderThread.java
 *
 * Created on August 6, 2003, 12:39 AM
 */

package geoirc.util;

import enigma.console.terminal.AnsiOutputStream;

import geoirc.CommandExecutor;
import geoirc.GIProcess;
import geoirc.gui.DisplayManager;
import java.io.*;

/**
 *
 * @author  Pistos
 */
public class InputStreamReaderThread extends Thread
{
    protected BufferedReader reader;
    protected CommandExecutor executor;
    protected DisplayManager display_manager;
    protected AnsiOutputStream out;
    protected GIProcess parent;
    
    private InputStreamReaderThread() { }
    
    /**
     * If executor is non-null, lines of text from the reader are executed as commands.
     */
    public InputStreamReaderThread(
        CommandExecutor executor,
        DisplayManager display_manager,
        AnsiOutputStream out,
        BufferedReader reader,
        GIProcess parent
    )
    {
        this.reader = reader;
        this.executor = executor;
        this.display_manager = display_manager;
        this.out = out;
        this.parent = parent;
    }

    public void run()
    {
        if( reader != null )
        {
            try
            {
                if( executor != null )
                {
                    String line = null;
                    while( ( line = reader.readLine() ) != null )
                    {
                        if( line.length() > 0 )
                        {
                            if( line.charAt( 0 ) == '/' )
                            {
                                line = line.substring( 1 );
                            }
                            executor.execute( line );
                        }
                    }
                }
                else if( out != null )
                {
                    int ch;
                    while( ( ch = reader.read() ) != -1 )
                    {
                        out.write( ch );
                    }
                }
            }
            catch( IOException e )
            {
                Util.printException( display_manager, e, "I/O error during external execution." );
            }
        }
    }
    
}
