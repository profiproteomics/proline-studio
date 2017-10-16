package fr.proline.studio.table;

import com.thierry.filtering.TableSelection;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.AdvancedSelectionPanel;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.AbstractParameter.LabelVisibility;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.SettingsInterface;
import fr.proline.studio.utils.RelativePainterHighlighter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultRowSorter;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbPreferences;
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

    private ObjectParameter m_columnArrangementParameter = null;
    private IntegerParameter m_columnWidthParameter = null;

    private MultiObjectParameter m_columnsVisibilityParameter = null;
    private ArrayList<ParameterList> m_parameterListArray = null;

    private ObjectParameter m_sortCol1Parameter = null;
    private ObjectParameter m_sortOrderCol1Parameter = null;
    private ObjectParameter m_sortCol2Parameter = null;
    private ObjectParameter m_sortOrderCol2Parameter = null;

    private static final String COLUMNS_VISIBILITY_KEY = "COLUMNS_VISIBILITY_KEY";

    public static final String DEFAULT_COLUMNS_ARRANGEMENT_KEY = "Columns_Arrangement";
    public static final String DEFAULT_COLUMNS_ARRANGEMENT_NAME = "Columns Arrangement";
    
    public static final int DEFAULT_COLUMNS_ARRANGEMENT_INDEX = 2;
    
    public static final String DEFAULT_WIDTH_KEY = "Column_Width";
    public static final String DEFAULT_WIDTH_NAME = "Column Width";
    public static final int COLUMN_DEFAULT_WIDTH = 120;
    public static final int COLUMN_MIN_WIDTH = 20;
    public static final int COLUMN_MAX_WIDTH = 300;
    public static final int AUTOMATIC_COLUMNS_SIZE = 0;
    public static final int FIXED_COLUMNS_SIZE = 1;
    public static final int SMART_COLUMNS_SIZE = 2;
    public static final String TABLE_PARAMETERS = "Table Parameters";


    private boolean m_firstPaint = true;

    private boolean m_alreadyLoaded = false;
    private int m_width = -1;
    private int m_selection = -1;
    private int m_meanWidth = -1;
    private int m_totalWidth = 0;


    private int m_nbVisible = 0;

    private JComboBox m_arrangementComboBox;
    private JTextField m_widthTextField;


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

    
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // JPM.HACK : to take in account parameters for table autosize/fixed size
        // -------------------------
        int width = getParent().getWidth();
        if (Math.abs(m_totalWidth-width) > 50) {
            m_firstPaint = true;
            m_totalWidth = width;
        }
        
        if (m_firstPaint) {
            m_firstPaint = false;
            panelResized();  
        }
        // -------------------------

    }
    

    public void reinitParameters() {
        m_firstPaint = true;
        m_parameterListArray = null;
    }

    
    @Override
    public void columnAdded(TableColumnModelEvent e) {
        super.columnAdded(e);
        reinitParameters();
    }
    
    private void initParameters() {

        m_alreadyLoaded = false;
        m_width = -1;
        m_selection = -1;

        ParameterList parameterTableList = new ParameterList(TABLE_PARAMETERS);
        Object[] associatedTable = {"Automatic Column Size", "Fixed Column Size", "Smart Column Size"};
        m_arrangementComboBox = new JComboBox(associatedTable);
        Object[] objectTable = {DecoratedTable.AUTOMATIC_COLUMNS_SIZE, DecoratedTable.FIXED_COLUMNS_SIZE, DecoratedTable.SMART_COLUMNS_SIZE};
        m_columnArrangementParameter = new ObjectParameter(DecoratedTable.DEFAULT_COLUMNS_ARRANGEMENT_KEY, DecoratedTable.DEFAULT_COLUMNS_ARRANGEMENT_NAME, m_arrangementComboBox, associatedTable, objectTable, DecoratedTable.DEFAULT_COLUMNS_ARRANGEMENT_INDEX, null);
        

        List<TableColumn> columns = getColumns(true);
        int colCount = columns.size();
        Object[] columnNamesArray = new Object[colCount];
        Object[] columnsArray = new Object[colCount];
        boolean[] selection = new boolean[colCount];

        m_nbVisible = 0;
        for (int i = 0; i < colCount; i++) {
            TableColumnExt column = (TableColumnExt) columns.get(i);
            columnNamesArray[i] = column.getHeaderValue().toString().replaceAll("<br/>", " ");
            columnsArray[i] = column;

            boolean visible = column.isVisible();

            selection[i] = visible;
            if (visible) {
                m_nbVisible++;
            }
        }

        calculateMeanWidth();

        m_widthTextField = new JTextField();
        m_columnWidthParameter = new IntegerParameter(DEFAULT_WIDTH_KEY, DEFAULT_WIDTH_NAME, m_widthTextField, COLUMN_DEFAULT_WIDTH, 10, COLUMN_MAX_WIDTH);

        m_columnsVisibilityParameter = new MultiObjectParameter(COLUMNS_VISIBILITY_KEY, "Columns Visibility", "Visible Columns", "Hidden Columns", AdvancedSelectionPanel.class, columnNamesArray, columnsArray, selection, null);


        parameterTableList.add(m_columnArrangementParameter);
        parameterTableList.add(m_columnWidthParameter);

        parameterTableList.loadParameters(NbPreferences.root());

        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterTableList) {

            @Override
            public void valueChanged(String value, Object associatedValue) {

                if (!m_alreadyLoaded) {
                    parameterTableList.loadParameters(NbPreferences.root());
                    m_alreadyLoaded = true;
                    m_width = Integer.parseInt(m_columnWidthParameter.getStringValue());
                    m_selection = Integer.parseInt(m_columnArrangementParameter.getStringValue());
                }

                m_selection = Integer.parseInt(m_columnArrangementParameter.getStringValue());

                showParameter(m_columnWidthParameter, m_selection == DecoratedTable.FIXED_COLUMNS_SIZE || m_selection == DecoratedTable.SMART_COLUMNS_SIZE, m_width);
                updateParameterListPanel();

            }

        };

        parameterTableList.add(m_columnsVisibilityParameter);

        m_parameterListArray = new ArrayList<>();
        m_parameterListArray.add(parameterTableList);

        parameterTableList.getPanel(); // generate panel at once
        m_columnArrangementParameter.addLinkedParameters(linkedParameters);
        linkedParameters.valueChanged("true", Boolean.TRUE);  //JPM.TOOD true or false

        addSortingParameters();
    }

    private void calculateMeanWidth() {
        int totalWidth = m_totalWidth;
        m_meanWidth = (m_nbVisible != 0) ? totalWidth / m_nbVisible : 30;
        if (m_meanWidth < 30) {
            m_meanWidth = 30;
        } else if (m_meanWidth > COLUMN_MAX_WIDTH) {
            m_meanWidth = COLUMN_MAX_WIDTH;
        }
    }

    private void addSortingParameters() {

        TableModel model = getModel();

        List<TableColumn> colums = getColumns(true);
        int nbCols = colums.size();

        String[] colNames = new String[nbCols + 1];
        Integer[] indexes = new Integer[nbCols + 1];
        colNames[0] = "< No Sorting >";
        indexes[0] = -1;
        for (int i = 0; i < nbCols; i++) {
            indexes[i + 1] = colums.get(i).getModelIndex();
            colNames[i + 1] = model.getColumnName(indexes[i + 1]);
        }

        RowSorter<TableModel> sorter = (RowSorter<TableModel>) getRowSorter();

        List<SortKey> sortKeys = (List<SortKey>) sorter.getSortKeys();
        int numberOfSortingKeys = Math.min(sortKeys.size(), 2);
        int[] colIndex = new int[2];
        SortOrder[] sorterOrder = new SortOrder[2];

        for (int i = 0; i < numberOfSortingKeys; i++) {
            SortKey key = sortKeys.get(i);
            colIndex[i] = key.getColumn() + 1;
            sorterOrder[i] = key.getSortOrder();
        }
        for (int i = numberOfSortingKeys; i < 2; i++) {
            colIndex[i] = 0;
            sorterOrder[i] = SortOrder.ASCENDING;
        }

        String[] orderString = {"Ascending", "Descending"};
        SortOrder[] orderEnum = {SortOrder.ASCENDING, SortOrder.DESCENDING};

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

        parameterTableList.getPanel(); // generate panel at once

        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterTableList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                boolean show = ((Integer) associatedValue) != -1;
                showParameter(m_sortOrderCol1Parameter, show);

                if (!show) {
                    showParameter(m_sortCol2Parameter, false);
                    showParameter(m_sortOrderCol2Parameter, false);
                } else {
                    showParameter(m_sortCol2Parameter, true);
                    showParameter(m_sortOrderCol2Parameter, ((Integer) m_sortCol2Parameter.getAssociatedObjectValue()) != -1);
                }

                updateParameterListPanel();
            }

        };
        m_sortCol1Parameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel

        AbstractLinkedParameters linkedParameters2 = new AbstractLinkedParameters(parameterTableList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_sortOrderCol2Parameter, ((Integer) associatedValue) != -1);

                updateParameterListPanel();
            }

        };
        m_sortCol2Parameter.addLinkedParameters(linkedParameters2); // link parameter, it will modify the panel

        linkedParameters.valueChanged(colNames[colIndex[0]], indexes[colIndex[0]]);
        linkedParameters2.valueChanged(colNames[colIndex[1]], indexes[colIndex[1]]);

        m_parameterListArray.add(parameterTableList);
    }

    private void fixResize() {
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
        if (m_parameterListArray == null) {
            initParameters();
        }

        return m_parameterListArray;
    }

    private void panelResized() {
        if (m_parameterListArray == null) {
            initParameters();
        }

        int arrangement = Integer.parseInt(m_columnArrangementParameter.getStringValue());

        switch (arrangement) {
            case DecoratedTable.AUTOMATIC_COLUMNS_SIZE:
                setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
                break;
            case DecoratedTable.FIXED_COLUMNS_SIZE:
                fixResize();
                break;
            case DecoratedTable.SMART_COLUMNS_SIZE:

                calculateMeanWidth();

                if (m_meanWidth == -1) {
                    setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
                    break;
                }

                float width = ((Integer) m_columnWidthParameter.getObjectValue()).floatValue();
                float meanWidth = (float) m_meanWidth;

                if (meanWidth / width < 0.9) {
                    fixResize();
                    break;

                } else {
                    setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
                    break;
                }

        }
    }
    
    
    @Override
    public void parametersChanged() {

        panelResized();

        List selectedColumns = (List) m_columnsVisibilityParameter.getAssociatedValues(true);
        List nonSelectedColumns = (List) m_columnsVisibilityParameter.getAssociatedValues(false);
        for (Object column : selectedColumns) {
            ((TableColumnExt) column).setVisible(true);
        }
        for (Object column : nonSelectedColumns) {
            ((TableColumnExt) column).setVisible(false);
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

        DefaultRowSorter sorter = (DefaultRowSorter) getRowSorter();
        sorter.setSortKeys(sortKeys);
        sorter.sort();

    }
    
    @Override
    public boolean parametersCanceled() {
        return false; // nothing has changed due to the parameter cancellation
    }

}
