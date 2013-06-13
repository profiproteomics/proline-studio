package fr.proline.studio.pattern;

import java.util.ArrayList;

/**
 * Manage all databox and can find a databox which can be used
 * @author JM235353
 */
public class DataboxManager {
    
    private static DataboxManager m_databoxManager = null;
    
    private AbstractDataBox[] m_dataBoxArray = { new DataBoxRsetAll(), new DataBoxRsetPeptide(), new DataBoxRsetPeptideSpectrum(),
                                                 new DataBoxRsetProteinsForPeptideMatch(), new DataBoxRsmPeptide(), new DataBoxRsmPeptideInstances(),
                                                 new DataBoxRsmPeptidesOfProtein(), new DataBoxRsmProteinAndPeptideSequence(), new DataBoxRsmProteinSet(true),
                                                 new DataBoxRsmProteinSet(false), new DataBoxRsmProteinsOfProteinSet() };
    
    private DataboxManager() {
    }
    
    public static DataboxManager getDataboxManager() {
        if (m_databoxManager == null) {
            m_databoxManager = new DataboxManager();
        }
        return m_databoxManager;
    }
    
    public ArrayList<AbstractDataBox> findCompatibleDataboxList(ArrayList<DataParameter> outParameters) {
        
        ArrayList<AbstractDataBox> compatibilityList = new ArrayList<>();
        for (int i=0;i<m_dataBoxArray.length;i++) {
            if (m_dataBoxArray[i].isCompatible(outParameters)) {
                compatibilityList.add(m_dataBoxArray[i]);
            }
        }
        
        return compatibilityList;
    }
    
    public ArrayList<AbstractDataBox> findCompatibleDataboxList(AbstractDataBox previousDatabox) {
        
        ArrayList<AbstractDataBox> compatibilityList = new ArrayList<>();
        for (int i=0;i<m_dataBoxArray.length;i++) {
            if (previousDatabox.isCompatible(m_dataBoxArray[i])) {
                compatibilityList.add(m_dataBoxArray[i]);
            }
        }
        
        return compatibilityList;
    }
    
}
