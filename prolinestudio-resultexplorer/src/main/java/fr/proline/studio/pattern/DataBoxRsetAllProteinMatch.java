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


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.rsmexplorer.gui.RsetProteinsPanel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinMatchesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;


/**
 * All Protein Matches of a Search Result
 * @author JM235353
 */
public class DataBoxRsetAllProteinMatch extends AbstractDataBox {

    private ResultSet m_rset;
    
    public DataBoxRsetAllProteinMatch() {
        super(DataboxType.DataBoxRsetAllProteinMatch, DataboxStyle.STYLE_RSET);
        
        // Name of this databox
        m_typeName = "Proteins";
        m_description = "All Proteins of a Search Result";
        
        // Register Possible in parameters
        // One PeptideMatch
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSet.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple ProteinMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinMatch.class, true);
        outParameter.addParameter(ResultSet.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class, true);
        registerOutParameter(outParameter);
    }
    
    @Override
    public void createPanel() {
        RsetProteinsPanel p = new RsetProteinsPanel(true);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {

        final ResultSet _rset = (m_rset != null) ? m_rset : (ResultSet) m_previousDataBox.getData(false, ResultSet.class);

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

                    DProteinMatch[] proteinMatchArray = _rset.getTransientData(TransientMemoryCacheManager.getSingleton()).getProteinMatches();
                    
                    
                    ((RsetProteinsPanel) getDataBoxPanelInterface()).setDataProteinMatchArray(proteinMatchArray, finished);
                } else {
                    ((RsetProteinsPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    
                    
                    
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
                
            }
        };


        // ask asynchronous loading of data

        DatabaseProteinMatchesTask task = new DatabaseProteinMatchesTask(callback, getProjectId(), _rset);
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
            if (parameterType.equals(DProteinMatch.class)) {
                return ((RsetProteinsPanel)getDataBoxPanelInterface()).getSelectedProteinMatch();
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
                return  ((GlobalTabelModelProviderInterface)getDataBoxPanelInterface()).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
    
        @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        
        m_rset = (ResultSet) data;
        
        dataChanged();
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DProteinMatch.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DProteinMatch pm = (DProteinMatch) getData(false, DProteinMatch.class);
        if (pm != null) {
            return pm.getAccession();
        }
        return null;
    }
    
}
