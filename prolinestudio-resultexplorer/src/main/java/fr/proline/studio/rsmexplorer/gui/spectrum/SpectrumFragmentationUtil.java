package fr.proline.studio.rsmexplorer.gui.spectrum;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class SpectrumFragmentationUtil {
    
    private static HashMap<Character, Double> m_aaMassHashMap = null;

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
            m_aaMassHashMap.put('?', (double) 100);
        }
        return m_aaMassHashMap.get(aa);
    }


}
