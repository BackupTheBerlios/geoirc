/*
 * TriggerPane.java
 * 
 * Created on 18.09.2003
 */
package geoirc.conf.panes;

import geoirc.GeoIRCConstants;
import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.JValidatingTable;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.ValidationListener;
import geoirc.conf.beans.Trigger;
import geoirc.util.JBoolRegExTextField;
import geoirc.util.JRegExTextField;
import geoirc.util.JValidatingTextField;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * @author netseeker aka Michael Manske
 */
public class TriggerPane extends BaseSettingsPanel implements Storable, GeoIRCConstants
{
    private LittleTableModel ltm = new LittleTableModel();

    private JValidatingTable table;
    private JButton addButton;
    private JButton delButton;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public TriggerPane(XmlProcessable settings, GeoIRCDefaults valueRules, ValidationListener validationListener, String name)
    {
        super(settings, valueRules, validationListener, name);
    }

    public void initialize()
    {

        addComponent(new TitlePane("Trigger Settings"), 0, 0, 3, 1, 0, 0);

        table = new JValidatingTable( ltm, validation_listener );
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(18);
        ltm.setData(getTriggers());

        final JBoolRegExTextField valueField = new JBoolRegExTextField( null );
        valueField.setToolTipText("boolean regular expression");
        table.setValidatingCellEditor(valueField, 0);

        final JRegExTextField patternField = new JRegExTextField( null );
        patternField.setToolTipText("any regular expression");
        table.setValidatingCellEditor(patternField, 1);

        final JValidatingTextField commandField = new JValidatingTextField( null );
        patternField.setToolTipText("any available command");
        table.setValidatingCellEditor(commandField, 2);

        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
        table.getColumnModel().getColumn(0).setPreferredWidth(175);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(175);

        JScrollPane scroller = new JScrollPane(table);
        addComponent(scroller, 0, 1, 4, 1, 0, 0);

        delButton = new JButton("delete");
        delButton.setToolTipText("Deletes the selected trigger");
        delButton.setEnabled(false);
        delButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                int pos = table.getSelectedRow();
                ltm.delRow(pos);
            }
        });

        addButton = new JButton("add");
        addButton.setToolTipText("Add a new trigger");
        addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                ltm.addRow(new Trigger("", ".*", ""));
            }
        });

        addComponent(delButton, 0, 2, 1, 1, 0, 0);
        addComponent(addButton, 3, 2, 1, 1, 0, 0, GridBagConstraints.NORTHEAST);
        addHorizontalLayoutStopper(4, 2);
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

    private List getTriggers()
    {
        String path = "/triggers/";
        int i = 0;
        String node = path + String.valueOf(i) + "/";
        List data = new ArrayList();

        while (settings_manager.nodeExists(node) == true)
        {
            String filter = settings_manager.getString(node + "filter", "");
            String regexp = settings_manager.getString(node + "regexp", "");
            String command = settings_manager.getString(node + "command", "");
            if (filter.length() > 0 || regexp.length() > 0)
            {
                data.add(new Trigger(filter, regexp, command));
            }                

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
        List list = ltm.getData();
        String path = "/triggers/";
        String node;
        settings_manager.removeNode(path);
        int a = 0;

        for (int i = 0; i < list.size(); i++)
        {
            node = path + String.valueOf(a) + "/";
            Trigger trigger = (Trigger)list.get(i);

            if (trigger.getCommand().length() > 0)
            {
                settings_manager.setString(node + "filter", trigger.getFilter());
                settings_manager.setString(node + "regexp", trigger.getRegexp());
                settings_manager.setString(node + "command", trigger.getCommand());
                a++;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#hasErrors()
     */
    public boolean hasErrors()
    {
        Iterator it = ltm.getData().iterator();

        while (it.hasNext())
        {
            Trigger trigger = (Trigger)it.next();
            try
            {
                Pattern.compile(trigger.getRegexp());
            }
            catch (PatternSyntaxException e)
            {
                return true;
            }
        }

        return false;
    }


    class LittleTableModel extends AbstractTableModel
    {
        final String[] columnNames = { "Filter", "Regular Expression", "Command" };
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
            Trigger trigger = (Trigger)data.get(row);
            switch (col)
            {
                case 0 :
                    trigger.setFilter((String)value);
                    break;
                case 1 :
                    trigger.setRegexp((String)value);
                    break;
                case 2 :
                    trigger.setCommand((String)value);
                    break;
            }
        }

        private Object getListValue(int row, int col)
        {
            Trigger trigger = (Trigger)data.get(row);
            Object result = null;

            switch (col)
            {
                case 0 :
                    result = trigger.getFilter();
                    break;
                case 1 :
                    result = trigger.getRegexp();
                    break;
                case 2 :
                    result = trigger.getCommand();
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
    }
}
