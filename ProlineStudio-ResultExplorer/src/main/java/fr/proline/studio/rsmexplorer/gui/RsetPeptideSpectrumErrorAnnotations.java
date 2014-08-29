package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.rsmexplorer.spectrum.SpectrumFragmentationUtil;
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
import java.util.Map;

import javax.persistence.EntityManager;

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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.rsmexplorer.spectrum.FragmentMatch_AW;
import fr.proline.studio.rsmexplorer.spectrum.FragmentationJsonProperties;
import fr.proline.studio.rsmexplorer.spectrum.TheoreticalFragmentSeries_AW;

// created by AW
//
// purpose: to add error annotations over the spectrum chart.
// 
public class RsetPeptideSpectrumErrorAnnotations {

    AbstractDataBox m_dataBox;
    DefaultXYDataset m_dataSet;
    JFreeChart m_chart;
    DPeptideMatch m_peptideMatch;
    double m_spectrumMaxY = 0; // y range from data...will be updated and then available for caller.
    double m_spectrumMinY = 0;

    public RsetPeptideSpectrumErrorAnnotations(AbstractDataBox dBox, DefaultXYDataset dSet, JFreeChart chrt, DPeptideMatch pepMatch) {
        m_dataBox = dBox;
        m_dataSet = dSet;
        m_chart = chrt;
        m_peptideMatch = pepMatch;
    }

    void removeErrorAnnotations() {
        XYPlot p = (XYPlot) m_chart.getPlot();

        //@SuppressWarnings("unchecked")
        List<XYAnnotation> annotationsList = p.getAnnotations();
        int lsize = annotationsList.size();
        for (int i = 0; i < lsize; i++) {
            p.removeAnnotation(annotationsList.get(i));
        }
    }



    public void addErrorAnnotations() {

        if (m_peptideMatch == null) {
            return;
        }

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_dataBox.getProjectId()).getEntityManagerFactory().createEntityManager();

        try {

            final String SERIES_NAME = "spectrumData";

            entityManagerMSI.getTransaction().begin();


            PeptideMatch pmORM = entityManagerMSI.find(PeptideMatch.class, m_peptideMatch.getId());

            DMsQuery msQuery = m_peptideMatch.isMsQuerySet() ? m_peptideMatch.getMsQuery() : null;

            Spectrum spectrum = msQuery.isSpectrumSet() ? msQuery.getSpectrum() : null;


            Map<String, Long> aw_Map = pmORM.getObjectTreeIdByName();

            Long objectTreeId = null; //
            for (Map.Entry<String, Long> entry : aw_Map.entrySet()) {
                objectTreeId = entry.getValue();
            }

            if (objectTreeId == null) {
                removeErrorAnnotations(); // no object tree means no JSON data to be displayed
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug("objectr tree id is null, no annotations to show for pm_id=" + m_peptideMatch.getId());

            } else {
                ObjectTree ot = entityManagerMSI.find(ObjectTree.class, objectTreeId); // get
                // the
                // objectTree
                // from
                // id.

                String clobData = ot.getClobData();
                String jsonProperties = clobData;
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug(
                        "objectr tree for pm_id=" + m_peptideMatch.getId() + " "
                        + m_peptideMatch.getPeptide().getSequence() + "\n" + jsonProperties);

                JsonParser parser = new JsonParser();
                Gson gson = new Gson();

                JsonObject array = parser.parse(jsonProperties).getAsJsonObject();
                FragmentationJsonProperties jsonProp = gson.fromJson(array, FragmentationJsonProperties.class);

                // compute the charge for each fragment match from the label
                for (FragmentMatch_AW fragMa : jsonProp.frag_matches) {
                    fragMa.computeChargeFromLabel();
                }

                TheoreticalFragmentSeries_AW[] fragSer = jsonProp.frag_table;
                FragmentMatch_AW[] fragMa = jsonProp.frag_matches;

                if (spectrum == null) {
                    m_dataSet.removeSeries(SERIES_NAME);
                    removeErrorAnnotations();
                    return;
                }

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

                @SuppressWarnings("unused")
                class SpectrumMatchAW { // not used at the moment but perhaps later

                    TheoreticalFragmentSeries_AW[] fragmentationTable;
                    FragmentMatch_AW[] fragmentMatches;

                    public SpectrumMatchAW(TheoreticalFragmentSeries_AW[] fragT, FragmentMatch_AW[] fragMatches) {
                        this.fragmentationTable = fragT;
                        this.fragmentMatches = fragMatches;
                    }
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

                        case 'a': // either a,b or c do:
                        case 'b':
                        case 'c':
                            if (fragSer[i].frag_series.length() > 1) {
                                // then it is either a ++ or a b-H2O and so on...
                            } else { // it's a 'a/b/c' ion
                                positionIonABC = i;
                                abcSerieName = "" + fragSer[i].frag_series;
                            }
                            break;
                        case 'v':
                        case 'w':
                        case 'x':
                        case 'y':

                            if (fragSer[i].frag_series.length() > 1) {
                                // then it is either a ++ or a b-H2O and so on...
                            } else { // it's a 'x/y/z' ion
                                xyzSerieName = "" + fragSer[i].frag_series;
                                positionIonXYZ = i;
                            }
                            break;
                        case 'z':
                            if (fragSer[i].frag_series.length() == 3) {
                                if (fragSer[i].frag_series.equals("z+1")) {
                                    xyzSerieName = "(z+1)";
                                    positionIonXYZ = i;
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
                for (j = 0; j < fragSer.length; j++) { // loop through theoFragment
                    // series here
                    for (int k = 0; k < fragSer[j].masses.length; k++) { // loop
                        // through
                        // masses
                        // for
                        // each
                        // fragment
                        // series
                        for (int i = 0; i < fragMa.length; i++) { // find matching
                            // fragMatches
                            // with
                            // theoFragSeries
                            fragSer[j].computeCharge();
                            if (j == positionIonABC) {
                                fragTableTheo[0][nbThroughB] = maxY - (maxY - minY) * 0.15; // data[1][i];
                                // //
                                // intensity
                                // for
                                // b
                                // ions
                                fragTableTheo[1][nbThroughB] = fragSer[j].masses[k]; // data[0][i];
                                fragTableTheoCharge[0][nbThroughB] = fragSer[j].charge;
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
                            if (j == positionIonXYZ) {
                                fragTableTheo[5][nbThroughY] = maxY - (maxY - minY) * 0.25; // intensity
                                fragTableTheo[6][nbThroughY] = fragSer[j].masses[k];
                                fragTableTheoCharge[5][nbThroughY] = fragSer[j].charge;
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
                // match or not. (if yes then
                // highlight the AA)
                boolean abcPrevFound = false;

                String surroundingCharacters = "";
                float orientationFactor = 0;
                TextAnchor pointerAnchor;

                if(!abcSerieName.equals("")) {
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
                if(!xyzSerieName.equals("")) {
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

            entityManagerMSI.getTransaction().commit();

        } catch (Exception e) {
            entityManagerMSI.getTransaction().rollback();
        } finally {


            entityManagerMSI.close();
        }

    }
}
