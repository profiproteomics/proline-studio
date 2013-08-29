package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.utils.DataFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Table Model for PeptideInstance (peptides of a ProteinMatch in a Rsm)
 * @author JM235353
 */
public class PeptideTableModel extends AbstractTableModel {

    public static final int COLTYPE_PEPTIDE_NAME = 0;
    public static final int COLTYPE_PEPTIDE_SCORE = 1;
    public static final int COLTYPE_PROTEIN_GROUPS_MATCHES = 2;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 3;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 4;
    public static final int COLTYPE_PEPTIDE_DELTA_MOZ = 5;
    public static final int COLTYPE_PEPTIDE_CHARGE = 6;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 7;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 8;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 9;
    public static final int COLTYPE_PEPTIDE_PTM = 10;
    private static final String[] m_columnNames = {"Peptide", "Score", "Protein S. Matches", "Calc. Mass", "Exp. MoZ", "Delta MoZ", "Charge", "Missed Cl.", "RT", "Ion Parent Int.", "PTM"};
    private PeptideInstance[] m_peptideInstances = null;

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
    public Class getColumnClass(int col) {
        switch (col){
            case COLTYPE_PEPTIDE_NAME:
                return Peptide.class;
            case COLTYPE_PROTEIN_GROUPS_MATCHES:
            case COLTYPE_PEPTIDE_PTM:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return Float.class;
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_DELTA_MOZ:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
                return Double.class;
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
                return Integer.class;
        }
        return null; // should not happen
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
            case COLTYPE_PROTEIN_GROUPS_MATCHES: {
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
                return DataFormat.format(peptideMatch.getCharge());
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
                return Double.valueOf(peptideMatch.getDeltaMoz());
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
                return DataFormat.format(peptideMatch.getMissedCleavage());
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
        fireTableDataChanged();

    }

    public PeptideInstance[] getPeptideInstances() {
        return m_peptideInstances;
    }

    
}
