package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.uds.QuantitationLabel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.studio.table.renderer.DefaultColoredCellRenderer;
import fr.proline.studio.utils.StringUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;

public class PurityCorrectionMatrixTableModel extends AbstractCorrectionMatrixTableModel {

    final QuantitationMethod m_quantMethod;

    int m_nbrReporters;

    boolean m_readOnly;
    public PurityCorrectionMatrixTableModel(QuantitationMethod quantMethod, boolean readOnly) {
        m_quantMethod = quantMethod;
        m_nbrReporters = m_quantMethod.getLabels().size();
        m_readOnly= readOnly;
        initData();
    }
    public PurityCorrectionMatrixTableModel(QuantitationMethod quantMethod) {
        this(quantMethod, false);
    }

    private void initData(){
        massReporters = new ArrayList<>();
        columnNames = new ArrayList<>();
        columnNames.add("Mass Reporter");
        for(QuantitationLabel l : m_quantMethod.getLabels()){
            MassReporter nextMR =new MassReporter(getReporterLabel(l), m_nbrReporters, new int[0]);
            massReporters.add(nextMR);
            columnNames.add(nextMR.getName());
        }
        valueCoeffColumWidth = 100;
   }

   /**
    * Specified matrix should be ordered in the same way as mass reporter!!!!
    */
   public void setData(Double[][] allReporterCoeff){

     if(allReporterCoeff == null) {
       JOptionPane.showMessageDialog(null, "Unable to set matrix data, no data specified !", "Matrix Error", JOptionPane.ERROR_MESSAGE);
       return;
     }

     if(allReporterCoeff.length != massReporters.size())
         JOptionPane.showMessageDialog(null,"Unable to set matrix data, not the same number of reporters !", "Matrix Error", JOptionPane.ERROR_MESSAGE );

//           throw new RuntimeException("Unable to set matrix data, not the same number of reporters !");

        for(int i =0 ; i< allReporterCoeff.length; i++){
            MassReporter mr = massReporters.get(i);
            Double[] coeff = allReporterCoeff[i];
            for(int j = 0; j<coeff.length; j++)
                mr.coef.set(j, coeff[j].floatValue());
        }
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

    @Override
    public boolean isCellEditable(int row, int col) {
       if(m_readOnly)
           return false;
        return super.isCellEditable(row, col);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
       if(m_readOnly ) {
           return DefaultColoredCellRenderer.disabledCellRendered;
       }
        return super.getRenderer(row, col);
    }
}
