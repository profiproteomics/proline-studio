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
package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AdjustPFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.SCDiffAnalysisFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ColumnFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ComputeFDRFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffAnalysisFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ExpressionFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.FilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ImportTSVFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.JoinFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.LogFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.MissingValuesImputationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.NormalizationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.QuantiFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.AbstractGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.BoxPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.CalibrationPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.DensityPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ParallelCoordinatesGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ScatterOrHistogramGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.VarianceDistPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.VennDiagramGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.macros.AbstractMacro;
import fr.proline.studio.rsmexplorer.gui.calc.macros.ProStarMacro;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rsmexplorer.gui.calc.macros.MacroSavedManager;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Tree Panel for the DataAnalyzer with data and functions
 * 
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
        setCellRenderer(new DataAnalyzerTreeRenderer()); 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    triggerPopup(e);
                }else if (e.getClickCount() == 2) {

                     TreePath path = getSelectionPath();
                     if (path == null) {
                         return;
                     }
                     DataNode node = (DataNode) path.getLastPathComponent();
                     
                     // check for R functions that R server is started
                     boolean actionNotAllowed = (node.isRNeeded() && (!RServerManager.getRServerManager().isRStarted()));
                     if (actionNotAllowed) {
                         return;
                     }
                     
                     action(node);

                     clearSelection();
                }

            }
        });
        
        // expand first level of the tree
        DefaultMutableTreeNode currentNode = root.getNextNode();
        do {
            if (currentNode.getLevel() == 1) {
                expandPath(new TreePath(currentNode.getPath()));
            }
            currentNode = currentNode.getNextNode();
        } while (currentNode != null);
    }
    
    private void triggerPopup(MouseEvent e) {
        // retrieve selected nodes
        DataNode[] selectedNodes = getSelectedNodes();
        
        int nbNodes = selectedNodes.length;

        if (nbNodes == 0) {
            // nothing selected
            return;
        }
        
        for (int i = 0; i < nbNodes; i++) {
            DataNode n = selectedNodes[i];
            if (n.getType() != DataNode.DataNodeType.USERMACRO) {
                return; // popup only on user macro
            }
        }
        
        
        JPopupMenu userMacroPopup = new JPopupMenu();
        
        final DataTree _tree = this;
        userMacroPopup.add(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTreeModel model = (DefaultTreeModel) (_tree.getModel());
                TreePath[] paths = _tree.getSelectionPaths();
                for (int i = 0; i < paths.length; i++) {
                    UserMacroNode node = (UserMacroNode) (paths[i].getLastPathComponent());
                    String macroName = node.toString();
                    model.removeNodeFromParent(node);
                    
                    MacroSavedManager.removeSavedMacro(macroName);
                }
            }
            
        });
        
        userMacroPopup.show((JComponent) e.getSource(), e.getX(), e.getY());
    }
    
    public void updataDataNodes() {
        fillDataNodes(m_parentDataNode);
    }
    
    public void addUserMacro(String xmlMacro) {
        UserMacroNode node = new UserMacroNode(xmlMacro);
        m_parentMacrosNode.add(node);
        
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.nodeStructureChanged(m_parentMacrosNode);
    }
    
    public abstract void action(DataNode node);

    
    private void fillDataNodes(ParentDataNode parentNode) {
                
        parentNode.removeAllChildren();

        HashMap<Integer, JXTable> tableMap = new HashMap<>();
        
        ArrayList<TableInfo> list = new ArrayList<>();



        Set< AbstractTopPanel > tcs = WindowManager.getDefault().getMainWindow().getTopPanels();
        Iterator<AbstractTopPanel> itTop = tcs.iterator();
        while (itTop.hasNext()) {
            AbstractTopPanel topComponent = itTop.next();
            if (topComponent instanceof DataBoxViewerTopPanel) {
                list.clear();
                DataBoxViewerTopPanel DataBoxViewerTopPanel = (DataBoxViewerTopPanel) topComponent;
                DataBoxViewerTopPanel.retrieveTableModels(list);
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
        parentMacrosNode.add( new MacroNode(proStar, true));
        
        ArrayList<String> macros = MacroSavedManager.readSavedMacros();
        if (macros != null) {
            for (String xmlMacro : macros) {
                UserMacroNode node = new UserMacroNode(xmlMacro);
                parentMacrosNode.add(node);
            }
        }
        
        
    }
    
    private void fillFunctionNodes(ParentFunctionNode parentFunctionNode) {
        
        ParentFunctionNode tableFonctionsNode = new ParentFunctionNode("Table");
        parentFunctionNode.add(tableFonctionsNode);
        
        FunctionNode tsvFunction = new FunctionNode(new ImportTSVFunction(null), false);
        tableFonctionsNode.add(tsvFunction);
        
        FunctionNode columnFilterFunction = new FunctionNode(new ColumnFilterFunction(null), false);
        tableFonctionsNode.add(columnFilterFunction);
        
        FunctionNode filterFunction = new FunctionNode(new FilterFunction(null), false);
        tableFonctionsNode.add(filterFunction);
        
        FunctionNode expressionFunction = new FunctionNode(new ExpressionFunction(null), false);
        tableFonctionsNode.add(expressionFunction);
        
        FunctionNode logFunction = new FunctionNode(new LogFunction(null, false), false);
        tableFonctionsNode.add(logFunction);
        
        FunctionNode log10Function = new FunctionNode(new LogFunction(null, true), false);
        tableFonctionsNode.add(log10Function);
        
        FunctionNode diffFunction = new FunctionNode(new DiffFunction(null), false);
        tableFonctionsNode.add(diffFunction);
        
        FunctionNode joinFunction = new FunctionNode(new JoinFunction(null), false);
        tableFonctionsNode.add(joinFunction);
        
        
        
        ParentFunctionNode statsFonctionsNode = new ParentFunctionNode("Statistics");
        parentFunctionNode.add(statsFonctionsNode);
        
        FunctionNode quantiFilterFunction = new FunctionNode(new QuantiFilterFunction(null), false);
        statsFonctionsNode.add(quantiFilterFunction);
        
        FunctionNode missingValuesImputationFunction = new FunctionNode(new MissingValuesImputationFunction(null), true);
        statsFonctionsNode.add(missingValuesImputationFunction);
        
        FunctionNode normalizationFunction = new FunctionNode(new NormalizationFunction(null), true);
        statsFonctionsNode.add(normalizationFunction);
        
        FunctionNode bbinomialFunction = new FunctionNode(new SCDiffAnalysisFunction(null), true);
        statsFonctionsNode.add(bbinomialFunction);
        
        FunctionNode diffAnalysisFunction = new FunctionNode(new DiffAnalysisFunction(null), true);
        statsFonctionsNode.add(diffAnalysisFunction);

        FunctionNode adjustPFunction = new FunctionNode(new AdjustPFunction(null), true);
        statsFonctionsNode.add(adjustPFunction);
        
        FunctionNode computeFDRFunction = new FunctionNode(new ComputeFDRFunction(null), true);
        statsFonctionsNode.add(computeFDRFunction);

//        FunctionNode pvalueFunction = new FunctionNode(new PValueFunction(null));
//        parentFunctionNode.add(pvalueFunction);
//        
//        FunctionNode ttdFunction = new FunctionNode(new TtdFunction(null));
//        parentFunctionNode.add(ttdFunction);
        
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.nodeStructureChanged(parentFunctionNode);
    }
    
    private void fillGraphicNodes(ParentGraphicNode parentGraphicNode) {
        
        GraphicNode node = new GraphicNode(new BoxPlotGraphic(null), true);
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new CalibrationPlotGraphic(null), true);
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new DensityPlotGraphic(null), true);
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new ParallelCoordinatesGraphic(null), false);
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new ScatterOrHistogramGraphic(null, PlotType.HISTOGRAM_PLOT), false);
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new ScatterOrHistogramGraphic(null, PlotType.SCATTER_PLOT), false);
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new VarianceDistPlotGraphic(null), true);
        parentGraphicNode.add(node);
        
        node = new GraphicNode(new VennDiagramGraphic(null), false);
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
    
    /**
     * Base class for all node of the Tree
     */
    public static abstract class DataNode extends DefaultMutableTreeNode {

        public enum DataNodeType {
            ROOT_DATA_ANALYZER,
            PARENT_DATA,
            PARENT_MACROS,
            PARENT_FUNCTION,
            FUNCTION,
            MACRO,
            USERMACRO,
            PARENT_GRAPHIC,
            GRAPHIC,
            WINDOW_DATA,
            VIEW_DATA,
            COLUMN_DATA
        }
        
        private DataNodeType m_type;
        private boolean m_needsR;
   
        public DataNode(DataNodeType type, boolean needsR) {
            m_type =  type;
            m_needsR = needsR;
        }

        public DataNode(DataNodeType type, TableInfo tableInfo, boolean needsR) {
            m_type =  type;
            m_needsR = needsR;
            setUserObject(tableInfo);
        }
        
        public boolean isRNeeded() {
            return m_needsR;
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

    /**
     * Parent Node with name "Data Analyzer"
     */
    public static class RootDataAnalyzerNode extends DataNode {

        public RootDataAnalyzerNode() {
            super(DataNodeType.ROOT_DATA_ANALYZER, false);
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
            super(DataNodeType.PARENT_DATA, false);
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
            super(DataNodeType.PARENT_MACROS, false);
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

        String name;
        
        public ParentFunctionNode() {
            this("Functions");
        }
        
        public ParentFunctionNode(String label) {
            super(DataNodeType.PARENT_FUNCTION, false);
            this.name = label;
        }
        
        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.FUNCTION);
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public static class ParentGraphicNode extends DataNode {

        public ParentGraphicNode() {
            super(DataNode.DataNodeType.PARENT_GRAPHIC, false);
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

        public MacroNode(AbstractMacro macro, boolean needsR) {
            super(DataNode.DataNodeType.MACRO, needsR);
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
    
    public static class UserMacroNode extends DataNode {

        private String m_xmlMacro = null;
        private String m_name = "";

        public UserMacroNode(String xmlMacro) {
            super(DataNode.DataNodeType.USERMACRO, false);
            m_xmlMacro = xmlMacro;
            final String ID_DATAANALYZER_XML = "<dataanalyzer name=\"";
            int indexStartName = xmlMacro.indexOf(ID_DATAANALYZER_XML);
            if (indexStartName != -1) {
                indexStartName += ID_DATAANALYZER_XML.length();
                int indexEndName = xmlMacro.indexOf("\"",indexStartName+1);
                if (indexEndName != -1) {
                    m_name = m_xmlMacro.substring(indexStartName, indexEndName);
                }
            }
        }

        public String getXMLMacro() {
            return m_xmlMacro;
        }

        @Override
        public ImageIcon getIcon() {
            return IconManager.getIcon(IconManager.IconType.GEAR);
        }

        @Override
        public String toString() {
            return m_name;
        }
    }
    
    public static class FunctionNode extends DataNode {

        private AbstractFunction m_function = null;
        
        public FunctionNode(AbstractFunction function, boolean needsR) {
            super(DataNodeType.FUNCTION, needsR);
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

        public GraphicNode(AbstractGraphic function, boolean needsR) {
            super(DataNodeType.GRAPHIC, needsR);
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
            super(DataNodeType.WINDOW_DATA, tableInfo, false);
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
            super(DataNodeType.VIEW_DATA, tableInfo, false);
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
            super(DataNodeType.COLUMN_DATA, tableInfo, false);
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
    
    /**
     * Renderer for the tree of the Data Analyzer.
     * 
     * It manages icons and grayed nodes
     */
    private class DataAnalyzerTreeRenderer extends DefaultTreeCellRenderer {

        public DataAnalyzerTreeRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DataNode node = ((DataNode) value);

            boolean grayed =  !node.isVisible() || (node.isRNeeded() && (!RServerManager.getRServerManager().isRStarted()));
            if (grayed) {
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
