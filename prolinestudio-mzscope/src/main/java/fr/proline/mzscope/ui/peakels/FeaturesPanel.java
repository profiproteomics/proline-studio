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
package fr.proline.mzscope.ui.peakels;

import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumData;
import fr.proline.mzscope.model.BaseFeature;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IPeakel;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.processing.PeakelsHelper;
import fr.proline.mzscope.ui.IMzScopeController;
import fr.proline.mzscope.ui.dialog.RTParamDialog;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ImportedDataTableModel;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * panel that contains the features/peaks table
 *
 * @author CB205360
 */
public class FeaturesPanel extends AbstractPeakelsPanel {

  final private static Logger logger = LoggerFactory.getLogger(FeaturesPanel.class);

  private List<IFeature> m_features = new ArrayList<>();
  private PeakelsHelper m_helper = null;

  public FeaturesPanel(IMzScopeController controller) {
    super(controller);
  }

  protected CompoundTableModel buildTableModel() {
    FeaturesTableModel model = new FeaturesTableModel();
    return new CompoundTableModel(model, true);
  }


  protected JToolBar initToolbar() {

    JToolBar toolbar = super.initToolbar();

    JButton matchIonsBtn = new JButton();
    matchIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.TABLE_IMPORT));
    matchIonsBtn.setToolTipText("Match (m/z,rt) values from a csv file...");
    matchIonsBtn.addActionListener(e -> matchCSVIons());

    toolbar.add(matchIonsBtn);

    JButton buildSpectrumBtn = new JButton();
    buildSpectrumBtn.setIcon(IconManager.getIcon(IconManager.IconType.SIGNAL));
    buildSpectrumBtn.setToolTipText("Build Spectrum from peakels");
    buildSpectrumBtn.addActionListener(e -> buildSpectrum());

    toolbar.add(buildSpectrumBtn);

    return toolbar;
  }


  private void buildSpectrum() {
    List<IPeakel> peakels = getSelectedIPeakels();
    Peakel peakel = peakels.get(0).getPeakel();
    PeakelsHelper helper = getPeakelsHelper();
    RTParamDialog dialog = new RTParamDialog((Window)this.getTopLevelAncestor());
    dialog.setHelpHeaderText("The spectrum construction needs to search for co-eluting peakels.<br>" +
            " The coelution definition is based on the following time tolerance.");
    dialog.pack();
    dialog.setVisible(true);
    
    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

      List<Peakel> coelutingPeakels = helper.findCoelutingPeakels(peakel.getApexMz() - 5,
            peakel.getApexMz() + 5,
             peakel.getElutionTime() - dialog.getRTTolerance(),
             peakel.getLastElutionTime() + dialog.getRTTolerance());
      SpectrumData spectrumData = helper.buildSpectrumFromPeakels(coelutingPeakels, peakel);
      Spectrum spectrum = new Spectrum(-1, peakel.getElutionTime(), spectrumData.getMzList(), spectrumData.getIntensityList(), 1, Spectrum.ScanType.CENTROID);
      m_viewersController.getRawFileViewer(peakels.get(0).getRawFile(), true).setReferenceSpectrum(spectrum, 1.0f);
    }
  }

  private PeakelsHelper getPeakelsHelper() {
    if (m_helper == null) {
      Peakel[] apeakels = m_features.stream().flatMap(f -> Arrays.stream(f.getPeakels())).toArray(s -> new Peakel[s]);
      m_helper = new PeakelsHelper(apeakels);
    }
    return m_helper;
  }

  @Override
  protected List<IPeakel> getSelectedIPeakels() {
    if ((m_features != null) && (!m_features.isEmpty()) && (m_table.getSelectedRowCount() > 0)) {
      int[] selectedRows = m_table.getSelectedRows();
      List<IPeakel> selectedPeakels = Arrays.stream(selectedRows).mapToObj(r -> m_features.get(getModelRowId(r))).collect(Collectors.toList());
      return selectedPeakels;
    }
    return null;
  }

  @Override
  protected List<Peakel> getSelectedPeakels() {
    if ((m_features != null) && (!m_features.isEmpty()) && (m_table.getSelectedRowCount() > 0)) {
      int[] selectedRows = m_table.getSelectedRows();
      List<Peakel> selectedPeakels = Arrays.stream(selectedRows).mapToObj(r -> m_features.get(getModelRowId(r))).flatMap(f-> Arrays.stream(f.getPeakels())).collect(Collectors.toList());
      return selectedPeakels;
    }
    return null;
  }

  public void addFeatures(List<IFeature> features) {
    ((FeaturesTableModel)m_compoundTableModel.getBaseModel()).addFeatures(features);
    this.m_features.addAll(features);
    m_markerContainerPanel.setMaxLineNumber(features.size());
  }

  public void setFeatures(List<IFeature> features, boolean displayRawFileColumn) {
    m_modelSelectedRowBeforeSort = -1;
    m_helper = null;    
    m_features = features == null ? new ArrayList<IFeature>() : features;
    ((FeaturesTableModel)m_compoundTableModel.getBaseModel()).setFeatures(m_features);
    m_markerContainerPanel.setMaxLineNumber(m_features.size());
    // hide RawFileName column
    m_table.getColumnExt(m_table.convertColumnIndexToView(FeaturesTableModel.COLTYPE_FEATURE_RAWFILE.getIndex())).setVisible(displayRawFileColumn);
  }


  protected void matchIons(List<BaseFeature> ions, ImportedDataTableModel importedTableModel) {

    float moztol = MzScopePreferences.getInstance().getMzPPMTolerance();
    Map<Integer, List<IFeature>> featuresByNominalMass = m_features.stream().collect(Collectors.groupingBy(f -> Integer.valueOf((int) f.getMz()), Collectors.toList()));
    int matchingCount = 0;

    List<Pair<Integer, IFeature>> matchedFeatures = new ArrayList<>();
    List<IFeature> notFound = new ArrayList<>();

    List<IFeature> duplicateIons = ions.stream().filter(i -> Collections.frequency(ions, i) > 1).collect(Collectors.toList());
    logger.info("ions duplicates summary :: {}", duplicateIons.size());


    for (int k = 0; k < ions.size(); k++) {
      BaseFeature ion = ions.get(k);
      double mz = ion.getMz();
      double rt = ion.getElutionTime() * 60.0;
      int z = ion.getCharge();
      double i = ion.getApexIntensity();
      
      List<IFeature> features = featuresByNominalMass.get(Integer.valueOf((int) mz));
      if (Math.abs( Integer.valueOf((int) mz) - mz) < 0.01) {
        features.addAll(featuresByNominalMass.get(Integer.valueOf((int) mz) - 1));
      } else if (Math.abs( Integer.valueOf((int) mz) - mz) > 0.99) {
        features.addAll(featuresByNominalMass.get(Integer.valueOf((int) mz) + 1));
      }

      if (features != null) {
        IFeature feature = features.stream().filter(f -> {
                  double tolDa = (f.getMz() * moztol) / 1e6;
                  return (rt >= (f.getFirstElutionTime() -0.6) && rt <= (f.getLastElutionTime()+0.6) && (Math.abs(f.getMz() - mz) < tolDa) && ((f.getCharge() == 0) || (z == 0) || (z == f.getCharge())));
                }
        ).findFirst().orElse(null);

        if (feature != null) {
          matchingCount++;
          matchedFeatures.add(new ImmutablePair<>(k, feature));
          //logger.info("Feature matching: {}; {}; {}; {}+ = {}; {}; {}+; {}", k, mz, rt, z, feature.getMz(), feature.getElutionTime(), feature.getCharge(), feature.getPeakelsCount());
          if ((z == 0) || (feature.getCharge() == 0)) {
            logger.warn("Charge 0 detected !!!!!!!!!!");
          }

          if (Math.abs(ion.getApexIntensity() - feature.getApexIntensity()) > 2) {
            Peakel[] peakels = feature.getPeakels();
            int j = 0;
            for (; j < peakels.length; j++) {
              if (Math.abs(ion.getApexIntensity() - peakels[j].getApexIntensity()) < 2) {
                break;
              }
            }
           
            if (j >= peakels.length) {
              logger.info("Inconsistent matching : the ion intensity cannot be found: {}; {}; {}; {}; {}+ = {}; {}; {}; {}+; {}", k, mz, rt/60.0, i , z, feature.getMz(), feature.getElutionTime()/60.0, feature.getApexIntensity(), feature.getCharge(), feature.getPeakelsCount());
            } else {
              logger.info("The peakel matching the ion has been found at index {}. Feature matching: {}; {}; {}; {}; {}+ = {}; {}; {}; {}+; {}", j,  k, mz, rt/60.0, i , z, feature.getMz(), feature.getElutionTime()/60.0, feature.getApexIntensity(), feature.getCharge(), feature.getPeakelsCount());
            }
          }


        } else {
          notFound.add(ion);
          //logger.warn("no feature found for {}, {}, {}+, {}", mz, rt / 60.0, z, getRowAsString(k, importedTableModel));
        }
      } else {
        notFound.add(ion);
        //logger.warn("no feature found for {}, {}, {}+, {}", mz, rt / 60.0, z,  getRowAsString(k, importedTableModel));
      }
    }

    logger.info("Found {} matches over {} ions among {} features ", matchingCount, ions.size(), m_features.size());

    logger.info("Matched summary :: {}", matchedFeatures.size());
     //matchedFeatures.forEach(p -> logger.info("matched Feature  = {}; {}; {}; {}; {} ",p.getLeft(), p.getRight().getMz(), p.getRight().getElutionTime() / 60.0, p.getRight().getCharge(), p.getRight().getPeakelsCount()));

    logger.info("Not found summary :: {}", notFound.size());
    //notFound.forEach(feature -> logger.info("Feature not found = {}, {}, {}+", feature.getMz(), feature.getElutionTime() / 60.0, feature.getCharge()));

    logger.info("Found {} matches over {}", matchingCount, ions.size());
    logger.info("Distinct features matched :: {}", matchedFeatures.stream().map(p -> p.getRight()).distinct().count());
    logger.info("Not found summary :: {}", notFound.size());

    Map<Integer, List<IFeature>> result = matchedFeatures.stream().map(p -> p.getRight()).collect(Collectors.groupingBy(IFeature::getPeakelsCount, Collectors.toList()));
    for (Map.Entry<Integer, List<IFeature>> e : result.entrySet()) {
      logger.info("matched Features with {} peakels: {} or {} distinct", e.getKey(), e.getValue().size(), e.getValue().stream().distinct().count());

//      List<IFeature> list = e.getValue();
//      List<IFeature> duplicates = list.stream().filter(i -> Collections.frequency(list, i) > 1).collect(Collectors.toList());
//      logger.info("duplicates matches :: {}", duplicates.size());

    }

    List<IFeature> monoIsotopicFeatures = matchedFeatures.stream().map(p -> p.getRight()).filter(f -> f.getPeakelsCount() == 1).collect(Collectors.toList());
    logger.info("Mono isotopic features summary :: {}", monoIsotopicFeatures.size());
    //monoIsotopicFeatures.forEach(feature -> logger.info("Mono Feature  = {}, {}, {}+, {} ", feature.getMz(), feature.getElutionTime() / 60.0, feature.getCharge(), feature.getPeakelsCount()));

  }


}

