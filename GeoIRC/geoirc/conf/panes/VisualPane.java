/*
 * VisualPane.java
 * 
 * Created on 20.08.2003
 */
package geoirc.conf.panes;

import geoirc.SettingsManager;
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

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * @author netseeker aka Michael Manske
 */
public class VisualPane extends BaseSettingsPanel implements Storable
{
    private SettingsSaveHandler save_handler;
    private JValidatingTextField kderc;
    private JValidatingTextField gtkrc;
    private JValidatingTextField fgColor;
    private JValidatingTextField bgColor;
    private JValidatingTextField alternate_bgcolor;
    private JValidatingTextField nick_width;
    private JValidatingTextField timestamp;
    private JComboBox fontFace;
    private JComboBox fontSize;
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
    
    private JComboBox sortBox;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public VisualPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
    {
        super(settings, valueRules, name);
        save_handler = new SettingsSaveHandler(settings);
    }

    public void initialize()
    {
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
        kderc = new JValidatingTextField(kdercRule.getPattern(), value, validation_listener, 250);
        save_handler.register(addComponent(kderc, 1, 1, 3, 1, 0, 0, new Insets(5, 5, 5, 2)), path + "skin1");
        chooseKderc = ComponentFactory.getFurtherInfoButton(new FileChooserHandler(kderc, this, value));
        addComponent(chooseKderc, 4, 1, 1, 1, 1, 0, new Insets(5, 0, 5, 5));

        addComponent(new JLabel("Path to .gtkrc:"), 0, 2, 1, 1, 0, 0);
        value = settings_manager.getString(path + "skin2", (String)gtkrcRule.getValue());
        gtkrc = new JValidatingTextField(gtkrcRule.getPattern(), value, validation_listener, 250);
        save_handler.register(addComponent(gtkrc, 1, 2, 3, 1, 0, 0, new Insets(5, 5, 5, 2)), path + "skin2");
        chooseGtkrc = ComponentFactory.getFurtherInfoButton(new FileChooserHandler(gtkrc, this, value));
        addComponent(chooseGtkrc, 4, 2, 1, 1, 1, 0, new Insets(5, 0, 5, 5));

        //TEXT WINDOWS
        addComponent(new TitlePane("Text Windows"), 0, 3, 5, 1, 0, 0);
        path = "/gui/text windows/";
        addComponent(new JLabel("Font"), 0, 4, 1, 1, 0, 0);
        //font face
        value = settings_manager.getString(path + "font face", fontFaceRule.getValue().toString());
        fontFace = ComponentFactory.getFontFaceComponent();
        fontFace.setSelectedItem(value);
        save_handler.register(addComponent(fontFace, 1, 4, 2, 1, 0, 0), path + "font face");
        //font size
        value = settings_manager.getString(path + "font size", fontSizeRule.getValue().toString());
        fontSize = ComponentFactory.getFontSizeComponent();
        fontSize.setSelectedItem(value);
        save_handler.register(addComponent(fontSize, 3, 4, 1, 1, 0, 0), path + "font size");
        //foreground colour
        addComponent(new JLabel("Foreground color"), 0, 5, 1, 1, 0, 0);
        value = settings_manager.getString(path + "default foreground colour", fgColorRule.getValue().toString());
        fgColor = new JValidatingTextField(fgColorRule.getPattern(), value, validation_listener);
        save_handler.register(
            addComponent(fgColor, 1, 5, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "default foreground colour");
        JButton chooseColor3 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(fgColor, this));
        addComponent(chooseColor3, 2, 5, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        //background color
        addComponent(new JLabel("Backgound color"), 0, 6, 1, 1, 0, 0);
        value = settings_manager.getString(path + "default background colour", bgColorRule.getValue().toString());
        bgColor = new JValidatingTextField(bgColorRule.getPattern(), value, validation_listener);
        save_handler.register(
            addComponent(bgColor, 1, 6, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "default background colour");
        JButton chooseColor4 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(bgColor, this));
        addComponent(chooseColor4, 2, 6, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        //alternate background color
        addComponent(new JLabel("Alternate backgound color"), 0, 7, 1, 1, 0, 0);
        value = settings_manager.getString(path + "alternate background colour", bgColorRule.getValue().toString());
        alternate_bgcolor = new JValidatingTextField(bgColorRule.getPattern(), value, validation_listener);
        save_handler.register(
            addComponent(alternate_bgcolor, 1, 7, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
            path + "alternate background colour");
        JButton chooseColor5 = ComponentFactory.getFurtherInfoButton(new ColorChooserHandler(alternate_bgcolor, this));
        addComponent(chooseColor5, 2, 7, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
        //FORMAT
        path = "/gui/format/";
        //timestamp
        value = settings_manager.getString(path + "timestamp", timestampRule.getValue().toString());
        addComponent(new JLabel("Timestamp"), 0, 8, 1, 1, 0, 0);
        timestamp = new JValidatingTextField(timestampRule.getPattern(), value, validation_listener);
        save_handler.register(addComponent(timestamp, 1, 8, 1, 1, 0, 0, new Insets(5, 5, 5, 2)), path + "timestamp");
        //nick width        
        value = settings_manager.getString(path + "maximum nick width", nickWidthRule.getValue().toString());
        addComponent(new JLabel("max. nick width"), 0, 9, 1, 1, 0, 0);
        nick_width = new JValidatingTextField(nickWidthRule.getPattern(), value, validation_listener, 40);
        save_handler.register(addComponent(nick_width, 1, 9, 1, 1, 0, 0, new Insets(5, 5, 5, 2)), path + "maximum nick width");
        //INFO WINDOWS
        addComponent(new TitlePane("Info windows, eg. nick names tree"), 0, 10, 5, 1, 0, 0);
        path = "/gui/info windows/";
        boolean bValue = settings_manager.getBoolean(path + "show root node", true);
        JCheckBox show_root_box = new JCheckBox("Show root node", bValue);
        addComponent(show_root_box, 0, 11, 2, 1, 0, 0);
        save_handler.register(show_root_box, path + "show root node");
        
        int sort_order = settings_manager.getInt(path + "sort order", DEFAULT_SORT_ORDER);
        switch( sort_order )
        {
            case -1:    value = "unsorted";
                        break;
            case 0:     value = "alphabetic ascending";
                        break;
            case 1:     value = "activity descending";                    
                        break;
            case 2:     value = "mode and alphabetic ascending";                    
                        break;
            case 3:     value = "mode and activity descending";                    
                        break;
        }
        
        addComponent(new JLabel("Nicknames sort order"), 0, 12, 1, 1, 0, 0);
        String[] sort_options = { "unsorted", "alphabetic ascending", "activity descending", "mode and alphabetic ascending", "mode and activity descending" };        
        sortBox = new JComboBox(sort_options);
        addComponent(sortBox, 1, 12, 3, 1, 0, 0);
        sortBox.setSelectedItem(value);
        addLayoutStopper(0, 16);
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData()
    {
        save_handler.save();
        
        int sort_order = sortBox.getSelectedIndex(); 
        if( sort_order != -1 )
        {            
            ((SettingsManager)settings_manager).putInt("/gui/info windows/sort order", sort_order - 1);
        }
        
        return true;
    }

}
