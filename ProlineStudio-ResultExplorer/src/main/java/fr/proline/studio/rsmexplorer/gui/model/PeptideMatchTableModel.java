package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.LazyTable;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.BooleanFilter;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.BooleanRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TableDefaultRendererManager;
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
    public static final int COLTYPE_PEPTIDE_PTM = 4;
    public static final int COLTYPE_PEPTIDE_SCORE = 5;
    public static final int COLTYPE_PEPTIDE_START = 6;
    public static final int COLTYPE_PEPTIDE_STOP = 7;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 8;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 9;
    public static final int COLTYPE_PEPTIDE_PPM = 10;
    public static final int COLTYPE_PEPTIDE_CHARGE = 11;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 12;
    public static final int COLTYPE_PEPTIDE_RANK = 13;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 14;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_COUNT = 15;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 16;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 17;
    public static final int COLTYPE_PEPTIDE_IS_DECOY = 18;
    public static final int COLTYPE_PEPTIDE_IS_VALIDATED = 19;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 20;
    public static final int COLTYPE_SRC_DAT_FILE = 21;
    public static final int COLTYPE_SPECTRUM_TITLE = 22;
    
    private static final String[] m_columnNames =  {"Id", "Prev. AA", "Peptide", "Next AA", "PTM", "Score", "Start", "Stop", "Calc. Mass", "Exp. MoZ", "Ppm", "Charge", "Missed Cl.", "Rank", "RT", "Protein Set Count", "Protein Sets", "Ion Parent Int.","Decoy", "Validated", "MsQuery", ".dat File", "Spectrum Title"};
    private static final String[] m_columnTooltips = {"PeptideMatch Id", "Previous Amino Acid","Peptide", "Next Amino Acid", "Post Translational Modifications", "Score", "Start", "Stop", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million" , "Charge", "Missed Clivage", "Pretty Rank", "Retention Time (min)", "Potein Set Count", "Protein Sets", "Ion Parent Intensity", "Is Decoy", "Is Validated", "MsQuery", ".dat file of best PSM", "Spectrum Title"};
    
    private final ArrayList<Integer> m_colUsed = new ArrayList<>();

    
    
    private DPeptideMatch[] m_peptideMatches = null; // some of the DPeptideMatch can be null : they are loaded in sub task
    private long[] m_peptideMatchesId = null;

    private final boolean m_forRSM;
    private final boolean m_hasPrevNextAA;
    private final boolean m_isDecoyAndValidated;
    private final boolean m_isMerged;

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    private String m_modelName;
    
    public PeptideMatchTableModel(LazyTable table, boolean forRSM, boolean hasPrevNextAA, boolean isDecoyAndValidated, boolean isMerged) {
        super(table);
        
        m_forRSM = forRSM;
        m_hasPrevNextAA = hasPrevNextAA;
        m_isDecoyAndValidated = isDecoyAndValidated;
        m_isMerged = isMerged;

        m_colUsed.add(COLTYPE_PEPTIDE_ID);
        if (hasPrevNextAA) {
            m_colUsed.add(COLTYPE_PEPTIDE_PREVIOUS_AA);
        }
        m_colUsed.add(COLTYPE_PEPTIDE_NAME);
        if (hasPrevNextAA) {
            m_colUsed.add(COLTYPE_PEPTIDE_NEXT_AA);
        }
        m_colUsed.add(COLTYPE_PEPTIDE_PTM);
        m_colUsed.add(COLTYPE_PEPTIDE_SCORE);
        if (hasPrevNextAA) {
            m_colUsed.add(COLTYPE_PEPTIDE_START);
            m_colUsed.add(COLTYPE_PEPTIDE_STOP);
        }
        
        m_colUsed.add(COLTYPE_PEPTIDE_CALCULATED_MASS);
        m_colUsed.add(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ);
        m_colUsed.add(COLTYPE_PEPTIDE_PPM);
        m_colUsed.add(COLTYPE_PEPTIDE_CHARGE);
        m_colUsed.add(COLTYPE_PEPTIDE_MISSED_CLIVAGE);
        m_colUsed.add(COLTYPE_PEPTIDE_RANK);
        m_colUsed.add(COLTYPE_PEPTIDE_RETENTION_TIME);
        
        
        if (forRSM) {
            m_colUsed.add(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT);
            m_colUsed.add(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES);
        }
        m_colUsed.add(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY);
        
        
        if (isDecoyAndValidated){
            m_colUsed.add(COLTYPE_PEPTIDE_IS_DECOY);
            if (forRSM) {
                m_colUsed.add(COLTYPE_PEPTIDE_IS_VALIDATED);
            }
        }
        m_colUsed.add(COLTYPE_PEPTIDE_MSQUERY);
        if (isMerged) {
            m_colUsed.add(COLTYPE_SRC_DAT_FILE);
        }
        m_colUsed.add(COLTYPE_SPECTRUM_TITLE);



    }
    
    public int convertColToColUsed(int col) {
        for (int i=0; i<m_colUsed.size(); i++) {
            if (col == m_colUsed.get(i)) {
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
        return m_colUsed.size();
     
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[m_colUsed.get(col)];
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_columnTooltips[m_colUsed.get(col)];
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        col = m_colUsed.get(col);
        if (col == COLTYPE_PEPTIDE_ID) {
            return Long.class;
        }
        
        return LazyData.class;        
    }


    @Override
    public int getSubTaskId(int col) {
        col = (col == -1) ? -1 : m_colUsed.get(col);
        switch (col) {
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: // no data read at all (we only have the id of the PeptideMatch)
            case COLTYPE_PEPTIDE_IS_DECOY:
            case COLTYPE_PEPTIDE_IS_VALIDATED:
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
            case COLTYPE_SPECTRUM_TITLE:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_MSQUERY;
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_MSQUERY;
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_PROTEINSET_NAME_LIST;
            case COLTYPE_SRC_DAT_FILE:
                return DatabaseLoadPeptideMatchTask.SUB_TASK_SRC_DAT_FILE;
                
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
        
        col =  m_colUsed.get(col);

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
            case COLTYPE_SPECTRUM_TITLE: {
                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);

                } else {
                    String spectrumTitle = peptideMatch.getMsQuery().getDSpectrum().getTitle();
                    lazyData.setData(spectrumTitle);
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
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                
                String[] proteinSetNames = peptideMatch.getProteinSetStringArray();
                if (proteinSetNames == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    lazyData.setData(proteinSetNames.length);
                }
                return lazyData;
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {

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
            case COLTYPE_PEPTIDE_IS_DECOY: {
                // Retrieve typical Peptide Match
                Boolean isDecoy = peptideMatch.isDecoy();
                lazyData.setData(isDecoy);
                return lazyData;
            }
            case COLTYPE_PEPTIDE_IS_VALIDATED: {
                // Retrieve typical Peptide Match
                Boolean isValidated = peptideMatch.isValidated();
                lazyData.setData(isValidated);
                return lazyData;
            }
            case COLTYPE_SRC_DAT_FILE: {
                 String srcDatFile = peptideMatch.getSourceDatFile();
                if (srcDatFile == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    lazyData.setData(srcDatFile);
                }
                return lazyData;
            }
                
        }
        return null; // should never happen
    }
    private final StringBuilder m_sb = new StringBuilder();

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
        

        if (m_scoreRenderer == null) {
            return;
        }
     
        float maxScore = 0;
        int size = m_peptideMatches.length;
        for (int i = 0; i < size; i++) {
            DPeptideMatch peptideMatch = m_peptideMatches[i];
            if (peptideMatch == null) {
                continue;
            }
            float score = peptideMatch.getScore();
            if (score > maxScore) {
                maxScore = score;
            }
        }
        m_scoreRenderer.setMaxValue(maxScore);

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
            filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx));
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
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), peptideConverter, colIdx)); //COLTYPE_PEPTIDE_NAME
        colIdx++;

        
        // COLTYPE_PEPTIDE_NEXT_AA
        if (m_hasPrevNextAA) {
            filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx));
            colIdx++;
        }
        
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_PTM
            
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx)); colIdx++;//COLTYPE_PEPTIDE_SCORE
        

        // COLTYPE_PEPTIDE_START and COLTYPE_PEPTIDE_STOP
        if (m_hasPrevNextAA) {
            filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx)); colIdx++; // COLTYPE_PEPTIDE_START
            filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx)); colIdx++; // COLTYPE_PEPTIDE_STOP
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
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_CALCULATED_MASS
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_PPM
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_CHARGE
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_MISSED_CLIVAGE
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_RANK
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_RETENTION_TIME
        
        if (m_forRSM) {
            filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++; // COLTYPE_PEPTIDE_PROTEIN_SET_COUNT
            filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx)); colIdx++; //COLTYPE_PEPTIDE_PROTEIN_SET_NAMES
        }
        
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_ION_PARENT_INTENSITY
        if (m_isDecoyAndValidated){
            filtersMap.put(colIdx, new BooleanFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_IS_DECOY
            if (m_forRSM) {
                filtersMap.put(colIdx, new BooleanFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_IS_VALIDATED
            }
        }

        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), msQueryConverter, colIdx));  colIdx++;//COLTYPE_PEPTIDE_MSQUERY
        
        
        if (m_isMerged) {
            filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx)); colIdx++;//COLTYPE_SRC_DAT_FILE
        }

        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_SPECTRUM_TITLE



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
        int realColumnIndex =  m_colUsed.get(columnIndex);
        switch (realColumnIndex) {
            case COLTYPE_PEPTIDE_PREVIOUS_AA:
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_NEXT_AA:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
            case COLTYPE_SPECTRUM_TITLE:
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
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT:
                return Integer.class;
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return Object.class; // Float or String... JPM.TODO
            case COLTYPE_PEPTIDE_IS_DECOY:
            case COLTYPE_PEPTIDE_IS_VALIDATED:
                return Boolean.class;
                
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
        return ExportModelUtilities.getExportRowCell(this, row, col);
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }
    
    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        col =  m_colUsed.get(col);
        
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
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_PPM: {
                renderer = new FloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            case COLTYPE_PEPTIDE_IS_DECOY:
            case COLTYPE_PEPTIDE_IS_VALIDATED:{
                renderer = new BooleanRenderer();
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
        list.add(new ExtraDataType(DPeptideMatch.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DPeptideMatch.class)) {
            return m_peptideMatches[row];
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

}
