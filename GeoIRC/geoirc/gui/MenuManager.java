/*
 * MenuManager.java
 *
 * Created on November 4, 2003, 7:21 PM
 */

package geoirc.gui;

import geoirc.CommandExecutor;
import geoirc.SettingsManager;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Iterator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jdom.Element;

/**
 *
 * @author  Pistos
 */
public class MenuManager
    implements
        geoirc.GeoIRCConstants,
        java.awt.event.ActionListener
{
    protected CommandExecutor executor;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected JPopupMenu menu;
    
    /** Creates a new instance of MenuManager */
    public MenuManager(
        CommandExecutor executor,
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.executor = executor;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        menu = null;
    }
    
    public void showPopup( MouseEvent event )
    {
        showPopup( event, null, null );
    }
    
    public void showPopup( MouseEvent event, Object param1 )
    {
        showPopup( event, param1, null );
    }
    
    public void showPopup( MouseEvent event, Object param1, Object param2 )
    {
        if( menu != null )
        {
            menu.setVisible( false );
        }
        menu = new JPopupMenu();
        JMenuItem item;
        
        if( event.getSource() instanceof javax.swing.JTextPane )
        {
            // %0  pane index
            
            GIPaneWrapper gipw = (GIPaneWrapper) param1;
            int index = display_manager.trueIndexToUserIndex(
                display_manager.getPaneIndexByPaneWrapper( gipw )
            );
            String index_str = Integer.toString( index );
            if( index == -1 )
            {
                index_str = "";
            }

            /*
            JMenu submenu = new JMenu( "Dock" );
            menu.add( submenu );

            item = new JMenuItem( "Top" );
            item.setActionCommand( "dockpane " + index_str + " t" );
            item.addActionListener( this );
            submenu.add( item );
            item = new JMenuItem( "Left" );
            item.setActionCommand( "dockpane " + index_str + " l" );
            item.addActionListener( this );
            submenu.add( item );
            item = new JMenuItem( "Right" );
            item.setActionCommand( "dockpane " + index_str + " r" );
            item.addActionListener( this );
            submenu.add( item );
            item = new JMenuItem( "Bottom" );
            item.setActionCommand( "dockpane " + index_str + " b" );
            item.addActionListener( this );
            submenu.add( item );
             */
            
            List menu_items = settings_manager.getNodes(
                "/menu/context/text_pane",
                DONT_CREATE_NODES
            );
            Iterator it = menu_items.iterator();
            Element element;
            String menu_text;
            String command;
            while( it.hasNext() )
            {
                element = (Element) it.next();
                menu_text = element.getAttributeValue( "text" );
                command = element.getAttributeValue( "command" );
                menu_text.replaceAll( "%0", index_str );
                command.replaceAll( "%0", index_str );
                item = new JMenuItem( menu_text );
                item.setActionCommand( command );
                item.addActionListener( this );
                menu.add( item );
            }
            

            item = new JMenuItem( "Clear" );
            item.setActionCommand( "clearpane " + index_str );
            item.addActionListener( this );
            menu.add( item );

            menu.show( event.getComponent(), event.getX(), event.getY() );
        }
    }
    
    public void actionPerformed( java.awt.event.ActionEvent e )
    {
        executor.execute( e.getActionCommand() );
    }
    
}
