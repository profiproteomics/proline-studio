/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.*;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.data.SelectLevelEnum;
import fr.proline.studio.dam.tasks.DatabaseDatasetPTMsTask;
import fr.proline.studio.dam.tasks.data.ptm.AggregatedMasterQuantPeptide;
import fr.proline.studio.dam.tasks.data.ptm.ComparableList;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.*;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.table.*;
import fr.proline.studio.table.renderer.*;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author JM235353
 */
public class PTMClusterTableModel extends LazyTableModel implements GlobalTableModelInterface {

  private final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ptm");

  public static final int COLTYPE_PTM_CLUSTER_ID = 0;
  public static final int COLTYPE_PTM_CLUSTER_SELECTION_LEVEL = 1;
  public static final int COLTYPE_PROTEIN_ID = 2;
  public static final int COLTYPE_PROTEIN_NAME = 3;
  public static final int COLTYPE_PEPTIDE_NAME = 4;
  public static final int COLTYPE_CLUSTER_NOTATION= 5;
  public static final int COLTYPE_CLUSTER_INFO = 6;
  public static final int COLTYPE_PEPTIDE_PTM = 7;
  public static final int COLTYPE_PTM_PROBA = 8;


  public static final int COLTYPE_PEPTIDE_SCORE = 9;
  public static final int COLTYPE_PEPTIDE_COUNT = 10;
  public static final int COLTYPE_PTMSITE_COUNT = 11;
  public static final int COLTYPE_PTMSITE_POSITIONS = 12;
  public static final int COLTYPE_PTMSITE_PEP_POSITIONS = 13;
  public static final int COLTYPE_PTM_CLUSTER_CONFIDENCE = 14;
  public static final int COLTYPE_PTMSITE_CONFIDENCES = 15;

  public static final int COLTYPE_DELTA_MASS_PTM = 16;
  public static final int COLTYPE_SPECTRUM_TITLE = 17;

  public static final int LAST_STATIC_COLUMN = COLTYPE_SPECTRUM_TITLE;
  
  private static final String[] m_columnNames = {"Id", "Status", "Protein Id", "Protein", "Peptide", "Status confidence","Status description" ,"PTMs", "PTMs Confid.(MDScore, %)", "Score", "Peptide count",  "Site count", "Sites Loc.","Sites Modif. on peptide", "Confidence" ,"Sites Confid.(%)", "PTM D.Mass", "Spectrum title"};
  private static final String[] m_columnTooltips = {"PTM cluster Id", "Cluster Status: Validated or Invalidated ", "Protein match Id", "Protein", "Peptide","Value illustrating the confidence in the Cluster Status", "Comment to outline Cluster Status", "Peptide's PTMs", "PTMs localisation confidence (%)", "Score of the peptide match", "Number of peptides matching the modification site", "Number of modification sites grouped into the cluster", "Sites localisation on the protein \nMay be greater than site count in case of merged clusters. ", "Sites modification and localisation on the representative peptide", "Sites combined confidence", "Sites localisation confidence (%) \nMay be greater than site count in case of merged clusters. ", "PTMs delta mass", "Peptide match spectrum title"};
     
  //Dynamique columns

  //One per QuantChannel 
  public static final int COLTYPE_START_QUANT_INDEX = LAST_STATIC_COLUMN+1;
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
  private final RendererMouseCallback m_selectLevelRendererCallback; // To be called by renderer  when mouse action

  public PTMClusterTableModel(LazyTable table, RendererMouseCallback callback) {
    super(table);
    m_selectLevelRendererCallback = callback;
  }

  @Override
  public int getColumnCount() {
    // remove last column for identification datasets
    int nbrCol = m_columnNames.length;
    if(m_isQuantitationDS){
       nbrCol= nbrCol + m_quantChannelNumber * m_columnNamesQC.length;
    }
    return nbrCol;
  }

  @Override
  public String getColumnName(int col) {
    if (col <= LAST_STATIC_COLUMN) {
      return m_columnNames[col];
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

  @Override
    public String getToolTipForHeader(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnTooltips[col];
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

  @Override
  public String getTootlTipValue(int row, int col) {
    return null;
  }
  
  
  public List<Integer> getDefaultColumnsToHide() {  
    List<Integer> listIds = new ArrayList<>();
    listIds.add(COLTYPE_PROTEIN_ID);
    listIds.add(COLTYPE_PTM_CLUSTER_CONFIDENCE);
    listIds.add(COLTYPE_CLUSTER_NOTATION);
    listIds.add(COLTYPE_CLUSTER_INFO);
    listIds.add(COLTYPE_PTMSITE_PEP_POSITIONS);
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
      case COLTYPE_PTM_CLUSTER_SELECTION_LEVEL:
        return SelectLevelEnum.class;
      case COLTYPE_PTM_CLUSTER_ID:
      case COLTYPE_PROTEIN_ID:
        return Long.class;      
      case COLTYPE_PROTEIN_NAME:
      case COLTYPE_CLUSTER_INFO:
        return String.class;
      case COLTYPE_PTM_CLUSTER_CONFIDENCE:
        return Float.class;
      case COLTYPE_PEPTIDE_COUNT:
      case COLTYPE_CLUSTER_NOTATION:
        return Integer.class;
      case COLTYPE_PTMSITE_POSITIONS:
      case COLTYPE_PTMSITE_CONFIDENCES:
      case COLTYPE_PTMSITE_PEP_POSITIONS:
        return ComparableList.class;
      case COLTYPE_PTMSITE_COUNT:
      case COLTYPE_PEPTIDE_NAME:
      case COLTYPE_PEPTIDE_PTM:
      case COLTYPE_SPECTRUM_TITLE:
      case COLTYPE_PTM_PROBA:  
      case COLTYPE_DELTA_MASS_PTM:
      case COLTYPE_PEPTIDE_SCORE:
        return LazyData.class;     
    default:
        return LazyData.class;
    }

  }

  @Override
  public int getSubTaskId(int col) {
    switch (col) {
      case COLTYPE_PTMSITE_COUNT:
      case COLTYPE_PEPTIDE_NAME:
      case COLTYPE_PEPTIDE_PTM:
      case COLTYPE_SPECTRUM_TITLE:
      case COLTYPE_PTM_PROBA:    
      case COLTYPE_DELTA_MASS_PTM:
      case COLTYPE_PEPTIDE_SCORE:          
        return DatabaseDatasetPTMsTask.SUB_TASK_PTMCLUSTER_PEPTIDES;
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

    PTMCluster ptmCluster = m_ptmClusters.get(row);
    DProteinMatch proteinMatch = ptmCluster.getProteinMatch();
    //DInfoPTM infoPtm = ptmCluster.getPTMSpecificity();
    DPeptideMatch peptideMatch = ptmCluster.getRepresentativePepMatch();
    LazyData lazyData = getLazyData(row,col);

    switch (col) {
      case COLTYPE_PTM_CLUSTER_ID:
        return ptmCluster.getId();

      case COLTYPE_PTM_CLUSTER_SELECTION_LEVEL:
        SelectLevelEnum level = SelectLevelEnum.valueOf(ptmCluster.getSelectionLevel());
        if (level == null) {
            level = SelectLevelEnum.SELECTED_AUTO;
        }
        return  level;

      case COLTYPE_CLUSTER_INFO:
        return  ptmCluster.getSelectionInfo();

      case COLTYPE_CLUSTER_NOTATION:
        return ptmCluster.getSelectionNotation();

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

      case COLTYPE_PTMSITE_POSITIONS:
        return ptmCluster.getPositionsOnProtein();

      case COLTYPE_PTMSITE_PEP_POSITIONS:
        return  ptmCluster.getPTMSites().stream().map(s -> s.peptideSpecificReadablePtmString(peptideMatch.getPeptide().getId())).collect(ComparableList::new, ComparableList::add, ComparableList::addAll);

      case COLTYPE_PTMSITE_CONFIDENCES:
        return ptmCluster.getSiteConfidences();

      case COLTYPE_PTM_CLUSTER_CONFIDENCE:
        return ptmCluster.getLocalizationConfidence()*100;

      case COLTYPE_PEPTIDE_COUNT:
        return ptmCluster.getPeptideCount();

      case COLTYPE_PTMSITE_COUNT:
        Integer siteSize = ptmCluster.getPTMSitesCount();
        if (siteSize < 0) {
          lazyData.setData(null);
          givePriorityTo(m_taskId, row, col);
          return lazyData;
        }
        lazyData.setData(siteSize);
        return lazyData;

      case COLTYPE_SPECTRUM_TITLE: {
        if (peptideMatch == null) {            
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        }  
        lazyData.setData(peptideMatch.getMsQuery().getDSpectrum().getTitle());
        return lazyData;        
      }

      //VDS DEV
//      case COLTYPE_QUANT_SELLEVEL: {
//        AggregatedMasterQuantPeptide masterQuantPep = ptmCluster.getRepresentativeMQPepMatch();
//        if(masterQuantPep != null)
//          return masterQuantPep.getGenericSelectionLevel();
//        else
//          return -5;
//      }

      default: 
        // Quant Channel columns       
        if (ptmCluster.getRepresentativeMQPepMatch() == null) {
            lazyData.setData(null);
            givePriorityTo(m_taskId, row, col);
            return lazyData;
        }  
        
        AggregatedMasterQuantPeptide masterQuantPep = ptmCluster.getRepresentativeMQPepMatch();
        int dynQchIndex = col - (COLTYPE_START_QUANT_INDEX); 
        int nbQc = dynQchIndex / m_columnNamesQC.length;
        int id = dynQchIndex - (nbQc * m_columnNamesQC.length);                        
        Map<Long, DQuantPeptide> quantPeptideByQchIds = masterQuantPep.getQuantPeptideByQchIds();   
        if (quantPeptideByQchIds == null) {
            switch (id) {
                case COLTYPE_RAW_ABUNDANCE, COLTYPE_ABUNDANCE:
                    lazyData.setData(0f);
                    break;
            }
        } else {
            QuantChannelInfo qcinfo = (QuantChannelInfo)getSingleValue(QuantChannelInfo.class);
            DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qcinfo.getQuantChannels()[nbQc].getId());
            if (quantPeptide == null) {
                switch (id) {
                    case COLTYPE_RAW_ABUNDANCE, COLTYPE_ABUNDANCE:
                        lazyData.setData(0f);
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

    if (m_ptmClusters != null) {
        m_ptmClusters.sort((o1, o2) -> {
            if(o1 == null)
                return -1;
            if(o2 == null)
                return 1;
            return o1.getId().compareTo(o2.getId());
        });
      Map<Boolean, List<PTMCluster>> clustersBySitesCount = m_ptmClusters.stream().collect(Collectors.partitioningBy(c -> c.getPTMSites().size() > 1));
      StringBuilder sb = new StringBuilder();
      Map<String, Long> countByPtms = clustersBySitesCount.get(Boolean.FALSE).stream().collect(Collectors.groupingBy(c -> c.getPTMSites().get(0).getPTMSpecificity().getPtmShortName(), Collectors.counting()));
      Iterator<String> it = countByPtms.keySet().iterator();
      while (it.hasNext()) {
        String modification = it.next();
        Long nbModifications = countByPtms.get(modification);
        sb.append(modification).append(":").append(nbModifications);
        if (it.hasNext()) {
          sb.append("   ");
        }
      }
      List<PTMCluster> multiplePtmsClusters = clustersBySitesCount.get(Boolean.TRUE);
      if (multiplePtmsClusters != null && !multiplePtmsClusters.isEmpty()) {
        sb.append("   Multiple Sites:").append(multiplePtmsClusters.size());
      }

      m_modificationInfo = sb.toString();

      QuantChannelInfo qcInfo = (QuantChannelInfo) getSingleValue(QuantChannelInfo.class);
      m_isQuantitationDS = qcInfo != null;
      if (qcInfo != null) {
        m_quantChannelNumber = qcInfo.getQuantChannels().length;
        m_quantChannels = qcInfo.getQuantChannels();
      } else {
        //reset value
        m_quantChannelNumber = 0;
        m_quantChannels = null;
      }
    }
    fireTableStructureChanged();
  }

  public String getModificationsInfo() {
    return m_modificationInfo;
  }

  public void dataUpdated() {
        //Verify if structure has changed
        QuantChannelInfo qcInfo = (QuantChannelInfo) getSingleValue(QuantChannelInfo.class);
        boolean isQuandDS = qcInfo != null;        
        boolean structChanged = false;
        if((isQuandDS && m_isQuantitationDS)){
            if(m_quantChannelNumber != qcInfo.getQuantChannels().length ){                
                m_quantChannelNumber = qcInfo.getQuantChannels().length; 
                m_quantChannels = qcInfo.getQuantChannels();
                structChanged = true;
            }
                
        } else if (isQuandDS) {
            m_isQuantitationDS = true;
            m_quantChannelNumber = qcInfo.getQuantChannels().length;            
            m_quantChannels = qcInfo.getQuantChannels(); 
            structChanged = true;
        }
        
    
    if (structChanged) {
        fireTableStructureChanged();
    } else {
        fireTableDataChanged();
    }
  }

  public boolean isRowEditable(int rowIndex) {
    return true;
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
          case COLTYPE_PTMSITE_COUNT:
            return  Integer.class;
          default:
            if(columnIndex <= LAST_STATIC_COLUMN)
              return getColumnClass(columnIndex);
            return Float.class; //Quanti specific col : (raw)abundances
      }    
  }

  @Override
  public Object getDataValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == COLTYPE_PEPTIDE_NAME) {
      return ((DPeptideMatch)((LazyData) getValueAt(rowIndex, columnIndex)).getData()).getPeptide().getSequence();
    } else if (columnIndex == COLTYPE_PTM_CLUSTER_SELECTION_LEVEL){
      return ((SelectLevelEnum)getValueAt(rowIndex, columnIndex)).getIntValue();
    }

    return getValueAt(rowIndex, columnIndex);
  }

  @Override
  public int[] getKeysColumn() {
    return new int[]{COLTYPE_PROTEIN_NAME};
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

    filtersMap.put(COLTYPE_PTM_CLUSTER_ID, new LongFilter(getColumnName(COLTYPE_PTM_CLUSTER_ID), null, COLTYPE_PTM_CLUSTER_ID));
    filtersMap.put(COLTYPE_PROTEIN_NAME, new StringDiffFilter(getColumnName(COLTYPE_PROTEIN_NAME), null, COLTYPE_PROTEIN_NAME));

    ConvertValueInterface peptideConverter = o -> {
      if (o == null) {
        return null;
      }
      return ((DPeptideMatch) o).getPeptide().getSequence();
    };
    filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter, COLTYPE_PEPTIDE_NAME));
    filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));
    filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
    filtersMap.put(COLTYPE_DELTA_MASS_PTM, new DoubleFilter(getColumnName(COLTYPE_DELTA_MASS_PTM), null, COLTYPE_DELTA_MASS_PTM));
    filtersMap.put(COLTYPE_PTM_PROBA, new DoubleFilter(getColumnName(COLTYPE_PTM_PROBA), null, COLTYPE_PTM_PROBA));
    filtersMap.put(COLTYPE_PTM_CLUSTER_CONFIDENCE, new DoubleFilter(getColumnName(COLTYPE_PTM_CLUSTER_CONFIDENCE), null, COLTYPE_PTM_CLUSTER_CONFIDENCE));

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
//    filtersMap.put(COLTYPE_MODIFICATION_LOC, new StringDiffFilter(getColumnName(COLTYPE_MODIFICATION_LOC), null, COLTYPE_MODIFICATION_LOC));
//    filtersMap.put(COLTYPE_PROTEIN_LOC, new IntegerFilter(getColumnName(COLTYPE_PROTEIN_LOC), null, COLTYPE_PROTEIN_LOC));
//    filtersMap.put(COLTYPE_PROTEIN_NTERM_CTERM, new StringDiffFilter(getColumnName(COLTYPE_PROTEIN_NTERM_CTERM), null, COLTYPE_PROTEIN_NTERM_CTERM));
    filtersMap.put(COLTYPE_PEPTIDE_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_COUNT), null, COLTYPE_PEPTIDE_COUNT));
    filtersMap.put(COLTYPE_PTMSITE_COUNT, new IntegerFilter(getColumnName(COLTYPE_PTMSITE_COUNT), null, COLTYPE_PTMSITE_COUNT));
//    filtersMap.put(COLTYPE_MODIFICATION_PROBA, new DoubleFilter(getColumnName(COLTYPE_MODIFICATION_PROBA), null, COLTYPE_MODIFICATION_PROBA));
    filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringDiffFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));
//    filtersMap.put(COLTYPE_ABUNDANCE, new StringDiffFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));

    int nbCol = getColumnCount();
    for (int i = LAST_STATIC_COLUMN + 1; i < nbCol; i++) {
      int dynQchIndex = i - (COLTYPE_START_QUANT_INDEX);
      int nbQc = dynQchIndex / m_columnNamesQC.length;
      int id = dynQchIndex - (nbQc * m_columnNamesQC.length);

      switch (id) {
        case COLTYPE_ABUNDANCE:
          filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
          break;
        case COLTYPE_RAW_ABUNDANCE:
          filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
          break;
      }
    }
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
      DPeptideMatch peptideMatch = proteinPTMCluster.getRepresentativePepMatch();
      return ExportFontModelUtilities.getExportFonts(peptideMatch);
    }
    return null;
  }

  @Override
  public String getExportColumnName(int col) {
    if (col <= LAST_STATIC_COLUMN + 1) {
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
        renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.LEFT);
        break;
      }
      case COLTYPE_PTM_CLUSTER_CONFIDENCE:
      case COLTYPE_PTM_PROBA: {
        renderer = new PercentageRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT));
        break;
      }
      case COLTYPE_DELTA_MASS_PTM: {
        renderer = new DoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 4);
        break;
      }
      case COLTYPE_PEPTIDE_SCORE: {
        renderer = m_scoreRenderer;
        break;
      }
      case COLTYPE_PTMSITE_CONFIDENCES: {
        renderer = new CollectionRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 2);
        break;
      }
      case COLTYPE_PTMSITE_PEP_POSITIONS:
      case COLTYPE_PTMSITE_POSITIONS: {
        renderer = new CollectionRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT));
        break;
      }

      case COLTYPE_PTM_CLUSTER_SELECTION_LEVEL: {
          renderer = new SelectLevelRenderer(m_selectLevelRendererCallback, COLTYPE_PTM_CLUSTER_SELECTION_LEVEL);
      }
      case COLTYPE_PROTEIN_ID :
      case COLTYPE_PTM_CLUSTER_ID:
      case COLTYPE_PEPTIDE_COUNT:
      case COLTYPE_PTMSITE_COUNT:
      case COLTYPE_CLUSTER_INFO:
      case COLTYPE_CLUSTER_NOTATION:
        //Return explicitly null to don't go in default case
        break;

      default: {
        if (m_isQuantitationDS) {
          int nbQc = (col - COLTYPE_START_QUANT_INDEX) / m_columnNamesQC.length;
          int id = col - COLTYPE_START_QUANT_INDEX - (nbQc * m_columnNamesQC.length);
          switch (id) {
            case COLTYPE_ABUNDANCE:
            case COLTYPE_RAW_ABUNDANCE: {
              renderer = new BigFloatOrDoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0);
              break;
            }
          }
        }
      }
    }
    m_rendererMap.put(col, renderer);
    return renderer;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == COLTYPE_PTM_CLUSTER_SELECTION_LEVEL;
  }

    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap<>();

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
      return m_ptmClusters.get(row).getRepresentativePepMatch();
    }
    return null;
  }

  @Override
  public Object getColValue(Class c, int col) {
    return null;
  }

  public int getModelIndexFor(PTMCluster cluster){
    return m_ptmClusters.indexOf(cluster);
  }

}
