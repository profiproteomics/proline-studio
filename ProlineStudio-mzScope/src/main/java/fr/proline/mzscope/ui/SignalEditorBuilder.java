/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Signal;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotInformation;
import java.awt.Color;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.PlotDataSpec;

/**
 *
 * @author CB205360
 */
public class SignalEditorBuilder {
    
     static Container buildEditor(List<Signal> signals) {
        if (signals.size() == 1) {
            return new SignalEditorPanel(signals.get(0));
        } else {
            return new SignalMultipleEditorPanel(signals);
        }
    }
}

class SignalWrapper implements ExtendedTableModelInterface {

   private Signal m_signal;
   private String m_title;
   private Color m_color;
   
   public SignalWrapper(Signal signal, String title, Color color) {
      m_signal = signal;
      m_title = title;
      m_color = color;
   }
   
   @Override
   public int getRowCount() {
      return m_signal.getXSeries().length;
   }

   @Override
   public int getColumnCount() {
      return 2;
   }

   @Override
   public String getDataColumnIdentifier(int columnIndex) {
      return (columnIndex == 0) ? "X" : "Y";
   }

   @Override
   public Class getDataColumnClass(int columnIndex) {
      return Double.class;
   }

   @Override
   public Object getDataValueAt(int rowIndex, int columnIndex) {
      return (columnIndex == 0) ? m_signal.getXSeries()[rowIndex] : m_signal.getYSeries()[rowIndex];
   }

   @Override
   public int[] getKeysColumn() {
      return new int[]{0};
   }

   @Override
   public int getInfoColumn() {
      return 0;
   }

   @Override
   public void setName(String name) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public String getName() {
      return "Signal";
   }

   @Override
   public Map<String, Object> getExternalData() {
      return null;
   }

   @Override
   public PlotInformation getPlotInformation() {
        PlotInformation plotInformation = new PlotInformation();
        plotInformation.setPlotColor(m_color);
        plotInformation.setPlotTitle(m_title);
        plotInformation.setDrawPoints(false);
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
    public Object getValue(Class c) {
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addSingleValue(Object v) {
        
    }

    @Override
    public Object getSingleValue(Class c) {
        return null;
    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }
   
}

