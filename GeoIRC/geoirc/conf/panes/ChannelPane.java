/*
 * ConnectionPane.java
 * 
 * Created on 19.08.2003
 */
package geoirc.conf.panes;

import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.Channel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.IRCServer;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.conf.ValueRule;
import geoirc.util.JValidatingTextField;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author netseeker aka Michael Manske
 */
public class ChannelPane extends BaseSettingsPanel implements Storable
{
	private Map servers = new HashMap();
	private JList serverList;
	private DefaultListModel serverListModel = new DefaultListModel();
	private JList channelList;
	private DefaultListModel channelListModel = new DefaultListModel();
	private ValueRule typeRule;
	private ValueRule hostRule;
	private ValueRule portRule;
	private ValueRule channelRule;
	private JValidatingTextField hostName;
	private JValidatingTextField hostPort;
	private JValidatingTextField channelName;
	private JCheckBox channelJoin;
	private JButton addServerButton;
	private JButton delServerButton;
	private JButton addChannelButton;
	private JButton delChannelButton;

	/**
	 * @param settings
	 * @param valueRules
	 * @param name
	 */
	public ChannelPane(
		XmlProcessable settings,
		GeoIRCDefaults valueRules,
		String name)
	{
		super(settings, valueRules, name);
		typeRule = rules.getValueRule("SERVER_TYPE");
		hostRule = rules.getValueRule("HOSTNAME");
		portRule = rules.getValueRule("PORT");
		channelRule = rules.getValueRule("CHANNEL");
		fillServerMap();
		initComponents();
	}

	private void initComponents()
	{
		addComponent(new TitlePane("Known IRC Server"), 0, 0, 10, 1, 0, 0);

		applyValuesToListModel(serverListModel, servers.keySet().toArray());
		serverList = new JList(serverListModel);
		serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		((DefaultListModel) serverList.getModel()).insertElementAt("New", 0);
		serverList.addListSelectionListener(
			new ServerListSelectionHandler(this));
		JScrollPane serverScroller = new JScrollPane(serverList);
		serverScroller.setPreferredSize(new Dimension(140, 140));
		addComponent(serverScroller, 0, 1, 2, 5, 0, 0);

		addComponent(new JLabel("Hostname"), 2, 1, 2, 1, 1, 0);
		hostName = new JValidatingTextField(hostRule.getPattern(), null, 150);
		hostName.getDocument().addDocumentListener(
			new ServerInputHandler(this));
		addComponent(hostName, 2, 2, 2, 1, 1, 0, new Insets(2, 5, 2, 5));

		addComponent(new JLabel("Port"), 2, 3, 2, 1, 1, 0);
		hostPort = new JValidatingTextField(portRule.getPattern(), null);
		hostPort.getDocument().addDocumentListener(
			new ServerInputHandler(this));
		addComponent(hostPort, 2, 4, 2, 1, 1, 0, new Insets(2, 5, 2, 5));

		addServerButton = new JButton("Add");
		addServerButton.addActionListener(new AddServerActionHandler(this));
		addComponent(
			addServerButton,
			2,
			5,
			1,
			1,
			0,
			0,
			GridBagConstraints.SOUTHWEST);
		delServerButton = new JButton("Delete");
		delServerButton.addActionListener(new DelServerActionHandler(this));
		addComponent(
			delServerButton,
			3,
			5,
			1,
			1,
			1,
			0,
			GridBagConstraints.SOUTHWEST);

		addComponent(
			new TitlePane("channels of selected server"),
			0,
			6,
			10,
			1,
			0,
			0);

		channelList = new JList(channelListModel);
		channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		channelList.addListSelectionListener(
		new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent arg0)
			{
				if(channelList.getSelectedIndex() > 0)
				{
					channelName.setEditable(false);
					channelJoin.setEnabled(false);
					addChannelButton.setEnabled(false);
					delChannelButton.setEnabled(true);
				}
				else
				{
					channelName.setEditable(true);
					channelJoin.setEnabled(true);
					channelName.setText(null);
					channelJoin.setSelected(false);			
					addChannelButton.setEnabled(true);
					delChannelButton.setEnabled(false);
				}
			}
		}
		);
		JScrollPane channelScroller = new JScrollPane(channelList);
		channelScroller.setPreferredSize(new Dimension(140, 120));
		addComponent(channelScroller, 0, 7, 2, 4, 0, 0);

		addComponent(new JLabel("Channel name"), 2, 7, 2, 1, 1, 0);
		channelName =
			new JValidatingTextField(channelRule.getPattern(), null, 150);
		addComponent(channelName, 2, 8, 2, 1, 1, 0, new Insets(2, 5, 2, 5));
		channelName.getDocument().addDocumentListener(
			new ChannelInputHandler(this));
		channelJoin = new JCheckBox("autojoin");
		addComponent(channelJoin, 2, 9, 2, 1, 1, 0);

		addChannelButton = new JButton("Add");
		addChannelButton.addActionListener(new AddChannelActionHandler(this));
		addComponent(
			addChannelButton,
			2,
			10,
			1,
			1,
			0,
			0,
			GridBagConstraints.SOUTHWEST);
		delChannelButton = new JButton("Delete");
		delChannelButton.addActionListener(new DelChannelActionHandler(this));
		addComponent(
			delChannelButton,
			3,
			10,
			1,
			1,
			1,
			0,
			GridBagConstraints.SOUTHWEST);

		addLayoutStopper(0, 11);

		serverList.setSelectedIndex(0);

		channelName.setEditable(false);
		channelJoin.setEnabled(false);
		addChannelButton.setEnabled(false);
		delChannelButton.setEnabled(false);
	}

	/**
	 * Fills a map with all known irc server and their known channels
	 */
	private void fillServerMap()
	{
		String path = "/connections/";

		int i = 0;
		String nodePath = path + String.valueOf(i) + "/";
		while (settings_manager.nodeExists(nodePath))
		{
			String type =
				settings_manager.getString(
					nodePath + "type",
					(String) typeRule.getValue());
			String hostname =
				settings_manager.getString(nodePath + "hostname", null);
			if (hostname == null)
				break;

			int port =
				settings_manager.getInt(
					nodePath + "port",
					((Integer) portRule.getValue()).intValue());
			IRCServer server = new IRCServer(type, hostname, port);

			int a = 0;
			String childPath = nodePath + "channels/";
			String childNodePath = childPath + String.valueOf(a) + "/";
			while (settings_manager.nodeExists(childNodePath))
			{
				String channelName =
					settings_manager.getString(childNodePath + "name", "");
				boolean autojoin =
					settings_manager.getBoolean(
						childNodePath + "autojoin",
						true);
				server.addChannel(new Channel(channelName, autojoin));
				a++;
				childNodePath = childPath + String.valueOf(a) + "/";
			}

			servers.put(hostname, server);
			i++;
			nodePath = path + String.valueOf(i) + "/";
		}
	}

	/**
	 * Controls the state of the "Add Server" button depending
	 * on the input in the related textfields
	 */
	public void handleServerInput()
	{
		if (serverList.getSelectedIndex() == 0)
		{
			if (hostName.isValid()
				&& hostPort.isValid()
				&& hostName.isEmpty() == false
				&& hostPort.isEmpty() == false)
			{
				addServerButton.setEnabled(true);
			}
			else
				addServerButton.setEnabled(false);
		}
	}

	/**
	 * Controls the state of the "Add Channel" button depending
	 * on the input in the related textfield. 
	 */
	public void handleChannelInput()
	{
		if (channelList.getSelectedIndex() == 0)
		{
			if (channelName.isValid() && channelName.isEmpty() == false)
			{
				addChannelButton.setEnabled(true);
			}
			else
				addChannelButton.setEnabled(false);
		}
	}

	/**
	 * Listener for the the serverlist to receive messages
	 * whenever selection changes
	 */
	class ServerListSelectionHandler implements ListSelectionListener
	{
		private ChannelPane pane;

		public ServerListSelectionHandler(ChannelPane pane)
		{
			this.pane = pane;
		}

		public void valueChanged(ListSelectionEvent e)
		{
			if (e.getValueIsAdjusting() == false)
			{
				if (serverList.getSelectedIndex() > 0)
				{
					String name = (String) serverList.getSelectedValue();
					IRCServer server = (IRCServer) servers.get(name);
					hostName.setText(server.getHostname());
					hostPort.setText(String.valueOf(server.getPort()));

					applyValuesToListModel(
						channelListModel,
						server.getChannels().toArray());
					(
						(DefaultListModel) channelList
							.getModel())
							.insertElementAt(
						"New",
						0);
					delServerButton.setEnabled(true);
					addServerButton.setEnabled(false);
					hostName.setEditable(false);
					hostPort.setEditable(false);
				}
				else
				{
					delServerButton.setEnabled(false);
					addServerButton.setEnabled(false);
					hostName.setEditable(true);
					hostPort.setEditable(true);
					hostName.setText(null);
					hostPort.setText(null);
				}
			}
		}
	}

	/**
	 * Listener for the "Add Server" button
	 */
	class AddServerActionHandler implements ActionListener
	{
		private ChannelPane pane;

		public AddServerActionHandler(ChannelPane pane)
		{
			this.pane = pane;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			IRCServer server =
				new IRCServer(
					(String) typeRule.getValue(),
					hostName.getText(),
					Integer.parseInt(hostPort.getText()));
			String name = server.getHostname();
			servers.put(name, server);
			((DefaultListModel) serverList.getModel()).addElement(name);
			serverList.setSelectedValue(name, true);
		}
	}

	/**
	 * Listener for the "Delete Server" button
	 */
	class DelServerActionHandler implements ActionListener
	{
		private ChannelPane pane;

		public DelServerActionHandler(ChannelPane pane)
		{
			this.pane = pane;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			String name = (String) serverList.getSelectedValue();
			int pos = serverList.getSelectedIndex() - 1;
			servers.remove(name);
			applyValuesToListModel(serverListModel, servers.keySet().toArray());
			((DefaultListModel) serverList.getModel()).insertElementAt(
				"New",
				0);
			serverList.setSelectedIndex(pos);
			serverList.ensureIndexIsVisible(pos);
		}
	}

	/**
	 * Listener for the "Add Channel" button
	 */
	class AddChannelActionHandler implements ActionListener
	{
		private ChannelPane pane;

		public AddChannelActionHandler(ChannelPane pane)
		{
			this.pane = pane;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			if (serverList.getSelectedIndex() > 0)
			{
				String name = (String) serverList.getSelectedValue();
				IRCServer server = (IRCServer) servers.get(name);
				int pos = channelList.getSelectedIndex() + 1;
				Channel channel =
					new Channel(
						channelName.getText(),
						channelJoin.isSelected());
				server.addChannel(channel);
				((DefaultListModel) channelList.getModel()).addElement(channel);
				channelList.setSelectedIndex(pos);
				channelList.ensureIndexIsVisible(pos);
			}
			else
			{
				delChannelButton.setEnabled(false);
				addChannelButton.setEnabled(false);
				channelName.setEditable(true);
				channelJoin.setEnabled(true);
			}

		}
	}

	/**
	 * Listener for the "Delete Channel" button
	 */
	class DelChannelActionHandler implements ActionListener
	{
		private ChannelPane pane;

		public DelChannelActionHandler(ChannelPane pane)
		{
			this.pane = pane;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			String name = (String) serverList.getSelectedValue();
			int pos = channelList.getSelectedIndex() - 1;
			IRCServer server = (IRCServer) servers.get(name);
			server.removeChannel((Channel) channelList.getSelectedValue());
			applyValuesToListModel(
				channelListModel,
				server.getChannels().toArray());
			((DefaultListModel) channelList.getModel()).insertElementAt(
				"New",
				0);
			channelList.setSelectedIndex(pos);
			channelList.ensureIndexIsVisible(pos);
		}
	}

	/**
	 * Listener for receiving input messages of server related
	 * textfields
	 */
	class ServerInputHandler implements DocumentListener
	{
		private ChannelPane pane;

		public ServerInputHandler(ChannelPane pane)
		{
			this.pane = pane;
		}

		public void insertUpdate(DocumentEvent arg0)
		{
			pane.handleServerInput();
		}

		public void removeUpdate(DocumentEvent arg0)
		{
			pane.handleServerInput();
		}

		public void changedUpdate(DocumentEvent arg0)
		{
			pane.handleServerInput();
		}
	}

	/**
	 * Listener for receiving input messages of channel related
	 * textfields
	 */
	class ChannelInputHandler implements DocumentListener
	{
		private ChannelPane pane;

		public ChannelInputHandler(ChannelPane pane)
		{
			this.pane = pane;
		}

		public void insertUpdate(DocumentEvent arg0)
		{
			pane.handleChannelInput();
		}

		public void removeUpdate(DocumentEvent arg0)
		{
			pane.handleChannelInput();
		}

		public void changedUpdate(DocumentEvent arg0)
		{
			pane.handleChannelInput();
		}
	}

	/* (non-Javadoc)
	 * @see geoirc.conf.Storable#saveData()
	 */
	public boolean saveData()
	{
		Iterator it = servers.values().iterator();
		String path = "/connections/";
		int i = 0;

		while (it.hasNext())
		{
			String nodePath = path + String.valueOf(i) + "/";
			IRCServer server = (IRCServer) it.next();
			settings_manager.setString(nodePath + "type", server.getType());
			settings_manager.setString(
				nodePath + "hostname",
				server.getHostname());
			settings_manager.setInt(nodePath + "port", server.getPort());

			int a = 0;
			String childPath = nodePath + "channels/";
			String childNodePath = childPath + String.valueOf(a) + "/";
			Iterator cIt = server.getChannels().iterator();
			while (cIt.hasNext())
			{
				Channel channel = (Channel) cIt.next();
				settings_manager.setString(
					childNodePath + "name",
					channel.getName());
				settings_manager.setBoolean(
					childNodePath + "autojoin",
					channel.isAutojoin());
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see geoirc.conf.Storable#hasErrors()
	 */
	public boolean hasErrors()
	{
		if ((hostName.isValid()
			&& hostPort.isValid()
			|| hostName.isEditable() == false)
			&& (channelName.isValid() || channelName.isEditable() == false))
			return false;

		return true;
	}

	private void applyValuesToListModel(
		DefaultListModel model,
		Object values[])
	{
		model.removeAllElements();
		for (int i = 0; i < values.length; i++)
		{
			model.addElement(values[i]);
		}
	}
}
