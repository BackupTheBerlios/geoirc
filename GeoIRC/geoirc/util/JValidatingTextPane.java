/*
 * JValidatingTextPane.java
 * 
 * Created on 25.09.2003
 */
package geoirc.util;

import geoirc.conf.ValidationListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * @author netseeker aka Michael Manske
 */
public class JValidatingTextPane extends JEditorPane implements DocumentListener
{
    protected boolean textValid = true;
    protected Pattern pattern;

    public static final int PREFERED_WIDTH = 300;
    public static final int PREFERED_HEIGHT = 80;

    /**
     * 
     */
    public JValidatingTextPane(ValidationListener listener)
    {
        super();
        setContentType("text/plain");
        addValidationListener(listener);
        setPreferredSize(new Dimension(PREFERED_WIDTH, PREFERED_HEIGHT));
        init();
    }

    /**
     * Creates a new instance with an available value
     * @param regex regular expression to use for validating input
     * @param value value to be inserted in the text field
     */
    public JValidatingTextPane(String regex, String value, ValidationListener listener)
    {
        this(listener);
        setPattern(regex);
        if (value != null)
            this.setText(value);
    }

    /**
     * @param regex
     * @param value
     * @param listener
     * @param width
     */
    public JValidatingTextPane(String regex, String value, ValidationListener listener, int width)
    {
        this(regex, value, listener);
        setPreferredSize(new Dimension(width, PREFERED_HEIGHT));
    }

    /**
     * 
     */
    protected void init()
    {
        this.setBorder( BorderFactory.createLineBorder(Color.BLACK) );
        
        if (pattern == null || pattern.pattern() == null)
        {
            pattern = Pattern.compile(".*", Pattern.DOTALL);
        }

        setVerifyInputWhenFocusTarget(true);
        this.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent arg0)
            {
                validateText();
            }

            public void focusLost(FocusEvent arg0)
            {
                validateText();
            }
        });

        validateText();

    }

    /* (non-Javadoc)
     * @see javax.swing.text.JTextComponent#setDocument(javax.swing.text.Document)
     */
    public void setDocument(Document doc)
    {
        if (getDocument() != null)
        {
            getDocument().removeDocumentListener(this);
        }
        super.setDocument(doc);
        if (getDocument() != null)
        {
            getDocument().addDocumentListener(this);
        }
    }

    /**
     * sets a new regular expression used for validating
     * @param regex regular expression to use for validating input
     */
    public void setPattern(String regex)
    {
        if (regex == null)
            regex = new String(".*");
        pattern = Pattern.compile(regex, Pattern.DOTALL);
        validateText();
    }

    /**
     * 
     */
    protected void validateText()
    {
        String t = getText();
        //int pos = (pattern.pattern().indexOf("+") != -1) ? 1 : 0;

        //boolean valid = (t.length() >= pos && pattern.matcher(t).matches()) || isEnabled() == false;
        boolean valid = pattern.matcher(t).matches() || isEnabled() == false;
        
        setTextValid(valid);
    }

    /**
     * checks if the inserted value does validate
     * @return true if the inserted value validates
     * otherwise false
     */
    public boolean isValid()
    {
        return textValid;
    }

    /**
     * @return
     */
    public boolean isEmpty()
    {
        String str = getText();
        if (str == null)
            return true;
        else if (str.length() == 0)
            return true;

        return false;
    }

    /**
     * @param valid
     */
    protected void setTextValid(boolean valid)
    {
        if( textValid != valid )
        {
            firePropertyChange(ValidationListener.VALIDATION_RESULT, textValid, valid);
            textValid = valid;                        
        }
        setForeground(valid ? Color.BLACK : Color.RED);
        setCaretColor(valid ? Color.BLACK : Color.RED);        
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent arg0)
    {
        validateText();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent arg0)
    {
        validateText();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent arg0)
    {
        validateText();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        if( !enabled )
        {
            this.setBackground( Color.LIGHT_GRAY );
        }
        else
        {
            this.setBackground( Color.WHITE );
        }        
    }

    /**
     * @param listener
     */
    public void addValidationListener(ValidationListener listener)
    {
        addPropertyChangeListener(ValidationListener.VALIDATION_RESULT, listener);
    }

    public PropertyChangeListener[] getValidationListeners()
    {
        return getPropertyChangeListeners(ValidationListener.VALIDATION_RESULT);
    }

    /**
     * @param listener
     */
    public void removeValidationListener(ValidationListener listener)
    {
        removePropertyChangeListener(listener);
    }    

    /* (non-Javadoc)
     * @see javax.swing.text.JTextComponent#setEditable(boolean)
     */
    public void setEditable(boolean enabled)
    {
        super.setEditable(enabled);
        
        if( !enabled )
        {
            this.setBackground( Color.LIGHT_GRAY );
        }
        else
        {
            this.setBackground( Color.WHITE );
        }        
    }

}
