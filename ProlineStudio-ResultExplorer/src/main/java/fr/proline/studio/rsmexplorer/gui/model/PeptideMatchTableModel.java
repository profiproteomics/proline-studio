package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
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
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 3;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 4;
    public static final int COLTYPE_PEPTIDE_DELTA_MOZ = 5;
    public static final int COLTYPE_PEPTIDE_CHARGE = 6;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 7;
    //public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 8;  //JPM.TODO : retention time (=elution time) is for the moment in PeptideInstance
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 8;
    public static final int COLTYPE_PEPTIDE_PTM = 9;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 10;
    private static final String[] m_columnNamesRset = {"Peptide", "Score", "MsQuery", "Calc. Mass", "Exp. MoZ", "Delta MoZ", "Charge", "Missed Cl.", /*"RT",*/ "Ion Parent Int.", "PTM"};
    private static final String[] m_columnNamesRsm =  {"Peptide", "Score", "MsQuery", "Calc. Mass", "Exp. MoZ", "Delta MoZ", "Charge", "Missed Cl.", /*"RT",*/ "Ion Parent Int.", "PTM", "Protein Sets"};
    
    private DPeptideMatch[] m_peptideMatches = null; // some of the DPeptideMatch can be null : they are loaded in sub task
    private long[] m_peptideMatchesId = null;

    private boolean m_forRSM;
    
    public PeptideMatchTableModel(LazyTable table, boolean forRSM) {
        super(table);
        
        m_forRSM = forRSM;
    }
    
    
    public DPeptideMatch getPeptideMatch(int row) {
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
        return m_peptideMatches.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatches[row];

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
                } else {
                    String ptmString = peptide.getPtmString();
                    if (ptmString != null) {
                        lazyData.setData(ptmString);
                    } else {
                        lazyData.setData("");
                    }
                    
                }
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
        
        fireTableDataChanged();

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
        
        fireTableDataChanged();
    }
    
    public int findRow(long proteinSetId) {
        int nb = m_peptideMatches.length;
        for (int i = 0; i < nb; i++) {
            if (proteinSetId == m_peptideMatchesId[i]) {
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

        int nb = m_peptideMatches.length;
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);

            Long psId = m_peptideMatchesId[iModel];
            if (peptideMatchIdMap.contains(psId)) {
                peptideMatchIds.set(iCur++, psId);
            }

        }

    }
}
