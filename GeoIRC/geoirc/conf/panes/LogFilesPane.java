/*
 * LogPane.java
 * 
 * Created on 24.09.2003
 */
package geoirc.conf.panes;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.JValidatingTable;
import geoirc.conf.SettingsPeer;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.beans.Log;
import geoirc.conf.beans.ValueRule;
import geoirc.util.JBoolRegExTextField;
import geoirc.util.JRegExTextField;
import geoirc.util.JValidatingTextField;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * @author netseeker aka Michael Manske
 */
public class LogFilesPane extends BaseSettingsPanel implements Storable
{
    private JValidatingTable table;
    private LittleTableModel ltm = new LittleTableModel();
    private JButton delButton = new JButton("delete");
    private JButton addButton = new JButton("new");
    private String default_log_dir;
    private ValueRule log_file_rule;

    
    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public LogFilesPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
    {
        super(settings, valueRules, name);
        ValueRule path_rule = rules.getValueRule("DIRECTORY");
        log_file_rule = rules.getValueRule("LOG_FILE");
        default_log_dir = settings_manager.getString("/logs/default log path", path_rule.getValue().toString());
    }

    /* (non-Javadoc)
     * @see geoirc.conf.BaseSettingsPanel#initialize()
     */
    public void initialize()
    {                                                      
        addComponent(new TitlePane("Logfiles"), 0, 0, 5, 1, 0, 0);        
        table = new JValidatingTable(ltm, validation_listener);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(18);
        ltm.setData( SettingsPeer.loadLogs(settings_manager, rules) );

        JBoolRegExTextField valueField = new JBoolRegExTextField(null);
        table.setValidatingCellEditor(valueField, 0);

        JRegExTextField patternField = new JRegExTextField(null);
        //table.setValidatingCellEditor(patternField, 1);
        table.setRegExpTestingCellEditor(patternField, 1);
        
        JValidatingTextField pathField = new JValidatingTextField(log_file_rule.getPattern(), null, null);        
        table.setValidatingCellEditor(pathField, 2);        
        
        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);

        JScrollPane scroller = new JScrollPane(table);
        addComponent(scroller, 0, 1, 5, 1, 0, 0);

        addComponent(delButton, 0, 2, 2, 1, 0, 0);
        addComponent(addButton, 4, 2, 1, 1, 0, 0, GridBagConstraints.NORTHEAST);

        delButton.setEnabled(false);
        delButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                int pos = table.getSelectedRow();
                ltm.delRow(pos);
            }
        });

        addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                ltm.addRow(new Log("new log", ".*", default_log_dir + "/" + log_file_rule.getValue()));
            }
        });
        
        addHorizontalLayoutStopper(7, 2);
        addLayoutStopper(0, 3);
        
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                //Ignore extra messages.
                if (e.getValueIsAdjusting())
                    return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                delButton.setEnabled(!lsm.isSelectionEmpty());
            }
        });                        
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData()
    {
        List list = ltm.getData();
        String path = "/logs/";
        String node;
        settings_manager.removeNode(path);
        int a = 0;

        for (int i = 0; i < list.size(); i++)
        {
            node = path + String.valueOf(a) + "/";
            Log log = (Log)list.get(i);

            if (log.getFilter().length() > 0)
            {
                settings_manager.setString(node + "filter", log.getFilter());
                settings_manager.setString(node + "regexp", log.getRegexp());
                settings_manager.setString(node + "file", log.getFilter());
                a++;
            }
        }

        return true;
    }
    
    class LittleTableModel extends AbstractTableModel
    {
        final String[] columnNames = { "Filter", "Regular Expression", "File" };
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

        public String getColumnName(int col)
        {
            return columnNames[col];
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
            Log log = (Log)data.get(row);
            switch (col)
            {
                case 0 :
                    log.setFilter((String)value);
                    break;
                case 1 :
                    log.setRegexp((String)value);
                    break;
                case 2 :
                    log.setFile((String)value);
                    break;
            }
        }

        private Object getListValue(int row, int col)
        {
            Log log = (Log)data.get(row);
            Object result = null;

            switch (col)
            {
                case 0 :
                    result = log.getFilter();
                    break;
                case 1 :
                    result = log.getRegexp();
                    break;
                case 2 :
                    result = log.getFile();
                    break;
            }

            return result;
        }

        public void setData(List data)
        {
            this.data = data;
            fireTableDataChanged();
        }

        public List getData()
        {
            return this.data;
        }

        public void addRow(Object row)
        {
            data.add(row);
            fireTableDataChanged();
        }

        public void delRow(int row)
        {
            data.remove(row);
            fireTableDataChanged();
        }
        
        public Object getRow(int row)
        {
            return data.get(row);
        }
        
        public void setRow(int row, Object rowdata)
        {
            data.set(row, rowdata);
            fireTableDataChanged();            
        }
    }
    

}
