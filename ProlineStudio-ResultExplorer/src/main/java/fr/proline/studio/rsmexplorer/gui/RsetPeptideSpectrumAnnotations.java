package fr.proline.studio.rsmexplorer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

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

// created by AW
//
// purpose: to add amino acids annotations over the spectrum chart.
// 

public class RsetPeptideSpectrumAnnotations {

	AbstractDataBox m_dataBox;
	DefaultXYDataset m_dataSet;
	JFreeChart m_chart;
	DPeptideMatch m_peptideMatch;

	
	public RsetPeptideSpectrumAnnotations(AbstractDataBox dBox, DefaultXYDataset dSet, JFreeChart chrt, DPeptideMatch pepMatch) {
		m_dataBox = dBox;
		m_dataSet = dSet;
		m_chart = chrt;
		m_peptideMatch = pepMatch;
		}

	void removeAnnotations() {
		XYPlot p = (XYPlot) m_chart.getPlot();

		@SuppressWarnings("unchecked")
		List<XYAnnotation> annotationsList =  p.getAnnotations();
		int lsize = annotationsList.size();
		for (int i = 0; i < lsize; i++) {
			p.removeAnnotation(annotationsList.get(i));
		}
		}

	class JsonProperties {
		public int ms_query_initial_id;
		public int peptide_match_rank;
		public TheoreticalFragmentSeries_AW[] frag_table;
		public FragmentMatch_AW[] frag_matches;

	}

	class TheoreticalFragmentSeries_AW {
		public String frag_series;
		public double[] masses;
		public int charge = 1; // default to 1 because it is used to multiply
								// the m/z to obtain real mass values for aa
								// calculation

		public void computeCharge() {
			this.charge = 0;
			if (frag_series != null) {
				for (int i = 0; i < frag_series.length(); i++) {
					if (frag_series.charAt(i) == '+') {
						this.charge++;
					}
				}
			}
			if (this.charge == 0)
				this.charge = 1;

		}

	}

	class FragmentMatch_AW {

		public String label;
		public Double moz;
		public Double calculated_moz;
		public Float intensity;
		public int charge = 0; // the charge taken from the serie (++ means
								// double charged)

		public void computeChargeFromLabel() {
			this.charge = 0;
			if (label != null) {
				for (int i = 0; i < label.length(); i++) {
					if (label.charAt(i) == '+') {
						this.charge++;
					}
				}
			}

		}
	}

	public void addAnnotations() {

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

		DataStoreConnectorFactory dsConnectorFactory = DataStoreConnectorFactory.getInstance();
		if (dsConnectorFactory.isInitialized() == false) {
			dsConnectorFactory.initialize(DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_dataBox.getProjectId()));
		}

		Map<String, Long> aw_Map = pmORM.getObjectTreeIdByName();

		Long objectTreeId = null; //
		for (Map.Entry<String, Long> entry : aw_Map.entrySet()) {
			objectTreeId = entry.getValue();
		}

		if (objectTreeId != null) {
			ObjectTree ot = entityManagerMSI.find(ObjectTree.class, objectTreeId); // get
																					// the
																					// objectTree
																					// from
																					// id.

			String clobData = ot.getClobData();
			String jsonProperties = clobData;

			JsonParser parser = new JsonParser();
			Gson gson = new Gson();

			JsonObject array = parser.parse(jsonProperties).getAsJsonObject();
			JsonProperties jsonProp = gson.fromJson(array, JsonProperties.class);

			// compute the charge for each fragment match from the label
			for (FragmentMatch_AW fragMa : jsonProp.frag_matches) {
				fragMa.computeChargeFromLabel();
			}

			TheoreticalFragmentSeries_AW[] fragSer = jsonProp.frag_table;
			FragmentMatch_AW[] fragMa = jsonProp.frag_matches;

			if (spectrum == null) {
				m_dataSet.removeSeries(SERIES_NAME);
				removeAnnotations();
				return;
			}

			byte[] intensityByteArray = spectrum.getIntensityList(); // package$EasyLzma$.MODULE$.uncompress(spectrum.getIntensityList());
			byte[] massByteArray = spectrum.getMozList(); // package$EasyLzma$.MODULE$.uncompress(spectrum.getMozList());
			ByteBuffer intensityByteBuffer = ByteBuffer.wrap(intensityByteArray).order(ByteOrder.LITTLE_ENDIAN);
			FloatBuffer intensityFloatBuffer = intensityByteBuffer.asFloatBuffer();
			double[] intensityDoubleArray = new double[intensityFloatBuffer.remaining()];

			for (int i = 0; i < intensityDoubleArray.length; i++)
				intensityDoubleArray[i] = (double) intensityFloatBuffer.get();

			ByteBuffer massByteBuffer = ByteBuffer.wrap(massByteArray).order(ByteOrder.LITTLE_ENDIAN);
			DoubleBuffer massDoubleBuffer = massByteBuffer.asDoubleBuffer();
			double[] massDoubleArray = new double[massDoubleBuffer.remaining()];

			for (int i = 0; i < massDoubleArray.length; i++)
				massDoubleArray[i] = massDoubleBuffer.get();

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
				if (fragSer[i].masses.length > sizeMaxSeries)
					sizeMaxSeries = fragSer[i].masses.length;

			}

			double[][] fragTableTheo = new double[11][sizeMaxSeries + 1];
			float[][] fragTableTheoCharge = new float[11][sizeMaxSeries + 1];
			double[][] fragTable = new double[11][sizeMaxSeries + 1];
			
			// **-*-*-* HERE READING Data from Objects *-*-*-*-**-
			
			String peptideSequence = m_peptideMatch.getPeptide().getSequence();
			
			removeAnnotations();
			XYTextAnnotation xyta;
			XYPlot plot = (XYPlot) m_chart.getPlot();

			// double minX = (float) plot.getDomainAxis().getLowerBound(); // this is the bounds from data.
			// double maxX = (float) plot.getDomainAxis().getUpperBound();
			double minY = (float) plot.getRangeAxis().getLowerBound(); // this is bounds from window
			double maxY = (float) plot.getRangeAxis().getUpperBound();

			int j = 0;
			// ************************************************************
			// *-*-*- Load fragmentation table (theoretical and measured) *
			// ************************************************************

			// String peptideSequence = "RVPPLG";
			int positionIonABC = 0;
			int positionIonXYZ = 0;
			String xyzSerieName = "";
			String abcSerieName = "";
			for (int i = 0; i < fragSer.length; i++) { // TODO: en fait les frag
														// series b s'appliquent
														// aussi a b++ etc. donc
														// va falloir faire un
														// tableau de positions
														// au lieu de juste
														// Bposition

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
			target.setPaint(new Color(255, 85, 85));
			target.setLabel(xyzSerieName);
			target.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			plot.addRangeMarker(target);
			Marker target2 = new ValueMarker(maxY - (maxY - minY) * 0.15);
			target2.setPaint(new Color(51, 153, 255));
			target2.setLabel(abcSerieName);
			target2.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			target2.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			plot.addRangeMarker(target2);

			int sizeABCserie = fragSer[positionIonABC].masses.length;
			int sizeXYZserie = fragSer[positionIonXYZ].masses.length;

			
			if (xyzSerieName.contains("z+1"))
				xyzSerieName = "z"; // we keep only the char sequence instead of
									// full (ex: z+1 -> z)
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
								//System.out.println("nbThroughB = " + nbThroughB + " , found" + nbFound + " moz" + fragMa[i].moz);
								fragTable[0][nbThroughB] = fragMa[i].intensity; 
								fragTable[1][nbThroughB] = fragSer[j].masses[k];
								; 
							} else {
								
							}

						}
						if (j == positionIonXYZ) {
							fragTableTheo[5][nbThroughY] = maxY - (maxY - minY) * 0.25; 
							fragTableTheo[6][nbThroughY] = fragSer[j].masses[k]; 
							fragTableTheoCharge[5][nbThroughY] = fragSer[j].charge;
							if ((fragMa[i].calculated_moz - roundTol <= fragSer[j].masses[k])
									&& (fragMa[i].calculated_moz + roundTol >= fragSer[j].masses[k])) {
								nbFound++;
								//System.out.println("nbThroughY = " + nbThroughY + " , found" + nbFound + " moz" + fragMa[i].calculated_moz);
								fragTable[5][nbThroughY] = fragMa[i].intensity; 
								fragTable[6][nbThroughY] = fragSer[j].masses[k]; 
							} else {
								
							}

						}

					}
					if (j == positionIonABC)
						nbThroughB++;
					if (j == positionIonXYZ)
						nbThroughY++;
				}
			}

			// place annotations
			double xyzPrev = 0; 
			fragTableTheo[6][0] = fragTableTheo[6][1] + getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length() - 1));
			double abcPrev = 0; 
			float xyzPrevCharge = fragTableTheoCharge[6][1];
			float abcPrevCharge = 0;
			boolean xyzPrevFound = false; // indicates if last iteration was a
											// match or not. (if yes then
											// highlight the AA)
			boolean abcPrevFound = false;

			// place initial and last peptide sequence elements

			// y ions
			// last peptide element of sequence
			xyta = new XYTextAnnotation("" + peptideSequence.charAt(peptideSequence.length() - 1),
					(fragTableTheo[6][sizeXYZserie - 1] + (fragTableTheo[6][sizeXYZserie - 1] - getMassFromAminoAcid(peptideSequence.charAt(peptideSequence
							.length() - 1)))) / 2, maxY - (maxY - minY) * 0.25);
			xyta.setPaint(new Color(255, 85, 85));
			xyta.setFont(new Font(null, Font.BOLD, 11));
			xyta.setBackgroundPaint(Color.white);
			plot.addAnnotation(xyta);

			// b ions
			// first element of sequence
			xyta = new XYTextAnnotation(" " + peptideSequence.charAt(0) + " ", (0 + fragTableTheo[1][0]) / 2, maxY - (maxY - minY) * 0.15);
			xyta.setPaint(new Color(51, 153, 255));
			xyta.setBackgroundPaint(Color.white);
			xyta.setFont(new Font(null, Font.BOLD, 11));
			plot.addAnnotation(xyta);
			// last element of sequence
			xyta = new XYTextAnnotation(" " + peptideSequence.charAt(peptideSequence.length() - 1) + " ", (fragTableTheo[1][sizeABCserie - 2]
					+ getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length() - 1)) + fragTableTheo[1][sizeABCserie - 2]) / 2, maxY
					- (maxY - minY) * 0.15);
			xyta.setPaint(new Color(51, 153, 255));
			xyta.setBackgroundPaint(Color.white);
			xyta.setFont(new Font(null, Font.BOLD, 11));
			plot.addAnnotation(xyta);

			int size = Math.max(fragSer[positionIonABC].masses.length, fragSer[positionIonXYZ].masses.length);

			for (int i = 0; i < size; i++) { // loop through the series points

				// place separators marks------
				if (abcPrev != 0) {
					xyta = new XYTextAnnotation("|", abcPrev, maxY - (maxY - minY) * 0.15);
					xyta.setPaint(new Color(51, 153, 255));
					plot.addAnnotation(xyta);
				}

				if (xyzPrev != 0) {
					xyta = new XYTextAnnotation("|", xyzPrev, maxY - (maxY - minY) * 0.25);
					xyta.setPaint(new Color(255, 85, 85));
					plot.addAnnotation(xyta);
				} else {
					xyta = new XYTextAnnotation("|", fragTableTheo[6][sizeXYZserie - 1], maxY - (maxY - minY) * 0.25);
					xyta.setPaint(new Color(255, 85, 85));
					plot.addAnnotation(xyta);
				}

				// place AA highlighting

				// ----- if 2 contiguous mass peaks are represented...draw the
				// aa interval Y

				if (i != 0 && fragTable[6][i] != 0) {
					// write the triangle for y number peak
					xyta = new XYTextAnnotation("\u25BE", fragTableTheo[6][i], fragTable[5][i] + (maxY - minY) * 0.01);
					xyta.setPaint(new Color(255, 85, 85));
					plot.addAnnotation(xyta);
					// write the yx series number
					xyta = new XYTextAnnotation(xyzSerieName + (sizeXYZserie - i), fragTableTheo[6][i], fragTable[5][i] + (maxY - minY) * 0.035);
					xyta.setPaint(new Color(255, 85, 85));
					plot.addAnnotation(xyta);
					// dashed vertical bar
					float yAboveBar = (float) ((maxY - minY) * 0.041);
					float dash[] = { 4.0f };
					if (fragTable[5][i] + yAboveBar < fragTableTheo[5][i]) { // draw
																				// only
																				// dashline
																				// if
																				// the
																				// y
																				// or
																				// b
																				// tag
																				// is
																				// not
																				// above
																				// the
																				// y/b
																				// line
						BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
						XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[6][i], fragTable[5][i] + yAboveBar, fragTableTheo[6][i],
								fragTableTheo[5][i], stk, new Color(255, 85, 85));
						plot.addAnnotation(line);
					}

				}
				// draw the outlined AA : Y series
				if (xyzPrev != 0 && fragTable[6][i - 1] != 0) {

					if (fragTable[6][i] != 0) {
						xyzPrevFound = true;
					} else {
						xyzPrevFound = false;
					}
					String aa = "" + peptideSequence.charAt(i - 1); 
					// draw the aa letter
					xyta = new XYTextAnnotation(" " + aa + " ", (xyzPrev + fragTable[6][i - 1]) / 2, maxY - (maxY - minY) * 0.25);
					if (xyzPrevFound) { // 2 consecutive fragments matching,
										// then highlight the AA
						xyta.setPaint(Color.white);
						xyta.setBackgroundPaint(new Color(255, 85, 85));
					} else {
						xyta.setPaint(new Color(255, 85, 85));
						xyta.setBackgroundPaint(Color.white);
					}
					xyta.setFont(new Font(null, Font.BOLD, 11));
					plot.addAnnotation(xyta);

				} else {
					xyzPrevFound = false;
					if (xyzPrev != 0 && fragTableTheo[6][i - 1] != 0) {
						String aa = "" + peptideSequence.charAt(i - 1); 
						xyta = new XYTextAnnotation("" + aa, (xyzPrev + fragTableTheo[6][i - 1]) / 2, maxY - (maxY - minY) * 0.25);
						xyta.setPaint(new Color(255, 85, 85));
						xyta.setFont(new Font(null, Font.BOLD, 11));
						xyta.setBackgroundPaint(Color.white);
						plot.addAnnotation(xyta);
					}
				}
				// draw the outlined AA : B series
				if (abcPrev != 0 && fragTable[1][i] != 0) {
					if (i == sizeABCserie - 1) {
						;// xyta = new XYTextAnnotation("b" + (i+1), maxX,
							// fragTableTheo[0][i] + (maxY - minY) * 0.05);
					} else {
						// draw the triangle above the b number peak
						xyta = new XYTextAnnotation("\u25BE", fragTableTheo[1][i], fragTable[0][i] + (maxY - minY) * 0.055);
						xyta.setPaint(new Color(51, 153, 255));
						plot.addAnnotation(xyta);
						// draw the b number overt the peak
						xyta = new XYTextAnnotation(abcSerieName + (i + 1), fragTableTheo[1][i], fragTable[0][i] + (maxY - minY) * 0.08);
						xyta.setPaint(new Color(51, 153, 255));
						plot.addAnnotation(xyta);
						// dashed vertical bar over the b number
						float yAboveBar = (float) ((maxY - minY) * 0.091);
						float dash[] = { 5.0f };
						if (fragTable[0][i] + yAboveBar < fragTableTheo[0][i]) { // draw
																					// only
																					// dashline
																					// if
																					// the
																					// y
																					// or
																					// b
																					// tag
																					// is
																					// not
																					// above
																					// the
																					// y/b
																					// line
							BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
							XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[1][i], fragTable[0][i] + yAboveBar, fragTableTheo[1][i],
									fragTableTheo[0][i], stk, new Color(51, 153, 255));
							plot.addAnnotation(line);
						}

					}
					String aa = "" + peptideSequence.charAt(i);
					xyta = new XYTextAnnotation(" " + aa + " ", (abcPrev + fragTable[1][i]) / 2, maxY - (maxY - minY) * 0.15);
					if (abcPrevFound) { // 2 consecutives fragments matching,
										// then highlight the AA
						xyta.setPaint(Color.white);
						xyta.setBackgroundPaint(new Color(51, 153, 255));
					} else {
						xyta.setPaint(new Color(51, 153, 255));
						xyta.setBackgroundPaint(Color.white);
					}
					xyta.setFont(new Font(null, Font.BOLD, 11));
					plot.addAnnotation(xyta);
					abcPrevFound = true;
				} else // draw the regular expected (but not found) aa
				{
					abcPrevFound = false;
					if (abcPrev != 0 && fragTableTheo[1][i] != 0) {
						String aa = "" + peptideSequence.charAt(i);

						xyta = new XYTextAnnotation("" + aa, (abcPrev + fragTableTheo[1][i]) / 2, maxY - (maxY - minY) * 0.15);
						xyta.setPaint(new Color(51, 153, 255));
						xyta.setFont(new Font(null, Font.BOLD, 11));
						xyta.setBackgroundPaint(Color.white);
						plot.addAnnotation(xyta);
					}
				}

				xyzPrev = fragTableTheo[6][i + 1];
				abcPrev = fragTableTheo[1][i];
				xyzPrevCharge = fragTableTheoCharge[6][i];
				abcPrevCharge = fragTableTheoCharge[1][i];
			}
			jsonProp = null;
			array = null;
			gson = null;
			parser = null;

		}

                entityManagerMSI.getTransaction().commit(); 
                
            } catch (Exception e) {
                entityManagerMSI.getTransaction().rollback(); 
            } finally {
                
		
		entityManagerMSI.close();
            }
		
	}

	public static double getMassFromAminoAcid(char aa) {
		HashMap<Character, Double> aaHashMap = new HashMap<Character, Double>();

		aaHashMap.put('A', (double) 71.03711);
		aaHashMap.put('C', (double) 103.00919);
		aaHashMap.put('D', (double) 115.02694);
		aaHashMap.put('E', (double) 129.04259);
		aaHashMap.put('F', (double) 147.06841);
		aaHashMap.put('G', (double) 57.02146);
		aaHashMap.put('H', (double) 137.05891);
		aaHashMap.put('I', (double) 113.08406);
		aaHashMap.put('K', (double) 128.09496);
		aaHashMap.put('L', (double) 113.08406);
		aaHashMap.put('M', (double) 131.04049);
		aaHashMap.put('N', (double) 114.04293);
		aaHashMap.put('P', (double) 97.05276);
		aaHashMap.put('Q', (double) 128.05858);
		aaHashMap.put('R', (double) 156.10111);
		aaHashMap.put('S', (double) 87.03203);
		aaHashMap.put('T', (double) 101.04768);
		aaHashMap.put('V', (double) 99.06841);
		aaHashMap.put('W', (double) 186.07931);
		aaHashMap.put('Y', (double) 163.06333);

		// float deltaMoZ = 0;
		return aaHashMap.get(aa);

	}

	// the getAminoAcidName is not used but could be in the future...

	public String getAminoAcidName(double deltaMass, double tolerance) {

		// scan the spectrum to find potential aminoacids
		HashMap<Double, Character> aaHashMap = new HashMap<Double, Character>();

		aaHashMap.put((double) 71.03711, 'A');
		aaHashMap.put((double) 103.00919, 'C');
		aaHashMap.put((double) 115.02694, 'D');
		aaHashMap.put((double) 129.04259, 'E');
		aaHashMap.put((double) 147.06841, 'F');
		aaHashMap.put((double) 57.02146, 'G');
		aaHashMap.put((double) 137.05891, 'H');
		aaHashMap.put((double) 113.08406, 'I');
		aaHashMap.put((double) 128.09496, 'K');
		aaHashMap.put((double) 113.08406, 'L');
		aaHashMap.put((double) 131.04049, 'M');
		aaHashMap.put((double) 114.04293, 'N');
		aaHashMap.put((double) 97.05276, 'P');
		aaHashMap.put((double) 128.05858, 'Q');
		aaHashMap.put((double) 156.10111, 'R');
		aaHashMap.put((double) 87.03203, 'S');
		aaHashMap.put((double) 101.04768, 'T');
		aaHashMap.put((double) 99.06841, 'V');
		aaHashMap.put((double) 186.07931, 'W');
		aaHashMap.put((double) 163.06333, 'Y');
		
		double toleranceCalc = tolerance;
		//System.out.println("--->Submitted mass of " + deltaMass);
		for (double aaMass : aaHashMap.keySet()) {
			if ((aaMass - toleranceCalc < deltaMass) && (aaMass + toleranceCalc > deltaMass)) {
				return (aaHashMap.get(aaMass).toString());
			}
		}
		NumberFormat formatter = null;
		formatter = java.text.NumberFormat.getInstance(java.util.Locale.FRENCH);
		formatter = new DecimalFormat("#0.000");

		return ("" + formatter.format(deltaMass)); // return ("*");

	}

}
