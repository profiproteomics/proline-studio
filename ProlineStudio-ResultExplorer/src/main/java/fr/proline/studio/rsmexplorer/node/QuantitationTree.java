package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DatasetAction;
import fr.proline.studio.rsmexplorer.actions.*;
import fr.proline.studio.utils.ActionRegistry;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class QuantitationTree extends RSMTree {
    
    private RSMTreeModel m_model;
    private boolean m_isMainTree;
    
    private static HashMap<ProjectQuantitationData, QuantitationTree> treeMap = new HashMap<>();
    private static QuantitationTree m_currentTree = null;
    
    public static QuantitationTree getTree(ProjectQuantitationData projectData) {
        
        QuantitationTree tree = treeMap.get(projectData);
        if (tree == null) {
            tree = new QuantitationTree(projectData);
            treeMap.put(projectData, tree);
        }
        
        m_currentTree = tree;
        
        return tree;
    }
    
    private QuantitationTree(ProjectQuantitationData projectData) {

        setEditable(true);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        /*RSMTransferHandler handler = new RSMTransferHandler();
        setTransferHandler(handler);

        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);*/ //JPM.TODO
        
        m_isMainTree = true;

        // Model of the tree
        RSMNode top = RSMChildFactory.createNode(projectData);

        initTree(top);
        
        startLoading(top);

    }

    
    public static QuantitationTree getCurrentTree() {
        return m_currentTree;
    }

    
     
    /**
     * Return an array of all selected nodes of the tree
     * @return 
     */
    /*public RSMNode[] getSelectedNodes() {
        TreePath[] paths = getSelectionModel().getSelectionPaths();
        
        int nbPath = paths.length;
        
        RSMNode[] nodes = new RSMNode[nbPath];
        
        for (int i=0;i<nbPath;i++) {
            nodes[i] = (RSMNode) paths[i].getLastPathComponent();
        }

        return nodes;
    }*/
    
     private void triggerPopup(MouseEvent e) {
        
        // retrieve selected nodes
        RSMNode[] selectedNodes = getSelectedNodes();

        int nbNodes = selectedNodes.length;

        
        if (nbNodes>1) {
            return;
        }
        RSMNode n = selectedNodes[0];
        if (! n.isRoot()) {
            return;
        }
        
        
        if (m_rootPopup == null) {
                // create the actions
                m_rootActions = new ArrayList<>(1);  // <--- get in sync

                CreateXICAction createXICAction = new CreateXICAction();
                
                m_rootActions.add(createXICAction);



                // add actions to popup
                m_rootPopup = new JPopupMenu();
                for (int i = 0; i < m_rootActions.size(); i++) {
                    AbstractRSMAction action = m_rootActions.get(i);
                    if (action == null) {
                        m_rootPopup.addSeparator();
                    } else {
                        m_rootPopup.add(action.getPopupPresenter());
                    }
                }
            }
            
            JPopupMenu popup = m_rootPopup;
            ArrayList<AbstractRSMAction> actions = m_rootActions;

        
        
        
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
    private JPopupMenu m_rootPopup;
    private ArrayList<AbstractRSMAction> m_rootActions;


    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {

            int[] selectedRows = getSelectionRows();
            int nbSelectedRows = selectedRows.length;
            if (nbSelectedRows == 0) {
                // no row is selected, we select the current row
                int row = getRowForLocation(e.getX(), e.getY());
                if (row != -1) {
                    setSelectionRow(row);
                }
            } else if (nbSelectedRows == 1) {
                // one row is selected
                int row = getRowForLocation(e.getX(), e.getY());
                 if ((row!=-1) && (e.isShiftDown() || e.isControlDown())) {
                     addSelectionRow(row);
                 } else if ((row!=-1) && (row != selectedRows[0])) {
                    // we change the selection
                    setSelectionRow(row);
                }
            } else {
                // multiple row are already selected
                // if ctrl or shift is down, we add the row to the selection
                if (e.isShiftDown() || e.isControlDown()) {
                    int row = getRowForLocation(e.getX(), e.getY());
                    if (row !=-1) {
                        addSelectionRow(row);
                    }
                }
            }

        } else if (e.getClickCount() == 2) {
            
            // display All imported rset on double click
            RSMNode[] selectedNodes = getSelectedNodes();
            int nbNodes = selectedNodes.length;
            if (nbNodes == 1) {
                RSMNode n = selectedNodes[0];
                if (n instanceof RSMAllImportedNode) {
                    new DisplayAllRsetAction().actionPerformed(selectedNodes, e.getX(), e.getY());
                }
            }
        }




    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (m_isMainTree && SwingUtilities.isRightMouseButton(e)) {
            triggerPopup(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    
    
    /*private class RSMTreeModel extends DefaultTreeModel {

        public RSMTreeModel(TreeNode root) {
            super(root, false);
        }


        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            RSMNode rsmNode = (RSMNode) path.getLastPathComponent();

            if (rsmNode.getType()== RSMNode.NodeTypes.PROJECT_QUANTITATION) {  //JPM.TODO
                RSMProjectIdentificationNode projectNode = (RSMProjectIdentificationNode) rsmNode;
                Project project = projectNode.getProject();
                ((RSMProjectIdentificationNode) rsmNode).changeNameAndDescription(newValue.toString(), project.getDescription());
            } else if (rsmNode.getType()== RSMNode.NodeTypes.DATA_SET) {
                ((RSMDataSetNode) rsmNode).rename(newValue.toString());
            }
        }
    }*/
}
