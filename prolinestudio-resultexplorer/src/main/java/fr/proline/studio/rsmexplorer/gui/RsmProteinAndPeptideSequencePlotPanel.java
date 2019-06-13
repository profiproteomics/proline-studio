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
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideView;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.utils.DataFormat;
import fr.proline.studio.utils.GlobalValues;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.Range;

/**
 * reference : Vaudel et al. Nature Biotechnol. 2015 Jan;33(1):22–24. Projet
 * PeptideShaker: OverviewPanel.java, ProteinSequencePanel.java
 *
 * @author Karine XUE
 */
public class RsmProteinAndPeptideSequencePlotPanel extends JPanel {

    private static final Logger m_logger = LoggerFactory.getLogger(RsmProteinAndPeptideSequencePlotPanel.class);

    private Color SELECTED_COLOR = Color.blue;
    private Color PEPTIDE_COLOR = new Color(0, 200, 0);//green
    private Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);//transparent

    private BlocToolTipGenerator m_sequenceTipsGenerator;
    private BlocToolTipGenerator m_ptmTipsGenerator;
    HashMap<Integer, ArrayList<DPeptideMatch>> m_AAPeptideMap;
    HashMap<Integer, AminoAcidPtmData> m_AAPtmMap;
    ArrayList<BlocData> m_sequenceBlocList;
    ArrayList<BlocData> m_ptmBlocList;

    private String m_sequence;
    private DPeptideInstance[] m_peptideInstances;

    public RsmProteinAndPeptideSequencePlotPanel() {
        super();
        this.setLayout(new BorderLayout());
        initComponent();
    }

    private void initComponent() {
        this.setBorder(BorderFactory.createTitledBorder("Protein Sequence Coverage"));
    }

    public void setData(String sequence, DPeptideInstance selectedPeptide, DPeptideInstance[] peptideInstances) {
        boolean isNew = true;
        if (m_sequence != null && m_sequence.equals(sequence)) {
            if (m_peptideInstances.equals(peptideInstances)) {
                isNew = false;
            }
        }
        int proteinLength = sequence.length();
        //m_logger.debug("length: {} Amino Acid", proteinLength);
        if (isNew) {//if is not new, don't need to updata map
            createAADataMap(proteinLength, peptideInstances);
            createPtmBloc(proteinLength);
        }
        createSequenceBloc(proteinLength, selectedPeptide);

        m_sequence = sequence;
        m_peptideInstances = peptideInstances;

        ChartPanel seqPlot = getSequencePlot(m_sequenceBlocList, m_sequenceTipsGenerator, true, true);
        ChartPanel ptmPlot = getSequencePlot(m_ptmBlocList, m_ptmTipsGenerator, false, false);

        ptmPlot.setPreferredSize(
                new java.awt.Dimension(this.getWidth() - 40, 10));
        seqPlot.setPreferredSize(
                new java.awt.Dimension(this.getWidth() - 40, 20));
        seqPlot.getChart()
                .addChangeListener(new ChartChangeListener() {
                    @Override
                    public void chartChanged(ChartChangeEvent cce
                    ) {
                        if (ptmPlot != null) {
                            Range range = ((CategoryPlot) seqPlot.getChart().getPlot()).getRangeAxis().getRange();
                            ((CategoryPlot) ptmPlot.getChart().getPlot()).getRangeAxis().setRange(range);
                            ptmPlot.revalidate();
                            ptmPlot.repaint();
                        }
                    }
                }
                );

        String title = "Protein Sequence Coverage, " + proteinLength + " amino acid";

        ((TitledBorder) getBorder()).setTitle(title);

        this.removeAll();

        this.add(ptmPlot, BorderLayout.NORTH);

        this.add(seqPlot, BorderLayout.CENTER);

        this.repaint();
    }

    private void createSequenceBloc(int nbAminoAcid, DPeptideInstance selectedPeptide) {
        //long beginTime = System.currentTimeMillis();
        int selectStart = -1, selectStop = -1;
        if (selectedPeptide != null) {
            DPeptideMatch selectBestPeptideMatch = selectedPeptide.getBestPeptideMatch();
            selectStart = selectBestPeptideMatch.getSequenceMatch().getId().getStart();
            selectStop = selectBestPeptideMatch.getSequenceMatch().getId().getStop();
        }
        m_sequenceTipsGenerator = new BlocToolTipGenerator();
        //m_logger.debug("Map {}", AAMap);
        m_sequenceBlocList = new ArrayList();
        int newIndex;
        ArrayList<DPeptideMatch> newPepList, previousPepList = new ArrayList();
        int seqBlocIndex = 0;
        BlocData seqBlocData = null;

        for (int i = 1; i <= nbAminoAcid; i++) {//index from 1
            newIndex = i;
            newPepList = m_AAPeptideMap.get(newIndex);

            if (newPepList != null) {

                //1. create peptide bloc from peptide info
                if (newPepList.equals(previousPepList)) {
                    seqBlocData.addLength();
                } else {
                    if (seqBlocData != null) {
                        //perhaps this bloc is non covrage bloc
                        if (m_sequenceTipsGenerator.isNullTips(seqBlocData.getIndex())) {
                            int start = seqBlocData.getStartPosition();
                            int stop = seqBlocData.getStartPosition() + seqBlocData._length - 1;
                            seqBlocData.setColor(TRANSPARENT_COLOR);
                            m_sequenceTipsGenerator.addTooltips(seqBlocData.getIndex(), "(" + start + " - " + stop + ") no covered ");
                        }
                        m_sequenceBlocList.add(seqBlocData);//one bloc terminate
                    }
                    seqBlocData = new BlocData(newIndex, seqBlocIndex++);//new bloc begin.
                    if (newIndex >= selectStart && newIndex <= selectStop) {
                        seqBlocData.setColor(SELECTED_COLOR);
                    }
                    float maxScore = 0;
                    for (DPeptideMatch pep : newPepList) {

                        int start = pep.getSequenceMatch().getId().getStart();
                        int stop = pep.getSequenceMatch().getId().getStop();
                        //String sequence = pep.getPeptide().getSequence();
                        String sequence = PeptideRenderer.constructPeptideDisplay(pep.getPeptide())
                                .replaceAll(GlobalValues.HTML_TAG_BEGIN, "")
                                .replaceAll(GlobalValues.HTML_TAG_END, "");
                        String score = DataFormat.format(pep.getScore(),2);
                        String tips = String.format("%d -%s- %d, score: %s", start, sequence, stop, score);
                        if (sequence.contains(GlobalValues.HTML_TAG_SPAN_END)) {
                            tips = GlobalValues.HTML_TAG_BEGIN + "<body>" + tips + "</body>" + GlobalValues.HTML_TAG_END;
                        }
                        m_sequenceTipsGenerator.addTooltips(seqBlocData.getIndex(), tips);

                        maxScore = Math.max(maxScore, pep.getScore());//to transfer to Color
                    }
                    seqBlocData.setScore(maxScore);
                }
            } else {//aminoAcideData==null

                if (previousPepList != newPepList) {
                    if (seqBlocData != null) {
                        m_sequenceBlocList.add(seqBlocData);
                    }
                    seqBlocData = new BlocData(newIndex, seqBlocIndex++);
                } else {
                    seqBlocData.addLength(); //bloc peptideList.size == 0,peptide withoutColor
                    if (newIndex == nbAminoAcid) {//last bloc
                        int start = seqBlocData.getStartPosition();
                        int stop = seqBlocData.getStartPosition() + seqBlocData._length - 1;
                        m_sequenceTipsGenerator.addTooltips(seqBlocData.getIndex(), "(" + start + " - " + stop + ") no covered ");
                        seqBlocData.setColor(TRANSPARENT_COLOR);
                        m_sequenceBlocList.add(seqBlocData);
                    }

                }
            }
            previousPepList = newPepList;
        }
//        m_logger.debug("sequence bloc result: ");
//        showSeqBloc(m_sequenceBlocList, m_sequenceTipsGenerator);
//        m_logger.debug("createSequence Bloc execution time: {} ms", (System.currentTimeMillis() - beginTime));
    }

    private void createPtmBloc(int nbAminoAcid) {
        //long beginTime = System.currentTimeMillis();
        Stream<BlocData> ptmStream = m_AAPtmMap.values().stream().map(bloc -> bloc.getPtmBlocList()).flatMap(Collection::stream);
        List<BlocData> sortedPtm = ptmStream.sorted(Comparator.comparing(BlocData::getStartPosition)).collect(Collectors.toList());
        //m_logger.debug(" sorted ptm {}", sortedPtm);
        m_ptmBlocList = new ArrayList();

        int newIndex = 1, previousIndex = 1;//1er positoin index = 1;
        int ptmBlocIndex = 0;
        m_ptmTipsGenerator = new BlocToolTipGenerator();
        BlocData ptmBloc = new BlocData(newIndex, ptmBlocIndex++);//first one
        for (BlocData onePtm : sortedPtm) {
            newIndex = onePtm.getStartPosition();
            if (newIndex != previousIndex) {
                if (!ptmBloc.isCovered()) {//first one is empty
                    ptmBloc.setLength(newIndex - previousIndex);//empty bloc
                    ptmBloc.setColor(TRANSPARENT_COLOR);
                } else if (ptmBloc.isCovered() && (newIndex - previousIndex > 1)) { //
                    m_ptmBlocList.add(ptmBloc);//save previous bloc
                    ptmBloc = new BlocData(previousIndex + 1, ptmBlocIndex++);//empty bloc       
                    ptmBloc.setLength(newIndex - previousIndex - 1);
                    ptmBloc.setColor(TRANSPARENT_COLOR);
                }
                m_ptmBlocList.add(ptmBloc);//save previous bloc
                ptmBloc = new BlocData(newIndex, ptmBlocIndex++);//new length=1 bloc
                m_ptmTipsGenerator.addPtmTooltips(ptmBloc.getIndex(), onePtm.getTooltips());
                ptmBloc.setColor(onePtm.getColor());
                ptmBloc.setCovered();
            } else {//newIndex = previousIndex 1. first one is ptm, 2. mutliple ptm at a AA
                if (!ptmBloc.isCovered()) {//first is ptm
                    ptmBloc.setCovered();
                }
                m_ptmTipsGenerator.addPtmTooltips(ptmBloc.getIndex(), onePtm.getTooltips());
                ptmBloc.setColor(onePtm.getColor());
            }
            previousIndex = newIndex;
        }
        m_ptmBlocList.add(ptmBloc);
        if (previousIndex != nbAminoAcid) {//last null bloc
            ptmBloc = new BlocData(previousIndex + 1, ptmBlocIndex++);//empty bloc       
            ptmBloc.setLength(nbAminoAcid - previousIndex - 1);
            ptmBloc.setColor(TRANSPARENT_COLOR);
            m_ptmBlocList.add(ptmBloc);
        }
//        m_logger.debug("ptm bloc result: ");
//        showSeqBloc(m_ptmBlocList, m_ptmTipsGenerator);
//        m_logger.debug("createPtmBloc execution time: {} ms", (System.currentTimeMillis() - beginTime));
    }

    private void showSeqBloc(ArrayList<BlocData> sequenceBlocList, BlocToolTipGenerator tipsGenerator) {
        for (BlocData d : sequenceBlocList) {
            if (d == null) {
                continue;
            }
            m_logger.debug("{},{}", d, tipsGenerator.getTipsAt(d.getIndex()));
        }

    }

    private void createAADataMap(int nbAminoAcid, DPeptideInstance[] peptideInstances) {
        //long beginTime = System.currentTimeMillis();
        m_AAPeptideMap = new HashMap();
        m_AAPtmMap = new HashMap();
        ArrayList<DPeptideMatch> pepMatchList;
        AminoAcidPtmData ptmOnAA;
        for (DPeptideInstance pep : peptideInstances) {
            int start = 0, stop = 0;
            try {
                //create peptide on protein sequence couvrage
                DPeptideMatch bestPeptideMatch = pep.getBestPeptideMatch();
                if (bestPeptideMatch != null) {
                    start = bestPeptideMatch.getSequenceMatch().getId().getStart();
                    stop = bestPeptideMatch.getSequenceMatch().getId().getStop();
                    for (int i = start; i <= stop; i++) {
                        pepMatchList = m_AAPeptideMap.get(i);
                        pepMatchList = (pepMatchList == null ? new ArrayList() : pepMatchList);
                        pepMatchList.add(bestPeptideMatch);
                        m_AAPeptideMap.put(i, pepMatchList);
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
                    String tooltips = ptmTypeInfo.toReadablePtmString(position);
                    //m_logger.debug("ptm{}", tooltips);
                    ptmOnAA = m_AAPtmMap.get(position);
                    ptmOnAA = (ptmOnAA == null ? new AminoAcidPtmData() : ptmOnAA);

                    Color c = ViewSetting.getColor(ptmType);
                    ptmOnAA.addPtmBloc(position, c, tooltips);
                    m_AAPtmMap.put(position, ptmOnAA);
                }
                //need to know ptm caractor, type , position
            } catch (NullPointerException nullE) {
                //has not peptide information, skip                
            }
        }
        //m_logger.debug("createAADataMap execution time: {} ms", (System.currentTimeMillis() - beginTime));
    }

    public ChartPanel getSequencePlot(ArrayList<BlocData> bloc, CategoryToolTipGenerator tipsGenerator,
            boolean addReferenceLine, boolean allowZooming) {
        Color backgroundColor = Color.white;
        DefaultCategoryDataset barChartDataset = new DefaultCategoryDataset();
        StackedBarRenderer renderer = new StackedBarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);

        // add the data
        for (int i = 0; i < bloc.size(); i++) {
            barChartDataset.addValue(Integer.valueOf(bloc.get(i).getLength()), Integer.valueOf(bloc.get(i).getIndex()), Integer.valueOf(0)); //data, length pep, index start, 0

            renderer.setSeriesPaint(i, bloc.get(i).getColor()); //@todo according score color
            renderer.setSeriesToolTipGenerator(i, tipsGenerator); //tooltips
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
            double referenceLineWidth = 0.03;
            Color referenceLineColor = Color.BLACK;
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

    private class AminoAcidPtmData {

        private ArrayList<BlocData> _ptmBlocList;

        AminoAcidPtmData() {
            _ptmBlocList = new ArrayList();
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

        public ArrayList<BlocData> getPtmBlocList() {
            return _ptmBlocList;
        }

    }

    private class BlocData {

        private int _length;
        private int _startPosition;
        private Color _color;
        private float _score;
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

        public ArrayList<String> getTooltips() {
            return _tooltips;
        }

        public int getIndex() {
            return _blocIndex;
        }

        public Color getColor() {
            if (this._color == null) {
                if (this._score != 0f) {//for peptide sequence
                    return PeptideView.getColorWithProbability(PEPTIDE_COLOR, (float) Math.min((Math.max(_score, 15f)) / 100.0, 1.0));
                }
            } else {
                return _color;
            }
            return TRANSPARENT_COLOR;
        }

        public void setColor(Color color) {
            this._color = color;
        }

        public void setScore(float newScore) {
            this._score = Math.max(_score, newScore);
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
