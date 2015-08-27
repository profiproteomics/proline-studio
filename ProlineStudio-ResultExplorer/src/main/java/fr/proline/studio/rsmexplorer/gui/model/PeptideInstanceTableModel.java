package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.LazyTable;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
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
    public static final int COLTYPE_PEPTIDE_SCORE = 2;
    //public static final int COLTYPE_PEPTIDE_MSQUERY = 2;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 3;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 4;
    //public static final int COLTYPE_PEPTIDE_DELTA_MOZ = 4;
    public static final int COLTYPE_PEPTIDE_PPM = 5;
    public static final int COLTYPE_PEPTIDE_CHARGE = 6;
    
    
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 7;
    public static final int COLTYPE_PEPTIDE_NB_PROTEIN_SETS = 8;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 9; 
    //public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 8;
    public static final int COLTYPE_PEPTIDE_PTM = 10;
    private static final String[] m_columnNames = {"Id", "Peptide", "Score", "Calc. Mass", "Exp. MoZ", "Ppm" /*"Delta MoZ"*/, "Charge", "Missed Cl.", "Protein Set Count", "RT", "PTM"};
    private static final String[] m_columnTooltips = {"Peptide Id","Peptide", "Score", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million" /*"Delta Mass to Charge Ratio"*/, "Charge", "Missed Clivage", "Protein Set Count", "Retention Time", "Post Translational Modifications"};
    private PeptideInstance[] m_peptideInstances = null;

    private String m_modelName;
    
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
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            //case COLTYPE_PEPTIDE_RETENTION_TIME:
            //case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PTM:
                return LazyData.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            //case COLTYPE_PEPTIDE_DELTA_MOZ:
            case COLTYPE_PEPTIDE_PPM:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
                return Float.class;
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_NB_PROTEIN_SETS:
                return Integer.class;
            
            default:
                return String.class;
        }
        
    }


    @Override
    public int getSubTaskId(int col) {
        switch (col) {
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_PTM:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_PEPTIDE;
            /*case COLTYPE_PEPTIDE_MSQUERY:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_MSQUERY;*/
          /*case COLTYPE_PEPTIDE_RETENTION_TIME:*/
            /*case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_SPECTRUM;*/
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
                
                LazyData lazyData = getLazyData(row,col);

                lazyData.setData(peptideMatch);

                return lazyData;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match
                Float score = Float.valueOf((float) peptideMatch.getScore()) ;
                return score;
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
            /*case COLTYPE_PEPTIDE_DELTA_MOZ: {
                return Float.valueOf((float) peptideMatch.getDeltaMoz());
            }*/
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
                LazyData lazyData = getLazyData(row,col);

                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    Float calculatedMass = Float.valueOf((float) peptide.getCalculatedMass()) ;
                    lazyData.setData(calculatedMass);
                }
                
                return lazyData;
 
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                return peptideMatch.getMissedCleavage();
            }
            case COLTYPE_PEPTIDE_NB_PROTEIN_SETS: {
                return peptideInstance.getValidatedProteinSetCount();
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                return peptideInstance.getElutionTime();
            }
            /*case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
                
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                if (!data.getIsMsQuerySet()) {
                    givePriorityTo(taskId, row, COLTYPE_PEPTIDE_MSQUERY);
                    lazyData.setData(null);
                    
                } else {
                    MsQuery msQuery = peptideMatch.getMsQuery();
                    
                    if (!msQuery.getTransientIsSpectrumSet()) {
                        givePriorityTo(taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        Spectrum spectrum = msQuery.getTransientIsSpectrumSet() ? msQuery.getSpectrum() : null;
                        if (spectrum == null) {
                            lazyData.setData(Float.valueOf(Float.NaN)); //JPM.WART : value loaded, but there is no result in database
                        } else {
                            Float precursorIntensity = spectrum.getPrecursorIntensity();
                            if (precursorIntensity != null) {
                                lazyData.setData(precursorIntensity);
                            } else {
                                lazyData.setData(Float.valueOf(Float.NaN)); //JPM.WART : value loaded, but there is no result in database
                            }
                        }
                    }  
                }
                return lazyData;

            }*/
            case COLTYPE_PEPTIDE_PTM: {
                LazyData lazyData = getLazyData(row, col);

                Peptide peptide = peptideMatch.getPeptide();
                if (peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    return lazyData;
                }
                
                boolean ptmStringLoadeed = peptide.getTransientData().isPeptideReadablePtmStringLoaded();
                if (!ptmStringLoadeed) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    return lazyData;
                }
                
                
                String ptm = "";
                PeptideReadablePtmString ptmString = peptide.getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }
                
                lazyData.setData(ptm);

                return lazyData;
            }
        }
        return null; // should never happen
    }

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
        
        RelativePainterHighlighter.NumberRelativizer relativizer = m_table.getRelativizer();
        if (relativizer == null) {
            return;
        }

        double maxScore = 0;
        int size = m_peptideInstances.length;
        for (int i = 0; i < size; i++) {
            PeptideInstance peptideInstance = m_peptideInstances[i];
            double score = ((DPeptideMatch)peptideInstance.getTransientData().getBestPeptideMatch()).getScore();
            if (score > maxScore) {
                maxScore = score;
            }
        }
        relativizer.setMax(maxScore);

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
        filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null));
        filtersMap.put(COLTYPE_PEPTIDE_CALCULATED_MASS, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS), null));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null));
        filtersMap.put(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ), null));
        filtersMap.put(COLTYPE_PEPTIDE_PPM, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PPM), null));
        filtersMap.put(COLTYPE_PEPTIDE_RETENTION_TIME, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_RETENTION_TIME), null));
        filtersMap.put(COLTYPE_PEPTIDE_CHARGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE), null));
        filtersMap.put(COLTYPE_PEPTIDE_MISSED_CLIVAGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLIVAGE), null));
        filtersMap.put(COLTYPE_PEPTIDE_NB_PROTEIN_SETS, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_NB_PROTEIN_SETS), null));

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
            case COLTYPE_PEPTIDE_PTM:
                return String.class;

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
    public TableCellRenderer getRenderer(int col) {

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
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_PPM:
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_NB_PROTEIN_SETS: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            } 
            case COLTYPE_PEPTIDE_PTM:{
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
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
    
    
}
