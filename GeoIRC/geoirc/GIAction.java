/*
 * GIAction.java
 *
 * Created on July 2, 2003, 4:20 PM
 */

package geoirc;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author  Pistos
 */
public class GIAction extends AbstractAction
{
    
    protected String command;
    protected CommandExecutor executor;
    
    // No default constructor
    private GIAction() { }
    
    public GIAction( String command, CommandExecutor executor )
    {
        super();
        this.command = command;
        this.executor = executor;
    }
    
    public void actionPerformed( ActionEvent e )
    {
        executor.execute( command );
    }
    
}
