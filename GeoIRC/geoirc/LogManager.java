/*
 * LogManager.java
 *
 * Created on August 7, 2003, 12:11 PM
 */

package geoirc;

import geoirc.gui.DisplayManager;

import java.io.File;
import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class LogManager implements GeoIRCConstants
{
    protected Vector loggers;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected I18nManager i18n_manager;
    protected String log_path;
    protected boolean initializing;
    
    // No default constructor
    private LogManager() { }
    
    public LogManager(
        SettingsManager settings_manager,
        DisplayManager display_manager,
        I18nManager i18n_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        loggers = new Vector();
        initializing = true;
        
        log_path = settings_manager.getString(
            "/logs/default log path", DEFAULT_LOG_PATH
        );
        File log_path_f = new File( log_path );
        if( ! log_path_f.exists() )
        {
            log_path_f.mkdirs();
        }
        
        int i = 0;
        String i_str;
        String filter;
        String regexp;
        String file;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            file = settings_manager.getString(
                "/logs/" + i_str + "/file",
                ""
            );
            if( file.equals( "" ) )
            {
                // No more log files stored in the settings.
                break;
            }
            
            regexp = settings_manager.getString(
                "/logs/" + i_str + "/regexp",
                ""
            );
            filter = settings_manager.getString(
                "/logs/" + i_str + "/filter",
                ""
            );
            
            addLogger( filter, regexp, file );
            
            i++;
        }
        
        initializing = false;
    }
    
    public void log( String message, String qualities )
    {
        int n = loggers.size();
        Logger logger;
        for( int i = 0; i < n; i++ )
        {
            logger = (Logger) loggers.elementAt( i );
            logger.log( message, qualities );
        }
    }
    
    public boolean addLogger( String filter, String regexp, String file )
    {
        boolean success = false;
        Logger logger;

        if(
            ( file.charAt( 0 ) != File.separatorChar )
            && ( ! file.startsWith( log_path ) )
        )
        {
            // This is a relative path.
            file = log_path + file;
        }
        
        try
        {
            logger = new Logger(
                display_manager, i18n_manager, file, filter, regexp,
                settings_manager.getString(
                    "/logs/log start message", DEFAULT_LOG_START_MESSAGE
                )             
            );
            loggers.add( logger );
            
            if( ! initializing )
            {
                recordLoggers();
            }
            
            display_manager.printlnDebug(
                i18n_manager.getString(
                    "logging started",
                    new Object [] { file, filter, regexp }
                )
            );
            
            success = true;
        }
        catch( java.util.regex.PatternSyntaxException e ) { }
        catch( java.io.IOException e ) { }
        
        return success;
    }
    
    public void removeLogger( int index )
    {
        Logger l = (Logger) loggers.remove( index );
        l.close();
        recordLoggers();
        
        display_manager.printlnDebug(
            i18n_manager.getString(
                "logging stopped",
                new Object [] { l.getFilename(), l.getFilter(), l.getRegexp() }
            )
        );
    }
    
    protected void recordLoggers()
    {
        String start_msg = settings_manager.getString( "/logs/log start message", DEFAULT_LOG_START_MESSAGE );
        settings_manager.removeNode( "/logs/" );
        
        settings_manager.putString( "/logs/default log path", log_path );
        settings_manager.putString( "/logs/log start message", start_msg );
        
        int n = loggers.size();
        String i_str;
        Logger logger;
        String setting_path;
        
        for( int i = 0; i < n; i++ )
        {
            logger = (Logger) loggers.elementAt( i );
            i_str = Integer.toString( i );
            
            setting_path = "/logs/" + i_str;
            
            settings_manager.putString( setting_path + "/filter", logger.getFilter() );
            settings_manager.putString( setting_path + "/regexp", logger.getRegexp() );
            settings_manager.putString( setting_path + "/file", logger.getFilename() );
        }
    }
    
    public void listLogs()
    {
        Logger l;
        for( int i = 0, n = loggers.size(); i < n; i++ )
        {
            l = (Logger) loggers.elementAt( i );
            display_manager.printlnDebug(
                Integer.toString( i ) + ": "
                + l.getFilename() + "; "
                + l.getFilter() + "; "
                + l.getRegexp()
            );
        }
    }
}
