package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.LazyTable;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
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
 * Table Model for PeptideMatch of a RSet
 * @author JM235353
 */
public class PeptideMatchTableModel extends LazyTableModel implements GlobalTableModelInterface  {

    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_PEPTIDE_PREVIOUS_AA = 1;
    public static final int COLTYPE_PEPTIDE_NAME = 2;
    public static final int COLTYPE_PEPTIDE_NEXT_AA = 3;
    public static final int COLTYPE_PEPTIDE_SCORE = 4;
    public static final int COLTYPE_PEPTIDE_START = 5;
    public static final int COLTYPE_PEPTIDE_STOP = 6;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 7;
    public static final int COLTYPE_PEPTIDE_RANK = 8;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 9;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 10;
    public static final int COLTYPE_PEPTIDE_PPM = 11;  //COLTYPE_PEPTIDE_DELTA_MOZ = 8;
    public static final int COLTYPE_PEPTIDE_CHARGE = 12;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 13;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 14;
    public static final int COLTYPE_PEPTIDE_PTM = 15;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 16;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 17;
    private static final String[] m_columnNames =  {"Id", "Prev. AA", "Peptide", "Next AA", "Score", "Start", "Stop", "MsQuery", "Rank", "Calc. Mass", "Exp. MoZ", "Ppm" /*"Delta MoZ"*/, "Charge", "Missed Cl.", "Ion Parent Int.", "PTM", "Protein Sets", "RT"};
    private static final String[] m_columnTooltips = {"PeptideMatch Id", "Previous Amino Acid","Peptide", "Next Amino Acid", "Score", "Start", "Stop", "MsQuery", "Rank", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million" , "Charge", "Missed Clivage", "Ion Parent Intensity", "Post Translational Modifications", "Protein Sets", "Retention Time"};
    
    private int[] m_colUsed = null;

    
    
    private DPeptideMatch[] m_peptideMatches = null; // some of the DPeptideMatch can be null : they are loaded in sub task
    private long[] m_peptideMatchesId = null;

    private final boolean m_forRSM;
    private final boolean m_hasPrevNextAA;


    private String m_modelName;
    
    public PeptideMatchTableModel(LazyTable table, boolean forRSM, boolean hasPrevNextAA) {
        super(table);
        
        if (forRSM) {
            if (hasPrevNextAA) {
                
                final int[] colUsed = {COLTYPE_PEPTIDE_ID, COLTYPE_PEPTIDE_PREVIOUS_AA, COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_NEXT_AA, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_START, COLTYPE_PEPTIDE_STOP, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, COLTYPE_PEPTIDE_RETENTION_TIME};
                m_colUsed = colUsed;
            } else {
                final int[] colUsed = {COLTYPE_PEPTIDE_ID, COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, /*"RT",*/ COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, COLTYPE_PEPTIDE_RETENTION_TIME};
                m_colUsed = colUsed;
            }
        } else {
            if (hasPrevNextAA) {
                final int[] colUsed = {COLTYPE_PEPTIDE_ID, COLTYPE_PEPTIDE_PREVIOUS_AA, COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_NEXT_AA, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_START, COLTYPE_PEPTIDE_STOP, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM, COLTYPE_PEPTIDE_RETENTION_TIME};
                m_colUsed = colUsed;
            } else {
                final int[] colUsed = {COLTYPE_PEPTIDE_ID, COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_SCORE, COLTYPE_PEPTIDE_MSQUERY, COLTYPE_PEPTIDE_RANK, COLTYPE_PEPTIDE_CALCULATED_MASS, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, COLTYPE_PEPTIDE_PPM /*COLTYPE_PEPTIDE_DELTA_MOZ*/, COLTYPE_PEPTIDE_CHARGE, COLTYPE_PEPTIDE_MISSED_CLIVAGE, /*"RT",*/ COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, COLTYPE_PEPTIDE_PTM, COLTYPE_PEPTIDE_RETENTION_TIME};
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
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        
        if (col == COLTYPE_PEPTIDE_ID) {
            return Long.class;
        }
        
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
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_PEPTIDE;
            case COLTYPE_PEPTIDE_MSQUERY:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_MSQUERY;
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
        
        LazyData lazyData = getLazyData(row,col);
        
        col =  m_colUsed[col];

        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatches[row];

        
        
        if (peptideMatch == null) {
            givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MISSED_CLIVAGE); // no data att all : need to read PeptideMatch like for the COLTYPE_PEPTIDE_MISSED_CLIVAGE column
            lazyData.setData(null);
            return lazyData;
        }
        
        switch (col) {
            case COLTYPE_PEPTIDE_ID: {
                return peptideMatch.getId();
            }
            case COLTYPE_PEPTIDE_PREVIOUS_AA: {
 
                /*Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {*/
                    SequenceMatch sequenceMatch = peptideMatch.getSequenceMatch();
                    if (sequenceMatch == null) {
                        givePriorityTo(m_taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        Character residueBefore = sequenceMatch.getResidueBefore();
                        if (residueBefore != null) {
                             lazyData.setData(String.valueOf(Character.toUpperCase(residueBefore)));
                        } else {
                            lazyData.setData("");
                        }
                    }
                    
                //}
                
                return lazyData; 
            }
            case COLTYPE_PEPTIDE_NAME: {
                if (m_hasPrevNextAA) {
                    SequenceMatch sequenceMatch = peptideMatch.getSequenceMatch();
                    if (sequenceMatch == null) {
                        givePriorityTo(m_taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        lazyData.setData(peptideMatch);
                    }


                } else {
                    lazyData.setData(peptideMatch);
                }
                
                return lazyData;
            }
            case COLTYPE_PEPTIDE_NEXT_AA: {
 
                /*Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {*/
                    SequenceMatch sequenceMatch = peptideMatch.getSequenceMatch();
                    if (sequenceMatch == null) {
                        givePriorityTo(m_taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        Character residueAfter = sequenceMatch.getResidueAfter();
                        if (residueAfter != null) {
                             lazyData.setData(String.valueOf(Character.toUpperCase(residueAfter)));
                        } else {
                            lazyData.setData("");
                        }
                    }
                    
                //}
                
                return lazyData; 
            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match

                Float score = Float.valueOf((float) peptideMatch.getScore()) ;
                lazyData.setData(score);
                return lazyData;
            }
            case COLTYPE_PEPTIDE_START: {
                /*Peptide peptide = peptideMatch.getPeptide();
                if (peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {*/
                    SequenceMatch sequenceMatch = peptideMatch.getSequenceMatch();
                    if (sequenceMatch == null) {
                        givePriorityTo(m_taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        int start = sequenceMatch.getId().getStart();
                        lazyData.setData(Integer.valueOf(start));
                    }

               // }

                return lazyData;
            }
            case COLTYPE_PEPTIDE_STOP: {
                /*Peptide peptide = peptideMatch.getPeptide();
                if (peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {*/
                    SequenceMatch sequenceMatch = peptideMatch.getSequenceMatch();
                    if (sequenceMatch == null) {
                         givePriorityTo(m_taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        int stop = sequenceMatch.getId().getStop();
                        lazyData.setData(Integer.valueOf(stop));
                    }

                //}

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
               lazyData.setData(peptideMatch.getCDPrettyRank() == null? "" : peptideMatch.getCDPrettyRank()); // could be null for quantitation datasetnode
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
                    return lazyData;
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
                lazyData.setData(Integer.valueOf(peptideMatch.getMissedCleavage()));
                return lazyData;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
               
                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    
                } else {
                    Float retentionTime = peptideMatch.getRetentionTime();
                    if (retentionTime== null) {
                        lazyData.setData( Float.NaN );  // JPM.WART will not happen in the future
                    } else {
                        lazyData.setData( retentionTime );
                    }
                }
                return lazyData;

   
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {

                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MSQUERY);
                    lazyData.setData(null);
                    
                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    Float precursorIntenstity = msQuery.getPrecursorIntensity();
                    if (precursorIntenstity == null) {
                        lazyData.setData(Float.NaN);
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
        
        fireTableDataChanged();

    }

    private void updateMinMax() {

        if (m_peptideMatches == null) {
            return;
        }
        
        RelativePainterHighlighter.NumberRelativizer relativizer = m_table.getRelativizer();
        if (relativizer == null) {
            return;
        }
     
        double maxScore = 0;
        int size = m_peptideMatches.length;
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
    
    public int findRow(long peptideMatchId) {

        int nb = m_peptideMatches.length;
        for (int i = 0; i < nb; i++) {
            if (peptideMatchId == m_peptideMatchesId[i]) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideMatchIds, CompoundTableModel compoundTableModel) {
        
        if (m_peptideMatches == null) {
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
            Long psId = m_peptideMatchesId[iModel];
            if (peptideMatchIdMap.contains(psId)) {
                peptideMatchIds.set(iCur++, psId);
            }

        }


    }

    
    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        
        int colIdx = 0;
        colIdx++;//COLTYPE_PEPTIDE_ID
        
        // COLTYPE_PEPTIDE_PREVIOUS_AA
        if (m_hasPrevNextAA) {
            filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null));
            colIdx++;
        }
        
        ConvertValueInterface peptideConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DPeptideMatch) o).getPeptide().getSequence();
            }
            
        };
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), peptideConverter)); //COLTYPE_PEPTIDE_NAME
        colIdx++;

        
        // COLTYPE_PEPTIDE_NEXT_AA
        if (m_hasPrevNextAA) {
            filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null));
            colIdx++;
        }
            
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null)); //COLTYPE_PEPTIDE_SCORE
        colIdx++;

        // COLTYPE_PEPTIDE_START and COLTYPE_PEPTIDE_STOP
        if (m_hasPrevNextAA) {
            filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null)); colIdx++; // COLTYPE_PEPTIDE_START
            filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null)); colIdx++; // COLTYPE_PEPTIDE_STOP
        }
           
                    
        
        ConvertValueInterface msQueryConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DMsQuery) o).getInitialId();
            }
            
        };
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), msQueryConverter));  colIdx++;//COLTYPE_PEPTIDE_MSQUERY
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_RANK
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_CALCULATED_MASS
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_DELTA_MOZ  (COLTYPE_PEPTIDE_PPM)
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_CHARGE
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_MISSED_CLIVAGE
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_ION_PARENT_INTENSITY
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_PTM
            
        //COLTYPE_PEPTIDE_PROTEIN_SET_NAMES
        if (m_forRSM) {
            filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null)); colIdx++;
        }
        //filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null));  colIdx++;//COLTYPE_PEPTIDE_RETENTION_TIME

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
        columnIndex =  m_colUsed[columnIndex];
        switch (columnIndex) {
            case COLTYPE_PEPTIDE_PREVIOUS_AA:
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_NEXT_AA:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_PPM:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
                return Float.class;
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:
            case COLTYPE_PEPTIDE_MSQUERY:
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
                return Integer.class;
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return Object.class; // Float or String... JPM.TODO
        }
        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            
            if (data instanceof DPeptideMatch) {
                data = ((DPeptideMatch)data).getPeptide().getSequence();
            } else if (data instanceof DMsQuery) {
                data = Integer.valueOf(((DMsQuery)data).getInitialId());
            }

        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { convertColToColUsed(COLTYPE_PEPTIDE_NAME), convertColToColUsed(COLTYPE_PEPTIDE_ID) };
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
    public PlotType getBestPlotType() {
        return PlotType.HISTOGRAM_PLOT;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case HISTOGRAM_PLOT:
                return convertColToColUsed(COLTYPE_PEPTIDE_PPM);
            case SCATTER_PLOT:
                return convertColToColUsed(COLTYPE_PEPTIDE_CALCULATED_MASS);
        }
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case SCATTER_PLOT:
                return convertColToColUsed(COLTYPE_PEPTIDE_SCORE);
        }
        return -1;
    }

    @Override
    public int getInfoColumn() {
        return convertColToColUsed(COLTYPE_PEPTIDE_NAME);
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
    public String getExportRowCell(int row, int col) {
        return null; // no specific export
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
        col =  m_colUsed[col];
        
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }
        
        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                renderer =  new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: 
            case COLTYPE_PEPTIDE_CALCULATED_MASS: 
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                renderer = new FloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)),4 );
                break;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                renderer = new MsQueryRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_PPM: {
                renderer = new FloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }

            
        }
        m_rendererMap.put(col, renderer);
        return renderer;
    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

}
