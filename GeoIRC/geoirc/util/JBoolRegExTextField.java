/*
 * JRegExTextField.java
 * 
 * Created on 23.08.2003
 */
package geoirc.util;

import geoirc.conf.ValidationListener;

/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 */
public class JBoolRegExTextField extends JValidatingTextField
{

    /**
     * 
     */
    public JBoolRegExTextField(ValidationListener validation_listener)
    {
        super(validation_listener);
    }

    /**
     * @param value
     */
    public JBoolRegExTextField(String value, ValidationListener validation_listener)
    {
        super(".*", value, validation_listener);
    }

    /**
     * @param value
     * @param width
     */
    public JBoolRegExTextField(String value, ValidationListener validation_listener, int width)
    {
        super(".*", value, validation_listener, width);
    }

    protected void validateText()
    {
        String t = getText();
        boolean valid = false;

        try
        {
            BoolExpEvaluator.evaluate(t, pattern.pattern());
            valid = true;
        }
        catch (BadExpressionException e)
        {
            valid = false;
        }

        setTextValid(valid);
    }
}
