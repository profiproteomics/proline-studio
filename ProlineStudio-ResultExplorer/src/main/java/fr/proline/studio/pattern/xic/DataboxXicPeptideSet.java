package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinSet;

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
import fr.proline.studio.rsmexplorer.gui.xic.PeptidePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptidePanel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxXicPeptideSet extends AbstractDataBox {

    private DDataset m_dataset;
    private DProteinSet m_proteinSet;
    private DMasterQuantProteinSet m_masterQuantProteinSet;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private QuantChannelInfo m_quantChannelInfo;
    private DQuantitationChannel[] quantitationChannelArray = null;

    private List<MapAlignment> m_mapAlignments;
    private List<MapAlignment> m_allMapAlignments;
    private List<ProcessedMap> m_allMaps;

    private boolean m_isXICMode = true;

    public DataboxXicPeptideSet() {
        super(DataboxType.DataboxXicPeptideSet);

        // Name of this databox
        m_typeName = "Quanti Peptides";
        m_description = "All Quanti. Peptides of a ProteinSet";

        // Register Possible in parameters
        // One Dataset and list of Peptide
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        inParameter.addParameter(DProteinSet.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DMasterQuantPeptide.class, false);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);

    }

    public boolean isXICMode() {
        return m_isXICMode;
    }

    public void setXICMode(boolean isXICMode) {
        this.m_isXICMode = isXICMode;
    }

    @Override
    public void createPanel() {
        XicPeptidePanel p = new XicPeptidePanel(false);
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final boolean allProteinSet = m_previousDataBox == null;
        DProteinSet oldProteinSet = m_proteinSet;

        if (!allProteinSet) {
            m_proteinSet = (DProteinSet) m_previousDataBox.getData(false, DProteinSet.class);
            m_masterQuantProteinSet = (DMasterQuantProteinSet) m_previousDataBox.getData(false, DMasterQuantProteinSet.class);
            m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);
            if (m_proteinSet == null || m_proteinSet.equals(oldProteinSet)) {
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

                    if (!allProteinSet) {
                        quantitationChannelArray = m_quantChannelInfo.getQuantChannels();
                        ((XicPeptidePanel) m_panel).setData(taskId, m_proteinSet != null, quantitationChannelArray, m_masterQuantPeptideList, m_isXICMode, finished);
                         
                    
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

                                ((XicPeptidePanel) m_panel).setData(taskId, m_proteinSet != null, quantitationChannelArray, m_masterQuantPeptideList, m_isXICMode, finished);
                                if (!allProteinSet) {
                                    getPeptideTableModelList();
                                }
                    
                                if (finished) {
                                    unregisterTask(task2Id);
                                    propagateDataChanged(CompareDataInterface.class); 
                                }
                            }
                        };
                        // ask asynchronous loading of data
                        m_mapAlignments = new ArrayList();
                        m_allMapAlignments = new ArrayList();
                        m_allMaps = new ArrayList();
                        DatabaseLoadLcMSTask maptask = new DatabaseLoadLcMSTask(mapCallback);
                        maptask.initLoadAlignmentForXic(getProjectId(), m_dataset, m_mapAlignments, m_allMapAlignments, m_allMaps);
                        registerTask(maptask);
                    }

                } else {
                    ((XicPeptidePanel) m_panel).dataUpdated(subTask, finished);
                }
                if (!allProteinSet) {
                    getPeptideTableModelList();
                }
                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        if (allProteinSet) {
            task.initLoadPeptides(getProjectId(), m_dataset, m_masterQuantPeptideList);
        } else {
            task.initLoadPeptides(getProjectId(), m_dataset, m_proteinSet, m_masterQuantProteinSet, m_masterQuantPeptideList);
        }
        registerTask(task);

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
            if (parameterType.equals(DMasterQuantPeptide.class)) {
                return ((XicPeptidePanel) m_panel).getSelectedMasterQuantPeptide();
            }
            if (parameterType.equals(DDataset.class)) {
                return m_dataset;
            }
            if (parameterType.equals(QuantChannelInfo.class)) {
                return m_quantChannelInfo;
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getCrossSelectionInterface();
            }
            if (parameterType.equals(Boolean.class)) {
                return isXICMode();
            }
        }
        return super.getData(getArray, parameterType);
    }

    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        if (parameterType != null && isList) {
            if (parameterType.equals(CompareDataInterface.class)) {
                return getCompareDataInterfaceList();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return getCrossSelectionInterfaceList();
            }
        }
        return super.getData(getArray, parameterType, isList);
    }

    @Override
    public String getFullName() {
        return m_dataset.getName() + " " + getTypeName();
    }

    private List<CompareDataInterface> getCompareDataInterfaceList() {
        List<CompareDataInterface> listCDI = new ArrayList();
        List<PeptidePanel> listPeptidePanel = getPeptideTableModelList();
        for (PeptidePanel peptidePanel : listPeptidePanel) {
            listCDI.add(peptidePanel.getGlobalTableModelInterface());
        }
        return listCDI;
    }

    private List<CrossSelectionInterface> getCrossSelectionInterfaceList() {
        List<CrossSelectionInterface> listCSI = new ArrayList();
        List<PeptidePanel> listPeptidePanel = getPeptideTableModelList();
        for (PeptidePanel peptidePanel : listPeptidePanel) {
            listCSI.add(peptidePanel.getCrossSelectionInterface());
        }
        return listCSI;
    }

    private List<PeptidePanel> getPeptideTableModelList() {
        List<PeptidePanel> list = new ArrayList();
        if (m_masterQuantPeptideList != null){
            // one table model per row
            for (DMasterQuantPeptide quantPeptide : m_masterQuantPeptideList) {
                PeptidePanel aPepPanel = new PeptidePanel();
                aPepPanel.setData(quantitationChannelArray, quantPeptide, m_isXICMode);
                list.add(aPepPanel);
            }
        }
        return list;
    }
}
