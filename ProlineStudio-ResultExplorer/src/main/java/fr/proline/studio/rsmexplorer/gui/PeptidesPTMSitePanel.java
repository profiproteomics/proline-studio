/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.data.PTMSite;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.pattern.MsQueryInfoRset;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.PeptidesOfPtmSiteTableModel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Comparator;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXTable;

/**
 *HourglassPanel
 * @author VD225637
 */
public class PeptidesPTMSitePanel extends JPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_peptidesPtmSiteScrollPane;
    private PeptidesPTMSiteTable m_peptidesPtmSiteTable;
    private MarkerContainerPanel m_markerContainerPanel;

    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;

    private boolean m_displayPeptidesMatches;
    /**
     * Display Peptides Matches of a PTMSite.
     * 
     * @param viewAll if true display all Peptides Matches of all Peptide Instance for this PTMSite. If false, 
     * display only best PeptideMatch of all Peptide Instance for this PTMSite.
     * 
     */
    public PeptidesPTMSitePanel(boolean viewAll) {
        m_displayPeptidesMatches = viewAll;
        initComponents();
    }

    public PTMSite getSelectedPTMSite() {

        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // Retrieve ProteinPTMSite selected
        PeptidesOfPtmSiteTableModel tableModel = (PeptidesOfPtmSiteTableModel) compoundTableModel.getBaseModel();
        return tableModel.getCurrentPTMSite();
    }
    
    public DPeptideMatch getSelectedPeptideMatchSite() {

        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // Retrieve ProteinPTMSite selected
        PeptidesOfPtmSiteTableModel tableModel = (PeptidesOfPtmSiteTableModel) compoundTableModel.getBaseModel();
        
        // Retrieve Selected Row
        int selectedRow = getSelectedTableModelRow();    
        
        return tableModel.getSelectedPeptideMatchSite(selectedRow);
    }
    
    
    public DPeptideInstance getSelectedPeptideInstance() {
        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // Retrieve ProteinPTMSite selected
        PeptidesOfPtmSiteTableModel tableModel = (PeptidesOfPtmSiteTableModel) compoundTableModel.getBaseModel();
        
        // Retrieve Selected Row
        int selectedRow = getSelectedTableModelRow();    
        
        return tableModel.getSelectedPeptideInstance(selectedRow);
    }
    
    private int getSelectedTableModelRow(){
        
        // Retrieve Selected Row
        int selectedRow = m_peptidesPtmSiteTable.getSelectedRow();
        
         // nothing selected
        if (selectedRow == -1) {
            return -1;
        }

        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // convert according to the sorting
        selectedRow = m_peptidesPtmSiteTable.convertRowIndexToModel(selectedRow);
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);
        return selectedRow;
    }
    
    public void setData(PTMSite peptidesPTMSite) {
        setData(peptidesPTMSite, null);
    }
    
    public void setData(PTMSite peptidesPTMSite, DPeptideInstance pepInst) {

        PeptidesOfPtmSiteTableModel model = ((PeptidesOfPtmSiteTableModel) ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()).getBaseModel());
        model.setData(peptidesPTMSite, m_displayPeptidesMatches, pepInst);
        // select the first row
        if ((peptidesPTMSite != null) && ((!m_displayPeptidesMatches) || (pepInst != null)) ) {
            m_peptidesPtmSiteTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(m_peptidesPtmSiteTable.getModel().getRowCount());
        }

        m_infoToggleButton.updateInfo();
        m_peptidesPtmSiteTable.setSortable(true);
    }

    private void initComponents() {

        setLayout(new BorderLayout());

        final JPanel proteinPTMSitePanel = createPeptidesPTMSitePanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                proteinPTMSitePanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(proteinPTMSitePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), new Integer(JLayeredPane.PALETTE_LAYER + 1));

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_exportButton = new ExportButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel()), "Peptides", m_peptidesPtmSiteTable);
        m_searchToggleButton = new SearchToggleButton(m_peptidesPtmSiteTable, m_peptidesPtmSiteTable, ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()));        
        m_settingsButton = new SettingsButton(((ProgressInterface) m_peptidesPtmSiteTable.getModel()), m_peptidesPtmSiteTable);
        m_infoToggleButton = new InfoToggleButton(m_peptidesPtmSiteTable, m_peptidesPtmSiteTable);
        m_filterButton = new FilterButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel())) {
            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
                m_infoToggleButton.updateInfo();
            }
        };

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel())) {

            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getUserName(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i != null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
            }
        };

        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        toolbar.add(m_infoToggleButton);

        return toolbar;
    }

    private JPanel createPeptidesPTMSitePanel() {

        JPanel peptidesPTMSitePanel = new JPanel();
        peptidesPTMSitePanel.setBounds(0, 0, 500, 400);
        peptidesPTMSitePanel.setLayout(new BorderLayout());

        //--- Create Internal Pane
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        m_peptidesPtmSiteScrollPane = new JScrollPane();

        m_peptidesPtmSiteTable = new PeptidesPTMSiteTable();
        PeptidesOfPtmSiteTableModel model = new PeptidesOfPtmSiteTableModel();
        m_peptidesPtmSiteTable.setModel(new CompoundTableModel(model, true));

        // hide the id column
        m_peptidesPtmSiteTable.getColumnExt(m_peptidesPtmSiteTable.convertColumnIndexToView(PeptidesOfPtmSiteTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        //m_ptmProteinSiteTable.displayColumnAsPercentage(PtmProtenSiteTableModel_V2.COLTYPE_PEPTIDE_SCORE);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_peptidesPtmSiteTable.getModel());
        m_peptidesPtmSiteTable.setRowSorter(sorter);

        sorter.setComparator(PeptidesOfPtmSiteTableModel.COLTYPE_MODIFICATION_LOC, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                int pos1;
                if (s1.compareTo("N-term") == 0) {
                    pos1 = -1;
                } else if (s1.compareTo("C-term") == 0) {
                    pos1 = Integer.MAX_VALUE;
                } else {
                    pos1 = Integer.valueOf(s1);
                }
                int pos2;
                if (s2.compareTo("N-term") == 0) {
                    pos2 = 0;
                } else if (s2.compareTo("C-term") == 0) {
                    pos2 = Integer.MAX_VALUE;
                } else {
                    pos2 = Integer.valueOf(s2);
                }

                return pos2 - pos1;
            }

        });

        sorter.setComparator(PeptidesOfPtmSiteTableModel.COLTYPE_PROTEIN_LOC, new Comparator<Integer>() {

            @Override
            public int compare(Integer s1, Integer s2) {
                return s1 - s2;
            }

        });

        m_markerContainerPanel = new MarkerContainerPanel(m_peptidesPtmSiteScrollPane, m_peptidesPtmSiteTable);

        m_peptidesPtmSiteScrollPane.setViewportView(m_peptidesPtmSiteTable);
        m_peptidesPtmSiteTable.setFillsViewportHeight(true);
        m_peptidesPtmSiteTable.setViewport(m_peptidesPtmSiteScrollPane.getViewport());

//
//        m_countModificationTextField = new JTextField();
//        m_countModificationTextField.setEditable(false);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

//        c.gridx++;
//        c.gridy++;
//        c.weightx = 1;
//        c.gridwidth = 1;
//        internalPanel.add(m_countModificationTextField, c);
//        
        JToolBar toolbar = initToolbar();
        peptidesPTMSitePanel.add(toolbar, BorderLayout.WEST);
        peptidesPTMSitePanel.add(internalPanel, BorderLayout.CENTER);

        return peptidesPTMSitePanel;
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
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_peptidesPtmSiteTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_peptidesPtmSiteTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peptidesPtmSiteTable;
    }

    @Override
    public void setLoading(int id) {
        //Nothing to Do
    }

    @Override
    public void setLoading(int id, boolean calculating) {
        //Nothing to Do
    }

    @Override
    public void setLoaded(int id) {
        //Nothing to Do
    }

    private class PeptidesPTMSiteTable extends DecoratedMarkerTable implements  CrossSelectionInterface, InfoInterface, ProgressInterface {

        public PeptidesPTMSiteTable() {
            super();
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
            
            if(m_displayPeptidesMatches) {
                m_dataBox.propagateDataChanged(DPeptideMatch.class);
                m_dataBox.propagateDataChanged(MsQueryInfoRset.class);
            } else {
                m_dataBox.propagateDataChanged(PTMSite.class);            
            }
        }

        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        
        private boolean selectionWillBeRestored = false;


//        @Override
//        public void importSelection(HashSet selectedData) {

//            ListSelectionModel selectionTableModel = getSelectionModel();
//            selectionTableModel.clearSelection();
//
//            int firstRow = -1;
//            PtmProtenSiteTableModel_V2 model = (PtmProtenSiteTableModel_V2) ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()).getBaseModel();
//            int rowCount = model.getRowCount();
//            for (int i = 0; i < rowCount; i++) {
//                Object v = model.getValueAt(i, PtmProtenSiteTableModel_V2.COLTYPE_PROTEIN_ID);
//                if (selectedData.remove(v)) {
//                    if (firstRow == -1) {
//                        firstRow = i;
//                    }
//                    selectionTableModel.addSelectionInterval(i, i);
//                    BookmarkMarker marker = new BookmarkMarker(i);
//                    m_markerContainerPanel.addMarker(marker);
//                    if (selectedData.isEmpty()) {
//                        break;
//                    }
//                }
//            }
//
//            // scroll to the first row
//            if (firstRow != -1) {
//                final int row = firstRow;
//                scrollToVisible(row);
//            }
//
//            throw new RuntimeException(" HA HA NEEDED !!");
//        }

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(PeptidesPTMSitePanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;

        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
            StringBuilder sb = new StringBuilder(count);
            if(count > 1) {
                sb.append(" Peptides ");
            } else {
                sb.append(" Peptide ");
            }
            
            if(m_displayPeptidesMatches)
                sb.append("Match ");
            
            sb.append("for PTM Site");
            
            return sb.toString();
        }

        @Override
        public boolean isLoaded() {
            return true;            
        }

        @Override
        public int getLoadingPercentage() {
            return 100;            
        }

    }

}
