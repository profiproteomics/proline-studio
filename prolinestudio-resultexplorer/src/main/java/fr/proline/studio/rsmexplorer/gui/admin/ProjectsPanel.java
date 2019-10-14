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
package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.studio.table.BeanTableModel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ProjectToDBs;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TableModelListener;
/**
 *
 * Panel displaying all projects of all users with the database size.
 * It is used in the admin dialog
 * 
 * @author JM235353
 */
public class ProjectsPanel extends JPanel {
    
    private JDialog m_dialogOwner = null;
    private Boolean m_isEditable = true;
    
    private final BeanTableModel m_genericBeanModel = new BeanTableModel<ProjectToDBs>(ProjectToDBs.class);
    private final CompoundTableModel m_projectsModel = new CompoundTableModel(m_genericBeanModel, true);
    private DecoratedMarkerTable m_projectsTable;
    
    private ArrayList<ProjectToDBs> m_resultProjectsList = new ArrayList<>();

    public ProjectsPanel(JDialog dialog, Boolean editable) {
        m_isEditable = editable;
        m_dialogOwner = dialog;

        setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        add(toolbar, BorderLayout.WEST);
        
    }
    
    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();
        
        initGenericModel();

        internalPanel.setBorder(BorderFactory.createTitledBorder("Projects"));
        
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JScrollPane tableScrollPane = new JScrollPane();
        m_projectsTable = new DecoratedMarkerTable() {

            @Override
            public TablePopupMenu initPopupMenu() {
                return new TablePopupMenu();
            }

            @Override
            public void prepostPopupMenu() {
            }

            @Override
            public void addTableModelListener(TableModelListener l) {
                getModel().addTableModelListener(l);
            }
        };
        tableScrollPane.setViewportView(m_projectsTable);
        m_projectsTable.setFillsViewportHeight(true);
        m_projectsTable.setModel(m_projectsModel);


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(tableScrollPane, c);
        
        
        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 0;
        internalPanel.add(Box.createHorizontalGlue(), c);

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    updateData();
                }
            }

        };

        fr.proline.studio.dam.tasks.DatabaseProjectTask task = new fr.proline.studio.dam.tasks.DatabaseProjectTask(callback);
        task.initLoadProjectsList(m_resultProjectsList); 
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        return internalPanel;
    }

    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        FilterButton filterButton = new FilterButton(m_projectsModel) {

            @Override
            protected void filteringDone() {
            }
            
        };
        ExportButton exportButton = new ExportButton(m_projectsModel, "Peptides", m_projectsTable);

        
        
        toolbar.add(filterButton);
        toolbar.add(exportButton);
        
        return toolbar;
    }
    
    private void initGenericModel() {
        m_genericBeanModel.addProperties("projectId", 0, "Id");
        m_genericBeanModel.addProperties("name", 1, "Project");
        m_genericBeanModel.addProperties("description", 2);
        m_genericBeanModel.addProperties("size", 3, "Size (MB)", new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0, true, true), null);
        m_genericBeanModel.addProperties("user", 4, "Owner");
        m_genericBeanModel.addProperties("DBName", 5, "Databases");
        
        // must be called only one time
        m_genericBeanModel.firePropertiesChanged();
    }
    
    public void updateData() {
        
        m_genericBeanModel.setData(m_resultProjectsList);

    }
    

}
