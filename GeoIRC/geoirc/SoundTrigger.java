/*
 * SoundTrigger.java
 *
 * Created on July 7, 2003, 8:04 AM
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
public class SoundTrigger
{
    String filter;
    DisplayManager display_manager;
    Pattern regexp;
    AudioClip clip;
    
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
        
        URL url = null;
        clip = null;
        try
        {
            url = new File( sound_file ).toURL(); 
            clip = Applet.newAudioClip( url );
        }
        catch ( MalformedURLException e )
        {
            display_manager.printlnDebug( e.getMessage() );
            display_manager.printlnDebug( "Failed to load '" + sound_file + "'" );
        }
        if( clip == null )
        {
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
            clip.play();
        }
        
        return passed;
    }
}
