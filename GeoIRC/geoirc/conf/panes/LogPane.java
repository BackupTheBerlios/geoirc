/*
 * LogPane.java
 * 
 * Created on 24.09.2003
 */
package geoirc.conf.panes;

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
import geoirc.util.JValidatingTextPane;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * @author netseeker aka Michael Manske
 */
public class LogPane extends BaseSettingsPanel implements Storable
{
    private SettingsSaveHandler save_handler;

    
    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public LogPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
    {
        super(settings, valueRules, name);
        save_handler = new SettingsSaveHandler(settings);
    }

    /* (non-Javadoc)
     * @see geoirc.conf.BaseSettingsPanel#initialize()
     */
    public void initialize()
    {
        String path = "/logs/";
        addComponent(new TitlePane("Log Settings"), 0, 0, 5, 1, 0, 0);
        
        addComponent(new JLabel("Default log path"), 0, 1, 1, 1, 0, 0);
        ValueRule path_rule = rules.getValueRule("DIRECTORY");
        String value = settings_manager.getString(path + "default log path", path_rule.getValue().toString());
        JValidatingTextField log_path_field = new JValidatingTextField(path_rule.getPattern(), value, validation_listener, 270);
        save_handler.register( addComponent(log_path_field, 1, 1, 2, 1, 0, 0, new Insets(5, 5, 5, 2)), path + "default log path" );       
        JButton choose_log_path = ComponentFactory.getFurtherInfoButton(new FileChooserHandler(log_path_field, this, value, JFileChooser.DIRECTORIES_ONLY));
        addComponent(choose_log_path, 3, 1, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        
        addComponent(new JLabel("Log start message"), 0, 2, 1, 1, 0, 0);
        ValueRule message_rule = rules.getValueRule("LOG_START_MESSAGE");
        value = settings_manager.getString(path + "log start message", message_rule.getValue().toString());
        JValidatingTextPane start_msg_field = new JValidatingTextPane(message_rule.getPattern(), value, validation_listener);
        start_msg_field.setFont(log_path_field.getFont());
        JScrollPane msg_scroller = new JScrollPane(start_msg_field);       
        addComponent(msg_scroller, 1, 2, 3, 3, 0, 0);
        save_handler.register(start_msg_field, path + "log start message" );
                                                       
        
        addHorizontalLayoutStopper(4, 2);
        addLayoutStopper(0, 3);                
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData()
    {
        save_handler.save();
        return true;
    }      
}
