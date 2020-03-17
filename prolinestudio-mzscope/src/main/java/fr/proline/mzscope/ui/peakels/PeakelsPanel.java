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

import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumData;
import fr.proline.mzscope.model.IPeakel;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.processing.PeakelsHelper;
import fr.proline.mzscope.ui.IMzScopeController;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import javax.swing.event.RowSorterListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    return toolbar;
  }

  private void buildSpectrum() {
    List<IPeakel> peakels = getSelectedIPeakels();
    Peakel peakel = peakels.get(0).getPeakel();
    PeakelsHelper helper = getPeakelsHelper();
    List<Peakel> coelutingPeakels = helper.findCoelutigPeakels(peakel.getApexMz() - 5,
            peakel.getApexMz() + 5,
            peakel.getFirstElutionTime(),
            peakel.getLastElutionTime());
    SpectrumData spectrumData = helper.buildSpectrumDataFromPeakels(peakel, coelutingPeakels);
    Spectrum spectrum = new Spectrum(-1, peakel.getApexElutionTime(), spectrumData.getMzList(), spectrumData.getIntensityList(), 1, Spectrum.ScanType.CENTROID);
    m_viewersController.getRawFileViewer(peakels.get(0).getRawFile(), true).setReferenceSpectrum(spectrum);
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

