/*
 * Channel.java
 *
 * Created on July 12, 2003, 6:37 PM
 */

package geoirc;

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
    
    // No default constructor
    private Channel() { }
    
    public Channel( Server server, String name )
    {
        this.server = server;
        this.name = name;
        topic = null;
        topic_setter = null;
        topic_set_date = null;
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
        return server.toString() + "/" + name;
    }
}

