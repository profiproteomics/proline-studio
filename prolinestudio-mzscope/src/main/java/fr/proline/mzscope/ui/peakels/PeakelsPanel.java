/*
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.ui.peakels;

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumData;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IPeakel;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.mzdb.MzdbFeatureWrapper;
import fr.proline.mzscope.processing.PeakelsHelper;
import fr.proline.mzscope.ui.IMzScopeController;
import fr.proline.mzscope.ui.dialog.RTParamDialog;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Window;
import java.io.StreamCorruptedException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;

/**
 * Panel presenting a list of peakels in a table
 *
 * @author CB205360
 */
public class PeakelsPanel extends AbstractPeakelsPanel  {

  protected List<IPeakel> m_peakels = new ArrayList<>();
  protected PeakelsHelper m_helper = null;

  public PeakelsPanel(IMzScopeController controller) {
    super(controller);
  }

  protected CompoundTableModel buildTableModel() {
    PeakelsTableModel model = new PeakelsTableModel();
    return new CompoundTableModel(model, true);
  }

  protected List<IPeakel> getSelectedIPeakels() {
    if ((m_peakels != null) && (!m_peakels.isEmpty()) && (m_table.getSelectedRowCount() > 0)) {
      int[] selectedRows = m_table.getSelectedRows();
      List<IPeakel> selectedPeakels = Arrays.stream(selectedRows).mapToObj(r -> m_peakels.get(getModelRowId(r))).collect(Collectors.toList());
      return selectedPeakels;
    }
    return null;
  }

  @Override
  protected List<Peakel> getSelectedPeakels() {
    return getSelectedIPeakels().stream().map(p-> p.getPeakel()).collect(Collectors.toList());
  }

  protected JToolBar initToolbar() {

    JToolBar toolbar = super.initToolbar();

    JButton buildSpectrumBtn = new JButton();
    buildSpectrumBtn.setIcon(IconManager.getIcon(IconManager.IconType.SIGNAL));
    buildSpectrumBtn.setToolTipText("Build Spectrum from peakels");
    buildSpectrumBtn.addActionListener(e -> buildSpectrum());

    toolbar.add(buildSpectrumBtn);

    JButton buildFeaturesBtn = new JButton();
    buildFeaturesBtn.setIcon(IconManager.getIcon(IconManager.IconType.ISOTOPES_PREDICTION));
    buildFeaturesBtn.setToolTipText("Build Features from peakels");
    buildFeaturesBtn.addActionListener(e -> buildFeatures());

    toolbar.add(buildFeaturesBtn);

    return toolbar;
  }

  private void buildSpectrum() {
    List<IPeakel> peakels = getSelectedIPeakels();
    Peakel peakel = peakels.get(0).getPeakel();
    PeakelsHelper helper = getPeakelsHelper();

    RTParamDialog dialog = new RTParamDialog((Window)this.getTopLevelAncestor());
    dialog.pack();
    dialog.setVisible(true);
    
    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
      
      List<Peakel> coelutingPeakels = helper.findCoelutingPeakels(peakel.getApexMz() - 5,
              peakel.getApexMz() + 5,
              peakel.getElutionTime() - dialog.getRTTolerance(),
              peakel.getLastElutionTime() + dialog.getRTTolerance());
      SpectrumData spectrumData = helper.buildSpectrumDataFromPeakels(peakel, coelutingPeakels);
      Spectrum spectrum = new Spectrum(-1, peakel.getElutionTime(), spectrumData.getMzList(), spectrumData.getIntensityList(), 1, Spectrum.ScanType.CENTROID);
      m_viewersController.getRawFileViewer(peakels.get(0).getRawFile(), true).setReferenceSpectrum(spectrum);
    }
  }
  
   private void buildFeatures() {
  
    RTParamDialog dialog = new RTParamDialog(null);
    dialog.pack();
    dialog.setVisible(true);
    
    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
      try {
        
        PeakelsHelper helper = getPeakelsHelper();
        List<Feature> features = helper.deisotopePeakels(MzScopePreferences.getInstance().getMzPPMTolerance(), dialog.getRTTolerance());
        List<IFeature> listF = new ArrayList<>();
        IRawFile rawFile = m_peakels.get(0).getRawFile();
        features.forEach(f -> listF.add(new MzdbFeatureWrapper(f, rawFile, 1)));
        m_viewersController.displayFeatures(listF);
        
      } catch (StreamCorruptedException ex) {
        Exceptions.printStackTrace(ex);
      } catch (SQLiteException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  private PeakelsHelper getPeakelsHelper() {
    if (m_helper == null) {
      Peakel[] apeakels = m_peakels.stream().map(p -> p.getPeakel()).toArray(s -> new Peakel[s]);
      m_helper = new PeakelsHelper(apeakels);
    }
    return m_helper;
  }

  public void addPeakels(List<IPeakel> peakels) {
    ((PeakelsTableModel)m_compoundTableModel.getBaseModel()).addPeakels(peakels);
    this.m_peakels.addAll(peakels);
    m_markerContainerPanel.setMaxLineNumber(peakels.size());
  }


  public void setPeakels(List<IPeakel> peakels, boolean displayRawFileColumn) {
    m_modelSelectedRowBeforeSort = -1;
    m_helper = null;
    ((PeakelsTableModel)m_compoundTableModel.getBaseModel()).setPeakels(peakels);
    this.m_peakels = peakels;
    m_markerContainerPanel.setMaxLineNumber(peakels.size());
    // hide RawFileName column
    m_table.getColumnExt(m_table.convertColumnIndexToView(FeaturesTableModel.COLTYPE_FEATURE_RAWFILE.getIndex())).setVisible(displayRawFileColumn);
  }

}

