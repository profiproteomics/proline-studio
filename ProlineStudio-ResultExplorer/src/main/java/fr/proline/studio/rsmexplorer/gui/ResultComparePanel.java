package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.comparedata.CompareTableModel;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author JM235353
 */
public class ResultComparePanel extends JPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;


    private FilterButtonV2 m_filterButton;
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
        m_dataTable.setModel(new CompoundTableModel(null, true));
        


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


        m_filterButton = new FilterButtonV2((((CompoundTableModel) m_dataTable.getModel()))) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };  // does not work for the moment, finish it later
        m_exportButton = new ExportButton(((ProgressInterface) m_dataTable.getModel()), "Data", m_dataTable);


        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);

        return toolbar;
    }
    
    
    public void setData(GlobalTableModelInterface dataInterface) {

        CompoundTableModel model = new CompoundTableModel(dataInterface, true);
        m_dataTable.setModel(model);
        m_filterButton.setModelFilterInterface(model);
        m_exportButton.setProgressInterface(model);

        m_markerContainerPanel.setMaxLineNumber(model.getRowCount());
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
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return ((CompoundTableModel) m_dataTable.getModel()).getBaseModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_dataTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_dataTable;
    }

    
    private class DataTable extends LazyTable {

        public DataTable() {
            super(m_dataScrollPane.getVerticalScrollBar());
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
        public void dataUpdated() {

        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        @Override
        public boolean isLoaded() {
            return ((CompoundTableModel) getModel()).isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return ((CompoundTableModel) getModel()).getLoadingPercentage();
        }

    }
}
