/*
 * HighlightTrigger.java
 *
 * Created on July 9, 2003, 2:45 PM
 */

package geoirc;

import geoirc.gui.DisplayManager;
import geoirc.gui.GITextPane;
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
    
    protected String filter;
    protected DisplayManager display_manager;
    protected I18nManager i18n_manager;
    protected Pattern regexp;
    protected String format;
    
    public HighlightTrigger(
        DisplayManager display_manager,
        I18nManager i18n_manager,
        String filter,
        String regexp_str,
        String format
    )
    throws PatternSyntaxException
    {
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        this.filter = filter;

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
        
            if( BoolExpEvaluator.evaluate( filter, qualities ) )
            {
                while( matcher.find() )
                {
                    int group_count = matcher.groupCount();
                    if( group_count == 0 )
                    {
                        // No parentheses, so highlight the whole line.

                        text_pane.applyStyle( offset, length, format );
                        break;
                    }
                    else
                    {
                        // There are one or more groups marked off by parentheses,
                        // so we want to apply the highlighting only to those groups.

                        for( int group_number = 1; group_number <= group_count; group_number++ )
                        {
                            group = matcher.group( group_number );
                            if( ( group != null ) && ( ! group.equals( "" ) ) )
                            {
                                int start = offset + matcher.start( group_number );
                                int end = offset + matcher.end( group_number );

                                text_pane.applyStyle( start, end - start, format );
                            }
                        }
                    }
                }
            }
        }
        catch( BadExpressionException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "filter error", new Object [] { filter } )
            );
            display_manager.printlnDebug( e.getMessage() );
        }
        catch( BadLocationException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString(
                    "bad doc location",
                    new Object [] { new Integer( e.offsetRequested() ) }
                )
            );
            display_manager.printlnDebug( e.getMessage() );
        }
    }
}
