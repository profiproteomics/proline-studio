package fr.proline.studio.rsmexplorer.gui.ptm;

public class ViewContext {

  private int m_ajustedStartLocation;
  private int m_ajustedEndLocation;
  private int m_areaWidth;
  private boolean m_showNCtermIndex = true;

  public ViewContext() {

  }

  public int getAreaWidth() {
    return m_areaWidth;
  }

  public ViewContext setAreaWidth(int areaWidth) {
    this.m_areaWidth = areaWidth;
    return this;
  }


    public boolean isNCtermIndexShown() {
        return m_showNCtermIndex;
    }

    public ViewContext setShowNCtermIndex(boolean showNCterm) {
        this.m_showNCtermIndex = showNCterm;
        return this;
    }
}
