package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.export.ExportRowTextInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class XicPeptideMatchTableModel extends LazyTableModel implements ExportTableSelectionInterface, ExportColumnTextInterface, ExportRowTextInterface, CompareDataInterface, BestGraphicsInterface{

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
    private static final String[] m_toolTipColumns = {"PeptideMatch Id", "Quantitation channel", "Peptide", "Score", "MsQuery", "Rank", "Calculated Mass", "Experimental Mass to Charge Ratio", "parts-per-million" , "Charge", "Missed Clivage", "Retention Time", "Ion Parent Intensity", "Post Translational Modifications"};
        
    private DQuantitationChannel[] m_quantitationChannelArray = null;
    private Map<Long, List<Long>> m_peptideMatchListPerQC;
    private List<DPeptideMatch> m_peptideMatchList;
    private QuantChannelInfo m_quantChannelInfo;

    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
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
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_peptideMatchList.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        LazyData lazyData = getLazyData(row,col);
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatchList.get(rowFiltered);

        
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
        
        m_filteredIds = null;
        m_isFiltering = false;

        m_taskId = taskId;

        if (m_restrainIds != null) {
            m_restrainIds = null;
            m_filteringAsked = true;
        }
        
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        if (m_filteredIds != null) {
            filter();
        } else {
            fireTableDataChanged();
        }
    }

    public DPeptideMatch getPSM(int i) {

        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }

        return m_peptideMatchList.get(i);
    }
    
    
    public int findRow(long psmId) {

        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (psmId == m_peptideMatchList.get(m_filteredIds.get(i)).getId()) {
                    return i;
                }
            }
            return -1;
        }

        int nb = m_peptideMatchList.size();
        for (int i = 0; i < nb; i++) {
            if (psmId == m_peptideMatchList.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> psmIds) {

        if (m_peptideMatchListPerQC == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> psmIdMap = new HashSet<>(psmIds.size());
        psmIdMap.addAll(psmIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve PSM
            DPeptideMatch f = getPSM(iModel);
            if (psmIdMap.contains(f.getId())) {
                psmIds.set(iCur++, f.getId());
            }
        }

        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }

    @Override
    public void filter() {

        if (m_peptideMatchList == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_peptideMatchList.size();
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(Integer.valueOf(i));
            }

        } finally {
            m_isFiltering = false;
        }
        fireTableDataChanged();
    }

    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }

        Object data = ((LazyData) getValueAt(row, col)).getData();
        if (data == null) {
            return true; // should not happen
        }
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        // Retrieve Protein Set
        DPeptideMatch peptideMatch = m_peptideMatchList.get(rowFiltered);
        

        switch (col) {
            case COLTYPE_PEPTIDE_QC_NAME:{
                int idQc = getPeptideMatchQuantChannel(Long.valueOf(peptideMatch.getId())) ;
                if (idQc != -1) {
                    return ((StringFilter) filter).filter(m_quantitationChannelArray[idQc].getResultFileName());
                }
            }
            case COLTYPE_PEPTIDE_NAME:
                return ((StringFilter) filter).filter(((DPeptideMatch)data).getPeptide().getSequence());
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_PPM: 
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_RETENTION_TIME:{
                return ((DoubleFilter) filter).filter((Float)data);
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
              if (data instanceof String) {
                  return true;
              } else {
                  return ((DoubleFilter) filter).filter((Float)data);
              }
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_CHARGE: 
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_MSQUERY: {
                return ((IntegerFilter) filter).filter(((DMsQuery)data).getInitialId());
            }
        }

        return true; // should never happen
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            int colIdx = 0;
            
            
            m_filters[colIdx] = null;  colIdx++;//COLTYPE_PEPTIDE_ID
            m_filters[colIdx] = new StringFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_QC_NAME
            m_filters[colIdx] = new StringFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_NAME
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_SCORE
            m_filters[colIdx] = new IntegerFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_MSQUERY
            m_filters[colIdx] = new IntegerFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_RANK
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_CALCULATED_MASS
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_PPM
            m_filters[colIdx] = new IntegerFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_CHARGE
            m_filters[colIdx] = new IntegerFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_MISSED_CLIVAGE
            m_filters[colIdx] = new IntegerFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_RETENTION_TIME
            m_filters[colIdx] = new DoubleFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_ION_PARENT_INTENSITY
            m_filters[colIdx] = new StringFilter(getColumnName(colIdx));  colIdx++;//COLTYPE_PEPTIDE_PTM
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
    public HashSet exportSelection(int[] rows) {

        int nbRows = rows.length;
        HashSet selectedObjects = new HashSet();
        for (int i = 0; i < nbRows; i++) {

            int row = rows[i];
            int rowFiltered = row;
            if ((!m_isFiltering) && (m_filteredIds != null)) {
                rowFiltered = m_filteredIds.get(row).intValue();
            }

            // Retrieve PSM
            DPeptideMatch psm = m_peptideMatchList.get(rowFiltered);

            selectedObjects.add(psm.getId());
        }
        return selectedObjects;
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
                return String.class;
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_PPM:
                return Float.class;
            case COLTYPE_PEPTIDE_MSQUERY:
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
                return Integer.class;
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
                return Object.class; // Float or String... JPM.TODO
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            
            if (data instanceof DPeptideMatch) {
                data = ((DPeptideMatch)data).getPeptide().getSequence();
            } else if (data instanceof DMsQuery) {
                data = Integer.valueOf(((DMsQuery)data).getInitialId());
            }

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
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
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
                    
                }else{
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

    
}
