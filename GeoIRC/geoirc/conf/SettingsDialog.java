/*
 * SettingsDialog.java
 * 
 * Created on 09.08.2003
 */
package geoirc.conf;

import geoirc.DisplayManager;
import geoirc.GeoIRC;
import geoirc.XmlProcessable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

public class SettingsDialog extends JDialog implements TreeSelectionListener, WindowListener
{
    static public String SETTINGS_PATH = "/gui/settings gui/";
    private XmlProcessable settings_manager;
    private JPanel mainPanel = new JPanel();
    private JPanel ButtonPanel = new JPanel();
    private JSplitPane jSplitPane1 = new JSplitPane();
    private JTree categoryTree = null;
    private JButton Apply = new JButton();
    private JButton Cancel = new JButton();
    private JButton Ok = new JButton();
    private BorderLayout borderLayout = new BorderLayout();
    private BorderLayout borderLayout1 = new BorderLayout();
    private FlowLayout flowLayout1 = new FlowLayout();
    private BaseSettingsPanel rootPane = SettingsPanelFactory.createRootPane();
    private GeoIRCDefaults valueRules = null;
    private DisplayManager display_manager;
    private List panels;
    private Frame parent;
    private ValidationListener validation_listener;
    private Set invalid_input_components = new HashSet();

    /**
     * Creates a new instance of SettingsDialog
     * @param title the windows title
     * @param settings_manager the settings manager used to load and save settings
     * @param display_manager display manager, used for debug logging
     */
    public SettingsDialog(String title, XmlProcessable settings_manager, DisplayManager display_manager)
    {
        super(display_manager.getGeoIRCInstance(), title );

        this.parent = display_manager.getGeoIRCInstance();
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.valueRules = new GeoIRCDefaults(display_manager);
        this.validation_listener = new ValidationListener()
        {
            void validationPerformed(Object source, boolean isvalid)
            {
                if (!isvalid)
                {
                    if (invalid_input_components.isEmpty())
                    {
                        Apply.setEnabled(false);
                        Ok.setEnabled(false);
                    }

                    invalid_input_components.add(source);
                }
                else
                {
                    invalid_input_components.remove(source);

                    if (invalid_input_components.isEmpty())
                    {
                        Apply.setEnabled(true);
                        Ok.setEnabled(true);
                    }
                }
            }
        };

        this.panels = SettingsPanelFactory.create(settings_manager, display_manager, valueRules, validation_listener);

        try
        {
            setResizable(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            addWindowListener(this);
            initComponents();
            //pack();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        /*        
        this.addWindowFocusListener(new WindowFocusListener()
        {
            public void windowGainedFocus(WindowEvent arg0)
            {
            }

            public void windowLostFocus(WindowEvent arg0)
            {
                requestFocus( true );
            }
            
        });
        */
    }

    /**
     * Creates a new instance of SettingsDialog 
     * @param settings_manager the settings manager used to load and save settings
     * @param display_manager display manager, used for debug logging
     */
    public SettingsDialog(XmlProcessable settings_manager, DisplayManager display_manager)
    {
        this("GeoIRC Settings", settings_manager, display_manager);
    }

    protected JRootPane createRootPane()
    {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener escape_listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                close();
                dispose();
            }
        };

        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(escape_listener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }

    private void initComponents() throws Exception
    {
        mainPanel.setLayout(borderLayout);
        //Buttons
        Ok.addActionListener(new SettingsDialog_Ok_actionAdapter(this));
        Ok.setText("OK");
        Ok.addActionListener(new SettingsDialog_Ok_actionAdapter(this));
        Ok.setActionCommand("onOk");
        Ok.setToolTipText("Save settings and close this window.");
        Ok.setPreferredSize(new Dimension(80, 25));
        Ok.setMinimumSize(new Dimension(80, 25));
        Ok.setMaximumSize(new Dimension(80, 25));
        Apply.setMaximumSize(new Dimension(80, 25));
        Apply.setMinimumSize(new Dimension(80, 25));
        Apply.setPreferredSize(new Dimension(80, 25));
        Apply.setToolTipText("Save settings.");
        Apply.setActionCommand("onApply");
        Apply.setText("Apply");
        Apply.addActionListener(new SettingsDialog_Apply_actionAdapter(this));
        Cancel.setMaximumSize(new Dimension(80, 25));
        Cancel.setMinimumSize(new Dimension(80, 25));
        Cancel.setPreferredSize(new Dimension(80, 25));
        Cancel.setToolTipText("Close without saving");
        Cancel.setActionCommand("onCancel");
        Cancel.setText("Close");
        Cancel.addActionListener(new SettingsDialog_Cancel_actionAdapter(this));
        ButtonPanel.setLayout(flowLayout1);

        getContentPane().setLayout(borderLayout1);

        //left category tree
        categoryTree = buildCategoryTree();
        categoryTree.setShowsRootHandles(true);
        categoryTree.setScrollsOnExpand(true);
        categoryTree.setAutoscrolls(true);
        categoryTree.setCellRenderer(new TreeRenderer());
        JScrollPane scrollTree = new JScrollPane(categoryTree);
        scrollTree.setAutoscrolls(true);

        jSplitPane1.setMinimumSize(new Dimension());
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.setDividerSize(5);
        jSplitPane1.setResizeWeight(0.0);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        mainPanel.add(jSplitPane1, BorderLayout.CENTER);
        jSplitPane1.add(scrollTree, JSplitPane.LEFT);
        jSplitPane1.add(new JScrollPane(rootPane), JSplitPane.RIGHT);
        rootPane.setVisible(true);
        mainPanel.add(ButtonPanel, BorderLayout.SOUTH);
        ButtonPanel.add(Cancel, null);
        ButtonPanel.add(Ok, null);
        ButtonPanel.add(Apply, null);

        //restore window positions
        open();
    }

    /**
     * @param panels
     * @return
     */
    private JTree buildCategoryTree()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootPane);
        Iterator it = panels.iterator();

        while (it.hasNext())
        {
            BaseSettingsPanel panel = (BaseSettingsPanel)it.next();
            root.add(buildSubTree(panel));
        }

        JTree categoryTree = new JTree(root);
        categoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        categoryTree.getSelectionModel().addTreeSelectionListener(this);

        return categoryTree;
    }

    /**
     * @param panel
     * @return
     */
    private DefaultMutableTreeNode buildSubTree(BaseSettingsPanel panel)
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(panel);
        List childs = panel.getChilds();
        Iterator it = childs.iterator();

        while (it.hasNext())
        {
            node.add(buildSubTree((BaseSettingsPanel)it.next()));
        }

        return node;
    }

    void Apply_actionPerformed(ActionEvent e)
    {
        saveAllPanelData();
        if (parent instanceof GeoIRC)
        {
            ((GeoIRC)parent).applySettings();
        }
    }

    void Cancel_actionPerformed(ActionEvent e)
    {
        close();
        dispose();
    }

    void Ok_actionPerformed(ActionEvent e)
    {
        this.setVisible(false);

        saveAllPanelData();
        close();

        if (parent instanceof GeoIRC)
        {
            ((GeoIRC)parent).applySettings();
        }

        dispose();
    }

    private void saveAllPanelData()
    {
        Iterator it = this.panels.iterator();

        while (it.hasNext())
        {
            Object obj = it.next();
            if (obj instanceof BaseSettingsPanel)
            {
                savePaneData((BaseSettingsPanel)obj);
            }
        }
    }

    private void savePaneData(BaseSettingsPanel pane)
    {
        //store pane itself
        if (pane.isInitialized() == true && (pane.hasChanges() && pane.hasErrors() == false))
        {
            pane.saveData();
        }

        //store pane's children
        Iterator it = pane.getChilds().iterator();

        while (it.hasNext())
        {
            Object obj = it.next();
            if (obj instanceof BaseSettingsPanel)
            {
                savePaneData((BaseSettingsPanel)obj);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)categoryTree.getLastSelectedPathComponent();

        if (node == null)
            return;

        try
        {
            BaseSettingsPanel panel = (BaseSettingsPanel)node.getUserObject();
            int a = jSplitPane1.getDividerLocation();
            jSplitPane1.setRightComponent(new JScrollPane(panel));
            panel.setVisible(true);
            jSplitPane1.setDividerLocation(a);
        }
        catch (ClassCastException ce)
        {
            ce.printStackTrace();
        }
    }

    /**
     * 
     */
    private void open()
    {
        int x = settings_manager.getInt(SETTINGS_PATH + "main frame x", 100);
        int y = settings_manager.getInt(SETTINGS_PATH + "main frame y", 100);
        int wx = settings_manager.getInt(SETTINGS_PATH + "main frame wx", 500);
        int wy = settings_manager.getInt(SETTINGS_PATH + "main frame wy", 400);
        int divX = settings_manager.getInt(SETTINGS_PATH + "divider location", 120);

        setBounds(x, y, wx, wy);
        jSplitPane1.setDividerLocation(divX);
    }

    /**
     * 
     */
    private void close()
    {
        settings_manager.setInt(SETTINGS_PATH + "main frame x", getX());
        settings_manager.setInt(SETTINGS_PATH + "main frame y", getY());
        settings_manager.setInt(SETTINGS_PATH + "main frame wx", getWidth());
        settings_manager.setInt(SETTINGS_PATH + "main frame wy", getHeight());
        settings_manager.setInt(SETTINGS_PATH + "divider location", jSplitPane1.getDividerLocation());
    }

    public void windowOpened(WindowEvent arg0)
    {}

    public void windowClosing(WindowEvent arg0)
    {
        boolean should_save = false;
        Iterator it = this.panels.iterator();

        while (it.hasNext())
        {
            Object obj = it.next();
            if (obj instanceof BaseSettingsPanel)
            {
                BaseSettingsPanel pane = (BaseSettingsPanel)obj;
                if (pane.hasChanges() == true || pane.hasChangesInChilds() == true)
                {
                    should_save = true;
                    break;
                }
            }
        }

        if (should_save == true)
        {
            int result =
                JOptionPane.showConfirmDialog(
                    this,
                    "You did made changes, would you like to save before closing?",
                    "Changes detected",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                this.setVisible(false);
                saveAllPanelData();
            }
        }

        close();
        dispose();
    }
    public void windowClosed(WindowEvent arg0)
    {}
    public void windowIconified(WindowEvent arg0)
    {}
    public void windowDeiconified(WindowEvent arg0)
    {}
    public void windowActivated(WindowEvent arg0)
    {}
    public void windowDeactivated(WindowEvent arg0)
    {}

    private final class TreeRenderer extends DefaultTreeCellRenderer
    {
        ImageIcon leaf_icon;
        ImageIcon leaf_change_icon;
        ImageIcon leaf_error_icon;
        ImageIcon folder_open_change_icon;
        ImageIcon folder_closed_change_icon;
        ImageIcon folder_open_error_icon;
        ImageIcon folder_closed_error_icon;
        ImageIcon folder_open_icon;
        ImageIcon folder_closed_icon;
        boolean images_loaded = true;

        public TreeRenderer()
        {
            try
            {
                leaf_icon = new ImageIcon(SettingsDialog.class.getResource("images/leaf.png"));
                leaf_change_icon = new ImageIcon(SettingsDialog.class.getResource("images/change.png"));
                leaf_error_icon = new ImageIcon(SettingsDialog.class.getResource("images/error.png"));

                folder_open_change_icon =
                    new ImageIcon(SettingsDialog.class.getResource("images/folder_open_change.png"));
                folder_closed_change_icon = new ImageIcon(SettingsDialog.class.getResource("images/folder_change.png"));
                folder_open_error_icon =
                    new ImageIcon(SettingsDialog.class.getResource("images/folder_open_error.png"));
                folder_closed_error_icon = new ImageIcon(SettingsDialog.class.getResource("images/folder_error.png"));
                folder_open_icon = new ImageIcon(SettingsDialog.class.getResource("images/folder_open.png"));
                folder_closed_icon = new ImageIcon(SettingsDialog.class.getResource("images/folder.png"));
            }
            catch (NullPointerException e)
            {
                images_loaded = false;
            }
        }

        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus)
        {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (images_loaded == true)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                BaseSettingsPanel pane = (BaseSettingsPanel) (node.getUserObject());

                if (pane.hasChanges())
                {
                    if (pane.hasErrors())
                    {
                        if (leaf)
                        {
                            setIcon(leaf_error_icon);
                        }
                        else
                        {
                            if (expanded)
                            {
                                setIcon(folder_open_error_icon);
                            }
                            else
                            {
                                setIcon(folder_closed_error_icon);
                            }
                        }
                    }
                    else
                    {
                        if (leaf)
                        {
                            setIcon(leaf_change_icon);
                        }
                        else
                        {
                            if (expanded)
                            {
                                setIcon(folder_open_change_icon);
                            }
                            else
                            {
                                setIcon(folder_closed_change_icon);
                            }
                        }
                    }
                }
                else
                {
                    if (leaf)
                    {
                        setIcon(leaf_icon);
                    }
                    else
                    {
                        if (expanded)
                        {
                            setIcon(folder_open_icon);
                        }
                        else
                        {
                            setIcon(folder_closed_icon);
                        }
                    }
                }
            }
            return this;
        }
    }

    public void setVisible( boolean visible )
    {
        super.setVisible( visible );
        requestFocus( true );
    }
}

class SettingsDialog_Apply_actionAdapter implements java.awt.event.ActionListener
{
    SettingsDialog adaptee;

    SettingsDialog_Apply_actionAdapter(SettingsDialog adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.Apply_actionPerformed(e);
    }
}

class SettingsDialog_Cancel_actionAdapter implements java.awt.event.ActionListener
{
    SettingsDialog adaptee;

    SettingsDialog_Cancel_actionAdapter(SettingsDialog adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.Cancel_actionPerformed(e);
    }
}

class SettingsDialog_Ok_actionAdapter implements java.awt.event.ActionListener
{
    SettingsDialog adaptee;

    SettingsDialog_Ok_actionAdapter(SettingsDialog adaptee)
    {
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e)
    {
        adaptee.Ok_actionPerformed(e);
    }
}