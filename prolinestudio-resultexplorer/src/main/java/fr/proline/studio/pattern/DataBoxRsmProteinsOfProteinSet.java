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



import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsFromProteinSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.RsmProteinsOfProteinSetPanel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;



/**
 * Databox : Proteins of a Protein Set
 * @author JM235353
 */
public class DataBoxRsmProteinsOfProteinSet extends AbstractDataBox {
    
    private long m_proteinSetCurId = -1;

    public DataBoxRsmProteinsOfProteinSet() {
        super(DataboxType.DataBoxRsmProteinsOfProteinSet, DataboxStyle.STYLE_RSM);

        // Name of this databox
        m_typeName = "Proteins";
        m_description = "All Proteins of a Protein Set";
        
        // Register Possible in parameters
        // One ProteinSet
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DProteinSet.class);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One ProteinMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinMatch.class);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class);
        registerOutParameter(outParameter);
       
    }

    @Override
    public void createPanel() {
        RsmProteinsOfProteinSetPanel p = new RsmProteinsOfProteinSetPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {
        final DProteinSet proteinSet = (DProteinSet) m_previousDataBox.getData(DProteinSet.class);

        if (proteinSet == null) {
            ((RsmProteinsOfProteinSetPanel)getDataBoxPanelInterface()).setData(null, null);
            m_proteinSetCurId = -1;
            return;
        }
        
         if ((m_proteinSetCurId!=-1) && (proteinSet.getId() == m_proteinSetCurId)) {
            return;
        }
        
        m_proteinSetCurId = proteinSet.getId();
        
        
        final int loadingId = setLoading();
        
        //final String searchedText = searchTextBeingDone; //JPM.TODO
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {


                ((RsmProteinsOfProteinSetPanel)getDataBoxPanelInterface()).setData(proteinSet, null /*
                         * searchedText
                         */); //JPM.TODO
                
                setLoaded(loadingId);
                unregisterTask(taskId);
                propagateDataChanged(ExtendedTableModelInterface.class);
            }
        };

        // Load data if needed asynchronously
        DatabaseProteinsFromProteinSetTask task = new DatabaseProteinsFromProteinSetTask(callback, getProjectId(), proteinSet);
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
        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(DProteinMatch.class)) {
                    return ((RsmProteinsOfProteinSetPanel) getDataBoxPanelInterface()).getSelectedProteinMatch();
                }
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
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
        Class[] classList = {DProteinMatch.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DProteinMatch pm = (DProteinMatch) getData(DProteinMatch.class);

        if (pm != null) {
            return pm.getAccession();
        }

        return null;
    }
}
