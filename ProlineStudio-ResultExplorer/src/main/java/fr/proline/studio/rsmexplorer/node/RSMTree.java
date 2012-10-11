/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ParentData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseSlicerTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.actions.*;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    
    private static RSMTree instance = null;
    
    public static RSMTree getTree() {
        if (instance == null) {
            instance = new RSMTree();
        }
        return instance;
    }
    
    private RSMTree() {
        
        // Model of the tree
        RSMNode top = RSMChildFactory.createNode(new ParentData());
        model = new DefaultTreeModel(top);
        setModel(model);
        
        // rendering of the tree
        putClientProperty("JTree.lineStyle", "Horizontal");
        setCellRenderer(new RSMTreeRenderer());
        
        // -- listeners
        addTreeWillExpandListener(this); // used for lazy loading
        addMouseListener(this);         // used for popup triggering
        
        // JPM.TODO
        startLoading(top);
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
                            }
                
                        });

                    }
                };

           
                
                parentData.load(callback, childrenList);
    }

    private static HashMap<AbstractData, RSMNode> loadingMap = new HashMap<AbstractData, RSMNode>();

    public  void dataLoaded(AbstractData data, List<AbstractData> list) {

        RSMNode parentNode = loadingMap.remove(data);

        
        
        //model.removeNodeFromParent((RSMNode)parentNode.getChildAt(0));

        parentNode.removeAllChildren();
        

        Iterator<AbstractData> it = list.iterator();
        while (it.hasNext()) {
            AbstractData dataCur = it.next();
            parentNode.add(RSMChildFactory.createNode(dataCur));
        }
        
        model.nodeStructureChanged(parentNode);
        
        //model.insertNodeInto(RSMChildFactory.createNode(dataCur), parentNode, parentNode.getChildCount());


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
        if (e.isPopupTrigger()) {
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
