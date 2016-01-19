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
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.progress.ProgressInterface;
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

    private List<Feature> m_features = new ArrayList<>();
    private Map<Integer, IRawFile> m_mapRawFileByFeatureId = new HashMap();
    
    private int m_modelSelectedIdxBeforeSort = -1;

    private FeatureTable m_featureTable;
    private FeaturesTableModel m_featureTableModel;
    private IFeatureViewer m_featureViewer;
    
    private JScrollPane m_jScrollPane;

    private MarkerContainerPanel m_markerContainerPanel;
    // toolbar
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;


    public FeaturesPanel(IFeatureViewer featureViewer) {
        m_featureViewer = featureViewer;
        
        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        m_featureTableModel = new FeaturesTableModel();
        m_jScrollPane = new JScrollPane();
        m_featureTable = new FeatureTable();
        CompoundTableModel compoundTableModel = new CompoundTableModel(m_featureTableModel, true);
        m_featureTable.setModel(compoundTableModel);

        m_featureTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                featureTableMouseClicked(evt);
            }
        });
        m_featureTable.getRowSorter().addRowSorterListener(this);

        m_jScrollPane.setViewportView(m_featureTable);
        m_featureTable.setFillsViewportHeight(true);
        m_featureTable.setViewport(m_jScrollPane.getViewport());

        JToolBar toolbar = initToolbar();

        m_markerContainerPanel = new MarkerContainerPanel(m_jScrollPane, m_featureTable);

        this.add(toolbar, BorderLayout.WEST);
        this.add(m_markerContainerPanel, BorderLayout.CENTER);
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_featureTable.getModel()), m_featureTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_featureTable.getModel())) {
            @Override
            protected void filteringDone() {
                //
            }
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_featureTable.getModel()), "Features-Peakels", m_featureTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

        return toolbar;
    }

    public void addFeatures(List<Feature> features, Map<Integer, IRawFile> mapRawFileByFeatureId) {
        m_featureTableModel.addFeatures(features, mapRawFileByFeatureId);
        this.m_features.addAll(features);
        mapRawFileByFeatureId.entrySet().stream().forEach((entrySet) -> {
            this.m_mapRawFileByFeatureId.put(entrySet.getKey(), entrySet.getValue());
        });
        m_markerContainerPanel.setMaxLineNumber(features.size());
    }
    
    
    public void setFeatures(List<Feature> features, Map<Integer, IRawFile> mapRawFileByFeatureId, boolean displayRawFile) {
        m_modelSelectedIdxBeforeSort = -1;
        m_featureTableModel.setFeatures(features, mapRawFileByFeatureId);
        this.m_features = features;
        this.m_mapRawFileByFeatureId = mapRawFileByFeatureId;
        m_markerContainerPanel.setMaxLineNumber(features.size());
        // hide RawFileName column
        m_featureTable.getColumnExt(m_featureTable.convertColumnIndexToView(FeaturesTableModel.COLTYPE_FEATURE_RAWFILE)).setVisible(displayRawFile);
    }

    private void featureTableMouseClicked(MouseEvent evt) {
        if ((m_features != null) && (!m_features.isEmpty()) && (m_mapRawFileByFeatureId != null)
                && (evt.getClickCount() == 2) && (m_featureTable.getSelectedRow() != -1)) {
            // Retrieve Selected Row
            int selectedRow = m_featureTable.getSelectedRow();
            Feature f = m_features.get(getModelRowId(selectedRow));
            m_featureViewer.displayFeatureInRawFile(new MzdbFeatureWrapper(f), m_mapRawFileByFeatureId.get(f.getId()));
        }
    }
    
    private int getModelRowId(int rowId){
        CompoundTableModel compoundTableModel = (CompoundTableModel) m_featureTable.getModel();
        if (compoundTableModel.getRowCount() != 0) {
            // convert according to the sorting
            rowId = m_featureTable.convertRowIndexToModel(rowId);
            rowId = compoundTableModel.convertCompoundRowToBaseModelRow(rowId);
        }
        return rowId;
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
        if (m_featureTable.getSelectedRow() == -1) {
            return;
        }
        if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
            // Retrieve Selected Row
            m_modelSelectedIdxBeforeSort = m_featureTable.getSelectedRow();
            m_modelSelectedIdxBeforeSort = getModelRowId(m_modelSelectedIdxBeforeSort);
        } else if (m_modelSelectedIdxBeforeSort != -1) {
            int idx =m_modelSelectedIdxBeforeSort;
            idx = getModelRowId(idx);
            m_featureTable.scrollRectToVisible(new Rectangle(m_featureTable.getCellRect(idx, 0, true)));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // selects the row at which point the mouse is clicked
        Point point = e.getPoint();
        int currentRow = m_featureTable.rowAtPoint(point);
        m_featureTable.setRowSelectionInterval(currentRow, currentRow);
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
            Feature f = m_features.get(rowIndex);
            m_featureViewer.displayFeatureInRawFile(new MzdbFeatureWrapper(f), m_mapRawFileByFeatureId.get(f.getId()));
        }

        public void displayInCurrentRawFile(int rowIndex) {
            Feature f = m_features.get(rowIndex);
            m_featureViewer.displayFeatureInCurrentRawFile(new MzdbFeatureWrapper(f));
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

    }
}
