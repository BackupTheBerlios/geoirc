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
public abstract class BaseSettingsPanel extends JPanel implements GeoIRCConstants
{
    protected XmlProcessable settings_manager = null;
    protected GeoIRCDefaults rules = null;
    protected GridBagLayout layout = new GridBagLayout();
    protected List childs = new ArrayList();
    protected ValidationListener validation_listener;
    private InputChangeListener source_change_listener;
    private InputChangeListener target_change_listener;
    private boolean isInitialized = false;

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
        ValidationListener validationListener,
        InputChangeListener changeListener,
        String name)
    {
        this();
        this.settings_manager = settings;
        this.rules = valueRules;
        setName(name);
        this.validation_listener = validationListener;
        this.target_change_listener = changeListener;
        this.source_change_listener = new InputChangeListener()
        {
            public void valueChanged(Object source)
            {
                if (isVisible() == true && isInitialized() == true)
                {
                    target_change_listener.valueChanged( this );
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
            ((JTextComponent)c).getDocument().addDocumentListener(source_change_listener);
        }
        else if (c instanceof JComboBox)
        {
            ((JComboBox)c).addActionListener(source_change_listener);
        }
        else if (c instanceof JCheckBox)
        {
            ((JCheckBox)c).addActionListener(source_change_listener);
        }
        else if (c instanceof JTable)
        {
            ((JTable)c).getModel().addTableModelListener(source_change_listener);
        }
        else if (c instanceof TextComponent)
        {
            ((TextComponent)c).addTextListener(source_change_listener);
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

    //ABSTRACT METHODS
    public abstract void initialize();
}
