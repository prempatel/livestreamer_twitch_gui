package twitchlsgui;

import ircClient.IRCClientFrame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import settings.Globals;
import settings.SettingsManager;
import settings.SettingsPanel;
import stream.GenericStreamInterface;
import stream.StreamList;
import stream.TwitchStream;
import twitchAPI.Twitch_API;
import twitchUpdate.TwitchUpdateThread;

/**
 * 
 * @author Niklas 27.09.2014
 * 
 */
public class Main_GUI extends JFrame {

	private static final long serialVersionUID = -7120936298791835733L;

	public Globals globals;

	public DefaultListModel<JLabel> streamListModel;

	public String currentStreamService = "twitch.tv";
	public JComboBox<String> streamServicesBox;

	public IRCClientFrame ircFrame = null;
	public JLabel onlineStatus;
	public JLabel updateStatus;
	public boolean streamPaneActive = true;
	public boolean canUpdate = true;

	private JPanel contentPane;
	private SettingsPanel settingsPane;
	private JPanel innerContentPane;
	private JList<JLabel> stream_list;
	private JTextField customStreamTF;
	private JPanel previewPanel;
	private TwitchUpdateThread twitchUpdateThread;
	private boolean shiftPressed = false;
	private TwitchStream ts;
	private GenericStreamInterface gs;
	private JComboBox<String> qualityComboBox;
	private JButton openChatButton;
	private Component verticalStrut;
	private Process prc;
	private JPanel msgPanel;
	private Thread reader;
	private JPanel addPanel;
	private String customStreamName = "";
	private final static int newWidth = 267;
	private final static int newHeight = 150;
	private static BufferedImage small;
	private static Graphics g;
	private static Main_GUI frame;
	private static JLabel previewLabel;
	private Main_GUI mainGUI;

	private JButton openBrowserButton;
	private static JTextArea log;
	private JScrollPane logScrollPane;
	private JButton logButton;
	private JButton recordStreambutton;

	private final Dimension DIM = new Dimension(620, 420);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {

				frame = new Main_GUI();
				frame.setVisible(true);
				frame.setTitle("Livestreamer GUI");

				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}

				frame.addWindowListener(new WindowListener() {
					@Override
					public void windowOpened(WindowEvent arg0) {
					}

					@Override
					public void windowIconified(WindowEvent arg0) {
					}

					@Override
					public void windowDeiconified(WindowEvent arg0) {
					}

					@Override
					public void windowDeactivated(WindowEvent arg0) {
					}

					@Override
					public void windowClosing(WindowEvent arg0) {
						if (frame.ircFrame != null) {
							frame.ircFrame.dispose();
							frame.ircFrame = null;
						}
						if (frame.globals.settingsManager != null) {
							frame.globals.settingsManager.writeSettings();
						}
					}

					@Override
					public void windowClosed(WindowEvent arg0) {
					}

					@Override
					public void windowActivated(WindowEvent arg0) {
					}
				});

				if (frame != null) {
					frame.setPreviewImage(null);
				}
			}
		});
	}

	/**
	 * Updates the streamListModel of the JList
	 */
	public void updateList() {
		int selection_index = stream_list.getSelectedIndex();

		streamListModel.clear();
		if (globals.sortTwitch) {
			if (currentStreamService.equals("twitch.tv")
					|| currentStreamService.equals("Twitch")) {
				selectStreamService(currentStreamService).sortList();
			}
		}

		for (int i = 0; i < selectStreamService(currentStreamService)
				.getStreamList().size(); i++) {
			streamListModel.addElement(new JLabel(selectStreamService(
					currentStreamService).getStreamList().get(i).getChannel()));
		}
		stream_list.setSelectedIndex(selection_index);
	}

	/**
	 * Updates the streamservicelist
	 */
	public void updateServiceList() {
		if (streamServicesBox != null) {
			streamServicesBox.removeAllItems();
			if (globals.streamServicesList.size() == 0) {
				globals.streamServicesList.add(new StreamList("twitch.tv",
						"Twitch"));
			}

			for (int i = 0; i < globals.streamServicesList.size(); i++) {
				streamServicesBox.addItem(globals.streamServicesList.get(i)
						.getDisplayName());
			}
			streamServicesBox
					.setSelectedIndex(streamServicesBox.getItemCount() - 1);
		}
	}

	/**
	 * Create the frame.
	 */
	public Main_GUI() {
		mainGUI = this;
		globals = new Globals();
		globals.twitchAPI = new Twitch_API(this);
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				Main_GUI.class.getResource("/assets/icon.jpg")));
		setResizable(true);
		setMinimumSize(DIM);
		setPreferredSize(DIM);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				float verticalSpace = verticalStrut.getHeight() - 13;
				float amount = verticalSpace / 20f;
				if (verticalStrut.getHeight() <= 13) {
					stream_list.setVisibleRowCount(9);
				} else {
					stream_list.setVisibleRowCount(stream_list
							.getVisibleRowCount() + (int) amount);
				}
				stream_list.revalidate();
				DIM.width = frame.getWidth();
				DIM.height = frame.getHeight();
				stream_list.setFixedCellWidth(DIM.width / 2 - 15);
			}
		});

		globals.settingsManager = new SettingsManager(this);

		settingsPane = new SettingsPanel(this);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		streamListModel = new DefaultListModel<JLabel>();

		GridBagConstraints gbc_middle_panel = new GridBagConstraints();
		gbc_middle_panel.fill = GridBagConstraints.NONE;

		innerContentPane = new JPanel();
		contentPane.add(innerContentPane, BorderLayout.CENTER);
		innerContentPane.setLayout(new BorderLayout(0, 0));

		JPanel stream_panel = new JPanel();
		innerContentPane.add(stream_panel, BorderLayout.WEST);
		stream_panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		stream_panel.add(scrollPane, BorderLayout.NORTH);

		stream_list = new JList<JLabel>();
		stream_list.setVisibleRowCount(9);
		stream_list.setFixedCellWidth(DIM.width / 2 - 15);
		stream_list.setCellRenderer(new CustomListCellRenderer(this));
		stream_list.setModel(streamListModel);

		stream_list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				setStream();
			}
		});
		stream_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		stream_list.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					OpenStream(globals.currentStreamName,
							globals.currentQuality);
				}
			}
		});

		scrollPane.setViewportView(stream_list);

		streamServicesBox = new JComboBox<String>();
		scrollPane.setColumnHeaderView(streamServicesBox);
		streamServicesBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (streamServicesBox.getSelectedItem() != null) {

					currentStreamService = selectStreamServiceD(
							(String) streamServicesBox.getSelectedItem())
							.getUrl();
					if (currentStreamService == null) {
						currentStreamService = "twitch.tv";
					}
					globals.settingsManager
							.readStreamList(currentStreamService);
					updateList();
				}
			}
		});
		updateServiceList();
		JPanel custom_StreamPanel = new JPanel();
		stream_panel.add(custom_StreamPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_custom_StreamPanel = new GridBagLayout();
		gbl_custom_StreamPanel.columnWidths = new int[] { 280 };
		gbl_custom_StreamPanel.rowHeights = new int[] { 20, 20, 80 };
		gbl_custom_StreamPanel.columnWeights = new double[] { 0.0 };
		gbl_custom_StreamPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		custom_StreamPanel.setLayout(gbl_custom_StreamPanel);

		JLabel customStreamLabel = new JLabel("Custom Stream");

		customStreamLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_customStreamLabel = new GridBagConstraints();
		gbc_customStreamLabel.fill = GridBagConstraints.BOTH;
		gbc_customStreamLabel.insets = new Insets(0, 0, 5, 5);
		gbc_customStreamLabel.gridx = 0;
		gbc_customStreamLabel.gridy = 0;
		custom_StreamPanel.add(customStreamLabel, gbc_customStreamLabel);

		customStreamTF = new JTextField();
		customStreamTF.setHorizontalAlignment(SwingConstants.CENTER);

		GridBagConstraints gbc_customStreamTF = new GridBagConstraints();
		gbc_customStreamTF.fill = GridBagConstraints.BOTH;
		gbc_customStreamTF.insets = new Insets(0, 0, 5, 0);
		gbc_customStreamTF.gridx = 0;
		gbc_customStreamTF.gridy = 1;
		custom_StreamPanel.add(customStreamTF, gbc_customStreamTF);
		customStreamTF.setColumns(1);
		customStreamTF.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent e) {
						customStreamName = customStreamTF.getText();
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						customStreamName = customStreamTF.getText();
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						customStreamName = customStreamTF.getText();
					}
				});

		onlineStatus = new JLabel("No Stream selected");
		onlineStatus.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_onlineStatus = new GridBagConstraints();
		gbc_onlineStatus.fill = GridBagConstraints.BOTH;
		gbc_onlineStatus.gridx = 0;
		gbc_onlineStatus.gridy = 2;
		custom_StreamPanel.add(onlineStatus, gbc_onlineStatus);

		verticalStrut = Box.createVerticalStrut(5);

		stream_panel.add(verticalStrut, BorderLayout.CENTER);

		JPanel middle_panel = new JPanel();
		innerContentPane.add(middle_panel, BorderLayout.EAST);
		middle_panel.setMaximumSize(new Dimension(200, 200));
		GridBagLayout gbl_middle_panel = new GridBagLayout();
		gbl_middle_panel.setConstraints(middle_panel, gbc_middle_panel);
		gbl_middle_panel.columnWidths = new int[] { 0 };
		gbl_middle_panel.rowHeights = new int[] { 40, 40, 40, 99, 0, 20 };
		gbl_middle_panel.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0 };
		gbl_middle_panel.rowWeights = new double[] { 1.0, 1.0, 1.0, 1.0 };
		middle_panel.setLayout(gbl_middle_panel);

		JButton startStreambutton = new JButton("Start VLC Stream");
		GridBagConstraints gbc_startStreambutton = new GridBagConstraints();
		gbc_startStreambutton.fill = GridBagConstraints.BOTH;
		gbc_startStreambutton.insets = new Insets(0, 0, 5, 0);
		gbc_startStreambutton.gridx = 5;
		gbc_startStreambutton.gridy = 0;
		startStreambutton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (globals.currentStreamName != "") {
					OpenStream(globals.currentStreamName,
							globals.currentQuality);
				} else {
					displayMessage("No Stream selected");
				}
			}
		});
		middle_panel.add(startStreambutton, gbc_startStreambutton);

		recordStreambutton = new JButton("Record Stream");
		GridBagConstraints gbc_recordStreambutton = new GridBagConstraints();
		gbc_recordStreambutton.fill = GridBagConstraints.BOTH;
		gbc_recordStreambutton.insets = new Insets(0, 0, 5, 0);
		gbc_recordStreambutton.gridx = 5;
		gbc_recordStreambutton.gridy = 1;
		recordStreambutton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				if (recordStreambutton.getText().equals("Record Stream")) {
					if (globals.currentStreamName != "") {
						recordStream(globals.currentStreamName,
								globals.currentQuality);
					} else {
						displayMessage("No Stream selected");
					}
				} else {
					if (prc != null) {
						prc.destroy();
					}
				}

			}
		});
		middle_panel.add(recordStreambutton, gbc_recordStreambutton);

		JButton startCustomStreamBtn = new JButton("Start Custom VLC Stream");
		GridBagConstraints gbc_startCustomStreamBtn = new GridBagConstraints();
		gbc_startCustomStreamBtn.fill = GridBagConstraints.BOTH;
		gbc_startCustomStreamBtn.insets = new Insets(0, 0, 5, 0);
		gbc_startCustomStreamBtn.gridx = 5;
		gbc_startCustomStreamBtn.gridy = 2;
		startCustomStreamBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (customStreamName != "") {
					OpenStream(customStreamTF.getText(), globals.currentQuality);
				} else {
					displayMessage("No Stream entered");
				}
			}
		});
		middle_panel.add(startCustomStreamBtn, gbc_startCustomStreamBtn);

		previewPanel = new JPanel();
		previewLabel = new JLabel();
		GridBagConstraints gbc_preview_panel = new GridBagConstraints();
		gbc_preview_panel.insets = new Insets(0, 0, 5, 0);
		gbc_preview_panel.fill = GridBagConstraints.NONE;
		gbc_preview_panel.gridx = 5;
		gbc_preview_panel.gridy = 3;
		GridBagConstraints gbc_preview_label = new GridBagConstraints();
		gbc_preview_label.fill = GridBagConstraints.NONE;
		middle_panel.add(previewPanel, gbc_preview_panel);
		previewPanel.add(previewLabel, gbc_preview_label);

		JButton exitBtn = new JButton("Exit");
		GridBagConstraints gbc_exitBtn = new GridBagConstraints();
		gbc_exitBtn.fill = GridBagConstraints.HORIZONTAL;
		gbc_exitBtn.anchor = GridBagConstraints.SOUTH;
		gbc_exitBtn.gridx = 5;
		gbc_exitBtn.gridy = 4;
		exitBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				globals.settingsManager.writeSettings();
				System.exit(0);
			}
		});
		middle_panel.add(exitBtn, gbc_exitBtn);

		JToolBar statusBar = new JToolBar();
		contentPane.add(statusBar, BorderLayout.SOUTH);
		statusBar.setFloatable(false);

		updateStatus = new JLabel("1");
		statusBar.add(updateStatus);

		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout(0, 0));

		logButton = new JButton("Close Log");
		logButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (logButton.getText().equals("Close Log")) {
					logScrollPane.setVisible(false);
					logButton.setText("Show Log");
					globals.showLog = false;
					Dimension newDim = new Dimension(DIM.width, DIM.height
							- logScrollPane.getPreferredSize().height);
					setMinimumSize(newDim);
					setPreferredSize(newDim);
					setMaximumSize(newDim);
					setSize(newDim);

				} else {
					logScrollPane.setVisible(true);
					logButton.setText("Close Log");
					globals.showLog = true;
					Dimension newDim = new Dimension(DIM.width, DIM.height
							+ logScrollPane.getPreferredSize().height);
					setMinimumSize(newDim);
					setPreferredSize(newDim);
					setMaximumSize(newDim);
					setSize(newDim);
				}
				validate();
			}
		});

		log = new JTextArea();
		log.setRows(5);
		log.setLineWrap(true);
		log.setFont(new Font("Consolas", Font.PLAIN, 11));

		DefaultCaret caret = (DefaultCaret) log.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		logScrollPane = new JScrollPane(log);
		logScrollPane.setMinimumSize(new Dimension(DIM.width, 76));
		logScrollPane.setPreferredSize(new Dimension(DIM.width, 76));
		logScrollPane.setMaximumSize(new Dimension(DIM.width, 76));

		if (globals.showLog == true) {
			logButton.setText("Close Log");
			Dimension newDim = new Dimension(DIM.width, DIM.height
					+ logScrollPane.getPreferredSize().height);
			setMinimumSize(newDim);
			setPreferredSize(newDim);
			setMaximumSize(newDim);
			setSize(newDim);
		} else {
			logButton.setText("Show Log");
			setMinimumSize(DIM);
			setPreferredSize(DIM);
			setMaximumSize(DIM);
			setSize(DIM);
		}
		logScrollPane.setVisible(globals.showLog);

		logPanel.add(logButton, BorderLayout.NORTH);
		logPanel.add(logScrollPane, BorderLayout.CENTER);

		contentPane.add(logPanel, BorderLayout.SOUTH);

		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
		toolBar.setFloatable(false);

		JButton streamPaneButton = new JButton("Streams");
		streamPaneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!streamPaneActive) {
					contentPane.remove(settingsPane);
					contentPane.add(innerContentPane);
					streamPaneActive = true;
					revalidate();
					repaint();
				}
			}
		});
		toolBar.add(streamPaneButton);

		JButton optionsPaneButton = new JButton("Options");
		optionsPaneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				contentPane.remove(innerContentPane);
				contentPane.add(settingsPane);
				streamPaneActive = false;
				revalidate();
				repaint();
			}
		});
		toolBar.add(optionsPaneButton);

		qualityComboBox = new JComboBox<String>();
		qualityComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
				"Worst", "Low", "Medium", "High", "Best" }));

		for (int i = 0; i < qualityComboBox.getModel().getSize(); i++) {
			if (globals.currentQuality.equals(qualityComboBox.getModel()
					.getElementAt(i))) {
				qualityComboBox.setSelectedIndex(i);
			}
		}
		qualityComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setQuality();
			}
		});
		Component topToolbarStrutLeft = Box.createHorizontalStrut(20);
		toolBar.add(topToolbarStrutLeft);

		JLabel lblQuality = new JLabel("Quality: ");
		toolBar.add(lblQuality);
		qualityComboBox.setMaximumRowCount(5);
		toolBar.add(qualityComboBox);

		JButton addButton = new JButton("");
		toolBar.add(addButton);
		addButton.setIcon(new ImageIcon(Main_GUI.class
				.getResource("/assets/plus.png")));
		addButton.setToolTipText("Add custom Stream to List");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addButton();
			}
		});
		addButton.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftPressed = false;
				}
			}

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftPressed = true;
				}

			}
		});

		JButton removeButton = new JButton("");
		toolBar.add(removeButton);
		removeButton.setIcon(new ImageIcon(Main_GUI.class
				.getResource("/assets/minus.png")));
		removeButton.setToolTipText("Remove selected Stream");
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeButton();
			}
		});
		removeButton.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftPressed = false;
				}
			}

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftPressed = true;
				}
			}
		});

		JButton refreshButton = new JButton("");
		toolBar.add(refreshButton);
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (canUpdate) {
					twitchUpdateThread.update();
				}
			}
		});
		refreshButton
				.setToolTipText("Runs an update on the Twitch.tv stream list");
		refreshButton.setIcon(new ImageIcon(Main_GUI.class
				.getResource("/assets/refresh.png")));

		Component topToolbarStrutRight = Box.createHorizontalStrut(150);
		toolBar.add(topToolbarStrutRight);

		openBrowserButton = new JButton("Open in Browser");
		openBrowserButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					openWebpage(new URI("http://www.twitch.tv/"
							+ globals.currentStreamName));
				} catch (URISyntaxException e1) {
					if (globals._DEBUG)
						e1.printStackTrace();
				}
			}
		});
		openBrowserButton
				.setToolTipText("Only available for Twitch.tv Streams");
		toolBar.add(openBrowserButton);

		openChatButton = new JButton("Open Chat");
		openChatButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (globals.currentStreamName != "") {
					ircFrame = new IRCClientFrame(mainGUI);
					ircFrame.setVisible(true);
				} else {
					displayMessage("No Stream selected");
				}
			}
		});
		openChatButton.setToolTipText("Only available for Twitch.tv Streams");
		toolBar.add(openChatButton);

		// start checker thread
		twitchUpdateThread = new TwitchUpdateThread(settingsPane, this);
		twitchUpdateThread.start();

	}

	/**
	 * Scales and sets the previewImage
	 * 
	 * @param prev
	 */
	public void setPreviewImage(BufferedImage prev) {
		small = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_RGB);
		g = small.createGraphics();
		if (prev != null && globals.showPreview) {
			g.drawImage(prev, 0, 0, newWidth, newHeight, null);
		} else {
			g.setColor(frame.getBackground());
			g.fillRect(0, 0, newWidth, newHeight);
		}
		g.dispose();
		previewLabel.setIcon(new ImageIcon(small));
	}

	/**
	 * Sets selected Quality
	 * 
	 * @param event
	 */
	private void setQuality() {
		globals.currentQuality = qualityComboBox.getModel().getSelectedItem()
				.toString();
	}

	/**
	 * Sets current Stream from Stream list
	 * 
	 * @param event
	 */
	private void setStream() {
		if (stream_list.getSelectedValue() != null) {
			globals.currentStreamName = stream_list.getSelectedValue()
					.getText();
			if (currentStreamService.equals("twitch.tv")) {
				for (int i = 0; i < selectStreamService(currentStreamService)
						.getStreamList().size(); i++) {
					ts = (TwitchStream) selectStreamService(
							currentStreamService).getStreamList().get(i);
					if (ts.getChannel().equals(globals.currentStreamName)) {
						if (ts.isOnline()) {
							onlineStatus.setText(ts.getOnlineString());
							onlineStatus.setToolTipText(ts.getTitle());
							setPreviewImage(ts.getPreview());
						} else {
							onlineStatus.setText("Stream is Offline");
							setPreviewImage(null);
						}
					}
				}
			} else {
				for (int i = 0; i < selectStreamService(currentStreamService)
						.getStreamList().size(); i++) {
					gs = selectStreamService(currentStreamService)
							.getStreamList().get(i);
					if (gs.getChannel().equals(globals.currentStreamName)) {
						onlineStatus.setText("No Stream Information");
						setPreviewImage(null);
					}
				}
			}
		} else {
			globals.currentStreamName = "";
		}
	}

	/**
	 * 
	 * @param streamService
	 * @return
	 */
	public StreamList selectStreamService(String streamService) {
		for (StreamList sl : globals.streamServicesList) {
			if (sl.getUrl().equals(streamService)) {
				return sl;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param streamService
	 * @return
	 */
	public StreamList selectStreamServiceD(String streamService) {
		for (StreamList sl : globals.streamServicesList) {
			if (sl.getDisplayName().equals(streamService)) {
				return sl;
			}
		}
		return null;
	}

	/**
	 * Opens a Dialog to add a stream/streamService when pressed
	 */
	private void addButton() {
		if (shiftPressed) {
			JTextField urlField = new JTextField(20);
			JTextField displayNameField = new JTextField(20);

			addPanel = new JPanel();
			addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
			addPanel.add(new JLabel("Stream Service URL:"));
			addPanel.add(urlField);
			addPanel.add(Box.createHorizontalStrut(15)); // a spacer
			addPanel.add(new JLabel("Stream Service Display Name:"));
			addPanel.add(displayNameField);

			int result = JOptionPane.showConfirmDialog(null, addPanel,
					"Please Enter URL and display Name",
					JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				globals.streamServicesList.add(new StreamList(urlField
						.getText(), displayNameField.getText().trim()));
				updateServiceList();
				updateList();
				globals.settingsManager.writeSettings();
			}
		} else {
			JTextField channelField = new JTextField(20);
			addPanel = new JPanel();
			addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
			addPanel.add(new JLabel("Channel Name:"));
			addPanel.add(channelField);

			int result = JOptionPane.showConfirmDialog(null, addPanel,
					"Please Enter Channel Name", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				globals.settingsManager.saveStream(channelField.getText()
						.trim(), currentStreamService);
				updateList();
				if (currentStreamService.equals("twitch.tv")) {
					twitchUpdateThread.interrupt();
				}
			}
		}
	}

	/**
	 * Removes the currently selected stream/streamService when pressed
	 */
	private void removeButton() {
		if (shiftPressed && streamServicesBox.getItemCount() > 1) {
			for (int i = 0; i < globals.streamServicesList.size(); i++) {
				if (globals.streamServicesList.get(i).getUrl()
						.equals(currentStreamService)) {
					globals.streamServicesList.remove(i);
					break;
				}
			}
			updateServiceList();
			globals.settingsManager.writeSettings();
			if (globals.streamServicesList.size() == 1) {
				twitchUpdateThread.interrupt();
			}
		} else {
			globals.settingsManager.removeStream(globals.currentStreamName,
					currentStreamService);
			updateList();
			if (currentStreamService.equals("twitch.tv")) {
				twitchUpdateThread.interrupt();
			}
		}
	}

	/**
	 * Creates a command to play a stram via livestreamer
	 * 
	 * @param name
	 * @param quality
	 */
	private void OpenStream(final String name, final String quality) {

		new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws Exception {

				String stream = currentStreamService + "/" + name;
				runLivestreamer(Arrays.asList("livestreamer", stream, quality));

				return null;
			}
		}.execute();
	}

	/**
	 * Creates a command to record a stram via livestreamer
	 * 
	 * @param name
	 * @param quality
	 */
	private void recordStream(final String name, final String quality) {

		new SwingWorker<Void, Void>() {

			protected void done() {
				updateLog("Recording stopped.");
				recordStreambutton.setText("Record Stream");
				try {
					get();
				} catch (Exception e) {
					if (globals._DEBUG)
						e.printStackTrace();
				}
			}

			protected Void doInBackground() throws Exception {

				GregorianCalendar now = new GregorianCalendar();
				DateFormat df = DateFormat.getDateTimeInstance(
						DateFormat.SHORT, DateFormat.SHORT);
				String date = df.format(now.getTime());

				String fileName = onlineStatus.getToolTipText() + "_" + date;
				fileName = fileName.replace(".", "-").replace(" ", "_")
						.replace(":", "-").replaceAll("[^a-zA-Z0-9_-]", "")
						.concat(".mpeg4");
				String path = globals.path;

				final JFileChooser fc = new JFileChooser();
				if (path != "") {
					fc.setCurrentDirectory(new File(path));
				}
				fc.setSelectedFile(new File(fileName));
				int returnVal = fc.showSaveDialog(mainGUI);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String recordPath = fc.getCurrentDirectory().toString();
					globals.path = recordPath;
					String file = recordPath + "/"
							+ fc.getSelectedFile().getName() + ".mpeg4";
					String stream = currentStreamService + "/" + name;

					recordStreambutton.setText("Stop Recording");

					runLivestreamer(Arrays.asList("livestreamer", "-o", file,
							stream, quality));
				}
				return null;
			}
		}.execute();
	}

	/**
	 * Creates a process and starts livestreamer with the specified command
	 * 
	 * @param cmd
	 */
	private void runLivestreamer(List<String> cmd) {

		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			prc = pb.start();
			reader = new Thread(new PromptReader(prc.getInputStream(), this));
			reader.start();
			prc.waitFor();
		} catch (IOException | InterruptedException e) {
			if (globals._DEBUG)
				e.printStackTrace();
		}
	}

	/**
	 * Append new lines of log to the log console
	 * 
	 */
	public static void updateLog(String text) {
		if (!text.isEmpty()) {
			GregorianCalendar now = new GregorianCalendar();
			DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
			String date = df.format(now.getTime());
			log.append("[" + date + "] " + text + "\n");
		}
	}

	/**
	 * Display a Message box containing message
	 * 
	 * @param message
	 */
	public void displayMessage(String message) {
		msgPanel = new JPanel();
		msgPanel.add(new JLabel(message));
		JOptionPane.showMessageDialog(this, msgPanel);
	}

	/**
	 * Open Webbrowser with specified uri
	 * 
	 */
	public void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
				: null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				if (globals._DEBUG)
					e.printStackTrace();
			}
		}
	}

	/**
	 * Open Webbrowser with specified url
	 * 
	 */
	public void openWebpage(URL url) {

		updateLog("Open chat" + url.toString());

		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			if (globals._DEBUG)
				e.printStackTrace();
		}
	}

}