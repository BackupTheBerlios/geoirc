/*
 * MessageFieldPane.java
 * 
 * Created on 20.08.2003
 */
package geoirc.conf.panes;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.ColorChooserHandler;
import geoirc.conf.ComponentFactory;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.SettingsSaveHandler;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.beans.ValueRule;
import geoirc.util.JValidatingTextField;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * @author netseeker aka Michael Manske
 */
public class MessageFieldPane extends BaseSettingsPanel implements Storable
{
    private SettingsSaveHandler save_handler;
    private JValidatingTextField fgColor1;
    private JValidatingTextField bgColor1;
    private JComboBox fontFace1;
    private JComboBox fontSize1;
    private ValueRule fontFaceRule;
    private ValueRule fontSizeRule;
    private ValueRule fgColorRule;
    private ValueRule bgColorRule;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public MessageFieldPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
    {
        super(settings, valueRules, name);
        save_handler = new SettingsSaveHandler(settings);
    }

    public void initialize()
    {
        //PRELOAD VALUE RULES
        fontFaceRule = rules.getValueRule("FONT");
        fontSizeRule = rules.getValueRule("FONT_SIZE");
        fgColorRule = rules.getValueRule("FGCOLOR");
        bgColorRule = rules.getValueRule("BGCOLOR");

        //INPUT BOX
        addComponent(new TitlePane("Input Box"), 0, 3, 5, 1, 0, 0);
        String path = "/gui/input field/";
        addComponent(new JLabel("Font"), 0, 4, 1, 1, 0, 0);
        //font face
        String value = settings_manager.getString(path + "font face", fontFaceRule.getValue().toString());
        fontFace1 = ComponentFactory.getFontFaceComponent();
        fontFace1.setSelectedItem(value);
        save_handler.register(addComponent(fontFace1, 1, 4, 2, 1, 0, 0), path + "font face");
        //font size
        value = settings_manager.getString(path + "font size", fontSizeRule.getValue().toString());
        fontSize1 = ComponentFactory.getFontSizeComponent();
        fontSize1.setSelectedItem(value);
        save_handler.register(addComponent(fontSize1, 3, 4, 1, 1, 0, 0), path + "font size");
        //foreground colour
        addComponent(new JLabel("Foreground color"), 0, 5, 1, 1, 0, 0);
        value = settings_manager.getString(path + "foreground colour", fgColorRule.getValue().toString());
        fgColor1 = new JValidatingTextField(fgColorRule.getPattern(), value, validation_listener);
        save_handler.register(
            addComponent(fgColor1, 1, 5, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "foreground colour");
        JButton chooseColor1 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(fgColor1, this));
        addComponent(chooseColor1, 2, 5, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        //background color
        addComponent(new JLabel("Backgound color"), 0, 6, 1, 1, 0, 0);
        value = settings_manager.getString(path + "background colour", bgColorRule.getValue().toString());
        bgColor1 = new JValidatingTextField(bgColorRule.getPattern(), value, validation_listener);
        save_handler.register(
            addComponent(bgColor1, 1, 6, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "background colour");
        JButton chooseColor2 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(bgColor1, this));
        addComponent(chooseColor2, 2, 6, 1, 1, 0, 0, new Insets(5, 0, 5, 5));

        //NICK COMPLETION
        path = "/misc/";
        addComponent(new TitlePane("Nickname completion"), 0, 7, 5, 1, 0, 0);

        addComponent(new JLabel("nick completion suffix"), 0, 8, 1, 1, 0, 0);
        ValueRule nick_rule = rules.getValueRule("NICK_COMPLETION_SUFFIX");
        value = settings_manager.getString(path + "nick completion suffix", nick_rule.getValue().toString());
        JValidatingTextField suffix_field = new JValidatingTextField(nick_rule.getPattern(), value, validation_listener);
        suffix_field.setPreferredSize(new Dimension(30, JValidatingTextField.PREFERED_HEIGHT));
        save_handler.register(addComponent(suffix_field, 1, 8, 1, 1, 0, 0), path + "nick completion suffix");
        
        addComponent(new JLabel("nick completion prefix"), 0, 9, 1, 1, 0, 0);
        nick_rule = rules.getValueRule("NICK_COMPLETION_PREFIX");
        value = settings_manager.getString(path + "nick completion prefix", nick_rule.getValue().toString());
        JValidatingTextField prefix_field = new JValidatingTextField(nick_rule.getPattern(), value, validation_listener);
        prefix_field.setPreferredSize(new Dimension(30, JValidatingTextField.PREFERED_HEIGHT));
        save_handler.register(addComponent(prefix_field, 1, 9, 1, 1, 0, 0), path + "nick completion prefix");

        //FLOOD SETTINGS
        path += "paste flood/";
        addComponent(new TitlePane("Flood protection"), 0, 10, 5, 1, 0, 0);
        
        addComponent(new JLabel("max. lines per paste/insert"), 0, 11, 1, 1, 0, 0);
        ValueRule flood_rule = rules.getValueRule("FLOOD_PROTECTION_ALLOWANCE");
        value = settings_manager.getString(path + "allowance", flood_rule.getValue().toString());
        JValidatingTextField flood_allowance_field = new JValidatingTextField(flood_rule.getPattern(), value, validation_listener);
        flood_allowance_field.setPreferredSize(new Dimension(40, JValidatingTextField.PREFERED_HEIGHT));
        save_handler.register(addComponent(flood_allowance_field, 1, 11, 1, 1, 0, 0), path + "allowance");
        
        addComponent(new JLabel("delay when sending multiple lines"), 0, 12, 1, 1, 0, 0);
        flood_rule = rules.getValueRule("FLOOD_PROTECTION_DELAY");
        value = settings_manager.getString(path + "delay", flood_rule.getValue().toString());
        JValidatingTextField flood_delay_field = new JValidatingTextField(flood_rule.getPattern(), value, validation_listener);
        flood_delay_field.setPreferredSize(new Dimension(60, JValidatingTextField.PREFERED_HEIGHT));
        save_handler.register(addComponent(flood_delay_field, 1, 12, 1, 1, 0, 0), path + "delay");

        addHorizontalLayoutStopper(4, 12);
        addLayoutStopper(0, 13);
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
