/*
 * Highlightning.java
 * 
 * Created on 21.08.2003
 */
package geoirc.conf.beans;

import java.util.regex.Pattern;

/**
 * @author netseeker aka Michael Manske
 */
public class Highlightning
{
	private Pattern regexp;
	private String filter;
	private String format;

	/**
	 * 
	 */
	public Highlightning()
	{
	}
	
	public Highlightning(String filter, String regexp, String format)
	{
		setFilter(filter);
		setRegexp(regexp);
		setFormat(format);
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
	public String getFormat()
	{
		return format;
	}

	/**
	 * @return
	 */
	public String getColorString()
	{
		return format.substring(2);
	}
	
	public void setColorString(String value)
	{
		String str = getColorPrefix();
		setFormat(str + value);
	}

	/**
	 * @return
	 */
	public String getColorPrefix()
	{
		return format.substring(0, 2);
	}
	
	public void setColorPrefix(String value)
	{
		String str = getColorString();
		setFormat(value + str);
	}

	/**
	 * @return
	 */
	public String getRegexp()
	{
		return regexp.pattern();
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
	public void setFormat(String string)
	{
		format = string;
	}

	/**
	 * @param pattern
	 */
	public void setRegexp(String pattern)
	{
		regexp = Pattern.compile(pattern);
	}

}
