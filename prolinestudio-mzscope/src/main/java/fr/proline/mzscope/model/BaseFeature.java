package fr.proline.mzscope.model;

import fr.profi.mzdb.model.Peakel;

public class BaseFeature extends BasePeakel implements IFeature {

  private int charge;

  public BaseFeature(double mz, float elutionTime, float firstElutionTime, float lastElutionTime, IRawFile rawfile, int msLevel) {
    super(mz, elutionTime, firstElutionTime, lastElutionTime, rawfile, msLevel);
  }

  public BaseFeature(double mz, float elutionTime, float firstElutionTime, float lastElutionTime, IRawFile rawfile, int msLevel, int charge) {
    super(mz, elutionTime, firstElutionTime, lastElutionTime, rawfile, msLevel);
    this.charge = charge;
  }

  @Override
  public int getPeakelsCount() {
    return 0;
  }

  @Override
  public Peakel[] getPeakels() {
    return new Peakel[0];
  }

  public void setCharge(int charge) {
    this.charge = charge;
  }

  @Override
  public int getCharge() {
    return this.charge;
  }
}
