package fr.proline.studio.pattern;


import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.utils.IconManager;
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
    
    public static WindowBox getUserDefinedWindowBox(String name, AbstractDataBox databox, boolean isDecoy) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = databox;

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
        WindowBox winBox = new WindowBox(name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;
    }
    
    public static WindowBox getPeptidesWindowBox(String name, boolean isDecoy) {
        return getPeptidesForRsetOnlyWindowBox(name, isDecoy);

    }
    
    public static WindowBox getPeptidesForRsetOnlyWindowBox(String name, boolean isDecoy) {
        // AW: search results / PSM set of boxes.
    	// 
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[5];
        boxes[0] = new DataBoxRsetPeptide();
        boxes[1] = new DataBoxRsetPeptideSpectrum();
        boxes[2] = new DataBoxRsetPeptideSpectrumError();
        boxes[2].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[3] = new DataBoxRsetPeptideFragmentation();
        boxes[3].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[4] = new DataBoxRsetProteinsForPeptideMatch();
        
        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
        WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType) );

        return winBox;
        
    }
    
    public static WindowBox getHistogramWindowBox(String name) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxStatisticsFrequencyResponse();
        IconManager.IconType iconType = IconManager.IconType.CHART;
         WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType) );
         return winBox;
    }
    
    public static WindowBox getProteinMatchesForRsetWindowBox(String name, boolean isDecoy) {
        
        // create boxes
    	// AW: search results / proteins
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxRsetAllProteinMatch();
        boxes[1] = new DataboxRsetPeptidesOfProtein();

        
        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
        WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType) );

        return winBox;
        
    }
    
    /**
     * 
     * @param name : title of the created Windows 
     * @param readData : specify if the spectral count to display has to be retrieve from computing (false)
     * or read back operation (true)
     * @return 
     */
    public static WindowBox getRsmWSCWindowBox(String name, boolean readData) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsmWSC(readData);


        WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(IconManager.IconType.QUANT_SC) );

        return winBox;
        
    }
    
    public static WindowBox getRsmPSMWindowBox(String name, boolean isDecoy) {
        // create boxes
    	// AW: All PSM of an Identification Summary or corresponding to a Peptide Instance
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsmPeptide();

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
        return new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType) );
    }  
    
    public static WindowBox getRsmPeptidesWindowBox(String name, boolean isDecoy) {
        // create boxes
    	// AW: Identification Summary / Peptide Instances";
        AbstractDataBox[] boxes = new AbstractDataBox[4];
        boxes[0] = new DataBoxRsmPeptideInstances();
        boxes[1] = new DataBoxRsmProteinSetOfPeptides();
        boxes[2] = new DataBoxRsmProteinsOfProteinSet();
        boxes[3] = new DataBoxRsmPeptidesOfProtein();
        boxes[3].setLayout(AbstractDataBox.DataBoxLayout.HORIZONTAL);

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
        WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        
        return winBox;
    }
    
    public static WindowBox getProteinSetsWindowBox(String name, boolean isDecoy) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[7];
        boxes[0] = new DataBoxRsmAllProteinSet();
        boxes[1] = new DataBoxRsmProteinsOfProteinSet();
        boxes[2] = new DataBoxRsmPeptidesOfProtein();
        boxes[3] = new DataBoxRsmProteinAndPeptideSequence();
        boxes[4] = new DataBoxRsetPeptideSpectrum();
        boxes[4].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[5] = new DataBoxRsetPeptideSpectrumError();
        boxes[5].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[6] = new DataBoxRsetPeptideFragmentation();
        boxes[6].setLayout(AbstractDataBox.DataBoxLayout.TABBED);        
        
        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
        
        WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        
        return winBox; 
    }
    
    public static WindowBox getAllResultSetWindowBox(String name) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsetAll();

        WindowBox winBox = new WindowBox(name, generatePanel(boxes), boxes[0], null);

        return winBox;

    }

    public static WindowBox getTaskListWindowBox() {
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxTaskList();
        boxes[1] = new DataBoxTaskDescription();

        WindowBox winBox = new WindowBox("Tasks Log", generatePanel(boxes), boxes[0], null);

        return winBox;
    }

    private static SplittedPanelContainer generatePanel(AbstractDataBox[] boxes) {

        // link boxes together
        int nb = boxes.length - 1;
        for (int i = 0; i < nb; i++) {
            boxes[i].setNextDataBox(boxes[i + 1]);
        }

        // create panels for each Box
        nb = boxes.length;
        for (int i = 0; i < nb; i++) {
            boxes[i].createPanel();
        }

        // create container panel for TABBED AND HORIZONTAL Boxes
        int nbContainerPanels = 0;
        for (int i = 0; i < nb; i++) {
            if (boxes[i].getLayout() == AbstractDataBox.DataBoxLayout.VERTICAL) {
                nbContainerPanels++;
            }
        }

        JComponent[] panels = new JComponent[nbContainerPanels];
        int panelIdx = 0;
        AbstractDataBox.DataBoxLayout prevLayout = AbstractDataBox.DataBoxLayout.VERTICAL;
        for (int i = 0; i < nb; i++) {
            AbstractDataBox.DataBoxLayout layout = boxes[i].getLayout();
            if (layout == AbstractDataBox.DataBoxLayout.VERTICAL) {
                panels[panelIdx++] = (JPanel) boxes[i].getPanel();
            } else if (layout == AbstractDataBox.DataBoxLayout.HORIZONTAL) {
                JSplitPane sp = new JSplitPane();
                JComponent leftComponent = panels[--panelIdx];
                sp.setLeftComponent(leftComponent);
                JComponent rightComponent = (JComponent) boxes[i].getPanel();
                sp.setRightComponent(rightComponent);
                sp.setName(leftComponent.getName() + " / " + rightComponent.getName());

                sp.setDividerLocation(350); //JPM.TODO
                panels[panelIdx++] = sp;
            } else if (layout == AbstractDataBox.DataBoxLayout.TABBED) {
                if (prevLayout == AbstractDataBox.DataBoxLayout.TABBED) {
                    JTabbedPane tb = (JTabbedPane) panels[panelIdx - 1];
                    tb.addTab(boxes[i].getName(), (JPanel) boxes[i].getPanel());
                    tb.setName(tb.getName() + " / " + boxes[i].getName());
                } else {
                    JTabbedPane tb = new JTabbedPane();
                    tb.setBorder(new EmptyBorder(8, 8, 8, 8));
                    tb.addTab(boxes[i - 1].getName(), panels[--panelIdx]);
                    tb.addTab(boxes[i].getName(), (JPanel) boxes[i].getPanel());
                    tb.setName(boxes[i - 1].getName() + " / " + boxes[i].getName());
                    panels[panelIdx++] = tb;

                }
            }
            prevLayout = layout;
        }


        SplittedPanelContainer splittedPanel = new SplittedPanelContainer();


        for (int i = 0; i < nbContainerPanels; i++) {
            splittedPanel.registerPanel(panels[i]);
        }
        splittedPanel.createPanel();

        return splittedPanel;
    }
    




}
