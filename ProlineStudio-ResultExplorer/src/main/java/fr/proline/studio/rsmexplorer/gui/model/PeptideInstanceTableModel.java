package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.utils.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Table Model for PeptideInstance of a Rsm
 * @author JM235353
 */
public class PeptideInstanceTableModel extends LazyTableModel {  //JPM.TODO : should be removed, model seems not to use lazy model in fact

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
    private static final String[] m_columnTooltips = {"PeptideMatch Id","Peptide", "Score", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million" /*"Delta Mass to Charge Ratio"*/, "Charge", "Missed Clivage", "Protein Set Count", "Retention Time", "Post Translational Modifications"};
    private PeptideInstance[] m_peptideInstances = null;

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    
    
    public PeptideInstanceTableModel(LazyTable table) {
        super(table);
    }
    
    
    public PeptideInstance getPeptideInstance(int row) {
        
        if (m_filteredIds != null) {
            row = m_filteredIds.get(row).intValue();
        }
        
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
        
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        
        return m_peptideInstances.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }

        // Retrieve Protein Set
        PeptideInstance peptideInstance = m_peptideInstances[rowFiltered];
        DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();

        switch (col) {
            case COLTYPE_PEPTIDE_ID:
                return peptideMatch.getId();
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
        
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }

    }

    private void updateMinMax() {
        RelativePainterHighlighter.NumberRelativizer relativizer = m_table.getRelativizer();
        if (relativizer == null) {
            return;
        }
        
        double maxScore = 0;
        int size = getRowCount();
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
        
        if (m_filteredIds != null) {
            filter();
        } else {
            fireTableDataChanged();
        }
    }
    
    public int findRow(long peptideInstanceId) {
        
        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (peptideInstanceId == m_peptideInstances[m_filteredIds.get(i)].getId()) {
                    return i;
                }
            }
            return -1;
        }
        
        
        int nb = m_peptideInstances.length;
        for (int i = 0; i < nb; i++) {
            if (peptideInstanceId == m_peptideInstances[i].getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideMatchIds) {
        
        if (m_peptideInstances == null) {
            // data not loaded 
            return;
        }
        
        HashSet<Long> peptideMatchIdMap = new HashSet<>(peptideMatchIds.size());
        peptideMatchIdMap.addAll(peptideMatchIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            PeptideInstance ps = getPeptideInstance(iModel);
            if (peptideMatchIdMap.contains(ps.getId())) {
                peptideMatchIds.set(iCur++, ps.getId());
            }
        }
        
        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }

    }
    
    @Override
    public void filter() {

        if (m_peptideInstances == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_peptideInstances.length;
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(Integer.valueOf(i));
            }

        } finally {
            m_isFiltering = false;
        }
        fireTableDataChanged();
    }
   
    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }

        Object data = getValueAt(row, col);
        if (data == null) {
            return true; // should not happen
        }
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            if (data == null) {
                return true; // should not happen
            }
        }

        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                return ((StringFilter) filter).filter(((Peptide) data).getSequence());
            }
                
            case COLTYPE_PEPTIDE_PTM: {
                return ((StringFilter) filter).filter((String) data);
            }
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: 
            /*case COLTYPE_PEPTIDE_DELTA_MOZ:*/
            case COLTYPE_PEPTIDE_PPM:
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                return ((DoubleFilter) filter).filter((Float) data);
            }
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_NB_PROTEIN_SETS: {
                return ((IntegerFilter) filter).filter((Integer) data);
            }

        }

        return true; // should never happen
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PEPTIDE_ID] = null;
            m_filters[COLTYPE_PEPTIDE_NAME] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_NAME));
            m_filters[COLTYPE_PEPTIDE_SCORE] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE));
            m_filters[COLTYPE_PEPTIDE_CALCULATED_MASS] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS));
            m_filters[COLTYPE_PEPTIDE_PTM] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM));
            
            m_filters[COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ));
            //m_filters[COLTYPE_PEPTIDE_DELTA_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_DELTA_MOZ));
            m_filters[COLTYPE_PEPTIDE_PPM] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PPM));
            
            m_filters[COLTYPE_PEPTIDE_RETENTION_TIME] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_RETENTION_TIME));
            
            m_filters[COLTYPE_PEPTIDE_CHARGE] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE));
            m_filters[COLTYPE_PEPTIDE_MISSED_CLIVAGE] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLIVAGE));
            m_filters[COLTYPE_PEPTIDE_NB_PROTEIN_SETS] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_NB_PROTEIN_SETS));

        }
    }

    @Override
    public boolean isLoaded() {
        return m_table.isSortable();
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    
    
}
