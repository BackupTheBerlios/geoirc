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
    
    // No default constructor
    private Channel() { }
    
    public Channel( Server server, String name, InfoManager info_manager )
    {
        this.server = server;
        this.name = name;
        this.info_manager = info_manager;
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
    public void setChannelMembership( String namlist )
    {
        // Prefixes:
        // +   voiced
        // @   channel operator
        User user;
        String [] nicks = Util.tokensToArray( namlist );
        for( int i = 0; i < nicks.length; i++ )
        {
            user = new User( this, nicks[ i ] );
            members.add( user );
            info_manager.addMember( user );
        }
    }
    
    public void addMember( String nick )
    {
        User user = new User( this, nick );
        members.add( user );
        info_manager.addMember( user );
    }
    
    public void removeMember( String nick )
    {
        int n = members.size();
        User u;
        for( int i = 0; i < n; i++ )
        {
            u = (User) members.elementAt( i );
            if( u.getNick().equals( nick ) )
            {
                members.remove( u );
                info_manager.removeMember( u );
                break;
            }
        }
    }
    
    public boolean acknowledgeNickChange( String old_nick, String new_nick )
    {
        boolean changed = false;
        int n = members.size();
        User u;
        for( int i = 0; i < n; i++ )
        {
            u = (User) members.elementAt( i );
            if( u.getNick().equals( old_nick ) )
            {
                u.setNick( new_nick );
                changed = true;
                break;
            }
        }
        
        return changed;
    }
    
    public User getUserByNick( String nick )
    {
        User retval = null;
        
        User u;
        int n = members.size();
        for( int i = 0; i < n; i++ )
        {
            u = (User) members.elementAt( i );
            if( u.getNick().equals( nick ) )
            {
                retval = u;
                break;
            }
        }
        
        return retval;
    }
    
    public String completeNick( String incomplete_nick )
    {
        String completed_nick = incomplete_nick;
        
        User u;
        String nick;
        for( int i = 0, n = members.size(); i < n; i++ )
        {
            u = (User) members.elementAt( i );
            nick = u.getNick();
            if( nick.startsWith( incomplete_nick ) )
            {
                completed_nick = nick;
                break;
            }
        }
        
        return completed_nick;
    }
}
