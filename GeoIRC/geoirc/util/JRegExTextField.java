/*
 * JRegExTextField.java
 * 
 * Created on 23.08.2003
 */
package geoirc.util;

import geoirc.conf.ValidationListener;

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
    public JRegExTextField(ValidationListener validation_listener)
    {
        super( validation_listener );
    }

    /**
     * @param regex
     * @param value
     */
    public JRegExTextField(String regex, ValidationListener validation_listener)
    {
        super(".*", regex, validation_listener);
    }

    /**
     * @param regex
     * @param value
     * @param width
     */
    public JRegExTextField(String regex, ValidationListener validation_listener, int width)
    {
        super(".*", regex, validation_listener, width);
    }

    protected void validateText()
    {
        String t = getText();
        boolean valid = true;

        try
        {
            Pattern.compile( t, Pattern.DOTALL );
        }
        catch (PatternSyntaxException e)
        {
            valid = false;
        }

        setTextValid( valid );
    }

    public void setPattern(String regex)
    {
        setText(regex);
    }

}
