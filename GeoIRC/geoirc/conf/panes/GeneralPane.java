/*
 * GeneralPane.java
 * 
 * Created on 19.08.2003
 */
package geoirc.conf.panes;

import java.awt.Component;
import java.util.Iterator;
import java.util.Map;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.SettingsSaveHandler;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.ValidationListener;
import geoirc.conf.beans.ValueRule;
import geoirc.util.JValidatingTextField;

import javax.swing.JLabel;

/**
 * @author netseeker aka Michael Manske
 */
public class GeneralPane extends BaseSettingsPanel implements Storable
{
    private SettingsSaveHandler save_handler;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public GeneralPane(
        XmlProcessable settings,
        GeoIRCDefaults valueRules,
        ValidationListener validationListener,
        String name)
    {
        super(settings, valueRules, validationListener, name);
        save_handler = new SettingsSaveHandler(settings);
    }

    public void initialize()
    {
        addComponent(new TitlePane("Personal Information"), 0, 0, 10, 1, 0, 0);
        String path = "/personal/";

        addComponent(new JLabel("Real Name"), 0, 1, 1, 1, 0, 0);
        ValueRule rule = rules.getValueRule("NAME");
        String value = settings_manager.getString(path + "name", (String)rule.getValue());
        String pattern = rule.getPattern();

        save_handler.register(
            addComponent(new JValidatingTextField(pattern, value, validation_listener, 180), 1, 1, 1, 1, 1, 0),
            path + "name");

        rule = rules.getValueRule("NICK");
        value = settings_manager.getString(path + "nick1", (String)rule.getValue());
        pattern = rule.getPattern();

        addComponent(new JLabel("Nick Name1"), 0, 2, 1, 1, 0, 0);
        save_handler.register(
            addComponent(new JValidatingTextField(pattern, value, validation_listener, 150), 1, 2, 1, 1, 1, 0),
            path + "nick1");

        value = settings_manager.getString(path + "nick2", "");
        addComponent(new JLabel("Nick Name2"), 0, 3, 1, 1, 0, 0);
        save_handler.register(
            addComponent(new JValidatingTextField(pattern, value, validation_listener, 150), 1, 3, 1, 1, 1, 0),
            path + "nick2");

        value = settings_manager.getString(path + "nick3", "");
        addComponent(new JLabel("Nick Name3"), 0, 4, 1, 1, 0, 0);
        save_handler.register(
            addComponent(new JValidatingTextField(pattern, value, validation_listener, 150), 1, 4, 1, 1, 1, 0),
            path + "nick3");

        path += "ident/";
        addComponent(new TitlePane("IDENT"), 0, 5, 10, 1, 0, 0);
        value = settings_manager.getString(path + "username", "");
        rule = rules.getValueRule("NAME");
        addComponent(new JLabel("Username"), 0, 6, 1, 1, 0, 0);
        save_handler.register(
            addComponent(
                new JValidatingTextField(rule.getPattern(), value, validation_listener, 150),
                1,
                6,
                1,
                1,
                1,
                0),
            path + "username");

        value = settings_manager.getString(path + "os", "");
        addComponent(new JLabel("OS"), 0, 7, 1, 1, 0, 0);
        save_handler.register(
            addComponent(new JValidatingTextField(".+", value, validation_listener, 180), 1, 7, 1, 1, 1, 0),
            path + "os");

        path = "/personal/ctcp/";
        addComponent(new TitlePane("CTCP"), 0, 8, 10, 1, 0, 0);
        rule = rules.getValueRule("CTCP_VERSION");
        value = settings_manager.getString(path + "version", rule.getValue().toString());
        addComponent(new JLabel("Version"), 0, 9, 1, 1, 0, 0);
        save_handler.register(
            addComponent(
                new JValidatingTextField(rule.getPattern(), value, validation_listener, 150),
                1,
                9,
                1,
                1,
                1,
                0),
            path + "version");

        rule = rules.getValueRule("USER_INFO");
        value = settings_manager.getString(path + "userinfo", rule.getValue().toString());
        addComponent(new JLabel("Userinfo"), 0, 10, 1, 1, 0, 0);
        save_handler.register(
            addComponent(new JValidatingTextField(".+", value, validation_listener, 180), 1, 10, 1, 1, 1, 1),
            path + "userinfo");
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
        Map components = save_handler.getRegisteredComponents();
        Iterator it = components.values().iterator();

        while (it.hasNext())
        {
            Component comp = (Component)it.next();

            if (comp instanceof JValidatingTextField)
            {
                if (((JValidatingTextField)comp).isValid() == false)
                    return true;
            }
        }

        return false;
    }
}
