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

import fr.proline.studio.pattern.xic.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Manage all databox and can find a databox which can be used
 *
 * @author JM235353
 */
public class DataboxManager {

    private static DataboxManager m_databoxManager = null;

    private final AbstractDataBox[] m_dataBoxStartingArray = {new DataBoxRsetAll(), new DataBoxRsetPSM(), new DataBoxRsetAllProteinMatch(),
        new DataBoxRsmPSM(), new DataBoxRsmPeptideInstances(), new DataBoxAdjacencyMatrixChoice(),
        new DataBoxRsmAllProteinSet(), new DataboxXicPeptideSet(), new DataboxXicPeptideIon(), new DataboxXicProteinSet(),
        new DataBoxMSQueriesForRSM(), new DataBoxMSQueriesForRset(), new DataBoxPTMClusters(), new DataBoxPTMClusters(true)};

    //VDS : If some databox takes parameter in constructor : config will be lost when adding the databox : newInstance called in AddDataBoxActionListener
    // Added specific code in AddDataBoxActionListener to configure these specific databox !
    private final AbstractDataBox[] m_dataBoxContinuingArray = {
        new DataBoxRsetPeptideSpectrum(), 
        new DataBoxRsetPeptideSpectrumValues(),
        new DataBoxRsetProteinsForPeptideMatch(),
        new DataboxRsetPeptidesOfProtein(),
        new DataBoxRsmPeptidesOfProtein(), 
        new DataBoxRsmProteinAndPeptideSequence(),
        new DataBoxRsmProteinSetOfPeptides(), 
        new DataBoxRsmProteinsOfProteinSet(),
        new DataBoxTaskDescription()/*, new DataBoxStatisticsFrequencyResponse()*/, 
        new DataBoxRsetPeptideFragmentation(),
        new DataBoxRsetPeptideSpectrumError(), 
        new DataboxRsmPSMOfProteinSet(), 
        new DataboxRsmPSMOfPeptide(),
        new DataboxGraphics(false), 
        new DataboxPSMOfMasterQuantPeptide(),
        new DataboxXicPeptideSet(), 
        new DataboxXicPeptideIon(), 
        new DataboxChildFeature(),
        new DataboxMultiGraphics(false, false,true), 
        new DataboxMultiGraphics(false, false),
        new DataboxMapAlignment(),
        new DataBoxAdjacencyMatrixChoice(), 
        new DataBoxAdjacencyMatrix(),
        new DataBoxRsmPSMForMsQuery(), 
        new DataboxRsetPSMForMsQuery(),
        new DataBoxPTMPeptides(false, false), // Ident PTMs Peptides
        new DataBoxPTMPeptides(true, false),  // Quanti PTMs Peptides
        new DataBoxPTMPeptides(false, true),  // Ident PTMs Peptides Matches
        new DataBoxPTMPeptides(true, true),    // Quanti PTMs Peptides Matches
        new DataBoxPTMPeptidesGraphic(),
        new DataboxXicParentsPeptideIon(),
        new DataBoxPTMClustersSites(),
        new DataBoxPTMClustersSites(true),
        new DataboxXicReporterIon()
    };

    public static AbstractDataBox getDataboxNewInstance(AbstractDataBox sourceDB) throws IllegalAccessException, InstantiationException {

        AbstractDataBox newGenericDatabox = null; // copy the databox
        try {
            newGenericDatabox = sourceDB.getClass().getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        //Some databox must be specifically configured ...
        // FIXME VDS : To be more generic ?!
        if(DataboxGraphics.class.isInstance(newGenericDatabox)) {
            ((DataboxGraphics)newGenericDatabox).setDefaultLocked(((DataboxGraphics)sourceDB).isDefaultLocked());
        } else if (DataboxMultiGraphics.class.isInstance(newGenericDatabox) ){
            newGenericDatabox = new DataboxMultiGraphics(false, false, ((DataboxMultiGraphics)sourceDB).isDoubleYAxis());
        } else if (DataBoxPTMPeptides.class.equals(newGenericDatabox.getClass())) {
            newGenericDatabox = new DataBoxPTMPeptides(((DataBoxPTMPeptides) sourceDB).isMS1LabelFreeQuantitation(), ((DataBoxPTMPeptides) sourceDB).isAllPSMsDisplayed());
        } else  if(DataBoxPTMClusters.class.equals(newGenericDatabox.getClass())) {
            newGenericDatabox = new DataBoxPTMClusters( ((DataBoxPTMClusters)sourceDB).m_type.equals(AbstractDataBox.DataboxType.DataBoxPTMSiteAsClusters) );
        } else  if(DataBoxPTMClustersSites.class.equals(newGenericDatabox.getClass())) {
            newGenericDatabox = new DataBoxPTMClustersSites( ((DataBoxPTMClustersSites)sourceDB).m_type.equals(AbstractDataBox.DataboxType.DataBoxXicPTMClustersSites) );
        }

        return  newGenericDatabox;
    }

    private DataboxManager() {
    }

    public static DataboxManager getDataboxManager() {
        if (m_databoxManager == null) {
            m_databoxManager = new DataboxManager();
        }
        return m_databoxManager;
    }

    public TreeMap<ParameterDistance, AbstractDataBox> findCompatibleStartingDataboxList(ArrayList<ParameterList> outParameters) {

        TreeMap<ParameterDistance, AbstractDataBox> compatibilityList = new TreeMap<>();
        for (int i = 0; i < m_dataBoxStartingArray.length; i++) {
            double averageDistance = m_dataBoxStartingArray[i].calculateParameterCompatibilityDistance(outParameters);
            if (averageDistance >= 0) {
                compatibilityList.put(new ParameterDistance(averageDistance), m_dataBoxStartingArray[i]);
            }
        }

        return compatibilityList;
    }

    public TreeMap<ParameterDistance, AbstractDataBox> findCompatibleDataboxList(AbstractDataBox previousDatabox, Class[] importantInParameter) {

        AvailableParameters avalaibleParameters = new AvailableParameters(previousDatabox);

        TreeMap<ParameterDistance, AbstractDataBox> compatibilityList = new TreeMap<>();
        if (importantInParameter != null) {
            for (int j = 0; j < importantInParameter.length; j++) {
                for (int i = 0; i < m_dataBoxContinuingArray.length; i++) {

                    AbstractDataBox databox = m_dataBoxContinuingArray[i];
                    if (databox.m_type.equals(previousDatabox.m_type)) {
                        // do not allow the same databox twice
                        continue;
                    }

                    double averageDistance = previousDatabox.calculateParameterCompatibilityDistance(avalaibleParameters, databox, importantInParameter[j]);
                    if (averageDistance >= 0) {
                        compatibilityList.put(new ParameterDistance(averageDistance), databox);
                    }
                }
            }
        } else {
            for (int i = 0; i < m_dataBoxContinuingArray.length; i++) {

                AbstractDataBox databox = m_dataBoxContinuingArray[i];
                if (databox.m_type.equals(previousDatabox.m_type)) {
                    // do not allow the same databox twice
                    continue;
                }

                double averageDistance = previousDatabox.calculateParameterCompatibilityDistance(avalaibleParameters, databox, null);
                if (averageDistance >= 0) {
                    compatibilityList.put(new ParameterDistance(averageDistance), databox);
                }
            }
        }

        return compatibilityList;
    }

}
