/*
 * MenuManager.java
 *
 * Created on November 4, 2003, 7:21 PM
 */

package geoirc.gui;

import geoirc.Channel;
import geoirc.CommandExecutor;
import geoirc.GeoIRC;
import geoirc.RemoteMachine;
import geoirc.SettingsManager;
import geoirc.User;

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
    protected GeoIRC geo_irc;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected JPopupMenu popup_menu;
    
    /** Creates a new instance of MenuManager */
    public MenuManager(
        GeoIRC geo_irc,
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.geo_irc = geo_irc;
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
        popup_menu = new JPopupMenu();
        String [] args;
        
        if( event.getSource() instanceof javax.swing.JTextPane )
        {
            // %0  pane index
            // %1  pane title
            
            GIPaneWrapper gipw = (GIPaneWrapper) param1;
            int index = display_manager.trueIndexToUserIndex(
                display_manager.getPaneIndexByPaneWrapper( gipw )
            );
            String index_str = Integer.toString( index );
            if( index == -1 )
            {
                index_str = "";
            }
            
            args = new String [] {
                index_str,
                gipw.getTitle()
            };

            buildMenu(
                popup_menu,
                settings_manager.getChildren(
                    "/menu/context/text_pane/",
                    DONT_CREATE_NODES
                ),
                args
            );
            
            if( popup_menu.getComponentCount() > 0 )
            {
                popup_menu.show( event.getComponent(), event.getX(), event.getY() );
            }
        }
        else if( event.getSource() instanceof javax.swing.JTree )
        {
            GIPaneWrapper gipw = (GIPaneWrapper) param1;
            
            GIInfoPane giip = (GIInfoPane) gipw.getPane();
            Object user_object = giip.getUserObjectForLocation( event );
            if( user_object != null )
            {
                if( user_object instanceof RemoteMachine )
                {
                    // %0  remote machine id number
                    
                    RemoteMachine rm = (RemoteMachine) user_object;
                    args = new String [] {
                        geo_irc.getRemoteMachineID( rm )
                    };
                    buildMenu(
                        popup_menu,
                        settings_manager.getChildren(
                            "/menu/context/remote_machine/",
                            DONT_CREATE_NODES
                        ),
                        args
                    );
                }
                else if( user_object instanceof Channel )
                {
                    // %0  user nick
                    
                    Channel channel = (Channel) user_object;
                    args = new String [] {
                        channel.getName()
                    };
                    buildMenu(
                        popup_menu,
                        settings_manager.getChildren(
                            "/menu/context/channel/",
                            DONT_CREATE_NODES
                        ),
                        args
                    );
                }
                else if( user_object instanceof User )
                {
                    // %0  user nick
                    
                    User user = (User) user_object;
                    args = new String [] {
                        user.getNick()
                    };
                    buildMenu(
                        popup_menu,
                        settings_manager.getChildren(
                            "/menu/context/user/",
                            DONT_CREATE_NODES
                        ),
                        args
                    );
                }
            }
            else
            {
                // %0  pane index
            
                int index = display_manager.trueIndexToUserIndex(
                    display_manager.getPaneIndexByPaneWrapper( gipw )
                );
                String index_str = Integer.toString( index );
                if( index == -1 )
                {
                    index_str = "";
                }

                args = new String [] {
                    index_str
                };

                buildMenu(
                    popup_menu,
                    settings_manager.getChildren(
                        "/menu/context/info_pane/",
                        DONT_CREATE_NODES
                    ),
                    args
                );
            }
            
            if( popup_menu.getComponentCount() > 0 )
            {
                popup_menu.show( event.getComponent(), event.getX(), event.getY() );
            }
        }
        else if( event.getSource() instanceof org.jscroll.JScrollDesktopPane )
        {
            // No arguments.
            buildMenu(
                popup_menu,
                settings_manager.getChildren(
                    "/menu/context/desktop/",
                    DONT_CREATE_NODES
                ),
                new String [ 0 ]
            );
        }
        else if( event.getSource() instanceof geoirc.gui.GIPaneBarButton )
        {
            // %0  pane index
            // %1  pane title
            
            GIPaneWrapper gipw = (GIPaneWrapper) param1;
            int index = display_manager.trueIndexToUserIndex(
                display_manager.getPaneIndexByPaneWrapper( gipw )
            );
            String index_str = Integer.toString( index );
            if( index == -1 )
            {
                index_str = "";
            }
            
            args = new String [] {
                index_str,
                gipw.getTitle()
            };

            buildMenu(
                popup_menu,
                settings_manager.getChildren(
                    "/menu/context/pane_bar_button/",
                    DONT_CREATE_NODES
                ),
                args
            );
            
            if( popup_menu.getComponentCount() > 0 )
            {
                popup_menu.show( event.getComponent(), event.getX(), event.getY() );
            }
        }
    }
    
    protected void buildMenu( javax.swing.JComponent menu, List menu_items, String [] args )
    {
        if( menu_items == null )
        {
            return;
        }
        
        Iterator it = menu_items.iterator();
        Element element;
        String menu_text;
        String command;
        String node_type;
        JMenuItem item;
        while( it.hasNext() )
        {
            element = (Element) it.next();
            node_type = element.getName();
            menu_text = element.getAttributeValue( "text" );
            for( int i = 0; i < args.length; i++ )
            {
                menu_text = menu_text.replaceAll( "%" + Integer.toString( i ), args[ i ] );
            }
            
            if( node_type.equals( SUBMENU_NODE ) )
            {
                JMenu submenu = new JMenu( menu_text );
                List children = element.getChildren();
                buildMenu( submenu, children, args );
                if( menu instanceof JPopupMenu )
                {
                    ((JPopupMenu) menu).add( submenu );
                }
                else if( menu instanceof JMenu )
                {
                    ((JMenu) menu).add( submenu );
                }
            }
            else if( node_type.equals( PANE_LIST_NODE ) )
            {
                command = element.getAttributeValue( "command" );
                for( int i = 0; i < args.length; i++ )
                {
                    command = command.replaceAll( "%" + Integer.toString( i ), args[ i ] );
                }
                String [] pane_titles = display_manager.getPaneTitles();
                String subcommand;
                String subtext;
                for( int i = 0; i < pane_titles.length; i++ )
                {
                    subcommand = command.replaceAll( "%i", Integer.toString( i + 1 ) );
                    subtext = menu_text.replaceAll( "%i", Integer.toString( i + 1 ) );
                    subtext = subtext.replaceAll( "%t", pane_titles[ i ] );
                    item = new JMenuItem( subtext );
                    item.setActionCommand( subcommand );
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
            else if( node_type.equals( ITEM_NODE ) )
            {
                command = element.getAttributeValue( "command" );
                for( int i = 0; i < args.length; i++ )
                {
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
        geo_irc.execute( e.getActionCommand() );
    }
    
}
