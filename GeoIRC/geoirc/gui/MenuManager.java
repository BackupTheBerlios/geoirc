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
    protected JPopupMenu popup_menu;
    
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
        popup_menu = null;
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
        if( popup_menu != null )
        {
            popup_menu.setVisible( false );
        }
        popup_menu = new JPopupMenu();
        
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
            
            String [] args = new String [] { index_str };

            /*
            JMenu submenu = new JMenu( "Dock" );
            popup_menu.add( submenu );

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
            
            buildMenu( popup_menu, "/popup_menu/context/text_pane/", args );
            
            popup_menu.show( event.getComponent(), event.getX(), event.getY() );
        }
    }
    
    protected void buildMenu( javax.swing.JComponent menu, String path, String [] args )
    {
        List menu_items = settings_manager.getChildren(
            path, DONT_CREATE_NODES
        );
        Iterator it = menu_items.iterator();
        Element element;
        String menu_text;
        String command;
        JMenuItem item;
        String is_submenu = "false";
        while( it.hasNext() )
        {
            element = (Element) it.next();
            menu_text = element.getAttributeValue( "text" );
            is_submenu = element.getAttributeValue( "submenu" );
            if( ( is_submenu != null ) && is_submenu.equals( "true" ) )
            {
            }
            else
            {
                command = element.getAttributeValue( "command" );
                for( int i = 0; i < args.length; i++ )
                {
                    menu_text = menu_text.replaceAll( "%" + Integer.toString( i ), args[ i ] );
                    command = command.replaceAll( "%" + Integer.toString( i ), args[ i ] );
                }
                item = new JMenuItem( menu_text );
                item.setActionCommand( command );
                item.addActionListener( this );
                if( menu instanceof JPopupMenu )
                {
                    ((JPopupMenu) menu).add( item );
                }
                else if( menu instanceof JMenu )
                {
                    ((JMenu) menu).add( item );
                }
            }
        }
    }
    
    public void actionPerformed( java.awt.event.ActionEvent e )
    {
        executor.execute( e.getActionCommand() );
    }
    
}
