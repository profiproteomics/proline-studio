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
 *
 * @author JM235353
 */
public class RSMTree extends JTree implements TreeWillExpandListener, MouseListener {
    
    private DefaultTreeModel model;
    
    private boolean isMainTree;
    
    private static RSMTree instance = null;
    
    public static RSMTree getTree() {
        if (instance == null) {
            instance = new RSMTree();
        }
        return instance;
    }
    
    private RSMTree() {

        isMainTree = true;
        
        // Model of the tree
        RSMNode top = RSMChildFactory.createNode(new ParentData());
        /*
         * model = new DefaultTreeModel(top); setModel(model);
         *
         * // rendering of the tree putClientProperty("JTree.lineStyle",
         * "Horizontal"); setCellRenderer(new RSMTreeRenderer());
         *
         * // -- listeners addTreeWillExpandListener(this); // used for lazy
         * loading addMouseListener(this); // used for popup triggering
         */

        initTree(top); 
                
        startLoading(top);
    }

    private RSMTree(RSMNode top) {

        isMainTree = false;

        initTree(top);

        startLoading(top); // JPM.TODO

    }
    
    private void initTree(RSMNode top) {
        model = new DefaultTreeModel(top);
        setModel(model);

        // rendering of the tree
        putClientProperty("JTree.lineStyle", "Horizontal");
        setCellRenderer(new RSMTreeRenderer());

        // -- listeners
        addTreeWillExpandListener(this); // used for lazy loading
        addMouseListener(this);         // used for popup triggering

    }

    public RSMTree copyResultSetRootSubTree(ResultSet rset) {

        int rsetId = rset.getId().intValue();

        RSMResultSetNode rsetNode = findResultSetNode((RSMNode) this.getModel().getRoot(), rsetId);

        if (rsetNode == null) {
            return null;
        }
        
        rsetNode = findResultSetNodeRootParent(rsetNode);

        return new RSMTree(rsetNode);

    }

    private RSMResultSetNode findResultSetNode(RSMNode node, int rsetId) {

        int nbChildren = node.getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            RSMNode childNode = (RSMNode) node.getChildAt(i);
            if (childNode.getType() == RSMNode.NodeTypes.RESULT_SET) {
                RSMResultSetNode rsetNode = ((RSMResultSetNode) childNode);
                if (rsetNode.getResultSet().getId().intValue() == rsetId) {
                    return rsetNode;
                }
            }
            if (!childNode.isLeaf()) {
                RSMResultSetNode rsetNode = findResultSetNode(childNode, rsetId);
                if (rsetNode != null) {
                    return rsetNode;
                }
            }

        }

        return null;
    }

    private RSMResultSetNode findResultSetNodeRootParent(RSMResultSetNode node) {

        RSMResultSetNode resultSetRootParent = node;
        RSMNode parentNode = (RSMNode) node.getParent();
        while (parentNode != null) {
            if (parentNode.getType() == RSMNode.NodeTypes.RESULT_SET) {
                resultSetRootParent = (RSMResultSetNode) parentNode;
            }
            parentNode = (RSMNode) parentNode.getParent();
        }

        return resultSetRootParent;
    }
    
    
    public void setSelection(ArrayList<ResultSummary> rsmArray) {

        if (! loadingMap.isEmpty()) {
            // Tree is loading, we must postpone the selection
            selectionFromrsmArray = rsmArray;
            return;
        }
        
        RSMNode rootNode = (RSMNode) getModel().getRoot();
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
            if (node.getType() == RSMNode.NodeTypes.RESULT_SUMMARY) {
                ResultSummary rsm = ((RSMResultSummaryNode) node).getResultSummary();
                int size = rsmArray.size();
                for (int i = 0; i < size; i++) {
                    ResultSummary rsmItem = rsmArray.get(i);
                    if (rsmItem.getId().intValue() == rsm.getId().intValue()) {
                        nodePath.add(node);
                        TreePath path = new TreePath(nodePath.toArray());
                        selectedPathArray.add(path);
                        nodePath.remove(nodePath.size()-1);
                        break;
                    }
                }
            }
            if (!node.isLeaf()) {
                nodePath.add(node);
                setSelectionImpl(node, nodePath, rsmArray, selectedPathArray);
            }
        }
        nodePath.remove(nodePath.size()-1);
    }
    
    
    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

        TreePath path = event.getPath();
        RSMNode nodeExpanded = (RSMNode) path.getLastPathComponent();
        
        // check if the node contains a GlassHourNode (ie : children are not loaded)
        if (nodeExpanded.getChildCount() == 1) {
            RSMNode childNode = (RSMNode) nodeExpanded.getChildAt(0);
            if (childNode.getType() == RSMNode.NodeTypes.HOUR_GLASS) {
                
                startLoading(nodeExpanded);
            }
        }
        
    }
    private void startLoading(RSMNode nodeToLoad) {

        
        
        // check if the loading is necessary :
        // it is necessary only if we have an hour glass child
        // which correspond to data not already loaded
        if (nodeToLoad.getChildCount() != 1) {
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

        // Callback used only for the synchronization with the AccessDatabaseThread
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
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

    private static HashMap<AbstractData, RSMNode> loadingMap = new HashMap<AbstractData, RSMNode>();

    public  void dataLoaded(AbstractData data, List<AbstractData> list) {

        RSMNode parentNode = loadingMap.remove(data);


        parentNode.removeAllChildren();
        

        Iterator<AbstractData> it = list.iterator();
        while (it.hasNext()) {
            AbstractData dataCur = it.next();
            parentNode.add(RSMChildFactory.createNode(dataCur));
        }
        
        model.nodeStructureChanged(parentNode);


    }
    
    private void triggerPopup(MouseEvent e) {
        
        // creation of the popup if needed
        if (popup == null) {
            
            // create the actions
            actions = new ArrayList<AbstractRSMAction>(5);  // <--- get in sync
            
            DisplayAction displayAction = new DisplayAction();
            actions.add(displayAction);
            
            ChildParentAction childParentAction = new ChildParentAction();
            actions.add(childParentAction);
            
            actions.add(null);  // separator
            
            RenameAction renameAction = new RenameAction();
            actions.add(renameAction);
            
            DeleteAction deleteAction = new DeleteAction();
            actions.add(deleteAction);
            
            // add actions to popup
            popup = new JPopupMenu();
            for (int i=0;i<actions.size();i++) {
                AbstractRSMAction action = actions.get(i);
                if (action == null) {
                    popup.addSeparator();
                } else {
                    popup.add(action.getPopupPresenter());
                }
            }

        }
        
        // update of the enable/disable state
        //popup.setE
        
        popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
    }
    private JPopupMenu popup;
    private ArrayList<AbstractRSMAction> actions;
    
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


    class RSMTreeRenderer extends DefaultTreeCellRenderer {

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
    
    
}
