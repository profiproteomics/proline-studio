/*
 * @cea 
 * http://www.profiproteomics.fr
 * Create Date: 27 mai 2019 
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * reference : Vaudel et al. Nature Biotechnol. 2015 Jan;33(1):22–24. Projet
 * PeptideShaker: OverviewPanel.java, ProteinSequencePanel.java
 *
 * @author KX257079
 */
public class RsmProteinAndPeptideSequencePlotPanel extends JPanel {

    private static final Logger m_logger = LoggerFactory.getLogger(RsmProteinAndPeptideSequencePlotPanel.class);
    private static Color DEFALUT_COLOR = Color.GREEN;

    /**
     * bloc start position map to BlocData
     */
    private HashMap<Integer, BlocData> m_startBlocMap;
    private BlocToolTipGenerator m_sequenceTipsGenerator;
    private BlocToolTipGenerator m_ptmTipsGenerator;

    public void setData(String sequence, DPeptideInstance selectedPeptide, DPeptideInstance[] peptideInstances) {
        int proteinLength = sequence.length();
        m_logger.debug("length: {} Amino Acid", proteinLength);
        //getBlocFromMap(proteinLength, selectedPeptide, peptideInstances);
        getBlocFromMap(proteinLength, selectedPeptide, peptideInstances);
    }

    private void getBlocFromMap(int nbAminoAcid, DPeptideInstance selectedPeptide, DPeptideInstance[] peptideInstances) {
        HashMap<Integer, AminoAcidData> AAMap = createAADataMap(nbAminoAcid, peptideInstances);
//        for (Integer key : AAMap.keySet()) {
//            AminoAcidData value = AAMap.get(key);
//            ArrayList ptm = value.getPtmBlocList();
//            if (!ptm.isEmpty()) {
//                m_logger.debug("key: {}, ptm : {}", key, ptm);
//            }
//        }
        m_sequenceTipsGenerator = new BlocToolTipGenerator();
        //m_logger.debug("Map {}", AAMap);
        ArrayList<BlocData> sequenceBlocList = new ArrayList();

        AminoAcidData aminoAcideData;
        int newIndex;
        AminoAcidData newAA, previousAA;
        ArrayList<DPeptideMatch> newPepList, previousPepList = new ArrayList();

        int seqBlocIndex = 0;
        BlocData seqBlocData = null;

        ArrayList<BlocData> ptmBlocList = new ArrayList();
        for (int i = 1; i <= nbAminoAcid; i++) {//index from 1
            newIndex = i;
            aminoAcideData = AAMap.get(newIndex);

            if (aminoAcideData != null) {
                if (!aminoAcideData.getPtmBlocList().isEmpty()) {
                    ptmBlocList.addAll(aminoAcideData.getPtmBlocList());
                }
                //1. create peptide bloc from peptide info
                newPepList = aminoAcideData.getPeptideList();
                if (newPepList.equals(previousPepList)) {
                    seqBlocData.addLength();
                } else {
                    if (seqBlocData != null) {
                        //perhaps this bloc is non covrage bloc
                        if (m_sequenceTipsGenerator.isNullTips(seqBlocData._blocIndex)) {
                            int start = seqBlocData._startPosition;
                            int stop = seqBlocData._startPosition + seqBlocData._length - 1;
                            m_sequenceTipsGenerator.addTooltips(seqBlocData._blocIndex, "(" + start + "," + stop + ") non covered ");
                        }
                        sequenceBlocList.add(seqBlocData);//one bloc terminate
                    }
                    seqBlocData = new BlocData(newIndex, seqBlocIndex++);//new bloc begin.
                    float maxScore = 0;
                    for (DPeptideMatch pep : newPepList) {
                        int start = pep.getSequenceMatch().getId().getStart();
                        int stop = pep.getSequenceMatch().getId().getStop();
                        String sequence = pep.getPeptide().getSequence();
                        String tips = "" + start + "-" + sequence + "-" + stop;
                        maxScore = Math.max(maxScore, pep.getScore());//to transfer to Color
                        m_sequenceTipsGenerator.addTooltips(seqBlocData._blocIndex, tips);
                    }
                }
            } else {//aminoAcideData==null
                newPepList = null;
                if (previousPepList != newPepList) {
                    if (seqBlocData != null) {
                        sequenceBlocList.add(seqBlocData);
                    }
                    seqBlocData = new BlocData(newIndex, seqBlocIndex++);
                } else {
                    seqBlocData.addLength(); //bloc peptideList.size == 0,peptide withoutColor
                    if (newIndex == nbAminoAcid) {
                        int start = seqBlocData._startPosition;
                        int stop = seqBlocData._startPosition + seqBlocData._length - 1;
                        m_sequenceTipsGenerator.addTooltips(seqBlocData._blocIndex, "(" + start + "," + stop + ") non covered ");
                        sequenceBlocList.add(seqBlocData);
                    }

                }
            }
            previousPepList = newPepList;
        }
//        m_logger.debug("ptm bloc result: ");
//        showSeqBloc(sequenceBlocList, m_sequenceTipsGenerator);
        getPtmBlocFromMap(nbAminoAcid, ptmBlocList);
    }

    private void showSeqBloc(ArrayList<BlocData> sequenceBlocList, BlocToolTipGenerator tipsGenerator) {
        for (BlocData d : sequenceBlocList) {
            if (d == null) {
                continue;
            }
            m_logger.debug("{},{}", d, tipsGenerator.getTipsAt(d._blocIndex));
        }

    }

    private void getPtmBlocFromMap(int nbAminoAcid, ArrayList<BlocData> ptmBlocList) {
        List<BlocData> sortedPtm = ptmBlocList.stream().sorted(Comparator.comparing(BlocData::getStartPosition)).collect(Collectors.toList());
        //m_logger.debug(" sorted ptm {}", sortedPtm);
        ArrayList<BlocData> resultPtmBlocList = new ArrayList();

        int newIndex = 1, previousIndex = 1;//1er positoin index = 1;
        int ptmBlocIndex = 0;
        m_ptmTipsGenerator = new BlocToolTipGenerator();
        BlocData ptmBloc = new BlocData(newIndex, ptmBlocIndex++);//first one
        for (BlocData onePtm : sortedPtm) {
            newIndex = onePtm.getStartPosition();
            if (newIndex != previousIndex) {
                if (!ptmBloc.isCovered()) {//first one is empty
                    ptmBloc.setLength(newIndex - previousIndex);//empty bloc
                } else if (ptmBloc.isCovered() && (newIndex - previousIndex > 1)) { //
                    resultPtmBlocList.add(ptmBloc);//save previous bloc
                    ptmBloc = new BlocData(previousIndex + 1, ptmBlocIndex++);//empty bloc       
                    ptmBloc.setLength(newIndex - previousIndex - 1);
                }
                resultPtmBlocList.add(ptmBloc);//save previous bloc
                ptmBloc = new BlocData(newIndex, ptmBlocIndex++);//new length=1 bloc
                m_ptmTipsGenerator.addPtmTooltips(ptmBloc._blocIndex, onePtm._tooltips);
                ptmBloc.setCovered();
            } else {//newIndex = previousIndex 1. first one is ptm, 2. mutliple ptm at a AA
                if (!ptmBloc.isCovered()) {//first is ptm
                    ptmBloc.setCovered();
                }
                m_ptmTipsGenerator.addPtmTooltips(ptmBloc._blocIndex, onePtm._tooltips);
            }
            previousIndex = newIndex;
        }
        resultPtmBlocList.add(ptmBloc);
        if (previousIndex != nbAminoAcid) {//last null bloc
            ptmBloc = new BlocData(previousIndex + 1, ptmBlocIndex++);//empty bloc       
            ptmBloc.setLength(nbAminoAcid - previousIndex - 1);
            resultPtmBlocList.add(ptmBloc);
        }
//        m_logger.debug("ptm bloc result: ");
//        showSeqBloc(resultPtmBlocList, m_ptmTipsGenerator);
    }

    private HashMap<Integer, AminoAcidData> createAADataMap(int nbAminoAcid, DPeptideInstance[] peptideInstances) {
        HashMap<Integer, AminoAcidData> AADataMap = new HashMap();
        AminoAcidData aData;
        for (DPeptideInstance pep : peptideInstances) {
            int start = 0, stop = 0;
            try {
                //create peptide on protein sequence couvrage
                DPeptideMatch bestPeptideMatch = pep.getBestPeptideMatch();
                if (bestPeptideMatch != null) {
                    start = bestPeptideMatch.getSequenceMatch().getId().getStart();
                    stop = bestPeptideMatch.getSequenceMatch().getId().getStop();
                    for (int i = start; i <= stop; i++) {
                        aData = AADataMap.get(i);
                        aData = (aData == null ? new AminoAcidData() : aData);
                        aData.addPeptide(bestPeptideMatch);
                        AADataMap.put(i, aData);
                    }
                }
                //create ptm
                Collection<DPeptidePTM> allPtm = bestPeptideMatch.getPeptide().getTransientData().getDPeptidePtmMap().values();
                for (DPeptidePTM ptm : allPtm) {
                    long ptmType = ptm.getIdPtmSpecificity();
                    HashMap<Long, DInfoPTM> map2Debug = DInfoPTM.getInfoPTMMap();
                    String mapOverview = "";
                    for (Long type : map2Debug.keySet()) {
                        DInfoPTM info = map2Debug.get(type);
                        mapOverview += info.getPtmShortName() + String.valueOf(info.getResidueAASpecificity()) + '\n';

                    }
                    //m_logger.debug("all ptm type: {}", mapOverview);
                    DInfoPTM ptmTypeInfo = DInfoPTM.getInfoPTMMap().get(ptmType);
                    int position = (int) ptm.getSeqPosition() + start - 1;//position convert to int
                    String tooltips = ptmTypeInfo.getPtmShortName() + "(" + ptmTypeInfo.getResidueAASpecificity() + position + ")";
                    m_logger.debug("ptm{}", tooltips);
                    aData = AADataMap.get(position);
                    aData = (aData == null ? new AminoAcidData() : aData);
                    aData.addPtm(ptm);
                    Color c = ViewSetting.getColor(ptmType);
                    aData.addPtmBloc(position, c, tooltips);
                    AADataMap.put(position, aData);
                }
                //need to know ptm caractor, type , position
            } catch (NullPointerException nullE) {
                //has not peptide information, skip                
            }
        }
        return AADataMap;
    }

    /**
     * The reference line width.
     */
    private double referenceLineWidth = 0.03;
    /**
     * The reference line color.
     */
    private Color referenceLineColor = Color.BLACK;
    private Color backgroundColor = Color.white;

    public ChartPanel getSequencePlot(ArrayList<Integer> peptide, ArrayList pepToolTips,
            boolean addReferenceLine, boolean allowZooming) {
        DefaultCategoryDataset barChartDataset = new DefaultCategoryDataset();
        StackedBarRenderer renderer = new StackedBarRenderer();
        renderer.setShadowVisible(false);

        // add the data
        for (int i = 0; i < peptide.size(); i++) {
            barChartDataset.addValue(peptide.get(i), Integer.valueOf(i), Integer.valueOf(0)); //data, length pep, index start, 0

            renderer.setSeriesPaint(i, Color.GREEN); //@todo according score color
            renderer.setSeriesToolTipGenerator(i, m_sequenceTipsGenerator); //tooltips
        }

        // create the chart
        JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, barChartDataset, PlotOrientation.HORIZONTAL, false, false, false);

        // fine tune the chart properites
        CategoryPlot plot = chart.getCategoryPlot();

        // remove space before/after the domain axis
        plot.getDomainAxis().setUpperMargin(0);
        plot.getDomainAxis().setLowerMargin(0);

        // remove space before/after the range axis
        plot.getRangeAxis().setUpperMargin(0);
        plot.getRangeAxis().setLowerMargin(0);

        renderer.setRenderAsPercentages(true);
        renderer.setBaseToolTipGenerator(new IntervalCategoryToolTipGenerator());

        // add the dataset to the plot
        plot.setDataset(barChartDataset);

        // hide unwanted chart details
        plot.getRangeAxis().setVisible(false);
        plot.getDomainAxis().setVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setDomainGridlinesVisible(false);

        // add a reference line in the middle of the dataset
        if (addReferenceLine) { //PTM don't need a line in middle
            DefaultCategoryDataset referenceLineDataset = new DefaultCategoryDataset();
            referenceLineDataset.addValue(1.0, "A", "B");
            plot.setDataset(1, referenceLineDataset);
            LayeredBarRenderer referenceLineRenderer = new LayeredBarRenderer();
            referenceLineRenderer.setSeriesBarWidth(0, referenceLineWidth);
            referenceLineRenderer.setSeriesFillPaint(0, referenceLineColor);
            referenceLineRenderer.setSeriesPaint(0, referenceLineColor);
            plot.setRenderer(1, referenceLineRenderer);
        }

        // set up the chart renderer
        plot.setRenderer(0, renderer);

        // hide the outline
        chart.getPlot().setOutlineVisible(false);

        // make sure the background is the same as the panel
        chart.getPlot().setBackgroundPaint(backgroundColor);
        chart.setBackgroundPaint(backgroundColor);

        // create the chart panel
        ChartPanel chartPanel = new ChartPanel(chart);

        if (!allowZooming) {//ptm need not zooming, it's range change according Sequence
            chartPanel.setPopupMenu(null);
            chartPanel.setRangeZoomable(false);
        }

        chartPanel.setBackground(Color.WHITE);

        return chartPanel;

    }

    private class AminoAcidData {

        private ArrayList<DPeptideMatch> _peptideList;
        private ArrayList<DPeptidePTM> _ptmList;
        private ArrayList<BlocData> _ptmBlocList;

        AminoAcidData() {
            _peptideList = new ArrayList();
            _ptmList = new ArrayList();
            _ptmBlocList = new ArrayList();
        }

        void addPeptide(DPeptideMatch pep) {
            this._peptideList.add(pep);
        }

        void addPtm(DPeptidePTM ptm) {
            this._ptmList.add(ptm);
        }

        void addPtmBloc(int ptmPositionInProt, Color color, String message) {
            BlocData bd = new BlocData(ptmPositionInProt, color, message);
            for (BlocData d : this._ptmBlocList) {
                if (d.equals(bd)) {
                    return;
                }
            }
            this._ptmBlocList.add(bd);
        }

        public ArrayList<DPeptideMatch> getPeptideList() {
            return _peptideList;
        }

        public ArrayList<BlocData> getPtmBlocList() {
            return _ptmBlocList;
        }

        @Override
        public String toString() {
            String peptideInfo = "";
            for (DPeptideMatch pep : _peptideList) {
                peptideInfo += "{" + pep.getPeptide().getSequence()
                        + " pos:(" + pep.getSequenceMatch().getId().getStart() + ","
                        + pep.getSequenceMatch().getId().getStop() + ")}";
            }

            return "AminoAcidData{" + "_peptideList=" + peptideInfo + ", _ptmBlocList=" + _ptmBlocList + '}';
        }

    }

    private class BlocData {

        private int _length;
        private int _startPosition;
        private Color _color;
        private ArrayList<String> _tooltips;
        private boolean _isCovered;
        private int _blocIndex;

        public BlocData(int startPosition, int blocIndex) {
            this._blocIndex = blocIndex;
            this._length = 1;
            this._startPosition = startPosition;
            this._color = null;
            this._tooltips = new ArrayList();
            _isCovered = false;
        }

        /**
         * Constructor for ptm
         *
         * @param start
         * @param color
         * @param message
         */
        BlocData(int start, Color color, String message) {
            _length = 1;
            _startPosition = start;
            _color = color;
            _tooltips = new ArrayList<>();
            _tooltips.add(message);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BlocData other = (BlocData) obj;
            if (this._length != other._length) {
                return false;
            }
            if (this._startPosition != other._startPosition) {
                return false;
            }
            if (!Objects.equals(this._color, other._color)) {
                return false;
            }
            if (!Objects.equals(this._tooltips, other._tooltips)) {
                return false;
            }
            return true;
        }

        public int getStartPosition() {
            return this._startPosition;
        }

        public int getLength() {
            return _length;
        }

        void addLength() {
            _length++;
        }

        void setLength(int length) {
            _length = length;
        }

        void addTooltips(String message) {
            this._tooltips.add(message);
        }

        void addTooltips(ArrayList<String> message) {
            this._tooltips.addAll(message);
        }

        boolean isCovered() {
            return _isCovered;
        }

        void setCovered() {
            this._isCovered = true;
        }

        @Override
        public String toString() {
            return "BlocData{" + _blocIndex + ",start=" + _startPosition + ", length=" + _length + ", tooltips=" + _tooltips + '}';
        }
    }

    private class BlocToolTipGenerator implements CategoryToolTipGenerator {

        private HashMap<Integer, ArrayList<String>> _blocIndexPeptideMap;

        public BlocToolTipGenerator(HashMap<Integer, ArrayList<String>> blocIndexPeptideMap) {
            _blocIndexPeptideMap = blocIndexPeptideMap;
        }

        public BlocToolTipGenerator() {
            _blocIndexPeptideMap = new HashMap();
        }

        public boolean isNullTips(int blocIndex) {
            ArrayList<String> tips = this._blocIndexPeptideMap.get(blocIndex);
            return (tips == null);

        }

        public String getTipsAt(Integer blocIndex) {
            ArrayList<String> tips = this._blocIndexPeptideMap.get(blocIndex);
            return tips == null ? "null" : tips.toString();
        }

        public void addTooltips(Integer blocIndex, String message) {
            ArrayList<String> tips = this._blocIndexPeptideMap.get(blocIndex);
            if (tips == null) {
                tips = new ArrayList();
                tips.add(message);
                this._blocIndexPeptideMap.put(blocIndex, tips);
            } else {
                tips.add(message);
            }
        }

        public void addPtmTooltips(Integer blocIndex, ArrayList<String> messageList) {
            ArrayList<String> tips = this._blocIndexPeptideMap.get(blocIndex);
            if (tips == null) {
                tips = messageList;
                this._blocIndexPeptideMap.put(blocIndex, tips);
            } else {
                boolean isSame = false;
                for (String newMsg : messageList) {
                    for (String s : tips) {
                        if (newMsg.equals(s)) {
                            isSame = true;
                        }
                    }
                    if (!isSame) {
                        tips.add(newMsg);
                    }
                }
            }
        }

        @Override// * @param i, index of bloc = peptide != index of peptide position in
        public String generateToolTip(CategoryDataset cd, int i, int i1) {
            ArrayList<String> msgList = this._blocIndexPeptideMap.get(i);
            StringBuilder tooltip = new StringBuilder();
            if (msgList != null) {
                tooltip.append("<html>");
                if (msgList.size() == 1) {
                    tooltip.append(msgList.get(0));
                } else {
                    for (int j = 0; j < msgList.size(); j++) {
                        tooltip.append((j + 1));
                        tooltip.append(": ");
                        tooltip.append(msgList.get(j));
                        tooltip.append("<br>");
                    }
                }
                tooltip.append("</html>");
            }
            if (tooltip.length() == 0) {
                return null;
            } else {
                return tooltip.toString();
            }
        }

    }

 

}
