package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.RegisterRawFileTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNode;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.Window;
import java.util.*;
import javax.persistence.EntityManager;
import javax.swing.tree.TreePath;
import org.slf4j.LoggerFactory;


/**
 * Dialog to build a XIC Design
 * @author JM235353
 */
public class CreateXICDialog extends DefaultDialog {

    
    private static final int STEP_PANEL_CREATE_XIC_DESIGN = 0;
    private static final int STEP_PANEL_DEFINE_XIC_PARAMS = 1;
    private int m_step = STEP_PANEL_CREATE_XIC_DESIGN;
    
    private static CreateXICDialog m_singletonDialog = null;
    
    private AbstractNode finalXICDesignNode = null;

    public static CreateXICDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new CreateXICDialog(parent);
        }

        return m_singletonDialog;
    }

    private CreateXICDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("XIC Quantitation Wizard");

        setHelpURL(null); //JPM.TODO

      

        setSize(640, 500);
        setResizable(true);
        
    }
    

    public final void reinit(){
        
        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);
        finalXICDesignNode = null;
        m_step = STEP_PANEL_CREATE_XIC_DESIGN;
        finalXICDesignNode = new DataSetNode(new DataSetData("XIC", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION));

        replaceInternaleComponent(CreateXICDesignPanel.getPanel(finalXICDesignNode));
        revalidate();
        repaint();
         
    }
    
    @Override
    public void pack() {
        // forbid pack by overloading the method
    }
    
    public AbstractNode getDesignRSMNode(){
        return finalXICDesignNode;
    }
    
    /*
     * Return map reflecting JSON obhject for experimental_design :
        "biological_samples": [{
                "name": "ff 0",
                "number": 0
        }],
        "master_quant_channels": [{
                "quant_channels": [{
                        "ident_result_summary_id": 6,
                        "number": 0,
                        "sample_number": 0
                }],
                "name": "TEST 0206",
                "number": 0
        }],
        "group_setups": [{
                "biological_groups": [{
                        "name": "br 0",
                        "number": 0,
                        "sample_numbers": [0]
                }],
                "ratio_definitions": [{
                        "denominator_group_number": 0,
                        "numerator_group_number": 0,
                        "number": 0
                }],
                "name": "TEST 0206",
                "number": 0
        }]
     
     * @throws IllegalAccessException 
     */
    
    private HashMap<Long, Long> getRunIdForRSMs(Collection<Long> rsmIDs){
        //Get Run Ids for specified RSMs
        Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
        HashMap<Long, Long> runIdByRsmId = new HashMap<>();
        
        for (Long rsmId : rsmIDs) {
            ArrayList<Long> returnedRunId = new ArrayList<> ();
            DatabaseRunsTask loadRunIdsTask = new DatabaseRunsTask(null);
            loadRunIdsTask.initLoadRunIdForRsm(pID, rsmId, returnedRunId);
            loadRunIdsTask.fetchData();
            if(returnedRunId.size() >0) {
                runIdByRsmId.put(rsmId, returnedRunId.get(0));
            } else {
                runIdByRsmId.put(rsmId, -1l);
            }
        }
        
        return runIdByRsmId;
    }

    public String registerRawFiles() {

        
        long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
        
        Project project;
        String errorMsg = null;
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        
        try {
            project = entityManagerUDS.find(Project.class, pID);
           
                final Object mutexFileRegistered = new Object();

                Enumeration xicGrps = finalXICDesignNode.children();
                while (xicGrps.hasMoreElements() && errorMsg == null) {
                    AbstractNode grpNode = (AbstractNode) xicGrps.nextElement();
                    //Iterate over Samples
                    Enumeration grpSpls = grpNode.children();
                    while (grpSpls.hasMoreElements() && errorMsg == null) {
                        AbstractNode bioSplNode = (AbstractNode) grpSpls.nextElement();
                        //Iterate over SampleAnalysis
                        Enumeration identRSMs = bioSplNode.children();
                        while (identRSMs.hasMoreElements()) {
                            AbstractNode bioSplAnalysisNode = (AbstractNode) identRSMs.nextElement();
                            Enumeration runNodes = bioSplAnalysisNode.children();
                            while (runNodes.hasMoreElements()) {
                                XICRunNode runNode = (XICRunNode) runNodes.nextElement();
                                RunInfoData runData = (RunInfoData) runNode.getData();
                                if (!runData.isRunInfoInDatabase()) {
                                    // RawFile does not exists, create it
                                    try {
                                        synchronized (mutexFileRegistered) {
                                            AbstractServiceCallback callback = new AbstractServiceCallback() {

                                                @Override
                                                public boolean mustBeCalledInAWT() {
                                                    return false;
                                                }

                                                @Override
                                                public void run(boolean success) {
                                                    synchronized (mutexFileRegistered) {
                                                        mutexFileRegistered.notifyAll();
                                                    }
                                                }
                                            };
                                            // TODO : get the right instrumentId !!! 
                                            long instrumentID = 1;
                                            RegisterRawFileTask task = new RegisterRawFileTask(callback, instrumentID, project.getOwner().getId(), runData);
                                            AccessServiceThread.getAccessServiceThread().addTask(task);
                                            // wait untill the files are loaded
                                            mutexFileRegistered.wait();
                                        }

                                    } catch (InterruptedException ie) {
                                        // should not happen
                                    }
                                }
                                
                                
                                /*ArrayList<RawFile> returnedRaw = new ArrayList<>();
                                DatabaseRunsTask loadRunIdsTask = new DatabaseRunsTask(null);
                                loadRunIdsTask.initSearchRawFile(runData.getRawFile().getRawFileName(), returnedRaw);
                                loadRunIdsTask.fetchData();
                                if (returnedRaw.size() == 1) {
                                    if (!project.getRawFiles().contains(runData.getRawFile())) {
                                        // TODO update Project - rawFile map
                                    } 
                                } else if (returnedRaw.size() > 1) {
                                    errorMsg = "More than one rawFile with name " + runData.getRawFile().getRawFileName() + " found.";
                                    return errorMsg;
                                } else {
                                    // RawFile does not exists, create it
                                    try {
                                        synchronized (mutexFileRegistered) {
                                            AbstractServiceCallback callback = new AbstractServiceCallback() {

                                                @Override
                                                public boolean mustBeCalledInAWT() {
                                                    return false;
                                                }

                                                @Override
                                                public void run(boolean success) {
                                                    synchronized (mutexFileRegistered) {
                                                        mutexFileRegistered.notifyAll();
                                                    }
                                                }
                                            };
                                            // TODO : get the right instrumentId !!! 
                                            long instrumentID = 1;
                                            RegisterRawFileTask task = new RegisterRawFileTask(callback, instrumentID, project.getOwner().getId(), runData);
                                            AccessServiceThread.getAccessServiceThread().addTask(task);
                                            // wait untill the files are loaded
                                            mutexFileRegistered.wait();
                                        }

                                    } catch (InterruptedException ie) {
                                        // should not happen
                                    } 
                                }*/
                                //  then map IdentificationDataset to RawFile and Run
                                DatabaseRunsTask registerRunIdsTask = new DatabaseRunsTask(null);
                                registerRunIdsTask.initRegisterIdentificationDatasetRun(((DataSetData)bioSplAnalysisNode.getData()).getDataset().getId(), runData.getRawFileSouce().getSelectedRawFile(), runData.getRun());
                                registerRunIdsTask.fetchData();

                            }
                        }
                    }
                } //End go through group's sample

              } catch (Exception e) {
             LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", e);
         } finally {
            entityManagerUDS.close();
        }
        
        return errorMsg;
    }

    public Map<String, Object>  getDesignParameters() throws IllegalAccessException {
        if (finalXICDesignNode==null) {
            throw new IllegalAccessException("Design parameters have not been set.");
        }
        
        Map<String,Object> experimentalDesignParams = new HashMap<>();
                       
        String errorMsg = null;
             
        //Number used for Experimental Design data 
        int ratioNumeratorGrp = 1;
        int ratioDenominatorGrp =1;
        int grpNumber = 1;
        int splNumber = 1;
        int splAnalysisNumber = 1;
        
        List _biologicalGroupList = new ArrayList();
        HashMap<String, Long> _rsmIdBySampleAnalysis = new HashMap<>();
        HashMap<Long, Long> _runIdByRSMId;
        HashMap<String, ArrayList<String>> _samplesAnalysisBySample = new HashMap<>();
        Map<String, Integer> splNbrByName = new HashMap<>();            
                
        Enumeration xicGrps = finalXICDesignNode.children();   
        while(xicGrps.hasMoreElements()&& errorMsg==null){
            AbstractNode grpNode = (AbstractNode) xicGrps.nextElement();
            String grpName = grpNode.getData().getName();
            
            //Iterate over Samples
            Enumeration grpSpls  = grpNode.children();      
            List<Integer> splNumbers = new ArrayList();
            
            while(grpSpls.hasMoreElements() && errorMsg==null){
                AbstractNode splNode = (AbstractNode) grpSpls.nextElement();
                String sampleName = grpName+splNode.getData().getName();                
                splNbrByName.put(sampleName, splNumber);
                splNumbers.add(splNumber++);
                 
                //Iterate over SampleAnalysis
                Enumeration identRSMs  = splNode.children();      
                ArrayList<String> splAnalysisNames = new ArrayList<>();
                while(identRSMs.hasMoreElements()){
                    //VD TODO TEST child type
                    AbstractNode qChannelNode = (AbstractNode) identRSMs.nextElement();
                    String spAnalysisName = qChannelNode.getData().getName();
                    splAnalysisNames.add(spAnalysisName);
                    if(!DataSetData.class.isInstance(qChannelNode.getData())){
                        errorMsg = "Invalide Sample Analysis specified ";
                        break;
                    }
                    _rsmIdBySampleAnalysis.put(spAnalysisName, ((DataSetData)qChannelNode.getData()).getDataset().getResultSummaryId());                            
                }
                _samplesAnalysisBySample.put(sampleName, splAnalysisNames);
            } //End go through group's sample
                         
            Map<String, Object> biologicalGroupParams = new HashMap<>();
            biologicalGroupParams.put("number", grpNumber++);
            biologicalGroupParams.put("name", grpName);
            biologicalGroupParams.put("sample_numbers", splNumbers);
            _biologicalGroupList.add(biologicalGroupParams);

        }// End go through groups
        
        //Read Run IDs
        _runIdByRSMId = getRunIdForRSMs(_rsmIdBySampleAnalysis.values());
        
        if(errorMsg!=null)
            throw new IllegalAccessException(errorMsg);
        
        if(grpNumber>2)
            ratioDenominatorGrp = 2; //VD TODO :  Comment gerer les ratois ?
            
        Map<String, Object> ratioParams = new HashMap<>();
        ratioParams.put("number", 1);
        ratioParams.put("numerator_group_number", ratioNumeratorGrp);
        ratioParams.put("denominator_group_number",ratioDenominatorGrp);
        List ratioParamsList = new ArrayList();
        ratioParamsList.add(ratioParams);
            
        Map<String, Object> groupSetupParams = new HashMap<>();
        groupSetupParams.put("number", 1);
        groupSetupParams.put("name", finalXICDesignNode.getData().getName());
        groupSetupParams.put("biological_groups", _biologicalGroupList);
        groupSetupParams.put("ratio_definitions", ratioParamsList);
            
        ArrayList groupSetupParamsList = new ArrayList();
        groupSetupParamsList.add(groupSetupParams);
        experimentalDesignParams.put("group_setups",groupSetupParamsList );            
            
        List biologicalSampleList = new ArrayList();
        List quantChanneList = new ArrayList();
            
        Iterator<String> samplesIt =  splNbrByName.keySet().iterator();
        while(samplesIt.hasNext()){
            String nextSpl = samplesIt.next();
            Integer splNbr = splNbrByName.get(nextSpl);
                
            Map<String, Object> biologicalSampleParams = new HashMap<>();
            biologicalSampleParams.put("number", splNbr);
            biologicalSampleParams.put("name", nextSpl);

            biologicalSampleList.add(biologicalSampleParams);
                    
            List<String> splAnalysis = _samplesAnalysisBySample.get(nextSpl);
            for(int i =0; i<splAnalysis.size(); i++){
                String nextSplAnalysis = splAnalysis.get(i);
                Map<String, Object> quantChannelParams = new HashMap<>();
                quantChannelParams.put("number", splAnalysisNumber++);
                quantChannelParams.put("sample_number", splNbr);
                quantChannelParams.put("ident_result_summary_id", _rsmIdBySampleAnalysis.get(nextSplAnalysis));                    
                quantChannelParams.put("run_id", _runIdByRSMId.get(_rsmIdBySampleAnalysis.get(nextSplAnalysis)));
                quantChanneList.add(quantChannelParams);
            }

        } // End go through samples
        experimentalDesignParams.put("biological_samples", biologicalSampleList);

        List masterQuantChannelsList = new ArrayList();
        Map<String, Object> masterQuantChannelParams = new HashMap<>();
        masterQuantChannelParams.put("number", 1);
        masterQuantChannelParams.put("name", finalXICDesignNode.getData().getName());
        masterQuantChannelParams.put("quant_channels", quantChanneList);
        masterQuantChannelsList.add(masterQuantChannelParams);
       
        experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);
 
        return experimentalDesignParams;
    }
                  
            /*"quantitation_config": {
		"extraction_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM"
		},
		"clustering_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM",
			"time_tol": "15",
			"time_computation": "MOST_INTENSE",
			"intensity_computation": "MOST_INTENSE"
		},
		"aln_method_name": "ITERATIVE",
		"aln_params": {
			"mass_interval": "20000",
			"max_iterations": "3",
			"smoothing_method_name": "TIME_WINDOW",
			"smoothing_params": {
				"window_size": "200",
				"window_overlap": "20",
				"min_window_landmarks": "50"
			},
			"ft_mapping_params": {
				"moz_tol": "5",
				"moz_tol_unit": "PPM",
				"time_tol": "600"
			}
		},
		"ft_filter": {
			"name": "INTENSITY",
			"operator": "GT",
			"value": "0"
		},
		"ft_mapping_params": {
			"moz_tol": "10",
			"moz_tol_unit": "PPM",
			"time_tol": "120"
		},
		"normalization_method": "MEDIAN_RATIO"
	}*/
    
    public Map<String, Object> getQuantiParameters(){
        return DefineQuantParamsPanel.getDefineQuantPanel().getQuantParams();
    }
    
    @Override
    protected boolean okCalled() {

        if (m_step == STEP_PANEL_CREATE_XIC_DESIGN)  {
            
            if ((!checkDesignStructure(finalXICDesignNode)) || (!checkRawFiles(finalXICDesignNode))) {
                return false;
            }

            // change to ok button
            setButtonName(DefaultDialog.BUTTON_OK, "OK");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));
 
            //Update panel
         
            DefineQuantParamsPanel quantPanel =  DefineQuantParamsPanel.getDefineQuantPanel();
            quantPanel.resetScrollbar();
            replaceInternaleComponent(quantPanel);
            revalidate();
            repaint();
            
            setButtonVisible(DefaultDialog.BUTTON_DEFAULT, true);
            m_step = STEP_PANEL_DEFINE_XIC_PARAMS;
            
            return false;
        } else { //STEP_PANEL_DEFINE_QUANT_PARAMS
            
            ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParameterList();
            
            // check parameters
            ParameterError error = parameterList.checkParameters();
            if (error != null) {
                setStatus(true, error.getErrorMessage());
                highlight(error.getParameterComponent());
                return false;
            }

            // Save Parameters        
            parameterList.saveParameters();

            return true;
        }

    }
    
    @Override
    protected boolean defaultCalled() {
        ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParameterList();
        parameterList.initDefaults();

        return false;
    }
    
    private boolean checkDesignStructure(AbstractNode parentNode) {
        AbstractNode.NodeTypes type = parentNode.getType();
     
        switch(type) {
            case DATA_SET:
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "Your XIC Design is empty.");
                    return false;
                }
                break;
            case BIOLOGICAL_GROUP:
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "You must add at least one Biological Sample for each Biological Group.");
                    return false;
                }
                break;
            case BIOLOGICAL_SAMPLE:
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "You must add at least one Identification for each Biological Sample.");
                    return false;
                }
                break;
        }
        
        Enumeration children = parentNode.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode rsmNode = (AbstractNode) children.nextElement();
            if (!checkDesignStructure(rsmNode)) {
                return false;
            }
        }

        return true;
    }
    
    private boolean checkRawFiles(AbstractNode node) {
        AbstractNode.NodeTypes type = node.getType();
        if (type == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
            XICBiologicalSampleAnalysisNode sampleAnalysisNode = (XICBiologicalSampleAnalysisNode) node;
            if (sampleAnalysisNode.getChildCount() != 1 ||  !((RunInfoData)((AbstractNode)sampleAnalysisNode.getChildAt(0)).getData()).hasRawFile()) {
                showErrorOnNode((AbstractNode)sampleAnalysisNode.getChildAt(0), "You must specify a Raw(mzDb) File for each identification.");
                return false;
            }
        }

        Enumeration children = node.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode rsmNode = (AbstractNode) children.nextElement();
            if (!checkRawFiles(rsmNode)) {
                return false;
            }
        }

        return true;
    }
    
    private void showErrorOnNode(AbstractNode node, String error) {

        // expand parentnode if needed
        AbstractNode parentNode = (AbstractNode) node.getParent();
        if (parentNode != null) {
            TreePath pathToExpand = new TreePath(parentNode.getPath());
            if (!XICDesignTree.getDesignTree().isExpanded(pathToExpand)) {

                XICDesignTree.getDesignTree().expandPath(pathToExpand);
            }
        }

        // scroll to node if needed
        TreePath path = new TreePath(node.getPath());
        XICDesignTree.getDesignTree().scrollPathToVisible(path);

        // display error
        setStatus(true, error);
        highlight(XICDesignTree.getDesignTree(), XICDesignTree.getDesignTree().getPathBounds(path));
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    
}
