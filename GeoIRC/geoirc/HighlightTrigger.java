/*
 * HighlightTrigger.java
 *
 * Created on July 9, 2003, 2:45 PM
 */

package geoirc;

import geoirc.util.*;
import java.util.regex.*;

/**
 *
 * @author  Pistos
 */
public class HighlightTrigger implements GeoIRCConstants
{
    private HighlightTrigger() { }
    
    String filter;
    DisplayManager display_manager;
    Pattern regexp;
    String format;
    
    public HighlightTrigger(
        DisplayManager display_manager,
        String filter,
        String regexp_str,
        String format
    )
    throws PatternSyntaxException
    {
        this.display_manager = display_manager;
        this.filter = filter;

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
        this.format = format;
    }
    
    /* Check against a message which has certain qualities.
     * Return the line in highlighted form.
     */
    public String highlight( String line, String qualities )
    {
        String highlighted_line = line;
        Matcher matcher = regexp.matcher( line );
        String group = null;;
        
        try
        {
            if(
                BoolExpEvaluator.evaluate( filter, qualities )
                && matcher.matches()
            )
            {
                if( matcher.groupCount() > 0 )
                {
                    group = matcher.group( 1 );
                    if( ( group != null ) && ( ! group.equals( "" ) ) )
                    {
                        /* Problem: We're searching for the first match of this
                         * matched group.  The ACTUAL match may not be the first
                         * one!  :(  Ugh.
                         */
                        int index = line.indexOf( group );
                        highlighted_line =
                            line.substring( 0, index )
                            + STYLE_ESCAPE_SEQUENCE
                            + format
                            + STYLE_TERMINATION_SEQUENCE
                            + group
                            + STYLE_ESCAPE_SEQUENCE
                            + "normal"
                            + STYLE_TERMINATION_SEQUENCE
                            + line.substring( index + group.length() );
                    }
                }
                else
                {
                    // No parentheses, so highlight the whole line.
                    highlighted_line =
                        STYLE_ESCAPE_SEQUENCE
                        + format
                        + STYLE_TERMINATION_SEQUENCE
                        + line;
                }
            }
        }
        catch( BadExpressionException e )
        {
            display_manager.printlnDebug( "Filter evaluation error for filter '" + filter + "'" );
            display_manager.printlnDebug( e.getMessage() );
        }
        
        return highlighted_line;
    }
}
