package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.studio.pattern.*;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptideMatchPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Load the PSM of a Peptide
 *
 * @author JM235353
 */
public class DataboxPSMOfMasterQuantPeptide extends AbstractDataBox {

    private boolean m_finishedLoading = false;

    private DDataset m_dataset;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private DQuantitationChannel[] quantitationChannelArray = null;
    private Map<Long, List<Long>> m_peptideMatchIdListPerQC;
    private List<DPeptideMatch> m_peptideMatchList;
    private QuantChannelInfo m_quantChannelInfo;

    public DataboxPSMOfMasterQuantPeptide() {
        super(DataboxType.DataboxPSMOfMasterQuantPeptide);

        // Name of this databox
        m_name = "PSM / XIC Peptide";
        m_description = "All PSM of a XIC Peptide";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DMasterQuantPeptide.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        XicPeptideMatchPanel p = new XicPeptideMatchPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {

        m_masterQuantPeptide = (DMasterQuantPeptide) m_previousDataBox.getData(false, DMasterQuantPeptide.class);
        m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
        m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);

        if (m_masterQuantPeptide == null) {
            ((XicPeptideMatchPanel) m_panel).setData((long)-1, null, m_quantChannelInfo, null, null, true);
            return;
        }
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                // list quant Channels
                List<DQuantitationChannel> listQuantChannel = new ArrayList();
                if (m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()) {
                    DMasterQuantitationChannel masterChannel = m_dataset.getMasterQuantitationChannels().get(0);
                    listQuantChannel = masterChannel.getQuantitationChannels();
                }
                quantitationChannelArray = new DQuantitationChannel[listQuantChannel.size()];
                listQuantChannel.toArray(quantitationChannelArray);

                if (subTask == null) {
                    ((XicPeptideMatchPanel) m_panel).setData(taskId, quantitationChannelArray, m_quantChannelInfo, m_peptideMatchList, m_peptideMatchIdListPerQC, finished);
                } else {
                    ((XicPeptideMatchPanel) m_panel).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        m_peptideMatchList = new ArrayList();
        m_peptideMatchIdListPerQC = new HashMap<>();
        // ask asynchronous loading of data
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadPSMForPeptide(getProjectId(), m_dataset, m_masterQuantPeptide, m_peptideMatchList, m_peptideMatchIdListPerQC);

        registerTask(task);

    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(DPeptideMatch.class)) {
                return ((XicPeptideMatchPanel) m_panel).getSelectedPSM();
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((CompareDataProviderInterface) m_panel).getCompareDataInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((CompareDataProviderInterface) m_panel).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }

}
