/*
 * GIConsolePane.java
 *
 * Created on November 9, 2003, 6:26 PM
 */

package geoirc.gui;

import enigma.console.java2d.Java2DTextWindow;
import enigma.console.TextAttributes;
import enigma.console.terminal.AnsiOutputStream;
import enigma.core.Enigma;

import geoirc.I18nManager;
import geoirc.SettingsManager;
import geoirc.util.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

import org.jscroll.*;
import org.jscroll.widgets.*;


/**
 *
 * @author  Pistos
 */
public class GIConsolePane
    extends GIPane
    implements geoirc.GeoIRCConstants
{
    //protected JTextPane text_pane;
    //protected String filter;
    protected Java2DTextWindow console_pane;
    protected JScrollBar scrollbar;
    //protected boolean colour_toggle;
    protected Color foreground_colour;
    protected Color background_colour;
    //protected boolean paint_mirc_codes;
    protected I18nManager i18n_manager;
    protected int last_found_index;
    protected String last_search_text;
    protected TextAttributes current_attributes;
    protected PrintStream print_stream;
    protected PrintWriter console_writer;
    protected AnsiOutputStream ansi_stream;
    
    public GIConsolePane(
        MouseListener mouse_listener,
        DisplayManager display_manager,
        SettingsManager settings_manager,
        I18nManager i18n_manager,
        String title
    )
    {
        super( display_manager, settings_manager, title, null );
        
        this.i18n_manager = i18n_manager;
        //this.filter = filter;
        this.title = title;
        
        // Setup text pane.
        
        /*
        text_pane = new JTextPane();
        text_pane.setEditable( false );
        text_pane.addKeyListener( display_manager );
        text_pane.addMouseListener( mouse_listener );
        text_pane.addMouseListener( this );
         */
        console_pane = new Java2DTextWindow(
            DEFAULT_NUM_CONSOLE_COLUMNS,
            DEFAULT_NUM_CONSOLE_ROWS,
            DEFAULT_CONSOLE_BUFFER_SIZE
        );
        setViewportView( console_pane );
        
        console_writer = new PrintWriter(
            new Writer() {
                public void close() { }
                public void flush() { }
                public void write( char c )
                {
                    console_pane.output( c, current_attributes );
                }
                public void write( char [] c, int offset, int length )
                {
                    console_pane.output( c, offset, length, current_attributes );
                }
                public void write( String str )
                {
                    console_pane.output( str, current_attributes );
                }
            }
        );

        print_stream = new PrintStream(
            new OutputStream()
            {
                public void close()
                {
                    console_writer.close();
                }
                public void flush()
                {
                    console_writer.flush();
                }
                public void write( int b )
                {
                    console_writer.write( (char) b );
                }
                public void write( byte [] b, int offset, int length )
                {
                    console_writer.write(new String(b, offset, length));
                }
                public void write( byte[] b )
                {
                    console_writer.write( new String( b ) );
                }
            }
        );
        
        ansi_stream = new AnsiOutputStream( this );
        
        setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        scrollbar = getVerticalScrollBar();
        
        applySettings();
        
        //colour_toggle = false;
        //paint_mirc_codes = true;
    }
    
    /*
    public String appendLine( String text )
    {
        return append( text + "\n" );
    }
     */
    
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
        foreground_colour = new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
        console_pane.setForeground( foreground_colour );
        
        rgb_str = settings_manager.getString(
            "/gui/text windows/default background colour",
            "000000"
        );
        rgb[ 0 ] = 0;  rgb[ 1 ] = 0;  rgb[ 2 ] = 0;
        try {
            rgb = Util.getRGB( rgb_str );
        } catch( NumberFormatException e ) { /* accept defaults */ }
        background_colour = new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
        console_pane.setBackground( background_colour );
        
        current_attributes = new TextAttributes( foreground_colour, background_colour );
        
        //display_manager.getStyleManager().initializeTextPane( text_pane );
    }
    
    /**
     * @return the text in modified form (control codes stripped, etc.)
     */
    /*
    synchronized public String append( String text_ )
    {
        StyledDocument document = text_pane.getStyledDocument();
        int offset = document.getLength();
        
        String text = text_;
        
        // Search for mIRC format codes, and paint the text accordingly.
        
        // Colours
        
        int ptr = 0;
        String remainder;
        Matcher m;
        String format;
        Vector formats = new Vector();
        Vector indices = new Vector();
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        SimpleAttributeSet formatting;
        int len = text.length();
        
        while( ptr < len )
        {
            switch( text.charAt( ptr ) )
            {
                case MIRC_COLOUR_CONTROL_CHAR:
                    // Get the number(s) following the control code.

                    remainder = text.substring( ptr + 1 );
                    m = Pattern.compile( "^(\\d{1,2}(?:,\\d{1,2})?).*" ).matcher( remainder );
                    format = "";
                    formatting = new SimpleAttributeSet();

                    if( m.find() )
                    {
                        format = m.group( 1 );

                        String [] fg_bg = format.split( "," );
                        int [] rgb = new int[ 3 ];
                        rgb = Util.getRGB(
                            settings_manager.getString(
                                "/gui/format/mirc colours/" + fg_bg[ 0 ],
                                DEFAULT_MIRC_FOREGROUND_COLOUR
                            )
                        );
                        StyleConstants.setForeground(
                            formatting,
                            new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] )
                        );

                        if( fg_bg.length > 1 )
                        {
                            rgb = Util.getRGB(
                                settings_manager.getString(
                                    "/gui/format/mirc colours/" + fg_bg[ 1 ],
                                    DEFAULT_MIRC_BACKGROUND_COLOUR
                                )
                            );
                            StyleConstants.setBackground(
                                formatting,
                                new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] )
                            );
                        }

                        formats.add( formatting );
                    }
                    else
                    {
                        StyleConstants.setForeground( formatting, foreground_colour );
                        StyleConstants.setBackground( formatting, background_colour );
                        formats.add( formatting );
                    }
                    indices.add( new Integer( ptr ) );

                    // Remove the parsed format code string.
                    text =
                        text.substring( 0, ptr )
                        + text.substring( ptr + format.length() + 1 );
                    len = text.length();
                    break;
                    
                case MIRC_BOLD_CONTROL_CHAR:
                    bold = ! bold;
                    formatting = new SimpleAttributeSet();
                    StyleConstants.setBold( formatting, bold );

                    formats.add( formatting );
                    indices.add( new Integer( ptr ) );

                    // Remove the parsed format code string.
                    text = text.substring( 0, ptr ) + text.substring( ptr + 1 );
                    len = text.length();
                    
                    break;
                case MIRC_ITALIC_CONTROL_CHAR:
                    italic = ! italic;
                    formatting = new SimpleAttributeSet();
                    StyleConstants.setItalic( formatting, italic );

                    formats.add( formatting );
                    indices.add( new Integer( ptr ) );

                    // Remove the parsed format code string.
                    text = text.substring( 0, ptr ) + text.substring( ptr + 1 );
                    len = text.length();
                    break;
                case MIRC_UNDERLINE_CONTROL_CHAR:
                    underline = ! underline;
                    formatting = new SimpleAttributeSet();
                    StyleConstants.setUnderline( formatting, underline );

                    formats.add( formatting );
                    indices.add( new Integer( ptr ) );

                    // Remove the parsed format code string.
                    text = text.substring( 0, ptr ) + text.substring( ptr + 1 );
                    len = text.length();
                    break;
                case MIRC_NORMAL_CONTROL_CHAR:
                    formats.add( null );
                    indices.add( new Integer( ptr ) );

                    // Remove the parsed format code string.
                    text = text.substring( 0, ptr ) + text.substring( ptr + 1 );
                    len = text.length();
                    break;
                default:
                    ptr++;
                    continue;
                    //break;
            }
        }
        
        try
        {
            document.insertString(
                offset,
                text,
                text_pane.getStyle(
                    colour_toggle ? "normal" : "alternate"
                )
            );
            
            // Apply mIRC formatting.
                    
            int index;
            SimpleAttributeSet sas;
            
            for( int i = 0, n = formats.size(); i < n; i++ )
            {
                index = ((Integer) indices.elementAt( i )).intValue();
                sas = (SimpleAttributeSet) formats.elementAt( i );
                if( ( sas != null ) && paint_mirc_codes )
                {
                    document.setCharacterAttributes(
                        offset + index,
                        text.length() - index,
                        sas,
                        false
                    );
                }
                else
                {
                    document.setCharacterAttributes(
                        offset + index,
                        text.length() - index,
                        text_pane.getStyle(
                            colour_toggle ? "normal" : "alternate"
                        ),
                        true
                    );
                }
            }
            
            colour_toggle = ! colour_toggle;
        }
        catch( BadLocationException e )
        {
            display_manager.printlnDebug( e.getMessage() );
        }
        
        GIPaneWrapper selected_gipw = display_manager.getSelectedPane();
        boolean highlight_button = true;
        if( selected_gipw == gipw )
        {
            highlight_button = false;
        }
        
        if( highlight_button )
        {
            gipw.highlightButton();
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
        
        return text;
    }
     */
    
    /*
    public void applyStyle( int offset, int length, String style_name )
    {
        text_pane.getStyledDocument().setCharacterAttributes(
            offset, length, text_pane.getStyle( style_name ), false
        );
    }
     */
    
    /**
     * Print starting at the current cursor location.
     */
    public synchronized void print( String text )
    {
        console_pane.output( text, current_attributes );
        
        GIPaneWrapper selected_gipw = display_manager.getSelectedPane();
        boolean highlight_button = true;
        if( selected_gipw == gipw )
        {
            highlight_button = false;
        }
        
        if( highlight_button )
        {
            gipw.highlightButton();
        }
    }
    
    public synchronized void printAt( String text, int x, int y )
    {
        console_pane.setCursorPosition( x, y );
        print( text );
    }
    
    /**
     * @return true if and only if the filter for this text pane accepts messages with the given qualities
     */
    /*
    public boolean accepts( String qualities )
    {
        boolean result = false;
        try
        {
            result = BoolExpEvaluator.evaluate( filter, qualities );
        }
        catch( BadExpressionException e )
        {
            display_manager.printlnDebug(
                i18n_manager.getString( "filter error", new Object [] { filter } )
            );
            display_manager.printlnDebug( e.getMessage() );
        }
        return result;
    }
     */
    
    public String getText( int offset, int length )
        throws BadLocationException
    {
        //return text_pane.getStyledDocument().getText( offset, length );
        return null;
    }
    
    /*
    public String getFilter()
    {
        return filter;
    }
    
    public void setFilter( String filter )
    {
        this.filter = filter;
        title = filter;
    }
     */
    
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
    
    /*
    public int getDocumentLength()
    {
        return text_pane.getStyledDocument().getLength();
    }
    
    public void setPaintMIRCCodes( boolean setting )
    {
        paint_mirc_codes = setting;
    }
    
    public void clearDocument()
    {
        text_pane.setDocument( text_pane.getEditorKit().createDefaultDocument() );
        display_manager.getStyleManager().initializeTextPane( text_pane );
    }
     */
    
    public boolean findText( String text_, boolean case_insensitive )
    {
        String text = case_insensitive ? text_.toLowerCase( i18n_manager.getLocale() ) : text_;
        boolean found = false;
        last_search_text = text;
        
        /*
        try
        {
            Document document = text_pane.getDocument();
            String document_text = document.getText( 0, document.getLength() );
            if( case_insensitive )
            {
                document_text = document_text.toLowerCase( i18n_manager.getLocale() );
            }
            int index;
            if( last_search_text.equals( text ) )
            {
                index = document_text.indexOf( text, last_found_index + 1 );
            }
            else
            {
                index = document_text.indexOf( text );
            }
            last_found_index = index;
            java.awt.Rectangle r = text_pane.modelToView( index );
            if( r != null )
            {
                java.awt.Point p = new java.awt.Point( 0, (int) r.getY() );
                getViewport().setViewPosition( p );
                found = true;
            }
        } catch( BadLocationException e ) { }
        */
        
        return found;
    }
    
    public enigma.console.TextWindow getEnigmaTextWindow()
    {
        return console_pane;
    }
    
    public AnsiOutputStream getANSIStream()
    {
        return ansi_stream;
    }
    
    public PrintStream getPrintStream()
    {
        return print_stream;
    }
    
    public void setTextAttributes( TextAttributes attribs )
    {
        current_attributes = attribs;
    }
}
