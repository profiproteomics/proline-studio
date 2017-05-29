/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMProteinSiteTask_V2;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.PTMSite;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.PTMProteinSitePanel_V2;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSitePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.types.XicMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author VD225637
 */
public class DataBoxXICPTMProteinSite extends AbstractDataBox {

    private DDataset m_dataset;
    private ResultSummary m_rsm;
    private Map<Long,DMasterQuantProteinSet> m_masterQuantProtMatchByIdProtMatchId;
     
    public DataBoxXICPTMProteinSite(){
        super(DataboxType.DataBoxXICPTMProteinSite, DataboxStyle.STYLE_XIC);
        
        // Name of this databox
        m_typeName = "Quanti PTM ProteinSite";
        m_description = "All Quanti. PTM ProteinSite";
        
        // Register Possible in parameters
        // One Dataset and list of Peptide
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
//        outParameter.addParameter(DMasterQuantPeptide.class, false);
//        outParameter.addParameter(XicMode.class, false);
        outParameter.addParameter(DDataset.class, false);
        outParameter.addParameter(ResultSummary.class, false);
//        outParameter.addParameter(QuantChannelInfo.class, false);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(DProteinMatch.class, true); 
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);
    }
    
    
    @Override
    public Long getRsetId() {
        if (m_dataset == null) {
            return null;
        }
        return m_dataset.getResultSetId();
    }

    @Override
    public Long getRsmId() {
        if (m_dataset == null) {
            return null;
        }
        return m_dataset.getResultSummaryId();
    }
    
    @Override
    public void createPanel() {       
        PTMProteinSitePanel_V2 p = new PTMProteinSitePanel_V2();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);        
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();

        final ArrayList<PTMSite> proteinPTMSiteArray = new ArrayList<>();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                loadXicData(taskId, proteinPTMSiteArray,finished );
                setLoaded(loadingId);
                
                if (finished) {
                    if(m_previousTaskId != null && m_previousTaskId.equals(taskId))
                        m_previousTaskId = null; // Reset PreviousTask. Was finished ! 
                    
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };


        // ask asynchronous loading of data

        DatabasePTMProteinSiteTask_V2 task = new DatabasePTMProteinSiteTask_V2(callback, getProjectId(), m_rsm, proteinPTMSiteArray);
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);

    }
    private Long m_previousTaskId = null;

  
    //Load XIC Information    
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    private DQuantitationChannel[] m_quantitationChannelArray = null;
    private List<MapAlignment> m_mapAlignments;
    private List<MapAlignment> m_allMapAlignments;
    private List<ProcessedMap> m_allMaps;
    private QuantChannelInfo m_quantChannelInfo;

    public void loadXicData(long taskId, ArrayList<PTMSite> proteinPTMSiteArray, boolean finished) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                if (subTask == null) {

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
                            m_quantitationChannelArray = new DQuantitationChannel[listQuantChannel.size()];
                            listQuantChannel.toArray(m_quantitationChannelArray);
                            m_quantChannelInfo = new QuantChannelInfo(m_quantitationChannelArray);
                            m_quantChannelInfo.setAllMapAlignments(m_allMapAlignments);
                            m_quantChannelInfo.setMapAlignments(m_mapAlignments);
                            m_quantChannelInfo.setAllMaps(m_allMaps);
                            getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);

                            // proteins set 
//                            ((XicProteinSetPanel) getDataBoxPanelInterface()).setData(taskId, m_quantitationChannelArray, m_masterQuantProteinSetList, true, finished);
                            
                            if (finished) {
                                m_masterQuantProtMatchByIdProtMatchId = m_masterQuantProteinSetList.stream().collect(Collectors.toMap(mqPS -> mqPS.getProteinSetId(), mqPS->mqPS));
                                                                                
                                ((PTMProteinSitePanel_V2) getDataBoxPanelInterface()).setData(taskId, proteinPTMSiteArray, finished);         
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

                } else {
                   //TODO =>  ((XicProteinSetPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }

                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantProteinSetList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadProteinSets(getProjectId(), m_dataset, m_masterQuantProteinSetList);
        registerTask(task);

    }
    
    
    
    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;
        m_rsm = m_dataset.getResultSummary();
        dataChanged();
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(ResultSummary.class)) {
                return m_rsm;
            }
            
            if (parameterType.equals(DProteinMatch.class)) {
                PTMSite proteinPtmSite = ((PTMProteinSitePanel_V2)getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getProteinMatch();
                }
            }
            if (parameterType.equals(DPeptideMatch.class)) {
                PTMSite proteinPtmSite = ((PTMProteinSitePanel_V2) getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getBestPeptideMatch();
                }
            }
            if (parameterType.equals(PTMSite.class)) {
                return ((PTMProteinSitePanel_V2) getDataBoxPanelInterface()).getSelectedProteinPTMSite();
            }            
            
            if(parameterType.equals(DMasterQuantProteinSet.class)) {
                PTMSite proteinPtmSite = ((PTMProteinSitePanel_V2)getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return m_masterQuantProtMatchByIdProtMatchId.get(proteinPtmSite.getProteinMatch().getId());
                }
            }
                        
//            if (parameterType.equals(DMasterQuantPeptide.class)) {
//                return ((XicPeptidePanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptide();
//            }
//            if (parameterType.equals(DPeptideMatch.class)) {
//                DMasterQuantPeptide mqp = ((XicPeptidePanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptide();
//                if (mqp == null) {
//                    return null;
//                }
//                DPeptideInstance pi = mqp.getPeptideInstance();
//                if (pi == null) {
//                    return null;
//                }
//                return pi.getBestPeptideMatch();
//            }
            if (parameterType.equals(DDataset.class)) {
                return m_dataset;
            }
            if (parameterType.equals(QuantChannelInfo.class)) {
                return m_quantChannelInfo;
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
            }
            if (parameterType.equals(XicMode.class)) {
                return new XicMode(true);
            }
        }
        return super.getData(getArray, parameterType);
    }
    
        @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DProteinMatch.class};
        return classList;
    }
    
    @Override
    public String getImportantOutParameterValue() {
        DProteinMatch p = (DProteinMatch) getData(false, DProteinMatch.class);
        if (p != null) {
            return p.getAccession();
        }
        return null;
    }
   
}
