/*
 * SettingsPanelFactory.java
 * 
 * Created on 16.08.2003
 */
package geoirc.conf;

import geoirc.DisplayManager;
import geoirc.XmlProcessable;
import geoirc.conf.panes.ChannelPane;
import geoirc.conf.panes.ConnectionPane;
import geoirc.conf.panes.GeneralPane;
import geoirc.conf.panes.RootPane;
import geoirc.conf.panes.VisualPane;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the place where all setting panels are born. :-)
 * 
 * @author netseeker aka Michael Manske
 */
public class SettingsPanelFactory
{
	private static List panels = new ArrayList();
	private static BaseSettingsPanel rootPane = null;

	/**
	 * Creates all setting panels - if they weren't already created.
	 * @param settings_manager a instance of XmlProcessable which
	 * allows access to the apllication preferences
	 * @return a list of all available, instanciated settings panels
	 * @see geoic.conf.BaseSettingsPanel
	 */
	public static List create(
		XmlProcessable settings_manager,
		DisplayManager display_manager,
		GeoIRCDefaults valueRules)
	{
		if (panels.size() == 0)
		{
			//Generell Settings
			panels.add(new GeneralPane(settings_manager, valueRules, "Generell Settings"));
			//Connection Settings
			BaseSettingsPanel conPane = new ConnectionPane(settings_manager, valueRules, "Connection Settings");
			conPane.addChild(new ChannelPane(settings_manager, valueRules, "IRC Server/Channels"));
			panels.add(conPane);
			//Visual Settings
			BaseSettingsPanel visPane = new VisualPane(settings_manager, valueRules, "Visual Settings");
			
			panels.add(visPane);
		}

		return panels;
	}

	/**
	 * @param settings_manager
	 * @return
	 */
	public static BaseSettingsPanel createRootPane()
	{
		if (rootPane == null)
		{
			rootPane = new RootPane(null, null, "GeoIRC");
		}
		return rootPane;
	}
}
