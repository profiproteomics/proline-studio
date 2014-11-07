package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.xic.DeleteAction;
import fr.proline.studio.rsmexplorer.actions.xic.RenameAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateXICDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.SelectRawFileDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.slf4j.LoggerFactory;

/**
 * XICDesignTree represents a XIC design
 * @author JM235353
 */
public class XICDesignTree extends AbstractTree {

    private static XICDesignTree m_designTree = null;
    //private boolean m_rootPathError = false;
    //private ServerFile m_defaultDirectory = null;

    public static XICDesignTree getDesignTree() {
        return m_designTree;
    }

    public static XICDesignTree getDesignTree(AbstractNode top) {
        m_designTree = new XICDesignTree(top);
        return m_designTree;
    }

    private XICDesignTree(AbstractNode top) {

        setEditable(true);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        XICTransferHandler handler = new XICTransferHandler(false);
        setTransferHandler(handler);

        setDropMode(DropMode.ON_OR_INSERT);
        setDragEnabled(true);

        
        
        initTree(top);
    }

    @Override
    protected void initTree(AbstractNode top) {
        super.initTree(top);

    }
    
    @Override
    public void rename(AbstractNode rsmNode, String newName) {
        
        AbstractNode.NodeTypes nodeType = rsmNode.getType();
         if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) ||
             (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE) ||
             (nodeType == AbstractNode.NodeTypes.DATA_SET)) {
            
            ((DataSetData)rsmNode.getData()).setTemporaryName(newName);
        }
    }
    
    @Override
    public boolean isPathEditable(TreePath path) {

        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            return false;
        }


        if (isEditable()) {
            AbstractNode node = (AbstractNode) path.getLastPathComponent();
            AbstractNode.NodeTypes nodeType = node.getType();
            if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) ||
                (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE) ||
                (nodeType == AbstractNode.NodeTypes.DATA_SET)) {
            
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            
            TreePath path = getPathForLocation(e.getX(), e.getY());
            if (path == null) {
                return;
            }
            AbstractNode n = (AbstractNode) path.getLastPathComponent();

            if (n instanceof XICRunNode) {
                XICRunNode runNode = (XICRunNode) n;

                if (runNode.isChanging()) {
                    return;
                }
                
                RunInfoData runInfoData = (RunInfoData) runNode.getData();

                if (runInfoData.getRawFileSouce().getLinkedRawFile() != null) {
                    return; // already a registered raw file, the user can no longer change it
                }
                
                ArrayList<RawFile> potentialRawFiles = runInfoData.getPotentialRawFiles();
                if (potentialRawFiles == null) {
                    potentialRawFiles = new ArrayList<>();
                    runInfoData.setPotentialRawFiles(potentialRawFiles);
                }
                
                SelectRawFileDialog selectRawFileDialog = SelectRawFileDialog.getSelectRawFileDialog(CreateXICDialog.getDialog(null));
                selectRawFileDialog.init(potentialRawFiles, runInfoData.getRawFileSouce());
                selectRawFileDialog.centerToWindow(CreateXICDialog.getDialog(null));
                selectRawFileDialog.setVisible(true);
                if (selectRawFileDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    
                    
    
                    RawFile rawFile = selectRawFileDialog.getSelectedRawFile();
                    if (rawFile != null) {
                        runInfoData.getRawFileSouce().setSelectedRawFile(rawFile);
                       
                        runInfoData.setRun(rawFile.getRuns().get(0));
                        //runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+rawFile.getRawFileName());  //JPM.RUNINFODATA
                        //runInfoData.setRunInfoInDatabase(true);
                        runNode.warnParent(false);
                        ((DefaultTreeModel)getModel()).nodeChanged(runNode);
                        return;
                    }
                    File rawFileOnDisk = selectRawFileDialog.getSelectedDiskFile();
                    if (rawFileOnDisk != null) {
                        runNode.setRawFile(rawFileOnDisk);
                    }
                }
                
                /*if (runInfoData.hasPotentialRawFiles()) {
                    // select one of the raw files
                    SelectRawFileDialog selectRawFileDialog = SelectRawFileDialog.getSelectRawFileDialog(CreateXICDialog.getDialog(null));
                    selectRawFileDialog.init(runInfoData.getPotentialRawFiles());
                    selectRawFileDialog.centerToWindow(CreateXICDialog.getDialog(null));
                    selectRawFileDialog.setVisible(true);
                    if (selectRawFileDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        RawFile rawFile = selectRawFileDialog.getSelectedRawFile();
                        runInfoData.setRawFile(rawFile);
                        runInfoData.setRun(rawFile.getRuns().get(0));
                        runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+rawFile.getRawFileName());
                        runInfoData.setRunInfoInDatabase(true);
                        runNode.warnParent(false);
                        ((DefaultTreeModel)getModel()).nodeChanged(runNode);
                    } else {
                        return;
                    }
                } else {

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
                }*/
            }

        }
    }
    //private JFileChooser m_fileChooser = null;

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
        AbstractNode[] selectedNodes = getSelectedNodes();

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
