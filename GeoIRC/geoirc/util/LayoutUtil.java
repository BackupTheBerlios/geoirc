/*
 * LayoutUtil.java
 * 
 * Created on 30.09.2003
 */
package geoirc.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

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

}
