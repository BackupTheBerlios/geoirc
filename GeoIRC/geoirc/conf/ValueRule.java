/*
 * DefaultValue.java
 * 
 * Created on 07.08.2003
 */
package geoirc.conf;

import geoirc.util.TypeConverter;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;


/**
 * Simple wrapper for configured default value rules
 * @author netseeker aka Michael Manske
 */
public class ValueRule
{
	protected String name = null;
	protected String value = null;
	protected String pattern = null;
	protected String javaType = null;

	/**
	 * Creates a new preconfigured instance of ValueRule<br>
	 * with values of the given settings node 
	 * @param valueNode xml configuration node
	 */
	public ValueRule(Preferences valueNode)
	{		
		this(valueNode.name(), valueNode.get("pattern", ""), valueNode.get("javaType", TypeConverter.STRING), valueNode.get("default", null));
	}

	/**
	 * Creates a new preconfigured instance of ValueRule
	 * @param name name of this value
	 * @param pattern regex pattern to match
	 * @param value value of this object
	 */
	public ValueRule(String name, String pattern, String javaType, String value)
	{
		setName(name);
		setPattern(pattern);
		setJavaType(javaType);
		setValue(value);
	}

	/**
	 * @return
	 */
	public Object getValue()
	{
		return TypeConverter.convert(value, javaType);
	}
	
	/**
	 * @param object
	 */
	public void setValue(String value)
	{
		this.value = value;
	}	
	
	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param string
	 */
	public void setName(String string)
	{
		name = string;
	}

	/**
	 * @return
	 */
	public String getPattern()
	{
		return pattern;
	}

	/**
	 * @param pattern
	 */
	public void setPattern(String pattern)
	{
		//throws an exception if the pattern isn't well formed
		Pattern.compile(pattern);
		
		this.pattern = pattern;
	}

	/**
	 * @return
	 */
	public String getJavaType()
	{
		return javaType;
	}

	/**
	 * @param string
	 */
	public void setJavaType(String string)
	{
		javaType = string;
	}	
}
