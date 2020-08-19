package fr.proline.studio.rsmexplorer;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dock.AbstractDockFrame;
import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.dock.container.*;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rsmexplorer.actions.*;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ServerConnectionDialog;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;

public class MainFrame extends AbstractDockFrame implements WindowListener {

    private static MainFrame m_singleton = null;

    private DockContainerRoot m_containerRoot = null;
    private DockContainerTab m_windowAreaTab = null;
    private DockContainerTab m_propertiesAreaTab = null;

    public static MainFrame getInstance() {
        if (m_singleton == null) {
            m_singleton = new MainFrame();
        }
        return m_singleton;
    }

    private MainFrame() {
        super("Proline Studio TEST DOCK");
        setSize(800, 800);
        setIconImage(IconManager.getImage(IconManager.IconType.FRAME_ICON));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JMenuBar menuBar = createMenu();
        setJMenuBar(menuBar);

        // Root
        m_containerRoot = new DockContainerRoot();

        try {

            // left tab
            m_propertiesAreaTab = new DockContainerTab();
            m_propertiesAreaTab.setZoneArea("PROPERTIES_AREA");

            JPanel p1 = RSMExplorerTopPanel.getSingleton();
            DockComponent comp1 = new DockComponent(RSMExplorerTopPanel.getSingleton(), DockComponent.PROP_MINIMIZE);
            m_propertiesAreaTab.add(comp1);

            DockComponent comp2 = new DockComponent(MzdbFilesTopPanel.getSingleton(), DockComponent.PROP_MINIMIZE);
            m_propertiesAreaTab.add(comp2);


            // left tab put in container which accepts minimized containers
            DocContainerMinimizeZone dockContainerMinimizeZone = new DocContainerMinimizeZone();
            dockContainerMinimizeZone.set(m_propertiesAreaTab);


            // right tab
            m_windowAreaTab = new DockContainerTab();
            m_windowAreaTab.setZoneArea("WINDOWS_AREA");

            DockComponent comp4 = new DockComponent(null, "Window 1", null, createComponent(Color.blue), DockComponent.PROP_CLOSE);
            m_windowAreaTab.add(comp4);

            DockComponent comp5 = new DockComponent(null, "Window 2", null, createComponent(Color.pink), DockComponent.PROP_CLOSE);
            m_windowAreaTab.add(comp5);

            DockComponent comp6 = new DockComponent(null, "Window 3", null, createComponent(Color.pink), DockComponent.PROP_CLOSE);
            m_windowAreaTab.add(comp6);

            // Split pane
            DockContainerSplit mainContainerSplit = new DockContainerSplit();
            mainContainerSplit.add(true, dockContainerMinimizeZone, m_windowAreaTab);
            mainContainerSplit.setCanRemoveChildren(false);

            // add right tab to root
            m_containerRoot.add(mainContainerSplit, DockPosition.CENTER);

        } catch (DockException e) {
            // should not happen
            System.err.println(e.getMessage());
        }

        getContentPane().add(m_containerRoot.getMainPanel());

        addWindowListener(this);


        WindowManager.getDefault().setMainWindow(this);
    }

    private static JPanel createComponent(Color c) {
        JPanel background = new JPanel() ;
        background.setOpaque ( true ) ;
        background.setBackground (c) ;
        return background;
    }


    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // FILE
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        // FILE > Admin
        JMenuItem menuItem = new JMenuItem(new AdminAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        fileMenu.add(menuItem);

        // FILE > Connect...
        menuItem = new JMenuItem(ConnectAction.getAction());
        fileMenu.add(menuItem);

        // FILE > Change Password
        menuItem = new JMenuItem(new ChangePasswordAction());
        fileMenu.add(menuItem);

        // FILE > General Settings
        menuItem = new JMenuItem(new SettingsAction());
        fileMenu.add(menuItem);

        // FILE > Open File JPM.DOCK

        // FILE > Open Recent Files JPM.DOCK

        // -------------------
        fileMenu.addSeparator();

        // FILE > Exit
        menuItem = new JMenuItem(new ExitAction());
        fileMenu.add(menuItem);

        // ---------------------------------------------------------------

        // WINDOW
        JMenu windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(windowMenu);

        // WINDOW > Projects
        menuItem = new JMenuItem(new DisplayWindow("Projects", RSMExplorerTopPanel.getSingleton()));
        windowMenu.add(menuItem);

        // WINDOW > Ms Files
        menuItem = new JMenuItem(new DisplayWindow("Ms Files", MzdbFilesTopPanel.getSingleton()));
        windowMenu.add(menuItem);

        // WINDOW > Projects
        menuItem = new JMenuItem(new DisplayWindow("Logs", TaskLogTopPanel.getSingleton()));
        windowMenu.add(menuItem);

        // Window > Data Analyzer
        menuItem = new JMenuItem(new DataAnalyzerAction());
        windowMenu.add(menuItem);

        // Window > Memory Usage
        menuItem = new JMenuItem(new MemoryAction());
        windowMenu.add(menuItem);



        // ---------------------------------------------------------------

        // HELP
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        // Help > Getting Started
        menuItem = new JMenuItem(new HelpAction());
        helpMenu.add(menuItem);

        // Help > Proline Help
        menuItem = new JMenuItem(new HelpProlineAction());
        helpMenu.add(menuItem);

        // Help > How to
        menuItem = new JMenuItem(new HelpHowToAction());
        helpMenu.add(menuItem);





        return menuBar;

    }

    @Override
    public void closeWindow(AbstractTopPanel topPanel) {
        DockContainer container = m_containerRoot.search(topPanel.getTopPanelIdentifierKey());
        if (container != null) {
            ((DockContainerTab) ((DockComponent) container).getParent()).remove(container);
        }
    }

    @Override
    public void displayWindow(AbstractTopPanel topPanel) {
        DockContainer searchedContainer = m_containerRoot.search(topPanel.getTopPanelIdentifierKey());
        if (searchedContainer != null) {
            searchedContainer.toFront();
        } else {
            // container not found, we must create it

            DockComponent component = new DockComponent(topPanel, DockComponent.PROP_CLOSE);
            m_windowAreaTab.add(component);
        }
    }

    @Override
    public boolean isDisplayed(String windowKey) {
        DockContainer searchedContainer = m_containerRoot.search(windowKey);
        return (searchedContainer != null);
    }

    @Override
    public void toFront(String windowKey) {
        DockContainer searchedContainer = m_containerRoot.search(windowKey);
        if (searchedContainer != null) {
            searchedContainer.toFront();
        }
    }

    @Override
    public HashSet<AbstractTopPanel> getTopPanels() {
        HashSet<AbstractTopPanel> set = new HashSet<>();
        m_containerRoot.getTopPanels(set);

        return set;
    }




    public void exit() {

        // check if there is tasks being done which ask not to close the application
        if (TaskInfoManager.getTaskInfoManager().askBeforeExitingApp()) {
            InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "You should not exit. Important tasks are being done.\nAre you sure you want to exit ?");
            exitDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
            exitDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
            exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            exitDialog.setVisible(true);

            if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
                // No clicked
                return;
            }

        }

        // Close connection to R Server and kill it if needed
        RServerManager.getRServerManager().stopRProcess();

        //Close connection to JMS Server if needed
        JMSConnectionManager.getJMSConnectionManager().closeConnection();


        System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {


            Thread t = new Thread() {
                @Override
                public void run() {

                    ServerConnectionManager serverConnectionMgr = ServerConnectionManager.getServerConnectionManager();
                    while (serverConnectionMgr.isConnectionAsked()) {
                        // wait for the connection to have succedeed or failed
                        try {
                            Thread.sleep(100); // JPM.TODO : one day remove the polling and write blocking code instead
                        } catch (InterruptedException ex) {
                        }
                    }

                    if ((serverConnectionMgr.isNotConnected()) || (serverConnectionMgr.isConnectionFailed())) {
                        // the user need to enter connection parameters

                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
                                serverConnectionDialog.centerToScreen();
                                //databaseConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow()); // does not work : main window has not its size most of the time at this point
                                serverConnectionDialog.setVisible(true);

                                ServerConnectionManager serciceConnectionMgr = ServerConnectionManager.getServerConnectionManager();
                                if (serciceConnectionMgr.isConnectionDone()) {
                                    ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();
                                    //RSMTree.getTree().startLoading();
                                }
                            }
                        });

                    } else if (serverConnectionMgr.isConnectionDone()) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();
                                //RSMTree.getTree().startLoading();
                            }
                        });
                    }

                }
            };
            t.start();


            // check if the connection to the UDS is done

    }

    @Override
    public void windowClosing(WindowEvent e) {

        exit();

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

}
