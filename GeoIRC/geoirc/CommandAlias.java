/*
 * CommandAlias.java
 *
 * Created on July 9, 2003, 5:35 PM
 */

package geoirc;

import geoirc.util.Util;

/**
 * Argument indices are 1-based, not 0-based.
 * @author  Pistos
 */
public class CommandAlias implements GeoIRCConstants
{
    protected String alias;
    protected String expansion;
    
    private CommandAlias() { }

    public CommandAlias( String alias, String expansion )
    {
        this.alias = alias;
        this.expansion = expansion;
    }
    
    public String expand( String command_line, VariableManager variable_manager )
    {
        String [] tokens = Util.tokensToArray( command_line );
        String retval = command_line;
        
        if( tokens[ 0 ].equals( alias ) )
        {
            retval = expansion;
            String i_str;
            for( int i = 1; i < tokens.length; i++ )
            {
                i_str = Integer.toString( i );
                
                // Single argument substitutions.
                
                retval = retval.replaceAll(
                    ALIAS_ARG_CHAR + i_str,
                    tokens[ i ]
                );
                
                // "Rest of line" substitutions.
                
                retval = retval.replaceAll(
                    ALIAS_ARG_REST_CHAR + i_str,
                    Util.stringArrayToString( tokens, i )
                );
                
            }
            
            // Missing arguments are just replaced with empty strings.
            retval = retval.replaceAll(
                ALIAS_ARG_CHAR + "\\d+",
                ""
            );
            retval = retval.replaceAll(
                ALIAS_ARG_REST_CHAR + "\\d+",
                ""
            );
            
            // Variable substitution
            //retval = variable_manager.replaceAll( retval );
            // (Already handled now in the GeoIRC class)
        }
        
        return retval;
    }
    
    public String getAlias()
    {
        return alias;
    }
    
    public void setAlias(String alias)
    {
        this.alias = alias;
    }
    
    public String getExpansion()
    {
        return expansion;
    }
    
    public void setExpansion(String expansion)
    {
        this.expansion = expansion;
    }
    
    /**
     * Tries to identify the used command within expansion
	 * @return the used command
	 */
	public String getCommand()
    {
    	for ( int i = 0; i < CMDS.length; i++ )
    	{
    		if( expansion.startsWith(CMDS[i] + " ") == true)
    		{
    			return CMDS[i];
    		}
    	}
    	
    	return null;
    }
    
    public void setCommand(String command)
    {
        int pos = 0;
        
        for ( int i = 0; i < CMDS.length; i++ )
        {
            if( expansion.startsWith(CMDS[i] + " ") == true)
            {
                pos = CMDS[i].length() + 1;
                break;
            }
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append(command);
        sb.append(" ");
        sb.append(expansion.substring(pos));
        
        expansion = sb.toString();
    }
    
	/**
	 * Tries to identify the used parameter list within expansion
	 * @return the used parameter list as string
	 */
    public String getParamString()
    {
        String str = expansion;
		
		for ( int i = 0; i < CMDS.length; i++ )
		{
			if( expansion.startsWith(CMDS[i] + " ") == true)
			{
                str = expansion.substring(CMDS[i].length() + 1);
                break;
			}
		}
		for ( int i = 0; i < IRC_CMDS.length; i++ )
		{
			int pos = str.indexOf(IRC_CMDS[i] + " "); 
            if( pos != -1)
			{				
                str = str.substring(pos + IRC_CMDS[i].length());
                break;
			}
		}
        
        if(str.startsWith(" "))
            str = str.substring(1);		
	            
		return str;
    }
    
    public void setParamString(String params)
    {
        String str = getParamString();
        int pos = expansion.indexOf(str);
        StringBuffer sb = new StringBuffer();
        sb.append(expansion.substring(0, pos));
        sb.append(params);
        
        expansion = sb.toString();
    }
    
    /**
     * Tries to identify the used IRC Command within expansion
	 * @return the used irc command as string or null if no irc command is used
	 */
	public String getIRCCommand()    
    {    	
    	for ( int i = 0; i < IRC_CMDS.length; i++ )
    	{
    		if(expansion.indexOf(IRC_CMDS[i] + " ") != -1)
    		{
    			return IRC_CMDS[i];
    		}
    	}
    	
    	return null;
    }
    
    public void setIRCCommand(String command)
    {
        StringBuffer sb = new StringBuffer();
        String str = getIRCCommand();
        
        if(str != null)
        {
            sb.append(expansion.substring(0, expansion.lastIndexOf(str)));
            sb.append(" ");
        }
        else
        {
            str = getCommand();
            if(str != null)
            {
                sb.append(str);
                sb.append(" ");                 
            }
        }        
        
        sb.append(command);
        sb.append(" ");
        sb.append(getParamString());
        expansion = sb.toString();        
    }
        
    
    /**
     * Checks whether this command alias uses a irc command or not
	 * @return true if a irc command is used otherwise false
	 */
	public boolean usesIRCCommand()
    {
    	return (getIRCCommand() != null);    		
    }
}
