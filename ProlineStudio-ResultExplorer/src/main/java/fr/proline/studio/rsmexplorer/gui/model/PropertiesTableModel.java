package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.Enzyme;
import fr.proline.core.orm.msi.InstrumentConfig;
import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.MsmsSearch;
import fr.proline.core.orm.msi.Peaklist;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.SearchSetting;
import fr.proline.core.orm.msi.PeaklistSoftware;
import fr.proline.core.orm.msi.UsedPtm;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.utils.SerializedPropertiesUtil;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author JM235353
 */
public class PropertiesTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_GROUP_NAME = 0;
    public static final int COLTYPE_PROPERTY_NAME = 1;
    
    private static final String[] m_columnNames = {"Group", "Type"};
    
    
    private ArrayList<DDataset> m_datasetArrayList = null;
    
    private ArrayList<DataGroup> m_dataGroupList = null;
    private HashMap<Integer, DataGroup> m_dataGroupMap = null;
    private int m_rowCount = -1;
    
    private String m_modelName; 

    private final PropertiesRenderer m_propertiesOkRenderer = new PropertiesRenderer(false);
    private final PropertiesRenderer m_propertiesNonOkRenderer = new PropertiesRenderer(true);
    
    private JTable m_table = null;
    
    private StringBuilder m_sb = new StringBuilder();
    
    public PropertiesTableModel() {
    }
    
    public void setData(ArrayList<DDataset> datasetArrayList) {
        m_datasetArrayList = datasetArrayList;
        m_rowCount = -1; // will be recalculated later
         
        if (m_dataGroupList == null) {
            m_dataGroupList = new ArrayList<>();
            m_dataGroupMap = new HashMap<>();
            
            int startRow = 0;
            DataGroup group = new GeneralInformationGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            group = new SearchPropertiesGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            group = new SearchResultInformationGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            group = new IdentificationSummaryInformationGroup(startRow, datasetArrayList); m_dataGroupList.add(group); startRow+=group.getRowCount();
            group = new SqlIdGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();

        }
        
        fireTableStructureChanged();
    }
    
    @Override
    public String getColumnName(int col) {
        if (col<=COLTYPE_PROPERTY_NAME) {
            return m_columnNames[col];
        }
        return m_datasetArrayList.get(col-2).getName();
    }
    
    @Override
    public int getColumnCount() {
        if (m_datasetArrayList == null) {
            return 2;
        }
        return 2+m_datasetArrayList.size();
    }
    
    @Override
    public int getRowCount() {
        if (m_rowCount != -1) {
            return m_rowCount;
        }
        if (m_dataGroupList == null) {
            return 0;
        }
        
        // calculate row count
        m_rowCount = 0;
        
        for (DataGroup dataGroup : m_dataGroupList) {
            int start = m_rowCount;
            m_rowCount += dataGroup.getRowCount();
            for (int i=start;i<m_rowCount;i++) {
                m_dataGroupMap.put(i, dataGroup);
            }
        }
        
        return m_rowCount;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataGroup dataGroup = m_dataGroupMap.get(rowIndex);
        String v = (String) dataGroup.getValueAt(rowIndex, columnIndex);
        if (v == null) {
            v = "";
        }
        return v;
    }
    
    

    @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        
        DataGroup dataGroup = m_dataGroupMap.get(row);
        boolean isFirstRow = dataGroup.isFirstRow(row);
        if (isFirstRow) {
            return dataGroup.getRenderer(row);
        } else if (col == COLTYPE_GROUP_NAME) {
            return dataGroup.getRenderer(row);
        } else if (col == COLTYPE_PROPERTY_NAME) {
            int nbCol = getColumnCount();
            if (nbCol>COLTYPE_PROPERTY_NAME+2) {
                String value = (String) getValueAt(row, COLTYPE_PROPERTY_NAME+1);
                for (int i = COLTYPE_PROPERTY_NAME + 2; i < nbCol; i++) {
                    String valueCur = (String) getValueAt(row, i);
                    if (value.compareTo(valueCur) != 0) {
                        return m_propertiesNonOkRenderer;
                    }
                }
            }
        }
        
        return m_propertiesOkRenderer;

    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return -1l;
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null;
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {   
    }

    @Override
    public void sortingChanged(int col) {
        return; // not used
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
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
    }

    @Override
    public int[] getKeysColumn() {
        return null;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_GROUP_NAME;
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
        return null;
    }

    @Override
    public Object getValue(Class c, int row) {
        return null;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        filtersMap.put(COLTYPE_GROUP_NAME, new StringFilter(getColumnName(COLTYPE_GROUP_NAME), null, COLTYPE_GROUP_NAME));
        filtersMap.put(COLTYPE_PROPERTY_NAME, new StringFilter(getColumnName(COLTYPE_PROPERTY_NAME), null, COLTYPE_PROPERTY_NAME));
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getLoadingPercentage() {
        return 100;
    }

    @Override
    public PlotType getBestPlotType() {
        return null;
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
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }
    
    
    public abstract class DataGroup {

        private String m_name;
        protected int m_rowStart;
        
        private PropertiesRenderer m_groupRenderer = new PropertiesRenderer(false);
        private PropertiesRenderer m_groupSubRenderer = new PropertiesRenderer(false);
        
        public DataGroup(String name, int rowStart) {
            m_name = name;
            m_rowStart = rowStart;
        }

        private String getName(int row) {
            if (row == m_rowStart) {
                return m_name;
            }
            if ((m_table==null) || m_table.getRowSorter().getSortKeys().isEmpty()) {
                return ""; // no sorting, for group name, we show no text
            }
            return m_name;
        }
        
        public TableCellRenderer getRenderer(int row) {
            if (row == m_rowStart) {
                return m_groupRenderer;
            } else {
                return m_groupSubRenderer;
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == COLTYPE_GROUP_NAME) {
                return getName(rowIndex);
            }
            if (rowIndex == m_rowStart) {
                return "";
            }
            
            if (columnIndex == COLTYPE_PROPERTY_NAME) {
                return getGroupNameAt(rowIndex-m_rowStart-1);
            }
            
            return getGroupValueAt(rowIndex-m_rowStart-1, columnIndex-2);
        }
        
        public boolean isFirstRow(int row) {
            return row == m_rowStart;
        }

        
        public abstract String getGroupValueAt(int rowIndex, int columnIndex);
        public abstract String getGroupNameAt(int rowIndex);
        public abstract Color getGroupColor(int row);
        
        public int getRowCount() {
            return getRowCountImpl()+1;
        }
        
        public abstract int getRowCountImpl();
    }
    
     /**
     * GeneralInformationGroup
     */
    public class GeneralInformationGroup extends DataGroup {
        
        private static final int ROWTYPE_RAW_FILE_NAME = 0;
        private static final int ROWTYPE_SEARCH_RESULT_NAME = 1;
        private static final int ROWTYPE_INSTRUMENT_NAME = 2;
        private static final int ROWTYPE_TARGET_DECOY_MODE = 3;
        private static final int ROWTYPE_TARGET_DECOY_RULE = 4;
        private static final int ROWTYPE_PEAKLIST_SOFTWARE_NAME = 5;
        private static final int ROW_COUNT = 6; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(0,0,0);
        
        public GeneralInformationGroup(int rowStart) {
            super("General Information", rowStart);
        }
        
        @Override
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_RAW_FILE_NAME:
                    return "Raw File Name";
                case ROWTYPE_SEARCH_RESULT_NAME:
                    return "Search Result Name";
                case ROWTYPE_INSTRUMENT_NAME:
                    return "Instrument Name";
                case ROWTYPE_TARGET_DECOY_MODE:
                    return "Target Decoy Mode";
                case ROWTYPE_TARGET_DECOY_RULE:
                    return "Decoy Rule";
                case ROWTYPE_PEAKLIST_SOFTWARE_NAME:
                    return "Peaklist Software Name";
            }
            
            return null;
        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            ResultSet rset = dataset.getResultSet();
            ResultSet rsetDecoy = (rset==null) ? null : rset.getDecoyResultSet();
            
            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();
            InstrumentConfig instrumentConfig = (searchSetting==null) ? null : searchSetting.getInstrumentConfig();
            Peaklist peaklist = (msiSearch == null) ? null : msiSearch.getPeaklist();
            PeaklistSoftware peaklistSoftware = (peaklist == null) ? null : peaklist.getPeaklistSoftware();
            ResultSummary rsm = dataset.getResultSummary();


            switch (rowIndex) {
                case ROWTYPE_RAW_FILE_NAME:
                    if (peaklist==null) {
                        return "";
                    } else {
                        String path = peaklist.getPath();
                        File f = new File(path);
                        return f.getName();
                    }
                case ROWTYPE_SEARCH_RESULT_NAME:
                    return (rset==null) ? "" : rset.getName();
                case ROWTYPE_INSTRUMENT_NAME:
                    return (instrumentConfig == null) ? "" : instrumentConfig.getName();
                case ROWTYPE_TARGET_DECOY_MODE: {
                    if (rsetDecoy == null) {
                        return "";
                    }
                    try {
                        Map<String, Object> map = rsetDecoy.getSerializedPropertiesAsMap();
                        Object o = map.get("target_decoy_mode");
                        if (o == null) {
                            return "";
                        }
                        return o.toString();
                    } catch (Exception e) {
                        return "";
                    }
                }
                case ROWTYPE_TARGET_DECOY_RULE: {
                    return "NA";
                }
                case ROWTYPE_PEAKLIST_SOFTWARE_NAME:
                    return (peaklistSoftware==null) ? "" : peaklistSoftware.getName();
                    
            }
            
            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
    
    
    /**
     * SearchPropertiesGroup
     */
    public class SearchPropertiesGroup extends DataGroup {
        
        private static final int ROWTYPE_RESULT_FILE_NAME = 0;
        private static final int ROWTYPE_SEARCH_DATE = 1;
        private static final int ROWTYPE_SOFTWARE_NAME = 2;
        private static final int ROWTYPE_SOFTWARE_VERSION = 3;
        private static final int ROWTYPE_DATABASE_NAME = 4;
        private static final int ROWTYPE_TAXONOMY = 5;
        private static final int ROWTYPE_ENZYME = 6;
        private static final int ROWTYPE_MAX_MISSED_CLIVAGE = 7;
        private static final int ROWTYPE_FIXED_MODIFICATIONS = 8;
        private static final int ROWTYPE_VARIABLE_MODIFICATIONS = 9;
        private static final int ROWTYPE_PEPTIDE_CHARGE_STATES = 10;
        private static final int ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE = 11;
        private static final int ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT = 12;
        private static final int ROWTYPE_FRAGMENT_CHARGE_STATES = 13;
        private static final int ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE = 14;
        private static final int ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE_UNIT = 15;
        
        private static final int ROW_COUNT = 13; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(254,163,71);
        
        public SearchPropertiesGroup(int rowStart) {
            super("Search Properties", rowStart);
        }
        
        @Override
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_RESULT_FILE_NAME:
                    return "Result File Name";
                case ROWTYPE_SEARCH_DATE:
                    return "Search Date";
                case ROWTYPE_SOFTWARE_NAME:
                    return "Software Name";
                case ROWTYPE_SOFTWARE_VERSION:
                    return "Software Version";
                case ROWTYPE_DATABASE_NAME:
                    return "Database Name";
                case ROWTYPE_TAXONOMY:
                    return "Taxonomy";
                case ROWTYPE_ENZYME:
                    return "Enzyme";
                case ROWTYPE_MAX_MISSED_CLIVAGE:
                    return "Max Missed Clivage";
                case ROWTYPE_FIXED_MODIFICATIONS:
                    return "Fixed Modifications";
                case ROWTYPE_VARIABLE_MODIFICATIONS:
                    return "Variable Modifications";
                case ROWTYPE_PEPTIDE_CHARGE_STATES:
                    return "Peptide Charge States";
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE:
                    return "Peptide Mass Error Tolerance";
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT:
                    return "Peptide Mass Error Tolerance Unit";
                case ROWTYPE_FRAGMENT_CHARGE_STATES:
                    return "Fragment Charge States";
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE:
                    return "Fragment Mass Error Tolerance";
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE_UNIT:
                    return "Fragment Mass Error Tolerance Unit";
            }
            
            return null;
        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            ResultSet rset = dataset.getResultSet();
            
            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();
            MsmsSearch msmsSearch =  null;
            if (searchSetting instanceof MsmsSearch) {
                msmsSearch = (MsmsSearch) searchSetting;
            }
            ResultSummary rsm = dataset.getResultSummary();
            if (rsm == null) {
                return "";
            }

            switch (rowIndex) {
                case ROWTYPE_RESULT_FILE_NAME: {
                    return (msiSearch==null) ? "" : msiSearch.getResultFileName();
                }
                case ROWTYPE_SEARCH_DATE: {
                    if (msiSearch==null) {
                        return "";
                    }
                    Timestamp timeStamp = msiSearch.getDate();
                    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
                    final String dateFormatted = df.format(timeStamp);
                    return dateFormatted;
                }
                case ROWTYPE_SOFTWARE_NAME: {
                    return (searchSetting==null) ? "" : searchSetting.getSoftwareName();
                }
                case ROWTYPE_SOFTWARE_VERSION: {
                    return (searchSetting==null) ? "" : searchSetting.getSoftwareVersion();
                }
                case ROWTYPE_DATABASE_NAME: {
                    return "NA";
                }
                case ROWTYPE_TAXONOMY: {
                    return (searchSetting == null) ? "" : searchSetting.getTaxonomy();
                }
                case ROWTYPE_ENZYME: {
                    String enzyme = "";
                    if (searchSetting != null) {
                        Set<Enzyme> enzymeSet = searchSetting.getEnzymes();
                        Iterator<Enzyme> it = enzymeSet.iterator();
                        while (it.hasNext()) {
                            if (!enzyme.isEmpty()) {
                                enzyme +="";
                            }
                            enzyme += ((Enzyme)it.next()).getName();
                        }
                    }
                    return enzyme;
                }
                case ROWTYPE_MAX_MISSED_CLIVAGE: {
                    return (searchSetting == null) ? "" : searchSetting.getMaxMissedCleavages().toString();
                }
                case ROWTYPE_FIXED_MODIFICATIONS: {
                    if (searchSetting == null) {
                        return "";
                    }
                    Set<UsedPtm> usedPtmSet = searchSetting.getUsedPtms();
                    Iterator<UsedPtm> it = usedPtmSet.iterator();
                    while (it.hasNext()) {
                        UsedPtm usedPtm = it.next();
                        if (usedPtm.getIsFixed()) {
                            if (m_sb.length()>0) {
                                m_sb.append(", ");
                            }
                            m_sb.append(usedPtm.getShortName());
                        }
                        String modifications = m_sb.toString();
                        m_sb.setLength(0);
                        return modifications;
                    }
                    
                }
                case ROWTYPE_VARIABLE_MODIFICATIONS: {
                    if (searchSetting == null) {
                        return "";
                    }
                    Set<UsedPtm> usedPtmSet = searchSetting.getUsedPtms();
                    Iterator<UsedPtm> it = usedPtmSet.iterator();
                    while (it.hasNext()) {
                        UsedPtm usedPtm = it.next();
                        if (!usedPtm.getIsFixed()) {
                            if (m_sb.length() > 0) {
                                m_sb.append(", ");
                            }
                            m_sb.append(usedPtm.getShortName());
                        }
                        String modifications = m_sb.toString();
                        m_sb.setLength(0);
                        return modifications;
                    }
                }
                case ROWTYPE_PEPTIDE_CHARGE_STATES: {
                    return (searchSetting == null) ? "" : searchSetting.getPeptideChargeStates();
                }
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE: {
                    return (searchSetting == null) ? "" : searchSetting.getPeptideMassErrorTolerance().toString();
                }
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT: {
                    return (searchSetting == null) ? "" : searchSetting.getPeptideMassErrorToleranceUnit();
                }   
                case ROWTYPE_FRAGMENT_CHARGE_STATES: {
                    return (msmsSearch == null) ? "" : msmsSearch.getFragmentChargeStates();
                }
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE: {
                    return (msmsSearch == null) ? "" : String.valueOf(msmsSearch.getFragmentMassErrorTolerance());
                }
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE_UNIT: {
                    return (msmsSearch == null) ? "" : msmsSearch.getFragmentMassErrorToleranceUnit();
                }
            }
            
            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
    
    
    /**
     * SearchPropertiesGroup
     */
    public class SearchResultInformationGroup extends DataGroup {
        
        private static final int ROWTYPE_QUERIES_NUMBER = 0;
        private static final int ROWTYPE_PSM_NUMBER = 1;
        private static final int ROWTYPE_PEPTIDE_NUMBER = 2;
        private static final int ROWTYPE_PROTEIN_NUMBER = 3;
        private static final int ROWTYPE_DECOY_QUERIES_NUMBER = 4;
        private static final int ROWTYPE_DECOY_PSM_NUMBER = 5;
        private static final int ROWTYPE_DECOY_PEPTIDE_NUMBER = 6;
        private static final int ROWTYPE_DECOY_PROTEIN_NUMBER = 7;

        
        private static final int ROW_COUNT = 8; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(76,166,107);
        
        public SearchResultInformationGroup(int rowStart) {
            super("Search Result Information", rowStart);
        }
        
        @Override
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_QUERIES_NUMBER:
                    return "Queries Number";
                case ROWTYPE_PSM_NUMBER:
                    return "PSM Number";
                case ROWTYPE_PEPTIDE_NUMBER:
                    return "Peptide Number";
                case ROWTYPE_PROTEIN_NUMBER:
                    return "Protein Number";
                case ROWTYPE_DECOY_QUERIES_NUMBER:
                    return "Queries Decoy Number";
                case ROWTYPE_DECOY_PSM_NUMBER:
                    return "PSM Decoy Number";
                case ROWTYPE_DECOY_PEPTIDE_NUMBER:
                    return "Peptide Decoy Number";
                case ROWTYPE_DECOY_PROTEIN_NUMBER:
                    return "Protein Decoy Number";

            }
            
            return null;
        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            ResultSet rset = dataset.getResultSet();
            ResultSet rsetDecoy = (rset==null) ? null : rset.getDecoyResultSet();

            if (rset == null) {
                return "";
            }

            switch (rowIndex) {
                case ROWTYPE_QUERIES_NUMBER:
                    return "NA";
                case ROWTYPE_PSM_NUMBER: {
                    Object data = rset.getTransientData().getPeptideMatchesCount();
                    if (data == null) {
                        return "";
                    }
                    return data.toString();
                }
                case ROWTYPE_PEPTIDE_NUMBER:
                    return "NA";
                case ROWTYPE_PROTEIN_NUMBER: {
                    Object data = rset.getTransientData().getProteinMatchesCount();
                    if (data == null) {
                        return "";
                    }
                    return data.toString();
                }
                case ROWTYPE_DECOY_QUERIES_NUMBER:
                    return "NA";
                case ROWTYPE_DECOY_PSM_NUMBER: {
                    if (rsetDecoy == null) {
                        return "";
                    }
                    Object data = rsetDecoy.getTransientData().getPeptideMatchesCount();
                    if (data == null) {
                        return "";
                    }
                    return data.toString();
                }
                case ROWTYPE_DECOY_PEPTIDE_NUMBER:
                    return "NA";
                case ROWTYPE_DECOY_PROTEIN_NUMBER: {
                    if (rsetDecoy == null) {
                        return "";
                    }
                    Object data = rsetDecoy.getTransientData().getProteinMatchesCount();
                    if (data == null) {
                        return "";
                    }
                    return data.toString();
                }

            }

            
            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
    
        /**
     * SearchPropertiesGroup
     */
    public class IdentificationSummaryInformationGroup extends DataGroup {
        
        private static final int ROWTYPE_PROTEINSET_NUMBER = 0;
        private static final int ROWTYPE_PSM_NUMBER = 1;
        private static final int ROWTYPE_PEPTIDE_NUMBER= 2;
        private static final int ROWTYPE_PROTEIN_NUMBER = 3;
        private static final int ROWTYPE_PROTEINSET_DECOY_NUMBER = 4;
        private static final int ROWTYPE_PSM_DECOY_NUMBER = 5;
        private static final int ROWTYPE_PEPTIDE_DECOY_NUMBER= 6;
        private static final int ROWTYPE_PROTEIN_DECOY_NUMBER = 7;

        
        private static final int ROW_COUNT = 8; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(71,163,254);
        
        private ArrayList<String> m_valuesName = new ArrayList<>();
        private HashMap<Integer, HashMap<String, String>> m_valuesMap = new HashMap<>();
        
        public IdentificationSummaryInformationGroup(int rowStart, ArrayList<DDataset> datasetArrayList) {
            super("Identification Summary Information", rowStart);
            
            // retrieve information on maps
            TreeSet<String> keysSet = new TreeSet<>();
            int nbDataset = datasetArrayList.size();

            
            try {
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetArrayList.get(i);
                    ResultSummary rsm = dataset.getResultSummary();
                    if (rsm == null) {
                        continue;
                    }
                    Map<String, Object> map = dataset.getResultSummary().getSerializedPropertiesAsMap();

                    HashMap<String, String> propertiesList = new HashMap<>();
                    SerializedPropertiesUtil.getProperties(propertiesList, "Identification Summary Information", map);

                    m_valuesMap.put(Integer.valueOf(i), propertiesList);
                    
                    for (String key : propertiesList.keySet()) {
                        keysSet.add(key);
                    }
                }
            } catch (Exception e) {
                // should not happen
            }
            
            for (String key : keysSet) {
                m_valuesName.add(key);
            }
        }
        
        @Override
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_PROTEINSET_NUMBER:
                    return "Protein Sets Number";
                case ROWTYPE_PSM_NUMBER:
                    return "PSM Number";
                case ROWTYPE_PEPTIDE_NUMBER:
                    return "Peptide Number";
                case ROWTYPE_PROTEIN_NUMBER:
                    return "Protein Number";
                case ROWTYPE_PROTEINSET_DECOY_NUMBER:
                    return "Protein Sets Decoy Number";
                case ROWTYPE_PSM_DECOY_NUMBER:
                    return "PSM Decoy Number";
                case ROWTYPE_PEPTIDE_DECOY_NUMBER:
                    return "Peptide Decoy Number";
                case ROWTYPE_PROTEIN_DECOY_NUMBER:
                    return "Protein Decoy Number";
                default:
                    return m_valuesName.get(rowIndex-ROW_COUNT);
            }

        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            ResultSet rset = dataset.getResultSet();

            
            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();

            ResultSummary rsm = dataset.getResultSummary();
            if (rsm == null) {
                return "";
            }
            ResultSummary rsmDecoy = rsm.getDecotResultSummary();

            switch (rowIndex) {
                case ROWTYPE_PROTEINSET_NUMBER:
                    return String.valueOf(rsm.getTransientData().getNumberOfProteinSet());
                case ROWTYPE_PSM_NUMBER:
                    return "NA";
                case ROWTYPE_PEPTIDE_NUMBER:
                    return "NA";
                case ROWTYPE_PROTEIN_NUMBER:
                    return "NA";
                case ROWTYPE_PROTEINSET_DECOY_NUMBER:
                    if (rsmDecoy == null) {
                        return "";
                    }
                    Integer number = rsmDecoy.getTransientData().getNumberOfProteinSet();
                    if (number == null) {
                        return "";
                    }
                    return String.valueOf(number);
                case ROWTYPE_PSM_DECOY_NUMBER:
                    return "NA";
                case ROWTYPE_PEPTIDE_DECOY_NUMBER:
                    return "NA";
                case ROWTYPE_PROTEIN_DECOY_NUMBER:
                    return "NA";
                default: {
                    String key = m_valuesName.get(rowIndex-ROW_COUNT);
                    String value = m_valuesMap.get(columnIndex).get(key);
                    if (value == null) {
                        value = "";
                    }
                    return value;
                }

            }
   
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT+m_valuesName.size();
        }

    }
    
    /**
     * Dataset
     */
    public class SqlIdGroup extends DataGroup {
        
        private static final int ROWTYPE_PROJECT_ID = 0;
        private static final int ROWTYPE_DATASET_ID = 1;
        private static final int ROWTYPE_RSET_ID = 2;
        private static final int ROWTYPE_RSM_ID = 3;
        private static final int ROWTYPE_MSI_SEARCH_ID = 4;
        private static final int ROWTYPE_PEAKLIST_SEARCH_ID = 5;
        private static final int ROWTYPE_PEAKLIST_SOFTWARE_ID = 6;
        private static final int ROWTYPE_SEARCH_SETTINGS_ID = 7;
        private static final int ROWTYPE_INSTRUMENT_CONFIGURATION_ID = 8;
        private static final int ROW_COUNT = 9; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(153,122,141);
        
        public SqlIdGroup(int rowStart) {
            super("Sql Ids", rowStart);
        }
        
        @Override
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {

                case ROWTYPE_PROJECT_ID:
                    return "Project id";
                case ROWTYPE_DATASET_ID:
                    return "Dataset id";
                case ROWTYPE_RSET_ID:
                    return "ResultSet id";
                case ROWTYPE_RSM_ID:
                    return "ResultSummary id";
                case ROWTYPE_MSI_SEARCH_ID:
                    return "Msi Search id";
                case ROWTYPE_PEAKLIST_SEARCH_ID:
                    return "Peaklist Search id";
                case ROWTYPE_PEAKLIST_SOFTWARE_ID:
                    return "Peaklist Software id";
                case ROWTYPE_SEARCH_SETTINGS_ID:
                    return "Search Settings id";
                case ROWTYPE_INSTRUMENT_CONFIGURATION_ID:
                    return "Instrument Configuration id";
            }

            return null;
        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            ResultSet rset = dataset.getResultSet();

            ResultSummary rsm = dataset.getResultSummary();

            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();

            InstrumentConfig instrumentConfig = (searchSetting==null) ? null : searchSetting.getInstrumentConfig();
            Peaklist peaklist = (msiSearch == null) ? null : msiSearch.getPeaklist();
            PeaklistSoftware peaklistSoftware = (peaklist == null) ? null : peaklist.getPeaklistSoftware();

            
            switch (rowIndex) {
                case ROWTYPE_PROJECT_ID:
                    return String.valueOf(dataset.getProject().getId());
                case ROWTYPE_DATASET_ID:
                    return String.valueOf(dataset.getId());
                case ROWTYPE_RSET_ID:
                    return (rset==null) ? "" : String.valueOf(rset.getId());
                case ROWTYPE_RSM_ID:
                    return (rsm==null) ? "" : String.valueOf(rsm.getId());
                case ROWTYPE_MSI_SEARCH_ID:
                    return (msiSearch==null) ? "" : String.valueOf(msiSearch.getId());
                case ROWTYPE_PEAKLIST_SEARCH_ID:
                    return (peaklist==null) ? "" : String.valueOf(peaklist.getId());
                case ROWTYPE_PEAKLIST_SOFTWARE_ID:
                    return (peaklistSoftware==null) ? "" : String.valueOf(peaklistSoftware.getId());
                case ROWTYPE_SEARCH_SETTINGS_ID:
                    return (searchSetting==null) ? "" : String.valueOf(searchSetting.getId());
                case ROWTYPE_INSTRUMENT_CONFIGURATION_ID:
                    return (instrumentConfig==null) ? "" : String.valueOf(instrumentConfig.getId());
            }
            
            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }
        
         
    }
    
    
    public class PropertiesRenderer extends DefaultTableCellRenderer {

        private final boolean m_nonOkRenderer;
        private final Color COLOR_DIFFERENT = new Color(253,241,184);
        
        public PropertiesRenderer(boolean nonOkenderer) {
            m_nonOkRenderer = nonOkenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            m_table = table;

            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);

            if (!isSelected) {
                if (m_nonOkRenderer) {
                    setBackground(COLOR_DIFFERENT);
                    setForeground(Color.black);
                } else if (column == COLTYPE_GROUP_NAME) {
                    DataGroup dataGroup = m_dataGroupMap.get(row);
                    if (dataGroup.isFirstRow(row)) {
                        setBackground(dataGroup.getGroupColor(row));
                        setForeground(Color.white);
                    } else {
                        setBackground(javax.swing.UIManager.getColor("Table.background"));
                        setForeground(javax.swing.UIManager.getColor("Table.foreground"));
                    }
                } else {
                    setBackground(javax.swing.UIManager.getColor("Table.background"));
                    setForeground(javax.swing.UIManager.getColor("Table.foreground"));
                }
            } else {
                setBackground(javax.swing.UIManager.getColor("Table.selectionBackground"));
                setForeground(javax.swing.UIManager.getColor("Table.selectionForeground"));
            }

            return this;

        }

    }

}
