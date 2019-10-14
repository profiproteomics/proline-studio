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

import fr.proline.studio.dam.data.ClearProjectData;
import fr.proline.studio.rsmexplorer.gui.DataClearProjectTable;
import fr.proline.studio.rsmexplorer.gui.model.DataClearProjectTableModel;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Panel used to clear project (rsm and rs)
 * @author MB243701
 */
public class ClearProjectPanel extends JPanel{
    private List<ClearProjectData> m_dataToClear;
    
    private JScrollPane m_scrollPane;
    private DataClearProjectTable m_dataTable;
    
    public ClearProjectPanel(){
        super();
        initComponents();
    }
    
    private void initComponents(){
        this.setLayout(new BorderLayout());
        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        m_scrollPane = new JScrollPane();

        m_dataTable = new DataClearProjectTable();
        m_dataTable.setModel(new CompoundTableModel(new DataClearProjectTableModel(), true));
         
        m_scrollPane.setViewportView(m_dataTable);
        m_dataTable.setFillsViewportHeight(true);
        m_dataTable.setViewport(m_scrollPane.getViewport());
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_scrollPane, c);
        
        this.add(internalPanel, BorderLayout.CENTER);
        
        TableColumnModel columnModel = m_dataTable.getColumnModel();   
        TableColumn col = columnModel.getColumn(1);
        col.setWidth(20);
        col.setPreferredWidth(20);
        col = columnModel.getColumn(2);
        col.setWidth(100);
        col.setPreferredWidth(200);

    }
    
    public void setData(List<ClearProjectData> dataToClear){
        m_dataToClear = dataToClear;
        ((DataClearProjectTableModel) ((CompoundTableModel) m_dataTable.getModel()).getBaseModel()).setData(m_dataToClear);
    }
    
    
    public List<ClearProjectData> getSelectedData(){
        return ((DataClearProjectTableModel) ((CompoundTableModel) m_dataTable.getModel()).getBaseModel()).getSelectedData();
    }
    
    
}

