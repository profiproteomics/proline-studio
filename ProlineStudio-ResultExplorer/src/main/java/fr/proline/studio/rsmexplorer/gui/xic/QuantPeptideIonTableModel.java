package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.TimeRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.types.QuantitationType;
import fr.proline.studio.types.XicGroup;
import fr.proline.studio.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class QuantPeptideIonTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEPTIDE_ION_ID = 0;
    public static final int COLTYPE_PEPTIDE_ION_NAME = 1;
    public static final int COLTYPE_PEPTIDE_PTM = 2;
    public static final int COLTYPE_PEPTIDE_SCORE = 3;
    public static final int COLTYPE_PEPTIDE_ION_CHARGE = 4;
    public static final int COLTYPE_PEPTIDE_ION_MOZ = 5;
    public static final int COLTYPE_PEPTIDE_ION_ELUTION_TIME = 6;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_COUNT = 7;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 8;
    public static final int LAST_STATIC_COLUMN = COLTYPE_PEPTIDE_PROTEIN_SET_NAMES;
    private static final String[] m_columnNames = {"Id", "Peptide Sequence", "PTM", "Score", "Charge", "m/z", "<html>Elution<br/>Time (min)</html>", "Protein Set Count", "Protein Sets"};
    private static final String[] m_columnNamesForFilter = {"Id", "Peptide Sequence", "PTM", "Score", "Charge", "m/z", "Elution Time", "Protein Set Count", "Protein Sets"};
    private static final String[] m_toolTipColumns = {"MasterQuantPeptideIon Id", "Identified Peptide Sequence", "Post Translational Modifications", "Score", "Charge", "Mass to Charge Ratio", "Elution time", "Protein Set Count", "Protein Sets"};

    public static final int COLTYPE_SELECTION_LEVEL = 0;
    public static final int COLTYPE_PSM = 1;
    public static final int COLTYPE_RAW_ABUNDANCE = 2;
    public static final int COLTYPE_ABUNDANCE = 3;

    private static final String[] m_columnNamesQC = {"Sel. level", "Pep. match count", "Raw abundance", "Abundance" };
    private static final String[] m_toolTipQC = {"Selection level", "Peptides match count", "Raw abundance", "Abundance" };
    private static final String[] m_columnNamesQC_SC = {"Sel. level", "Basic SC", "Specific SC", "Abundance"};
    private static final String[] m_toolTipQC_SC = {"Selection level", "Basic Spectral Count","Specific Spectral Count",  "Abundance"};

    private List<DMasterQuantPeptideIon> m_quantPeptideIons = null;
    private DQuantitationChannel[] m_quantChannels = null;
    private int m_quantChannelNumber;
    private boolean m_isXICMode = true;

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();
    
    private String m_modelName;

    public QuantPeptideIonTableModel(LazyTable table) {
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
            if(m_isXICMode){
                sb.append(m_columnNamesQC[id]);
            }else{
                sb.append(m_columnNamesQC_SC[id]);
            }
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            /*sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getRawFileName());*/

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
            
            StringBuilder sb = new StringBuilder();
            if(m_isXICMode){
                sb.append(m_columnNamesQC[id]);
            }else{
                sb.append(m_columnNamesQC_SC[id]);
            }
            sb.append(" ");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            return sb.toString();
        }else {
            return ""; // should not happen
        }
        
    }
    
    public String getColumnNameForFilter(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            return m_isXICMode ? m_columnNamesQC[id]:  m_columnNamesQC_SC[id];
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
            if (m_isXICMode){
                sb.append(m_toolTipQC[id]);
            }else{
                sb.append(m_toolTipQC_SC[id]);
            }
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
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
        if (col == COLTYPE_PEPTIDE_ION_ID) {
            return Long.class;
        }
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadXicMasterQuantTask.SUB_TASK_PEPTIDE_ION;
    }

    @Override
    public int getRowCount() {
        if (m_quantPeptideIons == null) {
            return 0;
        }

        return m_quantPeptideIons.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Quant Peptide Ion
        DMasterQuantPeptideIon peptideIon = m_quantPeptideIons.get(row);

        switch (col) {
            case COLTYPE_PEPTIDE_ION_ID: {
                return peptideIon.getId();
            }
            case COLTYPE_PEPTIDE_ION_NAME: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    if (peptideIon.getPeptideInstance() == null) {
                        lazyData.setData("");
                    }else{
                        lazyData.setData(peptideIon.getPeptideInstance().getPeptide().getSequence());
                    }
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_PTM: {
                LazyData lazyData = getLazyData(row, col);
                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else if (peptideInstance == null) {
                    lazyData.setData("");
                    
                }else if (peptideInstance.getBestPeptideMatch() != null) {
                    boolean ptmStringLoaded = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().isPeptideReadablePtmStringLoaded();
                    if (!ptmStringLoaded) {
                        lazyData.setData(null);
                    } else {
                        String ptm = "";
                        PeptideReadablePtmString ptmString = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().getPeptideReadablePtmString();
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
                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else if (peptideInstance.getBestPeptideMatch() != null) {
                    Float score = Float.valueOf((float) peptideInstance.getBestPeptideMatch().getScore());
                    lazyData.setData(score);
                } else {
                    lazyData.setData(null);
                }
                return lazyData;
               
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                LazyData lazyData = getLazyData(row, col);
                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideInstance.getValidatedProteinSetCount());
                }
                return lazyData;
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {

                LazyData lazyData = getLazyData(row, col);

                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    DPeptideMatch peptideMatch = peptideInstance.getBestPeptideMatch();
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
                }
                return lazyData;
            }
            case COLTYPE_PEPTIDE_ION_CHARGE: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideIon.getCharge());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_ION_MOZ: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideIon.getMoz());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_ION_ELUTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideIon.getElutionTime());
                }
                return lazyData;

            }
            default: {
                // Quant Channel columns 
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {

                    // retrieve quantPeptideIon for the quantChannelId
                    int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                    int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                    Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = peptideIon.getQuantPeptideIonByQchIds();
                    if (quantPeptideIonByQchIds == null) {
                        switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                            }
                    } else {
                        DQuantPeptideIon quantPeptideIon = quantPeptideIonByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantPeptideIon == null) {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                            }
                        } else {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(quantPeptideIon.getSelectionLevel());
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(quantPeptideIon.getAbundance().isNaN() ? Float.valueOf(0) : quantPeptideIon.getAbundance());
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(quantPeptideIon.getRawAbundance().isNaN() ? Float.valueOf(0) : quantPeptideIon.getRawAbundance());
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(quantPeptideIon.getPeptideMatchesCount() == null? Integer.valueOf(0) :quantPeptideIon.getPeptideMatchesCount() );
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
    private final StringBuilder m_sb = new StringBuilder();

    public void setData(Long taskId, DQuantitationChannel[] quantChannels, List<DMasterQuantPeptideIon> peptideIons, boolean isXICMode) {
        boolean structureChanged = true;
        m_isXICMode = isXICMode;
        if (m_quantChannels !=null && m_quantChannels.length == quantChannels.length ) {
            for (int i=0; i<m_quantChannels.length; i++) {
                structureChanged = !(m_quantChannels[i].equals(quantChannels[i]));
            }
        }
        m_quantPeptideIons = peptideIons;
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

    public DMasterQuantPeptideIon getPeptideIon(int i) {

        return m_quantPeptideIons.get(i);
    }

    public int findRow(long peptideId) {


        int nb = m_quantPeptideIons.size();
        for (int i = 0; i < nb; i++) {
            if (peptideId == m_quantPeptideIons.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideIds, CompoundTableModel compoundTableModel) {

        if (m_quantPeptideIons == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> peptideIdMap = new HashSet<>(peptideIds.size());
        peptideIdMap.addAll(peptideIds);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Peptide Ion
            DMasterQuantPeptideIon p = getPeptideIon(iModel);
            if (peptideIdMap.contains(p.getId())) {
                peptideIds.set(iCur++, p.getId());
            }
        }

    }


    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        filtersMap.put(COLTYPE_PEPTIDE_ION_NAME, new StringDiffFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_NAME), null, COLTYPE_PEPTIDE_ION_NAME));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_ION_CHARGE, new IntegerFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_CHARGE), null, COLTYPE_PEPTIDE_ION_CHARGE));
        filtersMap.put(COLTYPE_PEPTIDE_ION_MOZ, new DoubleFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_MOZ), null, COLTYPE_PEPTIDE_ION_MOZ));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT), null, COLTYPE_PEPTIDE_PROTEIN_SET_COUNT));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES), null, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES));
        
        ConvertValueInterface minuteConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((Float) o) / 60d;
            }

        };
        filtersMap.put(COLTYPE_PEPTIDE_ION_ELUTION_TIME, new DoubleFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_ELUTION_TIME), minuteConverter, COLTYPE_PEPTIDE_ION_ELUTION_TIME));
        int nbCol = getColumnCount();
        for (int i=LAST_STATIC_COLUMN+1; i< nbCol; i++){
            int nbQc = (i - m_columnNames.length) / m_columnNamesQC.length;
            int id = i - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            switch (id) {
                case COLTYPE_SELECTION_LEVEL:
                    filtersMap.put(i, new IntegerFilter(getColumnName(i), null, i));
                    break;
                case COLTYPE_ABUNDANCE:
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
                    break;
                case COLTYPE_RAW_ABUNDANCE:
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
                    break;
                case COLTYPE_PSM:
                    filtersMap.put(i, new IntegerFilter(getColumnName(i), null, i));
                    break;
                default:
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

    public int getByQCCount() {
        return m_columnNamesQC.length;
    }

    public int getQCCount() {
        return m_quantChannels.length;
    }

    public int getColumStart(int index) {
        return m_columnNames.length + index * m_columnNamesQC.length;
    }

    public int getColumStop(int index) {
        return m_columnNames.length + (1 + index) * m_columnNamesQC.length - 1;
    }

    public String getQCName(int i) {

        StringBuilder sb = new StringBuilder();

        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(i);
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(m_quantChannels[i].getResultFileName());
        /*sb.append("<br/>");
        sb.append(m_quantChannels[i].getRawFileName());*/
        sb.append("</html>");

        return sb.toString();
    }

    public String getByQCMColumnName(int index) {
        return m_isXICMode ? m_columnNamesQC[index] : m_columnNamesQC_SC[index];
    }

    public int getQCNumber(int col) {
        return (col - m_columnNames.length) / m_columnNamesQC.length;
    }

    public int getTypeNumber(int col) {
        return (col - m_columnNames.length) % m_columnNamesQC.length;
    }

    public Long getResultSummaryId() {
        if ((m_quantPeptideIons == null) || (m_quantPeptideIons.size() == 0)) {
            return null;
        }

        return m_quantPeptideIons.get(0).getResultSummary().getId();
    }

    /**
     * by default the rawAbundance and selectionLevel are hidden return the list
     * of columns ids of these columns
     *
     * @return
     */
    public List<Integer> getDefaultColumnsToHide() {
        List<Integer> listIds = new ArrayList();
        if (m_quantChannels != null) {
            for (int i = m_quantChannels.length - 1; i >= 0; i--) {
                if (m_isXICMode){
                    listIds.add(m_columnNames.length + COLTYPE_RAW_ABUNDANCE + (i * m_columnNamesQC.length));
                }
                listIds.add(m_columnNames.length + COLTYPE_SELECTION_LEVEL + (i * m_columnNamesQC.length));
            }
        }
        return listIds;
    }



    @Override
    public String getExportRowCell(int row, int col) {
        int rowFiltered = row;

        // Retrieve Quant Peptide Ion
        DMasterQuantPeptideIon peptideIon = m_quantPeptideIons.get(rowFiltered);

        switch (col) {
            case COLTYPE_PEPTIDE_ION_ID: {
                return String.valueOf(peptideIon.getId());
            }
            case COLTYPE_PEPTIDE_ION_NAME: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    if (peptideIon.getPeptideInstance() == null) {
                        return "";
                    }else{
                        return peptideIon.getPeptideInstance().getPeptide().getSequence();
                    }
                }

            }
            case COLTYPE_PEPTIDE_PTM: {
                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    return "";
                } else if (peptideInstance.getBestPeptideMatch() != null) {
                    boolean ptmStringLoadeed = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().isPeptideReadablePtmStringLoaded();
                    if (!ptmStringLoadeed) {
                        return null;
                    }
                    String ptm = "";
                    PeptideReadablePtmString ptmString = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().getPeptideReadablePtmString();
                    if (ptmString != null) {
                        ptm = ptmString.getReadablePtmString();
                    }

                    return ptm;
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_SCORE: {
                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    return "";
                    
                } else if (peptideInstance.getBestPeptideMatch() != null) {
                    float score = (float) peptideInstance.getBestPeptideMatch().getScore() ;
                    return String.valueOf(score);
                }
                return "";
               
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {

                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    return "";
                } else {
                    return String.valueOf(peptideInstance.getValidatedProteinSetCount());
                }
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {

                DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
                if (peptideInstance == null) {
                    return "";
                } else {
                    DPeptideMatch peptideMatch = peptideInstance.getBestPeptideMatch();
                    String[] proteinSetNames = peptideMatch.getProteinSetStringArray();
                    if (proteinSetNames == null) {
                        return "";
                    } else {
                        for (int i = 0; i < proteinSetNames.length; i++) {
                            String name = proteinSetNames[i];
                            if (i < proteinSetNames.length - 1) {
                                m_sb.append(name).append(", ");
                            } else {
                                m_sb.append(name);
                            }
                        }
                        String t = m_sb.toString();
                        m_sb.setLength(0);
                        return t;
                    }
                }
            }
            case COLTYPE_PEPTIDE_ION_CHARGE: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    return String.valueOf(peptideIon.getCharge());
                }

            }
            case COLTYPE_PEPTIDE_ION_MOZ: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    return String.valueOf(peptideIon.getMoz());
                }

            }
            case COLTYPE_PEPTIDE_ION_ELUTION_TIME: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    return String.valueOf(peptideIon.getElutionTime());
                }

            }
            default: {
                // Quant Channel columns
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                if (peptideIon.getResultSummary() == null) {
                    switch (id) {
                        case COLTYPE_SELECTION_LEVEL:
                            return Integer.toString(0);
                        case COLTYPE_ABUNDANCE:
                            return Float.toString(0);
                        case COLTYPE_RAW_ABUNDANCE:
                            Float.toString(0);
                        case COLTYPE_PSM:
                            return Integer.toString(0);
                    }
                } else {

                    // retrieve quantPeptideIon for the quantChannelId
                    Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = peptideIon.getQuantPeptideIonByQchIds();
                    if (quantPeptideIonByQchIds == null) {
                        switch (id) {
                            case COLTYPE_SELECTION_LEVEL:
                                return Integer.toString(0);
                            case COLTYPE_ABUNDANCE:
                                return Float.toString(0);
                            case COLTYPE_RAW_ABUNDANCE:
                                Float.toString(0);
                            case COLTYPE_PSM:
                                return Integer.toString(0);
                        }
                    } else {
                        
                        DQuantPeptideIon quantPeptideIon = quantPeptideIonByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantPeptideIon == null) {
                            switch (id) {
                            case COLTYPE_SELECTION_LEVEL:
                                return Integer.toString(0);
                            case COLTYPE_ABUNDANCE:
                                return Float.toString(0);
                            case COLTYPE_RAW_ABUNDANCE:
                                Float.toString(0);
                            case COLTYPE_PSM:
                                return Integer.toString(0);
                        }
                        } else {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL : return (quantPeptideIon.getSelectionLevel() == null?Integer.toString(0):Integer.toString(quantPeptideIon.getSelectionLevel()));
                                case COLTYPE_ABUNDANCE : return ((quantPeptideIon.getAbundance() == null || quantPeptideIon.getAbundance().isNaN())? Float.toString(0):Float.toString(quantPeptideIon.getAbundance()));
                                case COLTYPE_RAW_ABUNDANCE : return ((quantPeptideIon.getRawAbundance() == null || quantPeptideIon.getRawAbundance().isNaN())? Float.toString(0):Float.toString(quantPeptideIon.getRawAbundance()));
                                case COLTYPE_PSM : return (quantPeptideIon.getPeptideMatchesCount()== null?Integer.toString(0):Integer.toString(quantPeptideIon.getPeptideMatchesCount()));
                            }
                        }
                    }
                }
            }
        }
       return ""; // should never happen
    }

    @Override
    public String getDataColumnIdentifier(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);

            StringBuilder sb = new StringBuilder();
            if (m_isXICMode){
                sb.append(m_columnNamesQC[id]);
            }else{
                sb.append(m_columnNamesQC_SC[id]);
            }
            sb.append(' ');
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            return sb.toString();
        }
    }

    @Override
    public Class getDataColumnClass(int col) {
        switch (col) {
            case COLTYPE_PEPTIDE_ION_ID: {
                return Long.class;
            }
            case COLTYPE_PEPTIDE_ION_NAME: {
                return String.class;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                return Float.class;
            }
            case COLTYPE_PEPTIDE_PTM: {
                return String.class;
            }
            case COLTYPE_PEPTIDE_ION_CHARGE: {
                return Integer.class; 
            }
            case COLTYPE_PEPTIDE_ION_MOZ: {
                return Double.class; 
            }
            case COLTYPE_PEPTIDE_ION_ELUTION_TIME: {
                return Float.class; 
            }
            default: {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                switch (id) {
                    case COLTYPE_SELECTION_LEVEL:
                        return Integer.class;
                    case COLTYPE_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_RAW_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_PSM:
                        return Integer.class;
                        
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
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_PEPTIDE_ION_NAME, COLTYPE_PEPTIDE_ION_ID };
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PEPTIDE_ION_NAME;
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
    public int getBestXAxisColIndex(PlotType plotType) {
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col) {

            case COLTYPE_PEPTIDE_ION_NAME:
            case COLTYPE_PEPTIDE_PTM: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PEPTIDE_ION_CHARGE: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            case COLTYPE_PEPTIDE_ION_MOZ: {
                renderer = new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4);
                break;
            }
            case COLTYPE_PEPTIDE_ION_ELUTION_TIME: {
                renderer = new TimeRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            default: {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                switch (id) {
                    case COLTYPE_SELECTION_LEVEL:
                    case COLTYPE_PSM: {
                        renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                        break;
                    }
                        
                    case COLTYPE_ABUNDANCE:
                    case COLTYPE_RAW_ABUNDANCE: {
                       renderer = new BigFloatOrDoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0);
                       break;

                    }
  
                }

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
            return m_quantPeptideIons.get(row);
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
                if (m_isXICMode) {
                    switch (id) {
                        case COLTYPE_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.ABUNDANCE);
                        case COLTYPE_RAW_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.RAW_ABUNDANCE);
                    }
                } else {
                    switch (id) {
                        case COLTYPE_PSM:
                            return QuantitationType.getQuantitationType(QuantitationType.BASIC_SC);
                        case COLTYPE_RAW_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.SPECIFIC_SC);
                        case COLTYPE_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.WEIGHTED_SC);
                    }
                }

                
                return null;
            }

        }
        return null;
    }

}
