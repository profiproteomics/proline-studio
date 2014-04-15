package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DatasetAction;
import fr.proline.studio.rsmexplorer.actions.*;
import fr.proline.studio.utils.ActionRegistry;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;

/**
 * Tree of projects and datasets
 * @author JM235353
 */
public class IdentificationTree extends RSMTree implements TreeWillExpandListener {

    
    private boolean m_isMainTree;

    private static HashMap<ProjectIdentificationData, IdentificationTree> treeMap = new HashMap<>();
    private static IdentificationTree m_currentTree = null;
    
    private boolean m_loadingDone = false;
    
    public static IdentificationTree getTree(ProjectIdentificationData projectData) {
        
        IdentificationTree tree = treeMap.get(projectData);
        if (tree == null) {
            tree = new IdentificationTree(projectData);
            treeMap.put(projectData, tree);
        }
        
        m_currentTree = tree;
        
        return tree;
    }
    
    public static IdentificationTree getCurrentTree() {
        return m_currentTree;
    }
    

    private IdentificationTree(ProjectIdentificationData projectData) {

        setEditable(true);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        RSMTransferHandler handler = new RSMTransferHandler();
        setTransferHandler(handler);

        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        
        m_isMainTree = true;

        // Model of the tree
        RSMNode top = RSMChildFactory.createNode(projectData);

        initTree(top);
        
        startLoading(top);

    }

    private IdentificationTree(RSMNode top) {

        m_isMainTree = false;

        initTree(top);

        startLoading(top);

    }

    @Override
    protected final void initTree(RSMNode top) {
        super.initTree(top);
        
        addTreeWillExpandListener(this);
    }

    
    
    public static void clearAll() {
        treeMap.clear();
    }
    
    public void removeRootChildren() {

        m_loadingDone = false;
        
        RSMNode root = ((RSMNode) m_model.getRoot());
        
        root.removeAllChildren();
        
        m_model.nodeStructureChanged(root);

    }
    
    @Override
    public boolean isPathEditable(TreePath path) {
        if (isEditable()) {
            RSMNode node = (RSMNode) path.getLastPathComponent();
            RSMNode.NodeTypes nodeType = node.getType();
            if ((nodeType == RSMNode.NodeTypes.DATA_SET) || (nodeType == RSMNode.NodeTypes.PROJECT_IDENTIFICATION)) {
                return true;
            }
        }
        return false;
    }

    public IdentificationTree copyResultSetRootSubTree(ResultSet rset, long projectId) {

        long rsetId = rset.getId();

        RSMDataSetNode rsetNode = findResultSetNode((RSMNode) m_model.getRoot(), rsetId, projectId);

        if (rsetNode == null) {
            return null;
        }

        return new IdentificationTree(rsetNode);

    }
    
    
    public IdentificationTree copyDataSetRootSubTree(DDataset dset, long projectId) {

        long dsetId = dset.getId();
        RSMDataSetNode dsetNode = findDataSetNode((RSMNode) m_model.getRoot(), dsetId, projectId);

        if (dsetNode == null) {
            return null;
        }

        return new IdentificationTree(dsetNode);

    }

    public RSMNode copyRootNodeForSelection() {
        RSMNode node = (RSMNode) m_model.getRoot();
        return node.copyNode();
        
    }
    
    
    private RSMDataSetNode findDataSetNode(RSMNode node, long dsetId, long projectId) {

        int nbChildren = node.getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            RSMNode childNode = (RSMNode) node.getChildAt(i);
            RSMNode.NodeTypes childType = childNode.getType();
            if (childType == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode dataSetNode = ((RSMDataSetNode) childNode);
                Long datasetId = dataSetNode.getDataset().getId();
                if ((datasetId != null) && (datasetId.longValue() == dsetId)) {
                    return dataSetNode;
                }

            } else if (childType == RSMNode.NodeTypes.PROJECT_IDENTIFICATION) {
                RSMProjectIdentificationNode projectNode = ((RSMProjectIdentificationNode) childNode);
                if (projectNode.getProject().getId() != projectId) {
                    // we are not in the right project
                    continue;
                }
            }
            if (!childNode.isLeaf()) {
                RSMDataSetNode dataSetNode = findDataSetNode(childNode, dsetId, projectId);
                if (dataSetNode != null) {
                    return dataSetNode;
                }
            }

        }

        return null;
    }

    private RSMDataSetNode findResultSetNode(RSMNode node, long rsetId, long projectId) {

        int nbChildren = node.getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            RSMNode childNode = (RSMNode) node.getChildAt(i);
            RSMNode.NodeTypes childType = childNode.getType();
            if (childType == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode dataSetNode = ((RSMDataSetNode) childNode);
                Long resultSetId = dataSetNode.getResultSetId();

                if ((resultSetId != null) && (resultSetId.intValue() == rsetId)) {
                    return dataSetNode;
                }
            } else if (childType == RSMNode.NodeTypes.PROJECT_IDENTIFICATION) {
                RSMProjectIdentificationNode projectNode = ((RSMProjectIdentificationNode) childNode);
                if (projectNode.getProject().getId() != projectId) {
                    // we are not in the right project
                    continue;
                }
            }
            if (!childNode.isLeaf()) {
                RSMDataSetNode dataSetNode = findResultSetNode(childNode, rsetId, projectId);
                if (dataSetNode != null) {
                    return dataSetNode;
                }
            }

        }

        return null;
    }





    
    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

        TreePath path = event.getPath();
        RSMNode nodeExpanded = (RSMNode) path.getLastPathComponent();
        
        // check if the node contains a GlassHourNode (ie : children are not loaded)
        if (nodeExpanded.getChildCount() > 0) {
            RSMNode childNode = (RSMNode) nodeExpanded.getChildAt(0);
            if (childNode.getType() == RSMNode.NodeTypes.HOUR_GLASS) {
                
                startLoading(nodeExpanded);
            }
        }
        
    }
    

    public void startLoading() {
        
        if (m_loadingDone) {
            return;
        }
        
        m_loadingDone = true;
        
        // add hourglass node
        RSMNode root = (RSMNode) m_model.getRoot(); 
        m_model.insertNodeInto(new RSMHourGlassNode(null), root, 0);
        
        
        
        // show loading and start the loading
        expandRow(0);
        

    }

    
    
    /**
     * Load a node if needed. The node will not be expanded, but the data will be used
     * @param nodeToLoad 
     */
    public void loadInBackground(final RSMNode nodeToLoad, final AbstractDatabaseCallback parentCallback ) {

        // check if the loading is necessary :
        // it is necessary only if we have an hour glass child
        // which correspond to data not already loaded
        if (nodeToLoad.getChildCount() == 0) {
            parentCallback.run(true, 0, null, true);
            return;
        }
        RSMNode childNode = (RSMNode) nodeToLoad.getChildAt(0);
        if (childNode.getType() != RSMNode.NodeTypes.HOUR_GLASS) {
            parentCallback.run(true, 0, null, true);
            return;
        }

        // register hour glass which is expanded
        loadingMap.put(nodeToLoad.getData(), nodeToLoad);

        final ArrayList<AbstractData> childrenList = new ArrayList<>();
        final AbstractData parentData = nodeToLoad.getData();


        // Callback used only for the synchronization with the AccessDatabaseThread
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        
                        dataLoaded(parentData, childrenList);
                        
                        // use of childrenList / parentData
                       parentCallback.run(true, 0, null, true);
                    }
                });

            }
        };

        

        parentData.load(callback, childrenList);
    }
    
    public void moveToTrash(RSMNode[] selectedNodes) {

        int nbSelectedNode = selectedNodes.length;
        if (nbSelectedNode==0) {
            return; // should not happen
        }
        

            // we must keep only parent nodes
            // if a child and its parent are selected, we keep only the parent
            ArrayList<RSMNode> keptNodes = new ArrayList<>(nbSelectedNode);
            keptNodes.add(selectedNodes[0]);
            mainloop:
            for (int i=1;i<nbSelectedNode;i++) {
                RSMNode curNode = selectedNodes[i];
                
                // look for an ancestor
                int nbKeptNodes = keptNodes.size();
                for (int j=0;j<nbKeptNodes;j++) {
                    
                    RSMNode curKeptNode = keptNodes.get(j);
                    if (curNode.isNodeAncestor(curKeptNode)) {
                        // ancestor is already in kept node
                        continue mainloop;
                    }
                }
                // look for children and remove them
                for (int j=nbKeptNodes-1;j>=0;j--) {
                    
                    RSMNode curKeptNode = keptNodes.get(j);
                    if (curKeptNode.isNodeAncestor(curNode)) {
                        // we have found a children
                        keptNodes.remove(j);
                    }
                }
                keptNodes.add(curNode);
 
            }

        
        
        // search Project Node
        RSMProjectIdentificationNode projectNode = null;
        RSMNode parentNodeCur = (RSMNode) keptNodes.get(0).getParent();
        while (parentNodeCur != null) {
            if (parentNodeCur instanceof RSMProjectIdentificationNode) {
                projectNode = (RSMProjectIdentificationNode) parentNodeCur;
                break;
            }
            parentNodeCur = (RSMNode) parentNodeCur.getParent();
        }

        if (projectNode == null) {
            return; // should not happen
        }

        // search trash (should be at the end)
        RSMDataSetNode trash = null;
        int nbChildren = projectNode.getChildCount();
        for (int i = nbChildren - 1; i >= 0; i--) {
            RSMNode childNode = (RSMNode) projectNode.getChildAt(i);
            if (childNode instanceof RSMDataSetNode) {
                RSMDataSetNode datasetNode = (RSMDataSetNode) childNode;
                if (datasetNode.isTrash()) {
                    trash = datasetNode;
                    break;
                }
            }
        }
        if (trash == null) {
            return; // should not happen
        }

        // move node to Trash
        LinkedHashSet<RSMNode> allParentNodeModified = new LinkedHashSet<>();


        int nbKeptNodes = keptNodes.size();
        for (int i=0;i<nbKeptNodes;i++) {
            RSMNode nodeCur = keptNodes.get(i);
            allParentNodeModified.add((RSMNode) nodeCur.getParent());
            m_model.removeNodeFromParent(nodeCur);
            m_model.insertNodeInto(nodeCur, trash, trash.getChildCount());
        }

        // Trash must be added at the end : because the add of dataset must be done
        // after children have been removed from their parents
        allParentNodeModified.add(trash);

        LinkedHashMap<Object, ArrayList<DDataset>> databaseObjectsToModify = new LinkedHashMap<>();
        //HashSet<RSMDataSetNode> nodeToBeChanged = new HashSet<>();
        
        Iterator<RSMNode> it = allParentNodeModified.iterator();
        while (it.hasNext()) {

            RSMNode parentNode = it.next();

            // get Parent Database Object
            Object databaseParentObject = null;
            RSMNode.NodeTypes type = parentNode.getType();
            if (type == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode datasetNode = ((RSMDataSetNode) parentNode);
                databaseParentObject = datasetNode.getDataset();
                //nodeToBeChanged.add(datasetNode);
            } else if (type == RSMNode.NodeTypes.PROJECT_IDENTIFICATION) {
                RSMProjectIdentificationNode projectNodeS = ((RSMProjectIdentificationNode) parentNode);
                databaseParentObject = projectNodeS.getProject();
            }



            // get new Dataset children
            int nb = parentNode.getChildCount();
            ArrayList<DDataset> datasetList = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                // we are sure that it is a Dataset
                RSMNode childNode = ((RSMNode) parentNode.getChildAt(i));
                if (childNode instanceof RSMDataSetNode) {
                    DDataset dataset = ((RSMDataSetNode)childNode).getDataset();
                    datasetList.add(dataset);
                    //nodeToBeChanged.add((RSMDataSetNode)childNode);
                } else if (childNode instanceof RSMHourGlassNode) {
                    // potential bug
                    //JPM.TODO ??? (should not happen)
                }
            }

            // register this modification
            databaseObjectsToModify.put(databaseParentObject, datasetList);
        }

        // ask the modification to the database at once (intricate to put in a thread in Dnd context)
        DatabaseDataSetTask.updateDatasetAndProjectsTree(databaseObjectsToModify);


        
    }
        
    


    
    public void expandNodeIfNeeded(RSMNode n) {
        final TreePath pathToExpand = new TreePath(n.getPath());
        if (!isExpanded(pathToExpand)) {

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    expandPath(pathToExpand);
                }
            });
        }
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

        // check if the Root node or Trash or a Node in Trash is selected
        //boolean rootNodeSelected = false;
        boolean trashNodeSelected = false;
        boolean allImportedNodeSelected = false;
        for (int i=0;i<nbNodes;i++) {
            RSMNode n = selectedNodes[i];
            /*if (n.isRoot()) {
                rootNodeSelected = true;
            }*/

            if (n instanceof RSMDataSetNode) {
                RSMDataSetNode datasetNode = (RSMDataSetNode) n;
                if (datasetNode.isTrash()) {
                    trashNodeSelected = true;
                } else if (datasetNode.isInTrash()) {
                    // no popup on nodes in trash
                    return;
                }
            } else if (n instanceof RSMAllImportedNode) {
                allImportedNodeSelected = true;
            }
        }
        
                
        // check if nodes are changing
        if ((nbNodes!=1) || (!allImportedNodeSelected)) {
            for (int i = 0; i < nbNodes; i++) {
                if (selectedNodes[i].isChanging()) {
                    // do not show a popup on a node which is changing
                    // except for All Imported Node
                    return;
                }
            }
        }
        
        
        JPopupMenu popup;
        ArrayList<AbstractRSMAction> actions;
        
        /*if (rootNodeSelected && (nbNodes >= 1)) {
            if (nbNodes > 1) {
                // the root node is selected and multiple nodes are
                // selected : we do not show the popup
                return;
            }

            // we show the popup to connect or disconnect
            if (m_rootPopup == null) {
                // create the actions
                m_rootActions = new ArrayList<>(4);  // <--- get in sync

                AddProjectAction addProjectAction = new AddProjectAction();
                m_rootActions.add(addProjectAction);
                
                m_rootActions.add(null);  // separator
                
                
                ConnectAction connectAction = new ConnectAction();
                m_rootActions.add(connectAction);

                DisconnectAction disconnectAction = new DisconnectAction();
                m_rootActions.add(disconnectAction);

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
            
        } else */
        if (trashNodeSelected && (nbNodes == 1)) {
            
            // creation of the popup if needed
            if (m_trashPopup == null) {
                 // create the actions
                m_trashActions = new ArrayList<>(1);  // <--- get in sync
                
                EmptyTrashAction emtpyTrashAction = new EmptyTrashAction();
                m_trashActions.add(emtpyTrashAction);
                
                m_trashPopup = new JPopupMenu();
                
                m_trashPopup.add(emtpyTrashAction.getPopupPresenter());
            }
            
            popup = m_trashPopup;
            actions = m_trashActions;
            
        } else if (allImportedNodeSelected && (nbNodes == 1)) {
            
            // creation of the popup if needed
            if (m_allImportedPopup == null) {
                 // create the actions
                m_allImportedActions = new ArrayList<>(3);  // <--- get in sync
   

                
                DisplayAllRsetAction allRsetAction = new DisplayAllRsetAction();
                m_allImportedActions.add(allRsetAction);

                m_allImportedActions.add(null);
                
                ImportSearchResultAsRsetAction importAction = new ImportSearchResultAsRsetAction();
                m_allImportedActions.add(importAction);
                
                m_allImportedPopup = new JPopupMenu();
                
                for (int i = 0; i < m_allImportedActions.size(); i++) {
                    AbstractRSMAction action = m_allImportedActions.get(i);
                    if (action == null) {
                        m_allImportedPopup.addSeparator();
                    } else {
                        m_allImportedPopup.add(action.getPopupPresenter());
                    }
                }
            }
 
            popup = m_allImportedPopup;
            actions = m_allImportedActions;
            
        } else {


            // creation of the popup if needed
            if (m_mainPopup == null) {

                // create the actions
                m_mainActions = new ArrayList<>(11);  // <--- get in sync

                DisplayRsetAction displayRsetAction = new DisplayRsetAction();
                m_mainActions.add(displayRsetAction);
                
                DisplayRsmAction displayRsmAction = new DisplayRsmAction();
                m_mainActions.add(displayRsmAction);


                PropertiesAction propertiesAction = new PropertiesAction();
                m_mainActions.add(propertiesAction);

                
                m_mainActions.add(null);  // separator

                AddAction addAction = new AddAction();
                m_mainActions.add(addAction);
                
                MergeAction mergeAction = new MergeAction();
                m_mainActions.add(mergeAction);
                
                ValidateAction validateAction = new ValidateAction();
                m_mainActions.add(validateAction);
                
                ChangeTypicalProteinAction changeTypicalProteinAction = new ChangeTypicalProteinAction();
                m_mainActions.add(changeTypicalProteinAction);
                
                //CompareWithSCAction computeSCAction = new CompareWithSCAction(); //JPM.BETA : removed Spectral count for beta
                //m_mainActions.add(computeSCAction); 
                
                SpectralCountAction spectralCountAction = new SpectralCountAction();
                m_mainActions.add(spectralCountAction); 
                  
                //RetrieveSCDataAction getSCAction = new RetrieveSCDataAction();
                //m_mainActions.add(getSCAction); 
                
                m_mainActions.add(null);  // separator
                
                ExportRSMAction exportRSMAction = new ExportRSMAction();
                m_mainActions.add(exportRSMAction);                        
                
                m_mainActions.add(null);  // separator

                
                ChangeDescriptionAction changeDescriptionAction = new ChangeDescriptionAction();
                m_mainActions.add(changeDescriptionAction);
                
                RenameAction renameAction = new RenameAction();
                m_mainActions.add(renameAction);

                
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
                //mainPopup.add(new DoItAction());
                List<Action> additionalActions = ActionRegistry.getInstance().getActions(DDataset.class);
                if(additionalActions != null) {
                    for (Action action : additionalActions)
                        m_mainPopup.add(new DatasetWrapperAction(((DatasetAction)action)));
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
    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRSMAction> m_mainActions;
    //private JPopupMenu m_rootPopup;
    //private ArrayList<AbstractRSMAction> m_rootActions;
    private JPopupMenu m_trashPopup;
    private ArrayList<AbstractRSMAction> m_trashActions;
    private JPopupMenu m_allImportedPopup;
    private ArrayList<AbstractRSMAction> m_allImportedActions;
    
    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        // nothing to do
    }

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


    public static class RSMTreeRenderer extends DefaultTreeCellRenderer {

        public RSMTreeRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            ImageIcon icon = ((RSMNode) value).getIcon();
            if (icon != null) {
                setIcon(icon);
            }

            return this;
        }
    }
    
    public static class RSMTreeCellEditor extends DefaultTreeCellEditor {
        public RSMTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
            super(tree, renderer);
        }
        
        @Override
        protected void determineOffset(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true);
            editingIcon = renderer.getIcon();
            
            if(editingIcon != null) {
                offset = renderer.getIconTextGap() + editingIcon.getIconWidth();
            } else {
                offset = renderer.getIconTextGap();
            }
        }
    }
    
    /*private class RSMTreeModel extends DefaultTreeModel {

        public RSMTreeModel(TreeNode root) {
            super(root, false);
        }


        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            RSMNode rsmNode = (RSMNode) path.getLastPathComponent();

            if (rsmNode.getType()== RSMNode.NodeTypes.PROJECT_IDENTIFICATION) {
                RSMProjectIdentificationNode projectNode = (RSMProjectIdentificationNode) rsmNode;
                Project project = projectNode.getProject();
                ((RSMProjectIdentificationNode) rsmNode).changeNameAndDescription(newValue.toString(), project.getDescription());
            } else if (rsmNode.getType()== RSMNode.NodeTypes.DATA_SET) {
                ((RSMDataSetNode) rsmNode).rename(newValue.toString());
            }
        }
    }*/

    
}
