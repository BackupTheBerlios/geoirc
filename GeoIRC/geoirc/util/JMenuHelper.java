/*
 * JMenuHelper.java
 * 
 * Created on 13.08.2003
 */
package geoirc.util;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * @author netseeker aka Michael Manske
 */
public class JMenuHelper
{
	/**
	 * Add an element to the menubar.
	 *
	 * @param    menuBar             a  JMenuBar
	 * @param    s                   a  String
	 *
	 * @return   added JMenu
	 *
	 */
	public static JMenu addMenuBarItem(JMenuBar menuBar, String s)
	{
		JMenu menu;
		if (s.indexOf("_") > -1)
		{
			int pos = s.indexOf("_");
			char c = s.charAt(pos + 1);
			StringBuffer sb = new StringBuffer(s).delete(pos, pos + 1);
			menu = new JMenu(sb.toString());
			menu.setMnemonic(c);
		}
		else
			menu = new JMenu(s);
		menuBar.add(menu);
		return menu;
	}

	private static JMenuItem processMnemonic(String s)
	{
		if (s.indexOf("_") > -1)
		{
			int pos = s.indexOf("_");
			char c = s.charAt(pos + 1);
			StringBuffer sb = new StringBuffer(s).delete(pos, pos + 1);
			return new JMenuItem(sb.toString(), c);
		}
		else
			return new JMenuItem(s);
	}
	/**
	 * Insert a JMenuItem to a given JMenu.
	 *
	 * @param    m                   a  JMenu
	 * @param    s                   a  String
	 * @param    keyChar             a  char
	 * @param    al                  an ActionListener
	 *
	 * @return   a JMenuItem
	 */
	public static JMenuItem addMenuItem(
		JMenu m,
		String s,
		char keyChar,
		ActionListener al)
	{
		if (s.startsWith("-"))
		{
			m.addSeparator();
			return null;
		}
		JMenuItem menuItem = processMnemonic(s);
		m.add(menuItem);
		if (keyChar != 0)
			menuItem.setAccelerator(
				KeyStroke.getKeyStroke(keyChar, InputEvent.CTRL_MASK));
		if (al != null)
			menuItem.addActionListener(al);
		return menuItem;
	}

	public static JMenuItem addMenuItem(JMenu m, String s, char c)
	{
		return addMenuItem(m, s, c, null);
	}

	public static JMenuItem addMenuItem(JMenu m, String s)
	{
		return addMenuItem(m, s, (char) 0, null);
	}

	public static JMenuItem addMenuItem(JMenu m, String s, ActionListener al)
	{
		return addMenuItem(m, s, (char) 0, al);
	}
}
