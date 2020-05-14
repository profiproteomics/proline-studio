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


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.RsmPeptidesOfProteinPanel;
import java.util.ArrayList;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Databox : Peptides of a Protein in a Rsm
 * @author JM235353
 */
public class DataBoxRsmPeptidesOfProtein extends AbstractDataBox {
    

    
    public DataBoxRsmPeptidesOfProtein() {
        super(DataboxType.DataBoxRsmPeptidesOfProtein, DataboxStyle.STYLE_RSM);

        // Name of this databox
        m_typeName = "Peptides";
        m_description = "All Peptides of a Protein Match";
        
        // Register Possible in parameters
        // One ProteinMatch AND one ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DProteinMatch.class);
        inParameter.addParameter(ResultSummary.class);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple  PeptideInstance
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideInstance.class);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class);
        registerOutParameter(outParameter);
       
    }
    

     
     @Override
    public void createPanel() {
        RsmPeptidesOfProteinPanel p = new RsmPeptidesOfProteinPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    

    @Override
    public void dataChanged() {
        final DProteinMatch proteinMatch = (DProteinMatch) m_previousDataBox.getData(DProteinMatch.class);
        final DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(DPeptideMatch.class);
        final ResultSummary rsm = (ResultSummary) m_previousDataBox.getData(ResultSummary.class);

        // register the link to the Transient Data
        linkCache(rsm);
        
        if (proteinMatch == null) {
            ((RsmPeptidesOfProteinPanel) getDataBoxPanelInterface()).setData(null, null, null);
            return;
        }

        final int loadingId = setLoading();
        
        // prepare callback to view new data
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {
                    ((RsmPeptidesOfProteinPanel) getDataBoxPanelInterface()).setData(proteinMatch, peptideMatch, rsm);
                } else {
                    ((RsmPeptidesOfProteinPanel) getDataBoxPanelInterface()).setData(null, null, null);
                }
                
                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };

        // Load data if needed asynchronously
        ArrayList<ResultSummary> rsmList = new ArrayList<>(1);
        rsmList.add(rsm);
        DatabaseLoadPeptidesInstancesTask task = new DatabaseLoadPeptidesInstancesTask(callback, getProjectId(), proteinMatch, rsmList);
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
    public Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType!= null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(DPeptideInstance.class)) {
                    return ((RsmPeptidesOfProteinPanel) getDataBoxPanelInterface()).getSelectedPeptide();
                }
                if (parameterType.equals(DPeptideMatch.class)) {
                    DPeptideInstance pi = ((RsmPeptidesOfProteinPanel) getDataBoxPanelInterface()).getSelectedPeptide();
                    if (pi != null) {
                        return pi.getBestPeptideMatch();
                    }
                }
                if (parameterType.equals(ExtendedTableModelInterface.class)) {//get all peptide instances
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
            }
        }
        return super.getData(parameterType, parameterSubtype);
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DPeptideMatch.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DPeptideMatch p = (DPeptideMatch) getData(DPeptideMatch.class);
        if (p != null) {
            Peptide peptide = p.getPeptide();
            if (peptide != null) {
                return peptide.getSequence();
            }
        }
        return null;
    }

}
