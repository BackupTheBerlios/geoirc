/*
 * JRegExTextField.java
 * 
 * Created on 23.08.2003
 */
package geoirc.util;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 */
public class JRegExTextField extends JValidatingTextField
{
	/**
	 * 
	 */
	public JRegExTextField()
	{
		super();
	}

	/**
	 * @param regex
	 * @param value
	 */
	public JRegExTextField(String regex)
	{
		super(".+", regex);
	}

	/**
	 * @param regex
	 * @param value
	 * @param width
	 */
	public JRegExTextField(String regex, int width)
	{
		super(".+", regex, width);		
	}

	protected void validateText()
	{
		String t = getText();
		boolean valid = true;
		
		if(isEmpty())
			valid = false;
		else
		{
			try
			{
				Pattern.compile(t);
			}
			catch( PatternSyntaxException e )
			{
				valid = false;
			}
		}		
		setTextValid(valid);
	}
	
	public void setPattern(String regex)
	{
		setText(regex);
	}
	

}
