/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.utils.GlobalValues;
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
 */
public class XICComparePeptideTableModel implements ExtendedTableModelInterface,TableModel, BestGraphicsInterface {

    public static final int COLTYPE_QC_ID = 0;
    public static final int COLTYPE_QC_NAME = 1;
    public static final int COLTYPE_ABUNDANCE = 2;
    public static final int COLTYPE_RAW_ABUNDANCE = 3;
    public static final int COLTYPE_PSM = 4;
    protected static final String[] m_columnNames = {"Id", "Quant. Channel", "Abundance", "Raw Abundance", "Pep. match count"};
    protected static final String[] m_columnNames_SC = {"Id", "Quant. Channel",  "[W. SC]", "Specific SC", "Basic SC"};
    protected final ArrayList<Integer> m_colUsed = new ArrayList<>();

    private DMasterQuantPeptide m_quantPeptide = null;
    protected DQuantitationChannel[] m_quantChannels = null;

    protected String m_modelName;

    protected boolean m_isSC;
    protected DDatasetType.QuantitationMethodInfo m_quantMethodInfo;
    protected HashMap<Class, Object> m_extraValues = null;
    private boolean m_isSelected;

    public XICComparePeptideTableModel() {
        m_quantMethodInfo = DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION;
        m_isSC = false;
        setColUsed();
    }

    protected void setColUsed(){
        m_colUsed.clear();
        if(m_isSC){
            m_colUsed.add(COLTYPE_QC_ID);
            m_colUsed.add(COLTYPE_QC_NAME);
            m_colUsed.add(COLTYPE_RAW_ABUNDANCE);
            m_colUsed.add(COLTYPE_PSM);
        }else{
            m_colUsed.add(COLTYPE_QC_ID);
            m_colUsed.add(COLTYPE_QC_NAME);
            m_colUsed.add(COLTYPE_ABUNDANCE);
            m_colUsed.add(COLTYPE_RAW_ABUNDANCE);
            m_colUsed.add(COLTYPE_PSM);
        }
    }

    public void setSelected(boolean isSelected) {
        m_isSelected = isSelected;
    }

    public void setData(DQuantitationChannel[] quantChannels, DMasterQuantPeptide peptide, DDatasetType.QuantitationMethodInfo quantitationMethodInfo) {
        this.m_quantChannels = quantChannels;
        this.m_quantPeptide = peptide;
        this.m_quantMethodInfo = quantitationMethodInfo;
        m_isSC = m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING);
        setColUsed();
        fireTableDataChanged();
    }

    /**
     * copy of javax.swing.table.AbstractTableModel.java
     *
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
        return m_colUsed.size();
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
        if (m_quantPeptide.getPeptideInstance() != null && m_quantPeptide.getPeptideInstance().getPeptide() != null) {
            Peptide peptide = m_quantPeptide.getPeptideInstance().getPeptide();
            String sequence =  PeptideRenderer.constructPeptideDisplay(peptide).replaceAll(GlobalValues.HTML_TAG_BEGIN, "").replaceAll(GlobalValues.HTML_TAG_END, "");
            StringBuilder sb = new StringBuilder(sequence);
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

        int realCol = m_colUsed.get(col);
        // Retrieve QuantChannel
        DQuantitationChannel qc = m_quantChannels[row];
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantPeptide> quantPeptideByQchIds = m_quantPeptide.getQuantPeptideByQchIds();
        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qc.getId());
        switch (realCol) {
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

                if(m_isSC) {
                    return quantPeptide.getRawAbundance().isNaN() ? null : quantPeptide.getRawAbundance();
                } else {
                    return quantPeptide.getAbundance().isNaN() ? null : quantPeptide.getAbundance();
                }
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
        return m_isSC ? m_columnNames_SC[m_colUsed.get(columnIndex)] :  m_columnNames[m_colUsed.get(columnIndex)];

    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        int realColumnIndex = m_colUsed.get(columnIndex);
        switch (realColumnIndex) {
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
                if(m_isSC)
                    return Integer.class;
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

    @Override
    public PlotType getBestPlotType() {
        return PlotType.LINEAR_PLOT;
    }

    protected int convertColToColUsed(int col) {
        for (int i = 0; i < m_colUsed.size(); i++) {
            if (col == m_colUsed.get(i)) {
                return i;
            }
        }
        return -1; // should not happen
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        switch (plotType) {
            case LINEAR_PLOT: {
                int[] cols = new int[2];
                cols[0] = convertColToColUsed(COLTYPE_QC_NAME);
                if(m_isSC){
                    cols[1] = convertColToColUsed(COLTYPE_RAW_ABUNDANCE);
                } else {
                    cols[1] = convertColToColUsed(COLTYPE_ABUNDANCE);
                }
                return cols;
            }
        }
        return null;
    }
}
