/*
 * StyleManager.java
 *
 * Created on July 8, 2003, 8:40 AM
 */

package geoirc;

import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  Pistos
 */
public class StyleManager
{
    Style base_style;
    
    // No default constructor.
    private StyleManager() { }
    
    public StyleManager(
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        // Setup some default text styles.
        
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
        
        /*
        s = text_pane.addStyle( "blue", style_normal );
        StyleConstants.setBackground( s, new Color( 0, 0, 255 ) );
         */
    }

    public void initializeTextPane( JTextPane text_pane )
    {
        text_pane.addStyle( "normal", base_style );
        
    }
    
}
