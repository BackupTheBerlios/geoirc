/*
 * DCCPane.java
 * 
 * Created on 19.08.2003
 */
package geoirc.conf.panes;

import java.awt.Insets;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.ComponentFactory;
import geoirc.conf.FileChooserHandler;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.SettingsSaveHandler;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.beans.ValueRule;
import geoirc.util.JValidatingTextField;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 * @author netseeker aka Michael Manske
 */
public class DCCPane extends BaseSettingsPanel implements Storable
{
    private SettingsSaveHandler save_handler;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public DCCPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
    {
        super(settings, valueRules, name);
        save_handler = new SettingsSaveHandler(settings);
    }

    public void initialize()
    {
        String path = "/dcc/file transfers/";
        addComponent(new TitlePane("DCC File Transfers"), 0, 0, 10, 1, 0, 0);
        boolean bValue = settings_manager.getBoolean(path + "confirm receive", true);
        save_handler.register(addComponent(new JCheckBox("Confirm receive", bValue), 0, 1, 1, 1, 0, 0), path + "name");

        addComponent(new JLabel("Default download directory"), 0, 2, 1, 1, 0, 0);
        ValueRule rule = rules.getValueRule("DIRECTORY");
        String value = settings_manager.getString(path + "default download directory", (String)rule.getValue());
        JValidatingTextField dirField = new JValidatingTextField(rule.getPattern(), value, 200);
        save_handler.register(
            addComponent(dirField, 1, 2, 2, 1, 0, 0, new Insets(2, 5, 5, 2)),
            path + "Download directory");

        addComponent(
            ComponentFactory.getFurtherInfoButton(new FileChooserHandler(dirField, this)),
            2,
            2,
            1,
            1,
            1,
            0,
            new Insets(2, 0, 5, 5));

        path = "/dcc/";
        addComponent(new JLabel("Lowest allowed port"), 0, 3, 1, 1, 0, 0);
        rule = rules.getValueRule("DCC_LOWEST_PORT");
        value = settings_manager.getString(path + "lowest port", rule.getValue().toString());
        JValidatingTextField low_port_field = new JValidatingTextField(rule.getPattern(), value);
        save_handler.register(
            addComponent(low_port_field, 1, 3, 1, 1, 0, 0),
            path + "lowest port");

        addComponent(new JLabel("Highest allowed port"), 0, 4, 1, 1, 0, 0);
        rule = rules.getValueRule("DCC_HIGHEST_PORT");
        value = settings_manager.getString(path + "highest port", rule.getValue().toString());
        JValidatingTextField high_port_field = new JValidatingTextField(rule.getPattern(), value);
        save_handler.register(
            addComponent(high_port_field, 1, 4, 1, 1, 0, 0),
            path + "lowhighestest port");

        addLayoutStopper(0, 5);
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData()
    {
        save_handler.save();
        return true;
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#hasErrors()
     */
    public boolean hasErrors()
    {
        return false;
    }

}
