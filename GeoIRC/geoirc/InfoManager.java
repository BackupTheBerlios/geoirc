/*
 * InfoManager.java
 *
 * Created on July 12, 2003, 12:54 AM
 */

package geoirc;

import javax.swing.tree.*;
import java.util.Hashtable;

/**
 * Manages the tree of servers, channels and users.
 *
 * @author  Pistos
 */
public class InfoManager
{
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected DefaultTreeModel tree;
    protected DefaultMutableTreeNode root;
    protected Hashtable tree_inverse;
    
    // No default constructor.
    private InfoManager() { }
    
    public InfoManager(
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        root = new DefaultMutableTreeNode( "Servers" );
        tree = new DefaultTreeModel( root );
        tree_inverse = new Hashtable();
        
        // Temporary line:
        //display_manager.addNewInfoWindow( "Info", "/" );
    }
    
    public void addRemoteMachine( RemoteMachine rm )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( rm );
        root.add( node );
        tree.reload( root );
        tree_inverse.put( rm, node );
    }
    
    public void removeRemoteMachine( DefaultMutableTreeNode node )
    {
        removeTreeNode( node );
    }
    public void removeChannel( DefaultMutableTreeNode node )
    {
        removeTreeNode( node );
    }
    public void removeTreeNode( DefaultMutableTreeNode node )
    {
        root.remove( node );
        tree.reload( root );
        tree_inverse.remove( node.getUserObject() );
    }
    
    public void removeRemoteMachine( RemoteMachine rm )
    {
        root.remove( (DefaultMutableTreeNode) tree_inverse.get( rm ) );
        tree.reload( root );
        tree_inverse.remove( rm );
    }
    
    public void addChannel( Channel c )
    {
        Server server = c.getServer();
        DefaultMutableTreeNode node
            = (DefaultMutableTreeNode) tree_inverse.get( server );
        node.add( new DefaultMutableTreeNode( c ) );
        tree.reload( node );
        tree_inverse.put( server, node );
    }
    
    public void removeChannel( Channel c )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_inverse.get( c );
        root.remove( node );
        tree.reload( node );
        tree_inverse.remove( c );
    }
}
