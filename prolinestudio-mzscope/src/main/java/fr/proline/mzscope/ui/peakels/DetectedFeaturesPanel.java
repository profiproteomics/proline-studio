package fr.proline.mzscope.ui.peakels;

import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.ui.IMzScopeController;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DetectedFeaturesPanel extends JPanel {

  private FeaturesPanel featuresPanel;
  private FeaturesPanel invalidFeaturesPanel;

  public DetectedFeaturesPanel(IMzScopeController controller) {
    featuresPanel = new FeaturesPanel(controller);
    invalidFeaturesPanel = new FeaturesPanel(controller);
    setLayout(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    tabbedPane.add("Detected features", featuresPanel);
    tabbedPane.add("Invalid features", invalidFeaturesPanel);
    add(tabbedPane, BorderLayout.CENTER);
  }

  public void addFeatures(List<IFeature> features) {
    Map<Boolean, List<IFeature>> list = features.stream().collect(Collectors.partitioningBy(f -> f.getPeakelsCount() > 1));
    featuresPanel.addFeatures(list.get(Boolean.TRUE));
    invalidFeaturesPanel.addFeatures(list.get(Boolean.FALSE));
  }

  public void setFeatures(List<IFeature> features, boolean displayRawFileColumn) {
    Map<Boolean, List<IFeature>> list = features.stream().collect(Collectors.partitioningBy(f -> f.getPeakelsCount() > 1));
    featuresPanel.setFeatures(list.get(Boolean.TRUE), displayRawFileColumn);
    invalidFeaturesPanel.setFeatures(list.get(Boolean.FALSE), displayRawFileColumn);
  }

}
