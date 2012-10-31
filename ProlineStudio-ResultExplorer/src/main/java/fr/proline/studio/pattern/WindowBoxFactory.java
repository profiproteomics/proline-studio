/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class WindowBoxFactory {
    
    
    public static WindowBox getProteinSetsWindowBox() {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[4];
        boxes[0] = new DataBoxRsmProteinSet();
        boxes[1] = new DataBoxRsmProteinsOfProteinSet();
        boxes[2] = new DataboxRsmPeptidesOfProtein();
        boxes[3] = new DataboxRsmProteinAndPeptideSequence();
        
        return new WindowBox( "Protein Groups", generatePanel(boxes), boxes[0] );
    }
    private static SplittedPanelContainer generatePanel(AbstractDataBox[] boxes) {
        
        // link boxes together
        int nb = boxes.length-1;
        for (int i=0;i<nb;i++) {
            boxes[i].setNextDataBox(boxes[i+1]);
        }
        
        // create panels for each Box
        nb = boxes.length;
        for (int i=0;i<nb;i++) {
            boxes[i].createPanel();
        }
        
        SplittedPanelContainer splittedPanel = new SplittedPanelContainer();
        for (int i=0;i<nb;i++) {
            JPanel p = (JPanel) boxes[i].getPanel();
            if (p != null) {
                splittedPanel.registerPanel(p);
            }
        }
        splittedPanel.createPanel();
        
        return splittedPanel;
    }
    

}
