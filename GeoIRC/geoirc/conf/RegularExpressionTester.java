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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
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
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setTitle(title);
        this.getContentPane().setLayout(layout);
        this.setResizable(false);
        Container content_pane = getContentPane();

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
        
        JButton close_button = new JButton("Close");
        close_button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                regexp_field.setText("");
                input_field.setText("");
                output_field.setText("");
                setVisible( false );                
            }
        });
        content_pane.add(close_button, new  GridBagConstraints(1, 5, 1, 1, 1, 0,  GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0));

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
                hide();
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

                    if (start == 0 && end + 1 == text.length())
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
}
