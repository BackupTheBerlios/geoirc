/*
 * GITextWindow.java
 *
 * Created on June 23, 2003, 7:42 PM
 */

package geoirc;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import org.jscroll.*;
import org.jscroll.widgets.*;

/**
 *
 * @author  Pistos
 */
public class GITextWindow extends JScrollInternalFrame
//public class GITextWindow extends JInternalFrame
{
    protected JScrollPane scroll_pane;
    protected JTextPane text_pane;
    protected Style default_style;
    protected Document document;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected String filter;
    //protected RemoteMachine associated_machine;
    protected JScrollBar scrollbar;
    
    // No default constructor.
    private GITextWindow() { }

    public GITextWindow( DisplayManager display_manager, SettingsManager settings_manager, String title )
    {
        this( display_manager, settings_manager, title, (String) null /*, (RemoteMachine) null */ );
    }

    public GITextWindow( DisplayManager display_manager, SettingsManager settings_manager, String title, String filter )
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

        // Setup some default text styles.
        
        Style style_def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
        StyleConstants.setFontFamily(
            style_def,
            settings_manager.getString( "/gui/text windows/font face", "Lucida Console" )
        );
        
        Style style_normal = text_pane.addStyle( "normal", style_def );
        StyleConstants.setFontSize(
            style_normal,
            settings_manager.getInt( "/gui/text windows/font size", 14 )
        );
        default_style = style_normal;
        
        Style s = text_pane.addStyle( "italic", style_normal );
        StyleConstants.setItalic( s, true );
        
        s = text_pane.addStyle( "bold", style_normal );
        StyleConstants.setBold( s, true );
        
        selectFrameAndAssociatedButtons();
        
    }
    
    public void appendLine( String text )
    {
        appendLine( text, "normal" );
    }
    
    public void appendLine( String text, String style )
    {
        append( text + "\n", style );
    }

    public void append( String text )
    {
        append( text, "normal" );
    }
    
    synchronized public void append( String text, String style_string )
    {
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
