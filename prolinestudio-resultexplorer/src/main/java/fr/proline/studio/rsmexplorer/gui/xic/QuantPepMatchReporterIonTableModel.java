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

import fr.proline.core.orm.msi.MasterQuantReporterIon;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DQuantReporterIon;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.data.SelectLevelEnum;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.XicStatusRenderer;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.types.QuantitationType;
import fr.proline.studio.types.XicGroup;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.util.*;

/**
 * Table Model to display abundance values of Reporter Ions associated to PSMs
 *
 */
public class QuantPepMatchReporterIonTableModel extends LazyTableModel implements GlobalTableModelInterface {

    private static final Logger m_logger = LoggerFactory.getLogger(QuantPepMatchReporterIonTableModel.class);
    public static final int COLTYPE_PEPTIDE_MATCH_ID = 0;
    public static final int COLTYPE_PEPTIDE_MATCH_REP_ION_ID = 1;
    public static final int COLTYPE_PEPTIDE_SEQUENCE = 2;
    public static final int COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS = 3;
    public static final int COLTYPE_PEPTIDE_PTM = 4;
    public static final int COLTYPE_PEPTIDE_SCORE = 5;
    public static final int COLTYPE_PEPTIDE_MATCH_CHARGE = 6;
    public static final int COLTYPE_PEPTIDE_MATCH_MOZ = 7;
    public static final int COLTYPE_PEPTIDE_MATCH_RETENTION_TIME = 8;

    public static final int COLTYPE_PEPTIDE_MATCH_PIF = 9;

    public static final int LAST_STATIC_COLUMN = COLTYPE_PEPTIDE_MATCH_PIF;
    private static final String[] m_columnNames = {"Pep. Match Id", "PSM Rep. Ion Id", "Peptide Sequence", "Status", "PTMs", "Score", "Charge", "m/z", "RT","PIF"};
    private static final String[] m_columnNamesForFilter = {"Peptide Match Id", "Pep. Match Reporter Ion id", "Peptide Sequence", "Pep. Match status" , "PTMs", "Score", "Charge", "m/z", "RT","PIF"};
    private static final String[] m_toolTipColumns = {"Peptide Match Id",  "Master Quant Reporter Ion Id", "Identified Peptide Sequence", "Pep. Match status: invalid, valid",  "Post Translational Modifications", "Score", "Charge", "Mass to Charge Ratio", "Retention time (min)","Precursor Intensity Fraction "};

    public static final int COLTYPE_RAW_ABUNDANCE = 0;
    public static final int COLTYPE_ABUNDANCE = 1;

    private static final String[] m_columnNamesQC = { "Raw abundance", "Abundance"};
    private static final String[] m_toolTipQC = {"Raw abundance", "Abundance"};

    private List<MasterQuantReporterIon> m_quantPSMReporterIons = null;
    private DQuantitationChannel[] m_quantChannels = null;
    private int m_quantChannelNumber;

    private final ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    private String m_modelName;

    public QuantPepMatchReporterIonTableModel(LazyTable table) {
        super(table);
    }

    @Override
    public int getColumnCount() {
        if (m_quantChannels == null) {
            return m_columnNames.length;
        } else {
            return m_columnNames.length + m_quantChannelNumber * m_columnNamesQC.length;
        }
    }

    @Override
    public String getColumnName(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);

            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_columnNamesQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getName());

            sb.append("</html>");
            return sb.toString();
        } else {
            return ""; // should not happen
        }
    }

    @Override
    public String getExportColumnName(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);

            return m_columnNamesQC[id] +" " + m_quantChannels[nbQc].getName();
        } else {
            return ""; // should not happen
        }

    }

    public String getColumnNameForFilter(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            return m_columnNamesQC[id];
        } else {
            return ""; // should not happen
        }
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_toolTipColumns[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            String rawFilePath = StringUtils.truncate(m_quantChannels[nbQc].getRawFilePath(), 50);

            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_toolTipQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getFullName());
            sb.append("<br/>");
            sb.append(rawFilePath);
            sb.append("</html>");
            return sb.toString();
        } else {
            return ""; // should not happen
        }
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_PEPTIDE_MATCH_REP_ION_ID)
            return Long.class;
        else if(col == COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS)
            return XicStatusRenderer.SelectLevel.class;
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadXicMasterQuantTask.SUB_TASK_PEPTIDE_ION;
    }

    @Override
    public int getRowCount() {
        if (m_quantPSMReporterIons == null) {
            return 0;
        }

        return m_quantPSMReporterIons.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Quant Peptide Ion
        MasterQuantReporterIon psmReporterIon = m_quantPSMReporterIons.get(row);
        DMasterQuantPeptideIon peptideIon = psmReporterIon.getTransientData().getDMasterQuantPeptideIon();

        switch (col) {
            case COLTYPE_PEPTIDE_MATCH_REP_ION_ID: {
                return psmReporterIon.getId();
            }

            case COLTYPE_PEPTIDE_MATCH_PIF:{
                LazyData lazyData = getLazyData(row, col);
                DPeptideMatch peptideMatch = psmReporterIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    Map<String, Object> vals = peptideMatch.getPropertiesAsMap();
                    if(vals!=null && !vals.isEmpty() && vals.containsKey("precursor_intensity_fraction"))
                        lazyData.setData(vals.get("precursor_intensity_fraction").toString());
                    else
                        lazyData.setData(null);
                }
                return lazyData;
            }

            case COLTYPE_PEPTIDE_MATCH_ID: {
                // Retrieve Peptide Match
                LazyData lazyData = getLazyData(row, col);
                DPeptideMatch peptideMatch = psmReporterIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideMatch.getId());
                }
                return lazyData;
            }

            case COLTYPE_PEPTIDE_SEQUENCE: {
                LazyData lazyData = getLazyData(row, col);
                if (psmReporterIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    if (peptideIon == null || peptideIon.getPeptideInstance() == null) {
                        lazyData.setData(null);
                    } else {
                        lazyData.setData(peptideIon.getPeptideInstance().getPeptide());
                    }
                }
                return lazyData;
            }


            case COLTYPE_PEPTIDE_PTM: {
                LazyData lazyData = getLazyData(row, col);

                if (psmReporterIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else if (peptideIon == null || peptideIon.getPeptideInstance() == null) {
                    lazyData.setData("");

                } else if (peptideIon.getPeptideInstance().getPeptide() != null) {
                    if (!peptideIon.getPeptideInstance().getPeptide().getTransientData().isPeptideReadablePtmStringLoaded()) {
                        lazyData.setData(null);
                    } else {
                        String ptm = "";
                        PeptideReadablePtmString ptmString = peptideIon.getPeptideInstance().getPeptide().getTransientData().getPeptideReadablePtmString();
                        if (ptmString != null) {
                            ptm = ptmString.getReadablePtmString();
                        }

                        lazyData.setData(ptm);
                    }
                } else {
                    lazyData.setData("");
                }
                return lazyData;
            }

            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match
                LazyData lazyData = getLazyData(row, col);
                DPeptideMatch peptideMatch = psmReporterIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideMatch.getScore());
                }
                return lazyData;

            }

            case COLTYPE_PEPTIDE_MATCH_CHARGE: {
                LazyData lazyData = getLazyData(row, col);
                DPeptideMatch peptideMatch = psmReporterIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideMatch.getCharge());
                }
                return lazyData;

            }

            case COLTYPE_PEPTIDE_MATCH_MOZ: {
                LazyData lazyData = getLazyData(row, col);
                DPeptideMatch peptideMatch = psmReporterIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideMatch.getExperimentalMoz());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_MATCH_RETENTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                DPeptideMatch peptideMatch = psmReporterIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(psmReporterIon.getTransientData().getPeptideMatch().getRetentionTime());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS:{
                SelectLevelEnum selectionLevel =  SelectLevelEnum.valueOf(psmReporterIon.getMasterQuantComponent().getSelectionLevel());
                return new XicStatusRenderer.SelectLevel(selectionLevel, selectionLevel);
//                switch (selectionLevel) {
//                    case 1 : return  "invalid";
//                    default: return "valid";
//                }
            }
            default: {
                // Quant Channel columns
                LazyData lazyData = getLazyData(row, col);
                if (psmReporterIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {

                    // retrieve quantPeptideIon for the quantChannelId
                    int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                    int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                    Map<Long, DQuantReporterIon> quantReporterIonByQchIds = psmReporterIon.getTransientData().getQuantReporterIonByQchIds();
                    if (quantReporterIonByQchIds == null) {
                        switch (id) {
                            case COLTYPE_ABUNDANCE:
                            case COLTYPE_RAW_ABUNDANCE:
                                lazyData.setData(0f);
                                break;
                        }
                    } else {
                        DQuantReporterIon quantRepIon = quantReporterIonByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantRepIon == null) {
                            switch (id) {
                                case COLTYPE_ABUNDANCE:
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(0f);
                                    break;
                            }
                        } else {
                            switch (id) {
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(quantRepIon.getAbundance().isNaN() ? Float.valueOf(0) : quantRepIon.getAbundance());
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(quantRepIon.getRawAbundance().isNaN() ? Float.valueOf(0) : quantRepIon.getRawAbundance());
                                    break;
                            }
                        }
                    }
                }
                return lazyData;
            }
        }
        //return null; // should never happen
    }

    public void setData(Long taskId, DQuantitationChannel[] quantChannels, List<MasterQuantReporterIon> peptideReporterIons) {
        boolean structureChanged = true;
        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int i = 0; i < m_quantChannels.length; i++) {
                structureChanged = !(m_quantChannels[i].equals(quantChannels[i]));
            }
        }
        m_quantPSMReporterIons = peptideReporterIons;
        m_quantChannels = quantChannels;
        m_quantChannelNumber = quantChannels.length;

        if (structureChanged) {
            fireTableStructureChanged();
        }

        m_taskId = taskId;

        fireTableDataChanged();

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();
    }

    public MasterQuantReporterIon getPeptideMatchRepIon(int i) {

        return m_quantPSMReporterIons.get(i);
    }

    public int findRow(long peptideRepIonId) {

        int nb = m_quantPSMReporterIons.size();
        for (int i = 0; i < nb; i++) {
            if (peptideRepIonId == m_quantPSMReporterIons.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

//    public void sortAccordingToModel(ArrayList<Long> peptideIds, CompoundTableModel compoundTableModel) {
//
//        if (m_quantPeptideIons == null) {
//            // data not loaded
//            return;
//        }
//
//        HashSet<Long> peptideIdMap = new HashSet<>(peptideIds.size());
//        peptideIdMap.addAll(peptideIds);
//
//        int nb = m_table.getRowCount();
//        int iCur = 0;
//        for (int iView = 0; iView < nb; iView++) {
//            int iModel = m_table.convertRowIndexToModel(iView);
//            if (compoundTableModel != null) {
//                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
//            }
//            // Retrieve Peptide Ion
//            DMasterQuantPeptideIon p = getPeptideIon(iModel);
//            if (peptideIdMap.contains(p.getId())) {
//                peptideIds.set(iCur++, p.getId());
//            }
//        }
//
//    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        ConvertValueInterface peptideConverter = o -> {
            if (o == null) {
                return null;
            }
            return ((Peptide) o).getSequence();
        };
        filtersMap.put(COLTYPE_PEPTIDE_SEQUENCE, new StringDiffFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_SEQUENCE), peptideConverter, COLTYPE_PEPTIDE_SEQUENCE));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_MATCH_CHARGE, new IntegerFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_MATCH_CHARGE), null, COLTYPE_PEPTIDE_MATCH_CHARGE));
        filtersMap.put(COLTYPE_PEPTIDE_MATCH_MOZ, new DoubleFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_MATCH_MOZ), null, COLTYPE_PEPTIDE_MATCH_MOZ));

        ConvertValueInterface minuteConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((Float) o) / 60d;
            }

        };
        filtersMap.put(COLTYPE_PEPTIDE_MATCH_RETENTION_TIME, new DoubleFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_MATCH_RETENTION_TIME), minuteConverter, COLTYPE_PEPTIDE_MATCH_RETENTION_TIME));
        int nbCol = getColumnCount();
        for (int i = LAST_STATIC_COLUMN + 1; i < nbCol; i++) {
            int nbQc = (i - m_columnNames.length) / m_columnNamesQC.length;
            int id = i - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            switch (id) {
                case COLTYPE_ABUNDANCE:
                case COLTYPE_RAW_ABUNDANCE:
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
                    break;
            }
        }

    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }

    public Long getResultSummaryId() {
        if ((m_quantPSMReporterIons == null) || (m_quantPSMReporterIons.size() == 0)) {
            return null;
        }

        return m_quantPSMReporterIons.get(0).getResultSummary().getId();
    }

    /**
     * by default the rawAbundance and selectionLevel are hidden return the list
     * of columns ids of these columns
     *
     * @return
     */
    public List<Integer> getDefaultColumnsToHide() {

        List<Integer> listIds = new ArrayList<>();
        listIds.add(COLTYPE_PEPTIDE_MATCH_ID);
        listIds.add(COLTYPE_PEPTIDE_MATCH_REP_ION_ID);
        listIds.add(COLTYPE_PEPTIDE_MATCH_PIF);
        if (m_quantChannels != null) {
            for (int i = m_quantChannels.length - 1; i >= 0; i--) {
                listIds.add(m_columnNames.length + COLTYPE_RAW_ABUNDANCE + (i * m_columnNamesQC.length));
            }
        }
        return listIds;
    }

    @Override
    public String getExportRowCell(int row, int col) {

        // Retrieve Quant Peptide Ion
        MasterQuantReporterIon peptideMatchReportertIon = m_quantPSMReporterIons.get(row);
        DMasterQuantPeptideIon peptideIon = peptideMatchReportertIon.getTransientData().getDMasterQuantPeptideIon();

        switch (col) {
            case COLTYPE_PEPTIDE_MATCH_REP_ION_ID: {
                return String.valueOf(peptideMatchReportertIon.getId());
            }
            case COLTYPE_PEPTIDE_MATCH_ID: {
                if (peptideIon == null) {
                    return "";
                } else {
                    if (peptideIon.getPeptideInstance() == null) {
                        return "";
                    } else {
                        return String.valueOf(peptideIon.getPeptideInstance().getPeptideId());
                    }
                }

            }
            case COLTYPE_PEPTIDE_SEQUENCE: {
                if (peptideIon == null) {
                    return "";
                } else {
                    if (peptideIon.getPeptideInstance() == null) {
                        return "";
                    } else {
                        return peptideIon.getPeptideInstance().getPeptide().getSequence();
                    }
                }

            }

            case COLTYPE_PEPTIDE_PTM: {
                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    return "";
                } else if (peptideInstance.getPeptide() != null) {
                    boolean ptmStringLoadeed = peptideInstance.getPeptide().getTransientData().isPeptideReadablePtmStringLoaded();
                    if (!ptmStringLoadeed) {
                        return null;
                    }
                    String ptm = "";
                    PeptideReadablePtmString ptmString = peptideInstance.getPeptide().getTransientData().getPeptideReadablePtmString();
                    if (ptmString != null) {
                        ptm = ptmString.getReadablePtmString();
                    }

                    return ptm;
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_SCORE: {
                DPeptideMatch peptideMatch = peptideMatchReportertIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    return "";

                } else {
                    float score = peptideMatch.getScore();
                    return String.valueOf(score);
                }

            }
            case COLTYPE_PEPTIDE_MATCH_CHARGE: {
                if (peptideIon == null) {
                    return "";
                } else {
                    return String.valueOf(peptideIon.getCharge());
                }

            }
            case COLTYPE_PEPTIDE_MATCH_MOZ: {
                if (peptideMatchReportertIon.getTransientData().getPeptideMatch() == null) {
                    return "";
                } else {
                    return String.valueOf(peptideMatchReportertIon.getTransientData().getPeptideMatch().getExperimentalMoz());
                }

            }
            case COLTYPE_PEPTIDE_MATCH_RETENTION_TIME: {
                if (peptideMatchReportertIon.getTransientData().getPeptideMatch() == null) {
                    return "";
                } else {
                    return StringUtils.getTimeInMinutes(peptideMatchReportertIon.getTransientData().getPeptideMatch().getRetentionTime(), 2);
                }
            }
            case  COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS:{
                XicStatusRenderer.SelectLevel selectLevel =(XicStatusRenderer.SelectLevel)  getValueAt(row, col);
                return selectLevel.getDescription();
//                Integer selectionLevel= (Integer)  getValueAt(row, col);
//                switch (selectionLevel) {
//                    case 1 : return  "invalid";
//                    default: return "valid";
//                }
            }
            case COLTYPE_PEPTIDE_MATCH_PIF:{
                DPeptideMatch peptideMatch = peptideMatchReportertIon.getTransientData().getPeptideMatch();
                if (peptideMatch == null) {
                    return "";
                } else {
                    Map<String, Object> vals = peptideMatch.getPropertiesAsMap();
                    if(vals!=null && !vals.isEmpty() && vals.containsKey("precursor_intensity_fraction"))
                        return vals.get("precursor_intensity_fraction").toString();
                    else
                        return "";
                }
            }
            default: {
                // Quant Channel columns
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                if (peptideMatchReportertIon.getResultSummary() == null) {
                    switch (id) {
                        case COLTYPE_ABUNDANCE:
                        case COLTYPE_RAW_ABUNDANCE:
                            return "0";
                    }
                } else {

                    // retrieve quantPeptideIon for the quantChannelId
                    Map<Long, DQuantReporterIon> quantPepMatchReporterIonByQchIds = peptideMatchReportertIon.getTransientData().getQuantReporterIonByQchIds();
                    if (quantPepMatchReporterIonByQchIds == null) {
                        switch (id) {
                            case COLTYPE_ABUNDANCE:
                            case COLTYPE_RAW_ABUNDANCE:
                                return "0";
                        }
                    } else {

                        DQuantReporterIon quantPepMatchRepIon = quantPepMatchReporterIonByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantPepMatchRepIon == null) {
                            switch (id) {
                                case COLTYPE_ABUNDANCE:
                                case COLTYPE_RAW_ABUNDANCE:
                                    return "0";
                            }
                        } else {
                            switch (id) {
                                case COLTYPE_ABUNDANCE:
                                    return ((quantPepMatchRepIon.getAbundance() == null || quantPepMatchRepIon.getAbundance().isNaN()) ? "0" : Float.toString(quantPepMatchRepIon.getAbundance()));
                                case COLTYPE_RAW_ABUNDANCE:
                                    return ((quantPepMatchRepIon.getRawAbundance() == null || quantPepMatchRepIon.getRawAbundance().isNaN()) ? "0" : Float.toString(quantPepMatchRepIon.getRawAbundance()));
                            }
                        }
                    }
                }
            }
        }
        return ""; // should never happen
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getDataColumnIdentifier(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);

            return m_columnNamesQC[id] + ' ' + m_quantChannels[nbQc].getName();
        }
    }

    @Override
    public Class getDataColumnClass(int col) {
        switch (col) {
            case COLTYPE_PEPTIDE_MATCH_ID:
            case COLTYPE_PEPTIDE_MATCH_REP_ION_ID: {
                return Long.class;
            }
            case COLTYPE_PEPTIDE_SEQUENCE:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_MATCH_RETENTION_TIME: {
                return Float.class;
            }
            case COLTYPE_PEPTIDE_MATCH_PIF:
            case COLTYPE_PEPTIDE_PTM: {
                return String.class;
            }
            case COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS:
            case COLTYPE_PEPTIDE_MATCH_CHARGE: {
                return Integer.class;
            }
            case COLTYPE_PEPTIDE_MATCH_MOZ: {
                return Double.class;
            }
            default: {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                switch (id) {
                    case COLTYPE_ABUNDANCE:
                    case COLTYPE_RAW_ABUNDANCE:
                        return Float.class;

                }

            }
        }
        return null; // should never happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
        }
        if(columnIndex == COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS)
            return ((XicStatusRenderer.SelectLevel) data).getStatus().getIntValue(); // return XicStatusRenderer.SelectLevel.class;
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        return new int[]{COLTYPE_PEPTIDE_MATCH_ID, COLTYPE_PEPTIDE_MATCH_REP_ION_ID};
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PEPTIDE_SEQUENCE;
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
        return null;
    }

    @Override
    public PlotType getBestPlotType() {
        return null;
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col) {
            case COLTYPE_PEPTIDE_MATCH_ID:
            case COLTYPE_PEPTIDE_MATCH_REP_ION_ID: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Long.class), JLabel.RIGHT);
                break;
            }
            case COLTYPE_PEPTIDE_SEQUENCE: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS:{
                return  new XicStatusRenderer(null, COLTYPE_PEPTIDE_MATCH_REP_ION_STATUS);
            }
            case COLTYPE_PEPTIDE_MATCH_PIF:
            case COLTYPE_PEPTIDE_PTM: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.LEFT);
                break;
            }
            case COLTYPE_PEPTIDE_MATCH_CHARGE: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class), JLabel.RIGHT);
                break;
            }
            case COLTYPE_PEPTIDE_MATCH_MOZ: {
                renderer = new DoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 4);
                break;
            }
            case COLTYPE_PEPTIDE_MATCH_RETENTION_TIME: {
//                renderer = new TimeRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 4);
                break;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            default: {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                switch (id) {
                    case COLTYPE_ABUNDANCE:
                        renderer = new BigFloatOrDoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0);
                        break;
                    case COLTYPE_RAW_ABUNDANCE: {
                        renderer = new BigFloatOrDoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0);
                        break;
                    }

                }

            }
        }

        m_rendererMap.put(col, renderer);
        return renderer;
    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap<>();

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(DMasterQuantPeptideIon.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DMasterQuantPeptideIon.class)) {
            return m_quantPSMReporterIons.get(row);
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        if (c.equals(XicGroup.class)) {
            if (col <= LAST_STATIC_COLUMN) {
                return null;
            } else {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                return new XicGroup(m_quantChannels[nbQc].getBiologicalGroupId(), null); //biologicalGroupName.getBiologicalGroupName(); JPM.TODO
            }

        }
        if (c.equals(QuantitationType.class)) {
            if (col <= LAST_STATIC_COLUMN) {
                return null;
            } else {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                switch (id) {
                    case COLTYPE_ABUNDANCE:
                        return QuantitationType.getQuantitationType(QuantitationType.ABUNDANCE);
                    case COLTYPE_RAW_ABUNDANCE:
                        return QuantitationType.getQuantitationType(QuantitationType.RAW_ABUNDANCE);
                }

                return null;
            }

        }
        return null;
    }

}
