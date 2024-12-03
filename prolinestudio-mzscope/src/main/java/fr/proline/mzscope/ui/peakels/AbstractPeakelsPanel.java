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
package fr.proline.mzscope.ui.peakels;

import fr.profi.mzdb.model.Peakel;
import fr.proline.mzscope.model.BaseFeature;
import fr.proline.mzscope.model.IPeakel;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.ui.IMzScopeController;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ImportedDataTableModel;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.coordinates.PixelCoordinates;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.table.AbstractTableAction;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Panel presenting a list of peakels in a table
 *
 * @author CB205360
 */
public abstract class AbstractPeakelsPanel extends JPanel  {

  final private static Logger logger = LoggerFactory.getLogger(AbstractPeakelsPanel.class);
  private final static String LAST_DIR = "mzscope.last.csv.features.directory";

  private JFileChooser m_fchooser;
  protected int m_modelSelectedRowBeforeSort = -1;

  protected DecoratedMarkerTable m_table;
  protected CompoundTableModel m_compoundTableModel;

  protected IMzScopeController m_viewersController;
  protected BasePlotPanel m_graphPlot;
  protected MarkerContainerPanel m_markerContainerPanel;

  public AbstractPeakelsPanel(IMzScopeController controller) {
    this.m_viewersController = controller;
    m_fchooser = new JFileChooser();
    m_fchooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.getName().endsWith(".csv");
      }

      @Override
      public String getDescription() {
        return "*.csv";
      }
    });
    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BorderLayout());

    JScrollPane jScrollPane = new JScrollPane();
    m_table = new FeatureTable();
    m_compoundTableModel = buildTableModel();
    m_table.setModel(m_compoundTableModel);

    m_table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent evt) {
        tableMouseClicked(evt);
      }
    });

    jScrollPane.setViewportView(m_table);
    m_table.setFillsViewportHeight(true);
    m_table.setViewport(jScrollPane.getViewport());
    m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updatePeakelsViewer(getSelectedPeakels());
      }
    });

    JToolBar toolbar = initToolbar();

    m_markerContainerPanel = new MarkerContainerPanel(jScrollPane, m_table);

    tablePanel.add(toolbar, BorderLayout.WEST);
    tablePanel.add(m_markerContainerPanel, BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane();
    splitPane.setLeftComponent(tablePanel);
    m_graphPlot = new BasePlotPanel();
    splitPane.setRightComponent(m_graphPlot);

    this.add(splitPane, BorderLayout.CENTER);

  }


  abstract protected CompoundTableModel buildTableModel();

  protected JToolBar initToolbar() {
    JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    toolbar.setFloatable(false);

    FilterButton filterButton = new FilterButton(((CompoundTableModel) m_table.getModel())) {
      @Override
      protected void filteringDone() {
        //
      }
    };

    ExportButton exportButton = new ExportButton(((CompoundTableModel) m_table.getModel()), "Features-Peakels", m_table);
    toolbar.add(filterButton);
    toolbar.add(exportButton);

    return toolbar;
  }

  protected void tableMouseClicked(MouseEvent evt) {
    List<IPeakel> peakels = getSelectedIPeakels();
    updatePeakelsViewer(getSelectedPeakels());
    if (evt.getClickCount() == 2 && peakels != null && peakels.size() > 0) {
      m_viewersController.getRawFileViewer(peakels.get(0).getRawFile(), true).displayPeakel(peakels.get(0));
    }
  }

  abstract protected List<IPeakel> getSelectedIPeakels();

  abstract protected List<Peakel> getSelectedPeakels();

  private void updatePeakelsViewer(List<Peakel> peakels) {
    if (peakels != null) {
      m_graphPlot.clearPlots();
      float maxY = 0.0f;
      int index = 0;
      for (Peakel p : peakels) {
          maxY = Math.max(p.getApexIntensity(), maxY);
          PeakelWrapper wrapper = new PeakelWrapper(p, index++);
          PlotLinear plot = new PlotLinear(m_graphPlot, wrapper, null, 0, 1);
          plot.setPlotInformation(wrapper.getPlotInformation());
          m_graphPlot.addPlot(plot);
      }

      m_graphPlot.getPlots().get(0).clearMarkers();

      //for 2 peakels, compute and display correlation coefficient
      if (peakels.size() == 2) {
        double correlation = SpectrumUtils.correlation(peakels.get(0), peakels.get(1));
        double ompCorrelation = SpectrumUtils.correlationOMP(peakels.get(0), peakels.get(1), false);
        double ompSmoothedCorrelation = SpectrumUtils.correlationOMP(peakels.get(0), peakels.get(1), true);

        LabelMarker marker = new LabelMarker(m_graphPlot, new PixelCoordinates(10, 60), MessageFormat.format("Corr: {0}, OMP: {1}, smooth OMP: {2}", correlation, ompCorrelation, ompSmoothedCorrelation));
        m_graphPlot.getPlots().get(0).addMarker(marker);
      }

      m_graphPlot.getYAxis().lockMinValue(0.0);
      m_graphPlot.getYAxis().setRange(0, maxY);
      m_graphPlot.repaint();
    }
  }

  protected int getModelRowId(int rowId) {
    CompoundTableModel compoundTableModel = (CompoundTableModel) m_table.getModel();
    if (compoundTableModel.getRowCount() != 0) {
      // convert according to the sorting
      rowId = m_table.convertRowIndexToModel(rowId);
      rowId = compoundTableModel.convertCompoundRowToBaseModelRow(rowId);
    }
    return rowId;
  }


  void matchCSVIons() {
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    String directory = prefs.get(LAST_DIR, m_fchooser.getCurrentDirectory().getAbsolutePath());
    m_fchooser.setCurrentDirectory(new File(directory));
    int result = m_fchooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File csvFile = m_fchooser.getSelectedFile();
      String fileName = csvFile.getName();
      if (!fileName.endsWith(".csv")) {
        JOptionPane.showMessageDialog(this, "The file must be a csv file", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      prefs.put(LAST_DIR, csvFile.getParentFile().getAbsolutePath());
      ImportedDataTableModel importedTableModel = new ImportedDataTableModel();
      Exception csvException  = ImportedDataTableModel.loadFile(importedTableModel, csvFile.getAbsolutePath(), ';', true, false);
      if (csvException == null) {
        int mzColumnIdx = findColumn(importedTableModel, new String[]{"moz", "m/z", "mz", "exp. moz"});
        int rtColumnIdx = findColumn(importedTableModel, new String[]{"rt", "retention_time", "retention time", "elution_time", "elution time", "time"});
        int zColumnIdx = findColumn(importedTableModel, new String[]{"charge", "z"});
        int iColumnIdx = findColumn(importedTableModel, new String[]{"Abundance"});

        if (mzColumnIdx != -1) {
          List<BaseFeature> ions = new ArrayList<>(importedTableModel.getRowCount());
          for (int k = 0; k < importedTableModel.getRowCount(); k++) {
            double mz =  ((Double) importedTableModel.getValueAt(k, mzColumnIdx));
            float rt = ((rtColumnIdx != -1) ? ((Double) importedTableModel.getValueAt(k, rtColumnIdx)).floatValue() : -1.0f);
            int z = ((zColumnIdx != -1) ? ((Long) importedTableModel.getValueAt(k, zColumnIdx)).intValue() : 0);
            float i = ((iColumnIdx != -1)) ? ((Double) importedTableModel.getValueAt(k, iColumnIdx)).floatValue() : -1.0f;

            BaseFeature ion = new BaseFeature(mz, rt, rt, rt, null, 1);
            ion.setCharge(z);
            ion.setApexIntensity(i);
            ions.add(ion);
          }
          logger.info("Matching features from {}", csvFile.getName());
          matchIons(ions, importedTableModel);
        } else {
          StringBuffer columnNamesBuffer = new StringBuffer("[");
          for (int i = 0; i < importedTableModel.getColumnCount(); i++) {
            columnNamesBuffer.append(importedTableModel.getColumnName(i)).append(";");
          }
          columnNamesBuffer.deleteCharAt(columnNamesBuffer.length()-1);
          columnNamesBuffer.append("]");

          String message = "No column named \"mz\",\"moz\" or \"m/z\" detected in the imported file.\n Verify the column headers (the column separator must be \";\") \n" +
                  columnNamesBuffer.toString();
          JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
      } else {

        JOptionPane.showMessageDialog(this, "Error while redading CSV file : "+csvException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  protected abstract void matchIons(List<BaseFeature> ions, ImportedDataTableModel importedTableModel);

  protected String getRowAsString(int row, ImportedDataTableModel tableModel) {
    StringBuilder stb = new StringBuilder();
    for (int k = 0; k < tableModel.getColumnCount(); k++) {
      stb.append(tableModel.getValueAt(row, k)).append(", ");
    }
    return stb.toString();
  }


  protected int findColumn(AbstractTableModel tableModel, String[] alternativeNames) {
    int columnIdx = -1;
    for (String name : alternativeNames) {
      for (int i = 0; i < tableModel.getColumnCount(); i++) {
        if (name.trim().equalsIgnoreCase(tableModel.getColumnName(i).trim())) {
          columnIdx = i;
        }
      }
      if (columnIdx != -1) {
        break;
      }
    }
    return columnIdx;
  }

  protected class DisplayRawFileAction extends AbstractTableAction {

    public DisplayRawFileAction() {
      super("Display in the raw file");
    }

    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
      List<IPeakel> peakels = getSelectedIPeakels();
      if(peakels != null && peakels.size() > 0) {
        m_viewersController.getRawFileViewer(peakels.get(0).getRawFile(), true).displayPeakel(peakels.get(0));
      }
    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
      setEnabled(selectedRows.length == 1);
    }
  }

  protected class DisplayFeatureInCurrentRawFileAction extends AbstractTableAction {

    public DisplayFeatureInCurrentRawFileAction() {
      super("Display in the currently active raw file");
    }

    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
      List<IPeakel> peakels = getSelectedIPeakels();
      if(peakels != null && peakels.size() > 0) {
        m_viewersController.getCurrentRawFileViewer().displayPeakel(peakels.get(0));
      }
    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
      setEnabled(selectedRows.length == 1);
    }
  }

  protected class FeatureTable extends DecoratedMarkerTable {

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

    @Override
    public void addTableModelListener(TableModelListener l) {
      getModel().addTableModelListener(l);
    }

  }
}

