/*
 * GITextWindow.java
 *
 * Created on June 23, 2003, 7:42 PM
 */

package geoirc;

import geoirc.util.*;
import java.awt.*;
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
public class GITextWindow extends JScrollInternalFrame implements GeoIRCConstants
//public class GITextWindow extends JInternalFrame
{
    protected JScrollPane scroll_pane;
    protected JTextPane text_pane;
    protected Document document;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected String filter;
    //protected RemoteMachine associated_machine;
    protected JScrollBar scrollbar;
    
    // No default constructor.
    private GITextWindow() { }

    public GITextWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        StyleManager style_manager,
        String title
    )
    {
        this(
            display_manager,
            settings_manager,
            style_manager,
            title,
            (String) null
        );
    }

    public GITextWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        StyleManager style_manager,
        String title,
        String filter
    )
    {
        super();
        setIconifiable( true );
        setClosable( true );
        setMaximizable( true );
        setResizable( true );
        setTitle( title );
        
        addInternalFrameListener( display_manager );
        
        this.display_manager = display_manager;
        this.filter = filter;
        this.settings_manager = settings_manager;
        
        text_pane = new JTextPane();
        text_pane.setEditable( false );
        text_pane.addKeyListener( display_manager );
        
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

        scroll_pane = new JScrollPane( text_pane );
        scroll_pane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        getContentPane().add( scroll_pane );
        scrollbar = scroll_pane.getVerticalScrollBar();
        
        document = text_pane.getDocument();

        style_manager.initializeTextPane( text_pane );
        
        selectFrameAndAssociatedButtons();
        
    }
    
    public void appendLine( String text )
    {
        append( text + "\n" );
    }
    
    synchronized public void append( String text )
    {
        // Tokenize this string into styled fragments.
        
        String [] fragments = text.split( STYLE_ESCAPE_SEQUENCE );
        String [] fragment_parts;
        boolean first_is_styled = ( text.indexOf( STYLE_ESCAPE_SEQUENCE ) == 0 );
        try
        {
            int index;
            for( int i = 0; i < fragments.length; i++ )
            {
                if( ( ! first_is_styled ) && ( i == 0 ) )
                {
                    document.insertString(
                        document.getLength(),
                        fragments[ i ],
                        text_pane.getStyle( "normal" )
                    );
                }
                else
                {
                    index = fragments[ i ].indexOf( STYLE_TERMINATION_SEQUENCE );
                    if( ( index > -1 ) && ( index < fragments[ i ].length() ) )
                    {
                        fragment_parts = fragments[ i ].split( STYLE_TERMINATION_SEQUENCE, 2 );
                        document.insertString(
                            document.getLength(),
                            fragment_parts[ 1 ],
                            text_pane.getStyle( fragment_parts[ 0 ] )
                        );
                    }
                    else
                    {
                        document.insertString(
                            document.getLength(),
                            fragments[ i ],
                            text_pane.getStyle( "normal" )
                        );
                    }
                }
            }
        }
        catch( BadLocationException e )
        {
            display_manager.printlnDebug( e.getMessage() );
        }

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
    
    /*
    public RemoteMachine getAssociatedMachine()
    {
        return associated_machine;
    }
     */
    
    public String getFilter()
    {
        return filter;
    }
    
    public void setFilter( String filter )
    {
        this.filter = filter;
        setTitle( filter );
    }
    
}
