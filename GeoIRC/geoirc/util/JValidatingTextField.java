/*
 * JValidatingTextField.java
 * 
 * Created on 14.08.2003
 */
package geoirc.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * A validating text input field with validating based on regular expressions.<br>
 * As long as the inserted value does not validate a red<br>
 * border will be drawn around the input field.
 * @author netseeker aka Michael Manske
 */
public class JValidatingTextField extends JTextField implements DocumentListener {
    protected static final Border normalBorder = BorderFactory.createLineBorder(Color.BLACK);
    protected static final Border errorBorder = BorderFactory.createLineBorder(Color.RED);
    public static final int PREFERED_WIDTH = 100;
    public static final int PREFERED_HEIGHT = 18;

    protected boolean textValid = true;
    protected Pattern pattern;

    /**
     * Creates a new instance
     */
    public JValidatingTextField() {
        super();
        this.setPreferredSize(new Dimension(PREFERED_WIDTH, PREFERED_HEIGHT));
        init();
    }

    /**
     * Creates a new instance with an available value
     * @param regex regular expression to use for validating input
     * @param value value to be inserted in the text field
     */
    public JValidatingTextField(String regex, String value) {
        this();
        setPattern(regex);
        if (value != null)
            this.setText(value);
    }

    public JValidatingTextField(String regex, String value, int width) {
        this(regex, value);
        this.setPreferredSize(new Dimension(width, PREFERED_HEIGHT));
    }

    protected void init() {
        //setIgnoreRepaint(true);

        if (pattern == null || pattern.pattern() == null) {
            pattern = Pattern.compile(".+");
        }

        setVerifyInputWhenFocusTarget(true);
        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent arg0) {
                validateText();
            }

            public void focusLost(FocusEvent arg0) {
                validateText();
            }
        });

        validateText();

    }

    /* (non-Javadoc)
     * @see javax.swing.text.JTextComponent#setDocument(javax.swing.text.Document)
     */
    public void setDocument(Document doc) {
        if (getDocument() != null) {
            getDocument().removeDocumentListener(this);
        }
        super.setDocument(doc);
        if (getDocument() != null) {
            getDocument().addDocumentListener(this);
        }
    }

    /**
     * sets a new regular expression used for validating
     * @param regex regular expression to use for validating input
     */
    public void setPattern(String regex) {
        if (regex == null)
            regex = new String(".+");
        pattern = Pattern.compile(regex);
        validateText();
    }

    protected void validateText() {
        String t = getText();
        int pos = (pattern.pattern().indexOf("+") != -1) ? 1 : 0;

        boolean valid = ( t.length() >= pos && pattern.matcher(t).matches() ) || isEnabled() == false;
        firePropertyChange("textValid", !valid, valid);
        setBorder(valid ? normalBorder : errorBorder);

        setTextValid(valid);
    }

    /**
     * checks if the inserted value does validate
     * @return true if the inserted value validates
     * otherwise false
     */
    public boolean isTextValid() {
        return textValid;
    }

    public boolean isEmpty() {
        String str = getText();
        if (str == null)
            return true;
        else if (str.length() == 0)
            return true;

        return false;
    }

    protected void setTextValid(boolean valid) {
        textValid = valid;
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent arg0) {
        validateText();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent arg0) {
        validateText();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent arg0) {
        validateText();
    }

    public boolean isOpaque() {
        return true;
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        validateText();
    }
}
