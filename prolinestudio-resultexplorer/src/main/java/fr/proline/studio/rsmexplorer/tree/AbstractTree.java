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
package fr.proline.studio.rsmexplorer.tree;

import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.actions.identification.SetRsetNameAction;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.*;

/**
 * Base Class for all trees with identification or quantification dataset
 *
 * @author JM235353
 */
public abstract class AbstractTree extends JTree implements MouseListener {

    private static int ID_INC = 0;
    private int m_id = ID_INC++;

    private SetRsetNameAction m_subscribedRenamer;
    private int m_expected = -1;

    protected RSMTreeModel m_model;
    protected RSMTreeSelectionModel m_selectionModel;
    protected HashMap<AbstractData, AbstractNode> m_loadingMap = new HashMap<>();
    private HashSet<AbstractNode> m_loadingStartedMap = new HashSet<>();

    public int getId() {
        return m_id;
    }

    public void subscribeRenamer(SetRsetNameAction subscribedRenamer) {
        this.m_subscribedRenamer = subscribedRenamer;
    }

    public SetRsetNameAction getSubscribedRenamer() {
        return this.m_subscribedRenamer;
    }

    public void setExpected(int expected) {
        this.m_expected = expected;
    }

    protected void initTree(AbstractNode top) {
        m_model = new RSMTreeModel(this, top);
        setModel(m_model);

        m_selectionModel = new RSMTreeSelectionModel();
        setSelectionModel(m_selectionModel);

        // rendering of the tree
        putClientProperty("JTree.lineStyle", "Horizontal");
        setRowHeight(18);

        IdentificationTree.RSMTreeRenderer renderer = new IdentificationTree.RSMTreeRenderer();
        setCellRenderer(renderer);
        if (isEditable()) {
            setCellEditor(new RSMTreeCellEditor(this, renderer));
        }

        // -- listeners
        addMouseListener(this);         // used for popup triggering

        // add tooltips 
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    protected void startLoading(final AbstractNode nodeToLoad, final boolean identificationDataset) {

        // check if the loading is necessary :
        // it is necessary only if we have an hour glass child
        // which correspond to data not already loaded
        if (nodeToLoad.getChildCount() == 0) {
            return;
        }
        AbstractNode childNode = (AbstractNode) nodeToLoad.getChildAt(0);
        if (childNode.getType() != AbstractNode.NodeTypes.HOUR_GLASS) {
            return;
        }
        
        if (m_loadingStartedMap.contains(nodeToLoad)) {
            // user has already started the loading (can happens, when the user click on [+], then [-], then [+] during the loading
            return;
        }

        // register hour glass which is expanded
        m_loadingStartedMap.add(nodeToLoad);
        m_loadingMap.put(nodeToLoad.getData(), nodeToLoad);

        final ArrayList<AbstractData> childrenList = new ArrayList<>();
        final AbstractData parentData = nodeToLoad.getData();

        if (nodeToLoad.getType() == AbstractNode.NodeTypes.TREE_PARENT) {
            nodeToLoad.setIsChanging(true);
            m_model.nodeChanged(nodeToLoad);
        }

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

                        if (nodeToLoad.getType() == AbstractNode.NodeTypes.TREE_PARENT) {
                            nodeToLoad.setIsChanging(false);
                            m_model.nodeChanged(nodeToLoad);
                        }

                        dataLoaded(parentData, childrenList);

                        if (m_loadingMap.isEmpty() && (selectionFromrsmArray != null)) {
                            // A selection has been postponed, we do it now
                            setSelection(selectionFromrsmArray);
                            selectionFromrsmArray = null;
                        }

                        if (identificationDataset) {
                            IdentificationTree.getCurrentTree().loadTrash();
                        } else {
                            QuantitationTree.getCurrentTree().loadTrash();
                        }
                    }
                });

            }
        };

        parentData.load(callback, childrenList, identificationDataset);
    }

    protected void dataLoaded(AbstractData data, List<AbstractData> list) {

        AbstractNode parentNode = m_loadingMap.remove(data);

        parentNode.remove(0); // remove the first child which correspond to the hour glass

        int indexToInsert = 0;
        Iterator<AbstractData> it = list.iterator();
        while (it.hasNext()) {
            AbstractData dataCur = it.next();
            parentNode.insert(ChildFactory.createNode(dataCur), indexToInsert);
            indexToInsert++;
        }

        m_model.nodeStructureChanged(parentNode);

    }

    
    ///////////////////////////////////////////////
    
    public void loadNodes(HashSet<AbstractNode> nodes, Runnable callback, final boolean identificationDataset) {
        if (nodes.isEmpty()) {
            if (callback != null) {
                callback.run();
                return;
            }
        }
        
        AbstractNode node = nodes.iterator().next();
        loadNodes(nodes, node, callback, identificationDataset);
    }
    
    public void loadNode(AbstractNode node, Runnable callback, final boolean identificationDataset) {
        HashSet nodes = new HashSet<>(1);
        nodes.add(node);
        loadNodes(nodes, node, callback, identificationDataset);
    }
    
    private void loadNodes(final HashSet<AbstractNode> nodes, AbstractNode node, Runnable callback, final boolean identificationDataset) {
        
        if (node.getChildCount() == 0) {
            // node already loaded
            nodes.remove(node);
            loadNodes(nodes, callback, identificationDataset);
            return;
        }
        
        AbstractNode childNode = (AbstractNode) node.getChildAt(0);

        if (childNode.getType() != AbstractNode.NodeTypes.HOUR_GLASS) {
            // node already loaded, look to its children
            int nbChildren = node.getChildCount();
            for (int i = 0; i < nbChildren; i++) {
                nodes.add((AbstractNode) node.getChildAt(i));
            }
            nodes.remove(node);
            loadNodes(nodes, callback, identificationDataset);
        } else {
            // this node needs to be loaded

            // register hour glass which is expanded
            m_loadingMap.put(node.getData(), node);

            final ArrayList<AbstractData> childrenList = new ArrayList<>();
            final AbstractData parentData = node.getData();

            if (node.getType() == AbstractNode.NodeTypes.TREE_PARENT) {
                node.setIsChanging(true);
                m_model.nodeChanged(node);
            }

            // Callback used only for the synchronization with the AccessDatabaseThread
            AbstractDatabaseCallback databaseCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    if (node.getType() == AbstractNode.NodeTypes.TREE_PARENT) {
                        node.setIsChanging(false);
                        m_model.nodeChanged(node);
                    }

                    dataLoadingDone(nodes, node, callback, parentData, childrenList, identificationDataset);

                }
            };

            parentData.load(databaseCallback, childrenList, Priority.TOP, identificationDataset);
        }

    }
    
    private void dataLoadingDone(final HashSet<AbstractNode> nodes, AbstractNode node, Runnable callback, AbstractData data, List<AbstractData> list, boolean identificationDataset) {

        AbstractNode parentNode = m_loadingMap.remove(data);

        nodes.remove(node);
        
        parentNode.remove(0); // remove the first child which correspond to the hour glass

        int indexToInsert = 0;
        Iterator<AbstractData> it = list.iterator();
        while (it.hasNext()) {
            AbstractData dataCur = it.next();
            parentNode.insert(ChildFactory.createNode(dataCur), indexToInsert);
            indexToInsert++;
        }

        m_model.nodeStructureChanged(parentNode);

        int nbChildren = parentNode.getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            AbstractNode childNode = (AbstractNode) parentNode.getChildAt(i);
            if (childNode.getChildCount() != 0) {
                nodes.add(childNode);
            }
        }
        loadNodes(nodes, callback, identificationDataset);


    }
    
    ///////////////////////////////////////
    
    public void loadAllAtOnce(final AbstractNode nodeToLoad, final boolean identificationDataset) {

        if (nodeToLoad.getChildCount() == 0) {
            this.m_expected--;
            if (identificationDataset && this.m_subscribedRenamer != null) {
                if (m_expected == 0) {
                    this.m_subscribedRenamer.proceedWithRenaming();
                }
            }
            return;
        }

        AbstractNode childNode = (AbstractNode) nodeToLoad.getChildAt(0);

        if (childNode.getType() != AbstractNode.NodeTypes.HOUR_GLASS) {
            int nbChildren = nodeToLoad.getChildCount();

            this.m_expected += nbChildren;

            for (int i = 0; i < nbChildren; i++) {
                loadAllAtOnce((AbstractNode) nodeToLoad.getChildAt(i), identificationDataset);
            }

            this.m_expected--;

            if (identificationDataset && this.m_subscribedRenamer != null) {
                if (m_expected == 0) {
                    this.m_subscribedRenamer.proceedWithRenaming();
                }
            }

            return;
        }

        // register hour glass which is expanded
        m_loadingMap.put(nodeToLoad.getData(), nodeToLoad);

        final ArrayList<AbstractData> childrenList = new ArrayList<>();
        final AbstractData parentData = nodeToLoad.getData();

        if (nodeToLoad.getType() == AbstractNode.NodeTypes.TREE_PARENT) {
            nodeToLoad.setIsChanging(true);
            m_model.nodeChanged(nodeToLoad);
        }

        // Callback used only for the synchronization with the AccessDatabaseThread
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (nodeToLoad.getType() == AbstractNode.NodeTypes.TREE_PARENT) {
                    nodeToLoad.setIsChanging(false);
                    m_model.nodeChanged(nodeToLoad);
                }

                dataLoadedAtOnce(parentData, childrenList, identificationDataset);
            }
        };

        parentData.load(callback, childrenList, Priority.TOP, identificationDataset);

    }

    private void dataLoadedAtOnce(AbstractData data, List<AbstractData> list, boolean identificationDataset) {

        AbstractNode parentNode = m_loadingMap.remove(data);

        parentNode.remove(0); // remove the first child which correspond to the hour glass

        int indexToInsert = 0;
        Iterator<AbstractData> it = list.iterator();
        while (it.hasNext()) {
            AbstractData dataCur = it.next();
            parentNode.insert(ChildFactory.createNode(dataCur), indexToInsert);
            indexToInsert++;
        }

        m_model.nodeStructureChanged(parentNode);

        int nbChildren = parentNode.getChildCount();

        this.m_expected = m_expected + nbChildren - 1;

        for (int i = 0; i < nbChildren; i++) {
            loadAllAtOnce((AbstractNode) parentNode.getChildAt(i), identificationDataset);
        }

        if (identificationDataset &&this.m_subscribedRenamer != null) {
            if (m_expected == 0) {
                this.m_subscribedRenamer.proceedWithRenaming();
            }
        }

    }

    /**
     * Set all nodes that don't belong to selectionPath to specified status
     * (disabled or not)
     *
     * @param disabled : true if none selection should be set to disable
     * otherwise true
     */
    public void revertSelectionSetDisabled(boolean disabled) {
        TreePath[] selectedPaths = getSelectionPaths();
        List<AbstractNode> availableNodes = new ArrayList<>();
        if (selectedPaths != null) {
            for (TreePath selectedPath : selectedPaths) {
                Object[] pathElements = selectedPath.getPath();
                for (Object nextElem : pathElements) {
                    AbstractNode nextNode = (AbstractNode) nextElem;
                    if (!availableNodes.contains(nextNode)) {
                        availableNodes.add(nextNode);
                    }
                }
            }
        }

        AbstractNode rootNode = (AbstractNode) m_model.getRoot();
        if (!availableNodes.contains(rootNode)) {
            rootNode.setIsDisabled(disabled);
            m_model.nodeChanged(rootNode);
        }
        childRevertSelectionSetDisabled(rootNode, availableNodes, disabled);
    }

    private void childRevertSelectionSetDisabled(AbstractNode parent, List<AbstractNode> enabledNodes, boolean disabled) {
        int nbrChild = m_model.getChildCount(parent);
        for (int i = 0; i < nbrChild; i++) {
            AbstractNode childNode = (AbstractNode) m_model.getChild(parent, i);
            if (!enabledNodes.contains(childNode)) {
                childNode.setIsDisabled(disabled);
                m_model.nodeChanged(childNode);
            }
            childRevertSelectionSetDisabled(childNode, enabledNodes, disabled);
        }
    }

    public void setSelection(ArrayList<ResultSummary> rsmArray) {

        if (!m_loadingMap.isEmpty()) {
            // Tree is loading, we must postpone the selection
            selectionFromrsmArray = rsmArray;
            return;
        }

        AbstractNode rootNode = (AbstractNode) m_model.getRoot();
        ArrayList<TreePath> selectedPathArray = new ArrayList<>(rsmArray.size());

        ArrayList<AbstractNode> nodePath = new ArrayList<>();
        nodePath.add(rootNode);
        setSelectionImpl(rootNode, nodePath, rsmArray, selectedPathArray);

        TreePath[] selectedPaths = selectedPathArray.toArray(new TreePath[selectedPathArray.size()]);

        setSelectionPaths(selectedPaths);

    }
    private ArrayList<ResultSummary> selectionFromrsmArray = null;

    private void setSelectionImpl(AbstractNode parentNode, ArrayList<AbstractNode> nodePath, ArrayList<ResultSummary> rsmArray, ArrayList<TreePath> selectedPathArray) {
        Enumeration en = parentNode.children();
        while (en.hasMoreElements()) {
            AbstractNode node = (AbstractNode) en.nextElement();
            if (node.getType() == AbstractNode.NodeTypes.DATA_SET && !node.isDisabled()) {

                DataSetNode dataSetNode = (DataSetNode) node;
                Long rsmId = dataSetNode.getResultSummaryId();
                if (rsmId != null) {

                    int size = rsmArray.size();
                    for (int i = 0; i < size; i++) {
                        ResultSummary rsmItem = rsmArray.get(i);
                        if (rsmItem.getId() == rsmId.longValue()) {
                            nodePath.add(node);
                            TreePath path = new TreePath(nodePath.toArray());
                            selectedPathArray.add(path);
                            nodePath.remove(nodePath.size() - 1);
                            break;
                        }
                    }
                }
            }
            if (!node.isLeaf() && !node.isDisabled()) {
                nodePath.add(node);
                setSelectionImpl(node, nodePath, rsmArray, selectedPathArray);
            }
        }
        nodePath.remove(nodePath.size() - 1);
    }

    /**
     * Return an array of all selected nodes of the tree
     *
     * @return
     */
    public AbstractNode[] getSelectedNodes() {
        TreePath[] paths = getSelectionModel().getSelectionPaths();

        int nbPath = paths.length;

        AbstractNode[] nodes = new AbstractNode[nbPath];

        for (int i = 0; i < nbPath; i++) {
            nodes[i] = (AbstractNode) paths[i].getLastPathComponent();
        }

        return nodes;
    }

    public boolean isSelected(int row) {
        if (row == -1) {
            return false;
        }
        return getSelectionModel().isRowSelected(row);
    }

    /**
     * set selectedRows
     * @param e 
     */
    protected void manageSelectionOnRightClick(MouseEvent e) {

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
            if ((row != -1) && (e.isShiftDown() || e.isControlDown())) {
                addSelectionRow(row);
            } else if ((row != -1) && (row != selectedRows[0])) {
                // we change the selection
                setSelectionRow(row);
            }
        } else {
            // multiple row are already selected
            // if ctrl or shift is down, we add the row to the selection
            if (e.isShiftDown() || e.isControlDown()) {
                int row = getRowForLocation(e.getX(), e.getY());
                if (row != -1) {
                    addSelectionRow(row);
                }
            } else {
                int row = getRowForLocation(e.getX(), e.getY());
                if ((row != -1) && (!isSelected(row))) {
                    setSelectionRow(row);
                }
            }
        }
    }

    public void expandNodeIfNeeded(AbstractNode n) {
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

    public abstract void rename(AbstractNode rsmNode, String newName);

    @Override
    public String getToolTipText(MouseEvent evt) {
        int row = getRowForLocation(evt.getX(), evt.getY());
        if (row == -1) {
            return null;
        }
        TreePath curPath = getPathForRow(row);
        AbstractNode node = (AbstractNode) curPath.getLastPathComponent();
        return node.getToolTipText();

    }

    public class RSMTreeModel extends DefaultTreeModel {

        private AbstractTree m_parentTree = null;

        public RSMTreeModel(AbstractTree parentTree, TreeNode root) {
            super(root, false);
            m_parentTree = parentTree;
        }

        /**
         * This sets the user object of the TreeNode identified by path and
         * posts a node changed. If you use custom user objects in the TreeModel
         * you're going to need to subclass this and set the user object of the
         * changed node to something meaningful.
         */
        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            AbstractNode rsmNode = (AbstractNode) path.getLastPathComponent();

            m_parentTree.rename(rsmNode, newValue.toString());

        }
    }

    public static class RSMTreeSelectionModel extends DefaultTreeSelectionModel {

        public RSMTreeSelectionModel() {
            super();
        }

        @Override
        public void setSelectionPaths(TreePath[] pPaths) {
            List<TreePath> newSelectionList = new ArrayList<>();
            for (TreePath path : pPaths) {
                AbstractNode node = (AbstractNode) path.getLastPathComponent();
                if (!node.isDisabled()) {
                    newSelectionList.add(path);
                }
            }
            super.setSelectionPaths(newSelectionList.toArray(new TreePath[newSelectionList.size()])); //To change body of generated methods, choose Tools | Templates.
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

            if (editingIcon != null) {
                offset = renderer.getIconTextGap() + editingIcon.getIconWidth();
            } else {
                offset = renderer.getIconTextGap();
            }
        }
    }

}
