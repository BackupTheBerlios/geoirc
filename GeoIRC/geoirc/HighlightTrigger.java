/*
 * HighlightTrigger.java
 *
 * Created on July 9, 2003, 2:45 PM
 */

package geoirc;

import geoirc.util.*;
import java.util.regex.*;
import javax.swing.text.*;

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
    public void highlight(
        GITextPane text_pane,
        int offset,
        int length,
        String qualities
    )
    {
        try
        {
            String line = text_pane.getText( offset, length );
            Matcher matcher = regexp.matcher( line );
            String group = null;
        
            if(
                BoolExpEvaluator.evaluate( filter, qualities )
                && matcher.find()
            )
            {
                if( matcher.groupCount() > 0 )
                {
                    // There are one or more groups marked off by parentheses,
                    // so we want to apply the highlighting only to those groups.
                    
                    do
                    {
                        group = matcher.group( 1 );
                        if( ( group != null ) && ( ! group.equals( "" ) ) )
                        {
                            int start = offset + matcher.start( 1 );
                            int end = offset + matcher.end( 1 );
                            
                            text_pane.applyStyle( start, end - start, format );
                        }
                    } while( matcher.find() );
                }
                else
                {
                    // No parentheses, so highlight the whole line.
                    
                    text_pane.applyStyle( offset, length, format );
                }
            }
        }
        catch( BadExpressionException e )
        {
            display_manager.printlnDebug( "Filter evaluation error for filter '" + filter + "'" );
            display_manager.printlnDebug( e.getMessage() );
        }
        catch( BadLocationException e )
        {
            display_manager.printlnDebug( "highlight called on invalid location" );
            display_manager.printlnDebug( e.getMessage() );
        }
    }
}
