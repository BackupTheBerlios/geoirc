/*
 * Trigger.java
 *
 * Created on August 6, 2003, 12:05 PM
 */

package geoirc;

import geoirc.gui.DisplayManager;
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
    protected I18nManager i18n_manager;
    protected Pattern regexp;
    protected String command;
    
    // No default constructor.
    private Trigger() { }
    
    public Trigger(
        CommandExecutor executor,
        DisplayManager display_manager,
        I18nManager i18n_manager,
        String filter,
        String regexp_str,
        String command
    )
        throws PatternSyntaxException
    {
        this.executor = executor;
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        this.filter = filter;
        this.command = command;

        try
        {
            regexp = Pattern.compile( regexp_str );
        }
        catch( PatternSyntaxException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "regexp error", new Object [] { regexp_str } )
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
            display_manager.printlnDebug(
                i18n_manager.getString( "filter error", new Object [] { filter } )
            );
            display_manager.printlnDebug( e.getMessage() );
        }
        
        if( passed )
        {
            executor.execute( command );
        }
        
        return passed;
    }
}
