/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 7 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.xic.alignment;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotInformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RT = elution time = retention time
 *
 * @author Karine XUE
 */
public class RTCompareTableModel implements ExtendedTableModelInterface {

    private String m_modelName;

    /**
     * Map: MapId,resultSummaryId
     */
    private Map<Long, Long> m_idMap;
    private long[] m_rsmIdArray;
    private List<RTCompareRow> m_data;

    public List<RTCompareRow> getData() {
        return m_data;
    }
    /**
     * Map : rsmId,MapTitleName
     */
    private Map<Long, String> m_idNameMap;
    private String[] m_columnName;
    private static int PEPTEDE_ID = 0;
    private static int PEPTEDE_SEQUENCE = 1;
    private static int CHARGE = 2;
    private static int ELUTION_TIME_FROM = 3;

    /**
     *
     * @param mapIdFrom
     * @param idTitleMap, Map<MapId,msFileName>
     * @param idMap, Map<MapId,resultSummaryId>
     * @param m_masterQuantPeptideIonList
     */
    public RTCompareTableModel(List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList,
            Map<Long, Long> idMap, Map<Long, String> idNameMap, long[] rsmIdArray) {
        m_idNameMap = idNameMap;
        m_idMap = idMap;
        m_rsmIdArray = rsmIdArray;
        m_data = new ArrayList<RTCompareRow>();
        m_columnName = new String[rsmIdArray.length + 3];
        m_columnName[PEPTEDE_ID] = "Peptide Id";//[0]
        m_columnName[PEPTEDE_SEQUENCE] = "Peptide Seqence";//[1]
        m_columnName[CHARGE] = "Charge";//[2]
        m_columnName[ELUTION_TIME_FROM] = "Time in Map "+m_idNameMap.get(rsmIdArray[0])+" (min)";
        for (int i = 4; i < rsmIdArray.length + 3; i++) {
            m_columnName[i] ="Delta time in Map "+ m_idNameMap.get(rsmIdArray[i - 3])+ " (s)";
        }
      
        long peptideId;
        String peptideSequence;
        int charge;
        float rTimeFrom;
        float[] rTimeTo;
        DPeptideInstance pi;
        Map<Long, DQuantPeptideIon> nbPepInMap; //Map<key=rsmId<>mapId, DQuantPeptideIon>
        for (DMasterQuantPeptideIon masterPep : m_masterQuantPeptideIonList) {
            rTimeTo = new float[rsmIdArray.length - 1];
            pi = masterPep.getPeptideInstance();
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
                DQuantPeptideIon quantPeptideIon;
                for (int i = 1; i < rsmIdArray.length; i++) {//0 is from
                    quantPeptideIon = nbPepInMap.get(rsmIdArray[i]);
                    if (quantPeptideIon == null) {
                        rTimeTo[i - 1] = Float.NaN; //not a number, then show nothing in the PlotLiner
                    } else {
                        float deltaTime = quantPeptideIon.getElutionTime() - rTimeFrom;
                         rTimeTo[i - 1] = deltaTime;
//                        if (Math.abs(deltaTime) > 120) { //@todo debug use
//                            eTimeTo[i - 1] = Float.NaN;
//                        } else {
//                            eTimeTo[i - 1] = deltaTime;
//                        }
                    }
                }
            }
            this.m_data.add(new RTCompareRow(peptideId, peptideSequence, charge, rTimeFrom, rTimeTo));
        }
    }


    public String getInfo(int rowIndex) {
        return this.m_data.get(rowIndex).toString();

    }

    protected class RTCompareRow {

        long _peptideId;
        String _peptideSequence;
        int _charge;
        float _eTimeFrom;
        float[] _eTimeTo;

        public RTCompareRow(long peptideId, String peptideSequence, int charge, float eTimeFrom, float[] eTimeTo) {
            this._peptideId = peptideId;
            this._peptideSequence = peptideSequence;
            this._charge = charge;
            this._eTimeFrom = eTimeFrom;
            this._eTimeTo = eTimeTo;
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
            case 0://MapRTCompareTableModel.PEPTEDE_ID:
                return Long.class;
            case 1://MapRTCompareTableModel.PEPTEDE_SEQUENCE:
                return String.class;
            case 2://MapRTCompareTableModel.CHARGE:
                return Integer.class;
            default:
                return Float.class;
        }
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        RTCompareRow row = this.m_data.get(rowIndex);
        if (columnIndex == RTCompareTableModel.PEPTEDE_ID) {
            return row._peptideId;
        } else if (columnIndex == RTCompareTableModel.PEPTEDE_SEQUENCE) {
            return row._peptideSequence;
        } else if (columnIndex == RTCompareTableModel.CHARGE) {
            return row._charge;
        } else if (columnIndex == RTCompareTableModel.ELUTION_TIME_FROM) {
            return row._eTimeFrom / 60d; //in minute
        } else {
            return row._eTimeTo[columnIndex - 4]; //in seconde
        }
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
        return RTCompareTableModel.PEPTEDE_SEQUENCE;
    }

    public String getToolTipInfo(int rowIndex) {
        RTCompareRow row = this.m_data.get(rowIndex);
        String infoValue = " Peptied id : " + row._peptideId + "<BR>";
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
