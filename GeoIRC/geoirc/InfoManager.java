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
        
        display_manager.activateInfoWindows( "/", tree );
    }
    
    public void addRemoteMachine( RemoteMachine rm )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( rm );
        root.add( node );
        tree.reload( root );
        tree_inverse.put( rm, node );
        display_manager.activateInfoWindows(
            "/" + rm.toString(),
            new DefaultTreeModel( node )
        );
    }
    
    public void removeRemoteMachine( DefaultMutableTreeNode node )
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
        display_manager.deactivateInfoWindows(
            "/" + rm.toString()
        );
    }
    
    public void addChannel( Channel c )
    {
        Server server = c.getServer();
        DefaultMutableTreeNode node
            = (DefaultMutableTreeNode) tree_inverse.get( server );
        node.add( new DefaultMutableTreeNode( c ) );
        tree.reload( node );
        tree_inverse.put( c, node );
        display_manager.activateInfoWindows(
            "/" + c.getServer().toString() + "/" + c.getName(),
            new DefaultTreeModel( node )
        );
    }
    
    public void removeChannel( Channel c )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_inverse.get( c );
        ( (DefaultMutableTreeNode) node.getParent() ).remove( node );
        tree.reload( node );
        tree_inverse.remove( c );
        display_manager.deactivateInfoWindows(
            "/" + c.getServer().toString() + "/" + c.getName()
        );
    }
}
