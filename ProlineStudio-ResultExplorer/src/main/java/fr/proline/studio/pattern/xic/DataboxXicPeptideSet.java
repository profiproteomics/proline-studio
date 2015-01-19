package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinSet;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
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
    private List<DMasterQuantPeptide> m_masterQuantPeptideList ;
    private QuantChannelInfo m_quantChannelInfo;
    private  DQuantitationChannel[] quantitationChannelArray = null;
    
    public DataboxXicPeptideSet() { 
        super(DataboxType.DataboxXicPeptideSet);
        
        // Name of this databox
        m_name = "XIC Peptides";
        m_description = "All Peptides of a XIC";

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

    }
    
     @Override
    public void createPanel() {
        XicPeptidePanel p = new XicPeptidePanel();
        p.setName(m_name);
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
        }
        
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    // list quant Channels
                    List<DQuantitationChannel> listQuantChannel = new ArrayList();
                    if (m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()) {
                        DMasterQuantitationChannel masterChannel = m_dataset.getMasterQuantitationChannels().get(0);
                        listQuantChannel = masterChannel.getQuantitationChannels();
                    }
                    boolean qcChanged = true;
                    if (quantitationChannelArray != null && quantitationChannelArray.length == listQuantChannel.size()) {
                        for (int q=0; q<quantitationChannelArray.length; q++) {
                            qcChanged =  !(quantitationChannelArray[q].equals(listQuantChannel.get(q))) ;
                        }
                    }
                    quantitationChannelArray = new DQuantitationChannel[listQuantChannel.size()];
                    listQuantChannel.toArray(quantitationChannelArray);
                    m_quantChannelInfo = new QuantChannelInfo(quantitationChannelArray);
                    ((XicPeptidePanel) m_panel).setData(taskId, m_proteinSet != null, quantitationChannelArray, m_masterQuantPeptideList, finished);
                    if (qcChanged) {
                        ((XicPeptidePanel) m_panel).setColumnsVisibility();
                    }
                } else {
                    ((XicPeptidePanel) m_panel).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        if (allProteinSet) {
            task.initLoadPeptides(getProjectId(), m_dataset, m_masterQuantPeptideList);
        }else {
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
                return ((CompareDataProviderInterface) m_panel).getCompareDataInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((CompareDataProviderInterface)m_panel).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
   
    @Override
    public String getFullName() {
        return m_dataset.getName()+" "+getName();
    }
}
