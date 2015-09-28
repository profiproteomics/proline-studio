package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.msi.MasterQuantPeptideIon;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptideIonPanel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxXicPeptideIon extends AbstractDataBox {

    private DDataset m_dataset;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private List<MasterQuantPeptideIon> m_masterQuantPeptideIonList;
    private DQuantitationChannel[] quantitationChannelArray = null;

    private QuantChannelInfo m_quantChannelInfo;

    private List<MapAlignment> m_mapAlignments;
    private List<MapAlignment> m_allMapAlignments;
    private List<ProcessedMap> m_allMaps;

    private boolean m_isXICMode = true;

    public DataboxXicPeptideIon() {
        super(DataboxType.DataboxXicPeptideIon);

        // Name of this databox
        m_typeName = "Quanti. Peptides Ions";
        m_description = "All Peptides Ions of a Quanti. Peptide";

        // Register Possible in parameters
        // One Dataset and list of Peptide
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        registerInParameter(inParameter);
        
        inParameter = new GroupParameter();
        inParameter.addParameter(DMasterQuantPeptide.class, false);
        registerInParameter(inParameter);
        

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(MasterQuantPeptideIon.class, false);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(DDataset.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        XicPeptideIonPanel p = new XicPeptideIonPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final boolean allPeptides = m_previousDataBox == null;
        DMasterQuantPeptide oldPeptide = m_masterQuantPeptide;

        if (!allPeptides) {
            m_masterQuantPeptide = (DMasterQuantPeptide) m_previousDataBox.getData(false, DMasterQuantPeptide.class);
            m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);
            if (m_masterQuantPeptide == null || m_masterQuantPeptide.equals(oldPeptide)) {
                return;
            }
            m_isXICMode = (Boolean) m_previousDataBox.getData(false, Boolean.class);
        }
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    if (!allPeptides) {
                        quantitationChannelArray = m_quantChannelInfo.getQuantChannels();
                        ((XicPeptideIonPanel) m_panel).setData(taskId, quantitationChannelArray, m_masterQuantPeptideIonList, m_isXICMode, finished);
                    } else {
                        AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                                // list quant Channels
                                List<DQuantitationChannel> listQuantChannel = new ArrayList();
                                if (m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()) {
                                    DMasterQuantitationChannel masterChannel = m_dataset.getMasterQuantitationChannels().get(0);
                                    listQuantChannel = masterChannel.getQuantitationChannels();
                                }
                                quantitationChannelArray = new DQuantitationChannel[listQuantChannel.size()];
                                listQuantChannel.toArray(quantitationChannelArray);
                                m_quantChannelInfo = new QuantChannelInfo(quantitationChannelArray);
                                m_quantChannelInfo.setAllMapAlignments(m_allMapAlignments);
                                m_quantChannelInfo.setMapAlignments(m_mapAlignments);
                                m_quantChannelInfo.setAllMaps(m_allMaps);

                                ((XicPeptideIonPanel) m_panel).setData(taskId, quantitationChannelArray, m_masterQuantPeptideIonList, m_isXICMode, finished);

                                if (finished) {
                                    unregisterTask(task2Id);
                                }
                            }
                        };
                        // ask asynchronous loading of data
                        m_mapAlignments = new ArrayList();
                        m_allMapAlignments = new ArrayList();
                        m_allMaps = new ArrayList();
                        DatabaseLoadLcMSTask taskMap = new DatabaseLoadLcMSTask(mapCallback);
                        taskMap.initLoadAlignmentForXic(getProjectId(), m_dataset, m_mapAlignments, m_allMapAlignments, m_allMaps);
                        registerTask(taskMap);
                    }

                } else {
                    ((XicPeptideIonPanel) m_panel).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideIonList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        if (allPeptides) {
            task.initLoadPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptideIonList);
        } else {
            task.initLoadPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptide, m_masterQuantPeptideIonList);
        }
        registerTask(task);

    }

    public boolean isXICMode() {
        return m_isXICMode;
    }

    public void setXICMode(boolean isXICMode) {
        this.m_isXICMode = isXICMode;
    }

    @Override
    public void setEntryData(Object data) {
        m_dataset = (DDataset) data;
        dataChanged();
    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(ResultSummary.class)) {
                return m_dataset.getResultSummary();
            }
            if (parameterType.equals(MasterQuantPeptideIon.class)) {
                return ((XicPeptideIonPanel) m_panel).getSelectedMasterQuantPeptideIon();
            }
            if (parameterType.equals(DDataset.class)) {
                return m_dataset;
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getCrossSelectionInterface();
            }
            if (parameterType.equals(QuantChannelInfo.class)) {
                return m_quantChannelInfo;
            }
        }
        return super.getData(getArray, parameterType);
    }

    @Override
    public String getFullName() {
        return m_dataset.getName() + " " + getTypeName();
    }
}
