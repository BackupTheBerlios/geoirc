/*
 * GIProcess.java
 *
 * Created on August 9, 2003, 12:33 PM
 */

package geoirc;

import enigma.console.terminal.AnsiOutputStream;

import geoirc.GeoIRCConstants;
import geoirc.ProcessJanitor;
import geoirc.gui.DisplayManager;
import geoirc.gui.GIConsolePane;
import geoirc.gui.GIPaneWrapper;
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
    protected I18nManager i18n_manager;
    
    private GIProcess() { }
    
    public GIProcess(
        DisplayManager display_manager,
        I18nManager i18n_manager,
        Hashtable processes,
        String exec_string,
        CommandExecutor executor,
        int exec_type
    )
        throws IOException
    {
        this.i18n_manager = i18n_manager;
        
        this.exec_string = exec_string;
        if( exec_type == CMD_EXEC2 )
        {
            this.executor = executor;
        }
        else
        {
            this.executor = null;
        }
        
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
        
        GIConsolePane gicp = null;
        if( exec_type == CMD_EXEC_WITH_WINDOW )
        {
            //executor.execute( "newwindow process=" + getPIDString() );
            GIPaneWrapper gipw = display_manager.addConsoleWindow( PID_PREFIX + getPIDString() );
            if( gipw != null )
            {
                gicp = (GIConsolePane) gipw.getPane();
            }
        }
        
        Runtime rt = Runtime.getRuntime();
        process = rt.exec( exec_string );
        
        stdin = new PrintWriter( process.getOutputStream() );
        
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();

        BufferedReader out = new BufferedReader( new InputStreamReader( stdout ) );
        BufferedReader err = new BufferedReader( new InputStreamReader( stderr ) );
        
        AnsiOutputStream ansi_stream = null;
        if( gicp != null )
        {
            ansi_stream = gicp.getANSIStream();
        }

        new InputStreamReaderThread(
            this.executor,
            display_manager,
            ansi_stream,
            out,
            this
        ).start();
        new InputStreamReaderThread(
            this.executor,
            display_manager,
            ansi_stream,
            err,
            this
        ).start();
        
        new ProcessJanitor( display_manager, i18n_manager, this, processes ).start();
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
    
    public void destroy()
    {
        if( process != null )
        {
            process.destroy();
            process = null;
        }
    }
}
