package fr.proline.mzscope.ui;

import fr.proline.mzscope.ui.model.FeaturesTableModel;
import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.mzdb.MzdbFeatureWrapper;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.table.AbstractTableAction;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * panel that contains the features/peaks table
 *
 * @author CB205360
 */
public class FeaturesPanel extends JPanel implements RowSorterListener, MouseListener {

    final private static Logger logger = LoggerFactory.getLogger(FeaturesPanel.class);

    private List<Feature> features = new ArrayList<Feature>();
    private Map<Integer, IRawFile> m_mapRawFileByFeatureId = new HashMap();
    
    private int modelSelectedIdxBeforeSort = -1;

    private FeatureTable featureTable;
    private FeaturesTableModel featureTableModel;
    private IFeatureViewer featureViewer;
    
    private JScrollPane jScrollPane;

    private MarkerContainerPanel m_markerContainerPanel;
    // toolbar
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;


    public FeaturesPanel(IFeatureViewer featureViewer) {
        this.featureViewer = featureViewer;
        
        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        featureTableModel = new FeaturesTableModel();
        jScrollPane = new JScrollPane();
        featureTable = new FeatureTable();
        CompoundTableModel compoundTableModel = new CompoundTableModel(featureTableModel, true);
        featureTable.setModel(compoundTableModel);

        featureTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                featureTableMouseClicked(evt);
            }
        });
        featureTable.getRowSorter().addRowSorterListener(this);

        jScrollPane.setViewportView(featureTable);
        featureTable.setFillsViewportHeight(true);
        featureTable.setViewport(jScrollPane.getViewport());

        JToolBar toolbar = initToolbar();

        m_markerContainerPanel = new MarkerContainerPanel(jScrollPane, featureTable);

        this.add(toolbar, BorderLayout.WEST);
        this.add(m_markerContainerPanel, BorderLayout.CENTER);
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_filterButton = new FilterButton(((CompoundTableModel) featureTable.getModel())) {
            @Override
            protected void filteringDone() {
                //
            }
        };

        m_exportButton = new ExportButton(((CompoundTableModel) featureTable.getModel()), "Features-Peakels", featureTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);

        return toolbar;
    }

    public void addFeatures(List<Feature> features, Map<Integer, IRawFile> mapRawFileByFeatureId) {
        featureTableModel.addFeatures(features, mapRawFileByFeatureId);
        this.features.addAll(features);
        mapRawFileByFeatureId.entrySet().stream().forEach((entrySet) -> {
            this.m_mapRawFileByFeatureId.put(entrySet.getKey(), entrySet.getValue());
        });
        m_markerContainerPanel.setMaxLineNumber(features.size());
    }
    
    
    public void setFeatures(List<Feature> features, Map<Integer, IRawFile> mapRawFileByFeatureId, boolean displayRawFile) {
        modelSelectedIdxBeforeSort = -1;
        featureTableModel.setFeatures(features, mapRawFileByFeatureId);
        this.features = features;
        this.m_mapRawFileByFeatureId = mapRawFileByFeatureId;
        m_markerContainerPanel.setMaxLineNumber(features.size());
        // hide RawFileName column
        featureTable.getColumnExt(featureTable.convertColumnIndexToView(FeaturesTableModel.COLTYPE_FEATURE_RAWFILE)).setVisible(displayRawFile);
    }

    private void featureTableMouseClicked(MouseEvent evt) {
        if ((features != null) && (!features.isEmpty()) && (m_mapRawFileByFeatureId != null)
                && (evt.getClickCount() == 2) && (featureTable.getSelectedRow() != -1)) {
            // Retrieve Selected Row
            int selectedRow = featureTable.getSelectedRow();
            Feature f = features.get(getModelRowId(selectedRow));
            featureViewer.displayFeatureInRawFile(new MzdbFeatureWrapper(f), m_mapRawFileByFeatureId.get(f.getId()));
        }
    }
    
    private int getModelRowId(int rowId){
        CompoundTableModel compoundTableModel = (CompoundTableModel) featureTable.getModel();
        if (compoundTableModel.getRowCount() != 0) {
            // convert according to the sorting
            rowId = featureTable.convertRowIndexToModel(rowId);
            rowId = compoundTableModel.convertCompoundRowToBaseModelRow(rowId);
        }
        return rowId;
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
        if (featureTable.getSelectedRow() == -1) {
            return;
        }
        if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
            // Retrieve Selected Row
            modelSelectedIdxBeforeSort = featureTable.getSelectedRow();
            modelSelectedIdxBeforeSort = getModelRowId(modelSelectedIdxBeforeSort);
        } else if (modelSelectedIdxBeforeSort != -1) {
            int idx =modelSelectedIdxBeforeSort;
            idx = getModelRowId(idx);
            featureTable.scrollRectToVisible(new Rectangle(featureTable.getCellRect(idx, 0, true)));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // selects the row at which point the mouse is clicked
        Point point = e.getPoint();
        int currentRow = featureTable.rowAtPoint(point);
        featureTable.setRowSelectionInterval(currentRow, currentRow);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private static class DisplayRawFileAction extends AbstractTableAction {

        public DisplayRawFileAction() {
            super("Display in the raw file");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
            FeatureTable featureTable = (FeatureTable) table;
            if (featureTable.getSelectedRow() == -1) {
                return;
            }
            // Retrieve Selected Row
                int selectedRow = featureTable.getSelectedRow();
                CompoundTableModel compoundTableModel = (CompoundTableModel) featureTable.getModel();
                if (compoundTableModel.getRowCount() != 0) {
                    // convert according to the sorting
                    selectedRow = featureTable.convertRowIndexToModel(selectedRow);
                    selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);
                }
            featureTable.displayInRawFile(selectedRow);
        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(selectedRows.length == 1);
        }
    }

    private static class DisplayFeatureInCurrentRawFileAction extends AbstractTableAction {

        public DisplayFeatureInCurrentRawFileAction() {
            super("Display in the selected raw file");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
            FeatureTable featureTable = (FeatureTable) table;
            if (featureTable.getSelectedRow() == -1) {
                return;
            }
            int selectedRow = featureTable.getSelectedRow();
                CompoundTableModel compoundTableModel = (CompoundTableModel) featureTable.getModel();
                if (compoundTableModel.getRowCount() != 0) {
                    // convert according to the sorting
                    selectedRow = featureTable.convertRowIndexToModel(selectedRow);
                    selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);
                }
            featureTable.displayInCurrentRawFile(selectedRow);
        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(selectedRows.length == 1);
        }
    }

    private class FeatureTable extends DecoratedMarkerTable {

        @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();

            popupMenu.addAction(new DisplayRawFileAction());
            popupMenu.addAction(new DisplayFeatureInCurrentRawFileAction());
            popupMenu.addAction(null);

            popupMenu.addAction(new RestrainAction() {
                @Override
                public void filteringDone() {
                    //
                }
            });
            popupMenu.addAction(new ClearRestrainAction() {
                @Override
                public void filteringDone() {
                    //
                }
            });

            return popupMenu;
        }

        @Override
        public void prepostPopupMenu() {
            //
        }


        public void displayInRawFile(int rowIndex) {
            Feature f = features.get(rowIndex);
            featureViewer.displayFeatureInRawFile(new MzdbFeatureWrapper(f), m_mapRawFileByFeatureId.get(f.getId()));
        }

        public void displayInCurrentRawFile(int rowIndex) {
            Feature f = features.get(rowIndex);
            featureViewer.displayFeatureInCurrentRawFile(new MzdbFeatureWrapper(f));
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

    }
}
