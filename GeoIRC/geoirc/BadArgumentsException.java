/*
 * BadArgumentsException.java
 *
 * Created on July 3, 2003, 12:22 PM
 */

package geoirc;

/**
 *
 * @author  Alex
 */
public class BadArgumentsException extends Exception
{
    
    public BadArgumentsException()
    {
        super();
    }
    
    public BadArgumentsException( String message )
    {
        super( message );
    }
    
}
