package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.*;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.dialog.CalcDialog;
import fr.proline.studio.rsmexplorer.gui.model.PeptideInstanceTableModel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Panel for Peptides Instances
 *
 * @author JM235353
 */
public class RsmPeptidesPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    private PeptideInstanceTable m_peptideInstanceTable;
    private JScrollPane m_scrollPane;
    private MarkerContainerPanel m_markerContainerPanel;
    private JButton m_decoyButton;
    private SearchToggleButton m_searchToggleButton;

    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    private JButton m_calcButton;
    
    public RsmPeptidesPanel() {
        initComponents();

    }

    private void initComponents() {


        setLayout(new BorderLayout());

        final JPanel peptidesPanel = createPeptidesPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptidesPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peptidesPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), JLayeredPane.PALETTE_LAYER);

    }

    private String getTopComponentName() {
        Container c = getParent();
        while ((c != null) && !(c instanceof TopComponent)) {
            c = c.getParent();
        }
        if ((c != null) && (c instanceof TopComponent)) {
            return ((TopComponent) c).getName();
        }
        return "";
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_decoyButton = new JButton(IconManager.getIcon(IconManager.IconType.DATASET_RSM_DECOY));
        m_decoyButton.setToolTipText("Display Decoy Data");
        m_decoyButton.setEnabled(false);



        m_decoyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
                ResultSummary decoyRsm = rsm.getDecotResultSummary();
                if (decoyRsm == null) {
                    return;
                }

                String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);

                AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(savedWindow);
                WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, false, WindowSavedManager.SAVE_WINDOW_FOR_RSM);
                wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive();

            }
        });

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_peptideInstanceTable, m_peptideInstanceTable, ((CompoundTableModel) m_peptideInstanceTable.getModel()));

        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_peptideInstanceTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };
        
        m_exportButton = new ExportButton(((CompoundTableModel) m_peptideInstanceTable.getModel()), "Peptide Instances", m_peptideInstanceTable);
        
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_peptideInstanceTable.getModel())) {
           
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
        
        m_calcButton = new JButton(IconManager.getIcon(IconManager.IconType.CALCULATOR));
        m_calcButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CalcDialog dialog = CalcDialog.getCalcDialog(WindowManager.getDefault().getMainWindow(), m_peptideInstanceTable);
                dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);

            }

        });

        toolbar.add(m_decoyButton);
        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        toolbar.add(m_calcButton);
        
        return toolbar;
    }

    private JPanel createPeptidesPanel() {
        JPanel peptidesPanel = new JPanel();
        peptidesPanel.setBounds(0, 0, 500, 400);

        peptidesPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        peptidesPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        peptidesPanel.add(toolbar, BorderLayout.WEST);


        return peptidesPanel;

    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);



        // create objects
        m_scrollPane = new JScrollPane();

        m_peptideInstanceTable = new PeptideInstanceTable();
        m_peptideInstanceTable.setModel(new CompoundTableModel(new PeptideInstanceTableModel((LazyTable) m_peptideInstanceTable), true));
        m_peptideInstanceTable.getColumnExt(m_peptideInstanceTable.convertColumnIndexToView(PeptideInstanceTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        
        
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, (PeptideInstanceTable) m_peptideInstanceTable);


        m_scrollPane.setViewportView(m_peptideInstanceTable);
        m_peptideInstanceTable.setFillsViewportHeight(true);
        m_peptideInstanceTable.setViewport(m_scrollPane.getViewport());




        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);



        return internalPanel;
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
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_peptideInstanceTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_peptideInstanceTable;
    }
    
    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peptideInstanceTable;
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

    public void setData(long taskId, PeptideInstance[] peptideInstances, boolean finished) {

        ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
        if (rsm != null) {
            m_decoyButton.setEnabled(rsm.getDecotResultSummary() != null);
        }

        ((PeptideInstanceTableModel) ((CompoundTableModel) m_peptideInstanceTable.getModel()).getBaseModel()).setData(taskId, peptideInstances);


        // select the first row
        if ((peptideInstances != null) && (peptideInstances.length > 0)) {
            m_peptideInstanceTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peptideInstances.length);
            
        }

        if (finished) {
            ((PeptideInstanceTable) m_peptideInstanceTable).setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {

        ((PeptideInstanceTable) m_peptideInstanceTable).dataUpdated(subTask, finished);


    }

    public PeptideInstance getSelectedPeptideInstance() {

        PeptideInstanceTable table = ((PeptideInstanceTable) m_peptideInstanceTable);

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);

        CompoundTableModel compoundTableModel = ((CompoundTableModel)table.getModel());
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

        // Retrieve ProteinSet selected
        PeptideInstanceTableModel tableModel = (PeptideInstanceTableModel) compoundTableModel.getBaseModel();
        return tableModel.getPeptideInstance(selectedRow);
    }

    private class PeptideInstanceTable extends LazyTable {

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        public PeptideInstanceTable() {
            super(m_scrollPane.getVerticalScrollBar());
            displayColumnAsPercentage(PeptideInstanceTableModel.COLTYPE_PEPTIDE_SCORE);

        }


        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            if (selectionWillBeRestored) {
                return;
            }



            m_dataBox.propagateDataChanged(PeptideInstance.class);
            m_dataBox.propagateDataChanged(DPeptideMatch.class);

        }

        public boolean selectPeptideInstance(Long peptideInstanceId, String searchText) {

            PeptideInstanceTableModel tableModel = (PeptideInstanceTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(peptideInstanceId);
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
                    ((PeptideInstanceTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
                } finally {
                    selectionWillBeRestored(false);
                }



                // restore selected row
                if (rowSelectedInModel != -1) {
                    int rowSelectedInView = convertRowIndexToView(rowSelectedInModel);
                    setSelection(rowSelectedInView);

                    // if the subtask correspond to the loading of the data of the sorted column,
                    // we keep the row selected visible
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
                        ((PeptideInstanceTable) m_peptideInstanceTable).scrollRowToVisible(rowSelectedInView);
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
            //((SearchButton)searchButton).sortingChanged();
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
    



}
