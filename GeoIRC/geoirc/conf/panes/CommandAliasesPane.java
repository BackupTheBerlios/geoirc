/*
 * CommandAliasesPane.java
 * 
 * Created on 24.08.2003
 */
package geoirc.conf.panes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import geoirc.CommandAlias;
import geoirc.GeoIRCConstants;
import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;

/**
 * @author netseeker aka Michael Manske
 */
public class CommandAliasesPane extends BaseSettingsPanel implements Storable, GeoIRCConstants
{
	LittleTableModel ltm = new LittleTableModel();
	
	/**
	 * @param settings
	 * @param valueRules
	 * @param name
	 */
	public CommandAliasesPane(
		XmlProcessable settings,
		GeoIRCDefaults valueRules,
		String name)
	{
		super(settings, valueRules, name);
		// TODO Auto-generated constructor stub
	}
	
	private void initComponents()
	{
		addComponent(new TitlePane("Command Aliases"), 0, 0, 10, 1, 0, 0);
		String path = "/command aliases/";
		int i = 0;
		String nodePath = path + String.valueOf(i) + "/";
		while ( settings_manager.nodeExists(nodePath) == true )
		{
			String alias = settings_manager.getString(nodePath + "alias", "");
			String expansion = settings_manager.getString(nodePath + "expansion", "");
			ltm.addCommandAlias(new CommandAlias(alias, expansion));
		}
	}
	
	private void initCellEditors()
	{
	}

	/* (non-Javadoc)
	 * @see geoirc.conf.Storable#saveData()
	 */
	public boolean saveData()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see geoirc.conf.Storable#hasErrors()
	 */
	public boolean hasErrors()
	{
		// TODO Auto-generated method stub
		return false;
	}

	class LittleTableModel extends AbstractTableModel
	{
		final String[] columnNames =
			{ "Alias", "Command", "IRC Command", "Executable", "Parameter" };
		List data = new ArrayList();

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount()
		{
			return data.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount()
		{
			return columnNames.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col)
		{
			CommandAlias ca = (CommandAlias)data.get(row);
			Object ret = null;
			
			switch(col)
			{
				case 0: ret = ca.getAlias();
					break;
				case 1: ret = ca.getCommand();
					break;
				case 2: ret = ca.getIRCCommand();
					break; 
				case 3: 
					break;
			}
			return ret;
		}
		
		public void addCommandAlias(CommandAlias ca)
		{
			data.add(ca);
			fireTableDataChanged();
		}
	}
}
