/*
 * VisualPane.java
 * 
 * Created on 20.08.2003
 */
package geoirc.conf.panes;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.ColorChooserHandler;
import geoirc.conf.ComponentFactory;
import geoirc.conf.FileChooserHandler;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.SettingsSaveHandler;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.beans.ValueRule;
import geoirc.util.JValidatingTextField;

import java.awt.Component;
import java.awt.Insets;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * @author netseeker aka Michael Manske
 */
public class VisualPane extends BaseSettingsPanel implements Storable {
    private SettingsSaveHandler save_handler;
    private JValidatingTextField kderc;
    private JValidatingTextField gtkrc;
    private JValidatingTextField fgColor1;
    private JValidatingTextField bgColor1;
    private JValidatingTextField fgColor2;
    private JValidatingTextField bgColor2;
    private JValidatingTextField alternate_bgcolor;    
    private JValidatingTextField nick_width;
    private JValidatingTextField timestamp;    
    private JComboBox fontFace1;
    private JComboBox fontSize1;
    private JComboBox fontFace2;
    private JComboBox fontSize2;
    private ValueRule kdercRule;
    private ValueRule gtkrcRule;
    private ValueRule fontFaceRule;
    private ValueRule fontSizeRule;
    private ValueRule fgColorRule;
    private ValueRule bgColorRule;
    private ValueRule nickWidthRule;
    private ValueRule timestampRule;

    private JButton chooseKderc;
    private JButton chooseGtkrc;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public VisualPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name) {
        super(settings, valueRules, name);
        save_handler = new SettingsSaveHandler(settings);
    }

    public void initialize() {        
        //PRELOAD VALUE RULES
        kdercRule = rules.getValueRule("KDERC");
        gtkrcRule = rules.getValueRule("GTKRC");
        fontFaceRule = rules.getValueRule("FONT");
        fontSizeRule = rules.getValueRule("FONT_SIZE");
        fgColorRule = rules.getValueRule("FGCOLOR");
        bgColorRule = rules.getValueRule("BGCOLOR");
        nickWidthRule = rules.getValueRule("NICK_WIDTH");
        timestampRule = rules.getValueRule("TIMESTAMP");

        //SKIN
        String path = "/gui/";
        addComponent(new TitlePane("Skin"), 0, 0, 5, 1, 0, 0);

        addComponent(new JLabel("Path to .kderc:"), 0, 1, 1, 1, 0, 0);
        String value = settings_manager.getString(path + "skin1", (String)kdercRule.getValue());
        kderc = new JValidatingTextField(kdercRule.getPattern(), value, 250);
        save_handler.register(addComponent(kderc, 1, 1, 3, 1, 0, 0, new Insets(5, 5, 5, 2)), path + "skin1");
        chooseKderc = ComponentFactory.getFurtherInfoButton(new FileChooserHandler(kderc, this));
        addComponent(chooseKderc, 4, 1, 1, 1, 1, 0, new Insets(5, 0, 5, 5));

        addComponent(new JLabel("Path to .gtkrc:"), 0, 2, 1, 1, 0, 0);
        value = settings_manager.getString(path + "skin2", (String)gtkrcRule.getValue());
        gtkrc = new JValidatingTextField(gtkrcRule.getPattern(), value, 250);
        save_handler.register(addComponent(gtkrc, 1, 2, 3, 1, 0, 0, new Insets(5, 5, 5, 2)), path + "skin2");
        chooseGtkrc = ComponentFactory.getFurtherInfoButton(new FileChooserHandler(gtkrc, this));
        addComponent(chooseGtkrc, 4, 2, 1, 1, 1, 0, new Insets(5, 0, 5, 5));

        //INPUT BOX
        addComponent(new TitlePane("Input Box"), 0, 3, 5, 1, 0, 0);
        path = "/gui/input field/";
        addComponent(new JLabel("Font"), 0, 4, 1, 1, 0, 0);
        //font face
        value = settings_manager.getString(path + "font face", fontFaceRule.getValue().toString());
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
        fgColor1 = new JValidatingTextField(fgColorRule.getPattern(), value);
        save_handler.register(
            addComponent(fgColor1, 1, 5, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "foreground colour");
        JButton chooseColor1 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(fgColor1, this));
        addComponent(chooseColor1, 2, 5, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        //background color
        addComponent(new JLabel("Backgound color"), 0, 6, 1, 1, 0, 0);
        value = settings_manager.getString(path + "background colour", bgColorRule.getValue().toString());
        bgColor1 = new JValidatingTextField(bgColorRule.getPattern(), value);
        save_handler.register(
            addComponent(bgColor1, 1, 6, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "background colour");
        JButton chooseColor2 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(bgColor1, this));
        addComponent(chooseColor2, 2, 6, 1, 1, 0, 0, new Insets(5, 0, 5, 5));

        //TEXT WINDOWS
        addComponent(new TitlePane("Text Windows"), 0, 7, 5, 1, 0, 0);
        path = "/gui/text windows/";
        addComponent(new JLabel("Font"), 0, 8, 1, 1, 0, 0);
        //font face
        value = settings_manager.getString(path + "font face", fontFaceRule.getValue().toString());
        fontFace2 = ComponentFactory.getFontFaceComponent();
        fontFace2.setSelectedItem(value);
        save_handler.register(addComponent(fontFace2, 1, 8, 2, 1, 0, 0), path + "font face");
        //font size
        value = settings_manager.getString(path + "font size", fontSizeRule.getValue().toString());
        fontSize2 = ComponentFactory.getFontSizeComponent();
        fontSize2.setSelectedItem(value);
        save_handler.register(addComponent(fontSize2, 3, 8, 1, 1, 0, 0), path + "font size");
        //foreground colour
        addComponent(new JLabel("Foreground color"), 0, 9, 1, 1, 0, 0);
        value = settings_manager.getString(path + "default foreground colour", fgColorRule.getValue().toString());
        fgColor2 = new JValidatingTextField(fgColorRule.getPattern(), value);
        save_handler.register(
            addComponent(fgColor2, 1, 9, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "default foreground colour");
        JButton chooseColor3 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(fgColor2, this));
        addComponent(chooseColor3, 2, 9, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        //background color
        addComponent(new JLabel("Backgound color"), 0, 10, 1, 1, 0, 0);
        value = settings_manager.getString(path + "default background colour", bgColorRule.getValue().toString());
        bgColor2 = new JValidatingTextField(bgColorRule.getPattern(), value);
        save_handler.register(
            addComponent(bgColor2, 1, 10, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "default background colour");
        JButton chooseColor4 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(bgColor2, this));
        addComponent(chooseColor4, 2, 10, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        //alternate background color
        addComponent(new JLabel("Alternate backgound color"), 0, 11, 1, 1, 0, 0);
        value = settings_manager.getString(path + "alternate background colour", bgColorRule.getValue().toString());
        alternate_bgcolor = new JValidatingTextField(bgColorRule.getPattern(), value);
        save_handler.register(
            addComponent(alternate_bgcolor, 1, 11, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "alternate background colour");
        JButton chooseColor5 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(alternate_bgcolor, this));
        addComponent(chooseColor5, 2, 11, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        
        //FORMAT
        addComponent(new TitlePane("Format"), 0, 12, 5, 1, 0, 0);
        path = "/gui/format/";
        //nick width
        value = settings_manager.getString(path + "nick width", nickWidthRule.getValue().toString());
        addComponent(new JLabel("Nick width"), 0, 13, 1, 1, 0, 0);        
        nick_width = new JValidatingTextField(nickWidthRule.getPattern(), value, 40);
        save_handler.register(
            addComponent(nick_width, 1, 13, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "nick width");        
        //timestamp
        value = settings_manager.getString(path + "timestamp", timestampRule.getValue().toString());
        addComponent(new JLabel("Timestamp"), 0, 14, 1, 1, 0, 0);        
        timestamp = new JValidatingTextField(timestampRule.getPattern(), value);
        save_handler.register(
            addComponent(timestamp, 1, 14, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "timestamp");        
                
        addLayoutStopper(0, 15);
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData() {
        save_handler.save();
        return true;
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#hasErrors()
     */
    public boolean hasErrors() {
        Map components = save_handler.getRegisteredComponents();
        Iterator it = components.values().iterator();

        while (it.hasNext()) {
            Component comp = (Component)it.next();

            if (comp instanceof JValidatingTextField) {
                if (((JValidatingTextField)comp).isValid() == false)
                    return true;
            }
        }

        return false;
    }

}
