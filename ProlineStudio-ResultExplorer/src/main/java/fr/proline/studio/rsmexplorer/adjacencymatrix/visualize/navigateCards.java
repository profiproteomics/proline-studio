package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class navigateCards implements ActionListener {

    String action;
    JPanel cardPanel;
    navigationData nData;
    String GLOBALPANEL = "Main Panel";
    JButton previousB, nextB;
    JTextField idDisplay;

    public navigateCards(String s, JPanel card, navigationData d, JButton previousB, JButton nextB, JTextField idDisplay) {
        this.action = s;
        this.cardPanel = card;
        this.nData = d;
        this.previousB = previousB;
        this.nextB = nextB;
        this.idDisplay = idDisplay;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        if (action == "previous") {

            if (nData.previous.size() == 0) {
                previousB.setEnabled(false);
                if (nData.currentCard != GLOBALPANEL) {
                    nData.next.push(nData.currentCard);
                }
                nData.currentCard = GLOBALPANEL;
                cl.show(cardPanel, GLOBALPANEL);
                //System.out.println("A");
            } else {
                idDisplay.setVisible(true);
				//System.out.println("B");
                //System.out.println("previous size before: "+nData.previous);
                if (nData.currentCard != GLOBALPANEL) {
                    nData.next.push(nData.currentCard);
                    nextB.setEnabled(true);
                }

                String S = nData.previous.pop();
                if (nData.previous.isEmpty()) {
                    previousB.setEnabled(false);
                }

                //System.out.println("previous size after: "+nData.previous);
                cl.show(cardPanel, S);
                nData.currentCard = S;
                nData.NavigationFlag = true;
                //System.out.println(S);
            }
        } else if (action == "next") {
            if (nData.next.size() == 0) {
                nextB.setEnabled(false);
                if (nData.currentCard != GLOBALPANEL) {
                    nData.previous.push(nData.currentCard);
                }
                nData.currentCard = GLOBALPANEL;

                //		System.out.println("C");
                cl.show(cardPanel, GLOBALPANEL);
            } else {

                idDisplay.setVisible(true);
                if (nData.currentCard != GLOBALPANEL) {
                    nData.previous.push(nData.currentCard);
                    previousB.setEnabled(true);
                }
			//	System.out.println("D");

                String S = nData.next.pop();
                if (nData.next.isEmpty()) {
                    nextB.setEnabled(false);
                }

                cl.show(cardPanel, S);
                nData.currentCard = S;
            }
        } else {
            System.out.println("E");
        }

    }

}
