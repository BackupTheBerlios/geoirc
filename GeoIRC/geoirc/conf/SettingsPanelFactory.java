/*
 * SettingsPanelFactory.java
 * 
 * Created on 16.08.2003
 */
package geoirc.conf;

import geoirc.DisplayManager;
import geoirc.XmlProcessable;
import geoirc.conf.panes.CommandAliasesPane;
import geoirc.conf.panes.DCCPane;
import geoirc.conf.panes.GeneralPane;
import geoirc.conf.panes.HighlightingPane;
import geoirc.conf.panes.HotkeyPane;
import geoirc.conf.panes.RootPane;
import geoirc.conf.panes.TriggerPane;
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
        GeoIRCDefaults valueRules,
        ValidationListener validation_listener)
    {
        List panels = new ArrayList();

        //General Settings
        BaseSettingsPanel genPane = new GeneralPane(settings_manager, valueRules, "General Settings");
        genPane.addValidationListener(validation_listener);
        BaseSettingsPanel caPane = new CommandAliasesPane(settings_manager, valueRules, "Command Aliases");
        caPane.addValidationListener(validation_listener);
        genPane.addChild(caPane);

        BaseSettingsPanel hotkeyPane = new HotkeyPane(settings_manager, valueRules, "Keyboard");
        hotkeyPane.addValidationListener(validation_listener);
        genPane.addChild(hotkeyPane);

        BaseSettingsPanel triggerPane = new TriggerPane(settings_manager, valueRules, "Trigger Settings");
        triggerPane.addValidationListener(validation_listener);
        genPane.addChild(triggerPane);

        panels.add(genPane);

        //Connection Settings
        BaseSettingsPanel conPane = new DCCPane(settings_manager, valueRules, "DCC Settings");
        conPane.addValidationListener(validation_listener);
        //conPane.addChild(new ChannelPane(settings_manager, valueRules, "IRC Server/Channels"));
        panels.add(conPane);

        //Visual Settings
        BaseSettingsPanel visPane = new VisualPane(settings_manager, valueRules, "Visual Settings");
        visPane.addValidationListener(validation_listener);

        BaseSettingsPanel hlPane = new HighlightingPane(settings_manager, valueRules, "Highlighting");
        hlPane.addValidationListener(validation_listener);
        visPane.addChild(hlPane);
        panels.add(visPane);

        return panels;
    }

    /**
     * @return
     */
    public static BaseSettingsPanel createRootPane()
    {
        return new RootPane(null, null, "GeoIRC");
    }
}
