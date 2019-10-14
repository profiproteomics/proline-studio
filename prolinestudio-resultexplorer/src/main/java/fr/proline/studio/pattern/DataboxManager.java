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

import fr.proline.studio.pattern.xic.DataboxChildFeature;
import fr.proline.studio.pattern.xic.DataboxMapAlignment;
import fr.proline.studio.pattern.xic.DataboxPSMOfMasterQuantPeptide;
import fr.proline.studio.pattern.xic.DataboxXicPeptideIon;
import fr.proline.studio.pattern.xic.DataboxXicPeptideSet;
import fr.proline.studio.pattern.xic.DataboxXicProteinSet;
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
        new DataBoxMSQueriesForRSM(), new DataBoxMSQueriesForRset(), new DataBoxPTMSiteProtein(), new DataBoxPTMClusters()};

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
        new DataBoxPTMSitePeptides(), 
        new DataBoxPTMSitePeptidesGraphic(),
        new DataBoxPTMSitePepMatches(),
        new DataBoxPTMPeptides(false, false), // Ident PTMs Peptides
        new DataBoxPTMPeptides(true, false),  // Quanti PTMs Peptides
        new DataBoxPTMPeptides(false, true),  // Ident PTMs Peptides Matches
        new DataBoxPTMPeptides(true, true),    // Quanti PTMs Peptides Matches
        new DataBoxPTMPeptidesGraphic()        
        
    };

    private DataboxManager() {
    }

    public static DataboxManager getDataboxManager() {
        if (m_databoxManager == null) {
            m_databoxManager = new DataboxManager();
        }
        return m_databoxManager;
    }

    public TreeMap<ParameterDistance, AbstractDataBox> findCompatibleStartingDataboxList(ArrayList<GroupParameter> outParameters) {

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
                    if (databox.getClass().equals(previousDatabox.getClass())) {
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
