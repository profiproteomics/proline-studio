package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.LazyTable;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 * Table Model for PeptideInstance of a Rsm
 * @author JM235353
 */
public class PeptideInstanceTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_PEPTIDE_NAME = 1;
    public static final int COLTYPE_PEPTIDE_PTM = 2;
    public static final int COLTYPE_PEPTIDE_SCORE = 3;
    
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 4;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 5;
    public static final int COLTYPE_PEPTIDE_PPM = 6;
    public static final int COLTYPE_PEPTIDE_CHARGE = 7;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 8;
    public static final int COLTYPE_PEPTIDE_RANK = 9;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 10; 
    
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_COUNT = 11;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 12;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 13;
    public static final int COLTYPE_SPECTRUM_TITLE = 14;
    private static final String[] m_columnNames = {"Id", "Peptide", "PTM", "Score", "Calc. Mass", "Exp. MoZ", "Ppm", "Charge", "Missed Cl.", "Rank", "RT", "Protein Set Count", "Protein Sets", "MsQuery", "Spectrum Title"};
    private static final String[] m_columnTooltips = {"Peptide Id", "Peptide", "Post Translational Modifications", "Score", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million", "Charge", "Missed Clivage", "Best Peptide Match Rank", "Retention Time", "Protein Set Count", "Protein Sets", "Best PeptideMatch MsQuery", "Best PeptideMatch Spectrum Title"};
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
            case COLTYPE_PEPTIDE_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
                return LazyData.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            //case COLTYPE_PEPTIDE_DELTA_MOZ:
            case COLTYPE_PEPTIDE_PPM:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
                return Float.class;
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT:
            case COLTYPE_PEPTIDE_RANK:
                return Integer.class;
            case COLTYPE_PEPTIDE_MSQUERY:
            case COLTYPE_SPECTRUM_TITLE:
                return LazyData.class;
            case COLTYPE_PEPTIDE_NAME:
                return DPeptideMatch.class;
            default:
                return String.class;
        }
        
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
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match
                Float score = Float.valueOf((float) peptideMatch.getScore()) ;
                return score;
            }
            case COLTYPE_PEPTIDE_RANK: {
                return (peptideMatch.getCDPrettyRank() == null) ? "" : peptideMatch.getCDPrettyRank(); 
            }
            /*case COLTYPE_PEPTIDE_MSQUERY: {
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                if (!data.getIsMsQuerySet()) {
                    givePriorityTo(taskId, row, col);
                    lazyData.setData(null);
                    
                } else {
                    MsQuery msQuery = peptideMatch.getMsQuery();
                    lazyData.setData( msQuery );
                }
                return lazyData;

   
            }*/

            case COLTYPE_PEPTIDE_CHARGE: {
                return peptideMatch.getCharge();
            }
             
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                return Float.valueOf((float) peptideMatch.getExperimentalMoz());
            }
            case COLTYPE_PEPTIDE_PPM: {

                double deltaMoz = (double) peptideMatch.getDeltaMoz();
                double calculatedMass = peptideMatch.getPeptide().getCalculatedMass() ;
                double charge = (double) peptideMatch.getCharge(); 
                final double CSTE = 1.007825;
                final double EXP_CSTE = StrictMath.pow(10, 6);
                float ppm = (float) ((deltaMoz*EXP_CSTE) / ((calculatedMass + (charge*CSTE))/charge));
                
                return Float.valueOf(ppm);
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                
                Peptide peptide = peptideMatch.getPeptide();
                Float calculatedMass = Float.valueOf((float) peptide.getCalculatedMass()) ;
                return calculatedMass;
 
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                return peptideMatch.getMissedCleavage();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                return peptideInstance.getValidatedProteinSetCount();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {
                
                LazyData lazyData = getLazyData(row,col);
                
                String[] proteinSetNames = peptideMatch.getProteinSetStringArray();
                if (proteinSetNames == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    for (int i=0;i<proteinSetNames.length;i++) {
                        String name = proteinSetNames[i];
                        if (i<proteinSetNames.length-1) {
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
                LazyData lazyData = getLazyData(row,col);
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
            float score = ((DPeptideMatch)peptideInstance.getTransientData().getBestPeptideMatch()).getScore();
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
        filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter, COLTYPE_PEPTIDE_NAME));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_RANK, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_RANK), null, COLTYPE_PEPTIDE_RANK));
        filtersMap.put(COLTYPE_PEPTIDE_CALCULATED_MASS, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS), null, COLTYPE_PEPTIDE_CALCULATED_MASS));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
        filtersMap.put(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ), null, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ));
        filtersMap.put(COLTYPE_PEPTIDE_PPM, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PPM), null, COLTYPE_PEPTIDE_PPM));
        filtersMap.put(COLTYPE_PEPTIDE_RETENTION_TIME, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_RETENTION_TIME), null, COLTYPE_PEPTIDE_RETENTION_TIME));
        filtersMap.put(COLTYPE_PEPTIDE_CHARGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE), null, COLTYPE_PEPTIDE_CHARGE));
        filtersMap.put(COLTYPE_PEPTIDE_MISSED_CLIVAGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLIVAGE), null, COLTYPE_PEPTIDE_MISSED_CLIVAGE));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT), null, COLTYPE_PEPTIDE_PROTEIN_SET_COUNT));
        
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES), null, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES));
        
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
        filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));
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
        }
        
        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            if (data instanceof DPeptideMatch) {
                data = ((DPeptideMatch) data).getPeptide().getSequence();
            }
        }
        
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_PEPTIDE_ID, COLTYPE_PEPTIDE_NAME };
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
    public int getBestXAxisColIndex(PlotType plotType) {
        return -1; //JPM.TODO
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1; //JPM.TODO
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null; // no specific export
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
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4);
                break;
            } 
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_PPM:
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            } 
            case COLTYPE_PEPTIDE_PTM:{
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
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
