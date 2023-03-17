package fr.proline.mzscope.model;

public class IsolationWindow extends fr.profi.mzdb.model.IsolationWindow {

  String center;

  public IsolationWindow(String center, double minMz, double maxMz) {
    super(minMz, maxMz);
    center = center;
  }

  public String getCenter() {
    return center;
  }

  public boolean contains(double mz) {
    return (mz >= getMinMz() && mz <= getMaxMz());
  }
}