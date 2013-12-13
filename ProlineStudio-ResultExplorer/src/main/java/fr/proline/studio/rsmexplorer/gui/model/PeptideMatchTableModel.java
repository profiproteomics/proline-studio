package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DMsQuery;
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
 * Table Model for PeptideMatch of a RSet
 * @author JM235353
 */
public class PeptideMatchTableModel extends LazyTableModel {

    public static final int COLTYPE_PEPTIDE_NAME = 0;
    public static final int COLTYPE_PEPTIDE_SCORE = 1;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 2;
    public static final int COLTYPE_PEPTIDE_RANK = 3;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 4;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 5;
    public static final int COLTYPE_PEPTIDE_DELTA_MOZ = 6; // COLTYPE_PEPTIDE_PPM = 6;
    public static final int COLTYPE_PEPTIDE_CHARGE = 7;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 8;
    //public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 9;  //JPM.TODO : retention time (=elution time) is for the moment in PeptideInstance
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 9;
    public static final int COLTYPE_PEPTIDE_PTM = 10;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 11;
    private static final String[] m_columnNamesRset = {"Peptide", "Score", "MsQuery", "Rank", "Calc. Mass", "Exp. MoZ", "Delta MoZ" /*"Ppm"*/, "Charge", "Missed Cl.", /*"RT",*/ "Ion Parent Int.", "PTM"};
    private static final String[] m_columnNamesRsm =  {"Peptide", "Score", "MsQuery", "Rank", "Calc. Mass", "Exp. MoZ", "Delta MoZ" /*"Ppm"*/, "Charge", "Missed Cl.", /*"RT",*/ "Ion Parent Int.", "PTM", "Protein Sets"};
    
    private DPeptideMatch[] m_peptideMatches = null; // some of the DPeptideMatch can be null : they are loaded in sub task
    private long[] m_peptideMatchesId = null;

    private boolean m_forRSM;

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    
    public PeptideMatchTableModel(LazyTable table, boolean forRSM) {
        super(table);
        
        m_forRSM = forRSM;
    }
    
    
    public DPeptideMatch getPeptideMatch(int row) {
        
        if (m_filteredIds != null) {
            row = m_filteredIds.get(row).intValue();
        }
        
        return m_peptideMatches[row];

    }
    
    @Override
    public int getColumnCount() {
        if (m_forRSM) {
            return m_columnNamesRsm.length;
        } else {
            return m_columnNamesRset.length;
        }
        
    }

    @Override
    public String getColumnName(int col) {
        if (m_forRSM) {
            return m_columnNamesRsm[col];
        } else {
            return m_columnNamesRset[col];
        }
    }

    @Override
    public Class getColumnClass(int col) {
        
        return LazyData.class;        
    }


    @Override
    public int getSubTaskId(int col) {
 
        switch (col) {
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: // no data read at all (we only have the id of the PeptideMatch)
                return DatabaseLoadPeptideMatchTask.SUB_TASK_PEPTIDE_MATCH;
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_PTM:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_PEPTIDE;
            case COLTYPE_PEPTIDE_MSQUERY:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_MSQUERY;
            /*
             * case COLTYPE_PEPTIDE_RETENTION_TIME:
             */
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_MSQUERY;
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_PROTEINSET_NAME_LIST;

        }
        return -1;
    }
    
    @Override
    public int getRowCount() {
        if (m_peptideMatches == null) {
            return 0;
        }
        
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        
        return m_peptideMatches.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        
        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatches[rowFiltered];

        LazyData lazyData = getLazyData(row,col);
        
        if (peptideMatch == null) {
            givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MISSED_CLIVAGE); // no data att all : need to read PeptideMatch like for the COLTYPE_PEPTIDE_MISSED_CLIVAGE column
            lazyData.setData(null);
            return lazyData;
        }
        
        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {

                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    lazyData.setData(peptide);
                }
                
                return lazyData;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match

                Float score = Float.valueOf((float) peptideMatch.getScore()) ;
                lazyData.setData(score);
                return lazyData;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {

                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    
                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    lazyData.setData( msQuery );
                }
                return lazyData;

   
            }
            case COLTYPE_PEPTIDE_RANK: {
                lazyData.setData(peptideMatch.getRank());
            }

            case COLTYPE_PEPTIDE_CHARGE: {
                lazyData.setData(peptideMatch.getCharge());
                return lazyData;
            }
             
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                lazyData.setData(Float.valueOf((float) peptideMatch.getExperimentalMoz()));
                return lazyData;
            }
            case COLTYPE_PEPTIDE_DELTA_MOZ: {
                lazyData.setData(Float.valueOf((float) peptideMatch.getDeltaMoz()));
                return lazyData;
            }
            /*case COLTYPE_PEPTIDE_PPM: { 
                
                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                }
                
                
                double deltaMoz = (double) peptideMatch.getDeltaMoz();
                double calculatedMass = peptide.getCalculatedMass() ;
                double charge = (double) peptideMatch.getCharge(); 
                final double CSTE = 1.007825;
                final double EXP_CSTE = StrictMath.pow(10, 6);
                float ppm = (float) ((deltaMoz*EXP_CSTE) / ((calculatedMass + (charge*CSTE))/charge));
                
                //double deltaMoz
                lazyData.setData(Float.valueOf(ppm));
                return  lazyData;
            }*/
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {

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
                lazyData.setData(Float.valueOf(peptideMatch.getMissedCleavage()));
                return lazyData;
            }
            /*case COLTYPE_PEPTIDE_RETENTION_TIME: { //JPM.TODO : can not do it for the moment: Retention Time is on Peptide Instance
                
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
                            lazyData.setData("");
                        } else {
                            double retentionTime = (spectrum.getFirstTime()+spectrum.getLastTime())/2;
                            lazyData.setData(DataFormat.format(retentionTime, 2));
                        }
                    }  
                }
                return lazyData;
            }*/
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {

                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MSQUERY);
                    lazyData.setData(null);
                    
                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    Float precursorIntenstity = msQuery.getPrecursorIntensity();
                    if (precursorIntenstity == null) {
                        lazyData.setData("");
                    } else {
                        lazyData.setData(precursorIntenstity);
                    }  
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_PTM: {

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
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {

                String proteinSetNames = peptideMatch.getProteinSetStringList();
                if (proteinSetNames == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    lazyData.setData(proteinSetNames);
                }
                return lazyData;
            }
                
        }
        return null; // should never happen
    }

    public void setData(Long taskId, DPeptideMatch[] peptideMatches, long[] peptideMatchesId) {
        m_peptideMatches = peptideMatches;
        m_peptideMatchesId = peptideMatchesId;
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
            DPeptideMatch peptideMatch = m_peptideMatches[i];
            if (peptideMatch == null) {
                continue;
            }
            double score = peptideMatch.getScore();
            if (score > maxScore) {
                maxScore = score;
            }
        }
        relativizer.setMax(maxScore);

    }
    
    public DPeptideMatch[] getPeptideMatches() {
        return m_peptideMatches;
    }
    
    public long getResultSetId() {
        if ((m_peptideMatches == null) || (m_peptideMatches.length == 0)) {
            return -1;
        }
        return m_peptideMatches[0].getResultSetId();
    }

    public void dataUpdated() {
        
        // call to updateMinMax() is not necessary : peptideMatch are loaded in one time
        
        if (m_filteredIds != null) {
            filter();
        } else {
            fireTableDataChanged();
        }
    }
    
    public int findRow(long peptideMatchId) {

        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (peptideMatchId == m_peptideMatchesId[m_filteredIds.get(i)]) {
                    return i;
                }
            }
            return -1;
        }


        int nb = m_peptideMatches.length;
        for (int i = 0; i < nb; i++) {
            if (peptideMatchId == m_peptideMatchesId[i]) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideMatchIds) {
        
        if (m_peptideMatches == null) {
            // data not loaded 
            return;
        }
        
        HashSet<Long> peptideMatchIdMap = new HashSet<>(peptideMatchIds.size());
        peptideMatchIdMap.addAll(peptideMatchIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);

            Long psId = m_peptideMatchesId[iModel];
            if (peptideMatchIdMap.contains(psId)) {
                peptideMatchIds.set(iCur++, psId);
            }

        }
        
        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }

    }
    
    @Override
    public void filter() {

        if (m_peptideMatches == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_peptideMatches.length;
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
            case COLTYPE_PEPTIDE_NAME:
                return ((StringFilter) filter).filter(((Peptide)data).getSequence());
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {
                return ((StringFilter) filter).filter((String)data);
            }
            case COLTYPE_PEPTIDE_DELTA_MOZ: {  // COLTYPE_PEPTIDE_PPM
                return ((DoubleFilter) filter).filter((Float)data);
            }  
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
              if (data instanceof String) {
                  return true;
              } else {
                  return ((DoubleFilter) filter).filter((Float)data);
              }
            } case COLTYPE_PEPTIDE_CHARGE: {
                return ((IntegerFilter) filter).filter((Integer)data);
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                return ((IntegerFilter) filter).filter(((DMsQuery)data).getInitialId());
            }
    
        }
        
        return true; // should never happen
    }


    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PEPTIDE_NAME] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_NAME));
            m_filters[COLTYPE_PEPTIDE_SCORE] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE));
            m_filters[COLTYPE_PEPTIDE_MSQUERY] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MSQUERY));
            m_filters[COLTYPE_PEPTIDE_CALCULATED_MASS] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS));
            m_filters[COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ));
            m_filters[COLTYPE_PEPTIDE_DELTA_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_DELTA_MOZ));  // COLTYPE_PEPTIDE_PPM
            
            m_filters[COLTYPE_PEPTIDE_CHARGE] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE));
            m_filters[COLTYPE_PEPTIDE_MISSED_CLIVAGE] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLIVAGE));
            m_filters[COLTYPE_PEPTIDE_ION_PARENT_INTENSITY] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY));
            m_filters[COLTYPE_PEPTIDE_PTM] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM));
            if (m_forRSM) {
                m_filters[COLTYPE_PEPTIDE_PROTEIN_SET_NAMES] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES));
            }
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
