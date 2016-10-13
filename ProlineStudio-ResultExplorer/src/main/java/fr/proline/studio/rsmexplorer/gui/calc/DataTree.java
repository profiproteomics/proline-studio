package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AdjustPFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.BBinomialFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ColumnFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ComputeFDRFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffAnalysisFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.FilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ImportTSVFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.JoinFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.LogFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.MissingValuesImputationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.NormalizationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.PValueFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.QuantiFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.TtdFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.AbstractGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.BoxPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.CalibrationPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.DensityPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ScatterGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.VarianceDistPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.macros.AbstractMacro;
import fr.proline.studio.rsmexplorer.gui.calc.macros.ProStarMacro;
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
 * Tree Panel for the DataAnalyzer with data and functions
 * @author JM235353
 */
public abstract class DataTree extends JTree {

    private ParentDataNode m_parentDataNode = null;
    private ParentMacrosNode m_parentMacrosNode = null;
    private ParentFunctionNode m_parentFunctionNode = null;
    private ParentGraphicNode m_parentGraphicNode = null;

    
    protected DataTree(DataNode root, boolean tableOnly, GraphPanel graphPanel) {
        super(root);
        
        if (tableOnly) {
            m_parentDataNode = (ParentDataNode) root;
            fillDataNodes(m_parentDataNode);
        } else {
            m_parentDataNode = new ParentDataNode();
            fillDataNodes(m_parentDataNode);
            root.add(m_parentDataNode); 
            
            m_parentMacrosNode = new ParentMacrosNode();
            fillMacrosNodes(m_parentMacrosNode);
            root.add(m_parentMacrosNode);
            
            m_parentFunctionNode = new ParentFunctionNode();
            fillFunctionNodes(m_parentFunctionNode);
            root.add(m_parentFunctionNode);

            m_parentGraphicNode = new ParentGraphicNode();
            fillGraphicNodes(m_parentGraphicNode);
            root.add(m_parentGraphicNode);
            
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.nodeStructureChanged(root);
            
            setDragEnabled(true);
            setTransferHandler(new DataTreeTransferHandler(graphPanel));
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
        
        for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
        }
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
    
    private void fillMacrosNodes(ParentMacrosNode parentMacrosNode) {
        ProStarMacro proStar = new ProStarMacro();
        parentMacrosNode.add( new MacroNode(proStar));
    }
    
    private void fillFunctionNodes(ParentFunctionNode parentFunctionNode) {
        
        FunctionNode adjustPFunction = new FunctionNode(new AdjustPFunction(null));
        parentFunctionNode.add(adjustPFunction);
        
        FunctionNode diffFunction = new FunctionNode(new DiffFunction(null));
        parentFunctionNode.add(diffFunction);
        
        FunctionNode tsvFunction = new FunctionNode(new ImportTSVFunction(null));
        parentFunctionNode.add(tsvFunction);
        
        FunctionNode joinFunction = new FunctionNode(new JoinFunction(null));
        parentFunctionNode.add(joinFunction);
        
        FunctionNode bbinomialFunction = new FunctionNode(new BBinomialFunction(null));
        parentFunctionNode.add(bbinomialFunction);

        FunctionNode pvalueFunction = new FunctionNode(new PValueFunction(null));
        parentFunctionNode.add(pvalueFunction);
        
        FunctionNode ttdFunction = new FunctionNode(new TtdFunction(null));
        parentFunctionNode.add(ttdFunction);

        FunctionNode columnFilterFunction = new FunctionNode(new ColumnFilterFunction(null));
        parentFunctionNode.add(columnFilterFunction);
        
        FunctionNode filterFunction = new FunctionNode(new FilterFunction(null));
        parentFunctionNode.add(filterFunction);
        
        FunctionNode quantiFilterFunction = new FunctionNode(new QuantiFilterFunction(null));
        parentFunctionNode.add(quantiFilterFunction);
        
        FunctionNode normalizationFunction = new FunctionNode(new NormalizationFunction(null));
        parentFunctionNode.add(normalizationFunction);
        
        FunctionNode missingValuesImputationFunction = new FunctionNode(new MissingValuesImputationFunction(null));
        parentFunctionNode.add(missingValuesImputationFunction);

        FunctionNode diffAnalysisFunction = new FunctionNode(new DiffAnalysisFunction(null));
        parentFunctionNode.add(diffAnalysisFunction);
        
        FunctionNode computeFDRFunction = new FunctionNode(new ComputeFDRFunction(null));
        parentFunctionNode.add(computeFDRFunction);
        
        FunctionNode logFunction = new FunctionNode(new LogFunction(null, false));
        parentFunctionNode.add(logFunction);
        
        FunctionNode log10Function = new FunctionNode(new LogFunction(null, true));
        parentFunctionNode.add(log10Function);
        
        
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.nodeStructureChanged(parentFunctionNode);
    }
    
    private void fillGraphicNodes(ParentGraphicNode parentGraphicNode) {
        
        GraphicNode node = new GraphicNode(new BoxPlotGraphic(null));
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new CalibrationPlotGraphic(null));
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new DensityPlotGraphic(null));
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new ScatterGraphic(null));
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new VarianceDistPlotGraphic(null));
        parentGraphicNode.add(node);
        
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.nodeStructureChanged(parentGraphicNode);
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
            PARENT_MACROS,
            PARENT_FUNCTION,
            FUNCTION,
            MACRO,
            PARENT_GRAPHIC,
            GRAPHIC,
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
            return IconManager.getIcon(IconManager.IconType.DATA_ANALYZER);
        }

        @Override
        public String toString() {
            return "Data Analyzer";
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
    
    public static class ParentMacrosNode extends DataNode {

        public ParentMacrosNode() {
            super(DataNodeType.PARENT_MACROS);
        }
        
        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.GEAR);
        }

        @Override
        public String toString() {
            return "Macros";
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
    
    public static class ParentGraphicNode extends DataNode {

        public ParentGraphicNode() {
            super(DataNode.DataNodeType.PARENT_GRAPHIC);
        }
        
        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.WAVE);
        }

        @Override
        public String toString() {
            return "Graphics";
        }
    } 
    
    public static class MacroNode extends DataNode {

        private AbstractMacro m_macro = null;

        public MacroNode(AbstractMacro macro) {
            super(DataNode.DataNodeType.MACRO);
            m_macro = macro;
        }

        public AbstractMacro getMacro() {
            return m_macro;
        }

        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.GEAR);
        }

        @Override
        public String toString() {
            return m_macro.getName();
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
    
    public static class GraphicNode extends DataNode {

        private AbstractGraphic m_graphic = null;

        public GraphicNode(AbstractGraphic function) {
            super(DataNodeType.GRAPHIC);
            m_graphic = function;
        }

        public AbstractGraphic getGraphic() {
            return m_graphic;
        }

        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.WAVE);
        }

        @Override
        public String toString() {
            return m_graphic.getName();
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
