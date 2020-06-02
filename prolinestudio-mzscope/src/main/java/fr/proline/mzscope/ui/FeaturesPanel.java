/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.mzscope.ui;

import fr.profi.mzdb.model.Peakel;
import fr.proline.mzscope.ui.model.FeaturesTableModel;
import fr.proline.mzscope.model.IFeature;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.table.AbstractTableAction;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.DataFormat;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

    private List<IFeature> features = new ArrayList<IFeature>();
    private int modelSelectedIdxBeforeSort = -1;

    private FeatureTable featureTable;
    private FeaturesTableModel featureTableModel;
    private IFeatureViewer featureViewer;
    private BasePlotPanel graphPlot;
    private JScrollPane jScrollPane;
    private MarkerContainerPanel m_markerContainerPanel;
    
    // toolbar buttons
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;


    public FeaturesPanel(IFeatureViewer featureViewer) {
        this.featureViewer = featureViewer;
        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        
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
        
        featureTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                if (e.getValueIsAdjusting()) {
                    // value is adjusting, so valueChanged will be called again
                    return;
                }
                
                updateFeatureViewer();
            }
        });
                
        featureTable.getRowSorter().addRowSorterListener(this);

        jScrollPane.setViewportView(featureTable);
        featureTable.setFillsViewportHeight(true);
        featureTable.setViewport(jScrollPane.getViewport());

        JToolBar toolbar = initToolbar();

        m_markerContainerPanel = new MarkerContainerPanel(jScrollPane, featureTable);

        tablePanel.add(toolbar, BorderLayout.WEST);
        tablePanel.add(m_markerContainerPanel, BorderLayout.CENTER);
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(tablePanel);
        graphPlot = new BasePlotPanel();
        splitPane.setRightComponent(graphPlot);
        
        this.add(splitPane, BorderLayout.CENTER);
        
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

    public void addFeatures(List<IFeature> features) {
        featureTableModel.addFeatures(features);
        this.features.addAll(features);
        m_markerContainerPanel.setMaxLineNumber(features.size());
    }
    
    
    public void setFeatures(List<IFeature> features, boolean displayRawFile) {
        modelSelectedIdxBeforeSort = -1;
        featureTableModel.setFeatures(features);
        this.features = features;
        m_markerContainerPanel.setMaxLineNumber(features.size());
        // hide RawFileName column
        featureTable.getColumnExt(featureTable.convertColumnIndexToView(FeaturesTableModel.COLTYPE_FEATURE_RAWFILE)).setVisible(displayRawFile);
    }

    private void featureTableMouseClicked(MouseEvent evt) {
        IFeature f = updateFeatureViewer();
        if (evt.getClickCount() == 2 ) {
                featureViewer.displayFeatureInRawFile(f);
        } 

    }
    
    private IFeature updateFeatureViewer() {
        IFeature f = null;
        if ((features != null) && (!features.isEmpty()) && (featureTable.getSelectedRow() != -1)) {
            // Retrieve Selected Row
            int selectedRow = featureTable.getSelectedRow();
            f = features.get(getModelRowId(selectedRow));
            graphPlot.clearPlots();
            float maxY = 0.0f;
            int index = 0;
            for (Peakel p : f.getPeakels()) {
                maxY = Math.max(p.getApexIntensity(), maxY);
                PlotLinear plot = new PlotLinear(graphPlot, new PeakelWrapper(p, index++), null, 0, 1);
                graphPlot.addPlot(plot, true);
            }
            graphPlot.getYAxis().setRange(0, maxY);
            graphPlot.lockMinYValue();
            graphPlot.repaint();
        }
        return f;
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
            IFeature f = features.get(rowIndex);
            featureViewer.displayFeatureInRawFile(f);
        }

        public void displayInCurrentRawFile(int rowIndex) {
            IFeature f = features.get(rowIndex);
            featureViewer.displayFeatureInCurrentRawFile(f);
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

    }
}

class PeakelWrapper implements ExtendedTableModelInterface {

    Peakel peakel;
    int isotopeIndex;
    
    public PeakelWrapper(Peakel p, int i) {
        peakel = p;
        isotopeIndex = i;
    }
    
     @Override
   public int getRowCount() {
      return peakel.getElutionTimes().length;
   }

   @Override
   public int getColumnCount() {
      return 2;
   }

   @Override
   public String getDataColumnIdentifier(int columnIndex) {
      return (columnIndex == 0) ? "rt" : "intensity";
   }

   @Override
   public Class getDataColumnClass(int columnIndex) {
      return Float.class;
   }

   @Override
   public Object getDataValueAt(int rowIndex, int columnIndex) {
      return (columnIndex == 0) ? peakel.getElutionTimes()[rowIndex]/60.0f : peakel.getIntensityValues()[rowIndex];
   }

   @Override
   public int[] getKeysColumn() {
      return new int[]{0};
   }

   @Override
   public int getInfoColumn() {
      return 0;
   }

   @Override
   public void setName(String name) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public String getName() {
      return "Elution peak";
   }

   @Override
   public Map<String, Object> getExternalData() {
      return null;
   }

   @Override
   public PlotInformation getPlotInformation() {
        PlotInformation plotInformation = new PlotInformation();
        plotInformation.setPlotColor(CyclicColorPalette.getColor(1));
        plotInformation.setPlotTitle("Elution peak");
        plotInformation.setDrawPoints(false);
        plotInformation.setDrawGap(true);
        HashMap<String, String> plotInfo = new HashMap();
        plotInfo.put("m/z", Double.toString(peakel.getApexMz()));
        plotInfo.put("Apex Int.", DataFormat.formatWithGroupingSep(peakel.getApexIntensity(), 0));
        plotInfo.put("Isotope index", Integer.toString(isotopeIndex));

        plotInformation.setPlotInfo(plotInfo);
        return plotInformation;
   }

    @Override
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }
    
    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addSingleValue(Object v) {
        
    }

    @Override
    public Object getSingleValue(Class c) {
        return null;
    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }
    
}
