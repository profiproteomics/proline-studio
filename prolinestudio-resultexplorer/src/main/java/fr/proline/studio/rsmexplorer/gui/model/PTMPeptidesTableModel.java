/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.table.ExportModelUtilities;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMPeptidesTableModel extends LazyTableModel implements GlobalTableModelInterface {
    
    
    // Static columns list
    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_PEPTIDE_NAME = 1;
    public static final int COLTYPE_PEPTIDE_SCORE = 2;
    public static final int COLTYPE_PEPTIDE_PTM = 3;    
    public static final int COLTYPE_DELTA_MASS_PTM = 4;
    public static final int COLTYPE_PTM_PROBA = 5;
    public static final int COLTYPE_SPECTRUM_TITLE = 6;   
    public static final int LAST_STATIC_COLUMN = COLTYPE_SPECTRUM_TITLE;
    
    private static final String[] m_columnNames = {"Id", "Peptide", "Score", "PTM", "PTM D.Mass", "PTM Probability", "Spectrum title"};
    private static final String[] m_columnTooltips = {"Peptide Id (Instance Id)", "Peptide Sequence", "Score of the peptide match", "All PTMs modifications associated with this peptide", "All PTMs delta mass", "All PTMs probability", "Peptide match query title"};

    // Dynamic columns list 1
    private static final String COLTYPE_SITE_PROBA_SUFFIX = "Probability";
    private static final String COLTYPE_SITE_PROBA_TOOLTIP = "Probability of the specific site for this peptide";
    private int m_ptmSiteCount;
    
    // Dynamic columns list 2 
    public static final int DYNAMIC_COLTYPE_SELECTION_LEVEL = 0;
    public static final int DYNAMIC_COLTYPE_IDENT_PSM = 1;
    public static final int DYNAMIC_COLTYPE_PSM = 2;
    public static final int DYNAMIC_COLTYPE_RAW_ABUNDANCE = 3;
    public static final int DYNAMIC_COLTYPE_ABUNDANCE = 4;
    public static final int DYNAMIC_COLTYPE_RETENTION_TIME = 5;
    private int m_quantChannelCount =0;
    private boolean m_isXicResult;
    
    private static final String[] m_columnNamesQC = {"Sel. level", "Ident. Pep. match count", "Pep. match count", "Raw abundance", "Abundance", "Retention time"};
    private static final String[] m_toolTipQC = {"Selection level", "Identification peptides match count", "Peptides match count", "Raw abundance", "Abundance", "Retention time"};

    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

    private List<PTMPeptideInstance> m_ptmPepInstances = new ArrayList<>();
    private Map<Long, DMasterQuantPeptide> m_quantPeptidesByPepInsId = new HashMap<Long, DMasterQuantPeptide>();
    private Map<String, Map<Long,Float>> m_pepMatchProbaByIdByPTMSite = new HashMap<>();
    private TreeMap<Integer, String> m_dynamiqueColumnNamesByProtLoc = new TreeMap<>();

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    private String m_modelName;
    private boolean m_showPeptideMatches = false;

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    public PTMPeptidesTableModel(LazyTable table, boolean isXicResult){
        super(table);
        m_isXicResult = isXicResult;
    }
    
    /**
     * Set data for this table Model
     *
     * @param taskId : Id of the task which should load data in background if necessary
     * @param ptmPeptides : List of the PTMPeptideInstance to display 
     * @param Map<Long, DMasterQuantPeptide> quantPeptidesByPepInsId : DMasterQuantPeptide associated to PTMPeptideInstance to get Quant info for.
     * May be null if TableModel was not initialized with isXicResult = true.
     * NOT USED showPeptideMatches : specify if all peptide matches should be
     * displayed or only best ones
     * @param parentPepInstance : NOT USED  Specify the peptide instance ID to display
     * peptide matches for. This parameter is only used if showPeptideMatches =
     * true     
     */
    public void setData(Long taskId, List<PTMPeptideInstance> ptmPeptides, Map<Long, DMasterQuantPeptide> quantPeptidesByPepInsId , DPeptideInstance parentPepInstance) {       
        //m_showPeptideMatches = showPeptideMatches;

        m_taskId = taskId;
       
       if( (m_isXicResult && (Objects.equals(ptmPeptides, m_ptmPepInstances) && Objects.equals(m_quantPeptidesByPepInsId, quantPeptidesByPepInsId))) 
               ||  (!m_isXicResult && Objects.equals(ptmPeptides, m_ptmPepInstances))) 
           return;
       
        m_quantPeptidesByPepInsId = quantPeptidesByPepInsId;    
         m_quantChannelCount = 0;
        boolean structureIdentical = true;
        if(m_isXicResult){
            QuantChannelInfo qcInfo = ((QuantChannelInfo) getSingleValue(QuantChannelInfo.class));
            int qcCount = 0;
            if(qcInfo != null)
                qcCount = qcInfo.getQuantChannels().length;
            structureIdentical = (qcCount == m_quantChannelCount);
            m_quantChannelCount = qcCount;       
        }

        //-- Create column and data for probalibilites for all PTM Sites of peptides 
        m_pepMatchProbaByIdByPTMSite = new HashMap<>();
        m_dynamiqueColumnNamesByProtLoc = new TreeMap<>();
        m_ptmPepInstances = ptmPeptides != null ? ptmPeptides : new ArrayList<>();
        m_ptmPepInstances.forEach(ptmPI -> {
            ptmPI.getSites().forEach(site -> { 
                // For each PTMSite of each peptide instance
                String colName = site.toProteinReadablePtmString();
                Integer protLoc = site.getPositionOnProtein();
                m_dynamiqueColumnNamesByProtLoc.put(protLoc, colName);

                Map<Long,Float> probaByPepMatchd= m_pepMatchProbaByIdByPTMSite.get(colName);
                if(probaByPepMatchd == null) {
                    probaByPepMatchd = new HashMap<Long,Float>();
                }
                //Get PTM Site proba for current site for best PSM of Peptide Instance
                // VDS Workaround test for issue #16643  (Still needed ?)
                Float proba = getSiteProba(ptmPI, site.toReadablePtmString(ptmPI.getPeptideInstance().getPeptideId()));
                if(proba==null)
                    proba = getSiteProba(ptmPI, site.toOtherReadablePtmString(ptmPI.getPeptideInstance().getPeptideId()));
                DPeptideMatch pepMatch = ptmPI.getBestPepMatch();
                if(pepMatch != null)
                    probaByPepMatchd.put(pepMatch.getId(), proba);
                m_pepMatchProbaByIdByPTMSite.put(colName, probaByPepMatchd);
            });
        });
        // Test if structure changed since last display
        structureIdentical = structureIdentical && (m_ptmSiteCount==m_pepMatchProbaByIdByPTMSite.size());
        m_ptmSiteCount = m_pepMatchProbaByIdByPTMSite.size();
        if(!structureIdentical){
            setDefaultColumnToHide();
            m_rendererMap.clear();
            fireTableStructureChanged();            
        }
        
        fireTableDataChanged();
    }
    
    private List<Integer> columnsIdsToHide = new ArrayList<>();
    private void setDefaultColumnToHide(){
         columnsIdsToHide = new ArrayList();
         columnsIdsToHide.add(COLTYPE_PEPTIDE_ID); 
         int startQChColIndex = m_columnNames.length+m_ptmSiteCount;
         if (m_isXicResult){
            QuantChannelInfo qcInfo = (QuantChannelInfo)getSingleValue(QuantChannelInfo.class);            
            for (int i = qcInfo.getQuantChannels().length - 1; i >= 0; i--) {
                columnsIdsToHide.add(startQChColIndex + DYNAMIC_COLTYPE_RAW_ABUNDANCE + (i * m_columnNamesQC.length));
                columnsIdsToHide.add(startQChColIndex + DYNAMIC_COLTYPE_SELECTION_LEVEL + (i * m_columnNamesQC.length));                 
            }
         }
    }
    
    public List<Integer> getDefaultColumnsToHide() {       
        return columnsIdsToHide;
    }
        
    public void dataUpdated() {
        fireTableDataChanged();
    }
    
    private Float getSiteProba(PTMPeptideInstance ptmPepInstance, String readablePTM){
        
        DPeptideMatch pepMatch = ptmPepInstance.getBestPepMatch();
        
        if(pepMatch == null){ 
            return null;
        }
        DPtmSiteProperties properties =pepMatch.getPtmSiteProperties();
        if (properties != null) {
            Float proba = properties.getMascotProbabilityBySite().get(readablePTM);
            if(proba != null)
                return proba * 100;
        }
        return null;
    }

    private String getPtmSiteProbaColNameAt(int col){
        if(col>=m_dynamiqueColumnNamesByProtLoc.size())
            return "";
        Iterator<Integer> keys = m_dynamiqueColumnNamesByProtLoc.keySet().iterator();
        Integer keyCol = null;
        for(int i=0; i<col; i++){
            keys.next();// Just go to the expected column (tree map index)
        }
        keyCol =  keys.next();
        return m_dynamiqueColumnNamesByProtLoc.get(keyCol);
    }

    public PTMPeptideInstance getPTMPeptideInstanceAt(int row) {
        if (row < 0 || (row >= getRowCount())) {
            return null;
        }
        return m_ptmPepInstances.get(row);
    }

    public int getPeptideInstanceIndex(PTMPeptideInstance pep) {
        return m_ptmPepInstances.indexOf(pep);        
    }

    @Override
    public int getRowCount() {
        return m_ptmPepInstances.size();
    }

    @Override
    public int getColumnCount() {        
        return m_columnNames.length+m_ptmSiteCount+m_quantChannelCount*m_columnNamesQC.length;
    }

    @Override
    public String getColumnName(int col) {
        if(col <= LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        } else if(col <= LAST_STATIC_COLUMN+m_ptmSiteCount){
            //It's PTM Site proba columns
            if(!m_dynamiqueColumnNamesByProtLoc.isEmpty())
                return getPtmSiteProbaColNameAt(col-m_columnNames.length)+" " +COLTYPE_SITE_PROBA_SUFFIX;            
        } else if (m_isXicResult) {
            //It's XIC specific columns
            QuantChannelInfo qcinfo =(QuantChannelInfo) getSingleValue(QuantChannelInfo.class);
            int dynQchIndex = col - (m_columnNames.length+m_ptmSiteCount);
            int nbQc = dynQchIndex / m_columnNamesQC.length;
            int id = dynQchIndex - (nbQc * m_columnNamesQC.length);
            
            StringBuilder sb = new StringBuilder();            
            sb.append("<html><font color='").append(qcinfo.getRsmHtmlColor(nbQc)).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_columnNamesQC[id]);
            sb.append("<br/>");
            sb.append(qcinfo.getQuantChannels()[nbQc].getName());
            sb.append("</html>");
            return sb.toString();            
        }
        
        return "";
    }

    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        PTMPeptideInstance ptmPepInstance = m_ptmPepInstances.get(rowIndex);
        DPeptideMatch pepMatch = ptmPepInstance.getBestPepMatch();
        
        switch (columnIndex) {
            case COLTYPE_PEPTIDE_ID:
//                if (m_showPeptideMatches) {
//                    return pepMatch.getId();
//                } else {
                    return ptmPepInstance.getPeptideInstance().getId();
//                }
            case COLTYPE_PEPTIDE_NAME:{
                return pepMatch;
            }
            case COLTYPE_PEPTIDE_SCORE:{
                return (pepMatch==null) ? Float.valueOf(0) : pepMatch.getScore();                        
            }                    
            case COLTYPE_PEPTIDE_PTM: {
//                LazyData lazyData = getLazyData(rowIndex, columnIndex);
//                if(pepMatch == null) {
//                    lazyData.setData(null);
//                    givePriorityTo(m_taskId, rowIndex, columnIndex);                    
//                } else {                    
//                    boolean ptmStringLoaded = pepMatch.getPeptide().getTransientData().isPeptideReadablePtmStringLoaded();
//                    if (!ptmStringLoaded) {
//                        return null;
//                    }
//                    String ptm = "";
//                    PeptideReadablePtmString ptmString = pepMatch.getPeptide().getTransientData().getPeptideReadablePtmString();
//                    if (ptmString != null) {
//                        ptm = ptmString.getReadablePtmString();
//                    }
//                    lazyData.setData(ptm);            
//                }
//                return lazyData;
                if(pepMatch == null)
                    return "UNKNOWN";
                String ptm = "";
                PeptideReadablePtmString ptmString = pepMatch.getPeptide().getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }
                return ptm;  
            }
            case COLTYPE_DELTA_MASS_PTM: {
//                LazyData lazyData = getLazyData(rowIndex, columnIndex);
//                if(pepMatch == null){
//                    lazyData.setData(null);
//                    givePriorityTo(m_taskId, rowIndex, columnIndex);
//                } else {
//                    double deltaMass = 0;
//                    for (DPeptidePTM peptidePTM : pepMatch.getPeptidePTMArray()) {
//                        DInfoPTM pepInfoPtm = DInfoPTM.getInfoPTMMap().get(peptidePTM.getIdPtmSpecificity());
//                        deltaMass += pepInfoPtm.getMonoMass();
//                    }
//                    lazyData.setData(deltaMass);
//                }
//                return lazyData;
                double deltaMass = 0;
                 if(pepMatch == null)
                    return deltaMass;
                 if(pepMatch.getPeptidePTMArray() == null)
                     return deltaMass;
                for (DPeptidePTM peptidePTM : pepMatch.getPeptidePTMArray()) {
                    DInfoPTM pepInfoPtm = DInfoPTM.getInfoPTMMap().get(peptidePTM.getIdPtmSpecificity());
                    deltaMass += pepInfoPtm.getMonoMass();
                }
                return deltaMass;
            }
            case COLTYPE_PTM_PROBA: {
               if(pepMatch == null)
                    return Float.valueOf(0);
                DPtmSiteProperties properties = pepMatch.getPtmSiteProperties();
                if (properties != null) {
                    return Float.valueOf(properties.getMascotDeltaScore() * 100);
                }
                return Float.valueOf(0);
                
//                LazyData lazyData = getLazyData(rowIndex, columnIndex);
//                if(pepMatch == null){
//                    lazyData.setData(null);
//                    givePriorityTo(m_taskId, rowIndex, columnIndex); 
//                } else {                   
//                    DPtmSiteProperties properties = pepMatch.getPtmSiteProperties();
//                    if (properties != null) {
//                        lazyData.setData(properties.getMascotDeltaScore() * 100);
//                    } else 
//                        lazyData.setData(null);
//                }
//                return lazyData;
            }
            case COLTYPE_SPECTRUM_TITLE: {
                return (pepMatch==null) ?  "UNKNOWN" : pepMatch.getMsQuery().getDSpectrum().getTitle();    
            }
            default: {                
                //Correspond to dynamically added columns
                if(columnIndex <= (LAST_STATIC_COLUMN+m_ptmSiteCount)){
                    //PTM Proba data 
                    String colName = getPtmSiteProbaColNameAt(columnIndex-m_columnNames.length);
                    Map<Long,Float> probaByPepMatchId = m_pepMatchProbaByIdByPTMSite.get(colName);
                    if(probaByPepMatchId == null)
                        return Float.NaN;
                    return probaByPepMatchId.getOrDefault(pepMatch.getId(), Float.NaN);
                } else {
                    // QChannels data
                    DMasterQuantPeptide qPep = m_quantPeptidesByPepInsId.get(ptmPepInstance.getPeptideInstance().getId());                    
                    LazyData lazyData = getLazyData(rowIndex, columnIndex);
                    if (qPep == null) {
                        lazyData.setData(null);
                        givePriorityTo(m_taskId, rowIndex, columnIndex);
                    } else {
                        int dynQchIndex = columnIndex - (m_columnNames.length+m_ptmSiteCount);
                        int nbQc = dynQchIndex / m_columnNamesQC.length;
                        int id = dynQchIndex - (nbQc * m_columnNamesQC.length);                        
                        Map<Long, DQuantPeptide> quantPeptideByQchIds = qPep.getQuantPeptideByQchIds();   
                        if (quantPeptideByQchIds == null) {
                            switch (id) {
                                case DYNAMIC_COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case DYNAMIC_COLTYPE_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case DYNAMIC_COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case DYNAMIC_COLTYPE_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case DYNAMIC_COLTYPE_IDENT_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case DYNAMIC_COLTYPE_RETENTION_TIME:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                            }
                        } else {
                            QuantChannelInfo qcinfo = (QuantChannelInfo)getSingleValue(QuantChannelInfo.class);
                            DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qcinfo.getQuantChannels()[nbQc].getId());
                            if (quantPeptide == null) {
                                switch (id) {
                                    case DYNAMIC_COLTYPE_SELECTION_LEVEL:
                                        lazyData.setData(Integer.valueOf(0));
                                        break;
                                    case DYNAMIC_COLTYPE_ABUNDANCE:
                                        lazyData.setData(Float.valueOf(0));
                                        break;
                                    case DYNAMIC_COLTYPE_RAW_ABUNDANCE:
                                        lazyData.setData(Float.valueOf(0));
                                        break;
                                    case DYNAMIC_COLTYPE_PSM:
                                        lazyData.setData(Integer.valueOf(0));
                                        break;
                                    case DYNAMIC_COLTYPE_IDENT_PSM:
                                        lazyData.setData(Integer.valueOf(0));
                                        break;
                                    case DYNAMIC_COLTYPE_RETENTION_TIME:
                                        lazyData.setData(Float.valueOf(0));
                                        break;
                                }   
                            } else {
                                switch (id) {
                                    case DYNAMIC_COLTYPE_SELECTION_LEVEL:
                                        lazyData.setData(quantPeptide.getSelectionLevel());
                                        break;
                                    case DYNAMIC_COLTYPE_ABUNDANCE:
                                        lazyData.setData((quantPeptide.getAbundance() == null || quantPeptide.getAbundance().isNaN()) ? Float.valueOf(0) : quantPeptide.getAbundance());
                                        break;
                                    case DYNAMIC_COLTYPE_RAW_ABUNDANCE:
                                        lazyData.setData((quantPeptide.getRawAbundance() == null || quantPeptide.getRawAbundance().isNaN()) ? Float.valueOf(0) : quantPeptide.getRawAbundance());
                                        break;
                                    case DYNAMIC_COLTYPE_PSM:
                                        lazyData.setData(quantPeptide.getPeptideMatchesCount() == null ? Integer.valueOf(0) : quantPeptide.getPeptideMatchesCount());
                                        break;
                                    case DYNAMIC_COLTYPE_IDENT_PSM:
                                        lazyData.setData(quantPeptide.getIdentPeptideMatchCount() == null ? Integer.valueOf(0) : quantPeptide.getIdentPeptideMatchCount());
                                        break;
                                    case DYNAMIC_COLTYPE_RETENTION_TIME:
                                        lazyData.setData(quantPeptide.getElutionTime()== null ? Float.valueOf(0) : (quantPeptide.getElutionTime()/60.0f));
                                }
                            }
                        }
                    } //End MasterQuantPep != null
                    return lazyData;
                } //End Quand Channels columns
            } //End dynamic column   
        } //End switch

      //return null; // should never happen
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col == COLTYPE_PEPTIDE_ID && m_showPeptideMatches) {
            return "Peptide Match Id";
        } else if(col <= LAST_STATIC_COLUMN) {
            return m_columnTooltips[col];
        }else  if(col <= (LAST_STATIC_COLUMN+m_ptmSiteCount)){            
            return COLTYPE_SITE_PROBA_TOOLTIP;
        } else {
            QuantChannelInfo qcInfo = (QuantChannelInfo) getSingleValue(QuantChannelInfo.class);
            int dynQchIndex = col - (m_columnNames.length+m_ptmSiteCount);
            int nbQc = dynQchIndex / m_columnNamesQC.length;
            int id = dynQchIndex - (nbQc * m_columnNamesQC.length);                        
            String rawFilePath = StringUtils.truncate(qcInfo.getQuantChannels()[nbQc].getRawFilePath(), 50); 
            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = qcInfo.getRsmHtmlColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_toolTipQC[id]);
            sb.append("<br/>");
            sb.append(qcInfo.getQuantChannels()[nbQc].getFullName());
            sb.append("<br/>");
            sb.append(rawFilePath);
            sb.append("</html>");
            return sb.toString();
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
            case COLTYPE_SPECTRUM_TITLE: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PEPTIDE_ID: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }            
            case COLTYPE_PTM_PROBA: {
                renderer =  new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 2);
                break;
            }
            case COLTYPE_DELTA_MASS_PTM: {
                renderer = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4);
                break;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            default : {
                if(col <= LAST_STATIC_COLUMN+m_ptmSiteCount){
                    renderer =  new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 2);
                } else {  
                    //Qchannels
                    int dynQchIndex = col - (m_columnNames.length+m_ptmSiteCount);
                    int nbQc = dynQchIndex / m_columnNamesQC.length;
                    int id = dynQchIndex - (nbQc * m_columnNamesQC.length);
                    switch (id) {
                        case DYNAMIC_COLTYPE_SELECTION_LEVEL:
                        case DYNAMIC_COLTYPE_PSM:
                        case DYNAMIC_COLTYPE_IDENT_PSM: {                        
                            renderer =  new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                            break;
                        }
                        case DYNAMIC_COLTYPE_ABUNDANCE:
                        case DYNAMIC_COLTYPE_RAW_ABUNDANCE:{
                            renderer =  new BigFloatOrDoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0);                        
                                    //new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Float.class));            
                            break;
                        }
                        case DYNAMIC_COLTYPE_RETENTION_TIME:{
                            renderer =   new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 2);
                            break;
                        }

                    }              
                    break;
                }
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
    public int getSubTaskId(int col) {
        return -1; // not used ??? VDS : should be !
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_PEPTIDE_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_NAME:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_SPECTRUM_TITLE:
                return String.class;            
            case COLTYPE_DELTA_MASS_PTM:
                return Double.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PTM_PROBA:
                return Float.class;
            default:  //For dynamically columns                                
            {
                //PTM Site Probabilities
                if(columnIndex <= LAST_STATIC_COLUMN+m_ptmSiteCount)
                    return Float.class;
                else {
                    //Qchannels
                    int dynQchIndex = columnIndex - (m_columnNames.length+m_ptmSiteCount);
                    int nbQc = dynQchIndex / m_columnNamesQC.length;
                    int id = dynQchIndex - (nbQc * m_columnNamesQC.length);
                    switch (id) {
                        case DYNAMIC_COLTYPE_SELECTION_LEVEL:
                        case DYNAMIC_COLTYPE_PSM:
                        case DYNAMIC_COLTYPE_IDENT_PSM:                            
                            return Integer.class;
                        case DYNAMIC_COLTYPE_ABUNDANCE:
                        case DYNAMIC_COLTYPE_RAW_ABUNDANCE:
                        case DYNAMIC_COLTYPE_RETENTION_TIME:
                            return Float.class;                            
                    }                    
                }
            }
        }
        return null; // should never happen        
        
    }

    @Override
    public Class getColumnClass(int col) {

        switch (col) {
            case COLTYPE_PEPTIDE_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_NAME:
                return DPeptideMatch.class;
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_SPECTRUM_TITLE:
                return String.class;
            case COLTYPE_DELTA_MASS_PTM:
                return Double.class;
            case COLTYPE_PTM_PROBA:                
            case COLTYPE_PEPTIDE_SCORE:
                return Float.class;
            default:  //For dynamically columns                                
            {
                //PTM Site Probabilities
                if(col <= LAST_STATIC_COLUMN+m_ptmSiteCount)
                    return Float.class;
                else {
                   return LazyData.class;                    
                }
            }
        }        
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();

        }
        if (data instanceof DPeptideMatch) {
            data = ((DPeptideMatch) data).getPeptide().getSequence();
        }
        if (columnIndex == COLTYPE_PEPTIDE_NAME) {
            return ((DPeptideMatch) getValueAt(rowIndex, columnIndex)).getPeptide().getSequence();
        }
        if (columnIndex == COLTYPE_PEPTIDE_ID) {
            return m_ptmPepInstances.get(rowIndex).getPeptideInstance().getId();
        }
        return data;
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
        return getPTMPeptideInstanceAt(row);
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


        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));

        filtersMap.put(COLTYPE_DELTA_MASS_PTM, new DoubleFilter(getColumnName(COLTYPE_DELTA_MASS_PTM), null, COLTYPE_DELTA_MASS_PTM));
        filtersMap.put(COLTYPE_PTM_PROBA, new DoubleFilter(getColumnName(COLTYPE_PTM_PROBA), null, COLTYPE_PTM_PROBA));
        filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringDiffFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
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
            PTMPeptideInstance peptide = m_ptmPepInstances.get(row);
            return ExportModelUtilities.getExportFonts(peptide.getPeptideInstance());
        }
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }


}
