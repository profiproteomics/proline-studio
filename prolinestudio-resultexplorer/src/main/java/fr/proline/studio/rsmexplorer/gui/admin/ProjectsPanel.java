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
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ProjectInfo;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.LongFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import fr.proline.studio.table.AbstractDecoratedGlobalTableModel;
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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import org.openide.windows.WindowManager;

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

    //private final BeanTableModel m_projectBeanModel = new BeanTableModel<ProjectInfo>(ProjectInfo.class);
//    private final CompoundTableModel m_projectsModel = new CompoundTableModel(m_projectBeanModel, true);
    private final ProjectsInfoTableModel m_projectsBaseModel = new ProjectsInfoTableModel();
    private final CompoundTableModel m_projectsModel = new CompoundTableModel(m_projectsBaseModel, true);
    private DecoratedMarkerTable m_projectsTable;

    //private final BeanTableModel m_rawfilesBeanModel = new BeanTableModel<DRawFile>(DRawFile.class);
    //private final CompoundTableModel m_rawfilesModel = new CompoundTableModel(m_rawfilesBeanModel, true);
    private final RawFilesTableModel m_rawFileTableModel = new RawFilesTableModel();
    private DecoratedMarkerTable m_rawfilesTable;

    private ArrayList<ProjectInfo> m_resultProjectsList = new ArrayList<>();

    private HashMap<String, ProjectInfo.Status> m_projectStatusMap = new HashMap();
    private HashMap<Long, ProjectInfo> m_projectMap = new HashMap();
    private SharedProjectDialg m_sharedProjectDialog;
    private HashMap<Long, ProjectInfo> m_sharedProjects4SelectMap;
    private ProjectsInfoTableModel m_sharedProjects4SelectModel = new ProjectsInfoTableModel();
    String m_mzdbPath;

    public ProjectsPanel(JDialog dialog, Boolean editable) {
        m_isEditable = editable;
        m_dialogOwner = dialog;
        m_mzdbPath = this.getDRawFilePathRoot();
        setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        add(internalPanel, BorderLayout.CENTER);
    }

    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel(new BorderLayout());
        internalPanel.setBorder(BorderFactory.createTitledBorder("Projects"));

        JScrollPane projectTableScrollPane = new JScrollPane();
        m_projectsTable = createProjectsTable();
        projectTableScrollPane.setViewportView(m_projectsTable);
        m_projectsTable.setFillsViewportHeight(true);
        m_projectsTable.setModel(m_projectsModel);
        m_projectsTable.getSelectionModel().addListSelectionListener(this);

        JToolBar toolbar = initTopToolbar();
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(projectTableScrollPane, BorderLayout.CENTER);
        topPanel.add(toolbar, BorderLayout.WEST);

        createRawFileTable();
        JScrollPane rawFilesScrollPane = new JScrollPane();
        rawFilesScrollPane.setViewportView(m_rawfilesTable);
        m_rawfilesTable.setFillsViewportHeight(true);
        m_rawfilesTable.setModel(m_rawFileTableModel);

        JToolBar bottomToolbar = initBottomToolbar();
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(rawFilesScrollPane, BorderLayout.CENTER);
        bottomPanel.add(bottomToolbar, BorderLayout.WEST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        splitPane.setDividerLocation(150);
        internalPanel.add(splitPane, BorderLayout.CENTER);

        loadProjectsData();

        return internalPanel;
    }

    private JToolBar initTopToolbar() {
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
        ExportButton exportButton = new ExportButton(m_rawFileTableModel, "Raw Files", m_rawfilesTable);
        toolbar.add(exportButton);
        JButton otherProjectBt = new JButton(IconManager.getIcon(IconManager.IconType.PROJECT));
        otherProjectBt.setToolTipText("Projects share at least one mzdb file with current projet(s)");
        otherProjectBt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_sharedProjects4SelectMap.isEmpty()) {
                    JOptionPane.showMessageDialog(m_dialogOwner, "No other raw file shared project");
                } else {
                    if (m_sharedProjectDialog == null) {
                        m_sharedProjectDialog = new SharedProjectDialg();
                    }
                    m_sharedProjectDialog.setVisible(true);
                }
            }
        });
        toolbar.add(otherProjectBt);
        return toolbar;
    }

    class SharedProjectDialg extends DefaultDialog {

        private SharedProjectDialg() {
            setIconImage(IconManager.getImage(IconManager.IconType.PROJECT));
            Frame f = WindowManager.getDefault().getMainWindow();
            setSize(new Dimension(500, 480));
            setResizable(true);
            setButtonVisible(BUTTON_CANCEL, false);
            setButtonName(BUTTON_OK, "Close");
            initInternalPanel();

            setLocationRelativeTo(ProjectsPanel.this);
            setVisible(true);
        }

        void initInternalPanel() {
            JPanel sharedProjectPane = new JPanel(new BorderLayout());
            sharedProjectPane.setBorder(BorderFactory.createTitledBorder("Other Project(s) Which Share Raw Files"));

            DecoratedMarkerTable sharedProjectTable = createProjectsTable();
            JScrollPane sharedProjectTableScrollPane = new JScrollPane();

            sharedProjectTableScrollPane.setViewportView(sharedProjectTable);
            sharedProjectTable.setFillsViewportHeight(true);

            sharedProjectTable.setModel(m_sharedProjects4SelectModel);
            sharedProjectPane.add(sharedProjectTableScrollPane, BorderLayout.CENTER);
            setInternalComponent(sharedProjectPane);
        }

    }

    private DecoratedMarkerTable createProjectsTable() {
        //m_projectsTable
        DecoratedMarkerTable table = new DecoratedMarkerTable() {

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
            public TableCellRenderer getCellRenderer(int row, int columnIndex) {
                //don't use convertColumnIndexToModel, will have class cast error in CellRenderer
                switch (columnIndex) {
                    case ProjectsInfoTableModel.COLTYPE_STATUS:
                        TableColumn column0;
                        column0 = this.getColumnModel().getColumn(columnIndex);
                        column0.setPreferredWidth(30);
                        return new StatusRenderer();
                    case ProjectsInfoTableModel.COLTYPE_SIZE:
                        DefaultRightAlignRenderer rightAign = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                        return new DoubleRenderer(rightAign, 0, true, true);
                    default:
                        return super.getCellRenderer(row, columnIndex);
                }
            }
        };
        return table;
    }

    private void createRawFileTable() {
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
            public TableCellRenderer getCellRenderer(int row, int columnIndex) {
                switch (columnIndex) {
                    case RawFilesTableModel.COLTYPE_PROJECT_STATUS:
                        TableColumn column0;
                        column0 = this.getColumnModel().getColumn(columnIndex);
                        column0.setPreferredWidth(30);
                        return new StatusRenderer();
                    default:
                        return super.getCellRenderer(row, columnIndex);
                }
            }
        };
    }

    private void loadProjectsData() {
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
    }

    private void updateData() {
        m_projectsBaseModel.setData(m_resultProjectsList);
        m_projectStatusMap = new HashMap<>();
        for (ProjectInfo pi : m_resultProjectsList) {
            m_projectStatusMap.put("" + pi.getProjectId(), pi.getStatus());
            m_projectMap.put(pi.getProjectId(), pi);
        }
    }

    /**
     * For each DRawFile, mark it's ProjectIds in color according to project
     * status, register colored ProjectsId by setProjectIdsDecorted().<br>
     * Build m_sharedProjects4SelectMap, which register another projets, those
     * use also the DRawFile in this selected project(s).<br>
     * Mark relative rawFile directory.
     *
     * @param resultRawfiles
     * @param selectedProjectIds
     */
    private void updateRawFiles(ArrayList<DRawFile> resultRawfiles, List<Long> selectedProjectIds) {
        ArrayList<String> idList = new ArrayList();
        ArrayList<DRawFile> fileList = new ArrayList<>();
        m_sharedProjects4SelectMap = new HashMap();
        for (DRawFile file : resultRawfiles) {
            String identifier = file.getIdentifier();
            if (idList.contains(identifier)) {
                continue;
            } else {
                idList.add(identifier);
                fileList.add(file);
            }
            String path = file.getRawFileDirectory();
            if (path.startsWith(m_mzdbPath)) {
                file.setRawFileDirectory(path.replaceFirst(m_mzdbPath, "<" + m_mzdbPath + ">"));
            }
            DRawFile.ProjectStatus ps = DRawFile.ProjectStatus.ACTIVE;
            int nbArchived = 0;
            String projectIds = file.getProjectIds();
            String[] ids = projectIds.split(",");
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            Color c;
            for (String id : ids) {
                Long idProject = Long.parseLong(id);
                if (!selectedProjectIds.contains(idProject) && m_sharedProjects4SelectMap.get(idProject) == null) {
                    m_sharedProjects4SelectMap.put(idProject, m_projectMap.get(idProject));
                }
                ProjectInfo.Status status = m_projectStatusMap.get(id);
                switch (status) {
                    case ARCHIVED:
                        nbArchived++;
                        c = Color.GRAY;
                        break;
                    case INACTIVE:
                        nbArchived++;
                        c = Color.BLUE;
                        break;
                    default:
                        c = Color.BLACK;
                        break;
                }
                String projectColor = CyclicColorPalette.getHTMLColor(c);
                sb.append("<span style='color:#").append(projectColor).append(";'>" + id + "</span>,");
            }
            String toShow = sb.substring(0, sb.length() - 1) + "</html>";
            file.setProjectIdsDecorted(toShow);

            if (nbArchived == file.getProjectsCount()) {
                ps = DRawFile.ProjectStatus.ALL_ARCHIVED;
            } else if (nbArchived > 0) {
                ps = DRawFile.ProjectStatus.SOME_ARCHIVED;
            }
            file.setProjectStatus(ps);
        }
        List data = m_sharedProjects4SelectMap.values().stream().sorted(Comparator.comparingLong(ProjectInfo::getProjectId)).collect(Collectors.toList());
        m_sharedProjects4SelectModel.setData(data);
        m_rawFileTableModel.setData(fileList);
    }

    private String getDRawFilePathRoot() {
        ArrayList<String> paths = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);
        if (paths != null && !paths.isEmpty()) {
            return paths.get(0);
        }
        return "";
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
                        updateRawFiles(resultRawfiles, projectIds);
                    }
                }
            };
            fr.proline.studio.dam.tasks.DatabaseProjectTask task = new fr.proline.studio.dam.tasks.DatabaseProjectTask(callback);
            task.initLoadRawFilesList(projectIds, resultRawfiles);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }

    public class ProjectsInfoTableModel extends AbstractDecoratedGlobalTableModel<ProjectInfo> {

        public static final int COLTYPE_STATUS = 0;
        public static final int COLTYPE_PROJECTID = 1;// "Id"
        public static final int COLTYPE_NAME = 2;//"Project"
        public static final int COLTYPE_DESCRIPTION = 3;
        public static final int COLTYPE_SIZE = 4;// "Size (MB)", new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0, true, true), null);
        public static final int COLTYPE_USER = 5; //"Owner"
        public static final int COLTYPE_RAW_FILES_COUNT = 6;
        public static final int COLTYPE_DB_NAME = 7;// "Databases"
        public static final int COLTYPE_LAST_DATASET_DATE = 8;// "DatasetDate"
        public static final int COLTYPE_PROPERTIES = 9;

        {
            String[] columnNames = {" ", "Id", "Project", "Description", "Size (MB)", "Owner", "Count Raw File", "Databases", "Dataset Date", "Properties"};
            m_columnNames = columnNames;
            String[] columnTooltips = {"Status", "Id", "Project", "Description", "Size (MB)", "Owner", "Count Raw File", "Databases", "Dataset Date", "Properties"};
            m_columnTooltips = columnTooltips;
        }

        public ProjectsInfoTableModel() {
            m_entities = new ArrayList<>();
        }

        @Override
        public int getRowCount() {
            return m_entities.size();
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            ProjectInfo project = m_entities.get(row);

            switch (col) {
                case COLTYPE_STATUS:
                    return project.getStatus();
                case COLTYPE_PROJECTID:
                    return project.getProjectId();
                case COLTYPE_NAME:
                    return project.getName();
                case COLTYPE_DESCRIPTION:
                    return project.getDescription();
                case COLTYPE_SIZE:
                    return project.getSize();
                case COLTYPE_USER:
                    return project.getUser();
                case COLTYPE_RAW_FILES_COUNT:
                    return project.getRawFilesCount();
                case COLTYPE_DB_NAME:
                    return project.getDBName();
                case COLTYPE_LAST_DATASET_DATE:
                    return project.getLastDatasetDate();
                case COLTYPE_PROPERTIES:
                    return project.getProperties();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case COLTYPE_STATUS:
                    return Integer.class;
                case COLTYPE_PROJECTID:
                    return Long.class;
                case COLTYPE_NAME:
                    return String.class;
                case COLTYPE_DESCRIPTION:
                    return String.class;
                case COLTYPE_SIZE:
                    return Double.class;
                case COLTYPE_USER:
                    return String.class;
                case COLTYPE_RAW_FILES_COUNT:
                    return Integer.class;
                case COLTYPE_DB_NAME:
                    return String.class;
                case COLTYPE_LAST_DATASET_DATE:
                    return Date.class;
                case COLTYPE_PROPERTIES:
                    return String.class;
                default:
                    return Object.class;//as in AbstractTableModel
            }
        }

        @Override
        public String getToolTipForHeader(int col) {
            return m_columnTooltips[col];
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return null;
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
            filtersMap.put(COLTYPE_STATUS, new IntegerFilter(m_columnNames[COLTYPE_STATUS], null, COLTYPE_STATUS));
            filtersMap.put(COLTYPE_NAME, new StringFilter(m_columnNames[COLTYPE_NAME], null, COLTYPE_NAME));
            filtersMap.put(COLTYPE_PROJECTID, new LongFilter(m_columnNames[COLTYPE_PROJECTID], null, COLTYPE_PROJECTID));
            filtersMap.put(COLTYPE_NAME, new StringFilter(m_columnNames[COLTYPE_NAME], null, COLTYPE_NAME));
            filtersMap.put(COLTYPE_DESCRIPTION, new StringFilter(m_columnNames[COLTYPE_DESCRIPTION], null, COLTYPE_DESCRIPTION));
            filtersMap.put(COLTYPE_USER, new StringFilter(m_columnNames[COLTYPE_USER], null, COLTYPE_USER));
            filtersMap.put(COLTYPE_DB_NAME, new StringFilter(m_columnNames[COLTYPE_DB_NAME], null, COLTYPE_DB_NAME));
            ConvertValueInterface dateConverter = new ConvertValueInterface() {
                @Override
                public Object convertValue(Object o) {
                    if (o == null) {
                        return null;
                    }
                    return ((Date) o).getTime();
                }

            };
            filtersMap.put(COLTYPE_LAST_DATASET_DATE, new LongFilter(m_columnNames[COLTYPE_LAST_DATASET_DATE], dateConverter, COLTYPE_LAST_DATASET_DATE));

        }

        //***************** Next is specific GlobalTableModelInterface******************//
        @Override
        public Object getRowValue(Class c, int row) {
            if (c.equals(ProjectInfo.class)) {
                return m_entities.get(row);
            }
            return null;
        }

        @Override
        public int[] getKeysColumn() {
            int[] keys = {COLTYPE_PROJECTID, COLTYPE_NAME};
            return keys;
        }

    }

    public class RawFilesTableModel extends AbstractDecoratedGlobalTableModel<DRawFile> {

        public static final int COLTYPE_PROJECT_STATUS = 0;// "Status"
        public static final int COLTYPE_CREATION_TIMESTAMP = 1;// "CreationTimestamp"
        public static final int COLTYPE_IDENTIFIER = 2;// "Identifier"
        public static final int COLTYPE_RAW_FILE_NAME = 3;// "rawFileName"
        public static final int COLTYPE_RAW_FILE_DIRECTORY = 4;// "rawFileDirectory"
        public static final int COLTYPE_PROJECT_IDS = 5;// "project_ids"
        public static final int COLTYPE_PROJECTS_COUNT = 6;// "projectsCount"
        public static final int COLTYPE_SERIALIZED_PROPERTIES = 7;//"serializedProperties"

        {
            String[] columnNames = {" ", "Creation Timestamp", "Identifier", "Raw File Name", "Raw File Directory", "Project Ids", "Projects Count", "Properties"};
            m_columnNames = columnNames;
            String[] columnTooltips = {"Projects Status", "Creation Timestamp", "Identifier", "Raw File Name", "Raw File Directory", "Project Ids", "Projects Count", "Properties"};
            m_columnTooltips = columnTooltips;
        }

        public RawFilesTableModel() {
            m_entities = new ArrayList<>();
        }

        /**
         * used for export
         *
         * @param rowIndex
         * @param columnIndex
         * @return
         */
        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            DRawFile file = m_entities.get(rowIndex);

            switch (columnIndex) {
                case COLTYPE_PROJECT_IDS:
                    return file.getProjectIds();
                default:
                    return getValueAt(rowIndex, columnIndex);
            }

        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DRawFile file = m_entities.get(rowIndex);

            switch (columnIndex) {
                case COLTYPE_PROJECT_STATUS:
                    return file.getProjectStatus();
                case COLTYPE_CREATION_TIMESTAMP:
                    return file.getCreationTimestamp();
                case COLTYPE_IDENTIFIER:
                    return file.getIdentifier();
                case COLTYPE_RAW_FILE_NAME:
                    return file.getRawFileName();
                case COLTYPE_RAW_FILE_DIRECTORY:
                    return file.getRawFileDirectory();
                case COLTYPE_PROJECT_IDS:
                    return file.getProjectIdsDecorted();
                case COLTYPE_PROJECTS_COUNT:
                    return file.getProjectsCount();
                case COLTYPE_SERIALIZED_PROPERTIES:
                    return file.getSerializedProperties();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case COLTYPE_PROJECT_STATUS:
                    return Integer.class;
                case COLTYPE_CREATION_TIMESTAMP:
                    return Timestamp.class;
                case COLTYPE_IDENTIFIER:
                case COLTYPE_RAW_FILE_NAME:
                case COLTYPE_RAW_FILE_DIRECTORY:
                case COLTYPE_PROJECT_IDS:
                    return String.class;
                case COLTYPE_PROJECTS_COUNT:
                    return Integer.class;
                case COLTYPE_SERIALIZED_PROPERTIES:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            if (col == COLTYPE_PROJECT_IDS) {
                String projectId = (String)getDataValueAt(row, col); //return projectId without html tag
                ArrayList<ExportFontData> fonds = fr.proline.studio.table.ExportFontModelUtilities.getExportFonts(projectId,m_projectStatusMap);
                return fonds;
            }
            return null;
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return null;//renderer at table level, not here
        }

        @Override
        public Object getRowValue(Class c, int row) {
            if (c.equals(DRawFile.class)) {
                return m_entities.get(row);
            }
            return null;
        }

        @Override
        public int[] getKeysColumn() {
            int[] keys = {COLTYPE_IDENTIFIER};
            return keys;
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
            //no Filters
        }

    }

    private class StatusRenderer extends DefaultTableCellRenderer {

        String active = "<html><font color='" + CyclicColorPalette.getHTMLColor(Color.green) + "'>&#x2587;&nbsp;</font></html>";
        String archived = "<html><font color='" + CyclicColorPalette.getHTMLColor(Color.blue) + "'>&#x2587;&nbsp;</font></html>";
        String some_archived = "<html><font color='" + CyclicColorPalette.getHTMLColor(Color.orange) + "'>&#x2587;&nbsp;</font></html>";
        String inactive = "<html><font color='" + CyclicColorPalette.getHTMLColor(Color.gray) + "'>&#x2587;&nbsp;</font></html>";

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.setHorizontalTextPosition(JLabel.CENTER);
            this.setVerticalTextPosition(JLabel.BOTTOM);

            if (value instanceof ProjectInfo.Status) {

                ProjectInfo.Status status = (ProjectInfo.Status) value;

                switch (status) {
                    case ACTIVE:
                        this.setText(active);
                        this.setToolTipText("Active");
                        break;
                    case ARCHIVED:
                        this.setText(archived);
                        this.setToolTipText("Archived");
                        break;
                    case INACTIVE:
                        this.setText(inactive);
                        this.setToolTipText("Inactive");
                        break;
                }
            } else if (value instanceof DRawFile.ProjectStatus) {
                DRawFile.ProjectStatus status = (DRawFile.ProjectStatus) value;

                switch (status) {
                    case ACTIVE:
                        this.setText(active);
                        this.setToolTipText("Active");
                        break;
                    case SOME_ARCHIVED:
                        this.setText(some_archived);
                        this.setToolTipText("Some Archived");
                        break;
                    case ALL_ARCHIVED:
                        this.setText(inactive);
                        this.setToolTipText("All archived/Inactive");
                        break;

                }
            }
            this.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
                this.setForeground(Color.WHITE);
            } else {
                this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
                this.setForeground(Color.BLACK);
            }
            return this;
        }
    }
}
