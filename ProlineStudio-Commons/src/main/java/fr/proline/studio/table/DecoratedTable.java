package fr.proline.studio.table;

import com.thierry.filtering.TableSelection;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.AbstractParameter.LabelVisibility;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.SettingsInterface;
import fr.proline.studio.utils.RelativePainterHighlighter;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table which has a bi-color striping, an ability to select columns viewed and
 * the possibility to display an "histogram" on a column
 *
 * @author JM235353
 */
public abstract class DecoratedTable extends JXTable implements CrossSelectionInterface, SettingsInterface {

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio");
   
    private RelativePainterHighlighter.NumberRelativizer m_relativizer = null;
    
    private TablePopupMenu m_popupMenu;
    
    private Highlighter m_stripingHighlighter = null;

    private BooleanParameter m_autoSizeColumnParameter = null;
    private IntegerParameter m_columnWidthParameter = null;
    private MultiObjectParameter m_columnsGroupVisibilityParameter = null;
    private MultiObjectParameter m_columnsVisibilityParameter = null;
    private ArrayList<ParameterList> m_parameterListArray = null;
    
    private ObjectParameter m_sortCol1Parameter = null;
    private ObjectParameter m_sortOrderCol1Parameter = null;
    private ObjectParameter m_sortCol2Parameter = null;
    private ObjectParameter m_sortOrderCol2Parameter = null;


    
    private static final String AUTOSIZE_COLUMN_KEY = "AUTOSIZE_COLUMN_KEY";
    private static final String COLUMN_WIDTH_KEY = "COLUMN_WIDTH_KEY";
    private static final String COLUMNS_VISIBILITY_KEY = "COLUMNS_VISIBILITY_KEY";
    private static final String COLUMNS_GROUP_VISIBILITY_KEY = "COLUMNS_GROUP_VISIBILITY_KEY";
    
    public DecoratedTable() {

        // allow user to hide/show columns
        setColumnControlVisible(true);

        // highlight one line of two
        m_stripingHighlighter = HighlighterFactory.createSimpleStriping();
        addHighlighter(m_stripingHighlighter);

        setGridColor(Color.lightGray);
        setRowHeight(16);
        setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED);
        
        TableSelection.installCopyAction(this);
  
        TablePopupMenu popup = initPopupMenu();
        if (popup != null) {
            setTablePopup(popup);
        }

    }
    
    protected void initParameters() {
 
        ParameterList parameterTableList = new ParameterList("Table Parameters");

        final boolean autoResized = (getAutoResizeMode() != JXTable.AUTO_RESIZE_OFF);
        m_autoSizeColumnParameter = new BooleanParameter(AUTOSIZE_COLUMN_KEY, "Auto-size Columns", JCheckBox.class, autoResized);
        

        
        List<TableColumn> columns = getColumns(true);
        int colCount = columns.size();
        Object[] columnNamesArray = new Object[colCount];
        Object[] columnsArray = new Object[colCount];
        boolean[] selection = new boolean[colCount];

        TableModel model = getModel();
        ExportModelInterface exportableModel = null;
        if (model instanceof ExportModelInterface) {
            exportableModel = (ExportModelInterface) model;
        }
        
        HashMap<String, Integer> similarColumnsNumberMap = new HashMap<>();
        HashMap<String, Boolean> similarColumnsVisibilityMap = new HashMap<>();
        HashMap<String, String> similarColumnsColorsMap = new HashMap<>();
        
        int nbVisible = 0;
        int totalWidth = 0;
        for (int i = 0; i < colCount; i++) {
            TableColumnExt column = (TableColumnExt) columns.get(i);
            columnNamesArray[i] = column.getHeaderValue().toString().replaceAll("<br/>"," ");
            columnsArray[i] = column;
            
            boolean visible = column.isVisible();
            
            if (exportableModel != null) {
                
                String columnFullName = model.getColumnName(column.getModelIndex());
                String columnExportName = exportableModel.getExportColumnName(column.getModelIndex());
                int indexSpace = columnExportName.lastIndexOf(' ');
                if (indexSpace != -1) {
                    columnExportName = columnExportName.substring(0, indexSpace);
                }
                if (columnExportName.length()>0) {
                    Integer nb = similarColumnsNumberMap.get(columnExportName);
                    if (nb == null) {
                        similarColumnsNumberMap.put(columnExportName, 1);
                        similarColumnsVisibilityMap.put(columnExportName, visible);
                    } else {
                        similarColumnsNumberMap.put(columnExportName, nb+1);
                        if (!visible) {
                            similarColumnsVisibilityMap.put(columnExportName, visible);
                        }
                    }
                    
                    int colorIndexStart = columnFullName.indexOf("<font color='", 0);
                    int colorIndexStop = columnFullName.indexOf("</font>", 0);
                    if ((colorIndexStart>-1) && (colorIndexStop>colorIndexStart)) {
                        String colorName = columnFullName.substring(colorIndexStart,colorIndexStop+"</font>".length());
                        String curColorName = similarColumnsColorsMap.get(columnExportName);
                        if (curColorName != null) {
                            if (curColorName.compareTo(colorName) != 0) {
                                similarColumnsColorsMap.put(columnExportName, "");
                            }
                        } else {
                            similarColumnsColorsMap.put(columnExportName, colorName);
                        }
                    } else {
                        similarColumnsColorsMap.put(columnExportName, "");
                    }
                }
                if (indexSpace != -1) {
                    columnExportName = exportableModel.getExportColumnName(column.getModelIndex());
                    columnExportName = columnExportName.substring(indexSpace, columnExportName.length());
                    if (columnExportName.length() > 0) {
                        Integer nb = similarColumnsNumberMap.get(columnExportName);
                        if (nb == null) {
                            similarColumnsNumberMap.put(columnExportName, 1);
                            similarColumnsVisibilityMap.put(columnExportName, visible);
                        } else {
                            similarColumnsNumberMap.put(columnExportName, nb + 1);
                            if (!visible) {
                                similarColumnsVisibilityMap.put(columnExportName, visible);
                            }
                        }
                        
                        int colorIndexStart = columnFullName.indexOf("<font color='", 0);
                        int colorIndexStop = columnFullName.indexOf("</font>", 0);
                        if ((colorIndexStart > -1) && (colorIndexStop > colorIndexStart)) {
                            String colorName = columnFullName.substring(colorIndexStart, colorIndexStop + "</font>".length());
                            String curColorName = similarColumnsColorsMap.get(columnExportName);
                            if (curColorName != null) {
                                if (curColorName.compareTo(colorName) != 0) {
                                    similarColumnsColorsMap.put(columnExportName, "");
                                }
                            } else {
                                similarColumnsColorsMap.put(columnExportName, colorName);
                            }
                        } else {
                            similarColumnsColorsMap.put(columnExportName, "");
                        }
                    }
                }
            }
            
            
            selection[i] = visible;
            if (visible) {
                totalWidth += column.getWidth();
                nbVisible++;
            }
        }
        
        // suppression des lignes à un résultat dans similarColumnsNumberMap
        Set<String> colNamesSet = similarColumnsNumberMap.keySet();
        String[] colNamesArray = colNamesSet.toArray(new String[colNamesSet.size()]);
        for (int i=0;i<colNamesArray.length;i++) {
            String colName = colNamesArray[i];
            Integer nb = similarColumnsNumberMap.get(colName);
            if (nb<=1) {
                similarColumnsNumberMap.remove(colName);
                similarColumnsVisibilityMap.remove(colName);
            }
        }
        ColumnGroup[] groups = null;
        int nbGroups = similarColumnsNumberMap.size();
        if (nbGroups>0) {
            groups = new ColumnGroup[nbGroups];
            Iterator<String> it = similarColumnsNumberMap.keySet().iterator();
            int i=0;
            while (it.hasNext()) {
                String name = it.next();
                String colorName = similarColumnsColorsMap.get(name);
                groups[i] = new ColumnGroup((colorName.length()>0) ? "<html>"+colorName+name+"</html>" : name, similarColumnsVisibilityMap.get(name).booleanValue());
                i++;
            }
            
            Arrays.sort( groups );
            Object[] columnGroupNamesArray = new Object[nbGroups];
            boolean[] groupSelection = new boolean[nbGroups];
            for (i=0;i<nbGroups;i++) {
                columnGroupNamesArray[i] = groups[i].m_groupName;
                groupSelection[i] = groups[i].m_selected;
            }
            m_columnsGroupVisibilityParameter = new MultiObjectParameter(COLUMNS_GROUP_VISIBILITY_KEY, "Columns Type Visibility", null, columnGroupNamesArray, null, groupSelection, null, false);
        }
        
        final int MAX_WIDTH = 500;
        int meanWidth = totalWidth / nbVisible;
        if (meanWidth<30) {
            meanWidth = 30;
        } else if (meanWidth>MAX_WIDTH) {
            meanWidth = MAX_WIDTH;
        }
        m_columnWidthParameter = new IntegerParameter(COLUMN_WIDTH_KEY, "Column Width", JTextField.class, meanWidth, 30, MAX_WIDTH);

        
        
        
        m_columnsVisibilityParameter = new MultiObjectParameter(COLUMNS_VISIBILITY_KEY, "Columns Visibility", null, columnNamesArray, columnsArray, selection, null, true);
        

        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterTableList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_columnWidthParameter, (value.compareTo((autoResized) ? "false" : "true") == 0));

                updataParameterListPanel();
            }

        };
        
        AbstractLinkedParameters linkedParameter2 = null;
        if (m_columnsGroupVisibilityParameter != null) {
            
            final ExportModelInterface _exportableModel = exportableModel;
            
            linkedParameter2 = new AbstractLinkedParameters(parameterTableList) {
                @Override
                public void valueChanged(String value, Object associatedValue) {
                    
                    // color
                    int colorRemoveStart = value.indexOf("</font>", 0);
                    int colorRemoveStop = value.indexOf("</html>", 0);
                    if ((colorRemoveStart>-1) && (colorRemoveStop>colorRemoveStart)) {
                        value = value.substring(colorRemoveStart+"</font>".length(), colorRemoveStop);
                    }
                    
                    for (int i = 0; i < colCount; i++) {
                        TableColumnExt column = (TableColumnExt) columns.get(i);
                        String columnExportName = _exportableModel.getExportColumnName(column.getModelIndex());
                        int indexSpace = columnExportName.lastIndexOf(' ');
                        if (indexSpace != -1) {
                            columnExportName = columnExportName.substring(0, indexSpace);
                        }
                        if (columnExportName.compareTo(value) == 0) {
                            m_columnsVisibilityParameter.setSelection(i, ((Boolean)associatedValue).booleanValue());
                        }
                        if (indexSpace != -1) {
                            columnExportName = _exportableModel.getExportColumnName(column.getModelIndex());
                            columnExportName = columnExportName.substring(indexSpace, columnExportName.length());
                            if (columnExportName.compareTo(value) == 0) {
                                m_columnsVisibilityParameter.setSelection(i, ((Boolean) associatedValue).booleanValue());
                            }
                        }
                    }

                }

            };
        }
        
        parameterTableList.add(m_autoSizeColumnParameter);
        parameterTableList.add(m_columnWidthParameter);
        parameterTableList.add(m_columnsVisibilityParameter);      
        if (m_columnsGroupVisibilityParameter != null) {
            parameterTableList.add(m_columnsGroupVisibilityParameter);
        }
        
        
        
        m_parameterListArray = new ArrayList<>(3);
        m_parameterListArray.add(parameterTableList);

        parameterTableList.getPanel(true); // generate panel at once
        m_autoSizeColumnParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel
        linkedParameters.valueChanged("true", Boolean.TRUE);  //JPM.TOOD true or false
        
        
        if (m_columnsGroupVisibilityParameter != null) {
            m_columnsGroupVisibilityParameter.addLinkedParameters(linkedParameter2);
        }
        
        addSortingParameters();
    }
    
    
    private void addSortingParameters() {
        
        TableModel model = getModel();

        List<TableColumn> colums = getColumns(true);
        int nbCols = colums.size();
        
        String[] colNames = new String[nbCols+1];
        Integer[] indexes = new Integer[nbCols+1];
        colNames[0] = "< No Sorting >";
        indexes[0] = -1;
        for (int i=0;i<nbCols;i++) {
            indexes[i+1] = colums.get(i).getModelIndex();
            colNames[i+1] = model.getColumnName(indexes[i+1]);
        }

        RowSorter<TableModel> sorter = (RowSorter<TableModel>) getRowSorter();

        List<SortKey> sortKeys = (List<SortKey>) sorter.getSortKeys();
        int numberOfSortingKeys = Math.min(sortKeys.size(), 2);
        int[] colIndex = new int[2];
        SortOrder[] sorterOrder = new SortOrder[2];
        
        for (int i=0;i<numberOfSortingKeys;i++) {
            SortKey key = sortKeys.get(i);
            colIndex[i] = key.getColumn()+1;
            sorterOrder[i] = key.getSortOrder();
        }
        for (int i=numberOfSortingKeys;i<2;i++) {
            colIndex[i] = 0;
            sorterOrder[i] = SortOrder.ASCENDING;
        }
    
        String[] orderString = { "Ascending", "Descending" };
        SortOrder[] orderEnum = { SortOrder.ASCENDING, SortOrder.DESCENDING};
        
        m_sortCol1Parameter = new ObjectParameter("COL_1_SORT", "First Sorting Column", null, colNames, indexes, colIndex[0], null);
        m_sortOrderCol1Parameter = new ObjectParameter("COL_1_ORDER", "", null, orderString, orderEnum, (sorterOrder[0] != SortOrder.DESCENDING) ? 0 : 1, null);
        m_sortOrderCol1Parameter.forceShowLabel(LabelVisibility.NO_VISIBLE);
        m_sortCol2Parameter = new ObjectParameter("COL_2_SORT", "Second Sorting Column", null, colNames, indexes, colIndex[1], null);
        m_sortOrderCol2Parameter = new ObjectParameter("COL_2_ORDER", "", null, orderString, orderEnum, (sorterOrder[1] != SortOrder.DESCENDING) ? 0 : 1, null);
        m_sortOrderCol2Parameter.forceShowLabel(LabelVisibility.NO_VISIBLE);
        
        ParameterList parameterTableList = new ParameterList("Sorting Parameters");
        parameterTableList.add(m_sortCol1Parameter);
        parameterTableList.add(m_sortOrderCol1Parameter);
        parameterTableList.add(m_sortCol2Parameter);
        parameterTableList.add(m_sortOrderCol2Parameter);
        
        parameterTableList.getPanel(true); // generate panel at once
        
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterTableList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                boolean show = ((Integer)associatedValue) != -1;
                showParameter(m_sortOrderCol1Parameter, show);
                
                if (!show) {
                    showParameter(m_sortCol2Parameter, false);
                    showParameter(m_sortOrderCol2Parameter, false);
                } else {
                    showParameter(m_sortCol2Parameter, true);
                    showParameter(m_sortOrderCol2Parameter,((Integer) m_sortCol2Parameter.getAssociatedObjectValue()) != -1);
                }

                updataParameterListPanel();
            }

        };
        m_sortCol1Parameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel
        

        AbstractLinkedParameters linkedParameters2 = new AbstractLinkedParameters(parameterTableList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_sortOrderCol2Parameter, ((Integer)associatedValue) != -1);

                updataParameterListPanel();
            }

        };
        m_sortCol2Parameter.addLinkedParameters(linkedParameters2); // link parameter, it will modify the panel
        
        linkedParameters.valueChanged(colNames[colIndex[0]], indexes[colIndex[0]]);
        linkedParameters2.valueChanged(colNames[colIndex[1]], indexes[colIndex[1]]);
        
        m_parameterListArray.add(parameterTableList);
    }
    
    
    public void removeStriping() {
        removeHighlighter(m_stripingHighlighter);
    }

    
    public String getToolTipForHeader(int modelColumn) {
        return ((DecoratedTableModelInterface) getModel()).getToolTipForHeader(modelColumn);
    }

    
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new CustomTooltipTableHeader(this);
    }

    @Override
    public void select(ArrayList<Long> uniqueIds) {
        ListSelectionModel selModel = getSelectionModel();
        selModel.clearSelection();
        
        TableModel model = getModel();
        
        if (model instanceof CompareDataInterface) {
            CompareDataInterface compareDataInterface = (CompareDataInterface) model;
            for (Long id : uniqueIds) {
                int row = compareDataInterface.uniqueId2Row(id);
                if (row == -1) {
                    // id not found (possible after a filtering for instance)
                    continue;
                }
                row = convertRowIndexToView(row);
                selModel.addSelectionInterval(row, row);
            }
        } else {
            for (Long id : uniqueIds) {
                int row = convertRowIndexToView(id.intValue());  // should not happen
                selModel.addSelectionInterval(row, row);
            }
        }
        
        
       
        int minRow = selModel.getMinSelectionIndex();
        if (minRow != -1) {
            scrollRowToVisible(minRow);
        }
    }
    
    @Override
    public ArrayList<Long> getSelection() {

        ArrayList<Long> selectionList = new ArrayList<>();

        ListSelectionModel selModel = getSelectionModel();
        AbstractTableModel model = (AbstractTableModel) getModel();

        if (model instanceof CompareDataInterface) {
            CompareDataInterface compareDataInterface = (CompareDataInterface) model;
            for (int i = 0; i < model.getRowCount(); i++) {
                int row = convertRowIndexToView(i);
                if (selModel.isSelectedIndex(row)) {
                    selectionList.add(compareDataInterface.row2UniqueId(i));
                }
            }
        } else {
            for (int i = 0; i < model.getRowCount(); i++) {
                int row = convertRowIndexToView(i);
                if (selModel.isSelectedIndex(row)) {
                    selectionList.add((long) i);
                }
            }
        }

        return selectionList;
    }
    
    private class CustomTooltipTableHeader extends JXTableHeader {
        
        private DecoratedTable m_table;
        
        public CustomTooltipTableHeader(DecoratedTable table) {
            super(table.getColumnModel());
            m_table = table;
        }
        
        @Override
        public String getToolTipText(MouseEvent e) {
            
            Point p = e.getPoint();
            int column = columnAtPoint(p);
            if (column != -1) {
                column = m_table.convertColumnIndexToModel(column);
                String tooltip = getToolTipForHeader(column);
                if (tooltip != null) {
                    return tooltip;
                }
            }

            return super.getToolTipText(e);
        }
    }
    
    private void setTablePopup(TablePopupMenu popupMenu) {
        if (m_popupMenu == null) {
            addMouseListener(new TablePopupMouseAdapter(this));
        }
                    
        
        m_popupMenu = popupMenu;
    }
    
    public TablePopupMenu getTablePopup() {
        m_popupMenu.preparePopup();
        return m_popupMenu;
    }

    public abstract TablePopupMenu initPopupMenu();

    public abstract void prepostPopupMenu();
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        TableModel model = getModel();
        if (model instanceof GlobalTableModelInterface) {
            int columnInModel = convertColumnIndexToModel(column);
            TableCellRenderer renderer = ((GlobalTableModelInterface) model).getRenderer(row, columnInModel);
            if (renderer != null) {
                return renderer;
            }
        }

        return super.getCellRenderer(row, column);
    }
    
    @Override
    public ArrayList<ParameterList> getParameters() {
        initParameters(); // must be done each time, because they can be changed in another way
        // than with the parameter dialog

        return m_parameterListArray;
    }
    
    @Override
    public void parametersChanged() {
        
        
        boolean autosize = ((Boolean) m_autoSizeColumnParameter.getObjectValue());

        
        
        if (autosize) {
            setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
            setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
            
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    int width = ((Integer) m_columnWidthParameter.getObjectValue()).intValue();
                    int colCount = getColumnCount();
                    TableColumnModel columnModel = getColumnModel();
                    for (int i = 0; i < colCount; i++) {
                        TableColumn col = columnModel.getColumn(i);
                        col.setWidth(width);
                        col.setPreferredWidth(width);
                    }
                }
                
            });
            

        }
        
        
        List selectedColumns =(List) m_columnsVisibilityParameter.getAssociatedValues(true);
        List nonSelectedColumns =(List) m_columnsVisibilityParameter.getAssociatedValues(false);
        for (Object column : selectedColumns) {
            ((TableColumnExt)column).setVisible(true);
        }
        for (Object column : nonSelectedColumns) {
            ((TableColumnExt)column).setVisible(false);
        }
        
        
        ArrayList<SortKey> sortKeys = new ArrayList<>();
        Integer modelColIndex1 = (Integer) m_sortCol1Parameter.getAssociatedObjectValue();
        SortOrder sortOrder1 = (SortOrder) m_sortOrderCol1Parameter.getAssociatedObjectValue();
        Integer modelColIndex2 = (Integer) m_sortCol2Parameter.getAssociatedObjectValue();
        SortOrder sortOrder2 = (SortOrder) m_sortOrderCol2Parameter.getAssociatedObjectValue();
        if (modelColIndex1 > -1) {
            sortKeys.add(new SortKey(modelColIndex1, sortOrder1));
            if (modelColIndex2 > -1) {
                sortKeys.add(new SortKey(modelColIndex2, sortOrder2));
            }
        }
        
        
        RowSorter<TableModel> sorter = (RowSorter<TableModel>) getRowSorter();
        sorter.setSortKeys(sortKeys);
        ((TableRowSorter)sorter).sort();
    }
    
    public class ColumnGroup implements Comparable<ColumnGroup> {
        public String m_groupName;
        public boolean m_selected;
        
        public ColumnGroup(String groupName, boolean selected) {
            m_groupName = groupName;
            m_selected = selected;
        }

        @Override
        public int compareTo(ColumnGroup o) {
            return m_groupName.compareTo(o.m_groupName);
        }
        

    }
}
