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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dam.memory.MemoryReference;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.swingx.JXTable;

/**
 *
 * Visualize unused and used TransientData memory blocks to be able to free them
 * 
 * @author Jean-Philippe
 */
public class MemoryDialog extends DefaultDialog  {

    private static MemoryDialog m_singletonDialog = null;

    private MemoryTable m_usedTable = null;
    private MemoryTable m_freeTable = null;
    
    
    
    public static MemoryDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new MemoryDialog(parent);
        } 
        return m_singletonDialog;
    }

    private MemoryDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Memory Management");

        setSize(new Dimension(640, 480));
        setMinimumSize(new Dimension(400, 360));
        setResizable(true);

        //setDocumentationSuffix("h.gktawz9n942e");

        setButtonName(BUTTON_OK, "Free Memory");

        initInternalPanel();

    }
    
    private void update() {
               
        ArrayList<MemoryReference> freeCacheList = TransientMemoryCacheManager.getSingleton().getFreeCacheList();
        MemoryTableModel freeModel = new MemoryTableModel(true, freeCacheList);
        m_freeTable.setModel(freeModel);
        

        ArrayList<MemoryReference> usedCacheList = TransientMemoryCacheManager.getSingleton().getUsedCacheList();
        MemoryTableModel usedModel = new MemoryTableModel(false, usedCacheList);
        m_usedTable.setModel(usedModel); 
    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JTabbedPane tabbedPane = new JTabbedPane(); 
        

        m_freeTable = new MemoryTable();
        JScrollPane freeListScrollPane = new JScrollPane(m_freeTable);


        m_usedTable = new MemoryTable();       
        JScrollPane usedListScrollPane = new JScrollPane(m_usedTable);

        tabbedPane.add("Recoverable Memory", freeListScrollPane);
        tabbedPane.add("Used Memory", usedListScrollPane);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(tabbedPane, c);

        setInternalComponent(internalPanel);

    }

    /*@Override
    public void pack() {
        // forbid pack by overloading the method
    }*/

    @Override
    public void setVisible(boolean b) {
        update();
        super.setVisible(b);
    }
    
    @Override
    protected boolean okCalled() {
        
        // Free unused memory caches
        TransientMemoryCacheManager.getSingleton().freeUnusedCache();
        
        // Remove data nodes in Data Analyzer
        DataAnalyzerWindowBoxManager.updateToFreeMemory();
        
        // call garbage collector
        System.gc(); 
        
        return true;
    }

    
    private class MemoryTable extends JXTable {
        
        
        public MemoryTable() {
            setSortable(false);
        }
    }
    
    private static class MemoryTableModel extends AbstractTableModel {

        private static final int COLUMN_CACHE = 0;
        private static final int COLUMN_WINDOW = 1;
        private static final int NB_COLS = 2;

        private static final String[] FREE_COLUMN_NAMES = {"Cache", "Was Used in Window"};
        private static final String[] USED_COLUMN_NAMES = {"Cache", "Used in Window"};
        
        private ArrayList<MemoryReference> m_memoryReferenceList = null;
        private String[] m_columnNames;

        public MemoryTableModel(boolean freeCacheModel, ArrayList<MemoryReference> memoryReferenceList) {
            m_memoryReferenceList = memoryReferenceList;
            m_columnNames = freeCacheModel ? FREE_COLUMN_NAMES : USED_COLUMN_NAMES;
        }

        public String getColumnName(int column) {
            return m_columnNames[column];
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public int getRowCount() {
            return m_memoryReferenceList.size();
        }

        public int getColumnCount() {
            return NB_COLS;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case COLUMN_CACHE: {
                    String name = m_memoryReferenceList.get(rowIndex).getCacheName();
                    if ((rowIndex == 0) || (! name.equals(m_memoryReferenceList.get(rowIndex-1).getCacheName()))) {
                        return name;
                    } else {
                        return "";
                    }
                }
                case COLUMN_WINDOW: {
                    return m_memoryReferenceList.get(rowIndex).getClientName();
                }
            }
            return null;
        }
    }


}
