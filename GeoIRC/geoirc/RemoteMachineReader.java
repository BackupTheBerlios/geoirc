/*
 * RemoteMachineReader.java
 *
 * Created on August 16, 2003, 4:29 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public abstract class RemoteMachineReader extends Thread
{
    protected abstract void interpretLine( String line );
    public abstract void run();
}
