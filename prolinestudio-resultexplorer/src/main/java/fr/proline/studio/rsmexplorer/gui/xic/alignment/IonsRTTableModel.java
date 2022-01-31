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
package fr.proline.studio.rsmexplorer.gui.xic.alignment;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
import fr.proline.studio.corewrapper.util.PeptideClassesUtils;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Represent Peptide Ion information in Map alignment context, for a specified Source Map
 *
 * RT = elution time = retention time. The table is organized as follows:
 * |PEPTIDE_ID|PEPTIDE_SEQUENCE|CHARGE|MOZ|DELTA_MOZ|ELUTION_TIME_FROM|_eTimeTo[0]|_eTimeTo[1]|....|_eTimeTo[n]|
 *
 * @author Karine XUE
 */
public class IonsRTTableModel implements ExtendedTableModelInterface {

    private String m_modelName;

    private Map<Long, Long> m_rsmIdByMapId;
    private long[] m_rsmIdArray;
    private List<IonRTMoZRow> m_data;

    private Map<Long, String> m_mapTitleByRsmId;
    private String[] m_columnName;
    private static final int PEPTIDE_ID = 0;
    private static final int PEPTIDE_SEQUENCE = 1;
    private static final int CHARGE = 2;
    private static final int MOZ = 3;
    private static final int DELTA_MOZ = 4;
    private static final int ELUTION_TIME_FROM = 5;
    private static final int START_ELUTION_TO = 6;
    private int m_mapCount;

    public static final int MOZ_COL_INDEX = MOZ;
    public static final int ELUTION_TIME_FROM_COL_INDEX = ELUTION_TIME_FROM;
    public static final int DELTA_MOZ_COL_INDEX = DELTA_MOZ;

    /**
     *
     * @param mapTitleByRsmId: Map<MapId,msFileName>
     * @param rsmIdByMapId: Map<MapId,resultSummaryId>
     * @param rsmIdArray
     */
    public IonsRTTableModel(List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList,
                            Map<Long, Long> rsmIdByMapId, Map<Long, String> mapTitleByRsmId, long[] rsmIdArray) {
        m_mapCount = rsmIdArray.length;
        m_mapTitleByRsmId = mapTitleByRsmId;
        m_rsmIdByMapId = rsmIdByMapId;
        m_rsmIdArray = rsmIdArray;
        m_data = new ArrayList<>();
        m_columnName = new String[rsmIdArray.length + ELUTION_TIME_FROM];
        m_columnName[PEPTIDE_ID] = "Peptide Id";
        m_columnName[PEPTIDE_SEQUENCE] = "Peptide Sequence";
        m_columnName[CHARGE] = "Charge";
        m_columnName[MOZ] = "moz";
        m_columnName[DELTA_MOZ] = "Delta moz";
        m_columnName[ELUTION_TIME_FROM] = "Time in Map " + m_mapTitleByRsmId.get(rsmIdArray[0]) + " (min)";
        for (int i = START_ELUTION_TO; i < rsmIdArray.length + ELUTION_TIME_FROM; i++) {
            m_columnName[i] = "Delta time in Map " + m_mapTitleByRsmId.get(rsmIdArray[i - ELUTION_TIME_FROM]) + " (s)";
        }

        float rTimeFrom;
        float[] rTimeTo;
        int matchCountFrom;
        int[] matchCountTo;
        double deltaMoz;
        DPeptideInstance peptideInstance;

        Map<Long, DQuantPeptideIon> qPepIonByQCId; //Map<key=rsmId<>mapId, DQuantPeptideIon>
        for (DMasterQuantPeptideIon masterQuantPeptideIon : m_masterQuantPeptideIonList) {
            rTimeTo = new float[m_mapCount - 1];
            matchCountTo = new int[rTimeTo.length];
            peptideInstance = masterQuantPeptideIon.getPeptideInstance();
            if (peptideInstance == null)
                continue;

            qPepIonByQCId = masterQuantPeptideIon.getQuantPeptideIonByQchIds();
            if (qPepIonByQCId == null || qPepIonByQCId.isEmpty()) {
                continue;
            } else {
                Long rsmIdFrom = rsmIdArray[0];
                m_modelName = mapTitleByRsmId.get(rsmIdFrom) + " ElutionTime compare table model";
                DQuantPeptideIon srcQPepIon = qPepIonByQCId.get(rsmIdFrom);
                if (srcQPepIon == null) {
                    continue;
                }
                rTimeFrom = srcQPepIon.getElutionTime();
                matchCountFrom = srcQPepIon.getPeptideMatchesCount();
                DQuantPeptideIon destQPepIon;
                for (int i = 1; i < rsmIdArray.length; i++) {//0 is from
                    destQPepIon = qPepIonByQCId.get(rsmIdArray[i]);
                    if (destQPepIon == null) {
                        rTimeTo[i - 1] = Float.NaN; //not a number, then show nothing in the PlotLiner
                    } else {
                        float deltaTime = destQPepIon.getElutionTime() - rTimeFrom;
                        rTimeTo[i - 1] = deltaTime;
                        matchCountTo[i - 1] = destQPepIon.getPeptideMatchesCount();
                    }
                }
            }
            deltaMoz =  Double.NaN;
            if( masterQuantPeptideIon.getPeptideInstance() != null && masterQuantPeptideIon.getPeptideInstance().getPeptide() != null  && masterQuantPeptideIon.getRepresentativePepMatch() != null)
                deltaMoz =  PeptideClassesUtils.getPPMFor(masterQuantPeptideIon.getRepresentativePepMatch(),masterQuantPeptideIon.getPeptideInstance().getPeptide() );
            m_data.add(
                    new IonRTMoZRow(peptideInstance.getPeptideId(),
                                 peptideInstance.getPeptide().getSequence(),
                                 masterQuantPeptideIon.getCharge(),
                                 masterQuantPeptideIon.getMoz(),
                                 deltaMoz,
                                 rTimeFrom,
                                 rTimeTo,
                                 matchCountFrom,
                                 matchCountTo)
            );
        }
    }

    public List<IonRTMoZRow> getData() {
        return m_data;
    }

    public String getInfo(int rowIndex) {
        return m_data.get(rowIndex).toString();
    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }

    protected class IonRTMoZRow {

        long _peptideId;
        String _peptideSequence;
        int _charge;
        float _eTimeFrom;
        float[] _deltaTimeTo;
        int _MatchCountFrom;
        int[] _MatchCountTo;
        double _moz;
        double _deltaMoz;

        public IonRTMoZRow(long peptideId, String peptideSequence, int charge, double moz, double delaMoz,  float eTimeFrom, float[] eTimeTo, int matchCountFrom, int[] matchCountTo) {
            this._peptideId = peptideId;
            this._peptideSequence = peptideSequence;
            this._charge = charge;
            this._eTimeFrom = eTimeFrom;
            this._deltaTimeTo = eTimeTo;
            this._moz = moz;
            this._deltaMoz = delaMoz;

            this._MatchCountFrom = matchCountFrom;
            this._MatchCountTo = matchCountTo;
        }

        public String toString() {
            String s = "";
            for (float f : this._deltaTimeTo) {
                s += ";" + f;
            }
            return _peptideId + ";" + _peptideSequence + ";" + _charge + ";" + _eTimeFrom + s;
        }
    }

    @Override
    public int getRowCount() {
        return this.m_data.size();
    }

    @Override
    public int getColumnCount() {
        return this.m_columnName.length;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return this.m_columnName[columnIndex];
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case PEPTIDE_ID://MapRTCompareTableModel.PEPTIDE_ID:
                return Long.class;
            case PEPTIDE_SEQUENCE://MapRTCompareTableModel.PEPTIDE_SEQUENCE:
                return String.class;
            case CHARGE://MapRTCompareTableModel.CHARGE:
                return Integer.class;
            case MOZ:
            case DELTA_MOZ:
                return Double.class;
            default:
                return Float.class;
        }
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        IonRTMoZRow row = m_data.get(rowIndex);
        switch (columnIndex) {
            case PEPTIDE_ID:
                return row._peptideId;

            case PEPTIDE_SEQUENCE:
                return row._peptideSequence;

            case CHARGE:
                return row._charge;

            case MOZ:
                return row._moz;

            case DELTA_MOZ:
                return row._deltaMoz;

            case ELUTION_TIME_FROM:
                return row._eTimeFrom / 60d; //in minute

            default: {
                if (columnIndex > ELUTION_TIME_FROM && columnIndex < m_mapCount + ELUTION_TIME_FROM) {
                    return row._deltaTimeTo[columnIndex - START_ELUTION_TO]; //in second
                } else if (columnIndex == ELUTION_TIME_FROM + m_mapCount) {
                    return row._MatchCountTo;
                } else {
                    return row._MatchCountTo[columnIndex - START_ELUTION_TO - m_mapCount];
                }
            }
        }
    }

    public boolean isCrossAssigned(int rowIndex, int colY) {
        IonRTMoZRow row = m_data.get(rowIndex);
        int countFrom = row._MatchCountFrom;
        int countTo = colY < START_ELUTION_TO ? 1 : row._MatchCountTo[colY - START_ELUTION_TO]; // if not compare to other map, just test countFrom
        return (countFrom == 0 || countTo == 0);
    }

    /**
     * from XIC map id, to find the result summary id, then find the column
     * index in the table
     *
     * @param mapId, XIC map id
     * @return
     */
    public int getColumnIndex(Long mapId) {
        long rsmId = m_rsmIdByMapId.get(mapId);
        for (int i = 0; i < m_rsmIdArray.length; i++) {
            if (m_rsmIdArray[i] == rsmId) {
                return i + ELUTION_TIME_FROM;
            }
        }
        return -1;
    }

    @Override
    public int[] getKeysColumn() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getInfoColumn() {
        return IonsRTTableModel.PEPTIDE_SEQUENCE;
    }

    /**
     * return the information of the peptide id, sequence and charge
     * @param rowIndex
     * @return 
     */
    public String getToolTipInfo(int rowIndex) {
        IonRTMoZRow row = m_data.get(rowIndex);
        StringBuilder infoValueSB = new StringBuilder(" Peptide id : ");
        infoValueSB.append(row._peptideId).append("<BR>");
        infoValueSB.append("Sequence : ").append(row._peptideSequence).append("<BR>");
        infoValueSB.append("charge : ").append(row._charge).append( "<BR>");
        infoValueSB.append("moz: ").append(row._moz).append(" dmoz: ").append(row._deltaMoz).append( "<BR>");
        return infoValueSB.toString();
    }

    @Override
    public void setName(String name) {
        this.m_modelName = name;
    }

    @Override
    public String getName() {
        return this.m_modelName;
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        PlotInformation plotInformation = new PlotInformation();
        //plotInformation.setPlotColor(null);
        plotInformation.setPlotTitle("");
        plotInformation.setDrawPoints(true);
        plotInformation.setDrawGap(true);
        return plotInformation;
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }

    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {//for ExtraData
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {//for ExtraData
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {//for ExtraData
        return null;
    }

    @Override
    public void addSingleValue(Object v) {//for ExtraData
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getSingleValue(Class c) {//for ExtraData
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
