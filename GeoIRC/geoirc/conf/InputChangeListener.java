/*
 * InputChangeListener.java
 * 
 * Created on 21.09.2003
 */
package geoirc.conf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * @author netseeker aka Michael Manske
 */
public abstract class InputChangeListener
    implements PropertyChangeListener, DocumentListener, ActionListener, TableModelListener
{
    public static String IDENT = "VALUE_CHANGE";

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(IDENT))
        {
            valueChanged(evt.getSource());
        }
    }

    private void documentChange(Document doc)
    {
        String text = "";
        int length = doc.getLength() - 1;

        if (length > 0)
        {
            try
            {
                text = doc.getText(0, length);
            }
            catch (BadLocationException e)
            {}
        }

        PropertyChangeEvent evt = new PropertyChangeEvent(doc, IDENT, "", text);
        propertyChange(evt);
    }

    abstract void valueChanged(Object source);

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent evt)
    {
        documentChange(evt.getDocument());
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent evt)
    {
        documentChange(evt.getDocument());
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent evt)
    {
        documentChange(evt.getDocument());
    }
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt)
    {
        PropertyChangeEvent p_evt = new PropertyChangeEvent(evt.getSource(), IDENT, "0", "1");
        propertyChange(p_evt);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent evt)
    {
        PropertyChangeEvent p_evt = new PropertyChangeEvent(evt.getSource(), IDENT, "0", "1");
        propertyChange(p_evt);
    }
}
