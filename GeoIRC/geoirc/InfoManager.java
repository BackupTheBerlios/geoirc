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
    protected I18nManager i18n_manager;
    protected DefaultTreeModel tree;
    protected DefaultMutableTreeNode root;
    protected Hashtable tree_inverse;
    protected Hashtable tree_for_path;
    
    // No default constructor.
    private InfoManager() { }
    
    public InfoManager(
        SettingsManager settings_manager,
        DisplayManager display_manager,
        I18nManager i18n_manager
    )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.i18n_manager = i18n_manager;
        root = new DefaultMutableTreeNode( i18n_manager.getString( "connections" ) );
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
    
    public synchronized void addRemoteMachine( RemoteMachine rm )
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
    
    public synchronized void removeRemoteMachine( RemoteMachine rm )
    {
        DefaultMutableTreeNode rm_node = (DefaultMutableTreeNode) tree_inverse.get( rm );
        if( rm_node != null )
        {
            root.remove( rm_node );
        }
        tree.reload( root );
        
        tree_inverse.remove( rm );
        String path = "/" + rm.toString();
        tree_for_path.remove( path );
        
        display_manager.deactivateInfoPanes( path );
    }
    
    public synchronized void addChannel( Channel c )
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
    
    public synchronized void removeChannel( Channel c )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_inverse.get( c );
        ( (DefaultMutableTreeNode) node.getParent() ).remove( node );
        tree.reload( node );
        tree_inverse.remove( c );
        String path = "/" + c.getServer().toString() + "/" + c.getName();
        tree_for_path.remove( path );
        display_manager.deactivateInfoPanes( path );
    }
    
    public synchronized void removeAllMembers( Channel c )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_inverse.get( c );
        node.removeAllChildren();
        tree.reload( node );
    }
    
    public synchronized void setMembers( Channel c, java.util.Vector members )
    {
        DefaultMutableTreeNode channel_node
            = (DefaultMutableTreeNode) tree_inverse.get( c );

        User u;
        DefaultMutableTreeNode user_node;
        String path;
        DefaultTreeModel model;
        
        channel_node.removeAllChildren();
        
        for( int i = 0, n = members.size(); i < n; i++ )
        {
            u = (User) members.elementAt( i );
            user_node = new DefaultMutableTreeNode( u );
            channel_node.add( user_node );
            path = "/" + c.getServer().toString()
                + "/" + c.getName()
                + "/" + u.getNick();
            model = new DefaultTreeModel( user_node );
            tree_for_path.put( path, model );
            display_manager.activateInfoPanes( path, model );
        }
        
        tree.reload( channel_node );
    }
    
    public synchronized void addMember( User u, Channel c, int index )
    {
        DefaultMutableTreeNode channel_node
            = (DefaultMutableTreeNode) tree_inverse.get( c );
        DefaultMutableTreeNode user_node = new DefaultMutableTreeNode( u );
        channel_node.insert( user_node, index );
        tree.reload( channel_node );

        String path = "/" + c.getServer().toString()
            + "/" + c.getName()
            + "/" + u.getNick();
        DefaultTreeModel model = new DefaultTreeModel( user_node );
        tree_for_path.put( path, model );
        display_manager.activateInfoPanes( path, model );
    }
    
    public synchronized void removeMember( User u, Channel c )
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
        
        String path = "/" + c.getServer().toString()
            + "/" + c.getName()
            + "/" + u.getNick();
        tree_for_path.remove( path );
        display_manager.deactivateInfoPanes( path );
    }
    
    public synchronized void acknowledgeUserChange( Channel c, User u, int new_index )
    {
        DefaultMutableTreeNode channel_node = (DefaultMutableTreeNode) tree_inverse.get( c );
        java.util.Enumeration children = channel_node.children();
        DefaultMutableTreeNode user_node;
        User user;
        DefaultMutableTreeNode the_user_node = null;
        while( children.hasMoreElements() )
        {
            user_node = (DefaultMutableTreeNode) children.nextElement();
            user = (User) user_node.getUserObject();
            if( user == u )
            {
                the_user_node = user_node;
                break;
            }
        }
        
        if( the_user_node != null )
        {
            channel_node.remove( the_user_node );
            channel_node.insert( the_user_node, new_index );
            tree.reload( channel_node );
        }
    }
}
