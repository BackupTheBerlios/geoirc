/*
 * GITreeCellRenderer.java
 *
 * Created on September 15, 2003, 5:02 PM
 */

package geoirc;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
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
    ImageIcon halfop_icon;
    ImageIcon op_icon;
    
    public GITreeCellRenderer() throws FileNotFoundException
    {
        if( ( regular_icon = loadIcon( ICON_PATH + "regular_icon.png" ) ) == null )
        {
            throw new FileNotFoundException( "regular_icon.png" );
        }
        if( ( voiced_icon = loadIcon( ICON_PATH + "voiced_icon.png" ) ) == null )
        {
            throw new FileNotFoundException( "voiced_icon.png" );
        }
        if( ( halfop_icon = loadIcon( ICON_PATH + "halfop_icon.png" ) ) == null )
        {
            throw new FileNotFoundException( "halfop_icon.png" );
        }
        if( ( op_icon = loadIcon( ICON_PATH + "op_icon.png" ) ) == null )
        {
            throw new FileNotFoundException( "op_icon.png" );
        }
    }
    
    protected ImageIcon loadIcon( String filename )
    {
        ImageIcon retval = null;
        
        File f = new File( filename );
        if( f.exists() )
        {
            retval = new ImageIcon( filename );
        }
        
        return retval;
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
                    if( user.hasModeFlag( channel, MODE_HALFOP ) )
                    {
                        setIcon( halfop_icon );
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
