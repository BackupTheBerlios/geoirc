/*
 * ProcessJanitor.java
 *
 * Created on August 10, 2003, 6:22 PM
 */

package geoirc;

import java.util.Hashtable;

/**
 *
 * @author  Pistos
 */
public class ProcessJanitor extends Thread implements GeoIRCConstants
{
    protected GIProcess process;
    protected Hashtable processes;
    protected DisplayManager display_manager;
    protected I18nManager i18n_manager;
    
    private ProcessJanitor() { }
    
    public ProcessJanitor(
        DisplayManager display_manager,
        I18nManager i18n_manager,
        GIProcess process,
        Hashtable processes
    )
    {
        this.process = process;
        this.processes = processes;
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
    }
    
    public void run()
    {
        int exit_value;
        Process process_object = process.getProcessObject();
        while( GOD_IS_GOOD )
        {
            try
            {
                exit_value = process_object.exitValue();
                break;
            }
            catch( IllegalThreadStateException e )
            {
                try
                {
                    Thread.sleep( PROCESS_WATCH_INTERVAL );
                } catch( InterruptedException e2 ) { }
            }
        }

        processes.remove( new Integer( process.getPID() ) );

        display_manager.printlnDebug(
            i18n_manager.getString(
                "process death",
                    new Object [] {
                        process.getPIDString(),
                        new Integer( exit_value )
                    }
            )
        );
    }
}
