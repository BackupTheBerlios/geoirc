/*
 * JRegExTextField.java
 * 
 * Created on 23.08.2003
 */
package geoirc.util;


/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 * TODO Add source documentation
 */
public class JBoolRegExTextField extends JValidatingTextField {

    /**
     * 
     */
    public JBoolRegExTextField() {
        super();
    }

    /**
     * @param regex
     * @param value
     */
    public JBoolRegExTextField(String regex) {
        super(regex, "");
    }

    /**
     * @param regex
     * @param value
     */
    public JBoolRegExTextField(String regex, String value) {
        super(regex, value);
    }

    /**
     * @param regex
     * @param value
     * @param width
     */
    public JBoolRegExTextField(String regex, String value, int width) {
        super(regex, value, width);
    }

    /**
     * @param regex
     * @param value
     * @param width
     */
    public JBoolRegExTextField(String regex, int width) {
        super(regex, "", width);
    }

    protected void validateText() {
        String t = getText();
        boolean valid = true;

        if (!isEmpty()) {
            try {
                BoolExpEvaluator.evaluate(t, pattern.pattern());
            }
            catch (BadExpressionException e) {
                valid = false;
            }
        }
        setTextValid(valid);
    }
}
