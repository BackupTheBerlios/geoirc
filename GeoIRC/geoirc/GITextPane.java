/*
 * GITextPane.java
 *
 * Created on July 17, 2003, 12:07 AM
 */

package geoirc;

import geoirc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.util.StringTokenizer;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import org.jscroll.*;
import org.jscroll.widgets.*;

/**
 *
 * @author  Pistos
 */
public class GITextPane extends GIPane implements GeoIRCConstants
{
    protected JTextPane text_pane;
    protected StyledDocument document;
    protected String filter;
    protected JScrollBar scrollbar;
    protected boolean colour_toggle;
    
    public GITextPane(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title
    )
    {
        this(
            display_manager,
            settings_manager,
            title,
            (String) null
        );
    }

    public GITextPane(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        String filter
    )
    {
        super( display_manager, settings_manager, title, null );
        
        this.filter = filter;
        this.title = title;
        
        // Setup text pane.
        
        text_pane = new JTextPane();
        text_pane.setEditable( false );
        text_pane.addKeyListener( display_manager );
        
        document = text_pane.getStyledDocument();

        setViewportView( text_pane );
        setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        scrollbar = getVerticalScrollBar();
        
        applySettings();
        
        colour_toggle = false;
    }
    
    public int appendLine( String text )
    {
        return append( text + "\n" );
    }
    
    public void applySettings()
    {
        String rgb_str = settings_manager.getString(
            "/gui/text windows/default foreground colour",
            "cccccc"
        );
        int [] rgb = new int [ 3 ];
        rgb[ 0 ] = 0xcc;  rgb[ 1 ] = 0xcc;  rgb[ 2 ] = 0xcc;
        try {
            rgb = Util.getRGB( rgb_str );
        } catch( NumberFormatException e ) { /* accept defaults */ }
        text_pane.setForeground( new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] ) );
        
        rgb_str = settings_manager.getString(
            "/gui/text windows/default background colour",
            "000000"
        );
        rgb[ 0 ] = 0;  rgb[ 1 ] = 0;  rgb[ 2 ] = 0;
        try {
            rgb = Util.getRGB( rgb_str );
        } catch( NumberFormatException e ) { /* accept defaults */ }
        text_pane.setBackground( new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] ) );
        
        display_manager.getStyleManager().initializeTextPane( text_pane );
    }
    
    /**
     * @return the document offset of the appended text.
     */
    synchronized public int append( String text )
    {
        int offset = document.getLength();
        
        try
        {
            document.insertString(
                offset, text, text_pane.getStyle(
                    colour_toggle ? "normal" : "alternate"
                )
            );
            colour_toggle = ! colour_toggle;
        }
        catch( BadLocationException e )
        {
            display_manager.printlnDebug( e.getMessage() );
        }
        
        GIWindow giw = display_manager.getSelectedFrame();
        boolean highlight_button = true;
        if( giw != null )
        {
            if( giw.getPane() == this )
            {
                highlight_button = false;
            }
        }
        
        if( highlight_button )
        {
            display_manager.highlightButton( this );
        }
        
        // Autoscroll if the user is not holding the scrollbar.

        if( ! scrollbar.getValueIsAdjusting() )
        {
            SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {   
                        scrollbar.setValue( scrollbar.getMaximum() );                   
                    }
                }
            );
        }
        
        return offset;
    }
    
    public void applyStyle( int offset, int length, String style_name )
    {
        document.setCharacterAttributes(
            offset, length, text_pane.getStyle( style_name ), false
        );
    }
    
    public boolean accepts( String text )
    {
        boolean result = false;
        try
        {
            result = BoolExpEvaluator.evaluate( filter, text );
        }
        catch( BadExpressionException e )
        {
            display_manager.printlnDebug( "Filter evaluation error for filter '" + filter + "'" );
            display_manager.printlnDebug( e.getMessage() );
        }
        return result;
    }
    
    public String getText( int offset, int length )
        throws BadLocationException
    {
        return document.getText( offset, length );
    }
    
    public String getFilter()
    {
        return filter;
    }
    
    public void setFilter( String filter )
    {
        this.filter = filter;
        title = filter;
    }
    
    public void pageUp()
    {
        int min = scrollbar.getMinimum();
        int value = scrollbar.getValue() - scrollbar.getVisibleAmount();
        if( value < min )
        {
            value = min;
        }
        scrollbar.setValue( value );
    }
    
    public void pageDown()
    {
        int max = scrollbar.getMaximum();
        int value = scrollbar.getValue() + scrollbar.getVisibleAmount();
        if( value > max )
        {
            value = max;
        }
        scrollbar.setValue( value );
    }
    
    public void nudgeUp()
    {
        int min = scrollbar.getMinimum();
        int value =
            scrollbar.getValue()
            - settings_manager.getInt(
                "/gui/nudge amount",
                DEFAULT_NUDGE_AMOUNT
            );
        if( value < min )
        {
            value = min;
        }
        scrollbar.setValue( value );
    }
    
    public void nudgeDown()
    {
        int max = scrollbar.getMaximum();
        int value =
            scrollbar.getValue()
            + settings_manager.getInt(
                "/gui/nudge amount",
                DEFAULT_NUDGE_AMOUNT
            );
        if( value > max )
        {
            value = max;
        }
        scrollbar.setValue( value );
    }
    
}
