package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.table.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PercentageRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.StringUtils;
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
 * @author JM235353
 */
public class ProteinPTMClusterTableModel extends LazyTableModel implements GlobalTableModelInterface {

  private final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ptm");

  public static final int COLTYPE_PROTEIN_ID = 0;
  public static final int COLTYPE_PROTEIN_NAME = 1;
  public static final int COLTYPE_PEPTIDE_NAME = 2;
  public static final int COLTYPE_PEPTIDE_SCORE = 3;
  public static final int COLTYPE_PEPTIDE_COUNT = 4;
  public static final int COLTYPE_PTMSITE_COUNT = 5;
  public static final int COLTYPE_PEPTIDE_PTM = 6;
  public static final int COLTYPE_DELTA_MASS_PTM = 7;
  public static final int COLTYPE_PTM_PROBA = 8;
  public static final int COLTYPE_SPECTRUM_TITLE = 9;
  public static final int LAST_STATIC_COLUMN = COLTYPE_SPECTRUM_TITLE;
  
  private static final String[] m_columnNames = {"Id", "Protein", "Peptide", "Score", "Peptide count",  "Modif. Site count", "PTMs", "PTM D.Mass", "PTM Probability", "Spectrum title"};
  private static final String[] m_columnTooltips = {"Protein Id", "Protein", "Peptide", "Score of the peptide match", "Number of peptides matching the modification site", "Number of Modification sites clustered", "Post Translational Modifications of this peptide", "PTMs delta mass", "PTMs probability", "Peptide match spectrum title"};
     
  //Dynamique columns  
  //In case of xic data only
  public static final int COLTYPE_EXPRESSION = LAST_STATIC_COLUMN +1;
  public static final String COLTYPE_EXPRESSION_TITLE ="FC dist.";
  public static final String COLTYPE_EXPRESSION_TOOLTIP ="Fold change distance";
  
  //One per QuantChannel 
  public static final int COLTYPE_START_QUANT_INDEX = COLTYPE_EXPRESSION+1;
  public static final int COLTYPE_RAW_ABUNDANCE = 0;
  public static final int COLTYPE_ABUNDANCE = 1;

  private static final String[] m_columnNamesQC = {"Raw abundance", "Abundance"};
  private static final String[] m_columnTooltipsQC = {"Raw abundance", "Abundance"};
   //Data initialized only if Quantitation data
  private DQuantitationChannel[] m_quantChannels = null;
  private int m_quantChannelNumber = 0;
  private boolean m_isQuantitationDS = false;
  
  //Model data
  private ArrayList<PTMCluster> m_ptmClusters = null;

  private String m_modelName;
  private String m_modificationInfo = "";
  
  private final ScoreRenderer m_scoreRenderer = new ScoreRenderer();

  public ProteinPTMClusterTableModel(LazyTable table) {
    super(table);
  }

  @Override
  public int getColumnCount() {
    // remove last column for identification datasets
    int nbrCol = m_columnNames.length;
    if(m_isQuantitationDS){
       nbrCol= nbrCol + 1 + m_quantChannelNumber * m_columnNamesQC.length;
    }
    return nbrCol;
  }

    @Override
    public String getColumnName(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        } else {
            if (col == COLTYPE_EXPRESSION) {
                return COLTYPE_EXPRESSION_TITLE;
            } else {
                int nbQc = (col - COLTYPE_START_QUANT_INDEX) / m_columnNamesQC.length;
                int id = col - COLTYPE_START_QUANT_INDEX - (nbQc * m_columnNamesQC.length);

                StringBuilder sb = new StringBuilder();
                String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
                sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                sb.append(m_columnNamesQC[id]);
                sb.append("<br/>");
                sb.append(m_quantChannels[nbQc].getName());
                sb.append("</html>");
                return sb.toString();
            }
        }
    }

  @Override
    public String getToolTipForHeader(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnTooltips[col];
        } else {
            if (col == COLTYPE_EXPRESSION) {
                return COLTYPE_EXPRESSION_TOOLTIP;
            } else {
                int nbQc = (col - COLTYPE_START_QUANT_INDEX) / m_columnNamesQC.length;
                int id = col - COLTYPE_START_QUANT_INDEX - (nbQc * m_columnNamesQC.length);
                String rawFilePath = StringUtils.truncate(m_quantChannels[nbQc].getRawFilePath(), 50);

                StringBuilder sb = new StringBuilder();
                String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
                sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                sb.append(m_columnTooltipsQC[id]);
                
                sb.append("<br/>");
                sb.append(m_quantChannels[nbQc].getFullName());
                sb.append("<br/>");
                sb.append(rawFilePath);

                sb.append("</html>");
                return sb.toString();
            }
        }
  }

  @Override
  public String getTootlTipValue(int row, int col) {
    return null;
  }
  
  
  public List<Integer> getDefaultColumnsToHide() {  
    List<Integer> listIds = new ArrayList();
    listIds.add(COLTYPE_PROTEIN_ID);
    if(m_isQuantitationDS){
        for (int i = m_quantChannels.length - 1; i >= 0; i--) {
            listIds.add(COLTYPE_START_QUANT_INDEX + COLTYPE_RAW_ABUNDANCE + (i * m_columnNamesQC.length));            
        }
    }
    return listIds;
  }
  
  @Override
  public Class getColumnClass(int col) {

    switch (col) {
      case COLTYPE_PROTEIN_ID:
        return Long.class;      
      case COLTYPE_PROTEIN_NAME:
        return String.class;
      case COLTYPE_PEPTIDE_COUNT: 
      case COLTYPE_PTMSITE_COUNT:
        return Integer.class;         
      case COLTYPE_PEPTIDE_NAME:
      case COLTYPE_PEPTIDE_PTM:          
      case COLTYPE_SPECTRUM_TITLE:     
      case COLTYPE_PTM_PROBA:  
      case COLTYPE_DELTA_MASS_PTM:
      case COLTYPE_PEPTIDE_SCORE:
        return LazyData.class;     
      case COLTYPE_EXPRESSION:        
        return Double.class;   
    default: 
        return LazyData.class;
    }

  }

  @Override
  public int getSubTaskId(int col) {
    switch (col) {
      case COLTYPE_PEPTIDE_NAME:
      case COLTYPE_PEPTIDE_PTM:          
      case COLTYPE_SPECTRUM_TITLE:           
      case COLTYPE_PTM_PROBA:    
      case COLTYPE_DELTA_MASS_PTM:
      case COLTYPE_PEPTIDE_SCORE:          
        return DatabasePTMsTask.SUB_TASK_PTMCLUSTER_PEPTIDES;
      case COLTYPE_EXPRESSION:
          if(m_isQuantitationDS)
            return DatabaseLoadXicMasterQuantTask.SUB_TASK_PEPTIDE_INSTANCE;
    }
    return -1;
  }
  
  @Override
  public int getRowCount() {
    if (m_ptmClusters == null) {
      return 0;
    }

    return m_ptmClusters.size();
  }

  @Override
  public Object getValueAt(int row, int col) {

    PTMCluster protPTMCluster = m_ptmClusters.get(row);
    DProteinMatch proteinMatch = protPTMCluster.getProteinMatch();
    //DInfoPTM infoPtm = protPTMCluster.getPTMSpecificity();
    DPeptideMatch peptideMatch = protPTMCluster.getBestPeptideMatch();
    LazyData lazyData = getLazyData(row,col);

    switch (col) {
      case COLTYPE_PROTEIN_ID:
        return proteinMatch.getId();
        
      case COLTYPE_PROTEIN_NAME:
        return proteinMatch.getAccession();
        
      case COLTYPE_PEPTIDE_NAME:
        if (peptideMatch == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        }     
        lazyData.setData(peptideMatch);
        return lazyData;
        
      case COLTYPE_PEPTIDE_SCORE:       
        if (peptideMatch == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        }
        lazyData.setData(peptideMatch.getScore());
        return lazyData;

      case COLTYPE_PEPTIDE_PTM:
        if (peptideMatch == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        } 
        String ptm = "";
        PeptideReadablePtmString ptmString = peptideMatch.getPeptide().getTransientData().getPeptideReadablePtmString();
        if (ptmString != null) {
          ptm = ptmString.getReadablePtmString();
        }
        lazyData.setData(ptm);
        return lazyData;
      
      case COLTYPE_PTM_PROBA: {
        if (peptideMatch == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        } 
        Float proba= Float.NaN;
        DPtmSiteProperties properties = peptideMatch.getPtmSiteProperties();
        if (properties != null) {
            proba= (properties.getMascotDeltaScore() * 100);
        }
        lazyData.setData(proba);
        return lazyData;
      }
      
      case COLTYPE_DELTA_MASS_PTM: {

        if (peptideMatch == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        }          
        double deltaMass = 0;
        if(peptideMatch.getPeptidePTMArray() != null){
            for (DPeptidePTM peptidePTM : peptideMatch.getPeptidePTMArray()) {
              DInfoPTM pepInfoPtm = DInfoPTM.getInfoPTMMap().get(peptidePTM.getIdPtmSpecificity());
              deltaMass += pepInfoPtm.getMonoMass();
            }
            lazyData.setData(deltaMass);
        } else
            lazyData.setData(Double.NaN);
        return lazyData;
      }

 
      case COLTYPE_PEPTIDE_COUNT:
        return protPTMCluster.getPeptideCount();

      case COLTYPE_PTMSITE_COUNT:
        return protPTMCluster.getClusteredSites().size();
                
      case COLTYPE_SPECTRUM_TITLE: {
        if (peptideMatch == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        }  
        lazyData.setData(peptideMatch.getMsQuery().getDSpectrum().getTitle());
        return lazyData;        
      }
      
      case COLTYPE_EXPRESSION:
        return protPTMCluster.getExpressionValue();
        
      default: 
        // Quant Channel columns       
        if (protPTMCluster.getBestQuantPeptideMatch() == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        }  
        
        DMasterQuantPeptide masterQuantPep = protPTMCluster.getBestQuantPeptideMatch();
        int dynQchIndex = col - (COLTYPE_START_QUANT_INDEX); 
        int nbQc = dynQchIndex / m_columnNamesQC.length;
        int id = dynQchIndex - (nbQc * m_columnNamesQC.length);                        
        Map<Long, DQuantPeptide> quantPeptideByQchIds = masterQuantPep.getQuantPeptideByQchIds();   
        if (quantPeptideByQchIds == null) {
            switch (id) {
                case COLTYPE_RAW_ABUNDANCE:
                    lazyData.setData(Integer.valueOf(0));
                    break;
                case COLTYPE_ABUNDANCE:
                    lazyData.setData(Float.valueOf(0));
                    break;
            }
        } else {
            QuantChannelInfo qcinfo = (QuantChannelInfo)getSingleValue(QuantChannelInfo.class);
            DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qcinfo.getQuantChannels()[nbQc].getId());
            if (quantPeptide == null) {
                switch (id) {
                    case COLTYPE_RAW_ABUNDANCE:
                        lazyData.setData(Integer.valueOf(0));
                        break;
                    case COLTYPE_ABUNDANCE:
                        lazyData.setData(Float.valueOf(0));
                        break;
                }   
            } else {
                switch (id) {
                    case COLTYPE_ABUNDANCE:
                        lazyData.setData((quantPeptide.getAbundance() == null || quantPeptide.getAbundance().isNaN()) ? Float.valueOf(0) : quantPeptide.getAbundance());
                        break;
                    case COLTYPE_RAW_ABUNDANCE:
                        lazyData.setData((quantPeptide.getRawAbundance() == null || quantPeptide.getRawAbundance().isNaN()) ? Float.valueOf(0) : quantPeptide.getRawAbundance());
                        break;                                   
                }
            }
        }
       return lazyData;
    }

    
  }
  

  public void setData(Long taskId, ArrayList<PTMCluster> proteinPTMClusterArray) {
    m_ptmClusters = proteinPTMClusterArray;
    m_taskId = taskId;
    m_modificationInfo ="";
    QuantChannelInfo qcInfo = (QuantChannelInfo) getSingleValue(QuantChannelInfo.class);
    m_isQuantitationDS = qcInfo != null;
    if(qcInfo != null) {
        m_quantChannelNumber = qcInfo.getQuantChannels().length;            
        m_quantChannels = qcInfo.getQuantChannels(); 
    } else {
        //reset value
        m_quantChannelNumber = 0;
        m_quantChannels = null;
    }
    fireTableStructureChanged();
  }

  public String getModificationsInfo() {
    return m_modificationInfo;
  }

  public void dataUpdated() {
        //Verify if strructure changed
        QuantChannelInfo qcInfo = (QuantChannelInfo) getSingleValue(QuantChannelInfo.class);
        boolean isQuandDS = qcInfo != null;        
        boolean structChanged = false;
        if((isQuandDS && m_isQuantitationDS) || (!isQuandDS && !m_isQuantitationDS)){
            if(m_quantChannelNumber != qcInfo.getQuantChannels().length ){                
                m_quantChannelNumber = qcInfo.getQuantChannels().length; 
                m_quantChannels = qcInfo.getQuantChannels();
                structChanged = true;
            }
                
        } else {
            m_isQuantitationDS = isQuandDS;
            m_quantChannelNumber = qcInfo.getQuantChannels().length;            
            m_quantChannels = qcInfo.getQuantChannels(); 
            structChanged = true;
        }
        
    fireTableDataChanged();
    if(structChanged)
        fireTableStructureChanged();

  }

  public PTMCluster getProteinPTMCluster(int i) {
    return m_ptmClusters.get(i);
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
      switch(columnIndex){
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
          default: 
            return getColumnClass(columnIndex);
      }    
  }

  @Override
  public Object getDataValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == COLTYPE_PEPTIDE_NAME) {
      return ((DPeptideMatch)((LazyData) getValueAt(rowIndex, columnIndex)).getData()).getPeptide().getSequence();
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

//    ConvertValueInterface modificationConverter = new ConvertValueInterface() {
//      @Override
//      public Object convertValue(Object o) {
//        if (o == null) {
//          return null;
//        }
//
//        return m_modificationsMap.get((String) o);
//
//      }
//
//    };

//    filtersMap.put(COLTYPE_MODIFICATION, new ValueListAssociatedStringFilter(getColumnName(COLTYPE_MODIFICATION), m_modificationsArray.toArray(new String[m_modificationsArray.size()]), modificationConverter, COLTYPE_MODIFICATION, COLTYPE_RESIDUE_AA));
//
//    ConvertValueInterface residueConverter = new ConvertValueInterface() {
//      @Override
//      public Object convertValue(Object o) {
//        if (o == null) {
//          return null;
//        }
//
//        return m_residuesMap.get((Character) o);
//
//      }
//
//    };
//    filtersMap.put(COLTYPE_RESIDUE_AA, new ValueFilter(getColumnName(COLTYPE_RESIDUE_AA), m_residuesArray.toArray(new Character[m_residuesArray.size()]), null, ValueFilter.ValueFilterType.EQUAL, residueConverter, COLTYPE_RESIDUE_AA));
//    filtersMap.put(COLTYPE_DELTA_MASS_MODIFICATION, new DoubleFilter(getColumnName(COLTYPE_DELTA_MASS_MODIFICATION), null, COLTYPE_DELTA_MASS_MODIFICATION));
//    filtersMap.put(COLTYPE_MODIFICATION_LOC, new StringFilter(getColumnName(COLTYPE_MODIFICATION_LOC), null, COLTYPE_MODIFICATION_LOC));
//    filtersMap.put(COLTYPE_PROTEIN_LOC, new IntegerFilter(getColumnName(COLTYPE_PROTEIN_LOC), null, COLTYPE_PROTEIN_LOC));
//    filtersMap.put(COLTYPE_PROTEIN_NTERM_CTERM, new StringFilter(getColumnName(COLTYPE_PROTEIN_NTERM_CTERM), null, COLTYPE_PROTEIN_NTERM_CTERM));
    filtersMap.put(COLTYPE_PEPTIDE_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_COUNT), null, COLTYPE_PEPTIDE_COUNT));
    filtersMap.put(COLTYPE_PTMSITE_COUNT, new IntegerFilter(getColumnName(COLTYPE_PTMSITE_COUNT), null, COLTYPE_PTMSITE_COUNT));
//    filtersMap.put(COLTYPE_MODIFICATION_PROBA, new DoubleFilter(getColumnName(COLTYPE_MODIFICATION_PROBA), null, COLTYPE_MODIFICATION_PROBA));
    filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));

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
      PTMCluster proteinPTMCluster = m_ptmClusters.get(row);
      DPeptideMatch peptideMatch = proteinPTMCluster.getBestPeptideMatch();
      return ExportModelUtilities.getExportFonts(peptideMatch);
    }
    return null;
  }

  @Override
  public String getExportColumnName(int col) {
    if (col <= LAST_STATIC_COLUMN+1) {
        return getColumnName(col);
    } else if (m_isQuantitationDS) {
       int nbQc = (col - COLTYPE_START_QUANT_INDEX) / m_columnNamesQC.length;
            int id = col - COLTYPE_START_QUANT_INDEX - (nbQc * m_columnNamesQC.length);

            StringBuilder sb = new StringBuilder();
            sb.append(m_columnNamesQC[id]);
            sb.append(" ");
            sb.append(m_quantChannels[nbQc].getName());

            return sb.toString();
    } else {
        return ""; // should not happen
    }
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
      case COLTYPE_SPECTRUM_TITLE: {
        renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
        break;
      }
      case COLTYPE_PTM_PROBA: {
        renderer = new PercentageRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
        break;
      }
      case COLTYPE_DELTA_MASS_PTM:
      case COLTYPE_EXPRESSION: {
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
    if (c.equals(PTMCluster.class)) {
      return m_ptmClusters.get(row);
    }
    if (c.equals(DProteinMatch.class)) {
      return m_ptmClusters.get(row).getProteinMatch();
    }
    if (c.equals(DPeptideMatch.class)) {
      return ((PTMCluster) m_ptmClusters.get(row)).getBestPeptideMatch();
    }
    return null;
  }

  @Override
  public Object getColValue(Class c, int col) {
    return null;
  }

}
