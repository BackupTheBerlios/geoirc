/*
 * Channel.java
 *
 * Created on July 12, 2003, 6:37 PM
 */

package geoirc;

import geoirc.util.Util;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class Channel
{
    protected String name;
    protected String topic;
    protected String topic_setter;
    protected Date topic_set_date; 
    protected Vector members;
    protected Server server;
    protected InfoManager info_manager;
    protected SettingsManager settings_manager;
    
    // No default constructor
    private Channel() { }
    
    public Channel(
        Server server,
        String name,
        InfoManager info_manager,
        SettingsManager settings_manager
    )
    {
        this.server = server;
        this.name = name;
        this.info_manager = info_manager;
        this.settings_manager = settings_manager;
        topic = null;
        topic_setter = null;
        topic_set_date = null;
        
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
        info_manager.removeChannel( this );
        /*
        if( new_member_list == null )
        {
            members = new Vector();
        }
        else
         */
        {
            members = new_member_list;
        }
        info_manager.addChannel( this );
        
        User user;
        for( int i = 0, n = members.size(); i < n; i++ )
        {
            user = (User) members.elementAt( i );
            info_manager.addMember( user, this );
        }
    }
    
    public void addMember( User user )
    {
        members.add( user );
        info_manager.addMember( user, this );
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
        User u;
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
}
