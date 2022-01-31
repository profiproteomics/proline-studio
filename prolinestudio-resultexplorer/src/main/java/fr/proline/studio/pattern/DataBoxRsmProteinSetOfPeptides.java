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


import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.RsmProteinSetPanel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;


/**
 * Databox for the list of Protein Sets corresponding to a Peptide Instance
 * @author JM235353
 */
public class DataBoxRsmProteinSetOfPeptides extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;

    private long m_peptideCurId = -1;
    
    public DataBoxRsmProteinSetOfPeptides() { 
        super(DataboxType.DataBoxRsmProteinSetOfPeptides, DataboxStyle.STYLE_RSM);

         // Name of this databox
        m_typeName = "Protein Set";
        m_description = "All Protein Sets corresponding to a Peptide Instance";

        // Register in parameters

        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(PeptideInstance.class);
        registerInParameter(inParameter);


        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(DProteinSet.class);
        outParameter.addParameter(ResultSummary.class);
        outParameter.addParameter(ExtendedTableModelInterface.class);
        registerOutParameter(outParameter);
    }


    @Override
    public void createPanel() {
        RsmProteinSetPanel p = new RsmProteinSetPanel(false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {

        // register the link to the Transient Data
        linkCache(m_rsm);
        
        final PeptideInstance _peptideInstance = (PeptideInstance) getData(PeptideInstance.class);


        if (_peptideInstance == null) {
            ((RsmProteinSetPanel) getDataBoxPanelInterface()).setData(null, null, true);
            m_peptideCurId = -1;
            return;
        }

        if ((m_peptideCurId!=-1) && (_peptideInstance.getId() == m_peptideCurId)) {
            return;
        }
        m_peptideCurId = _peptideInstance.getId();
        
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {

                    DProteinSet[] proteinSetArray = _peptideInstance.getTransientData().getProteinSetArray();
                    
                    ((RsmProteinSetPanel) getDataBoxPanelInterface()).setData(taskId, proteinSetArray, finished);
                } else {
                    ((RsmProteinSetPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
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
        DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
        task.initLoadProteinSetForPeptideInstance(getProjectId(), _peptideInstance);
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
                if (parameterType.equals(DProteinSet.class)) {
                    return ((RsmProteinSetPanel) getDataBoxPanelInterface()).getSelectedProteinSet();
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
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }
 
    @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        
        m_rsm = (ResultSummary) data;
        dataChanged();
    }

    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        return new Class[]{DProteinSet.class};
    }

    @Override
    public String getDataboxNavigationDisplayValue() {
        DProteinSet p = (DProteinSet) getData(DProteinSet.class);
        if (p != null) {
            DProteinMatch pm = p.getTypicalProteinMatch();
            if (pm != null) {
                return pm.getAccession();
            }
        }
        return null;
    }
}
