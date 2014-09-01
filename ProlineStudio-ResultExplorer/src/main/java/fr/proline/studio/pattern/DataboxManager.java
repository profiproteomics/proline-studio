package fr.proline.studio.pattern;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Manage all databox and can find a databox which can be used
 * @author JM235353
 */
public class DataboxManager {
    
    private static DataboxManager m_databoxManager = null;
    
    
    private AbstractDataBox[] m_dataBoxStartingArray = { new DataBoxRsetAll(), new DataBoxRsetPeptide(), new DataBoxRsetAllProteinMatch(),
                                                 new DataBoxRsmPeptide(), new DataBoxRsmPeptideInstances(),
                                                 new DataBoxRsmAllProteinSet() };
    
    private AbstractDataBox[] m_dataBoxContinuingArray = { new DataBoxRsetPeptideSpectrum(),
                                                 new DataBoxRsetProteinsForPeptideMatch(),
                                                 new DataboxRsetPeptidesOfProtein(),
                                                 new DataBoxRsmPeptidesOfProtein(), new DataBoxRsmProteinAndPeptideSequence(),
                                                 new DataBoxRsmProteinSetOfPeptides(), new DataBoxRsmProteinsOfProteinSet(), new DataBoxTaskDescription(), new DataBoxStatisticsFrequencyResponse(), new DataBoxRsetPeptideFragmentation() };
    
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
    
    public TreeMap<ParameterDistance, AbstractDataBox> findCompatibleDataboxList(AbstractDataBox previousDatabox) {
        
        AvailableParameters avalaibleParameters = new AvailableParameters(previousDatabox);
        
        TreeMap<ParameterDistance, AbstractDataBox> compatibilityList = new TreeMap<>();
        for (int i=0;i<m_dataBoxContinuingArray.length;i++) {

            AbstractDataBox databox =  m_dataBoxContinuingArray[i];
            if (databox.getClass().equals(previousDatabox.getClass())) {
                // do not allow the same databox twice
                continue;
            }
            
            double averageDistance = previousDatabox.calculateParameterCompatibilityDistance(avalaibleParameters,databox);
            if (averageDistance >=0) {
                compatibilityList.put(new ParameterDistance(averageDistance), databox);
            }
        }
        
        return compatibilityList;
    }
    
}
