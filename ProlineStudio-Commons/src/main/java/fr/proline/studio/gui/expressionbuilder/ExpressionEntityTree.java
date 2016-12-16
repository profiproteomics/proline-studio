package fr.proline.studio.gui.expressionbuilder;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class ExpressionEntityTree  extends JTree {

    private ExpressionBuilderPanel m_builderPanel;
    private TreeType m_type;
    
    public enum TreeType {
        FUNCTIONS,
        VARIABLES
    };
    
    protected ExpressionEntityTree(ExpressionBuilderPanel builderPanel, TreeType type) {
        super(new RootFunctionNode(type));

        m_type = type;
                
        setPreferredSize(new Dimension(140, 120));
        
        m_builderPanel = builderPanel;
        
        setToggleClickCount(0); // avoid expanding when double clicking
        setCellRenderer(new EntityTreeRenderer()); 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 1) {

                     TreePath path = getSelectionPath();
                     if (path == null) {
                         return;
                     }
                     DataNode node = (DataNode) path.getLastPathComponent();
                     action(node);

                     clearSelection();
                }

            }
        });
        
        // expand first level of the tree
        /*DefaultMutableTreeNode currentNode = root.getNextNode();
        do {
            if (currentNode.getLevel() == 1) {
                expandPath(new TreePath(currentNode.getPath()));
            }
            currentNode = currentNode.getNextNode();
        } while (currentNode != null);*/
    }

    
    public void action(DataNode node) {
        if (node.getType() == DataNode.DataNodeType.VALUE) {
            ExpressionEntity expressionEntity = ((EntityNode) node).getExpressionEntity();
            m_builderPanel.addEntityToExpression(expressionEntity);
        }
    }

    
    public void addEntity(ExpressionEntity entity) {
        EntityNode function = new EntityNode(entity);
        
        RootFunctionNode root = (RootFunctionNode) getModel().getRoot();
        
        root.add(function);
        
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.nodeStructureChanged(root);
    }

    
    public static abstract class DataNode extends DefaultMutableTreeNode {

        public enum DataNodeType {
            ROOT,
            VALUE
        }
        
        private DataNodeType m_type;
        
        public DataNode(DataNodeType type) {
            m_type =  type;
        }


        public DataNodeType getType() {
            return m_type;
        }

        public boolean isVisible() {
            return true;
        }

        public abstract ImageIcon getIcon();
    }

    public static class RootFunctionNode extends DataNode {

        private ImageIcon m_icon;
        private String m_name;
        
        public RootFunctionNode(TreeType type) {
            super(DataNodeType.ROOT);
            
            switch (type) {
                case FUNCTIONS:
                    m_icon = IconManager.getIcon(IconManager.IconType.FUNCTION);
                    m_name = "Functions";
                    break;
                  case VARIABLES:
                    m_icon = IconManager.getIcon(IconManager.IconType.COLUMN);
                    m_name = "Variables";
                    break;
            }
        }

        @Override
        public ImageIcon getIcon() {
            return m_icon;
        }

        @Override
        public String toString() {
            return m_name;
        }
    }

    public static class EntityNode extends DataNode {

        private ExpressionEntity m_expressionEntity = null;
        
        public EntityNode(ExpressionEntity entity) {
            super(DataNodeType.VALUE);
            m_expressionEntity = entity;
        }
        
        public ExpressionEntity getExpressionEntity() {
            return m_expressionEntity;
        }
        
        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.CONTROL_SMALL);
        }

        @Override
        public String toString() {
            return m_expressionEntity.getName();
        }
    }

    
    private class EntityTreeRenderer extends DefaultTreeCellRenderer {

        public EntityTreeRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DataNode node = ((DataNode) value);

            boolean visible = node.isVisible();
            if (!visible) {
                setForeground(Color.gray);
            } else {
                setForeground(Color.black);
            }

            ImageIcon icon = node.getIcon();
            if (icon != null) {
                setIcon(icon);
            }

            return this;
        }
    }

}
