package fr.proline.studio.graphics.venndiagram;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 *
 * @author JM235353
 */
public class TmpVennDiagramFrame extends JFrame {
    
    public TmpVennDiagramFrame(ArrayList<Set> setArrayList) {
        
        TmpVennDiagramPanel panel = new TmpVennDiagramPanel(setArrayList);
        getContentPane().add(panel, BorderLayout.CENTER);
        
        pack();
    }
}
