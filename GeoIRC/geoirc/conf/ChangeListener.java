/*
 * ChangeListener.java
 * 
 * Created on 21.09.2003
 */
package geoirc.conf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author netseeker aka Michael Manske
 */
public abstract class ChangeListener implements PropertyChangeListener
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

    abstract void valueChanged(Object source);
}
