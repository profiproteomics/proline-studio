package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.utils.LazyTable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author JM235353
 */
public class XicProteinSetPanel  extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_proteinSetScrollPane;
    private QuantProteinSetTable m_quantProteinSetTable;

    private MarkerContainerPanel m_markerContainerPanel;
    
    private QuantitationChannel[] m_quantChannels;
    
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    
    /*private SearchFloatingPanel m_searchPanel;
    private JToggleButton m_searchToggleButton;
    private Search m_search = null;*/ //JPM.TODO
    

    public XicProteinSetPanel() {
 
        //initComponents();

    }
    
        private void initComponents() {


        setLayout(new BorderLayout());

        /*m_search = new Search();
        m_searchPanel = new SearchFloatingPanel(m_search);*/  //JPM.TODO
        final JPanel proteinSetPanel = createProteinSetPanel();
        //m_searchPanel.setToggleButton(m_searchToggleButton); //JPM.TODO

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                proteinSetPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(proteinSetPanel, JLayeredPane.DEFAULT_LAYER);
        //layeredPane.add(m_searchPanel, JLayeredPane.PALETTE_LAYER); //JPM.TODO


    }
        
    private JPanel createProteinSetPanel() {

        JPanel proteinSetPanel = new JPanel();
        proteinSetPanel.setBounds(0, 0, 500, 400);
        proteinSetPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        proteinSetPanel.add(toolbar, BorderLayout.WEST);
        proteinSetPanel.add(internalPanel, BorderLayout.CENTER);

        return proteinSetPanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);



            // Search Button
            //m_searchToggleButton = new SearchToggleButton(m_searchPanel);

            //toolbar.add(m_searchToggleButton);
    
        
        m_filterButton = new FilterButton(((QuantProteinSetTableModel) m_quantProteinSetTable.getModel()));

        m_exportButton = new ExportButton(((QuantProteinSetTableModel) m_quantProteinSetTable.getModel()), "Protein Sets", m_quantProteinSetTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        
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
        m_proteinSetScrollPane = new JScrollPane();
        
        m_quantProteinSetTable = new QuantProteinSetTable();
        m_quantProteinSetTable.setModel(new QuantProteinSetTableModel((LazyTable)m_quantProteinSetTable, m_quantChannels));
        // hide the id column
        m_quantProteinSetTable.getColumnExt(QuantProteinSetTableModel.COLTYPE_PROTEIN_SET_ID).setVisible(false);
        m_quantProteinSetTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_proteinSetScrollPane, m_quantProteinSetTable);
        
        m_proteinSetScrollPane.setViewportView(m_quantProteinSetTable);
        m_quantProteinSetTable.setFillsViewportHeight(true);
        m_quantProteinSetTable.setViewport(m_proteinSetScrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        

        
        return internalPanel;
    }                 
    
    public void setData(Long taskId, QuantitationChannel[] quantChannels,  DMasterQuantProteinSet[] proteinSets, boolean finished) {
        m_quantChannels = quantChannels;
        initComponents();
        ((QuantProteinSetTableModel) m_quantProteinSetTable.getModel()).setData(taskId,  proteinSets);

        // select the first row
        if ((proteinSets != null) && (proteinSets.length > 0)) {
            m_quantProteinSetTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(proteinSets.length);
        }

        if (finished) {
            m_quantProteinSetTable.setSortable(true);
        }
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_quantProteinSetTable.dataUpdated(subTask, finished);
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

    
    
    private class QuantProteinSetTable extends LazyTable  {

        private DMasterQuantProteinSet m_proteinSetSelected = null;
        
        
        public QuantProteinSetTable() {
            super(m_proteinSetScrollPane.getVerticalScrollBar() );
            
            //setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) ); //JPM.TODO
            
            //setDefaultRenderer(ProteinSetTableModel.ProteinCount.class, new ProteinCountRenderer());
            

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
 
            m_dataBox.propagateDataChanged(DMasterQuantProteinSet.class);

        }
        
        public boolean selectProteinSet(Long proteinSetId, String searchText) {
            QuantProteinSetTableModel tableModel = (QuantProteinSetTableModel) getModel();
            int row = tableModel.findRow(proteinSetId);
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
                ((QuantProteinSetTableModel) getModel()).dataUpdated();
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
                if (((keepLastAction == LastAction.ACTION_SELECTING ) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((QuantProteinSetTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
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
            //m_search.reinitSearch(); //JPM.TODO
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
        
        
    }
    
}
