package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class XicPeptideMatchTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_PEPTIDE_QC_NAME = 1;
    public static final int COLTYPE_PEPTIDE_NAME = 2;
    public static final int COLTYPE_PEPTIDE_SCORE = 3;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 4;
    public static final int COLTYPE_PEPTIDE_RANK = 5;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 6;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 7;
    public static final int COLTYPE_PEPTIDE_PPM = 8;  
    public static final int COLTYPE_PEPTIDE_CHARGE = 9;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 10;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 11;  
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 12;
    public static final int COLTYPE_PEPTIDE_PTM = 13;
    
    
    
    private static final String[] m_columnNames = {"Id", "Quant. channel", "Peptide", "Score",  "MsQuery", "Rank", "Calc. Mass", "Exp. m/z", "Ppm" , "Charge", "Missed Cl.", "RT","Ion Parent Int.", "PTM"};
    private static final String[] m_toolTipColumns = {"PeptideMatch Id", "Quantitation channel", "Peptide", "Score", "MsQuery", "Rank", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million" , "Charge", "Missed Clivage", "Retention Time (min)", "Ion Parent Intensity", "Post Translational Modifications"};
        
    private DQuantitationChannel[] m_quantitationChannelArray = null;
    private Map<Long, List<Long>> m_peptideMatchListPerQC;
    private List<DPeptideMatch> m_peptideMatchList;
    private QuantChannelInfo m_quantChannelInfo;

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();
    
    private String m_modelName;


    public XicPeptideMatchTableModel(LazyTable table) {
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
    public String getExportColumnName(int col) {
        return m_columnNames[col];
        
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_toolTipColumns[col];
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_PEPTIDE_ID) {
            return Long.class;
        }
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
    }

    @Override
    public int getRowCount() {
        if (m_peptideMatchList == null) {
            return 0;
        }

        return m_peptideMatchList.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        LazyData lazyData = getLazyData(row,col);

        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatchList.get(row);

        
        if (peptideMatch == null) {
            givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MISSED_CLIVAGE); // no data att all : need to read PeptideMatch like for the COLTYPE_PEPTIDE_MISSED_CLIVAGE column
            lazyData.setData(null);
            return lazyData;
        }
        
        switch (col) {
            case COLTYPE_PEPTIDE_ID: {
                return peptideMatch.getId();
            }
            case COLTYPE_PEPTIDE_QC_NAME: {
                int idQc = getPeptideMatchQuantChannel(Long.valueOf(peptideMatch.getId())) ;
                if (idQc != -1) {
                    StringBuilder sb = new StringBuilder();
                    String rsmHtmlColor =  m_quantChannelInfo.getRsmHtmlColor(idQc);
                    sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                    sb.append(m_quantitationChannelArray[idQc].getResultFileName());
                    sb.append("</html>");
                    lazyData.setData(sb.toString());
                    return lazyData;
                }else{
                    lazyData.setData("");
                    return lazyData;
                }
            }
            case COLTYPE_PEPTIDE_NAME: {
                lazyData.setData(peptideMatch);
                
                return lazyData;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match

                Float score = peptideMatch.getScore() ;
                lazyData.setData(score);
                return lazyData;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {

                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    
                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    lazyData.setData( msQuery );
                }
                return lazyData;

   
            }
            case COLTYPE_PEPTIDE_RANK: {
                lazyData.setData(peptideMatch.getCDPrettyRank());
                return lazyData;
            }

            case COLTYPE_PEPTIDE_CHARGE: {
                lazyData.setData(peptideMatch.getCharge());
                return lazyData;
            }
             
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                lazyData.setData(Float.valueOf((float) peptideMatch.getExperimentalMoz()));
                return lazyData;
            }
            case COLTYPE_PEPTIDE_PPM: { 
                
                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    return lazyData;
                }
                
                
                double deltaMoz = (double) peptideMatch.getDeltaMoz();
                double calculatedMass = peptide.getCalculatedMass() ;
                double charge = (double) peptideMatch.getCharge(); 
                final double CSTE = 1.007825;
                final double EXP_CSTE = StrictMath.pow(10, 6);
                float ppm = (float) ((deltaMoz*EXP_CSTE) / ((calculatedMass + (charge*CSTE))/charge));
                
                if ((calculatedMass >= -1e-10) && (calculatedMass <= 1e-10)) {
                    //calculatedMass == 0; // it was a bug, does no longer exist, but 0 values can exist in database.
                    ppm = 0;
                }

                lazyData.setData(Float.valueOf(ppm));
                return  lazyData;
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {

                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                } else {
                    Float calculatedMass = Float.valueOf((float) peptide.getCalculatedMass()) ;
                    lazyData.setData(calculatedMass);
                }
                
                return lazyData;
 
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                lazyData.setData(Integer.valueOf(peptideMatch.getMissedCleavage()));
                return lazyData;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
               lazyData.setData(peptideMatch.getRetentionTime());
               return lazyData ;
            }
            /*case COLTYPE_PEPTIDE_RETENTION_TIME: { //JPM.TODO : can not do it for the moment: Retention Time is on Peptide Instance
                
                LazyData lazyData = getLazyData(row,col);
                
                PeptideMatch.TransientData data = peptideMatch.getTransientData();
                if (!data.getIsMsQuerySet()) {
                    givePriorityTo(taskId, row, COLTYPE_PEPTIDE_MSQUERY);
                    lazyData.setData(null);
                    
                } else {
                    MsQuery msQuery = peptideMatch.getMsQuery();
                    
                    if (!msQuery.getTransientIsSpectrumSet()) {
                        givePriorityTo(taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        Spectrum spectrum = msQuery.getTransientIsSpectrumSet() ? msQuery.getSpectrum() : null;
                        if (spectrum == null) {
                            lazyData.setData("");
                        } else {
                            double retentionTime = (spectrum.getFirstTime()+spectrum.getLastTime())/2;
                            lazyData.setData(DataFormat.format(retentionTime, 2));
                        }
                    }  
                }
                return lazyData;
            }*/
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {

                if (!peptideMatch.isMsQuerySet()) {
                    givePriorityTo(m_taskId, row, COLTYPE_PEPTIDE_MSQUERY);
                    lazyData.setData(null);
                    
                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    Float precursorIntenstity = msQuery.getPrecursorIntensity();
                    if (precursorIntenstity == null) {
                        lazyData.setData(Float.NaN);
                    } else {
                        lazyData.setData(precursorIntenstity);
                    }  
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_PTM: {

                Peptide peptide = peptideMatch.getPeptide();
                if (peptide == null) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    return lazyData;
                }
                
                boolean ptmStringLoadeed = peptide.getTransientData().isPeptideReadablePtmStringLoaded();
                if (!ptmStringLoadeed) {
                    givePriorityTo(m_taskId, row, col);
                    lazyData.setData(null);
                    return lazyData;
                }
                
                String ptm = "";
                PeptideReadablePtmString ptmString = peptide.getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }
                
                lazyData.setData(ptm);

                return lazyData;
            }
                
        }
        return null; // should never happen
    }

    public void setData(Long taskId, DQuantitationChannel[] quantChannelArray, QuantChannelInfo quantChannelInfo, List<DPeptideMatch> peptideMatchList, Map<Long, List<Long>> peptideMatchIdListPerQC) {
        this.m_quantChannelInfo = quantChannelInfo;
        this.m_quantitationChannelArray = quantChannelArray;
        this.m_peptideMatchListPerQC = peptideMatchIdListPerQC;
        this.m_peptideMatchList = peptideMatchList;

        m_taskId = taskId;

        
        fireTableDataChanged();

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();

    }

    public DPeptideMatch getPSM(int i) {

        return m_peptideMatchList.get(i);
    }
    
    
    public int findRow(long psmId) {

        int nb = m_peptideMatchList.size();
        for (int i = 0; i < nb; i++) {
            if (psmId == m_peptideMatchList.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> psmIds, CompoundTableModel compoundTableModel) {

        if (m_peptideMatchListPerQC == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> psmIdMap = new HashSet<>(psmIds.size());
        psmIdMap.addAll(psmIds);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve PSM
            DPeptideMatch f = getPSM(iModel);
            if (psmIdMap.contains(f.getId())) {
                psmIds.set(iCur++, f.getId());
            }
        }

    }



    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        int colIdx = 0;
        colIdx++;//COLTYPE_PEPTIDE_ID
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_QC_NAME
            
        ConvertValueInterface peptideConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DPeptideMatch) o).getPeptide().getSequence();
            }

        };
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), peptideConverter, colIdx));  colIdx++;//COLTYPE_PEPTIDE_NAME
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_SCORE
            
        ConvertValueInterface msQueryConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DMsQuery) o).getInitialId();
            }

        };
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), msQueryConverter, colIdx));  colIdx++;//COLTYPE_PEPTIDE_MSQUERY
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_RANK
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_CALCULATED_MASS
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_PPM
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_CHARGE
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_MISSED_CLIVAGE
        filtersMap.put(colIdx, new IntegerFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_RETENTION_TIME
        filtersMap.put(colIdx, new DoubleFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_ION_PARENT_INTENSITY
        filtersMap.put(colIdx, new StringFilter(getColumnName(colIdx), null, colIdx));  colIdx++;//COLTYPE_PEPTIDE_PTM

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
        switch (columnIndex){
            case COLTYPE_PEPTIDE_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_QC_NAME:
            case COLTYPE_PEPTIDE_PTM:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_PPM:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return Float.class;
            case COLTYPE_PEPTIDE_MSQUERY:
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
                return Integer.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
        }
        if (data instanceof DPeptideMatch) {
            data = ((DPeptideMatch) data).getPeptide().getSequence();
        } else if (data instanceof DMsQuery) {
            data = Integer.valueOf(((DMsQuery) data).getInitialId());
        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_ID };
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
    public PlotType getBestPlotType() {
        return PlotType.HISTOGRAM_PLOT;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case HISTOGRAM_PLOT:
                return COLTYPE_PEPTIDE_PPM;
            case SCATTER_PLOT:
                return COLTYPE_PEPTIDE_CALCULATED_MASS;
        }
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case SCATTER_PLOT:
                return COLTYPE_PEPTIDE_SCORE;
        }
        return -1;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PEPTIDE_NAME;
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
    public String getTootlTipValue(int row, int col) {
        return "";
    }
    
    private int  getPeptideMatchQuantChannel(Long peptideMatchId) {
        int nbQC = m_quantitationChannelArray.length ;
        for (Map.Entry<Long, List<Long>> entrySet : m_peptideMatchListPerQC.entrySet()) {
            long qcId = (long) entrySet.getKey();
            List<Long> psmIds = entrySet.getValue();
            int id = psmIds.indexOf(peptideMatchId);
            if (id > -1) {
                for(int q=0; q<nbQC;q++) {
                    if (m_quantitationChannelArray[q].getId() == qcId) {
                        return q;
                    }
                }
                return -1;// should not happen
            }
        }
        return -1; // should not happen
    }

    @Override
    public String getExportRowCell(int row, int col) {
       int rowFiltered = row;

        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatchList.get(rowFiltered);

        switch (col) {
            case COLTYPE_PEPTIDE_ID: {
                return Long.toString(peptideMatch.getId());
            }
            case COLTYPE_PEPTIDE_QC_NAME: {
                int idQc = getPeptideMatchQuantChannel(Long.valueOf(peptideMatch.getId())) ;
                if (idQc != -1) {
                    return m_quantitationChannelArray[idQc].getResultFileName();
                } else {
                    return "";
                }
            }
            
            case COLTYPE_PEPTIDE_NAME: {
                return peptideMatch.getPeptide().getSequence();
            }
            case COLTYPE_PEPTIDE_SCORE: {
                return Float.toString(peptideMatch.getScore());
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                if (!peptideMatch.isMsQuerySet()) {
                    return "";
                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    return Integer.toString(msQuery.getInitialId());
                }
            }
            case COLTYPE_PEPTIDE_RANK: {
                return Integer.toString(peptideMatch.getCDPrettyRank());
            }

            case COLTYPE_PEPTIDE_CHARGE: {
                return Integer.toString(peptideMatch.getCharge());
            }
             
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                return Double.toString(peptideMatch.getExperimentalMoz());
            }
            case COLTYPE_PEPTIDE_PPM: { 
                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    return "";
                }
                
                double deltaMoz = (double) peptideMatch.getDeltaMoz();
                double calculatedMass = peptide.getCalculatedMass() ;
                double charge = (double) peptideMatch.getCharge(); 
                final double CSTE = 1.007825;
                final double EXP_CSTE = StrictMath.pow(10, 6);
                float ppm = (float) ((deltaMoz*EXP_CSTE) / ((calculatedMass + (charge*CSTE))/charge));
                
                if ((calculatedMass >= -1e-10) && (calculatedMass <= 1e-10)) {
                    //calculatedMass == 0; // it was a bug, does no longer exist, but 0 values can exist in database.
                    ppm = 0;
                }

                return  Float.toString(ppm);
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                Peptide peptide = peptideMatch.getPeptide();
                if ( peptide == null) {
                    return "";
                } else {
                    return Double.toString(peptide.getCalculatedMass());
                }
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                return Integer.toString(peptideMatch.getMissedCleavage());
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
               return Float.toString(peptideMatch.getRetentionTime());
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {

                if (!peptideMatch.isMsQuerySet()) {
                    return "";
                    
                } else {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    Float precursorIntenstity = msQuery.getPrecursorIntensity();
                    if (precursorIntenstity == null) {
                        return "";
                    } else {
                        return Float.toString(precursorIntenstity);
                    }  
                }
            }
            case COLTYPE_PEPTIDE_PTM: {

                Peptide peptide = peptideMatch.getPeptide();
                if (peptide == null) {
                    return "";
                }
                
                boolean ptmStringLoadeed = peptide.getTransientData().isPeptideReadablePtmStringLoaded();
                if (!ptmStringLoadeed) {
                    return "";
                }
                
                String ptm = "";
                PeptideReadablePtmString ptmString = peptide.getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }
                return ptm;
            }
            default:
                return "";
        }
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }
    
    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col){

            case COLTYPE_PEPTIDE_NAME: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_QC_NAME:
            case COLTYPE_PEPTIDE_PTM: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PPM: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4);
                break;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                renderer = new MsQueryRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
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

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(DPeptideMatch.class, true));
        list.add(new ExtraDataType(QuantChannelInfo.class, false));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        if (c.equals(QuantChannelInfo.class)) {
            return m_quantChannelInfo;
        }
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DPeptideMatch.class)) {
            return m_peptideMatchList.get(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }
    
}
