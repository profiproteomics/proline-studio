package fr.proline.studio.rsmexplorer.gui.spectrum;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Abstract panel used to display Spectrum information graphically
 *
 * @author CB205360
 */
public abstract class AbstractPeptideSpectrumPanel extends HourglassPanel implements DataBoxPanelInterface, SplittedPanelContainer.ReactiveTabbedComponent, PlotChangeListener {

  public static Color ABC_SERIE_COLOR = new Color(51, 153, 255);
  public static Color XYZ_SERIE_COLOR = new Color(255, 85, 85);

  private static final int ABC_SERIE_LABEL_YPOS = 0;
  private static final int ABC_SERIE_LABEL_XPOS = 1;
  private static final int XYZ_SERIE_LABEL_YPOS = 2;
  private static final int XYZ_SERIE_LABEL_XPOS = 3;

  private static final String SERIES_NAME = "spectrumData";

  protected final DefaultXYDataset m_dataSet;
  protected final JFreeChart m_chart;

  protected AbstractDataBox m_dataBox;
  protected ChartPanel m_spectrumPanel;
  protected JToolBar m_toolbar;
  protected DPeptideMatch m_peptideMatch;
  protected PeptideFragmentationData m_peptideFragmentationData;

  /**
   * Creates new form RsetPeptideSpectrumErrorPanel
   */
  public AbstractPeptideSpectrumPanel(String yAxisTitle) {

    m_dataSet = new DefaultXYDataset();
    m_chart = ChartFactory.createXYLineChart("", "m/z", yAxisTitle, m_dataSet, PlotOrientation.VERTICAL, true, true, false);
    m_chart.setNotify(true); // disable notifications. commented as it was "hard" to get it back...doesn't respond properly otherwise to mouse control
    m_chart.removeLegend();
    m_chart.setBackgroundPaint(Color.white);
    TextTitle textTitle = m_chart.getTitle();
    textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 10.0f));

    XYPlot plot = (XYPlot) m_chart.getPlot();
    plot.getRangeAxis().setUpperMargin(0.2);

    float maxXvalue;
    maxXvalue = (float) m_chart.getXYPlot().getDomainAxis().getUpperBound();

    m_chart.getXYPlot().getDomainAxis().setDefaultAutoRange(new Range(0, maxXvalue * 1.60));

    plot.setBackgroundPaint(Color.white);

    XYStickRenderer renderer = new XYStickRenderer();
    renderer.setBaseStroke(new BasicStroke(1.0f));

    plot.setRenderer(renderer);

    initComponents();

  }

  private void initComponents() {

    setLayout(new BorderLayout());

    ChartPanel cp = new ChartPanel(m_chart, true) {
      @Override
      public void restoreAutoBounds() {

        XYPlot plot = (XYPlot) getChart().getPlot();
        double domainStart = plot.getDomainAxis().getDefaultAutoRange().getLowerBound();
        double domainEnd = plot.getDomainAxis().getDefaultAutoRange().getUpperBound();
        double rangeStart = plot.getRangeAxis().getDefaultAutoRange().getLowerBound();
        double rangeEnd = plot.getRangeAxis().getDefaultAutoRange().getUpperBound();
        plot.getDomainAxis().setRange(domainStart, domainEnd);
        plot.getRangeAxis().setRange(rangeStart, rangeEnd);

      }
    };
    cp.setMinimumDrawWidth(0);
    cp.setMinimumDrawHeight(0);
    cp.setMaximumDrawWidth(Integer.MAX_VALUE); // make the legend to have a fixed size and not strecht it
    cp.setMaximumDrawHeight(Integer.MAX_VALUE); // when the windows becomes bigger.
    m_spectrumPanel = cp;

    // creation of the menuItem Show Spectrum Title
    JMenuItem showSpectrumTitle = new JMenuItem("Show Spectrum Title");
    showSpectrumTitle.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if ((m_peptideMatch != null) && (m_peptideMatch.isMsQuerySet()) && (m_peptideMatch.getMsQuery().isSpectrumFullySet())) {
          JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), m_peptideMatch.getMsQuery().getDSpectrum().getTitle(), "Spectrum Title", 1);
        }
      }
    });
    m_spectrumPanel.getPopupMenu().add(showSpectrumTitle);

    m_toolbar = initToolbar();

    add(m_toolbar, BorderLayout.WEST);
    add(m_spectrumPanel, BorderLayout.CENTER);

  }

  public final JToolBar initToolbar() {

    JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    toolbar.setFloatable(false);

    ExportButton exportImageButton = new ExportButton("Spectre", m_spectrumPanel);
    toolbar.add(exportImageButton);
    return toolbar;

  }

  public void setData(DPeptideMatch peptideMatch, PeptideFragmentationData peptideFragmentationData) {
    if ((peptideMatch != m_peptideMatch) || (m_peptideFragmentationData != peptideFragmentationData)) {
      m_peptideMatch = peptideMatch;
      m_peptideFragmentationData = peptideFragmentationData;
      updateFragmentationPlot(peptideMatch, peptideFragmentationData);
    }
  }

  abstract public void updateFragmentationPlot(DPeptideMatch peptideMatch, PeptideFragmentationData peptideFragmentationData);

  abstract public void addCustomAnnotations();

  public void annotateSpectrum() {

    clearAnnotations();

    if ((m_peptideMatch == null) || (m_peptideFragmentationData == null))
      return;

    if (m_peptideFragmentationData.isEmpty())
      return;

    if (!m_peptideMatch.isMsQuerySet() || !m_peptideMatch.getMsQuery().isSpectrumFullySet())
      return;

    List<PeptideFragmentationData.TheoreticalFragmentSeries> theoreticalFragmentSeries = m_peptideFragmentationData.getTheoreticalFragmentSeries();
    PeptideFragmentationData.FragmentMatch[] fragmentMatches = m_peptideFragmentationData.getFragmentMatches();
    String peptideSequence = m_peptideMatch.getPeptide().getSequence();
    int sizeMaxSeries = peptideSequence.length();

    double[][] fragTableTheo = new double[4][sizeMaxSeries + 1];
    double[][] fragTable = new double[4][sizeMaxSeries + 1];

    // **-*-*-* HERE READING Data from Objects *-*-*-*-**-

    XYTextAnnotation xyta;
    XYPlot plot = (XYPlot) m_chart.getPlot();

    String xyzSerieName = m_peptideFragmentationData.getXYZReferenceSeriesName();
    String abcSerieName = m_peptideFragmentationData.getABCReferenceSeriesName();
    int abcSeriesIndex = m_peptideFragmentationData.getTheoreticalFragmentSeriesIndex(abcSerieName);
    int xyzSeriesIndex = m_peptideFragmentationData.getTheoreticalFragmentSeriesIndex(xyzSerieName);

    double abcReferenceYPosition = getReferenceABCSeriesRange();
    double xyzReferenceYPosition =  getReferenceXYZSeriesRange();
    double tickHalfSize = pixelToValueRange(3);

    plot.clearRangeMarkers();
    Marker target = new ValueMarker(xyzReferenceYPosition);
    target.setPaint(XYZ_SERIE_COLOR);
    target.setLabel(xyzSerieName);
    target.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
    plot.addRangeMarker(target);
    Marker target2 = new ValueMarker(abcReferenceYPosition);
    target2.setPaint(ABC_SERIE_COLOR);
    target2.setLabel(abcSerieName);
    target2.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    target2.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
    plot.addRangeMarker(target2);

    int sizeABCserie = (theoreticalFragmentSeries.size() == 0) ? 0 : theoreticalFragmentSeries.get(abcSeriesIndex).masses.length;
    int sizeXYZserie = (theoreticalFragmentSeries.size() == 0) ? 0 : theoreticalFragmentSeries.get(xyzSeriesIndex).masses.length;


    if (xyzSerieName.contains("z+1")) {
      xyzSerieName = "z"; // we keep only the char sequence instead of full (ex: z+1 -> z)
    }

    int nbThroughB = 0;
    int nbThroughY = 0;

    for (int j = 0; j < theoreticalFragmentSeries.size(); j++) {
      // loop through theoFragment series here
      for (int k = 0; k < theoreticalFragmentSeries.get(j).masses.length; k++) {
        // loop through m_masses for each fragment series
        fragTableTheo[ABC_SERIE_LABEL_YPOS][nbThroughB] = abcReferenceYPosition; // intensity for b ions
        fragTableTheo[ABC_SERIE_LABEL_XPOS][nbThroughB] = theoreticalFragmentSeries.get(j).masses[k];
        fragTableTheo[XYZ_SERIE_LABEL_YPOS][nbThroughY] = xyzReferenceYPosition; // intensity for y ions
        fragTableTheo[XYZ_SERIE_LABEL_XPOS][nbThroughY] = theoreticalFragmentSeries.get(j).masses[k];
        for (int i = 0; i < fragmentMatches.length; i++) {
          // find matching fragMatches with theoFragSeries
          if (j == abcSeriesIndex) {
            if ((fragmentMatches[i].getCharge() == theoreticalFragmentSeries.get(j).getCharge())
                    && fragmentMatches[i].getSeriesName().equals(theoreticalFragmentSeries.get(j).frag_series)
                    && fragmentMatches[i].getPosition() == nbThroughB + 1
                    && Math.abs(fragmentMatches[i].calculated_moz - theoreticalFragmentSeries.get(j).masses[k]) < 0.1) {
              fragTable[ABC_SERIE_LABEL_YPOS][nbThroughB] = fragmentMatches[i].intensity;
              fragTable[ABC_SERIE_LABEL_XPOS][nbThroughB] = theoreticalFragmentSeries.get(j).masses[k];
            }
          }
          if (j == xyzSeriesIndex) {
            if ((fragmentMatches[i].getCharge() == theoreticalFragmentSeries.get(j).getCharge())
                    && fragmentMatches[i].getSeriesName().equals(theoreticalFragmentSeries.get(j).frag_series)
                    && (sizeMaxSeries - fragmentMatches[i].getPosition()) == nbThroughY
                    && Math.abs(fragmentMatches[i].calculated_moz - theoreticalFragmentSeries.get(j).masses[k]) < 0.1) {
              fragTable[XYZ_SERIE_LABEL_YPOS][nbThroughY] = fragmentMatches[i].intensity;
              fragTable[XYZ_SERIE_LABEL_XPOS][nbThroughY] = theoreticalFragmentSeries.get(j).masses[k];
            }
          }
        }

        if (j == abcSeriesIndex) {
          nbThroughB++;
        }
        if (j == xyzSeriesIndex) {
          nbThroughY++;
        }
      }
    }

    double abcPrev = fragTable[ABC_SERIE_LABEL_XPOS][0] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(0));

    boolean xyzPrevFound = false; // indicates if last iteration was a match or not. (if yes then highlight the AA)
    boolean abcPrevFound = false;
    String surroundingCharacters = "";

    Font font = new Font(Font.MONOSPACED, Font.BOLD, 11);

    if (!abcSerieName.equals("")) {
      if (peptideSequence.length() < sizeABCserie)  // fill sequence in case of length problem. should not happen
      {
        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(
                "AW: strange, serie length is not same length as sequence length...serie length:"
                        + sizeABCserie + " for sequence " + peptideSequence);
        for (int filler = 0; filler < (sizeABCserie - peptideSequence.length()); filler++) {
          peptideSequence = peptideSequence + "?";
        }
      }
      for (int i = 0; i < sizeABCserie; i++) { // loop through the series points

        // place separators marks------
        if (abcPrev != 0 && i > 0) {
          float dash[] = {10.0f};
          BasicStroke stk = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
          XYLineAnnotation line = new XYLineAnnotation(abcPrev, abcReferenceYPosition + tickHalfSize,
                  abcPrev, abcReferenceYPosition - tickHalfSize, stk, ABC_SERIE_COLOR);
          plot.addAnnotation(line);
        }

        // draw the outlined AA : B series
        if ((fragTable[ABC_SERIE_LABEL_XPOS][i] != 0) || ((i == sizeABCserie - 1) && abcPrevFound)) // if the aa is matched
        {
          if (i == (sizeABCserie - 1)) { // if last element to be highlighted
            abcPrevFound = true;
            fragTable[ABC_SERIE_LABEL_XPOS][i] = abcPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
          }
          String aa = "" + peptideSequence.charAt(i);
          xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (abcPrev + fragTable[ABC_SERIE_LABEL_XPOS][i]) / 2, abcReferenceYPosition);
          if (abcPrevFound || i == 0 || i == (sizeABCserie - 1)) {
            // 2 consecutives fragments matching, or first element or last element, then highlight the AA
            xyta.setPaint(Color.white);
            xyta.setBackgroundPaint(ABC_SERIE_COLOR);

          } else {
            xyta.setPaint(ABC_SERIE_COLOR);
            xyta.setBackgroundPaint(Color.white);
          }
          xyta.setFont(font);
          plot.addAnnotation(xyta);
          abcPrev = fragTableTheo[ABC_SERIE_LABEL_XPOS][i]; //
          abcPrevFound = true;

        } else // draw the regular expected (but not found) aa
        {
          String aa = "" + peptideSequence.charAt(i);
          if (i == sizeABCserie - 1) { // last element not highlighted
            fragTableTheo[ABC_SERIE_LABEL_XPOS][i] = abcPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
          }
          if (i == 0) {
            abcPrev = fragTableTheo[ABC_SERIE_LABEL_XPOS][0] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
          }
          xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (abcPrev + fragTableTheo[ABC_SERIE_LABEL_XPOS][i]) / 2, abcReferenceYPosition);
          xyta.setPaint(ABC_SERIE_COLOR);
          xyta.setFont(font);
          xyta.setBackgroundPaint(Color.white);
          plot.addAnnotation(xyta);

          abcPrev = fragTableTheo[ABC_SERIE_LABEL_XPOS][i];
          abcPrevFound = false;
        }
      }
    }

    //--------------------- xyz
    double xyzPrev = 0;
    if (!xyzSerieName.equals("")) {
      for (int i = sizeXYZserie - 1; i >= 0; i--) { // loop through the series points


        // place separators marks------
        if (xyzPrev != 0) {
          float dash[] = {10.0f};
          BasicStroke stk = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
          XYLineAnnotation line = new XYLineAnnotation(xyzPrev, xyzReferenceYPosition - tickHalfSize,
                  xyzPrev, xyzReferenceYPosition + tickHalfSize, stk, XYZ_SERIE_COLOR);
          plot.addAnnotation(line);
        }

        // draw the outlined AA : Y series
        if ((fragTable[XYZ_SERIE_LABEL_XPOS][i] != 0) || ((i == 0) && xyzPrevFound)) // if the aa is matched
        {
          if (i == 0) { // if last element to be highlighted
            xyzPrevFound = true;
            fragTable[XYZ_SERIE_LABEL_XPOS][i] = xyzPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
          }
          String aa = "" + peptideSequence.charAt(i);
          xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (xyzPrev + fragTable[XYZ_SERIE_LABEL_XPOS][i]) / 2, xyzReferenceYPosition);
          if (xyzPrevFound
                  || i == sizeXYZserie - 1
                  || i == 0) {// 2 consecutives fragments matching,
            // or first element or last element, then highlight the AA
            xyta.setPaint(Color.white);
            xyta.setBackgroundPaint(XYZ_SERIE_COLOR);

          } else {
            xyta.setPaint(XYZ_SERIE_COLOR);
            xyta.setBackgroundPaint(Color.white);
          }
          xyta.setFont(font);
          plot.addAnnotation(xyta);
          xyzPrev = fragTableTheo[XYZ_SERIE_LABEL_XPOS][i]; //
          xyzPrevFound = true;
        } else // draw the regular expected (but not found) aa
        {
          String aa = "" + peptideSequence.charAt(i);
          if (i == 0) { // first element not highlighted
            fragTableTheo[XYZ_SERIE_LABEL_XPOS][i] = xyzPrev + SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
          }
          if (i == sizeXYZserie - 1) {
            xyzPrev = fragTableTheo[XYZ_SERIE_LABEL_XPOS][i] - SpectrumFragmentationUtil.getMassFromAminoAcid(peptideSequence.charAt(i));
          }
          xyta = new XYTextAnnotation(surroundingCharacters + aa + surroundingCharacters, (xyzPrev + fragTableTheo[XYZ_SERIE_LABEL_XPOS][i]) / 2, xyzReferenceYPosition);
          xyta.setPaint(XYZ_SERIE_COLOR);
          xyta.setFont(font);
          xyta.setBackgroundPaint(Color.white);
          plot.addAnnotation(xyta);

          xyzPrev = fragTableTheo[XYZ_SERIE_LABEL_XPOS][i];
          xyzPrevFound = false;
        }
      }
    }

    addCustomAnnotations();
  }

  public double getReferenceABCSeriesRange() {
    XYPlot plot = (XYPlot) m_chart.getPlot();
    return (float) plot.getRangeAxis().getUpperBound() - pixelToValueRange(18);
  }

  public double getReferenceXYZSeriesRange() {
    XYPlot plot = (XYPlot) m_chart.getPlot();
    return (float) plot.getRangeAxis().getUpperBound() - pixelToValueRange(36);
  }

  public double pixelToValueRange(double pixelSize) {
    XYPlot plot = (XYPlot) m_chart.getPlot();
    Rectangle2D dataArea = m_spectrumPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
    double java2DToValue = plot.getRangeAxis().java2DToValue(0, dataArea, plot.getRangeAxisEdge());
    java2DToValue = Math.abs(java2DToValue - plot.getRangeAxis().java2DToValue(pixelSize, dataArea, plot.getRangeAxisEdge()));
    return java2DToValue;
  }

  abstract public double[][] getMassIntensityValues(DSpectrum spectrum);

  protected void clearAnnotations() {
    // clear all data
    XYPlot plot = (XYPlot) m_chart.getPlot();
    plot.clearAnnotations();
    plot.clearRangeMarkers();
  }

  protected void constructSpectrumChart(DPeptideMatch pm) {

    final XYPlot plot = (XYPlot) m_chart.getPlot();

    clearAnnotations();

    m_dataSet.removeSeries(SERIES_NAME);

    if (pm == null) {
      m_chart.setTitle("No Data Available");
      return;
    }

    Peptide p = pm.getPeptide();
    if (p == null) {
      m_chart.setTitle("No Data Available");
      return;
    }

    DMsQuery msQuery = pm.isMsQuerySet() ? pm.getMsQuery() : null;
    if (msQuery == null) {
      m_chart.setTitle("No Data Available");
      return;
    }

    DSpectrum spectrum = msQuery.isSpectrumFullySet() ? msQuery.getDSpectrum() : null;
    if (spectrum == null) {
      m_chart.setTitle("No Data Available");
      return;
    }

    double[][] data = getMassIntensityValues(spectrum);

    m_dataSet.addSeries(SERIES_NAME, data);

    // Set title
    String title = "Query " + msQuery.getInitialId() + " - " + p.getSequence();
    m_chart.setTitle(title);

    m_spectrumPanel.setBackground(Color.white);

    plot.getDomainAxis().setAutoRange(true);
    plot.getRangeAxis().setAutoRange(true);
  }

  @Override
  public void plotChanged(PlotChangeEvent arg0) {

    m_chart.getPlot().removeChangeListener(this);
    annotateSpectrum();
    m_chart.getPlot().addChangeListener(this);

  }

  @Override
  public void setDataBox(AbstractDataBox dataBox) {
    m_dataBox = dataBox;
  }

  @Override
  public AbstractDataBox getDataBox() {
    return m_dataBox;
  }

  @Override
  public void addSingleValue(Object v) {
    // not used for the moment JPM.TODO ?
  }

  @Override
  public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
    return m_dataBox.getRemoveAction(splittedPanel);
  }

  @Override
  public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
    return m_dataBox.getAddAction(splittedPanel);
  }

  @Override
  public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
    return m_dataBox.getSaveAction(splittedPanel);
  }

  public static class XYStickRenderer extends AbstractXYItemRenderer {
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea,
                         PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis,
                         XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {

      double x = dataset.getXValue(series, item);
      double y = dataset.getYValue(series, item);
      if (!Double.isNaN(y)) {
        org.jfree.ui.RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        org.jfree.ui.RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double transX = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
        double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);
        double transOY = rangeAxis.valueToJava2D(0, dataArea, yAxisLocation);
        g2.setPaint(getItemPaint(series, item));
        setSeriesPaint(0, Color.black); // AW: change peak bars color to black (was red by default)
        // g2.setStroke(getBaseStroke()); // AW: original
        g2.setStroke(new BasicStroke(0.5f)); // AW: A thinner stroke than the original
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.VERTICAL) {
          g2.drawLine((int) transX, (int) transOY, (int) transX, (int) transY);
        } else if (orientation == PlotOrientation.HORIZONTAL) {
          g2.drawLine((int) transOY, (int) transX, (int) transY, (int) transX);
        }
        int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
        int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
        updateCrosshairValues(crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY,
                orientation);

      }
    }
  }

}
