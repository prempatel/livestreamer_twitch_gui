package twitchlsgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import twitchUpdate.StreamCheck;

/**
 * 
 * @author Niklas 27.09.2014
 * 
 */
public class Main_GUI extends JFrame {

    private static final long serialVersionUID = 1L;
    public static ConfigUtil cfgUtil;

    public static String currentStreamName = "";
    public static String currentQuality = "High";
    public static String customStreamName = "";
    public static String currentStreamService = "twitch.tv";
    public static DefaultListModel<JLabel> streamListModel;
    public static JLabel onlineStatus;
    public static boolean showPreview = true;
    public static boolean autoUpdate = true;
    public static JComboBox<String> streamServicesBox;
    public static JLabel updateStatus;
    public static ArrayList<StreamList> streamServicesList;
    public static int checkTimer = 30;

    private JPanel contentPane;
    private JPanel optionsPane;
    private JPanel innerContentPane;
    private JList<String> qualityList;
    private JList<JLabel> stream_list;
    private JTextField customStreamTF;
    private static JLabel previewLabel;
    private JPanel previewPanel;
    private static Main_GUI frame;
    private Thread checkThread;
    private boolean shiftPressed = false;
    public static boolean streamPaneActive = true;

    public static int downloadedBytes = 0;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    frame = new Main_GUI();
		    frame.setVisible(true);
		    frame.setTitle("Twitch.tv Livestreamer GUI");
		    UIManager.setLookAndFeel(UIManager
			    .getSystemLookAndFeelClassName());
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
			    cfgUtil.writeConfig();
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		    });
		} catch (Exception e) {
		    e.printStackTrace();
		}
		setPreviewImage(null);
	    }
	});
    }

    /**
     * Updates the streamListModel of the JList
     */
    public void updateList() {
	streamListModel.clear();
	for (int i = 0; i < selectStreamService(currentStreamService)
		.getStreamList().size(); i++) {

	    streamListModel.addElement(new JLabel(selectStreamService(
		    currentStreamService).getStreamList().get(i).getChannel()));
	}
    }

    public void updateServiceList() {
	if (streamServicesBox != null) {
	    streamServicesBox.removeAllItems();
	    if (streamServicesList.size() == 0) {
		System.out.println("service list is empty");
		streamServicesList.add(new StreamList("twitch.tv", "Twitch"));
	    }

	    for (int i = 0; i < streamServicesList.size(); i++) {
		streamServicesBox.addItem(streamServicesList.get(i)
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
	setIconImage(Toolkit.getDefaultToolkit().getImage(
		Main_GUI.class.getResource("/twitchlsgui/icon.jpg")));
	setResizable(false);
	cfgUtil = new ConfigUtil();

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 577, 400);

	optionsPane = new OptionsPanel();
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
	innerContentPane.add(stream_panel, BorderLayout.CENTER);

	stream_panel.setLayout(null);

	JScrollPane scrollPane = new JScrollPane();
	scrollPane.setBounds(10, 0, 307, 185);
	stream_panel.add(scrollPane);

	stream_list = new JList<JLabel>();
	stream_list.setVisibleRowCount(10);
	stream_list.setCellRenderer(new MyListCellRenderer());

	stream_list.setModel(streamListModel);

	stream_list.addListSelectionListener(new ListSelectionListener() {

	    @Override
	    public void valueChanged(ListSelectionEvent event) {
		setStream();
	    }
	});
	stream_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
		    cfgUtil.readStreamList(currentStreamService);
		    updateList();
		}
	    }
	});
	updateServiceList();
	updateList();
	JPanel custom_StreamPanel = new JPanel();
	custom_StreamPanel.setBounds(10, 196, 307, 126);
	stream_panel.add(custom_StreamPanel);
	GridBagLayout gbl_custom_StreamPanel = new GridBagLayout();
	gbl_custom_StreamPanel.columnWidths = new int[] { 307 };
	gbl_custom_StreamPanel.rowHeights = new int[] { 20, 20, 80 };
	gbl_custom_StreamPanel.columnWeights = new double[] { 0.0 };
	gbl_custom_StreamPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
	custom_StreamPanel.setLayout(gbl_custom_StreamPanel);

	JLabel lblCustomStream = new JLabel("Custom Stream");
	lblCustomStream.setHorizontalAlignment(SwingConstants.CENTER);
	GridBagConstraints gbc_lblCustomStream = new GridBagConstraints();
	gbc_lblCustomStream.fill = GridBagConstraints.BOTH;
	gbc_lblCustomStream.insets = new Insets(0, 0, 5, 0);
	gbc_lblCustomStream.gridx = 0;
	gbc_lblCustomStream.gridy = 0;
	custom_StreamPanel.add(lblCustomStream, gbc_lblCustomStream);

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

	JPanel middle_panel = new JPanel();
	innerContentPane.add(middle_panel, BorderLayout.EAST);
	middle_panel.setMaximumSize(new Dimension(200, 200));
	GridBagLayout gbl_middle_panel = new GridBagLayout();
	gbl_middle_panel.setConstraints(middle_panel, gbc_middle_panel);
	gbl_middle_panel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
	gbl_middle_panel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
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
		OpenStream(currentStreamName, currentQuality);
	    }
	});

	JPanel panel = new JPanel();
	GridBagConstraints gbc_panel = new GridBagConstraints();
	gbc_panel.fill = GridBagConstraints.VERTICAL;
	gbc_panel.insets = new Insets(0, 0, 5, 5);
	gbc_panel.gridx = 3;
	gbc_panel.gridy = 0;
	middle_panel.add(panel, gbc_panel);
	GridBagLayout gbl_panel = new GridBagLayout();
	gbl_panel.columnWidths = new int[] { 0, 0 };
	gbl_panel.rowHeights = new int[] { 0, 0, 0 };
	gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
	gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
	panel.setLayout(gbl_panel);

	JButton addButton = new JButton("");
	addButton.setIcon(new ImageIcon(Main_GUI.class
		.getResource("/twitchlsgui/plus.png")));
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
	GridBagConstraints gbc_addButton = new GridBagConstraints();
	gbc_addButton.insets = new Insets(0, 0, 5, 0);
	gbc_addButton.gridx = 0;
	gbc_addButton.gridy = 0;
	panel.add(addButton, gbc_addButton);

	JButton removeButton = new JButton("");
	removeButton.setIcon(new ImageIcon(Main_GUI.class
		.getResource("/twitchlsgui/minus.png")));
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
	GridBagConstraints gbc_removeButton = new GridBagConstraints();
	gbc_removeButton.gridx = 0;
	gbc_removeButton.gridy = 1;
	panel.add(removeButton, gbc_removeButton);
	middle_panel.add(startStreambutton, gbc_startStreambutton);

	JButton startCustomStreamBtn = new JButton("Start Custom VLC Stream");
	GridBagConstraints gbc_startCustomStreamBtn = new GridBagConstraints();
	gbc_startCustomStreamBtn.fill = GridBagConstraints.BOTH;
	gbc_startCustomStreamBtn.insets = new Insets(0, 0, 5, 0);
	gbc_startCustomStreamBtn.gridx = 5;
	gbc_startCustomStreamBtn.gridy = 1;
	startCustomStreamBtn.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent arg0) {
		if (customStreamName != "") {
		    OpenStream(customStreamTF.getText(), currentQuality);
		}
	    }
	});

	qualityList = new JList<String>();
	qualityList.setModel(new AbstractListModel<String>() {

	    private static final long serialVersionUID = 1L;
	    String[] values = new String[] { "Worst", "Low", "Medium", "High",
		    "Best" };

	    public int getSize() {
		return values.length;
	    }

	    public String getElementAt(int index) {
		return values[index];
	    }
	});

	qualityList.addListSelectionListener(new ListSelectionListener() {

	    @Override
	    public void valueChanged(ListSelectionEvent event) {
		setQuality(event);
	    }
	});

	qualityList.setVisibleRowCount(5);
	qualityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	GridBagConstraints gbc_qualityList = new GridBagConstraints();
	gbc_qualityList.insets = new Insets(0, 0, 5, 5);
	gbc_qualityList.gridx = 3;
	gbc_qualityList.gridy = 1;
	middle_panel.add(qualityList, gbc_qualityList);
	for (int i = 0; i < qualityList.getModel().getSize(); i++) {
	    if (currentQuality.equals(qualityList.getModel().getElementAt(i))) {
		qualityList.setSelectedIndex(i);
	    }
	}
	middle_panel.add(startCustomStreamBtn, gbc_startCustomStreamBtn);

	JButton refreshButton = new JButton("");
	refreshButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		StreamCheck.update();

	    }
	});
	refreshButton
		.setToolTipText("Runs an update on the Twitch.tv stream list");
	refreshButton.setIcon(new ImageIcon(Main_GUI.class
		.getResource("/twitchlsgui/refresh.png")));
	GridBagConstraints gbc_refreshButton = new GridBagConstraints();
	gbc_refreshButton.insets = new Insets(0, 0, 5, 5);
	gbc_refreshButton.gridx = 3;
	gbc_refreshButton.gridy = 2;
	middle_panel.add(refreshButton, gbc_refreshButton);

	previewPanel = new JPanel();
	previewLabel = new JLabel();
	GridBagConstraints gbc_preview_panel = new GridBagConstraints();
	gbc_preview_panel.insets = new Insets(0, 0, 5, 0);
	gbc_preview_panel.fill = GridBagConstraints.NONE;
	gbc_preview_panel.gridx = 5;
	gbc_preview_panel.gridy = 2;
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
		cfgUtil.writeConfig();
		System.exit(0);

	    }
	});
	middle_panel.add(exitBtn, gbc_exitBtn);

	JToolBar statusBar = new JToolBar();
	innerContentPane.add(statusBar, BorderLayout.SOUTH);
	statusBar.setFloatable(false);

	updateStatus = new JLabel("1");
	statusBar.add(updateStatus);

	JToolBar toolBar = new JToolBar();
	contentPane.add(toolBar, BorderLayout.NORTH);
	toolBar.setFloatable(false);

	JButton streamPaneButton = new JButton("Streams");
	streamPaneButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		if (!streamPaneActive) {
		    contentPane.remove(optionsPane);
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
		contentPane.add(optionsPane);
		streamPaneActive = false;
		revalidate();
		repaint();
	    }
	});
	toolBar.add(optionsPaneButton);

	// start checker thread
	checkThread = new Thread(new StreamCheck());
	checkThread.start();
    }

    /**
     * Scales and sets the previewImage
     * 
     * @param prev
     */
    public static void setPreviewImage(BufferedImage prev) {
	int newWidth = 176;
	int newHeight = 99;
	BufferedImage small = new BufferedImage(newWidth, newHeight,
		BufferedImage.TYPE_INT_RGB);
	Graphics g = small.createGraphics();
	if (prev != null && showPreview) {
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
    private void setQuality(ListSelectionEvent event) {
	currentQuality = qualityList.getSelectedValue();
    }

    /**
     * Sets current Stream from Stream list
     * 
     * @param event
     */
    private void setStream() {
	if (stream_list.getSelectedValue() != null) {
	    currentStreamName = stream_list.getSelectedValue().getText();
	    if (currentStreamService.equals("twitch.tv")) {
		for (int i = 0; i < selectStreamService(currentStreamService)
			.getStreamList().size(); i++) {
		    TwitchStream ts = (TwitchStream) selectStreamService(
			    currentStreamService).getStreamList().get(i);
		    if (ts.getChannel().equals(currentStreamName)) {
			if (ts.isOnline()) {
			    onlineStatus.setText(ts.getOnlineString());
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
		    GenericStream ts = selectStreamService(currentStreamService)
			    .getStreamList().get(i);
		    if (ts.getChannel().equals(currentStreamName)) {
			onlineStatus.setText("No Stream Information");
			setPreviewImage(null);
		    }
		}
	    }
	} else {
	    currentStreamName = "";
	}
	// showLivePopup(currentStreamName);
    }

    /**
     * 
     * @param streamService
     * @return
     */
    public static StreamList selectStreamService(String streamService) {
	for (StreamList sl : streamServicesList) {
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
	for (StreamList sl : streamServicesList) {
	    if (sl.getDisplayName().equals(streamService)) {
		return sl;
	    }
	}
	return null;
    }

    private void addButton() {
	if (shiftPressed) {
	    JTextField urlField = new JTextField(20);
	    JTextField displayNameField = new JTextField(20);

	    JPanel myPanel = new JPanel();
	    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
	    myPanel.add(new JLabel("Stream Service URL:"));
	    myPanel.add(urlField);
	    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
	    myPanel.add(new JLabel("Stream Service Display Name:"));
	    myPanel.add(displayNameField);

	    int result = JOptionPane.showConfirmDialog(null, myPanel,
		    "Please Enter URL and display Name",
		    JOptionPane.OK_CANCEL_OPTION);
	    if (result == JOptionPane.OK_OPTION) {
		streamServicesList.add(new StreamList(urlField.getText(),
			displayNameField.getText()));
		updateServiceList();
		updateList();
		cfgUtil.writeConfig();
	    }
	} else {
	    JTextField channelField = new JTextField(20);
	    JPanel myPanel = new JPanel();
	    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
	    myPanel.add(new JLabel("Channel Name:"));
	    myPanel.add(channelField);

	    int result = JOptionPane.showConfirmDialog(null, myPanel,
		    "Please Enter Channel Name", JOptionPane.OK_CANCEL_OPTION);
	    if (result == JOptionPane.OK_OPTION) {
		cfgUtil.saveStream(channelField.getText(), currentStreamService);
		updateList();
		if (currentStreamService.equals("twitch.tv")) {
		    checkThread.interrupt();
		}
	    }
	}
    }

    public void removeButton() {
	if (shiftPressed && streamServicesBox.getItemCount() > 1) {
	    for (int i = 0; i < streamServicesList.size(); i++) {
		if (streamServicesList.get(i).getUrl()
			.equals(currentStreamService)) {
		    streamServicesList.remove(i);
		    break;
		}
	    }

	    updateServiceList();
	    cfgUtil.writeConfig();
	    if (streamServicesList.size() == 1) {
		checkThread.interrupt();
	    }
	} else {
	    cfgUtil.removeStream(currentStreamName, currentStreamService);
	    updateList();
	    if (currentStreamService.equals("twitch.tv")) {
		checkThread.interrupt();
	    }
	}
    }

    /**
     * 
     * @param name
     * @param quality
     */
    public void OpenStream(String name, String quality) {
	String cmd = "livestreamer " + Main_GUI.currentStreamService + "/"
		+ name + " " + quality;
	try {
	    Process prc = Runtime.getRuntime().exec(cmd);
	    Thread reader = new Thread(new PromptReader(prc.getInputStream()));
	    reader.start();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
