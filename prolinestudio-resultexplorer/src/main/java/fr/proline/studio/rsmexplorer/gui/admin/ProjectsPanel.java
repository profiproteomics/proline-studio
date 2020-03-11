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
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * Panel displaying all projects of all users with the database size. It is used
 * in the admin dialog
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

    private HashMap<String, ProjectInfo.Status> m_projectStatusMap = new HashMap();

    public ProjectsPanel(JDialog dialog, Boolean editable) {
        m_isEditable = editable;
        m_dialogOwner = dialog;

        setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        add(internalPanel, BorderLayout.CENTER);
    }

    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel(new BorderLayout());

        initGenericModel();//add desired Table columns order
        initRawFileModel();
        internalPanel.setBorder(BorderFactory.createTitledBorder("Projects"));

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

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                int columnIndex = this.convertColumnIndexToModel(column);
                switch (columnIndex) {
                    case 0:
                        TableColumn column0;
                        column0 = this.getColumnModel().getColumn(columnIndex);
                        column0.setPreferredWidth(30);
                        return new StatusRenderer();
                    default:
                        return super.getCellRenderer(convertRowIndexToModel(row), columnIndex);
                }
            }
        };
        projectTableScrollPane.setViewportView(m_projectsTable);
        m_projectsTable.setFillsViewportHeight(true);
        m_projectsTable.setModel(m_projectsModel);
        m_projectsTable.getSelectionModel().addListSelectionListener(this);
        JToolBar toolbar = initToolbar();
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(projectTableScrollPane, BorderLayout.CENTER);
        topPanel.add(toolbar, BorderLayout.WEST);
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

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                int columnIndex = this.convertColumnIndexToModel(column);
                switch (columnIndex) {
                    case 0:
                        TableColumn column0;
                        column0 = this.getColumnModel().getColumn(columnIndex);
                        column0.setPreferredWidth(30);
                        return new StatusRenderer();
                    default:
                        return super.getCellRenderer(convertRowIndexToModel(row), columnIndex);
                }
            }
        };

        JScrollPane rawFilesScrollPane = new JScrollPane();
        rawFilesScrollPane.setViewportView(m_rawfilesTable);
        m_rawfilesTable.setFillsViewportHeight(true);
        m_rawfilesTable.setModel(m_rawfilesModel);
        JToolBar bottomToolbar = initBottomToolbar();
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(rawFilesScrollPane, BorderLayout.CENTER);
        bottomPanel.add(bottomToolbar, BorderLayout.WEST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);

        splitPane.setDividerLocation(150);

        internalPanel.add(splitPane, BorderLayout.CENTER);
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
        ExportButton exportButton = new ExportButton(m_projectsModel, "Project", m_projectsTable);

        toolbar.add(filterButton);
        toolbar.add(exportButton);

        return toolbar;
    }

    private JToolBar initBottomToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton exportButton = new ExportButton(m_rawfilesModel, "Raw Files", m_rawfilesTable);
        toolbar.add(exportButton);
        return toolbar;
    }

    private void initGenericModel() {
        m_projectBeanModel.addProperties("status", 0, "Status"); //(properties name, order, column name)
        m_projectBeanModel.addProperties("projectId", 1, "Id");
        m_projectBeanModel.addProperties("name", 2, "Project");
        m_projectBeanModel.addProperties("description", 3);
        m_projectBeanModel.addProperties("size", 4, "Size (MB)", new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0, true, true), null);
        m_projectBeanModel.addProperties("user", 5, "Owner");
        m_projectBeanModel.addProperties("DBName", 6, "Databases");
        m_projectBeanModel.addProperties("lastDatasetDate", 7, "DatasetDate");

        // must be called only one time
        m_projectBeanModel.firePropertiesChanged();
    }

    private void initRawFileModel() {

        m_rawfilesBeanModel.addProperties("projectStatus", 0, "Status");
        m_rawfilesBeanModel.addProperties("creationTimestamp", 1, "CreationTimestamp");
        m_rawfilesBeanModel.addProperties("identifier", 2, "Identifier");
        m_rawfilesBeanModel.addProperties("project_ids", 3, "project_ids");
        m_rawfilesBeanModel.addProperties("projectsCount", 4, "projectsCount");
        m_rawfilesBeanModel.addProperties("rawFileName", 5, "rawFileName");
        m_rawfilesBeanModel.addProperties("rawFileDirectory", 6, "rawFileDirectory");
        m_rawfilesBeanModel.addProperties("mzdbFileName", 7, "mzdbFileName");
        m_rawfilesBeanModel.addProperties("mzdbFileDirectory", 8, "mzdbFileDirectory");
        m_rawfilesBeanModel.addProperties("serializedProperties", 9, "serializedProperties");

        // must be called only one time
        m_rawfilesBeanModel.firePropertiesChanged();
    }

    public void updateData() {
        m_projectBeanModel.setData(m_resultProjectsList);
        m_projectStatusMap = new HashMap<>();
        for (ProjectInfo pi : m_resultProjectsList) {
            m_projectStatusMap.put("" + pi.getProjectId(), pi.getStatus());
        }
    }

    private void updateRawFiles(ArrayList<DRawFile> resultRawfiles) {
        m_rawfilesBeanModel.setData(resultRawfiles);
        for (DRawFile file : resultRawfiles) {
            DRawFile.ProjectStatus ps = DRawFile.ProjectStatus.ACTIVE;
            int nbArchived = 0;
            String projectIds = file.getProjectIds();
            String[] ids = projectIds.split(",");
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            Color c;
            for (String id : ids) {
                ProjectInfo.Status s = m_projectStatusMap.get(id);
                if (s == ProjectInfo.Status.ARCHIVED) {
                    nbArchived++;
                    c = Color.LIGHT_GRAY;
                } else {
                    c = Color.BLACK;
                }
                String projectColor = CyclicColorPalette.getHTMLColor(c);
                sb.append("<font color='").append(projectColor).append("'>" + id + "</font>,");
            }
            String toShow = sb.substring(0, sb.length() - 2) + "</html>";
            file.setProjectIds(toShow);

            if (nbArchived == file.getProjectsCount()) {
                ps = DRawFile.ProjectStatus.ALL_ARCHIVED;
            } else if (nbArchived > 0) {
                ps = DRawFile.ProjectStatus.SOME_ARCHIVED;
            }
            file.setProjectStatus(ps);
        }
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

    private class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.setHorizontalTextPosition(JLabel.CENTER);
            this.setVerticalTextPosition(JLabel.BOTTOM);

            if (value instanceof ProjectInfo.Status) {

                ProjectInfo.Status status = (ProjectInfo.Status) value;

                switch (status) {
                    case ACTIVE:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.STATUS_ACTIVE));
                        this.setToolTipText("ACTIVE");
                        break;
                    case ARCHIVED:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.STATUS_ALL_ACHIVED));
                        this.setToolTipText("ARCHIVED");
                        break;
                }
            } else if (value instanceof DRawFile.ProjectStatus) {
                DRawFile.ProjectStatus status = (DRawFile.ProjectStatus) value;

                switch (status) {
                    case ACTIVE:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.STATUS_ACTIVE));
                        this.setToolTipText("ACTIVE");
                        break;
                    case SOME_ARCHIVED:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.STATUS_SOME_ARCHIVED));
                        this.setToolTipText("SOME ARCHIVED");
                        break;
                    case ALL_ARCHIVED:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.STATUS_ALL_ACHIVED));
                        this.setToolTipText("ALL ARCHIVED");
                        break;

                }
            }
            this.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
                this.setForeground(Color.WHITE);
            } else {
                this.setBackground(null);
                this.setForeground(Color.BLACK);
            }
            return this;
        }
    }
}
