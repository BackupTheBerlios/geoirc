/*
 * BaseSettingsPanel.java
 * 
 * Created on 14.08.2003
 */
package geoirc.conf;

import geoirc.XmlProcessable;
import geoirc.util.JValidatingTextField;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author netseeker aka Michael Manske
 */
public abstract class BaseSettingsPanel extends JPanel
{
	protected XmlProcessable settings_manager = null;
	protected GeoIRCDefaults rules = null;
	protected GridBagLayout layout = new GridBagLayout();
	protected String name = null;
	protected List childs = new ArrayList();
    protected boolean isInitialized = false;

	/**
	 * @param settings
	 */
	public BaseSettingsPanel(
		XmlProcessable settings,
		GeoIRCDefaults valueRules,
		String name)
	{
		super();
		setVisible(false);
		this.settings_manager = settings;
		this.rules = valueRules;
		this.name = name;
		setMinimumSize(new Dimension());
		setLayout(layout);
	}

	/**
	 * @param valueRules
	 * @return
	 */
	public boolean checkDefaults(Map valueRules)
	{
		for (int i = 0; i < getComponentCount(); i++)
		{
			Component c = getComponent(i);
			if (c instanceof JValidatingTextField)
			{
				if (!((JValidatingTextField) c).isTextValid())
					return false;
			}
		}

		return true;
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
	public Component addComponent(
		Component c,
		int x,
		int y,
		int width,
		int height,
		double weightx,
		double weighty)
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
		return addComponent(
			c,
			x,
			y,
			width,
			height,
			weightx,
			weighty,
			insets,
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
		int align)
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
			align);
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

		return add(c);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		if (name != null)
			return name;
		else
			return super.toString();
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
    
    public abstract void initialize();
    
    public void setVisible(boolean visible)
    {
        if(visible == true && isInitialized == false)
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
}
