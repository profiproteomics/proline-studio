package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.MasterQuantPeptideIon;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;

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
    private List<MasterQuantPeptideIon> m_masterQuantPeptideIonList ;
    private DQuantitationChannel[] quantitationChannelArray = null;
    
    public DataboxXicPeptideIon() { 
        super(DataboxType.DataboxXicPeptideIon);
        
        // Name of this databox
        m_name = "XIC Peptides Ions";
        m_description = "All Peptides Ions of a XIC";

        // Register Possible in parameters
        // One Dataset and list of Peptide
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false); 
        inParameter.addParameter(DMasterQuantPeptide.class, false); 
        registerInParameter(inParameter);


        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(MasterQuantPeptideIon.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

    }
    
     @Override
    public void createPanel() {
        XicPeptideIonPanel p = new XicPeptideIonPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        boolean allPeptides = m_previousDataBox == null;
        DMasterQuantPeptide oldPeptide = m_masterQuantPeptide;
        
        if (!allPeptides) {
            m_masterQuantPeptide = (DMasterQuantPeptide) m_previousDataBox.getData(false, DMasterQuantPeptide.class);
            m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
            if (m_masterQuantPeptide == null || m_masterQuantPeptide.equals(oldPeptide)) {
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
                    ((XicPeptideIonPanel) m_panel).setData(taskId, quantitationChannelArray, m_masterQuantPeptideIonList, finished);
                    if (qcChanged) {
                        ((XicPeptideIonPanel) m_panel).setColumnsVisibility();
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
        }else {
            task.initLoadPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptide, m_masterQuantPeptideIonList);
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
            if (parameterType.equals(MasterQuantPeptideIon.class)) {
                return ((XicPeptideIonPanel) m_panel).getSelectedMasterQuantPeptideIon();
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
