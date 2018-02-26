package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.dam.tasks.data.PTMSite;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.filter.ValueFilter;
import fr.proline.studio.filter.ValueListAssociatedStringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PercentageRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.utils.IconManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;
import org.apache.poi.hssf.util.HSSFColor;

/**
 *
 * @author JM235353
 */
public class PtmProtenSiteTableModel_V2 extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PROTEIN_ID = 0;
    public static final int COLTYPE_PROTEIN_NAME = 1;
    public static final int COLTYPE_PEPTIDE_NAME = 2;
    public static final int COLTYPE_PEPTIDE_SCORE = 3;

    public static final int COLTYPE_MODIFICATION = 4;
    public static final int COLTYPE_RESIDUE_AA = 5;
    public static final int COLTYPE_MODIFICATION_PROBA = 6;
    public static final int COLTYPE_DELTA_MASS_MODIFICATION = 7;
    public static final int COLTYPE_MODIFICATION_LOC = 8;
    public static final int COLTYPE_PROTEIN_LOC = 9;
    public static final int COLTYPE_PROTEIN_NTERM_CTERM = 10;
    public static final int COLTYPE_PEPTIDE_COUNT = 11;
    
    public static final int COLTYPE_PEPTIDE_PTM = 12;
    public static final int COLTYPE_DELTA_MASS_PTM = 13;
    public static final int COLTYPE_PTM_PROBA = 14;
    
    public static final int COLTYPE_QUERY_TITLE = 15;

    public static final int COLTYPE_HIDDEN_PROTEIN_PTM = 16; // hidden column, must be the last

    private static final String[] m_columnNames = {"Id", "Protein", "Peptide", "Score", "Modification", "Residue", "Site Probability", "Modification D.Mass", "Modification Loc.", "Protein Loc.", "Protein N/C-term", "Peptide count", "PTM", "PTM D.Mass", "PTM Probability", "Query title"};
    private static final String[] m_columnTooltips = {"Protein Id", "Protein", "Peptide", "Score of the peptide match", "Modification", "Modified residue","Modification probability", "Delta mass of the given modification", "Position of the modification on the peptide sequence", "Position of the modification on the protein sequence", "Protein N/C-term", "Number of peptides matching the modification site","PTM modifications associated with this peptide", "PTMs delta mass", "PTMs probability", "Peptide match query title"};

    private ArrayList<PTMSite> m_arrayInUse = null;
    private ArrayList<PTMSite> m_proteinPTMSiteArray = new ArrayList<>();
    private final ArrayList<PTMSite> m_proteinPTMSiteNoRedundantArray = new ArrayList<>();

    private final ArrayList<String> m_modificationsArray = new ArrayList<>();
    private final HashMap<String, Integer> m_modificationsMap = new HashMap<>();

    private final ArrayList<Character> m_residuesArray = new ArrayList<>();
    private final HashMap<Character, Integer> m_residuesMap = new HashMap<>();

    private String m_modelName;

    private String m_modificationInfo = "";

    private boolean m_hideRedundantPeptides = false;

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    public PtmProtenSiteTableModel_V2(LazyTable table) {
        super(table);
    }

    public boolean hideRedundantsPeptides(boolean hide) {
        boolean changed = m_hideRedundantPeptides ^ hide;

        if (changed) {
            m_hideRedundantPeptides = hide;
            if (hide) {
                m_arrayInUse = m_proteinPTMSiteNoRedundantArray;
            } else {
                m_arrayInUse = m_proteinPTMSiteArray;
            }
            fireTableDataChanged();
        }

        return changed;
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
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {

        switch (col) {
            case COLTYPE_PROTEIN_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_NAME:
                return DPeptideMatch.class;
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_MODIFICATION:
            case COLTYPE_MODIFICATION_LOC:
            case COLTYPE_PROTEIN_NTERM_CTERM:
            case COLTYPE_QUERY_TITLE:
                return String.class;
            case COLTYPE_PROTEIN_LOC:
            case COLTYPE_PEPTIDE_COUNT:
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
    public int getSubTaskId(int col) {
        /*switch (col) {
         case COLTYPE_PROTEIN_SET_NAME:
         case COLTYPE_PROTEIN_SET_DESCRIPTION:
         return DatabaseProteinSetsTask.SUB_TASK_TYPICAL_PROTEIN;
         case COLTYPE_PROTEIN_SCORE:
         case COLTYPE_PROTEINS_COUNT:
         return DatabaseProteinSetsTask.SUB_TASK_SAMESET_SUBSET_COUNT;
         case COLTYPE_UNIQUE_SEQUENCES_COUNT:
         case COLTYPE_PEPTIDES_COUNT:
         return DatabaseProteinSetsTask.SUB_TASK_TYPICAL_PROTEIN;
         case COLTYPE_SPECTRAL_COUNT:
         return DatabaseProteinSetsTask.SUB_TASK_SPECTRAL_COUNT;
         case COLTYPE_SPECIFIC_SPECTRAL_COUNT:
         return DatabaseProteinSetsTask.SUB_TASK_SPECIFIC_SPECTRAL_COUNT;
         }*/ //JPM.TODO : not used for the moment
        return -1;
    }

    @Override
    public int getRowCount() {
        if (m_arrayInUse == null) {
            return 0;
        }

        return m_arrayInUse.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        PTMSite proteinPTMSite = m_arrayInUse.get(row);
        DProteinMatch proteinMatch = proteinPTMSite.getProteinMatch();
        DPeptideMatch peptideMatch = proteinPTMSite.getBestPeptideMatch();
        DInfoPTM infoPtm = proteinPTMSite.getPtmSpecificity();

        switch (col) {
            case COLTYPE_PROTEIN_ID:
                return proteinMatch.getId();
            case COLTYPE_PROTEIN_NAME:
                return proteinMatch.getAccession();
            case COLTYPE_PEPTIDE_NAME:
                return peptideMatch;
            case COLTYPE_PEPTIDE_SCORE:
                return peptideMatch.getScore();
            case COLTYPE_PEPTIDE_PTM:
                String ptm = "";
                PeptideReadablePtmString ptmString = peptideMatch.getPeptide().getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }
                return ptm;
            case COLTYPE_MODIFICATION: {
                return infoPtm.getPtmShortName();
            }
            case COLTYPE_MODIFICATION_LOC: {
                String locationSpecitifcity = infoPtm.getLocationSpecificity();
                if (locationSpecitifcity.contains("N-term")) {
                    return "N-term";
                } else if (locationSpecitifcity.contains("C-term")) {
                    return "C-term";
                }

                return String.valueOf((int) proteinPTMSite.getPtmPeptidePosition(peptideMatch.getPeptide().getId()));
            }
            case COLTYPE_PROTEIN_LOC: {
                return proteinPTMSite.seqPosition;
            }
            case COLTYPE_PROTEIN_NTERM_CTERM: {
                String locationSpecitifcity = infoPtm.getLocationSpecificity();
                if (locationSpecitifcity.contains("-term")) {
                    return locationSpecitifcity;
                }
                return "";
            }
            case COLTYPE_PTM_PROBA: {
                DPtmSiteProperties properties = peptideMatch.getPtmSiteProperties();
                if (properties != null) {
                    return properties.getMascotDeltaScore() * 100;
                }
                return null;
            }
            case COLTYPE_MODIFICATION_PROBA: {
                DPtmSiteProperties properties = peptideMatch.getPtmSiteProperties();
                if (properties != null) {
                    // VDS Workaround test for issue #16643   
                    Float proba = properties.getMascotProbabilityBySite().get(proteinPTMSite.toReadablePtmString(peptideMatch.getPeptide().getId()));
                    if (proba == null) {
                        proba = properties.getMascotProbabilityBySite().get(proteinPTMSite.toOtherReadablePtmString(peptideMatch.getPeptide().getId()));
                    }
                    //END VDS Workaround
                    return proba * 100;
                }
                return null;
            }
            case COLTYPE_DELTA_MASS_PTM:
//                return 0.0;                
                //return proteinPTMSite.getDeltaMassPTM();
                //Previously (Version 1) was : 
//                double deltaMass = 0;                
//                for (DPeptidePTM peptidePTM : peptidePTMArray) {
//                    DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePTM.getIdPtmSpecificity());
//                    deltaMass += infoPtm.getMonoMass();
//                }
                double deltaMass = 0;
                for (DPeptidePTM peptidePTM : peptideMatch.getPeptidePTMArray()) {
                    DInfoPTM pepInfoPtm = DInfoPTM.getInfoPTMMap().get(peptidePTM.getIdPtmSpecificity());
                    deltaMass += pepInfoPtm.getMonoMass();
                }
                return deltaMass;

            case COLTYPE_DELTA_MASS_MODIFICATION: {
                return infoPtm.getMonoMass();
            }
            case COLTYPE_PEPTIDE_COUNT: {
                return proteinPTMSite.getPeptideCount();
            }
            case COLTYPE_RESIDUE_AA: {
                return infoPtm.getRresidueAASpecificity();
            }
            case COLTYPE_HIDDEN_PROTEIN_PTM:
                return proteinPTMSite;
            case COLTYPE_QUERY_TITLE: {
                return proteinPTMSite.getBestPeptideMatch().getMsQuery().getDSpectrum().getTitle();
            }
        }

        return null; // should never happen
    }
    private static final StringBuilder m_sb = new StringBuilder(20);

    public void setData(Long taskId, ArrayList<PTMSite> proteinPTMSiteArray) {
        m_proteinPTMSiteArray = proteinPTMSiteArray;
        m_arrayInUse = m_proteinPTMSiteArray;
        m_taskId = taskId;

        m_modificationInfo = PtmProteinSiteTableModelProcessing.calculateDataWORedundance(this, m_modificationsArray, m_residuesArray, m_residuesMap, proteinPTMSiteArray, m_modificationsMap);
        fireTableDataChanged();

    }

    public String getModificationsInfo() {
        return m_modificationInfo;
    }

    public void dataUpdated() {

        fireTableDataChanged();

    }

    public PTMSite getProteinPTMSite(int i) {
        return m_arrayInUse.get(i);
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
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
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COLTYPE_PEPTIDE_NAME) {
            return ((DPeptideMatch) getValueAt(rowIndex, columnIndex)).getPeptide().getSequence();
        }
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_PROTEIN_NAME};
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
    public int getInfoColumn() {
        return COLTYPE_PROTEIN_NAME;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        filtersMap.put(COLTYPE_PROTEIN_NAME, new StringFilter(getColumnName(COLTYPE_PROTEIN_NAME), null, COLTYPE_PROTEIN_NAME));

        ConvertValueInterface peptideConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DPeptideMatch) o).getPeptide().getSequence();
            }

        };
        filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter, COLTYPE_PEPTIDE_NAME));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
        filtersMap.put(COLTYPE_DELTA_MASS_PTM, new DoubleFilter(getColumnName(COLTYPE_DELTA_MASS_PTM), null, COLTYPE_DELTA_MASS_PTM));
        filtersMap.put(COLTYPE_PTM_PROBA, new DoubleFilter(getColumnName(COLTYPE_PTM_PROBA), null, COLTYPE_PTM_PROBA));

        ConvertValueInterface modificationConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }

                return m_modificationsMap.get((String) o);

            }

        };

        filtersMap.put(COLTYPE_MODIFICATION, new ValueListAssociatedStringFilter(getColumnName(COLTYPE_MODIFICATION), m_modificationsArray.toArray(new String[m_modificationsArray.size()]), modificationConverter, COLTYPE_MODIFICATION, COLTYPE_RESIDUE_AA));

        ConvertValueInterface residueConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }

                return m_residuesMap.get((Character) o);

            }

        };
        filtersMap.put(COLTYPE_RESIDUE_AA, new ValueFilter(getColumnName(COLTYPE_RESIDUE_AA), m_residuesArray.toArray(new Character[m_residuesArray.size()]), null, ValueFilter.ValueFilterType.EQUAL, residueConverter, COLTYPE_RESIDUE_AA));
        filtersMap.put(COLTYPE_DELTA_MASS_MODIFICATION, new DoubleFilter(getColumnName(COLTYPE_DELTA_MASS_MODIFICATION), null, COLTYPE_DELTA_MASS_MODIFICATION));
        filtersMap.put(COLTYPE_MODIFICATION_LOC, new StringFilter(getColumnName(COLTYPE_MODIFICATION_LOC), null, COLTYPE_MODIFICATION_LOC));
        filtersMap.put(COLTYPE_PROTEIN_LOC, new IntegerFilter(getColumnName(COLTYPE_PROTEIN_LOC), null, COLTYPE_PROTEIN_LOC));
        filtersMap.put(COLTYPE_PROTEIN_NTERM_CTERM, new StringFilter(getColumnName(COLTYPE_PROTEIN_NTERM_CTERM), null, COLTYPE_PROTEIN_NTERM_CTERM));
        filtersMap.put(COLTYPE_PEPTIDE_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_COUNT), null, COLTYPE_PEPTIDE_COUNT));
        filtersMap.put(COLTYPE_MODIFICATION_PROBA, new DoubleFilter(getColumnName(COLTYPE_MODIFICATION_PROBA), null, COLTYPE_MODIFICATION_PROBA));
        filtersMap.put(COLTYPE_QUERY_TITLE, new StringFilter(getColumnName(COLTYPE_QUERY_TITLE), null, COLTYPE_QUERY_TITLE));

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
        if (col == COLTYPE_PEPTIDE_NAME) {
            PTMSite proteinPTMSite = m_arrayInUse.get(row);
            DPeptideMatch peptideMatch = proteinPTMSite.getBestPeptideMatch();
            return ExportModelUtilities.getExportFonts(peptideMatch);
        }
        return null;
    }
    
    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
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
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_QUERY_TITLE:
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
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    public class ProteinCount implements Comparable {

        private final int m_sameSetCount;
        private final int m_subSetCount;

        public ProteinCount(int sameSetCount, int subSetCount) {
            m_sameSetCount = sameSetCount;
            m_subSetCount = subSetCount;
        }

        public int getNbProteins() {
            return m_sameSetCount + m_subSetCount;
        }

        public String toHtml() {

            String urlSameset = IconManager.getURLForIcon(IconManager.IconType.SAME_SET);
            String urlSubset = IconManager.getURLForIcon(IconManager.IconType.SUB_SET);

            m_sb.setLength(0);
            m_sb.append("<html>");
            m_sb.append((int) (m_sameSetCount + m_subSetCount));
            m_sb.append("&nbsp;&nbsp;(").append(m_sameSetCount).append("&nbsp;<img src=\"").append(urlSameset).append("\">");
            m_sb.append("&nbsp;,&nbsp;").append(m_subSetCount).append("&nbsp;<img src=\"").append(urlSubset).append("\">&nbsp;");
            m_sb.append(')');
            m_sb.append("</html>");
            return m_sb.toString();
        }

        @Override
        public String toString() {

            m_sb.setLength(0);
            m_sb.append((int) (m_sameSetCount + m_subSetCount));
            m_sb.append(" (").append(m_sameSetCount);
            m_sb.append(",").append(m_subSetCount);
            m_sb.append(')');
            return m_sb.toString();
        }

        @Override
        public int compareTo(Object o) {
            int sameSet = m_sameSetCount - ((ProteinCount) o).m_sameSetCount;
            if (sameSet != 0) {
                return sameSet;
            }
            return m_subSetCount - ((ProteinCount) o).m_subSetCount;
        }

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
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(PTMSite.class, true));
        list.add(new ExtraDataType(DProteinMatch.class, true));
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
        if (c.equals(PTMSite.class)) {
            return m_arrayInUse.get(row);
        }
        if (c.equals(DProteinMatch.class)) {
            return m_arrayInUse.get(row).getProteinMatch();
        }
        if (c.equals(DPeptideMatch.class)) {
            return ((PTMSite) m_arrayInUse.get(row)).getBestPeptideMatch();
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

}
