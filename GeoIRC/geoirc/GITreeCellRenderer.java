/*
 * GITreeCellRenderer.java
 *
 * Created on September 15, 2003, 5:02 PM
 */

package geoirc;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author  Pistos
 */
public class GITreeCellRenderer extends DefaultTreeCellRenderer
{
    ImageIcon regular_icon;
    ImageIcon voiced_icon;
    ImageIcon op_icon;
    
    public GITreeCellRenderer()
    {
        regular_icon = new ImageIcon( "regular_user.png" );
        voiced_icon = new ImageIcon( "voiced_user.png", "voiced" );
        op_icon = new ImageIcon( "op_user.png" );
    }
    
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean sel,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus
    )
    {
        super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );
        
        if( leaf )
        {
            if( value != null )
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                User user = (User) node.getUserObject();
                setIcon( regular_icon );
            }
        }
        
        return this;
    }
}
