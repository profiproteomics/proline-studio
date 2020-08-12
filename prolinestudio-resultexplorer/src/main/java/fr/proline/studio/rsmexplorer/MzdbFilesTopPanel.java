package fr.proline.studio.rsmexplorer;

import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.msfiles.MsFilesExplorer;
import fr.proline.studio.msfiles.WorkingSetView;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;

import javax.swing.*;
import java.awt.*;

public class MzdbFilesTopPanel extends JPanel {

    private static MsFilesExplorer m_explorer;
    private JTabbedPane m_tabbedPane;

    public MzdbFilesTopPanel() {
        initComponents();
        setName("Ms Files");
        setToolTipText("Ms Files");
    }

    private void initComponents() {

        setLayout(new BorderLayout());
        m_tabbedPane = new JTabbedPane();

        m_explorer = new MsFilesExplorer();
        m_tabbedPane.add("Explorer", m_explorer);

        m_tabbedPane.add("Working Sets", WorkingSetView.getWorkingSetView());
        add(m_tabbedPane, BorderLayout.CENTER);

        if (!ServerConnectionManager.getServerConnectionManager().isNotConnected()) {
            m_explorer.getTreeFileChooserPanel().initTree();
            m_explorer.getTreeFileChooserPanel().restoreTree(TreeUtils.TreeType.SERVER);
        }

    }

    public static MsFilesExplorer getExplorer() {
        return m_explorer;
    }
}
