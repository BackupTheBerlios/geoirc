/*
 * User.java
 *
 * Created on July 12, 2003, 7:59 PM
 */

package geoirc;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author  Pistos
 */
public class User
    implements GeoIRCConstants
{
    protected String nick;
    protected String username;
    protected String host;
    protected Hashtable channel_flag_sets;
    protected Date time_of_last_activity;
    protected Object lock_owner;
    
    // No default constructor
    private User() { }
    
    public User( Channel originating_channel, String nick_possibly_with_flags )
    {
        username = null;
        host = null;
        channel_flag_sets = new Hashtable();
        lock_owner = null;
        noteActivity();
        nick = parseModeChars( originating_channel, nick_possibly_with_flags );
    }
    
    /**
     * @return the nickname without the mode chars in front
     */
    protected String parseModeChars( Channel channel, String nick_with_chars )
    {
        boolean mode_char_found;
        String nick_without_chars = nick_with_chars;
        do
        {
            mode_char_found = true;
            
            switch( nick_without_chars.charAt( 0 ) )
            {
                case NAMLIST_OP_CHAR:
                    addModeFlag( channel, MODE_OP );
                    nick_without_chars = nick_without_chars.substring( 1 );
                    break;
                case NAMLIST_HALFOP_CHAR:
                    addModeFlag( channel, MODE_HALFOP );
                    nick_without_chars = nick_without_chars.substring( 1 );
                    break;
                case NAMLIST_VOICE_CHAR:
                    addModeFlag( channel, MODE_VOICE );
                    nick_without_chars = nick_without_chars.substring( 1 );
                    break;
                default:
                    mode_char_found = false;
                    break;
            }
        } while( mode_char_found );
        
        return nick_without_chars;
    }
    
    public void addModeChars( Channel channel, String nick_with_chars )
    {
        if( nick_with_chars.endsWith( nick ) )
        {
            parseModeChars( channel, nick_with_chars );
        }
        else
        {
            System.err.println(
                "Internal error: Attempted to User.addModeChars() with non-matching nick."
            );
        }
    }
    
    public String getNick()
    {
        return nick;
    }

    public void setNick( String new_nick )
    {
        nick = new_nick;
    }
    
    public String toString()
    {
        return nick;
    }
    
    public boolean equals( Object o )
    {
        boolean equal = false;
        
        if( o instanceof User )
        {
            User other = (User) o;
            
            return( other.getNick().equals( nick ) );
        }
        
        return equal;
    }
    
    public int hashCode()
    {
        return nick.hashCode();
    }
    
    public void noteActivity()
    {
        Date date = new Date();
        lock( this );
        time_of_last_activity = date;
        unlock( this );
    }
    
    public Date getTimeOfLastActivity()
    {
        return time_of_last_activity;
    }
    
    public synchronized void lock( Object requester )
    {
        while( lock_owner != null )
        {
            try
            {
                wait();
            } catch( InterruptedException e ) { }
        }
        
        lock_owner = requester;
    }
    
    public synchronized boolean unlock( Object requester )
    {
        boolean retval = false;
        
        if( requester == lock_owner )
        {
            lock_owner = null;
            notifyAll();
            retval = true;
        }
        
        return retval;
    }
    
    public void addModeFlag( Channel channel, String mode_flag )
    {
        HashSet mode_flags = (HashSet) channel_flag_sets.get( channel );
        if( mode_flags == null )
        {
            mode_flags = new HashSet();
            channel_flag_sets.put( channel, mode_flags );
        }
        mode_flags.add( mode_flag );
    }
    
    public void removeModeFlag( Channel channel, String mode_flag )
    {
        HashSet mode_flags = (HashSet) channel_flag_sets.get( channel );
        if( mode_flags != null )
        {
            mode_flags.remove( mode_flag );
        }
    }
    
    public boolean hasModeFlag( Channel channel, String mode_flag )
    {
        boolean retval = false;
        HashSet mode_flags = (HashSet) channel_flag_sets.get( channel );
        if( mode_flags != null )
        {
            retval = mode_flags.contains( mode_flag );            
        }
        
        return retval;
    }
}
