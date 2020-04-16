/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Jean-Philippe
 */
public class LogAdapterModel implements ExtendedTableModelInterface {

    private ExtendedTableModelInterface m_innerModel = null;
    private int m_colX;
    private int m_colY;
    
    public enum POLICY {
        REMOVE_INCORRECT_VALUES,
        REPLACE_BY_VALUE
    }
    private POLICY m_policy;
    private Double m_replacementValue;
    
    private double[] m_dataX;
    private double[] m_dataY;
    private boolean[] m_error;
    private int[] m_row2sourceRowConversion;
    private int[] m_sourceRow2Conversion;
    private int m_rowCount;
    
    public LogAdapterModel(ExtendedTableModelInterface innerModel) {
        m_innerModel = innerModel;
    }
    
    public void update(POLICY policy, int colX, int colY, Double replacementValue) {
        m_policy = policy;
        m_colX = colX;
        m_colY = colY;
        m_replacementValue = replacementValue;
        
        int nb = m_innerModel.getRowCount();
        m_dataX = new double[nb];
        m_dataY = new double[nb];
        m_error = (m_policy == POLICY.REPLACE_BY_VALUE) ? new boolean[nb] : null;
        m_row2sourceRowConversion = new int[nb];
        m_sourceRow2Conversion = new int[nb];
        
        int row  = 0;
        for (int i=0;i<nb;i++) {
            
            Double x = null;
            if (colX != -1) {
                Object value = m_innerModel.getDataValueAt(i, colX);
                x = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); 
                if (x<=0) {
                    x = Double.NaN;
                }
            }
            
            Double y = null;
            if (colY != -1) {
                Object value = m_innerModel.getDataValueAt(i, colY);
                y = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); 
                
                //JPM.TEST.LOG !!!!
                /*if (i == 50) {
                  y = -100d;  
                } */
                
                if (y<=0) {
                    y = Double.NaN;
                }
            }
            

            boolean error = false;
            if ((colX!=-1) && (x.isNaN())) {
                error = true;
                if (m_policy == POLICY.REPLACE_BY_VALUE) {
                    x = m_replacementValue;
                }
            }
            if ((colY!=-1) && (y.isNaN())) {
                error = true;
                if (m_policy == POLICY.REPLACE_BY_VALUE) {
                    y = m_replacementValue;
                }
            }
            if (error && m_policy == POLICY.REMOVE_INCORRECT_VALUES) {
                continue; // squeeze this value
            }
            
            if (m_policy == POLICY.REPLACE_BY_VALUE) {
                m_error[row] = error;
            }
            
            if (colX != -1) m_dataX[row] = x;
            if (colY != -1) m_dataY[row] = y;
            m_row2sourceRowConversion[row] = i;
            m_sourceRow2Conversion[row] = i;
            row++;
        }
        m_rowCount = row;
        
        
    }
    
    public ExtendedTableModelInterface getInnerModel() {
        return m_innerModel;
    }
    
    @Override
    public int getRowCount() {
        return m_rowCount;
    }
    
    public boolean isOnError(int rowIndex) {
        if (m_policy == POLICY.REMOVE_INCORRECT_VALUES) {
            return false;
        }
        return m_error[rowIndex];
    }

    @Override
    public int getColumnCount() {
        return m_innerModel.getColumnCount();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_innerModel.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if ((m_colX == columnIndex) || (m_colY == columnIndex)) {
            return Double.class;
        }
        return m_innerModel.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (m_colX == columnIndex) {
            return m_dataX[rowIndex];
        }
        if (m_colY == columnIndex) {
            return m_dataY[rowIndex];
        }
        return m_innerModel.getDataValueAt(m_row2sourceRowConversion[rowIndex], columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return m_innerModel.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_innerModel.getInfoColumn();
    }

    @Override
    public void setName(String name) {
        m_innerModel.setName(name);
    }

    @Override
    public String getName() {
        return  m_innerModel.getName();
    }

    @Override
    public Map<String, Object> getExternalData() {
        return  m_innerModel.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return  m_innerModel.getPlotInformation();
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        return  m_innerModel.row2UniqueId(m_row2sourceRowConversion[rowIndex]);
    }

    @Override
    public int uniqueId2Row(long id) {
        return  m_sourceRow2Conversion[m_innerModel.uniqueId2Row(id)];
    }
    

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return  m_innerModel.getDataSpecAt(i);
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return  m_innerModel.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        return m_innerModel.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return m_innerModel.getRowValue(c, row);
    }

    @Override
    public Object getColValue(Class c, int col) {
        return m_innerModel.getColValue(c, col);
    }

    @Override
    public void addSingleValue(Object v) {
        m_innerModel.addSingleValue(v);
    }

    @Override
    public Object getSingleValue(Class c) {
       return m_innerModel.getSingleValue(c);
    }
    
}
