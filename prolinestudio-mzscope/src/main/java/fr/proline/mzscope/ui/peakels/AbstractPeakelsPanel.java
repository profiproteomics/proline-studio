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
package fr.proline.mzscope.ui.peakels;

import fr.profi.mzdb.model.Peakel;
import fr.proline.mzscope.model.IPeakel;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.table.AbstractTableAction;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel presenting a list of peakels in a table
 *
 * @author CB205360
 */
public abstract class AbstractPeakelsPanel extends JPanel  {

  final private static Logger logger = LoggerFactory.getLogger(AbstractPeakelsPanel.class);

  protected int m_modelSelectedRowBeforeSort = -1;

  protected DecoratedMarkerTable m_table;
  protected CompoundTableModel m_compoundTableModel;

  protected IPeakelViewer m_peakelViewer;
  protected BasePlotPanel m_graphPlot;
  protected MarkerContainerPanel m_markerContainerPanel;

  public AbstractPeakelsPanel(IPeakelViewer peakelViewer) {
    this.m_peakelViewer = peakelViewer;
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
      m_peakelViewer.displayPeakelInRawFile(peakels.get(0));
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
          PlotLinear plot = new PlotLinear(m_graphPlot, new PeakelWrapper(p, index++), null, 0, 1);
          m_graphPlot.addPlot(plot);
      }
      m_graphPlot.getYAxis().setRange(0, maxY);
      m_graphPlot.lockMinYValue();
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

  private class DisplayRawFileAction extends AbstractTableAction {

    public DisplayRawFileAction() {
      super("Display in the raw file");
    }

    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
      List<IPeakel> peakels = getSelectedIPeakels();
      if(peakels != null && peakels.size() > 0) {
        m_peakelViewer.displayPeakelInRawFile(peakels.get(0));
      }
    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
      setEnabled(selectedRows.length == 1);
    }
  }

  private class DisplayFeatureInCurrentRawFileAction extends AbstractTableAction {

    public DisplayFeatureInCurrentRawFileAction() {
      super("Display in the currently active raw file");
    }

    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
      List<IPeakel> peakels = getSelectedIPeakels();
      if(peakels != null && peakels.size() > 0) {
        m_peakelViewer.displayPeakelInCurrentRawFile(peakels.get(0));
      }
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

    @Override
    public void addTableModelListener(TableModelListener l) {
      getModel().addTableModelListener(l);
    }

  }
}

