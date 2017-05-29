package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.comparedata.CompareDataInterface;
import java.util.HashMap;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.xic.DataBoxXICPTMProteinSite;
import fr.proline.studio.pattern.xic.DataboxChildFeature;
import fr.proline.studio.pattern.xic.DataboxExperimentalDesign;
import fr.proline.studio.pattern.xic.DataboxMapAlignment;
import fr.proline.studio.pattern.xic.DataboxXicPeptideIon;
import fr.proline.studio.pattern.xic.DataboxXicPeptideSet;
import fr.proline.studio.pattern.xic.DataboxXicProteinSet;
import fr.proline.studio.utils.IconManager;
import java.awt.Image;

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

    public static WindowBox getUserDefinedWindowBox(String dataName, String windowName, AbstractDataBox databox, boolean isDecoy, boolean isXIC, char windowType) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = databox;
        boxes[0].setDataName(dataName);

        Image icon = null;
        if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM) {
            icon = IconManager.getImage(isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM);
        } else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET) {
            icon = IconManager.getImage(isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET);
        } else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI) {
            icon = IconManager.getImage(isXIC ? IconManager.IconType.QUANT_XIC : IconManager.IconType.QUANT_SC);
        } else {
            icon = databox.getDefaultIcon();
        }

        WindowBox winBox = new WindowBox(windowName, generatePanel(boxes), boxes[0], icon);

        return winBox;
    }

    public static WindowBox getDetailWindowBox(String dataName, String windowName, AbstractDataBox databox) {
        AbstractDataBox[] boxes = createBoxArray(databox, dataName);

        Image icon = databox.getDefaultIcon();

        WindowBox winBox = new WindowBox(windowName, generatePanel(boxes), boxes[0], icon);

        return winBox;
    }

    private static AbstractDataBox[] createBoxArray(AbstractDataBox databox, String dataName) {

        ResultSet rset = (ResultSet) databox.getData(false, ResultSet.class);

        AbstractDataBox[] boxes;
        if (databox instanceof DataBoxRsetProteinsForPeptideMatch) {

            boolean mergedData = false;
            if (rset != null) {
                ResultSet.Type rsType = rset.getType();
                mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
            }

            boxes = new AbstractDataBox[2];
            boxes[1] = new DataboxRsetPeptidesOfProtein(mergedData);

        } else if (databox instanceof DataBoxRsetPeptideSpectrumValues) {
            boxes = new AbstractDataBox[2];
            boxes[1] = new DataBoxRsetPeptideSpectrum();
            boxes[1].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        } else if (databox instanceof DataBoxRsetPeptideSpectrum) {
            boxes = new AbstractDataBox[4];
            boxes[1] = new DataBoxRsetPeptideSpectrumError();
            boxes[1].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[2] = new DataBoxRsetPeptideFragmentation();
            boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[3] = new DataBoxRsetPeptideSpectrumValues();
            boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);

        } else if (databox instanceof DataBoxAdjacencyMatrixChoice) {

            DataBoxAdjacencyMatrixChoice adjacencyMatrixChoice = ((DataBoxAdjacencyMatrixChoice) databox);
            adjacencyMatrixChoice.setKeepSameset(true);
            adjacencyMatrixChoice.doNotTakeFirstSelection(true);
            adjacencyMatrixChoice.createPanel();  //JPM.WART
            databox = new DataBoxAdjacencyMatrix();
            adjacencyMatrixChoice.addNextDataBox(databox);
            boxes = new AbstractDataBox[7];

            boxes[1] = new DataBoxRsetPeptideSpectrum();
            boxes[2] = new DataBoxRsetPeptideSpectrumError();
            boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[3] = new DataBoxRsetPeptideFragmentation();
            boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[4] = new DataBoxRsetPeptideSpectrumValues();
            boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);

            boxes[5] = new DataBoxRsmProteinAndPeptideSequence();
            boxes[6] = new DataBoxRsmPeptidesOfProtein();
            boxes[6].setLayout(SplittedPanelContainer.PanelLayout.TABBED);

        } else if (databox instanceof DataBoxRsmProteinSetOfPeptides) {
            boxes = new AbstractDataBox[2];
            boxes[1] = new DataBoxRsmProteinsOfProteinSet();
            boxes[2] = new DataBoxRsmPeptidesOfProtein();
        } else if (databox instanceof DataBoxRsmProteinsOfProteinSet) {
            boxes = new AbstractDataBox[7];
            boxes[1] = new DataBoxRsmPeptidesOfProtein();
            boxes[2] = new DataBoxRsmProteinAndPeptideSequence();
            boxes[3] = new DataBoxRsetPeptideSpectrum();
            boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[4] = new DataBoxRsetPeptideSpectrumError();
            boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[5] = new DataBoxRsetPeptideFragmentation();
            boxes[5].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[6] = new DataBoxRsetPeptideSpectrumValues();
            boxes[6].setLayout(SplittedPanelContainer.PanelLayout.TABBED);

        } else if (databox instanceof DataboxRsmPSMOfProteinSet) {
            boxes = new AbstractDataBox[5];
            boxes[1] = new DataBoxRsetPeptideSpectrum();
            boxes[2] = new DataBoxRsetPeptideSpectrumError();
            boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[3] = new DataBoxRsetPeptideFragmentation();
            boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[4] = new DataBoxRsetPeptideSpectrumValues();
            boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        } else if (databox instanceof DataBoxRsmPeptidesOfProtein) {
            boxes = new AbstractDataBox[6];
            boxes[1] = new DataBoxRsmProteinAndPeptideSequence();
            boxes[2] = new DataBoxRsetPeptideSpectrum();
            boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[3] = new DataBoxRsetPeptideSpectrumError();
            boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[4] = new DataBoxRsetPeptideFragmentation();
            boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[5] = new DataBoxRsetPeptideSpectrumValues();
            boxes[5].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        } else {
            boxes = new AbstractDataBox[1];
        }

        boxes[0] = databox;
        boxes[0].setDataName(dataName);

        return boxes;
    }

    public static WindowBox getPeptidesWindowBox(String dataName, boolean isDecoy, boolean isMerged) {
        return getPeptidesForRsetOnlyWindowBox(dataName, isDecoy, isMerged);

    }

    public static WindowBox getPeptidesForRsetOnlyWindowBox(String dataName, boolean isDecoy, boolean isMerged) {
        // AW: search results / PSM set of boxes.
        // 
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[6];
        boxes[0] = new DataBoxRsetPSM(isMerged);
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxRsetPeptideSpectrum();
        boxes[2] = new DataBoxRsetPeptideSpectrumError();
        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[3] = new DataBoxRsetPeptideFragmentation();
        boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[4] = new DataBoxRsetPeptideSpectrumValues();
        boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[5] = new DataBoxRsetProteinsForPeptideMatch();

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
        WindowBox winBox = new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;

    }

    public static WindowBox getMSDiagWindowBox(String dataName, HashMap<String, String> resultMessage) {
        // MSDiag
        // 
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsetMSDiag(resultMessage);
        boxes[0].setDataName(dataName);

        IconManager.IconType iconType = IconManager.IconType.CHART_PIE; // TODO: change icon
        WindowBox winBox = new WindowBox(dataName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;

    }

    public static WindowBox getGraphicsWindowBox(String fullName, AbstractDataBox srcDatabox, boolean locked) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxGraphics(locked);
        srcDatabox.addNextDataBox(boxes[0]);
        IconManager.IconType iconType = IconManager.IconType.CHART;
        WindowBox winBox = new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
        boxes[0].dataChanged();
        return winBox;
    }

    public static WindowBox getGraphicsWindowBox(String fullName, CompareDataInterface srcDataInterface, boolean locked) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxGraphics(locked);
        IconManager.IconType iconType = IconManager.IconType.CHART;
        WindowBox winBox = new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
        boxes[0].setEntryData(srcDataInterface);
        return winBox;
    }

    public static WindowBox getMultiGraphicsWindowBox(String fullName, AbstractDataBox srcDatabox, boolean canChooseColor) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxMultiGraphics(true, canChooseColor);
        srcDatabox.addNextDataBox(boxes[0]);
        IconManager.IconType iconType = IconManager.IconType.CHART;
        WindowBox winBox = new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
        boxes[0].dataChanged();
        return winBox;
    }

    public static WindowBox getProteinMatchesForRsetWindowBox(String dataName, boolean isDecoy, boolean mergedData) {

        // create boxes
        // AW: search results / proteins
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxRsetAllProteinMatch();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataboxRsetPeptidesOfProtein(mergedData);

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
        WindowBox winBox = new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;

    }

    public static WindowBox getPTMProteinSiteWindowBox(String dataName) {

        AbstractDataBox[] boxes = new AbstractDataBox[7];
        boxes[0] = new DataBoxPTMProteinSite();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxRsmPeptidesOfProtein();
        boxes[2] = new DataBoxRsmProteinAndPeptideSequence();
        boxes[3] = new DataBoxRsetPeptideSpectrum();
        boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[4] = new DataBoxRsetPeptideSpectrumError();
        boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[5] = new DataBoxRsetPeptideFragmentation();
        boxes[5].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[6] = new DataBoxRsetPeptideSpectrumValues();
        boxes[6].setLayout(SplittedPanelContainer.PanelLayout.TABBED);

        IconManager.IconType iconType = IconManager.IconType.DATASET_RSM;
        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getPTMProteinSiteWindowBox_V2(String dataName) {

        AbstractDataBox[] boxes = new AbstractDataBox[4];
        boxes[0] = new DataBoxPTMProteinSite_V2();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxPTMSitePeptides();
        boxes[2] = new DataBoxPTMSitePepMatches();
        boxes[3] = new DataboxRsetPSMForMsQuery();
        
        IconManager.IconType iconType = IconManager.IconType.DATASET_RSM;
        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }
    
     public static WindowBox getXICPTMProteinSiteWindowBox(String dataName) {

        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxXICPTMProteinSite();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxXICPTMSitePeptides();
                
        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getRsmPSMWindowBox(String dataName, boolean isDecoy, boolean mergedData) {
        // create boxes
        // AW: All PSM of an Identification Summary or corresponding to a Peptide Instance
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsmPSM(mergedData);
        boxes[0].setDataName(dataName);

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getRsmPeptidesWindowBox(String dataName, boolean isDecoy) {
        // create boxes
        // AW: Identification Summary / Peptide Instances";
        AbstractDataBox[] boxes = new AbstractDataBox[4];
        boxes[0] = new DataBoxRsmPeptideInstances();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxRsmProteinSetOfPeptides();
        boxes[2] = new DataBoxRsmProteinsOfProteinSet();
        boxes[3] = new DataBoxRsmPeptidesOfProtein();
        boxes[3].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
        WindowBox winBox = new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;
    }

    public static WindowBox getProteinSetsWindowBox(String dataName, boolean isDecoy) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[8];
        boxes[0] = new DataBoxRsmAllProteinSet();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxRsmProteinsOfProteinSet();
        boxes[2] = new DataBoxRsmPeptidesOfProtein();
        boxes[3] = new DataBoxRsmProteinAndPeptideSequence();
        boxes[4] = new DataBoxRsetPeptideSpectrum();
        boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[5] = new DataBoxRsetPeptideSpectrumError();
        boxes[5].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[6] = new DataBoxRsetPeptideFragmentation();
        boxes[6].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        boxes[7] = new DataBoxRsetPeptideSpectrumValues();
        boxes[7].setLayout(SplittedPanelContainer.PanelLayout.TABBED);

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;

        WindowBox winBox = new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;
    }

    public static WindowBox getAdjacencyMatrixWindowBox(String dataName, boolean isDecoy) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxAdjacencyMatrixChoice();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxAdjacencyMatrix();

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;

        WindowBox winBox = new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;
    }

    public static WindowBox getAllResultSetWindowBox(String dataName) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsetAll();
        boxes[0].setDataName(dataName);

        WindowBox winBox = new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], null);

        return winBox;

    }

    /**
     * xicMode : if false => SC
     *
     * @param dataName
     * @param fullName
     * @param xicMode
     * @return
     */
    public static WindowBox getXicQuantProteinSetWindowBox(String dataName, String fullName, boolean xicMode) {

        // create boxes
        int nbBoxes = xicMode ? 6 : 3;
        AbstractDataBox[] boxes = new AbstractDataBox[nbBoxes];
        boxes[0] = new DataboxXicProteinSet();
        boxes[0].setDataName(dataName);
        ((DataboxXicProteinSet) boxes[0]).setXICMode(xicMode);
        boxes[1] = new DataboxXicPeptideSet();
        ((DataboxXicPeptideSet) boxes[1]).setXICMode(xicMode);
        boxes[2] = new DataboxMultiGraphics(false, false);
        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
        if (xicMode) {
            boxes[3] = new DataboxXicPeptideIon();
            ((DataboxXicPeptideIon) boxes[3]).setXICMode(xicMode);
            boxes[3].setLayout(SplittedPanelContainer.PanelLayout.VERTICAL);
            boxes[4] = new DataboxChildFeature();
            boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
            boxes[5] = new DataboxMultiGraphics(false, false);
            boxes[5].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        }

        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        if (!xicMode) {
            iconType = IconManager.IconType.QUANT_SC;
        }
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getXicQuantPeptideSetWindowBox(String dataName, String fullName, boolean xicMode) {

        // create boxes
        int nbBoxes = xicMode ? 2 : 1;
        AbstractDataBox[] boxes = new AbstractDataBox[nbBoxes];
        boxes[0] = new DataboxXicPeptideSet();
        boxes[0].setDataName(dataName);
        ((DataboxXicPeptideSet) boxes[0]).setXICMode(xicMode);
        if (xicMode) {
            boxes[1] = new DataboxXicPeptideIon();
            ((DataboxXicPeptideIon) boxes[1]).setXICMode(xicMode);
        }

        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        if (!xicMode) {
            iconType = IconManager.IconType.QUANT_SC;
        }
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getXicQuantPeptideIonWindowBox(String dataName, String fullName, boolean xicMode) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxXicPeptideIon();
        boxes[0].setDataName(dataName);
        ((DataboxXicPeptideIon) boxes[0]).setXICMode(xicMode);

        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        if (!xicMode) {
            iconType = IconManager.IconType.QUANT_SC;
        }
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getMzScopeWindowBox() {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxMzScope();

        WindowBox winBox = new WindowBox("MzScope", generatePanel(boxes), boxes[0], IconManager.getImage(IconManager.IconType.WAVE));

        return winBox;
    }

    public static WindowBox getExperimentalDesignWindowBox(String dataName, String fullName) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxExperimentalDesign();
        boxes[0].setDataName(dataName);
        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getMapAlignmentWindowBox(String dataName, String fullName) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataboxMapAlignment();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataboxMultiGraphics(false, false);
        boxes[1].setLayout(SplittedPanelContainer.PanelLayout.VERTICAL);
        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getMSQueriesWindowBoxForRSM(String dataName, boolean mergedData) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxMSQueriesForRSM();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataboxRSMPSMForMsQuery(mergedData);
        boxes[1].setLayout(SplittedPanelContainer.PanelLayout.VERTICAL);
        IconManager.IconType iconType = IconManager.IconType.DATASET_RSM;
        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getMSQueriesWindowBoxForRset(String dataName, boolean mergedData) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxMSQueriesForRset();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataboxRsetPSMForMsQuery(mergedData);
        boxes[1].setLayout(SplittedPanelContainer.PanelLayout.VERTICAL);
        IconManager.IconType iconType = IconManager.IconType.DATASET_RSET;
        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getTaskListWindowBox() {
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxTaskList();
        boxes[1] = new DataBoxTaskDescription();

        WindowBox winBox = new WindowBox("User Tasks", generatePanel(boxes), boxes[0], null);

        return winBox;
    }

    public static WindowBox getSystemTaskLogWindowBox() {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxSystemTasks();

        WindowBox winBox = new WindowBox("Server Tasks", generatePanel(boxes), boxes[0], null);
        return winBox;
    }

    public static WindowBox getDataAnalyzerWindowBox() {
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataboxDataAnalyzer();
        boxes[1] = new DataBoxDataAnalyzerResults();
        WindowBox winBox = new WindowBox("Data Analyzer", generatePanel(boxes, false), boxes[0], IconManager.getImage(IconManager.IconType.DATA_ANALYZER));
        return winBox;
    }

    public static WindowBox[] getSystemMonitoringWindowBox() {
        WindowBox[] m_windowBoxes = new WindowBox[2];
        m_windowBoxes[0] = WindowBoxFactory.getTaskListWindowBox();
        m_windowBoxes[1] = WindowBoxFactory.getSystemTaskLogWindowBox();

//        WindowBox[] m_windowBoxes = new WindowBox[1];
//        m_windowBoxes[0] = WindowBoxFactory.getTaskListWindowBox();
        return m_windowBoxes;
    }

    public static WindowBox getImageWindowBox(String dataName, Image img) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxImage();

        WindowBox winBox = new WindowBox(dataName + " Image", generatePanel(boxes, false), boxes[0], IconManager.getImage(IconManager.IconType.WAVE));
        winBox.setEntryData(-1, img);

        return winBox;
    }

    public static WindowBox getGenericWindowBox(String dataName, String functionName, IconManager.IconType iconType, boolean removeStripAndSort) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxGeneric(dataName, functionName, removeStripAndSort);

        String windowName = (dataName == null) ? functionName : dataName + " " + functionName;
        WindowBox winBox = new WindowBox(windowName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;
    }

    public static WindowBox getFromBoxesWindowBox(String title, AbstractDataBox[] boxes, boolean isDecoy, boolean isXIC, char windowType) {

        IconManager.IconType iconType = IconManager.IconType.DATASET;
        if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM) {
            iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
        } else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET) {
            iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
        } else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI) {
            iconType = isXIC ? IconManager.IconType.QUANT_XIC : IconManager.IconType.QUANT_SC;
        } else {
            iconType = IconManager.IconType.CHALKBOARD;
        }

        WindowBox winBox = new WindowBox(title, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;
    }

    private static SplittedPanelContainer generatePanel(AbstractDataBox[] boxes) {
        return generatePanel(boxes, true);
    }

    private static SplittedPanelContainer generatePanel(AbstractDataBox[] boxes, boolean includeSaveAndAddButtonsInToolbar) {

        // link boxes together
        int nb = boxes.length - 1;
        for (int i = 0; i < nb; i++) {
            boxes[i].addNextDataBox(boxes[i + 1]);
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
                    tb.addTab(boxes[i].getTypeName(), (JPanel) boxes[i].getPanel());
                    tb.setName(tb.getName() + " / " + boxes[i].getTypeName());
                } else {
                    SplittedPanelContainer.ReactiveTabbedPane tb = new SplittedPanelContainer.ReactiveTabbedPane();
                    tb.setBorder(new EmptyBorder(8, 8, 8, 8));
                    tb.addTab(boxes[i - 1].getTypeName(), panels[--panelIdx]);
                    tb.addTab(boxes[i].getTypeName(), (JPanel) boxes[i].getPanel());
                    tb.setName(boxes[i - 1].getTypeName() + " / " + boxes[i].getTypeName());
                    panels[panelIdx++] = tb;

                }
            }
            prevLayout = layout;
        }

        SplittedPanelContainer splittedPanel = new SplittedPanelContainer(includeSaveAndAddButtonsInToolbar);

        for (int i = 0; i < nbContainerPanels; i++) {
            splittedPanel.registerPanel(panels[i]);
        }
        splittedPanel.createPanel();

        return splittedPanel;
    }

}
