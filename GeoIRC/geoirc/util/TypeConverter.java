/*
 * TypeConverter.java
 * 
 * Created on 07.08.2003
 */
package geoirc.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author netseeker aka Michael Manske
 */
public class TypeConverter
{
	//supported object data types
	public static final String BYTE			= Byte.class.getName();
	public static final String SHORT	 	= Short.class.getName();
	public static final String INTEGER 		= Integer.class.getName();
	public static final String BIGDECIMAL 	= BigDecimal.class.getName();
	public static final String FLOAT 		= Float.class.getName();
	public static final String DOUBLE	 	= Double.class.getName();
	public static final String STRING 		= String.class.getName();
	public static final String DATE 		= Date.class.getName();
	public static final String BOOLEAN		= Boolean.class.getName(); 
	
	/**
	 * Converts a string value to its representation in another type<br>
	 * If converation fails an IllegalArgumentException will be thrown.
	 * @param source String value to convert
	 * @param targetType class name of target type
	 * @return the target type representation of source
	 * @throws IllegalArgumentException
	 */
	public static Object convert(String source, String targetType) throws IllegalArgumentException
	{
		Object target = null;
		
		if(targetType.equals(STRING))
			return source;
		else if(targetType.equals(BYTE))
		{
			target = new Byte(Byte.parseByte(source));
		}
		else if(targetType.equals(SHORT))
		{
			target = new Short(Short.parseShort(source));
		}
		else if(targetType.equals(INTEGER))
		{
			target = new Integer(Integer.parseInt(source));
		}
		else if(targetType.equals(BIGDECIMAL))
		{
			target = new BigDecimal(source);
		}
		else if(targetType.equals(FLOAT))
		{
			target = new Float(Float.parseFloat(source));
		}
		else if(targetType.equals(DOUBLE))
		{
			target = new Double(Double.parseDouble(source));
		}
		else if(targetType.equals(DATE))
		{
			DateFormat formatter = DateFormat.getInstance();
			try
			{
				target = formatter.parse(source);
			}
			catch (ParseException e)
			{
				throw new IllegalArgumentException(e.getLocalizedMessage());
			}
		}
		else if(targetType.equals(BOOLEAN))
		{
			target = new Boolean(source);
		}
		else
			throw new IllegalArgumentException("Unknown target type '" + targetType + "'");
		
		return target;
	}

	/**
	 * Converts a string value to its representation in another type<br>
	 * if conversion is not possible a default value will be returned
	 * @param source String value to convert
	 * @param targetType class name of target type
	 * @param defaultValue value to return if conversion fails
	 * @return either the target type representation of source or defaultValue
	 */
	public static Object convert(String source, String targetType, Object defaultValue)
	{
		Object obj = null;
		try
		{
			obj = convert(source, targetType);		
		}
		catch(IllegalArgumentException e)
		{
			return defaultValue;
		}
		
		if(obj == null)
			return defaultValue;
			
		return obj;
	}
}
