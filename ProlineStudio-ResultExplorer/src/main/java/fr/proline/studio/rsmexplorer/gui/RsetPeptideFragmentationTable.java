package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import fr.proline.studio.utils.DecoratedTable;
import javax.swing.table.*;

public class RsetPeptideFragmentationTable {

	AbstractDataBox m_dataBox;
//	DefaultXYDataset m_dataSet;
	//JPanel fragmentationTablePanel;
	JPanel fragPanelContainer;

	DPeptideMatch peptideMatch;
	DPeptideMatch pm;
	DecoratedTable jTable1 ;
	
	public RsetPeptideFragmentationTable (AbstractDataBox m_dBox, JPanel fragPanel /* DefaultXYDataset m_dSet,*//* JFreeChart m_chrt,*/, DPeptideMatch pepMatch) {
		

		 m_dataBox = m_dBox;
		/* m_dataSet = m_dSet;
		 m_chart = m_chrt;*/
		
		 peptideMatch  = pepMatch;	
		 pm = peptideMatch;
		 fragPanelContainer = fragPanel;

	}


	private class JsonProperties {
	    //String jsonProperties = "{\"ms_query_initial_id\":3,\"peptide_match_rank\":4,\"frag_table\":[{\"frag_series\":\"b\",\"masses\":[72.04439,171.112804,268.165568,365.218332,478.302396,0]},\"frag_matches\":[{\"label\":\"b(2)\",\"moz\":171.161,\"calculated_moz\":171.112804,\"intensity\":4.932}]}";
	    public int ms_query_initial_id;
	    public int peptide_match_rank;
	    public TheoreticalFragmentSeries_AW [] frag_table;
	    public FragmentMatch_AW [] frag_matches;
	           
	}
	
	protected class TheoreticalFragmentSeries_AW {
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
	
	protected class FragmentMatch_AW {
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
	
		

		jTable1=  new DecoratedTable() ;
	  
	         
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


			jTable1 = new DecoratedTable();

			
                        
                    FragmentationTableModel fragmentationTableModel = new FragmentationTableModel();
                    fragmentationTableModel.setData(fragMa, fragSer, pm.getPeptide().getSequence());


			RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(fragmentationTableModel);
			FragTableCustomRenderer cr = new FragTableCustomRenderer();
			jTable1.setDefaultRenderer(Double.class, cr);  
			jTable1.setRowSorter(sorter);

			jTable1.setModel(fragmentationTableModel);
			jTable1.setVisible(true);

			    
			jTable1.setModel(fragmentationTableModel);
				
			cr.setSelectMatrix(fragmentationTableModel.getMatrix());
			
			JScrollPane fragPane = new JScrollPane(jTable1);
                        fragPane.setViewportView(jTable1);
                        
			fragPanelContainer.removeAll();
			fragPanelContainer.add(fragPane,BorderLayout.CENTER); //fragmentationTablePanel);
			


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
		
                
    public static class FragmentationTableModel  extends AbstractTableModel {

        private TheoreticalFragmentSeries_AW[] m_fragSer;
        private FragmentMatch_AW [] m_fragMa;
        
        private String m_peptideSequence;
        private int m_sizeMaxSeries;

        private  String[][] m_matrix;
        
        private String[] m_columnNames;
        
        public FragmentationTableModel() {

        }
        
        public void setData(FragmentMatch_AW [] fragMa, TheoreticalFragmentSeries_AW[] fragSer, String peptideSequence) {
            m_fragMa = fragMa;
            m_fragSer = fragSer;
            m_peptideSequence = peptideSequence;
            
            int sizeMaxSeries = 0;
            for (int i = 0; i < fragSer.length; i++) { // TODO: en fait les frag series b s'appliquent aussi a b++ etc. donc va falloir faire un tableau de positions au lieu de juste Bposition
                if (fragSer[i].masses.length > sizeMaxSeries) {
                    sizeMaxSeries = fragSer[i].masses.length;
                }

            }
            
            m_sizeMaxSeries = sizeMaxSeries;
         
            
            // get series names
            String xyzSerieName = "";
            String abcSerieName = "";
            for (int i = 0; i < fragSer.length; i++) { // TODO: en fait les frag series b s'appliquent aussi a b++ etc. donc va falloir faire un tableau de positions au lieu de juste Bposition
                switch (fragSer[i].frag_series.charAt(0)) {
                    case 'a':  // either a,b or c do:
                    case 'b':
                    case 'c':
                        if (fragSer[i].frag_series.length() > 1) {
                            // then it is either a ++ or a b-H2O and so on...
                        } else { // it's a 'a/b/c' ion
                            abcSerieName = "" + fragSer[i].frag_series.charAt(0);
                        }
                        break;
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':

                        if (fragSer[i].frag_series.length() > 1) {
                            // then it is either a ++ or a b-H2O and so on...
                        } else { // it's a 'x/y/z' ion
                            xyzSerieName = "" + fragSer[i].frag_series.charAt(0);
                        }
                        break;
                    case 'z':
                        xyzSerieName = "" + fragSer[i].frag_series.charAt(0);
                        break;
                    default:
                        break;
                }
            }

            
            m_columnNames = new String[fragSer.length+3];
            int i = 0;
            m_columnNames[i++] = "amino acid";
            m_columnNames[i++] = abcSerieName + " ion";
            
            for (TheoreticalFragmentSeries_AW currentFrag : fragSer) {
                m_columnNames[i++] = currentFrag.frag_series;

            }
            
            m_columnNames[i] = xyzSerieName + " ion";
            
            
            m_matrix = new String[sizeMaxSeries][fragSer.length + 3];

            double roundTol = 0.0001;
            int nbFound = 0;

            for (int j = 0; j < fragSer.length; j++) { // loop through theoFragment series here
                for (int k = 0; k < fragSer[j].masses.length; k++) { // loop through masses for each fragment serie
                    for (i = 0; i < fragMa.length; i++) {  // find matching fragMatches with theoFragSeries

                        //fragSer[j].computeCharge();


                        if ((fragMa[i].calculated_moz - roundTol <= (fragSer[j].masses[k])) && (fragMa[i].calculated_moz + roundTol >= fragSer[j].masses[k])) {
                            nbFound++;

                            if (fragSer[j].frag_series.toUpperCase().contains("A") || fragSer[j].frag_series.toUpperCase().contains("B") || fragSer[j].frag_series.toUpperCase().contains("C")) {
                                m_matrix[k][j + 2] = "ABC";
                            } else if (fragSer[j].frag_series.toUpperCase().contains("X") || fragSer[j].frag_series.toUpperCase().contains("Y") || fragSer[j].frag_series.toUpperCase().contains("Z")) {
                                m_matrix[k][j + 2] = "XYZ";
                            } else {
                                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("AW: strange, there is no ABC nor XYZ ions..." + fragSer[j].frag_series);
                            }


                        } else {
                        }

                    }
                }
            }

        }
        
        public String[][] getMatrix() {
            return m_matrix;
        }
        
        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }

        @Override
        public int getRowCount() {
            return m_sizeMaxSeries;
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            if (columnIndex == 1) {
                return Integer.class;
            }
            if (columnIndex == m_columnNames.length-1) {
                return Integer.class;
            }
            
            return Double.class;
            
            
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {

                if (rowIndex < m_peptideSequence.length()) {
                    return m_peptideSequence.charAt(rowIndex);
                } else {
                    return "?"; // problem: should be of the right size...need debugging!
                }
            }
            
            if (columnIndex == 1) {
                    return rowIndex+1;
            }
            
            if (columnIndex == m_columnNames.length-1) {
                   return m_sizeMaxSeries-rowIndex; 
            }
                    
            TheoreticalFragmentSeries_AW currentFragSer = m_fragSer[columnIndex-2];

            if (currentFragSer.masses[rowIndex] !=0 ) {
                return (double)Math.round(currentFragSer.masses[rowIndex] * 10000) / 10000;
	    } else { 
                return null;
            }
            
        }
        
    }
                
    public static class FragTableCustomRenderer extends org.jdesktop.swingx.renderer.DefaultTableRenderer {

        private String[][] m_selectMatrix = new String[100][100];

        private Font m_fontPlain = null;
        private Font m_fontBold = null;
        
        private final static Color LIGHT_BLUE_COLOR = new Color(51, 153, 255);
        private final static Color LIGHT_RED_COLOR = new Color(255, 85, 85);
        
        private final static Color EXTRA_LIGHT_BLUE_COLOR = new Color(175, 255, 255);
        private final static Color EXTRA_LIGHT_RED_COLOR = new Color(255, 230, 230);
        
        public void setSelectMatrix(String[][] matx) {
            m_selectMatrix = matx;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // prepare needed fonts
            if (m_fontPlain == null) {
                m_fontPlain = component.getFont().deriveFont(Font.PLAIN);
                m_fontBold = m_fontPlain.deriveFont(Font.BOLD);
            }

            // select font
            if (m_selectMatrix[row][column] != null) {
                component.setFont(m_fontBold);
            } else {
                component.setFont(m_fontPlain);
            }
            
            // select color
            Color foregroundColor = null;

            if (m_selectMatrix[row][column] != null) {

                if (m_selectMatrix[row][column].contains("ABC")) { // highlight the cell if true in selectMatrix
                    foregroundColor = (isSelected) ? EXTRA_LIGHT_BLUE_COLOR : LIGHT_BLUE_COLOR;
                } else if (m_selectMatrix[row][column].contains("XYZ")) {
                    foregroundColor = (isSelected) ? EXTRA_LIGHT_RED_COLOR : LIGHT_RED_COLOR;
                } else {
                    foregroundColor = (isSelected) ? Color.white : Color.black;
                }
            } else {
                // standard color:
                foregroundColor = (isSelected) ? Color.white : Color.black;
            }
            
            component.setForeground(foregroundColor);

            return component;

        }
        
        
        
    }
		
}

  



