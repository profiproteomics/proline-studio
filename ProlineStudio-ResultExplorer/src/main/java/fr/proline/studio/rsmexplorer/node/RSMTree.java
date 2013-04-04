package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ParentData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.actions.*;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;

/**
 * Tree of projects and datasets
 * @author JM235353
 */
public class RSMTree extends JTree implements TreeWillExpandListener, MouseListener {

    private RSMTreeModel model;
    private boolean isMainTree;
    private static RSMTree instance = null;

    public static RSMTree getTree() {
        if (instance == null) {
            instance = new RSMTree();
        }
        return instance;
    }

    private RSMTree() {

        setEditable(true);
        
        RSMTransferHandler handler = new RSMTransferHandler();
        setTransferHandler(handler);

        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        
        isMainTree = true;

        // Model of the tree
        RSMNode top = RSMChildFactory.createNode(new ParentData());

        initTree(top);

    }

    private RSMTree(RSMNode top) {

        isMainTree = false;

        initTree(top);

        startLoading(top);

    }

    private void initTree(RSMNode top) {
        model = new RSMTreeModel(top);
        setModel(model);

        // rendering of the tree
        putClientProperty("JTree.lineStyle", "Horizontal");
        
        RSMTreeRenderer renderer = new RSMTreeRenderer();
        setCellRenderer(renderer);
        if (isEditable()) {
            setCellEditor(new RSMTreeCellEditor(this, renderer));
        }
        
        // -- listeners
        addTreeWillExpandListener(this); // used for lazy loading
        addMouseListener(this);         // used for popup triggering

    }
    
    @Override
    public boolean isPathEditable(TreePath path) {
        if (isEditable()) {
            RSMNode node = (RSMNode) path.getLastPathComponent();
            RSMNode.NodeTypes nodeType = node.getType();
            if ((nodeType == RSMNode.NodeTypes.DATA_SET) || (nodeType == RSMNode.NodeTypes.PROJECT)) {
                return true;
            }
        }
        return false;
    }

    public RSMTree copyResultSetRootSubTree(ResultSet rset, Integer projectId) {

        int rsetId = rset.getId().intValue();

        RSMDataSetNode rsetNode = findResultSetNode((RSMNode) model.getRoot(), rsetId, projectId);

        if (rsetNode == null) {
            return null;
        }

        rsetNode = findResultSetNodeRootParent(rsetNode);

        return new RSMTree(rsetNode);

    }

    private RSMDataSetNode findResultSetNode(RSMNode node, int rsetId, Integer projectId) {

        int nbChildren = node.getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            RSMNode childNode = (RSMNode) node.getChildAt(i);
            RSMNode.NodeTypes childType = childNode.getType();
            if (childType == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode dataSetNode = ((RSMDataSetNode) childNode);
                Integer resultSetId = dataSetNode.getResultSetId();

                if ((resultSetId != null) && (resultSetId.intValue() == rsetId)) {
                    return dataSetNode;
                }
            } else if (childType == RSMNode.NodeTypes.PROJECT) {
                RSMProjectNode projectNode = ((RSMProjectNode) childNode);
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

    private RSMDataSetNode findResultSetNodeRootParent(RSMDataSetNode node) {

        /*RSMDataSetNode resultSetRootParent = node;
        RSMNode parentNode = (RSMNode) node.getParent();
        while (parentNode != null) {
            if (parentNode.getType() == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode datasetParentNode = (RSMDataSetNode) parentNode;
                
                resultSetRootParent = (RSMDataSetNode) parentNode;
            }
            parentNode = (RSMNode) parentNode.getParent();
        }

        return resultSetRootParent;*/
        //JPM.TODO : no longer correct for the way I use Dataset
        return node;
    }

    public void setSelection(ArrayList<ResultSummary> rsmArray) {

        if (!loadingMap.isEmpty()) {
            // Tree is loading, we must postpone the selection
            selectionFromrsmArray = rsmArray;
            return;
        }

        RSMNode rootNode = (RSMNode) model.getRoot();
        ArrayList<TreePath> selectedPathArray = new ArrayList<TreePath>(rsmArray.size());

        ArrayList<RSMNode> nodePath = new ArrayList<RSMNode>();
        nodePath.add(rootNode);
        setSelectionImpl(rootNode, nodePath, rsmArray, selectedPathArray);


        TreePath[] selectedPaths = selectedPathArray.toArray(new TreePath[selectedPathArray.size()]);

        setSelectionPaths(selectedPaths);

    }
    private ArrayList<ResultSummary> selectionFromrsmArray = null;

    private void setSelectionImpl(RSMNode parentNode, ArrayList<RSMNode> nodePath, ArrayList<ResultSummary> rsmArray, ArrayList<TreePath> selectedPathArray) {
        Enumeration en = parentNode.children();
        while (en.hasMoreElements()) {
            RSMNode node = (RSMNode) en.nextElement();
            if (node.getType() == RSMNode.NodeTypes.DATA_SET) {

                RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
                Integer rsmId = dataSetNode.getResultSummaryId();
                if (rsmId != null) {


                    int size = rsmArray.size();
                    for (int i = 0; i < size; i++) {
                        ResultSummary rsmItem = rsmArray.get(i);
                        if (rsmItem.getId().intValue() == rsmId.intValue()) {
                            nodePath.add(node);
                            TreePath path = new TreePath(nodePath.toArray());
                            selectedPathArray.add(path);
                            nodePath.remove(nodePath.size() - 1);
                            break;
                        }
                    }
                }
            }
            if (!node.isLeaf()) {
                nodePath.add(node);
                setSelectionImpl(node, nodePath, rsmArray, selectedPathArray);
            }
        }
        nodePath.remove(nodePath.size() - 1);
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
        
        // add hourglass node
        RSMNode root = (RSMNode) model.getRoot(); 
        model.insertNodeInto(new RSMHourGlassNode(null), root, 0);
        
        
        
        // show loading and start the loading
        expandRow(0);
        

    }
    private void startLoading(final RSMNode nodeToLoad) {

        
        
        // check if the loading is necessary :
        // it is necessary only if we have an hour glass child
        // which correspond to data not already loaded
        if (nodeToLoad.getChildCount() == 0) {
            return;
        }
        RSMNode childNode = (RSMNode) nodeToLoad.getChildAt(0);
        if (childNode.getType() != RSMNode.NodeTypes.HOUR_GLASS) {
            return;
        }

        // register hour glass which is expanded
        loadingMap.put(nodeToLoad.getData(), nodeToLoad);

        final ArrayList<AbstractData> childrenList = new ArrayList<AbstractData>();
        final AbstractData parentData = nodeToLoad.getData();

        if (nodeToLoad.getType() == RSMNode.NodeTypes.TREE_PARENT) {
            nodeToLoad.setIsChanging(true);
            model.nodeChanged(nodeToLoad);
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
                        
                        if (nodeToLoad.getType() == RSMNode.NodeTypes.TREE_PARENT) {
                            nodeToLoad.setIsChanging(false);
                            model.nodeChanged(nodeToLoad);
                        }
                        
                        dataLoaded(parentData, childrenList);

                        if (loadingMap.isEmpty() && (selectionFromrsmArray != null)) {
                            // A selection has been postponed, we do it now
                            setSelection(selectionFromrsmArray);
                            selectionFromrsmArray = null;
                        }
                    }
                });

            }
        };

        

        parentData.load(callback, childrenList);
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
    
    //TOTOCHE

    private static HashMap<AbstractData, RSMNode> loadingMap = new HashMap<AbstractData, RSMNode>();

    public  void dataLoaded(AbstractData data, List<AbstractData> list) {

        RSMNode parentNode = loadingMap.remove(data);


        parentNode.remove(0); // remove the first child which correspond to the hour glass
        
        int indexToInsert = 0;
        Iterator<AbstractData> it = list.iterator();
        while (it.hasNext()) {
            AbstractData dataCur = it.next();
            parentNode.insert(RSMChildFactory.createNode(dataCur), indexToInsert);
            indexToInsert++;
        }
        
        model.nodeStructureChanged(parentNode);


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
    public RSMNode[] getSelectedNodes() {
        TreePath[] paths = getSelectionModel().getSelectionPaths();
        
        int nbPath = paths.length;
        
        RSMNode[] nodes = new RSMNode[nbPath];
        
        for (int i=0;i<nbPath;i++) {
            nodes[i] = (RSMNode) paths[i].getLastPathComponent();
        }

        return nodes;
    }
    
    private void triggerPopup(MouseEvent e) {
        
        // retrieve selected nodes
        RSMNode[] selectedNodes = getSelectedNodes();
        
        // check if nodes are changing
        int nbNodes = selectedNodes.length;
        for (int i=0;i<nbNodes;i++) {
            if (selectedNodes[i].isChanging()) {
                // do not show a popup on a node which is changing
                return;
            }
        }
        
        // check if the Root node or Trash is selected
        boolean rootNodeSelected = false;
        boolean trashNodeSelected = false;
        for (int i=0;i<nbNodes;i++) {
            RSMNode n = selectedNodes[i];
            if (n.isRoot()) {
                rootNodeSelected = true;
                break;
            }
            if (n instanceof RSMDataSetNode) {
                RSMDataSetNode datasetNode = (RSMDataSetNode) n;
                if (datasetNode.isTrash()) {
                    trashNodeSelected = true;
                }
            }
        }
        
        JPopupMenu popup = null;
        ArrayList<AbstractRSMAction> actions = null;
        
        if (rootNodeSelected && (nbNodes >= 1)) {
            if (nbNodes > 1) {
                // the root node is selected and multiple nodes are
                // selected : we do not show the popup
                return;
            }

            // we show the popup to connect or disconnect
            if (rootPopup == null) {
                // create the actions
                rootActions = new ArrayList<AbstractRSMAction>(2);  // <--- get in sync

                AddProjectAction addProjectAction = new AddProjectAction();
                rootActions.add(addProjectAction);
                
                /*rootActions.add(null);  // separator
                
                ConnectAction connectAction = new ConnectAction();
                rootActions.add(connectAction);*/

                /*DisconnectAction disconnectAction = new DisconnectAction();
                rootActions.add(disconnectAction);*/ //JPM.TODO

                // add actions to popup
                rootPopup = new JPopupMenu();
                for (int i = 0; i < rootActions.size(); i++) {
                    AbstractRSMAction action = rootActions.get(i);
                    if (action == null) {
                        rootPopup.addSeparator();
                    } else {
                        rootPopup.add(action.getPopupPresenter());
                    }
                }
            }
            
            popup = rootPopup;
            actions = rootActions;
            
        } else if (trashNodeSelected && (nbNodes == 1)) {
            
            // creation of the popup if needed
            if (trashPopup == null) {
                 // create the actions
                trashActions = new ArrayList<>(1);  // <--- get in sync
                
                EmptyTrashAction emtpyTrashAction = new EmptyTrashAction();
                trashActions.add(emtpyTrashAction);
                
                trashPopup = new JPopupMenu();
                
                trashPopup.add(emtpyTrashAction.getPopupPresenter());
            }
            
            popup = trashPopup;
            actions = trashActions;
            
        } else {


            // creation of the popup if needed
            if (mainPopup == null) {

                // create the actions
                mainActions = new ArrayList<>(11);  // <--- get in sync

                DisplayRsetAction displayRsetAction = new DisplayRsetAction();
                mainActions.add(displayRsetAction);
                
                DisplayRsmAction displayRsmAction = new DisplayRsmAction();
                mainActions.add(displayRsmAction);


                PropertiesAction propertiesAction = new PropertiesAction();
                mainActions.add(propertiesAction);
                
                mainActions.add(null);  // separator

                AddAction addAction = new AddAction();
                mainActions.add(addAction);
                
                MergeAction mergeAction = new MergeAction();
                mainActions.add(mergeAction);
                
                ValidateAction validateAction = new ValidateAction();
                mainActions.add(validateAction);
                

                mainActions.add(null);  // separator

                RenameAction renameAction = new RenameAction();
                mainActions.add(renameAction);

                
                DeleteAction deleteAction = new DeleteAction();
                mainActions.add(deleteAction);


                // add actions to popup
                mainPopup = new JPopupMenu();
                for (int i = 0; i < mainActions.size(); i++) {
                    AbstractRSMAction action = mainActions.get(i);
                    if (action == null) {
                        mainPopup.addSeparator();
                    } else {
                        mainPopup.add(action.getPopupPresenter());
                    }
                }

            }
            
            popup = mainPopup;
            actions = mainActions;
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
    private JPopupMenu mainPopup;
    private ArrayList<AbstractRSMAction> mainActions;
    private JPopupMenu rootPopup;
    private ArrayList<AbstractRSMAction> rootActions;
    private JPopupMenu trashPopup;
    private ArrayList<AbstractRSMAction> trashActions;
    
    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        // nothing to do
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isMainTree && e.isPopupTrigger()) {
            triggerPopup(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}


    private class RSMTreeRenderer extends DefaultTreeCellRenderer {

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
    
    private class RSMTreeCellEditor extends DefaultTreeCellEditor {
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
    
    private class RSMTreeModel extends DefaultTreeModel {

        public RSMTreeModel(TreeNode root) {
            super(root, false);
        }

        /**
         * This sets the user object of the TreeNode identified by path and
         * posts a node changed. If you use custom user objects in the TreeModel
         * you're going to need to subclass this and set the user object of the
         * changed node to something meaningful.
         */
        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            RSMNode rsmNode = (RSMNode) path.getLastPathComponent();

            if (rsmNode.getType()== RSMNode.NodeTypes.PROJECT) {
                ((RSMProjectNode) rsmNode).rename(newValue.toString());
            } else if (rsmNode.getType()== RSMNode.NodeTypes.DATA_SET) {
                ((RSMDataSetNode) rsmNode).rename(newValue.toString());
            }
        }
    }

    
}
