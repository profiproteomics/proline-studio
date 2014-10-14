package fr.proline.studio.rsmexplorer.tree;

import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import fr.proline.studio.dam.tasks.SubTask;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.*;

/**
 *
 * @author JM235353
 */
public abstract class AbstractTree extends JTree implements MouseListener {
 
    public enum TreeType {

        TREE_IDENTIFICATION,
        TREE_QUANTITATION,
        TREE_SELECTION,
        TREE_XIC_DESIGN
    }
    
    
    protected RSMTreeModel m_model;
    
    protected HashMap<AbstractData, AbstractNode> loadingMap = new HashMap<>();
    
    protected void initTree(AbstractNode top) {
        m_model = new RSMTreeModel(this, top);
        setModel(m_model);

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
    
    
    protected void startLoading(final AbstractNode nodeToLoad) {

        
        
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

        // register hour glass which is expanded
        loadingMap.put(nodeToLoad.getData(), nodeToLoad);

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


    }
    
    

    public void loadAllAtOnce(final AbstractNode nodeToLoad) {
        if (nodeToLoad.getChildCount() == 0) {
            return;
        }
        AbstractNode childNode = (AbstractNode) nodeToLoad.getChildAt(0);
        if (childNode.getType() != AbstractNode.NodeTypes.HOUR_GLASS) {
            int nbChildren = nodeToLoad.getChildCount();
            for (int i = 0; i < nbChildren; i++) {
                loadAllAtOnce((AbstractNode) nodeToLoad.getChildAt(i));
            }
            return; 
        }
        
        // register hour glass which is expanded
        loadingMap.put(nodeToLoad.getData(), nodeToLoad);
        
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

                dataLoadedAtOnce(parentData, childrenList);
            }
        };

        

        parentData.load(callback, childrenList, Priority.TOP);
        
    }
    protected void dataLoadedAtOnce(AbstractData data, List<AbstractData> list) {

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

        int nbChildren = parentNode.getChildCount();
        for (int i=0;i<nbChildren;i++) {
            loadAllAtOnce((AbstractNode) parentNode.getChildAt(i));
        }

    }
    
    
    public void setSelection(ArrayList<ResultSummary> rsmArray) {

        if (!loadingMap.isEmpty()) {
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
            if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {

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
            if (!node.isLeaf()) {
                nodePath.add(node);
                setSelectionImpl(node, nodePath, rsmArray, selectedPathArray);
            }
        }
        nodePath.remove(nodePath.size() - 1);
    }
    
        /**
     * Return an array of all selected nodes of the tree
     * @return 
     */
    public AbstractNode[] getSelectedNodes() {
        TreePath[] paths = getSelectionModel().getSelectionPaths();
        
        int nbPath = paths.length;
        
        AbstractNode[] nodes = new AbstractNode[nbPath];
        
        for (int i=0;i<nbPath;i++) {
            nodes[i] = (AbstractNode) paths[i].getLastPathComponent();
        }

        return nodes;
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
        if (row  == -1) {
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
