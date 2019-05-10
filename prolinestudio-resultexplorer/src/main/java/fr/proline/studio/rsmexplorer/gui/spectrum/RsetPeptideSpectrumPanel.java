package fr.proline.studio.rsmexplorer.gui.spectrum;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.GenerateSpectrumMatchTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.DataBoxRsetPeptideSpectrum;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.GenerateSpectrumMarchesDialog;
import fr.proline.studio.utils.IconManager;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.ui.TextAnchor;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

/**
 * Panel used to display a Spectrum of a PeptideMatch
 *
 * @author JM235353
 */
public class RsetPeptideSpectrumPanel extends AbstractPeptideSpectrumPanel {


  protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
  private static String SPECTRUM_GENERATION_RUNNING = "Generate Spectrum Match in progress...";

  private JButton m_generateMatchButton;
  private boolean _isSpectrumMatchGenerationAsked = false;

  /**
   * Creates new form RsetPeptideSpectrumPanel
   */
  public RsetPeptideSpectrumPanel() {
    super("intensity");
    customizeToolbar();
  }

  private void customizeToolbar() {
    m_generateMatchButton = new JButton();
    m_generateMatchButton.setIcon(IconManager.getIcon(IconManager.IconType.FRAGMENTATION));
    m_generateMatchButton.setToolTipText("Generate & Store Spectrum Match");
    m_generateMatchButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            GenerateSpectrumMarchesDialog dialog = new GenerateSpectrumMarchesDialog(WindowManager.getDefault().getMainWindow());
            Point location = m_generateMatchButton.getLocationOnScreen();
            dialog.setLocation(location.x + 30, location.y - dialog.getHeight() - 60);
            dialog.setVisible(true);
            if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
              Long frsId = dialog.getFragmentationRuleSetId();
              Boolean forceGenerate = dialog.getDoForceGenerate();
              _isSpectrumMatchGenerationAsked = true;
              m_generateMatchButton.setIcon(IconManager.getIconWithHourGlass(IconManager.IconType.LOAD_ALIGNMENT_CLOUD));
              m_generateMatchButton.setToolTipText(SPECTRUM_GENERATION_RUNNING);
              generateSpectrumMatch(frsId, forceGenerate);

            }
          }
        }
    );
    m_toolbar.add(m_generateMatchButton);
  }

  private void generateSpectrumMatch(Long frsId, Boolean forceGenerateSM) {

    AbstractJMSCallback spectrumMatchCallback = new AbstractJMSCallback() {
      @Override
      public boolean mustBeCalledInAWT() {
        return true;
      }

      @Override
      public void run(boolean success) {
        if (success) {
          _isSpectrumMatchGenerationAsked = false;//this boolean will affect cloud show
          ((DataBoxRsetPeptideSpectrum) m_dataBox).loadAnnotations(m_peptideMatch);
        } else {
          m_logger.error("Fail to generate spectrum matches for peptide_match.id=" + m_peptideMatch.getId());
        }
        m_generateMatchButton.setIcon(IconManager.getIcon(IconManager.IconType.FRAGMENTATION));
        m_generateMatchButton.setToolTipText("Generate & Store Spectrum Match");
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    };

    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    GenerateSpectrumMatchTask task = new GenerateSpectrumMatchTask(spectrumMatchCallback, null, m_dataBox.getProjectId(), m_peptideMatch.getResultSetId(), null, m_peptideMatch.getId(), frsId, forceGenerateSM);
    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
  }

  public void updateFragmentationPlot(DPeptideMatch peptideMatch, PeptideFragmentationData peptideFragmentationData) {
    Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
    if (DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
      m_generateMatchButton.setEnabled(true);
    }

    m_chart.getPlot().removeChangeListener(this);
    constructSpectrumChart(peptideMatch);
    // set default auto bounds since this values will be used by restoreAutoBounds int he abstract super class
    m_chart.getXYPlot().getRangeAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getRangeAxis().getLowerBound(), m_chart.getXYPlot().getRangeAxis().getUpperBound()));
    m_chart.getXYPlot().getDomainAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getDomainAxis().getLowerBound(), m_chart.getXYPlot().getDomainAxis().getUpperBound()));
    annotateSpectrum();
    m_chart.getPlot().addChangeListener(this);
  }

  @Override
  public void addCustomAnnotations() {

    PeptideFragmentationData.FragmentMatch[] fragmentationMatches = m_peptideFragmentationData.getFragmentMatches();
    XYPlot plot = (XYPlot) m_chart.getPlot();

    double abcReferenceYPosition = getReferenceABCSeriesRange();
    double xyzReferenceYPosition =  getReferenceXYZSeriesRange();
    double spacerValue = pixelToValueRange(3);
    double pointerSizeValue = pixelToValueRange(20);

    for (PeptideFragmentationData.FragmentMatch match : fragmentationMatches) {
      String label = (match.neutral_loss_mass != null && match.neutral_loss_mass > 0) ? match.label + "-" + Math.round(match.neutral_loss_mass) : match.label ;
      final XYPointerAnnotation pointer = new XYPointerAnnotation(label,match.moz,match.intensity + spacerValue,6.0 * Math.PI / 4.0);
      pointer.setBaseRadius(5.0);
      pointer.setTipRadius(0.0);
      pointer.setArrowWidth(2);
      pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));

      Color color = match.isABCSerie() ? AbstractPeptideSpectrumPanel.ABC_SERIE_COLOR : AbstractPeptideSpectrumPanel.XYZ_SERIE_COLOR;
      double referenceYPosition =  match.isABCSerie() ? abcReferenceYPosition : xyzReferenceYPosition;
      pointer.setArrowPaint(color);
      pointer.setPaint(color);
      pointer.setTextAnchor(TextAnchor.BOTTOM_CENTER);

      StringBuilder builder = new StringBuilder("<html>");
      builder.append("m/z: ").append(match.moz).append("<br>");
      builder.append("intensity: ").append(match.intensity);
      if (match.neutral_loss_mass != null && match.neutral_loss_mass > 0) {
        builder.append("<br>").append("Neutral loss: ").append(match.neutral_loss_mass).append("<br>");
      }
      builder.append("</html>");
      pointer.setToolTipText(builder.toString());

      plot.addAnnotation(pointer);

      float dash[] = {3.0f};

      String referenceSeries = match.isABCSerie() ? m_peptideFragmentationData.getABCReferenceSeriesName() : m_peptideFragmentationData.getXYZReferenceSeriesName();

      if ( (m_peptideFragmentationData.getTheoreticalFragmentSeries(referenceSeries).isMatching(match))
              && ((match.intensity + spacerValue + pointerSizeValue)< referenceYPosition)) {
        BasicStroke stk = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash, 0.5f);
        XYLineAnnotation line = new XYLineAnnotation(match.calculated_moz,referenceYPosition, match.calculated_moz, match.intensity + spacerValue + pointerSizeValue, stk, color);
        plot.addAnnotation(line);
      }
    }
  }

  @Override
  public double[][] getMassIntensityValues(DSpectrum spectrum) {
    return spectrum.getMassIntensityValues();
  }

  @Override
  public void setShowed(boolean showed) {

  }
}
