package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.utils.IconManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[3] = new DataBoxRsetPeptideFragmentation();
        boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
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
        boxes[3].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);

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
        boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[5] = new DataBoxRsetPeptideSpectrumError();
        boxes[5].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[6] = new DataBoxRsetPeptideFragmentation();
        boxes[6].setLayout(SplittedPanelContainer.PanelLayout.TABBED);        
        
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
    
    public static WindowBox getFromBoxesWindowBox(String title, AbstractDataBox[] boxes, boolean isDecoy, boolean isRSM) {

        IconManager.IconType iconType;
        if (isRSM) {
            iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
        } else {
            iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
        }
        
        WindowBox winBox = new WindowBox(title, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

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
            if (boxes[i].getLayout() == SplittedPanelContainer.PanelLayout.VERTICAL) {
                nbContainerPanels++;
            }
        }

        JComponent[] panels = new JComponent[nbContainerPanels];
        int panelIdx = 0;
        SplittedPanelContainer.PanelLayout prevLayout = SplittedPanelContainer.PanelLayout.VERTICAL;
        for (int i = 0; i < nb; i++) {
            SplittedPanelContainer.PanelLayout layout = boxes[i].getLayout();
            if (layout == SplittedPanelContainer.PanelLayout.VERTICAL) {
                panels[panelIdx++] = (JPanel) boxes[i].getPanel();
            } else if (layout == SplittedPanelContainer.PanelLayout.HORIZONTAL) {
                JSplitPane sp = new JSplitPane();
                JComponent leftComponent = panels[--panelIdx];
                sp.setLeftComponent(leftComponent);
                JComponent rightComponent = (JComponent) boxes[i].getPanel();
                sp.setRightComponent(rightComponent);
                sp.setName(leftComponent.getName() + " / " + rightComponent.getName());

                sp.setDividerLocation(350); //JPM.TODO
                panels[panelIdx++] = sp;
            } else if (layout == SplittedPanelContainer.PanelLayout.TABBED) {
                if (prevLayout == SplittedPanelContainer.PanelLayout.TABBED) {
                    JTabbedPane tb = (JTabbedPane) panels[panelIdx - 1];
                    tb.addTab(boxes[i].getName(), (JPanel) boxes[i].getPanel());
                    tb.setName(tb.getName() + " / " + boxes[i].getName());
                } else {
                    SplittedPanelContainer.ReactiveTabbedPane tb = new SplittedPanelContainer.ReactiveTabbedPane();
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
    
    public static String writeBoxes(ArrayList<AbstractDataBox> boxList, ArrayList<SplittedPanelContainer.PanelLayout> layoutList) {
        
        boolean rset = false;
        boolean rsm = false;
        
        AbstractDataBox entryBox = boxList.get(0);
        HashSet<GroupParameter> entryParameterSet = entryBox.getInParameters();
        Iterator<GroupParameter> entryParameterIt = entryParameterSet.iterator();
        while (entryParameterIt.hasNext()) {
            GroupParameter groupParameter = entryParameterIt.next();
            ArrayList<DataParameter> parametersList = groupParameter.getParameterList();
            for (int i=0;i<parametersList.size();i++) {
                DataParameter parameter = parametersList.get(i);
                if (parameter.equalsData(ResultSet.class)) {
                    rset = true;
                } else if (parameter.equalsData(ResultSummary.class)) {
                    rsm = true;
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (rset) {
            sb.append("1#");
        } else if (rsm) {
            sb.append("2#");
        } else {
            sb.append("0#");
        }
        
        
        for (int i=0;i<boxList.size();i++) {
            sb.append(boxList.get(i).getType().intValue());
            sb.append('#');
            sb.append(layoutList.get(i).intValue());
            if (i<boxList.size()-1) {
                sb.append('#');
            }
        }
        return sb.toString();
    }
    
    public static boolean hasResultSetParameter(String dump) {
        return dump.charAt(0)  == '1';
    }
    
    public static boolean hasResultSummaryParameter(String dump) {
        return dump.charAt(0)  == '2';
    }
    
    public static AbstractDataBox[] readBoxes(String dump) {
        String[] values = dump.split("\\#");
        
        int nbBoxes = (values.length-1)/2;
        AbstractDataBox[] boxes = new AbstractDataBox[nbBoxes];
        int boxId = 0;

        for(int i=1;i<values.length;i+=2) {
            AbstractDataBox.DataboxType databoxType = AbstractDataBox.DataboxType.getDataboxType(Integer.parseInt(values[i]));
            SplittedPanelContainer.PanelLayout layout = SplittedPanelContainer.PanelLayout.getLayoutType(Integer.parseInt(values[i+1]));
            
            AbstractDataBox databox = databoxType.getDatabox();
            databox.setLayout(layout);
            
            boxes[boxId++] = databox;
        }
        
        return boxes;
    }


}
