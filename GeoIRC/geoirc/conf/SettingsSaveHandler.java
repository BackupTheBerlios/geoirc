/*
 * SettingsSaveHandler.java
 * 
 * Created on 18.08.2003
 */
package geoirc.conf;

import geoirc.XmlProcessable;

import java.awt.Component;
import java.awt.TextComponent;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

/**
 * A basic handler for saving user input of different types of JComponent
 * to the application preferences (settings.xml)<br>
 * NOTE: At the moment only values of instances of JTextField, JList, JComboBox and<br>
 * JCheckBox will get saved!
 * 
 * @author netseeker aka Michael Manske
 */
public class SettingsSaveHandler
{
	protected LinkedHashMap components = new LinkedHashMap();
	protected XmlProcessable settings_manager;

	/**
	 * Creates a new instance of SettingsSaveHandler
	 * @param settings an instance of SettingsManager
	 */
	public SettingsSaveHandler(XmlProcessable settings)
	{
		this.settings_manager = settings;
	}

	/**
	 * Registers a new input component for later saving
	 * @param component the input component
	 * @param preference_path the associated node path in preferences 
	 */
	public void register(Component component, String preference_path)
	{
		components.put(preference_path, component);
	}

	/**
	 * @return all registered components
	 */
	public Map getRegisteredComponents()
	{
		return components;
	}

	/**
	 * Unregisters a component from saving
	 * @param component Component to unregister
	 * @return True if the given component could be successfully unregistered, otherwise false
	 */
	public boolean unregister(Component component)
	{
		if (components.containsValue(component) == false)
			return false;

		Iterator it = components.keySet().iterator();

		while (it.hasNext())
		{
			String key = (String) it.next();
			Component comp = (Component) components.get(key);
			if (comp.equals(component))
				return unregister(key);
		}

		return false;
	}

	/**
	 * Unregister a component from saving by a given node path
	 * @param preference_path node path in preferences
	 * @return True if a component could be successfully unregistered for the given path, otherwise false
	 */
	public boolean unregister(String preference_path)
	{
		return components.remove(preference_path) == null;
	}

	/**
	 * saves all values of the registered components
	 */
	public void save()
	{
		Iterator it = components.keySet().iterator();

		while (it.hasNext())
		{
			String key = (String) it.next();
			Component comp = (Component) components.get(key);
			saveComponent(comp, key);
		}

	}

	/**
	 * Saves the value(s) of the given component to the given node path
	 * @param component component which value should be saved
	 * @param path node path in preferences
     * TODO: add further java awt/swing component types
	 */
	protected void saveComponent(Component component, String path)
	{
		if (component instanceof JTextComponent)
		{
			JTextComponent field = (JTextComponent) component;
			String value = field.getText();

			if (value != null)
				settings_manager.set(path, value);
		}
		else if(component instanceof JList)
		{
			JList list = (JList)component;
			Object values[] = list.getSelectedValues();
			
			settings_manager.removeNode(path);
			
			for(int i = 0; i < values.length; i++)
			{
				settings_manager.setString(path + String.valueOf(i), values[i].toString());				
			}
		}
		else if(component instanceof JComboBox)
		{
			JComboBox box = (JComboBox)component;
			Object value = box.getSelectedItem();
			
			if (value != null)
				settings_manager.set(path, value.toString());			
		}
		else if(component instanceof JCheckBox)
		{
			JCheckBox box = (JCheckBox)component;
			settings_manager.setBoolean(path, box.isSelected());
		}
        else if(component instanceof TextComponent)
        {
            TextComponent field = (TextComponent)component;
            String value = field.getText();
            
            if (value != null)
                settings_manager.set(path, value);                        
        }
	}
}
