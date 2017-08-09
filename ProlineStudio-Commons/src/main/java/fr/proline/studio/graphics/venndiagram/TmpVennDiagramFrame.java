package fr.proline.studio.graphics.venndiagram;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 *
 * @author JM235353
 */
public class TmpVennDiagramFrame extends JFrame {
    
    public TmpVennDiagramFrame(SetList setList) {
        
        TmpVennDiagramPanel panel = new TmpVennDiagramPanel(setList);
        getContentPane().add(panel, BorderLayout.CENTER);
        
        pack();
    }
}
