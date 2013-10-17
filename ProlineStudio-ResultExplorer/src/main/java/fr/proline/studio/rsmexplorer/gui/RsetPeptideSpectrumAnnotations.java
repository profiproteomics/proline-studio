package fr.proline.studio.rsmexplorer.gui;

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
				
				LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("AW: clob data:" + clobData);
	
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
			int size = intensityDoubleArray.length;
			double[][] data = new double[2][size];
			for (int i = 0; i < size; i++) {
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
	
			SpectrumMatchAW spectrMatch = new SpectrumMatchAW(fragSer, fragMa);

			//size = 25; 
			double[][] fragTableTheo = new double[11][size];
			float [][] fragTableTheoCharge = new float [11][size];
			double[][] fragTable = new double[11][size];
			char[] aaNames = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y' };
	
			// **-*-*-* HERE READING Data from Objects *-*-*-*-**-
			SpectrumMatchAW spMatch = spectrMatch;
		
			
			
			String peptideSequence = pm.getPeptide().getSequence();
			double minY = DatasetUtilities.findMinimumRangeValue(m_dataSet).doubleValue();
			double maxY = DatasetUtilities.findMaximumRangeValue(m_dataSet).doubleValue();
			double maxX = DatasetUtilities.findMaximumDomainValue(m_dataSet).doubleValue(); // in order to know where to place the last AA sequence limit
			removeAnnotations();
			XYTextAnnotation xyta;
			XYPlot plot = (XYPlot) m_chart.getPlot();
			// annotations 
	
			plot.clearRangeMarkers();
			Marker target = new ValueMarker(maxY - (maxY - minY) * 0.25);
			target.setPaint(Color.red);
			target.setLabel("y");
			target.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			plot.addRangeMarker(target);
			Marker target2 = new ValueMarker(maxY - (maxY - minY) * 0.15);
			target2.setPaint(Color.blue);
			target2.setLabel("b");
			//target.setLabelFont(new Font("Sansserif",Font.BOLD,11));
			target2.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			target2.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			//target2.setLabelFont(new Font("Sansserif",Font.BOLD,11));
			plot.addRangeMarker(target2);
	
			int j = 0;
			// ************************************************************
			// *-*-*- Load fragmentation table (theoretical and measured) *
			// ************************************************************
		
	
			//String peptideSequence = "RVPPLG";
			int positionIonB= 0;
			int positionIonY= 0;
			for(int i = 0; i<fragSer.length;i++) { // TODO: en fait les frag series b s'appliquent aussi a b++ etc. donc va falloir faire un tableau de positions au lieu de juste Bposition
				
				switch  ( fragSer[i].frag_series.charAt(0)) {
	
				case 'b' :
					if(fragSer[i].frag_series.length()>1) {
						// then it is either a ++ or a b-H2O and so on...
					}
					else
					{ // it's a 'b' ion
						positionIonB = i;
					}
					break;
				case 'y' :
					if(fragSer[i].frag_series.length()>1) {
						// then it is either a ++ or a b-H2O and so on...
					}
					else
					{ // it's a 'y' ion
						positionIonY = i;
					}
					break;
				default :
					break;
				}
				
				
			}
			
			int sizeBserie = fragSer[positionIonB].masses.length;
			
				
			int sizeYserie = fragSer[positionIonY].masses.length;
			
			size = Math.max(fragSer[positionIonB].masses.length,fragSer[positionIonY].masses.length);
				
			
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
						System.out.println("i,j,k:" + i + " " + j+ " " + k + "/" + fragSer[j].masses.length + " nbThroughB=" + nbThroughB + " nbThroughY=" + nbThroughY);
						System.out.println("Charge : " + fragSer[j].charge);
						fragSer[j].computeCharge();
						System.out.println("serie:" + fragSer[j].frag_series + " -  Charge : " + fragSer[j].charge);
						if(j == positionIonB) {
							fragTableTheo[0][nbThroughB] = maxY - (maxY - minY) * 0.15; // data[1][i]; // intensity for b ions
							fragTableTheo[1][nbThroughB] = fragSer[j].masses[k]; // data[0][i];
							//fragSer[j].computeCharge();
							fragTableTheoCharge[0][nbThroughB] = fragSer[j].charge; 
							if( (fragMa[i].calculated_moz - roundTol <= ((double)(fragSer[j].charge) * fragSer[j].masses[k])) && (fragMa[i].calculated_moz + roundTol >= (double)(fragSer[j].charge) * fragSer[j].masses[k])) {
								nbFound++;
								System.out.println("nbThroughB = " + nbThroughB + " , found" + nbFound + " moz" + fragMa[i].moz);
								fragTable[0][nbThroughB] =  fragMa[i].intensity ;  // /*maxY*/ - (maxY - minY) * 0.15; //data[1][i];
								fragTable[1][nbThroughB] =  fragSer[j].masses[k];; //fragMa[positionIonB].moz ; //data[0][i];
							}
							else
							{
								//if(fragTable[0][nbThroughB])
//								fragTable[0][nbThroughB] = 0; // 0 means no data so we omit the peak
//								fragTable[1][nbThroughB] = 0;
							}
							
						}
						if(j == positionIonY) {
							fragTableTheo[5][nbThroughY] = maxY - (maxY - minY) * 0.25; // data[1][i]; // intensity for b ions
							fragTableTheo[6][nbThroughY] = fragSer[j].masses[k]; // data[0][i];
						//	fragSer[j].computeCharge();
							fragTableTheoCharge[5][nbThroughY] = fragSer[j].charge; 
							if( (fragMa[i].calculated_moz - roundTol <= (double)(fragSer[j].charge) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(fragSer[j].charge) * fragSer[j].masses[k])) {
								nbFound++;
								System.out.println("nbThroughY = " + nbThroughY + " , found" + nbFound + " moz" + fragMa[i].calculated_moz);
								fragTable[5][nbThroughY] = fragMa[i].intensity ; //data[1][i];
								fragTable[6][nbThroughY] =  fragSer[j].masses[k]; //fragMa[positionIonB].moz ; //data[0][i];
							}
							else
							{
//								fragTable[5][nbThroughY] = 0; // 0 means no data so we omit the peak
//								fragTable[6][nbThroughY] = 0;
							}
							
						}
						
				
				}
				if(j == positionIonB) 
					nbThroughB ++;
				if(j == positionIonY) 
					nbThroughY++;
			}
		}
			
				//**-*-*-*-*
				
				float tolerance = (float) 0.7; //0.01; // could be 0 but to be sure a match is performed...
				// place annotations
				double yPrev =  0; //fragTableTheo[6][1] + getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)) ;
				fragTableTheo[6][0] =  fragTableTheo[6][1] + getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)) ;
				double bPrev = 0; //fragTableTheo[1][0];
				float yPrevCharge = fragTableTheoCharge[6][1];
				float bPrevCharge =0;// fragTableTheoCharge[1][0];
				boolean yPrevFound = false; // indicates if last iteration was a match or not. (if yes then highlight the AA)
				boolean bPrevFound = false;
				// place initial and last peptide sequence elements 
				// y ions
//				System.out.println("maxX:" + maxX);
				// first peptide element of sequence
//				xyta = new XYTextAnnotation("" + peptideSequence.charAt(0), (maxX + fragTableTheo[6][1]) / 2, maxY - (maxY - minY) * 0.25);
//				xyta.setPaint(Color.red);
//				xyta.setFont(new Font(null,Font.BOLD,11));
//				xyta.setBackgroundPaint(Color.white);
//				plot.addAnnotation(xyta);
				// last peptide element of sequence
				xyta = new XYTextAnnotation("" + peptideSequence.charAt(peptideSequence.length()-1), ( fragTableTheo[6][sizeYserie-1] + (fragTableTheo[6][sizeYserie-1] - getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)))) / 2, maxY - (maxY - minY) * 0.25);
				xyta.setPaint(Color.red);
				xyta.setFont(new Font(null,Font.BOLD,11));
				xyta.setBackgroundPaint(Color.white);
				plot.addAnnotation(xyta);
				
				// b ions 
				// first element of sequence
				xyta = new XYTextAnnotation(" " + peptideSequence.charAt(0) + " ", (0 + fragTableTheo[1][0]) / 2, maxY - (maxY - minY) * 0.15);
				xyta.setPaint(Color.blue);
				xyta.setBackgroundPaint(Color.white);
				xyta.setFont(new Font(null,Font.BOLD,11));
				plot.addAnnotation(xyta);
				// last element of sequence
				xyta = new XYTextAnnotation(" " + peptideSequence.charAt(peptideSequence.length()-1)+ " ", (fragTableTheo[1][sizeBserie-2] + getMassFromAminoAcid(peptideSequence.charAt(peptideSequence.length()-1)) + fragTableTheo[1][sizeBserie-2]) / 2, maxY - (maxY - minY) * 0.15);
				xyta.setPaint(Color.blue);
				xyta.setBackgroundPaint(Color.white);
				xyta.setFont(new Font(null,Font.BOLD,11));
				plot.addAnnotation(xyta);
				
				
				for (int i = 0; i < size; i++) {

					// place separators marks------
					if (bPrev != 0) {
						xyta = new XYTextAnnotation("|", bPrev, maxY - (maxY - minY) * 0.15);
						xyta.setPaint(Color.blue);
						plot.addAnnotation(xyta);
					}
					
					
					if (yPrev != 0) {
						xyta = new XYTextAnnotation("|", yPrev, maxY - (maxY - minY) * 0.25);
						xyta.setPaint(Color.red);
						plot.addAnnotation(xyta);
					}
					else
					{
						xyta = new XYTextAnnotation("|", fragTableTheo[6][sizeYserie -1], maxY - (maxY - minY) * 0.25);
						xyta.setPaint(Color.red);
						plot.addAnnotation(xyta);
					}

					// place AA highlightings
					
					System.out.println("i=" + i + " , yPrev= " + yPrev + " , \tfragTable[6][" +i + "]=" + fragTable[6][i] + "\t, fragTableTheo[6][" +i + "]=" + fragTableTheo[6][i] );
					System.out.println("i=" + i + " , bPrev= " + bPrev + " , \tfragTable[1][" +i + "]=" + fragTable[1][i] + "\t, fragTableTheo[1][" +i + "]=" + fragTableTheo[1][i] );
					// ----- if 2 contiguous mass peaks are represented...draw the aa
					// interval Y
				
					if(i!=0 && fragTable[6][ i] != 0)
					{
						xyta = new XYTextAnnotation("\u25BE" , fragTableTheo[6][i], fragTable[5][i] + (maxY - minY) * 0.05);
						xyta.setPaint(Color.red);
						plot.addAnnotation(xyta);
						xyta = new XYTextAnnotation("y" + ( sizeYserie - i), fragTableTheo[6][i], fragTable[5][i] + (maxY - minY) * 0.1);
						xyta.setPaint(Color.red);
						plot.addAnnotation(xyta);
					}
					
					if (yPrev != 0  && fragTable[6][ i-1] != 0) {
						
						if(fragTable[6][ i] != 0)
						{
							yPrevFound = true;
						}
						else
						{
							yPrevFound = false;
						}
						String aa = ""+  peptideSequence.charAt(i-1); /*getAminoAcidName( (float)Math.abs(yPrev - fragTableTheo[6][i]),tolerance);*/
						
						xyta = new XYTextAnnotation(" " + aa + " ", (yPrev + fragTable[6][i-1]) / 2, maxY - (maxY - minY) * 0.25);
						if(yPrevFound) { // 2 consecutives fragments matching, then highlight the AA
							xyta.setPaint(Color.white);
							xyta.setBackgroundPaint(Color.red);
						} else {
							xyta.setPaint(Color.red);
							xyta.setBackgroundPaint(Color.white);
						}
						xyta.setFont(new Font(null,Font.BOLD,11));
						plot.addAnnotation(xyta);
						
					} else {
						yPrevFound = false;
						if (yPrev != 0 && fragTableTheo[6][i-1] != 0) {
							String aa = ""+peptideSequence.charAt(i-1); // getAminoAcidName( (float)Math.abs(yPrev - fragTableTheo[6][i]),tolerance); // ,tolerance);
							xyta = new XYTextAnnotation("" + aa, (yPrev + fragTableTheo[6][i-1]) / 2, maxY - (maxY - minY) * 0.25);
							xyta.setPaint(Color.red);
							xyta.setFont(new Font(null,Font.BOLD,11));
							xyta.setBackgroundPaint(Color.white);
							plot.addAnnotation(xyta);
						}
					} 
					// draw the outlined AA : B
					if (bPrev != 0 && fragTable[1][i] != 0) {
						if(i==sizeBserie-1) {
							;//	xyta = new XYTextAnnotation("b" + (i+1), maxX, fragTableTheo[0][i] + (maxY - minY) * 0.05);
						}
						else
						{
							xyta = new XYTextAnnotation("\u25BE" , fragTableTheo[1][i], fragTable[0][i] + (maxY - minY) * 0.05);
							xyta.setPaint(Color.blue);
							plot.addAnnotation(xyta);
						
							xyta = new XYTextAnnotation("b" + (i+1), fragTableTheo[1][i], fragTable[0][i] + (maxY - minY) * 0.1);
							xyta.setPaint(Color.blue);
							plot.addAnnotation(xyta);
						}
						String aa = "" + peptideSequence.charAt(i); //getAminoAcidName( (float)Math.abs(bPrev - fragTableTheo[1][i]),tolerance);//  , tolerance);
						xyta = new XYTextAnnotation(" " + aa + " ", (bPrev + fragTable[1][i]) / 2, maxY - (maxY - minY) * 0.15);
						if(bPrevFound) { // 2 consecutives fragments matching, then highlight the AA
							xyta.setPaint(Color.white);
							xyta.setBackgroundPaint(Color.blue);
						} else {
							xyta.setPaint(Color.blue);
							xyta.setBackgroundPaint(Color.white);
						}
						xyta.setFont(new Font(null,Font.BOLD,11));
						plot.addAnnotation(xyta);
						bPrevFound = true;
					} else // draw the regular expected (but not found) aa
					{
						bPrevFound = false;
						if (bPrev != 0 && fragTableTheo[1][i] != 0) {
							String aa = ""+ peptideSequence.charAt(i); //getAminoAcidName( (float)Math.abs(bPrev - fragTableTheo[1][i]),tolerance); //,tolerance);
							
							xyta = new XYTextAnnotation("" + aa, (bPrev + fragTableTheo[1][i]) / 2, maxY - (maxY - minY) * 0.15);
							xyta.setPaint(Color.blue);
							xyta.setFont(new Font(null,Font.BOLD,11));
							 xyta.setBackgroundPaint(Color.white);
							plot.addAnnotation(xyta);
						}
					}

					//if(i<sizeYserie) 
						yPrev = fragTableTheo[6][i+1];
					//else
					//	yPrev = 0; // previous theoretical m/z for y & b ions
					bPrev = fragTableTheo[1][i];
					yPrevCharge = fragTableTheoCharge[6][i];
					bPrevCharge = fragTableTheoCharge[1][i];
				}
				jsonProp=null;
			    array =null;
			    gson=null;
			    parser=null;
			
			}
	
			entityManagerMSI.getTransaction().commit(); // TODO tester en l'enlevant
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

  



	
	
	
	
	