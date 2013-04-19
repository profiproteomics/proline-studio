package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author JM235353
 */
public class WindowBoxFactory {
    
    
    public static WindowBox getPeptidesWindowBox(String name) {
        return getPeptidesForRsetOnlyWindowBox(name);
        // create boxes
       /* AbstractDataBox[] boxes = new AbstractDataBox[5];
        boxes[0] = new DataBoxRsetPeptide();
        boxes[1] = new DataBoxRsetPeptideSpectrum();
        boxes[1].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[2] = new DataBoxRsetProteinsForPeptideMatch();
        boxes[3] = new DataBoxProteinSetsCmp();
        boxes[4] = new DataBoxRsetPeptidesOfProteinsCmp();
        boxes[4].setLayout(AbstractDataBox.DataBoxLayout.HORIZONTAL);
        
        return new WindowBox( name, generatePanel(boxes), boxes[0] );*/
    }
    
    public static WindowBox getPeptidesForRsetOnlyWindowBox(String name) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[3];
        boxes[0] = new DataBoxRsetPeptide();
        boxes[1] = new DataBoxRsetPeptideSpectrum();
        boxes[1].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[2] = new DataBoxRsetProteinsForPeptideMatch();
        

        WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0] );
        winBox.resetDefaultSize(); //JPM.WART
        return winBox;
        
    }
    
    public static WindowBox getRsmPSMWindowBox(String name) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsmPeptide();

        return new WindowBox( name, generatePanel(boxes), boxes[0] );
    }  
    
    public static WindowBox getRsmPeptidesWindowBox(String name) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[4];
        boxes[0] = new DataBoxRsmPeptideInstances();
        boxes[1] = new DataBoxRsmProteinSet();
        boxes[2] = new DataBoxRsmProteinsOfProteinSet();
        boxes[3] = new DataBoxRsmPeptidesOfProtein();
        boxes[3].setLayout(AbstractDataBox.DataBoxLayout.HORIZONTAL);

        
        return new WindowBox( name, generatePanel(boxes), boxes[0] );
    }
    
    public static WindowBox getProteinSetsWindowBox(String name) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[5];
        boxes[0] = new DataBoxRsmProteinSet();
        boxes[1] = new DataBoxRsmProteinsOfProteinSet();
        boxes[2] = new DataBoxRsmPeptidesOfProtein();
        boxes[3] = new DataBoxRsmProteinAndPeptideSequence();
        boxes[4] = new DataBoxRsetPeptideSpectrum();
        boxes[4].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        
        return new WindowBox( name, generatePanel(boxes), boxes[0] );
    }
    
    public static WindowBox getAllResultSetWindowBox(String name) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsetAll();

        WindowBox winBox = new WindowBox(name, generatePanel(boxes), boxes[0]);

        return winBox;

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
        
        // create container panel for TABBED AND HORIZONTAL Boxes
        int nbContainerPanels = 0;
        for (int i=0;i<nb;i++) {
            if (boxes[i].getLayout() == AbstractDataBox.DataBoxLayout.VERTICAL) {
                nbContainerPanels++;
            }
        }
        
        JComponent[] panels = new JComponent[nbContainerPanels];
        int panelIdx = 0;
        AbstractDataBox.DataBoxLayout prevLayout = AbstractDataBox.DataBoxLayout.VERTICAL;
        for (int i=0;i<nb;i++) {
            AbstractDataBox.DataBoxLayout layout = boxes[i].getLayout();
            if ( layout == AbstractDataBox.DataBoxLayout.VERTICAL) {
                panels[panelIdx++] = (JPanel) boxes[i].getPanel();
            } else if ( layout == AbstractDataBox.DataBoxLayout.HORIZONTAL) {
                JSplitPane sp = new JSplitPane();
                JComponent leftComponent = panels[--panelIdx];
                sp.setLeftComponent(leftComponent);
                JComponent rightComponent = (JComponent) boxes[i].getPanel();
                sp.setRightComponent(rightComponent);
                sp.setName(leftComponent.getName()+" / "+rightComponent.getName()); 
                
                sp.setDividerLocation(350); //JPM.TODO
                panels[panelIdx++] = sp;
            } else if ( layout == AbstractDataBox.DataBoxLayout.TABBED) {
                if (prevLayout == AbstractDataBox.DataBoxLayout.TABBED) {
                    JTabbedPane tb = (JTabbedPane) panels[panelIdx-1];
                    tb.addTab(boxes[i].getName(),(JPanel) boxes[i].getPanel());
                    tb.setName(tb.getName()+" / "+boxes[i].getName());
                } else {
                    JTabbedPane tb = new JTabbedPane();
                    tb.setBorder(new EmptyBorder(8,8,8,8));
                    tb.addTab(boxes[i-1].getName(), panels[--panelIdx]);
                    tb.addTab(boxes[i].getName(),(JPanel) boxes[i].getPanel());
                    tb.setName(boxes[i-1].getName()+" / "+boxes[i].getName());
                    panels[panelIdx++] = tb;
                    
                }
            }
            prevLayout = layout;
        }
        
        
        
        SplittedPanelContainer splittedPanel = new SplittedPanelContainer();
        for (int i=0;i<nbContainerPanels;i++) {
            splittedPanel.registerPanel(panels[i]);
        }
        splittedPanel.createPanel();
        
        return splittedPanel;
    }
    

}
