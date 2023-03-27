package fr.proline.mzscope.model;

import fr.proline.mzscope.utils.BinarySearch;

public class TIMSMobilityIndex implements IonMobilityIndex {

  BinarySearch bs = new BinarySearch();

  private double[] mobilityIndexes;

  public TIMSMobilityIndex(double[] mobilities) {
    mobilityIndexes = new double[mobilities.length];
    for (int k = 0; k < mobilities.length; k++) {
      mobilityIndexes[mobilities.length-1 - k] = mobilities[k];
    }
  }

  @Override
  public int getIndex(double value) {
    int index =  bs.searchIndex(mobilityIndexes, value, 0, mobilityIndexes.length);
    if (index >= 0) {
      return mobilityIndexes.length - 1 - index;
    } else {
      return (value < mobilityIndexes[0]) ? getMaxIndex() : 0;
    }
  }

  @Override
  public int[] getIndexRange(double min, double max) {
    int minIdx = getIndex(min);
    int maxIdx = getIndex(max);
    if (max > mobilityIndexes[maxIdx] && maxIdx < mobilityIndexes.length-1) maxIdx++;
    return new int[]{maxIdx, minIdx};
  }

  @Override
  public double getMobility(int index) {
    return mobilityIndexes[mobilityIndexes.length-1 - index];
  }

  @Override
  public int getMaxIndex() {
    return mobilityIndexes.length - 1;
  }

  @Override
  public double getMinValue() {
    return mobilityIndexes[0];
  }

  @Override
  public double getMaxValue() {
    return mobilityIndexes[mobilityIndexes.length - 1];
  }

  @Override
  public int getNextMobilityIndex(int index) {
    return (index > 0) ? index - 1 : index;
  }

  @Override
  public int getPreviousMobilityIndex(int index) {
    return (index < mobilityIndexes.length-1) ? index + 1 : index;
  }

}
