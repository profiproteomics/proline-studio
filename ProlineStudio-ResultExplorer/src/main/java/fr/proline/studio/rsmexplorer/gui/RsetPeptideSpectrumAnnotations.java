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
import org.jfree.data.general.DatasetUtilities;
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


// created by AW
//
// purpose: to add amino acids annotations over the spectrum chart.
// 

public class RsetPeptideSpectrumAnnotations {

	AbstractDataBox m_dataBox;
	DefaultXYDataset m_dataSet;
	JFreeChart m_chart;
	DPeptideMatch peptideMatch;
	DPeptideMatch pm;
	
	public RsetPeptideSpectrumAnnotations (AbstractDataBox m_dBox, DefaultXYDataset m_dSet, JFreeChart m_chrt, DPeptideMatch pepMatch) {
		 m_dataBox = m_dBox;
		 m_dataSet = m_dSet;
		 m_chart = m_chrt;
		 peptideMatch  = pepMatch;	
		 pm = peptideMatch;
	}

	
	  
  	void removeAnnotations() {
		XYPlot p = (XYPlot) m_chart.getPlot();

		List<XYAnnotation> annotationsList = p.getAnnotations();
		int lsize = annotationsList.size();
		for (int i = 0; i < lsize; i++) {
			p.removeAnnotation(annotationsList.get(i));
		}
		// p.clearRangeMarkers();
		// p.removeRangeMarker(p.g);
	}
  	

	class JsonProperties {
	    //String jsonProperties = "{\"ms_query_initial_id\":3,\"peptide_match_rank\":4,\"frag_table\":[{\"frag_series\":\"b\",\"masses\":[72.04439,171.112804,268.165568,365.218332,478.302396,0]},\"frag_matches\":[{\"label\":\"b(2)\",\"moz\":171.161,\"calculated_moz\":171.112804,\"intensity\":4.932}]}";
	    public int ms_query_initial_id;
	    public int peptide_match_rank;
	    public TheoreticalFragmentSeries_AW [] frag_table;
	    public FragmentMatch_AW [] frag_matches;
	           
	}
	
	class TheoreticalFragmentSeries_AW {
		public String frag_series;
		public double[] masses;
		public int charge=1; //default to 1 because it is used to multiply the m/z to obtain real mass values for aa calculation
		
		//String ionSeries;
		//String chargeStr;
		
		//public TheoreticalFragmentSeries_AW(String a, double[] b) {
		//	this.frag_series = a;
		//	this.masses = b;
		
		public void computeCharge() {
			this.charge=0;
			if(frag_series!=null) {
				for(int i =0;i< frag_series.length(); i++) {
					if(frag_series.charAt(i)=='+') {
						this.charge++;
					}
				}
			}
			if(this.charge==0) 
				this.charge=1;
			
		}
		
		//	String  fragSeriesRegex = "(\\w+)([+]*).*";  
		//	this.ionSeries = fragSeries.replaceAll(fragSeriesRegex, "$1").replace("0","-H2O").replace("*","-NH3");
			//ionSeries = ionSeries.replace("0","-H2O").replace("*","-NH3");
		//	this.chargeStr = fragSeries.replaceAll(fragSeriesRegex, "$2");
	//	}
	}
	
	  class FragmentMatch_AW {
	        //String type = "REGULAR"; // FragmentMatchType.REGULAR.toString,
		public String label;
		public Double moz;
		public Double calculated_moz;
		public Float intensity;
		public int charge=0; // the charge taken from the serie (++ means double charged)
		
		
		public void computeChargeFromLabel() {
			this.charge=0;
			if(label!=null) {
				for(int i =0;i< label.length(); i++) {
					if(label.charAt(i)=='+') {
						this.charge++;
					}
				}
			}
			
		}
	  }
	
	
	
		public void addAnnotations() {
	
			final String SERIES_NAME = "spectrumData";
			
		 
	
			EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_dataBox.getProjectId()).getEntityManagerFactory().createEntityManager();
			entityManagerMSI.getTransaction().begin();
			
			if(pm == null) {
				return;
			}
			PeptideMatch   pmORM = entityManagerMSI.find(PeptideMatch.class, pm.getId()); 
				
			DMsQuery msQuery = pm.isMsQuerySet() ? pm.getMsQuery() : null;
		   
			Spectrum spectrum = msQuery.isSpectrumSet() ? msQuery.getSpectrum() : null;
	     
			
	//////////////////
			
	
	        DataStoreConnectorFactory dsConnectorFactory = DataStoreConnectorFactory.getInstance();
		    if (dsConnectorFactory.isInitialized() == false) {
		      dsConnectorFactory.initialize(DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_dataBox.getProjectId()));
		    }
	
		    
			Map<String,Long> aw_Map = pmORM.getObjectTreeIdByName();
		
			Long objectTreeId=null; // 
			for (Map.Entry<String, Long> entry : aw_Map.entrySet()) {
			    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			 //  	LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("AW: keys: " + entry.getKey() + ", Value = " + entry.getValue());
			   	objectTreeId = entry.getValue();
			   	// here we got the object tree id. now let's get those things based on that id.
			}
	
			if( objectTreeId != null)
			{
				ObjectTree ot = entityManagerMSI.find(ObjectTree.class, objectTreeId); // get the objectTree from id.
		
		
				String clobData = ot.getClobData();
				
				//LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("AW: clob data:" + clobData);
	
			String jsonProperties = clobData;
			
			JsonParser parser = new JsonParser();
			Gson gson = new Gson();
			
		    JsonObject array = parser.parse(jsonProperties).getAsJsonObject();
		    JsonProperties jsonProp = gson.fromJson(array, JsonProperties.class);
		    
		    
			// compute the charge for each fragment match from the label 
		    for(FragmentMatch_AW fragMa : jsonProp.frag_matches) {
		    	fragMa.computeChargeFromLabel();
		    }
		    //   mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			
		        TheoreticalFragmentSeries_AW [] fragSer = jsonProp.frag_table;
		        FragmentMatch_AW [] fragMa = jsonProp.frag_matches;
	///////////////////////////////
			
		      
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
	
	
			class SpectrumMatchAW {
				TheoreticalFragmentSeries_AW[] fragmentationTable;
				FragmentMatch_AW[] fragmentMatches;
	
				public SpectrumMatchAW(TheoreticalFragmentSeries_AW[] fragT, FragmentMatch_AW[] fragMatches) {
					this.fragmentationTable = fragT;
					this.fragmentMatches = fragMatches;
				}
			}
	
		//	SpectrumMatchAW spectrMatch = new SpectrumMatchAW(fragSer, fragMa);

			//dataSize = 25; 
			int sizeMaxSeries = 0; 
			for(int i = 0; i<fragSer.length;i++) { // TODO: en fait les frag series b s'appliquent aussi a b++ etc. donc va falloir faire un tableau de positions au lieu de juste Bposition
				if(fragSer[i].masses.length>sizeMaxSeries)
					sizeMaxSeries = fragSer[i].masses.length;

			}
			
			double[][] fragTableTheo = new double[11][sizeMaxSeries+1];
			float [][] fragTableTheoCharge = new float [11][sizeMaxSeries+1];
			double[][] fragTable = new double[11][sizeMaxSeries+1];
			//char[] aaNames = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y' };
	
			// **-*-*-* HERE READING Data from Objects *-*-*-*-**-
		//	SpectrumMatchAW spMatch = spectrMatch;
		
			
			
			String peptideSequence = pm.getPeptide().getSequence();
//			double minY = DatasetUtilities.findMinimumRangeValue(m_dataSet).doubleValue();
//			double maxY = DatasetUtilities.findMaximumRangeValue(m_dataSet).doubleValue();
//			double maxX = DatasetUtilities.findMaximumDomainValue(m_dataSet).doubleValue(); // in order to know where to place the last AA sequence limit

			removeAnnotations();
			XYTextAnnotation xyta;
			XYPlot plot = (XYPlot) m_chart.getPlot();
			// annotations 
			//double minX = (float) plot.getDomainAxis().getLowerBound();
			//double maxX = (float) plot.getDomainAxis().getUpperBound();
			double minY = (float) plot.getRangeAxis().getLowerBound();
			double maxY = (float) plot.getRangeAxis().getUpperBound();

	
			int j = 0;
			// ************************************************************
			// *-*-*- Load fragmentation table (theoretical and measured) *
			// ************************************************************
		
	
			//String peptideSequence = "RVPPLG";
			int positionIonABC= 0;
			int positionIonXYZ= 0;
			String xyzSerieName = "";
			String abcSerieName = "";
			for(int i = 0; i<fragSer.length;i++) { // TODO: en fait les frag series b s'appliquent aussi a b++ etc. donc va falloir faire un tableau de positions au lieu de juste Bposition
				
				switch  ( fragSer[i].frag_series.charAt(0)) {
	
				case 'a' :  // either a,b or c do:
				case 'b' : 
				case 'c' : 
					if(fragSer[i].frag_series.length()>1) {
						// then it is either a ++ or a b-H2O and so on...
					}
					else
					{ // it's a 'a/b/c' ion
						positionIonABC = i;
						abcSerieName = ""+fragSer[i].frag_series;
					}
					break;
				case 'v' : 
				case 'w' : 
				case 'x' : 
				case 'y' : 
					
					if(fragSer[i].frag_series.length()>1) {
						// then it is either a ++ or a b-H2O and so on...
					}
					else
					{ // it's a 'x/y/z' ion
						xyzSerieName = ""+fragSer[i].frag_series;
						positionIonXYZ = i;
					}
					break;
				case 'z' : 
					if(fragSer[i].frag_series.length()==3) {
						if(fragSer[i].frag_series.equals("z+1")) {
							xyzSerieName = "(z+1)";	
							positionIonXYZ = i;
						}// else if (!xyzSerieName.equals("z+1")) {
							//xyzSerieName = "" + fragSer[i].frag_series;
						//	positionIonXYZ = i;
						//}
					} //else {
					//	xyzSerieName = ""+fragSer[i].frag_series.charAt(0);
					//	positionIonXYZ = i ;
					//}
					break;
				default :
					break;
				}
				
				
			}
			

			plot.clearRangeMarkers();
			Marker target = new ValueMarker(maxY - (maxY - minY) * 0.25);
			target.setPaint(new Color(255,85,85));
			target.setLabel(xyzSerieName);
			target.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			plot.addRangeMarker(target);
			Marker target2 = new ValueMarker(maxY - (maxY - minY) * 0.15);
			target2.setPaint(new Color(51,153,255));
			target2.setLabel(abcSerieName);
			//target.setLabelFont(new Font("Sansserif",Font.BOLD,11));
			target2.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			target2.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			//target2.setLabelFont(new Font("Sansserif",Font.BOLD,11));
			plot.addRangeMarker(target2);
			
			
			int sizeABCserie = fragSer[positionIonABC].masses.length;
			int sizeXYZserie = fragSer[positionIonXYZ].masses.length;
			
			//int size = Math.max(fragSer[positionIonABC].masses.length,fragSer[positionIonXYZ].masses.length);
				
			
			// *-*-*-* *-*-*-* *-*-*-* *-*-*-* ici on voit les match*-*-*-* *-*-*-* *-*-*-* *-*-*-* *-*-*-*
			// à noter que 2 manières de faire les match. soit par égalité de masse théo et match, ou bien par numéro de position sur le match.
			// exemple b(2) signifie sur le 2e element théorique ca matche. !!! 1ere solution employée ici.
			//int i=0;
			// Here: filling the fragTables (theo and measured, before displaying)
			j=0;
			double roundTol = 0.000001;
			int nbFound = 0;
			int nbThroughB = 0;
			int nbThroughY = 0;
			for ( j = 0; j < fragSer.length ; j++) { // loop through theoFragment series here
				for(int k = 0; k < fragSer[j].masses.length ;k++) { // loop through masses for each fragment serie
					for(int i = 0 ; i<fragMa.length ; i++) {  // find matching fragMatches with theoFragSeries
						//System.out.println("i,j,k:" + i + " " + j+ " " + k + "/" + fragSer[j].masses.length + " nbThroughB=" + nbThroughB + " nbThroughY=" + nbThroughY);
						//System.out.println("Charge : " + fragSer[j].charge);
						fragSer[j].computeCharge();
						//System.out.println("serie:" + fragSer[j].frag_series + " -  Charge : " + fragSer[j].charge);
						if(j == positionIonABC) {
							fragTableTheo[0][nbThroughB] = maxY - (maxY - minY) * 0.15; // data[1][i]; // intensity for b ions
							fragTableTheo[1][nbThroughB] = fragSer[j].masses[k]; // data[0][i];
							//fragSer[j].computeCharge();
							fragTableTheoCharge[0][nbThroughB] = fragSer[j].charge; 
							if( (fragMa[i].calculated_moz - roundTol <= (/*(double)(fragSer[j].charge) **/ fragSer[j].masses[k])) && (fragMa[i].calculated_moz + roundTol >= /*(double)(fragSer[j].charge) * */fragSer[j].masses[k])) {
								nbFound++;
								System.out.println("nbThroughB = " + nbThroughB + " , found" + nbFound + " moz" + fragMa[i].moz);
								fragTable[0][nbThroughB] =  fragMa[i].intensity ;  // /*maxY*/ - (maxY - minY) * 0.15; //data[1][i];
								fragTable[1][nbThroughB] =  fragSer[j].masses[k];; //fragMa[positionIonABC].moz ; //data[0][i];
							}
							else
							{
								//if(fragTable[0][nbThroughB])
//								fragTable[0][nbThroughB] = 0; // 0 means no data so we omit the peak
//								fragTable[1][nbThroughB] = 0;
							}
							
						}
						if(j == positionIonXYZ) {
							fragTableTheo[5][nbThroughY] = maxY - (maxY - minY) * 0.25; // data[1][i]; // intensity for b ions
							fragTableTheo[6][nbThroughY] = fragSer[j].masses[k]; // data[0][i];
						//	fragSer[j].computeCharge();
							fragTableTheoCharge[5][nbThroughY] = fragSer[j].charge; 
							if( (fragMa[i].calculated_moz - roundTol <= /*(double)(fragSer[j].charge) **/ fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= /*(double)(fragSer[j].charge) **/ fragSer[j].masses[k])) {
								nbFound++;
								System.out.println("nbThroughY = " + nbThroughY + " , found" + nbFound + " moz" + fragMa[i].calculated_moz);
								fragTable[5][nbThroughY] = fragMa[i].intensity ; //data[1][i];
								fragTable[6][nbThroughY] =  fragSer[j].masses[k]; //fragMa[positionIonABC].moz ; //data[0][i];
							}
							else
							{
//								fragTable[5][nbThroughY] = 0; // 0 means no data so we omit the peak
//								fragTable[6][nbThroughY] = 0;
							}
							
						}
						
				
				}
				if(j == positionIonABC) 
					nbThroughB ++;
				if(j == positionIonXYZ) 
					nbThroughY++;
			}
		}
			
				//**-*-*-*-*
				
				float tolerance = (float) 0.7; //0.01; // could be 0 but to be sure a match is performed...
				// place annotations
				double xyzPrev =  0; //fragTableTheo[6][1] + getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)) ;
				fragTableTheo[6][0] =  fragTableTheo[6][1] + getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)) ;
				double abcPrev = 0; //fragTableTheo[1][0];
				float xyzPrevCharge = fragTableTheoCharge[6][1];
				float abcPrevCharge =0;// fragTableTheoCharge[1][0];
				boolean xyzPrevFound = false; // indicates if last iteration was a match or not. (if yes then highlight the AA)
				boolean abcPrevFound = false;
				// place initial and last peptide sequence elements 
				// y ions
//				System.out.println("maxX:" + maxX);
				// first peptide element of sequence
//				xyta = new XYTextAnnotation("" + peptideSequence.charAt(0), (maxX + fragTableTheo[6][1]) / 2, maxY - (maxY - minY) * 0.25);
//				xyta.setPaint(new Color(255,85,85));
//				xyta.setFont(new Font(null,Font.BOLD,11));
//				xyta.setBackgroundPaint(Color.white);
//				plot.addAnnotation(xyta);
				// last peptide element of sequence
				xyta = new XYTextAnnotation("" + peptideSequence.charAt(peptideSequence.length()-1), ( fragTableTheo[6][sizeXYZserie-1] + (fragTableTheo[6][sizeXYZserie-1] - getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)))) / 2, maxY - (maxY - minY) * 0.25);
				xyta.setPaint(new Color(255,85,85));
				xyta.setFont(new Font(null,Font.BOLD,11));
				xyta.setBackgroundPaint(Color.white);
				plot.addAnnotation(xyta);
				
				// b ions 
				// first element of sequence
				xyta = new XYTextAnnotation(" " + peptideSequence.charAt(0) + " ", (0 + fragTableTheo[1][0]) / 2, maxY - (maxY - minY) * 0.15);
				xyta.setPaint(new Color(51,153,255));
				xyta.setBackgroundPaint(Color.white);
				xyta.setFont(new Font(null,Font.BOLD,11));
				plot.addAnnotation(xyta);
				// last element of sequence
				xyta = new XYTextAnnotation(" " + peptideSequence.charAt(peptideSequence.length()-1)+ " ", (fragTableTheo[1][sizeABCserie-2] + getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)) + fragTableTheo[1][sizeABCserie-2]) / 2, maxY - (maxY - minY) * 0.15);
				xyta.setPaint(new Color(51,153,255));
				xyta.setBackgroundPaint(Color.white);
				xyta.setFont(new Font(null,Font.BOLD,11));
				plot.addAnnotation(xyta);
				
				
				int size = Math.max(fragSer[positionIonABC].masses.length,fragSer[positionIonXYZ].masses.length);
				
				for (int i = 0; i < size; i++) { // loop through the series points

					// place separators marks------
					if (abcPrev != 0) {
						xyta = new XYTextAnnotation("|", abcPrev, maxY - (maxY - minY) * 0.15);
						xyta.setPaint(new Color(51,153,255));
						plot.addAnnotation(xyta);
					}
					
					
					if (xyzPrev != 0) {
						xyta = new XYTextAnnotation("|", xyzPrev, maxY - (maxY - minY) * 0.25);
						xyta.setPaint(new Color(255,85,85));
						plot.addAnnotation(xyta);
					}
					else
					{
						xyta = new XYTextAnnotation("|", fragTableTheo[6][sizeXYZserie -1], maxY - (maxY - minY) * 0.25);
						xyta.setPaint(new Color(255,85,85));
						plot.addAnnotation(xyta);
					}

					// place AA highlightings
					
					// ----- if 2 contiguous mass peaks are represented...draw the aa
					// interval Y
				
					if(i!=0 && fragTable[6][ i] != 0)
					{
						// write the triangle
						xyta = new XYTextAnnotation("\u25BE" , fragTableTheo[6][i], fragTable[5][i] + (maxY - minY) * 0.05);
						xyta.setPaint(new Color(255,85,85));
						plot.addAnnotation(xyta);
						// write the yx serie number
						xyta = new XYTextAnnotation(xyzSerieName + ( sizeXYZserie - i), fragTableTheo[6][i], fragTable[5][i] + (maxY - minY) * 0.1);
						xyta.setPaint(new Color(255,85,85));
						plot.addAnnotation(xyta);
						// dashed vertical bar
						float yAboveBar =  (float) ((maxY - minY) *0.15); 
						float dash[] = { 5.0f };
						if( fragTable[5][i]+ yAboveBar < fragTableTheo[5][i] ) { // draw only dashline if the y or b tag is not above the y/b line
							BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
							XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[6][i],fragTable[5][i] + yAboveBar, fragTableTheo[6][i] , fragTableTheo[5][i] , stk,new Color(255,85,85));
							plot.addAnnotation(line);
						}

					}
					
					if (xyzPrev != 0  && fragTable[6][ i-1] != 0) {
						
						if(fragTable[6][ i] != 0)
						{
							xyzPrevFound = true;
						}
						else
						{
							xyzPrevFound = false;
						}
						String aa = ""+  peptideSequence.charAt(i-1); /*getAminoAcidName( (float)Math.abs(xyzPrev - fragTableTheo[6][i]),tolerance);*/
						// draw the aa letter
						xyta = new XYTextAnnotation(" " + aa + " ", (xyzPrev + fragTable[6][i-1]) / 2, maxY - (maxY - minY) * 0.25);
						if(xyzPrevFound) { // 2 consecutives fragments matching, then highlight the AA
							xyta.setPaint(Color.white);
							xyta.setBackgroundPaint(new Color(255,85,85));
						} else {
							xyta.setPaint(new Color(255,85,85));
							xyta.setBackgroundPaint(Color.white);
						}
						xyta.setFont(new Font(null,Font.BOLD,11));
						plot.addAnnotation(xyta);
						
					} else {
						xyzPrevFound = false;
						if (xyzPrev != 0 && fragTableTheo[6][i-1] != 0) {
							String aa = ""+peptideSequence.charAt(i-1); // getAminoAcidName( (float)Math.abs(xyzPrev - fragTableTheo[6][i]),tolerance); // ,tolerance);
							xyta = new XYTextAnnotation("" + aa, (xyzPrev + fragTableTheo[6][i-1]) / 2, maxY - (maxY - minY) * 0.25);
							xyta.setPaint(new Color(255,85,85));
							xyta.setFont(new Font(null,Font.BOLD,11));
							xyta.setBackgroundPaint(Color.white);
							plot.addAnnotation(xyta);
						}
					} 
					// draw the outlined AA : B
					if (abcPrev != 0 && fragTable[1][i] != 0) {
						if(i==sizeABCserie-1) {
							;//	xyta = new XYTextAnnotation("b" + (i+1), maxX, fragTableTheo[0][i] + (maxY - minY) * 0.05);
						}
						else
						{
							// draw the triangle above the b number peak
							xyta = new XYTextAnnotation("\u25BE" , fragTableTheo[1][i], fragTable[0][i] + (maxY - minY) * 0.05);
							xyta.setPaint(new Color(51,153,255));
							plot.addAnnotation(xyta);
						// draw the b number overt the peak
							xyta = new XYTextAnnotation(abcSerieName + (i+1), fragTableTheo[1][i], fragTable[0][i] + (maxY - minY) * 0.1);
							xyta.setPaint(new Color(51,153,255));
							plot.addAnnotation(xyta);
							// dashed vertical bar over the b number
							float yAboveBar =  (float) ((maxY - minY) *0.15); 
							float dash[] = { 5.0f };
							if( fragTable[0][i] + yAboveBar < fragTableTheo[0][i] ) { // draw only dashline if the y or b tag is not above the y/b line 
								BasicStroke stk = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
							    XYLineAnnotation line = new XYLineAnnotation(fragTableTheo[1][i],fragTable[0][i] + yAboveBar, fragTableTheo[1][i] , fragTableTheo[0][i] , stk,new Color(51,153,255));
						        plot.addAnnotation(line);
							}

						}
						String aa = "" + peptideSequence.charAt(i); //getAminoAcidName( (float)Math.abs(abcPrev - fragTableTheo[1][i]),tolerance);//  , tolerance);
						xyta = new XYTextAnnotation(" " + aa + " ", (abcPrev + fragTable[1][i]) / 2, maxY - (maxY - minY) * 0.15);
						if(abcPrevFound) { // 2 consecutives fragments matching, then highlight the AA
							xyta.setPaint(Color.white);
							xyta.setBackgroundPaint(new Color(51,153,255));
						} else {
							xyta.setPaint(new Color(51,153,255));
							xyta.setBackgroundPaint(Color.white);
						}
						xyta.setFont(new Font(null,Font.BOLD,11));
						plot.addAnnotation(xyta);
						abcPrevFound = true;
					} else // draw the regular expected (but not found) aa
					{
						abcPrevFound = false;
						if (abcPrev != 0 && fragTableTheo[1][i] != 0) {
							String aa = ""+ peptideSequence.charAt(i); //getAminoAcidName( (float)Math.abs(abcPrev - fragTableTheo[1][i]),tolerance); //,tolerance);
							
							xyta = new XYTextAnnotation("" + aa, (abcPrev + fragTableTheo[1][i]) / 2, maxY - (maxY - minY) * 0.15);
							xyta.setPaint(new Color(51,153,255));
							xyta.setFont(new Font(null,Font.BOLD,11));
							 xyta.setBackgroundPaint(Color.white);
							plot.addAnnotation(xyta);
						}
					}

					
					xyzPrev = fragTableTheo[6][i+1];
					abcPrev = fragTableTheo[1][i];
					xyzPrevCharge = fragTableTheoCharge[6][i];
					abcPrevCharge = fragTableTheoCharge[1][i];
				}
				jsonProp=null;
			    array =null;
			    gson=null;
			    parser=null;
			   
			}
			
	
		    entityManagerMSI.getTransaction().commit(); // TODO tester en l'enlevant, mais il semble qu'on obtienne "Trop de commit" après avoir visualisé plusieurs dixaines de spectres...
			entityManagerMSI.close();
		//	entityManagerMSI.clear();
	  }
		
		
		
		public static double getMassFromAminoAcid (char aa) {
			double mass = 0;
			HashMap<Character,Double> aaHashMap  = new HashMap<Character,Double>();  

			aaHashMap.put('A', (double)71.03711);
			aaHashMap.put('C',(double)103.00919);
			aaHashMap.put('D',(double)115.02694);
			aaHashMap.put('E',(double)129.04259);
			aaHashMap.put('F',(double)147.06841);
			aaHashMap.put('G',(double)57.02146);
			aaHashMap.put('H',(double)137.05891);
			aaHashMap.put('I',(double)113.08406 );
			aaHashMap.put('K',(double)128.09496);
			aaHashMap.put('L',(double)113.08406);
			aaHashMap.put('M',(double)131.04049);
			aaHashMap.put('N',(double)114.04293);
			aaHashMap.put('P',(double)97.05276);
			aaHashMap.put('Q',(double)128.05858);
			aaHashMap.put('R',(double)156.10111);
			aaHashMap.put('S',(double)87.03203);
			aaHashMap.put('T',(double)101.04768);
			aaHashMap.put('V',(double)99.06841);
			aaHashMap.put('W',(double)186.07931);
			aaHashMap.put('Y',(double)163.06333);
			
		    //float deltaMoZ = 0;
			return aaHashMap.get(aa);
			
		}
		
		
		// the getAminoAcidName is not used but could be in the future...
	
		public String getAminoAcidName (double deltaMass, double tolerance)  {
			
		    // scan the spectrum to find potential aminoacids
			HashMap<Double,Character> aaHashMap  = new HashMap<Double,Character>();  
	
			 aaHashMap.put((double)71.03711, 'A');
			aaHashMap.put((double)103.00919, 'C');
			aaHashMap.put((double)115.02694, 'D');
			aaHashMap.put((double)129.04259, 'E');
			aaHashMap.put((double)147.06841, 'F');
			aaHashMap.put((double)57.02146, 'G');
			aaHashMap.put((double)137.05891, 'H');
			aaHashMap.put((double)113.08406, 'I');
			aaHashMap.put((double)128.09496, 'K');
			aaHashMap.put((double)113.08406, 'L');
			aaHashMap.put((double)131.04049, 'M');
			aaHashMap.put((double)114.04293, 'N');
			aaHashMap.put((double)97.05276, 'P');
			aaHashMap.put((double)128.05858, 'Q');
			aaHashMap.put((double)156.10111, 'R');
			aaHashMap.put((double)87.03203, 'S');
			aaHashMap.put((double)101.04768, 'T');
			aaHashMap.put((double)99.06841, 'V');
			aaHashMap.put((double)186.07931, 'W');
			aaHashMap.put((double)163.06333, 'Y');
		    //float deltaMoZ = 0;
		    
		 double toleranceCalc = tolerance;
		    System.out.println("--->Submitted mass of " + deltaMass);
	      for (double aaMass : aaHashMap.keySet()) {
	        // println (aaMass)
	    	  //toleranceCalc = (double) 0.1; //(aaMass) * tolerance / 1000000;
	        if ((aaMass - toleranceCalc < deltaMass) && (aaMass + toleranceCalc > deltaMass)) {
	          System.out.println("Tolerance " + tolerance + " (ppm) gives :" + toleranceCalc + " , Found Amino acid: " + aaHashMap.get(aaMass) + " of DeltaMass:" + deltaMass);
	          
	          return(aaHashMap.get(aaMass).toString());
	
	        }
	    
	     }
		     
		    
	  //    System.out.println("no AA found");
	      NumberFormat formatter = null;
	  	formatter=java.text.NumberFormat.getInstance(java.util.Locale.FRENCH); 
	  	formatter = new DecimalFormat("#0.000");
	 
		  return ("" + formatter.format( deltaMass)); //return ("*");
		  
	
		}
		
}

  



	
	
	
	
	