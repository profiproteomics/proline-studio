package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PercentageRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PeptidesOfPTMSiteTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_PEPTIDE_NAME = 1;
    public static final int COLTYPE_PEPTIDE_SCORE = 2;

    public static final int COLTYPE_MODIFICATION = 3;
    public static final int COLTYPE_RESIDUE_AA = 4;
    public static final int COLTYPE_MODIFICATION_PROBA = 5;
    public static final int COLTYPE_DELTA_MASS_MODIFICATION = 6;
    public static final int COLTYPE_MODIFICATION_LOC = 7;
    public static final int COLTYPE_PROTEIN_LOC = 8;
    public static final int COLTYPE_PROTEIN_NTERM_CTERM = 9;

    public static final int COLTYPE_PEPTIDE_PTM = 10;
    public static final int COLTYPE_DELTA_MASS_PTM = 11;
    public static final int COLTYPE_PTM_PROBA = 12;
    public static final int COLTYPE_SPECTRUM_TITLE = 13;

    static class Row {

        DPeptideInstance peptideInstance;
        DPeptideMatch peptideMatch;

        public Row(DPeptideInstance peptideInstance, DPeptideMatch peptideMatch) {
            this.peptideInstance = peptideInstance;
            this.peptideMatch = peptideMatch;
        }

    }
//    public static final int COLTYPE_HIDDEN_PROTEIN_PTM = 16; // hidden column, must be the last

    private static final String[] m_columnNames = {"Id", "Peptide", "Score", "Modification", "Residue", "Site Probability", "Modification D.Mass", "Modification Loc.", "Protein Loc.", "Protein N/C-term", "PTMs", "PTMs D.Mass", "PTMs Probability", "Spectrum title"};
    private static final String[] m_columnTooltips = {"Peptide Id (Instance Id)", "Peptide", "Score of the peptide match", "Modification", "Modified residue", "Site probability", "Delta mass of the given modification", "Position of the modification on the peptide sequence", "Position of the modification on the protein sequence", "Protein N/C-term", "PTM modifications associated with this peptide", "PTMs delta mass", "PTMs probability", "Peptide match spectrum title"};
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

    private PTMSite m_currentPtmSite;
    private ArrayList<Row> m_ptmSitePeptides = new ArrayList<>();
    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    private String m_modelName;
    private boolean m_showPeptideMatches = false;

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    /**
     * Set data for this table Model
     *
     * @param selectedPTMSite : PTM Site to display peptides [matches] for
     * @param showPeptideMatches : specify if all peptide matches should be
     * displayed or only best ones
     * @param parentPepInstance : Specify the peptide instance ID to display
     * peptide matches for. This parameter is only used if showPeptideMatches =
     * true
     */
    public void setData(PTMSite selectedPTMSite, boolean showPeptideMatches, DPeptideInstance parentPepInstance) {
        m_currentPtmSite = selectedPTMSite;
        m_showPeptideMatches = showPeptideMatches;
        m_ptmSitePeptides = new ArrayList<>();
        if (m_currentPtmSite == null) {
            fireTableDataChanged();
            return;
        }

        //Get all Peptide Instances Best PSM
        if (!m_currentPtmSite.isLoaded()) {
            m_logger.warn("Peptide Matches not loaded for PTM Site " + m_currentPtmSite.toString());
        } else if (!m_showPeptideMatches) {
//                m_currentPtmSite.getParentPeptideInstances().forEach(parentPeptideInstance -> {
//                    DPeptideMatch bestPM = m_currentPtmSite.getBestPeptideMatchForPeptide(parentPeptideInstance.getPeptideId());
//                    m_ptmSitePeptides.add(new Row(parentPeptideInstance, bestPM));
//                });

            List<DPeptideInstance> dpInstanceList = m_currentPtmSite.getParentPeptideInstances();
            for (DPeptideInstance parentPeptideInstance : dpInstanceList) {
                // TODO this check is mandatory to avoid NullPointer, but the problem must be handled: PTMSite and parentPepInstance parameters are inconsistent
                if (m_currentPtmSite.getPTMSitePeptideInstance(parentPeptideInstance.getPeptideId()) != null) {
                    DPeptideMatch bestPM = m_currentPtmSite.getPTMSitePeptideInstance(parentPeptideInstance.getPeptideId()).getBestPeptideMatch();
                    m_ptmSitePeptides.add(new Row(parentPeptideInstance, bestPM));
                }
            }
        } else if (parentPepInstance == null) {
            // TODO : request all PeptideMatches ??
            m_logger.warn("Must shown all peptide matches but no peptide instance specified for PTM Site " + m_currentPtmSite.toString());
        } else // TODO this check is mandatory to avoid NullPointer, but the problem must be handled: PTMSite and parentPepInstance parameters are inconsistent
        {
            if (m_currentPtmSite.getPTMSitePeptideInstance(parentPepInstance.getPeptideId()) != null) {
                List<DPeptideInstance> peptideInstancesForPeptide = m_currentPtmSite.getPTMSitePeptideInstance(parentPepInstance.getPeptideId()).getLeafPepInstances();

                if (peptideInstancesForPeptide == null) {
                    // PtmSite and parentPepInstance do not correspond for the moment (asynchronous loading)
                    m_currentPtmSite = null;
                    fireTableDataChanged();
                    return;
                }

                for (DPeptideInstance pi : peptideInstancesForPeptide) {
                    for (DPeptideMatch pepMatch : pi.getPeptideMatches()) {
                        m_ptmSitePeptides.add(new Row(pi, pepMatch));
                    }
                }

                /*peptideMatchesForPeptide.forEach(entry  -> {
                        entry.getValue().forEach( pepMatch -> {
                            m_ptmSitePeptides.add(new Row(entry.getKey(), pepMatch));
                        });
                    });          */
            }
        }
        fireTableDataChanged();
    }

    public PTMSite getCurrentPTMSite() {
        return m_currentPtmSite;
    }

    public DPeptideMatch getSelectedPeptideMatchSite(int row) {
        if (row < 0 || (row >= getRowCount())) {
            return null;
        }
        return m_ptmSitePeptides.get(row).peptideMatch;
    }

    public DPeptideInstance getSelectedPeptideInstance(int row) {
        if (row < 0 || (row >= getRowCount())) {
            return null;
        }
        return m_ptmSitePeptides.get(row).peptideInstance;
    }

    //VDS : Rename getPeptideInstanceIndex : No selection here
    public int getSelectedIndex(DPeptideInstance pep) {
        DPeptideInstance comparePep;
        for (int row = 0; row < this.getRowCount(); row++) {
            comparePep = m_ptmSitePeptides.get(row).peptideInstance;
            if (pep.equals(comparePep)) {
                return row;
            }
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        return m_ptmSitePeptides.size();
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
    public Object getValueAt(int rowIndex, int columnIndex) {

        DPeptideMatch pepMatch = m_ptmSitePeptides.get(rowIndex).peptideMatch;
        switch (columnIndex) {
            case COLTYPE_PEPTIDE_ID:
                if (m_showPeptideMatches) {
                    return pepMatch.getId();
                } else {
                    return m_ptmSitePeptides.get(rowIndex).peptideInstance.getId();
                }
            case COLTYPE_PEPTIDE_NAME:
                return pepMatch;
            case COLTYPE_PEPTIDE_SCORE:
                return pepMatch.getScore();
            case COLTYPE_PEPTIDE_PTM:
                String ptm = "";
                PeptideReadablePtmString ptmString = pepMatch.getPeptide().getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }
                return ptm;
            case COLTYPE_MODIFICATION: {
                return m_currentPtmSite.getPTMSpecificity().getPtmShortName();
            }
            case COLTYPE_MODIFICATION_LOC: {
                String locationSpecitifcity = m_currentPtmSite.getPTMSpecificity().getLocationSpecificity();
                if (locationSpecitifcity.contains("N-term")) {
                    return "N-term";
                } else if (locationSpecitifcity.contains("C-term")) {
                    return "C-term";
                }

                return String.valueOf((int) m_currentPtmSite.getPositionOnPeptide(pepMatch.getPeptide().getId()));
            }
            case COLTYPE_PROTEIN_LOC: {
                return m_currentPtmSite.getPositionOnProtein();
            }
            case COLTYPE_PROTEIN_NTERM_CTERM: {
                String locationSpecitifcity = m_currentPtmSite.getPTMSpecificity().getLocationSpecificity();
                if (locationSpecitifcity.contains("-term")) {
                    return locationSpecitifcity;
                }
                return "";
            }
            case COLTYPE_PTM_PROBA: {
                DPtmSiteProperties properties = pepMatch.getPtmSiteProperties();
                if (properties != null) {
                    return properties.getMascotDeltaScore() * 100;
                }
                return null;
            }
            case COLTYPE_MODIFICATION_PROBA: {
                DPtmSiteProperties properties = pepMatch.getPtmSiteProperties();
                if (properties != null) {
                    Map<String, Float> test = properties.getMascotProbabilityBySite();
                    Float proba = properties.getMascotProbabilityBySite().get(m_currentPtmSite.toReadablePtmString(pepMatch.getPeptide().getId()));
                    // VDS Workaround test for issue #16643                      
                    if (proba == null) {
                        proba = properties.getMascotProbabilityBySite().get(m_currentPtmSite.toOtherReadablePtmString(pepMatch.getPeptide().getId()));
                    }
                    //END VDS Workaround
//                    return properties.getMascotProbabilityBySite().get(m_currentPtmSite.toReadablePtmString(pepMatch.getPeptide().getId()))*100;
                    return proba * 100;
                }
                return null;
            }
            case COLTYPE_DELTA_MASS_PTM:
//                return 0.0;
                //return proteinPTMSite.getDeltaMassPTM();
                double deltaMass = 0;
                for (DPeptidePTM peptidePTM : pepMatch.getPeptidePTMArray()) {
                    DInfoPTM pepInfoPtm = DInfoPTM.getInfoPTMMap().get(peptidePTM.getIdPtmSpecificity());
                    deltaMass += pepInfoPtm.getMonoMass();
                }
                return deltaMass;

            case COLTYPE_DELTA_MASS_MODIFICATION: {
                return m_currentPtmSite.getPTMSpecificity().getMonoMass();
            }

            case COLTYPE_RESIDUE_AA: {
                return m_currentPtmSite.getPTMSpecificity().getResidueAASpecificity();
            }
            case COLTYPE_SPECTRUM_TITLE: {
                return pepMatch.getMsQuery().getDSpectrum().getTitle();
            }
        }

        return null; // should never happen
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col == COLTYPE_PEPTIDE_ID && m_showPeptideMatches) {
            return "Peptide Match Id";
        } else {
            return m_columnTooltips[col];
        }
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_SPECTRUM_TITLE:
            case COLTYPE_MODIFICATION: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_MODIFICATION_LOC:
            case COLTYPE_PROTEIN_NTERM_CTERM: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PROTEIN_LOC: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            case COLTYPE_PTM_PROBA:
            case COLTYPE_MODIFICATION_PROBA: {
                renderer = new PercentageRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_DELTA_MASS_PTM:
            case COLTYPE_DELTA_MASS_MODIFICATION: {
                renderer = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4);
                break;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }

        }
        m_rendererMap.put(col, renderer);
        return renderer;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return -1L; // not used
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null; // not used
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        //not used
    }

    @Override
    public void sortingChanged(int col) {
        // not used

    }

    @Override
    public int getSubTaskId(int col) {
        return -1; // not used
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex == COLTYPE_PEPTIDE_NAME) {
            return String.class;
        }
        return getColumnClass(columnIndex);
    }

    @Override
    public Class getColumnClass(int col) {

        switch (col) {
            case COLTYPE_PEPTIDE_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_NAME:
                return DPeptideMatch.class;
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_MODIFICATION:
            case COLTYPE_MODIFICATION_LOC:
            case COLTYPE_PROTEIN_NTERM_CTERM:
            case COLTYPE_SPECTRUM_TITLE:
                return String.class;
            case COLTYPE_PROTEIN_LOC:
                return Integer.class;
            case COLTYPE_PTM_PROBA:
            case COLTYPE_MODIFICATION_PROBA:
            case COLTYPE_DELTA_MASS_PTM:
            case COLTYPE_DELTA_MASS_MODIFICATION:
                return Double.class;
            case COLTYPE_PEPTIDE_SCORE:
                return Float.class;
            case COLTYPE_RESIDUE_AA:
                return Character.class;
        }

        return null;
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COLTYPE_PEPTIDE_NAME) {
            return ((DPeptideMatch) getValueAt(rowIndex, columnIndex)).getPeptide().getSequence();
        }
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_ID};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PEPTIDE_NAME;
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
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(PTMSite.class)) {
            return m_currentPtmSite;
        }
        if (c.equals(DProteinMatch.class)) {
            return m_currentPtmSite.getProteinMatch();
        }
        if (c.equals(DPeptideMatch.class)) {
            return m_ptmSitePeptides.get(row);
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        ConvertValueInterface peptideConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DPeptideMatch) o).getPeptide().getSequence();
            }

        };

        filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter, COLTYPE_PEPTIDE_NAME));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));

        filtersMap.put(COLTYPE_MODIFICATION, new StringDiffFilter(getColumnName(COLTYPE_MODIFICATION), null, COLTYPE_MODIFICATION));

        ConvertValueInterface aAConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((Character) o).toString();
            }

        };
        filtersMap.put(COLTYPE_RESIDUE_AA, new StringDiffFilter(getColumnName(COLTYPE_RESIDUE_AA), aAConverter, COLTYPE_RESIDUE_AA));

        filtersMap.put(COLTYPE_MODIFICATION_PROBA, new DoubleFilter(getColumnName(COLTYPE_MODIFICATION_PROBA), null, COLTYPE_MODIFICATION_PROBA));
        filtersMap.put(COLTYPE_DELTA_MASS_MODIFICATION, new DoubleFilter(getColumnName(COLTYPE_DELTA_MASS_MODIFICATION), null, COLTYPE_DELTA_MASS_MODIFICATION));
        filtersMap.put(COLTYPE_MODIFICATION_LOC, new StringDiffFilter(getColumnName(COLTYPE_MODIFICATION_LOC), null, COLTYPE_MODIFICATION_LOC));
        filtersMap.put(COLTYPE_PROTEIN_LOC, new IntegerFilter(getColumnName(COLTYPE_PROTEIN_LOC), null, COLTYPE_PROTEIN_LOC));
        filtersMap.put(COLTYPE_PROTEIN_NTERM_CTERM, new StringDiffFilter(getColumnName(COLTYPE_PROTEIN_NTERM_CTERM), null, COLTYPE_PROTEIN_NTERM_CTERM));

        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));

        filtersMap.put(COLTYPE_DELTA_MASS_PTM, new DoubleFilter(getColumnName(COLTYPE_DELTA_MASS_PTM), null, COLTYPE_DELTA_MASS_PTM));
        filtersMap.put(COLTYPE_PTM_PROBA, new DoubleFilter(getColumnName(COLTYPE_PTM_PROBA), null, COLTYPE_PTM_PROBA));
        filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringDiffFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getLoadingPercentage() {
        return 100;
    }

    @Override
    public PlotType getBestPlotType() {
        return PlotType.SCATTER_PLOT;
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
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

}
