/*
 * Logger.java
 *
 * Created on August 7, 2003, 11:59 AM
 */

package geoirc;

import geoirc.gui.DisplayManager;
import geoirc.util.*;
import java.io.*;
import java.util.regex.*;

/**
 *
 * @author  Pistos
 */
public class Logger implements GeoIRCConstants
{
    protected String filename;
    protected PrintWriter out;
    protected String filter;
    protected Pattern regexp;
    protected DisplayManager display_manager;
    protected I18nManager i18n_manager;
    
    private Logger() { }
    
    public Logger(
        DisplayManager display_manager,
        I18nManager i18n_manager,
        String filename,
        String filter,
        String regexp_str,
        String start_message
    )
        throws PatternSyntaxException, IOException
    {
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        this.filename = filename;
        this.filter = filter;
        
        try
        {
            regexp = Pattern.compile( regexp_str );
            out = new PrintWriter( new BufferedWriter( new FileWriter( filename, true ) ) );
            out.println( GeoIRC.getATimeStamp( start_message ) );
            out.flush();
        }
        catch( PatternSyntaxException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "regexp error", new Object [] { regexp_str } )
            );
            display_manager.printlnDebug( e.getMessage() );
            throw e;
        }
        catch( IOException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "io exception 6", new Object [] { filename } )
            );
            display_manager.printlnDebug( e.getMessage() );
            throw e;
        }
        
    }

    /**
     * @return true iff the line was logged.
     */
    public boolean log( String line, String qualities )
    {
        boolean passed = false;
        try
        {
            passed =
                BoolExpEvaluator.evaluate( filter, qualities )
                && regexp.matcher( line ).matches();
        }
        catch( BadExpressionException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "filter error", new Object [] { filter } )
            );
            display_manager.printlnDebug( e.getMessage() );
        }
        
        if( passed )
        {
            out.println( line );
            out.flush();
        }
        
        return passed;
    }
    
    public String getFilter()
    {
        return filter;
    }
    public String getRegexp()
    {
        return regexp.pattern();
    }
    public String getFilename()
    {
        return filename;
    }
    
    public void close()
    {
        out.flush();
        out.close();
    }
}
