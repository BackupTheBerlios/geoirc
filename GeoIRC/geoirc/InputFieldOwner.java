/*
 * InputFieldOwner.java
 *
 * Created on September 10, 2003, 3:09 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public interface InputFieldOwner
{
    void useInputField();
    void regularPaste( String text_to_paste );
    int getFloodAllowance();
    int getFloodDelay();
}
