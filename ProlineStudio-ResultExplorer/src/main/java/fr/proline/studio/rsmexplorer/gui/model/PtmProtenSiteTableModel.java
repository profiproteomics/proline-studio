package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinPTMSite;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PercentageRenderer;
import fr.proline.studio.table.GlobalTableModelInterface;
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

/**
 *
 * @author JM235353
 */
public class PtmProtenSiteTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PROTEIN_ID = 0;
    public static final int COLTYPE_PROTEIN_NAME = 1;
    public static final int COLTYPE_PEPTIDE_NAME = 2;
    public static final int COLTYPE_PEPTIDE_PTM = 3;
    public static final int COLTYPE_DELTA_MASS_PTM = 4;
    public static final int COLTYPE_PTM_PROBA = 5;
    public static final int COLTYPE_MODIFICATION = 6;
    public static final int COLTYPE_RESIDUE_AA = 7;
    public static final int COLTYPE_DELTA_MASS_MODIFICATION = 8;
    public static final int COLTYPE_MODIFICATION_LOC = 9;
    public static final int COLTYPE_PROTEIN_LOC = 10;
    public static final int COLTYPE_MODIFICATION_PROBA = 11;
    

    
    
    private static final String[] m_columnNames = {"Id", "Protein", "Peptide", "PTM", "PTM D.Mass", "PTM Probability", "Modification", "Residue", "Modification D.Mass", "Modification Loc.", "Protein Loc.", "Modification Probability"};
    private static final String[] m_columnTooltips =  {"Protein Id", "Protein", "Peptide", "Full PTM", "PTM Delta Mass", "PTM Probability", "One of the Modifications", "Modification Residue", "Modification Delta Mass", "Modification Location", "Protein Location of the Modification", "Modification Probability"};
    
    private ArrayList<DProteinPTMSite> m_proteinPTMSiteArray = null;

    private String m_modelName;
    
    private boolean m_mergedData = false;
    
    public PtmProtenSiteTableModel(LazyTable table) {
        super(table);
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
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_MODIFICATION:
                return String.class;
            case COLTYPE_MODIFICATION_LOC:
            case COLTYPE_PROTEIN_LOC:
                return Integer.class;
            case COLTYPE_PTM_PROBA:
            case COLTYPE_MODIFICATION_PROBA:
            case COLTYPE_DELTA_MASS_PTM:
            case COLTYPE_DELTA_MASS_MODIFICATION:
                return Double.class;
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
        if (m_proteinPTMSiteArray == null) {
            return 0;
        }

        return m_proteinPTMSiteArray.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        DProteinPTMSite proteinPTMSite = m_proteinPTMSiteArray.get(row);
        DProteinMatch proteinMatch = proteinPTMSite.getPoteinMatch();
        DPeptideMatch peptideMatch = proteinPTMSite.getPeptideMatch();
        DPeptidePTM peptidePtm = proteinPTMSite.getPeptidePTM();

        switch (col){
            case COLTYPE_PROTEIN_ID:
                return proteinMatch.getId();
            case COLTYPE_PROTEIN_NAME:
                return proteinMatch.getAccession();
            case COLTYPE_PEPTIDE_NAME:
                return peptideMatch.getPeptide().getSequence();
            case COLTYPE_PEPTIDE_PTM:
                return peptideMatch.getPeptide().getPtmString();
            case COLTYPE_MODIFICATION: {
                DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePtm.getIdPtmSpecificity());
                return infoPtm.getPtmShortName();
            }
            case COLTYPE_MODIFICATION_LOC:
                return Integer.valueOf((int) peptidePtm.getSeqPosition());
            case COLTYPE_PROTEIN_LOC:
                int start = peptideMatch.getSequenceMatch().getId().getStart();
                int locationOnPeptide = (int) peptidePtm.getSeqPosition();  // DInfoPTM.getInfoPTMMap().get(peptidePtm.getIdPtmSpecificity()).getLocationSpecificity();
                int proteinSite = start+locationOnPeptide;
                return Integer.valueOf(proteinSite);
            case COLTYPE_PTM_PROBA:
                return 0d; //JPM.TODO
            case COLTYPE_MODIFICATION_PROBA:
                return 0d; //JPM.TODO
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
        }

        
        
        return null; // should never happen
    }
    private static final StringBuilder m_sb = new StringBuilder(20);


    

     
    
    public void setData(Long taskId, ArrayList<DProteinPTMSite> proteinPTMSiteArray) {
        m_proteinPTMSiteArray = proteinPTMSiteArray;
        m_taskId = taskId;
 
        fireTableDataChanged();

    }

    public void dataUpdated() {

            fireTableDataChanged();

    }

    public DProteinPTMSite getProteinPTMSite(int i) {

        return m_proteinPTMSiteArray.get(i);
    }


    /*public int findRow(long proteinSetId) {

        int nb = m_proteinPTMSiteArray.size();
        for (int i=0;i<nb;i++) {
            if (proteinSetId == m_proteinSets[i].getId()) {
                return i;
            }
        }
        return -1;
        
    }*/
    
    
    /*public void sortAccordingToModel(ArrayList<Long> proteinSetIds, CompoundTableModel compoundTableModel) {
        
        if (m_proteinSets == null) {
            // data not loaded 
            return;
        }
        
        HashSet<Long> proteinSetIdMap = new HashSet<>(proteinSetIds.size());
        proteinSetIdMap.addAll(proteinSetIds);
        
        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView=0;iView<nb;iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Protein Set
            DProteinSet ps = getProteinSet(iModel);
            if (  proteinSetIdMap.contains(ps.getId())  ) {
                proteinSetIds.set(iCur++,ps.getId());
            }
        }

    }*/


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
        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return getValueAt(rowIndex, columnIndex);
        /*Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            if (data instanceof ProteinCount) {
                return ((ProteinCount) data).getNbProteins();
            }
        }
        return data;*/
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

        int colIdx = 0;
        
        colIdx++; // COLTYPE_PROTEIN_SET_ID
        
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx)); colIdx++; // COLTYPE_PROTEIN_SET_NAME
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx)); colIdx++; // COLTYPE_PROTEIN_SET_DESCRIPTION
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++; // COLTYPE_PROTEIN_SCORE
        colIdx++; // COLTYPE_PROTEINS_COUNT
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++; // COLTYPE_PEPTIDES_COUNT
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++; // COLTYPE_SPECTRAL_COUNT
        /*if (m_mergedData) {
            filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx)); colIdx++; // COLTYPE_BASIC_SPECTRAL_COUNT
        }*/
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++; // COLTYPE_SPECIFIC_SPECTRAL_COUNT
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++; // COLTYPE_UNIQUE_SEQUENCES_COUNT
    }

    @Override
    public PlotType getBestPlotType() {
        return PlotType.SCATTER_PLOT;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1;
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

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_MODIFICATION: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
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
    public Object getValue(Class c, int row) {
        if (c.equals(DProteinPTMSite.class)) {
            return m_proteinPTMSiteArray.get(row);
        }
        if (c.equals(DProteinMatch.class)) {
            return m_proteinPTMSiteArray.get(row).getPoteinMatch();
        }
        if (c.equals(DPeptideMatch.class)) {
            return m_proteinPTMSiteArray.get(row);
        }
        return null;
    }
    
}
