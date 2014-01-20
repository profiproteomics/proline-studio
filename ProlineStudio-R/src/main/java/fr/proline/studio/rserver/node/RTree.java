package fr.proline.studio.rserver.node;

import fr.proline.studio.rserver.actions.*;
import fr.proline.studio.rserver.data.RParentData;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import fr.proline.studio.rserver.data.RGraphicData;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import java.awt.Image;

/**
 *
 * @author JM235353
 */
public class RTree extends JTree implements MouseListener {
 
    private static RTree m_instance = null;

    private RTreeModel m_model = null;
    
    public static RTree getTree() {
        if (m_instance == null) {
            m_instance = new RTree();
        }
        return m_instance;
    }
    
        private RTree() {

        //setEditable(true);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        //RSMTransferHandler handler = new RSMTransferHandler();
        //setTransferHandler(handler);

        //setDragEnabled(true);
        //setDropMode(DropMode.ON_OR_INSERT);

        // Model of the tree
        RTreeParentNode top = new RTreeParentNode(new RParentData());

        initTree(top);

    }
        
    private void initTree(RNode top) {
        m_model = new RTreeModel(top);
        setModel(m_model);

        // rendering of the tree
        putClientProperty("JTree.lineStyle", "Horizontal");

        RTreeRenderer renderer = new RTreeRenderer();
        setCellRenderer(renderer);
        /*if (isEditable()) {
            setCellEditor(new RSMTreeCellEditor(this, renderer));
        }*/
        /*
        // -- listeners
        addTreeWillExpandListener(this); // used for lazy loading
        * */

        addMouseListener(this);         // used for popup triggering 

    }
    
    
    
    /**
     * Return an array of all selected nodes of the tree
     * @return 
     */
    public RNode[] getSelectedNodes() {
        TreePath[] paths = getSelectionModel().getSelectionPaths();
        
        int nbPath = paths.length;
        
        RNode[] nodes = new RNode[nbPath];
        
        for (int i=0;i<nbPath;i++) {
            nodes[i] = (RNode) paths[i].getLastPathComponent();
        }

        return nodes;
    }
    
    private void triggerPopup(MouseEvent e) {
        
        // retrieve selected nodes
        RNode[] selectedNodes = getSelectedNodes();

        int nbNodes = selectedNodes.length;

        // check if the Root node is selected
        boolean rootNodeSelected = false;
        for (int i=0;i<nbNodes;i++) {
            RNode n = selectedNodes[i];
            if (n.isRoot()) {
                rootNodeSelected = true;
                break;
            }
        }
        
                
        // check if nodes are changing
        /*if ((nbNodes!=1) || (!allImportedNodeSelected)) {
            for (int i = 0; i < nbNodes; i++) {
                if (selectedNodes[i].isChanging()) {
                    // do not show a popup on a node which is changing
                    // except for All Imported Node
                    return;
                }
            }
        }*/
        
        
        JPopupMenu popup;
        ArrayList<AbstractRAction> actions;
        
        if (rootNodeSelected && (nbNodes >= 1)) {
            if (nbNodes > 1) {
                // the root node is selected and multiple nodes are
                // selected : we do not show the popup
                return;
            }

            // we show the popup to connect or disconnect
            if (m_rootPopup == null) {
                // create the actions
                m_rootActions = new ArrayList<>(3);  // <--- get in sync


                RConnectAction connectAction = new RConnectAction();
                m_rootActions.add(connectAction);

                RDisconnectAction disconnectAction = new RDisconnectAction();
                m_rootActions.add(disconnectAction);

                m_rootActions.add(null);
                
                RImportMsnSetAction importMsnSetAction = new RImportMsnSetAction();
                m_rootActions.add(importMsnSetAction);
                
                // add actions to popup
                m_rootPopup = new JPopupMenu();
                for (int i = 0; i < m_rootActions.size(); i++) {
                    AbstractRAction action = m_rootActions.get(i);
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


            // creation of the popup if needed
            if (m_mainPopup == null) {

                // create the actions
                m_mainActions = new ArrayList<>(1);  // <--- get in sync

                //DisplayRsetAction displayRsetAction = new DisplayRsetAction();
                //m_mainActions.add(displayRsetAction);


                NormalizeMsnSetAction normalizeAction = new NormalizeMsnSetAction();
                m_mainActions.add(normalizeAction);

                m_mainActions.add(null);  // separator
                
                BoxPlotAction boxPlotAction = new BoxPlotAction();
                m_mainActions.add(boxPlotAction);

                // add actions to popup
                m_mainPopup = new JPopupMenu();
                for (int i = 0; i < m_mainActions.size(); i++) {
                    AbstractRAction action = m_mainActions.get(i);
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
            AbstractRAction action = actions.get(i);
            
            if (action == null) {
                continue;
            }
            
            action.updateEnabled(selectedNodes);
        }
        
        popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
    }
    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRAction> m_mainActions;
    private JPopupMenu m_rootPopup;
    private ArrayList<AbstractRAction> m_rootActions;

    
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
            
            RNode[] nodes = getSelectedNodes();
            if (nodes.length == 1) {
                RNode node = nodes[0];
                if (node.getType() == RNode.NodeTypes.GRAPHIC) {
                    Image i = ((RGraphicData)node.getData()).getImage();
                    ImageViewerTopComponent win = new ImageViewerTopComponent(node.getLongDisplayName(), i);
                    win.open();
                    win.requestActive();
                }
            }
        }

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

        }




    }

    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            triggerPopup(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    
    
    private class RTreeModel extends DefaultTreeModel {

        public RTreeModel(TreeNode root) {
            super(root, false);
        }
    }
    
    
    private class RTreeRenderer extends DefaultTreeCellRenderer {

        public RTreeRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            ImageIcon icon = ((RNode) value).getIcon();
            if (icon != null) {
                setIcon(icon);
            }

            return this;
        }
    }
    
    @Override
    public String getToolTipText(MouseEvent evt) {
        int row = getRowForLocation(evt.getX(), evt.getY());
        if (row  == -1) {
            return null;
        }
        TreePath curPath = getPathForRow(row);
        RNode node = (RNode) curPath.getLastPathComponent();
        return node.getToolTipText();

    } 
        
}
