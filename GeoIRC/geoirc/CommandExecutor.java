/*
 * CommandExecutor.java
 *
 * Created on June 26, 2003, 10:31 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public interface CommandExecutor
{
    public static final int EXEC_SUCCESS = 0;
    public static final int EXEC_GENERAL_FAILURE = 1;
    public static final int EXEC_BAD_COMMAND = 2;
    public static final int EXEC_BAD_ARG = 3;
    public static final int EXEC_INSUFFICIENT_ARGS = 4;
        
    public int execute( String command );
}
