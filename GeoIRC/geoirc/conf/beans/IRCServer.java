/*
 * IRCServer.java
 * 
 * Created on 19.08.2003
 */
package geoirc.conf.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author netseeker aka Michael Manske
 */
public class IRCServer
{
	private String type;
	private String hostname;
	private int port;
	private List channels = new ArrayList();
		 
	public IRCServer(String type, String hostname, int port)
	{
		this.type = type;
		this.hostname = hostname;
		this.port = port;
	}
		
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
		
	public void setPort(int port)
	{
		this.port = port;
	}
		
	public void addChannel(Channel channel)
	{
		if(channels.contains(channel) == false)
			this.channels.add(channel);
	}
		
	public void setChannel(int pos, Channel channel)
	{
		channels.set(pos, channel);
	}
		
	public boolean removeChannel(Channel channel)
	{
		return channels.remove(channel);
	}
		
	public boolean removeChannel(int index)
	{		
		return (channels.remove(index) != null);
	}
		
	public List getChannels()
	{
		return this.channels;
	}

	public String getHostname()
	{
		return hostname;
	}

	public int getPort()
	{
		return port;
	}

	public String getType()
	{
		return type;
	}

}
