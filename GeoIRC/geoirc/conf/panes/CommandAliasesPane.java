/*
 * CommandAliasesPane.java
 * 
 * Created on 24.08.2003
 */
package geoirc.conf.panes;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

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
	private JTable table;
	
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
		initComponents();
	}
	
	private void initComponents()
	{
		table = new JTable(ltm);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//setting up table cell with combo box for commands
		TableColumn cmdColumn = table.getColumnModel().getColumn(1);
		JComboBox cmdCombo = new JComboBox();
		cmdCombo.addItem("none");
		for(int i = 0; i < CMDS.length; i++)
		{
			cmdCombo.addItem(CMDS[i]);
		}
		cmdColumn.setCellEditor(new DefaultCellEditor(cmdCombo));

		//setting up table cell with combo box for irc commands
		TableColumn ircCmdColumn = table.getColumnModel().getColumn(1);
		JComboBox ircCombo = new JComboBox();
		ircCombo.addItem("none");
		for(int i = 0; i < IRCMSGS.length; i++)
		{
			ircCombo.addItem(IRCMSGS[i]);
		}
		ircCmdColumn.setCellEditor(new DefaultCellEditor(ircCombo));
		
		addComponent(new TitlePane("Command Aliases"), 0, 0, 10, 1, 0, 0);
		String path = "/command aliases/";
		int i = 0;
		String nodePath = path + String.valueOf(i) + "/";
		while ( settings_manager.nodeExists(nodePath) )
		{
			String alias = settings_manager.getString(nodePath + "alias", "");
			String expansion = settings_manager.getString(nodePath + "expansion", "");
			ltm.addCommandAlias(new CommandAlias(alias, expansion));
			i++;
			nodePath = path + String.valueOf(i) + "/";
		}
		
		table.setPreferredScrollableViewportSize(new Dimension(500, 200));
		JScrollPane scroller = new JScrollPane(table);
		addComponent(scroller, 0, 1, 5, 1, 0, 0);
		
		addLayoutStopper(0,2);		
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
			{ "Alias", "Command", "IRC Command", "Parameter" };
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
		
		public Class getColumnClass(int c)
		{
			return String.class;
		}

		public String getColumnName(int col) {
			 return columnNames[col];
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
				case 3: ret = ca.getParamString();
					break;
			}
			return ret;
		}
		
		public void addCommandAlias(CommandAlias ca)
		{
			data.add(ca);
			fireTableDataChanged();
		}
		
		public boolean isCellEditable(int row, int col)
		{
			return true;
		}
		
	}
}
