package enigma.core;

import java.io.*;

/** 
 * Thrown to indicate that a new Java virtual machine could not be instantiated with the
 * specified configuration.
 *
 *@status.experimental
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class VirtualMachineInstantiationException extends Exception {
    public VirtualMachineInstantiationException(String detail) {
        super(detail);
    }
}