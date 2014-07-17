package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import fr.proline.studio.rsmexplorer.node.xic.RSMRunNode;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class DesignTree extends RSMTree {

    private static DesignTree m_designTree = null;
    private boolean m_rootPathError = false;
    private ServerFile m_defaultDirectory = null;

    public static DesignTree getDesignTree() {
        return m_designTree;
    }

    public static DesignTree getDesignTree(RSMNode top) {
        m_designTree = new DesignTree(top);
        return m_designTree;
    }

    private DesignTree(RSMNode top) {

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SelectionTransferHandler handler = new SelectionTransferHandler(false);
        setTransferHandler(handler);

        setDragEnabled(true);

        initTree(top);
    }

    @Override
    protected void initTree(RSMNode top) {
        super.initTree(top);
        // -- get mzDB root path
        ArrayList<String> roots = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);
        if ((roots == null) || (roots.isEmpty())) {
            // check that the server has sent me at least one root path
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Server has returned no Root Path for mzDB Files. There is a problem with the server installation, please contact your administrator.");
            m_rootPathError = true;
            return;
        } else {
            m_rootPathError = false;
            //
            String filePath = roots.get(0);
            if (filePath != null) {
                ServerFile f = new ServerFile(filePath, filePath, true, 0, 0);
                if (f.isDirectory()) {
                    m_defaultDirectory = f;
                }
            }
        }



    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {

            RSMNode[] selectedNodes = getSelectedNodes();
            int nbNodes = selectedNodes.length;
            if (nbNodes == 1) {
                RSMNode n = selectedNodes[0];
                if (n instanceof RSMRunNode) {
                    RSMRunNode runNode = (RSMRunNode) n;
                    JFileChooser fchooser;
                    if ((m_defaultDirectory != null) && (m_defaultDirectory.isDirectory())) {
                        fchooser = new JFileChooser(m_defaultDirectory, ServerFileSystemView.getServerFileSystemView());
                    } else {
                        // should not happen in fact
                        fchooser = new JFileChooser(ServerFileSystemView.getServerFileSystemView());
                    }
                    int result = fchooser.showOpenDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        runNode.setRawFile(fchooser.getSelectedFile());
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
