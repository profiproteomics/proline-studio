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
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.RsmPeptidesPanel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;



/**
 *  Databox : All Peptides of an Identification Summary
 * @author JM235353
 */
public class DataBoxRsmPeptideInstances extends AbstractDataBox {

    
    ResultSummary m_rsm = null;
    
    public DataBoxRsmPeptideInstances() {
        super(DataboxType.DataBoxRsmPeptideInstances, DataboxStyle.STYLE_RSM);

        // Name of this databox
        m_typeName = "Peptides";
        m_description = "All Peptides of an Identification Summary";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(PeptideInstance.class, true);
        registerOutParameter(outParameter);
       
        outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class, false);
        registerOutParameter(outParameter);

        
    }
    

    @Override
    public void createPanel() {
        RsmPeptidesPanel p = new RsmPeptidesPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {
        
        final ResultSummary _rsm = (m_rsm!=null) ? m_rsm : (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);

        final int loadingId = setLoading();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    PeptideInstance[] peptideinstanceArray = _rsm.getTransientData().getPeptideInstanceArray();
                    ((RsmPeptidesPanel)getDataBoxPanelInterface()).setData(taskId, peptideinstanceArray, finished);
               } else {
                    ((RsmPeptidesPanel)getDataBoxPanelInterface()).dataUpdated(subTask, finished);
               }
               
               setLoaded(loadingId);
               
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };
        

        // ask asynchronous loading of data
        DatabaseLoadPeptidesInstancesTask task = new DatabaseLoadPeptidesInstancesTask(callback, getProjectId(), _rsm);
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
            if (parameterType.equals(PeptideInstance.class)) {
                return ((RsmPeptidesPanel)getDataBoxPanelInterface()).getSelectedPeptideInstance();
            }
            if (parameterType.equals(DPeptideMatch.class)) {
                PeptideInstance pi = ((RsmPeptidesPanel)getDataBoxPanelInterface()).getSelectedPeptideInstance();
                if (pi == null) {
                    return null;
                }
                return pi.getTransientData().getBestPeptideMatch();
            }
            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }
            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
            }
            
            
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        
        if (data instanceof ResultSummary) {
            m_rsm = (ResultSummary) data;
            dataChanged();
        }
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {PeptideInstance.class, DPeptideMatch.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DPeptideMatch p = (DPeptideMatch) getData(false, DPeptideMatch.class);
        if (p != null) {
            Peptide peptide = p.getPeptide();
            if (peptide != null) {
                return peptide.getSequence();
            }
        }
        return null;
    }
}
