/*
 * SettingsPanelFactory.java
 * 
 * Created on 16.08.2003
 */
package geoirc.conf;

import geoirc.DisplayManager;
import geoirc.XmlProcessable;
import geoirc.conf.panes.CommandAliasesPane;
import geoirc.conf.panes.ConnectionPane;
import geoirc.conf.panes.GeneralPane;
import geoirc.conf.panes.HighlightingPane;
import geoirc.conf.panes.HotkeyPane;
import geoirc.conf.panes.RootPane;
import geoirc.conf.panes.VisualPane;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the place where all setting panels are born. :-)
 * 
 * @author netseeker aka Michael Manske
 */
public class SettingsPanelFactory {

    /**
     * Creates all setting panels - if they weren't already created.
     * @param settings_manager a instance of XmlProcessable which
     * allows access to the application preferences
     * @return a list of all available, instanciated settings panels
     * @see geoic.conf.BaseSettingsPanel
     */
    public static List create(
        XmlProcessable settings_manager,
        DisplayManager display_manager,
        GeoIRCDefaults valueRules) {
        List panels = new ArrayList();
        //General Settings
        BaseSettingsPanel genPane = new GeneralPane(settings_manager, valueRules, "General Settings");
        genPane.addChild(new CommandAliasesPane(settings_manager, valueRules, "Command Aliases"));
        genPane.addChild(
            new HotkeyPane(
                settings_manager,
                valueRules,
                "Keyboard",
                display_manager.getGeoIRCInstance().getActionMap()));
        panels.add(genPane);
        //Connection Settings
        BaseSettingsPanel conPane = new ConnectionPane(settings_manager, valueRules, "Connection Settings");
        //conPane.addChild(new ChannelPane(settings_manager, valueRules, "IRC Server/Channels"));
        panels.add(conPane);
        //Visual Settings
        BaseSettingsPanel visPane = new VisualPane(settings_manager, valueRules, "Visual Settings");
        visPane.addChild(new HighlightingPane(settings_manager, valueRules, "Highlighting"));
        panels.add(visPane);

        return panels;
    }

    /**
     * @return
     */
    public static BaseSettingsPanel createRootPane() {
        return new RootPane(null, null, "GeoIRC");
    }
}
