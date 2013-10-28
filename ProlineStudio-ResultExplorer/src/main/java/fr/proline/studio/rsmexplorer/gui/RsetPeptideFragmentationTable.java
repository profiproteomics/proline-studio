package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.JFreeChart;
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

public class RsetPeptideFragmentationTable {

	AbstractDataBox m_dataBox;
//	DefaultXYDataset m_dataSet;
	//JPanel fragmentationTablePanel;
	JPanel fragPanelContainer;

	DPeptideMatch peptideMatch;
	DPeptideMatch pm;
	JTable jTable1 ;
	
	public RsetPeptideFragmentationTable (AbstractDataBox m_dBox, JPanel fragPanel /* DefaultXYDataset m_dSet,*//* JFreeChart m_chrt,*/, DPeptideMatch pepMatch) {
		

		 m_dataBox = m_dBox;
		/* m_dataSet = m_dSet;
		 m_chart = m_chrt;*/
		
		 peptideMatch  = pepMatch;	
		 pm = peptideMatch;
		 fragPanelContainer = fragPanel;

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
	
	
	
	public void createFragmentationTable() {
	
		

		jTable1=  new javax.swing.JTable() ;
	  
	         
			final String SERIES_NAME = "spectrumData"; // TODO: change series name to fragmentation table
			
		 
	
			EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_dataBox.getProjectId()).getEntityManagerFactory().createEntityManager();
			entityManagerMSI.getTransaction().begin();
			
		
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
				
		//		LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("AW: clob data:" + clobData);
	
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
		//		m_dataSet.removeSeries(SERIES_NAME);
		//		removeAnnotations();
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
	
	
			size = 100; 
			double[][] fragTableTheo = new double[11][size];
			float [][] fragTableTheoCharge = new float [11][size];
			double[][] fragTable = new double[11][size];
		

			
			String peptideSequence = pm.getPeptide().getSequence();
			
	
			int j = 0;
			
	
			int positionIonB= 0;
			int positionIon_B2H = 0;
			int positionIonY= 0;
			int positionIon_BNH3 = 0;
			int positionIon_YNH3 = 0;
			int positionIon_BH2O = 0;
			int positionIon_YH2O = 0;
			int positionIon_Y2H = 0;
			
			for(int i = 0; i<fragSer.length;i++) { // TODO: en fait les frag series b s'appliquent aussi a b++ etc. donc va falloir faire un tableau de positions au lieu de juste Bposition
				
				
				if(fragSer[i].frag_series.equals("b")) {
					positionIonB = i;
				}
				else if(fragSer[i].frag_series.equals("b++")) {
					positionIon_B2H = i;
				}
				else if(fragSer[i].frag_series.equals("b-NH3")) {
					positionIon_BNH3 = i;
				}
				else if(fragSer[i].frag_series.equals("b-H2O")) {
					positionIon_BH2O = i;
				}
				else if(fragSer[i].frag_series.equals("y")) {
					positionIonY = i;
				}	
				else if(fragSer[i].frag_series.equals("y-NH3")) {
					positionIon_YNH3 = i;
				}
				else if(fragSer[i].frag_series.equals("y-H2O")) {
					positionIon_YH2O = i;
				} 
				else if(fragSer[i].frag_series.equals("y++")) {
					positionIon_Y2H = i;
				}

			}
				
			int sizeBserie = fragSer[positionIonB].masses.length;
			
				
			int sizeYserie = fragSer[positionIonY].masses.length;
			
			size = Math.max(fragSer[positionIonB].masses.length,fragSer[positionIonY].masses.length);
				


			String [] titles = /*new String[11];//*/ 
				{ "B", "B ions", "B+2H", "B-NH3", "B-H20", "AA", "Y ions", "Y+2H", "Y-NH3", "Y-H2O", "Y" };
			
		

			  jTable1 = new javax.swing.JTable();
			  
			  DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
					  
						new Object[][] {

						},  titles) {
							Class[] types = new Class[] { Test2CustomRenderer.class, Test2CustomRenderer.class,Test2CustomRenderer.class,Test2CustomRenderer.class,Test2CustomRenderer.class,Test2CustomRenderer.class,Test2CustomRenderer.class,Test2CustomRenderer.class,Test2CustomRenderer.class, Test2CustomRenderer.class,Test2CustomRenderer.class};
									
									
									
//									java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
//										java.lang.Double.class,/* java.lang.String.class,*/ java.lang.Double.class, java.lang.Double.class/*, java.lang.Double.class,
//										java.lang.Object.class, java.lang.Object.class*/ };
							boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false, false, false,false, false/*, false, false, true*/ };

							public Class getColumnClass(int columnIndex) {
								return types[columnIndex];
							}

							public boolean isCellEditable(int rowIndex, int columnIndex) {
								return canEdit[columnIndex];
							}
						};

				RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
				Test2CustomRenderer cr = new Test2CustomRenderer();
				jTable1.setDefaultRenderer(String.class, cr);  
				jTable1.setRowSorter(sorter);
				
				
			

				int col = 0;
				for ( int i = 0; i < fragSer[0].masses.length ; i++) { // on boucle sur les masses de fragSer

					
					Vector v = new Vector(); // la ligne du tableau

					v.add(i+1); // le numero de ligne
					if(fragSer[positionIonB].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIonB].masses[i] * 10000) / 10000); // round mass to 4 decimals
					else
						v.add("");
					if(fragSer[positionIon_B2H].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIon_B2H].masses[i] * 10000) / 10000);
					else
						v.add("");
					if(fragSer[positionIon_BNH3].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIon_BNH3].masses[i] * 10000) / 10000); 
					else
						v.add(""); 
					if( fragSer[positionIon_BH2O].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIon_BH2O].masses[i] * 10000) / 10000);
					else
						v.add("");
					if(i<peptideSequence.length())
						v.add(peptideSequence.charAt(i));
					else
						v.add("?"); // problem: should be of the right size...need debugging!
					if( fragSer[positionIonY].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIonY].masses[i] * 10000) / 10000);
					else
						v.add("");
					if(fragSer[positionIon_Y2H].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIon_Y2H].masses[i] * 10000) / 10000); 
					else
						v.add(""); 
					if(fragSer[positionIon_YNH3].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIon_YNH3].masses[i] * 10000) / 10000);
					else
						v.add(""); 
					if(fragSer[positionIon_YH2O].masses[i]!=0)
						v.add((double)Math.round(fragSer[positionIon_YH2O].masses[i] * 10000) / 10000);
					else
						v.add(""); 
					v.add(fragSer[0].masses.length -i); // y serie index
				
					// remove zeros and display an empty field instead
					
					tableModel.addRow(v);
					
				}
				jTable1.setModel(tableModel);
				jTable1.setVisible(true);
			
			Test2CustomRenderer renderer =  (Test2CustomRenderer) new Test2CustomRenderer();  
			jTable1.setDefaultRenderer(Object.class , renderer);
			boolean [][] matrix = new boolean[100][100];
			j = 0;
			
			
			j=0;
			double roundTol = 0.000001;
			int nbFound = 0;
			int nbThroughB = 0;
			int nbThroughY = 0;
				for ( j = 0; j < fragSer.length ; j++) { // loop through theoFragment series here
					
					
					for(int k = 0; k < fragSer[j].masses.length ;k++) { // loop through masses for each fragment serie
						
						
					
						for( int i = 0 ; i<fragMa.length ; i++) {  // find matching fragMatches with theoFragSeries
							System.out.println("i,j,k:" + i + " " + j+ " " + k + "/" + fragSer[j].masses.length + " nbThroughB=" + nbThroughB + " nbThroughY=" + nbThroughY);
							System.out.println("Charge : " + fragSer[j].charge);
							fragSer[j].computeCharge();
							System.out.println("serie:" + fragSer[j].frag_series + " -  Charge : " + fragSer[j].charge);
							if(j == positionIonB) {
					//			fragTableTheo[0][nbThroughB] = maxY - (maxY - minY) * 0.15; // data[1][i]; // intensity for b ions
								fragTableTheo[1][nbThroughB] = fragSer[j].masses[k]; // data[0][i];
								//fragSer[j].computeCharge();
								fragTableTheoCharge[0][nbThroughB] = fragSer[j].charge; 
								if( (fragMa[i].calculated_moz - roundTol <= ((double)(fragSer[j].charge) * fragSer[j].masses[k])) && (fragMa[i].calculated_moz + roundTol >= (double)(fragSer[j].charge) * fragSer[j].masses[k])) {
									nbFound++;
							
									
									matrix[k][1] = true;
									renderer.setSelectMatrix(matrix);
									System.out.println("nbThroughB = " + nbThroughB + " , found" + nbFound + " moz" + fragMa[i].moz);
					//				fragTable[0][nbThroughB] = maxY - (maxY - minY) * 0.15; //data[1][i];
									fragTable[1][nbThroughB] =  fragSer[j].masses[k];; //fragMa[positionIonB].moz ; //data[0][i];
								}
								else
								{
									//if(fragTable[0][nbThroughB])
//									fragTable[0][nbThroughB] = 0; // 0 means no data so we omit the peak
//									fragTable[1][nbThroughB] = 0;
								}
								
							}
							if(j == positionIonY) {
					//			fragTableTheo[5][nbThroughY] = maxY - (maxY - minY) * 0.25; // data[1][i]; // intensity for b ions
								fragTableTheo[6][nbThroughY] = fragSer[j].masses[k]; // data[0][i];
							//	fragSer[j].computeCharge();
								fragTableTheoCharge[5][nbThroughY] = fragSer[j].charge; 
								if( (fragMa[i].calculated_moz - roundTol <= (double)(fragSer[j].charge) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(fragSer[j].charge) * fragSer[j].masses[k])) {
									nbFound++;
						
									matrix[k][6] = true;
									renderer.setSelectMatrix(matrix);
									System.out.println("nbThroughY = " + nbThroughY + " , found" + nbFound + " moz" + fragMa[i].calculated_moz);
					//				fragTable[5][nbThroughY] = maxY - (maxY - minY) * 0.25; //data[1][i];
									fragTable[6][nbThroughY] =  fragSer[j].masses[k]; //fragMa[positionIonB].moz ; //data[0][i];
								}
								else
								{
//									fragTable[5][nbThroughY] = 0; // 0 means no data so we omit the peak
//									fragTable[6][nbThroughY] = 0;
								}
								
							}
							else if(j == positionIon_YNH3) {
								if( (fragMa[i].calculated_moz - roundTol <= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k])) {
									nbFound++;
									matrix[k][8 /*position Y dans le tableau */] = true;
									renderer.setSelectMatrix(matrix);
								}
							}
							else if(j == positionIon_BNH3) {
								if( (fragMa[i].calculated_moz - roundTol <= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k])) {
									nbFound++;
									matrix[k][3 /*position Y dans le tableau */] = true;
									renderer.setSelectMatrix(matrix);
								}
							}
							else if(j == positionIon_BH2O) {
								if( (fragMa[i].calculated_moz - roundTol <= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k])) {
									nbFound++;
									matrix[k][4 /*position Y dans le tableau */] = true;
									renderer.setSelectMatrix(matrix);
								}
							}
							else if(j == positionIon_B2H) {
								if( (fragMa[i].calculated_moz - roundTol <= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k])) {
									nbFound++;
									matrix[k][2 /*position Y dans le tableau */] = true;
									renderer.setSelectMatrix(matrix);
								}
							}
							else if(j == positionIon_Y2H) {
								if( (fragMa[i].calculated_moz - roundTol <= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k])) {
									nbFound++;
									matrix[k][7 /*position Y dans le tableau */] = true;
									renderer.setSelectMatrix(matrix);
								}
							}
							else if(j == positionIon_YH2O) {
								if( (fragMa[i].calculated_moz - roundTol <= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k]) && (fragMa[i].calculated_moz + roundTol >= (double)(1/*fragSer[j].charge*/) * fragSer[j].masses[k])) {
									nbFound++;
									matrix[k][9 /*position Y dans le tableau */] = true;
									renderer.setSelectMatrix(matrix);
								}
							}
							
					
						}
						if(j == positionIonB) 
							nbThroughB ++;
						if(j == positionIonY) 
							nbThroughY++;
					}
					
				}
				
	
			    
				jTable1.setModel(tableModel);
					
				renderer.setSelectMatrix(matrix);
				JScrollPane fragPane = new JScrollPane(jTable1);
				fragPanelContainer.removeAll();
				fragPanelContainer.add(fragPane,BorderLayout.NORTH); //fragmentationTablePanel);
	
			    fragPane.setPreferredSize(fragPanelContainer.getSize());
		        fragPanelContainer.revalidate();
		        fragPanelContainer.repaint();
		
			
				jsonProp=null;
			    array =null;
			    gson=null;
			    parser=null;
			
			   
			}
			
		//	entityManagerMSI.getTransaction().commit(); // TODO tester en l'enlevant
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
		
		public class Test2CustomRenderer extends DefaultTableCellRenderer /*implements TableCellRenderer*/ {
			int targetRow = 0;
			int targetCol = 0;
			boolean [][] selectMatrix = new boolean [100][100];
			
			public void setTargetCell(int row, int col) {
				this.targetRow = row;
				this.targetCol = col;
				
			}
			public void setSelectMatrix(boolean[][] matx ) {
				this.selectMatrix = matx.clone();
			}
			
			    @Override
			    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			   
			      
			        if ( selectMatrix[row] [column]) { // highlight the cell if true in selectMatrix
			            Color clr = new Color(51, 153, 255);
					    component.setBackground(clr);
			            component.setForeground(new Color(255,255,255));
			        } else {
			        	Color clr;
			        	if(row % 2 ==0) 
			        		clr = new Color(255, 255, 255); 
			            else
			            	clr = new Color(224, 233, 246);
			            component.setBackground(clr);
			            component.setForeground(new Color(0,0,0));
			        }
			      
			        return component;
			    }
			}
		
}

  



