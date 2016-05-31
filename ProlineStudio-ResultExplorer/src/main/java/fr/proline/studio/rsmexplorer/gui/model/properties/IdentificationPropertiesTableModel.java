package fr.proline.studio.rsmexplorer.gui.model.properties;


import fr.proline.core.orm.msi.Enzyme;
import fr.proline.core.orm.msi.InstrumentConfig;
import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.MsmsSearch;
import fr.proline.core.orm.msi.Peaklist;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.SearchSetting;
import fr.proline.core.orm.msi.PeaklistSoftware;
import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.msi.UsedPtm;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.utils.SerializedPropertiesUtil;
import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 *
 * @author JM235353
 */
public class IdentificationPropertiesTableModel extends AbstractPropertiesTableModel {

    
    private ArrayList<ResultSet> m_rsetArray = null;
    private ArrayList<ResultSummary> m_rsmArray = null;

    private final StringBuilder m_sb = new StringBuilder();

    public IdentificationPropertiesTableModel() {
        
    }
    
    public void setData(ArrayList<DDataset> datasetArrayList) {

        m_datasetArrayList = datasetArrayList;
        
        int nbDataset = datasetArrayList.size();
        m_datasetNameArray = new ArrayList<>(nbDataset);
        m_rsetArray = new ArrayList<>(nbDataset);
        m_rsmArray = new ArrayList<>(nbDataset);
        m_projectIdArray = new ArrayList<>(nbDataset);
        m_datasetIdArray = new ArrayList<>(nbDataset);
        boolean hasRsm = false;
        for (int i=0;i<nbDataset;i++) {
            DDataset dataset = datasetArrayList.get(i);
            m_datasetNameArray.add(dataset.getName());
            m_projectIdArray.add(dataset.getProject().getId());
            m_datasetIdArray.add(dataset.getId());
            m_rsetArray.add(dataset.getResultSet());
            ResultSummary rsm = dataset.getResultSummary();
            m_rsmArray.add(rsm);
            if (rsm != null) {
                hasRsm = true;
            }
        }

        
        
        m_rowCount = -1; // will be recalculated later
         
        if (m_dataGroupList == null) {
            m_dataGroupList = new ArrayList<>();
            m_dataGroupMap = new HashMap<>();
            
            int startRow = 0;
            DataGroup group = new GeneralInformationGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            group = new SearchPropertiesGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            group = new SearchResultInformationGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            if (hasRsm) {
                group = new IdentificationSummaryInformationGroup(startRow, datasetArrayList); m_dataGroupList.add(group); startRow+=group.getRowCount();
            } 
            group = new SqlIdGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();

        }
        
        fireTableStructureChanged();
    }
    




    
    
    
    
     /**
     * GeneralInformationGroup
     */
    public class GeneralInformationGroup extends DataGroup {
        
        private static final int ROWTYPE_RAW_FILE_NAME = 0;
        private static final int ROWTYPE_SEARCH_RESULT_NAME = 1;
        private static final int ROWTYPE_INSTRUMENT_NAME = 2;
        private static final int ROWTYPE_TARGET_DECOY_MODE = 3;
        //private static final int ROWTYPE_TARGET_DECOY_RULE = 4;
        private static final int ROWTYPE_PEAKLIST_SOFTWARE_NAME = 4;
        private static final int ROW_COUNT = 5; // get in sync
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
                /*case ROWTYPE_TARGET_DECOY_RULE:
                    return "Decoy Rule";*/
                case ROWTYPE_PEAKLIST_SOFTWARE_NAME:
                    return "Peaklist Software Name";
            }
            
            return null;
        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {


            ResultSet rset = m_rsetArray.get(columnIndex);
            ResultSet rsetDecoy = (rset==null) ? null : rset.getDecoyResultSet();
            
            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();
            InstrumentConfig instrumentConfig = (searchSetting==null) ? null : searchSetting.getInstrumentConfig();
            Peaklist peaklist = (msiSearch == null) ? null : msiSearch.getPeaklist();
            PeaklistSoftware peaklistSoftware = (peaklist == null) ? null : peaklist.getPeaklistSoftware();
            ResultSummary rsm = m_rsmArray.get(columnIndex);


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
                /*case ROWTYPE_TARGET_DECOY_RULE: {
                    return "NA";
                }*/
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
        //private static final int ROWTYPE_DATABASE_NAME = 4;
        private static final int ROWTYPE_TAXONOMY = 4;
        private static final int ROWTYPE_ENZYME = 5;
        private static final int ROWTYPE_MAX_MISSED_CLIVAGE = 6;
        private static final int ROWTYPE_FIXED_MODIFICATIONS = 7;
        private static final int ROWTYPE_VARIABLE_MODIFICATIONS = 8;
        private static final int ROWTYPE_FRAGMENT_MASS_TOLERANCE = 9;
        private static final int ROWTYPE_PEPTIDE_CHARGE_STATES = 10;
        private static final int ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE = 11;
        //private static final int ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT = 12;
        private static final int ROWTYPE_FRAGMENT_CHARGE_STATES = 12;
        private static final int ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE = 13;
        //private static final int ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE_UNIT = 15;
        
        private static final int ROW_COUNT = 14; // get in sync
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
                /*case ROWTYPE_DATABASE_NAME:
                    return "Database Name";*/
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
                case ROWTYPE_FRAGMENT_MASS_TOLERANCE:
                    return "Fragment Mass Tolerance";
                case ROWTYPE_PEPTIDE_CHARGE_STATES:
                    return "Peptide Charge States";
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE:
                    return "Peptide Mass Error Tolerance";
                /*case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT:
                    return "Peptide Mass Error Tolerance Unit";*/
                case ROWTYPE_FRAGMENT_CHARGE_STATES:
                    return "Fragment Charge States";
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE:
                    return "Fragment Mass Error Tolerance";
                /*case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE_UNIT:
                    return "Fragment Mass Error Tolerance Unit";*/
            }
            
            return null;
        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSet rset = m_rsetArray.get(columnIndex);
            
            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();
            MsmsSearch msmsSearch =  null;
            if (searchSetting instanceof MsmsSearch) {
                msmsSearch = (MsmsSearch) searchSetting;
            }
            /*ResultSummary rsm = m_rsmArray.get(columnIndex);
            if (rsm == null) {
                return "";
            }*/

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
                /*case ROWTYPE_DATABASE_NAME: {
                    return "NA";
                }*/
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
                            String shortName = usedPtm.getShortName();
                            m_sb.append(shortName);
                            if (shortName.indexOf('(') == -1) {
                                PtmSpecificity ptmSpecificity = usedPtm.getPtmSpecificity();
                                if (ptmSpecificity != null) {
                                    Character c = ptmSpecificity.getResidue();
                                    if (c != null) {
                                        m_sb.append('(').append(c).append(')');
                                    }
                                }
                            }
                        }
                        
                    }
                    String modifications = m_sb.toString();
                    m_sb.setLength(0);
                    return modifications;

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
                            
                            String shortName = usedPtm.getShortName();
                            m_sb.append(shortName);
                            if (shortName.indexOf('(') == -1) {
                                PtmSpecificity ptmSpecificity = usedPtm.getPtmSpecificity();
                                if (ptmSpecificity != null) {
                                    Character c = ptmSpecificity.getResidue();
                                    if (c != null) {
                                        m_sb.append('(').append(c).append(')');
                                    }
                                }
                            }
                            
                        }
                        
                    }
                    String modifications = m_sb.toString();
                    m_sb.setLength(0);
                    return modifications;
                }
                case ROWTYPE_FRAGMENT_MASS_TOLERANCE: {
                    if (msmsSearch == null) {
                        return "";
                    }
                    return msmsSearch.getFragmentMassErrorTolerance() + " " + msmsSearch.getFragmentMassErrorToleranceUnit();
                }
                case ROWTYPE_PEPTIDE_CHARGE_STATES: {
                    return (searchSetting == null) ? "" : searchSetting.getPeptideChargeStates();
                }
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE: {
                    return (searchSetting == null) ? "" : searchSetting.getPeptideMassErrorTolerance().toString()+" "+searchSetting.getPeptideMassErrorToleranceUnit();
                }
                /*case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT: {
                    return (searchSetting == null) ? "" : searchSetting.getPeptideMassErrorToleranceUnit();
                }*/  
                case ROWTYPE_FRAGMENT_CHARGE_STATES: {
                    return (msmsSearch == null) ? "" : msmsSearch.getFragmentChargeStates();
                }
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE: {
                    return (msmsSearch == null) ? "" : String.valueOf(msmsSearch.getFragmentMassErrorTolerance())+" "+msmsSearch.getFragmentMassErrorToleranceUnit();
                }
                /*case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE_UNIT: {
                    return (msmsSearch == null) ? "" : msmsSearch.getFragmentMassErrorToleranceUnit();
                }*/
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
        //private static final int ROWTYPE_PEPTIDE_NUMBER = 2;
        private static final int ROWTYPE_PROTEIN_NUMBER = 2;
        private static final int ROWTYPE_DECOY_PSM_NUMBER = 3;
        //private static final int ROWTYPE_DECOY_PEPTIDE_NUMBER = 4;
        private static final int ROWTYPE_DECOY_PROTEIN_NUMBER = 4;

        
        private static final int ROW_COUNT = 5; // get in sync
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
                /*case ROWTYPE_PEPTIDE_NUMBER:
                    return "Peptide Number";*/
                case ROWTYPE_PROTEIN_NUMBER:
                    return "Protein Number";
                case ROWTYPE_DECOY_PSM_NUMBER:
                    return "PSM Decoy Number";
                /*case ROWTYPE_DECOY_PEPTIDE_NUMBER:
                    return "Peptide Decoy Number";*/
                case ROWTYPE_DECOY_PROTEIN_NUMBER:
                    return "Protein Decoy Number";

            }
            
            return null;
        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSet rset = m_rsetArray.get(columnIndex);
            ResultSet rsetDecoy = (rset==null) ? null : rset.getDecoyResultSet();
            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            
            if (rset == null) {
                return "";
            }

            switch (rowIndex) {
                case ROWTYPE_QUERIES_NUMBER:
                    if (msiSearch == null) {
                        return "";
                    }
                    return String.valueOf(msiSearch.getQueriesCount());
                case ROWTYPE_PSM_NUMBER: {
                    Object data = rset.getTransientData().getPeptideMatchesCount();
                    if (data == null) {
                        return "";
                    }
                    return data.toString();
                }
                /*case ROWTYPE_PEPTIDE_NUMBER:
                    return "NA";*/
                case ROWTYPE_PROTEIN_NUMBER: {
                    Object data = rset.getTransientData().getProteinMatchesCount();
                    if (data == null) {
                        return "";
                    }
                    return data.toString();
                }
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
                /*case ROWTYPE_DECOY_PEPTIDE_NUMBER:
                    return "NA";*/
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
     * IdentificationSummaryInformationGroup
     */
    public class IdentificationSummaryInformationGroup extends DataGroup {
        
        private static final int ROWTYPE_PROTEINSET_NUMBER = 0;
        private static final int ROWTYPE_PSM_NUMBER = 1;
        private static final int ROWTYPE_PEPTIDE_NUMBER= 2;
        //private static final int ROWTYPE_PROTEIN_NUMBER = 3;
        private static final int ROWTYPE_PROTEINSET_DECOY_NUMBER = 3;
        private static final int ROWTYPE_PSM_DECOY_NUMBER = 4;
        private static final int ROWTYPE_PEPTIDE_DECOY_NUMBER= 5;
        //private static final int ROWTYPE_PROTEIN_DECOY_NUMBER = 7;

        
        private static final int ROW_COUNT = 6; // get in sync
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
                /*case ROWTYPE_PROTEIN_NUMBER:
                    return "Protein Number";*/
                case ROWTYPE_PROTEINSET_DECOY_NUMBER:
                    return "Protein Sets Decoy Number";
                case ROWTYPE_PSM_DECOY_NUMBER:
                    return "PSM Decoy Number";
                case ROWTYPE_PEPTIDE_DECOY_NUMBER:
                    return "Peptide Decoy Number";
                /*case ROWTYPE_PROTEIN_DECOY_NUMBER:
                    return "Protein Decoy Number";*/
                default:
                    return m_valuesName.get(rowIndex-ROW_COUNT);
            }

        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSummary rsm = m_rsmArray.get(columnIndex);
            if (rsm == null) {
                return "";
            }
            ResultSummary rsmDecoy = rsm.getDecoyResultSummary();

            switch (rowIndex) {
                case ROWTYPE_PROTEINSET_NUMBER:
                    return String.valueOf(rsm.getTransientData().getNumberOfProteinSet());
                case ROWTYPE_PSM_NUMBER:
                    return String.valueOf(rsm.getTransientData().getNumberOfPeptideMatches());
                case ROWTYPE_PEPTIDE_NUMBER:
                    return String.valueOf(rsm.getTransientData().getNumberOfPeptides());
                /*case ROWTYPE_PROTEIN_NUMBER:
                    return "NA";*/
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
                    if (rsmDecoy == null) {
                        return "";
                    }
                    return String.valueOf(rsmDecoy.getTransientData().getNumberOfPeptideMatches());
                case ROWTYPE_PEPTIDE_DECOY_NUMBER:
                    if (rsmDecoy == null) {
                        return "";
                    }
                    return String.valueOf(rsmDecoy.getTransientData().getNumberOfPeptides());
                /*case ROWTYPE_PROTEIN_DECOY_NUMBER:
                    return "NA";*/
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

            ResultSet rset = m_rsetArray.get(columnIndex);

            ResultSummary rsm = m_rsmArray.get(columnIndex);

            MsiSearch msiSearch = (rset==null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();

            InstrumentConfig instrumentConfig = (searchSetting==null) ? null : searchSetting.getInstrumentConfig();
            Peaklist peaklist = (msiSearch == null) ? null : msiSearch.getPeaklist();
            PeaklistSoftware peaklistSoftware = (peaklist == null) ? null : peaklist.getPeaklistSoftware();

            switch (rowIndex) {
                case ROWTYPE_PROJECT_ID:
                    return String.valueOf(m_projectIdArray.get(columnIndex));
                case ROWTYPE_DATASET_ID:
                    return String.valueOf(m_datasetIdArray.get(columnIndex));
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
    
    
    

}
