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
public class User implements GeoIRCConstants
{
    protected String nick;
    protected String username;
    protected String host;
    protected String mode_flags;
    protected Channel channel;
    
    // No default constructor
    private User() { }

    public User( Channel channel, String nick_possibly_with_flags )
    {
        this.channel = channel;
        this.nick = nick_possibly_with_flags;
        username = null;
        host = null;
        mode_flags = "";
        
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
