/*
 * InfoManager.java
 *
 * Created on July 12, 2003, 12:54 AM
 */

package geoirc;

import javax.swing.tree.*;
import java.util.Enumeration;
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
    protected Hashtable tree_for_path;
    
    // No default constructor.
    private InfoManager() { }
    
    public InfoManager(
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        root = new DefaultMutableTreeNode( "Connections" );
        tree = new DefaultTreeModel( root );
        tree_inverse = new Hashtable();
        tree_for_path = new Hashtable();
        
        tree_for_path.put( "/", tree );
        activateInfoPanes( "/" );
    }
    
    /**
     * If a TreeModel exists for the given path, then all GIInfoPanes
     * with that path are activated.  (This method is used when adding
     * new InfoPanes.
     */
    public boolean activateInfoPanes( String path )
    {
        boolean success = false;
        TreeModel tm = (TreeModel) tree_for_path.get( path );
        if( tm != null )
        {
            display_manager.activateInfoPanes( path, tm );
            success = true;
        }
        
        return success;
    }
    
    public void addRemoteMachine( RemoteMachine rm )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( rm );
        root.add( node );
        tree.reload( root );
        tree_inverse.put( rm, node );
        DefaultTreeModel model = new DefaultTreeModel( node );
        String path = "/" + rm.toString();
        tree_for_path.put( path, model );
        display_manager.activateInfoPanes( path, model );
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
        String path = "/" + rm.toString();
        tree_for_path.remove( path );
        display_manager.deactivateInfoPanes( path );
    }
    
    public void addChannel( Channel c )
    {
        Server server = c.getServer();
        DefaultMutableTreeNode node
            = (DefaultMutableTreeNode) tree_inverse.get( server );
        DefaultMutableTreeNode channel_node = new DefaultMutableTreeNode( c );
        node.add( channel_node );
        tree.reload( node );
        tree_inverse.put( c, channel_node );
        String path = "/" + c.getServer().toString() + "/" + c.getName();
        DefaultTreeModel model = new DefaultTreeModel( channel_node );
        tree_for_path.put( path, model );
        display_manager.activateInfoPanes( path, model );
    }
    
    public void removeChannel( Channel c )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_inverse.get( c );
        ( (DefaultMutableTreeNode) node.getParent() ).remove( node );
        tree.reload( node );
        tree_inverse.remove( c );
        String path = "/" + c.getServer().toString() + "/" + c.getName();
        tree_for_path.remove( path );
        display_manager.deactivateInfoPanes( path );
    }
    
    public void addMember( User u, Channel c )
    {
        DefaultMutableTreeNode channel_node
            = (DefaultMutableTreeNode) tree_inverse.get( c );
        DefaultMutableTreeNode user_node = new DefaultMutableTreeNode( u );
        channel_node.add( user_node );
        tree.reload( channel_node );

        String path = "/" + c.getServer().toString()
            + "/" + c.getName()
            + "/" + u.getNick();
        DefaultTreeModel model = new DefaultTreeModel( user_node );
        tree_for_path.put( path, model );
        display_manager.activateInfoPanes( path, model );
    }
    
    public void removeMember( User u, Channel c )
    {
        DefaultMutableTreeNode channel_node = (DefaultMutableTreeNode) tree_inverse.get( c );
        if( channel_node != null )
        {
            Enumeration users = channel_node.children();
            DefaultMutableTreeNode user_node;
            while( users.hasMoreElements() )
            {
                user_node = (DefaultMutableTreeNode) users.nextElement();
                if( ((User) user_node.getUserObject()).equals( u ) )
                {
                    channel_node.remove( user_node );
                    break;
                }
            }
            tree.reload( channel_node );
        }
        
        String path = "/" + c.toString()
            + "/" + c.getName()
            + "/" + u.getNick();
        tree_for_path.remove( path );
        display_manager.deactivateInfoPanes( path );
    }
    
    public void acknowledgeNickChange( Channel c )
    {
        tree.reload( (DefaultMutableTreeNode) tree_inverse.get( c ) );
    }
}
