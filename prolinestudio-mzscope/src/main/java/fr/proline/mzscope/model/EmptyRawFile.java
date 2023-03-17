package fr.proline.mzscope.model;

import fr.profi.mzdb.model.SpectrumHeader;

import java.io.File;
import java.util.List;
import java.util.Map;

public class EmptyRawFile implements IRawFile {

  private String m_name;

  public EmptyRawFile(String name) {
    m_name=name;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public File getFile() {
    return null;
  }

  @Override
  public IChromatogram getXIC(ExtractionRequest params) {
    return null;
  }

  @Override
  public IChromatogram getTIC(int msLevel) {
    return null;
  }

  @Override
  public IChromatogram getBPI() {
    return null;
  }

  @Override
  public List<IPeakel> extractPeakels(FeaturesExtractionRequest params) {
    return null;
  }

  @Override
  public List<IFeature> extractFeatures(FeaturesExtractionRequest params) {
    return null;
  }

  @Override
  public Spectrum getSpectrum(int spectrumIndex) {
    return null;
  }

  @Override
  public int getSpectrumCount() {
    return 0;
  }

  @Override
  public int getSpectrumId(double retentionTime) {
    return 0;
  }

  @Override
  public double[] getElutionTimes(int msLevel) {
    return new double[0];
  }

  @Override
  public double getSpectrumElutionTime(int spectrumIndex) {
    return 0;
  }

  @Override
  public int getNextSpectrumId(int spectrumIndex, int msLevel) {
    return 0;
  }

  @Override
  public int getPreviousSpectrumId(int spectrumIndex, int msLevel) {
    return 0;
  }

  @Override
  public List<Float> getMsMsEvent(double minMz, double maxMz) {
    return null;
  }

  @Override
  public boolean exportRawFile(String outFileName, IExportParameters exportParams) {
    return false;
  }

  @Override
  public boolean isDIAFile() {
    return false;
  }

  @Override
  public boolean hasIonMobilitySeparation() {
    return false;
  }

  @Override
  public Map<String, Object> getFileProperties() {
    return null;
  }

  @Override
  public QCMetrics getFileMetrics() {
    return null;
  }

  @Override
  public void closeIRawFile() {

  }

  @Override
  public IonMobilityIndex getIonMobilityIndex() {
    return null;
  }

  @Override
  public Map<SpectrumHeader, IsolationWindow> getIsolationWindowByMs2Headers() { return null;  }

  @Override
  public String toString(){
    return "converting  "+getName();
  }
}
