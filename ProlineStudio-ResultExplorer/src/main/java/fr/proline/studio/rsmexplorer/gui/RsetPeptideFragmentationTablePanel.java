package fr.proline.studio.rsmexplorer.gui;



//import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;





/**
 * Panel used to display a Spectrum of a PeptideMatch
 * 
 * @author AW
 */

public class RsetPeptideFragmentationTablePanel extends HourglassPanel implements DataBoxPanelInterface {

    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AbstractDataBox m_dataBox;

//
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
     }



	private void constructFragmentationTable(DPeptideMatch pm) {

		if (pm == null) {
			if(fragmentationTable != null){
			}
			return;
		}
		DMsQuery msQuery = pm.isMsQuerySet() ? pm.getMsQuery() : null;
	        
		if (msQuery == null) {
			if(fragmentationTable != null){
			}
			return;
		}
//
		/*Spectrum spectrum = msQuery.isSpectrumSet() ? msQuery.getSpectrum() : null;
        
		if (spectrum == null) {
			if(fragmentationTable != null){
			}
			return;
		}*/
		
		//*-*-*-*-*-*-*-*
		
	
		if(fragmentationTable != null){
		}
		
		// TODO: clean up the following lines for the really necessary repainting...
	   fragmentationTable = new RsetPeptideFragmentationTable(m_dataBox,  this, /* m_dataSet,*//* null,*/ pm);
       fragmentationTable.createFragmentationTable();
       for(Component c : this.getComponents() ) {
    	   c.repaint();
       }
      
       this.fragmentationTable.m_fragPanelContainer.revalidate();
       this.fragmentationTable.m_fragPanelContainer.repaint();
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
