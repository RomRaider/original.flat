/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.logger;

import enginuity.Settings;
import enginuity.io.port.SerialPortRefresher;
import enginuity.logger.comms.controller.LoggerController;
import enginuity.logger.comms.controller.LoggerControllerImpl;
import enginuity.logger.comms.query.EcuInit;
import enginuity.logger.comms.query.EcuInitCallback;
import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataLoader;
import enginuity.logger.definition.EcuDataLoaderImpl;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.profile.UserProfile;
import enginuity.logger.profile.UserProfileImpl;
import enginuity.logger.profile.UserProfileItem;
import enginuity.logger.profile.UserProfileItemImpl;
import enginuity.logger.profile.UserProfileLoader;
import enginuity.logger.profile.UserProfileLoaderImpl;
import enginuity.logger.ui.DataRegistrationBroker;
import enginuity.logger.ui.DataRegistrationBrokerImpl;
import enginuity.logger.ui.EcuDataComparator;
import enginuity.logger.ui.EcuLoggerMenuBar;
import enginuity.logger.ui.MessageListener;
import enginuity.logger.ui.SerialPortComboBox;
import enginuity.logger.ui.StatusIndicator;
import enginuity.logger.ui.handler.DataUpdateHandler;
import enginuity.logger.ui.handler.DataUpdateHandlerManager;
import enginuity.logger.ui.handler.DataUpdateHandlerManagerImpl;
import enginuity.logger.ui.handler.DataUpdateHandlerThreadWrapper;
import enginuity.logger.ui.handler.dash.DashboardUpdateHandler;
import enginuity.logger.ui.handler.file.FileUpdateHandler;
import enginuity.logger.ui.handler.graph.GraphUpdateHandler;
import enginuity.logger.ui.handler.livedata.LiveDataTableModel;
import enginuity.logger.ui.handler.livedata.LiveDataUpdateHandler;
import enginuity.logger.ui.handler.table.TableUpdateHandler;
import enginuity.logger.ui.paramlist.ParameterListTable;
import enginuity.logger.ui.paramlist.ParameterListTableModel;
import enginuity.logger.ui.paramlist.ParameterRow;
import enginuity.swing.LookAndFeelManager;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ThreadUtil.sleep;

import javax.swing.*;
import static javax.swing.JLabel.RIGHT;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import static javax.swing.JTabbedPane.BOTTOM;
import javax.swing.border.BevelBorder;
import static javax.swing.border.BevelBorder.LOWERED;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.Color.BLACK;
import static java.awt.Color.RED;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import static java.util.Collections.sort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
TODO: add better debug logging, preferably to a file and switchable (on/off)
TODO: Clean up this class!
So much to do, so little time....

Autoconnect Stuff:
TODO: Finish ecu specific parameters (IAM, Engine Load) config in logger.xml
TODO: Add ecu id and calid to ecu_defs
TODO: Keyboard accessibility (enable/disable parameters, select tabs, etc)
TODO: Check/fix MRP corrected
TODO: Autoselect rear defogger switch for file logging - add checkbox to toolbar (toolbar checkbox enables/disables file logging, switch list checkbox enables/disables logging of rear defogger switch)
TODO: Add settings screen for setting warning levels etc per parameter/switch
*/

public final class EcuLogger extends JFrame implements WindowListener, PropertyChangeListener, MessageListener {
    private static final String ENGINUITY_ECU_LOGGER_TITLE = "Enginuity ECU Logger";
    private static final String HEADING_PARAMETERS = "Parameters";
    private static final String HEADING_SWITCHES = "Switches";
    private Settings settings;
    private LoggerController controller;
    private JLabel messageLabel;
    private JLabel statsLabel;
    private JTabbedPane tabbedPane;
    private SerialPortComboBox portsComboBox;
    private DataUpdateHandlerManager dataHandlerManager;
    private DataRegistrationBroker dataTabBroker;
    private ParameterListTableModel dataTabParamListTableModel;
    private ParameterListTableModel dataTabSwitchListTableModel;
    private DataUpdateHandlerManager graphHandlerManager;
    private DataRegistrationBroker graphTabBroker;
    private ParameterListTableModel graphTabParamListTableModel;
    private ParameterListTableModel graphTabSwitchListTableModel;
    private DataUpdateHandlerManager dashboardHandlerManager;
    private DataRegistrationBroker dashboardTabBroker;
    private ParameterListTableModel dashboardTabParamListTableModel;
    private ParameterListTableModel dashboardTabSwitchListTableModel;
    private FileUpdateHandler fileUpdateHandler;
    private LiveDataTableModel dataTableModel;
    private LiveDataUpdateHandler liveDataUpdateHandler;
    private JPanel graphPanel;
    private GraphUpdateHandler graphUpdateHandler;
    private JPanel dashboardPanel;
    private DashboardUpdateHandler dashboardUpdateHandler;
    private EcuInit ecuInit;

    public EcuLogger(Settings settings) {
        super(ENGINUITY_ECU_LOGGER_TITLE);
        bootstrap(settings);
        initControllerListeners();
        initUserInterface();
        initDataUpdateHandlers();
        startPortRefresherThread();
        if (!isLogging()) {
            startLogging();
        }
    }

    private void bootstrap(final Settings settings) {
        checkNotNull(settings);
        this.settings = settings;

        EcuInitCallback ecuInitCallback = new EcuInitCallback() {
            public void callback(EcuInit newEcuInit) {
                System.out.println("ECU ID = " + newEcuInit.getEcuId());
                if (ecuInit == null || !ecuInit.getEcuId().equals(newEcuInit.getEcuId())) {
                    System.out.println("Loading logger config for new ECU...");
                    ecuInit = newEcuInit;
                    loadLoggerConfig();
                }
            }
        };

        controller = new LoggerControllerImpl(settings, ecuInitCallback, this);
        messageLabel = new JLabel(ENGINUITY_ECU_LOGGER_TITLE);
        statsLabel = buildStatsLabel();
        tabbedPane = new JTabbedPane(BOTTOM);
        portsComboBox = new SerialPortComboBox(settings);
        dataHandlerManager = new DataUpdateHandlerManagerImpl();
        dataTabBroker = new DataRegistrationBrokerImpl(controller, dataHandlerManager);
        dataTabParamListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_PARAMETERS);
        dataTabSwitchListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_SWITCHES);
        graphHandlerManager = new DataUpdateHandlerManagerImpl();
        graphTabBroker = new DataRegistrationBrokerImpl(controller, graphHandlerManager);
        graphTabParamListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_PARAMETERS);
        graphTabSwitchListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_SWITCHES);
        dashboardHandlerManager = new DataUpdateHandlerManagerImpl();
        dashboardTabBroker = new DataRegistrationBrokerImpl(controller, dashboardHandlerManager);
        dashboardTabParamListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_PARAMETERS);
        dashboardTabSwitchListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_SWITCHES);
        fileUpdateHandler = new FileUpdateHandler(settings);
        dataTableModel = new LiveDataTableModel();
        liveDataUpdateHandler = new LiveDataUpdateHandler(dataTableModel);
        graphPanel = new JPanel(new SpringLayout());
        graphUpdateHandler = new GraphUpdateHandler(graphPanel);
        dashboardPanel = new JPanel(new GridLayout(3, 3, 4, 4));
        dashboardUpdateHandler = new DashboardUpdateHandler(dashboardPanel);
    }

    private void initControllerListeners() {
        controller.addListener(dataTabBroker);
        controller.addListener(graphTabBroker);
        controller.addListener(dashboardTabBroker);
    }

    private void startPortRefresherThread() {
        SerialPortRefresher serialPortRefresher = new SerialPortRefresher(portsComboBox, settings.getLoggerPort());
        Thread portRefresherThread = new Thread(serialPortRefresher);
        portRefresherThread.setDaemon(true);
        portRefresherThread.start();
        // wait until port refresher fully started before continuing
        while (!serialPortRefresher.isStarted()) {
            sleep(100);
        }
    }

    private void initUserInterface() {
        // add menubar to frame
        setJMenuBar(buildMenubar());

        // setup main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildControlToolbar(), NORTH);
        mainPanel.add(buildTabbedPane(), CENTER);
        mainPanel.add(buildStatusBar(), SOUTH);

        // add to container
        getContentPane().add(mainPanel);
    }

    public void loadLoggerConfig() {
        try {
            EcuDataLoader dataLoader = new EcuDataLoaderImpl();
            dataLoader.loadFromXml(settings.getLoggerConfigFilePath(), settings.getLoggerProtocol(), ecuInit);
            List<EcuParameter> ecuParams = dataLoader.getEcuParameters();
            addConvertorUpdateListeners(ecuParams);
            loadEcuParams(ecuParams, null);
            loadEcuSwitches(dataLoader.getEcuSwitches(), null);
        } catch (Exception e) {
            e.printStackTrace();
            reportError(e);
        }
    }

    public void loadUserProfile(String profileFilePath) {
        try {
            UserProfileLoader profileLoader = new UserProfileLoaderImpl();
            UserProfile profile = profileLoader.loadProfile(profileFilePath);
            setSelectedPort(profile);
            applyUserProfile(profile);
            File profileFile = new File(profileFilePath);
            if (profileFile.exists()) {
                setTitle("Profile: " + profileFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportError(e);
        }
    }

    private void applyUserProfile(UserProfile profile) {
        if (profile != null) {
            applyUserProfileToLiveDataTabParameters(dataTabParamListTableModel, profile);
            applyUserProfileToLiveDataTabParameters(dataTabSwitchListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabParamListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabSwitchListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabParamListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabSwitchListTableModel, profile);
        }
    }

    private void applyUserProfileToLiveDataTabParameters(ParameterListTableModel paramListTableModel, UserProfile profile) {
        List<ParameterRow> rows = paramListTableModel.getParameterRows();
        for (ParameterRow row : rows) {
            EcuData ecuData = row.getEcuData();
            paramListTableModel.selectParam(ecuData, isSelectedOnLiveDataTab(profile, ecuData));
        }
    }

    private void applyUserProfileToGraphTabParameters(ParameterListTableModel paramListTableModel, UserProfile profile) {
        List<ParameterRow> rows = paramListTableModel.getParameterRows();
        for (ParameterRow row : rows) {
            EcuData ecuData = row.getEcuData();
            paramListTableModel.selectParam(ecuData, isSelectedOnGraphTab(profile, ecuData));
        }
    }

    private void applyUserProfileToDashTabParameters(ParameterListTableModel paramListTableModel, UserProfile profile) {
        List<ParameterRow> rows = paramListTableModel.getParameterRows();
        for (ParameterRow row : rows) {
            EcuData ecuData = row.getEcuData();
            paramListTableModel.selectParam(ecuData, isSelectedOnDashTab(profile, ecuData));
        }
    }

    private void setSelectedPort(UserProfile profile) {
        if (profile != null) {
            settings.setLoggerPort(profile.getSerialPort());
            portsComboBox.setSelectedItem(profile.getSerialPort());
        }
    }

    private void addConvertorUpdateListeners(List<EcuParameter> ecuParams) {
        for (EcuParameter ecuParam : ecuParams) {
            ecuParam.addConvertorUpdateListener(fileUpdateHandler);
            ecuParam.addConvertorUpdateListener(liveDataUpdateHandler);
            ecuParam.addConvertorUpdateListener(graphUpdateHandler);
            ecuParam.addConvertorUpdateListener(dashboardUpdateHandler);
        }
    }

    private void clearParamTableModels() {
        dataTabParamListTableModel.clear();
        graphTabParamListTableModel.clear();
        dashboardTabParamListTableModel.clear();
    }

    private void clearSwitchTableModels() {
        dataTabSwitchListTableModel.clear();
        graphTabSwitchListTableModel.clear();
        dashboardTabSwitchListTableModel.clear();
    }

    private void loadEcuParams(List<EcuParameter> ecuParams, UserProfile profile) {
        clearParamTableModels();
        sort(ecuParams, new EcuDataComparator());
        for (EcuParameter ecuParam : ecuParams) {
            if (profile == null || profile.contains(ecuParam)) {
                setDefaultUnits(profile, ecuParam);
                dataTabParamListTableModel.addParam(ecuParam, isSelectedOnLiveDataTab(profile, ecuParam));
                graphTabParamListTableModel.addParam(ecuParam, isSelectedOnGraphTab(profile, ecuParam));
                dashboardTabParamListTableModel.addParam(ecuParam, isSelectedOnDashTab(profile, ecuParam));
            }
        }
    }

    private void loadEcuSwitches(List<EcuSwitch> ecuSwitches, UserProfile profile) {
        clearSwitchTableModels();
        sort(ecuSwitches, new EcuDataComparator());
        for (EcuSwitch ecuSwitch : ecuSwitches) {
            if (profile == null || profile.contains(ecuSwitch)) {
                dataTabSwitchListTableModel.addParam(ecuSwitch, isSelectedOnLiveDataTab(profile, ecuSwitch));
                graphTabSwitchListTableModel.addParam(ecuSwitch, isSelectedOnGraphTab(profile, ecuSwitch));
                dashboardTabSwitchListTableModel.addParam(ecuSwitch, isSelectedOnDashTab(profile, ecuSwitch));
            }
        }
    }

    private void setDefaultUnits(UserProfile profile, EcuParameter ecuParam) {
        if (profile != null) {
            try {
                ecuParam.selectConvertor(profile.getSelectedConvertor(ecuParam));
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    private boolean isSelectedOnLiveDataTab(UserProfile profile, EcuData ecuData) {
        return profile != null && profile.isSelectedOnLiveDataTab(ecuData);
    }

    private boolean isSelectedOnGraphTab(UserProfile profile, EcuData ecuData) {
        return profile != null && profile.isSelectedOnGraphTab(ecuData);
    }

    private boolean isSelectedOnDashTab(UserProfile profile, EcuData ecuData) {
        return profile != null && profile.isSelectedOnDashTab(ecuData);
    }

    public UserProfile getCurrentProfile() {
        Map<String, UserProfileItem> paramProfileItems = getProfileItems(dataTabParamListTableModel.getParameterRows(),
                graphTabParamListTableModel.getParameterRows(), dashboardTabParamListTableModel.getParameterRows());
        Map<String, UserProfileItem> switchProfileItems = getProfileItems(dataTabSwitchListTableModel.getParameterRows(),
                graphTabSwitchListTableModel.getParameterRows(), dashboardTabSwitchListTableModel.getParameterRows());
        return new UserProfileImpl((String) portsComboBox.getSelectedItem(), paramProfileItems, switchProfileItems);
    }

    private Map<String, UserProfileItem> getProfileItems(List<ParameterRow> dataTabRows, List<ParameterRow> graphTabRows, List<ParameterRow> dashTabRows) {
        Map<String, UserProfileItem> profileItems = new HashMap<String, UserProfileItem>();
        for (ParameterRow dataTabRow : dataTabRows) {
            String id = dataTabRow.getEcuData().getId();
            String units = dataTabRow.getEcuData().getSelectedConvertor().getUnits();
            boolean dataTabSelected = dataTabRow.getSelected();
            boolean graphTabSelected = isEcuDataSelected(id, graphTabRows);
            boolean dashTabSelected = isEcuDataSelected(id, dashTabRows);
            profileItems.put(id, new UserProfileItemImpl(units, dataTabSelected, graphTabSelected, dashTabSelected));
        }
        return profileItems;
    }

    private boolean isEcuDataSelected(String id, List<ParameterRow> parameterRows) {
        for (ParameterRow row : parameterRows) {
            if (id.equals(row.getEcuData().getId())) {
                return row.getSelected();
            }
        }
        return false;
    }

    private void initDataUpdateHandlers() {
        DataUpdateHandler threadedFileUpdateHandler = startHandlerInThread(fileUpdateHandler);
        dataHandlerManager.addHandler(startHandlerInThread(liveDataUpdateHandler));
        dataHandlerManager.addHandler(threadedFileUpdateHandler);
        dataHandlerManager.addHandler(startHandlerInThread(TableUpdateHandler.getInstance()));
        graphHandlerManager.addHandler(startHandlerInThread(graphUpdateHandler));
        graphHandlerManager.addHandler(threadedFileUpdateHandler);
        dashboardHandlerManager.addHandler(startHandlerInThread(dashboardUpdateHandler));
        dashboardHandlerManager.addHandler(threadedFileUpdateHandler);
    }

    private DataUpdateHandler startHandlerInThread(DataUpdateHandler handler) {
        DataUpdateHandlerThreadWrapper runnableHandler = new DataUpdateHandlerThreadWrapper(handler);
        Thread thread = new Thread(runnableHandler);
        thread.setDaemon(true);
        thread.start();
        return runnableHandler;
    }

    private JComponent buildTabbedPane() {
        tabbedPane.add("Data", buildSplitPane(buildParamListPane(dataTabParamListTableModel, dataTabSwitchListTableModel), buildDataTab()));
        tabbedPane.add("Graph", buildSplitPane(buildParamListPane(graphTabParamListTableModel, graphTabSwitchListTableModel), buildGraphTab()));
        tabbedPane.add("Dashboard", buildSplitPane(buildParamListPane(dashboardTabParamListTableModel, dashboardTabSwitchListTableModel), buildDashboardTab()));
        return tabbedPane;
    }

    private JComponent buildParamListPane(TableModel paramListTableModel, TableModel switchListTableModel) {
        JScrollPane paramList = new JScrollPane(buildParamListTable(paramListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane switchList = new JScrollPane(buildParamListTable(switchListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JSplitPane splitPane = new JSplitPane(VERTICAL_SPLIT, paramList, switchList);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(400);
        return splitPane;
    }

    private JTable buildParamListTable(TableModel tableModel) {
        JTable paramListTable = new ParameterListTable(tableModel);
        changeColumnWidth(paramListTable, 0, 20, 55, 55);
        changeColumnWidth(paramListTable, 2, 50, 250, 80);
        return paramListTable;
    }

    private void changeColumnWidth(JTable paramListTable, int colIndex, int minWidth, int maxWidth, int preferredWidth) {
        TableColumn column = paramListTable.getColumnModel().getColumn(colIndex);
        column.setMinWidth(minWidth);
        column.setMaxWidth(maxWidth);
        column.setPreferredWidth(preferredWidth);
    }

    private JComponent buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(new BevelBorder(LOWERED));
        statusBar.add(messageLabel, WEST);
        statusBar.add(statsLabel, EAST);
        return statusBar;
    }

    private JSplitPane buildSplitPane(JComponent leftComponent, JComponent rightComponent) {
        JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, leftComponent, rightComponent);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(300);
        splitPane.addPropertyChangeListener(this);
        return splitPane;
    }

    private JMenuBar buildMenubar() {
        return new EcuLoggerMenuBar(this);
    }

    private JPanel buildControlToolbar() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(buildPortsComboBox(), WEST);
        controlPanel.add(buildStatusIndicator(), EAST);
        return controlPanel;
    }

    private JPanel buildPortsComboBox() {
        portsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                // this is a hack...
                if (!actionEvent.paramString().endsWith("modifiers=")) {
                    restartLogging();
                }
            }
        });
        JPanel comboBoxPanel = new JPanel(new FlowLayout());
        comboBoxPanel.add(new JLabel("COM Port:"));
        comboBoxPanel.add(portsComboBox);
        return comboBoxPanel;
    }

    public void restartLogging() {
        stopLogging();
        startLogging();
    }

    private StatusIndicator buildStatusIndicator() {
        StatusIndicator statusIndicator = new StatusIndicator();
        controller.addListener(statusIndicator);
        fileUpdateHandler.addListener(statusIndicator);
        return statusIndicator;
    }

    private JComponent buildDataTab() {
        return new JScrollPane(new JTable(dataTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JComponent buildGraphTab() {
        return new JScrollPane(graphPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JComponent buildDashboardTab() {
        return dashboardPanel;
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
        handleExit();
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    }

    public boolean isLogging() {
        return controller.isStarted();
    }

    public void startLogging() {
        settings.setLoggerPort((String) portsComboBox.getSelectedItem());
        controller.start();
    }

    public void stopLogging() {
        controller.stop();
        sleep(1000L);
    }

    public void handleExit() {
        try {
            try {
                stopLogging();
            } finally {
                cleanUpUpdateHandlers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rememberWindowProperties();
        }
    }

    private void rememberWindowProperties() {
        settings.setLoggerWindowMaximized(getExtendedState() == MAXIMIZED_BOTH);
        settings.setLoggerWindowSize(getSize());
        settings.setLoggerWindowLocation(getLocation());
    }

    private void cleanUpUpdateHandlers() {
        dataHandlerManager.cleanUp();
        graphHandlerManager.cleanUp();
        dashboardHandlerManager.cleanUp();
    }

    public Settings getSettings() {
        return settings;
    }

    public void reportMessage(String message) {
        if (message != null) {
            messageLabel.setText(message);
            messageLabel.setForeground(BLACK);
        }
    }

    public void reportStats(String message) {
        if (message != null) {
            statsLabel.setText(message);
        }
    }

    private JLabel buildStatsLabel() {
        JLabel label = new JLabel();
        label.setForeground(BLACK);
        label.setHorizontalTextPosition(RIGHT);
        return label;
    }

    public void reportError(String error) {
        if (error != null) {
            messageLabel.setText("Error: " + error);
            messageLabel.setForeground(RED);
        }
    }

    public void reportError(Exception e) {
        if (e != null) {
            String error = e.getMessage();
            if (error != null) {
                reportError(error);
            } else {
                reportError(e.toString());
            }
        }
    }

    public void setTitle(String title) {
        if (!title.startsWith(ENGINUITY_ECU_LOGGER_TITLE)) {
            title = ENGINUITY_ECU_LOGGER_TITLE + (title.length() == 0 ? "" : " - " + title);
        }
        super.setTitle(title);
    }

    //**********************************************************************


    public static void main(String... args) {
        startLogger(EXIT_ON_CLOSE, new Settings());
    }

    public static void startLogger(final int defaultCloseOperation, final Settings settings) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(defaultCloseOperation, settings);
            }
        });
    }

    private static void createAndShowGUI(int defaultCloseOperation, Settings settings) {
        // set look and feel
        LookAndFeelManager.initLookAndFeel();

        // instantiate the controlling class.
        EcuLogger ecuLogger = new EcuLogger(settings);

        // set remaining window properties
        ecuLogger.setSize(settings.getLoggerWindowSize());
        ecuLogger.setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
        ecuLogger.setDefaultCloseOperation(defaultCloseOperation);
        ecuLogger.addWindowListener(ecuLogger);

        // display the window
        ecuLogger.setLocation(settings.getLoggerWindowLocation());
        if (settings.isWindowMaximized()) {
            ecuLogger.setExtendedState(MAXIMIZED_BOTH);
        }
        ecuLogger.setVisible(true);
    }

}
