package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.MathContext;
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
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.LoggerFactory;

import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

// created by AW
//
// purpose: to add error annotations over the spectrum chart.
// 
public class RsetPeptideSpectrumErrorAnnotations {


    JFreeChart m_chart;
    DPeptideMatch m_peptideMatch;
    private PeptideFragmentationData m_peptideFragmentationData;
    double m_spectrumMaxY = 0; // y range from data...will be updated and then available for caller.
    double m_spectrumMinY = 0;

    public RsetPeptideSpectrumErrorAnnotations(JFreeChart chrt, DPeptideMatch pepMatch, PeptideFragmentationData peptideFragmentationData) {

        m_chart = chrt;
        m_peptideMatch = pepMatch;
        m_peptideFragmentationData = peptideFragmentationData;
    }

    private void removeErrorAnnotations() {
        XYPlot p = (XYPlot) m_chart.getPlot();

        //@SuppressWarnings("unchecked")
        List<XYAnnotation> annotationsList = p.getAnnotations();
        int lsize = annotationsList.size();
        for (int i = 0; i < lsize; i++) {
            p.removeAnnotation(annotationsList.get(i));
        }

        p.clearRangeMarkers();
    }

    public void addErrorAnnotations() {

        removeErrorAnnotations();

        if ((m_peptideMatch == null) || (m_peptideFragmentationData == null)) {
            return;
        }

        if(m_peptideFragmentationData.isEmpty) {
        	return;
        }
    	
        DMsQuery msQuery = m_peptideMatch.isMsQuerySet() ? m_peptideMatch.getMsQuery() : null;
        Spectrum spectrum = msQuery.isSpectrumSet() ? msQuery.getSpectrum() : null;

        if (spectrum == null) {
            return;
        }
        
        PeptideFragmentationData.TheoreticalFragmentSeries_AW[] fragSer = m_peptideFragmentationData.getFragmentSeries();
        PeptideFragmentationData.FragmentMatch_AW[] fragMa = m_peptideFragmentationData.getFragmentMatch();


        //double m_precursorMass = spectrum.getPrecursorMoz()*spectrum.getPrecursorCharge(); // used for setting spectrum display range
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
            // series b s'appliquent
            // aussi a b++ etc. donc
            // va falloir faire un
            // tableau de positions
            // au lieu de juste
            // Bposition
            if (fragSer[i].masses.length > sizeMaxSeries) {
                sizeMaxSeries = fragSer[i].masses.length;
            }

        }

        double[][] fragTableTheo = new double[11][sizeMaxSeries + 1];
        float[][] fragTableTheoCharge = new float[11][sizeMaxSeries + 1];
        double[][] fragTable = new double[11][sizeMaxSeries + 1]; // will contain theo frag mass and measured one


        // doc: fragTable[0][i] contains intensity
        //		fragTableTheo[0][i] contains top position (aa axis)
        //		fragTable[1][i] contains measured mass
        //		fragTableTheo[0][i] contains theoretical mass
        // in this module we replace fragTable[0][i] with the mass difference between theoretical and measured
        // **-*-*-* HERE READING Data from Objects *-*-*-*-**-

        String peptideSequence = m_peptideMatch.getPeptide().getSequence();

        removeErrorAnnotations();
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

        int positionIonABC = 0;
        int positionIonXYZ = 0;
        String xyzSerieName = "";
        String abcSerieName = "";
        for (int i = 0; i < fragSer.length; i++) {

            switch (fragSer[i].frag_series.charAt(0)) {

            
            // this strategy is to be reviewed. in a next version, we'll be able to choose with serie to display
            // (b and y by default on the legend axes) and show anyway all series on the graph

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

       

        // draw horizontal axes for abc, xyz serie name (by default b and y series)
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
         

        int sizeABCserie = 0;
        if(positionIonABC>0) 
        {
        	sizeABCserie = fragSer[positionIonABC].masses.length;
        }
        int sizeXYZserie = 0;
        if(positionIonXYZ>0)
        {
        	sizeXYZserie = fragSer[positionIonXYZ].masses.length;
        }


        if (xyzSerieName.contains("z+1")) {
            xyzSerieName = "z"; // we keep only the char sequence instead of
        }										// full (ex: z+1 -> z)
        // à noter que 2 manières de faire les match. soit par égalité de
        // masse théo et match, ou bien par numéro de position sur le match.
        // exemple b(2) signifie sur le 2e element théorique ca matche. !!!
        // 1ere solution employée ici.
        // int i=0;
        // Here: filling the fragTables (theo and measured, before
        // displaying)
        j = 0;
        m_spectrumMinY = 0.00; // is overwritten in following loop
        m_spectrumMaxY = 0.00;
        double roundTol = 0.000001;
        int nbFound = 0;
        int nbThroughB = 0;
        int nbThroughY = 0;

        for (j = 0; j < fragSer.length; j++) { // loop through theoFragment series here
        	fragSer[j].computeChargeFromLabel();
            for (int k = 0; k < fragSer[j].masses.length; k++) { // loop
            	fragTableTheo[0][nbThroughB] = maxY - (maxY - minY) * 0.15; 
                fragTableTheo[1][nbThroughB] = fragSer[j].masses[k]; 
                fragTableTheoCharge[0][nbThroughB] = fragSer[j].charge;
                fragTableTheo[5][nbThroughY] = maxY - (maxY - minY) * 0.25; // intensity
                fragTableTheo[6][nbThroughY] = fragSer[j].masses[k];
                fragTableTheoCharge[5][nbThroughY] = fragSer[j].charge;
                
                for (int i = 0; i < fragMa.length; i++) { // find matching
                  
                    fragMa[i].computeChargeFromLabel();
                    if (j == positionIonABC) {
                    	if(    fragMa[i].charge == fragSer[j].charge
    							&& fragMa[i].countSeq('*') == fragSer[j].countSeq('*')
    							&& fragMa[i].countSeq('0') == fragSer[j].countSeq('0'))
	                    {
		                    	
		                        if ((fragMa[i].calculated_moz - roundTol <= (fragSer[j].masses[k]))
		                                && (fragMa[i].calculated_moz + roundTol >= fragSer[j].masses[k])) {
		                            nbFound++;
		                            fragTable[0][nbThroughB] = fragMa[i].calculated_moz - fragMa[i].moz;
		                            fragTable[1][nbThroughB] = fragSer[j].masses[k];
		                            if (fragTable[0][nbThroughB] > m_spectrumMaxY) {
		                                m_spectrumMaxY = fragTable[0][nbThroughB];
		                            }
		                            if (fragTable[0][nbThroughB] < m_spectrumMinY) {
		                                m_spectrumMinY = fragTable[0][nbThroughB];
		                            }
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
		                        if ((fragMa[i].calculated_moz - roundTol <= fragSer[j].masses[k])
		                                && (fragMa[i].calculated_moz + roundTol >= fragSer[j].masses[k])) {
		                            nbFound++;
		                            fragTable[5][nbThroughY] = fragMa[i].calculated_moz - fragMa[i].moz;
		                            fragTable[6][nbThroughY] = fragSer[j].masses[k];
		                            if (fragTable[5][nbThroughY] > m_spectrumMaxY) {
		                                m_spectrumMaxY = fragTable[5][nbThroughY];
		                            }
		                            if (fragTable[5][nbThroughY] < m_spectrumMinY) {
		                                m_spectrumMinY = fragTable[5][nbThroughY];
		                            }
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

        
       // maxY = m_spectrumMaxY;
       // minY = m_spectrumMinY;
        
        double abcPrev = fragTable[1][0] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(0));;

        boolean xyzPrevFound = false; // indicates if last iteration was a
        // match or not. (if yes then
        // highlight the AA)
        boolean abcPrevFound = false;

        String surroundingCharacters = "";
        float orientationFactor = 0;
        TextAnchor pointerAnchor;

       
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
                    if (abcPrevFound
                            || i == 0/*
                             * abcSerieFirstElementPosition
                             */
                            || i == (sizeABCserie - 1)) {// 2 consecutives fragments matching,
                        // or first element or last element, then highlight the AA
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

                    if (!(i == sizeABCserie - 1)) { // do not draw triangle and number if last element
                        // error sign and positioning of the pointer:
                        if (fragTable[0][i] >= 0) {
                            pointerAnchor = TextAnchor.BOTTOM_CENTER;
                            orientationFactor = 1;
                        } else {
                            pointerAnchor = TextAnchor.TOP_CENTER;
                            orientationFactor = -1;
                        }
                        // draw the triangle above the b number peak &
                        // draw the b number over the peak
                        final XYPointerAnnotation pointer = new XYPointerAnnotation(abcSerieName + (i + 1),
                                fragTableTheo[1][i],
                                fragTable[0][i] + orientationFactor * (maxY - minY) * 0.055,
                                orientationFactor * 6.0 * Math.PI / 4.0);
                        pointer.setBaseRadius(5.0);
                        pointer.setTipRadius(0.0);
                        pointer.setArrowWidth(2);
                        pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        pointer.setArrowPaint(abc_serie_color);;
                        pointer.setPaint(abc_serie_color);
                        pointer.setTextAnchor(pointerAnchor);
                        pointer.setToolTipText("<html>"
                                + "m/z: " + fragTable[1][i] + "<br>"
                                + "Error: " + new BigDecimal(fragTable[0][i], new MathContext(4)) + "<br>"
                                + new BigDecimal(1000000 * fragTable[0][i] / fragTable[1][i], new MathContext(3))
                                + " ppm"
                                + "</html>");
                        plot.addAnnotation(pointer);

                        // dashed vertical bar over the b number
                        float yAboveBar = (float) ((maxY - minY) * 0.091);
                        float dash[] = {0.01f};
                        //
                        // draw error
                        BasicStroke stk = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                        XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[1][i], 0/*
                                 * fragTable[0][i] + yAboveBar
                                 */, fragTableTheo[1][i],
                                fragTable[0][i], stk, abc_serie_color);
                        line.setToolTipText("<html>"
                                + "m/z: " + fragTable[1][i] + "<br>"
                                + "Error: " + new BigDecimal(fragTable[0][i], new MathContext(4)) + "<br>"
                                + new BigDecimal(1000000 * fragTable[0][i] / fragTable[1][i], new MathContext(3))
                                + " ppm"
                                + "</html>");

                        plot.addAnnotation(line);

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
                        // error sign and positioning of the pointer:
                        if (fragTable[5][i] >= 0) {
                            pointerAnchor = TextAnchor.BOTTOM_CENTER;
                            orientationFactor = 1;
                        } else {
                            pointerAnchor = TextAnchor.TOP_CENTER;
                            orientationFactor = -1;
                        }
                        // 	draw the b number over the peak &
                        // draw the triangle above the b number peak
                        final XYPointerAnnotation pointer = new XYPointerAnnotation(xyzSerieName + (sizeXYZserie - i),
                                fragTableTheo[6][i],
                                fragTable[5][i] + orientationFactor * (maxY - minY) * 0.01,
                                orientationFactor * 6.0 * Math.PI / 4.0);
                        pointer.setBaseRadius(5.0); // distance from pointer to?
                        pointer.setTipRadius(0.0); // length of the pointer
                        pointer.setArrowWidth(2);

                        pointer.setArrowPaint(xyz_serie_color);;
                        pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        pointer.setPaint(xyz_serie_color);
                        pointer.setTextAnchor(pointerAnchor);
                        pointer.setToolTipText(fragTable[5][i] + " " + fragTable[6][i] + " ppm");
                        pointer.setToolTipText("<html>"
                                + "m/z: " + fragTable[6][i] + "<br>"
                                + "Error: " + new BigDecimal(fragTable[5][i], new MathContext(4)) + "<br>"
                                + new BigDecimal(1000000 * fragTable[5][i] / fragTable[6][i], new MathContext(3))
                                + " ppm"
                                + "</html>");
                        plot.addAnnotation(pointer);

                        // dashed vertical bar over the b number
                        float yAboveBar = (float) ((maxY - minY) * 0.041);
                        float dash[] = {0.01f};
                        // draw error for xyz
                        BasicStroke stk = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
                        XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[6][i], 0/*
                                 * fragTable[5][i] + yAboveBar
                                 */, fragTableTheo[6][i],
                                fragTable[5][i], stk, xyz_serie_color);
                        line.setToolTipText("<html>"
                                + "m/z: " + fragTable[6][i] + "<br>"
                                + "Error: " + new BigDecimal(fragTable[5][i], new MathContext(4)) + "<br>"
                                + new BigDecimal(1000000 * fragTable[5][i] / fragTable[6][i], new MathContext(3))
                                + " ppm"
                                + "</html>");
                        plot.addAnnotation(line);


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
