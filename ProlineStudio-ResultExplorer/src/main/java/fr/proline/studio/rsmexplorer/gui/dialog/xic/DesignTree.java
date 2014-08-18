package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.rsmexplorer.actions.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.xic.DeleteAction;
import fr.proline.studio.rsmexplorer.actions.xic.RenameAction;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import fr.proline.studio.rsmexplorer.node.xic.RSMRunNode;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.TreePath;
import org.slf4j.LoggerFactory;

/**
 * DesignTree represents a XIC design
 * @author JM235353
 */
public class DesignTree extends RSMTree {

    private static DesignTree m_designTree = null;
    //private boolean m_rootPathError = false;
    private ServerFile m_defaultDirectory = null;

    public static DesignTree getDesignTree() {
        return m_designTree;
    }

    public static DesignTree getDesignTree(RSMNode top) {
        m_designTree = new DesignTree(top);
        return m_designTree;
    }

    private DesignTree(RSMNode top) {

        setEditable(true);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SelectionTransferHandler handler = new SelectionTransferHandler(false);
        setTransferHandler(handler);

        setDropMode(DropMode.ON_OR_INSERT);
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
            //m_rootPathError = true;
        } else {
            //m_rootPathError = false;
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
    public void rename(RSMNode rsmNode, String newName) {
        
        RSMNode.NodeTypes nodeType = rsmNode.getType();
         if ((nodeType == RSMNode.NodeTypes.BIOLOGICAL_GROUP) ||
             (nodeType == RSMNode.NodeTypes.BIOLOGICAL_SAMPLE) ||
             (nodeType == RSMNode.NodeTypes.DATA_SET)) {
            
            ((DataSetData)rsmNode.getData()).setTemporaryName(newName);
        }
    }
    
        @Override
    public boolean isPathEditable(TreePath path) {
        if (isEditable()) {
            RSMNode node = (RSMNode) path.getLastPathComponent();
            RSMNode.NodeTypes nodeType = node.getType();
            if ((nodeType == RSMNode.NodeTypes.BIOLOGICAL_GROUP) ||
                (nodeType == RSMNode.NodeTypes.BIOLOGICAL_SAMPLE) ||
                (nodeType == RSMNode.NodeTypes.DATA_SET)) {
            
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
        RSMNode[] selectedNodes = getSelectedNodes();
        int nbNodes = selectedNodes.length;
        if (nbNodes == 1) {
            RSMNode n = selectedNodes[0];
            if (n instanceof RSMRunNode) {
                RSMRunNode runNode = (RSMRunNode) n;
                
                if (m_fileChooser == null) {
                    if ((m_defaultDirectory != null) && (m_defaultDirectory.isDirectory())) {
                        m_fileChooser = new JFileChooser(m_defaultDirectory, ServerFileSystemView.getServerFileSystemView());
                    } else {
                        // should not happen in fact
                        m_fileChooser = new JFileChooser(ServerFileSystemView.getServerFileSystemView());
                    }
                }
                int result = m_fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    

                    runNode.setRawFile(m_fileChooser.getSelectedFile());
                }
            }
        }

    }
    private JFileChooser m_fileChooser = null;

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            triggerPopup(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    
    private void triggerPopup(MouseEvent e) {
        
        // retrieve selected nodes
        RSMNode[] selectedNodes = getSelectedNodes();

        int nbNodes = selectedNodes.length;

        
        
        JPopupMenu popup;
        ArrayList<AbstractRSMAction> actions;
        


        // creation of the popup if needed
        if (m_mainPopup == null) {

            // create the actions
            m_mainActions = new ArrayList<>(12);  // <--- get in sync

            RenameAction renameAction = new RenameAction();
            m_mainActions.add(renameAction);

            

            m_mainActions.add(null);  // separator

            DeleteAction deleteAction = new DeleteAction();
            m_mainActions.add(deleteAction);


            // add actions to popup
            m_mainPopup = new JPopupMenu();
            for (int i = 0; i < m_mainActions.size(); i++) {
                AbstractRSMAction action = m_mainActions.get(i);
                if (action == null) {
                    m_mainPopup.addSeparator();
                } else {
                    m_mainPopup.add(action.getPopupPresenter());
                }
            }

        }

        popup = m_mainPopup;
        actions = m_mainActions;


        
        // update of the enable/disable state
        for (int i=0;i<actions.size();i++) {
            AbstractRSMAction action = actions.get(i);
            
            if (action == null) {
                continue;
            }
            
            action.updateEnabled(selectedNodes);
        }
        
        popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
    }
    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRSMAction> m_mainActions;

    
    
}
