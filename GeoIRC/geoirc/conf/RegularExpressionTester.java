/*
 * RegularExpressionTester.java
 * 
 * Created on 28.09.2003
 */
package geoirc.conf;

import geoirc.util.JRegExTextField;
import geoirc.util.JValidatingTextField;
import geoirc.util.JValidatingTextPane;
import geoirc.util.LayoutUtil;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * @author netseeker aka Michael Manske
 */
public class RegularExpressionTester extends JDialog implements DocumentListener
{
    private static final String title = "Regular Expression Tester";
    private JRegExTextField regexp_field = new JRegExTextField(null);
    private JValidatingTextPane input_field = new JValidatingTextPane(null);
    private JEditorPane output_field = new JEditorPane();
    private JButton apply_button = new JButton("Apply");
    private JButton close_button = new JButton("Close");    
    private GridBagLayout layout = new GridBagLayout();    
    private List close_listeners = new ArrayList();
    private JPanel button_panel = new JPanel();    
    
    public static final String APPLY_OPTION = "apply";
    public static final String CANCEL_OPTION = "cancel";
    
    public static final int SHOW_CANCEL_OPTION = 0;
    public static final int SHOW_APPLY_CANCEL_OPTION = 1;
    public static final int SHOW_APPLY_OPTION = 2;

    /**
     * @throws java.awt.HeadlessException
     */
    public RegularExpressionTester(int options)
    {
        super();
        initComponents(options);
    }

    /**
     * @param arg0
     */
    public RegularExpressionTester(Dialog arg0, int options)
    {
        super(arg0);
        initComponents(options);
    }

    /**
     * @param arg0
     */
    public RegularExpressionTester(Frame arg0, int options)
    {
        super(arg0);
        initComponents(options);
    }

    private void initComponents(int options)
    {
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setTitle(title);
        this.getContentPane().setLayout(layout);
        this.setResizable(false);
        Container content_pane = getContentPane();
        button_panel.setLayout( new FlowLayout());        

        regexp_field.setPreferredSize(new Dimension(300, JValidatingTextField.PREFERED_HEIGHT));
        regexp_field.getDocument().addDocumentListener(this);

        content_pane.add(new JLabel("Regular Expression"), new  GridBagConstraints(0, 0, 2, 1, 1, 0,  GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,0,0,0), 0, 0));
        content_pane.add(regexp_field, LayoutUtil.getGBC(0, 1, 2, 1));

        content_pane.add(new JLabel("Value to validate"), new  GridBagConstraints(0, 2, 2, 1, 1, 0,  GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));        
        input_field.setPreferredSize(new Dimension(300, 75));
        input_field.setFont(regexp_field.getFont());
        input_field.getDocument().addDocumentListener(new InputHandler(regexp_field, output_field));
        JScrollPane input_scroller = new JScrollPane(input_field);
        content_pane.add(
            input_scroller,
            LayoutUtil.getGBC(0, 3, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL));

        output_field.setEditable(false);
        output_field.setPreferredSize(input_field.getPreferredSize());
        output_field.setFont(input_field.getFont());
        output_field.setForeground(Color.GRAY);
        output_field.setBackground( new Color ( 246, 248, 250 ));
        JScrollPane output_scroller = new JScrollPane(output_field);
        content_pane.add(
            output_scroller,
            LayoutUtil.getGBC(0, 4, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL));
        
        close_button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                regexp_field.setText("");
                input_field.setText("");
                output_field.setText("");
                setVisible ( false );   
                notifyCloseListeners(CANCEL_OPTION);                
            }
        });

        apply_button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                output_field.setText("");
                setVisible ( false );
                notifyCloseListeners(APPLY_OPTION);               
            }
        });

        switch ( options )
        {
            case SHOW_CANCEL_OPTION:
                button_panel.add(close_button, null);
                break;
            case SHOW_APPLY_CANCEL_OPTION:
                button_panel.add(close_button, null);
                button_panel.add(apply_button, null);
            case SHOW_APPLY_OPTION:
                button_panel.add(apply_button, null);
                break;
           default:
                button_panel.add(close_button, null);           
        }
        
        content_pane.add( button_panel, new GridBagConstraints(1, 5, 1, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0));
        
        this.pack();
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
                setVisible(false);
                notifyCloseListeners(CANCEL_OPTION);           
            }
        };

        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(escape_listener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }

    /**
     * @param regexp
     */
    public void setRegExp(String regexp)
    {
        this.regexp_field.setText(regexp);
    }
    
    public String getRegExp()
    {
        return this.regexp_field.getText();
    }

    /**
     * @param value
     */
    public void setValue(String value)
    {
        this.input_field.setText(value);
    }

    private void showValidationState()
    {
        if (this.regexp_field.isValid())
        {
            input_field.setPattern(this.regexp_field.getText());
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

    class InputHandler implements DocumentListener
    {
        JValidatingTextField input;
        JTextComponent output;

        public InputHandler(JValidatingTextField input, JTextComponent output)
        {
            this.input = input;
            this.output = output;
        }

        private void showMatchSequenzes(String text)
        {
            StringBuffer sb = new StringBuffer();

            if (input.isValid())
            {
                output.setText("");

                Pattern pattern = Pattern.compile(input.getText());
                Matcher matcher = pattern.matcher(text);
                int start = 0;
                int end = 0;

                while (matcher.find(end) == true)
                {
                    start = matcher.start();
                    end = matcher.end();

                    if (start == 0 && end == text.length())
                    {
                        sb.append("the complete string does match");
                        break;
                    }
                    else
                    {
                        sb.append("match found from position " + start + " to position " + end + "\n");
                    }
                }

                if (text.length() > 0 && end == 0)
                {
                    sb.append("no match found");
                }
            }
            else
            {
                sb.append("The entered regular expression is not valid.");
            }

            output.setText(sb.toString());
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent evt)
        {
            Document doc = evt.getDocument();
            if (doc.getLength() > 0)
            {
                try
                {
                    showMatchSequenzes(doc.getText(0, doc.getLength()));
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(DocumentEvent evt)
        {
            Document doc = evt.getDocument();
            if (doc.getLength() > 0)
            {
                try
                {
                    showMatchSequenzes(doc.getText(0, doc.getLength()));
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent evt)
        {
            Document doc = evt.getDocument();
            if (doc.getLength() > 0)
            {
                try
                {
                    showMatchSequenzes(doc.getText(0, doc.getLength()));
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void addCloseListener(ActionListener listener)
    {        
        close_listeners.add(listener);
    }
    
    public void removeCloseListener(ActionListener listener)
    {
        close_listeners.remove(listener);
    }
    
    public ActionListener[] getCloseListeners()
    {
        return (ActionListener[])close_listeners.toArray();
    }
    
    private void notifyCloseListeners(String option)
    {
        ActionEvent evt = new ActionEvent(this, 0, option);
        
        for(Iterator it = close_listeners.iterator(); it.hasNext(); )
        {
            ActionListener listener = (ActionListener)it.next();            
            listener.actionPerformed(evt);
        }
    }
    
    public void setVisible( boolean visible )
    {
        if( visible == true && (this.getRegExp() != null && this.getRegExp().length() > 0))
        {
            this.input_field.requestFocusInWindow();
        }
        
        super.setVisible(visible);
    }
}
