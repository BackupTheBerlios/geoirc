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
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.util.JValidatingTextField;
import geoirc.util.Util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * @author netseeker aka Michael Manske
 */
public class CommandAliasesPane
    extends BaseSettingsPanel
    implements Storable, GeoIRCConstants {
    private LittleTableModel ltm = new LittleTableModel();
    private JTable table;
    private JButton delButton = new JButton("delete");
    private JButton addButton = new JButton("add new");
    private JValidatingTextField custom_alias_field = new JValidatingTextField(".+?","");
    private JValidatingTextField custom_command_field = new JValidatingTextField(".+?","");
    private JButton addCustomButton = new JButton("add custom");

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public CommandAliasesPane(
        XmlProcessable settings,
        GeoIRCDefaults valueRules,
        String name) {
        super(settings, valueRules, name);
        initComponents();
    }

    private void initComponents() {
        table = new JTable(ltm);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //setting up table cell with combo box for commands
        TableColumn cmdColumn = table.getColumnModel().getColumn(1);
        JComboBox cmdCombo = new JComboBox();
        cmdCombo.addItem("");
        for (int i = 0; i < CMDS.length; i++) {
            cmdCombo.addItem(CMDS[i].toLowerCase());
        }
        cmdColumn.setCellEditor(new DefaultCellEditor(cmdCombo));

        //setting up table cell with combo box for irc commands
        TableColumn ircCmdColumn = table.getColumnModel().getColumn(2);
        JComboBox ircCombo = new JComboBox();
        ircCombo.addItem("");
        for (int i = 0; i < IRC_CMDS.length; i++) {
            ircCombo.addItem(IRC_CMDS[i]);
        }
        ircCmdColumn.setCellEditor(new DefaultCellEditor(ircCombo));

        addComponent(new TitlePane("Command Aliases"), 0, 0, 3, 1, 0, 0);
        String path = "/command aliases/";
        int i = 0;
        String nodePath = path + String.valueOf(i) + "/";
        while (settings_manager.nodeExists(nodePath)) {
            String alias = settings_manager.getString(nodePath + "alias", "");
            String expansion =
                settings_manager.getString(nodePath + "expansion", "");
            if(alias.length() > 0)
                ltm.addRow(new CommandAlias(alias, expansion));
            i++;
            nodePath = path + String.valueOf(i) + "/";
        }

        table.setPreferredScrollableViewportSize(new Dimension(550, 200));
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        JScrollPane scroller = new JScrollPane(table);
        addComponent(scroller, 0, 1, 4, 1, 0, 0);
        
        delButton.setEnabled(false);
        delButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int pos = table.getSelectedRow();
                ltm.delRow(pos);
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ltm.addRow(new CommandAlias("new alias", ""));
            }
        });

        addComponent(delButton, 0, 2, 1, 1, 0, 0);
        addComponent(addButton, 3, 2, 1, 1, 0, 0, GridBagConstraints.NORTHEAST);
        
        addComponent(new JLabel("or add/set a custom command alias"), 0, 3, 2, 1, 0, 0);
        addComponent(new JLabel("name"), 0, 4, 1, 1, 0, 0);
        addComponent(custom_alias_field, 1, 4, 1, 1, 0, 0);
        addComponent(new JLabel("command"), 0, 5, 1, 1, 0, 0);
        addComponent(custom_command_field, 1, 5, 1, 1, 0, 0);
        addComponent(addCustomButton, 2, 5, 1, 1, 0, 0, GridBagConstraints.NORTHEAST);
        
        addHorizontalLayoutStopper(4, 5);
        addLayoutStopper(0, 6);

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
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
    public boolean saveData() {
        List list = ltm.getData();
        String path = "/command aliases/";
        String node;
        settings_manager.removeNode(path);
        int a = 0;

        for (int i = 0; i < list.size(); i++) {
            node = path + String.valueOf(a) + "/";
            CommandAlias ca = (CommandAlias)list.get(i);
            String alias = Util.getDefaultIfNull(ca.getAlias(), "");
            String expansion = Util.getDefaultIfNull(ca.getExpansion(), "");
            if (alias.length() > 0 || expansion.length() > 0) {
                settings_manager.setString(node + "alias", alias);
                settings_manager.setString(node + "expansion", expansion);
                a++;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#hasErrors()
     */
    public boolean hasErrors() {
        for (Iterator it = ltm.getData().iterator(); it.hasNext();) {
            CommandAlias ca = (CommandAlias)it.next();
            if (Util.getDefaultIfNull(ca.getAlias(), "").length() == 0
                || Util.getDefaultIfNull(ca.getExpansion(), "").length() == 0)
                return true;
        }

        return false;
    }

    class LittleTableModel extends AbstractTableModel {
        final String[] columnNames =
            { "Alias", "Command", "IRC Command", "Parameter" };
        List data = new ArrayList();

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return data.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return columnNames.length;
        }

        public Class getColumnClass(int c) {
            return String.class;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int col) {
            CommandAlias ca = (CommandAlias)data.get(row);
            Object ret = null;

            switch (col) {
                case 0 :
                    ret = ca.getAlias();
                    break;
                case 1 :
                    ret = ca.getCommand();
                    if (ret == null)
                        ret = "";
                    else
                        ret = ((String)ret);
                    break;
                case 2 :
                    ret = ca.getIRCCommand();
                    if (ret == null)
                        ret = "";
                    else
                        ret = ((String)ret);
                    break;
                case 3 :
                    ret = ca.getParamString();
                    break;
            }
            return ret;
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object value, int row, int col) {
            CommandAlias ca = (CommandAlias)data.get(row);
            switch (col) {
                case 0 :
                    ca.setAlias((String)value);
                    break;
                case 1 :
                    ca.setCommand((String)value);
                    break;
                case 2 :
                    ca.setIRCCommand((String)value);
                    break;
                case 3 :
                    ca.setParamString((String)value);
                    break;
            }
            fireTableDataChanged();
        }

        public void addRow(CommandAlias ca) {
            data.add(ca);
            fireTableDataChanged();
        }

        public void delRow(int row) {
            data.remove(row);
            fireTableDataChanged();
        }
        public void setData(List data) {
            this.data = data;
            fireTableDataChanged();
        }

        public List getData() {
            return this.data;
        }
    }
}
