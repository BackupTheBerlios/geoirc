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
public class GITreeCellRenderer
    extends DefaultTreeCellRenderer
    implements GeoIRCConstants
{
    ImageIcon regular_icon;
    ImageIcon voiced_icon;
    ImageIcon op_icon;
    
    public GITreeCellRenderer()
    {
        regular_icon = new ImageIcon( "regular_icon.gif" );
        voiced_icon = new ImageIcon( "voiced_icon.gif", "voiced" );
        op_icon = new ImageIcon( "op_icon.gif", "operator" );
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
        
        this.setIcon( null );
        
        if( leaf )
        {
            if( value != null )
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object obj = node.getUserObject();
                if( obj instanceof User )
                {
                    DefaultMutableTreeNode channel_node = (DefaultMutableTreeNode) node.getParent();
                    Channel channel = (Channel) channel_node.getUserObject();
                    User user = (User) obj;
                    setIcon( regular_icon );
                    if( user.hasModeFlag( channel, MODE_VOICE ) )
                    {
                        setIcon( voiced_icon );
                    }
                    if( user.hasModeFlag( channel, MODE_OP ) )
                    {
                        setIcon( op_icon );
                    }
                }
            }
        }
        
        return this;
    }
}
