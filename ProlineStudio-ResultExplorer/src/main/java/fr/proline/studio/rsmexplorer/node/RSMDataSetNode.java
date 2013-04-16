package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Dataset.DatasetType;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.SerializedPropertiesUtil;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for Dataset
 * @author JM235353
 */
public class RSMDataSetNode extends RSMNode {

   
    
    
    public RSMDataSetNode(AbstractData data) {
        super(NodeTypes.DATA_SET, data);
    }

    @Override
    public ImageIcon getIcon() {
        
        Dataset dataset = ((DataSetData) getData()).getDataset();
        Dataset.DatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        switch(datasetType) {
            case IDENTIFICATION:
                
                if (dataset != null) {
                    if (dataset.getResultSummaryId() == null) {
                        if (isChanging()) {
                            return getIcon(IconManager.IconType.RSM); // will become a RSM
                        } else {
                            return getIcon(IconManager.IconType.RSET);
                        }
                    } else {
                        return getIcon(IconManager.IconType.RSM);
                    }
                } else {
                    return getIcon(IconManager.IconType.RSET);
                }
                
            case AGGREGATE:

                //Aggregation.ChildNature aggregateType = ((DataSetData) getData()).getAggregateType();
                //JPM.TODO : according to aggregateType type :icon must be different
                
                if (dataset != null) {
                    if (dataset.getResultSetId() != null) {
                        return getIcon(IconManager.IconType.VIAL_MERGED);
                    }
                }
                
                return getIcon(IconManager.IconType.VIAL);
            case TRASH:
                return getIcon(IconManager.IconType.TRASH);
            default:
                // sould not happen
                
        }
        
        return null;

    }
    

    
    public Dataset getDataset() {
        return ((DataSetData) getData()).getDataset();
    }
    
    public boolean isTrash() {
        Dataset dataset = ((DataSetData) getData()).getDataset();
        if (dataset == null) {
            return false;
        }
        Dataset.DatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        if (datasetType == Dataset.DatasetType.TRASH) {
            return true;
        }
        return false;
    }
    
    public boolean hasResultSummary() {
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSummaryId() != null);
    }
    

    public Integer getResultSummaryId() {
        return ((DataSetData) getData()).getDataset().getResultSummaryId();
    }
    
    public ResultSummary getResultSummary() {
        // getResultSummary() can return null if the resultSummary has not been loaded previously
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return dataSet.getTransientData().getResultSummary();
    }
    
    
    public boolean hasResultSet() {
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSetId() != null);
    }
    
    public Integer getResultSetId() {
        return ((DataSetData) getData()).getDataset().getResultSetId();
    }
    
    public ResultSet getResultSet() {
        // getResultSet() can return null if the resultSet has not been loaded previously
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return dataSet.getTransientData().getResultSet();
    }
    
    @Override
    public boolean isInTrash() {
        if (isTrash()) {
            return true;
        }
        return ((RSMNode) getParent()).isInTrash();
    }
    
    @Override
    public boolean canBeDeleted() {
        
        // for the moment, we can delete only empty DataSet with no leaf
        if (isChanging()) {
            return false;
        }
        if (isInTrash()) {
            return false;
        }
        
        
        
        
        return true;
        
    }
    
    public void rename(final String newName) {

        Dataset dataset = getDataset();
        String name = dataset.getName();

        if ((newName != null) && (newName.compareTo(name) != 0)) {

            final RSMDataSetNode datasetNode = this;

            setIsChanging(true);
            dataset.setName(newName + "...");
            ((DefaultTreeModel) RSMTree.getTree().getModel()).nodeChanged(this);


            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    datasetNode.setIsChanging(false);
                    datasetNode.getDataset().setName(newName);
                    ((DefaultTreeModel) RSMTree.getTree().getModel()).nodeChanged(datasetNode);
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initRenameDataset(dataset, newName);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    
    @Override
    public void loadDataForProperties(final Runnable callback) {
        
        // we must load resultSet and resultSummary
        final Dataset dataSet = ((DataSetData) getData()).getDataset();
        

        
        AbstractDatabaseCallback dbCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
                callback.run();

                
            }
        };


        // ask asynchronous loading of data
        
        // Task 1 : Load ResultSet and ResultSummary
        DatabaseDataSetTask task1 = new DatabaseDataSetTask(null);
        task1.setPriority(Priority.HIGH_3); // highest priority
        task1.initLoadRsetAndRsm(dataSet);
        
        // Task 2 : Load ResultSet Extra Data
        AbstractDatabaseCallback task2Callback = (dataSet.getResultSummaryId()!=null) ? null : dbCallback;
        DatabaseRsetProperties task2 = new DatabaseRsetProperties(task2Callback,dataSet.getProject().getId(),dataSet);
        task2.setPriority(Priority.HIGH_3); // highest priority
        task1.setConsecutiveTask(task2);
        
        // Task 3 : Count number of Protein Sets for Rsm
        if (dataSet.getResultSummaryId() != null) {
            DatabaseProteinSetsTask task3 = new DatabaseProteinSetsTask(dbCallback);
            task3.initCountProteinSets(dataSet);
            task3.setPriority(Priority.HIGH_3); // highest priority

            task2.setConsecutiveTask(task3);
        }
        
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task1);

    }
    
    
    @Override
    public Sheet createSheet() {
        Dataset dataset = getDataset();
        
        Sheet sheet = Sheet.createDefault();
        

        
        try {
            Sheet.Set propGroup = Sheet.createPropertiesSet();
            propGroup.setName("Data Set");
            propGroup.setDisplayName("Data Set");

            Property prop = new PropertySupport.Reflection<Integer>(dataset, Integer.class, "getId", null);
            prop.setName("id");
            propGroup.put(prop);
            
            sheet.put(propGroup);

            if (hasResultSummary()) {
                sheet.put(createResultSummarySheetSet(dataset));
                ResultSummary rsm = dataset.getTransientData().getResultSummary();
                try {
                    Map<String, Object> map = rsm.getSerializedPropertiesAsMap();
                    SerializedPropertiesUtil.getProperties(sheet, "SR Properties", map);
                } catch (Exception e) {
                    Logger logger = LoggerFactory.getLogger(RSMNode.class);
                    logger.error(getClass().getSimpleName() + " properties error ", e);
                }
            }

            if (hasResultSet()) {
                
                ResultSet rset = dataset.getTransientData().getResultSet();
                sheet.put(createResultSetSheetSet(rset, false));
                try {
                    Map<String, Object> map = rset.getSerializedPropertiesAsMap();
                    SerializedPropertiesUtil.getProperties(sheet, "IS Properties", map);
                } catch (Exception e) {
                    Logger logger = LoggerFactory.getLogger(RSMNode.class);
                    logger.error(getClass().getSimpleName() + " properties error ", e);
                }
                
                ResultSet rsetDecoy = rset.getDecoyResultSet();
                if (rsetDecoy != null) {
                    sheet.put(createResultSetSheetSet(rsetDecoy, true));
                    try {
                        Map<String, Object> map = rsetDecoy.getSerializedPropertiesAsMap();
                        SerializedPropertiesUtil.getProperties(sheet, "Decoy Ret Properties", map);
                    } catch (Exception e) {
                        Logger logger = LoggerFactory.getLogger(RSMNode.class);
                        logger.error(getClass().getSimpleName() + " properties error ", e);
                    }
                }
                
                
                MsiSearch msiSearch = rset.getMsiSearch();
                if (msiSearch != null) {
                    sheet.put(createMsiSearchSheetSet(msiSearch));
                
                
                    Peaklist peaklist = msiSearch.getPeaklist();
                    sheet.put(createPeakListSheetSet(peaklist));

                    PeaklistSoftware peaklistSoftware = peaklist.getPeaklistSoftware();
                    sheet.put(createPeakListSoftwareSheetSet(peaklistSoftware));
                    
                    SearchSetting searchSetting = msiSearch.getSearchSetting();
                    sheet.put(createSearchSettingSheetSet(searchSetting));

                    InstrumentConfig instrumentConfig = searchSetting.getInstrumentConfig();
                    sheet.put(createInstrumentConfigSheetSet(instrumentConfig));

                    Set<Enzyme> enzymeSet = searchSetting.getEnzymes();
                    Iterator<Enzyme> it = enzymeSet.iterator();
                    while (it.hasNext()) {
                        sheet.put(createEnzimeSheetSet(it.next()));
                    }

                    Set<SearchSettingsSeqDatabaseMap> searchSettingsSeqDatabaseMapSet = searchSetting.getSearchSettingsSeqDatabaseMaps();
                    Iterator<SearchSettingsSeqDatabaseMap> itSeqDbMap = searchSettingsSeqDatabaseMapSet.iterator();
                    while (itSeqDbMap.hasNext()) {
                        SearchSettingsSeqDatabaseMap seqDbMap = itSeqDbMap.next();

                        sheet.put(createSeqDatabaseheetSet(seqDbMap.getSeqDatabase()));
                    }
                }

            }



        } catch (NoSuchMethodException e) {
            Logger logger = LoggerFactory.getLogger(RSMNode.class);
            logger.error(getClass().getSimpleName() + " properties error ", e);
        }
        

        
        return sheet;
    }
    
    private Sheet.Set createResultSetSheetSet(final ResultSet rset, boolean decoy) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = decoy ? "Decoy Search Result" : "Search Result";
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        Property prop = new PropertySupport.Reflection<>(rset, Integer.class, "getId", null);
        prop.setName("ResultSet id");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(rset, String.class, "getName", null);
        prop.setName("Name");
        propGroup.put(prop);
        

        prop = new PropertySupport.Reflection<>(rset, String.class, "getDescription", null);
        prop.setName("Description");
        propGroup.put(prop);
        
        /*prop = new PropertySupport.Reflection<>(rset, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);*/

        prop = new PropertySupport.ReadOnly(
                "MS Query Count",
                Integer.class,
                "MS Query Count",
                "MS Query Count") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return rset.getTransientData().getMSQueriesCount();
            }
        };
        propGroup.put(prop);
        
        prop = new PropertySupport.ReadOnly(
                "Peptide Match Count",
                Integer.class,
                "Peptide Match Count",
                "Peptide Match Count") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return rset.getTransientData().getPeptideMatchesCount();
            }
        };
        
        propGroup.put(prop);

        prop = new PropertySupport.ReadOnly(
                "Protein Match Count",
                Integer.class,
                "Protein Match Count",
                "Protein Match Count") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return rset.getTransientData().getProteinMatchesCount();
            }
        };
        propGroup.put(prop);

        return propGroup;
    }
    
    private Sheet.Set createMsiSearchSheetSet(MsiSearch msiSearch) throws NoSuchMethodException {
        
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("MSI Search");
        propGroup.setDisplayName("MSI Search");

        
        Property prop = new PropertySupport.Reflection<>(msiSearch, Integer.class, "getId", null);
        prop.setName("MsiSearch id");
        propGroup.put(prop);
     
        prop = new PropertySupport.Reflection<>(msiSearch, String.class, "getTitle", null);
        prop.setName("Title");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(msiSearch, String.class, "getUserName", null);
        prop.setName("User Name");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(msiSearch, String.class, "getUserEmail", null);
        prop.setName("User Mail");
        propGroup.put(prop);
        
                prop = new PropertySupport.Reflection<>(msiSearch, Integer.class, "getJobNumber", null);
        prop.setName("Job Number");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(msiSearch, String.class, "getResultFileName", null);
        prop.setName("Result File Name");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(msiSearch, String.class, "getResultFileDirectory", null);
        prop.setName("Result File Directory");
        propGroup.put(prop);
        
        Timestamp timeStamp = msiSearch.getDate();
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        final String dateFormatted = df.format(timeStamp);
        prop = new PropertySupport.ReadOnly(
                "Search Date",
                String.class,
                "Search Date",
                "Search Date") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return dateFormatted;
            }
        };
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(msiSearch, Integer.class, "getQueriesCount", null);
        prop.setName("Queries Count");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(msiSearch, Integer.class, "getSearchedSequencesCount", null);
        prop.setName("Searched Sequences Count");
        propGroup.put(prop);
     
        prop = new PropertySupport.Reflection<>(msiSearch, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(msiSearch, Integer.class, "getSubmittedQueriesCount", null);
        prop.setName("Submitted Queries Count");
        propGroup.put(prop);
        
        return propGroup;
    }
    
    private Sheet.Set createPeakListSheetSet(Peaklist peaklist) throws NoSuchMethodException {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Peaklist");
        propGroup.setDisplayName("Peaklist");
        
        Property prop = new PropertySupport.Reflection<>(peaklist, Integer.class, "getId", null);
        prop.setName("Peaklist id");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklist, Integer.class, "getMsLevel", null);
        prop.setName("MS Level");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklist, String.class, "getPath", null);
        prop.setName("Path");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklist, String.class, "getRawFileName", null);
        prop.setName("Raw File Name");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklist, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklist, String.class, "getSpectrumDataCompression", null);
        prop.setName("Spectrum Data Compression");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklist, String.class, "getType", null);
        prop.setName("Type");
        propGroup.put(prop);

        
        return propGroup;
    }
    
    private Sheet.Set createPeakListSoftwareSheetSet(PeaklistSoftware peaklistSoftware) throws NoSuchMethodException {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Peaklist Software");
        propGroup.setDisplayName("Peaklist Software");
        
        Property prop = new PropertySupport.Reflection<>(peaklistSoftware, Integer.class, "getId", null);
        prop.setName("Peaklist Software id");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklistSoftware, String.class, "getName", null);
        prop.setName("Peaklist Name");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklistSoftware, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(peaklistSoftware, String.class, "getVersion", null);
        prop.setName("Version");
        propGroup.put(prop);
        
        
                
        return propGroup;
    }
    
    private Sheet.Set createSearchSettingSheetSet(SearchSetting searchSetting) throws NoSuchMethodException {
        
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Search Settings");
        propGroup.setDisplayName("Search Settings");
        
        Property prop = new PropertySupport.Reflection<>(searchSetting, Integer.class, "getId", null);
        prop.setName("Search Settings id");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, Boolean.class, "getIsDecoy", null);
        prop.setName("Decoy");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, Integer.class, "getMaxMissedCleavages", null);
        prop.setName("Max Missed Cleavages");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, String.class, "getPeptideChargeStates", null);
        prop.setName("Peptide Charge States");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, Double.class, "getPeptideMassErrorTolerance", null);
        prop.setName("Peptide Mass Error Tolerance");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, String.class, "getPeptideMassErrorToleranceUnit", null);
        prop.setName("Peptide Mass Error Tolerance Unit");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, String.class, "getQuantitation", null);
        prop.setName("Quantitation");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, String.class, "getSoftwareName", null);
        prop.setName("Software Name");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, String.class, "getSoftwareVersion", null);
        prop.setName("Software Version");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(searchSetting, String.class, "getTaxonomy", null);
        prop.setName("Taxonomy");
        propGroup.put(prop);
        
        
                
        return propGroup;
    }
    
    private Sheet.Set createInstrumentConfigSheetSet(InstrumentConfig instrumentConfig) throws NoSuchMethodException {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Instrument Configuration");
        propGroup.setDisplayName("Instrument Configuration");

        Property prop = new PropertySupport.Reflection<>(instrumentConfig, Integer.class, "getId", null);
        prop.setName("Instrument Configuration id");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(instrumentConfig, String.class, "getName", null);
        prop.setName("Name");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(instrumentConfig, String.class, "getMs1Analyzer", null);
        prop.setName("Ms1 Analyzer");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(instrumentConfig, String.class, "getMsnAnalyzer", null);
        prop.setName("Msn Analyzer");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(instrumentConfig, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);
        
        return propGroup;
    }
    
    private Sheet.Set createEnzimeSheetSet(Enzyme enzyme) throws NoSuchMethodException {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Enzyme"+enzyme.getName());
        propGroup.setDisplayName("Enzyme"+enzyme.getName());
        
        Property prop = new PropertySupport.Reflection<>(enzyme, Integer.class, "getId", null);
        prop.setName("Enzyme id");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(enzyme, String.class, "getCleavageRegexp", null);
        prop.setName("Cleavage Regexp");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(enzyme, Boolean.class, "getIsIndependant", null);
        prop.setName("Is Independant");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(enzyme, Boolean.class, "getIsSemiSpecific", null);
        prop.setName("Is Semi Specific");
        propGroup.put(prop);
   
        return propGroup;
    }
    
    private Sheet.Set createSeqDatabaseheetSet(SeqDatabase seqDatabase) throws NoSuchMethodException {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Seq Database" + seqDatabase.getName());
        propGroup.setDisplayName("Seq Database" + seqDatabase.getName());

        Property prop = new PropertySupport.Reflection<>(seqDatabase, Integer.class, "getId", null);
        prop.setName("Seq Database id");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(seqDatabase, String.class, "getFastaFilePath", null);
        prop.setName("Fasta File Path");
        propGroup.put(prop);
        
        Timestamp timeStamp = seqDatabase.getReleaseDate();
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        final String dateFormatted = df.format(timeStamp);
        prop = new PropertySupport.ReadOnly(
                "Release Date",
                String.class,
                "Release Date",
                "Release Date") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return dateFormatted;
            }
        };
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(seqDatabase, Integer.class, "getSequenceCount", null);
        prop.setName("Sequence count");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(seqDatabase, String.class, "getVersion", null);
        prop.setName("Version");
        propGroup.put(prop);
        
        
        
        prop = new PropertySupport.Reflection<>(seqDatabase, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);

        
        return propGroup;
    }
    
    
    private Sheet.Set createResultSummarySheetSet(Dataset dataset) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Identification Summary");
        propGroup.setDisplayName("Identification Summary");
        
        final ResultSummary rsm = dataset.getTransientData().getResultSummary();
        
        Property prop = new PropertySupport.Reflection<>(rsm, Integer.class, "getId", null);
        prop.setName("ResultSummary id");
        propGroup.put(prop);
        
        prop = new PropertySupport.Reflection<>(rsm, String.class, "getDescription", null);
        prop.setName("Description");
        propGroup.put(prop);
        
        prop = new PropertySupport.ReadOnly(
                "Protein Sets Count",
                Integer.class,
                "Protein Sets Count",
                "Protein Sets Count") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return rsm.getTransientData().getNumberOfProteinSet();
            }
        };
        propGroup.put(prop);
        
        /*prop = new PropertySupport.Reflection<>(rsm, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);*/

        prop = new PropertySupport.Reflection<>(rsm, Boolean.class, "getIsQuantified", null);
        prop.setName("Is Quantified");
        propGroup.put(prop);
        
        Timestamp timeStamp = rsm.getModificationTimestamp();
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        final String dateFormatted = df.format(timeStamp);
        prop = new PropertySupport.ReadOnly(
                "Date",
                String.class,
                "Date",
                "Date") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return dateFormatted;
            }
        };
        propGroup.put(prop);
        
        
        
        return propGroup;
    }
}
