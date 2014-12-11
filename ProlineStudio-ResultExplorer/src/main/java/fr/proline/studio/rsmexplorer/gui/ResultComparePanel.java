package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.comparedata.CompareTableModel;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.table.DecoratedMarkerTable;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 *
 * @author JM235353
 */
public class ResultComparePanel extends JPanel implements DataBoxPanelInterface, CompareDataProviderInterface {

    private AbstractDataBox m_dataBox;


    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    
    private JScrollPane m_dataScrollPane = new JScrollPane();
    private DataTable m_dataTable;
    private MarkerContainerPanel m_markerContainerPanel;
    
    public ResultComparePanel() {
        
        setLayout(new BorderLayout());
        setBounds(0, 0, 500, 400);


        JPanel internalPanel = initComponents();
        add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        add(toolbar, BorderLayout.WEST);
    }
    
    private JPanel initComponents() {


        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // create objects
        m_dataScrollPane = new JScrollPane();

        m_dataTable = new DataTable();
        m_dataTable.setModel(new CompareTableModel(null));
        


        m_markerContainerPanel = new MarkerContainerPanel(m_dataScrollPane, m_dataTable);

        m_dataScrollPane.setViewportView(m_dataTable);
        m_dataTable.setFillsViewportHeight(true);
        m_dataTable.setViewport(m_dataScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);


        return internalPanel;

    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);


        m_filterButton = new FilterButton(((CompareTableModel) m_dataTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };  // does not work for the moment, finish it later
        m_exportButton = new ExportButton(((CompareTableModel) m_dataTable.getModel()), "Data", m_dataTable);


        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);

        return toolbar;
    }
    
    
    public void setData(CompareDataInterface dataInterface) {
        ((CompareTableModel) m_dataTable.getModel()).setDataInterface(dataInterface);
        m_markerContainerPanel.setMaxLineNumber(dataInterface.getRowCount());
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

    @Override
    public void setLoading(int id) {}

    @Override
    public void setLoading(int id, boolean calculating) {}

    @Override
    public void setLoaded(int id) {}

    @Override
    public CompareDataInterface getCompareDataInterface() {
        return ((CompareTableModel) m_dataTable.getModel()).getDataInterface();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_dataTable;
    }

    
    private class DataTable extends DecoratedMarkerTable {

        public DataTable() {

        }

        public void dataUpdated() {

        }

    }
}
