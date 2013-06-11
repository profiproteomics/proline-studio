package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import fr.proline.studio.dam.tasks.DatabaseRsetProperties;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.PropertiesTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.utils.PropertiesProviderInterface;
import fr.proline.studio.utils.SerializedPropertiesUtil;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
import org.slf4j.LoggerFactory;


/**
 *
 * @author JM235353
 */
public class PropertiesAction extends AbstractRSMAction {

    public PropertiesAction() {
        super(NbBundle.getMessage(PropertiesAction.class, "CTL_PropertiesAction"));
    }

    @Override
    public void actionPerformed(final RSMNode[] selectedNodes, int x, int y) {

         String dialogName;
         if (selectedNodes.length == 1) {
             RSMNode firstNode = selectedNodes[0];
             String name = firstNode.getData().getName();
             dialogName = "Properties : " + name;
         } else {
             dialogName = "Properties";
         }
         
         final PropertiesTopComponent win = new PropertiesTopComponent(dialogName);
         win.open();
         win.requestActive();
         
         // load data for properties
         DataLoadedCallback dataLoadedCallback = new DataLoadedCallback(selectedNodes.length) {

            @Override
            public void run() {
                m_nbDataToLoad--;
                if (m_nbDataToLoad == 0) {

                    win.setProperties(selectedNodes);


                }
            }
             
         };
         
         int nbDataToLoad = selectedNodes.length;
         for (int i=0;i<nbDataToLoad;i++) {
             selectedNodes[i].loadDataForProperties(dataLoadedCallback);
         }
         
    }
    

    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        
        
        // properties action is enabled only if selected nodes
        // are of the same type and are of type PROJECT or DATA_SET
        RSMNode.NodeTypes currentType = null;
        for (int i=0;i<nbSelectedNodes;i++) {
            RSMNode node = selectedNodes[i];
            
            // node is being created, we can not show properties
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }
            
            RSMNode.NodeTypes type = node.getType();
            if ((currentType != null) && (currentType != type)) {
                setEnabled(false);
                return;
            }
            if ((type!=RSMNode.NodeTypes.PROJECT) && (type!=RSMNode.NodeTypes.DATA_SET)) {
                setEnabled(false);
                return;
            }
            
            currentType = type;
        }
        


        setEnabled(true);


    }


    
    public abstract class DataLoadedCallback implements Runnable {

        protected int m_nbDataToLoad = 0;
        
        public DataLoadedCallback(int nb) {
            m_nbDataToLoad = nb;
        }
        
        @Override
        public abstract void run();
        
    }
    
    public static Sheet createSheet(Dataset dataset) {
        ResultSet rset = dataset.getTransientData().getResultSet();
        ResultSummary rsm = dataset.getTransientData().getResultSummary();
        return createSheet(dataset, rset, rsm);
    }

    public static Sheet createSheet(Dataset dataset, ResultSet rset, ResultSummary rsm) {

        Sheet sheet = Sheet.createDefault();

        try {

            if (dataset != null) {
                sheet.put(createDataSetSheetSet(dataset));
            }

            
            if (rsm != null) {
                sheet.put(createResultSummarySheetSet(rsm));
                try {
                    Map<String, Object> map = rsm.getSerializedPropertiesAsMap();
                    SerializedPropertiesUtil.getProperties(sheet, "SR Properties", map);
                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
                }
            }

            
            if (rset!=null) {
                
                
                sheet.put(createResultSetSheetSet(rset, false));
                try {
                    Map<String, Object> map = rset.getSerializedPropertiesAsMap();
                    SerializedPropertiesUtil.getProperties(sheet, "IS Properties", map);
                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
                }
                
                ResultSet rsetDecoy = rset.getDecoyResultSet();
                if (rsetDecoy != null) {
                    sheet.put(createResultSetSheetSet(rsetDecoy, true));
                    try {
                        Map<String, Object> map = rsetDecoy.getSerializedPropertiesAsMap();
                        SerializedPropertiesUtil.getProperties(sheet, "Decoy Ret Properties", map);
                    } catch (Exception e) {
                        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
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
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
        }
        

        
        return sheet;
    }
    
    private static Sheet.Set createDataSetSheetSet(final Dataset dataset) throws NoSuchMethodException {
            Sheet.Set propGroup = Sheet.createPropertiesSet();
            propGroup.setName("Data Set");
            propGroup.setDisplayName("Data Set");

            Property prop = new PropertySupport.Reflection<>(dataset, Integer.class, "getId", null);
            prop.setName("id");
            propGroup.put(prop);
            
            return propGroup;
    }
    
    private static Sheet.Set createResultSetSheetSet(final ResultSet rset, boolean decoy) throws NoSuchMethodException {
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

        /*prop = new PropertySupport.ReadOnly(
                "Assigned Queries Count",
                Integer.class,
                "Assigned Queries Count",
                "Assigned Queries Count") {

            @Override
            public Object getValue() throws InvocationTargetException {
                return rset.getTransientData().getMSQueriesCount();
            }
        };
        propGroup.put(prop);*/
        
        prop = new PropertySupport.ReadOnly(
                "PSM Count",
                Integer.class,
                "PSM Count",
                "PSM Count") {

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
    
    private static Sheet.Set createMsiSearchSheetSet(MsiSearch msiSearch) throws NoSuchMethodException {
        
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
    
    private static Sheet.Set createPeakListSheetSet(Peaklist peaklist) throws NoSuchMethodException {

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
    
    private static Sheet.Set createPeakListSoftwareSheetSet(PeaklistSoftware peaklistSoftware) throws NoSuchMethodException {

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
    
    private static Sheet.Set createSearchSettingSheetSet(SearchSetting searchSetting) throws NoSuchMethodException {
        
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
    
    private static Sheet.Set createInstrumentConfigSheetSet(InstrumentConfig instrumentConfig) throws NoSuchMethodException {

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
    
    private static Sheet.Set createEnzimeSheetSet(Enzyme enzyme) throws NoSuchMethodException {

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
    
    private static Sheet.Set createSeqDatabaseheetSet(SeqDatabase seqDatabase) throws NoSuchMethodException {

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
    
    
    private static Sheet.Set createResultSummarySheetSet(final ResultSummary rsm) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Identification Summary");
        propGroup.setDisplayName("Identification Summary");

        
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