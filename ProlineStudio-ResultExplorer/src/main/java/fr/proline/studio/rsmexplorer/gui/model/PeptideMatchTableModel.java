package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchFromRsetTask;
import fr.proline.studio.utils.DataFormat;
import fr.proline.studio.utils.LazyData;
import fr.proline.studio.utils.LazyTable;
import fr.proline.studio.utils.LazyTableModel;

/**
 *
 * @author JM235353
 */
public class PeptideMatchTableModel extends LazyTableModel {

    public static final int COLTYPE_PEPTIDE_NAME = 0;
    public static final int COLTYPE_PEPTIDE_SCORE = 1;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 2;
    /*public static final int COLTYPE_PROTEIN_GROUPS_MATCHES = 2;
    public static final int COLTYPE_PEPTIDE_CHARGE = 3;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 4;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 5;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 6;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 7;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 8;
    public static final int COLTYPE_PEPTIDE_PTM = 9;*/
    private static final String[] columnNames = {"Peptide", "Score", "MsQuery"/*, "Protein G. Matches", "Charge", "MoZ Exp.", "Mass Calc.", "Missed Cl.", "RT", "Ion Parent Int.", "PTM"*/};
    private PeptideMatch[] peptideMatches = null;

    
    public PeptideMatchTableModel(LazyTable table) {
        super(table);
    }
    
    
    public PeptideMatch getPeptideMatch(int row) {
        return peptideMatches[row];

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
        /*if (col == COLTYPE_PEPTIDE_NAME) {
            return Peptide.class;
        } else */if (col == COLTYPE_PEPTIDE_SCORE) {
            return String.class;
        }
        return LazyData.class;
    }

        @Override
    public int getSubTaskId(int col) {
        switch (col) {
            case COLTYPE_PEPTIDE_NAME:
                return DatabaseLoadPeptideMatchFromRsetTask.SUB_TASK_PEPTIDE;
            case COLTYPE_PEPTIDE_MSQUERY:
                return DatabaseLoadPeptideMatchFromRsetTask.SUB_TASK_MSQUERY;
        }
        return -1;
    }
    
    @Override
    public int getRowCount() {
        if (peptideMatches == null) {
            return 0;
        }
        return peptideMatches.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Group
        PeptideMatch peptideMatch = peptideMatches[row];

        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                if ((data == null) || (data.getPeptide() == null)) {
                    givePriorityTo(taskId, row, col);
                    lazyData.setData(null);
                } else {
                    lazyData.setData(data.getPeptide());
                }
                
                return lazyData;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match
                return DataFormat.format(peptideMatch.getScore(), 2);
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                if ((data == null) || (!data.getIsMsQuerySet())) {
                    givePriorityTo(taskId, row, col);
                    lazyData.setData(null);
                    
                } else {
                    MsQuery msQuery = peptideMatch.getMsQuery();
                    lazyData.setData( String.valueOf(msQuery.getId())  ); //JPM.TODO
                }
                return lazyData;

   
            }
            /*case COLTYPE_PROTEIN_GROUPS_MATCHES: {
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
            }*/
        }
        return null; // should never happen
    }

    public void setData(PeptideMatch[] peptideMatches) {
        this.peptideMatches = peptideMatches;
        fireTableDataChanged();

    }

    public PeptideMatch[] getPeptideMatches() {
        return peptideMatches;
    }

    public void dataUpdated() {

        fireTableDataChanged();
    }
}
