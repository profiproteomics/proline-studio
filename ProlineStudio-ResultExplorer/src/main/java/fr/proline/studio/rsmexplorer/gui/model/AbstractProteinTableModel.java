/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */

    public abstract class AbstractProteinTableModel extends AbstractTableModel {

        protected static final int COLTYPE_PROTEIN_NAME  = 0;
        protected static final int COLTYPE_PROTEIN_SCORE = 1;

        private static final String[] columnNames = { "Proteins Groups", "Score" };
        
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }


        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLTYPE_PROTEIN_NAME:
                    return String.class;
                case COLTYPE_PROTEIN_SCORE:
                    return Float.class;
            }
            return null;
        }


}