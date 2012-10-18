package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.studio.utils.DataFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class PeptideTableModel extends AbstractTableModel {

    public static final int COLTYPE_PEPTIDE_NAME = 0;
    public static final int COLTYPE_PEPTIDE_SCORE = 1;
    public static final int COLTYPE_PROTEIN_GROUPS_MATCHES = 2;
    public static final int COLTYPE_PEPTIDE_CHARGE = 3;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 4;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 5;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 6;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 7;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 8;
    public static final int COLTYPE_PEPTIDE_PTM = 9;
    private static final String[] columnNames = {"Peptide", "Score", "Protein G. Matches", "Charge", "MoZ Exp.", "Mass Calc.", "Missed Cl.", "RT", "Ion Parent Int.", "PTM"};
    private PeptideInstance[] peptideInstances = null;

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
        return String.class;
    }

    @Override
    public int getRowCount() {
        if (peptideInstances == null) {
            return 0;
        }
        return peptideInstances.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Group
        PeptideInstance peptideInstance = peptideInstances[row];

        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                // Retrieve typical Peptide Match
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch == null) {
                }
                Peptide peptide = peptideMatch.getTransientPeptide();
                if (peptide == null) {
                    return ""; // should never happen

                }

                return constructPeptideDisplay(peptide);


            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                return DataFormat.format(peptideMatch.getScore(), 2);
            }
            case COLTYPE_PROTEIN_GROUPS_MATCHES: {
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                Peptide p = peptideMatch.getTransientPeptide();
                if (p == null) {
                    return "";
                }
                ArrayList<ProteinSet> proteinSetList = p.getTransientData().getProteinSetArray();
                if (proteinSetList == null) {
                    return "";
                }

                StringBuilder display = new StringBuilder();
                int nbProteinGroups = proteinSetList.size();
                display.append(nbProteinGroups);
                display.append(" (");
                for (int i = 0; i < nbProteinGroups; i++) {
                    display.append(proteinSetList.get(i).getTransientData().getTypicalProteinMatch().getAccession());
                    if (i + 1 < nbProteinGroups) {
                        display.append(',');
                    }
                }
                display.append(')');

                return display.toString();

            }
            case COLTYPE_PEPTIDE_CHARGE: {
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                return DataFormat.format(peptideMatch.getCharge());
            }
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                return DataFormat.format(peptideMatch.getExperimentalMoz(), 2);
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch != null) {
                    Peptide p = peptideMatch.getTransientPeptide();
                    if (p != null) {
                        return DataFormat.format(p.getCalculatedMass(), 2);
                    }
                }
                return "";
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch == null) {
                    return "";
                }
                return DataFormat.format(peptideMatch.getMissedCleavage());
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                return DataFormat.format(peptideInstance.getElutionTime(), 2);
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch != null) {
                    MsQuery msQuery = peptideMatch.getMsQuery();
                    if (msQuery != null) {
                        Spectrum spectrum = msQuery.getSpectrum();
                        if (spectrum != null) {
                            Float precursorIntensity = spectrum.getPrecursorIntensity();
                            if (precursorIntensity != null) {
                                return DataFormat.format(precursorIntensity, 2);
                            }
                        }
                    }
                }
                return "";
            }
            case COLTYPE_PEPTIDE_PTM: {
                PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                if (peptideMatch != null) {
                    Peptide p = peptideMatch.getTransientPeptide();
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
        this.peptideInstances = peptideInstances;
        fireTableDataChanged();

    }

    public PeptideInstance[] getPeptideInstances() {
        return peptideInstances;
    }

    private String constructPeptideDisplay(Peptide peptide) {

        SequenceMatch sequenceMatch = peptide.getTransientData().getSequenceMatch();

        if (sequenceMatch != null) {

            // Add Before residue of the peptide
            String residueBefore = sequenceMatch.getResidueBefore();
            if (residueBefore != null) {
                displaySB.append(residueBefore.toUpperCase());
                displaySB.append('-');
            }
            
            HashMap<Integer,PeptidePtm> ptmMap = peptide.getTransientData().getPeptidePtmMap();
            
            if (ptmMap == null) {
                displaySB.append(peptide.getSequence());
            } else {
                displaySB.append("<HTML>");
                
                String sequence = peptide.getSequence();
                PeptidePtm nterPtm = ptmMap.get(0);
                if (nterPtm != null) {
                    displaySB.append("<span style='color:#fd9b1d;'>");
                    displaySB.append(sequence.charAt(0));
                    displaySB.append("</span>");
                } else {
                    displaySB.append(sequence.charAt(0));
                }
                
                for (int i = 1; i<sequence.length()-1; i++) {
                    if (ptmMap.get(i+1) != null) {
                        displaySB.append("<span style='color:#fd9b1d;'>");
                        displaySB.append(sequence.charAt(i));
                        displaySB.append("</span>");
                    } else {
                        displaySB.append(sequence.charAt(i));
                    }
                }
                
                PeptidePtm cterPtm = ptmMap.get(-1);
                if (cterPtm != null) {
                    displaySB.append("<span style='color:#fd9b1d;'>");
                    displaySB.append(sequence.charAt(sequence.length()-1));
                    displaySB.append("</span>");
                } else {
                    displaySB.append(sequence.charAt(sequence.length()-1));
                }
            }
            
            // Add After residue of the peptide
            String residueAfter = sequenceMatch.getResidueAfter();
            if (residueAfter != null) {
                displaySB.append('-');
                displaySB.append(residueAfter.toUpperCase());
            }
            
            if (ptmMap != null) {
                displaySB.append("</HTML>");
            }
            
            String res = displaySB.toString();
            displaySB.setLength(0);
            return res;
        }

        return peptide.getSequence();

        
    }
    private static StringBuilder displaySB = new StringBuilder();
}
