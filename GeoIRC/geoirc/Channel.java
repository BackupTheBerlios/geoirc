/*
 * Channel.java
 *
 * Created on July 12, 2003, 6:37 PM
 */

package geoirc;

import geoirc.util.Util;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class Channel implements GeoIRCConstants
{
    protected String name;
    protected String topic;
    protected String topic_setter;
    protected Date topic_set_date; 
    protected Vector members;
    protected Server server;
    protected InfoManager info_manager;
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected int sort_order;
    protected UserComparator comparator;
    
    // No default constructor
    private Channel() { }
    
    public Channel(
        Server server,
        String name,
        InfoManager info_manager,
        SettingsManager settings_manager,
        DisplayManager display_manager
    )
    {
        this.server = server;
        this.name = name.toLowerCase();
        this.info_manager = info_manager;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        topic = null;
        topic_setter = null;
        topic_set_date = null;
        if( ! setSortOrder( 
                settings_manager.getInt(
                    "/gui/info windows/sort order",
                    DEFAULT_SORT_ORDER
                )
            )
        )
        {
            throw new IllegalArgumentException( "Invalid sort order." );
        }
        
        members = new Vector();
    }
    
    public String getName()
    {
        return name;
    }
    
    public Server getServer()
    {
        return server;
    }

    public String toString()
    {
        return /*server.toString() + "/" + */name;
    }

    /**
     * Clears any current membership list, and reinitializes it
     * using the provided content of a RPL_NAMREPLY message.
     */
    public void setChannelMembership( Vector new_member_list )
    {
        //info_manager.removeAllMembers( this );
        addToChannelMembership( new_member_list );
    }

    public void addToChannelMembership( Vector new_member_list )
    {
        //members = new_member_list;
        members.addAll( new_member_list );
        for( int i = 0, n = members.size(); i < n; i++ )
        {
            ( (User) members.elementAt( i ) ).lock( this );
        }
        
        Collections.sort( members, comparator );
        
        for( int i = 0, n = members.size(); i < n; i++ )
        {
            ( (User) members.elementAt( i ) ).unlock( this );
        }
        
        info_manager.addMembers( this, members );
    }
    
    public void sortMembers()
    {
        Collections.sort( members, comparator );
    }
    
    public int getSortOrder()
    {
        return sort_order;
    }
    
    public boolean setSortOrder( int new_sort_order )
    {
        boolean success = false;
        
        if( Util.isValidSortOrder( new_sort_order ) )
        {
            sort_order = new_sort_order;
            comparator = new UserComparator( sort_order, this );
            success = true;
        }
        
        return success;
    }
    
    public void addMember( User user )
    {
        int insertion_point =
        (
            Collections.binarySearch(
                members,
                user,
                comparator
            )
            + 1
        ) * (-1);
        try
        {
            members.insertElementAt( user, insertion_point );
            info_manager.addMember( user, this, insertion_point );
        }
        catch( ArrayIndexOutOfBoundsException e )
        {
            display_manager.printlnDebug(
                "Bad insertion point for new member: "
                + Integer.toString( insertion_point )
            );
        }
    }
    
    public User removeMember( String nick )
    {
        User retval = null;
        User user;
        
        for( int i = 0, n = members.size(); i < n; i++ )
        {
            user = (User) members.elementAt( i );
            if( user.getNick().equals( nick ) )
            {
                retval = user;
                break;
            }
        }
        
        if( retval != null )
        {
            members.remove( retval );
            info_manager.removeMember( retval, this );
        }
        
        return retval;
    }
    
    /**
     * @return true iff the given nick matches the nick of a member of the channel
     */
    public boolean nickIsPresent( String nick )
    {
        boolean is_present = false;

        for( int i = 0, n = members.size(); i < n; i++ )
        {
            if( ((User) members.elementAt( i )).getNick().equals( nick ) )
            {
                is_present = true;
                break;
            }
        }
        return is_present;
    }
    
    public boolean isMember( User user )
    {
        return members.contains( user );
    }
    
    public String completeNick( String incomplete_nick, boolean decorated )
    {
        String completed_nick = incomplete_nick;
        
        User u;
        String nick;
        for( int i = 0, n = members.size(); i < n; i++ )
        {
            u = (User) members.elementAt( i );
            nick = u.getNick();
            if( nick.toLowerCase().startsWith( incomplete_nick.toLowerCase() ) )
            {
                if( decorated )
                {
                    completed_nick =
                        settings_manager.getString(
                            "/misc/nick completion prefix",
                            ""
                        )
                        + nick
                        + settings_manager.getString(
                            "/misc/nick completion suffix",
                            ": "
                        );
                }
                else
                {
                    completed_nick = nick;
                }
                break;
            }
        }
        
        return completed_nick;
    }
    
    public void acknowledgeUserChange( User user )
    {
        if( user != null )
        {
            int old_index = members.indexOf( user );
            if( old_index >= 0 )
            {
                sortMembers();
                int new_index = members.indexOf( user );
                info_manager.acknowledgeUserChange( this, user, new_index );

                if(
                    ( sort_order == SORT_TIME_SINCE_LAST_ASCENDING )
                    && ( old_index == new_index )
                    && ( old_index != 0 )
                )
                {
                    display_manager.printlnDebug(
                        "Warning: " + user.getNick()
                        + " position unchanged in "
                        + name
                        + " after sort ("
                        + Integer.toString( old_index ) + ")"
                    );
                }
            } // else, could be ChanServ or something doing something in the channel
        }
    }
    
    public User [] getMembers()
    {
        User [] retval = new User[ 0 ];
        return (User []) members.toArray( retval );
    }
}
