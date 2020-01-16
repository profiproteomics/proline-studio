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

import fr.proline.studio.dam.tasks.data.DRawFile;
import fr.proline.studio.table.BeanTableModel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ProjectInfo;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
/**
 *
 * Panel displaying all projects of all users with the database size.
 * It is used in the admin dialog
 * 
 * @author JM235353
 */
public class ProjectsPanel extends JPanel implements ListSelectionListener {
    
    private JDialog m_dialogOwner = null;
    private Boolean m_isEditable = true;
    
    private final BeanTableModel m_projectBeanModel = new BeanTableModel<ProjectInfo>(ProjectInfo.class);
    private final CompoundTableModel m_projectsModel = new CompoundTableModel(m_projectBeanModel, true);
    private DecoratedMarkerTable m_projectsTable;

    private final BeanTableModel m_rawfilesBeanModel = new BeanTableModel<DRawFile>(DRawFile.class);
    private final CompoundTableModel m_rawfilesModel = new CompoundTableModel(m_rawfilesBeanModel, true);
    private DecoratedMarkerTable m_rawfilesTable;

    private ArrayList<ProjectInfo> m_resultProjectsList = new ArrayList<>();

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        JScrollPane projectTableScrollPane = new JScrollPane();
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
        projectTableScrollPane.setViewportView(m_projectsTable);
        m_projectsTable.setFillsViewportHeight(true);
        m_projectsTable.setModel(m_projectsModel);
        m_projectsTable.getSelectionModel().addListSelectionListener(this);

        m_rawfilesTable = new DecoratedMarkerTable() {
            @Override
            public void addTableModelListener(TableModelListener l) {
                getModel().addTableModelListener(l);
            }

            @Override
            public TablePopupMenu initPopupMenu() {
                return new TablePopupMenu();
            }

            @Override
            public void prepostPopupMenu() {

            }
        };
        
        JScrollPane rawFilesScrollPane = new JScrollPane();
        rawFilesScrollPane.setViewportView(m_rawfilesTable);
        m_rawfilesTable.setFillsViewportHeight(true);
        m_rawfilesTable.setModel(m_rawfilesModel);

        splitPane.setTopComponent(projectTableScrollPane);
        splitPane.setBottomComponent(rawFilesScrollPane);
        splitPane.setDividerLocation(0.5);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        internalPanel.add(splitPane, c);


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
        m_projectBeanModel.addProperties("projectId", 0, "Id");
        m_projectBeanModel.addProperties("name", 1, "Project");
        m_projectBeanModel.addProperties("description", 2);
        m_projectBeanModel.addProperties("size", 3, "Size (MB)", new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0, true, true), null);
        m_projectBeanModel.addProperties("user", 4, "Owner");
        m_projectBeanModel.addProperties("DBName", 5, "Databases");
        m_projectBeanModel.addProperties("lastDatasetDate", 6, "DatasetDate");
        
        
        // must be called only one time
        m_projectBeanModel.firePropertiesChanged();
    }
    
    public void updateData() {
        m_projectBeanModel.setData(m_resultProjectsList);
    }

    private void updateRawFiles(ArrayList<DRawFile> resultRawfiles) {
        m_rawfilesBeanModel.setData(resultRawfiles);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int[] rows = m_projectsTable.getSelectedRows();
        
        List<Long> projectIds = Arrays.stream(rows).mapToObj(i -> {
            int mi = m_projectsModel.convertRowToOriginalModel(i);
            mi = m_projectsTable.convertRowIndexToModel(i);
            mi = m_projectsModel.convertCompoundRowToBaseModelRow(mi);
            return Long.valueOf(m_resultProjectsList.get(mi).getProjectId());
        }).collect(Collectors.toList());

        if (!(projectIds == null) && (!projectIds.isEmpty())) {
            final ArrayList<DRawFile> resultRawfiles = new ArrayList<DRawFile>();
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        updateRawFiles(resultRawfiles);
                    }
                }
            };
            fr.proline.studio.dam.tasks.DatabaseProjectTask task = new fr.proline.studio.dam.tasks.DatabaseProjectTask(callback);
            task.initLoadRawFilesList(projectIds, resultRawfiles);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
}
