package fr.proline.mzscope.model;

import fr.profi.mzdb.model.Peakel;

public interface IFeature extends IPeakel {

  int getPeakelsCount();

  Peakel[] getPeakels();

  int getCharge();


}
