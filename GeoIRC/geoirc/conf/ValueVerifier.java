/*
 * ValueVerifier.java
 * 
 * Created on 08.08.2003
 */
package geoirc.conf;


import java.util.regex.Pattern;

/**
 * Simple value validation, mainly based on regex patterns
 * 
 * @author netseeker aka Michael Manske
 */
public class ValueVerifier
{
	/**
	 * @param value
	 * @param rule
	 * @return
	 */
	public static boolean check(Object value, Verifiable rule)
	{		
		String pattern = rule.getPattern();
		String javaType = rule.getJavaType();
		 
		if(checkType(value, javaType) == false)
			return false;
				
		return checkPattern(value, pattern);
	}
	
	/**
	 * @param value
	 * @param type
	 * @return
	 */
	public static boolean checkType(Object value, String type)
	{
		return value.getClass().getName().equalsIgnoreCase(type) ? true : false;
	}
	
	/**
	 * @param value
	 * @param regex
	 * @return
	 */
	public static boolean checkPattern(Object value, String regex)
	{
		return Pattern.matches(regex, value.toString());
	}
}
