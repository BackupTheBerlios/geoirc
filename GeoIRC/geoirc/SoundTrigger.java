/*
 * SoundTrigger.java
 *
 * Created on July 7, 2003, 8:04 AM
 */

package geoirc;

import com.antelmann.sound.*;
import java.io.*;
import java.util.regex.*;

/**
 *
 * @author  Pistos
 */
public class SoundTrigger
{
    String filter;
    SoundPlayer player;
    DisplayManager display_manager;
    Pattern regexp;
    
    // No default constructor.
    private SoundTrigger() { }
    
    public SoundTrigger(
        DisplayManager display_manager,
        String filter,
        String regexp_str,
        String sound_file
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
        
        try
        {
            player = new SoundPlayer( new File( sound_file ) );
        }
        catch( SoundException e )
        {
            display_manager.printlnDebug( e.getMessage() );
            display_manager.printlnDebug( "Failed to load '" + sound_file + "'" );
        }
    }
    
    /* Check against a message which has certain qualities.
     * Play the sound if the check passes.
     */
    public boolean check( String message, String qualities )
    {
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
            player.play();
        }
        
        return passed;
    }
}
