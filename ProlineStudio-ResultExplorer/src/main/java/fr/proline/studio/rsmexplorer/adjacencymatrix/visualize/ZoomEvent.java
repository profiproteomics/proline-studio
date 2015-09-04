/*Event to zoom inside the Panel from Global to Detail view
 * */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author shivanishah
 */
public class ZoomEvent implements ActionListener{

    Component c ; 
    JPanel cardPanel ;
    String CURRENTPANEL ;
    navigationData nData;
    String GLOBALPANEL = "Main Panel";
    JButton previousB, nextB;
    JTextField idDisplay;
    
    DrawVisualization m_drawVisualization;
    
    public ZoomEvent(Component cButton, JPanel card, String cId, navigationData d ,
    		JButton previousB, JButton nextB, JTextField idDisplay, DrawVisualization drawVisualization)
    {
          this.CURRENTPANEL = cId ;
          this.c = cButton;
          this.cardPanel = card ;
          this.nData = d ;
          this.previousB = previousB;
          this.nextB = nextB;
          this.idDisplay = idDisplay ;
          
          m_drawVisualization = drawVisualization;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	
    	nData.currentCard = CURRENTPANEL ;
	
    	idDisplay.setVisible(true);
    	JPanel cCard = new MatrixPanel(c, cardPanel, idDisplay, m_drawVisualization);
        cCard.setName(CURRENTPANEL);
        cardPanel.add(cCard, CURRENTPANEL);
        CardLayout cl = (CardLayout)(cardPanel.getLayout());
        cl.show(cardPanel, CURRENTPANEL);
        
        if(nData.NavigationFlag)
        {
        	nData.NavigationFlag = false;
        	nData.next.clear() ;
        	nextB.setEnabled(false);
        }
      
    }
    
    
}
