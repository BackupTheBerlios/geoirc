package enigma.shells.commandline.commands;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import enigma.core.*;
import enigma.util.*;

/** 
 * Adds another entry to the end of the current <code>path</code> environment property.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class Addpath {
    /** 
     * Adds another entry to the end of the current <code>path</code> environment property.
     * There must be exactly one argument in the <code>args</code> array, the path to
     * add.
     */
    public void main(String[] args) {
        if (args.length == 0) {
            System.out.println(Util.msg(getClass(), "usage.info"));
            return;
        }
        else if (args.length > 1) {
            System.out.println(Util.msg(getClass(), "too.many.arguments", new Object[] { Arrays.asList(args) }));
            return;
        }

        Environment env = Enigma.getEnvironment();
        String newPath = env.getProperty("path") + ";" + args[0];
        env.setProperty("path", newPath);
    }
}
