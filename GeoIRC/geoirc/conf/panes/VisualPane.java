/*
 * VisualPane.java
 * 
 * Created on 20.08.2003
 */
package geoirc.conf.panes;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.SettingsSaveHandler;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.ValueRule;
import geoirc.util.JValidatingTextField;
import geoirc.util.Util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * @author netseeker aka Michael Manske
 */
public class VisualPane extends BaseSettingsPanel implements Storable
{
	private SettingsSaveHandler save_handler;
	private JValidatingTextField kderc;
	private JValidatingTextField gtkrc;
	private JValidatingTextField fgColor1;
	private JValidatingTextField bgColor1;
	private JValidatingTextField fgColor2;
	private JValidatingTextField bgColor2;
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

	private JButton chooseKderc;
	private JButton chooseGtkrc;

	/**
	 * @param settings
	 * @param valueRules
	 * @param name
	 */
	public VisualPane(
		XmlProcessable settings,
		GeoIRCDefaults valueRules,
		String name)
	{
		super(settings, valueRules, name);
		save_handler = new SettingsSaveHandler(settings);
		kdercRule = rules.getValueRule("KDERC");
		gtkrcRule = rules.getValueRule("GTKRC");
		fontFaceRule = rules.getValueRule("FONT");
		fontSizeRule = rules.getValueRule("FONT_SIZE");
		fgColorRule = rules.getValueRule("FGCOLOR");
		bgColorRule = rules.getValueRule("BGCOLOR");
		initComponents();
	}

	private void initComponents()
	{
		//SKIN
		String path = "/gui/";
		addComponent(new TitlePane("Skin"), 0, 0, 10, 1, 0, 0);

		addComponent(new JLabel("Path to .kderc:"), 0, 1, 1, 1, 0, 0);
		String value =
			settings_manager.getString(
				path + "skin1",
				(String) kdercRule.getValue());
		kderc = new JValidatingTextField(kdercRule.getPattern(), value, 200);
		save_handler.register(
			addComponent(kderc, 1, 1, 3, 1, 0, 0, new Insets(5, 5, 5, 2)),
			path + "skin1");
		chooseKderc = getFurtherInfoButton();
		chooseKderc.addActionListener(new FileChooserHandler(kderc, this));
		addComponent(chooseKderc, 4, 1, 1, 1, 1, 0, new Insets(5, 0, 5, 5));

		addComponent(new JLabel("Path to .gtkrc:"), 0, 2, 1, 1, 0, 0);
		value =
			settings_manager.getString(
				path + "skin2",
				(String) gtkrcRule.getValue());
		gtkrc = new JValidatingTextField(gtkrcRule.getPattern(), value, 200);
		save_handler.register(
			addComponent(gtkrc, 1, 2, 3, 1, 0, 0, new Insets(5, 5, 5, 2)),
			path + "skin2");
		chooseGtkrc = getFurtherInfoButton();
		chooseGtkrc.addActionListener(new FileChooserHandler(gtkrc, this));
		addComponent(chooseGtkrc, 4, 2, 1, 1, 1, 0, new Insets(5, 0, 5, 5));

		//INPUT BOX
		addComponent(new TitlePane("Input Box"), 0, 3, 10, 1, 0, 0);
		path = "/gui/input field/";
		addComponent(new JLabel("Font"), 0, 4, 1, 1, 0, 0);
		//font face
		value =
			settings_manager.getString(
				path + "font face",
				fontFaceRule.getValue().toString());
		fontFace1 = getFontFaceComponent();
		fontFace1.setSelectedItem(value);
		
		save_handler.register(
			addComponent(fontFace1, 1, 4, 2, 1, 0, 0),
			path + "font face");
		//font size
		value =
			settings_manager.getString(
				path + "font size",
				fontSizeRule.getValue().toString());
		fontSize1 = getFontSizeComponent();
		fontSize1.setSelectedItem(value);
		save_handler.register(
			addComponent(fontSize1, 3, 4, 1, 1, 0, 0),
			path + "font size");
		//foreground colour
		addComponent(new JLabel("Foreground color"), 0, 5, 1, 1, 0, 0);
		value =
			settings_manager.getString(
				path + "foreground colour",
				fgColorRule.getValue().toString());
		fgColor1 = new JValidatingTextField(fgColorRule.getPattern(), value);
		save_handler.register(
			addComponent(fgColor1, 1, 5, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
			path + "foreground colour");
		JButton chooseColor1 = getFurtherInfoButton();
		chooseColor1.addActionListener(new ColorChooserHandler(fgColor1));
		addComponent(chooseColor1, 2, 5, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
		//background color
		addComponent(new JLabel("Backgound color"), 0, 6, 1, 1, 0, 0);
		value =
			settings_manager.getString(
				path + "background colour",
				bgColorRule.getValue().toString());
		bgColor1 = new JValidatingTextField(bgColorRule.getPattern(), value);
		save_handler.register(
			addComponent(bgColor1, 1, 6, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
			path + "background colour");
		JButton chooseColor2 = getFurtherInfoButton();
		chooseColor2.addActionListener(new ColorChooserHandler(bgColor1));
		addComponent(chooseColor2, 2, 6, 1, 1, 0, 0, new Insets(5, 0, 5, 5));

		//TEXT WINDOWS
		addComponent(new TitlePane("Text Windows"), 0, 7, 10, 1, 0, 0);
		path = "/gui/text windows/";
		addComponent(new JLabel("Font"), 0, 8, 1, 1, 0, 0);
		//font face
		value =
			settings_manager.getString(
				path + "font face",
				fontFaceRule.getValue().toString());
		fontFace2 = getFontFaceComponent();
		fontFace2.setSelectedItem(value);
		save_handler.register(
			addComponent(fontFace2, 1, 8, 2, 1, 0, 0),
			path + "font face");
		//font size
		value =
			settings_manager.getString(
				path + "font size",
				fontSizeRule.getValue().toString());
		fontSize2 = getFontSizeComponent();
		fontSize2.setSelectedItem(value);
		save_handler.register(
			addComponent(fontSize2, 3, 8, 1, 1, 0, 0),
			path + "font size");
		//foreground colour
		addComponent(new JLabel("Foreground color"), 0, 9, 1, 1, 0, 0);
		value =
			settings_manager.getString(
				path + "default foreground colour",
				fgColorRule.getValue().toString());
		fgColor2 = new JValidatingTextField(fgColorRule.getPattern(), value);
		save_handler.register(
			addComponent(fgColor2, 1, 9, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
			path + "default foreground colour");
		JButton chooseColor3 = getFurtherInfoButton();
		chooseColor3.addActionListener(new ColorChooserHandler(fgColor2));
		addComponent(chooseColor3, 2, 9, 1, 1, 0, 0, new Insets(5, 0, 5, 5));
		//background color
		addComponent(new JLabel("Backgound color"), 0, 10, 1, 1, 0, 0);
		value =
			settings_manager.getString(
				path + "default background colour",
				bgColorRule.getValue().toString());
		bgColor2 = new JValidatingTextField(bgColorRule.getPattern(), value);
		save_handler.register(
			addComponent(bgColor2, 1, 10, 1, 1, 0, 0, new Insets(5, 5, 5, 2)),
			path + "default background colour");
		JButton chooseColor4 = getFurtherInfoButton();
		chooseColor4.addActionListener(new ColorChooserHandler(bgColor2));
		addComponent(chooseColor4, 2, 10, 1, 1, 0, 0, new Insets(5, 0, 5, 5));

		addLayoutStopper(0, 11);
	}

	public static JComboBox getFontFaceComponent()
	{
		String fonts[] =
			GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
		return new JComboBox(fonts);
	}

	public static JComboBox getFontSizeComponent()
	{
		List sizes = new ArrayList();

		for (int i = 6; i < 23; i++)
			sizes.add(String.valueOf(i));

		return new JComboBox(sizes.toArray());
	}
	
	private JButton getFurtherInfoButton()
	{
		JButton button = new JButton("..");
		button.setPreferredSize(new Dimension(20, 18));
		
		return button;
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
			Component comp = (Component) it.next();

			if (comp instanceof JValidatingTextField)
			{
				if (((JValidatingTextField) comp).isValid() == false)
					return true;
			}
		}

		return false;
	}

	class ColorChooserHandler implements ActionListener
	{
		private JTextField field;

		public ColorChooserHandler(JTextField field)
		{
			this.field = field;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			Color color = Color.GRAY;
			try
			{
				int[] rgb = Util.getRGB(field.getText());
				color = new Color(rgb[0], rgb[1], rgb[2]);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		
			Color col = JColorChooser.showDialog(kderc, "Choose Color", color);
			if(col != null)
			{				
				StringBuffer sCol = new StringBuffer();				
				appendHexColor(sCol, col.getRed());
				appendHexColor(sCol, col.getGreen());
				appendHexColor(sCol, col.getBlue());
				field.setText(sCol.toString());
			}							
		}
		
		private void appendHexColor(StringBuffer sb, int val)
		{
			if(val == 0)
				sb.append("00");
			else
				sb.append(Integer.toHexString(val));
		}
	}
	
	class FileChooserHandler implements ActionListener
	{
		private JTextField field;
		private VisualPane pane;
		
		JFileChooser chooser = new JFileChooser();

		public FileChooserHandler(JTextField field, VisualPane parent)
		{
			this.field = field;
			this.pane = parent;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			int returnVal = chooser.showOpenDialog(pane);
			if(returnVal == JFileChooser.APPROVE_OPTION) 
			{				   
				field.setText(chooser.getSelectedFile().getPath());
			}		
		}
	}
	
}
