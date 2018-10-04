/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.ui.model.MapTableModel;
import fr.proline.mzscope.model.QCMetrics;
import fr.proline.mzscope.ui.model.ChromatogramTableModel;
import fr.proline.mzscope.ui.model.StatisticsTableModel;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.BaseGraphicsPanel;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class QCMetricsPanel extends JPanel implements CrossSelectionInterface {

    private final static Logger logger = LoggerFactory.getLogger(QCMetrics.class);
    private final static String SOURCE = "Source";
    private List<QCMetrics> m_metrics;
    private List<String> m_metricNames;
    private List<Map<String,Object>> m_metricsSummaries;
    
    private final CompoundTableModel m_tableModel = new CompoundTableModel(new MapTableModel(), true);
    private DecoratedMarkerTable m_table;
    private BaseGraphicsPanel m_graphicsPanel;
    private JTabbedPane m_tabbedPanel;
    private JToolBar m_toolbar;
    private MetricsDetailsPanel m_detailsPanel;
    
    
    public QCMetricsPanel(List<QCMetrics> metrics) {
        initComponents();
        addMetrics(metrics, "raw file");
    }

    public void addMetrics(List<QCMetrics> metrics, String label) {
        if (m_metrics == null) {
            m_metrics = new ArrayList<>();
            m_metricNames = new ArrayList<>();
            m_metricsSummaries = new ArrayList<>();
        }
        
        Set<String> existingMetrics = m_metrics.stream().map(m -> m.getName()).collect(Collectors.toSet()); 
        for (QCMetrics m : metrics) {
            if (!existingMetrics.contains(m.getName())) {
                m_metrics.add(m);
                Map<String, Object> summary = m.asSummary();
                summary.put(SOURCE, label);
                m_metricsSummaries.add(summary);
                m_metricNames.add(m.getName());
                
            }
        }
        
        ((MapTableModel)m_tableModel.getBaseModel()).setData(m_metricsSummaries, m_metricNames);
        m_graphicsPanel.setData(m_tableModel, this);
    }
    
    private void initComponents() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        m_toolbar = new JToolBar();
        m_toolbar.setFloatable(false);
        m_toolbar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        m_toolbar.setRollover(true);
        JScrollPane tableScrollPane = new JScrollPane();
        m_table = new DecoratedMarkerTable() {

            @Override
            public TablePopupMenu initPopupMenu() {
                return new TablePopupMenu();
            }

            @Override
            public void prepostPopupMenu() {
            }

            @Override
            public void addTableModelListener(TableModelListener l) {
                getModel().addTableModelListener(l);
            }
        };
        
        m_table.setModel(m_tableModel);
        tableScrollPane.setViewportView(m_table);
        m_table.setFillsViewportHeight(true);
        m_table.setViewport(tableScrollPane.getViewport());
        m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = m_table.getSelectedRow();
                        if (selectedRow >= 0) {
                            selectedRow = m_table.convertRowIndexToModel(selectedRow);
                            selectedRow = m_tableModel.convertCompoundRowToBaseModelRow(selectedRow);
                            m_detailsPanel.setQCMetrics(m_metrics.get(selectedRow));
                        }
                    
                }
            }
        });
        ExportButton exportButton = new ExportButton(m_tableModel, "Export", m_table);
        m_toolbar.add(exportButton);
        FilterButton filterButton = new FilterButton(m_tableModel) {

            @Override
            protected void filteringDone() {

            }

        };

        m_toolbar.add(filterButton);

        MarkerContainerPanel markerContainerPanel = new MarkerContainerPanel(tableScrollPane, m_table);
        JPanel tablePane = new JPanel();
        tablePane.setLayout(new BorderLayout());
        tablePane.add(m_toolbar, BorderLayout.WEST);
        tablePane.add(markerContainerPanel, BorderLayout.CENTER);

        m_tabbedPanel = new JTabbedPane();
        
        m_graphicsPanel = new BaseGraphicsPanel(false);
        m_tabbedPanel.add("All Metrics", m_graphicsPanel);
        
        m_detailsPanel = new MetricsDetailsPanel();
        m_tabbedPanel.add("Metrics details", m_detailsPanel);
        
        split.setLeftComponent(tablePane);
        split.setRightComponent(m_tabbedPanel);
        
        this.setLayout(new BorderLayout());
        this.add(split, BorderLayout.CENTER);
    }
    

    public JToolBar getToolBar() {
        return m_toolbar; 
    }
    
    @Override
    public void select(ArrayList<Long> uniqueIds) {
        m_table.select(uniqueIds);
    }

    @Override
    public ArrayList<Long> getSelection() {
        logger.debug("Get selection requested ! ");
        return m_table.getSelection();
    }

}

class MetricsDetailsPanel extends JPanel {

    private final static Logger logger = LoggerFactory.getLogger(MetricsDetailsPanel.class);
    
    private JComboBox<String> m_metricsCbx;
    private JComboBox<Integer> m_msLevelCbx;
    
    private QCMetrics m_metrics;
    private BasePlotPanel m_plotPanel;
    
    public MetricsDetailsPanel() {
        this.setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        m_metricsCbx = new JComboBox<>();
        m_metricsCbx.setPrototypeDisplayValue("XXXXXXXXXXXXXXXX");
        m_metricsCbx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCurrentMetric();
            }
        }) ;
        toolbar.setLayout(new FlowLayout(FlowLayout.LEADING,5,5));
        toolbar.add(m_metricsCbx);

        m_msLevelCbx = new JComboBox<>();
        m_msLevelCbx.addItem(1);
        m_msLevelCbx.addItem(2);
        
        m_msLevelCbx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCurrentMetric();
            }
        }) ;
        toolbar.add(m_msLevelCbx);        
        
        toolbar.add(Box.createHorizontalGlue());
        this.add(toolbar, BorderLayout.NORTH);
        PlotPanel panel = new PlotPanel();
        m_plotPanel = panel.getBasePlotPanel();
        this.add(panel, BorderLayout.CENTER);
    }
    
    public void updateCurrentMetric() {
     String metricName = (String)m_metricsCbx.getSelectedItem();
     Integer msLevel = (Integer)m_msLevelCbx.getSelectedItem();
     if (metricName == null) {
         m_plotPanel.clearPlots();
         m_plotPanel.repaintUpdateDoubleBuffer();
         return;
     } 
      DescriptiveStatistics[] stats = m_metrics.getMetricStatistics(metricName);
      if (stats == null) {
          logger.error("no stats found for metric "+metricName);
          m_plotPanel.clearPlots();
          m_plotPanel.repaintUpdateDoubleBuffer();
          return;
      }
      logger.info("set visible metric to "+metricName);
      double[] time;
      DescriptiveStatistics statistics;
      if (stats[0] != null) {
          time = m_metrics.getMetricStatistics("RT events ")[0].getValues();
          statistics = stats[0];
          m_msLevelCbx.setEnabled(false);
      }  else {
          time = m_metrics.getMetricStatistics("RT MS events ")[msLevel].getValues();
          statistics = stats[msLevel];  
          m_msLevelCbx.setEnabled(true);
      }
      logger.info("Statistics size = "+statistics.getN());
      if (statistics.getN() == time.length) {
        StatisticsTableModel model = new StatisticsTableModel(metricName, time, statistics);
        PlotLinear plot = new PlotLinear(m_plotPanel, model, null, ChromatogramTableModel.COLTYPE_CHROMATOGRAM_XIC_TIME, ChromatogramTableModel.COLTYPE_CHROMATOGRAM_XIC_INTENSITIES);
        plot.setPlotInformation(model.getPlotInformation());
        plot.setIsPaintMarker(true);
        plot.setStrokeFixed(true);
        m_plotPanel.clearPlots();
        m_plotPanel.addPlot(plot);
        m_plotPanel.repaintUpdateDoubleBuffer();
      } else {
          logger.warn("Should not append : time range and values size are not equal !");
      }
    }

    public void setQCMetrics(QCMetrics metrics) {
        if (this.isVisible()) {
            m_metrics = metrics;
            Set<String> metricNames = new HashSet<>();
            metricNames.addAll(m_metrics.getPopulationMetricNames());

            int previousSelection = m_metricsCbx.getSelectedIndex();
            m_metricsCbx.removeAllItems();
            for (String name : metricNames) {
                m_metricsCbx.addItem(name);
            }
            m_metricsCbx.setSize(10, m_metricsCbx.getPreferredSize().height);
            if (previousSelection != -1) {
                m_metricsCbx.setSelectedIndex(previousSelection);
            } else {
                m_metricsCbx.setSelectedIndex(0);
            }
        } else {
            logger.info("Panel is hidden : do not take selection into account");
        }
    }
    
}