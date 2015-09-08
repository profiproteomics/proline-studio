package fr.proline.studio.rsmexplorer.gui;



import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.model.ProteinTableModel;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;

/**
 * In : Window which display Protein Sets of a Result Summary - Panel used to display Proteins of a Protein Set (at the
 * center of the window)
 *
 * @author JM235353
 */
public class RsmProteinsOfProteinSetPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    private DProteinSet m_proteinSetCur = null;

    private JTextField m_proteinNameTextField;
    private ProteinTable m_proteinTable;
    private JScrollPane m_scrollPane;

    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    
    /**
     * Creates new form RsmProteinsOfProteinSetPanel
     */
    public RsmProteinsOfProteinSetPanel() {
        
        setLayout(new BorderLayout());
        
        JPanel proteinPanel = createProteinPanel();
        
        add(proteinPanel, BorderLayout.CENTER);
        
    }
    
    
    private JPanel createProteinPanel() {
        JPanel proteinPanel = new JPanel();
        proteinPanel.setBounds(0, 0, 500, 400);

        proteinPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        proteinPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        proteinPanel.add(toolbar, BorderLayout.WEST);


        return proteinPanel;

    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        
        m_proteinNameTextField = new JTextField();
        m_proteinNameTextField.setEditable(false);
        m_proteinNameTextField.setBackground(Color.white);
        
        
        m_scrollPane = new javax.swing.JScrollPane();
        m_proteinTable = new ProteinTable();
        CompoundTableModel model = new CompoundTableModel(new ProteinTableModel((ProgressInterface) m_proteinTable), true);
        m_proteinTable.setModel(model);
        m_scrollPane.setViewportView(m_proteinTable);

        
        
        m_proteinTable.displayColumnAsPercentage(ProteinTableModel.Column.PROTEIN_SCORE.ordinal());

        
        URLCellRenderer urlRenderer = (URLCellRenderer) model.getRenderer(ProteinTableModel.Column.PROTEIN_NAME.ordinal());
        m_proteinTable.addMouseListener(urlRenderer);
        m_proteinTable.addMouseMotionListener(urlRenderer);
        
        // hide the id column  (must be done after the URLCellRenderer is set)
        m_proteinTable.getColumnExt(m_proteinTable.convertColumnIndexToView(ProteinTableModel.Column.PROTEIN_ID.ordinal())).setVisible(false);
        
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(new JLabel("Typical Protein:"), c);
        
        c.gridx++;
        c.weightx = 1;
        internalPanel.add(m_proteinNameTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 1;
        internalPanel.add(m_scrollPane, c);
        
        return internalPanel;
        

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_proteinTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };
        m_exportButton = new ExportButton(((CompoundTableModel) m_proteinTable.getModel()), "Proteins", m_proteinTable);
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_proteinTable.getModel())) {
           
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
        
        
        
        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);

        return toolbar;
    }

    public DProteinMatch getSelectedProteinMatch() {

        ProteinTable table = (ProteinTable) m_proteinTable;

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        CompoundTableModel compoundTableModel = (CompoundTableModel) table.getModel();
        if (compoundTableModel.getRowCount() == 0) {
            return null; // this is a wart, for an unknown reason, it happens that the first row
            // is selected although it does not exist.
        }
        
        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);


        // Retrieve ProteinMatch selected
        ProteinTableModel tableModel = (ProteinTableModel) compoundTableModel.getBaseModel();
        return tableModel.getProteinMatch(selectedRow);
    }

    public void setData(DProteinSet proteinSet, String searchedText) {

        if (proteinSet == m_proteinSetCur) {
            return;
        }
        m_proteinSetCur = proteinSet;

        if (proteinSet == null) {
            clearData();
            return;
        }

        // retrieve sameset and subset
        DProteinMatch[] sameSetArray = proteinSet.getSameSet();
        DProteinMatch[] subSetArray = proteinSet.getSubSet();

        // retrieve Typical Protein Match
        DProteinMatch typicalProtein = proteinSet.getTypicalProteinMatch();

        
        if (typicalProtein == null) {
            // data not ready
            clearData();
            return;
        }
        

        // Modify protein description
        m_proteinNameTextField.setText(typicalProtein.getDescription());


        // Modify the Model
        ((ProteinTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).setData(proteinSet.getResultSummaryId(), proteinSet.getTypicalProteinMatch().getId() ,sameSetArray, subSetArray);
        
        
        // Select the Row
        int row = ((ProteinTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).findRowToSelect(searchedText);
        if (row != -1) {
            row = ((CompoundTableModel) m_proteinTable.getModel()).convertBaseModelRowToCompoundRow(row);
        }

        m_proteinTable.getSelectionModel().setSelectionInterval(row, row);

        m_proteinTable.setSortable(true);
        
    }

    private void clearData() {
        m_proteinNameTextField.setText("");
        ((ProteinTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).setData(-1, -1, null, null);

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
        return (GlobalTableModelInterface) m_proteinTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_proteinTable;
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
        return m_proteinTable;
    }





    private class ProteinTable extends DecoratedTable implements ProgressInterface {

        public ProteinTable() {

        }
        
        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            m_dataBox.propagateDataChanged(DProteinMatch.class);


        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
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
