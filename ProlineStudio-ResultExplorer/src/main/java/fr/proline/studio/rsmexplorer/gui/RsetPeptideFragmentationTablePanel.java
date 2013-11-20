package fr.proline.studio.rsmexplorer.gui;



import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.LoggerFactory;

import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;


/**
 * Panel used to display a Spectrum of a PeptideMatch
 * 
 * @author AW
 */

public class RsetPeptideFragmentationTablePanel extends HourglassPanel implements DataBoxPanelInterface {

    
	private AbstractDataBox m_dataBox;

	private DefaultXYDataset m_dataSet;
	//private JFreeChart m_chart;

	private DPeptideMatch m_previousPeptideMatch = null;

	private RsetPeptideFragmentationTable fragmentationTable= null;
	
	public RsetPeptideFragmentationTablePanel() {
            setLayout(new BorderLayout());
        }	
        
        
	/**
	 * Creates new form RsetPeptidefragmentationTablePanel
	 */
	//public RsetPeptideFragmentationTablePanel () {
	//	initComponents();
	//}

	//private void initComponents() {
   	 // all is moved to rsetPeptideFramgentationTable
	//}


	
  public void setData(DPeptideMatch peptideMatch) {

       if (peptideMatch == m_previousPeptideMatch) {
           return;
       }
       m_previousPeptideMatch = peptideMatch;
       
       constructFragmentationTable(peptideMatch);
   //    fragmentationTable = new RsetPeptideFragmentationTable(m_dataBox, this,  peptideMatch);
  //     fragmentationTable.createFragmentationTable();
// 
   }



	private void constructFragmentationTable(DPeptideMatch pm) {

		final String SERIES_NAME = "spectrumData";
		if (pm == null) {
			//dataSet.removeSeries(SERIES_NAME);
			//removeAnnotations();
			if(fragmentationTable != null){
			//	fragmentationTable.fragmentationTablePanel.removeAll();
			//	fragmentationTable = null;
			}
			return;
		}
//
		DMsQuery msQuery = pm.isMsQuerySet() ? pm.getMsQuery() : null;
	        
		//MsQuery msQuery = pm.getTransientData().getIsMsQuerySet() ? pm.getMsQuery() : null;
		if (msQuery == null) {
		//	dataSet.removeSeries(SERIES_NAME);
			if(fragmentationTable != null){
			//	fragmentationTable.fragmentationTablePanel.removeAll();
			//	fragmentationTable = null;
			}
			return;
		}
//
		Spectrum spectrum = msQuery.isSpectrumSet() ? msQuery.getSpectrum() : null;
        
		if (spectrum == null) {
		//	dataSet.removeSeries(SERIES_NAME);
			if(fragmentationTable != null){
			//	fragmentationTable.fragmentationTablePanel.removeAll();
				//fragmentationTable = null;
			}
			return;
		}
		
		//*-*-*-*-*-*-*-*
		
	
		if(fragmentationTable != null){
		//	fragmentationTable.fragmentationTablePanel.removeAll();
		//	fragmentationTable = null;
		}
		
		// TODO: clean up the following lines for the really necessary repainting...
	   fragmentationTable = new RsetPeptideFragmentationTable(m_dataBox,  this, /* m_dataSet,*//* null,*/ pm);
       fragmentationTable.createFragmentationTable();
       for(Component c : this.getComponents() ) {
    	   c.repaint();
       }
      
       this.fragmentationTable.fragPanelContainer.revalidate();
       this.fragmentationTable.fragPanelContainer.repaint();
       this.revalidate();
       this.repaint();
	}


	 @Override
	    public void setDataBox(AbstractDataBox dataBox) {
	        m_dataBox = dataBox;
	    }
	    
	    @Override
	    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
	        return m_dataBox.getRemoveAction(splittedPanel);
	    }

	    @Override
	    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
	        return m_dataBox.getAddAction(splittedPanel);
	    }


	public static byte[] floatsToBytes(float[] floats) {

		// Convert float to a byte buffer
		ByteBuffer byteBuf = ByteBuffer.allocate(4 * floats.length).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < floats.length; i++) {
			byteBuf.putFloat(floats[i]);
		}
		// Convert byte buffer into a byte array
		return byteBuf.array();
	}
}
