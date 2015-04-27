package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.JoinFunction;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.windows.TopComponent;

/**
 *
 * @author JM235353
 */
public abstract class DataTree extends JTree {
  
    //private DataNode m_root = null;
    
    private ParentDataNode m_parentDataNode = null;
    private ParentFunctionNode m_parentFunctionNode = null;
    
    /*public static DataTree createDataTree(boolean tableOnly) {
        
        DataNode root = null;
        
        if (tableOnly) {
            root = new ParentDataNode();
        } else {
            root = new RootDataMixerNode();
        }
        return new DataTree(root, tableOnly);
    }*/
    
    protected DataTree(DataNode root, boolean tableOnly) {
        super(root);
        
        if (tableOnly) {
            m_parentDataNode = (ParentDataNode) root;
            fillDataNodes(m_parentDataNode);
        } else {
            m_parentDataNode = new ParentDataNode();
            fillDataNodes(m_parentDataNode);
            root.add(m_parentDataNode); 
            
            m_parentFunctionNode = new ParentFunctionNode();
            fillFunctionNodes(m_parentFunctionNode);
            root.add(m_parentFunctionNode);

            DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.nodeStructureChanged(root);
            
            setDragEnabled(true);
            setTransferHandler(new DataTreeTransferHandler());
        }
        
        setToggleClickCount(0); // avoid expanding when double clicking
        setCellRenderer(new DataMixerTreeRenderer()); 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {

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
    }
    
    public void updataDataNodes() {
        fillDataNodes(m_parentDataNode);
    }
    
    public abstract void action(DataNode node);

    
    private void fillDataNodes(ParentDataNode parentNode) {
                
        parentNode.removeAllChildren();

        HashMap<Integer, JXTable> tableMap = new HashMap<>();
        
        ArrayList<TableInfo> list = new ArrayList<>();
        
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        Iterator<TopComponent> itTop = tcs.iterator();
        while (itTop.hasNext()) {
            TopComponent topComponent = itTop.next();
            if (topComponent instanceof DataBoxViewerTopComponent) {
                list.clear();
                DataBoxViewerTopComponent databoxViewerTopComponent = (DataBoxViewerTopComponent) topComponent;
                databoxViewerTopComponent.retrieveTableModels(list);
                int nb = list.size();
                if (nb > 0) {
                    WindowDataNode windowDataNode = new WindowDataNode(list.get(0));
                    parentNode.add(windowDataNode);
                    for (int i = 0; i < nb; i++) {
                        TableInfo tableInfo = list.get(i);
                        ViewDataNode viewNode = new ViewDataNode(tableInfo);
                        windowDataNode.add(viewNode);
                        viewNode.fillColumns();
                        tableMap.put(tableInfo.getId(), tableInfo.getTable());
                    }
                }

            }
        }

        Table.setTables(tableMap);
        
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.nodeStructureChanged(parentNode);   
    }
    
    private void fillFunctionNodes(ParentFunctionNode parentFunctionNode) {
        
        FunctionNode joinFunction = new FunctionNode(new JoinFunction());
        parentFunctionNode.add(joinFunction);
        
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.nodeStructureChanged(parentFunctionNode);
    }

    public DataNode[] getSelectedNodes() {
        TreePath[] paths = getSelectionModel().getSelectionPaths();

        int nbPath = paths.length;

        DataNode[] nodes = new DataNode[nbPath];

        for (int i = 0; i < nbPath; i++) {
            nodes[i] = (DataNode) paths[i].getLastPathComponent();
        }

        return nodes;
    }
    
    public static abstract class DataNode extends DefaultMutableTreeNode {

        public enum DataNodeType {
            ROOT_DATA_MIXER,
            PARENT_DATA,
            PARENT_FUNCTION,
            FUNCTION,
            WINDOW_DATA,
            VIEW_DATA,
            COLUMN_DATA
        }
        
        private DataNodeType m_type;
        
        public DataNode(DataNodeType type) {
            m_type =  type;
        }

        public DataNode(DataNodeType type, TableInfo tableInfo) {
            m_type =  type;
            setUserObject(tableInfo);
        }

        public DataNodeType getType() {
            return m_type;
        }
        
        public TableInfo getTableInfo() {
            return ((TableInfo) getUserObject());
        }


        public boolean isVisible() {
            return true;
        }

        public abstract ImageIcon getIcon();
    }

    public static class RootDataMixerNode extends DataNode {

        public RootDataMixerNode() {
            super(DataNodeType.ROOT_DATA_MIXER);
        }

        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.DATA_MIXER);
        }

        @Override
        public String toString() {
            return "Data Mixer";
        }
    }
    
    public static class ParentDataNode extends DataNode {

        public ParentDataNode() {
            super(DataNodeType.PARENT_DATA);
        }
        
        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.TABLES);
        }

        @Override
        public String toString() {
            return "Data Windows";
        }
    }
    


    public static class ParentFunctionNode extends DataNode {

        public ParentFunctionNode() {
            super(DataNodeType.PARENT_FUNCTION);
        }
        
        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.FUNCTION);
        }

        @Override
        public String toString() {
            return "Functions";
        }
    }
    
    public static class FunctionNode extends DataNode {

        private AbstractFunction m_function = null;
        
        public FunctionNode(AbstractFunction function) {
            super(DataNodeType.FUNCTION);
            m_function = function;
        }
        
        public AbstractFunction getFunction() {
            return m_function;
        }
        
        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.FUNCTION);
        }

        @Override
        public String toString() {
            return m_function.getName();
        }
    }
    
    private class WindowDataNode extends DataNode {

        public WindowDataNode(TableInfo tableInfo) {
            super(DataNodeType.WINDOW_DATA, tableInfo);
        }

        @Override
        public ImageIcon getIcon() {
            TableInfo tableInfo = getTableInfo();
            return tableInfo.getIcon();
        }

        @Override
        public String toString() {
            TableInfo tableInfo = getTableInfo();
            return tableInfo.getFullName();
        }
    }

    public class ViewDataNode extends DataNode {

        public ViewDataNode(TableInfo tableInfo) {
            super(DataNodeType.VIEW_DATA, tableInfo);
        }

        public void fillColumns() {
            TableInfo tableInfo = getTableInfo();

            JXTable table = tableInfo.getTable();
            List<TableColumn> tableColumnList = table.getColumns(true);
            for (TableColumn c : tableColumnList) {
                TableColumnExt tableColumn = (TableColumnExt) c;
                boolean visible = tableColumn.isVisible();
                int modelIndex = tableColumn.getModelIndex();
                ColumnDataNode colNode = new ColumnDataNode(tableInfo, modelIndex + 1, visible);
                add(colNode);

            }

        }

        public int getTableIndex() {
            TableInfo tableInfo = getTableInfo();
            return tableInfo.getId();
        }


        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.TABLE);
        }

        @Override
        public String toString() {
            TableInfo tableInfo = getTableInfo();
            return tableInfo.getNameWithId();
        }
    }

    public class ColumnDataNode extends DataNode {

        private final int m_columnIndex;
        private final boolean m_visible;

        public ColumnDataNode(TableInfo tableInfo, int columnIndex, boolean visible) {
            super(DataNodeType.COLUMN_DATA, tableInfo);
            m_columnIndex = columnIndex;
            m_visible = visible;
        }
        
        public int getColumnIndex() {
            return m_columnIndex;
        }


        @Override
        public boolean isVisible() {
            return m_visible;
        }

        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.COLUMN);
        }

        @Override
        public String toString() {
            TableInfo tableInfo = getTableInfo();
            GlobalTableModelInterface model = tableInfo.getModel();
            return m_columnIndex + ": " + model.getExportColumnName(m_columnIndex - 1);
        }
    }
    
    private class DataMixerTreeRenderer extends DefaultTreeCellRenderer {

        public DataMixerTreeRenderer() {
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
