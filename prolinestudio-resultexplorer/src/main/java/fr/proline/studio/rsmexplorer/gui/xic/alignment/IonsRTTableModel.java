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
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RT = elution time = retention time. The table is organized as follows:
 * |PEPTIDE_ID|PEPTIDE_SEQUENCE|CHARGE|ELUTION_TIME_FROM|_eTimeTo[0]|_eTimeTo[1]|....|_eTimeTo[n]|
 *
 * @author Karine XUE
 */
public class IonsRTTableModel implements ExtendedTableModelInterface {

    private String m_modelName;

    /**
     * Map: MapId,resultSummaryId
     */
    private Map<Long, Long> m_idMap;
    private long[] m_rsmIdArray;
    private List<IonRTRow> m_data;

    public List<IonRTRow> getData() {
        return m_data;
    }
    /**
     * Map : rsmId,MapTitleName
     */
    private Map<Long, String> m_idNameMap;
    private String[] m_columnName;
    private static int PEPTIDE_ID = 0;
    private static int PEPTIDE_SEQUENCE = 1;
    private static int CHARGE = 2;
    private static int ELUTION_TIME_FROM = 3;
    private int m_mapCount;

    /**
     *
     * @param mapIdFrom
     * @param idTitleMap, Map<MapId,msFileName>
     * @param idMap, Map<MapId,resultSummaryId>
     * @param m_masterQuantPeptideIonList
     */
    public IonsRTTableModel(List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList,
                            Map<Long, Long> idMap, Map<Long, String> idNameMap, long[] rsmIdArray) {
        m_mapCount = rsmIdArray.length;
        m_idNameMap = idNameMap;
        m_idMap = idMap;
        m_rsmIdArray = rsmIdArray;
        m_data = new ArrayList<>();
        m_columnName = new String[rsmIdArray.length + 3];
        m_columnName[PEPTIDE_ID] = "Peptide Id";//[0]
        m_columnName[PEPTIDE_SEQUENCE] = "Peptide Sequence";//[1]
        m_columnName[CHARGE] = "Charge";//[2]
        m_columnName[ELUTION_TIME_FROM] = "Time in Map " + m_idNameMap.get(rsmIdArray[0]) + " (min)";
        for (int i = 4; i < rsmIdArray.length + 3; i++) {
            m_columnName[i] = "Delta time in Map " + m_idNameMap.get(rsmIdArray[i - 3]) + " (s)";
        }

        long peptideId;
        String peptideSequence;
        int charge;
        float rTimeFrom;
        float[] rTimeTo;
        int matchCountFrom;
        int[] matchCountTo;
        DPeptideInstance pi;
        Map<Long, DQuantPeptideIon> nbPepInMap; //Map<key=rsmId<>mapId, DQuantPeptideIon>
        for (DMasterQuantPeptideIon masterPep : m_masterQuantPeptideIonList) {
            rTimeTo = new float[m_mapCount - 1];
            matchCountTo = new int[rTimeTo.length];
            pi = masterPep.getPeptideInstance();
            if (pi == null)
                continue;
            peptideId = pi.getPeptideId();
            peptideSequence = pi.getPeptide().getSequence();
            charge = masterPep.getCharge();
            nbPepInMap = masterPep.getQuantPeptideIonByQchIds();
            if (nbPepInMap == null || nbPepInMap.isEmpty()) {
                continue;
            } else {
                Long rsmIdFrom = rsmIdArray[0];
                this.m_modelName = idNameMap.get(rsmIdFrom) + " ElutionTime compare table model";
                DQuantPeptideIon element = nbPepInMap.get(rsmIdFrom);
                if (element == null) {
                    continue;
                }
                rTimeFrom = element.getElutionTime();
                matchCountFrom = element.getPeptideMatchesCount();
                DQuantPeptideIon quantPeptideIon;
                for (int i = 1; i < rsmIdArray.length; i++) {//0 is from
                    quantPeptideIon = nbPepInMap.get(rsmIdArray[i]);
                    if (quantPeptideIon == null) {
                        rTimeTo[i - 1] = Float.NaN; //not a number, then show nothing in the PlotLiner
                    } else {
                        float deltaTime = quantPeptideIon.getElutionTime() - rTimeFrom;
                        rTimeTo[i - 1] = deltaTime;
                        matchCountTo[i - 1] = quantPeptideIon.getPeptideMatchesCount();
                    }
                }
            }
            this.m_data.add(new IonRTRow(peptideId, peptideSequence, charge, rTimeFrom, rTimeTo, matchCountFrom, matchCountTo));
        }
    }

    public String getInfo(int rowIndex) {
        return this.m_data.get(rowIndex).toString();

    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }

    protected class IonRTRow {

        long _peptideId;
        String _peptideSequence;
        int _charge;
        float _eTimeFrom;
        float[] _eTimeTo;
        int _MatchCountFrom;
        int[] _MatchCountTo;

        public IonRTRow(long peptideId, String peptideSequence, int charge, float eTimeFrom, float[] eTimeTo, int matchCountFrom, int[] matchCountTo) {
            this._peptideId = peptideId;
            this._peptideSequence = peptideSequence;
            this._charge = charge;
            this._eTimeFrom = eTimeFrom;
            this._eTimeTo = eTimeTo;

            this._MatchCountFrom = matchCountFrom;
            this._MatchCountTo = matchCountTo;
        }

        public String toString() {
            String s = "";
            for (float f : this._eTimeTo) {
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
            case 0://MapRTCompareTableModel.PEPTIDE_ID:
                return Long.class;
            case 1://MapRTCompareTableModel.PEPTIDE_SEQUENCE:
                return String.class;
            case 2://MapRTCompareTableModel.CHARGE:
                return Integer.class;
            default:
                return Float.class;
        }
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        IonRTRow row = this.m_data.get(rowIndex);
        if (columnIndex == IonsRTTableModel.PEPTIDE_ID) {
            return row._peptideId;
        } else if (columnIndex == IonsRTTableModel.PEPTIDE_SEQUENCE) {
            return row._peptideSequence;
        } else if (columnIndex == IonsRTTableModel.CHARGE) {
            return row._charge;
        } else if (columnIndex == IonsRTTableModel.ELUTION_TIME_FROM) {
            return row._eTimeFrom / 60d; //in minute
        } else if (columnIndex > 3 && columnIndex < this.m_mapCount + 3) {
            return row._eTimeTo[columnIndex - 4]; //in seconde
        } else if (columnIndex == 3 + this.m_mapCount) {
            return row._MatchCountTo;
        } else {
            return row._MatchCountTo[columnIndex - 4 - this.m_mapCount];
        }
    }

    public boolean isCrossAssigned(int rowIndex, int colY) {
        IonRTRow row = this.m_data.get(rowIndex);
        int countFrom = row._MatchCountFrom;
        int countTo = row._MatchCountTo[colY - 4];
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
        long rsmId = this.m_idMap.get(mapId);
        for (int i = 0; i < m_rsmIdArray.length; i++) {
            if (m_rsmIdArray[i] == rsmId) {
                return i + 3;
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
        IonRTRow row = this.m_data.get(rowIndex);
        String infoValue = " Peptide id : " + row._peptideId + "<BR>";
        infoValue += ("Sequence : ") + row._peptideSequence + "<BR>";
        infoValue += ("charge : ") + row._charge + "<BR>";
        return infoValue;
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
