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
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsAndPeptidesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.Component;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.DrawVisualization;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.MatrixSelectionPanel;

/**
 * Databox to select one of the available adjacency matrices
 * 
 * @author JM235353
 */
public class DataBoxAdjacencyMatrixChoice extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;
    
    private boolean m_keepSameSet = false;
    private boolean m_doNotTakeFirstSelection = false;  //JPM.WART : when we select later the matrix to be showed.
    
    public DataBoxAdjacencyMatrixChoice() {
        super(DataboxType.DataBoxAdjacencyMatrixChoice, DataboxStyle.STYLE_RSM);
        
        // Name of this databox
        m_typeName = "Proteins Adjacency Matrices";
        m_description = "All Adjacency Matrices";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        inParameter.addParameter(DProteinSet.class, false);
        registerInParameter(inParameter);
        
        inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        inParameter.addParameter(DProteinMatch.class, false);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DrawVisualization.class, false);
        outParameter.addParameter(Component.class, false);
        registerOutParameter(outParameter);
    }
    
    public void setKeepSameset(boolean keepSameSet) {
        m_keepSameSet = keepSameSet;
    }
    
    public void doNotTakeFirstSelection(boolean doNotTakeFirstSelection) {
        m_doNotTakeFirstSelection = doNotTakeFirstSelection;
    }
    
    @Override
    public void createPanel() {
        
        //JPM.TODO
        MatrixSelectionPanel p = new MatrixSelectionPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {

        final ResultSummary _rsm = (m_rsm != null) ? m_rsm : (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
        
        
        DProteinMatch proteinMatch = (m_previousDataBox==null) ? null : (DProteinMatch) m_previousDataBox.getData(false, DProteinMatch.class);
        
        if (proteinMatch == null) {
            DProteinSet proteinSet = (m_previousDataBox==null) ? null : (DProteinSet) m_previousDataBox.getData(false, DProteinSet.class);
            if (proteinSet != null) {
                proteinMatch = proteinSet.getTypicalProteinMatch();
            }
        }
        
        final DProteinMatch _proteinMatch = proteinMatch;
        
         
        final AdjacencyMatrixData matrixData = new AdjacencyMatrixData();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                try {

                ((MatrixSelectionPanel) getDataBoxPanelInterface()).setData(matrixData, _proteinMatch, m_keepSameSet, m_doNotTakeFirstSelection);
                m_doNotTakeFirstSelection = false;

                if (finished) {
                    unregisterTask(taskId);
                }
                
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        };

        ((MatrixSelectionPanel) getDataBoxPanelInterface()).setData(_proteinMatch, m_doNotTakeFirstSelection);

        registerTask(new DatabaseProteinsAndPeptidesTask(callback, getProjectId(), _rsm, matrixData));



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
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(Component.class)) {
                return ((MatrixSelectionPanel) getDataBoxPanelInterface()).getCurrentComponent();
            }
            if (parameterType.equals(DrawVisualization.class)) {
                return ((MatrixSelectionPanel) getDataBoxPanelInterface()).getDrawVisualization();
            }
            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }
        }
        return super.getData(getArray, parameterType);
    }
    
}

