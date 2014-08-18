package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.rsmexplorer.actions.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;


/**
 *
 * @author JM235353
 */
public class QuantitationTree extends RSMTree {

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
    
    @Override
    public void rename(RSMNode rsmNode, String newName) {
        //JPM.TODO (setEditable must be done on tree)
    }

    
    private void triggerPopup(MouseEvent e) {
        
        // retrieve selected nodes
        RSMNode[] selectedNodes = getSelectedNodes();

        int nbNodes = selectedNodes.length;

        
        if ((nbNodes>1) || (nbNodes == 0)) {
            return;
        }
        RSMNode n = selectedNodes[0];
        boolean isRootPopup = n.isRoot();

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
                 m_mainActions = new ArrayList<>(4);  // <--- get in sync

                 RetrieveSCDataAction retrieveSCDataAction = new RetrieveSCDataAction();
                 m_mainActions.add(retrieveSCDataAction);

                 ExportXICAction exportXICActionIons = new ExportXICAction(ExportXICAction.ExportType.MASTER_QPEP_IONS);
                 m_mainActions.add(exportXICActionIons);
                 
                 ExportXICAction exportXICActionPep = new ExportXICAction(ExportXICAction.ExportType.MASTER_QPEPS);
                 m_mainActions.add(exportXICActionPep);
                 
                 ExportXICAction exportXICActionProtSet = new ExportXICAction(ExportXICAction.ExportType.MASTER_QPROT_SETS);
                 m_mainActions.add(exportXICActionProtSet);
                 

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
            RSMNode[] selectedNodes = getSelectedNodes();
            int nbNodes = selectedNodes.length;
            if (nbNodes == 1) {
                RSMNode n = selectedNodes[0];
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
