package fr.proline.studio.rsmexplorer.gui.spectrum;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.JXTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class RsetPeptideSpectrumValuesPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;
    private AbstractDataBox m_dataBox;

    private DPeptideMatch m_previousPeptideMatch = null;

    private String m_spectrumTitle;

    private JScrollPane m_scrollPane;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;

    private SpectrumValuesTable m_spectrumValuesTable = null;

    /**
     * Creates new form RsetPeptideSpectrumPanel
     */
    public RsetPeptideSpectrumValuesPanel() {
        setLayout(new BorderLayout());

        JPanel spectrumValuesPanel = createSpectrumValuesPanel();

        add(spectrumValuesPanel, BorderLayout.CENTER);

    }

    private JPanel createSpectrumValuesPanel() {
        JPanel spectrumValuesPanel = new JPanel();
        spectrumValuesPanel.setBounds(0, 0, 500, 400);

        spectrumValuesPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        spectrumValuesPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        spectrumValuesPanel.add(toolbar, BorderLayout.WEST);

        return spectrumValuesPanel;

    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_spectrumValuesTable = new SpectrumValuesTable();
        m_spectrumValuesTable.setModel(new CompoundTableModel(new SpectrumValuesTableModel(), true));

        m_scrollPane = new JScrollPane();
        m_scrollPane.setViewportView(m_spectrumValuesTable);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(m_scrollPane, c);

        return internalPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_spectrumValuesTable.getModel()), m_spectrumValuesTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_spectrumValuesTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }

        };
        m_exportButton = new ExportButton(((ProgressInterface) m_spectrumValuesTable.getModel()), "Spectrum Values", m_spectrumValuesTable);
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_spectrumValuesTable.getModel())) {

            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i != null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
            }
        };

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);

        return toolbar;
    }

    public void setData(DPeptideMatch peptideMatch) {

        if (peptideMatch == m_previousPeptideMatch) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;

        retrieveSpectrumData(peptideMatch);

    }

    private void retrieveSpectrumData(DPeptideMatch pm) {

        if (pm == null) {
            return;
        }

        Peptide p = pm.getPeptide();
        if (p == null) {
            return;
        }

        DMsQuery msQuery = pm.isMsQuerySet() ? pm.getMsQuery() : null;
        if (msQuery == null) {
            return;
        }

        DSpectrum spectrum = msQuery.isSpectrumFullySet() ? msQuery.getDSpectrum() : null;
        if (spectrum == null) {
            return;
        }

        byte[] intensityByteArray = spectrum.getIntensityList();
        byte[] massByteArray = spectrum.getMozList();

        if ((intensityByteArray == null) || (massByteArray == null)) {
            // should not happen
            return;
        }

        ByteBuffer intensityByteBuffer = ByteBuffer.wrap(intensityByteArray).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer intensityFloatBuffer = intensityByteBuffer.asFloatBuffer();
        double[] intensityDoubleArray = new double[intensityFloatBuffer.remaining()];
        for (int i = 0; i < intensityDoubleArray.length; i++) {
            intensityDoubleArray[i] = (double) intensityFloatBuffer.get();
        }

        ByteBuffer massByteBuffer = ByteBuffer.wrap(massByteArray).order(ByteOrder.LITTLE_ENDIAN);
        DoubleBuffer massDoubleBuffer = massByteBuffer.asDoubleBuffer();
        double[] massDoubleArray = new double[massDoubleBuffer.remaining()];
        for (int i = 0; i < massDoubleArray.length; i++) {
            massDoubleArray[i] = massDoubleBuffer.get();
        }

        int size = intensityDoubleArray.length;
        if (size != massDoubleArray.length) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug("Intensity and Mass List have different size");
            return;
        }

        // Set title
        String title = "Query " + msQuery.getInitialId() + " - " + p.getSequence();

        // set the spectrum title
        m_spectrumTitle = spectrum.getTitle();

        ((SpectrumValuesTableModel)((CompoundTableModel) m_spectrumValuesTable.getModel()).getBaseModel()).setData(massDoubleArray,intensityDoubleArray);
        
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
    public void addSingleValue(Object v) {
        getGlobalTableModelInterface().addSingleValue(v);
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_spectrumValuesTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_spectrumValuesTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class SpectrumValuesTable extends DecoratedTable {

        public SpectrumValuesTable() {

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

    public static class SpectrumValuesTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

        public static final int COLTYPE_VALUE_ID = 0;
        public static final int COLTYPE_PEPTIDE_M_Z = 1;
        public static final int COLTYPE_PEPTIDE_INTENSITY = 2;

        private static final String[] m_columnNames = {"Id", "m/z", "Intensity"};
        private static final String[] m_columnTooltips = {"Id", "m/z", "Intensity"};

        private double[] m_massDoubleArray = null;
        private double[] m_intensityDoubleArray = null;

        private String m_modelName;

        public void setData(double[] massDoubleArray ,double[] intensityDoubleArray) {
            m_massDoubleArray = massDoubleArray;
            m_intensityDoubleArray = intensityDoubleArray;

            fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }

        @Override
        public String getToolTipForHeader(int col) {
            return m_columnTooltips[col];
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLTYPE_VALUE_ID:
                    return Integer.class;
                case COLTYPE_PEPTIDE_M_Z:
                case COLTYPE_PEPTIDE_INTENSITY:
                    return Double.class;

            }
            return null; // should not happen
        }

        @Override
        public int getRowCount() {
            if (m_massDoubleArray == null) {
                return 0;
            }

            return m_massDoubleArray.length;
        }

        @Override
        public Object getValueAt(int row, int col) {

            switch (col) {
                case COLTYPE_VALUE_ID:
                    return row;
                case COLTYPE_PEPTIDE_M_Z: {
                    return m_massDoubleArray[row];
                }
                case COLTYPE_PEPTIDE_INTENSITY: {
                    return m_intensityDoubleArray[row];
                }
            }
            return null; // should never happen
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

            filtersMap.put(COLTYPE_VALUE_ID, new IntegerFilter(getColumnName(COLTYPE_VALUE_ID), null, COLTYPE_VALUE_ID));

            filtersMap.put(COLTYPE_PEPTIDE_M_Z, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_M_Z), null, COLTYPE_PEPTIDE_M_Z));
            filtersMap.put(COLTYPE_PEPTIDE_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_INTENSITY), null, COLTYPE_PEPTIDE_INTENSITY));

        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public String getDataColumnIdentifier(int columnIndex) {
            return getColumnName(columnIndex);
        }

        @Override
        public Class getDataColumnClass(int columnIndex) {
            return getColumnClass(columnIndex);
        }

        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            return getValueAt(rowIndex, columnIndex);
        }

        @Override
        public int[] getKeysColumn() {
            int[] keys = {COLTYPE_VALUE_ID};
            return keys;
        }

        @Override
        public void setName(String name) {
            m_modelName = name;
        }

        @Override
        public String getName() {
            return m_modelName;
        }

        @Override
        public int getInfoColumn() {
            return COLTYPE_VALUE_ID;
        }

        @Override
        public Map<String, Object> getExternalData() {
            return null;
        }

        @Override
        public PlotInformation getPlotInformation() {
            return null;
        }

        @Override
        public Long getTaskId() {
            return -1l; // not used
        }

        @Override
        public LazyData getLazyData(int row, int col) {
            return null; // not used
        }

        @Override
        public void givePriorityTo(Long taskId, int row, int col) {
            // not used
        }

        @Override
        public void sortingChanged(int col) {
            return; // not used
        }

        @Override
        public int getSubTaskId(int col) {
            return -1; // not used
        }

        @Override
        public PlotType getBestPlotType() {
            return null;
        }

        @Override
        public int getBestXAxisColIndex(PlotType plotType) {
            return -1;
        }

        @Override
        public int getBestYAxisColIndex(PlotType plotType) {
            return -1;
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return null;
        }

        @Override
        public String getExportColumnName(int col) {
            return getColumnName(col);
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {

            if (m_rendererMap.containsKey(col)) {
                return m_rendererMap.get(col);
            }

            TableCellRenderer renderer = null;
            switch (col) {
                case COLTYPE_VALUE_ID:
                    renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                    break;
                case COLTYPE_PEPTIDE_M_Z:
                case COLTYPE_PEPTIDE_INTENSITY:
                    renderer = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, false);
                    break;

            }
            m_rendererMap.put(col, renderer);
            return renderer;

        }
        private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

        @Override
        public GlobalTableModelInterface getFrozzenModel() {
            return this;
        }
        
        @Override
        public ArrayList<ExtraDataType> getExtraDataTypes() {
            return null;
        }

        @Override
        public Object getValue(Class c) {
            return getSingleValue(c);
        }

        @Override
        public Object getValue(Class c, int row) {
            return null;
        }

    }

}
