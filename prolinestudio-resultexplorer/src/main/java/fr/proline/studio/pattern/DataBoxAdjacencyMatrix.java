/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
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
        
        // Register in parameters
        // One ResultSummary
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(ResultSummary.class);
        inParameter.addParameter(DrawVisualization.class);
        inParameter.addParameter(Component.class);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(DPeptideMatch.class);
        outParameter.addParameter(DPeptideInstance.class);
        outParameter.addParameter(DProteinMatch.class);
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

        final ResultSummary _rsm = (ResultSummary) getData(ResultSummary.class);
        // register the link to the Transient Data
        linkCache( _rsm);
        
        Component component = (Component) getData(Component.class);
        DrawVisualization drawVisualization = (DrawVisualization) getData(DrawVisualization.class);

        
        
        
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
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType!= null ) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                
                if (parameterType.equals(DPeptideMatch.class)) {
                    return ((MatrixPanel) getDataBoxPanelInterface()).getSelectedPeptideMatch();
                }
                
                if (parameterType.equals(DProteinMatch.class)) {
                    return ((MatrixPanel) getDataBoxPanelInterface()).getSelectedProteinMatch();
                }
                
                if (parameterType.equals(DPeptideInstance.class)) {
                    DPeptideMatch peptideMatch = ((MatrixPanel) getDataBoxPanelInterface()).getSelectedPeptideMatch();
                    DProteinMatch pm = ((MatrixPanel) getDataBoxPanelInterface()).getSelectedProteinMatch();
                    if ((pm != null) && (peptideMatch != null)) {
                        ResultSummary rsm = (ResultSummary) getData(ResultSummary.class);
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
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }
    
}
