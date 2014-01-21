package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.filter.*;
import java.util.ArrayList;


/**
 * Table Model for PeptideInstance (peptides of a ProteinMatch in a Rsm)
 * @author JM235353
 */
public class PeptideTableModel extends FilterTableModel {

    public static final int COLTYPE_PEPTIDE_NAME = 0;
    public static final int COLTYPE_PEPTIDE_SCORE = 1;
    public static final int COLTYPE_PROTEIN_SETS_MATCHES = 2;
    public static final int COLTYPE_PEPTIDE_START = 3;
    public static final int COLTYPE_PEPTIDE_STOP = 4;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 5;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 6;
    public static final int COLTYPE_PEPTIDE_DELTA_MOZ = 7;
    public static final int COLTYPE_PEPTIDE_CHARGE = 8;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 9;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 10;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 11;
    public static final int COLTYPE_PEPTIDE_PTM = 12;
    private static final String[] m_columnNames = {"Peptide", "Score", "Protein S. Matches", "Start", "Stop", "Calc. Mass", "Exp. MoZ", "Delta MoZ", "Charge", "Missed Cl.", "RT", "Ion Parent Int.", "PTM"};
    private static final String[] m_columnTooltips = {"Peptide", "Score", "Protein Set Matches", "Start", "Stop", "Calculated Mass", "Experimental Mass to Charge Ration", "Delta Mass to Charge Ratio", "Charge", "Missed Clivage", "Retention Time", "Ion Parent Intensity", "Post Translational Modifications"};
    
    
    private PeptideInstance[] m_peptideInstances = null;

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    
    public PeptideInstance getPeptide(int row) {
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
        switch (col){
            case COLTYPE_PEPTIDE_NAME:
                return Peptide.class;
            case COLTYPE_PROTEIN_SETS_MATCHES:
            case COLTYPE_PEPTIDE_PTM:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_DELTA_MOZ:
                return Float.class;
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
                return Double.class;
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:    
                return Integer.class;
        }
        return null; // should not happen
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

        // Retrieve Peptide Instance
        PeptideInstance peptideInstance = m_peptideInstances[rowFiltered];

        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                // Retrieve typical Peptide Match
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null;
                }
                return peptideMatch.getPeptide();
            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return peptideMatch.getScore();
            }
            case COLTYPE_PROTEIN_SETS_MATCHES: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                Peptide p = peptideMatch.getPeptide();
                if (p == null) {
                    return "";
                }
                ArrayList<DProteinSet> proteinSetList = p.getTransientData().getProteinSetArray();
                if (proteinSetList == null) {
                    return "";
                }

                StringBuilder display = new StringBuilder();
                int nbProteinGroups = proteinSetList.size();
                display.append(nbProteinGroups);
                display.append(" (");
                for (int i = 0; i < nbProteinGroups; i++) {
                    display.append(((DProteinSet)proteinSetList.get(i)).getTypicalProteinMatch().getAccession());
                    if (i + 1 < nbProteinGroups) {
                        display.append(',');
                    }
                }
                display.append(')');

                return display.toString();

            }
            case COLTYPE_PEPTIDE_CHARGE: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                return Integer.valueOf(peptideMatch.getCharge());
            }
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return Double.valueOf(peptideMatch.getExperimentalMoz());
            }
            case COLTYPE_PEPTIDE_DELTA_MOZ: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return Float.valueOf(peptideMatch.getDeltaMoz());
            }
            case COLTYPE_PEPTIDE_START: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                Peptide p = peptideMatch.getPeptide();
                SequenceMatch sequenceMatch = p.getTransientData().getSequenceMatch();
                if (sequenceMatch == null) {
                    return null;
                }
                int start = sequenceMatch.getId().getStart();
                return Integer.valueOf(start);
            }
            case COLTYPE_PEPTIDE_STOP: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                Peptide p = peptideMatch.getPeptide();
                SequenceMatch sequenceMatch = p.getTransientData().getSequenceMatch();
                if (sequenceMatch == null) {
                    return null;
                }
                int stop = sequenceMatch.getId().getStop();
                return Integer.valueOf(stop);
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch != null) {
                    Peptide p = peptideMatch.getPeptide();
                    if (p != null) {
                        return p.getCalculatedMass();
                    }
                }
                return null;
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null;
                }
                return Integer.valueOf(peptideMatch.getMissedCleavage());
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                return peptideInstance.getElutionTime();
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch != null) {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    if (msQuery != null) {
                        Float precursorIntensity = msQuery.getPrecursorIntensity();
                        return precursorIntensity;

                    }
                }
                return null;
            }
            case COLTYPE_PEPTIDE_PTM: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch != null) {
                    Peptide p = peptideMatch.getPeptide();
                    if (p != null) {
                        return p.getPtmString();
                    }
                }
                return "";
            }
        }
        return null; // should never happen
    }

    public void setData(PeptideInstance[] peptideInstances) {
        m_peptideInstances = peptideInstances;
        
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }


    }

    public PeptideInstance[] getPeptideInstances() {
        return m_peptideInstances;
    }

    @Override
    public void initFilters() {
        
        
        
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PEPTIDE_NAME] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_NAME));
            m_filters[COLTYPE_PEPTIDE_SCORE] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE));

            m_filters[COLTYPE_PROTEIN_SETS_MATCHES] = new StringFilter(getColumnName(COLTYPE_PROTEIN_SETS_MATCHES));
            m_filters[COLTYPE_PEPTIDE_START] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_START));
            m_filters[COLTYPE_PEPTIDE_STOP] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_STOP));  
            m_filters[COLTYPE_PEPTIDE_CALCULATED_MASS] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS));
            m_filters[COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ));
            m_filters[COLTYPE_PEPTIDE_DELTA_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_DELTA_MOZ));
            m_filters[COLTYPE_PEPTIDE_CHARGE] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE));
            m_filters[COLTYPE_PEPTIDE_MISSED_CLIVAGE] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLIVAGE));
            m_filters[COLTYPE_PEPTIDE_RETENTION_TIME] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_RETENTION_TIME));
            m_filters[COLTYPE_PEPTIDE_ION_PARENT_INTENSITY] = new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY));
            m_filters[COLTYPE_PEPTIDE_PTM] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM));

        }
           
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

        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                return ((StringFilter) filter).filter(((Peptide) data).getSequence());
            }
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PROTEIN_SETS_MATCHES: {
                return ((StringFilter) filter).filter((String) data);
            }
            case COLTYPE_PEPTIDE_SCORE: 
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_DELTA_MOZ: 
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
                return ((DoubleFilter) filter).filter(((Number) data).doubleValue());
            }
            case COLTYPE_PEPTIDE_CHARGE: 
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP: {
            
                return ((IntegerFilter) filter).filter((Integer) data);
            }


        }

        return true; // should never happen
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
    public int getLoadingPercentage() {
        return 100;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    
}
