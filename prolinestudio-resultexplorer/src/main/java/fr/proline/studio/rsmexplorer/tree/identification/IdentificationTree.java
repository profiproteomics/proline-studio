/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.tree.identification;

import fr.proline.studio.rsmexplorer.actions.identification.*;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.ChildFactory;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.HourGlassNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import org.apache.commons.lang3.StringUtils;
import fr.proline.studio.NbPreferences;

/**
 * Tree of projects and datasets (identification type)
 *
 * @author JM235353
 */
public class IdentificationTree extends AbstractTree implements TreeWillExpandListener {

    private boolean m_isMainTree;
    private static boolean emptyFound = false;

    private static HashMap<ProjectIdentificationData, IdentificationTree> m_treeMap = new HashMap<>();
    private static IdentificationTree m_currentTree = null;

    private boolean m_loadingDone = false;

    public static IdentificationTree getTree(ProjectIdentificationData projectData) {

        IdentificationTree tree = m_treeMap.get(projectData);
        if (tree == null) {
            tree = new IdentificationTree(projectData);
            m_treeMap.put(projectData, tree);
        }

        m_currentTree = tree;

        return tree;
    }

    public static IdentificationTree getCurrentTree() {
        return m_currentTree;
    }

    public static boolean renameNode(DDataset dataset, String naming, DataSetNode datasetNode, AbstractTree tree) {

        if (dataset == null || dataset.getResultSet() == null || dataset.getResultSet().getMsiSearch() == null) {
            return false;
        }

        String newName = (dataset.getResultSet().getMsiSearch().getResultFileName() == null) ? "" : dataset.getResultSet().getMsiSearch().getResultFileName();
        
        if (newName.contains(".")) {
            newName = newName.substring(0, newName.indexOf("."));
        }
        
        if (naming.equalsIgnoreCase(ImportManager.SEARCH_RESULT_NAME_SOURCE)) {
            newName = dataset.getResultSet().getName();
        } else if (naming.equalsIgnoreCase(ImportManager.MASCOT_DAEMON_RULE)) {
            String nominee = dataset.getResultSet().getName();
            int leftBrackets = StringUtils.countMatches(nominee, "(");
            int rightBrackets = StringUtils.countMatches(nominee, ")");

            if (leftBrackets == rightBrackets && leftBrackets == 1 && nominee.lastIndexOf(")") > nominee.lastIndexOf("(")) {
                newName = nominee.substring(nominee.lastIndexOf("(") + 1, nominee.lastIndexOf(")"));
            } else {
                newName = nominee;
            }

        } else if (naming.equalsIgnoreCase(ImportManager.PEAKLIST_PATH_SOURCE)) {
            newName = (dataset.getResultSet().getMsiSearch().getPeaklist().getPath() == null) ? "" : dataset.getResultSet().getMsiSearch().getPeaklist().getPath();
            if (newName.contains(File.separator)) {
                newName = newName.substring(newName.lastIndexOf(File.separator) + 1);
            }
        }

        if (!newName.equalsIgnoreCase("")) {
            datasetNode.rename(newName, m_currentTree);
            dataset.setName(newName);
            m_currentTree.rename(datasetNode, newName);
            return false;
        } else {
            return true;
        }

    }

    public static boolean renameTreeNodes(AbstractNode root, IdentificationTree tree) {

        emptyFound = false;

        Preferences preferences = NbPreferences.root();
        String naming = preferences.get(ImportManager.DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY, ImportManager.MSI_SEARCH_FILE_NAME_SOURCE);

        Enumeration<TreeNode> e = root.depthFirstEnumeration();

        while (e.hasMoreElements()) {

            // TODO avoid this cast due to AbstractNode API modification (jdk13)
            AbstractNode currentElement = (AbstractNode)e.nextElement();

            if (currentElement.getType() == AbstractNode.NodeTypes.DATA_SET) {

                DataSetData datasetData = (DataSetData) currentElement.getData();

                if (datasetData.hasChildren()) {
                    continue;
                }

                DDataset dataset = datasetData.getDataset();

                DataSetNode node = (DataSetNode) currentElement;

                // we have to load the result set
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                        if (IdentificationTree.renameNode(dataset, naming, node, m_currentTree)) {
                            emptyFound = true;
                        }
                    }
                };

                if (dataset.getResultSet() == null) {
                    // ask asynchronous loading of data
                    DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                    task.initLoadRsetAndRsm(dataset);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                } else {

                    if (IdentificationTree.renameNode(dataset, naming, node, m_currentTree)) {
                        emptyFound = true;
                    }

                }
            }
        }

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.reload(root);

        return emptyFound;
    }

    private IdentificationTree(ProjectIdentificationData projectData) {

        setEditable(true);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        IdTransferHandler handler = new IdTransferHandler();
        setTransferHandler(handler);

        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);

        m_isMainTree = true;

        // Model of the tree
        AbstractNode top = ChildFactory.createNode(projectData);

        initTree(top);

        startLoading(top, true);

    }

    public IdentificationTree(AbstractNode top) {

        m_isMainTree = false;

        initTree(top);

        startLoading(top, true);

    }

    @Override
    protected final void initTree(AbstractNode top) {
        super.initTree(top);

        addTreeWillExpandListener(this);
    }

    public static void clearAll() {
        m_treeMap.clear();
    }

    public static HashMap<ProjectIdentificationData, IdentificationTree> getTreeMap() {
        return m_treeMap;
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
                return !((dataSetNode.isInTrash()) || (dataSetNode.isChanging()));
            } else if (nodeType == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                return true;
            }
        }
        return false;
    }

    public IdentificationTree copyResultSetRootSubTree(ResultSet rset, long projectId) {

        long rsetId = rset.getId();

        DataSetNode rsetNode = findResultSetNode((AbstractNode) m_model.getRoot(), rsetId, projectId);

        if (rsetNode == null) {
            return null;
        }

        return new IdentificationTree(rsetNode);

    }

    public IdentificationTree copyDataSetRootSubTree(DDataset dset, long projectId) {

        long dsetId = dset.getId();
        DataSetNode dsetNode = findDataSetNode((AbstractNode) m_model.getRoot(), dsetId, projectId);

        if (dsetNode == null) {
            return null;
        }

        return new IdentificationTree(dsetNode);

    }

    public AbstractNode copyRootNodeForSelection() {
        AbstractNode node = (AbstractNode) m_model.getRoot();
        return node.copyNode();

    }

    private DataSetNode findDataSetNode(AbstractNode node, long dsetId, long projectId) {

        int nbChildren = node.getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            AbstractNode childNode = (AbstractNode) node.getChildAt(i);
            AbstractNode.NodeTypes childType = childNode.getType();
            if (childType == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dataSetNode = ((DataSetNode) childNode);
                Long datasetId = dataSetNode.getDataset().getId();
                if ((datasetId != null) && (datasetId.longValue() == dsetId)) {
                    return dataSetNode;
                }
            } else if (childType == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                IdProjectIdentificationNode projectNode = ((IdProjectIdentificationNode) childNode);
                if (projectNode.getProject().getId() != projectId) {
                    // we are not in the right project
                    continue;
                }
            }
            if (!childNode.isLeaf()) {
                DataSetNode dataSetNode = findDataSetNode(childNode, dsetId, projectId);
                if (dataSetNode != null) {
                    return dataSetNode;
                }
            }

        }

        return null;
    }

    private DataSetNode findResultSetNode(AbstractNode node, long rsetId, long projectId) {

        int nbChildren = node.getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            AbstractNode childNode = (AbstractNode) node.getChildAt(i);
            AbstractNode.NodeTypes childType = childNode.getType();
            if (childType == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dataSetNode = ((DataSetNode) childNode);
                Long resultSetId = dataSetNode.getResultSetId();

                if ((resultSetId != null) && (resultSetId.intValue() == rsetId)) {
                    return dataSetNode;
                }
            } else if (childType == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                IdProjectIdentificationNode projectNode = ((IdProjectIdentificationNode) childNode);
                if (projectNode.getProject().getId() != projectId) {
                    // we are not in the right project
                    continue;
                }
            }
            if (!childNode.isLeaf()) {
                DataSetNode dataSetNode = findResultSetNode(childNode, rsetId, projectId);
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
        AbstractNode nodeExpanded = (AbstractNode) path.getLastPathComponent();

        // check if the node contains a GlassHourNode (ie : children are not loaded)
        if (nodeExpanded.getChildCount() > 0) {
            AbstractNode childNode = (AbstractNode) nodeExpanded.getChildAt(0);
            if (childNode.getType() == AbstractNode.NodeTypes.HOUR_GLASS) {

                startLoading(nodeExpanded, true);
            }
        }

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

    /**
     * Load a node if needed. The node will not be expanded, but the data will
     * be used
     *
     * @param nodeToLoad
     */
    public void loadInBackground(final AbstractNode nodeToLoad, final AbstractDatabaseCallback parentCallback) {

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
        m_loadingMap.put(nodeToLoad.getData(), nodeToLoad);

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

        parentData.load(callback, childrenList, true);
    }

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
        IdProjectIdentificationNode projectNode = null;
        AbstractNode parentNodeCur = (AbstractNode) keptNodes.get(0).getParent();
        while (parentNodeCur != null) {
            if (parentNodeCur instanceof IdProjectIdentificationNode) {
                projectNode = (IdProjectIdentificationNode) parentNodeCur;
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
        //HashSet<RSMDataSetNode> nodeToBeChanged = new HashSet<>();

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
            } else if (type == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                IdProjectIdentificationNode projectNodeS = ((IdProjectIdentificationNode) parentNode);
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
                    //nodeToBeChanged.add((DataSetNode)childNode);
                } else if (childNode instanceof HourGlassNode) {
                    // potential bug
                    //JPM.TODO ??? (should not happen)
                }
            }

            // register this modification
            databaseObjectsToModify.put(databaseParentObject, datasetList);
        }

        // ask the modification to the database at once (intricate to put in a thread in Dnd context)
        DatabaseDataSetTask.updateDatasetAndProjectsTree(databaseObjectsToModify, true);

    }

    @Override
    public void rename(AbstractNode rsmNode, String newName) {
        if (rsmNode.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
            IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) rsmNode;
            Project project = projectNode.getProject();
            ((IdProjectIdentificationNode) rsmNode).changeNameAndDescription(newName, project.getDescription());
        } else if (rsmNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
            ((DataSetNode) rsmNode).rename(newName, IdentificationTree.getCurrentTree());
        }
    }

    private void triggerPopup(MouseEvent e) {

        // retrieve selected nodes
        AbstractNode[] selectedNodes = getSelectedNodes();

        int nbNodes = selectedNodes.length;
        
        if (nbNodes == 0) {
            // nothing selected
            return;
        }

        // check if the Root node or Trash or a Node in Trash is selected
        //boolean rootNodeSelected = false;
        boolean trashNodeSelected = false;
        boolean allImportedNodeSelected = false;
        for (int i = 0; i < nbNodes; i++) {
            AbstractNode n = selectedNodes[i];
            /*if (n.isRoot()) {
             rootNodeSelected = true;
             }*/

            if (n instanceof DataSetNode) {
                DataSetNode datasetNode = (DataSetNode) n;
                if (datasetNode.isTrash()) {
                    trashNodeSelected = true;
                } else if (datasetNode.isInTrash()) {
                    // no popup on nodes in trash
                    return;
                }
            } else if (n instanceof IdAllImportedNode) {
                allImportedNodeSelected = true;
            }
        }

        // check if nodes are changing
        if ((nbNodes != 1) || (!allImportedNodeSelected)) {
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

        if (trashNodeSelected && (nbNodes == 1)) {

            // creation of the popup if needed
            if (m_trashPopup == null) {
                // create the actions
                m_trashActions = new ArrayList<>(1);  // <--- get in sync

                EmptyTrashAction emtpyTrashAction = new EmptyTrashAction(this);
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
                m_allImportedActions = new ArrayList<>(6);  // <--- get in sync

                DisplayAllRsetAction allRsetAction = new DisplayAllRsetAction(this);
                m_allImportedActions.add(allRsetAction);

                m_allImportedActions.add(null);

                ImportSearchResultAsRsetJMSAction importJmsAction = new ImportSearchResultAsRsetJMSAction(this);
                m_allImportedActions.add(importJmsAction);


                m_allImportedPopup = new JPopupMenu();

                for (AbstractRSMAction action : m_allImportedActions) {
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
                Preferences preferences = NbPreferences.root();
                // create the actions
                Boolean showHiddenFunctionnality = false; // JPM.TODO : completely removed for the moment preferences.getBoolean("Profi", false);

                m_mainActions = new ArrayList<>(24);  // <--- get in sync


                DisplayRsetAction displayRsetAction = new DisplayRsetAction(this);
                m_mainActions.add(displayRsetAction);

                DisplayRsmAction displayRsmAction = new DisplayRsmAction(this);
                m_mainActions.add(displayRsmAction);

                DisplayPTMRsmAction displayPtmRsmAction = new DisplayPTMRsmAction(this);
                m_mainActions.add(displayPtmRsmAction);

                m_mainActions.add(null);  // separator

                AggregateAction aggregateAction = new AggregateAction(this);
                m_mainActions.add(aggregateAction);

                AddIdentificationFolderAction addFolderAction = new AddIdentificationFolderAction(this);
                m_mainActions.add(addFolderAction);
                
                m_mainActions.add(null);  // separator
                
                CopySearchResult copyAction = new CopySearchResult(this);
                m_mainActions.add(copyAction);
                
                PasteSearchResult pasteAction = new PasteSearchResult(this);
                m_mainActions.add(pasteAction);
                
                m_mainActions.add(null);  // separator

                RenameRsetAction renameRsetAction = new RenameRsetAction(this);
                m_mainActions.add(renameRsetAction);

                ClearDatasetAction clearAction = new ClearDatasetAction(this, false);
                m_mainActions.add(clearAction);

                ClearDatasetAction clearFullAction = new ClearDatasetAction(this, true);
                m_mainActions.add(clearFullAction);

                DeleteAction deleteAction = new DeleteAction(this);
                m_mainActions.add(deleteAction);

                m_mainActions.add(null);  // separator

                ImportSearchResultAsDatasetJMSAction identificationAction = new ImportSearchResultAsDatasetJMSAction(this);
                m_mainActions.add(identificationAction);
//                    if (showHiddenFunctionnality) {
                ImportMaxQuantResultJMSAction importMaxQuant = new ImportMaxQuantResultJMSAction(this);
                m_mainActions.add(importMaxQuant);
//                    }
                ValidateJMSAction validateJMSAction = new ValidateJMSAction(this);
                m_mainActions.add(validateJMSAction);
                
                MergeJMSAction mergeJmsAction = new MergeJMSAction(this);
                m_mainActions.add(mergeJmsAction);
                
                FilterRSMProteinSetsJMSAction filterProtSetAction = new FilterRSMProteinSetsJMSAction(this);
                m_mainActions.add(filterProtSetAction);
                
                ChangeTypicalProteinJMSAction changeTypicalProteinJmsAction = new ChangeTypicalProteinJMSAction(this);
                m_mainActions.add(changeTypicalProteinJmsAction);
                
                IdentifyPtmSitesJMSAction identifyPtmSitesAction = new IdentifyPtmSitesJMSAction(this);
                m_mainActions.add(identifyPtmSitesAction);
                
                m_mainActions.add(null);  // separator
                
                GenerateSpectrumMatchesJMSAction generateSpectrumMatchesAction = new GenerateSpectrumMatchesJMSAction(this);
                m_mainActions.add(generateSpectrumMatchesAction);
                
                UpdatePeaklistSoftwareAction updatePeaklistSoftAction = new UpdatePeaklistSoftwareAction(this);
                m_mainActions.add(updatePeaklistSoftAction);
                
                RetrieveBioSeqJMSAction retrieveBioSeqAction = new RetrieveBioSeqJMSAction(this);
                m_mainActions.add(retrieveBioSeqAction);

                m_mainActions.add(null);  // separator

                QuantifyAction quantifyAction = new QuantifyAction(this);
                m_mainActions.add(quantifyAction);

                m_mainActions.add(null);  // separator

                ExportAction exportAction = new ExportAction(this);
                m_mainActions.add(exportAction);

                m_mainActions.add(null);  // separator

                PropertiesAction propertiesAction = new PropertiesAction(this);
                m_mainActions.add(propertiesAction);

                // add actions to popup
                m_mainPopup = new JPopupMenu();
                for (AbstractRSMAction action : m_mainActions) {
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
        for (AbstractRSMAction action : actions) {
            if (action == null) {
                continue;
            }

            action.updateEnabled(selectedNodes);
        }

        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
    }

    public static void reinitMainPopup() {

        Iterator<ProjectIdentificationData> it = m_treeMap.keySet().iterator();
        while (it.hasNext()) {
            IdentificationTree tree = m_treeMap.get(it.next());
            tree.m_mainPopup = null;
        }
    }
    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRSMAction> m_mainActions;
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
            manageSelectionOnRightClick(e);

        } else if (e.getClickCount() == 2) {

            // display All imported rset on double click
            AbstractNode[] selectedNodes = getSelectedNodes();
            int nbNodes = selectedNodes.length;
            if (nbNodes == 1) {
                AbstractNode n = selectedNodes[0];
                if (n instanceof IdAllImportedNode) {
                    new DisplayAllRsetAction(this).actionPerformed(selectedNodes, e.getX(), e.getY());
                }
            }
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

    public static class RSMTreeRenderer extends DefaultTreeCellRenderer {

        public RSMTreeRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (((AbstractNode) value).isDisabled()) {
                l.setForeground(Color.LIGHT_GRAY);
            } else {
                if (sel) {
                    l.setForeground(Color.WHITE);
                } else {
                    l.setForeground(Color.BLACK);
                }
            }
            ImageIcon icon = ((AbstractNode) value).getIcon(expanded);
            if (icon != null) {
                setIcon(icon);
            }

            return this;
        }
    }

    private DataSetNode getTrashNode() {
        // search Project Node
        IdProjectIdentificationNode projectNode = null;
        TreeNode root = (TreeNode) m_model.getRoot();
        if (root instanceof IdProjectIdentificationNode) {
            projectNode = (IdProjectIdentificationNode) root;
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
        return trash;
    }

    public void loadTrash() {
        DataSetNode trashNode = getTrashNode();
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
            IdentificationTree.getCurrentTree().loadInBackground(trashNode, callback);
        }
    }
}
