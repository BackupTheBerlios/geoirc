/*
 * Trigger.java
 *
 * Created on August 6, 2003, 12:05 PM
 */

package geoirc;

import geoirc.util.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.regex.*;

/**
 *
 * @author  Pistos
 */
public class Trigger
{
    protected String filter;
    protected CommandExecutor executor;
    protected DisplayManager display_manager;
    protected Pattern regexp;
    protected String command;
    
    // No default constructor.
    private Trigger() { }
    
    public Trigger(
        CommandExecutor executor,
        DisplayManager display_manager,
        String filter,
        String regexp_str,
        String command
    )
        throws PatternSyntaxException
    {
        this.executor = executor;
        this.display_manager = display_manager;
        this.filter = filter;
        this.command = command;

        try
        {
            regexp = Pattern.compile( regexp_str );
        }
        catch( PatternSyntaxException e )
        {
            display_manager.printlnDebug(
                "Regular expression syntax error for expression '"
                + regexp_str + "'"
            );
            display_manager.printlnDebug( e.getMessage() );
            throw e;
        }
        
    }
    
    /* Check against a message which has certain qualities.
     * Execute the command if the check passes.
     */
    public boolean check( String message_, String qualities_ )
    {
        String message = "";
        if( message_ != null )
        {
            message = message_;
        }
        String qualities = "";
        if( qualities_ != null )
        {
            qualities = qualities_;
        }
        boolean passed = false;
        try
        {
            passed =
                BoolExpEvaluator.evaluate( filter, qualities )
                && regexp.matcher( message ).matches();
        }
        catch( BadExpressionException e )
        {
            display_manager.printlnDebug( "Filter evaluation error for filter '" + filter + "'" );
            display_manager.printlnDebug( e.getMessage() );
        }
        
        if( passed )
        {
            executor.execute( command );
        }
        
        return passed;
    }
}
