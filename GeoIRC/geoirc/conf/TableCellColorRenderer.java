/*
 * ColorRenderer.java
 * 
 * Created on 22.08.2003
 */
package geoirc.conf;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * @author netseeker aka Michael Manske
 */
public class TableCellColorRenderer extends JLabel implements TableCellRenderer
{
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;

	public TableCellColorRenderer(boolean isBordered)
	{
		super();
		this.isBordered = isBordered;
		setOpaque(true); //MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object color,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column)
	{
		setBackground((Color) color);
		if (isBordered)
		{
			if (isSelected)
			{
				if (selectedBorder == null)
				{
					selectedBorder =
						BorderFactory.createMatteBorder(
							2,
							5,
							2,
							5,
							table.getSelectionBackground());
				}
				setBorder(selectedBorder);
			}
			else
			{
				if (unselectedBorder == null)
				{
					unselectedBorder =
						BorderFactory.createMatteBorder(
							2,
							5,
							2,
							5,
							table.getBackground());
				}
				setBorder(unselectedBorder);
			}
		}
		return this;
	}
}
