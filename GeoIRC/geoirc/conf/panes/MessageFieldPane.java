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

        addHorizontalLayoutStopper(4, 6);
        addLayoutStopper(0, 7);
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
