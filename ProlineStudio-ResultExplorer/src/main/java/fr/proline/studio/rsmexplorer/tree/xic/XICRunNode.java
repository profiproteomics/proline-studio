package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.utils.IconManager;
import java.io.File;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.openide.nodes.Sheet;

/**
 * Tree Node representing a Run
 * @author JM235353
 */
public class XICRunNode extends AbstractNode {

    private DefaultTreeModel m_treeModel = null;
    
    public XICRunNode(AbstractData data) {
        super(NodeTypes.RUN, data);
    }

    public void init(final DDataset dataset, DefaultTreeModel treeModel) {
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
                            warnParent(false);

                        } else {
                            search(dataset);
                        }
                    } else {
                        // it failed !
                        m_treeModel.removeNodeFromParent((MutableTreeNode) xicRunNode.getParent());
                    }
                    
                }
            };

            DatabaseRunsTask task = new DatabaseRunsTask(callback);
            task.initLoadRawFile(dataset.getId(), rawfileFounds, runOut);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        } else {
            search(dataset);
        }
    }
    
    private void search(DDataset dataset) {

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

                search(searchString);
            }
        };

        // ask asynchronous loading of data
        Long projectId = dataset.getProject().getId();
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initLoadPeakListPathForRset(projectId, rsetId, path);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    
    /*
    public void initOLD(DDataset dataset, DefaultTreeModel treeModel) {
        Long rsetId = dataset.getResultSetId();
        
        m_treeModel = treeModel;
        
        // look if we find a Raw File
        if (dataset.getType() == Dataset.DatasetType.IDENTIFICATION) {
            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                IdentificationDataset identificationDataset = entityManagerUDS.find(IdentificationDataset.class, dataset.getId());
                if (identificationDataset != null) {
                    RunInfoData runInfoData = ((RunInfoData) getData());
                    RawFile rawFile = identificationDataset.getRawFile();
                    if (rawFile != null) {
                        runInfoData.getRawFileSouce().setLinkedRawFile(rawFile);
                        runInfoData.setRun(identificationDataset.getRun());
                        //runInfoData.getRawFileSouce().setRawFileOnDisk(rawFile.getDirectory()+File.separator+rawFile.getRawFileName()); JPM.RUNINFODATA ???
                        //runInfoData.setRunInfoInDatabase(true);
                        warnParent(false);
                        // everything is set
                        return;
                    }
                }
                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
            } finally {
                entityManagerUDS.close();
            }
        }

        setIsChanging(true);

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
                
                search(searchString);
            }
        };


        // ask asynchronous loading of data
        Long projectId = dataset.getProject().getId();
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initLoadPeakListPathForRset(projectId, rsetId, path);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }*/
    
    
    private void search(String searchString) {
       
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
                    warnParent(true);
                    runInfoData.setMessage("<html><font color='#FF0000'>No Raw File found, select one</font></html>");
                } else if (m_rawFileList.size() == 1) {
                    warnParent(false);

                    // TODO : how to choose the right rawfile or run instead of the first one ??
                    
                    RawFile rawFile = m_rawFileList.get(0);
                    runInfoData.getRawFileSouce().setSelectedRawFile(rawFile);
                    runInfoData.setRun(rawFile.getRuns().get(0));
                    //runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+rawFile.getRawFileName());  //JPM.RUNINFODATA
                    //runInfoData.setRunInfoInDatabase(true);
                } else {
                    runInfoData.setPotentialRawFiles(m_rawFileList);
                    warnParent(true);
                    runInfoData.setMessage("<html><font color='#FF0000'>Multiple Raw Files found, select one</font></html>");
                }
            }
        };

        
        // ask asynchronous loading of data
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initSearchRawFile(searchString, m_rawFileList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    public final void warnParent(boolean error) {
        TreeNode parentNode = getParent();
        if (parentNode == null) {
            return;
        }
        if (parentNode instanceof XICBiologicalSampleAnalysisNode) {
            ((XICBiologicalSampleAnalysisNode) parentNode).setChildError(m_treeModel, error);
        }
    }
    
    @Override
    public String toString() {
        AbstractData data = getData();
        if (data == null) {
            return "loading";
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

    public void setRawFile(final File selectedFile) {

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
                
                warnParent(false);
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
