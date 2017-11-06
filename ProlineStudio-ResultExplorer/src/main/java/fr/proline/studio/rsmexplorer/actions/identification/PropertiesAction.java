package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.QuantitationLabel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.pattern.DataboxGeneric;
import fr.proline.studio.rsmexplorer.gui.model.properties.IdentificationPropertiesTableModel;
import java.util.*;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.GenericPanel;
import fr.proline.studio.rsmexplorer.gui.model.properties.AbstractPropertiesTableModel;
import fr.proline.studio.rsmexplorer.gui.model.properties.XICPropertiesTableModel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.SerializedPropertiesUtil;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.DateFormat;
import org.hibernate.LazyInitializationException;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
import org.slf4j.LoggerFactory;

/**
 * Display of the properties of a rset and rsm
 *
 * @author JM235353
 */


public class PropertiesAction extends AbstractRSMAction {

    public PropertiesAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(PropertiesAction.class, "CTL_PropertiesAction"), treeType);
    }

    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {

        //String dialogName;
        String name = "";
        int nbSelectedNodes = selectedNodes.length;
        if (nbSelectedNodes == 1) {
            AbstractNode firstNode = selectedNodes[0];
            name = firstNode.getData().getName();
        }

        
        boolean identificationProperties = false;
        boolean isIdentificationTree = isIdentificationTree();
        
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            AbstractNode.NodeTypes type = node.getType();

            if ((type == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) || ((type == AbstractNode.NodeTypes.DATA_SET) && (isIdentificationTree)) || (type == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
                identificationProperties = true;
 
            }

        }

        // new Properties window
        AbstractPropertiesTableModel model = null;

        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(name, "Properties", IconManager.IconType.DOCUMENT_LIST, true);
        model = identificationProperties ? new IdentificationPropertiesTableModel() : new XICPropertiesTableModel();
        windowBox.setEntryData(-1l, model);
        DataBoxViewerTopComponent win2 = new DataBoxViewerTopComponent(windowBox);
        win2.open();
        win2.requestActive();

        //JPM.HACK ! Impossible to set the max number of lines differently in this case
        DataboxGeneric databoxGeneric = ((DataboxGeneric) windowBox.getEntryBox());
        GenericPanel genericPanel = (GenericPanel) databoxGeneric.getPanel();

        final AbstractPropertiesTableModel _model = model;
        final GenericPanel _genericPanel = genericPanel;

        final int loadingId = databoxGeneric.setLoading();
        
        // load data for properties
        final DataLoadedCallback dataLoadedCallback = new DataLoadedCallback(selectedNodes.length) {

            @Override
            public void run() {
                m_nbDataToLoad--;
                if (m_nbDataToLoad == 0) {

                    databoxGeneric.setLoaded(loadingId);
                    
                    ArrayList<DDataset> datasetList = new ArrayList<>();
                    for (AbstractNode node : selectedNodes) {
                        if (node instanceof DataSetNode) {
                            DDataset dataset = ((DataSetNode) node).getDataset();
                            datasetList.add(dataset);
                        }
                    }

                    if (_model != null) {
                        _model.setData(datasetList);
                        _genericPanel.setMaxLineNumber(_model.getRowCount());
                    }

                }
            }

        };

        int nbDataToLoad = selectedNodes.length;
        for (int i = 0; i < nbDataToLoad; i++) {
            selectedNodes[i].loadDataForProperties(dataLoadedCallback);
        }

    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // properties action is enabled only if selected nodes
        // are of the same type and are of type PROJECT or DATA_SET
        AbstractNode.NodeTypes currentType = null;
        boolean identificationProperties = false;
        boolean quantitationProperties = false;
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            // node is being created, we can not show properties
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            AbstractNode.NodeTypes type = node.getType();
            if ((currentType != null) && (currentType != type)) {
                setEnabled(false);
                return;
            }
            if (((type != AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) || (type != AbstractNode.NodeTypes.PROJECT_QUANTITATION)) && (type != AbstractNode.NodeTypes.DATA_SET) && (type != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
                setEnabled(false);
                return;
            }

            // one can be the trash
            if (((DataSetNode) node).isTrash() || ((DataSetNode) node).isFolder()) {
                setEnabled(false);
                return;
            }

            if ((type == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) || (type == AbstractNode.NodeTypes.DATA_SET)) {
                DataSetNode datasetNode = (DataSetNode) node;
                identificationProperties = true;
                if (!datasetNode.hasResultSet() && !datasetNode.hasResultSummary()) {
                    setEnabled(false);
                    return;
                }
            }

            if ((type == AbstractNode.NodeTypes.PROJECT_QUANTITATION) || (type == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
                DataSetNode datasetNode = (DataSetNode) node;
                quantitationProperties = true;
                if (!datasetNode.hasResultSet() && !datasetNode.hasResultSummary()) {
                    setEnabled(false);
                    return;
                }
            }

            currentType = type;
        }
        
        if (identificationProperties && quantitationProperties) {
            // can not display combined property for identification and quantitation
            setEnabled(false);
            return;
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

    public static Sheet createSheet(DDataset dataset) {
        ResultSet rset = dataset.getResultSet();
        ResultSummary rsm = dataset.getResultSummary();
        QuantitationMethod quantMethod = dataset.getQuantitationMethod();
        if (quantMethod != null) {
            return createSheet(dataset, quantMethod);
        }
        return createSheet(dataset, rset, rsm);
    }

    public static Sheet createSheet(DDataset dataset, QuantitationMethod quantMethod) {

        Sheet sheet = Sheet.createDefault();

        try {

            if (dataset != null) {
                Sheet.Set dsSet = createDataSetSheetSet(dataset);
                Property prop = new PropertySupport.Reflection<>(dataset, String.class, "getDescription", null);
                prop.setName("description");
                dsSet.put(prop);

                sheet.put(dsSet);

                if (dataset.getResultSummary() != null) {
                    sheet.put(createResultSummarySheetSet(dataset.getResultSummary()));
                    try {
                        Map<String, Object> map = dataset.getResultSummary().getSerializedPropertiesAsMap();
                        SerializedPropertiesUtil.getProperties(sheet, "SR Properties", map);
                    } catch (Exception e) {
                        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
                    }
                }

                // post quant processing config
                if (dataset.getQuantProcessingConfig() != null) {
                    try {
                        createObjectTreeQuantiConfig(sheet, false, dataset.getQuantProcessingConfig(), dataset.getQuantProcessingConfigAsMap());
                    } catch (Exception ex) {
                        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", ex);
                    }
                }

                // post quant processing config
                if (dataset.getPostQuantProcessingConfig() != null) {
                    try {
                        createObjectTreeQuantiConfig(sheet, true, dataset.getPostQuantProcessingConfig(), dataset.getPostQuantProcessingConfigAsMap());
                    } catch (Exception ex) {
                        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", ex);
                    }
                }

            }

            if (quantMethod != null) {
                sheet.put(createQuantitationMethodSheetSet(quantMethod));
                // quantitation labels
                try {
                    Set<QuantitationLabel> labelsSet = quantMethod.getLabels();
                    Iterator<QuantitationLabel> it = labelsSet.iterator();
                    while (it.hasNext()) {
                        sheet.put(createQuantitationLabelSheetSet(it.next()));
                    }
                } catch (LazyInitializationException e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
                }

            }

            if (dataset.getMasterQuantitationChannels() != null && !dataset.getMasterQuantitationChannels().isEmpty()) {
                for (Iterator<DMasterQuantitationChannel> it = dataset.getMasterQuantitationChannels().iterator(); it.hasNext();) {
                    DMasterQuantitationChannel masterQuantitationChannel = it.next();
                    Sheet.Set masterSet = createMasterQuantitationChannelSheetSet(masterQuantitationChannel);
                    if (masterQuantitationChannel.getIdentDataset() != null) {
                        Property prop = new PropertySupport.Reflection<>(masterQuantitationChannel.getIdentDataset(), String.class, "getName", null);
                        prop.setName("Dataset name");
                        masterSet.put(prop);
                    }
                    sheet.put(masterSet);

                    // quantitation channels
                    try {
                        List<DQuantitationChannel> channels = masterQuantitationChannel.getQuantitationChannels();
                        int id = 1;
                        for (Iterator<DQuantitationChannel> itChannel = channels.iterator(); itChannel.hasNext();) {
                            QuantitationChannel quantitationChannel = itChannel.next();
                            sheet.put(createQuantitationChannelSheetSet(quantitationChannel, id));
                            if (quantitationChannel.getBiologicalSample() != null) {
                                sheet.put(createBiologicalSampleSheetSet(quantitationChannel.getBiologicalSample(), id));
                            }
                            id++;
                        }
                    } catch (LazyInitializationException e) {
                        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
                    }
                }
            }

        } catch (NoSuchMethodException e) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
        }

        return sheet;
    }

    public static Sheet createSheet(DDataset dataset, ResultSet rset, ResultSummary rsm) {

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

            if (rset != null) {

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

    private static Sheet.Set createDataSetSheetSet(final DDataset dataset) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Data Set");
        propGroup.setDisplayName("Data Set");

        Property prop = new PropertySupport.Reflection<>(dataset, Long.class, "getId", null);
        prop.setName("id");
        propGroup.put(prop);

        return propGroup;
    }

    private static Sheet.Set createQuantitationLabelSheetSet(final QuantitationLabel quantLabel) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = "Quantitation Label";
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        Property prop = new PropertySupport.Reflection<>(quantLabel, Long.class, "getId", null);
        prop.setName("QuantitationLabel id");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantLabel, String.class, "getName", null);
        prop.setName("Name");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantLabel, String.class, "getType", null);
        prop.setName("Type");
        propGroup.put(prop);

        /* prop = new PropertySupport.Reflection<>(quantLabel, String.class, "getSerializedProperties", null);
         prop.setName("Serialized Properties");
         propGroup.put(prop); */
        return propGroup;
    }

    private static Sheet.Set createMasterQuantitationChannelSheetSet(final DMasterQuantitationChannel masterQuantChannel) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = "Master Quantitation Channel";
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        Property prop = new PropertySupport.Reflection<>(masterQuantChannel, Long.class, "getId", null);
        prop.setName("MasterQuantitationChannel id");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(masterQuantChannel, String.class, "getName", null);
        prop.setName("Name");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(masterQuantChannel, String.class, "getSerializedProperties", null);
        prop.setName("Serialized Properties");
        propGroup.put(prop);

        return propGroup;
    }

    private static Sheet.Set createQuantitationChannelSheetSet(final QuantitationChannel quantChannel, int index) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = "Quantitation Channel " + index;
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        Property prop = new PropertySupport.Reflection<>(quantChannel, Long.class, "getId", null);
        prop.setName("QuantitationChannel id");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantChannel, String.class, "getResultFileName", null);
        prop.setName("Result File Name");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantChannel, String.class, "getRawFilePath", null);
        prop.setName("Raw File Path");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantChannel, String.class, "getMzdbFileName", null);
        prop.setName("Mzdb Raw File Name");
        propGroup.put(prop);

        /*prop = new PropertySupport.Reflection<>(quantChannel, String.class, "getSerializedProperties", null);
         prop.setName("Serialized Properties");
         propGroup.put(prop); */
        prop = new PropertySupport.Reflection<>(quantChannel, Long.class, "getIdentResultSummaryId", null);
        prop.setName("identResultSummaryId");
        propGroup.put(prop);

        return propGroup;
    }

    private static Sheet.Set createBiologicalSampleSheetSet(final BiologicalSample biologicalSample, int index) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = "Biological Sample " + index;
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        Property prop = new PropertySupport.Reflection<>(biologicalSample, Long.class, "getId", null);
        prop.setName("Biological Sample id");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(biologicalSample, String.class, "getName", null);
        prop.setName("Name");
        propGroup.put(prop);

        return propGroup;
    }

    private static Sheet.Set createQuantitationMethodSheetSet(final QuantitationMethod quantMethod) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = "Quantitation Method";
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        Property prop = new PropertySupport.Reflection<>(quantMethod, Long.class, "getId", null);
        prop.setName("QuantitationMethod id");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantMethod, String.class, "getName", null);
        prop.setName("Name");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantMethod, String.class, "getType", null);
        prop.setName("Type");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(quantMethod, String.class, "getAbundanceUnit", null);
        prop.setName("Abundance Unit");
        propGroup.put(prop);

        /* prop = new PropertySupport.Reflection<>(quantMethod, String.class, "getSerializedProperties", null);
         prop.setName("Serialized Properties");
         propGroup.put(prop); */
        return propGroup;
    }

    private static Sheet.Set createResultSetSheetSet(final ResultSet rset, boolean decoy) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = decoy ? "Decoy Search Result" : "Search Result";
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        Property prop = new PropertySupport.Reflection<>(rset, Long.class, "getId", null);
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
         public Object getRowValue() throws InvocationTargetException {
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

        Property prop = new PropertySupport.Reflection<>(msiSearch, Long.class, "getId", null);
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

        return propGroup;
    }

    private static Sheet.Set createPeakListSheetSet(Peaklist peaklist) throws NoSuchMethodException {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Peaklist");
        propGroup.setDisplayName("Peaklist");

        Property prop = new PropertySupport.Reflection<>(peaklist, Long.class, "getId", null);
        prop.setName("Peaklist id");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(peaklist, Integer.class, "getMsLevel", null);
        prop.setName("MS Level");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(peaklist, String.class, "getPath", null);
        prop.setName("Path");
        propGroup.put(prop);

        prop = new PropertySupport.Reflection<>(peaklist, String.class, "getRawFileIdentifier", null);
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

        Property prop = new PropertySupport.Reflection<>(peaklistSoftware, Long.class, "getId", null);
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

        Property prop = new PropertySupport.Reflection<>(searchSetting, Long.class, "getId", null);
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

        if (searchSetting instanceof MsmsSearch) {
            MsmsSearch msmsSearch = (MsmsSearch) searchSetting;

            prop = new PropertySupport.Reflection<>(msmsSearch, String.class, "getFragmentChargeStates", null);
            prop.setName("Fragment Charge States");
            propGroup.put(prop);

            prop = new PropertySupport.Reflection<>(msmsSearch, Double.class, "getFragmentMassErrorTolerance", null);
            prop.setName("Fragment Mass Error Tolerance");
            propGroup.put(prop);

            prop = new PropertySupport.Reflection<>(msmsSearch, String.class, "getFragmentMassErrorToleranceUnit", null);
            prop.setName("Fragment Mass Error Tolerance Unit");
            propGroup.put(prop);

        }

        return propGroup;
    }

    private static Sheet.Set createInstrumentConfigSheetSet(InstrumentConfig instrumentConfig) throws NoSuchMethodException {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Instrument Configuration");
        propGroup.setDisplayName("Instrument Configuration");

        Property prop = new PropertySupport.Reflection<>(instrumentConfig, Long.class, "getId", null);
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
        propGroup.setName("Enzyme" + enzyme.getName());
        propGroup.setDisplayName("Enzyme" + enzyme.getName());

        Property prop = new PropertySupport.Reflection<>(enzyme, Long.class, "getId", null);
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

        Property prop = new PropertySupport.Reflection<>(seqDatabase, Long.class, "getId", null);
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

        Property prop = new PropertySupport.Reflection<>(rsm, Long.class, "getId", null);
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

    private static Sheet.Set createObjectTreeQuantiConfig(final Sheet sheet, final boolean isPost, final fr.proline.core.orm.uds.ObjectTree objectTree, Map<String, Object> objectTreeAsMap) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        String name = "Quant Processing Config";
        if (isPost) {
            name = "Post Quant Processing Config";
        }
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        // not yet displayed
        Property prop = new PropertySupport.Reflection<>(objectTree, Long.class, "getId", null);
        prop.setName("ObjectTree id");
        propGroup.put(prop);

        try {
            SerializedPropertiesUtil.getProperties(sheet, name, objectTreeAsMap);
        } catch (Exception e) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(PropertiesAction.class.getSimpleName() + " properties error ", e);
        }
        /*
         prop = new PropertySupport.Reflection<>(objectTree, String.class, "getClobData", null);
         prop.setName("Config");
         propGroup.put(prop);
         */

        return propGroup;
    }

}
