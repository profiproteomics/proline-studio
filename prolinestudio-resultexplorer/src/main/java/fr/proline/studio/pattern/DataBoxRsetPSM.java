/* 
 * Copyright (C) 2019
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
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;

/**
 * Databox for all PSM of a ResultSet (Search Result)
 * @author JM235353
 */
public class DataBoxRsetPSM extends AbstractDataBox {

    
    private ResultSet m_rset = null;

    private boolean m_mergedData;
    
    public DataBoxRsetPSM() {
        this(false);
    }
    
    public DataBoxRsetPSM(boolean mergedData) {
        super(DataboxType.DataBoxRsetPSM, DataboxStyle.STYLE_RSET);

        m_mergedData = mergedData;
        
        // Name of this databox
        m_typeName = "PSMs";
        m_description = "All PSMs of a Search Result";
        
        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(ResultSet.class);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(DPeptideMatch.class);
        outParameter.addParameter(ExtendedTableModelInterface.class);
        registerOutParameter(outParameter);
       
    }
    

    @Override
    public void createPanel() {
        PeptideMatchPanel p = new PeptideMatchPanel(false, m_mergedData, true, true, false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {
        
        final ResultSet _rset = (m_rset!=null) ? m_rset : (ResultSet) getData(ResultSet.class);

        // register the link to the Transient Data
        linkCache(_rset);
        
        final int loadingId = setLoading();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {


                    DPeptideMatch[] peptideMatchArray = _rset.getTransientData(null).getPeptideMatches();
                    long[] peptideMatchIdArray = _rset.getTransientData(null).getPeptideMatchIds();
                    
                    ((PeptideMatchPanel)getDataBoxPanelInterface()).setData(taskId, peptideMatchArray, peptideMatchIdArray, finished);
               } else {
                    ((PeptideMatchPanel)getDataBoxPanelInterface()).dataUpdated(subTask, finished);
               }
               
               setLoaded(loadingId);
               
                if (finished) {
                    unregisterTask(taskId);

                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };
        

        // ask asynchronous loading of data
        DatabaseLoadPeptideMatchTask task = new DatabaseLoadPeptideMatchTask(callback, getProjectId(), _rset);
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
        if (parameterType != null) {

            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {

                if (parameterType.equals(DPeptideMatch.class)) {
                    return ((PeptideMatchPanel) getDataBoxPanelInterface()).getSelectedPeptideMatch();
                }
                if (parameterType.equals(ResultSet.class)) {
                    if (m_rset != null) {
                        return m_rset;
                    }
                }

                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
            }
            
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }
 
    @Override
    public void setEntryData(Object data) {
        
        
        
        if (data instanceof ResultSet) {
            m_rset = (ResultSet) data;
            getDataBoxPanelInterface().addSingleValue(m_rset);
            dataChanged();
        } else if (data instanceof ResultSummary) {
            m_rset = ((ResultSummary) data).getResultSet();
            getDataBoxPanelInterface().addSingleValue(m_rset);
        }
    }
    
    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        return new Class[]{DPeptideMatch.class};
    }

    @Override
    public String getDataboxNavigationDisplayValue() {
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
