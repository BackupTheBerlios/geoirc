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
 */
public class JBoolRegExTextField extends JValidatingTextField {

    /**
     * 
     */
    public JBoolRegExTextField() {
        super();
    }

    /**
     * @param value
     */
    public JBoolRegExTextField(String value) {
        super(".+", value);
    }

    /**
     * @param value
     * @param width
     */
    public JBoolRegExTextField(String value, int width) {
        super(".+", value, width);
    }

    protected void validateText() {
        String t = getText();
        boolean valid = false;

        if (!isEmpty()) {
            try {
                BoolExpEvaluator.evaluate(t, pattern.pattern());
                valid = true;
            }
            catch (BadExpressionException e) {
                valid = false;
            }
        }
        setTextValid(valid);
    }
}
