/*
 * GIInfoPane.java
 *
 * Created on July 16, 2003, 11:18 PM
 */

package geoirc.gui;

import geoirc.SettingsManager;
import javax.swing.JTree;

/**
 *
 * @author  Pistos
 */
public class GIInfoPane extends GIPane implements geoirc.GeoIRCConstants
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
    
}
