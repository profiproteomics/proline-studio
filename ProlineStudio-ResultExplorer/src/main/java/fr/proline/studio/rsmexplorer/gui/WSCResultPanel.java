package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dpm.data.SpectralCountResultData;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.JCheckBoxList;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.WSCProteinTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.BooleanRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.search.AbstractSearch;
import fr.proline.studio.search.SearchFloatingPanel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.LazyTable;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.windows.WindowManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

/**
 * Panel for Protein Matches
 *
 * @author JM235353
 */
public class WSCResultPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private SpectralCountResultData m_weightedSCResult = null;
    private ProteinTable m_proteinTable;
    private JScrollPane m_scrollPane;
    private SearchFloatingPanel m_searchPanel;
    private JToggleButton m_searchToggleButton;
    private Search m_search = null;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;    
    private JButton m_columnVisibilityButton;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
    /**
     * Creates new form RsmProteinsOfProteinSetPanel
     */
    public WSCResultPanel() {
        initComponents();

        TableColumn accColumn = m_proteinTable.getColumnModel().getColumn(WSCProteinTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", WSCProteinTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        m_proteinTable.addMouseListener(renderer);


        //TODO
        List<TableColumn> columns = m_proteinTable.getColumns(true);
        ((TableColumnExt) columns.get(0)).setVisible(false);

        
    }

    public void setData(SpectralCountResultData scResult) {

        if (scResult == m_weightedSCResult) {
            return;
        }
        m_weightedSCResult = scResult;

        if (m_weightedSCResult == null) {
            clearData();
            return;
        }

        // Modify the Model
        ((WSCProteinTableModel) m_proteinTable.getModel()).setData(scResult);

        // allow to change column visibility
        m_columnVisibilityButton.setEnabled(true);
        
        // update the number of lines
        m_markerContainerPanel.setMaxLineNumber(((WSCProteinTableModel) m_proteinTable.getModel()).getRowCount());
        

    }

    private void clearData() {
        ((WSCProteinTableModel) m_proteinTable.getModel()).setData(null);

    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {

        setLayout(new BorderLayout());

        m_search = new Search();
        m_searchPanel = new SearchFloatingPanel(m_search);
        final JPanel spectralCountPanel = createSpectralCountPanel();
        m_searchPanel.setToggleButton(m_searchToggleButton);

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                spectralCountPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(spectralCountPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_searchPanel, JLayeredPane.PALETTE_LAYER);

    }

    private JPanel createSpectralCountPanel() {

        JPanel spectralCountPanel = new JPanel();
        spectralCountPanel.setBounds(0, 0, 500, 400);
        spectralCountPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        spectralCountPanel.add(toolbar, BorderLayout.WEST);
        spectralCountPanel.add(internalPanel, BorderLayout.CENTER);


        return spectralCountPanel;
    }

    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_scrollPane = new javax.swing.JScrollPane();
        m_proteinTable = new ProteinTable();

        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane,  m_proteinTable);
        
        m_proteinTable.setModel(new WSCProteinTableModel(m_proteinTable));
        m_scrollPane.setViewportView(m_proteinTable);
        m_proteinTable.setViewport(m_scrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_searchPanel);
        toolbar.add(m_searchToggleButton);

        m_filterButton = new FilterButton(((WSCProteinTableModel) m_proteinTable.getModel()));

        m_exportButton = new ExportButton(((WSCProteinTableModel) m_proteinTable.getModel()), "Spectral Counts", m_proteinTable);

        
        m_columnVisibilityButton = new JButton();
        m_columnVisibilityButton.setIcon(IconManager.getIcon(IconManager.IconType.COLUMNS_VISIBILITY));
        m_columnVisibilityButton.setToolTipText("Hide/Show Columns...");
        m_columnVisibilityButton.setEnabled(false);
        m_columnVisibilityButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ColumnsVisibilityDialog dialog = new ColumnsVisibilityDialog(WindowManager.getDefault().getMainWindow(), m_proteinTable, (WSCProteinTableModel) m_proteinTable.getModel() );
                dialog.setLocation(m_columnVisibilityButton.getLocationOnScreen().x +m_columnVisibilityButton.getWidth(), m_columnVisibilityButton.getLocationOnScreen().y + m_columnVisibilityButton.getHeight());
                dialog.setVisible(true);
            }
        });
        
        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_columnVisibilityButton);
        
        return toolbar;
    }

    private class ProteinTable extends LazyTable implements ExportColumnTextInterface {

        public ProteinTable() {
            super(m_scrollPane.getVerticalScrollBar());
            setDefaultRenderer(Float.class, new FloatRenderer(new DefaultRightAlignRenderer(getDefaultRenderer(Float.class))));
            setDefaultRenderer(Boolean.class, new BooleanRenderer());

        }

        public boolean selectProtein(Integer row) {

            // must convert row index if there is a sorting
            row = convertRowIndexToView(row);

            // select the row
            getSelectionModel().setSelectionInterval(row, row);

            // scroll to the row
            scrollRowToVisible(row);

            return true;
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
        public void sortingChanged(int col) {
            m_search.reinitSearch();
        }

        @Override
        public boolean isLoaded() {
            return true; // not used
        }

        @Override
        public int getLoadingPercentage() {
            return 0; // not used
        }

        @Override
        public String getExportColumnName(int col) {
            return ((WSCProteinTableModel) m_proteinTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }
    }

    private class Search extends AbstractSearch {

        private String previousSearch = "";
        private int searchIndex = 0;
        private ArrayList<Integer> proteinNamesRow = new ArrayList<>();

        @Override
        public void reinitSearch() {
            if (proteinNamesRow.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((WSCProteinTableModel) m_proteinTable.getModel()).sortAccordingToModel(proteinNamesRow);
        }

        @Override
        public void doSearch(String text) {
            final String searchText = text.trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {

                int checkLoopIndex = -1;
                while (true) {
                    // search already done, display next result
                    searchIndex++;
                    if (searchIndex >= proteinNamesRow.size()) {
                        searchIndex = 0;
                    }

                    if (checkLoopIndex == searchIndex) {
                        break;
                    }

                    if (!proteinNamesRow.isEmpty()) {
                        boolean found = m_proteinTable.selectProtein(proteinNamesRow.get(searchIndex));
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

            } else {
                previousSearch = searchText;
                searchIndex = -1;

                String regex = wildcardToRegex(searchText);

                WSCProteinTableModel model = ((WSCProteinTableModel) m_proteinTable.getModel());

                proteinNamesRow.clear();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String name = (String) model.getValueAt(i, WSCProteinTableModel.COLTYPE_PROTEIN_NAME);
                    if (name.matches(regex)) {
                        proteinNamesRow.add(i);
                    }
                }

                if (!proteinNamesRow.isEmpty()) {

                    model.sortAccordingToModel(proteinNamesRow);

                    int checkLoopIndex = -1;
                    while (true) {
                        // search already done, display next result
                        searchIndex++;
                        if (searchIndex >= proteinNamesRow.size()) {
                            searchIndex = 0;
                        }

                        if (checkLoopIndex == searchIndex) {
                            break;
                        }

                        if (!proteinNamesRow.isEmpty()) {
                            boolean found = m_proteinTable.selectProtein(proteinNamesRow.get(searchIndex));
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


            }

        }
    }
    
    public class ColumnsVisibilityDialog extends DefaultDialog {

        private JCheckBoxList m_rsmList;
        private JCheckBoxList m_spectralCountList;
        
        public ColumnsVisibilityDialog(Window parent, ProteinTable table, WSCProteinTableModel proteinTableModel) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);

            // hide default and help buttons
            setButtonVisible(BUTTON_DEFAULT, false);
            setButtonVisible(BUTTON_HELP, false);

            setStatusVisible(false);

            setResizable(true);

            setTitle("Select Columns to Display");

            // Prepare data for RSM
            List<String> rsmList = new ArrayList<>();
            List<Boolean> visibilityRsmList = new ArrayList<>();

            // Prepare data for different column types
            int nbTypes = proteinTableModel.getByRsmCount();
            List<String> typeList = new ArrayList<>();
            List<Boolean> visibilityTypeList = new ArrayList<>();
            boolean[] visibilityTypeArray = new boolean[nbTypes];
            for (int i=0;i<nbTypes;i++) {
                visibilityTypeArray[i] = false;
            }
            
            
            List<TableColumn> columns = table.getColumns(true);

            int rsmCount = proteinTableModel.getRsmCount();
            for (int i = 0; i < rsmCount; i++) {
                int start = proteinTableModel.getColumStart(i);
                int stop = proteinTableModel.getColumStop(i);

                boolean rsmVisible = false;
                for (int j = start; j <= stop; j++) {
                    boolean columnVisible = ((TableColumnExt) columns.get(j)).isVisible();
                    if (columnVisible) {
                        rsmVisible = true;
                        int type = j-start;
                        visibilityTypeArray[type] |= columnVisible;
                    }

                    
                }

                String rsmName = proteinTableModel.getRsmName(i);
                rsmList.add(rsmName);
                visibilityRsmList.add(rsmVisible);

            }
            
            for (int i = 0; i < nbTypes; i++) {
                String name = proteinTableModel.getByRsmColumnName(i);
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

            JLabel idSummaryLabel = new JLabel("Identification Summaries");
            JLabel informationLabel = new JLabel("Information");
            
            JSeparator separator = new JSeparator(JSeparator.VERTICAL);

            JScrollPane rsmScrollPane = new JScrollPane();
            m_rsmList = new JCheckBoxList(rsmList, visibilityList);
            rsmScrollPane.setViewportView(m_rsmList);

            JScrollPane spectralCountScrollPane = new JScrollPane();
            m_spectralCountList = new JCheckBoxList(typeList, visibilityTypeList); 
            spectralCountScrollPane.setViewportView(m_spectralCountList);

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
            internalPanel.add(separator, c);
            

            c.gridx++;
            c.gridheight = 1;
            c.weightx = 0;
            c.weighty = 0;
            internalPanel.add(informationLabel, c);
            
            c.gridy++;
            c.weightx = 1;
            c.weighty = 1;
            internalPanel.add(spectralCountScrollPane, c);


            return internalPanel;
        }

        /*
         * @Override public void pack() { if (m_userSetSize) { // forbid pack by
         * overloading the method return; } super.pack();
    }
         */
        @Override
        protected boolean okCalled() {

            WSCProteinTableModel model = ((WSCProteinTableModel) m_proteinTable.getModel());
            
            List<TableColumn> columns = m_proteinTable.getColumns(true);
            for (int i=1;i<columns.size();i++) {
                int rsmCur = model.getRsmNumber(i);
                int type = model.getTypeNumber(i);
                boolean visible = m_rsmList.isVisible(rsmCur) && m_spectralCountList.isVisible(type);
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
