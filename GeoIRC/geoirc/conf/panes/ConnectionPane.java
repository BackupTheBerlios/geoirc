/*
 * ConnectionPane.java
 * 
 * Created on 19.08.2003
 */
package geoirc.conf.panes;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.TitlePane;

/**
 * @author netseeker aka Michael Manske
 */
public class ConnectionPane extends BaseSettingsPanel
{

	/**
	 * @param settings
	 * @param valueRules
	 * @param name
	 */
	public ConnectionPane(
		XmlProcessable settings,
		GeoIRCDefaults valueRules,
		String name)
	{
		super(settings, valueRules, name);
		initComponents();
	}
	
	private void initComponents()
	{
		addComponent(new TitlePane("Connection Settings"), 0, 0, 10, 1, 1, 1);
	}

}
