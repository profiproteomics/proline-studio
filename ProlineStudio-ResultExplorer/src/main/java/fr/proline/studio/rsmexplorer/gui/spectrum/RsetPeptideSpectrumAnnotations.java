package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.List;



import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
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

import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.pattern.AbstractDataBox;

// created by AW
//
// purpose: to add amino acids annotations over the spectrum chart.
// 
public class RsetPeptideSpectrumAnnotations {

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

        //@SuppressWarnings("unchecked")
        List<XYAnnotation> annotationsList = p.getAnnotations();
        int lsize = annotationsList.size();
        for (int i = 0; i < lsize; i++) {
            p.removeAnnotation(annotationsList.get(i));
        }

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

        DMsQuery msQuery = m_peptideMatch.isMsQuerySet() ? m_peptideMatch.getMsQuery() : null;
        Spectrum spectrum = msQuery.isSpectrumSet() ? msQuery.getSpectrum() : null;

        PeptideFragmentationData.TheoreticalFragmentSeries_AW[] fragSer = m_peptideFragmentationData.getFragmentSeries();
        PeptideFragmentationData.FragmentMatch_AW[] fragMa = m_peptideFragmentationData.getFragmentMatch();



        byte[] intensityByteArray = spectrum.getIntensityList(); // package$EasyLzma$.MODULE$.uncompress(spectrum.getIntensityList());
        byte[] massByteArray = spectrum.getMozList(); // package$EasyLzma$.MODULE$.uncompress(spectrum.getMozList());
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




        int sizeMaxSeries = 0;
        for (int i = 0; i < fragSer.length; i++) { // TODO: en fait les frag
            // series b s'appliquent aussi a b++ etc. donc
            // va falloir faire un tableau de positions au lieu de juste Bposition
            if (fragSer[i].masses.length > sizeMaxSeries) {
                sizeMaxSeries = fragSer[i].masses.length;
            }

        }

        double[][] fragTableTheo = new double[11][sizeMaxSeries + 1];
        float[][] fragTableTheoCharge = new float[11][sizeMaxSeries + 1];
        double[][] fragTable = new double[11][sizeMaxSeries + 1];

        // **-*-*-* HERE READING Data from Objects *-*-*-*-**-

        String peptideSequence = m_peptideMatch.getPeptide().getSequence();

        removeAnnotations();
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

        int sizeABCserie = fragSer[positionIonABC].masses.length;
        int sizeXYZserie = fragSer[positionIonXYZ].masses.length;


        if (xyzSerieName.contains("z+1")) {
            xyzSerieName = "z"; // we keep only the char sequence instead of
        }									// full (ex: z+1 -> z)
        // à noter que 2 manières de faire les match. soit par égalité de
        // masse théo et match, ou bien par numéro de position sur le match.
        // exemple b(2) signifie sur le 2e element théorique ca matche. !!!
        // 1ere solution employée ici.
        // int i=0;
        // Here: filling the fragTables (theo and measured, before
        // displaying)
        j = 0;
        double roundTol = 0.000001;
        int nbFound = 0;
        int nbThroughB = 0;
        int nbThroughY = 0;
        for (j = 0; j < fragSer.length; j++) {
            // loop through theoFragment series here
            for (int k = 0; k < fragSer[j].masses.length; k++) {
                // loop through m_masses for each fragment series
                for (int i = 0; i < fragMa.length; i++) {
                    // find matching fragMatches with theoFragSeries
                    fragSer[j].computeChargeFromLabel();
                    fragMa[i].computeChargeFromLabel();
                    if (j == positionIonABC) {
                    	if(    fragMa[i].charge == fragSer[j].charge
    							&& fragMa[i].countSeq('*') == fragSer[j].countSeq('*')
    							&& fragMa[i].countSeq('0') == fragSer[j].countSeq('0'))
	                    {
                    	
	                        fragTableTheo[0][nbThroughB] = maxY - (maxY - minY) * 0.15; // data[1][i];
	                        // intensity for b ions
	                        fragTableTheo[1][nbThroughB] = fragSer[j].masses[k]; // data[0][i];
	                        fragTableTheoCharge[0][nbThroughB] = fragSer[j].charge;
	                        if ((fragMa[i].calculated_moz - roundTol <= (fragSer[j].masses[k]))
	                                && (fragMa[i].calculated_moz + roundTol >= fragSer[j].masses[k])) {
	                            nbFound++;
	                            fragTable[0][nbThroughB] = fragMa[i].intensity;
	                            fragTable[1][nbThroughB] = fragSer[j].masses[k];
	                            ;
	                        } else {
	                        }
  
		                }
                    }
                    if (j == positionIonXYZ) {
                    	if(    fragMa[i].charge == fragSer[j].charge
    							&& fragMa[i].countSeq('*') == fragSer[j].countSeq('*')
    							&& fragMa[i].countSeq('0') == fragSer[j].countSeq('0'))
    	                 {
	                    		
	                        fragTableTheo[5][nbThroughY] = maxY - (maxY - minY) * 0.25; // intensity
	                        fragTableTheo[6][nbThroughY] = fragSer[j].masses[k];
	                        fragTableTheoCharge[5][nbThroughY] = fragSer[j].charge;
	                        if ((fragMa[i].calculated_moz - roundTol <= fragSer[j].masses[k])
	                                && (fragMa[i].calculated_moz + roundTol >= fragSer[j].masses[k])) {
	                            nbFound++;
	                            fragTable[5][nbThroughY] = fragMa[i].intensity;
	                            fragTable[6][nbThroughY] = fragSer[j].masses[k];
	                        } else {
	                        }
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

        double abcPrev = fragTable[1][0] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(0));;

        boolean xyzPrevFound = false; // indicates if last iteration was a
        // match or not. (if yes then highlight the AA)
        boolean abcPrevFound = false;

        String surroundingCharacters = "";

       
        if (!abcSerieName.equals("")) {
        	 if(peptideSequence.length() < sizeABCserie )  // fill sequence in case of length problem. should not happen
 	        {
 	        	LoggerFactory.getLogger(
 	                    "ProlineStudio.ResultExplorer").error(
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
                if ((fragTable[1][i] != 0) || ((i == sizeABCserie - 1) && abcPrevFound)) // if some data
                {
                    if (i == (sizeABCserie - 1)) { // if last element to be highlighted
                        abcPrevFound = true;
                        fragTable[1][i] = abcPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    String aa = "" + peptideSequence.charAt(i);
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (abcPrev + fragTable[1][i]) / 2, maxY - (maxY - minY) * 0.15);
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
                    abcPrev = fragTableTheo[1][i]; // 
                    abcPrevFound = true;

                    if (!(i == sizeABCserie - 1)) {
                        // do not draw triangle and number if last element
                        // draw the triangle above the b number peak &
                        // draw the b number over the peak
                        final XYPointerAnnotation pointer = new XYPointerAnnotation(abcSerieName + (i + 1),
                                fragTableTheo[1][i],
                                fragTable[0][i] + (maxY - minY) * 0.055,
                                6.0 * Math.PI / 4.0);
                        pointer.setBaseRadius(5.0);
                        pointer.setTipRadius(0.0);
                        pointer.setArrowWidth(2);
                        pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        pointer.setArrowPaint(abc_serie_color);;
                        pointer.setPaint(abc_serie_color);
                        pointer.setTextAnchor(TextAnchor.BOTTOM_CENTER);
                        pointer.setToolTipText("<html>"
                                + "m/z: " + fragTable[1][i] + "<br>"
                                + "intensity: " + fragTable[0][i]
                                + "</html>");
                        plot.addAnnotation(pointer);

                        // dashed vertical bar over the b number
                        float yAboveBar = (float) ((maxY - minY) * 0.091);
                        float dash[] = {5.0f};
                        // draw only dashline if the y or b tag is not above the y/b line
                        if (fragTable[0][i] + yAboveBar < fragTableTheo[0][i]) {
                            BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                            XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[1][i], fragTable[0][i] + yAboveBar, fragTableTheo[1][i],
                                    fragTableTheo[0][i], stk, abc_serie_color);
                            plot.addAnnotation(line);
                        }
                    }
                } else // draw the regular expected (but not found) aa
                {
                    abcPrevFound = false;
                    String aa = "" + peptideSequence.charAt(i);
                    if (i == sizeABCserie - 1) { // last element not highlighted
                        fragTableTheo[1][i] = abcPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    if (i == 0) {
                        abcPrev = fragTableTheo[1][0] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (abcPrev + fragTableTheo[1][i]) / 2, maxY - (maxY - minY) * 0.15);
                    xyta.setPaint(abc_serie_color);
                    xyta.setFont(new Font(null, Font.BOLD, 11));
                    xyta.setBackgroundPaint(Color.white);
                    plot.addAnnotation(xyta);

                    abcPrev = fragTableTheo[1][i];
                    abcPrevFound = false;
                }
            }
        }

        //--------------------- xyz
        double xyzPrev = 0;
        //if(fragTable[6][0] != 0))	
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
                if ((fragTable[6][i] != 0) || ((i == 0) && xyzPrevFound)) // if some data
                {
                    if (i == 0) { // if last element to be highlighted
                        xyzPrevFound = true;
                        fragTable[6][i] = xyzPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    String aa = "" + peptideSequence.charAt(i);
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (xyzPrev + fragTable[6][i]) / 2, maxY - (maxY - minY) * 0.25);
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
                    xyzPrev = fragTableTheo[6][i]; // 
                    xyzPrevFound = true;

                    if (!(i == 0)) { // do not draw triangle and number if last element
                        // 	draw the b number over the peak &
                        // draw the triangle above the b number peak
                        xyta = new XYTextAnnotation("" /*
                                 * "\u25BE"
                                 */, fragTableTheo[6][i], fragTable[5][i] + (maxY - minY) * 0.01);
                        xyta.setPaint(xyz_serie_color);
                        plot.addAnnotation(xyta);
                        final XYPointerAnnotation pointer = new XYPointerAnnotation(xyzSerieName + (sizeXYZserie - i),
                                fragTableTheo[6][i],
                                fragTable[5][i] + (maxY - minY) * 0.01,
                                6.0 * Math.PI / 4.0);
                        pointer.setBaseRadius(5.0);
                        pointer.setTipRadius(0.0);
                        pointer.setArrowWidth(2);
                        pointer.setArrowPaint(xyz_serie_color);;
                        pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        pointer.setPaint(xyz_serie_color);
                        pointer.setTextAnchor(TextAnchor.BOTTOM_CENTER);
                        pointer.setToolTipText("<html>"
                                + "m/z: " + fragTable[6][i] + "<br>"
                                + "intensity: " + fragTable[5][i]
                                + "</html>");
                        plot.addAnnotation(pointer);


                        // dashed vertical bar over the b number
                        float yAboveBar = (float) ((maxY - minY) * 0.041);
                        float dash[] = {5.0f};
                        // draw only dashline if the y or b tag is not above the y/b line
                        if (fragTable[5][i] + yAboveBar < fragTableTheo[5][i]) {
                            BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                            XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[6][i], fragTable[5][i] + yAboveBar, fragTableTheo[6][i],
                                    fragTableTheo[5][i], stk, xyz_serie_color);
                            plot.addAnnotation(line);
                        }
                    }
                } else // draw the regular expected (but not found) aa
                {
                    String aa = "" + peptideSequence.charAt(i);
                    if (i == 0) { // first element not highlighted
                        fragTableTheo[6][i] = xyzPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    if (i == sizeXYZserie - 1) {
                        xyzPrev = fragTableTheo[6][i] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
                    }
                    xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (xyzPrev + fragTableTheo[6][i]) / 2, maxY - (maxY - minY) * 0.25);
                    xyta.setPaint(xyz_serie_color);
                    xyta.setFont(new Font(null, Font.BOLD, 11));
                    xyta.setBackgroundPaint(Color.white);
                    plot.addAnnotation(xyta);

                    xyzPrev = fragTableTheo[6][i];
                    xyzPrevFound = false;
                }
            }
        }


        }

    }

