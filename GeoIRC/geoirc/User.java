/*
 * User.java
 *
 * Created on July 12, 2003, 7:59 PM
 */

package geoirc;

import java.util.Date;
import java.util.Vector;

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
    protected String mode_flags;
    protected Date time_of_last_activity;
    protected Object lock_owner;
    
    // No default constructor
    private User() { }
    
    public User( String nick_possibly_with_flags )
    {
        this.nick = nick_possibly_with_flags;
        username = null;
        host = null;
        mode_flags = "";
        lock_owner = null;
        
        boolean mode_char_found;
        do
        {
            mode_char_found = false;
            
            switch( nick.charAt( 0 ) )
            {
                case NAMLIST_OP_CHAR:
                    mode_flags += MODE_OP;
                    nick = nick.substring( 1 );
                    mode_char_found = true;
                    break;
                case NAMLIST_VOICE_CHAR:
                    mode_flags += MODE_VOICE;
                    nick = nick.substring( 1 );
                    mode_char_found = true;
                    break;
            }
        } while( mode_char_found );
    }
    
    public boolean isInitialized()
    {
        return(
            ( username == null )
            || ( host == null )
            || ( mode_flags == null )
        );
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
}
