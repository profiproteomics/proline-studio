/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSet;
import java.util.HashMap;

import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.xic.*;
import fr.proline.studio.rsmexplorer.DataBoxViewerManager;
import fr.proline.studio.utils.IconManager;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class WindowBoxFactory {

    //VDS TODO : use QuantMethodInfo instead odf isXic
    public static WindowBox getUserDefinedWindowBox(String dataName, String windowName, AbstractDataBox databox, boolean isDecoy, boolean isXIC, char windowType) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = databox;
        boxes[0].setDataName(dataName);

        Image icon;
        switch (windowType) {
            case WindowSavedManager.SAVE_WINDOW_FOR_RSM:
                icon = IconManager.getImage(isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM);
                break;
            case WindowSavedManager.SAVE_WINDOW_FOR_RSET:
                icon = IconManager.getImage(isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET);
                break;
            case WindowSavedManager.SAVE_WINDOW_FOR_QUANTI:
                icon = IconManager.getImage(isXIC ? IconManager.IconType.QUANT_XIC : IconManager.IconType.QUANT_SC);
                break;
            default:
                icon = databox.getDefaultIcon();
                break;
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

        ResultSet rset = (ResultSet) databox.getData( ResultSet.class);

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
            boxes = new AbstractDataBox[3];
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

    public static WindowBox getGraphicsWindowBox(String fullName, ExtendedTableModelInterface srcDataInterface, boolean locked) {
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

//    public static WindowBox getPTMSitesWindowBoxV1(String dataName) {
//
//        AbstractDataBox[] boxes = new AbstractDataBox[5];
//        boxes[0] = new DataBoxPTMSiteProtein();
//        boxes[0].setDataName(dataName);
//        ((DataBoxPTMSiteProtein) boxes[0]).setXicResult(false);
//        boxes[1] = new DataBoxPTMSitePeptidesGraphic();
//        boxes[2] = new DataBoxPTMSitePeptides();
//        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
//        boxes[3] = new DataBoxPTMSitePepMatches();
//        boxes[4] = new DataboxRsetPSMForMsQuery();
//
//        IconManager.IconType iconType = IconManager.IconType.DATASET_RSM;
//        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
//    }
//
//    public static WindowBox getPTMSitesWindowBoxV2(String dataName) {
//
//        AbstractDataBox[] boxes = new AbstractDataBox[5];
//        boxes[0] = new DataBoxPTMSiteProtein(true);
//        boxes[0].setDataName(dataName);
//        ((DataBoxPTMSiteProtein) boxes[0]).setXicResult(false);
//        boxes[1] = new DataBoxPTMPeptidesGraphic();
//        ((DataBoxPTMPeptidesGraphic)boxes[1]).setIsClusterData(false);
//        boxes[2] = new DataBoxPTMSitePeptides();
//        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
//        boxes[3] = new DataBoxPTMSitePepMatches();
//        boxes[4] = new DataboxRsetPSMForMsQuery();
//
//        IconManager.IconType iconType = IconManager.IconType.DATASET_RSM;
//        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
//    }

    /**
     * PTM cluster databox
     * @param dataName
     * @return 
     */
    public static WindowBox getPTMDataWindowBox(String dataName, boolean viewSites, boolean isAnnotated, boolean unsaved) {
        AbstractDataBox[] boxes = new AbstractDataBox[4];
        boxes[0] = new DataBoxPTMClusters(viewSites, isAnnotated);
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxPTMPeptidesGraphic();
        boxes[2] = new DataBoxPTMPeptides(false, false);
        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        boxes[3] = new DataBoxPTMPeptides(false,true);
        boxes[3].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
      //  boxes[4] = new DataboxRsetPSMForMsQuery();
        IconManager.IconType iconType = IconManager.IconType.DATASET_RSM;
        String title = boxes[0].getFullName();
        if(unsaved && !title.endsWith(DataBoxViewerManager.MODIFIED_TITLE_SUFFIX))
            title = title + " " + DataBoxViewerManager.MODIFIED_TITLE_SUFFIX;
        return new WindowBox(title, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
        
    }
    
    /**
     * XIC PTM cluster databox
     * @param dataName
     * @return 
     */
    public static WindowBox getXicPTMDataWindowBox(String dataName, boolean viewSites, boolean isAnnotated , boolean unsaved) {
        AbstractDataBox[] boxes = new AbstractDataBox[7];
        boxes[0] = new DataBoxPTMClusters(viewSites, isAnnotated);
        boxes[0].setDataName(dataName);
        ((DataBoxPTMClusters) boxes[0]).setQuantitationMethodInfo(DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION);
        boxes[1] = new DataBoxPTMPeptides(true, false);
        boxes[2] = new DataBoxPTMPeptides(false,true);
        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.TABBED);        
        boxes[3] = new DataBoxPTMPeptidesGraphic();
        boxes[4] = new DataboxMultiGraphics(false,false,true);//associate a graphic databox for DataboxXicPeptideSet
        ((DataboxMultiGraphics)boxes[4]).setHideButton(true);
        boxes[4].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);      
        boxes[5] = new DataboxXicPeptideSet(false);//display the selected peptideMatch in each chanel
        boxes[6] = new DataboxMultiGraphics(false,false,true); //associate a graphic databox for DataboxXicPeptideSet
        ((DataboxMultiGraphics)boxes[6]).setHideButton(true);
        boxes[6].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);     

        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        String title = boxes[0].getFullName();
        if(unsaved && !title.endsWith(DataBoxViewerManager.MODIFIED_TITLE_SUFFIX))
            title = title + " " + DataBoxViewerManager.MODIFIED_TITLE_SUFFIX;
        return new WindowBox(title, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }
    
//    public static WindowBox getXicPTMSitesWindowBoxV2(String dataName) {
//
//        AbstractDataBox[] boxes = new AbstractDataBox[6];
//        boxes[0] = new DataBoxPTMSiteProtein(true);
//        boxes[0].setDataName(dataName);
//        ((DataBoxPTMSiteProtein) boxes[0]).setXicResult(true);
//
//        boxes[1] = new DataBoxPTMPeptides();
//        boxes[2] = new DataBoxPTMPeptidesGraphic();
//        ((DataBoxPTMPeptidesGraphic)boxes[2]).setIsClusterData(false);
//        //boxes[2].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
//
//       // boxes[3] = new DataboxXicPeptideSet(true);//display the selected peptide in each chanel
//        //((DataboxXicPeptideSet) boxes[3]).setXICMode(true);
//        boxes[3] = new DataboxMultiGraphics(false,false,true);//associate a graphic databox for DataboxXicPeptideSet
//        ((DataboxMultiGraphics)boxes[3]).setHideButton(true);
//        boxes[3].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
////        boxes[5] = new DataBoxPTMSitePepMatches();
////        boxes[5].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
////
//        boxes[4] = new DataboxXicPeptideSet(false);//display the selected peptideMatch in each chanel
//        boxes[5] = new DataboxMultiGraphics(false,false,true); //associate a graphic databox for DataboxXicPeptideSet
//        ((DataboxMultiGraphics)boxes[5]).setHideButton(true);
//        boxes[5].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
//
//        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
//        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
//    }
//
//
//    public static WindowBox getXicPTMSitesWindowBoxV1(String dataName) {
//
//        AbstractDataBox[] boxes = new AbstractDataBox[8];
//        boxes[0] = new DataBoxPTMSiteProtein();
//        boxes[0].setDataName(dataName);
//        ((DataBoxPTMSiteProtein) boxes[0]).setXicResult(true);
//
//        boxes[1] = new DataBoxPTMSitePeptidesGraphic();
//
//        boxes[2] = new DataBoxPTMSitePeptides();//new DataBoxXicPTMSitePeptides();
//        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
//
//        boxes[3] = new DataboxXicPeptideSet(true);//display the selected peptide in each chanel
//        ((DataboxXicPeptideSet) boxes[3]).setXICMode(true);
//        boxes[4] = new DataboxMultiGraphics(false,false,true);//associate a graphic databox for DataboxXicPeptideSet
//        ((DataboxMultiGraphics)boxes[4]).setHideButton(true);
//        boxes[4].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
//        boxes[5] = new DataBoxPTMSitePepMatches();
//        boxes[5].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
//
//        boxes[6] = new DataboxXicPeptideSet(false);//display the selected peptideMatch in each chanel
//        boxes[7] = new DataboxMultiGraphics(false,false,true); //associate a graphic databox for DataboxXicPeptideSet
//        ((DataboxMultiGraphics)boxes[7]).setHideButton(true);
//        boxes[7].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
//
//        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
//        return new WindowBox(boxes[0].getFullName(), generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
//    }

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
     * @param methodInfo : {@link fr.proline.core.orm.uds.dto.DDatasetType.QuantitationMethodInfo} used for this dataset
     * @return
     */
    public static WindowBox getQuantificationProteinSetWindowBox(String dataName, String fullName, DDatasetType.QuantitationMethodInfo methodInfo, boolean aggregatedQuantiPeptideIon) {

    // create boxes
        int nbBoxes;
        IconManager.IconType iconType;

        switch (methodInfo) {
            case SPECTRAL_COUNTING -> {
                nbBoxes = 3;
                iconType = IconManager.IconType.QUANT_XIC;
            }
            case ISOBARIC_TAGGING ->  {
                if(aggregatedQuantiPeptideIon){
                    nbBoxes=5;
                    iconType = IconManager.IconType.QUANT_AGGREGATION_TMT;
                }else {
                    nbBoxes = 5;
                    iconType = IconManager.IconType.QUANT_TMT;
                }
            }
            case FEATURES_EXTRACTION -> {
                if(aggregatedQuantiPeptideIon){
                    nbBoxes=7;
                    iconType = IconManager.IconType.QUANT_AGGREGATION_XIC;
                }
                else {
                    nbBoxes = 6;
                    iconType = IconManager.IconType.QUANT_XIC;
                }
            }

            default -> {//should not occur !
                nbBoxes = (aggregatedQuantiPeptideIon) ? 7 : 6;
                iconType = IconManager.IconType.QUANT_XIC;
            }
        }

        AbstractDataBox[] boxes = new AbstractDataBox[nbBoxes];
        boxes[0] = new DataboxXicProteinSet();
        boxes[0].setDataName(dataName);
        ((DataboxXicProteinSet) boxes[0]).setQuantitationMethodInfo(methodInfo);
        boxes[1] = new DataboxXicPeptideSet();
        ((DataboxXicPeptideSet) boxes[1]).setQuantitationMethodInfo(methodInfo);
        boxes[2] = new DataboxMultiGraphics(false,true,true);
        ((DataboxMultiGraphics)boxes[2]).setHideButton(true);
        boxes[2].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        if (!methodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING)) {
            boxes[3] = new DataboxXicPeptideIon();
            ((DataboxXicPeptideIon) boxes[3]).setQuantitationMethodInfo(methodInfo);
            boxes[3].setLayout(SplittedPanelContainer.PanelLayout.VERTICAL);

            if (aggregatedQuantiPeptideIon) {
                int  nb = 4;
                if (!methodInfo.equals(DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING)) {
                    boxes[nb] = new DataboxXicParentsPeptideIon();
                    boxes[nb].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
                    nb++;
                    boxes[nb] = new DataboxChildFeature();
                    boxes[nb].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
                    nb++;
                    boxes[nb] = new DataboxMultiGraphics(false, false);
                    boxes[nb].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
                } else {
                    boxes[nb] = new DataboxXicReporterIon();
                }
            } else {
                if (methodInfo.equals(DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING)) {
                    boxes[4] = new DataboxXicReporterIon();
//                    boxes[4].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
                } else{
                    boxes[4] = new DataboxChildFeature();
                    boxes[4].setLayout(SplittedPanelContainer.PanelLayout.TABBED);
                    boxes[5] = new DataboxMultiGraphics(false, false);
                    boxes[5].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);
                }
            }
        }

        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getQuantificationPeptideSetWindowBox(String dataName, String fullName, DDatasetType.QuantitationMethodInfo methodInfo) {
        int nbBoxes = 0;
        IconManager.IconType iconType;
        switch (methodInfo) {
            case SPECTRAL_COUNTING -> {
                nbBoxes = 1;
                iconType = IconManager.IconType.QUANT_SC;
            }
            case FEATURES_EXTRACTION, RESIDUE_LABELING -> {
                nbBoxes = 2;
                iconType = IconManager.IconType.QUANT_XIC;
            }
            case ISOBARIC_TAGGING -> {
                nbBoxes = 2;
                iconType = IconManager.IconType.QUANT_TMT;
            }
            default ->  throw new RuntimeException("Unsupported Quant method "+methodInfo); //should  not occur
        }
        AbstractDataBox[] boxes = new AbstractDataBox[nbBoxes];
        boxes[0] = new DataboxXicPeptideSet();
        boxes[0].setDataName(dataName);
        ((DataboxXicPeptideSet) boxes[0]).setQuantitationMethodInfo(methodInfo);;
        if (nbBoxes>1) {
            boxes[1] = new DataboxXicPeptideIon();
            ((DataboxXicPeptideIon) boxes[1]).setQuantitationMethodInfo(methodInfo);
        }

        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getQuantificationReporterIonWindowBox(String dataName, String fullName, DDatasetType.QuantitationMethodInfo methodInfo) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxXicReporterIon();
        boxes[0].setDataName(dataName);
        ((DataboxXicReporterIon) boxes[0]).setQuantitationMethodInfo(methodInfo);

        IconManager.IconType iconType;
        switch (methodInfo) {
            case SPECTRAL_COUNTING -> iconType = IconManager.IconType.QUANT_SC;
            case FEATURES_EXTRACTION -> iconType = IconManager.IconType.QUANT_XIC;
            case ISOBARIC_TAGGING -> iconType = IconManager.IconType.QUANT_TMT;
            case RESIDUE_LABELING -> iconType = IconManager.IconType.QUANT_XIC;
            default ->  throw new RuntimeException("Unsupported Quant method "+methodInfo);//should  not occur
        }
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getQuantificationPeptideIonWindowBox(String dataName, String fullName, DDatasetType.QuantitationMethodInfo methodInfo) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxXicPeptideIon();
        boxes[0].setDataName(dataName);
        ((DataboxXicPeptideIon) boxes[0]).setQuantitationMethodInfo(methodInfo);

        IconManager.IconType iconType;
        switch (methodInfo) {
            case SPECTRAL_COUNTING -> iconType = IconManager.IconType.QUANT_SC;
            case FEATURES_EXTRACTION -> iconType = IconManager.IconType.QUANT_XIC;
            case ISOBARIC_TAGGING -> iconType = IconManager.IconType.QUANT_TMT;
            case RESIDUE_LABELING -> iconType = IconManager.IconType.QUANT_XIC;
            default ->  throw new RuntimeException("Unsupported Quant method "+methodInfo);//should  not occur
        }
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }

    public static WindowBox getMzScopeWindowBox() {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxMzScope();

        WindowBox winBox = new WindowBox("MzScope", generatePanel(boxes), boxes[0], IconManager.getImage(IconManager.IconType.WAVE));

        return winBox;
    }

    public static WindowBox getExperimentalDesignWindowBox(String dataName, String fullName, DDatasetType.QuantitationMethodInfo methodInfo) {

        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataboxExperimentalDesign();
        boxes[0].setDataName(dataName);
        ((DataboxExperimentalDesign) boxes[0]).setQuantitationMethodInfo(methodInfo);
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

    public static WindowBox getMapMozCalibrationWindowBox(String dataName, String fullName) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataboxMapAlignment(true);
        boxes[0].setDataName(dataName);
        boxes[1] = new DataboxMultiGraphics(false, false);
        boxes[1].setLayout(SplittedPanelContainer.PanelLayout.VERTICAL);
        IconManager.IconType iconType = IconManager.IconType.QUANT_XIC;
        return new WindowBox(fullName, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }


    public static WindowBox getMSQueriesWindowBoxForRsm(String dataName, boolean mergedData) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxMSQueriesForRSM();
        boxes[0].setDataName(dataName);
        boxes[1] = new DataBoxRsmPSMForMsQuery(mergedData);
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
        WindowBox[] m_windowBoxes = new WindowBox[2]; //2
        m_windowBoxes[0] = WindowBoxFactory.getTaskListWindowBox(); //JPM.DOCK.TEST
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

        IconManager.IconType iconType;
        switch (windowType) {
            case WindowSavedManager.SAVE_WINDOW_FOR_RSM:
                iconType = isDecoy ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSM;
                break;
            case WindowSavedManager.SAVE_WINDOW_FOR_RSET:
                iconType = isDecoy ? IconManager.IconType.DATASET_RSET_DECOY : IconManager.IconType.DATASET_RSET;
                break;
            case WindowSavedManager.SAVE_WINDOW_FOR_QUANTI:
                iconType = isXIC ? IconManager.IconType.QUANT_XIC : IconManager.IconType.QUANT_SC;
                break;
            default:
                iconType = IconManager.IconType.CHALKBOARD;
                break;
        }

        WindowBox winBox = new WindowBox(title, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));

        return winBox;
    }

    private static SplittedPanelContainer generatePanel(AbstractDataBox[] boxes) {
        return generatePanel(boxes, true);
    }

    /**
     * for each dataBox, add it's nextDataBox<br>
     * from seconde dataBox, for each dataBox, if it's layout is vertical
     * layout, add 1 nbContainerPanels<br>
     *
     * @param boxes
     * @param includeSaveAndAddButtonsInToolbar
     * @return
     */
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
