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
import geoirc.util.LayoutUtil;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * @author netseeker aka Michael Manske
 */
public class RegularExpressionTester extends JDialog implements DocumentListener
{
    private static final String title = "Regular Expression Tester";
    private JRegExTextField regexp_field = new JRegExTextField(null);
    private JBoolRegExTextField boolexp_field = new JBoolRegExTextField(null);
    private JValidatingTextPane input_field = new JValidatingTextPane(null);
    private JTextArea output_field = new JTextArea();
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
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setTitle(title);
        this.getContentPane().setLayout(layout);
        this.setResizable(false);
        Container content_pane = getContentPane();

        regexp_field.setPreferredSize(new Dimension(200, JValidatingTextField.PREFERED_HEIGHT));
        regexp_field.getDocument().addDocumentListener(this);

        boolexp_field.setPreferredSize(new Dimension(200, JValidatingTextField.PREFERED_HEIGHT));
        boolexp_field.getDocument().addDocumentListener(this);

        filter_box = new JCheckBox("Use Boolean Expression Tester", this.bool_only);

        filter_box.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                setBoolFilter(filter_box.isSelected());
            }
        });

        content_pane.add(filter_box, LayoutUtil.getGBC(0, 0, 2, 1));
        content_pane.add(new JLabel("Regular Expression"), LayoutUtil.getGBC(0, 1, 1, 1));
        content_pane.add(regexp_field, LayoutUtil.getGBC(1, 1, 2, 1));
        content_pane.add(boolexp_field, LayoutUtil.getGBC(1, 1, 2, 1));

        content_pane.add(new JLabel("Value to validate"), LayoutUtil.getGBC(0, 2, 3, 1, GridBagConstraints.CENTER));

        input_field.setPreferredSize(new Dimension(300, 50));
        input_field.setFont(regexp_field.getFont());
        input_field.setUseErrorBorder(true);
        input_field.getDocument().addDocumentListener( new InputHandler( regexp_field, output_field ) );
        
        content_pane.add(
            new JScrollPane(input_field),
            LayoutUtil.getGBC(0, 3, 3, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL));

        showInputFields();

        output_field.setEditable(false);
        output_field.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        //output_field.setBackground( Color.LIGHT_GRAY );
        output_field.setPreferredSize(input_field.getPreferredSize());
        output_field.setLineWrap( true );
        content_pane.add(
            new JScrollPane(output_field),
            LayoutUtil.getGBC(0, 4, 3, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL));

        this.pack();
    }

    private void showInputFields()
    {
        regexp_field.setVisible(!bool_only);
        boolexp_field.setVisible(bool_only);
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
    public void setBoolFilter(boolean boolonly)
    {
        if (this.bool_only != boolonly)
        {
            if (boolonly)
            {
                this.boolexp_field.setText(this.regexp_field.getText());
            }
            else
            {
                this.regexp_field.setText(this.boolexp_field.getText());
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
    public void setRegExp(String regexp)
    {
        if (this.bool_only)
        {
            this.boolexp_field.setText(regexp);
        }
        else
        {
            this.regexp_field.setText(regexp);
        }
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
        JTextField source = null;

        if (this.bool_only)
        {
            source = this.boolexp_field;
        }
        else
        {
            source = this.regexp_field;
        }

        if (source.isValid())
        {
            input_field.setPattern(source.getText());
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
        JTextArea output;

        public InputHandler(JValidatingTextField input, JTextArea output)
        {
            this.input = input;
            this.output = output;
        }

        private void showMatchSequenzes(String text)
        {
            if( input.isValid() )
            {
                output.setText("");
                
                Pattern pattern = Pattern.compile(input.getText());
                String[] matches = pattern.split(text);                
    
                for (int i = 0; i < matches.length; i++)
                {
                    String str = matches[i];
    
                    if (str.length() > 0)
                    {
                        output.append(str + "\t=> no match\n\r");
                    }
                }
            }
            else
            {
                output.setText( "The entered regular expression is not valid." );
            }
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
                    showMatchSequenzes(doc.getText(0, doc.getLength() - 1));
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
                    showMatchSequenzes(doc.getText(0, doc.getLength() - 1));
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
                    showMatchSequenzes(doc.getText(0, doc.getLength() - 1));
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
