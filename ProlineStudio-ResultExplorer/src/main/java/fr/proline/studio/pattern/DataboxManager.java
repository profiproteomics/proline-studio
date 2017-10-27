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
 * @author JM235353
 */
public class DataboxManager {
    
    private static DataboxManager m_databoxManager = null;
    
    
    private final AbstractDataBox[] m_dataBoxStartingArray = { new DataBoxRsetAll(), new DataBoxRsetPSM(), new DataBoxRsetAllProteinMatch(),
                                                 new DataBoxRsmPSM(), new DataBoxRsmPeptideInstances(), new DataBoxAdjacencyMatrixChoice(),
                                                 new DataBoxRsmAllProteinSet(), new DataboxXicPeptideSet(), new DataboxXicPeptideIon(), new DataboxXicProteinSet(), 
                                                new DataBoxMSQueriesForRSM(), new DataBoxMSQueriesForRset(), new DataBoxPTMProteinSite(), new DataBoxPTMProteinSite_V2() };
    
    private final AbstractDataBox[] m_dataBoxContinuingArray = { new DataBoxRsetPeptideSpectrum(), new DataBoxRsetPeptideSpectrumValues(),
                                                 new DataBoxRsetProteinsForPeptideMatch(),
                                                 new DataboxRsetPeptidesOfProtein(),
                                                 new DataBoxRsmPeptidesOfProtein(), new DataBoxRsmProteinAndPeptideSequence(),
                                                 new DataBoxRsmProteinSetOfPeptides(), new DataBoxRsmProteinsOfProteinSet(), 
                                                 new DataBoxTaskDescription()/*, new DataBoxStatisticsFrequencyResponse()*/, new DataBoxRsetPeptideFragmentation(), 
                                                 new DataBoxRsetPeptideSpectrumError(), new DataboxRsmPSMOfProteinSet(), new DataboxRsmPSMOfPeptide(), 
                                                 new DataboxGraphics(false), new DataboxPSMOfMasterQuantPeptide() , 
                                                 new DataboxXicPeptideSet(), new DataboxXicPeptideIon(), new DataboxChildFeature(), new DataboxMultiGraphics(false, false), 
                                                 new DataboxMapAlignment(),
                                                 new DataBoxAdjacencyMatrixChoice() , new DataBoxAdjacencyMatrix(),
                                                new DataboxRSMPSMForMsQuery(), new DataboxRsetPSMForMsQuery()};
    
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
        for (int i=0;i<m_dataBoxStartingArray.length;i++) {
            double averageDistance = m_dataBoxStartingArray[i].calculateParameterCompatibilityDistance(outParameters);
            if (averageDistance >=0) {
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
                if (databox.getClass().equals(previousDatabox.getClass())) {
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
