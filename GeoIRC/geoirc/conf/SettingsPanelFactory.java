/*
 * SettingsPanelFactory.java
 * 
 * Created on 16.08.2003
 */
package geoirc.conf;

import geoirc.XmlProcessable;
import geoirc.conf.panes.CommandAliasesPane;
import geoirc.conf.panes.DCCPane;
import geoirc.conf.panes.GeneralPane;
import geoirc.conf.panes.HighlightingPane;
import geoirc.conf.panes.HotkeyPane;
import geoirc.conf.panes.LogFilesPane;
import geoirc.conf.panes.LogPane;
import geoirc.conf.panes.MessageFieldPane;
import geoirc.conf.panes.RootPane;
import geoirc.conf.panes.TriggerPane;
import geoirc.conf.panes.VariablesPane;
import geoirc.conf.panes.VisualPane;
import geoirc.gui.DisplayManager;

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

        BaseSettingsPanel variablesPane = new VariablesPane(settings_manager, valueRules, "Variables");
        variablesPane.addValidationListener(validation_listener);
        genPane.addChild(variablesPane);

        panels.add(genPane);

        //Visual Settings
        BaseSettingsPanel visPane = new VisualPane(settings_manager, valueRules, "Visual Settings");
        visPane.addValidationListener(validation_listener);
        BaseSettingsPanel msg_field_pane = new MessageFieldPane(settings_manager, valueRules, "Message Field");
        msg_field_pane.addValidationListener(validation_listener);
        BaseSettingsPanel hlPane = new HighlightingPane(settings_manager, valueRules, "Highlighting");
        hlPane.addValidationListener(validation_listener);
        visPane.addChild(msg_field_pane);
        visPane.addChild(hlPane);
        panels.add(visPane);
        
        //Connection Settings
        BaseSettingsPanel conPane = new DCCPane(settings_manager, valueRules, "DCC Settings");
        conPane.addValidationListener(validation_listener);
        //conPane.addChild(new ChannelPane(settings_manager, valueRules, "IRC Server/Channels"));
        panels.add(conPane);

        //Log Settings        
        BaseSettingsPanel logPane = new LogPane(settings_manager, valueRules, "Log Settings");
        logPane.addValidationListener(validation_listener);
        BaseSettingsPanel logFilePane = new LogFilesPane(settings_manager, valueRules, "Logfiles");
        logFilePane.addValidationListener(validation_listener);
        logPane.addChild(logFilePane);
        
        //conPane.addChild(new ChannelPane(settings_manager, valueRules, "IRC Server/Channels"));
        panels.add(logPane);

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
