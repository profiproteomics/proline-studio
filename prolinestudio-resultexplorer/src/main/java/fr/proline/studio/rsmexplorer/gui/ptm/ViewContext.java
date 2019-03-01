package fr.proline.studio.rsmexplorer.gui.ptm;

public class ViewContext {

  private int _ajustedLocation;
  private int _areaWidth;

  public ViewContext() {

  }

  public int getAreaWidth() {
    return _areaWidth;
  }

  public ViewContext setAreaWidth(int areaWidth) {
    this._areaWidth = areaWidth;
    return this;
  }

  public int getAjustedLocation() {
    return _ajustedLocation;
  }

  public ViewContext setAjustedLocation(int ajustedLocation) {
    this._ajustedLocation = ajustedLocation;
    return this;
  }
}
