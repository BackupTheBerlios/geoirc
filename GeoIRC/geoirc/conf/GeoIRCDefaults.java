/*
 * GeoIRCDefaults.java
 * 
 * Created on 07.08.2003
 */
package geoirc.conf;

import geoirc.DisplayManager;
import geoirc.SettingsManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * Reading and evaluation of rules for handling of default values  
 * 
 * @author netseeker aka Michael Manske
 */
public class GeoIRCDefaults extends SettingsManager
{
	private HashMap defaults = new HashMap();
	
	/**
	 * Creates a new instance of GeoIRCDefaults
	 * @param newDisplayMgr DisplayManager to use for debug logging
	 */
	public GeoIRCDefaults(DisplayManager newDisplayMgr)
	{
		super(newDisplayMgr, null);
		root = Preferences.userNodeForPackage(GeoIRCDefaults.class);
		loadSettingsFromXML();
	}

	/* (non-Javadoc)
	 * @see geoirc.SettingsManager#loadSettingsFromXML()
	 */
	public boolean loadSettingsFromXML()
	{
		boolean success = false;

		try
		{
			InputStream is =
				new BufferedInputStream(
					getClass().getResourceAsStream("defaults.xml"));
			root.importPreferences(is);
			extractDefaults();
			success = true;
		}
		catch (InvalidPreferencesFormatException e)
		{
			printlnDebug("Invalid format in defaults.xml. Import of settings failed.");
			e.printStackTrace();
		}
		catch (IOException e1)
		{
			printlnDebug("I/O problem while trying to load settings from defaults.xml.");
		}
		catch (BackingStoreException e)
		{
			printlnDebug("Invalid format in defaults.xml. Import of settings failed.");
		}
			
		return success;
	}

	
	/**
	 * Extracts all default value definitions from defaults.xml
	 * @return true if all types could be succcessful extracted, otherwise false
	 */
	private void extractDefaults() throws BackingStoreException
	{
			//load default value rules
			Preferences prefs = root.node("defaults");
			String[] childs = prefs.childrenNames();
			for ( int i = 0; i < childs.length; i++ )
			{
				ValueRule def = new ValueRule(prefs.node(childs[i]));				
				String defName = def.getName();
				
				if(defName != null)
					defaults.put(defName, def);				
			}
			
	}


	/** 
	 * We override saveSettingsToXML to prevent overwriting of<br>
	 * default rules which should always be read only.
	 * @see geoirc.SettingsManager#saveSettingsToXML()
	 */
	public boolean saveSettingsToXML()
	{
		return false;
	}

	/**
	 * @param name
	 * @return a valide ValueRule if possible, otherwise null
	 */
	public ValueRule getValueRule(String name)
	{
		return (ValueRule)defaults.get(name);
	}	

	/**
	 * @return Map of available value rules
	 */
	public Map getValueRules()
	{
		return defaults;
	}	

}
