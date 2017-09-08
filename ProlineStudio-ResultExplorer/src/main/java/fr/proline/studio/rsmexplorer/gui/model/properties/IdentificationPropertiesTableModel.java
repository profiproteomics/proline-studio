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
import fr.proline.core.orm.msi.SearchSettingsSeqDatabaseMap;
import fr.proline.core.orm.msi.SeqDatabase;
import fr.proline.core.orm.msi.UsedPtm;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.utils.SerializedPropertiesUtil;
import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

    @Override
    public void setData(ArrayList<DDataset> datasetArrayList) {

        m_datasetArrayList = datasetArrayList;

        int nbDataset = datasetArrayList.size();
        m_datasetNameArray = new ArrayList<>(nbDataset);
        m_rsetArray = new ArrayList<>(nbDataset);
        m_rsmArray = new ArrayList<>(nbDataset);
        m_projectIdArray = new ArrayList<>(nbDataset);
        m_datasetIdArray = new ArrayList<>(nbDataset);
        boolean hasRsm = false;
        for (int i = 0; i < nbDataset; i++) {
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

            int startRow = 0;
            DataGroup group = new GeneralInformationGroup(startRow);
            m_dataGroupList.add(group);
            startRow += group.getRowCount();
            group = new SearchPropertiesGroup(startRow);
            m_dataGroupList.add(group);
            startRow += group.getRowCount();
            group = new SearchResultInformationGroup(startRow);
            m_dataGroupList.add(group);
            startRow += group.getRowCount();
            if (hasRsm) {
                group = new IdentificationSummaryInformationGroup(startRow, datasetArrayList);
                m_dataGroupList.add(group);
                startRow += group.getRowCount();

                group = new ValidationParametersGroup(startRow, datasetArrayList);
                m_dataGroupList.add(group);
                startRow += group.getRowCount();

                group = new ValidationResultsGroup(startRow, datasetArrayList);
                m_dataGroupList.add(group);
                startRow += group.getRowCount();

            }
            group = new SqlIdGroup(startRow);
            m_dataGroupList.add(group);
            startRow += group.getRowCount();

        }

        m_loaded = true;
        
        fireTableStructureChanged();
        
        
    }

    /**
     * GeneralInformationGroup
     */
    public class GeneralInformationGroup extends DataGroup {

        private static final int ROWTYPE_RAW_FILE_NAME = 0;
        private static final int FASTA_FILE_NAME = 1;
        private static final int ROWTYPE_SEARCH_RESULT_NAME = 2;
        private static final int ROWTYPE_INSTRUMENT_NAME = 3;
        private static final int ROWTYPE_TARGET_DECOY_MODE = 4;
        private static final int ROWTYPE_PEAKLIST_SOFTWARE_NAME = 5;
        private static final int ROW_COUNT = 6; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(0, 0, 0);

        public GeneralInformationGroup(int rowStart) {
            super("General Information", rowStart);
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_RAW_FILE_NAME:
                    return new GroupObject("Raw File Name", this);
                case FASTA_FILE_NAME:
                    return new GroupObject("Fasta Files", this);
                case ROWTYPE_SEARCH_RESULT_NAME:
                    return new GroupObject("Search Result Name", this);
                case ROWTYPE_INSTRUMENT_NAME:
                    return new GroupObject("Instrument Name", this);
                case ROWTYPE_TARGET_DECOY_MODE:
                    return new GroupObject("Target Decoy Mode", this);
                /*case ROWTYPE_TARGET_DECOY_RULE:
                 return "Decoy Rule";*/
                case ROWTYPE_PEAKLIST_SOFTWARE_NAME:
                    return new GroupObject("Peaklist Software", this);
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSet rset = m_rsetArray.get(columnIndex);
            ResultSet rsetDecoy = (rset == null) ? null : rset.getDecoyResultSet();

            MsiSearch msiSearch = (rset == null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();
            InstrumentConfig instrumentConfig = (searchSetting == null) ? null : searchSetting.getInstrumentConfig();
            Peaklist peaklist = (msiSearch == null) ? null : msiSearch.getPeaklist();
            PeaklistSoftware peaklistSoftware = (peaklist == null) ? null : peaklist.getPeaklistSoftware();

            switch (rowIndex) {
                case ROWTYPE_RAW_FILE_NAME: {
                    if (peaklist == null) {
                        return new GroupObject("", this);
                    } else {
                        String path = peaklist.getPath();
                        File f = new File(path);
                        return new GroupObject(f.getName(), this);
                    }
                }
                case FASTA_FILE_NAME: {
                    boolean first = true;
                    
                    if(searchSetting==null){
                        return new GroupObject("AGGREGATION", this);
                    }
                    
                    Set<SearchSettingsSeqDatabaseMap> searchSettingsSeqDatabaseMapSet = searchSetting.getSearchSettingsSeqDatabaseMaps();
                    Iterator<SearchSettingsSeqDatabaseMap> itSeqDbMap = searchSettingsSeqDatabaseMapSet.iterator();
                    while (itSeqDbMap.hasNext()) {
                        SearchSettingsSeqDatabaseMap seqDbMap = itSeqDbMap.next();

                        SeqDatabase seqDatabase = seqDbMap.getSeqDatabase();
                        if (!first) {
                            m_sb.append(" ; ");
                        }
                        m_sb.append(seqDatabase.getFastaFilePath());
                        first = false;

                    }
                    String paths = m_sb.toString();
                    m_sb.setLength(0);
                    return new GroupObject(paths, this);
                }
                case ROWTYPE_SEARCH_RESULT_NAME:
                    return new GroupObject((rset == null) ? "" : rset.getName(), this);
                case ROWTYPE_INSTRUMENT_NAME:
                    return new GroupObject((instrumentConfig == null) ? "" : instrumentConfig.getName(), this);
                case ROWTYPE_TARGET_DECOY_MODE: {
                    if (rsetDecoy == null) {
                        return new GroupObject("", this);
                    }
                    try {
                        Map<String, Object> map = rsetDecoy.getSerializedPropertiesAsMap();
                        Object o = map.get("target_decoy_mode");
                        if (o == null) {
                            //JPM.WART: bug in server/database :information sometimes saved in rset, not in decoy rset
                            map = rset.getSerializedPropertiesAsMap();
                            o = map.get("target_decoy_mode");
                        }
                        if (o == null) {
                            return new GroupObject("", this);
                        }
                        return new GroupObject(o.toString(), this);
                    } catch (Exception e) {
                        return new GroupObject("", this);
                    }
                }
                case ROWTYPE_PEAKLIST_SOFTWARE_NAME: {
                    if (peaklistSoftware == null) {
                        return new GroupObject("", this);
                    }
                    String name = peaklistSoftware.getName();
                    String version = peaklistSoftware.getVersion();
                    if ((version != null) && (!version.isEmpty())) {
                        name = name + " " + version;
                    }
                    return new GroupObject(name, this);
                }

            }

            return null;
        }
        private StringBuilder m_sb = new StringBuilder();

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
        private final Color GROUP_COLOR_BACKGROUND = new Color(254, 163, 71);

        public SearchPropertiesGroup(int rowStart) {
            super("Search Properties", rowStart);
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_RESULT_FILE_NAME:
                    return new GroupObject("Result File Name", this);
                case ROWTYPE_SEARCH_DATE:
                    return new GroupObject("Search Date", this);
                case ROWTYPE_SOFTWARE_NAME:
                    return new GroupObject("Software Name", this);
                case ROWTYPE_SOFTWARE_VERSION:
                    return new GroupObject("Software Version", this);
                /*case ROWTYPE_DATABASE_NAME:
                 return "Database Name";*/
                case ROWTYPE_TAXONOMY:
                    return new GroupObject("Taxonomy", this);
                case ROWTYPE_ENZYME:
                    return new GroupObject("Enzyme", this);
                case ROWTYPE_MAX_MISSED_CLIVAGE:
                    return new GroupObject("Max Missed Clivage", this);
                case ROWTYPE_FIXED_MODIFICATIONS:
                    return new GroupObject("Fixed Modifications", this);
                case ROWTYPE_VARIABLE_MODIFICATIONS:
                    return new GroupObject("Variable Modifications", this);
                case ROWTYPE_FRAGMENT_MASS_TOLERANCE:
                    return new GroupObject("Fragment Mass Tolerance", this);
                case ROWTYPE_PEPTIDE_CHARGE_STATES:
                    return new GroupObject("Peptide Charge States", this);
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE:
                    return new GroupObject("Peptide Mass Error Tolerance", this);
                /*case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT:
                 return "Peptide Mass Error Tolerance Unit";*/
                case ROWTYPE_FRAGMENT_CHARGE_STATES:
                    return new GroupObject("Fragment Charge States", this);
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE:
                    return new GroupObject("Fragment Mass Error Tolerance", this);
                /*case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE_UNIT:
                 return "Fragment Mass Error Tolerance Unit";*/
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSet rset = m_rsetArray.get(columnIndex);

            MsiSearch msiSearch = (rset == null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();
            MsmsSearch msmsSearch = null;
            if (searchSetting instanceof MsmsSearch) {
                msmsSearch = (MsmsSearch) searchSetting;
            }
            /*ResultSummary rsm = m_rsmArray.get(columnIndex);
             if (rsm == null) {
             return "";
             }*/

            switch (rowIndex) {
                case ROWTYPE_RESULT_FILE_NAME: {
                    return new GroupObject((msiSearch == null) ? "" : msiSearch.getResultFileName(), this);
                }
                case ROWTYPE_SEARCH_DATE: {
                    if (msiSearch == null) {
                        return new GroupObject("", this);
                    }
                    Timestamp timeStamp = msiSearch.getDate();
                    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
                    final String dateFormatted = df.format(timeStamp);
                    return new GroupObject(dateFormatted, this);
                }
                case ROWTYPE_SOFTWARE_NAME: {
                    return new GroupObject((searchSetting == null) ? "" : searchSetting.getSoftwareName(), this);
                }
                case ROWTYPE_SOFTWARE_VERSION: {
                    return new GroupObject((searchSetting == null) ? "" : searchSetting.getSoftwareVersion(), this);
                }
                /*case ROWTYPE_DATABASE_NAME: {
                 return "NA";
                 }*/
                case ROWTYPE_TAXONOMY: {
                    return new GroupObject((searchSetting == null) ? "" : searchSetting.getTaxonomy(), this);
                }
                case ROWTYPE_ENZYME: {
                    String enzyme = "";
                    if (searchSetting != null) {
                        Set<Enzyme> enzymeSet = searchSetting.getEnzymes();
                        Iterator<Enzyme> it = enzymeSet.iterator();
                        while (it.hasNext()) {
                            if (!enzyme.isEmpty()) {
                                enzyme += "";
                            }
                            enzyme += ((Enzyme) it.next()).getName();
                        }
                    }
                    return new GroupObject(enzyme, this);
                }
                case ROWTYPE_MAX_MISSED_CLIVAGE: {
                    return new GroupObject((searchSetting == null) ? "" : searchSetting.getMaxMissedCleavages().toString(), this);
                }
                case ROWTYPE_FIXED_MODIFICATIONS: {

                    if (searchSetting == null) {
                        return new GroupObject("", this);
                    }

                    Set<UsedPtm> usedPtmSet = searchSetting.getUsedPtms();

                    HashSet<String> stringPtmSet = new HashSet<String>();

                    Iterator<UsedPtm> it = usedPtmSet.iterator();

                    while (it.hasNext()) {

                        UsedPtm usedPtm = it.next();

                        if (usedPtm.getIsFixed()) {

                            /*
                             if (m_sb.length() > 0) {
                             m_sb.append(", ");
                             }
                             */
                            
                            String shortName = usedPtm.getShortName();

                            //m_sb.append(shortName);
                            if (shortName.indexOf('(') == -1) {
                                PtmSpecificity ptmSpecificity = usedPtm.getPtmSpecificity();
                                if (ptmSpecificity != null) {
                                    Character c = ptmSpecificity.getResidue();
                                    if (c != null) {
                                        //m_sb.append('(').append(c).append(')');
                                        shortName = shortName.concat("(").concat(c.toString()).concat(")");
                                    }
                                }
                            }

                            stringPtmSet.add(shortName);
                        }

                    }

                    String[] array = stringPtmSet.toArray(new String[stringPtmSet.size()]);

                    Arrays.sort(array);

                    for (String array1 : array) {
                        if (m_sb.length() > 0) {
                            m_sb.append(", ");
                        }
                        m_sb.append(array1);
                    }

                    String modifications = m_sb.toString();
                    m_sb.setLength(0);
                    return new GroupObject(modifications, this);

                }
                case ROWTYPE_VARIABLE_MODIFICATIONS: {
                    if (searchSetting == null) {
                        return new GroupObject("", this);
                    }

                    Set<UsedPtm> usedPtmSet = searchSetting.getUsedPtms();

                    HashSet<String> stringPtmSet = new HashSet<String>();

                    Iterator<UsedPtm> it = usedPtmSet.iterator();
                    while (it.hasNext()) {
                        UsedPtm usedPtm = it.next();

                        if (!usedPtm.getIsFixed()) {

                            /*
                             if (m_sb.length() > 0) {
                             m_sb.append(", ");
                             }
                             */
                            String shortName = usedPtm.getShortName();

                            //m_sb.append(shortName);
                            if (shortName.indexOf('(') == -1) {
                                PtmSpecificity ptmSpecificity = usedPtm.getPtmSpecificity();
                                if (ptmSpecificity != null) {
                                    Character c = ptmSpecificity.getResidue();
                                    if (c != null) {
                                        //m_sb.append('(').append(c).append(')');
                                        shortName = shortName.concat("(").concat(c.toString()).concat(")");
                                    }
                                }
                            }

                            stringPtmSet.add(shortName);

                        }

                    }

                    String[] array = stringPtmSet.toArray(new String[stringPtmSet.size()]);

                    Arrays.sort(array);

                    for (String array1 : array) {
                        if (m_sb.length() > 0) {
                            m_sb.append(", ");
                        }
                        m_sb.append(array1);
                    }

                    String modifications = m_sb.toString();
                    m_sb.setLength(0);
                    return new GroupObject(modifications, this);

                }
                case ROWTYPE_FRAGMENT_MASS_TOLERANCE: {
                    if (msmsSearch == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(msmsSearch.getFragmentMassErrorTolerance() + " " + msmsSearch.getFragmentMassErrorToleranceUnit(), this);
                }
                case ROWTYPE_PEPTIDE_CHARGE_STATES: {
                    return new GroupObject((searchSetting == null) ? "" : searchSetting.getPeptideChargeStates(), this);
                }
                case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE: {
                    return new GroupObject((searchSetting == null) ? "" : searchSetting.getPeptideMassErrorTolerance().toString() + " " + searchSetting.getPeptideMassErrorToleranceUnit(), this);
                }
                /*case ROWTYPE_PEPTIDE_MASS_ERROR_TOLERANCE_UNIT: {
                 return (searchSetting == null) ? "" : searchSetting.getPeptideMassErrorToleranceUnit();
                 }*/
                case ROWTYPE_FRAGMENT_CHARGE_STATES: {
                    return new GroupObject((msmsSearch == null) ? "" : msmsSearch.getFragmentChargeStates(), this);
                }
                case ROWTYPE_FRAGMENT_MASS_ERROR_TOLERANCE: {
                    return new GroupObject((msmsSearch == null) ? "" : String.valueOf(msmsSearch.getFragmentMassErrorTolerance()) + " " + msmsSearch.getFragmentMassErrorToleranceUnit(), this);
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
        private static final int ROWTYPE_PROTEIN_NUMBER = 2;
        private static final int ROWTYPE_DECOY_PSM_NUMBER = 3;
        private static final int ROWTYPE_DECOY_PROTEIN_NUMBER = 4;

        private static final int ROW_COUNT = 5; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(76, 166, 107);

        public SearchResultInformationGroup(int rowStart) {
            super("Search Result Information", rowStart);
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_QUERIES_NUMBER:
                    return new GroupObject("Queries Number", this);
                case ROWTYPE_PSM_NUMBER:
                    return new GroupObject("PSM Number", this);
                /*case ROWTYPE_PEPTIDE_NUMBER:
                 return "Peptide Number";*/
                case ROWTYPE_PROTEIN_NUMBER:
                    return new GroupObject("Protein Number", this);
                case ROWTYPE_DECOY_PSM_NUMBER:
                    return new GroupObject("PSM Decoy Number", this);
                /*case ROWTYPE_DECOY_PEPTIDE_NUMBER:
                 return "Peptide Decoy Number";*/
                case ROWTYPE_DECOY_PROTEIN_NUMBER:
                    return new GroupObject("Protein Decoy Number", this);

            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSet rset = m_rsetArray.get(columnIndex);
            ResultSet rsetDecoy = (rset == null) ? null : rset.getDecoyResultSet();
            MsiSearch msiSearch = (rset == null) ? null : rset.getMsiSearch();

            if (rset == null) {
                return new GroupObject("", this);
            }

            switch (rowIndex) {
                case ROWTYPE_QUERIES_NUMBER:
                    if (msiSearch == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(String.valueOf(msiSearch.getQueriesCount()), this);
                case ROWTYPE_PSM_NUMBER: {
                    Object data = rset.getTransientData().getPeptideMatchesCount();
                    if (data == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(data.toString(), this);
                }
                case ROWTYPE_PROTEIN_NUMBER: {
                    Object data = rset.getTransientData().getProteinMatchesCount();
                    if (data == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(data.toString(), this);
                }
                case ROWTYPE_DECOY_PSM_NUMBER: {
                    if (rsetDecoy == null) {
                        return new GroupObject("", this);
                    }
                    Object data = rsetDecoy.getTransientData().getPeptideMatchesCount();
                    if (data == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(data.toString(), this);
                }
                case ROWTYPE_DECOY_PROTEIN_NUMBER: {
                    if (rsetDecoy == null) {
                        return new GroupObject("", this);
                    }
                    Object data = rsetDecoy.getTransientData().getProteinMatchesCount();
                    if (data == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(data.toString(), this);
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
        private static final int ROWTYPE_PEPTIDE_NUMBER = 2;
        //private static final int ROWTYPE_PROTEIN_NUMBER = 3;
        private static final int ROWTYPE_PROTEINSET_DECOY_NUMBER = 3;
        private static final int ROWTYPE_PSM_DECOY_NUMBER = 4;
        private static final int ROWTYPE_PEPTIDE_DECOY_NUMBER = 5;
        //private static final int ROWTYPE_PROTEIN_DECOY_NUMBER = 7;

        private static final int ROW_COUNT = 6; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(71, 163, 254);

        private ArrayList<String> m_valuesName = new ArrayList<String>();
        private HashMap<Integer, HashMap<String, String>> m_valuesMap = new HashMap<>();

        public IdentificationSummaryInformationGroup(int rowStart, ArrayList<DDataset> datasetArrayList) {
            super("Identification Summary Information", rowStart);

            // retrieve information on maps
            TreeSet<String> keysSet = new TreeSet<String>();
            int nbDataset = datasetArrayList.size();

            try {
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetArrayList.get(i);
                    ResultSummary rsm = dataset.getResultSummary();
                    if (rsm == null) {
                        continue;
                    }
                    Map<String, Object> map = dataset.getResultSummary().getSerializedPropertiesAsMap();

                    HashMap<String, String> propertiesList = new HashMap<String, String>();
                    SerializedPropertiesUtil.getProperties(propertiesList, "Identification Summary Information", map);

                    m_valuesMap.put(Integer.valueOf(i), propertiesList);

                    for (String key : propertiesList.keySet()) {
                        if (!key.contains("validation_properties / results /") && !key.contains("validation_properties / params /")) {
                            keysSet.add(key);
                        }
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
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_PROTEINSET_NUMBER:
                    return new GroupObject("Protein Sets Number", this);
                case ROWTYPE_PSM_NUMBER:
                    return new GroupObject("PSM Number", this);
                case ROWTYPE_PEPTIDE_NUMBER:
                    return new GroupObject("Peptide Number", this);
                /*case ROWTYPE_PROTEIN_NUMBER:
                 return "Protein Number";*/
                case ROWTYPE_PROTEINSET_DECOY_NUMBER:
                    return new GroupObject("Protein Sets Decoy Number", this);
                case ROWTYPE_PSM_DECOY_NUMBER:
                    return new GroupObject("PSM Decoy Number", this);
                case ROWTYPE_PEPTIDE_DECOY_NUMBER:
                    return new GroupObject("Peptide Decoy Number", this);
                /*case ROWTYPE_PROTEIN_DECOY_NUMBER:
                 return "Protein Decoy Number";*/
                default:
                    return new GroupObject(m_valuesName.get(rowIndex - ROW_COUNT), this);
            }

        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSummary rsm = m_rsmArray.get(columnIndex);
            if (rsm == null) {
                return new GroupObject("", this);
            }
            ResultSummary rsmDecoy = rsm.getDecoyResultSummary();

            switch (rowIndex) {
                case ROWTYPE_PROTEINSET_NUMBER:
                    return new GroupObject(String.valueOf(rsm.getTransientData().getNumberOfProteinSet()), this);
                case ROWTYPE_PSM_NUMBER:
                    return new GroupObject(String.valueOf(rsm.getTransientData().getNumberOfPeptideMatches()), this);
                case ROWTYPE_PEPTIDE_NUMBER:
                    return new GroupObject(String.valueOf(rsm.getTransientData().getNumberOfPeptides()), this);
                /*case ROWTYPE_PROTEIN_NUMBER:
                 return "NA";*/
                case ROWTYPE_PROTEINSET_DECOY_NUMBER:
                    if (rsmDecoy == null) {
                        return new GroupObject("", this);
                    }
                    Integer number = rsmDecoy.getTransientData().getNumberOfProteinSet();
                    if (number == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(String.valueOf(number), this);
                case ROWTYPE_PSM_DECOY_NUMBER:
                    if (rsmDecoy == null) {
                        return new GroupObject("", this);
                    }
                    Integer psmDecoyNumber = rsmDecoy.getTransientData().getNumberOfPeptideMatches();
                    if (psmDecoyNumber == null) {
                        psmDecoyNumber = 0;
                    }
                    return new GroupObject(String.valueOf(psmDecoyNumber), this);
                case ROWTYPE_PEPTIDE_DECOY_NUMBER:
                    if (rsmDecoy == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(String.valueOf(rsmDecoy.getTransientData().getNumberOfPeptides()), this);
                /*case ROWTYPE_PROTEIN_DECOY_NUMBER:
                 return "NA";*/
                default: {
                    String key = m_valuesName.get(rowIndex - ROW_COUNT);
                    String value = m_valuesMap.get(columnIndex).get(key);
                    if (value == null) {
                        value = "";
                    }
                    return new GroupObject(value, this);
                }

            }

        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT + m_valuesName.size();
        }

    }

    /**
     * ValidationResultsGroup
     */
    public class ValidationResultsGroup extends DataGroup {

        private final Color GROUP_COLOR_BACKGROUND = new Color(205, 133, 63);

        private ArrayList<String> m_valuesName = new ArrayList<String>();
        private HashMap<Integer, HashMap<String, String>> m_valuesMap = new HashMap<>();

        private final String REQUIRED_PREFIX = "validation_properties / results /";

        public ValidationResultsGroup(int rowStart, ArrayList<DDataset> datasetArrayList) {
            super("Validation Results", rowStart);

            // retrieve information on maps
            TreeSet<String> keysSet = new TreeSet<String>();
            int nbDataset = datasetArrayList.size();

            try {
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetArrayList.get(i);
                    ResultSummary rsm = dataset.getResultSummary();
                    if (rsm == null) {
                        continue;
                    }
                    Map<String, Object> map = dataset.getResultSummary().getSerializedPropertiesAsMap();

                    HashMap<String, String> propertiesList = new HashMap<String, String>();
                    SerializedPropertiesUtil.getProperties(propertiesList, "Identification Summary Information", map);

                    m_valuesMap.put(Integer.valueOf(i), propertiesList);

                    for (String key : propertiesList.keySet()) {
                        if (key.contains(REQUIRED_PREFIX)) {
                            keysSet.add(key);
                        }
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
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {
            String key = m_valuesName.get(rowIndex);
            String value = m_valuesMap.get(columnIndex).get(key);
            if (value == null) {
                value = "";
            }
            return new GroupObject(value, this);
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            return new GroupObject(m_valuesName.get(rowIndex).substring(m_valuesName.get(rowIndex).indexOf(REQUIRED_PREFIX) + REQUIRED_PREFIX.length() + 1), this);
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return m_valuesName.size();
        }

    }

    /**
     * ValidationParametersGroup
     */
    public class ValidationParametersGroup extends DataGroup {

        private final Color GROUP_COLOR_BACKGROUND = new Color(250, 128, 114);

        private ArrayList<String> m_valuesName = new ArrayList<String>();
        private HashMap<Integer, HashMap<String, String>> m_valuesMap = new HashMap<>();

        private final String REQUIRED_PREFIX = "validation_properties / params /";

        public ValidationParametersGroup(int rowStart, ArrayList<DDataset> datasetArrayList) {
            super("Validation Parameters", rowStart);

            // retrieve information on maps
            TreeSet<String> keysSet = new TreeSet<String>();
            int nbDataset = datasetArrayList.size();

            try {
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetArrayList.get(i);
                    ResultSummary rsm = dataset.getResultSummary();
                    if (rsm == null) {
                        continue;
                    }
                    Map<String, Object> map = dataset.getResultSummary().getSerializedPropertiesAsMap();

                    HashMap<String, String> propertiesList = new HashMap<String, String>();
                    SerializedPropertiesUtil.getProperties(propertiesList, "Identification Summary Information", map);

                    m_valuesMap.put(Integer.valueOf(i), propertiesList);

                    for (String key : propertiesList.keySet()) {
                        if (key.contains(REQUIRED_PREFIX)) {
                            keysSet.add(key);
                        }
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
        public DataGroup.GroupObject getGroupValueAt(int rowIndex, int columnIndex) {
            String key = m_valuesName.get(rowIndex);
            String value = m_valuesMap.get(columnIndex).get(key);
            if (value == null) {
                value = "";
            }
            return new DataGroup.GroupObject(value, this);
        }

        @Override
        public DataGroup.GroupObject getGroupNameAt(int rowIndex) {
            return new GroupObject(m_valuesName.get(rowIndex).substring(m_valuesName.get(rowIndex).indexOf(REQUIRED_PREFIX) + REQUIRED_PREFIX.length() + 1), this);
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return m_valuesName.size();
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
        private final Color GROUP_COLOR_BACKGROUND = new Color(153, 122, 141);

        public SqlIdGroup(int rowStart) {
            super("Sql Ids", rowStart);
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {

                case ROWTYPE_PROJECT_ID:
                    return new GroupObject("Project id", this);
                case ROWTYPE_DATASET_ID:
                    return new GroupObject("Dataset id", this);
                case ROWTYPE_RSET_ID:
                    return new GroupObject("ResultSet id", this);
                case ROWTYPE_RSM_ID:
                    return new GroupObject("ResultSummary id", this);
                case ROWTYPE_MSI_SEARCH_ID:
                    return new GroupObject("Msi Search id", this);
                case ROWTYPE_PEAKLIST_SEARCH_ID:
                    return new GroupObject("Peaklist Search id", this);
                case ROWTYPE_PEAKLIST_SOFTWARE_ID:
                    return new GroupObject("Peaklist Software id", this);
                case ROWTYPE_SEARCH_SETTINGS_ID:
                    return new GroupObject("Search Settings id", this);
                case ROWTYPE_INSTRUMENT_CONFIGURATION_ID:
                    return new GroupObject("Instrument Configuration id", this);
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            ResultSet rset = m_rsetArray.get(columnIndex);

            ResultSummary rsm = m_rsmArray.get(columnIndex);

            MsiSearch msiSearch = (rset == null) ? null : rset.getMsiSearch();
            SearchSetting searchSetting = (msiSearch == null) ? null : msiSearch.getSearchSetting();

            InstrumentConfig instrumentConfig = (searchSetting == null) ? null : searchSetting.getInstrumentConfig();
            Peaklist peaklist = (msiSearch == null) ? null : msiSearch.getPeaklist();
            PeaklistSoftware peaklistSoftware = (peaklist == null) ? null : peaklist.getPeaklistSoftware();

            switch (rowIndex) {
                case ROWTYPE_PROJECT_ID:
                    return new GroupObject(String.valueOf(m_projectIdArray.get(columnIndex)), this);
                case ROWTYPE_DATASET_ID:
                    return new GroupObject(String.valueOf(m_datasetIdArray.get(columnIndex)), this);
                case ROWTYPE_RSET_ID:
                    return new GroupObject((rset == null) ? "" : String.valueOf(rset.getId()), this);
                case ROWTYPE_RSM_ID:
                    return new GroupObject((rsm == null) ? "" : String.valueOf(rsm.getId()), this);
                case ROWTYPE_MSI_SEARCH_ID:
                    return new GroupObject((msiSearch == null) ? "" : String.valueOf(msiSearch.getId()), this);
                case ROWTYPE_PEAKLIST_SEARCH_ID:
                    return new GroupObject((peaklist == null) ? "" : String.valueOf(peaklist.getId()), this);
                case ROWTYPE_PEAKLIST_SOFTWARE_ID:
                    return new GroupObject((peaklistSoftware == null) ? "" : String.valueOf(peaklistSoftware.getId()), this);
                case ROWTYPE_SEARCH_SETTINGS_ID:
                    return new GroupObject((searchSetting == null) ? "" : String.valueOf(searchSetting.getId()), this);
                case ROWTYPE_INSTRUMENT_CONFIGURATION_ID:
                    return new GroupObject((instrumentConfig == null) ? "" : String.valueOf(instrumentConfig.getId()), this);
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
