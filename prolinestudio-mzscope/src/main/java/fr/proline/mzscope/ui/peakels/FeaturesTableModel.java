/*
 * Copyright (C) 2019
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

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IPeakel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.table.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Table model for Features
 *
 * @author CB205360
 */
public class FeaturesTableModel extends PeakelsTableModel {

  public static final Column COLTYPE_FEATURE_CHARGE_COL = new Column("Charge", "Charge state", Integer.class, 8);
  public static final Column COLTYPE_FEATURE_PEAKELS_COUNT_COL = new Column("Peakels", "number of isotopes", Integer.class, 9);

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == COLTYPE_FEATURE_CHARGE_COL.getIndex()) {
      return ((IFeature) m_peakels.get(rowIndex)).getCharge();
    } else if (columnIndex == COLTYPE_FEATURE_PEAKELS_COUNT_COL.getIndex()) {
      return ((IFeature) m_peakels.get(rowIndex)).getPeakelsCount();
    }

    return super.getValueAt(rowIndex, columnIndex);
  }

  @Override
  public ArrayList<ExtraDataType> getExtraDataTypes() {
    ArrayList<ExtraDataType> list = new ArrayList<>();
    list.add(new ExtraDataType(Feature.class, true));
    registerSingleValuesAsExtraTypes(list);
    return list;
  }

  public void setFeatures(List<IFeature> features) {
    super.setPeakels(features.stream().map(f -> ((IPeakel)f)).collect(Collectors.toList()));
  }

  public void addFeatures(List<IFeature> features) {
    super.addPeakels(features.stream().map(f -> ((IPeakel)f)).collect(Collectors.toList()));
  }

}
