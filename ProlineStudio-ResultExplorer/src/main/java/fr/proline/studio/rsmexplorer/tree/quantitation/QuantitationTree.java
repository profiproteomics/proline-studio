package fr.proline.studio.rsmexplorer.tree.quantitation;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.actions.identification.CreateXICAction;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.identification.DeleteAction;
import fr.proline.studio.rsmexplorer.actions.identification.DisplayRsmAction;
import fr.proline.studio.rsmexplorer.actions.identification.EmptyTrashAction;
import fr.proline.studio.rsmexplorer.actions.identification.PropertiesAction;
import fr.proline.studio.rsmexplorer.actions.identification.RenameAction;
import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.rsmexplorer.actions.identification.DisplayRsetAction;
import fr.proline.studio.rsmexplorer.actions.identification.ExportAction;
import fr.proline.studio.rsmexplorer.actions.identification.ExportDatasetAction;
import fr.proline.studio.rsmexplorer.actions.identification.ExportDatasetJMSAction;
import fr.proline.studio.rsmexplorer.actions.xic.ComputeQuantitationProfileAction;
import fr.proline.studio.rsmexplorer.actions.xic.DisplayExperimentalDesignAction;
import fr.proline.studio.rsmexplorer.actions.xic.DisplayXICAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.*;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNode;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Quantitation tree (Spectral count + XIC dataset)
 * @author JM235353
 */
public class QuantitationTree extends AbstractTree implements TreeWillExpandListener {

    private boolean m_isMainTree;
    private static HashMap<ProjectQuantitationData, QuantitationTree> m_treeMap = new HashMap<>();
    private static QuantitationTree m_currentTree = null;
    private boolean m_loadingDone = false;
    private JPopupMenu m_rootPopup;
    private ArrayList<AbstractRSMAction> m_rootActions;
    private JPopupMenu m_mainPopup;
    private JPopupMenu m_multiPopup;
    private ArrayList<AbstractRSMAction> m_multiActions;
    private ArrayList<AbstractRSMAction> m_mainActions;
    private JPopupMenu m_trashPopup;
    private ArrayList<AbstractRSMAction> m_trashActions;
    private JPopupMenu m_identPopup;
    private ArrayList<AbstractRSMAction> m_identActions;

    public static QuantitationTree getTree(ProjectQuantitationData projectData) {

        QuantitationTree tree = m_treeMap.get(projectData);
        if (tree == null) {
            tree = new QuantitationTree(projectData);
            m_treeMap.put(projectData, tree);
        }

        m_currentTree = tree;

        return tree;
    }

    private QuantitationTree(ProjectQuantitationData projectData) {

        setEditable(true);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        QuantiTransferHandler handler = new QuantiTransferHandler();
        setTransferHandler(handler);

        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);

        m_isMainTree = true;

        // Model of the tree
        AbstractNode top = ChildFactory.createNode(projectData);

        initTree(top);

        startLoading(top, false);

    }

    @Override
    protected final void initTree(AbstractNode top) {
        super.initTree(top);

        addTreeWillExpandListener(this);
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


        if ((nbNodes == 0)) {
            return;
        }
        
        
        AbstractNode n = selectedNodes[0];
        boolean isRootPopup = n.isRoot();

        // check if the Root node or Trash or a Node in Trash is selected
        //boolean rootNodeSelected = false;
        boolean trashNodeSelected = false;
        if (n instanceof DataSetNode) {
            DataSetNode datasetNode = (DataSetNode) n;
            if (datasetNode.isTrash()) {
                trashNodeSelected = true;
            } else if (datasetNode.isInTrash()) {
                // no popup on nodes in trash
                return;
            }
        }
        // check if nodes are changing
        //check if xicSampleAnanlysisNode are selected
        boolean xicSampleAnalysisNodeSelected = false;
        for (int i = 0; i < nbNodes; i++) {
            if (selectedNodes[i].isChanging()) {
                // do not show a popup on a node which is changing
                return;
            }
            if (selectedNodes[i] instanceof XICBiologicalGroupNode || selectedNodes[i] instanceof XICBiologicalSampleNode || selectedNodes[i] instanceof XICRunNode ){
                // do not show  a popup on biological information
                return ;
            }
            if (selectedNodes[i] instanceof XICBiologicalSampleAnalysisNode){
                xicSampleAnalysisNodeSelected = true;
            }
        }
        JPopupMenu popup;
        ArrayList<AbstractRSMAction> actions;

        if ((nbNodes > 1) && !xicSampleAnalysisNodeSelected){
            if (m_multiPopup == null) {
                boolean isJSMDefined = JMSConnectionManager.getJMSConnectionManager().isJMSDefined();
                // create the actions
                m_multiActions = new ArrayList<>(3);  // <--- get in sync
                
                if (isJSMDefined) {
                    ExportDatasetJMSAction exportDatasetAction = new ExportDatasetJMSAction(AbstractTree.TreeType.TREE_QUANTITATION, true);
                    m_multiActions.add(exportDatasetAction);
                }else{
                    ExportDatasetAction exportDatasetAction = new ExportDatasetAction(AbstractTree.TreeType.TREE_QUANTITATION, true);
                    m_multiActions.add(exportDatasetAction);
                }   
                
                m_multiActions.add(null);  // separator
                
                PropertiesAction propertiesAction = new PropertiesAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_multiActions.add(propertiesAction);
                
                // add actions to popup
            }
            m_multiPopup = new JPopupMenu();
                for (int i = 0; i < m_multiActions.size(); i++) {
                    AbstractRSMAction action = m_multiActions.get(i);
                    if (action == null) {
                        m_multiPopup.addSeparator();
                    } else {
                        m_multiPopup.add(action.getPopupPresenter());
                    }
                }
            popup = m_multiPopup;
            actions = m_multiActions;
        }else{
        
        if (isRootPopup) {
            if (m_rootPopup == null) {
                // create the actions
                m_rootActions = new ArrayList<>(1);  // <--- get in sync
                boolean isJSMDefined = JMSConnectionManager.getJMSConnectionManager().isJMSDefined();
                CreateXICAction createXICAction = new CreateXICAction(isJSMDefined);

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
        } else if (trashNodeSelected) {

            // creation of the popup if needed
            if (m_trashPopup == null) {
                // create the actions
                m_trashActions = new ArrayList<>(1);  // <--- get in sync

                EmptyTrashAction emtpyTrashAction = new EmptyTrashAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_trashActions.add(emtpyTrashAction);

                m_trashPopup = new JPopupMenu();

                m_trashPopup.add(emtpyTrashAction.getPopupPresenter());
            }

            popup = m_trashPopup;
            actions = m_trashActions;
        } else if (xicSampleAnalysisNodeSelected){
            // creation of the popup if needed
            if (m_identPopup == null){
                
                m_identActions = new ArrayList<>(6);  // <--- get in sync

                boolean isJMSDefined = JMSConnectionManager.getJMSConnectionManager().isJMSDefined();

                DisplayRsetAction displayRsetAction = new DisplayRsetAction(AbstractTree.TreeType.TREE_QUANTITATION, isJMSDefined);
                m_identActions.add(displayRsetAction);

                DisplayRsmAction displayRsmAction = new DisplayRsmAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_identActions.add(displayRsmAction);
                
                m_identActions.add(null);  // separator
                
                ExportAction exportAction = new ExportAction(AbstractTree.TreeType.TREE_QUANTITATION, isJMSDefined);
                m_identActions.add(exportAction);
                
                
                m_identActions.add(null);  // separator
                
                PropertiesAction propertiesAction = new PropertiesAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_identActions.add(propertiesAction);
                
                // add actions to popup
                m_identPopup = new JPopupMenu();
                for (int i = 0; i < m_identActions.size(); i++) {
                    AbstractRSMAction action = m_identActions.get(i);
                    if (action == null) {
                        m_identPopup.addSeparator();
                    } else {
                        m_identPopup.add(action.getPopupPresenter());
                    }
                }
            }
            popup = m_identPopup;
            actions = m_identActions;
        }else {
            if (m_mainPopup == null) {
                // create the actions
                m_mainActions = new ArrayList<>(13);  // <--- get in sync
                boolean isJSMDefined = JMSConnectionManager.getJMSConnectionManager().isJMSDefined();
                
                DisplayXICAction displayXICAction = new DisplayXICAction();
                m_mainActions.add(displayXICAction);
                
                DisplayRsmAction displayRsmAction = new DisplayRsmAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_mainActions.add(displayRsmAction);
                
                DisplayExperimentalDesignAction expDesignAction = new DisplayExperimentalDesignAction();
                m_mainActions.add(expDesignAction);
                
                m_mainActions.add(null);  // separator
                
                RenameAction renameQuantitationAction = new RenameAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_mainActions.add(renameQuantitationAction);

                DeleteAction deleteAction = new DeleteAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_mainActions.add(deleteAction);
                
                m_mainActions.add(null);  // separator
                
                ComputeQuantitationProfileAction computeQuantProfileAction = new ComputeQuantitationProfileAction(isJSMDefined);
                m_mainActions.add(computeQuantProfileAction);
                
                CreateXICAction createXICAction = new CreateXICAction(true, isJSMDefined);
                m_mainActions.add(createXICAction);

                m_mainActions.add(null);  // separator
                
                if (isJSMDefined) {
                    ExportDatasetJMSAction exportDatasetAction = new ExportDatasetJMSAction(AbstractTree.TreeType.TREE_QUANTITATION, true);
                    m_mainActions.add(exportDatasetAction);
                }else{
                    ExportDatasetAction exportDatasetAction = new ExportDatasetAction(AbstractTree.TreeType.TREE_QUANTITATION, true);
                    m_mainActions.add(exportDatasetAction);
                }     
                
                m_mainActions.add(null);  // separator

                PropertiesAction propertiesAction = new PropertiesAction(AbstractTree.TreeType.TREE_QUANTITATION);
                m_mainActions.add(propertiesAction);
                
                

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
        }





        // update of the enable/disable state
        for (AbstractRSMAction action : actions) {
            if (action == null) {
                continue;
            }

            action.updateEnabled(selectedNodes);
        }

        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
    }

    
    public static void reinitMainPopup() {
        Iterator<ProjectQuantitationData> it = m_treeMap.keySet().iterator();
        while (it.hasNext()) {
            QuantitationTree tree = m_treeMap.get(it.next());
            tree.m_mainPopup = null;
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {
            manageSelectionOnRightClick(e);

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (m_isMainTree && SwingUtilities.isRightMouseButton(e)) {
            manageSelectionOnRightClick(e);
            triggerPopup(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

        TreePath path = event.getPath();
        AbstractNode nodeExpanded = (AbstractNode) path.getLastPathComponent();

        // check if the node contains a GlassHourNode (ie : children are not loaded)
        if (nodeExpanded.getChildCount() > 0) {
            AbstractNode childNode = (AbstractNode) nodeExpanded.getChildAt(0);
            if (childNode.getType() == AbstractNode.NodeTypes.HOUR_GLASS) {

                startLoading(nodeExpanded, false);
            }
        }

    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        // nothing to do
    }

    public void startLoading() {
        if (m_loadingDone) {
            return;
        }
        m_loadingDone = true;

        // add hourglass node
        AbstractNode root = (AbstractNode) m_model.getRoot();
        m_model.insertNodeInto(new HourGlassNode(null), root, 0);

        // show loading and start the loading
        expandRow(0);
    }

    public void removeRootChildren() {

        m_loadingDone = false;

        AbstractNode root = ((AbstractNode) m_model.getRoot());

        root.removeAllChildren();

        m_model.nodeStructureChanged(root);

    }

    @Override
    public boolean isPathEditable(TreePath path) {

        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            return false;
        }

        if (isEditable()) {

            if (path.getPathCount() == 1) {
                // root is not editable
                return false;
            }

            AbstractNode node = (AbstractNode) path.getLastPathComponent();
            AbstractNode.NodeTypes nodeType = node.getType();
            if (nodeType == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dataSetNode = (DataSetNode) node;
                if ((dataSetNode.isInTrash()) || (dataSetNode.isChanging())) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    // popUpMenu: delete (move to the quantitation trash)
    public void moveToTrash(AbstractNode[] selectedNodes) {

        int nbSelectedNode = selectedNodes.length;
        if (nbSelectedNode == 0) {
            return; // should not happen
        }


        // we must keep only parent nodes
        // if a child and its parent are selected, we keep only the parent
        ArrayList<AbstractNode> keptNodes = new ArrayList<>(nbSelectedNode);
        keptNodes.add(selectedNodes[0]);
        mainloop:
        for (int i = 1; i < nbSelectedNode; i++) {
            AbstractNode curNode = selectedNodes[i];

            // look for an ancestor
            int nbKeptNodes = keptNodes.size();
            for (int j = 0; j < nbKeptNodes; j++) {

                AbstractNode curKeptNode = keptNodes.get(j);
                if (curNode.isNodeAncestor(curKeptNode)) {
                    // ancestor is already in kept node
                    continue mainloop;
                }
            }
            // look for children and remove them
            for (int j = nbKeptNodes - 1; j >= 0; j--) {

                AbstractNode curKeptNode = keptNodes.get(j);
                if (curKeptNode.isNodeAncestor(curNode)) {
                    // we have found a children
                    keptNodes.remove(j);
                }
            }
            keptNodes.add(curNode);

        }



        // search Project Node
        QuantitationProjectNode projectNode = null;
        AbstractNode parentNodeCur = (AbstractNode) keptNodes.get(0).getParent();
        while (parentNodeCur != null) {
            if (parentNodeCur instanceof QuantitationProjectNode) {
                projectNode = (QuantitationProjectNode) parentNodeCur;
                break;
            }
            parentNodeCur = (AbstractNode) parentNodeCur.getParent();
        }

        if (projectNode == null) {
            return; // should not happen
        }

        // search trash (should be at the end)
        DataSetNode trash = null;
        int nbChildren = projectNode.getChildCount();
        for (int i = nbChildren - 1; i >= 0; i--) {
            AbstractNode childNode = (AbstractNode) projectNode.getChildAt(i);
            if (childNode instanceof DataSetNode) {
                DataSetNode datasetNode = (DataSetNode) childNode;
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
        LinkedHashSet<AbstractNode> allParentNodeModified = new LinkedHashSet<>();


        int nbKeptNodes = keptNodes.size();
        for (int i = 0; i < nbKeptNodes; i++) {
            AbstractNode nodeCur = keptNodes.get(i);
            allParentNodeModified.add((AbstractNode) nodeCur.getParent());
            m_model.removeNodeFromParent(nodeCur);
            m_model.insertNodeInto(nodeCur, trash, trash.getChildCount());
        }

        // Trash must be added at the end : because the add of dataset must be done
        // after children have been removed from their parents
        allParentNodeModified.add(trash);

        LinkedHashMap<Object, ArrayList<DDataset>> databaseObjectsToModify = new LinkedHashMap<>();

        Iterator<AbstractNode> it = allParentNodeModified.iterator();
        while (it.hasNext()) {

            AbstractNode parentNode = it.next();

            // get Parent Database Object
            Object databaseParentObject = null;
            AbstractNode.NodeTypes type = parentNode.getType();
            if (type == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode datasetNode = ((DataSetNode) parentNode);
                databaseParentObject = datasetNode.getDataset();
                //nodeToBeChanged.add(datasetNode);
            } else if (type == AbstractNode.NodeTypes.PROJECT_QUANTITATION) {
                QuantitationProjectNode projectNodeS = ((QuantitationProjectNode) parentNode);
                databaseParentObject = projectNodeS.getProject();
            }



            // get new Dataset children
            int nb = parentNode.getChildCount();
            ArrayList<DDataset> datasetList = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                // we are sure that it is a Dataset
                AbstractNode childNode = ((AbstractNode) parentNode.getChildAt(i));
                if (childNode instanceof DataSetNode) {
                    DDataset dataset = ((DataSetNode) childNode).getDataset();
                    datasetList.add(dataset);
                } else if (childNode instanceof HourGlassNode) {
                    // (should not happen)
                }
            }

            // register this modification
            databaseObjectsToModify.put(databaseParentObject, datasetList);
        }

        // ask the modification to the database at once (intricate to put in a thread in Dnd context)
        DatabaseDataSetTask.updateDatasetAndProjectsTree(databaseObjectsToModify, false);


    }
    
    // return the trash node
    private DataSetNode getTrashNode() {
        // search Project Node
        QuantitationProjectNode projectNode = null;
        TreeNode root = (TreeNode)m_model.getRoot();
        if (root instanceof QuantitationProjectNode) {
            projectNode = (QuantitationProjectNode) root;
        }

        if (projectNode == null) {
            return null; // should not happen
        }

        // search trash (should be at the end)
        DataSetNode trash = null;
        int nbChildren = projectNode.getChildCount();
        for (int i = nbChildren - 1; i >= 0; i--) {
            AbstractNode childNode = (AbstractNode) projectNode.getChildAt(i);
            if (childNode instanceof DataSetNode) {
                DataSetNode datasetNode = (DataSetNode) childNode;
                if (datasetNode.isTrash()) {
                    trash = datasetNode;
                    break;
                }
            }
        }
        if (trash == null) {
            return null; // should not happen
        }
        return trash ;
    }

    // load the trash node: called while loading project to display correctly the number of childs
    public void loadTrash() {
        DataSetNode trashNode = getTrashNode() ;
        if (trashNode != null) {
            // Callback used only for the synchronization with the AccessDatabaseThread
             AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return false;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
                }
            };
            QuantitationTree.getCurrentTree().loadInBackground(trashNode, callback);
        }
    }
    
    
    /**
     * Load a node if needed. The node will not be expanded, but the data will be used
     * @param nodeToLoad 
     */
    public void loadInBackground(final AbstractNode nodeToLoad, final AbstractDatabaseCallback parentCallback ) {

        // check if the loading is necessary :
        // it is necessary only if we have an hour glass child
        // which correspond to data not already loaded
        if (nodeToLoad.getChildCount() == 0) {
            parentCallback.run(true, 0, null, true);
            return;
        }
        AbstractNode childNode = (AbstractNode) nodeToLoad.getChildAt(0);
        if (childNode.getType() != AbstractNode.NodeTypes.HOUR_GLASS) {
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

        

        parentData.load(callback, childrenList, false);
    }
    
    public int getQuantitationChildCount(){
        AbstractNode root = (AbstractNode) m_model.getRoot();
        int nbC = root.getChildCount();
        int nb = 0;
        for (int i=0; i< nbC; i++) {
            TreeNode tnode = root.getChildAt(i);
            if (tnode instanceof DataSetNode) {
                DataSetNode dsnode = (DataSetNode)tnode;
                if (dsnode.isQuantitation()) {
                    nb++;
                }
            }
        }
        return nb;
    }
    
    public AbstractNode getQuantitationNode(int index) {
        AbstractNode root = (AbstractNode) m_model.getRoot();
        int nbC = root.getChildCount();
        for (int i=0; i< nbC; i++) {
            TreeNode tnode = root.getChildAt(i);
            if (tnode instanceof DataSetNode && index == i) {
                return (AbstractNode)tnode;
            }
        }
        return null;
    }
    
    public void clearQuantiSelection() {
        clearSelection();
    }
    
    
    /**
     * select or hightlight the nodes with a datasetId in the given list
     * @param listIds 
     */
    public void selectQuantiNodeWithId (List<Long> listIds) {
        AbstractNode root = (AbstractNode) m_model.getRoot();
        int nbC = root.getChildCount();
        for (int i=0; i< nbC; i++) {
            TreeNode tnode = root.getChildAt(i);
            if (tnode instanceof DataSetNode) {
                DataSetNode dsnode = (DataSetNode)tnode;
                DDataset dataset = dsnode.getDataset();
                if (listIds.contains(dataset.getId())) {
                    addSelectionRow(i+1);
                    //dsnode.setHighlighted(true);
                }
            }
        }
        getCurrentTree().updateUI();
    }
    
    public AbstractNode copyCurrentNodeForSelection() {
        AbstractNode[] nodes = getSelectedNodes();
        if (nodes != null && nodes.length > 0){
            return nodes[0].copyNode();
        }
        return null;

    }
    
    @Override
    protected void dataLoaded(AbstractData data, List<AbstractData> list) {

        AbstractNode parentNode = loadingMap.remove(data);


        parentNode.remove(0); // remove the first child which correspond to the hour glass


        int indexToInsert = 0;
        Iterator<AbstractData> it = list.iterator();
        while (it.hasNext()) {
            AbstractData dataCur = it.next();
            parentNode.insert(ChildFactory.createNode(dataCur), indexToInsert);
            indexToInsert++;
        }
        m_model.nodeStructureChanged(parentNode);

        if (parentNode instanceof DataSetNode && ((DataSetNode)parentNode).isQuantXIC()){
            XICDesignTree.setExpDesign(((DataSetNode)parentNode).getDataset(), parentNode,  this, false);
        }

    }
    
}
