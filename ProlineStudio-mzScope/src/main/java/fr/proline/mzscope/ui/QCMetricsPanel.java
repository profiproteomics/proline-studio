/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.ui.model.MapTableModel;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.QCMetrics;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.BaseGraphicsPanel;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
class QCMetricsPanel extends JPanel {

    private final static Logger logger = LoggerFactory.getLogger(QCMetrics.class);
    
    private final List<IRawFile> rawFiles;
    private final CompoundTableModel tableModel = new CompoundTableModel(new MapTableModel(), true);
    private BaseGraphicsPanel graphicsPanel;
    private JTabbedPane tabbedPanel;
    private List<QCMetrics> data;
    
    public QCMetricsPanel(List<IRawFile> rawFiles) {
        this.rawFiles = rawFiles;
        initComponents();
        data = this.rawFiles.stream().map(f -> f.getFileMetrics()).collect(Collectors.toList());
        List<String> names = rawFiles.stream().map(f -> f.getName()).collect(Collectors.toList());    
        List<Map<String,Object>> values = data.stream().map(m -> m.asSummary()).collect(Collectors.toList());    
        ((MapTableModel)tableModel.getBaseModel()).setData(values, names);
        graphicsPanel.setData(tableModel, null);
    }

    private void initComponents() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JToolBar tableToolbar = new JToolBar();
        tableToolbar.setFloatable(false);
        tableToolbar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        tableToolbar.setRollover(true);
        JScrollPane tableScrollPane = new JScrollPane();
        DecoratedMarkerTable table = new DecoratedMarkerTable() {

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
        
        table.setModel(tableModel);
        tableScrollPane.setViewportView(table);
        table.setFillsViewportHeight(true);
        table.setViewport(tableScrollPane.getViewport());
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        selectedRow = table.convertRowIndexToModel(selectedRow);
                        selectedRow = tableModel.convertCompoundRowToBaseModelRow(selectedRow);
                        logger.info("selection is : " + selectedRow + ", " + data.get(selectedRow).getRawFile().getName());
                    }
                }
            }
        });
        ExportButton exportButton = new ExportButton(tableModel, "Export", table);
        tableToolbar.add(exportButton);
        FilterButton filterButton = new FilterButton(tableModel) {

            @Override
            protected void filteringDone() {

            }

        };

        tableToolbar.add(filterButton);

        MarkerContainerPanel markerContainerPanel = new MarkerContainerPanel(tableScrollPane, table);
        JPanel tablePane = new JPanel();
        tablePane.setLayout(new BorderLayout());
        tablePane.add(tableToolbar, BorderLayout.WEST);
        tablePane.add(markerContainerPanel, BorderLayout.CENTER);

        tabbedPanel = new JTabbedPane();
        
        graphicsPanel = new BaseGraphicsPanel(false);
        tabbedPanel.add("All Metrics", graphicsPanel);
        tabbedPanel.add("Metrics details", new JPanel());
        
        split.setLeftComponent(tablePane);
        split.setRightComponent(tabbedPanel);
        
        this.setLayout(new BorderLayout());
        this.add(split, BorderLayout.CENTER);
    }

}
