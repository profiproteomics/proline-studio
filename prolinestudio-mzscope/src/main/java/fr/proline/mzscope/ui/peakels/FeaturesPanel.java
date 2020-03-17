/*
 * Copyright (C) 2019 VD225637
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
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ImportedDataTableModel;

import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import fr.proline.studio.utils.IconManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * panel that contains the features/peaks table
 *
 * @author CB205360
 */
public class FeaturesPanel extends AbstractPeakelsPanel {

  final private static Logger logger = LoggerFactory.getLogger(FeaturesPanel.class);
  private final static String LAST_DIR = "mzscope.last.csv.features.directory";

  private JFileChooser m_fchooser;
  private List<IFeature> m_features = new ArrayList<>();
  private PeakelsHelper m_helper = null;

  public FeaturesPanel(IMzScopeController controller) {
    super(controller);
    m_fchooser = new JFileChooser();
    m_fchooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.getName().endsWith(".csv");
      }

      @Override
      public String getDescription() {
        return "*.csv";
      }
    });
  }

  protected CompoundTableModel buildTableModel() {
    FeaturesTableModel model = new FeaturesTableModel();
    return new CompoundTableModel(model, true);
  }


  protected JToolBar initToolbar() {

    JToolBar toolbar = super.initToolbar();

    JButton importCSVBtn = new JButton();
    importCSVBtn.setIcon(IconManager.getIcon(IconManager.IconType.TABLE_IMPORT));
    importCSVBtn.setToolTipText("Import m/z values from a csv file...");
    importCSVBtn.addActionListener(e -> importCSVIons());

    toolbar.add(importCSVBtn);

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
    List<Peakel> coelutingPeakels = helper.findCoelutigPeakels(peakel.getApexMz() - 5,
            peakel.getApexMz() + 5,
            peakel.getFirstElutionTime(),
            peakel.getLastElutionTime());
    SpectrumData spectrumData = helper.buildSpectrumDataFromPeakels(peakel, coelutingPeakels);
    Spectrum spectrum = new Spectrum(-1, peakel.getApexElutionTime(), spectrumData.getMzList(), spectrumData.getIntensityList(), 1, Spectrum.ScanType.CENTROID);
    m_viewersController.getRawFileViewer(peakels.get(0).getRawFile(), true).setReferenceSpectrum(spectrum);
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
    ((FeaturesTableModel)m_compoundTableModel.getBaseModel()).setFeatures(features);
    this.m_features = features;
    m_markerContainerPanel.setMaxLineNumber(features.size());
    // hide RawFileName column
    m_table.getColumnExt(m_table.convertColumnIndexToView(FeaturesTableModel.COLTYPE_FEATURE_RAWFILE.getIndex())).setVisible(displayRawFileColumn);
  }

  private void importCSVIons() {
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    String directory = prefs.get(LAST_DIR, m_fchooser.getCurrentDirectory().getAbsolutePath());
    m_fchooser.setCurrentDirectory(new File(directory));
    int result = m_fchooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File csvFile = m_fchooser.getSelectedFile();
      String fileName = csvFile.getName();
      if (!fileName.endsWith(".csv")) {
        JOptionPane.showMessageDialog(this, "The file must be a csv file", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      prefs.put(LAST_DIR, csvFile.getParentFile().getAbsolutePath());
      ImportedDataTableModel importedTableModel = new ImportedDataTableModel();
      ImportedDataTableModel.loadFile(importedTableModel, csvFile.getAbsolutePath(), ';', true, false);
      int mzColumnIdx = findColumn(importedTableModel, new String[]{"moz", "m/z", "mz"});
      int rtColumnIdx = findColumn(importedTableModel, new String[]{"rt", "retention_time", "retention time", "elution_time", "elution time", "time"});
      int zColumnIdx = findColumn(importedTableModel, new String[]{"charge", "z"});

      if (mzColumnIdx != -1) {
        List<Double> mzValues = new ArrayList<>();
        List<Double> rtValues = new ArrayList<>();
        List<Integer> zValues = new ArrayList<>();
        for (int k = 0; k < importedTableModel.getRowCount(); k++) {
          mzValues.add((Double) importedTableModel.getValueAt(k, mzColumnIdx));
          rtValues.add((rtColumnIdx != -1) ? (Double) importedTableModel.getValueAt(k, rtColumnIdx) : -1.0);
          zValues.add((zColumnIdx != -1) ? ((Long) importedTableModel.getValueAt(k, zColumnIdx)).intValue() : 0);
        }

        matchFeatures(mzValues, rtValues, zValues);
      } else {
        JOptionPane.showMessageDialog(this, "No column named \"mz\",\"moz\" or \"m/z\" detected in the imported file.\n Verify the column headers (the column separator must be \";\")", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void matchFeatures(List<Double> mzValues, List<Double> rtValues, List<Integer> zValues) {
    float moztol = 10; //MzScopePreferences.getInstance().getMzPPMTolerance();

    Map<Integer, List<IFeature>> featuresByNominalMass = m_features.stream().collect(Collectors.groupingBy(f -> Integer.valueOf((int) f.getMz()), Collectors.toList()));
    int matchingCount = 0;

    List<Pair<Integer, IFeature>> matched = new ArrayList<>();
    List<IFeature> notFound = new ArrayList<>();
    List<IFeature> dubiousFeatures = new ArrayList<>();

    for (int k = 0; k < mzValues.size(); k++) {
      double mz = mzValues.get(k);
      double rt = rtValues.get(k) * 60.0;
      int z = zValues.get(k);
      List<IFeature> features = featuresByNominalMass.get(Integer.valueOf((int) mz));
      if (features != null) {
        IFeature feature = features.stream().filter(f -> {
                  double tolDa = f.getMz() * moztol / 1e6;
                  return (rt >= f.getFirstElutionTime() && rt <= f.getLastElutionTime() && (Math.abs(f.getMz() - mz) < tolDa) && ((f.getCharge() == 0) || (z == f.getCharge())));
                }
        ).findFirst().orElse(null);

        if (feature != null) {
          matchingCount++;
          matched.add(new ImmutablePair<>(k, feature));
          logger.info("Feature matching: {}; {}; {}; {}+ = {}; {}; {}+; {}", k, mz, rt, z, feature.getMz(), feature.getElutionTime(), feature.getCharge(), feature.getPeakelsCount());
          if (feature.getPeakelsCount() <= 2) {
            dubiousFeatures.add(feature);
          }
        } else {
          BaseFeature f = new BaseFeature(mz, (float) rt, (float) rt, (float) rt, null, 1);
          f.setCharge(z);
          notFound.add(f);
          logger.warn("no feature found for {}, {}, {}+", mz, rt / 60.0, z);
        }
      } else {
        BaseFeature f = new BaseFeature(mz, (float) rt, (float) rt, (float) rt, null, 1);
        f.setCharge(z);
        notFound.add(f);
        logger.warn("no feature found for {}, {}, {}+", mz, rt / 60.0, z);
      }
    }

    logger.info("Found {} matches over {}", matchingCount, mzValues.size());

    logger.info("Not matched summary :: {}", matched.size());
    matched.forEach(p -> logger.info("matched Feature  = {}; {}; {}; {}; {} ",p.getLeft(), p.getRight().getMz(), p.getRight().getElutionTime() / 60.0, p.getRight().getCharge(), p.getRight().getPeakelsCount()));

    logger.info("Not found summary :: {}", notFound.size());
    notFound.forEach(feature -> logger.info("Feature not found = {}, {}, {}+", feature.getMz(), feature.getElutionTime() / 60.0, feature.getCharge()));

    logger.info("Dubious features summary :: {} ", dubiousFeatures.size());

    dubiousFeatures.forEach(feature -> logger.info("Dubious Feature  = {}, {}, {}+, {} ", feature.getMz(), feature.getElutionTime() / 60.0, feature.getCharge(), feature.getPeakelsCount()));

    logger.info("Found {} matches over {}", matchingCount, mzValues.size());
    logger.info("Not found summary :: {}", notFound.size());
    logger.info("Dubious features summary :: {} ", dubiousFeatures.size());

    Map<Integer, Long> result = dubiousFeatures.stream().collect(Collectors.groupingBy(IFeature::getPeakelsCount, Collectors.counting()));
    for (Map.Entry<Integer, Long> e : result.entrySet()) {
      logger.info("dubious features with {} peakels: {}", e.getKey(), e.getValue());
    }

  }

  private int findColumn(AbstractTableModel tableModel, String[] alternativeNames) {
    int columnIdx = -1;
    for (String name : alternativeNames) {
      columnIdx = tableModel.findColumn(name);
      if (columnIdx != -1) {
        break;
      }
    }
    return columnIdx;
  }
}

