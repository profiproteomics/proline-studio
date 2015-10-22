package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.MasterQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.JCheckBoxList;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.CustomColumnControlButton;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class XicPeptideIonPanel  extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_peptideIonScrollPane;
    private QuantPeptideIonTable m_quantPeptideIonTable;

    private MarkerContainerPanel m_markerContainerPanel;
    
    private DQuantitationChannel[] m_quantChannels;
    private boolean m_isXICMode;
    
    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_columnVisibilityButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    

    private SearchToggleButton m_searchToggleButton;

    
    private JLabel m_titleLabel;
    private static final String TABLE_TITLE = "Peptides Ions";
    

    public XicPeptideIonPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());

        final JPanel peptideIonPanel = createPeptideIonPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptideIonPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peptideIonPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), JLayeredPane.PALETTE_LAYER); 


    }
        
    private JPanel createPeptideIonPanel() {

        JPanel peptideIonPanel = new JPanel();
        peptideIonPanel.setBounds(0, 0, 500, 400);
        peptideIonPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        
        m_titleLabel = new JLabel(TABLE_TITLE);
        peptideIonPanel.add(m_titleLabel, BorderLayout.NORTH);
        peptideIonPanel.add(toolbar, BorderLayout.WEST);
        peptideIonPanel.add(internalPanel, BorderLayout.CENTER);

        return peptideIonPanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_quantPeptideIonTable, m_quantPeptideIonTable, ((CompoundTableModel) m_quantPeptideIonTable.getModel()));
        toolbar.add(m_searchToggleButton);
        
        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_quantPeptideIonTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_quantPeptideIonTable.getModel()), "Peptides Ions", m_quantPeptideIonTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        
        m_columnVisibilityButton = new JButton();
        m_columnVisibilityButton.setIcon(IconManager.getIcon(IconManager.IconType.COLUMNS_VISIBILITY));
        m_columnVisibilityButton.setToolTipText("Hide/Show Columns...");
        m_columnVisibilityButton.setEnabled(false);
        m_columnVisibilityButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                XICPeptideIonColumnsVisibilityDialog dialog = new XICPeptideIonColumnsVisibilityDialog(WindowManager.getDefault().getMainWindow(), m_quantPeptideIonTable, (QuantPeptideIonTableModel) ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getBaseModel() );
                dialog.setLocation(m_columnVisibilityButton.getLocationOnScreen().x +m_columnVisibilityButton.getWidth(), m_columnVisibilityButton.getLocationOnScreen().y + m_columnVisibilityButton.getHeight());
                dialog.setVisible(true);
            }

        });
        toolbar.add(m_columnVisibilityButton);
        
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_quantPeptideIonTable.getModel())) {
           
            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i!=null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
            }
        };
        toolbar.add(m_addCompareDataButton);
        
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
        m_peptideIonScrollPane = new JScrollPane();
        
        m_quantPeptideIonTable = new QuantPeptideIonTable();
        m_quantPeptideIonTable.setModel(new CompoundTableModel(new QuantPeptideIonTableModel((LazyTable)m_quantPeptideIonTable), true));
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_quantPeptideIonTable);
        m_quantPeptideIonTable.setColumnControl(customColumnControl);
        
        
        m_quantPeptideIonTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_peptideIonScrollPane, m_quantPeptideIonTable);
        
        m_peptideIonScrollPane.setViewportView(m_quantPeptideIonTable);
        m_quantPeptideIonTable.setFillsViewportHeight(true);
        m_quantPeptideIonTable.setViewport(m_peptideIonScrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        

        
        return internalPanel;
    }                 
    
    public void setData(Long taskId, DQuantitationChannel[] quantChannels,  List<MasterQuantPeptideIon> peptideIons, boolean isXICMode, boolean finished) {
        boolean qcChanged = true;
        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int q = 0; q < m_quantChannels.length; q++) {
                qcChanged = !(m_quantChannels[q].equals(quantChannels[q]));
            }
        }
        m_quantChannels = quantChannels;
        m_isXICMode = isXICMode;
       ((QuantPeptideIonTableModel) ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getBaseModel()).setData(taskId, quantChannels, peptideIons, m_isXICMode);
        //m_quantPeptideIonTable.setColumnControlVisible(((QuantPeptideIonTableModel) ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getBaseModel()).getColumnCount() < XicProteinSetPanel.NB_MAX_COLUMN_CONTROL);     
        m_titleLabel.setText(TABLE_TITLE +" ("+peptideIons.size()+")");
        // select the first row
        if ((peptideIons != null) && (peptideIons.size() > 0)) {
            m_quantPeptideIonTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peptideIons.size());
        }

        if (finished) {
            // allow to change column visibility
            m_columnVisibilityButton.setEnabled(true);
            m_quantPeptideIonTable.setSortable(true);
        }
        if (qcChanged){
            setColumnsVisibility();
        }
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_quantPeptideIonTable.dataUpdated(subTask, finished);
        if (finished) {
            // allow to change column visibility
            m_columnVisibilityButton.setEnabled(true);
            m_quantPeptideIonTable.setSortable(true);
        }
    }
    
    public void setColumnsVisibility() {
        // hide the rawAbundance  and selectionLevel columns
        List<Integer> listIdsToHide = ((QuantPeptideIonTableModel) ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
        List<TableColumn> columns = m_quantPeptideIonTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_quantPeptideIonTable.getColumnExt(m_quantPeptideIonTable.convertColumnIndexToView(id)).setVisible(false);
            }
        }
        // hide the id column
        boolean columnVisible = ((TableColumnExt) columns.get(QuantPeptideIonTableModel.COLTYPE_PEPTIDE_ION_ID)).isVisible();
        if (columnVisible) {
            m_quantPeptideIonTable.getColumnExt(m_quantPeptideIonTable.convertColumnIndexToView(QuantPeptideIonTableModel.COLTYPE_PEPTIDE_ION_ID)).setVisible(false);
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
    public void addSingleValue(Object v) {
        getGlobalTableModelInterface().addSingleValue(v);
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_quantPeptideIonTable.getModel();
    }
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_quantPeptideIonTable;
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

    public MasterQuantPeptideIon getSelectedMasterQuantPeptideIon() {
        return m_quantPeptideIonTable.getSelectedMasterQuantPeptideIon();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_quantPeptideIonTable;
    }
    
    private class QuantPeptideIonTable extends LazyTable implements ExportModelInterface {

        
        public QuantPeptideIonTable() {
            super(m_peptideIonScrollPane.getVerticalScrollBar() );
            
        }

        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
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
 
            m_dataBox.propagateDataChanged(MasterQuantPeptideIon.class);

        }
        
        public boolean selectPeptideIon(Long peptideIonId, String searchText) {

            QuantPeptideIonTableModel tableModel = (QuantPeptideIonTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(peptideIonId);
            if (row == -1) {
                return false;
            }
            row = ((CompoundTableModel)getModel()).convertBaseModelRowToCompoundRow(row);
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
                ((QuantPeptideIonTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
                if (((keepLastAction == LastAction.ACTION_SELECTING ) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
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
        


        public MasterQuantPeptideIon getSelectedMasterQuantPeptideIon() {

            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }

            CompoundTableModel compoundTableModel = (CompoundTableModel) m_quantPeptideIonTable.getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            selectedRow = convertRowIndexToModel(selectedRow);
            selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

            // Retrieve PeptideIon selected
            QuantPeptideIonTableModel tableModel = (QuantPeptideIonTableModel) compoundTableModel.getBaseModel();
            return tableModel.getPeptideIon(selectedRow);
        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getExportRowCell(convertRowIndexToModel(row),  convertColumnIndexToModel(col));
        }

        
        @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();

            popupMenu.addAction(new RestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.propagateDataChanged(CompareDataInterface.class);
                }
            });
            popupMenu.addAction(new ClearRestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.propagateDataChanged(CompareDataInterface.class);
                }
            });

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

    }

    
    private class XICPeptideIonColumnsVisibilityDialog extends DefaultDialog {

        private JCheckBoxList m_rsmList;
        private JCheckBoxList m_xicList;
        

        public XICPeptideIonColumnsVisibilityDialog(Window parent, QuantPeptideIonTable table, QuantPeptideIonTableModel quantPeptideIonTableModel) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);

            // hide default and help buttons
            setButtonVisible(BUTTON_HELP, false);

            setStatusVisible(false);

            setResizable(true);

            setTitle("Select Columns to Display");

            // Prepare data for RSM
            List<String> rsmList = new ArrayList<>();
            List<Boolean> visibilityRsmList = new ArrayList<>();

            // Prepare data for different column types
            int nbTypes = quantPeptideIonTableModel.getByQCCount();
            List<String> typeList = new ArrayList<>();
            List<Boolean> visibilityTypeList = new ArrayList<>();
            boolean[] visibilityTypeArray = new boolean[nbTypes];
            for (int i=0;i<nbTypes;i++) {
                visibilityTypeArray[i] = false;
            }
            
            
            List<TableColumn> columns = table.getColumns(true);

            int rsmCount = quantPeptideIonTableModel.getQCCount();
            for (int i = 0; i < rsmCount; i++) {
                int start = quantPeptideIonTableModel.getColumStart(i);
                int stop = quantPeptideIonTableModel.getColumStop(i);

                boolean rsmVisible = false;
                for (int j = start; j <= stop; j++) {
                    boolean columnVisible = ((TableColumnExt) columns.get(j)).isVisible();
                    if (columnVisible) {
                        rsmVisible = true;
                        int type = j-start;
                        visibilityTypeArray[type] |= columnVisible;
                    }

                    
                }

                String rsmName = quantPeptideIonTableModel.getQCName(i);
                rsmList.add(rsmName);
                visibilityRsmList.add(rsmVisible);

            }
            
            for (int i = 0; i < nbTypes; i++) {
                String name = quantPeptideIonTableModel.getByQCMColumnName(i);
                typeList.add(name);
                visibilityTypeList.add(visibilityTypeArray[i]);
            }
            
             
            
            JPanel internalPanel = createInternalPanel(rsmList, visibilityRsmList, typeList, visibilityTypeList);
            setInternalComponent(internalPanel);


            
        }

        private JPanel createInternalPanel(List<String> rsmList, List<Boolean> visibilityList, List<String> typeList, List<Boolean> visibilityTypeList) {
            JPanel internalPanel = new JPanel();

            internalPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST; 
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            JLabel idSummaryLabel = new JLabel("Quantitation channel");
            JLabel informationLabel = new JLabel("Information");
            
            JSeparator separator1 = new JSeparator(JSeparator.VERTICAL);

            JScrollPane rsmScrollPane = new JScrollPane();
            m_rsmList = new JCheckBoxList(rsmList, visibilityList);
            rsmScrollPane.setViewportView(m_rsmList);

            JScrollPane xicScrollPane = new JScrollPane();
            m_xicList = new JCheckBoxList(typeList, visibilityTypeList); 
            xicScrollPane.setViewportView(m_xicList);

            c.gridx = 0;
            c.gridy = 0;
            
            internalPanel.add(idSummaryLabel, c);
            
            c.gridy++;
            c.weightx = 1;
            c.weighty = 1;
            internalPanel.add(rsmScrollPane, c);

            c.gridy = 0;
            c.gridx++;
            c.weightx = 0;
            c.weighty = 1;
            c.gridheight = 2;
            internalPanel.add(separator1, c);
            

            c.gridx++;
            c.gridheight = 1;
            c.weightx = 0;
            c.weighty = 0;
            internalPanel.add(informationLabel, c);
            
            c.gridy++;
            c.weightx = 1;
            c.weighty = 1;
            internalPanel.add(xicScrollPane, c);

            return internalPanel;
        }

       
        
        @Override
        protected boolean okCalled() {

            
            QuantPeptideIonTableModel model = (QuantPeptideIonTableModel) ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getBaseModel();
            
            int nbColumnsModel = model.getColumnCount();
            List<TableColumn> columns = m_quantPeptideIonTable.getColumns(true);
            for (int i=QuantPeptideIonTableModel.COLTYPE_PEPTIDE_ION_ELUTION_TIME+1;i<nbColumnsModel;i++) {
                int rsmCur = model.getQCNumber(i);
                int type = model.getTypeNumber(i);
                boolean visible = m_rsmList.isVisible(rsmCur) && m_xicList.isVisible(type);
                boolean columnVisible = ((TableColumnExt) columns.get(i)).isVisible();
                if (visible ^ columnVisible) {
                    ((TableColumnExt) columns.get(i)).setVisible(visible);
                }
            }
            
            

            return true;
        }

        @Override
        protected boolean cancelCalled() {

            return true;
        }

    }
    

    
}
