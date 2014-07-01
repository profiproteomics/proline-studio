package fr.proline.studio.rsmexplorer.node.xic;

import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProjectTask;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.DesignTree;
import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.utils.IconManager;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openide.nodes.Sheet;

/**
 *
 * @author JM235353
 */
public class RSMRunNode extends RSMNode {

    public RSMRunNode(AbstractData data, Long projectId, Long rsetId) {
        super(NodeTypes.RUN, data);

        setIsChanging(true);

        final String[] path = new String[1];

        final RSMRunNode _this = this;

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

                ((DefaultTreeModel) DesignTree.getDesignTree().getModel()).nodeChanged(_this);
                
                search(searchString);
            }
        };


        // ask asynchronous loading of data
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initLoadPeakListPathForRset(projectId, rsetId, path);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    
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

                if (m_rawFileList.isEmpty()) {
                    ((RunInfoData) getData()).setMessage("<html><font color='#FF0000'>No Raw File found, select one</font></html>");
                } else if (m_rawFileList.size() == 1) {
                    ((RunInfoData) getData()).setRawFile(m_rawFileList.get(0));
                } else {
                    ((RunInfoData) getData()).setMessage("<html><font color='#FF0000'>Multiple Raw Files found, select one</font></html>");
                }
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
    public RSMNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }
}
