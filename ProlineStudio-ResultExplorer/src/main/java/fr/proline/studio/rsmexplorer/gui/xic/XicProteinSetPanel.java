package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.calc.CalcDialog;
import fr.proline.studio.comparedata.AddCompareDataButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseSearchProteinSetsTask;
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
import fr.proline.studio.rsmexplorer.actions.table.DisplayIdentificationProteinSetsAction;
import fr.proline.studio.rsmexplorer.gui.renderer.BigFloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.CompareValueRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.search.AbstractSearch;
import fr.proline.studio.search.SearchFloatingPanel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class XicProteinSetPanel  extends HourglassPanel implements DataBoxPanelInterface, CompareDataProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_proteinSetScrollPane;
    private QuantProteinSetTable m_quantProteinSetTable;

    private MarkerContainerPanel m_markerContainerPanel;

    
    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_columnVisibilityButton;
    private AddCompareDataButton m_addCompareDataButton;
    
    private JButton m_testButton;
    

    private SearchFloatingPanel m_searchPanel;
    private JToggleButton m_searchToggleButton;
    private XICProteinSetSearch m_search = null;
    
    private JLabel m_titleLabel;
    private final String TABLE_TITLE = "Proteins Sets";

    private DQuantitationChannel[] m_quantChannels;
    
    public XicProteinSetPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());

        m_search = new XICProteinSetSearch();
        m_searchPanel = new SearchFloatingPanel(m_search);
        final JPanel proteinSetPanel = createProteinSetPanel();
        m_searchPanel.setToggleButton(m_searchToggleButton); 

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
        layeredPane.add(m_searchPanel, JLayeredPane.PALETTE_LAYER); 


    }
        
    private JPanel createProteinSetPanel() {

        JPanel proteinSetPanel = new JPanel();
        proteinSetPanel.setBounds(0, 0, 500, 400);
        proteinSetPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        m_titleLabel = new JLabel(TABLE_TITLE);
        proteinSetPanel.add(m_titleLabel, BorderLayout.NORTH);
        proteinSetPanel.add(toolbar, BorderLayout.WEST);
        proteinSetPanel.add(internalPanel, BorderLayout.CENTER);

        return proteinSetPanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_searchPanel);
        toolbar.add(m_searchToggleButton);
        
        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_quantProteinSetTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_quantProteinSetTable.getModel()), "Protein Sets", m_quantProteinSetTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        
        m_columnVisibilityButton = new JButton();
        m_columnVisibilityButton.setIcon(IconManager.getIcon(IconManager.IconType.COLUMNS_VISIBILITY));
        m_columnVisibilityButton.setToolTipText("Hide/Show Columns...");
        m_columnVisibilityButton.setEnabled(false);
        m_columnVisibilityButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                XICProteinSetColumnsVisibilityDialog dialog = new XICProteinSetColumnsVisibilityDialog(WindowManager.getDefault().getMainWindow(), m_quantProteinSetTable, ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()) );
                dialog.setLocation(m_columnVisibilityButton.getLocationOnScreen().x +m_columnVisibilityButton.getWidth(), m_columnVisibilityButton.getLocationOnScreen().y + m_columnVisibilityButton.getHeight());
                dialog.setVisible(true);
            }

        });
        toolbar.add(m_columnVisibilityButton);
        
        m_addCompareDataButton = new AddCompareDataButton(((CompoundTableModel) m_quantProteinSetTable.getModel()), (CompareDataInterface)  m_quantProteinSetTable.getModel()) {
            
            @Override
            public void actionPerformed(CompareDataInterface compareDataInterface) {
                compareDataInterface.setName(m_dataBox.getFullName());
                DataMixerWindowBoxManager.addCompareTableModel(compareDataInterface);
            }
        };
        toolbar.add(m_addCompareDataButton);
        
        m_testButton = new JButton(IconManager.getIcon(IconManager.IconType.CALCULATOR));
        m_testButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CalcDialog dialog = CalcDialog.getCalcDialog(WindowManager.getDefault().getMainWindow(), m_quantProteinSetTable);
                dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);

            }

        });
        toolbar.add(m_testButton);
        
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
        m_quantProteinSetTable.setModel(new CompoundTableModel(new QuantProteinSetTableModel((LazyTable)m_quantProteinSetTable), true));
        
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
    
    public void setData(Long taskId, DQuantitationChannel[] quantChannels,  List<DMasterQuantProteinSet> proteinSets, boolean finished) {
        m_quantChannels = quantChannels;
        ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).setData(taskId, quantChannels, proteinSets);

        m_titleLabel.setText(TABLE_TITLE +" ("+proteinSets.size()+")");
       // select the first row
        if ((proteinSets != null) && (proteinSets.size() > 0)) {
            m_quantProteinSetTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(proteinSets.size());
        }
        
        if (finished) {

            // allow to change column visibility
            m_columnVisibilityButton.setEnabled(true);
            m_quantProteinSetTable.setSortable(true);
            // hide the rawAbundance  and selectionLevel columns
            List<Integer> listIdsToHide = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
            for (Integer id : listIdsToHide) {
                m_quantProteinSetTable.getColumnExt(id.intValue()).setVisible(false);
            }
            // hide Id column
            m_quantProteinSetTable.getColumnExt(QuantProteinSetTableModel.COLTYPE_PROTEIN_SET_ID).setVisible(false);
            
        }
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_quantProteinSetTable.dataUpdated(subTask, finished);
        if (finished) {
            // allow to change column visibility
            m_columnVisibilityButton.setEnabled(true);
            m_quantProteinSetTable.setSortable(true);
            // hide the rawAbundance  and selectionLevel columns
            List<Integer> listIdsToHide = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
            for (Integer id : listIdsToHide) {
                m_quantProteinSetTable.getColumnExt(id.intValue()).setVisible(false);
            }
            // hide Id column
            m_quantProteinSetTable.getColumnExt(QuantProteinSetTableModel.COLTYPE_PROTEIN_SET_ID).setVisible(false);
        }
    }
    
    public DProteinSet getSelectedProteinSet() {
        return m_quantProteinSetTable.getSelectedProteinSet();
    }
    
    public DMasterQuantProteinSet getSelectedMasterQuantProteinSet() {
        return m_quantProteinSetTable.getSelectedMasterQuantProteinSet();
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
        return (CompareDataInterface) m_quantProteinSetTable.getModel();
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

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_quantProteinSetTable;
    }

    
    
    private class QuantProteinSetTable extends LazyTable implements ExportTableSelectionInterface, ExportModelInterface {

        
        public QuantProteinSetTable() {
            super(m_proteinSetScrollPane.getVerticalScrollBar() );
            setDefaultRenderer(Float.class, new BigFloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)), 0 ) ); 
            //setDefaultRenderer(Double.class, new DoubleRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) ); 
            setDefaultRenderer(CompareValueRenderer.CompareValue.class, new CompareValueRenderer());
            
        }
        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();

            m_idProteinSetAction = new DisplayIdentificationProteinSetsAction();
            popupMenu.addAction(m_idProteinSetAction);
            popupMenu.addAction(null);
            popupMenu.addAction(new RestrainAction());
            popupMenu.addAction(new ClearRestrainAction());

            return popupMenu;
        }
        private DisplayIdentificationProteinSetsAction m_idProteinSetAction;
        
        // set as abstract
        @Override
        public void prepostPopupMenu() {
            m_idProteinSetAction.setBox(m_dataBox);
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

            m_dataBox.propagateDataChanged(DProteinSet.class);

        }

        @Override
        public HashSet exportSelection(int[] rows) {
            CompoundTableModel tableModel = (CompoundTableModel) getModel();
            int[] modelRows = new int[rows.length];
            for (int i = 0; i < rows.length; i++) {
                int compoundModelRow = convertRowIndexToModel(rows[i]);
                modelRows[i] = tableModel.convertRowToOriginalModel(compoundModelRow);
            }
            
            QuantProteinSetTableModel baseModel = (QuantProteinSetTableModel) tableModel.getBaseModel();
            return baseModel.exportSelection(modelRows);
        }
        
        
        public DProteinSet getSelectedProteinSet() {

            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }

            CompoundTableModel compoundTableModel = (CompoundTableModel) getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            selectedRow = convertRowIndexToModel(selectedRow);
            selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

            // Retrieve PeptideIon selected
            QuantProteinSetTableModel tableModel = (QuantProteinSetTableModel) compoundTableModel.getBaseModel();
            return tableModel.getProteinSet(selectedRow);

        }
        
        public DMasterQuantProteinSet getSelectedMasterQuantProteinSet() {

            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }

            
            CompoundTableModel compoundTableModel = (CompoundTableModel) getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            selectedRow = convertRowIndexToModel(selectedRow);
            selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

            // Retrieve PeptideIon selected
            QuantProteinSetTableModel tableModel = (QuantProteinSetTableModel) compoundTableModel.getBaseModel();
            return tableModel.getMasterQuantProteinSet(selectedRow);

        }
        
        public boolean selectProteinSet(Long proteinSetId, String searchText) {

            QuantProteinSetTableModel tableModel = (QuantProteinSetTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(proteinSetId);
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
                ((QuantProteinSetTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
        
        @Override
        public void sortingChanged(int col) {
            m_search.reinitSearch(); 
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
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_quantProteinSetTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) m_quantProteinSetTable.getModel()).getExportRowCell(convertRowIndexToModel(row),  convertColumnIndexToModel(col));
        }

        

        
    }

    
    private class XICProteinSetColumnsVisibilityDialog extends DefaultDialog {

        private JCheckBoxList m_rsmList;
        private JCheckBoxList m_xicList;
        
        
        private JRadioButton m_noOverviewRB;
        private JRadioButton m_abundanceOverviewRB;
        private JRadioButton m_rawAbundanceOverviewRB;
        

        public XICProteinSetColumnsVisibilityDialog(Window parent, QuantProteinSetTable table, QuantProteinSetTableModel quantProteinSetTableModel) {
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
            int nbTypes = quantProteinSetTableModel.getByQCCount();
            List<String> typeList = new ArrayList<>();
            List<Boolean> visibilityTypeList = new ArrayList<>();
            boolean[] visibilityTypeArray = new boolean[nbTypes];
            for (int i=0;i<nbTypes;i++) {
                visibilityTypeArray[i] = false;
            }
            
            
            List<TableColumn> columns = table.getColumns(true);

            int rsmCount = quantProteinSetTableModel.getQCCount();
            for (int i = 0; i < rsmCount; i++) {
                int start = quantProteinSetTableModel.getColumStart(i);
                int stop = quantProteinSetTableModel.getColumStop(i);

                boolean rsmVisible = false;
                for (int j = start; j <= stop; j++) {
                    boolean columnVisible = ((TableColumnExt) columns.get(j)).isVisible();
                    if (columnVisible) {
                        rsmVisible = true;
                        int type = j-start;
                        visibilityTypeArray[type] |= columnVisible;
                    }

                    
                }

                String rsmName = quantProteinSetTableModel.getQCName(i);
                rsmList.add(rsmName);
                visibilityRsmList.add(rsmVisible);

            }
            
            for (int i = 0; i < nbTypes; i++) {
                String name = quantProteinSetTableModel.getByQCMColumnName(i);
                typeList.add(name);
                visibilityTypeList.add(visibilityTypeArray[i]);
            }
            
            int overviewType = quantProteinSetTableModel.getOverviewType();
            boolean overviewColumnVisible = ((TableColumnExt) columns.get(QuantProteinSetTableModel.COLTYPE_OVERVIEW)).isVisible();
            
            
            
            JPanel internalPanel = createInternalPanel(rsmList, visibilityRsmList, typeList, visibilityTypeList, overviewColumnVisible, overviewType);
            setInternalComponent(internalPanel);


            
        }

        private JPanel createInternalPanel(List<String> rsmList, List<Boolean> visibilityList, List<String> typeList, List<Boolean> visibilityTypeList,  boolean overviewColumnVisible, int overviewType) {
            JPanel internalPanel = new JPanel();

            internalPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST; 
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            JLabel idSummaryLabel = new JLabel("Quantitation channel");
            JLabel informationLabel = new JLabel("Information");
            JLabel overviewLabel = new JLabel("Overview");
            
            JSeparator separator1 = new JSeparator(JSeparator.VERTICAL);
            JSeparator separator2 = new JSeparator(JSeparator.VERTICAL);

            JScrollPane rsmScrollPane = new JScrollPane();
            m_rsmList = new JCheckBoxList(rsmList, visibilityList);
            rsmScrollPane.setViewportView(m_rsmList);

            JScrollPane xicScrollPane = new JScrollPane();
            m_xicList = new JCheckBoxList(typeList, visibilityTypeList); 
            xicScrollPane.setViewportView(m_xicList);

            JScrollPane overviewScrollPane = new JScrollPane();
            overviewScrollPane.setViewportView(createOverviewPanel(overviewColumnVisible, overviewType));

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
            
            c.gridy = 0;
            c.gridx++;
            c.weightx = 0;
            c.weighty = 1;
            c.gridheight = 2;
            internalPanel.add(separator2, c);
            

            c.gridx++;
            c.gridheight = 1;
            c.weightx = 0;
            c.weighty = 0;
            internalPanel.add(overviewLabel, c);
            
            c.gridy++;
            c.weightx = 1;
            c.weighty = 1;
            internalPanel.add(overviewScrollPane, c);

            return internalPanel;
        }

       private JPanel createOverviewPanel(boolean overviewColumnVisible, int overviewType) {
            JPanel overviewPanel = new JPanel();
            overviewPanel.setBackground(Color.white);

            overviewPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST; 
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(0, 0, 0, 0);
            
            m_noOverviewRB = new JRadioButton("No Overview");
            m_abundanceOverviewRB = new JRadioButton("Overview on Abundance");
            m_rawAbundanceOverviewRB = new JRadioButton("Overview on Raw Abundance");
            m_noOverviewRB.setBackground(Color.white);
            m_abundanceOverviewRB.setBackground(Color.white);
            m_rawAbundanceOverviewRB.setBackground(Color.white);
            
            ButtonGroup group = new ButtonGroup();
            group.add(m_noOverviewRB);
            group.add(m_abundanceOverviewRB);
            group.add(m_rawAbundanceOverviewRB);
            
            if (!overviewColumnVisible) {
                m_noOverviewRB.setSelected(true);
            } else {
                switch (overviewType) {
                    case QuantPeptideTableModel.COLTYPE_ABUNDANCE:
                        m_abundanceOverviewRB.setSelected(true);
                        break;
                    case QuantPeptideTableModel.COLTYPE_RAW_ABUNDANCE:
                        m_rawAbundanceOverviewRB.setSelected(true);
                        break;

                }
            }
            
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            
            overviewPanel.add(m_noOverviewRB, c);
            
            c.gridy++;
            overviewPanel.add(m_abundanceOverviewRB, c);
            
            c.gridy++;
            overviewPanel.add(m_rawAbundanceOverviewRB, c);
            
            c.gridy++;
            c.weighty = 1;
            overviewPanel.add(Box.createHorizontalGlue(), c);
            
            
            return overviewPanel;
        }
        
        @Override
        protected boolean okCalled() {

            QuantProteinSetTableModel model = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel());
            
            List<TableColumn> columns = m_quantProteinSetTable.getColumns(true);
            int nbQCCol = model.getColumnQCCount();
            int end = QuantProteinSetTableModel.LAST_STATIC_COLUMN+1 + nbQCCol;
            for (int i=QuantProteinSetTableModel.LAST_STATIC_COLUMN+1;i<end;i++) {
                int rsmCur = model.getQCNumber(i);
                int type = model.getTypeNumber(i);
                boolean visible = m_rsmList.isVisible(rsmCur) && m_xicList.isVisible(type);
                boolean columnVisible = ((TableColumnExt) columns.get(i)).isVisible();
                if (visible ^ columnVisible) {
                    ((TableColumnExt) columns.get(i)).setVisible(visible);
                }
            }
            if (m_abundanceOverviewRB.isSelected()) {
                model.setOverviewType(QuantProteinSetTableModel.COLTYPE_ABUNDANCE);
            } else if (m_rawAbundanceOverviewRB.isSelected()) {
                model.setOverviewType(QuantProteinSetTableModel.COLTYPE_RAW_ABUNDANCE);
            }
            
            boolean overviewVisible = !m_noOverviewRB.isSelected();
            boolean columnVisible = ((TableColumnExt) columns.get(QuantProteinSetTableModel.COLTYPE_OVERVIEW)).isVisible();
            if (overviewVisible ^ columnVisible) {
                ((TableColumnExt) columns.get(QuantProteinSetTableModel.COLTYPE_OVERVIEW)).setVisible(overviewVisible);
            }
            

            return true;
        }

        @Override
        protected boolean cancelCalled() {

            return true;
        }

    }
    
    
    
    private class XICProteinSetSearch extends AbstractSearch {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Long> proteinSetIds = new ArrayList<>();

        @Override
        public void reinitSearch() {
            if (proteinSetIds.isEmpty()) {
                return;
            }
            searchIndex = -1;

            ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).sortAccordingToModel(proteinSetIds, (CompoundTableModel) m_quantProteinSetTable.getModel());
        
        }

        @Override
        public void doSearch(String text) {
            final String searchText = text.trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                
                int checkLoopIndex = -1;
                while (true) {
                    // search already done, display next result
                    searchIndex++;
                    if (searchIndex >= proteinSetIds.size()) {
                        searchIndex = 0;
                    }

                    if (checkLoopIndex == searchIndex) {
                        break;
                    }
                    
                    if (!proteinSetIds.isEmpty()) {
                        boolean found = ((QuantProteinSetTable) m_quantProteinSetTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
                        if (found) {
                            break;
                        }
                    } else {
                        break;
                    }
                    if (checkLoopIndex == -1) {
                        checkLoopIndex =  searchIndex;
                    }
                }
                
            } else {
                previousSearch = searchText;
                searchIndex = -1;

                // prepare callback for the search
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                        // contruct the Map of proteinSetId
                        
                        
                        if (!proteinSetIds.isEmpty()) {
                            
                            ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).sortAccordingToModel(proteinSetIds, (CompoundTableModel) m_quantProteinSetTable.getModel());
        
                             int checkLoopIndex = -1;
                             while (true) {
                                // search already done, display next result
                                searchIndex++;
                                if (searchIndex >= proteinSetIds.size()) {
                                    searchIndex = 0;
                                }

                                if (checkLoopIndex == searchIndex) {
                                    break;
                                }

                                if (!proteinSetIds.isEmpty()) {
                                    boolean found = ((QuantProteinSetTable) m_quantProteinSetTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
                                    if (found) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                                if (checkLoopIndex == -1) {
                                    checkLoopIndex = searchIndex;
                                }
                            }
                            
                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        m_searchPanel.enableSearch(true);
                    }
                };

                Long rsmId = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).getResultSummaryId();


                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchProteinSetsTask(callback, m_dataBox.getProjectId() ,rsmId, searchText, proteinSetIds));

                m_searchPanel.enableSearch(false);
            }
        
        }
    }
    
}
