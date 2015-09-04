/* Event to Zoom from details of Component to Global view
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
public class GlobalViewEvent implements ActionListener {

    JPanel cardPanel ;
    String GLOBALPANEL = "Main Panel";
    navigationData nData;
    JButton previousB, nextB;
    JTextField idDisplay;
    
    public GlobalViewEvent(JPanel cards, navigationData data, JButton previousB, JButton nextB, JTextField idDisplay)
    {
        this.cardPanel = cards ;
        nData = data ;
        this.idDisplay = idDisplay ;
        this.previousB = previousB;
        this.nextB = nextB;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	
    	//System.out.println("GlobalButton");
    	idDisplay.setVisible(false);
    	//System.out.println("previous size before: "+nData.previous.size());
    	if(nData.currentCard != GLOBALPANEL)
    	{
    		nData.previous.push(nData.currentCard) ;
    		previousB.setEnabled(true);
    		//System.out.println("previous size: "+nData.previous);
    	}
    	nData.currentCard = GLOBALPANEL ;
        CardLayout cl = (CardLayout)(cardPanel.getLayout());
        cl.show(cardPanel, GLOBALPANEL);
    
    }
    
}
