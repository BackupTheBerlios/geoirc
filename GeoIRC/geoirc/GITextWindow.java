/*
 * GITextWindow.java
 *
 * Created on June 23, 2003, 7:42 PM
 */

package geoirc;

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
        String title
    )
    {
        this(
            display_manager,
            settings_manager,
            title,
            (String) null
        /*, (RemoteMachine) null */ );
    }

    public GITextWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        StyleManager style_manager,
        String title,
        String filter
    )
    {
        /*
        this( display_manager, settings_manager, title, filter, (RemoteMachine) null );
    }
    
    public GITextWindow(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        String filter,
        RemoteMachine associated_machine
    )
    {
         */
        super();
        setIconifiable( true );
        setClosable( true );
        setMaximizable( true );
        setResizable( true );
        setTitle( title );
        
        addInternalFrameListener( display_manager );
        
        this.display_manager = display_manager;
        this.filter = filter;
        //this.associated_machine = associated_machine;
        this.settings_manager = settings_manager;
        
        text_pane = new JTextPane();
        text_pane.setEditable( false );
        text_pane.setForeground( new Color( 204, 204, 204 ) );
        text_pane.setBackground( new Color( 0, 0, 0 ) );

        scroll_pane = new JScrollPane( text_pane );
        scroll_pane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        getContentPane().add( scroll_pane );
        scrollbar = scroll_pane.getVerticalScrollBar();
        
        document = text_pane.getDocument();

        style_manager.initializeTextPane( text_pane );
        
        Style s = text_pane.addStyle( "italic", style_normal );
        StyleConstants.setItalic( s, true );
        
        s = text_pane.addStyle( "bold", style_normal );
        StyleConstants.setBold( s, true );
        
        s = text_pane.addStyle( "underline", style_normal );
        StyleConstants.setUnderline( s, true );
        
        /*
        s = text_pane.addStyle( "blue", style_normal );
        StyleConstants.setBackground( s, new Color( 0, 0, 255 ) );
         */
        
        selectFrameAndAssociatedButtons();
        
    }
    
    public void appendLine( String text )
    {
        append( text + "\n" );
    }
    
    synchronized public void append( String text )
    {
        // Split this string into fragments along the style markings.
        
        StringTokenizer st = new StringTokenizer( text, Character.toString( STYLE_ESCAPE_CHAR ) );
        boolean has_escape_char = ( text.charAt( 0 ) == STYLE_ESCAPE_CHAR );
        String fragment;
        
        while( st.hasMoreTokens() )
        {
            fragment = st.nextToken();
            
            if( has_escape_char )
            {
                String control_string = fragment.substring( 0, fragment.indexOf( STYLE_TERMINATOR_CHAR ) );
                int len = control_string.length();
                for( int c = 0; c < len; c++ )
                {
                    if( c > len - 2 )
                    {
                        display_manager.printlnDebug( "Invalid style control string: "
                            + control_string );
                        break;
                    }
                    else
                    {
                        String code = control_string.substring( c, 2 );
                        if( code.equals( STYLE_FOREGROUND ) )
                        {
                        }
                        else if( code.equals( STYLE_BACKGROUND ) )
                        {
                        }
                        else if( code.equals( STYLE_BOLD ) )
                        {
                        }
                        else if( code.equals( STYLE_ITALIC ) )
                        {
                        }
                        else if( code.equals( STYLE_UNDERLINE ) )
                        {
                        }
                    }
                }
            }
            else
            {
                has_escape_char = true;
            }
        
            Style style = text_pane.getStyle( style_string );
            if( style == null )
            {
                style = default_style;
            }

            try
            {
                document.insertString( document.getLength(), text, style );
            }
            catch( BadLocationException e )
            {
                e.printStackTrace();
            }
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
