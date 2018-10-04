package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;



import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;


import org.slf4j.LoggerFactory;

import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.pattern.AbstractDataBox;

// created by AW
//
// purpose: to add amino acids annotations over the spectrum chart.
// 
public class RsetPeptideSpectrumAnnotations {

    private static final int ABC_SERIE_LABEL_YPOS = 0;
    private static final int ABC_SERIE_LABEL_XPOS = 1;
    private static final int XYZ_SERIE_LABEL_YPOS = 2;
    private static final int XYZ_SERIE_LABEL_XPOS = 3;

    private AbstractDataBox m_dataBox;
    private DefaultXYDataset m_dataSet;
    private JFreeChart m_chart;
    private DPeptideMatch m_peptideMatch;
    private PeptideFragmentationData m_peptideFragmentationData;

    public RsetPeptideSpectrumAnnotations(AbstractDataBox dBox, DefaultXYDataset dSet, JFreeChart chrt, DPeptideMatch pepMatch, PeptideFragmentationData peptideFragmentationData) {
        m_dataBox = dBox;
        m_dataSet = dSet;
        m_chart = chrt;
        m_peptideMatch = pepMatch;
        m_peptideFragmentationData = peptideFragmentationData;
    }

    private void removeAnnotations() {
        XYPlot p = (XYPlot) m_chart.getPlot();
        p.clearAnnotations();
        p.clearRangeMarkers();
    }

    public void addAnnotations() {

        removeAnnotations();
        
        if ((m_peptideMatch == null) || (m_peptideFragmentationData == null)) {
            return;
        }
    	if(m_peptideFragmentationData.isEmpty) {
        	return;
        }
        if(!m_peptideMatch.isMsQuerySet() || !m_peptideMatch.getMsQuery().isSpectrumFullySet())
            return;

        DSpectrum spectrum = m_peptideMatch.getMsQuery().getDSpectrum();

        PeptideFragmentationData.TheoreticalFragmentSeries[] fragSer = m_peptideFragmentationData.getFragmentSeries();
        PeptideFragmentationData.FragmentMatch[] fragMa = m_peptideFragmentationData.getFragmentMatch();

        byte[] intensityByteArray = spectrum.getIntensityList(); 
        byte[] massByteArray = spectrum.getMozList(); 
        ByteBuffer intensityByteBuffer = ByteBuffer.wrap(intensityByteArray).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer intensityFloatBuffer = intensityByteBuffer.asFloatBuffer();
        double[] intensityDoubleArray = new double[intensityFloatBuffer.remaining()];
        for (int i = 0; i < intensityDoubleArray.length; i++) {
            intensityDoubleArray[i] = (double) intensityFloatBuffer.get();
        }

        ByteBuffer massByteBuffer = ByteBuffer.wrap(massByteArray).order(ByteOrder.LITTLE_ENDIAN);
        DoubleBuffer massDoubleBuffer = massByteBuffer.asDoubleBuffer();
        double[] massDoubleArray = new double[massDoubleBuffer.remaining()];
        for (int i = 0; i < massDoubleArray.length; i++) {
            massDoubleArray[i] = massDoubleBuffer.get();
        }

        // get all the data to be plot
        int dataSize = intensityDoubleArray.length;
        double[][] data = new double[2][dataSize];
        for (int i = 0; i < dataSize; i++) {
            data[0][i] = massDoubleArray[i];
            data[1][i] = intensityDoubleArray[i];
        }

        String peptideSequence = m_peptideMatch.getPeptide().getSequence();
        int sizeMaxSeries = peptideSequence.length();

        double[][] fragTableTheo = new double[4][sizeMaxSeries + 1];
        double[][] fragTable = new double[4][sizeMaxSeries + 1];

        // **-*-*-* HERE READING Data from Objects *-*-*-*-**-

        removeAnnotations(); //VDS Done at start of method !
        XYTextAnnotation xyta;
        XYPlot plot = (XYPlot) m_chart.getPlot();

        double minY = (float) plot.getRangeAxis().getLowerBound(); // this is the Y data range
        double maxY = (float) plot.getRangeAxis().getUpperBound();

        Color abc_serie_color = new Color(51, 153, 255);
        Color xyz_serie_color = new Color(255, 85, 85);

        int j = 0;
        // ************************************************************
        // *-*-*- Load fragmentation table (theoretical and measured) *
        // ************************************************************

        // String peptideSequence = "RVPPLG";
        int positionIonABC = 0;
        int positionIonXYZ = 0;
        String xyzSerieName = "";
        String abcSerieName = "";
        for (int i = 0; i < fragSer.length; i++) {

            switch (fragSer[i].frag_series.charAt(0)) {

                case 'a': // either a,b or c do:
                case 'b':
                case 'c':
                    if (fragSer[i].frag_series.length() > 1) {
                        // then it is either a ++ or a b-H2O and so on...
                    } else { // it's a 'a/b/c' ion
                    	if(!abcSerieName.equals("b")) {// only if b not already defined, else we keep b
	                        positionIonABC = i;
	                        abcSerieName = "" + fragSer[i].frag_series;
                        }
                    }
                    break;
                case 'v':
                case 'w':
                case 'x':
                case 'y':

                    if (fragSer[i].frag_series.length() > 1) {
                        // then it is either a ++ or a b-H2O and so on...
                    } else { // it's a 'x/y/z' ion
                    	if(!xyzSerieName.equals("y")) {// only if b not already defined, else we keep b
	                        xyzSerieName = "" + fragSer[i].frag_series;
	                        positionIonXYZ = i;
                    	}
                    }
                    break;
                case 'z':
                    if (fragSer[i].frag_series.length() == 3) {
                        if (fragSer[i].frag_series.equals("z+1")) {
                        	if(!xyzSerieName.equals("z")) {// only if y not already defined, else we keep b
	                            xyzSerieName = "(z+1)";
	                            positionIonXYZ = i;
                        	}
                        }
                    }
                    break;
                default:
                    break;
            }

        }

        plot.clearRangeMarkers();
        Marker target = new ValueMarker(maxY - (maxY - minY) * 0.25);
        target.setPaint(xyz_serie_color);
        target.setLabel(xyzSerieName);
        target.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        plot.addRangeMarker(target);
        Marker target2 = new ValueMarker(maxY - (maxY - minY) * 0.15);
        target2.setPaint(abc_serie_color);
        target2.setLabel(abcSerieName);
        target2.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        target2.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        plot.addRangeMarker(target2);

        int sizeABCserie = (fragSer.length==0) ? 0 : fragSer[positionIonABC].masses.length;
        int sizeXYZserie = (fragSer.length==0) ? 0 : fragSer[positionIonXYZ].masses.length;


        if (xyzSerieName.contains("z+1")) {
            xyzSerieName = "z"; // we keep only the char sequence instead of full (ex: z+1 -> z)
        }									
 
        // Here: filling the fragTables (theo and measured, before displaying)

        int nbThroughB = 0;
        int nbThroughY = 0;
        
        for (j = 0; j < fragSer.length; j++) {
            // loop through theoFragment series here
            for (int k = 0; k < fragSer[j].masses.length; k++) {
                // loop through m_masses for each fragment series
            	 fragTableTheo[ABC_SERIE_LABEL_YPOS][nbThroughB] = maxY - (maxY - minY) * 0.15; // intensity for b ions
                 fragTableTheo[ABC_SERIE_LABEL_XPOS][nbThroughB] = fragSer[j].masses[k];
                 fragTableTheo[XYZ_SERIE_LABEL_YPOS][nbThroughY] = maxY - (maxY - minY) * 0.25; // intensity for y ions
                 fragTableTheo[XYZ_SERIE_LABEL_XPOS][nbThroughY] = fragSer[j].masses[k];
                 for (int i = 0; i < fragMa.length; i++) {
                    // find matching fragMatches with theoFragSeries
                    if (j == positionIonABC) {
                    	if ((fragMa[i].getCharge() == fragSer[j].getCharge())
                                && fragMa[i].getSeriesName().equals(fragSer[j].frag_series) 
                                && fragMa[i].getPosition() == nbThroughB+1 ) {
	                            fragTable[ABC_SERIE_LABEL_YPOS][nbThroughB] = fragMa[i].intensity;
	                            fragTable[ABC_SERIE_LABEL_XPOS][nbThroughB] = fragSer[j].masses[k];    
		          }
                    }
                    if (j == positionIonXYZ) {
                    	if ( (fragMa[i].getCharge() == fragSer[j].getCharge()) 
                                && fragMa[i].getSeriesName().equals(fragSer[j].frag_series) 
                                && (sizeMaxSeries - fragMa[i].getPosition()) == nbThroughY ) {
	                            fragTable[XYZ_SERIE_LABEL_YPOS][nbThroughY] = fragMa[i].intensity;
	                            fragTable[XYZ_SERIE_LABEL_XPOS][nbThroughY] = fragSer[j].masses[k];
                    	}
                    }
                }
                 
                if (j == positionIonABC) {
                    nbThroughB++;
                }
                if (j == positionIonXYZ) {
                    nbThroughY++;
                }
            }
        }

        double abcPrev = fragTable[ABC_SERIE_LABEL_XPOS][0] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(0));;

        boolean xyzPrevFound = false; // indicates if last iteration was a match or not. (if yes then highlight the AA)
        boolean abcPrevFound = false;
        String surroundingCharacters = "";
        
        if (!abcSerieName.equals("")) {
        	 if(peptideSequence.length() < sizeABCserie )  // fill sequence in case of length problem. should not happen
 	        {
 	        	LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(
 	                    "AW: strange, serie length is not same length as sequence length...serie length:"
 	                    + sizeABCserie + " for sequence " + peptideSequence);
 	        	for(int filler = 0 ; filler < (sizeABCserie - peptideSequence.length()); filler++) {
 	        		peptideSequence = peptideSequence + "?";	
 	        	}
 	        }    
            for (int i = 0; i < sizeABCserie; i++) { // loop through the series points


                // place separators marks------
                if (abcPrev != 0 && i > 0) {
                    float dash[] = {10.0f};
                    BasicStroke stk = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                    XYLineAnnotation line = new XYLineAnnotation(abcPrev, maxY - (maxY - minY) * 0.14,
                            abcPrev, maxY - (maxY - minY) * 0.16, stk, abc_serie_color);
                    plot.addAnnotation(line);
                }

                // draw the outlined AA : B series
                if ((fragTable[ABC_SERIE_LABEL_XPOS][i] != 0) || ((i == sizeABCserie - 1) && abcPrevFound)) // if some data
                {
                    if (i == (sizeABCserie - 1)) { // if last element to be highlighted
                        abcPrevFound = true;
                        fragTable[ABC_SERIE_LABEL_XPOS][i] = abcPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    String aa = "" + peptideSequence.charAt(i);
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (abcPrev + fragTable[ABC_SERIE_LABEL_XPOS][i]) / 2, maxY - (maxY - minY) * 0.15);
                    if (abcPrevFound || i == 0 || i == (sizeABCserie - 1)) {
                        // 2 consecutives fragments matching, or first element or last element, then highlight the AA
                        xyta.setPaint(Color.white);
                        xyta.setBackgroundPaint(abc_serie_color);

                    } else {
                        xyta.setPaint(abc_serie_color);
                        xyta.setBackgroundPaint(Color.white);
                    }
                    xyta.setFont(new Font(null, Font.BOLD, 11));
                    plot.addAnnotation(xyta);
                    abcPrev = fragTableTheo[ABC_SERIE_LABEL_XPOS][i]; // 
                    abcPrevFound = true;

                    if (!(i == sizeABCserie - 1)) {
                        // do not draw triangle and number if last element
                        // draw the triangle above the b number peak &
                        // draw the b number over the peak
                        final XYPointerAnnotation pointer = new XYPointerAnnotation(abcSerieName + (i + 1),
                                fragTableTheo[ABC_SERIE_LABEL_XPOS][i],
                                fragTable[ABC_SERIE_LABEL_YPOS][i] + (maxY - minY) * 0.055, 6.0 * Math.PI / 4.0);
                        pointer.setBaseRadius(5.0);
                        pointer.setTipRadius(0.0);
                        pointer.setArrowWidth(2);
                        pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        pointer.setArrowPaint(abc_serie_color);;
                        pointer.setPaint(abc_serie_color);
                        pointer.setTextAnchor(TextAnchor.BOTTOM_CENTER);
                        pointer.setToolTipText("<html>"
                                + "m/z: " + fragTable[ABC_SERIE_LABEL_XPOS][i] + "<br>"
                                + "intensity: " + fragTable[ABC_SERIE_LABEL_YPOS][i]
                                + "</html>");
                        plot.addAnnotation(pointer);

                        // dashed vertical bar over the b number
                        float yAboveBar = (float) ((maxY - minY) * 0.091);
                        float dash[] = {5.0f};
                        // draw only dashline if the y or b tag is not above the y/b line
                        if (fragTable[ABC_SERIE_LABEL_YPOS][i] + yAboveBar < fragTableTheo[ABC_SERIE_LABEL_YPOS][i]) {
                            BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                            XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[ABC_SERIE_LABEL_XPOS][i], fragTable[ABC_SERIE_LABEL_YPOS][i] + yAboveBar, fragTableTheo[ABC_SERIE_LABEL_XPOS][i],
                                    fragTableTheo[ABC_SERIE_LABEL_YPOS][i], stk, abc_serie_color);
                            plot.addAnnotation(line);
                        }
                    }
                } else // draw the regular expected (but not found) aa
                {
                    abcPrevFound = false;
                    String aa = "" + peptideSequence.charAt(i);
                    if (i == sizeABCserie - 1) { // last element not highlighted
                        fragTableTheo[ABC_SERIE_LABEL_XPOS][i] = abcPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    if (i == 0) {
                        abcPrev = fragTableTheo[ABC_SERIE_LABEL_XPOS][0] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (abcPrev + fragTableTheo[ABC_SERIE_LABEL_XPOS][i]) / 2, maxY - (maxY - minY) * 0.15);
                    xyta.setPaint(abc_serie_color);
                    xyta.setFont(new Font(null, Font.BOLD, 11));
                    xyta.setBackgroundPaint(Color.white);
                    plot.addAnnotation(xyta);

                    abcPrev = fragTableTheo[ABC_SERIE_LABEL_XPOS][i];
                    abcPrevFound = false;
                }
            }
        }

        //--------------------- xyz
        double xyzPrev = 0;
        if (!xyzSerieName.equals("")) {
            for (int i = sizeXYZserie - 1; i >= 0; i--) { // loop through the series points


                // place separators marks------
                if (xyzPrev != 0) {
                    float dash[] = {10.0f};
                    BasicStroke stk = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                    XYLineAnnotation line = new XYLineAnnotation(xyzPrev, maxY - (maxY - minY) * 0.24,
                            xyzPrev, maxY - (maxY - minY) * 0.26, stk, xyz_serie_color);
                    plot.addAnnotation(line);
                }

                // draw the outlined AA : Y series
                if ((fragTable[XYZ_SERIE_LABEL_XPOS][i] != 0) || ((i == 0) && xyzPrevFound)) // if some data
                {
                    if (i == 0) { // if last element to be highlighted
                        xyzPrevFound = true;
                        fragTable[XYZ_SERIE_LABEL_XPOS][i] = xyzPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    String aa = "" + peptideSequence.charAt(i);
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (xyzPrev + fragTable[XYZ_SERIE_LABEL_XPOS][i]) / 2, maxY - (maxY - minY) * 0.25);
                    if (xyzPrevFound
                            || i == sizeXYZserie - 1
                            || i == 0) {// 2 consecutives fragments matching,
                        // or first element or last element, then highlight the AA
                        xyta.setPaint(Color.white);
                        xyta.setBackgroundPaint(xyz_serie_color);

                    } else {
                        xyta.setPaint(xyz_serie_color);
                        xyta.setBackgroundPaint(Color.white);
                    }
                    xyta.setFont(new Font(null, Font.BOLD, 11));
                    plot.addAnnotation(xyta);
                    xyzPrev = fragTableTheo[XYZ_SERIE_LABEL_XPOS][i]; // 
                    xyzPrevFound = true;

                    if (!(i == 0)) { // do not draw triangle and number if last element
                        // 	draw the b number over the peak &
                        // draw the triangle above the b number peak
                        xyta = new XYTextAnnotation("" /*
                                 * "\u25BE"
                                 */, fragTableTheo[XYZ_SERIE_LABEL_XPOS][i], fragTable[XYZ_SERIE_LABEL_YPOS][i] + (maxY - minY) * 0.01);
                        xyta.setPaint(xyz_serie_color);
                        plot.addAnnotation(xyta);
                        final XYPointerAnnotation pointer = new XYPointerAnnotation(xyzSerieName + (sizeXYZserie - i),
                                fragTableTheo[XYZ_SERIE_LABEL_XPOS][i],
                                fragTable[XYZ_SERIE_LABEL_YPOS][i] + (maxY - minY) * 0.01,
                                6.0 * Math.PI / 4.0);
                        pointer.setBaseRadius(5.0);
                        pointer.setTipRadius(0.0);
                        pointer.setArrowWidth(2);
                        pointer.setArrowPaint(xyz_serie_color);;
                        pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        pointer.setPaint(xyz_serie_color);
                        pointer.setTextAnchor(TextAnchor.BOTTOM_CENTER);
                        pointer.setToolTipText("<html>"
                                + "m/z: " + fragTable[XYZ_SERIE_LABEL_XPOS][i] + "<br>"
                                + "intensity: " + fragTable[XYZ_SERIE_LABEL_YPOS][i]
                                + "</html>");
                        plot.addAnnotation(pointer);


                        // dashed vertical bar over the b number
                        float yAboveBar = (float) ((maxY - minY) * 0.041);
                        float dash[] = {5.0f};
                        // draw only dashline if the y or b tag is not above the y/b line
                        if (fragTable[XYZ_SERIE_LABEL_YPOS][i] + yAboveBar < fragTableTheo[XYZ_SERIE_LABEL_YPOS][i]) {
                            BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                            XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[XYZ_SERIE_LABEL_XPOS][i], fragTable[XYZ_SERIE_LABEL_YPOS][i] + yAboveBar, fragTableTheo[XYZ_SERIE_LABEL_XPOS][i],
                                    fragTableTheo[XYZ_SERIE_LABEL_YPOS][i], stk, xyz_serie_color);
                            plot.addAnnotation(line);
                        }
                    }
                } else // draw the regular expected (but not found) aa
                {
                    String aa = "" + peptideSequence.charAt(i);
                    if (i == 0) { // first element not highlighted
                        fragTableTheo[XYZ_SERIE_LABEL_XPOS][i] = xyzPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    if (i == sizeXYZserie - 1) {
                        xyzPrev = fragTableTheo[XYZ_SERIE_LABEL_XPOS][i] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (xyzPrev + fragTableTheo[XYZ_SERIE_LABEL_XPOS][i]) / 2, maxY - (maxY - minY) * 0.25);
                    xyta.setPaint(xyz_serie_color);
                    xyta.setFont(new Font(null, Font.BOLD, 11));
                    xyta.setBackgroundPaint(Color.white);
                    plot.addAnnotation(xyta);

                    xyzPrev = fragTableTheo[XYZ_SERIE_LABEL_XPOS][i];
                    xyzPrevFound = false;
                }
            }
        }


        }

    }

