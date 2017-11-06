package fr.proline.studio.rsmexplorer.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ClearProjectData;
import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseClearProjectTask;
import fr.proline.studio.dam.tasks.DatabaseProjectTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.ClearProjectTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.pattern.DataParameter;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.PropertiesTopComponent;
import fr.proline.studio.rsmexplorer.actions.ConnectAction;
import fr.proline.studio.rsmexplorer.gui.dialog.AddProjectDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ClearProjectDialog;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.PropertiesProviderInterface;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 * NB : The button to clear rs/rsm is ready but for now it is not added in the interface. To be added, uncomment the line 187
 */
public class ProjectExplorerPanel extends JPanel {

    private static ProjectExplorerPanel m_singleton = null;
    private JButton m_addProjectButton;
    private JButton m_editProjectButton;
    private JButton m_propertiesProjectButton;
    private JButton m_clearProjectButton;
    private JComboBox<ProjectItem> m_projectsComboBox = null;
    private JScrollPane m_identificationTreeScrollPane = null;
    private JScrollPane m_quantitationTreeScrollPane = null;

    public static ProjectExplorerPanel getProjectExplorerPanel() {
        if (m_singleton == null) {
            m_singleton = new ProjectExplorerPanel();
        }
        return m_singleton;
    }

    private ProjectExplorerPanel() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // ---- Create Objects
        m_projectsComboBox = new JComboBox<>();
        m_projectsComboBox.setRenderer(new ProjectComboboxRenderer());
        m_projectsComboBox.setMaximumRowCount(16);  // default value is 8. ticket #15005

        JPanel buttonsPanel = createButtonPanel();

        m_identificationTreeScrollPane = new JScrollPane();
        m_identificationTreeScrollPane.getViewport().setBackground(Color.white);

        m_quantitationTreeScrollPane = new JScrollPane();
        m_quantitationTreeScrollPane.getViewport().setBackground(Color.white);

        // ---- Add Objects to panel
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(m_projectsComboBox, c);

        c.gridx++;
        c.weightx = 0;
        add(buttonsPanel, c);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_identificationTreeScrollPane, m_quantitationTreeScrollPane);
        splitPane.setResizeWeight(0.5);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;
        add(splitPane, c);

    }

    public Project getSelectedProject() {

        ProjectItem pi = (ProjectItem) m_projectsComboBox.getSelectedItem();

        if (pi.m_projectIdentificationData == null) {
            return null;
        } else {
            return pi.m_projectIdentificationData.getProject();
        }
    }

    public void clearAll() {

        ConnectAction.setConnectionType(true, true);

        m_projectsComboBox.removeAllItems();

        m_identificationTreeScrollPane.setViewportView(null);

        m_quantitationTreeScrollPane.setViewportView(null);
    }

    private JPanel createButtonPanel() {
        JPanel buttonsPanel = new JPanel();

        buttonsPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 1, 5, 1);

        m_propertiesProjectButton = new JButton(IconManager.getIcon(IconManager.IconType.PROPERTY_SMALL_10X10));
        m_propertiesProjectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_propertiesProjectButton.setToolTipText("Display Project Properties");
        m_propertiesProjectButton.setEnabled(false);

        m_editProjectButton = new JButton(IconManager.getIcon(IconManager.IconType.EDIT_SMALL_10X10));
        m_editProjectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_editProjectButton.setToolTipText("Edit Project Name and Description");
        m_editProjectButton.setEnabled(false);

        m_addProjectButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS_SMALL_10X10));
        m_addProjectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_addProjectButton.setToolTipText("Create a New Project");
        m_addProjectButton.setEnabled(false);

        m_clearProjectButton = new JButton(IconManager.getIcon(IconManager.IconType.CLEAN_UP));
        m_clearProjectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_clearProjectButton.setToolTipText("Clean Up Project (Remove unused Search Result and Id. Summaries)...");
        m_clearProjectButton.setEnabled(false);

        c.gridx = 0;
        c.gridy = 0;

        buttonsPanel.add(m_propertiesProjectButton, c);

        c.gridx++;
        buttonsPanel.add(m_editProjectButton, c);

        c.gridx++;
        buttonsPanel.add(m_addProjectButton, c);

        c.gridx++;
        // uncomment this line to give access to clear rs/rsm project
        //buttonsPanel.add(m_clearProjectButton, c);

        // Interactions
        m_addProjectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addProjectActionPerformed();
                
            }
        });

        m_editProjectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editProjectActionPerformed();

            }
        });

        m_propertiesProjectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showPropertiesProjectActionPerformed();
            }
        });

        m_clearProjectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearProjectActionPerformed();
            }
        });

        return buttonsPanel;
    }
    
    private void addProjectActionPerformed() {
        AddProjectDialog dialog = AddProjectDialog.getAddProjectDialog(WindowManager.getDefault().getMainWindow());
        int x = (int) m_addProjectButton.getLocationOnScreen().getX() + m_addProjectButton.getWidth();
        int y = (int) m_addProjectButton.getLocationOnScreen().getY() + m_addProjectButton.getHeight();
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if ((dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) && (dialog.canModifyValues())) {

            // data needed to create the project
            String projectName = dialog.getProjectName();
            String projectDescription = dialog.getProjectDescription();
            UserAccount owner = DatabaseDataManager.getDatabaseDataManager().getLoggedUser();
            final ArrayList<UserAccount> userAccountList = dialog.getUserAccountList();

            // look where to put the node (alphabetical order)
            int insertionIndex = 0;
            ComboBoxModel<ProjectItem> model = m_projectsComboBox.getModel();
            int nbChildren = model.getSize();
            for (int i = 0; i < nbChildren; i++) {
                ProjectItem item = model.getElementAt(i);

                String itemProjectName = item.toString();
                if (projectName.compareToIgnoreCase(itemProjectName) >= 0) {
                    insertionIndex = i + 1;
                } else {
                    break;
                }

            }

            // Create a temporary node in the Project List
            ProjectIdentificationData projectIdentificationData = new ProjectIdentificationData(projectName);
            ProjectQuantitationData projectQuantitationData = new ProjectQuantitationData(projectName);
            final ProjectItem projectItem = new ProjectItem(projectIdentificationData, projectQuantitationData);
            projectItem.setIsChanging(true);

            m_projectsComboBox.insertItemAt(projectItem, insertionIndex);
            m_projectsComboBox.setSelectedItem(projectItem);

            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {
                        projectItem.setIsChanging(false);
                        getProjectExplorerPanel().selectProject(projectItem);
                        m_projectsComboBox.repaint();
                        m_projectsComboBox.setSelectedIndex(m_projectsComboBox.getSelectedIndex());

                        if (!userAccountList.isEmpty()) {
                            // we must add the user account list
                            DatabaseProjectTask task = new DatabaseProjectTask(null);
                            Project p = projectItem.getProjectIdentificationData().getProject();
                            task.initChangeSettingsOfProject(p, p.getName(), p.getDescription(), userAccountList);
                            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                        }

                    } else {
                        //JPM.TODO : manage error with errorMessage
                        m_projectsComboBox.removeItem(projectItem);
                    }
                }
            };

            fr.proline.studio.dpm.task.jms.CreateProjectTask task = new fr.proline.studio.dpm.task.jms.CreateProjectTask(callback, projectName, projectDescription, owner.getId(), projectIdentificationData, projectQuantitationData);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

        }
    }
    
    private void editProjectActionPerformed() {
        final ProjectItem projectItem = (ProjectItem) m_projectsComboBox.getSelectedItem();
        ProjectIdentificationData projectData = projectItem.getProjectIdentificationData();
        final Project project = projectData.getProject();
        final String oldName = project.getName();

        AddProjectDialog dialog = AddProjectDialog.getModifyProjectDialog(WindowManager.getDefault().getMainWindow(), project);
        int x = (int) m_addProjectButton.getLocationOnScreen().getX() + m_addProjectButton.getWidth();
        int y = (int) m_addProjectButton.getLocationOnScreen().getY() + m_addProjectButton.getHeight();
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if ((dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) && (dialog.canModifyValues())) {

            // data needed to create the project
            final String projectName = dialog.getProjectName();
            final String projectDescription = dialog.getProjectDescription();
            ArrayList<UserAccount> userAccountList = dialog.getUserAccountList();

            projectItem.setIsChanging(true);
            project.setName(projectName + "...");
            m_projectsComboBox.repaint();

            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        projectItem.setIsChanging(false);
                        project.setName(projectName);
                        project.setDescription(projectDescription);
                        m_projectsComboBox.repaint();
                    } else {
                        projectItem.setIsChanging(false);
                        project.setName(oldName);
                        m_projectsComboBox.repaint();
                    }
                }
            };

            // ask asynchronous loading of data
            DatabaseProjectTask task = new DatabaseProjectTask(callback);
            task.initChangeSettingsOfProject(project, projectName, projectDescription, userAccountList);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        }
    }

    private void showPropertiesProjectActionPerformed() {
        ProjectItem projectItem = (ProjectItem) m_projectsComboBox.getSelectedItem();
        ProjectIdentificationData projectData = projectItem.getProjectIdentificationData();
        String projectName = projectData.getName();

        String dialogName = "Properties : " + projectName;

        final PropertiesTopComponent win = new PropertiesTopComponent(dialogName);
        ProjectItem[] projectItemArray = new ProjectItem[1];
        projectItemArray[0] = projectItem;
        win.setProperties(projectItemArray);
        win.open();
        win.requestActive();

    }

    private void clearProjectActionPerformed() {
        ProjectItem projectItem = (ProjectItem) m_projectsComboBox.getSelectedItem();
        ProjectIdentificationData projectData = projectItem.getProjectIdentificationData();
        final Project project = projectData.getProject();
        List<ClearProjectData> data = new ArrayList();
        List<ClearProjectData> openedData = ProjectExplorerPanel.getOpenedData(project);
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                openClearProjectDialog(project, data);
            }
        };
        DatabaseClearProjectTask task = new DatabaseClearProjectTask(callback);
        task.initLoadDataToClearProject(project, data, openedData);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    
    /**
     * returns the list of rs/rsm opened in the application for a given project
     * @param projectId
     * @return 
     */
    public static  List<ClearProjectData> getOpenedData(Project project) {
        List<ClearProjectData> openedData = new ArrayList();
        long projectId = project.getId();
        String allImportedWindowsName = project.getName()+" : All Imported";
        
        // remove data which are opened in windows
        Iterator<TopComponent> itTop = TopComponent.getRegistry().getOpened().iterator();
        while (itTop.hasNext()) {
            TopComponent topComponent = itTop.next();
            if (topComponent instanceof DataBoxViewerTopComponent && !(topComponent.getName().startsWith(allImportedWindowsName))) {
                long pId = ((DataBoxViewerTopComponent) topComponent).getProjectId();
                if (pId == projectId) {
                    HashSet<GroupParameter> inParam = ((DataBoxViewerTopComponent) topComponent).getInParameters();
                    inParam.stream().forEach((inp) -> {
                        ArrayList<DataParameter> listP = inp.getParameterList();
                        for (DataParameter dataParam : listP) {
                            if (dataParam.equalsData(ResultSummary.class)) {
                                ResultSummary rsm = (ResultSummary) ((DataBoxViewerTopComponent) topComponent).getData(false, ResultSummary.class);
                                openedData.add(new ClearProjectData(projectId, rsm));
                                openedData.add(new ClearProjectData(projectId, rsm.getResultSet()));
                            } else if (dataParam.equalsData(ResultSet.class)) {
                                ResultSet rs = (ResultSet) ((DataBoxViewerTopComponent) topComponent).getData(false, ResultSet.class);
                                openedData.add(new ClearProjectData(projectId, rs));
                            }
                        }
                    });
                    ArrayList<GroupParameter> outParam = ((DataBoxViewerTopComponent) topComponent).getOutParameters();
                    outParam.stream().forEach((outp) -> {
                        ArrayList<DataParameter> listP = outp.getParameterList();
                        for (DataParameter dataParam : listP) {
                            if (dataParam.equalsData(ResultSummary.class)) {
                                ResultSummary rsm = (ResultSummary) ((DataBoxViewerTopComponent) topComponent).getData(false, ResultSummary.class);
                                openedData.add(new ClearProjectData(projectId, rsm));
                                openedData.add(new ClearProjectData(projectId, rsm.getResultSet()));
                            } else if (dataParam.equalsData(ResultSet.class)) {
                                ResultSet rs = (ResultSet) ((DataBoxViewerTopComponent) topComponent).getData(false, ResultSet.class);
                                openedData.add(new ClearProjectData(projectId, rs));
                            }
                        }
                    });
                }
            }
        }
        return openedData;
    }

    public void openClearProjectDialog(Project project, List<ClearProjectData> data) {
                 
        ClearProjectDialog clearProjectDialog = new ClearProjectDialog(WindowManager.getDefault().getMainWindow(), project, data);
        int x = (int) m_clearProjectButton.getLocationOnScreen().getX() + m_clearProjectButton.getWidth();
        int y = (int) m_clearProjectButton.getLocationOnScreen().getY() + m_clearProjectButton.getHeight();
        clearProjectDialog.setLocation(x, y);

        DefaultDialog.ProgressTask task = new DefaultDialog.ProgressTask() {
            @Override
            public int getMinValue() {
                return 0;
            }

            @Override
            public int getMaxValue() {
                return 100;
            }

            @Override
            protected Object doInBackground() throws Exception {

                if ((clearProjectDialog.canModifyValues())) {
                    List<ClearProjectData> dataToClear = clearProjectDialog.getSelectedData();

                    List<Long> rsmIds = new ArrayList();
                    List<Long> rsIds = new ArrayList();
                    dataToClear.stream().forEach((d) -> {
                        if (d.isResultSet()) {
                            rsIds.add(d.getResultSet().getId());
                        } else if (d.isResultSummary()) {
                            rsmIds.add(d.getResultSummary().getId());
                        }
                    });

                    AbstractJMSCallback clearCallBack = new AbstractJMSCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success) {
                            setProgress(100);
                        }

                    };
                    ClearProjectTask clearTaskDb = new ClearProjectTask(clearCallBack, project.getId(), rsmIds, rsIds);
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(clearTaskDb);

                }
                return null;
            }
        };

        clearProjectDialog.setTask(task);
        clearProjectDialog.setVisible(true);

    }
    
    

    public void startLoadingProjects() {

        ConnectAction.setConnectionType(true, false);

        // Null Item corresponds to Loading Projects...
        m_projectsComboBox.addItem(null);

        final ArrayList<AbstractData> projectList = new ArrayList<>();

        // Callback used only for the synchronization with the AccessDatabaseThread
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {

                        m_projectsComboBox.removeAllItems();

                        int nbProjects = projectList.size();
                        if (nbProjects > 0) {
                            m_projectsComboBox.addItem(new ProjectItem(null, null)); // Null Project corresponds to Select a Project Item
                        }
                        for (int i = 0; i < nbProjects; i++) {
                            ProjectIdentificationData identificationData = (ProjectIdentificationData) projectList.get(i);
                            m_projectsComboBox.addItem(new ProjectItem(identificationData, new ProjectQuantitationData(identificationData.getProject())));
                        }

                        m_addProjectButton.setEnabled(true);

                        m_projectsComboBox.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ProjectItem item = (ProjectItem) m_projectsComboBox.getSelectedItem();
                                getProjectExplorerPanel().selectProject(item);

                                if ((item != null) && (item.getProjectIdentificationData() != null) && (!item.isChanging())) {
                                    m_editProjectButton.setEnabled(true);
                                    m_propertiesProjectButton.setEnabled(true);
                                    m_clearProjectButton.setEnabled(true);

                                    Preferences preferences = NbPreferences.root();
                                    preferences.put("DefaultSelectedProject", item.getProjectIdentificationData().getName());
                                } else {
                                    m_editProjectButton.setEnabled(false);
                                    m_propertiesProjectButton.setEnabled(false);
                                    m_clearProjectButton.setEnabled(false);
                                }

                            }
                        });

                        Preferences preferences = NbPreferences.root();
                        String defaultProjectName = preferences.get("DefaultSelectedProject", null);
                        if (defaultProjectName != null) {
                            int count = m_projectsComboBox.getItemCount();
                            for (int i = 0; i < count; i++) {
                                ProjectItem item = m_projectsComboBox.getItemAt(i);
                                if ((item != null) && (item.toString().compareTo(defaultProjectName) == 0)) {
                                    m_projectsComboBox.setSelectedItem(item);
                                }
                            }
                        }

                        ConnectAction.setConnectionType(false, true);
                    }
                });

            }
        };

        DatabaseProjectTask task = new DatabaseProjectTask(callback);
        task.initLoadProject(DatabaseDataManager.getDatabaseDataManager().getLoggedUserName(), projectList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    public void selectProject(ProjectItem projectItem) {

        if ((projectItem == null) || (!projectItem.isActive())) {
            m_identificationTreeScrollPane.setViewportView(null);
            m_quantitationTreeScrollPane.setViewportView(null);
            
            if ((projectItem != null) && (!projectItem.isActive())) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        InfoDialog infoDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Project Deleted", "Databases corresponding to this project have been deleted.\nAsk to your Administrator to restore them.");
                        infoDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
                        infoDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                        infoDialog.setVisible(true);
                    }
                });

            }
            
            return;
        }

        ProjectIdentificationData projectIdentificationData = projectItem.getProjectIdentificationData();
        ProjectQuantitationData projectQuantitationData = projectItem.getProjectQuantitationData();

        if ((!projectItem.isChanging()) && (projectIdentificationData != null)) {
            IdentificationTree identificationTree = IdentificationTree.getTree(projectIdentificationData);
            QuantitationTree quantitationTree = QuantitationTree.getTree(projectQuantitationData);

            m_identificationTreeScrollPane.setViewportView(identificationTree);
            m_quantitationTreeScrollPane.setViewportView(quantitationTree);

        } else {
            m_identificationTreeScrollPane.setViewportView(null);
            m_quantitationTreeScrollPane.setViewportView(null);
        }
    }

    public class ProjectComboboxRenderer extends BasicComboBoxRenderer {

        public ProjectComboboxRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            ProjectItem projectItem = (ProjectItem) value;

            if ((index == -1) && (projectItem == null)) {
                return this;
            }

            if (projectItem == null) {
                l.setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS));
                l.setText("Loading Projects...");
            } else {
                if (projectItem.getProjectIdentificationData() == null) {
                    l.setIcon(null);
                } else {
                    if (projectItem.isChanging()) {
                        l.setIcon(IconManager.getIconWithHourGlass(IconManager.IconType.PROJECT));
                    } else {
                        Project project = projectItem.getProjectIdentificationData().getProject();
                        
                        JsonParser parser = new JsonParser();
                        
                        String serializedProperties = project.getSerializedProperties();
                        boolean isActive = true;
                        if (serializedProperties != null) {
                            JsonObject jsonObject = parser.parse(serializedProperties).getAsJsonObject();
                            JsonPrimitive isActiveObject = jsonObject.getAsJsonPrimitive("is_active");
                            isActive = isActiveObject.getAsBoolean();
                        }
                        projectItem.setIsActive(isActive);
                        
                        if (! isActive) {
                            l.setIcon(IconManager.getIcon(IconManager.IconType.PROJECT_DELETED));
                        } else if (DatabaseDataManager.getDatabaseDataManager().ownProject(project)) {
                            l.setIcon(IconManager.getIcon(IconManager.IconType.PROJECT));
                        } else {
                            l.setIcon(IconManager.getIcon(IconManager.IconType.PROJECT_READ_ONLY));
                        }
                        
                       

                    }
                }
            }
            return this;
        }
    }

    public static class ProjectItem implements PropertiesProviderInterface {

        private ProjectIdentificationData m_projectIdentificationData;
        private ProjectQuantitationData m_projectQuantitationData;
        private boolean m_isChanging = false;
        private boolean m_active = true;

        public ProjectItem(ProjectIdentificationData projectIdentificationData, ProjectQuantitationData projectQuantitationData) {
            m_projectIdentificationData = projectIdentificationData;
            m_projectQuantitationData = projectQuantitationData;
        }

        public ProjectIdentificationData getProjectIdentificationData() {
            return m_projectIdentificationData;
        }

        public ProjectQuantitationData getProjectQuantitationData() {
            return m_projectQuantitationData;
        }

        public void setIsChanging(boolean v) {
            m_isChanging = v;
        }

        public boolean isChanging() {
            return m_isChanging;
        }

        @Override
        public String toString() {
            if (m_projectIdentificationData == null) {
                return "< Select a Project >";
            }
            return m_projectIdentificationData.getName();
        }

        public void setIsActive(boolean v) {
            m_active = v;
        }

        public boolean isActive() {
            return m_active;
        }
        
        
        @Override
        public Sheet createSheet() {
            Project p = m_projectIdentificationData.getProject();

            Sheet sheet = Sheet.createDefault();

            try {

                Sheet.Set propGroup = Sheet.createPropertiesSet();

                Node.Property prop = new PropertySupport.Reflection<>(p, Long.class, "getId", null);
                prop.setName("id");
                propGroup.put(prop);

                prop = new PropertySupport.Reflection<>(p, String.class, "getName", null);
                prop.setName("name");
                propGroup.put(prop);

                prop = new PropertySupport.Reflection<>(p, String.class, "getDescription", null);
                prop.setName("description");
                propGroup.put(prop);

                sheet.put(propGroup);

            } catch (NoSuchMethodException e) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " properties error ", e);
            }

            return sheet;
        }

        @Override
        public void loadDataForProperties(Runnable callback) {
            // nothing to do
        }
    }
}
