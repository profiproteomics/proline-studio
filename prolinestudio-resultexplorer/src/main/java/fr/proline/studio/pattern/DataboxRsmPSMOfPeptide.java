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
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Load the PSM of a Peptide
 * @author JM235353
 */
public class DataboxRsmPSMOfPeptide extends AbstractDataBox {

    private boolean m_mergedData;
    
                
    public DataboxRsmPSMOfPeptide() {
        this(false);
    }
    
    public DataboxRsmPSMOfPeptide(boolean mergedData) {
        super(DataboxType.DataboxRsmPSMOfPeptide, DataboxStyle.STYLE_RSM);

        m_mergedData = mergedData;
        
        // Name of this databox
        m_typeName = "PSMs";
        m_description = "All PSMs of a Peptide";
        
        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(PeptideInstance.class);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(DPeptideMatch.class);
        outParameter.addParameter(ExtendedTableModelInterface.class);
        registerOutParameter(outParameter);
       
    }
    

    @Override
    public void createPanel() {
        PeptideMatchPanel p = new PeptideMatchPanel(true, m_mergedData, false, true, false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {

        final PeptideInstance peptideInstance = (PeptideInstance) getData(PeptideInstance.class);

        if (peptideInstance == null) {
             ((PeptideMatchPanel)getDataBoxPanelInterface()).setData(-1, null, null, true);
            return;
        }
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    DPeptideMatch[] peptideMatchArray = peptideInstance.getTransientData().getPeptideMatches();
                    
                    long[] peptideMatchIdArray = peptideInstance.getTransientData().getPeptideMatchesId();
                    ((PeptideMatchPanel)getDataBoxPanelInterface()).setData(taskId, peptideMatchArray, peptideMatchIdArray, finished);
               } else {
                    ((PeptideMatchPanel)getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }
               
                if (finished) {
                    unregisterTask(taskId);
                    //propagateDataChanged(ValuesForStatsAbstract.class);
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };
        

        // ask asynchronous loading of data
        registerTask(new DatabaseLoadPeptideMatchTask(callback, getProjectId(), peptideInstance));

       
        
    }
    
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType!= null ) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(DPeptideMatch.class)) {
                    return ((PeptideMatchPanel) getDataBoxPanelInterface()).getSelectedPeptideMatch();
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
    public Class[] getDataboxNavigationOutParameterClasses() {
        Class[] classList = {DPeptideMatch.class};
        return classList;
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
