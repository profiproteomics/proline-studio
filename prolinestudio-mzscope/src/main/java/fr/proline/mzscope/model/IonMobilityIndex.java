package fr.proline.mzscope.model;

public interface IonMobilityIndex {
  int getIndex(double value);

  int[] getIndexRange(double min, double max);

  double getMobility(int index);

  int getMaxIndex();

  double getMinValue();

  double getMaxValue();

  int getNextMobilityIndex(int index);

  int getPreviousMobilityIndex(int index);
}
