/*
 * TitlePane.java
 * 
 * Created on 19.08.2003
 */
package geoirc.conf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author netseeker aka Michael Manske
 */
public class TitlePane extends JPanel
{
	/**
	 * 
	 */
	public TitlePane(String title)
	{
		super();
		setPreferredSize(new Dimension(300, 20));
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
		//setBackground(Color.WHITE);
		//setBorder(BorderFactory.createEtchedBorder());
		Font font = new Font("Dialog", Font.BOLD, 12);
		JLabel label = new JLabel(title);
		label.setFont(font);
		label.setForeground(Color.WHITE);
		add(label);
		label.setVerticalAlignment(JLabel.CENTER);
		setMinimumSize(label.getMinimumSize());
	}

	public boolean isOpaque()
	{
		return true;
	}

	protected void paintComponent(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();

		// Create the gradient paint        
		GradientPaint paint =
			new GradientPaint(0, 0, new Color(85,134,190), width, height, getBackground(), true);

		// we need to cast to Graphics2D for this operation
		Graphics2D g2d = (Graphics2D) g;

		// save the old paint
		Paint oldPaint = g2d.getPaint();

		// set the paint to use for this operation
		g2d.setPaint(paint);

		// fill the background using the paint
		g2d.fillRect(0, 0, width, height);

		// restore the original paint
		g2d.setPaint(oldPaint);
	}

}
