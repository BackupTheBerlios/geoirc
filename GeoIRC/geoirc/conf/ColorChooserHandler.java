/*
 * ColorChooserHandler.java
 * 
 * Created on 21.08.2003
 */
package geoirc.conf;

import geoirc.util.Util;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JTextField;

/**
 * @author netseeker aka Michael Manske
 */
public class ColorChooserHandler implements ActionListener
{
	private JTextField field;
	private BaseSettingsPanel pane;

	public ColorChooserHandler(JTextField field, BaseSettingsPanel pane)
	{
		this.field = field;
		this.pane = pane;
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
		
		Color col = JColorChooser.showDialog(pane, "Choose Color", color);
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
