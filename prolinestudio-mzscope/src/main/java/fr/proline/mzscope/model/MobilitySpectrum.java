package fr.proline.mzscope.model;

import fr.profi.mzdb.model.SpectrumData;
import it.unimi.dsi.fastutil.doubles.Double2DoubleArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class MobilitySpectrum extends WrappedSpectrum {

  private static final Logger logger = LoggerFactory.getLogger(MobilitySpectrum.class);
  private final IonMobilityIndex ionMobilityIndex;

  public MobilitySpectrum(Spectrum spectrum, IonMobilityIndex mobilityIndex) {
    super(spectrum);
    this.ionMobilityIndex = mobilityIndex;
  }

  public Spectrum applyMobilityFilter(double min, double max) {
// TODO use mzdb-access ion_mobility branch to enable this feature
//    int minIndex = ionMobilityIndex.getIndex(min);
//    int maxIndex = ionMobilityIndex.getIndex(max);
//    logger.info("filtering Spectrum peaks within mobility range {}-{}", min, max);
//
//
//    int minMobilityIndex;
//    int maxMobilityIndex;
//    if (minIndex > maxIndex) {
//      maxMobilityIndex = minIndex;
//      minMobilityIndex = maxIndex;
//    } else {
//      minMobilityIndex = minIndex;
//      maxMobilityIndex = maxIndex;
//    }
//
//    final double[] masses = underlyingSpectrum.getSpectrumData().getMzList();
//    final float[] intensities = underlyingSpectrum.getSpectrumData().getIntensityList();
//    final short[] mobilityIndexes = underlyingSpectrum.getSpectrumData().getMobilityIndexList();
//    int count = 0;
//
//    for (int k = 0; k < mobilityIndexes.length; k++) {
//      if (mobilityIndexes[k] >= minMobilityIndex && mobilityIndexes[k] <= maxMobilityIndex) count++;
//    }
//
//    double[] fMasses = new double[count];
//    float[] fIntensities = new float[count];
//    short[] fMobilityIndexes = new short[count];
//    count = 0;
//    for (int k = 0; k < masses.length; k++) {
//      if (mobilityIndexes[k] >= minMobilityIndex && mobilityIndexes[k] <= maxMobilityIndex) {
//        fMasses[count] = masses[k];
//        fIntensities[count] = intensities[k];
//        fMobilityIndexes[count] = mobilityIndexes[k];
//        count++;
//      }
//    }
//
//    final SpectrumData data = new SpectrumData(fMasses, fIntensities, null, null, fMobilityIndexes);
//    return new WrappedSpectrum(underlyingSpectrum, data);
    return underlyingSpectrum;
  }

  public Spectrum applyMobilityFilterbySum(double min, double max) {

// TODO use mzdb-access ion_mobility branch to enable this feature
//    logger.info("filtering Spectrum peaks within mobility range {}-{}", min, max);
//
//    int minIndex = ionMobilityIndex.getIndex(min);
//    int maxIndex = ionMobilityIndex.getIndex(max);
//
//    int minMobilityIndex = Math.min(minIndex, maxIndex);
//    int maxMobilityIndex = Math.max(minIndex, maxIndex);
//
//    final double[] masses = underlyingSpectrum.getSpectrumData().getMzList();
//    final float[] intensities = underlyingSpectrum.getSpectrumData().getIntensityList();
//    final short[] mobilityIndexes = underlyingSpectrum.getSpectrumData().getMobilityIndexList();
//
//    Double2DoubleArrayMap masses2Intensities = new Double2DoubleArrayMap();
//
//    for (int k = 0; k < masses.length; k++) {
//      if (mobilityIndexes[k] >= minMobilityIndex && mobilityIndexes[k] <= maxMobilityIndex) {
//        double intensity = masses2Intensities.getOrDefault(masses[k], 0.0);
//        masses2Intensities.put(masses[k], intensity + intensities[k]);
//      }
//    }
//
//    double[] fMasses = new double[masses2Intensities.size()];
//    float[] fIntensities = new float[masses2Intensities.size()];
//    final Double2DoubleMap.FastEntrySet entries = masses2Intensities.double2DoubleEntrySet();
//    final List<Double2DoubleMap.Entry> sortedEntries = entries.stream().sorted((e1, e2) -> Double.compare(e1.getDoubleKey(), e2.getDoubleKey())).collect(Collectors.toList());
//    int count = 0;
//
//    for (Double2DoubleMap.Entry e : sortedEntries) {
//      fMasses[count] = e.getDoubleKey();
//      fIntensities[count++] = (float)e.getDoubleValue();
//    }
//
//    final SpectrumData data = new SpectrumData(fMasses, fIntensities, null, null, null);
//    return new WrappedSpectrum(underlyingSpectrum, data);
    return underlyingSpectrum;
  }
  public IonMobilityIndex getIonMobilityIndex() {
    return ionMobilityIndex;
  }

}
