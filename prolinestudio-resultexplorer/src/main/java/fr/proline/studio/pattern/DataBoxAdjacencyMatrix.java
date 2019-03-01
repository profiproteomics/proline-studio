package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsAndPeptidesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.Component;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.DrawVisualization;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.MatrixPanel;
import java.util.ArrayList;
import java.util.HashMap;



/**
 *
 * Databox to display an adjacency Matrix
 * 
 * @author JM235353
 */
public class DataBoxAdjacencyMatrix extends AbstractDataBox {
    
    public final static String DESCRIPTION = "Proteins Adjacency Matrix";
    
    public DataBoxAdjacencyMatrix() {
        super(DataboxType.DataBoxAdjacencyMatrix, DataboxStyle.STYLE_RSM);
        
        // Name of this databox
        m_typeName = DESCRIPTION;
        m_description = DESCRIPTION;
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DrawVisualization.class, false);
        inParameter.addParameter(Component.class, false);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideInstance.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DProteinMatch.class, false);
        registerOutParameter(outParameter);
        
        
        

    }
    
    @Override
    public void createPanel() {

        MatrixPanel p = new MatrixPanel(); 
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {

        Component component = (Component) m_previousDataBox.getData(false, Component.class);
        DrawVisualization drawVisualization = (DrawVisualization) m_previousDataBox.getData(false, DrawVisualization.class);

        
        final ResultSummary _rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
        
        ArrayList<LightProteinMatch> proteinMatchArray = component.getProteinArray(true);
        ArrayList<LightPeptideMatch> peptideMatchArray = component.getPeptideArray();
        
        int nbProteins = proteinMatchArray.size();
        ArrayList<Long> proteinMatchIdArray = new ArrayList<>(nbProteins);
        for (int i=0;i<nbProteins;i++) {
            proteinMatchIdArray.add(proteinMatchArray.get(i).getId());
        }
        
        int nbPeptides = peptideMatchArray.size();
        ArrayList<Long> peptideMatchIdArray = new ArrayList<>(nbPeptides);
        for (int i=0;i<nbPeptides;i++) {
            peptideMatchIdArray.add(peptideMatchArray.get(i).getId());
        }
  
        final HashMap<Long, DProteinMatch> proteinMap = new HashMap<>();
        final HashMap<Long, DPeptideMatch> peptideMap = new HashMap<>();
        
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                ((MatrixPanel) getDataBoxPanelInterface()).setData(component, drawVisualization, proteinMap, peptideMap, _rsm.getId());

                unregisterTask(taskId);
            }
        };
        
        DatabaseProteinsAndPeptidesTask task = new DatabaseProteinsAndPeptidesTask(callback, getProjectId(), _rsm, proteinMatchIdArray, peptideMatchIdArray, proteinMap, peptideMap);
        
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);

    }
    private Long m_previousTaskId = null;
    
        @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(DPeptideMatch.class)) {
                return ((MatrixPanel)getDataBoxPanelInterface()).getSelectedPeptideMatch();
            }
            if (parameterType.equals(DProteinMatch.class)) {
                return ((MatrixPanel)getDataBoxPanelInterface()).getSelectedProteinMatch();
            }
            if (parameterType.equals(DPeptideInstance.class)) {
                DPeptideMatch peptideMatch = ((MatrixPanel)getDataBoxPanelInterface()).getSelectedPeptideMatch();
                DProteinMatch pm = ((MatrixPanel)getDataBoxPanelInterface()).getSelectedProteinMatch();
                if ((pm != null) && (peptideMatch != null)) {
                    ResultSummary rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
                    if (rsm != null) {
                        DPeptideSet peptideSet = pm.getPeptideSet(rsm.getId());
                        DPeptideInstance[] peptideInstances = peptideSet.getPeptideInstances();
                        if (peptideInstances != null) {
                            for (DPeptideInstance peptideInstance : peptideInstances) {
                                if (peptideInstance.getPeptideId() == peptideMatch.getPeptide().getId()) {
                                    return peptideInstance;
                                }
                            }
                        }
                    }
                    
                }
               
            }
        }
        return super.getData(getArray, parameterType);
    }
    
}
