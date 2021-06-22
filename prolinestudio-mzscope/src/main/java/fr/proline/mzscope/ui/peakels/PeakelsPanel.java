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

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.algo.DotProductPatternScorer;
import fr.profi.mzdb.algo.PeakelsPatternPredictor;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.util.metrics.Metric;
import fr.proline.mzscope.model.*;
import fr.proline.mzscope.mzdb.MzdbFeatureWrapper;
import fr.proline.mzscope.processing.IsotopicPatternUtils;
import fr.proline.mzscope.processing.PeakelsHelper;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.ui.IMzScopeController;
import fr.proline.mzscope.ui.dialog.RTParamDialog;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ImportedDataTableModel;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Window;
import java.io.StreamCorruptedException;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.mutable.ArrayBuffer;

import static fr.proline.mzscope.processing.SpectrumUtils.MIN_CORRELATION_SCORE;

/**
 * Panel presenting a list of peakels in a table
 *
 * @author CB205360
 */
public class PeakelsPanel extends AbstractPeakelsPanel  {

  final private static Logger logger = LoggerFactory.getLogger(PeakelsPanel.class);

  protected List<IPeakel> m_peakels = new ArrayList<>();
  protected PeakelsHelper m_helper = null;

  public PeakelsPanel(IMzScopeController controller) {
    super(controller);
  }

  protected CompoundTableModel buildTableModel() {
    PeakelsTableModel model = new PeakelsTableModel();
    return new CompoundTableModel(model, true);
  }

  protected List<IPeakel> getSelectedIPeakels() {
    if ((m_peakels != null) && (!m_peakels.isEmpty()) && (m_table.getSelectedRowCount() > 0)) {
      int[] selectedRows = m_table.getSelectedRows();
      List<IPeakel> selectedPeakels = Arrays.stream(selectedRows).mapToObj(r -> m_peakels.get(getModelRowId(r))).collect(Collectors.toList());
      return selectedPeakels;
    }
    return null;
  }

  @Override
  protected List<Peakel> getSelectedPeakels() {
    List<IPeakel> selectedPeakels = getSelectedIPeakels();
    return selectedPeakels == null ? null : selectedPeakels.stream().map(p-> p.getPeakel()).collect(Collectors.toList());
  }

  @Override
  protected void matchIons(List<BaseFeature> ions, ImportedDataTableModel importedTableModel) {

    Metric metric = new Metric("ions matching");
    PeakelsHelper helper = getPeakelsHelper();
    float mzPPMTolerance = MzScopePreferences.getInstance().getMzPPMTolerance();
    IRawFile rawFile = m_peakels.get(0).getRawFile();
    int matchingCount = 0;

    List<Pair<Integer, IFeature>> matchedFeatures = new ArrayList<>();
    List<IPeakel> notFound = new ArrayList<>();

    List<IFeature> duplicateIons = ions.stream().filter(i -> Collections.frequency(ions, i) > 1).collect(Collectors.toList());
    logger.info("ions duplicates summary :: {}", duplicateIons.size());

    int psmRtColumnIdx = findColumn(importedTableModel, new String[]{"rt.y"});

    for (int k = 0; k < ions.size(); k++) {

      double ionMz = ions.get(k).getMz();
      double ionRt = ions.get(k).getElutionTime() * 60.0;
      int ionCharge = ions.get(k).getCharge();
      double ionApexIntensity = ions.get(k).getApexIntensity();

      double tolDa = (ionMz * mzPPMTolerance) / 1e6;
      float tolRt = 40.0f;

      List<Peakel> peakelList = helper.findCoelutingPeakels(ionMz - tolDa, ionMz + tolDa, (float)(ionRt - tolRt), (float)(ionRt + tolRt));

      if (peakelList != null) {
        // ions rt have been rounded to 0.01 minutes, take this approximation into account
        List<Peakel> filteredPeakelList = peakelList.stream().filter(p -> (ionRt >= (p.getFirstElutionTime()-0.6) && ionRt <= (p.getLastElutionTime()+0.6))).collect(Collectors.toList());
        Peakel peakel = null;
        if (!filteredPeakelList.isEmpty()) {
          matchingCount++;
          if (filteredPeakelList.size() == 1) {
            peakel = filteredPeakelList.get(0);
          } else {
            peakel = filteredPeakelList.stream().collect(Collectors.minBy(Comparator.comparingDouble(p -> Math.abs(ionMz-p.getApexMz())))).get();
          }

//          if ((Math.abs(peakel.getApexMz() - 546.31072) < 0.001) && (Math.abs(peakel.getApexElutionTime()/60.0 - 138.59) < 0.01)) {
//            logger.info("stop here");
//          }
          List<Peakel> isotopes = helper.findFeatureIsotopes(peakel, ionCharge, mzPPMTolerance);
          Feature feature = new Feature(isotopes.get(0).getMz(), ionCharge, JavaConverters.asScalaBufferConverter(isotopes).asScala(), true);
          matchedFeatures.add(new ImmutablePair<>(k, new MzdbFeatureWrapper(feature, rawFile, 1)));

          if (isotopes.size() > 1) {
            double dm = (isotopes.get(1).getMz() - isotopes.get(0).getMz()) * ionCharge;
            if (Math.abs(dm - IsotopePatternEstimator.avgIsoMassDiff()) < 0.01) {
              metric.addValue("isotopic dmass", dm);
            } else {
              metric.incr("missing second isotope detected");
            }
          } else {
            metric.incr("mono isotopic feature detected");
          }

          //try to assess monoisotope and charge of the matched peakel
          List<Peakel> coelutingPeakels = helper.findCoelutingPeakels(peakel.getApexMz() - helper.HALF_MZ_WINDOW,
                  peakel.getApexMz() + helper.HALF_MZ_WINDOW,
                  peakel.getElutionTime() - tolRt,
                  peakel.getElutionTime() + tolRt);

          SpectrumData spectrum = helper.buildSpectrumFromPeakels(coelutingPeakels, peakel);
          TheoreticalIsotopePattern pattern = IsotopicPatternUtils.predictIsotopicPattern(spectrum, peakel.getMz(), mzPPMTolerance)._2;

          // alternative method to select the best pattern hypothese
//          TheoreticalIsotopePattern alternativepattern = null;
//          Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = DotProductPatternScorer.calcIsotopicPatternHypotheses(spectrum, peakel.getMz(), mzPPMTolerance);
//          boolean atLeastOneCorrelation = false;
//          for (int j = 0;j< putativePatterns.length; j++) {
//            TheoreticalIsotopePattern putativePattern = putativePatterns[j]._2;
//            List<Peakel> scoredPeakels = helper.findFeatureIsotopes(peakel, putativePattern.charge(), mzPPMTolerance);
//            if (scoredPeakels.size() > 2) {
//              atLeastOneCorrelation = true;
//              double corr = SpectrumUtils.correlation(peakel, scoredPeakels.get(0));
//              double corr2 = SpectrumUtils.correlation(peakel, scoredPeakels.get(1));
//              double corr3 = SpectrumUtils.correlation(peakel, scoredPeakels.get(2));
//              if ((corr > 0.6) && (corr2 > 0.6) && (corr3 > 0.6)) {
//                alternativepattern = putativePattern;
//                metric.incr("correlating isotope peakels found");
//                break;
//              }
//            } else {
//
//            }
//          }
//
//          if (alternativepattern == null) {
//            metric.incr("no alternative pattern found");
//            if (atLeastOneCorrelation) {
//              metric.incr("at least one correlation calculated");
//            }
//          } else {
//            if ((alternativepattern.charge() == pattern.charge()) && (Math.abs(pattern.monoMz() - alternativepattern.monoMz()) <= tolDa)) {
//              metric.incr("alternative pattern == pattern");
//            }
//            pattern = alternativepattern;
//          }


          if ((pattern.charge() != ionCharge) || (Math.abs(pattern.monoMz() - peakel.getMz()) > tolDa)) {
            metric.incr("incorrect prediction");
            metric.addValue("incorrect prediction intensity distribution", ionApexIntensity);

            logger.info("incorrect prediction: expected {}, {}+, predicted {}, {}+ (rt = {})", peakel.getMz(), ionCharge, pattern.monoMz(), pattern.charge(), ionRt / 60.0);
            // try to predict from the psm RT
            if (psmRtColumnIdx != -1) {
              float psmRt = ((Double) importedTableModel.getValueAt(k, psmRtColumnIdx)).floatValue() * 60.0f;
              testPrediction(psmRt, helper, peakel, tolRt, mzPPMTolerance, tolDa, ionCharge, metric, "incorrect psm rt");
            }

            if (pattern.charge() != ionCharge) {
              metric.incr("incorrect charge prediction");
            }

            if (Math.abs(pattern.monoMz() - peakel.getMz()) > tolDa) {
              metric.incr("incorrect mono mz prediction");
            }

              // try to rescue prediction at 10ppm if the moz tolerance was below 5ppm
//            if (mzPPMTolerance <= 5.0) {
//              // try to predict at 10 ppm to assess moz tolerance impact
//              coelutingPeakels = helper.findCoelutingPeakels(peakel.getApexMz() - helper.HALF_MZ_WINDOW,
//                      peakel.getApexMz() + helper.HALF_MZ_WINDOW,
//                      peakel.getElutionTime() - tolRt,
//                      peakel.getElutionTime() + tolRt);
//
//              spectrum = helper.buildSpectrumFromPeakels(coelutingPeakels, peakel);
//              pattern = IsotopicPatternUtils.predictIsotopicPattern(spectrum, peakel.getMz(), 10.0)._2;
//              if ((pattern.charge() != ionCharge) || (Math.abs(pattern.monoMz() - peakel.getMz()) > tolDa)) {
//                metric.incr("incorrect even at 10ppm");
//              } else {
//                logger.info("rescued at 10ppm {}; {}; {}; {}; {}+", k, ionMz, ionRt/60.0, ionApexIntensity , ionCharge);
//                metric.incr("rescued at 10ppm");
//              }
//            }


          } else {
            // prediction was correct, negative control: test prediction from the psmRt
//            if (psmRtColumnIdx != -1) {
//              float psmRt = ((Double) importedTableModel.getValueAt(k, psmRtColumnIdx)).floatValue() * 60.0f;
//              testPrediction(psmRt, helper, peakel, tolRt, mzPPMTolerance, tolDa, ionCharge, metric, "ctr psm rt");
//            }
            metric.addValue("correct prediction intensity distribution", ionApexIntensity);
          }
          
        } else {
          notFound.add(ions.get(k));
          logger.warn("no peakel found for {}, {}, {}, {}+", k, ionMz, ionRt / 60.0, ionCharge);
        }
      }
    }

    logger.info("Found {} matches over {}", matchingCount, ions.size());
    logger.info("Distinct features matched :: {}", matchedFeatures.stream().map(p -> p.getRight()).distinct().count());

    List<IFeature> featureList = matchedFeatures.stream().map(p -> p.getRight()).collect(Collectors.toList());
    List<IFeature> duplicates = featureList.stream().filter(i -> Collections.frequency(featureList, i) > 1).collect(Collectors.toList());

    logger.info("incorrect predictions:: ");
    logger.info(metric.toString());
    logger.info("Not found summary :: {}", notFound.size());

    Map<Integer, List<IFeature>> result = matchedFeatures.stream().map(p -> p.getRight()).collect(Collectors.groupingBy(IFeature::getPeakelsCount, Collectors.toList()));
    for (Map.Entry<Integer, List<IFeature>> e : result.entrySet()) {
      logger.info("matched Features with {} peakels: {} or {} distinct", e.getKey(), e.getValue().size(), e.getValue().stream().distinct().count());
    }

    Map<String, List<IFeature>> map = new HashMap<>();
    map.put(PeakelsHelper.VALID_FEATURES, featureList);
    m_viewersController.displayFeatures(map);


    // dump peakels stats of matched features
//    for (IFeature f : featureList) {
//      for (Peakel p : f.getPeakels()) {
//        double[] mzValues = p.mzValues();
//        double standardDeviation = new DescriptiveStatistics(mzValues).getStandardDeviation();
//        double delta = 1e6*(p.getApexMz() - p.calcWeightedMz())/p.getApexMz();
//        logger.info("matched peakel: {}; {}; {}; {}; {}", p.getMz(), p.getElutionTime()/60.0, p.getApexIntensity(), delta, 1e6 * standardDeviation / p.getMz());
//      }
//    }

  }


  protected void testPrediction(float psmRt, PeakelsHelper helper, Peakel peakel, float tolRt, double moztol, double tolDa, int expectedCharge, Metric metric, String prefix) {
    if ((psmRt >= peakel.getFirstElutionTime()) && (psmRt <= peakel.getLastElutionTime())) {

      //logger.info("Trying to predict monoisotope & charge from rt={} instead of {} ...", psmRt/60.0, peakel.getElutionTime()/60.0);
      List<Peakel> coelutingPeakels = helper.findCoelutingPeakels(peakel.getApexMz() - helper.HALF_MZ_WINDOW,
              peakel.getApexMz() + helper.HALF_MZ_WINDOW,
              psmRt - tolRt,
              psmRt + tolRt);

      // search for the spectrumId corresponding to the psm RT
      int idx = java.util.Arrays.binarySearch(peakel.getElutionTimes(), psmRt);
      idx = (idx < 0) ? ~idx : idx;
      // build spectrum from the spectrumId
      Tuple2<ArrayBuffer<Object>, ArrayBuffer<Object>> pairs = PeakelsPatternPredictor.slicePeakels(JavaConverters.asScalaBufferConverter(coelutingPeakels).asScala(), peakel.getSpectrumIds()[idx]);
      List<Object> objectList = JavaConverters.bufferAsJavaListConverter(pairs._1).asJava();
      double[] mzList = new double[objectList.size()];
      int j = 0;
      for(Object o: objectList) {
        mzList[j++] = ((Double)o).doubleValue();
      }

      objectList = JavaConverters.bufferAsJavaListConverter(pairs._2).asJava();
      float[] intensitiesList = new float[objectList.size()];
      j = 0;
      for(Object o: objectList) {
        intensitiesList[j++] = ((Float)o).floatValue();
      }
      SpectrumData spectrum = new SpectrumData(mzList, intensitiesList);
      TheoreticalIsotopePattern pattern = IsotopicPatternUtils.predictIsotopicPattern(spectrum, peakel.getMz(), moztol)._2;
      if ((pattern.charge() != expectedCharge) || (Math.abs(pattern.monoMz() - peakel.getMz()) > tolDa)) {
        metric.incr(prefix+" incorrect prediction");
      } else {
        metric.incr(prefix +" prediction rescued");
      }
    } else {
      metric.incr(prefix+" best psm outside peakel bounds");
    }

  }
  protected JToolBar initToolbar() {

    JToolBar toolbar = super.initToolbar();

    JButton buildSpectrumBtn = new JButton();
    buildSpectrumBtn.setIcon(IconManager.getIcon(IconManager.IconType.CENTROID_SPECTRA));
    buildSpectrumBtn.setToolTipText("Build Spectrum from peakels");
    buildSpectrumBtn.addActionListener(e -> buildSpectrum());

    toolbar.add(buildSpectrumBtn);

    JButton buildFeaturesBtn = new JButton();
    buildFeaturesBtn.setIcon(IconManager.getIcon(IconManager.IconType.ISOTOPES_PREDICTION));
    buildFeaturesBtn.setToolTipText("Deconvolute peakels to features");
    buildFeaturesBtn.addActionListener(e -> buildFeatures());

    JButton matchIonsBtn = new JButton();
    matchIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.TABLE_IMPORT));
    matchIonsBtn.setToolTipText("Match (m/z,rt) values from a csv file...");
    matchIonsBtn.addActionListener(e -> matchCSVIons());

    toolbar.add(matchIonsBtn);

    toolbar.add(buildFeaturesBtn);

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

      logger.info(Arrays.toString(spectrumData.getMzList()));
      logger.info(Arrays.toString(spectrumData.getIntensityList()));

      Spectrum spectrum = new Spectrum(-1, peakel.getElutionTime(), spectrumData.getMzList(), spectrumData.getIntensityList(), 1, Spectrum.ScanType.CENTROID);
      m_viewersController.getRawFileViewer(peakels.get(0).getRawFile(), true).setReferenceSpectrum(spectrum);
    }
  }
  
   private void buildFeatures() {
  
    RTParamDialog dialog = new RTParamDialog(null);
    dialog.setHelpHeaderText("The feature detection (especially charge state detection) needs to <br>" +
            "group co-eluting peakels. The coelution definition is based on the following <br>" +
            "time tolerance.");
    dialog.pack();
    dialog.setVisible(true);
    
    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
      try {
        
        PeakelsHelper helper = getPeakelsHelper();
        Map<String, List<Feature>> features = helper.deisotopePeakels(MzScopePreferences.getInstance().getMzPPMTolerance(), dialog.getRTTolerance());
        Map<String, List<IFeature>> wrappedFeatures = new HashMap<>();
        IRawFile rawFile = m_peakels.get(0).getRawFile();

        for(Map.Entry<String, List<Feature>> e : features.entrySet()) {
          List<IFeature> listIFeatures = new ArrayList<>(e.getValue().size());
          e.getValue().forEach(f -> listIFeatures.add(new MzdbFeatureWrapper(f, rawFile, 1)));
          wrappedFeatures.put(e.getKey(), listIFeatures);
        }

        m_viewersController.displayFeatures(wrappedFeatures);

      } catch (StreamCorruptedException ex) {
        Exceptions.printStackTrace(ex);
      } catch (SQLiteException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  private PeakelsHelper getPeakelsHelper() {
    if (m_helper == null) {
      Peakel[] apeakels = m_peakels.stream().map(p -> p.getPeakel()).toArray(s -> new Peakel[s]);
      m_helper = new PeakelsHelper(apeakels);
    }
    return m_helper;
  }

  public void addPeakels(List<IPeakel> peakels) {
    ((PeakelsTableModel)m_compoundTableModel.getBaseModel()).addPeakels(peakels);
    this.m_peakels.addAll(peakels);
    m_markerContainerPanel.setMaxLineNumber(peakels.size());
  }


  public void setPeakels(List<IPeakel> peakels, boolean displayRawFileColumn) {
    m_modelSelectedRowBeforeSort = -1;
    m_helper = null;
    ((PeakelsTableModel)m_compoundTableModel.getBaseModel()).setPeakels(peakels);
    this.m_peakels = peakels;
    m_markerContainerPanel.setMaxLineNumber(peakels.size());
    // hide RawFileName column
    m_table.getColumnExt(m_table.convertColumnIndexToView(FeaturesTableModel.COLTYPE_FEATURE_RAWFILE.getIndex())).setVisible(displayRawFileColumn);
  }

}

