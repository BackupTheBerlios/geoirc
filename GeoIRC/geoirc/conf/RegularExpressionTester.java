/*
 * RegularExpressionTester.java
 * 
 * Created on 28.09.2003
 */
package geoirc.conf;

import geoirc.util.JBoolRegExTextField;
import geoirc.util.JRegExTextField;
import geoirc.util.JValidatingTextField;
import geoirc.util.JValidatingTextPane;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author netseeker aka Michael Manske
 */
public class RegularExpressionTester extends JDialog implements DocumentListener
{
    private static final String title = "Regular Expression Tester";
    private JRegExTextField regexp_field;
    private JBoolRegExTextField boolexp_field;
    private JValidatingTextPane input_field;
    private JCheckBox filter_box;
    
    private boolean bool_only = false;
    
    private GridBagLayout layout = new GridBagLayout();
    
    /**
     * @throws java.awt.HeadlessException
     */
    public RegularExpressionTester()
    {
        super();        
        initComponents();
    }

    /**
     * @param arg0
     * @throws java.awt.HeadlessException
     */
    public RegularExpressionTester(Dialog arg0)
    {
        super(arg0);
        initComponents();
    }

    /**
     * @param arg0
     * @throws java.awt.HeadlessException
     */
    public RegularExpressionTester(Frame arg0) throws HeadlessException
    {
        super(arg0);
        initComponents();
    }
    
    private void initComponents()
    {
        this.setDefaultCloseOperation( HIDE_ON_CLOSE );
        this.setTitle(title);       
        this.getContentPane().setLayout( layout );
        this.setResizable( false );
        
        regexp_field = new JRegExTextField( null );
        regexp_field.setPreferredSize( new Dimension( 200, JValidatingTextField.PREFERED_HEIGHT ) );
        regexp_field.getDocument().addDocumentListener( this );
        boolexp_field = new JBoolRegExTextField( null );           
        boolexp_field.setPreferredSize( new Dimension( 200, JValidatingTextField.PREFERED_HEIGHT ) );
        boolexp_field.getDocument().addDocumentListener( this );
        
        filter_box = new JCheckBox( "Use Boolean Expression Tester", this.bool_only );
        
        filter_box.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                setBoolFilter( filter_box.isSelected() );
            }            
        });
        
        addComponent(filter_box, 0, 0, 2, 1);
        addComponent(new JLabel("Regular Expression"), 0, 1, 1, 1);
        addComponent(regexp_field, 1, 1, 2, 1);
        addComponent(boolexp_field, 1, 1, 2, 1);
        
        addComponent(new JLabel("Value to validate"), 0, 2, 1, 1);
        input_field = new JValidatingTextPane( null );
        input_field.setPreferredSize( new Dimension( 300, 50 ) );
        input_field.setFont( regexp_field.getFont() );
        input_field.setUseErrorBorder( true );
        addComponent( input_field, 0, 3, 3, 1, GridBagConstraints.HORIZONTAL );
                                  
        showInputFields();
                       
        this.pack();
    }
    
    private void showInputFields()
    {
        regexp_field.setVisible( !bool_only );
        boolexp_field.setVisible( bool_only );        
    }


    /* (non-Javadoc)
     * @see javax.swing.JDialog#createRootPane()
     */
    protected JRootPane createRootPane()
    {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener escape_listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                hide();
            }
        };

        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(escape_listener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }
    
    /**
     * @param boolonly
     */
    public void setBoolFilter( boolean boolonly )
    {
        if( this.bool_only != boolonly )
        {
            if( boolonly )
            {
                this.boolexp_field.setText( this.regexp_field.getText() );            
            }
            else
            {
                this.regexp_field.setText( this.boolexp_field.getText() );
            }
            
            this.bool_only = boolonly;
        }
    }
    
    /**
     * @return
     */
    public boolean isBoolFilterSet()
    {
        return this.bool_only;
    }
    
    /**
     * @param regexp
     */
    public void setRegExp( String regexp )
    {        
        if( this.bool_only )
        {
            this.boolexp_field.setText( regexp );
        }
        else
        {
            this.regexp_field.setText( regexp );
        }
    }
    
    /**
     * @param value
     */
    public void setValue( String value )
    {
        this.input_field.setText( value );
    }

    /**
     * @param c
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void addComponent(
        Component c,
        int x,
        int y,
        int width,
        int height)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets( 5, 5, 5, 5);

        getContentPane().add(c, gbc);
    }    

    /**
     * @param c
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void addComponent(
        Component c,
        int x,
        int y,
        int width,
        int height,
        int fill)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets( 5, 5, 5, 5);

        getContentPane().add(c, gbc);
    }

    private void showValidationState ()
    {
        JTextField source = null;
        
        if( this.bool_only )
        {
            source = this.boolexp_field;
        }
        else
        {
            source = this.regexp_field;
        }
                               
        if(source.isValid())
        {
            input_field.setPattern( source.getText() );
        }                               
        
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent evt)
    {
        showValidationState();        
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent evt)
    {
        showValidationState();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent evt)
    {
        showValidationState();        
    }    

}
