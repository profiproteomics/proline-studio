package fr.proline.studio.rsmexplorer.gui.model;



import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinPTMSite;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.table.TableCellRenderer;
import org.apache.poi.hssf.util.HSSFColor;

/**
 *
 * @author JM235353
 */
public class PtmProtenSiteTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PROTEIN_ID = 0;
    public static final int COLTYPE_PROTEIN_NAME = 1;
    public static final int COLTYPE_PEPTIDE_NAME = 2;
    public static final int COLTYPE_PEPTIDE_SCORE = 3;
    public static final int COLTYPE_PEPTIDE_PTM = 4;
    public static final int COLTYPE_DELTA_MASS_PTM = 5;
    public static final int COLTYPE_PTM_PROBA = 6;
    public static final int COLTYPE_MODIFICATION = 7;
    public static final int COLTYPE_RESIDUE_AA = 8;
    public static final int COLTYPE_DELTA_MASS_MODIFICATION = 9;
    public static final int COLTYPE_MODIFICATION_LOC = 10;
    public static final int COLTYPE_PROTEIN_LOC = 11;
    public static final int COLTYPE_PROTEIN_NTERM_CTERM = 12;
    public static final int COLTYPE_MODIFICATION_PROBA = 13;
    
    public static final int COLTYPE_HIDDEN_PROTEIN_PTM = 14; // hidden column, must be the last
    

    
    
    private static final String[] m_columnNames = {"Id", "Protein", "Peptide", "Score", "PTM", "PTM D.Mass", "PTM Probability", "Modification", "Residue", "Modification D.Mass", "Modification Loc.", "Protein Loc.", "Protein N/C-term", "Modification Probability"};
    private static final String[] m_columnTooltips =  {"Protein Id", "Protein", "Peptide", "Score", "Full PTM", "PTM Delta Mass", "PTM Probability", "Modification", "Modification Residue", "Modification Delta Mass", "Modification Location", "Protein Location of the Modification", "Protein N/C-term", "Modification Probability"};
    
    
    private ArrayList<DProteinPTMSite> m_arrayInUse = null;
    private ArrayList<DProteinPTMSite> m_proteinPTMSiteArray = new ArrayList<>();
    private final ArrayList<DProteinPTMSite> m_proteinPTMSiteNoRedundantArray = new ArrayList<>();
    private final HashSet<DProteinPTMSite> m_proteinPTMSiteNoRedundantSet = new HashSet<>();
    
    private final ArrayList<String> m_modificationsArray = new ArrayList<>();
    private final HashMap<String, Integer> m_modificationsMap = new HashMap<>();
    
    private final ArrayList<Character> m_residuesArray = new ArrayList<>();
    private final HashMap<Character, Integer> m_residuesMap = new HashMap<>();
    
    private String m_modelName;
    
    private String m_modificationInfo = "";
    
    private boolean m_hideRedundantPeptides = false;
    
    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();
    
    public PtmProtenSiteTableModel(LazyTable table) {
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
        
        switch (col){
            case COLTYPE_PROTEIN_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_NAME:
                return DPeptideMatch.class;
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_MODIFICATION:
            case COLTYPE_MODIFICATION_LOC:
            case COLTYPE_PROTEIN_NTERM_CTERM:
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

        DProteinPTMSite proteinPTMSite = m_arrayInUse.get(row);
        DProteinMatch proteinMatch = proteinPTMSite.getPoteinMatch();
        DPeptideMatch peptideMatch = proteinPTMSite.getPeptideMatch();
        DPeptidePTM peptidePtm = proteinPTMSite.getPeptidePTM();

        switch (col){
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
                DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePtm.getIdPtmSpecificity());
                return infoPtm.getPtmShortName();
            }
            case COLTYPE_MODIFICATION_LOC: {
                DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePtm.getIdPtmSpecificity());
                String locationSpecitifcity = infoPtm.getLocationSpecificity();
                if (locationSpecitifcity.contains("N-term")) {
                    return "N-term";
                } else if (locationSpecitifcity.contains("C-term")) {
                    return "C-term";
                }
                
                return String.valueOf((int) peptidePtm.getSeqPosition());
            }
            case COLTYPE_PROTEIN_LOC: {

                int start = peptideMatch.getSequenceMatch().getId().getStart();
                int locationOnPeptide = (int) peptidePtm.getSeqPosition(); 
                int proteinSite = start+locationOnPeptide-1;
                return proteinSite;
            }
            case COLTYPE_PROTEIN_NTERM_CTERM: {
                DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePtm.getIdPtmSpecificity());
                String locationSpecitifcity = infoPtm.getLocationSpecificity();
                if (locationSpecitifcity.contains("-term")) {
                    return locationSpecitifcity;
                }
                return "";
            }
            case COLTYPE_PTM_PROBA: {
                
                //JPM.TODO : to be done in the task beforehand
                String properties = peptideMatch.getSerializedProperties();
                
                if (properties != null) {

                    try {
                    JsonParser parser = new JsonParser();

                    JsonObject jsonObject = parser.parse(properties).getAsJsonObject();
                    JsonObject ptmPropertiesObject = jsonObject.getAsJsonObject("ptm_site_properties");  // JPM.COMPATIBILITY : this var no longer exists in new databases

                    if (ptmPropertiesObject != null) {
                        
                        JsonPrimitive globalPercentage = ptmPropertiesObject.getAsJsonPrimitive("mascot_delta_score");
                        if (globalPercentage == null) {
                            JsonObject mascotPtmPropertiesObject = ptmPropertiesObject.getAsJsonObject("mascot_ptm_site_properties");
                            globalPercentage = mascotPtmPropertiesObject.getAsJsonPrimitive("mascot_delta_score");
                        }
                        
                        if (globalPercentage != null) {
                            return globalPercentage.getAsDouble() * 100;
                        }

                    }
                    } catch (Exception e) {
                        // should not happen
                        //System.err.println(e.getMessage());
                    }

                    // should not happen
                }
                
                return null;
            }
            case COLTYPE_MODIFICATION_PROBA: {
                
                //JPM.TODO : to be done in the task beforehand
                String properties = peptideMatch.getSerializedProperties();
                
                if (properties != null) {

                    try {
                    JsonParser parser = new JsonParser();

                    JsonObject jsonObject = parser.parse(properties).getAsJsonObject();
                    JsonObject ptmPropertiesObject = jsonObject.getAsJsonObject("ptm_site_properties");  // JPM.COMPATIBILITY : this var no longer exists in new databases
                    if (ptmPropertiesObject != null) {
                        
                        JsonObject siteProbabilities = ptmPropertiesObject.getAsJsonObject("mascot_probability_by_site");
                        if (siteProbabilities == null) {
                            JsonObject mascotPtmPropertiesObject = ptmPropertiesObject.getAsJsonObject("mascot_ptm_site_properties");
                            if (mascotPtmPropertiesObject != null) {
                                siteProbabilities = mascotPtmPropertiesObject.getAsJsonObject("site_probabilities");
                            }
                        }
                        if (siteProbabilities != null) {
                            Set<Entry<String, JsonElement>> entrySet = siteProbabilities.entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                String key = entry.getKey();
                                int modificationPos = -1;
                                if (key.contains("N-term")) {
                                    modificationPos = 0;
                                } else if (key.contains("C-term")) {
                                    modificationPos = peptideMatch.getPeptide().getSequence().length();
                                } else {
                                    int startIndex = key.indexOf('(');
                                    if (startIndex != -1) {
                                        startIndex++;
                                        char c = key.charAt(startIndex);
                                        while (c >= 'A' && c <= 'Z') {
                                            startIndex++;
                                            c = key.charAt(startIndex);
                                        }
                                        int endIndex = key.indexOf(')');
                                        if (endIndex != -1) {
                                            String modificationPosString = key.substring(startIndex, endIndex);
                                            modificationPos = Integer.valueOf(modificationPosString);

                                        }
                                    }
                                }
                                if (modificationPos == peptidePtm.getSeqPosition()) {
                                    JsonPrimitive value = siteProbabilities.getAsJsonPrimitive(key);
                                    double percentage = value.getAsDouble() * 100;
                                    return percentage;
                                }
                            }
                        }

                    }
                    } catch (Exception e) {
                        // should not happen
                        //System.err.println(e.getMessage());
                    }

                    // should not happen
                }
                
                return null;
            }
            case COLTYPE_DELTA_MASS_PTM:
                return proteinPTMSite.getDeltaMassPTM();
                
            case COLTYPE_DELTA_MASS_MODIFICATION: {
                DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePtm.getIdPtmSpecificity());
                return infoPtm.getMonoMass();
            }
            case COLTYPE_RESIDUE_AA: {
                DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePtm.getIdPtmSpecificity());
                return infoPtm.getRresidueAASpecificity();
            }
            case COLTYPE_HIDDEN_PROTEIN_PTM:
                return proteinPTMSite;
        }

        
        
        return null; // should never happen
    }
    private static final StringBuilder m_sb = new StringBuilder(20);


    

     
    
    public void setData(Long taskId, ArrayList<DProteinPTMSite> proteinPTMSiteArray) {
        m_proteinPTMSiteArray = proteinPTMSiteArray;
        m_arrayInUse = m_proteinPTMSiteArray;
        m_taskId = taskId;
 
        m_modificationInfo = PtmProteinSiteTableModelProcessing.calculateData(this, m_modificationsArray, m_residuesArray, m_residuesMap, proteinPTMSiteArray, m_proteinPTMSiteNoRedundantArray, m_modificationsMap);

        m_proteinPTMSiteNoRedundantSet.clear();
        m_proteinPTMSiteNoRedundantSet.addAll(m_proteinPTMSiteNoRedundantArray);

        
        fireTableDataChanged();

    }
    
    public String getModificationsInfo() {
        return m_modificationInfo;
    }
    
    public HashSet<DProteinPTMSite> getPTMSiteNoRedundantSet() {
        return m_proteinPTMSiteNoRedundantSet;
    }

 
    

    public void dataUpdated() {

            fireTableDataChanged();

    }

    public DProteinPTMSite getProteinPTMSite(int i) {

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
        int[] keys = {  COLTYPE_PROTEIN_NAME };
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

        filtersMap.put(COLTYPE_MODIFICATION_PROBA, new DoubleFilter(getColumnName(COLTYPE_MODIFICATION_PROBA), null, COLTYPE_MODIFICATION_PROBA));

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
            DProteinPTMSite proteinPTMSite = m_arrayInUse.get(row);
            DPeptideMatch peptideMatch = proteinPTMSite.getPeptideMatch();
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
            return m_sameSetCount+m_subSetCount;
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
            int sameSet =  m_sameSetCount-((ProteinCount) o).m_sameSetCount;
            if (sameSet != 0) {
                return sameSet;
            }
            return m_subSetCount-((ProteinCount) o).m_subSetCount;
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
        list.add(new ExtraDataType(DProteinPTMSite.class, true));
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
        if (c.equals(DProteinPTMSite.class)) {
            return m_arrayInUse.get(row);
        }
        if (c.equals(DProteinMatch.class)) {
            return m_arrayInUse.get(row).getPoteinMatch();
        }
        if (c.equals(DPeptideMatch.class)) {
            return ((DProteinPTMSite) m_arrayInUse.get(row)).getPeptideMatch();
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }
    
}
