package fr.proline.studio.rsmexplorer.spectrum;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public class SpectrumFragmentationUtil {
    
    private static HashMap<Character, Double> m_aaMassHashMap = null;
    private static HashMap<Double, Character> m_aaNameHashMap = null;
    private static NumberFormat m_aaDeltaMassFormatter = null;
    
    public static double getMassFromAminoAcid(char aa) {

        if (m_aaMassHashMap == null) {
            m_aaMassHashMap = new HashMap<>();
            m_aaMassHashMap.put('A', (double) 71.03711);
            m_aaMassHashMap.put('C', (double) 103.00919);
            m_aaMassHashMap.put('D', (double) 115.02694);
            m_aaMassHashMap.put('E', (double) 129.04259);
            m_aaMassHashMap.put('F', (double) 147.06841);
            m_aaMassHashMap.put('G', (double) 57.02146);
            m_aaMassHashMap.put('H', (double) 137.05891);
            m_aaMassHashMap.put('I', (double) 113.08406);
            m_aaMassHashMap.put('K', (double) 128.09496);
            m_aaMassHashMap.put('L', (double) 113.08406);
            m_aaMassHashMap.put('M', (double) 131.04049);
            m_aaMassHashMap.put('N', (double) 114.04293);
            m_aaMassHashMap.put('P', (double) 97.05276);
            m_aaMassHashMap.put('Q', (double) 128.05858);
            m_aaMassHashMap.put('R', (double) 156.10111);
            m_aaMassHashMap.put('S', (double) 87.03203);
            m_aaMassHashMap.put('T', (double) 101.04768);
            m_aaMassHashMap.put('V', (double) 99.06841);
            m_aaMassHashMap.put('W', (double) 186.07931);
            m_aaMassHashMap.put('Y', (double) 163.06333);
        }


        return m_aaMassHashMap.get(aa);

    }

    // the getAminoAcidName is not used but could be in the future...
    public static String getAminoAcidName(double deltaMass, double tolerance) {

        // scan the spectrum to find potential aminoacids
        if (m_aaNameHashMap == null) {
            m_aaNameHashMap = new HashMap<>();

            m_aaNameHashMap.put((double) 71.03711, 'A');
            m_aaNameHashMap.put((double) 103.00919, 'C');
            m_aaNameHashMap.put((double) 115.02694, 'D');
            m_aaNameHashMap.put((double) 129.04259, 'E');
            m_aaNameHashMap.put((double) 147.06841, 'F');
            m_aaNameHashMap.put((double) 57.02146, 'G');
            m_aaNameHashMap.put((double) 137.05891, 'H');
            m_aaNameHashMap.put((double) 113.08406, 'I');
            m_aaNameHashMap.put((double) 128.09496, 'K');
            m_aaNameHashMap.put((double) 113.08406, 'L');
            m_aaNameHashMap.put((double) 131.04049, 'M');
            m_aaNameHashMap.put((double) 114.04293, 'N');
            m_aaNameHashMap.put((double) 97.05276, 'P');
            m_aaNameHashMap.put((double) 128.05858, 'Q');
            m_aaNameHashMap.put((double) 156.10111, 'R');
            m_aaNameHashMap.put((double) 87.03203, 'S');
            m_aaNameHashMap.put((double) 101.04768, 'T');
            m_aaNameHashMap.put((double) 99.06841, 'V');
            m_aaNameHashMap.put((double) 186.07931, 'W');
            m_aaNameHashMap.put((double) 163.06333, 'Y');

            m_aaDeltaMassFormatter = new DecimalFormat("#0.000");
        }

        double toleranceCalc = tolerance;
        //System.out.println("--->Submitted mass of " + deltaMass);
        for (double aaMass : m_aaNameHashMap.keySet()) {
            if ((aaMass - toleranceCalc < deltaMass) && (aaMass + toleranceCalc > deltaMass)) {
                return (m_aaNameHashMap.get(aaMass).toString());
            }
        }


        return m_aaDeltaMassFormatter.format(deltaMass);

    }
    
    
    
}
