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
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.WindowManager;
import fr.proline.studio.corewrapper.util.PeptideClassesUtils;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dock.gui.InfoLabel;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.*;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Table Model for PeptideInstance of a Rsm
 *
 * @author JM235353
 */
public class PeptideInstanceTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_PEPTIDE_NAME = 1;
    public static final int COLTYPE_PEPTIDE_LENGTH = 2;
    public static final int COLTYPE_PEPTIDE_PTM = 3;
    public static final int COLTYPE_PEPTIDE_PSM_SCORE = 4;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 5;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 6;
    public static final int COLTYPE_PEPTIDE_PPM = 7;
    public static final int COLTYPE_PEPTIDE_CHARGE = 8;
    public static final int COLTYPE_PEPTIDE_MISSED_CLEAVAGE = 9;
    public static final int COLTYPE_PEPTIDE_RANK = 10;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 11;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_COUNT = 12;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 13;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 14;
    public static final int COLTYPE_SPECTRUM_TITLE = 15;
    public static final int COLTYPE_PEPTIDE_MATCH_COUNT = 16;
    public static final int COLTYPE_PEPTIDE_SCORE = 17;

    private static final TableColumn[] COLUMNS = new TableColumn.Builder()
            .add(COLTYPE_PEPTIDE_ID, "Id", "Peptide Id", Long.class)
            .add(COLTYPE_PEPTIDE_NAME, "Peptide", "Peptide", DPeptideMatch.class)
            .add(COLTYPE_PEPTIDE_LENGTH,"Length", "Length", Integer.class)
            .add(COLTYPE_PEPTIDE_PTM,"PTMs", "Post Translational Modifications",  String.class)
            .add(COLTYPE_PEPTIDE_PSM_SCORE,"PSM Score", "Best PSM Score",  Float.class)
            .add(COLTYPE_PEPTIDE_CALCULATED_MASS,"Calc. Mass", "Calculated Mass", Double.class)
            .add(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ,"Exp. MoZ", "Experimental Mass to Charge Ratio", Double.class)
            .add(COLTYPE_PEPTIDE_PPM,"Ppm", "parts-per-million", Float.class)
            .add(COLTYPE_PEPTIDE_CHARGE,"Charge", "Charge",  Integer.class)
            .add(COLTYPE_PEPTIDE_MISSED_CLEAVAGE,"Missed Cl.", "Missed Cleavages", Integer.class)
            .add(COLTYPE_PEPTIDE_RANK,"Rank", "Pretty Rank", Integer.class)
            .add(COLTYPE_PEPTIDE_RETENTION_TIME,"RT", "Retention Time (min)",  Float.class)
            .add(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, "Protein Set Count", "Protein Sets Count", Integer.class)
            .add(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, "Protein Sets", "List of matching Protein Sets", String.class, true)
            .add(COLTYPE_PEPTIDE_MSQUERY,"MsQuery", "MsQuery", DMsQuery.class, true)
            .add(COLTYPE_SPECTRUM_TITLE, "Spectrum Title", "Spectrum Title", String.class, true)
            .add(COLTYPE_PEPTIDE_MATCH_COUNT, "PSM Count", "Number of Peptide Spectrum Matches matching this Peptide", Integer.class)
            .add(COLTYPE_PEPTIDE_SCORE, "Score", "Peptide Score", Float.class).build();

    private PeptideInstance[] m_peptideInstances = null;

    private String m_modelName;

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    public PeptideInstanceTableModel(LazyTable table) {
        super(table);
    }

    public PeptideInstance getPeptideInstance(int row) {

        return m_peptideInstances[row];

    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMNS[col].getName();
    }

    @Override
    public String getToolTipForHeader(int col) {
        return COLUMNS[col].getTooltip();
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        return COLUMNS[col].getType();
    }

    @Override
    public int getSubTaskId(int col) {
        switch (col) {
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
                return DatabaseLoadPeptidesInstancesTask.SUB_TASK_PROTEINSET_NAME_LIST;
            case COLTYPE_PEPTIDE_MSQUERY:
            case COLTYPE_SPECTRUM_TITLE:
                return DatabaseLoadPeptidesInstancesTask.SUB_TASK_MSQUERY;
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        if (m_peptideInstances == null) {
            return 0;
        }

        return m_peptideInstances.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Protein Set
        PeptideInstance peptideInstance = m_peptideInstances[row];
        DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();

        switch (col) {
            case COLTYPE_PEPTIDE_ID:
                return peptideInstance.getPeptide().getId();
            case COLTYPE_PEPTIDE_NAME: {
                return peptideMatch;
            }
            case COLTYPE_PEPTIDE_PSM_SCORE: {
                // Retrieve typical Peptide Match
                Float score = Float.valueOf((float) peptideMatch.getScore());
                return score;
            }
            case COLTYPE_PEPTIDE_RANK: {
                return (peptideMatch.getCDPrettyRank() == null) ? "" : peptideMatch.getCDPrettyRank();
            }
            case COLTYPE_PEPTIDE_CHARGE: {
                return peptideMatch.getCharge();
            }
            case COLTYPE_PEPTIDE_LENGTH: {
                return (peptideMatch == null) ? null : peptideMatch.getPeptide().getSequence().length();
            }
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                BigDecimal bd = new BigDecimal(peptideMatch.getExperimentalMoz());
                Float expMoz = bd.setScale(4, RoundingMode.HALF_UP).floatValue();
                return expMoz;
            }
            case COLTYPE_PEPTIDE_PPM: {
                float ppm = PeptideClassesUtils.getPPMFor(peptideMatch, peptideMatch.getPeptide());
                return Float.valueOf(ppm);
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {

                Peptide peptide = peptideMatch.getPeptide();
                BigDecimal bd = new BigDecimal(peptide.getCalculatedMass());
                Float calculatedMass = bd.setScale(4, RoundingMode.HALF_UP).floatValue();
                return calculatedMass;

            }
            case COLTYPE_PEPTIDE_MISSED_CLEAVAGE: {
                return peptideMatch.getMissedCleavage();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                return peptideInstance.getValidatedProteinSetCount();
            }
            case COLTYPE_PEPTIDE_MATCH_COUNT: {
                return peptideInstance.getPeptideMatchCount();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {

                LazyData lazyData = getLazyData(row, col);

                String[] proteinSetNames = peptideMatch.getProteinSetStringArray();
                if (proteinSetNames == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    for (int i = 0; i < proteinSetNames.length; i++) {
                        String name = proteinSetNames[i];
                        if (i < proteinSetNames.length - 1) {
                            m_sb.append(name).append(", ");
                        } else {
                            m_sb.append(name);
                        }
                    }
                    lazyData.setData(m_sb.toString());
                    m_sb.setLength(0);
                }
                return lazyData;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                return peptideMatch.getRetentionTime();
            }
            case COLTYPE_PEPTIDE_PTM: {

                Peptide peptide = peptideMatch.getPeptide();
                if (peptide == null) {
                    return null; // should not happen
                }

                boolean ptmStringLoadeed = peptide.getTransientData().isPeptideReadablePtmStringLoaded();
                if (!ptmStringLoadeed) {
                    return null; // should not happen
                }

                String ptm = "";
                PeptideReadablePtmString ptmString = peptide.getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }

                return ptm;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                LazyData lazyData = getLazyData(row, col);
                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);

                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    lazyData.setData(msQuery);
                }
                return lazyData;

            }
            case COLTYPE_SPECTRUM_TITLE: {
                LazyData lazyData = getLazyData(row, col);
                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);

                } else {
                    String spectrumTitle = peptideMatch.getMsQuery().getDSpectrum().getTitle();
                    lazyData.setData(spectrumTitle);
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_SCORE: {
              try {
                Map<String, Object> properties = peptideInstance.getProperties();
                if ((properties == null) || !properties.containsKey("score")){
                    return null;
                }
                Double score = (Double)((Map<String, Object>)properties.get("score")).get("score");
                return score.floatValue();
              } catch (Exception e) {
                  WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR,"Peptide instance property cannot be read", e);
                return null;
              }
            }
        }
        return null; // should never happen
    }
    private final StringBuilder m_sb = new StringBuilder();

    public void setData(Long taskId, PeptideInstance[] peptideInstances) {
        m_peptideInstances = peptideInstances;
        m_taskId = taskId;

        updateMinMax();

        fireTableDataChanged();

    }

    private void updateMinMax() {

        if (m_peptideInstances == null) {
            return;
        }

        if (m_scoreRenderer == null) {
            return;
        }

        float maxScore = 0;
        int size = m_peptideInstances.length;
        for (int i = 0; i < size; i++) {
            PeptideInstance peptideInstance = m_peptideInstances[i];
            float score = ((DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch()).getScore();
            if (score > maxScore) {
                maxScore = score;
            }
        }
        m_scoreRenderer.setMaxValue(maxScore);

    }

    public PeptideInstance[] getPeptideInstances() {
        return m_peptideInstances;
    }

    public ResultSummary getResultSummary() {
        if ((m_peptideInstances == null) || (m_peptideInstances.length == 0)) {
            return null;
        }
        return m_peptideInstances[0].getResultSummary();
    }

    public void dataUpdated() {

        // call to updateMinMax() is not necessary : peptideMatch are loaded in one time
        fireTableDataChanged();

    }

    public int findRow(long peptideInstanceId) {

        int nb = m_peptideInstances.length;
        for (int i = 0; i < nb; i++) {
            if (peptideInstanceId == m_peptideInstances[i].getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideMatchIds, CompoundTableModel compoundTableModel) {

        if (m_peptideInstances == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> peptideMatchIdMap = new HashSet<>(peptideMatchIds.size());
        peptideMatchIdMap.addAll(peptideMatchIds);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            PeptideInstance ps = getPeptideInstance(iModel);
            if (peptideMatchIdMap.contains(ps.getId())) {
                peptideMatchIds.set(iCur++, ps.getId());
            }
        }

    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        ConvertValueInterface peptideConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DPeptideMatch) o).getPeptide().getSequence();
            }

        };
        filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter, COLTYPE_PEPTIDE_NAME));
        filtersMap.put(COLTYPE_PEPTIDE_PSM_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PSM_SCORE), null, COLTYPE_PEPTIDE_PSM_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_RANK, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_RANK), null, COLTYPE_PEPTIDE_RANK));
        filtersMap.put(COLTYPE_PEPTIDE_CALCULATED_MASS, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS), null, COLTYPE_PEPTIDE_CALCULATED_MASS));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
        filtersMap.put(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ), null, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ));
        filtersMap.put(COLTYPE_PEPTIDE_PPM, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PPM), null, COLTYPE_PEPTIDE_PPM));
        filtersMap.put(COLTYPE_PEPTIDE_RETENTION_TIME, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_RETENTION_TIME), null, COLTYPE_PEPTIDE_RETENTION_TIME));
        filtersMap.put(COLTYPE_PEPTIDE_CHARGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE), null, COLTYPE_PEPTIDE_CHARGE));
        filtersMap.put(COLTYPE_PEPTIDE_LENGTH, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_LENGTH), null, COLTYPE_PEPTIDE_LENGTH));
        filtersMap.put(COLTYPE_PEPTIDE_MISSED_CLEAVAGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLEAVAGE), null, COLTYPE_PEPTIDE_MISSED_CLEAVAGE));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT), null, COLTYPE_PEPTIDE_PROTEIN_SET_COUNT));
        filtersMap.put(COLTYPE_PEPTIDE_MATCH_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MATCH_COUNT), null, COLTYPE_PEPTIDE_MATCH_COUNT));

        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES), null, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES));

        ConvertValueInterface msQueryConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DMsQuery) o).getInitialId();
            }

        };
        filtersMap.put(COLTYPE_PEPTIDE_MSQUERY, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MSQUERY), msQueryConverter, COLTYPE_PEPTIDE_MSQUERY));
        filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringDiffFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));
    }

    @Override
    public boolean isLoaded() {
        return m_table.isSortable();
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {

        switch (columnIndex) {
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
                return Float.class;
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_SPECTRUM_TITLE:
                return String.class;
        }

        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();

        }
        if (data instanceof DPeptideMatch) {
            data = ((DPeptideMatch) data).getPeptide().getSequence();
        }

        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_PEPTIDE_ID, COLTYPE_PEPTIDE_NAME};
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
        return COLTYPE_PEPTIDE_NAME;
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
    public PlotType getBestPlotType() {
        return null; //JPM.TODO
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return null;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return ExportModelUtilities.getExportRowCell(this, row, col);
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
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
            case COLTYPE_PEPTIDE_NAME: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 4);
                break;
            }
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_PSM_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_PPM: {
                renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT));
                break;
            }
            case COLTYPE_PEPTIDE_ID: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Long.class), JLabel.RIGHT);
                break;
            }
            case COLTYPE_PEPTIDE_LENGTH:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLEAVAGE:
            case COLTYPE_PEPTIDE_MATCH_COUNT:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class), JLabel.RIGHT);
                break;
            }
            case COLTYPE_PEPTIDE_PTM: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.LEFT);
                break;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                renderer = new MsQueryRenderer();
                break;
            }
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
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(PeptideInstance.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(PeptideInstance.class)) {
            return m_peptideInstances[row];
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

}
