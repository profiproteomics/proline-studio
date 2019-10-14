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

import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData;
import fr.proline.studio.rsmexplorer.gui.spectrum.RsetPeptideSpectrumValuesPanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetPeptideSpectrumValues extends AbstractDataBox {

    private DPeptideMatch m_previousPeptideMatch = null;

    public DataBoxRsetPeptideSpectrumValues() {
        super(DataboxType.DataBoxRsetPeptideSpectrumValues, DataboxStyle.STYLE_RSET);

        // Name of this databox
        m_typeName = "Spectrum Values";
        m_description = "Spectrum Values of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
        registerInParameter(inParameter);

    }

    @Override
    public void createPanel() {
        RsetPeptideSpectrumValuesPanel p = new RsetPeptideSpectrumValuesPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
         DPeptideMatch peptideMatchData = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);
         PeptideFragmentationData fragmentationData = (PeptideFragmentationData) m_previousDataBox.getData(false, PeptideFragmentationData.class);
         DPeptideMatch peptideMatch = (fragmentationData != null) ? fragmentationData.getPeptideMatch() : peptideMatchData;
        if ((m_previousPeptideMatch == peptideMatch) && (fragmentationData == null)) {
            return;
        }

        m_previousPeptideMatch = peptideMatch;

        if (peptideMatch == null) {
            ((RsetPeptideSpectrumValuesPanel) getDataBoxPanelInterface()).setData(null);
            return;
        }

        boolean needToLoadData = true;

        // JPM.WART : look fo Spectrum table which will load same data
        if (needToLoadData) {
            AbstractDataBox previousBox = m_previousDataBox;
            while (previousBox != null) {
                if (previousBox instanceof DataBoxRsetPeptideSpectrum) {
                    needToLoadData = false;
                    break;
                }
                previousBox = previousBox.m_previousDataBox;
            }
        }
        //even if needToLoadData is true, that means we had DataBoxRsetPeptideSpectrum in previous DataBox, the peptideMatch  with SpectrumFullySet  is not sure loaded
        needToLoadData = ((!peptideMatch.isMsQuerySet())
                || (!peptideMatch.getMsQuery().isSpectrumFullySet()));
        
        if (needToLoadData) {

            final int loadingId = setLoading();

            //final String searchedText = searchTextBeingDone; //JPM.TODO
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    ((RsetPeptideSpectrumValuesPanel) getDataBoxPanelInterface()).setData(peptideMatch);

                    setLoaded(loadingId);

                    if (finished) {
                        unregisterTask(taskId);
                    }
                }
            };

            // Load data if needed asynchronously
            DatabaseLoadSpectrumsTask task = new DatabaseLoadSpectrumsTask(callback, getProjectId(), peptideMatch);
            Long taskId = task.getId();
            if (m_previousTaskId != null) {
                // old task is suppressed if it has not been already done
                AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
            }
            m_previousTaskId = taskId;
            registerTask(task);
        } else {
            ((RsetPeptideSpectrumValuesPanel) getDataBoxPanelInterface()).setData(peptideMatch);
        }
    }
    private Long m_previousTaskId = null;

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(DMsQuery.class)) {
                DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);
                if (peptideMatch != null) {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    if (msQuery != null) {
                        return msQuery;
                    }
                }
            }
            if (parameterType.equals(DSpectrum.class)) {
                DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);
                if (peptideMatch != null) {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    if (msQuery != null) {
                        DSpectrum spectrum = msQuery.getDSpectrum();
                        if (spectrum != null) {
                            return spectrum;
                        }
                    }
                }
            }
        }
        return super.getData(getArray, parameterType);
    }

}
