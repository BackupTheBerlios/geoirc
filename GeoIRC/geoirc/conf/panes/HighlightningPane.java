/*
 * HighlightningPane.java
 * 
 * Created on 21.08.2003
 */
package geoirc.conf.panes;

import geoirc.GeoIRCConstants;
import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.beans.Highlightning;
import geoirc.conf.beans.ValueRule;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * @author netseeker aka Michael Manske
 */
public class HighlightningPane extends BaseSettingsPanel implements Storable, GeoIRCConstants
{
	private LittleTableModel ltm = new LittleTableModel();
	private ValueRule colorRule;
	
	private JTable table;
	
	/**
	 * @param settings
	 * @param valueRules
	 * @param name
	 */
	public HighlightningPane(
		XmlProcessable settings,
		GeoIRCDefaults valueRules,
		String name)
	{
		super(settings, valueRules, name);
		colorRule = rules.getValueRule("COLOR");
		ltm.setData( getHighlightnings() );
		initComponents();
	}
	
	private void initComponents()
	{	
		addComponent(new TitlePane("Highlightning"), 0, 0, 10, 1, 0, 0);

		table = new JTable( ltm );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );		
		
		TableColumn sportColumn = table.getColumnModel().getColumn(3);
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("foreground");
		comboBox.addItem("background");
		sportColumn.setCellEditor(new DefaultCellEditor(comboBox));

		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;
        
				ListSelectionModel lsm =
					(ListSelectionModel)e.getSource();
				if (lsm.isSelectionEmpty()) {
					//no rows are selected
				} 
				else
				{
					int selectedRow = lsm.getMinSelectionIndex();					
				}
			}
		});

		addComponent(table, 0, 1, 5, 1, 0, 0);
	}
	
	private List getHighlightnings()
	{
		String path = "/gui/text windows/highlighting/";
		int i = 0;
		String node = path + String.valueOf(i) + "/";
		List data = new ArrayList();
		
		while(settings_manager.nodeExists(node) == true)
		{
			String filter = settings_manager.getString(node + "filter", "");
			String regexp = settings_manager.getString(node + "filter", "");
			String format = settings_manager.getString(node + "filter", colorRule.getValue().toString());
			data.add( new Highlightning(filter, regexp, format) );
			
			i++;
			node = path + String.valueOf(i) + "/";
		}	
		
		return data;	
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
		final String[] columnNames = { "Filter", "Regular Expression", "Color", "Color Type"};
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
			return getListValue(row, col);
		}
		
		public boolean isCellEditable(int row, int col) 
		{
			return true;	
		}

		public void setValueAt(Object value, int row, int col) 
		{
			setListValue(value, row, col);
			fireTableCellUpdated(row, col);
		}
		
		private void setListValue(Object value, int row, int col)
		{
			Highlightning hl = (Highlightning)data.get(row);
			switch ( col )
			{
				case 0:
					hl.setFilter((String)value);
					break;
				case 1:
					hl.setRegexp((String)value);
					break;
				case 2:
					hl.setColorString((String)value);
					break;
				case 3:
					hl.setColorPrefix((String)value);
					break;
			}
		}
		
		private Object getListValue(int row, int col)
		{
			Highlightning hl = (Highlightning)data.get(row);
			Object result = null;
			
			switch ( col )
			{
				case 0:
					result = hl.getFilter();
					break;
				case 1:
					result = hl.getRegexp();
					break;
				case 2:
					result = hl.getColorString();
					break;
				case 3:
					result = hl.getColorPrefix();
					break;
			}
			
			return result;
		}

		public void setData(List data)
		{
			this.data = data;
			fireTableDataChanged();
		}
	}
}
