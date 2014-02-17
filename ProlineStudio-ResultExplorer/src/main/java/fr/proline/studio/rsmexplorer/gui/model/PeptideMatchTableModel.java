package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.SequenceMatch;
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

    public static final int COLTYPE_PEPTIDE_PREVIOUS_AA = 0;
    public static final int COLTYPE_PEPTIDE_NAME = 1;
    public static final int COLTYPE_PEPTIDE_NEXT_AA = 2;
    public static final int COLTYPE_PEPTIDE_SCORE = 3;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 4;
    public static final int COLTYPE_PEPTIDE_RANK = 5;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 6;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 7;
    public static final int COLTYPE_PEPTIDE_PPM = 8;  //COLTYPE_PEPTIDE_DELTA_MOZ = 8;
    public static final int COLTYPE_PEPTIDE_CHARGE = 9;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 10;
    //public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 9;  //JPM.TODO : retention time (=elution time) is for the moment in PeptideInstance
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 11;
    public static final int COLTYPE_PEPTIDE_PTM = 12;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 13;
    private static final String[] m_columnNames =  {"Prev. AA", "Peptide", "Next AA", "Score", "MsQuery", "Rank", "Calc. Mass", "Exp. MoZ", "Ppm" /*"Delta MoZ"*/, "Charge", "Missed Cl.", /*"RT",*/ "Ion Parent Int.", "PTM", "Protein Sets"};
    private static final String[] m_columnTooltips = {"Previous Amino Acid","Peptide", "Next Amino Acid", "Score", "MsQuery", "Rank", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million" /* "Delta Mass to Charge Ratio"*/, "Charge", "Missed Clivage", /*"RT",*/ "Ion Parent Intensity", "Post Translational Modifications", "Protein Sets"};
    
    private int[] m_colUsed = null;

    
    
    private DPeptideMatch[] m_peptideMatches = null; // some of the DPeptideMatch can be null : they are loaded in sub task
    private long[] m_peptideMatchesId = null;

    private boolean m_forRSM;
    private boolean m_hasPrevNextAA;

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    
    public PeptideMatchTableModel(LazyTable table, boolean forRSM, boolean hasPrevNextAA) {
        super(table);
        
        if (forRSM) {
            if (hasPrevNextAA) {
                
                final int[] colUsed = {COLTYPE_PEPTIDE_PREVIOUS_AA, COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_NEXT_AA, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, /*"RT",*/ COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES};
                m_colUsed = colUsed;
            } else {
                final int[] colUsed = {COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, /*"RT",*/ COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES};
                m_colUsed = colUsed;
            }
        } else {
            if (hasPrevNextAA) {
                final int[] colUsed = {COLTYPE_PEPTIDE_PREVIOUS_AA, COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_NEXT_AA, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, /*"RT",*/ COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM};
                m_colUsed = colUsed;
            } else {
                final int[] colUsed = {COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, /*"RT",*/ COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM};
                m_colUsed = colUsed;
            }
        }

        m_forRSM = forRSM;
        m_hasPrevNextAA = hasPrevNextAA;
    }
    
    public int convertColToColUsed(int col) {
        for (int i=0; i<m_colUsed.length; i++) {
            if (col == m_colUsed[i]) {
                return i;
            }
        }
        return -1; // should not happen
    }
    
    
    public DPeptideMatch getPeptideMatch(int row) {
        
        if (m_filteredIds != null) {
            row = m_filteredIds.get(row).intValue();
        }
        
        return m_peptideMatches[row];

    }
    
    @Override
    public int getColumnCount() {
        return m_colUsed.length;
     
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[m_colUsed[col]];
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_columnTooltips[m_colUsed[col]];
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
            case COLTYPE_PEPTIDE_PREVIOUS_AA:
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_NEXT_AA:
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
        
        LazyData lazyData = getLazyData(row,col);
        
        col =  m_colUsed[col];
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        
        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatches[rowFiltered];

        
        
        if (peptideMatch == null) {
            givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MISSED_CLIVAGE); // no data att all : need to read PeptideMatch like for the COLTYPE_PEPTIDE_MISSED_CLIVAGE column
            lazyData.setData(null);
            return lazyData;
        }
        
        switch (col) {
            case COLTYPE_PEPTIDE_PREVIOUS_AA: {
 
                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    SequenceMatch sequenceMatch = peptide.getTransientData().getSequenceMatch();
                    if (sequenceMatch == null) {
                        lazyData.setData("");
                    } else {
                        Character residueBefore = sequenceMatch.getResidueBefore();
                        if (residueBefore != null) {
                             lazyData.setData(Character.toUpperCase(residueBefore));
                        } else {
                            lazyData.setData("");
                        }
                    }
                    
                }
                
                return lazyData; 
            }
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
            case COLTYPE_PEPTIDE_NEXT_AA: {
 
                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    SequenceMatch sequenceMatch = peptide.getTransientData().getSequenceMatch();
                    if (sequenceMatch == null) {
                        lazyData.setData("");
                    } else {
                        Character residueAfter = sequenceMatch.getResidueAfter();
                        if (residueAfter != null) {
                             lazyData.setData(Character.toUpperCase(residueAfter));
                        } else {
                            lazyData.setData("");
                        }
                    }
                    
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
                return lazyData;
            }

            case COLTYPE_PEPTIDE_CHARGE: {
                lazyData.setData(peptideMatch.getCharge());
                return lazyData;
            }
             
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                lazyData.setData(Float.valueOf((float) peptideMatch.getExperimentalMoz()));
                return lazyData;
            }
            /*case COLTYPE_PEPTIDE_DELTA_MOZ: {
                lazyData.setData(Float.valueOf((float) peptideMatch.getDeltaMoz()));
                return lazyData;
            }*/
            case COLTYPE_PEPTIDE_PPM: { 
                
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
                
                if ((calculatedMass >= -1e-10) && (calculatedMass <= 1e-10)) {
                    //calculatedMass == 0; // it was a bug, does no longer exist, but 0 values can exist in database.
                    ppm = 0;
                }

                lazyData.setData(Float.valueOf(ppm));
                return  lazyData;
            }
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
        
        Object data = getValueAt(row, m_colUsed[col]);
        if (data == null) {
            return true; // should not happen
        }
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            if (data == null) {
                return true; // should not happen
            }
        }
        
        switch (m_colUsed[col]) {
            case COLTYPE_PEPTIDE_PREVIOUS_AA: 
            case COLTYPE_PEPTIDE_NEXT_AA:{
                return ((StringFilter) filter).filter(data.toString());
            }
            case COLTYPE_PEPTIDE_NAME:
                return ((StringFilter) filter).filter(((Peptide)data).getSequence());
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {
                return ((StringFilter) filter).filter((String)data);
            }
            case COLTYPE_PEPTIDE_PPM: {  //  COLTYPE_PEPTIDE_DELTA_MOZ
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
            int colIdx = 0;
            
            // COLTYPE_PEPTIDE_PREVIOUS_AA
            if (m_hasPrevNextAA) {
                m_filters[colIdx] = new StringFilter(getColumnName(colIdx)); colIdx++;
            }
            
            m_filters[colIdx] = new StringFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_NAME
            
            //COLTYPE_PEPTIDE_NEXT_AA
            if (m_hasPrevNextAA) {
                m_filters[colIdx] = new StringFilter(getColumnName(colIdx)); colIdx++;
            }
            
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_SCORE
            m_filters[colIdx] = new IntegerFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_MSQUERY
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_CALCULATED_MASS
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_DELTA_MOZ  (COLTYPE_PEPTIDE_PPM)
            m_filters[colIdx] = new IntegerFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_CHARGE
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_MISSED_CLIVAGE
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_ION_PARENT_INTENSITY
            m_filters[colIdx] = new StringFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_PTM
            
            //COLTYPE_PEPTIDE_PROTEIN_SET_NAMES
            if (m_forRSM) {
                m_filters[colIdx] = new StringFilter(getColumnName(colIdx)); colIdx++;
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
