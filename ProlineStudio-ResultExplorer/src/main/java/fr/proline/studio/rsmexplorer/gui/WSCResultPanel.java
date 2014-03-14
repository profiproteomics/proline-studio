package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dpm.task.SpectralCountTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import fr.proline.studio.rsmexplorer.gui.model.WSCProteinTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.BooleanRenderer;
import fr.proline.studio.search.AbstractSearch;
import fr.proline.studio.search.SearchFloatingPanel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.utils.LazyTable;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Panel for Protein Matches
 * @author JM235353
 */
public class WSCResultPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private SpectralCountTask.WSCResultData m_weightedSCResult = null;
   
    private JTextField m_rsmRefNameField;
    private ProteinTable m_proteinTable;
    private JScrollPane m_scrollPane;
    
    private SearchFloatingPanel m_searchPanel;
    private JToggleButton m_searchToggleButton;
    private Search m_search = null;
    
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;    
    
    /**
     * Creates new form RsmProteinsOfProteinSetPanel
     */
    public WSCResultPanel() {
        initComponents();

        TableColumn accColumn = m_proteinTable.getColumnModel().getColumn(WSCProteinTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", WSCProteinTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        m_proteinTable.addMouseListener(renderer);

    }



    public void setData(SpectralCountTask.WSCResultData scResult) {

        if (scResult == m_weightedSCResult) {
            return;
        }
        m_weightedSCResult = scResult;

        if (m_weightedSCResult == null) {
            clearData();
            return;
        }


        // Modify protein description
        m_rsmRefNameField.setText(scResult.getDataSetReference().getName());


        // Modify the Model
        ((WSCProteinTableModel) m_proteinTable.getModel()).setData(scResult);



    }

    private void clearData() {
        m_rsmRefNameField.setText("");
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
        
        m_rsmRefNameField = new javax.swing.JTextField();
        m_rsmRefNameField.setEditable(false);
        m_rsmRefNameField.setBackground(Color.white);
        
        m_scrollPane = new javax.swing.JScrollPane();
        m_proteinTable = new ProteinTable();

        m_proteinTable.setModel(new WSCProteinTableModel(m_proteinTable));
        m_scrollPane.setViewportView(m_proteinTable);
        
                
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(m_rsmRefNameField, c);
        
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(m_scrollPane, c);
        
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

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        
        return toolbar;
    }


    private class ProteinTable extends LazyTable {

        public ProteinTable() {
            super(m_scrollPane.getVerticalScrollBar());
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(Float.class)) ) );
            setDefaultRenderer(Boolean.class, new BooleanRenderer());

        }
        
        public boolean selectProtein(Integer row) {
            WSCProteinTableModel tableModel = (WSCProteinTableModel) getModel();

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
                        checkLoopIndex =  searchIndex;
                    }
                }
                
            } else {
                previousSearch = searchText;
                searchIndex = -1;

                String regex = wildcardToRegex(searchText);
                
                WSCProteinTableModel model = ((WSCProteinTableModel) m_proteinTable.getModel());
                
                proteinNamesRow.clear();
                for (int i=0;i<model.getRowCount(); i++) {
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
}
