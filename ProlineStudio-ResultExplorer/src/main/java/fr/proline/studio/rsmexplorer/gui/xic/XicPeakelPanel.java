package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Peakel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMouseAdapter;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashSet;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class XicPeakelPanel  extends HourglassPanel implements DataBoxPanelInterface, CompareDataProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_peakelScrollPane;
    private PeakelTable m_peakelTable;

    private MarkerContainerPanel m_markerContainerPanel;
    
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    

    public XicPeakelPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());

        final JPanel peakelPanel = createPeakelPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peakelPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                layeredPane.revalidate();
                layeredPane.repaint();

            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        add(layeredPane, BorderLayout.CENTER);

        layeredPane.add(peakelPanel, JLayeredPane.DEFAULT_LAYER);


    }
        
    private JPanel createPeakelPanel() {

        JPanel peakelPanel = new JPanel();
        peakelPanel.setBounds(0, 0, 500, 400);
        peakelPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        peakelPanel.add(toolbar, BorderLayout.WEST);
        peakelPanel.add(internalPanel, BorderLayout.CENTER);

        return peakelPanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_filterButton = new FilterButton(((PeakelTableModel) m_peakelTable.getModel())) {

            @Override
            protected void filteringDone() {
                 m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
        };

        m_exportButton = new ExportButton(((PeakelTableModel) m_peakelTable.getModel()), "Peakels", m_peakelTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        
        // graphics button
        m_graphicsButton = new JButton(IconManager.getIcon(IconManager.IconType.CHART));
        m_graphicsButton.setToolTipText("Graphics : Histogram / Scatter Plot");
        m_graphicsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!((PeakelTableModel) m_peakelTable.getModel()).isLoaded()) {

                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), ((PeakelTableModel) m_peakelTable.getModel()), "Data loading", "Histogram functionnality is not available while data is loading. Please Wait.");
                    dialog.setLocation(getLocationOnScreen().x + m_graphicsButton.getWidth() + 5, m_graphicsButton.getLocationOnScreen().y + getHeight() + 5);
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        return;
                    }
                }
                // prepare window box
                WindowBox wbox = WindowBoxFactory.getGraphicsWindowBox("Peakel Graphic", m_dataBox);

                wbox.setEntryData(m_dataBox.getProjectId(), m_dataBox.getData(false, CompareDataInterface.class));

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive();
            }
        });
        toolbar.add(m_graphicsButton);
        
        return toolbar;
    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // create objects
        m_peakelScrollPane = new JScrollPane();
        
        m_peakelTable = new PeakelTable();
        m_peakelTable.setModel(new PeakelTableModel((LazyTable)m_peakelTable));
        
        // hide the id column
        m_peakelTable.getColumnExt(PeakelTableModel.COLTYPE_PEAKEL_ID).setVisible(false);
        
        m_peakelTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_peakelScrollPane, m_peakelTable);
        
        m_peakelScrollPane.setViewportView(m_peakelTable);
        m_peakelTable.setFillsViewportHeight(true);
        m_peakelTable.setViewport(m_peakelScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        return internalPanel;
    }                 
    
    public void setData(Long taskId, Feature feature, List<Peakel> peakels, Color color, String title, boolean finished) {
        ((PeakelTableModel) m_peakelTable.getModel()).setData(taskId,  feature, peakels, color, title);

        // select the first row
        if ((peakels != null) && (peakels.size() > 0)) {
            m_peakelTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peakels.size());
        }
        if (finished) {
            m_peakelTable.setSortable(true);
        }

    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_peakelTable.dataUpdated(subTask, finished);
        if (finished) {
           m_peakelTable.setSortable(true);
        }
    }
    
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }
    
    @Override
    public CompareDataInterface getCompareDataInterface() {
        return (CompareDataInterface) m_peakelTable.getModel();
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    public Peakel getSelectedPeakel() {
        return m_peakelTable.getSelectedPeakel();
    }
    
    public Feature getFeature(){
        return this.m_peakelTable.getFeature();
    }
    
    public Color getColor(){
        return this.m_peakelTable.getColor();
    }
    
    public String getTitle(){
        return this.m_peakelTable.getTitle();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peakelTable;
    }
    
    private class PeakelTable extends LazyTable implements ExportTableSelectionInterface, ExportColumnTextInterface  {

        private Peakel m_peakelSelected = null;
        
        
        public PeakelTable() {
            super(m_peakelScrollPane.getVerticalScrollBar() );
            
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) ); 
            setDefaultRenderer(Double.class, new DoubleRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) ); 
        }
        
        
        /** 
         * Called whenever the value of the selection changes.
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            
            super.valueChanged(e);
            
            if (selectionWillBeRestored) {
                return;
            }
 
            m_dataBox.propagateDataChanged(Peakel.class);

        }
        
        public boolean selectPeakel(Long peakelId, String searchText) {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            int row = tableModel.findRow(peakelId);
            if (row == -1) {
                return false;
            }
            
            // JPM.hack we need to keep the search text
            // to be able to give it if needed to the panel
            // which display proteins of a protein set
            searchTextBeingDone = searchText;
            
            // must convert row index if there is a sorting
            row = convertRowIndexToView(row);
            
            // select the row
            getSelectionModel().setSelectionInterval(row, row);
            
            // scroll to the row
            scrollRowToVisible(row);

            searchTextBeingDone = null;
            
            return true;
        }
        String searchTextBeingDone = null;

        public void dataUpdated(SubTask subTask, boolean finished) {
            
            LastAction keepLastAction = m_lastAction;
            try {
            
            
            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((PeakelTableModel) getModel()).dataUpdated();
            } finally {
                selectionWillBeRestored(false);
            }

            
            
            // restore selected row
            if (rowSelectedInModel != -1) {
                int rowSelectedInView = convertRowIndexToView(rowSelectedInModel);
                //getSelectionModel().setSelectionInterval(rowSelectedInView, rowSelectedInView);
                setSelection(rowSelectedInView);

                
                // if the subtask correspond to the loading of the data of the sorted column,
                // we keep the row selected visible
                if (((keepLastAction == LastAction.ACTION_SELECTING ) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((PeakelTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
                    scrollRowToVisible(rowSelectedInView);
                }
                    
            }

            } finally {

                m_lastAction = keepLastAction;
 
            }
            
            if (finished) {
                setSortable(true);
            }
        }
        
        @Override
        public void sortingChanged(int col) {
        }
    
        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }
        

        @Override
        public HashSet exportSelection(int[] rows) {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            return tableModel.exportSelection(rows);
        }

        public Peakel getSelectedPeakel() {

            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }

            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            if (tableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            selectedRow = convertRowIndexToModel(selectedRow);

            // Retrieve Peptide selected
            return tableModel.getPeakel(selectedRow);
        }
        
        public Feature getFeature() {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            return tableModel.getFeature();
        }
        
        public Color getColor() {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            return tableModel.getColor();
        }
        
        public String getTitle() {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            return tableModel.getTitle();
        }

        @Override
        public String getExportColumnName(int col) {
            return ((PeakelTableModel) m_peakelTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        
    }

}
