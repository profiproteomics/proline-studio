package fr.proline.mzscope.model;

import fr.profi.mzdb.model.SpectrumData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrappedSpectrum extends Spectrum {
  protected final Spectrum underlyingSpectrum;

  public WrappedSpectrum(Spectrum spectrum) {
    super(spectrum.getIndex(), spectrum.getRetentionTime(), spectrum.getSpectrumData(), spectrum.getMsLevel(), spectrum.getDataType());
    this.underlyingSpectrum = spectrum;
  }

  protected WrappedSpectrum(Spectrum spectrum, SpectrumData data) {
    this(spectrum);
    spectrumData = data;
  }

  @Override
  public Integer getIndex() {
    return underlyingSpectrum.getIndex();
  }

  @Override
  public float getRetentionTime() {
    return underlyingSpectrum.getRetentionTime();
  }

  @Override
  public String getTitle() {
    return underlyingSpectrum.getTitle();
  }

  @Override
  public void setTitle(String title) {
    underlyingSpectrum.setTitle(title);
  }

  @Override
  public int getMsLevel() {
    return underlyingSpectrum.getMsLevel();
  }

  @Override
  public ScanType getDataType() {
    return underlyingSpectrum.getDataType();
  }

  @Override
  public Double getPrecursorMz() {
    return underlyingSpectrum.getPrecursorMz();
  }

  @Override
  public void setPrecursorMz(Double precursorMz) {
    underlyingSpectrum.setPrecursorMz(precursorMz);
  }

  @Override
  public Integer getPrecursorCharge() {
    return underlyingSpectrum.getPrecursorCharge();
  }

  @Override
  public void setPrecursorCharge(Integer precursorCharge) {
    underlyingSpectrum.setPrecursorCharge(precursorCharge);
  }
}
