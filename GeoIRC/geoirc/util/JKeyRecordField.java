/*
 * JKeyRecordField.java
 * 
 * Created on 04.09.2003
 */
package geoirc.util;

import geoirc.GeoIRCConstants;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

/**
 * @author netseeker aka Michael Manske
 */
public class JKeyRecordField
    extends JTextField
    implements KeyListener, GeoIRCConstants {
    public JKeyRecordField() {
        super();
        //setEditable(false);
        setPreferredSize(
            new Dimension(
                JValidatingTextField.PREFERED_WIDTH,
                JValidatingTextField.PREFERED_HEIGHT));
        
        removeKeyListener(this);        
        addKeyListener(this);
    }
    /**
     * @param arg0
     */
    public JKeyRecordField(String hotkey) {
        this();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public JKeyRecordField(String hotkey, int width, int height) {
        this(hotkey);
        setPreferredSize(new Dimension(width, height));
    }

    protected boolean setHokey() {
        return true;
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent evt) {
        //System.out.println(evt);
        StringBuffer str = new StringBuffer();

        str.append(getModifiers(evt));
        str.append("|");

        if (evt.isActionKey()) {
            str.append(KeyEvent.getKeyText(evt.getKeyCode()));
        }
        else {
            char c = evt.getKeyChar();

            if (!Character.isISOControl(c)) {
                str.append(Character.toString(c));
            }
            else {
                int code = evt.getKeyCode();
                str.append(KeyEvent.getKeyText(code));
            }
        }
        
        evt.consume();
        setText(str.toString());
    }

    private String getModifiers(KeyEvent evt) {         
        int modifiers = evt.getModifiers();
        if(modifiers != 0)
        {
            String sb = KeyEvent.getKeyModifiersText(modifiers);
            return sb.replaceAll("\\+", "|");
        }
        
        return "";            
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent evt) {
        evt.consume();
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent evt) {
        evt.consume();
    }
    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
}
