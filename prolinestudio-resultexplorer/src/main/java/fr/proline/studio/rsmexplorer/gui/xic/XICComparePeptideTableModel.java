/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.table.LazyData;
import java.awt.Color;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author Karine XUE at CEA
 * Attention: compare wtih PeptideTableModel, delete PeptideTableModel.java in future
 */
public class XICComparePeptideTableModel implements ExtendedTableModelInterface,TableModel {

    public static final int COLTYPE_QC_ID = 0;
    public static final int COLTYPE_QC_NAME = 1;
    public static final int COLTYPE_ABUNDANCE = 2;
    public static final int COLTYPE_RAW_ABUNDANCE = 3;
    public static final int COLTYPE_PSM = 4;
    protected static final String[] m_columnNames = {"Id", "Quant. Channel", "Abundance", "Raw Abundance", "Pep. match count"};
    protected static final String[] m_columnNames_SC = {"Id", "Quant. Channel", "Abundance", "Specific SC", "Basic SC"};

    private DMasterQuantPeptide m_quantPeptide = null;
    protected DQuantitationChannel[] m_quantChannels = null;

    protected String m_modelName;

    protected boolean m_isXICMode = true;
    protected HashMap<Class, Object> m_extraValues = null;
    private boolean m_isSelected;

    public void setSelected(boolean isSelected) {
        m_isSelected = isSelected;
    }

    public void setData(DQuantitationChannel[] quantChannels, DMasterQuantPeptide peptide, boolean isXICMode) {
        this.m_quantChannels = quantChannels;
        this.m_quantPeptide = peptide;
        this.m_isXICMode = isXICMode;

        fireTableDataChanged();
    }

    /**
     * copy of javax.swing.table.AbstractTableModel.java
     *
     * @param e
     */
    public void fireTableDataChanged() {
        fireTableChanged(new TableModelEvent(this));
    }

    /**
     * copy of javax.swing.table.AbstractTableModel.java
     *
     * @param e
     */
    public void fireTableChanged(TableModelEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
            }
        }
    }

    @Override
    public int getRowCount() {
        if (m_quantChannels == null) {
            return 0;
        }

        return m_quantChannels.length;
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_isXICMode ? m_columnNames[columnIndex] : m_columnNames_SC[columnIndex];
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_QC_ID: {
                return Long.class;
            }
            case COLTYPE_QC_NAME: {
                return String.class;
            }
            case COLTYPE_ABUNDANCE: {
                return Float.class;
            }
            case COLTYPE_RAW_ABUNDANCE: {
                return Float.class;
            }
            case COLTYPE_PSM: {
                return Integer.class;
            }
        }
        return null;
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_QC_ID};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_QC_NAME;
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
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        PlotInformation plotInformation = new PlotInformation();
        plotInformation.setSelected(m_isSelected);
        if (m_quantPeptide.getPeptideInstance() != null && m_quantPeptide.getPeptideInstance().getBestPeptideMatch() != null) {
            Peptide peptide = m_quantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide();
            StringBuilder sb = new StringBuilder(peptide.getSequence());
            if (peptide.getTransientData().isPeptideReadablePtmStringLoaded() && peptide.getTransientData().getPeptideReadablePtmString() != null) {
                sb.append(" - ").append(peptide.getTransientData().getPeptideReadablePtmString().getReadablePtmString());
            }
            plotInformation.setPlotTitle(sb.toString());
        }

        if (m_quantPeptide.getSelectionLevel() < 2) {
            plotInformation.setPlotColor(Color.LIGHT_GRAY);
        }
        plotInformation.setDrawPoints(true);
        plotInformation.setDrawGap(true);
        return plotInformation;
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }

    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }

    @Override
    public PlotDataSpec getDataSpecAt(int row) {
        //m_logger.debug("########call getDataSpecAt");
        PlotDataSpec result = new PlotDataSpec();
        DQuantitationChannel qc = m_quantChannels[row];
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantPeptide> quantPeptideByQchIds = m_quantPeptide.getQuantPeptideByQchIds();
        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qc.getId());
        if (!(quantPeptide == null || quantPeptide.getRawAbundance() == null || quantPeptide.getPeptideMatchesCount() == null)) {
            int matcheCount = quantPeptide.getPeptideMatchesCount();
            //m_logger.debug(String.format("#########row %s, matchcount=%d", qc.getFullName(), matcheCount));
            if (matcheCount == 0) {
                result.setFill(PlotDataSpec.FILL.EMPTY);
            }
        }
        return result;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(DQuantitationChannel.class, true));
        list.add(new ExtraDataType(DMasterQuantPeptide.class, false));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    private void registerSingleValuesAsExtraTypes(ArrayList<ExtraDataType> extraDataTypeList) {
        if (m_extraValues == null) {
            return;
        }
        for (Class c : m_extraValues.keySet()) {
            extraDataTypeList.add(new ExtraDataType(c, false));
        }
    }

    @Override
    public Object getValue(Class c) {
        if (c.equals(DMasterQuantPeptide.class)) {
            return m_quantPeptide;
        }
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DQuantitationChannel.class)) {
            return m_quantChannels[row];
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addSingleValue(Object v) {
        if (m_extraValues == null) {
            m_extraValues = new HashMap<>();
        }
        m_extraValues.put(v.getClass(), v);
    }

    @Override
    public Object getSingleValue(Class c) {
        if (m_extraValues == null) {
            return null;
        }
        return m_extraValues.get(c);
    }

    public Object getValueAt(int row, int col) {

        // Retrieve QuantChannel
        DQuantitationChannel qc = m_quantChannels[row];
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantPeptide> quantPeptideByQchIds = m_quantPeptide.getQuantPeptideByQchIds();
        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qc.getId());
        switch (col) {
            case COLTYPE_QC_ID: {
                return qc.getId();
            }
            case COLTYPE_QC_NAME: {
                return qc.getName();
            }
            case COLTYPE_ABUNDANCE: {
                if (quantPeptide == null || quantPeptide.getAbundance() == null) {
                    return null;
                }
                return quantPeptide.getAbundance().isNaN() ? null : quantPeptide.getAbundance();
            }
            case COLTYPE_RAW_ABUNDANCE: {
                if (quantPeptide == null || quantPeptide.getRawAbundance() == null) {
                    return null;
                }
                return quantPeptide.getRawAbundance().isNaN() ? null : quantPeptide.getRawAbundance();
            }
            case COLTYPE_PSM: {
                if (quantPeptide == null || quantPeptide.getPeptideMatchesCount() == null) {
                    return null;
                }
                return quantPeptide.getPeptideMatchesCount();
            }
        }
        return null; // should never happen
    }

    //@Override
    public String getColumnName(int columnIndex) {
        return m_isXICMode ? m_columnNames[columnIndex] : m_columnNames_SC[columnIndex];

    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_QC_ID: {
                return Long.class;
            }
            case COLTYPE_QC_NAME: {
                return String.class;
            }
            case COLTYPE_ABUNDANCE: {
                return Float.class;
            }
            case COLTYPE_RAW_ABUNDANCE: {
                return Float.class;
            }
            case COLTYPE_PSM: {
                return Integer.class;
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }
    protected EventListenerList listenerList = new EventListenerList();

    //@Override
    public void addTableModelListener(TableModelListener l) {
        listenerList.add(TableModelListener.class, l);
    }

    //@Override
    public void removeTableModelListener(TableModelListener l) {
        listenerList.remove(TableModelListener.class, l);
    }
}
