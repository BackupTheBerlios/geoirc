/*
 * ComponentFactory.java
 * 
 * Created on 21.08.2003
 */
package geoirc.conf;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

/**
 * @author netseeker aka Michael Manske
 */
public class ComponentFactory
{
	/**
	 * Creates a new combo box containing all available font face names
	 */
	public static JComboBox getFontFaceComponent()
	{
		String fonts[] =
			GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
		return new JComboBox(fonts);
	}

	/**
	 * Creates a new combo box containing size values [6-22]
	 */
	public static JComboBox getFontSizeComponent()
	{
		List sizes = new ArrayList();

		for (int i = 6; i < 23; i++)
			sizes.add(String.valueOf(i));

		return new JComboBox(sizes.toArray());
	}
	
	/**
	 * Creates a simple JButton useable as filechooser buttons or colorchooser button 
	 */
	public static JButton getFurtherInfoButton()
	{
		JButton button = new JButton("..");
		button.setPreferredSize(new Dimension(30, 20));
		
		return button;
	}

	/**
	 * Creates a simple JButton useable as filechooser buttons or colorchooser button 
	 * @param listener action listener for the created button
	 * @return a new JButton with added action listener
	 */
	public static JButton getFurtherInfoButton(ActionListener listener)
	{
		JButton button = getFurtherInfoButton();
		button.addActionListener(listener);
		
		return button;
	}
}
