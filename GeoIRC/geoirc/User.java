/*
 * User.java
 *
 * Created on July 12, 2003, 7:59 PM
 */

package geoirc;

import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class User
{
    protected String nick;
    protected String username;
    protected String host;
    protected Vector mode_flags;
    protected Channel channel;
    
    // No default constructor
    private User() { }

    public User( Channel channel, String nick )
    {
        this.channel = channel;
        this.nick = nick;
        username = null;
        host = null;
        mode_flags = null;
    }
    
    public boolean isInitialized()
    {
        return(
            ( username == null )
            || ( host == null )
            || ( mode_flags == null )
        );
    }
    
    public Channel getChannel()
    {
        return channel;
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
}
