/*
 * Channel.java
 * 
 * Created on 20.08.2003
 */
package geoirc.conf.beans;

/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 * TODO Add source documentation
 */
public class Channel
{
	private String name;
	private boolean autojoin = true;
	
	public Channel(String name)
	{
		this.name = name;
	}

	public Channel(String name, boolean autojoin)
	{
		this.name = name;
		this.autojoin = autojoin;
	}

	/**
	 * @return
	 */
	public boolean isAutojoin()
	{
		return autojoin;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param b
	 */
	public void setAutojoin(boolean b)
	{
		autojoin = b;
	}

	/**
	 * @param string
	 */
	public void setName(String string)
	{
		name = string;
	}

	public String toString()
	{
		return name;
	}
}
