package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.*;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
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
    
    private PeptideMatch[] m_peptideMatches = null;

    private boolean m_forRSM;
    
    public PeptideMatchTableModel(LazyTable table, boolean forRSM) {
        super(table);
        
        m_forRSM = forRSM;
    }
    
    
    public PeptideMatch getPeptideMatch(int row) {
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
        
        switch (col) {
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_MSQUERY:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            //case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
                return LazyData.class;
            case COLTYPE_PEPTIDE_DELTA_MOZ:
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
                return Float.class;
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
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
            case COLTYPE_PEPTIDE_MSQUERY:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_MSQUERY;
            /*
             * case COLTYPE_PEPTIDE_RETENTION_TIME:
             */
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_SPECTRUM;
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
        PeptideMatch peptideMatch = m_peptideMatches[row];

        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                Peptide peptide = data.getPeptide();
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
                return score;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                if (!data.getIsMsQuerySet()) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    
                } else {
                    MsQuery msQuery = peptideMatch.getMsQuery();
                    lazyData.setData( msQuery );
                }
                return lazyData;

   
            }

            case COLTYPE_PEPTIDE_CHARGE: {
                return peptideMatch.getCharge();
            }
             
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                return Float.valueOf((float) peptideMatch.getExperimentalMoz()) ;
            }
            case COLTYPE_PEPTIDE_DELTA_MOZ: {
                return Float.valueOf((float) peptideMatch.getDeltaMoz()) ;
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                Peptide peptide = data.getPeptide();
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
                
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                if (!data.getIsMsQuerySet()) {
                    givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MSQUERY);
                    lazyData.setData(null);
                    
                } else {
                    MsQuery msQuery = peptideMatch.getMsQuery();
                    
                    if (!msQuery.getTransientIsSpectrumSet()) {
                        givePriorityTo(m_taskId, row, col);
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

            }
            case COLTYPE_PEPTIDE_PTM: {
                LazyData lazyData = getLazyData(row, col);

                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                Peptide peptide = data.getPeptide();
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
                LazyData lazyData = getLazyData(row, col);
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                String proteinSetNames = data.getProteinSetStringList();
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

    public void setData(Long taskId, PeptideMatch[] peptideMatches) {
        this.m_peptideMatches = peptideMatches;
        this.m_taskId = taskId;
        
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
            PeptideMatch peptideMatch = m_peptideMatches[i];
            double score = peptideMatch.getScore();
            if (score > maxScore) {
                maxScore = score;
            }
        }
        relativizer.setMax(maxScore);

    }
    
    public PeptideMatch[] getPeptideMatches() {
        return m_peptideMatches;
    }
    
    public ResultSet getResultSet() {
        if ((m_peptideMatches == null) || (m_peptideMatches.length == 0)) {
            return null;
        }
        return m_peptideMatches[0].getResultSet();
    }

    public void dataUpdated() {
        
        // call to updateMinMax() is not necessary : peptideMatch are loaded in one time
        
        fireTableDataChanged();
    }
    
    public int findRow(long proteinSetId) {
        int nb = m_peptideMatches.length;
        for (int i = 0; i < nb; i++) {
            if (proteinSetId == m_peptideMatches[i].getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideMatchIds) {
        HashSet<Long> peptideMatchIdMap = new HashSet<>(peptideMatchIds.size());
        peptideMatchIdMap.addAll(peptideMatchIds);

        int nb = m_peptideMatches.length;
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            PeptideMatch ps = m_peptideMatches[iModel];
            if (peptideMatchIdMap.contains(ps.getId())) {
                peptideMatchIds.set(iCur++, ps.getId());
            }
        }

    }
}
