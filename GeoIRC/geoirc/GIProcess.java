/*
 * GIProcess.java
 *
 * Created on August 9, 2003, 12:33 PM
 */

package geoirc;

import geoirc.DisplayManager;
import geoirc.GeoIRCConstants;
import geoirc.ProcessJanitor;
import geoirc.util.InputStreamReaderThread;
import java.io.*;
import java.util.Hashtable;

/**
 *
 * @author  Pistos
 */
public class GIProcess implements GeoIRCConstants
{
    protected Process process;
    private static int next_pid = 0;
    protected int pid;
    protected PrintWriter stdin;
    protected String exec_string;
    protected CommandExecutor executor;
    
    private GIProcess() { }
    
    public GIProcess(
        DisplayManager display_manager,
        Hashtable processes,
        String exec_string,
        CommandExecutor executor,
        int exec_type
    )
        throws IOException
    {
        this.exec_string = exec_string;
        this.executor = executor;
        
        pid = next_pid;
        if( next_pid == Integer.MAX_VALUE )
        {
            next_pid = 0;
        }
        else
        {
            next_pid++;
        }
        
        processes.put( new Integer( pid ), this );
        
        executor.execute( "newwindow process=" + getPIDString() );
        
        Runtime rt = Runtime.getRuntime();
        process = rt.exec( exec_string );
        
        stdin = new PrintWriter( process.getOutputStream() );
        
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();

        BufferedReader out = new BufferedReader( new InputStreamReader( stdout ) );
        BufferedReader err = new BufferedReader( new InputStreamReader( stderr ) );

        new InputStreamReaderThread(
            executor,
            display_manager,
            out,
            this
        ).start();
        new InputStreamReaderThread(
            executor,
            display_manager,
            err,
            this
        ).start();
        
        new ProcessJanitor( display_manager, this, processes ).start();
    }
    
    public int getPID()
    {
        return pid;
    }
    
    public String getPIDString()
    {
        return Integer.toString( pid );
    }
    
    public String toString()
    {
        return getPIDString() + ": " + exec_string;
    }
    
    public Process getProcessObject()
    {
        return process;
    }
    
    public void println( String line )
    {
        stdin.println( line );
        stdin.flush();
    }
}
