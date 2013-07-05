package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.*;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.utils.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Table Model for PeptideInstance of a Rsm
 * @author JM235353
 */
public class PeptideInstanceTableModel extends LazyTableModel {

    public static final int COLTYPE_PEPTIDE_NAME = 0;
    public static final int COLTYPE_PEPTIDE_SCORE = 1;
    //public static final int COLTYPE_PEPTIDE_MSQUERY = 2;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 2;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 3;
    public static final int COLTYPE_PEPTIDE_DELTA_MOZ = 4;
    public static final int COLTYPE_PEPTIDE_CHARGE = 5;
    
    
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 6;
    public static final int COLTYPE_PEPTIDE_NB_PROTEIN_SETS = 7;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 8; 
    //public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 8;
    public static final int COLTYPE_PEPTIDE_PTM = 9;
    private static final String[] columnNames = {"Peptide", "Score", "Mass Calc.", "Exp. MoZ", "Delta MoZ", "Charge", "Missed Cl.", "Protein Set Count", "RT", "PTM"};
    private PeptideInstance[] m_peptideInstances = null;

    
    public PeptideInstanceTableModel(LazyTable table) {
        super(table);
    }
    
    
    public PeptideInstance getPeptideInstance(int row) {
        return m_peptideInstances[row];

    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class getColumnClass(int col) {
        
        switch (col) {
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            //case COLTYPE_PEPTIDE_RETENTION_TIME:
            //case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PTM:
                return LazyData.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_DELTA_MOZ:
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
        PeptideMatch peptideMatch = peptideInstance.getTransientData().getBestPeptideMatch();

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
            case COLTYPE_PEPTIDE_DELTA_MOZ: {
                return Float.valueOf((float) peptideMatch.getDeltaMoz());
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
        }
        return null; // should never happen
    }

    public void setData(Long taskId, PeptideInstance[] peptideInstances) {
        m_peptideInstances = peptideInstances;
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
            PeptideInstance peptideInstance = m_peptideInstances[i];
            double score = peptideInstance.getTransientData().getBestPeptideMatch().getScore();
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

    public void sortAccordingToModel(ArrayList<Long> peptideMatchIds) {
        HashSet<Long> peptideMatchIdMap = new HashSet<>(peptideMatchIds.size());
        peptideMatchIdMap.addAll(peptideMatchIds);

        int nb = m_peptideInstances.length;
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            PeptideInstance ps = m_peptideInstances[iModel];
            if (peptideMatchIdMap.contains(ps.getId())) {
                peptideMatchIds.set(iCur++, ps.getId());
            }
        }

    }
}
