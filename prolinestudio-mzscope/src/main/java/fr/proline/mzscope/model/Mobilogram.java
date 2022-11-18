package fr.proline.mzscope.model;

import it.unimi.dsi.fastutil.shorts.Short2DoubleArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2DoubleMap;

import java.util.List;
import java.util.stream.Collectors;

public class Mobilogram {

  private String title;
  double[] mobilities;
  float[] intensities;
  MobilitySpectrum spectrum;

  public Mobilogram(MobilitySpectrum spectrum) {
    this.spectrum = spectrum;
    buildMobilogram(Double.MIN_VALUE, Double.MAX_VALUE);
    title = "Mobilogram - "+ spectrum.getTitle();
  }

  public String getTitle() {
    return title;
  }

  private void buildMobilogram(double minMz, double maxMz) {

    final float[] intensitiesArray = spectrum.getSpectrumData().getIntensityList();
    final short[] mobilityIndexes = spectrum.getSpectrumData().getMobilityIndexList();
    final double[] masses = spectrum.getSpectrumData().getMzList();

    Short2DoubleMap values = new Short2DoubleArrayMap();

    for (int k = 0; k < mobilityIndexes.length; k++) {
      double v = (values.containsKey(mobilityIndexes[k])) ? values.get(mobilityIndexes[k]) : 0.0;
      double increment = (masses[k] >= minMz) && (masses[k] <= maxMz) ? intensitiesArray[k] : 0.0;
      values.put(mobilityIndexes[k], v + increment);
    }

    final IonMobilityIndex mobilityIndex = spectrum.getIonMobilityIndex();
    final List<Short> shortList = values.keySet().stream().sorted().collect(Collectors.toList());
    mobilities = new double[shortList.size()];
    intensities = new float[shortList.size()];
    for (int k = shortList.size() - 1; k >= 0; k--) {
      mobilities[k] = mobilityIndex.getMobility(shortList.get(k).intValue());
      intensities[k] = (float)values.getOrDefault(shortList.get(k).shortValue(), 0.0);
    }
  }

  public void setMzFilter(double minMz, double maxMz) {
    buildMobilogram(minMz, maxMz);
  }

  public double[] getMobilities() {
    return mobilities;
  }

  public float[] getIntensities() {
    return intensities;
  }
}
