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

        StringBuffer str = new StringBuffer();

        if (evt.isActionKey())
        {
            System.out.println("keyPressed " + evt);
            str.append("|");
            str.append(KeyEvent.getKeyText(evt.getKeyCode()));
            setText(str.toString());
        }
    }

    private String getModifiers(KeyEvent evt) {
        StringBuffer sb = new StringBuffer();
        sb.append(KeyEvent.getKeyModifiersText(evt.getModifiers()));
        return sb.toString();//.replaceAll("\\p{Punct}", "|");
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent evt) {
        StringBuffer str = new StringBuffer();
        char c = evt.getKeyChar();
        int modifiers = evt.getModifiers();
                
        str.append(KeyEvent.getKeyModifiersText(modifiers));
        str.append("|");
        
        if (!Character.isISOControl(c)) {
            str.append(Character.toString(c));
        }

        setText(str.toString());
        System.out.println(evt);
    }
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent arg0) {}
}
