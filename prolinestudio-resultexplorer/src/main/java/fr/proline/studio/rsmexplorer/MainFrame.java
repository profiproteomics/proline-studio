package fr.proline.studio.rsmexplorer;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
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

public class MainFrame extends JFrame implements WindowListener {

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

            JPanel p1 = new RSMExplorerTopPanel();
            DockComponent comp1 = new DockComponent("PROPERTIES_WINDOW", p1.getName(), null, p1, DockComponent.PROP_MINIMIZE);
            m_propertiesAreaTab.add(comp1);

            JPanel p2 = new MzdbFilesTopPanel();
            DockComponent comp2 = new DockComponent("MZDB_WINDOW", p2.getName(), null, p2, DockComponent.PROP_MINIMIZE);
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
        menuItem = new JMenuItem(new DisplayWindow("Projects", "PROPERTIES_WINDOW"));
        windowMenu.add(menuItem);

        // WINDOW > Ms Files
        menuItem = new JMenuItem(new DisplayWindow("Ms Files", "MZDB_WINDOW"));
        windowMenu.add(menuItem);

        // WINDOW > Projects
        menuItem = new JMenuItem(new DisplayWindow("Logs", "LOG_WINDOW"));
        windowMenu.add(menuItem);

        return menuBar;

    }

    public void displayWindow(String windowKey) {
        DockContainer searchedContainer = m_containerRoot.search(windowKey);
        if (searchedContainer != null) {
            searchedContainer.toFront();
        } else {
            // container not found, we must create it

            DockComponent comp6 = new DockComponent(null, "Window 3", null, createComponent(Color.pink), DockComponent.PROP_CLOSE);
            m_windowAreaTab.add(comp6);
        }
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
