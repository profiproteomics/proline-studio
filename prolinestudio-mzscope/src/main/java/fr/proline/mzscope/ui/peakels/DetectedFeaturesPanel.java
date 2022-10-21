package fr.proline.mzscope.ui.peakels;

import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.processing.PeakelsHelper;
import fr.proline.mzscope.ui.IMzScopeController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetectedFeaturesPanel extends JPanel {

  private FeaturesPanel featuresPanel;
  private FeaturesPanel dubiousFeaturesPanel;

  public DetectedFeaturesPanel(IMzScopeController controller) {
    featuresPanel = new FeaturesPanel(controller);
    dubiousFeaturesPanel = new FeaturesPanel(controller);
    setLayout(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    tabbedPane.add("Valid features", featuresPanel);
    tabbedPane.add("Dubious features", dubiousFeaturesPanel);
    add(tabbedPane, BorderLayout.CENTER);
  }

  public void addFeatures(List<IFeature> features) {
    featuresPanel.addFeatures(features);
  }

  public void setFeatures(Map<String, List<IFeature>> features, boolean displayRawFileColumn) {
    featuresPanel.setFeatures(features.get(PeakelsHelper.VALID_FEATURES), displayRawFileColumn);
    dubiousFeaturesPanel.setFeatures(features.get(PeakelsHelper.DUBIOUS_FEATURES), displayRawFileColumn);
  }

  public void setFeatures(List<IFeature> features, boolean displayRawFileColumn) {
    featuresPanel.setFeatures(features, displayRawFileColumn);
    dubiousFeaturesPanel.setFeatures(new ArrayList<>(), displayRawFileColumn);
  }

}
