package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.LazyTable;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
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

    public static final int COLTYPE_PROTEIN_SET_ID = 0;
    public static final int COLTYPE_PROTEIN_SET_NAME = 1;
    public static final int COLTYPE_PROTEIN_SET_DESCRIPTION = 2;
    public static final int COLTYPE_PROTEIN_SCORE = 3;
    public static final int COLTYPE_PROTEINS_COUNT = 4;
    public static final int COLTYPE_PEPTIDES_COUNT = 5;
    public static final int COLTYPE_SPECTRAL_COUNT = 6;
    public static final int COLTYPE_SPECIFIC_SPECTRAL_COUNT = 7;
    public static final int COLTYPE_UNIQUE_SEQUENCES_COUNT = 8;
    private static final String[] m_columnNames = {"Id", "Protein Set", "Description", "Score", "Proteins", "Peptides", "Peptide Match Count", "Specific Peptide Match Count", "Sequence Count"};
    
    private DProteinSet[] m_proteinSets = null;
    
    //private boolean m_isFiltering = false;
    //private boolean m_filteringAsked = false;
    
    private String m_modelName;
    
    
    public ProteinSetTableModel(LazyTable table) {
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
        if (col == COLTYPE_PROTEINS_COUNT) {
            String urlSameset = IconManager.getURLForIcon(IconManager.IconType.SAME_SET);
            String urlSubset = IconManager.getURLForIcon(IconManager.IconType.SUB_SET);
            return "<html>Number of Proteins ( Sameset <img src=\"" + urlSameset + "\">&nbsp;,&nbsp; Subset <img src=\"" + urlSubset + "\">)</html>";
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
        if (col == COLTYPE_PROTEIN_SET_ID) {
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
        /*if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }*/
        return m_proteinSets.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        
        /*int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }*/
        
        // Retrieve Protein Set
        DProteinSet proteinSet = m_proteinSets[row];
        long rsmId = proteinSet.getResultSummaryId();

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
                Integer spectralCount = proteinSet.getSpectralCount();
                lazyData.setData(spectralCount);
                if (spectralCount == null) {
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT: {
                LazyData lazyData = getLazyData(row,col);
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
    private static StringBuilder m_sb = new StringBuilder(20);


    

     
    
    public void setData(Long taskId, DProteinSet[] proteinSets) {
        m_proteinSets = proteinSets;
        m_taskId = taskId;
        
        updateMinMax();
        
        /*if (m_restrainIds != null) {
            m_restrainIds = null;
            m_filteringAsked = true;
        }
        
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {*/
            fireTableDataChanged();
        //}
        
        
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
        
        /*if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (proteinSetId == m_proteinSets[m_filteredIds.get(i)].getId()) {
                    return i;
                }
            }
            return -1;
        }*/
        
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
        int[] keys = { COLTYPE_PROTEIN_SET_NAME, COLTYPE_PROTEIN_SET_ID };
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
        filtersMap.put(COLTYPE_PROTEIN_SET_NAME, new StringFilter(getColumnName(COLTYPE_PROTEIN_SET_NAME), null));
        filtersMap.put(COLTYPE_PROTEIN_SET_DESCRIPTION, new StringFilter(getColumnName(COLTYPE_PROTEIN_SET_DESCRIPTION), null));
        filtersMap.put(COLTYPE_PROTEIN_SCORE, new DoubleFilter(getColumnName(COLTYPE_PROTEIN_SCORE), null));
        filtersMap.put(COLTYPE_PEPTIDES_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDES_COUNT), null));
        filtersMap.put(COLTYPE_SPECTRAL_COUNT, new IntegerFilter(getColumnName(COLTYPE_SPECTRAL_COUNT), null));
        filtersMap.put(COLTYPE_SPECIFIC_SPECTRAL_COUNT, new IntegerFilter(getColumnName(COLTYPE_SPECIFIC_SPECTRAL_COUNT), null));
        filtersMap.put(COLTYPE_UNIQUE_SEQUENCES_COUNT, new IntegerFilter(getColumnName(COLTYPE_UNIQUE_SEQUENCES_COUNT), null));
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
            // COLTYPE_PEPTIDES_COUNT COLTYPE_SPECTRAL_COUNT COLTYPE_SPECIFIC_SPECTRAL_COUNT COLTYPE_UNIQUE_SEQUENCES_COUNT
           
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
    
}
