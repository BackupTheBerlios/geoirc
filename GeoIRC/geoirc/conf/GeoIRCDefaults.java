/*
 * GeoIRCDefaults.java
 * 
 * Created on 07.08.2003
 */
package geoirc.conf;

import geoirc.BaseXmlHandler;
import geoirc.XmlProcessable;
import geoirc.conf.beans.*;
import geoirc.gui.DisplayManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

/**
 * Reading and evaluation of rules for handling of default values  
 * 
 * @author netseeker aka Michael Manske
 */
public class GeoIRCDefaults
{
	private HashMap defaults = new HashMap();
	private DisplayManager displayManager;
	private XmlProcessable xmlProcessor;

	/**
	 * Creates a new instance of GeoIRCDefaults
	 * @param newDisplayMgr DisplayManager to use for debug logging
	 * @param filePath path to xml file to use 
	 */
	public GeoIRCDefaults(DisplayManager newDisplayMgr)
	{
		this(newDisplayMgr, "conf/defaults.xml", true);		
	}

	/**
	 * Creates a new instance of GeoIRCDefaults
	 * @param newDisplayMgr DisplayManager to use for debug logging
	 * @param filePath path to xml file to use 
	 */
	public GeoIRCDefaults(DisplayManager newDisplayMgr, String filePath, boolean isResource)
	{
		this.displayManager = newDisplayMgr;
		this.xmlProcessor = new BaseXmlHandler(newDisplayMgr, filePath, isResource);
		this.xmlProcessor.loadSettingsFromXML();
		extractDefaults();
	}

	/**
	 * Extracts all default value definitions from defaults.xml
	 * @return true if all types could be succcessful extracted, otherwise false
	 */
	private void extractDefaults()
	{
		//load default value rules
		Element root = this.xmlProcessor.getBuffer();
		List childs = root.getChild("defaults").getChildren();
		Iterator it = childs.iterator();

		while (it.hasNext())
		{
			Element rule = (Element) it.next();
			String defName = rule.getAttributeValue("name");
			ValueRule def =
				new ValueRule(
					defName,
					rule.getChildText("pattern"),
					rule.getChildText("javaType"),
					rule.getChildText("default"));

			if (defName != null)
				defaults.put(defName, def);
		}
	}

	/**
	 * @param name
	 * @return a valide ValueRule if possible, otherwise null
	 */
	public ValueRule getValueRule(String name)
	{
		return (ValueRule) defaults.get(name);
	}

	/**
	 * @return Map of available value rules
	 */
	public Map getValueRules()
	{
		return defaults;
	}
}
