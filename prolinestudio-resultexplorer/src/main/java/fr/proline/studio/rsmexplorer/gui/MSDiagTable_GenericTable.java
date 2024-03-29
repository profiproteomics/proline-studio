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
package fr.proline.studio.rsmexplorer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font; 

import javax.swing.JTable;

import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData.TheoreticalFragmentSeries;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TablePopupMenu;
import javax.swing.table.TableCellRenderer;
 

	/**
	* Created by AW
	*/

public class MSDiagTable_GenericTable extends DecoratedTable {

	private static final long serialVersionUID = 1L;
	

	    private MSDiagTableCustomRenderer m_matrixRenderer;
	    private String m_outputType = "";


		private RsetMSDiagPanel m_msdiagPanel;

	    
	    public MSDiagTable_GenericTable(RsetMSDiagPanel rsetMSDiagPanel) {
	        m_msdiagPanel = rsetMSDiagPanel;
	        MSdiagTable_GenericTableModel msDiagTableModel1 = new MSdiagTable_GenericTableModel();

	        m_matrixRenderer = new MSDiagTableCustomRenderer();
	        setModel(msDiagTableModel1);
	        setDefaultRenderer(Object.class, m_matrixRenderer);
	        setSortable(false);
	        
	    }




	    public void setData(MSDiagOutput_AW msdo) {
	 
	         
	        MSdiagTable_GenericTableModel msDiagTableModel1 = ((MSdiagTable_GenericTableModel) getModel());
	        
	        if (msdo == null) {
	            msDiagTableModel1.reinitData();
	           

	        } else {
	        	//m_outputType = msdo.output_typeZZ.name();
	            msDiagTableModel1.setData(msdo);
	            
	        }


	    }



	    public static class MSdiagTable_GenericTableModel extends DecoratedTableModel {

	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
	        private int m_nbRows;
	        private String[][] m_matrix;
	        private Object[][] m_matrixValues;
	        private String[] m_columnNames;

	        public MSdiagTable_GenericTableModel() { // constructor
	            initData();
	        }

	        public void reinitData() {
	            initData();
	            fireTableStructureChanged();
	                    
	        }
	        
	        private void initData() {
	           
	            m_nbRows = 0;
	            m_matrix = null;
	            m_matrixValues = null;
	            m_columnNames = null;
	        }
	        
	        
	        public void setData(MSDiagOutput_AW msdo) {
	            
	            initData();
	            
	            if(msdo != null)
				{
	            	  m_columnNames =  msdo.column_names;
			          m_matrixValues = msdo.matrix;
			          m_nbRows = msdo.matrix.length;
				          
				          
				     fireTableStructureChanged();
				}
	        }

	        public String[][] getMatrix() {
	            return m_matrix;
	        }

	        @Override
	        public String getColumnName(int col) {
	            return m_columnNames[col];
	        }

	        @Override
	        public String getToolTipForHeader(int col) {
	            return getColumnName(col);
	        }
                
                @Override
                public String getTootlTipValue(int row, int col) {
                    return null;
                }

	        @Override
	        public int getRowCount() {
	            return m_nbRows;
	        }

	        @Override
	        public int getColumnCount() {
	            if (m_columnNames == null) {
	                return 0;
	            }
	            return m_columnNames.length;
	        }

	        @Override
	        public Class getColumnClass(int columnIndex) {
	            if (columnIndex == 0) {
	                return String.class; // assume first column is always a String type. (even if data is a number)
	            }
	            else {
	                return Double.class;
	            }
	        
	        }
	        @Override
	        public Object getValueAt(int rowIndex, int columnIndex) {
				
	        	return m_matrixValues[rowIndex][columnIndex];
	        	
	        	

	        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return null;
        }

	    }

	    public static class MSDiagTableCustomRenderer extends org.jdesktop.swingx.renderer.DefaultTableRenderer {
	        
	        private static final long serialVersionUID = 1L;
	        private String[][] m_selectMatrix = new String[100][100];
	        private Font m_fontPlain = null;
	        private Font m_fontBold = null;
	        private final static Color LIGHT_BLUE_COLOR = new Color(51, 153, 255);
	        private final static Color LIGHT_RED_COLOR = new Color(255, 85, 85);
	        private final static Color EXTRA_LIGHT_BLUE_COLOR = new Color(175, 255,255);
	        private final static Color EXTRA_LIGHT_RED_COLOR = new Color(255, 230,230);

	        void setSelectMatrix(String[][] matx) {
	            m_selectMatrix = matx;
	        }

	        @Override
	        public Component getTableCellRendererComponent(JTable table,
	                Object value, boolean isSelected, boolean hasFocus, int row,
	                int column) {
	            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	            // prepare needed fonts
	            if (m_fontPlain == null) {
	                m_fontPlain = component.getFont().deriveFont(Font.PLAIN);
	                m_fontBold = m_fontPlain.deriveFont(Font.BOLD);
	            }

	            // select font
	            if (m_selectMatrix[row][column] != null) {
	                component.setFont(m_fontBold);
	            } else {
	                component.setFont(m_fontPlain);
	            }

	            // select color
	            Color foregroundColor;

	            if (m_selectMatrix[row][column] != null) {

	                if (m_selectMatrix[row][column].contains("ABC")) { // highlight
	                    // the cell
	                    // if true
	                    // in
	                    // selectMatrix
	                    foregroundColor = (isSelected) ? EXTRA_LIGHT_BLUE_COLOR : LIGHT_BLUE_COLOR;
	                } else if (m_selectMatrix[row][column].contains("XYZ")) {
	                    foregroundColor = (isSelected) ? EXTRA_LIGHT_RED_COLOR
	                            : LIGHT_RED_COLOR;
	                } else {
	                    foregroundColor = (isSelected) ? Color.white : Color.black;
	                }
	            } else {
	                // standard color:
	                foregroundColor = (isSelected) ? Color.white : Color.black;
	            }

	            component.setForeground(foregroundColor);

	            return component;

	        }
	    }
            
    @Override
    public TablePopupMenu initPopupMenu() {
        return null;
    }

    // set as abstract
    @Override
    public void prepostPopupMenu() {
        // nothing to do
    }
	
}
