/*
 * RootPane.java
 * 
 * Created on 19.08.2003
 */
package geoirc.conf.panes;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.TitlePane;

import java.awt.Insets;

import javax.swing.JLabel;

/**
 * @author netseeker aka Michael Manske
 */
public class RootPane extends BaseSettingsPanel
{

	/**
	 * @param settings
	 * @param name
	 */
	public RootPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
	{
		super(settings, valueRules, null, null, name);
	}

    public void initialize()
	{
		addComponent(new TitlePane("About" + BASE_GEOIRC_TITLE), 0, 0, 10, 1, 0, 0);
		addComponent(new JLabel(BASE_GEOIRC_TITLE + " " + GEOIRC_VERSION), 0, 1, 1, 1, 1, 0);
		addComponent(new JLabel(GEOIRC_HOMEPAGE), 0, 2, 1, 1, 1, 0);
		addComponent(new JLabel(GEOIRC_DEVPAGE), 0, 3, 1, 1, 1, 0);

		addComponent(new JLabel("Copyright @ 2003"), 0, 4, 1, 1, 1, 0, new Insets(20, 5, 5, 5));
		addComponent(new JLabel("Pistos"), 0, 5, 1, 1, 1, 0, new Insets(2, 5, 0, 5));
		addComponent(new JLabel("livesNbox"), 0, 6, 1, 1, 1, 0, new Insets(2, 5, 0, 5));
		addComponent(new JLabel("netseeker"), 0, 7, 1, 1, 1, 0, new Insets(2, 5, 20, 5));
		
		addLayoutStopper(0,8);
	}    
}
