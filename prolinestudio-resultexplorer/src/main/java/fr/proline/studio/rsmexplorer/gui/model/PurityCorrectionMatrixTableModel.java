package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.uds.QuantitationLabel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.studio.utils.StringUtils;

import java.util.ArrayList;

public class PurityCorrectionMatrixTableModel extends AbstractCorrectionMatrixTaleModel  {

    final QuantitationMethod m_quantMethod;

    int m_nbrReporters;

    public PurityCorrectionMatrixTableModel(QuantitationMethod quantMethod) {
        m_quantMethod = quantMethod;
        m_nbrReporters = m_quantMethod.getLabels().size();
        initData();
    }

    private void initData(){
        massReporters = new ArrayList<>();
        columnNames = new ArrayList<>();
        columnNames.add("Mass Reporter");
        for(QuantitationLabel l : m_quantMethod.getLabels()){
            massReporters.add(new MassReporter(l.getName(), m_nbrReporters+2, new int[0]));
            columnNames.add(l.getName());
        }
        valueCoeffColumWidth = 100;
   }
   @Override
    public int getCenterColIndex(){
        return getColumnCount()+1;
    }

    public String getPurityMatrixAsString() {
        StringBuilder sb = new StringBuilder("[");
        for(MassReporter mr : massReporters){
            sb.append(StringUtils.formatFloatList(mr.getCoefWithoutNan(), 4)).append(",");
        }
        sb.deleteCharAt(sb.length()-1).append("]");
        return sb.toString();
    }
}
