package fr.proline.studio.rsmexplorer.gui.ptm;

public class ViewContext {

  private int m_ajustedLocation;
  private int m_areaWidth;

  public ViewContext() {

  }

  public int getAreaWidth() {
    return m_areaWidth;
  }

  public ViewContext setAreaWidth(int areaWidth) {
    this.m_areaWidth = areaWidth;
    return this;
  }

  public int getAjustedLocation() {
    return m_ajustedLocation;
  }

  public ViewContext setAjustedLocation(int ajustedLocation) {
    this.m_ajustedLocation = ajustedLocation;
    return this;
  }
}
