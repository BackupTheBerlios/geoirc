/*
 * BaseSettingsPanel.java
 * 
 * Created on 14.08.2003
 */
package geoirc.conf;

import geoirc.GeoIRCConstants;
import geoirc.XmlProcessable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextComponent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

/**
 * @author netseeker aka Michael Manske
 */
public abstract class BaseSettingsPanel extends JPanel implements GeoIRCConstants, Storable
{
    protected XmlProcessable settings_manager = null;
    protected GeoIRCDefaults rules = null;
    protected GridBagLayout layout = new GridBagLayout();
    protected List childs = new ArrayList();
    protected ValidationListener validation_listener;
    private ArrayList target_validation_listeners = new ArrayList();
    private InputChangeListener change_listener;
    private ArrayList target_change_listeners = new ArrayList();
    private boolean isInitialized = false;
    private boolean has_changes = false;
    private HashSet has_errors_set = new HashSet();

    private BaseSettingsPanel()
    {
        super();
        setVisible(false);
        setMinimumSize(new Dimension());
        setLayout(layout);
        setName(getClass().getName());
    }

    /**
     * @param settings
     */
    public BaseSettingsPanel(
        XmlProcessable settings,
        GeoIRCDefaults valueRules,
        String name)
    {
        this();
        this.settings_manager = settings;
        this.rules = valueRules;
        setName(name);
        this.validation_listener = new ValidationListener()
        {
            void validationPerformed(Object source, boolean isvalid)
            {
                if( isvalid == false )
                {
                    has_errors_set.add(source);
                }
                else
                {
                    if(has_errors_set.contains(source))
                    {
                        has_errors_set.remove(source);
                    }                    
                }

                for( Iterator it = target_validation_listeners.iterator(); it.hasNext(); )
                {
                    ValidationListener listener = (ValidationListener)it.next();
                    listener.validationPerformed( this, isvalid );
                }                
            }            
        };
        
        this.change_listener = new InputChangeListener()
        {
            public void valueChanged(Object source)
            {
                if (isVisible() == true && isInitialized() == true)
                {
                    has_changes = true;
                    
                    for( Iterator it = target_change_listeners.iterator(); it.hasNext(); )
                    {
                        InputChangeListener listener = (InputChangeListener)it.next();
                        listener.valueChanged( this );
                    }
                }
            }
        };
    }

    /**
     * @param c
     * @param x
     * @param y
     * @param width
     * @param height
     * @param weightx
     * @param weighty
     */
    public Component addComponent(Component c, int x, int y, int width, int height, double weightx, double weighty)
    {
        return addComponent(
            c,
            x,
            y,
            width,
            height,
            weightx,
            weighty,
            new Insets(5, 5, 5, 5),
            GridBagConstraints.NORTHWEST);
    }

    public Component addComponent(
        Component c,
        int x,
        int y,
        int width,
        int height,
        double weightx,
        double weighty,
        Insets insets)
    {
        return addComponent(c, x, y, width, height, weightx, weighty, insets, GridBagConstraints.NORTHWEST);
    }

    public Component addComponent(
        Component c,
        int x,
        int y,
        int width,
        int height,
        double weightx,
        double weighty,
        int align)
    {
        return addComponent(c, x, y, width, height, weightx, weighty, new Insets(5, 5, 5, 5), align);
    }

    public Component addComponent(
        Component c,
        int x,
        int y,
        int width,
        int height,
        double weightx,
        double weighty,
        Insets insets,
        int align)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.anchor = align;
        gbc.insets = insets;

        layout.setConstraints(c, gbc);

        registerComponentForValueChangeListener(c);

        return add(c);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getName();
    }

    /**
     * @param child
     */
    public void addChild(BaseSettingsPanel child)
    {
        this.childs.add(child);
    }

    /**
     * @return
     */
    public List getChilds()
    {
        return this.childs;
    }

    /**
     * @param c
     */
    public void copySizes(Component c)
    {
        this.setMinimumSize(c.getMinimumSize());
        this.setPreferredSize(c.getPreferredSize());
        this.setMaximumSize(c.getMaximumSize());
        this.setBounds(c.getBounds());
    }

    public void addLayoutStopper(int x, int y)
    {
        JLabel label = new JLabel();
        //label.setVisible(false);
        addComponent(label, x, y, 1, 1, 0, 1, GridBagConstraints.NORTHWEST);
    }

    public void addHorizontalLayoutStopper(int x, int y)
    {
        JLabel label = new JLabel();
        //label.setVisible(false);
        addComponent(label, x, y, 1, 1, 1, 0, GridBagConstraints.NORTHWEST);
    }

    public void setVisible(boolean visible)
    {
        if (visible == true && isInitialized == false)
        {
            initialize();
            isInitialized = true;
        }

        super.setVisible(visible);
    }

    public boolean isInitialized()
    {
        return isInitialized;
    }

    /**
     * @param c
     * TODO: add further java awt/swing component types
     */
    private void registerComponentForValueChangeListener(Component c)
    {
        //Swing Components
        if (c instanceof JTextComponent)
        {
            ((JTextComponent)c).getDocument().addDocumentListener(change_listener);
        }
        else if (c instanceof JComboBox)
        {
            ((JComboBox)c).addActionListener(change_listener);
        }
        else if (c instanceof JCheckBox)
        {
            ((JCheckBox)c).addActionListener(change_listener);
        }
        else if (c instanceof JTable)
        {
            ((JTable)c).getModel().addTableModelListener(change_listener);
        }
        else if (c instanceof TextComponent)
        {
            ((TextComponent)c).addTextListener(change_listener);
        }
        //try to register component containers in a recursive way
        else if (c instanceof JComponent)
        {
            Component[] components = ((JComponent)c).getComponents();
            for (int i = 0; i < components.length; i++)
            {
                registerComponentForValueChangeListener(components[i]);
            }
        }
    }

    public boolean hasChanges()
    {
        return has_changes;
    }
    
    public boolean hasErrors()
    {
        return !has_errors_set.isEmpty();
    }
    
    public void addInputChangeListener( InputChangeListener listener )
    {
        target_change_listeners.add(listener);
    }
    
    public void removeInputChangeListener( InputChangeListener listener )
    {
        target_change_listeners.remove(listener);
    }

    public void addValidationListener( ValidationListener listener )
    {
        target_validation_listeners.add(listener);
    }
    
    public void removeValidationListener( ValidationListener listener )
    {
        target_validation_listeners.remove(listener);
    }

    //ABSTRACT METHODS
    public abstract void initialize();
    public abstract boolean saveData();
}
