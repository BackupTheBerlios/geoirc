/*
 * InputStreamReaderThread.java
 *
 * Created on August 6, 2003, 12:39 AM
 */

package geoirc.util;

import geoirc.*;
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
    
    private InputStreamReaderThread() { }
    
    /**
     * If executor is non-null, lines of text from the reader are executed as commands.
     */
    public InputStreamReaderThread(
        CommandExecutor executor,
        DisplayManager display_manager,
        BufferedReader reader
    )
    {
        this.reader = reader;
        this.executor = executor;
        this.display_manager = display_manager;
    }

    public void run()
    {
        if( reader != null )
        {
            String line = null;
            try
            {
                while( ( line = reader.readLine() ) != null )
                {
                    if( executor != null )
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
                    else
                    {
                        display_manager.printlnDebug( line );
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
