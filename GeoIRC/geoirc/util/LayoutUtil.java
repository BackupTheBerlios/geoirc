/*
 * LayoutUtil.java
 * 
 * Created on 30.09.2003
 */
package geoirc.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * @author netseeker aka Michael Manske
 */
public class LayoutUtil
{
    public static GridBagConstraints getGBC(        
        int x,
        int y,
        int width,
        int height,
        int anchor,
        int fill,
        Insets insets)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;

        return gbc;
    }

    public static GridBagConstraints getGBC(        
        int x,
        int y,
        int width,
        int height,
        int anchor,
        int fill)
    {
        return getGBC( x, y, width, height, anchor, fill, new Insets( 5, 5, 5, 5) );
    }

    public static GridBagConstraints getGBC(        
        int x,
        int y,
        int width,
        int height,
        int anchor)
    {
        return getGBC( x, y, width, height, anchor, GridBagConstraints.NONE );
    }

    public static GridBagConstraints getGBC(        
        int x,
        int y,
        int width,
        int height)
    {
        return getGBC( x, y, width, height, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE );
    }


    public static JButton getSafeImageButton( URL icon, String text, String tooltip )
    {
        JButton button = null;
        
        try
        {
            if(text != null )
            {
                button = new JButton( text, new ImageIcon( icon ));
            }
            else
            {
                button = new JButton( new ImageIcon( icon ));
            }            
        }
        catch (NullPointerException e)
        {
            if(text != null )
            {
                button = new JButton( text );
            }
            else
            {
                button = new JButton( ".." );
            }            
        }
        
        if( tooltip != null )
        {
            button.setToolTipText( tooltip );
        }
        
        return button;
    }


    public static JButton getSafeImageButton( URL icon, String text, String tooltip, int width, int height )
    {
        JButton button = getSafeImageButton( icon, text, tooltip );
        Dimension dim =  new Dimension( width, height );
        button.setPreferredSize( dim );
        button.setMaximumSize( dim );        
        
        return button;
    }
}
