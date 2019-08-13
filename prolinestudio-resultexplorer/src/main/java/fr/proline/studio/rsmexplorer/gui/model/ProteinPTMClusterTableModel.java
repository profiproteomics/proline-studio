package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
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
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

//  public static final int COLTYPE_MODIFICATION = 4;
//  public static final int COLTYPE_RESIDUE_AA = 5;
//  public static final int COLTYPE_MODIFICATION_PROBA = 6;
//  public static final int COLTYPE_DELTA_MASS_MODIFICATION = 7;
//  public static final int COLTYPE_MODIFICATION_LOC = 8;
//  public static final int COLTYPE_PROTEIN_LOC = 9;
//  public static final int COLTYPE_PROTEIN_NTERM_CTERM = 10;
  public static final int COLTYPE_PEPTIDE_COUNT = 4;
  public static final int COLTYPE_PTMSITE_COUNT = 5;
  

  public static final int COLTYPE_PEPTIDE_PTM = 6;
  public static final int COLTYPE_DELTA_MASS_PTM = 7;
  public static final int COLTYPE_PTM_PROBA = 8;

  public static final int COLTYPE_SPECTRUM_TITLE = 9;
  public static final int COLTYPE_EXPRESSION = 10;
  public static final int COLTYPE_HIDDEN_PROTEIN_PTM = 11; // hidden column, must be the last

  private static final String[] m_columnNames = {"Id", "Protein", "Peptide", "Score", "Peptide count",  "Modif. Site count", "PTMs", "PTM D.Mass", "PTM Probability", "Spectrum title", "FC dist."};
  private static final String[] m_columnTooltips = {"Protein Id", "Protein", "Peptide", "Score of the peptide match", "Number of peptides matching the modification site", "Number of Modification sites clustered", "Post Translational Modifications of this peptide", "PTMs delta mass", "PTMs probability", "Peptide match spectrum title", "Fold change distance"};

  private ArrayList<PTMCluster> m_ptmClusters = null;

  private final ArrayList<String> m_modificationsArray = new ArrayList<>();
  private final HashMap<String, Integer> m_modificationsMap = new HashMap<>();

  private final ArrayList<Character> m_residuesArray = new ArrayList<>();
  private final HashMap<Character, Integer> m_residuesMap = new HashMap<>();

  private String m_modelName;
  private PTMDataset m_ptmDataset = null;
  private String m_modificationInfo = "";

  private final boolean m_hideRedundantPeptides = false;

  private final ScoreRenderer m_scoreRenderer = new ScoreRenderer();

  public ProteinPTMClusterTableModel(LazyTable table) {
    super(table);
  }

  @Override
  public int getColumnCount() {
    // remove last column for identification datasets
    return m_columnNames.length - ((m_ptmDataset != null) && (m_ptmDataset.isIdentification()) ? 1 : 0);
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
      case COLTYPE_PROTEIN_NAME:
        return String.class;
      case COLTYPE_PEPTIDE_COUNT: 
      case COLTYPE_PTMSITE_COUNT:
        return Integer.class;      
      case COLTYPE_EXPRESSION:        
        return Double.class;      
      case COLTYPE_PEPTIDE_NAME:
      case COLTYPE_PEPTIDE_PTM:          
      case COLTYPE_SPECTRUM_TITLE:     
      case COLTYPE_PTM_PROBA:  
      case COLTYPE_DELTA_MASS_PTM:
      case COLTYPE_PEPTIDE_SCORE:        
        return LazyData.class;        
    }

    return null;
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

//      case COLTYPE_MODIFICATION: {
//        return infoPtm.getPtmShortName();
//      }
//      
//      case COLTYPE_MODIFICATION_LOC: {
//        if (peptideMatch == null) {            
//            lazyData.setData(null);
//            givePriorityTo(m_taskId, row, col);
//            return lazyData;
//        } 
//        String locationSpecitifcity = infoPtm.getLocationSpecificity();
//        if (locationSpecitifcity.contains("N-term")) {
//          return "N-term";
//        } else if (locationSpecitifcity.contains("C-term")) {
//          return "C-term";
//        }
//        lazyData.setData(String.valueOf((int) protPTMCluster.getPositionOnPeptide(peptideMatch.getPeptide().getId())));
//        return lazyData;
//      }
//      
//      case COLTYPE_PROTEIN_LOC: {
//        return protPTMCluster.getPositionOnProtein();
//      }
//      
//      case COLTYPE_PROTEIN_NTERM_CTERM: {
//        String locationSpecitifcity = infoPtm.getLocationSpecificity();
//        if (locationSpecitifcity.contains("-term")) {
//          return locationSpecitifcity;
//        }
//        return "";
//      }
      
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
      
//      case COLTYPE_MODIFICATION_PROBA: {
//        if (peptideMatch == null) {            
//            lazyData.setData(null);
//            givePriorityTo(m_taskId, row, col);
//            return lazyData;
//        }           
//        DPtmSiteProperties properties = peptideMatch.getPtmSiteProperties();
//        Float proba = Float.NaN;
//        if (properties != null) {
//          // VDS Workaround test for issue #16643   
//          proba = properties.getMascotProbabilityBySite().get(protPTMCluster.toReadablePtmString(peptideMatch.getPeptide().getId()));
//          if (proba == null) {
//            proba = (properties.getMascotProbabilityBySite().get(protPTMCluster.toOtherReadablePtmString(peptideMatch.getPeptide().getId()))) * 100;
//          }
//          //END VDS Workaround
//        }
//        lazyData.setData(proba);
//        return lazyData;        
//      }
      
      case COLTYPE_DELTA_MASS_PTM: {
//                return 0.0;                
        //return proteinPTMSite.getDeltaMassPTM();
        //Previously (Version 1) was : 
//                double deltaMass = 0;                
//                for (DPeptidePTM peptidePTM : peptidePTMArray) {
//                    DInfoPTM infoPtm = DInfoPTM.getInfoPTMMap().get(peptidePTM.getIdPtmSpecificity());
//                    deltaMass += infoPtm.getMonoMass();
//                }
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

//      case COLTYPE_DELTA_MASS_MODIFICATION:
//        return infoPtm.getMonoMass();
//        
      case COLTYPE_PEPTIDE_COUNT:
        return protPTMCluster.getPeptideCount();

      case COLTYPE_PTMSITE_COUNT:
        return protPTMCluster.getClusteredSites().size();
                
//      case COLTYPE_RESIDUE_AA: 
//        return infoPtm.getResidueAASpecificity();
//        
      case COLTYPE_HIDDEN_PROTEIN_PTM:
        return protPTMCluster;
        
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
    }

    return null; // should never happen
  }
  

  public void setData(Long taskId, ArrayList<PTMCluster> proteinPTMClusterArray) {
    m_ptmClusters = proteinPTMClusterArray;
    m_taskId = taskId;
    if(m_ptmClusters != null && !m_ptmClusters.isEmpty()){
        m_ptmDataset = m_ptmClusters.get(0).getPTMDataset();
    } 
    
    m_modificationInfo ="";

    fireTableDataChanged();
  }

  public String getModificationsInfo() {
    return m_modificationInfo;
  }

  public void dataUpdated() {
    
    fireTableDataChanged();

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
