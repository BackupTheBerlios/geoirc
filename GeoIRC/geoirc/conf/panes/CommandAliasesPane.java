/*
 * CommandAliasesPane.java
 * 
 * Created on 24.08.2003
 */
package geoirc.conf.panes;

import geoirc.CommandAlias;
import geoirc.GeoIRCConstants;
import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.JValidatingTable;
import geoirc.conf.SettingsPeer;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.beans.ValueRule;
import geoirc.util.JValidatingTextField;
import geoirc.util.Util;

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
public class CommandAliasesPane extends BaseSettingsPanel implements Storable, GeoIRCConstants
{
    private LittleTableModel ltm = new LittleTableModel();
    private JValidatingTable table;
    private JButton delButton = new JButton("delete");
    private JButton addButton = new JButton("new");

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public CommandAliasesPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
    {
        super(settings, valueRules, name);
    }

    public void initialize()
    {
        ltm.setData( SettingsPeer.loadCommandAliases( settings_manager ) );
        table = new JValidatingTable( ltm, validation_listener );
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(18);        
        table.setToolTipText("Click into a cell to edit values");
        
        //setting up table cell for alias name
        ValueRule rule = rules.getValueRule("COMMAND_ALIAS_NAME");
        final JValidatingTextField alias_field = new JValidatingTextField(rule.getPattern(), rule.getName(), null);
        alias_field.setToolTipText("click to edit, hit return to apply your change");
        table.setValidatingCellEditor(alias_field, 0);
        
        //setting up table cell for commands
        rule = rules.getValueRule("COMMAND_ALIAS_COMMAND");
        final JValidatingTextField command_field = new JValidatingTextField(rule.getPattern(), rule.getName(), null);
        command_field.setToolTipText("click to edit, hit return to apply your change");
        table.setValidatingCellEditor(command_field, 1);
        
        addComponent(new TitlePane("Command Aliases"), 0, 0, 5, 1, 0, 0);
        
        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(380);
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
                ltm.addRow(new CommandAlias("new alias", ""));
            }
        });

        addHorizontalLayoutStopper(5, 2);
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
        String path = "/command aliases/";
        String node;
        settings_manager.removeNode(path);
        int a = 0;

        for (int i = 0; i < list.size(); i++)
        {
            node = path + String.valueOf(a) + "/";
            CommandAlias ca = (CommandAlias)list.get(i);
            String alias = Util.getDefaultIfNull(ca.getAlias(), "");
            String expansion = Util.getDefaultIfNull(ca.getExpansion(), "");
            if (alias.length() > 0 || expansion.length() > 0)
            {
                settings_manager.setString(node + "alias", alias);
                settings_manager.setString(node + "expansion", expansion);
                a++;
            }
        }

        return true;
    }

    class LittleTableModel extends AbstractTableModel
    {
        final String[] columnNames = { "Alias", "Command" };
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

        public String getColumnName(int col)
        {
            return columnNames[col];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int col)
        {
            CommandAlias ca = (CommandAlias)data.get(row);
            Object ret = null;

            switch (col)
            {
                case 0 :
                    ret = ca.getAlias();
                    break;
                case 1 :
                    ret = ca.getExpansion();
                    if (ret == null)
                        ret = "";
                    else
                        ret = ((String)ret);
                    break;
            }
            return ret;
        }

        public boolean isCellEditable(int row, int col)
        {
            return true;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object value, int row, int col)
        {
            CommandAlias ca = (CommandAlias)data.get(row);
            switch (col)
            {
                case 0 :
                    ca.setAlias((String)value);
                    break;
                case 1 :
                    ca.setExpansion((String)value);
                    break;
            }
            
            fireTableDataChanged();
        }

        public void addRow(CommandAlias ca)
        {
            data.add(ca);
            fireTableDataChanged();
        }

        public void delRow(int row)
        {
            data.remove(row);
            fireTableDataChanged();
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
    }
}
