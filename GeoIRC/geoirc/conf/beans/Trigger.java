/*
 * Trigger.java
 * 
 * Created on 21.08.2003
 */
package geoirc.conf.beans;


/**
 * @author netseeker aka Michael Manske
 */
public class Trigger
{
	private String regexp;
	private String filter;
	private String command;

	/**
	 * 
	 */
	public Trigger()
	{
	}
	
	public Trigger(String filter, String regexp, String command)
	{
		setFilter(filter);
		setRegexp(regexp);
		setCommand(command);
	}

	/**
	 * @return
	 */
	public String getFilter()
	{
		return filter;
	}

	/**
	 * @return
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * @return
	 */
	public String getRegexp()
	{
		return regexp;
	}

	/**
	 * @param string
	 */
	public void setFilter(String string)
	{
		filter = string;
	}

	/**
	 * @param string
	 */
	public void setCommand(String string)
	{
        command = string;
	}

	/**
	 * @param pattern
	 */
	public void setRegexp(String pattern)
	{
		regexp = pattern;
	}
}
