package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.LazyTable;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ProteinCountRenderer;
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
 * Table Model for Protein Sets
 * @author JM235353
 */
public class ProteinSetTableModel extends LazyTableModel implements GlobalTableModelInterface {

    // ---------------------------------------------------
    // ---------------------------------------------------
    // --- JPM.TODO clean up : different columns between merge/non merged Identification summaries
    // --- are not used. Clean it up later when we will be sure that you will not use it
    // --- To be cleaned : fied m_colUsed and associated code.
    // ---------------------------------------------------
    // ---------------------------------------------------
    
    
    public static final int COLTYPE_PROTEIN_SET_ID = 0;
    public static final int COLTYPE_PROTEIN_SET_NAME = 1;
    public static final int COLTYPE_PROTEIN_SET_DESCRIPTION = 2;
    public static final int COLTYPE_PROTEIN_SCORE = 3;
    public static final int COLTYPE_PROTEINS_COUNT = 4;
    public static final int COLTYPE_PEPTIDES_COUNT = 5;
    public static final int COLTYPE_SPECTRAL_COUNT = 6;
    public static final int COLTYPE_SPECIFIC_SPECTRAL_COUNT = 7;
    public static final int COLTYPE_UNIQUE_SEQUENCES_COUNT = 8;
    private static final String[] m_columnNames = {"Id", "Protein Set", "Description", "Score", "Proteins", "Peptides", "Spectral Count", "Specific Spectral Count", "Sequence Count"};
    
    private DProteinSet[] m_proteinSets = null;

    private int[] m_colUsed = null;
    
    private String m_modelName;
    
    private boolean m_mergedData = false;
    
    public ProteinSetTableModel(LazyTable table) {
        super(table);
        setColUsed(m_mergedData);
    }
    
    private void setColUsed(boolean mergedRsm) {
        if (mergedRsm) {
            final int[] colUsed = {COLTYPE_PROTEIN_SET_ID, COLTYPE_PROTEIN_SET_NAME, COLTYPE_PROTEIN_SET_DESCRIPTION, COLTYPE_PROTEIN_SCORE, COLTYPE_PROTEINS_COUNT, COLTYPE_PEPTIDES_COUNT, COLTYPE_SPECTRAL_COUNT, COLTYPE_SPECIFIC_SPECTRAL_COUNT, COLTYPE_UNIQUE_SEQUENCES_COUNT};
            m_colUsed = colUsed;
        } else {
            final int[] colUsed = {COLTYPE_PROTEIN_SET_ID, COLTYPE_PROTEIN_SET_NAME, COLTYPE_PROTEIN_SET_DESCRIPTION, COLTYPE_PROTEIN_SCORE, COLTYPE_PROTEINS_COUNT, COLTYPE_PEPTIDES_COUNT, COLTYPE_SPECTRAL_COUNT, COLTYPE_SPECIFIC_SPECTRAL_COUNT, COLTYPE_UNIQUE_SEQUENCES_COUNT};
            m_colUsed = colUsed;
        }
    }
    
    public int convertColToColUsed(int col) {
        for (int i = 0; i < m_colUsed.length; i++) {
            if (col == m_colUsed[i]) {
                return i;
            }
        }
        return -1; // should not happen
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
        int colUsed = m_colUsed[col];
        if (colUsed == COLTYPE_PROTEINS_COUNT) {
            String urlSameset = IconManager.getURLForIcon(IconManager.IconType.SAME_SET);
            String urlSubset = IconManager.getURLForIcon(IconManager.IconType.SUB_SET);
            return "<html>Number of Proteins ( Sameset <img src=\"" + urlSameset + "\">&nbsp;,&nbsp; Subset <img src=\"" + urlSubset + "\">)</html>";
        } else if ((colUsed == COLTYPE_SPECTRAL_COUNT) && (m_mergedData)) {
            return "Number of PSM for all the merged Identification Summaries";
        } else {
            return getColumnName(col);
        }
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        int colUsed = m_colUsed[col];
        if (colUsed == COLTYPE_PROTEIN_SET_ID) {
            return Long.class;
        }
        return LazyData.class;
    }
    
    @Override
    public int getSubTaskId(int col) {
        switch (col) {
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
        }
        return -1;
    }

    
    @Override
    public int getRowCount() {
        if (m_proteinSets == null) {
            return 0;
        }

        return m_proteinSets.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Protein Set
        DProteinSet proteinSet = m_proteinSets[row];
        long rsmId = proteinSet.getResultSummaryId();

        col =  m_colUsed[col];
        
        switch (col) {
            case COLTYPE_PROTEIN_SET_ID: {
                return proteinSet.getId();
            }
            case COLTYPE_PROTEIN_SET_NAME: {

                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();

                if (proteinMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(proteinMatch.getAccession());
                }
                return lazyData;
            }
            case COLTYPE_PROTEIN_SET_DESCRIPTION: {
                LazyData lazyData = getLazyData(row, col);

                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();

                if (proteinMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(proteinMatch.getDescription());
                }
                return lazyData;
            }
            case COLTYPE_PROTEIN_SCORE: {
                /*Float score = Float.valueOf(proteinSet.getPeptideOverSet().getScore());
                return score;*/
                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
                
                if (proteinMatch == null) {
                    lazyData.setData(null);
                    
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData( Float.valueOf(proteinMatch.getPeptideSet(rsmId).getScore()) );
                }
                return lazyData;
            }
                
            case COLTYPE_PROTEINS_COUNT: {
                
                LazyData lazyData = getLazyData(row,col);
                
                Integer sameSetCount = proteinSet.getSameSetCount();
                Integer subSetCount = proteinSet.getSubSetCount();
                
                if ((sameSetCount == null) || (subSetCount == null)) {
                    // data is not already fetched
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    

                    lazyData.setData(new ProteinCount(sameSetCount, subSetCount));
                }
                
                return lazyData;
            }
            case COLTYPE_PEPTIDES_COUNT: {
                
                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
                
                if (proteinMatch == null) {
                    lazyData.setData(null);
                    
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData( proteinMatch.getPeptideSet(rsmId).getPeptideCount() );
                }
                return lazyData;
   
            }
                case COLTYPE_UNIQUE_SEQUENCES_COUNT: {
                
                LazyData lazyData = getLazyData(row,col);
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
                
                if (proteinMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                   Integer value = -1;  // pas tres propre mais NaN n'existe pas pour les Integer
                   try { 
                        value = ((Integer)proteinMatch.getPeptideSet(rsmId).getSequenceCount());
                    } catch (Exception e) { }
                   
                    lazyData.setData( value );
                }
                return lazyData;
   
            }
            case COLTYPE_SPECTRAL_COUNT: {
                LazyData lazyData = getLazyData(row,col);
                if (m_mergedData) {
                    lazyData.setData("Not Available");
                    return lazyData;
                }
                Integer spectralCount = proteinSet.getSpectralCount();
                lazyData.setData(spectralCount);
                if (spectralCount == null) {
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT: {
                LazyData lazyData = getLazyData(row,col);
                if (m_mergedData) {
                    lazyData.setData("Not Available");
                    return lazyData;
                }
                Integer specificSpectralCount = proteinSet.getSpecificSpectralCount();
                lazyData.setData(specificSpectralCount);
                if (specificSpectralCount == null) {
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }
        }
        return null; // should never happen
    }
    private static final StringBuilder m_sb = new StringBuilder(20);


    

     
    
    public void setData(Long taskId, DProteinSet[] proteinSets, boolean mergedData) {
        m_proteinSets = proteinSets;
        m_taskId = taskId;
        
        updateMinMax();

        if (m_mergedData != mergedData) {
            m_mergedData = mergedData;
            setColUsed(mergedData);
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }

    }
    
    private void updateMinMax() {
        
        if (m_proteinSets == null) {
            return;
        }
        
        RelativePainterHighlighter.NumberRelativizer relativizer = m_table.getRelativizer();
        if (relativizer == null) {
            return;
        }

        double maxScore = 0;
        int size = m_proteinSets.length;
        for (int i = 0; i < size; i++) {
            
            // Retrieve Protein Set
            DProteinSet proteinSet = m_proteinSets[i];

            DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
            if (proteinMatch != null) {
                long rsmId = proteinSet.getResultSummaryId();
                double score = proteinMatch.getPeptideSet(rsmId).getScore();
                if (score > maxScore) {
                    maxScore = score;
                }
            }
        }
        relativizer.setMax(maxScore);

    }
    
    public void dataUpdated() {
    
        // no need to do an updateMinMax : scores are known at once
        
        /*if (m_filteredIds != null) {
            filter();
        } else {*/
            fireTableDataChanged();
        //}
    }

    public DProteinSet getProteinSet(int i) {
        
        /*if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }*/
        
        return m_proteinSets[i];
    }
    
    public Long getResultSummaryId() {
        if ((m_proteinSets == null) || (m_proteinSets.length == 0)) {
            return null;
        }
        
        return m_proteinSets[0].getResultSummaryId();
    }

    public int findRow(long proteinSetId) {

        int nb = m_proteinSets.length;
        for (int i=0;i<nb;i++) {
            if (proteinSetId == m_proteinSets[i].getId()) {
                return i;
            }
        }
        return -1;
        
    }
    
    
    public void sortAccordingToModel(ArrayList<Long> proteinSetIds, CompoundTableModel compoundTableModel) {
        
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
        columnIndex =  m_colUsed[columnIndex];
        switch (columnIndex) {
            case COLTYPE_PROTEIN_SET_ID: {
                return Long.class;
            }
            case COLTYPE_PROTEIN_SET_NAME:
            case COLTYPE_PROTEIN_SET_DESCRIPTION: {
                return String.class;
            }
            case COLTYPE_PROTEIN_SCORE: {
                return Float.class;
            }
            case COLTYPE_PROTEINS_COUNT:
            case COLTYPE_PEPTIDES_COUNT:
            case COLTYPE_UNIQUE_SEQUENCES_COUNT:
            case COLTYPE_SPECTRAL_COUNT:
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT: {
                return Integer.class;
            }
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            if (data instanceof ProteinCount) {
                return ((ProteinCount) data).getNbProteins();
            }
        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {  convertColToColUsed(COLTYPE_PROTEIN_SET_NAME),  convertColToColUsed(COLTYPE_PROTEIN_SET_ID) };
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
        return COLTYPE_PROTEIN_SET_NAME;
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
        return PlotType.HISTOGRAM_PLOT;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
                switch (plotType) {
            case HISTOGRAM_PLOT:
                return COLTYPE_PEPTIDES_COUNT;
            case SCATTER_PLOT:
                return COLTYPE_PEPTIDES_COUNT;
        }
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return COLTYPE_PROTEIN_SCORE;
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
            case COLTYPE_PROTEIN_SET_NAME:
            case COLTYPE_PROTEIN_SET_DESCRIPTION: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PROTEIN_SCORE: {
                renderer = new FloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PROTEINS_COUNT: {
                renderer = new ProteinCountRenderer();
                break;
            }
            // COLTYPE_PEPTIDES_COUNT COLTYPE_SPECTRAL_COUNT COLTYPE_BASIC_SPECTRAL_COUNT COLTYPE_SPECIFIC_SPECTRAL_COUNT COLTYPE_UNIQUE_SEQUENCES_COUNT
           
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
        list.add(new ExtraDataType(DProteinSet.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getValue(Class c, int row) {
        if (c.equals(DProteinSet.class)) {
            return m_proteinSets[row];
        }
        return null;
    }
    
}
