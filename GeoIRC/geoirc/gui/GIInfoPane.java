/*
 * GIInfoPane.java
 *
 * Created on July 16, 2003, 11:18 PM
 */

package geoirc.gui;

import geoirc.SettingsManager;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author  Pistos
 */
public class GIInfoPane
    extends GIPane
    implements geoirc.GeoIRCConstants, java.awt.event.MouseListener
{
    protected JTree tree;
    String path;  // server-channel-user path
    
    public GIInfoPane(
        DisplayManager display_manager,
        SettingsManager settings_manager,
        String title,
        String path
    )
    {
        super( display_manager, settings_manager, title, null );
        if( ( path == null ) || ( path.equals( "" ) ) )
        {
            this.path = "/";
        }
        else
        {
            this.path = path;
        }
        tree = null;
    }
    
    public boolean isActive()
    {
        return ( tree != null );
    }
    
    public void activate( javax.swing.tree.TreeModel model )
    {
        tree = new JTree( model );
        tree.setCellRenderer( display_manager.getCellRenderer() );
        tree.setRowHeight( INFO_WINDOW_TREE_ROW_HEIGHT );
        tree.setLargeModel( true );
        tree.setDoubleBuffered( true );        
        tree.setRootVisible( settings_manager.getBoolean("/gui/info windows/show root node", true) );
        tree.setShowsRootHandles( true );
        setViewportView( tree );
        tree.addKeyListener( display_manager );
        tree.addMouseListener( this );
    }
    
    public void deactivate()
    {
        tree = null;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public void mouseClicked( MouseEvent e ) { }
    public void mouseEntered( MouseEvent e ) { }
    public void mouseExited( MouseEvent e ) { }
    public void mousePressed( MouseEvent e ) { }
    public void mouseReleased( MouseEvent e )
    {
        gipw.activate();
        
        if( e.isPopupTrigger() )
        {
            display_manager.getMenuManager().showPopup( e, gipw );
        }
    }
    
    public Object getUserObjectForLocation( MouseEvent e )
    {
        Object retval = null;
        
        javax.swing.tree.TreePath tree_path = tree.getPathForLocation( e.getX(), e.getY() );
        if( tree_path != null )
        {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree_path.getLastPathComponent();
            if( dmtn != null )
            {
                retval = dmtn.getUserObject();
            }
        }
        
        return retval;
    }
}
