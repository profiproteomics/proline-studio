package fr.proline.studio.rsmexplorer.tree.quantitation;

import fr.proline.studio.rsmexplorer.actions.identification.RetrieveSCDataAction;
import fr.proline.studio.rsmexplorer.actions.identification.CreateXICAction;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.identification.ExportXICAction;
import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.rsmexplorer.actions.identification.*;
import fr.proline.studio.rsmexplorer.tree.ChildFactory;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;


/**
 *
 * @author JM235353
 */
public class QuantitationTree extends AbstractTree {

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
        AbstractNode top = ChildFactory.createNode(projectData);

        initTree(top);
        
        startLoading(top);

    }

    
    public static QuantitationTree getCurrentTree() {
        return m_currentTree;
    }
    
    @Override
    public void rename(AbstractNode rsmNode, String newName) {
        if (rsmNode.getType().equals(AbstractNode.NodeTypes.DATA_SET)) {
            ((DataSetNode) rsmNode).rename(newName, QuantitationTree.getCurrentTree());
        }
    }

    
    private void triggerPopup(MouseEvent e) {
        
        // retrieve selected nodes
        AbstractNode[] selectedNodes = getSelectedNodes();

        int nbNodes = selectedNodes.length;

        
        if ((nbNodes>1) || (nbNodes == 0)) {
            return;
        }
        AbstractNode n = selectedNodes[0];
        boolean isRootPopup = n.isRoot();

        // check if nodes are changing
        for (int i = 0; i < nbNodes; i++) {
            if (selectedNodes[i].isChanging()) {
                // do not show a popup on a node which is changing
                return;
            }
         }
         JPopupMenu popup;
         ArrayList<AbstractRSMAction> actions;
        
         if (isRootPopup) {
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

             popup = m_rootPopup;
             actions = m_rootActions;
         } else {
             if (m_mainPopup == null) {
                 // create the actions
                 m_mainActions = new ArrayList<>(8);  // <--- get in sync

                 PropertiesAction propertiesAction = new PropertiesAction(AbstractTree.TreeType.TREE_QUANTITATION);
                 m_mainActions.add(propertiesAction);
                 
                 m_mainActions.add(null);  // separator
                 
                 RetrieveSCDataAction retrieveSCDataAction = new RetrieveSCDataAction();
                 m_mainActions.add(retrieveSCDataAction);

                 ExportXICAction exportXICActionIons = new ExportXICAction(ExportXICAction.ExportType.MASTER_QPEP_IONS);
                 m_mainActions.add(exportXICActionIons);
                 
                 ExportXICAction exportXICActionPep = new ExportXICAction(ExportXICAction.ExportType.MASTER_QPEPS);
                 m_mainActions.add(exportXICActionPep);
                 
                 ExportXICAction exportXICActionProtSet = new ExportXICAction(ExportXICAction.ExportType.BASIC_MASTER_QPROT_SETS);
                 m_mainActions.add(exportXICActionProtSet);
                 
                 m_mainActions.add(null);  // separator
                 
                 RenameAction renameQuantitationAction = new RenameAction(AbstractTree.TreeType.TREE_QUANTITATION);
                 m_mainActions.add(renameQuantitationAction);
                 

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

         }


        
        
        
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
    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRSMAction> m_mainActions;

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

        }/* else if (e.getClickCount() == 2) {
            
            // display All imported rset on double click
            AbstractNode[] selectedNodes = getSelectedNodes();
            int nbNodes = selectedNodes.length;
            if (nbNodes == 1) {
                AbstractNode n = selectedNodes[0];
                if (n instanceof RSMAllImportedNode) {
                    new DisplayAllRsetAction().actionPerformed(selectedNodes, e.getX(), e.getY());
                }
            }
        }*/




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


}
