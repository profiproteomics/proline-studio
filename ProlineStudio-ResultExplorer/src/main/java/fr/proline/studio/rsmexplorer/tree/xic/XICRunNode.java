package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.openide.nodes.Sheet;

/**
 * Tree Node representing a Run
 *
 * @author JM235353
 */
public class XICRunNode extends AbstractNode {

    private DefaultTreeModel m_treeModel = null;

    public XICRunNode(AbstractData data) {
        super(NodeTypes.RUN, data);
    }

    public void init(final DDataset dataset, DefaultTreeModel treeModel, final AbstractTableModel tableModel) {
        m_treeModel = treeModel;

        setIsChanging(true);

        // look if we find a Raw File
        if (dataset.getType() == Dataset.DatasetType.IDENTIFICATION) {

            final ArrayList<RawFile> rawfileFounds = new ArrayList<>(1);
            final Run[] runOut = new Run[1];
            final XICRunNode xicRunNode = this;

            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        if (!rawfileFounds.isEmpty()) {

                            setIsChanging(false);
                            m_treeModel.nodeChanged(xicRunNode);

                            RawFile rawFile = rawfileFounds.get(0);
                            RunInfoData runInfoData = ((RunInfoData) getData());
                            runInfoData.getRawFileSouce().setLinkedRawFile(rawFile);

                            runInfoData.getRawFileSouce().setLinkedRawFile(rawFile);
                            runInfoData.setRun(runOut[0]);

                        } else {
                            search(dataset, tableModel);
                        }
                    } else {
                        // it failed !
                        m_treeModel.removeNodeFromParent((MutableTreeNode) xicRunNode.getParent());
                    }

                    if (tableModel != null) {
                        // a table model display data in this Xic Run Node, so it must be updated
                        tableModel.fireTableDataChanged();
                    }

                }
            };

            DatabaseRunsTask task = new DatabaseRunsTask(callback);
            task.initLoadRawFile(dataset.getId(), rawfileFounds, runOut);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        } else {
            search(dataset, tableModel);
        }
    }

    private void search(DDataset dataset, final AbstractTableModel tableModel) {

        Long rsetId = dataset.getResultSetId();

        final String[] path = new String[1];

        final XICRunNode _this = this;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                //setIsChanging(false);

                String searchString = path[0];
                ((RunInfoData) getData()).setPeakListPath(searchString);

                if ((searchString == null) || (searchString.isEmpty())) {
                    searchString = "*";
                } else {
                    searchString = "*" + searchString + "*";
                }

                ((RunInfoData) getData()).setMessage("Search " + searchString);

                ((DefaultTreeModel) XICDesignTree.getDesignTree().getModel()).nodeChanged(_this);

                search(searchString, tableModel);
            }
        };

        // ask asynchronous loading of data
        Long projectId = dataset.getProject().getId();
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initLoadPeakListPathForRset(projectId, rsetId, path);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    private void search(String searchString, final AbstractTableModel tableModel) {

        final ArrayList<RawFile> m_rawFileList = new ArrayList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                setIsChanging(false);

                RunInfoData runInfoData = ((RunInfoData) getData());

                if (m_rawFileList.isEmpty()) {
                    runInfoData.setMessage("<html><font color='#FF0000'>Missing Raw File</font></html>");
                } else if (m_rawFileList.size() == 1) {
                    RawFile rawFile = m_rawFileList.get(0);
                    runInfoData.getRawFileSouce().setSelectedRawFile(rawFile);
                    runInfoData.setRun(rawFile.getRuns().get(0));
                    //runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+rawFile.getRawFileName());  //JPM.RUNINFODATA
                    //runInfoData.setRunInfoInDatabase(true);
                } else {
                    runInfoData.setPotentialRawFiles(m_rawFileList);
                    if (searchString.equalsIgnoreCase("*")) {
                        runInfoData.setMessage("<html><font color='#FF0000'>Unavailable Peaklist</font></html>");
                    } else {
                        runInfoData.setMessage("<html><font color='#FF0000'>Multiple Raw Files</font></html>");
                    }
                }

                tableModel.fireTableDataChanged();
            }
        };

        // ask asynchronous loading of data
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initSearchRawFile(searchString, m_rawFileList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    @Override
    public String toString() {
        AbstractData data = getData();
        if (data == null) {
            return "Loading";
        }
        return super.toString();
    }

    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.FILE); //JPM.TODO : to be changed
    }

    @Override
    public Sheet createSheet() {
        return null;
    }

    @Override
    public AbstractNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }

    public void setRawFile(final File selectedFile, ActionListener doneCallback) {

        // we search the raw file in the database, if we found it, we set this one
        // if we do not find it, we use the one choosed by the user
        setIsChanging(true);
        m_treeModel.nodeChanged(this);

        String searchString = selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.'));

        final ArrayList<RawFile> m_rawFileList = new ArrayList<>();
        final TreeNode _this = this;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                RunInfoData runInfoData = ((RunInfoData) getData());
                if (m_rawFileList.size() == 1) {

                    // we have found the raw file in the database, we use this one
                    RawFile rawFile = m_rawFileList.get(0);
                    runInfoData.getRawFileSouce().setSelectedRawFile(rawFile);
                    runInfoData.setRun(rawFile.getRuns().get(0));
                    //runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+m_rawFileList.get(0).getRawFileName()); //JPM.RUNINFODATA
                    //runInfoData.setRunInfoInDatabase(true);
                } else {
                    // we use the file choosen by the user
                    /*RawFile rawFile = new RawFile();
                     rawFile.setDirectory(selectedFile.getPath());
                     rawFile.setRawFileName(selectedFile.getName());
                     runInfoData.setRawFile(rawFile);
                     runInfoData.setRawFilePath(selectedFile.getPath());
                     runInfoData.setRunInfoInDatabase(false);*/  //JPM.RUNINFODATA
                    runInfoData.getRawFileSouce().setRawFileOnDisk(selectedFile);
                }
                setIsChanging(false);
                m_treeModel.nodeChanged(_this);

                if (doneCallback != null) {
                    doneCallback.actionPerformed(null);
                }

            }
        };

        // ask asynchronous loading of data
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initSearchRawFile(searchString, m_rawFileList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    public String getPeakListPath() {
        RunInfoData data = ((RunInfoData) getData());
        if (data == null) {
            return null;
        }
        return data.getPeakListPath();
    }

}
