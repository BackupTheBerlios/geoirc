/*
 * StyleManager.java
 *
 * Created on July 8, 2003, 8:40 AM
 */

package geoirc;

import java.awt.Color;
import java.util.Vector;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  Pistos
 */
public class StyleManager
    implements GeoIRCConstants
{
    protected String [] style_names;
    protected Style base_style;
    
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    
    // No default constructor.
    private StyleManager() { }
    
    public StyleManager(
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        
        Vector v = new Vector();
        
        style_names = new String[ 0 ];
        
        base_style = StyleContext.getDefaultStyleContext().getStyle(
            StyleContext.DEFAULT_STYLE
        );
        StyleConstants.setFontFamily(
            base_style,
            settings_manager.getString( "/gui/text windows/font face", "Lucida Console" )
        );
        StyleConstants.setFontSize(
            base_style,
            settings_manager.getInt( "/gui/text windows/font size", 14 )
        );
        
        // Read in more styles based on highlight settings.
        
        int i = 0;
        String i_str;
        String format;
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            format = settings_manager.getString(
                "/gui/text windows/highlighting/" + i_str + "/format",
                ""
            );
            if( format.equals( "" ) )
            {
                // No more highlight rules specified.
                break;
            }
            
            v.add( format );
            
            i++;
        }
        
        style_names = (String []) v.toArray( style_names );
    }

    public void initializeTextPane( JTextPane text_pane )
    {
        Style normal = text_pane.addStyle( "normal", base_style );
        
        String format;
        for( int i = 0; i < style_names.length; i++ )
        {
            format = style_names[ i ];
            int len = format.length();
            boolean valid_format = true;
            
            // Duplicate the base style, and adjust the new copy to create the new style.
            Style style = text_pane.addStyle( format, normal );
            for( int c = 0; c < len; )
            {
                if( c > len - 2 )
                {
                    valid_format = false;
                    break;
                }
                else
                {
                    
                    String arg;
                    String code = format.substring( c, c + 2 );
                    c += 2;
                    if( code.equals( STYLE_FOREGROUND ) )
                    {
                        if( c > len - 6 )
                        {
                            valid_format = false;
                            break;
                        }
                        
                        arg = format.substring( c, c + 6 );
                        c += 6;
                        int red = 0xff;
                        int green = 0xff;
                        int blue = 0xff;
                        try
                        {
                            red = Integer.parseInt( arg.substring( 0, 2 ), 16 );
                            green = Integer.parseInt( arg.substring( 2, 4 ), 16 );
                            blue = Integer.parseInt( arg.substring( 4, 6 ), 16 );
                        }
                        catch( NumberFormatException e )
                        {
                            valid_format = false;
                            break;
                        }
                        
                        StyleConstants.setForeground( style, new Color( red, green, blue ) );
                    }
                    else if( code.equals( STYLE_BACKGROUND ) )
                    {
                        if( c > len - 6 )
                        {
                            valid_format = false;
                            break;
                        }
                        
                        arg = format.substring( c, c + 6 );
                        c += 6;
                        int red = 0xff;
                        int green = 0xff;
                        int blue = 0xff;
                        try
                        {
                            red = Integer.parseInt( arg.substring( 0, 2 ), 16 );
                            green = Integer.parseInt( arg.substring( 2, 4 ), 16 );
                            blue = Integer.parseInt( arg.substring( 4, 6 ), 16 );
                        }
                        catch( NumberFormatException e )
                        {
                            valid_format = false;
                            break;
                        }
                        
                        StyleConstants.setBackground( style, new Color( red, green, blue ) );
                    }
                    else if( code.equals( STYLE_BOLD ) )
                    {
                        StyleConstants.setBold( style, true );
                    }
                    else if( code.equals( STYLE_ITALIC ) )
                    {
                        StyleConstants.setItalic( style, true );
                    }
                    else if( code.equals( STYLE_UNDERLINE ) )
                    {
                        StyleConstants.setUnderline( style, true );
                    }
                }
            }
            
            if( ! valid_format )
            {
                System.err.println( "Bad style format definition: '" + style_names[ i ] + "'" );
            }
        }
    }
    
}
