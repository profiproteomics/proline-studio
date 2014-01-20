package fr.proline.studio.rserver.actions;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rserver.data.RGraphicData;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import fr.proline.studio.rserver.node.RGraphicNode;
import fr.proline.studio.rserver.node.RMsnSetNode;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class BoxPlotAction extends AbstractRAction {

    public BoxPlotAction() {
        super("Box Plot");
    }
    
    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        
        RTree tree = RTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        final RMsnSetNode parentNode = (RMsnSetNode) selectedNodes[0];
        final RGraphicNode resultNode = new RGraphicNode(new RGraphicData("Box Plot"));
        resultNode.getData().setLongDisplayName("BoxPlot("+parentNode.getLongDisplayName()+")");
        resultNode.setIsChanging(true);
        treeModel.insertNodeInto(resultNode, parentNode, parentNode.getChildCount());
        // expand parent node if needed
        final TreePath pathToExpand = new TreePath(parentNode.getPath());
        if (!tree.isExpanded(pathToExpand)) {
            tree.expandPath(pathToExpand);
        }

        final BufferedImage[] image = new BufferedImage[1];

        
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    resultNode.setIsChanging(false);
                    ((RGraphicData)resultNode.getData()).setImage(image[0]);
                    treeModel.nodeChanged(resultNode);
                    
                    ImageViewerTopComponent win = new ImageViewerTopComponent(resultNode.getLongDisplayName(), image[0]);
                    win.open();
                    win.requestActive();
                    
                } else {
                    treeModel.removeNodeFromParent(resultNode);
                }
            }
        };

        GraphicTask task = new GraphicTask(callback, parentNode.toString(), parentNode.getRExpression().getRVariable(), image);
        AccessServiceThread.getAccessServiceThread().addTask(task);
        
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        
        RNode parentNode = selectedNodes[0];
        
        setEnabled(parentNode.getType() == RNode.NodeTypes.MSN_SET);
    }
    
    
    
       
    public class GraphicTask extends AbstractServiceTask {

        private String m_srcRVariable;
        private BufferedImage[] m_image;

        public GraphicTask (AbstractServiceCallback callback, String name, String srcRVariable, BufferedImage[] image) {
            super(callback, true /** synchronous */, new TaskInfo("BoxPlot " + name, false, TASK_LIST_INFO));

            m_srcRVariable = srcRVariable;
            m_image = image;
        }

        @Override
        public boolean askService() {

            String timestamp = String.valueOf(System.currentTimeMillis());
            String boxPlotFileName = "boxPlot"+timestamp+".png";
            
            RServerManager serverR = RServerManager.getRServerManager();

            try {

                String code = "boxplotAsPng(" + m_srcRVariable + ",'"+boxPlotFileName+"')";
                serverR.eval(code);
                
                // download box plot png file
                File boxPlotTempFile = File.createTempFile("boxPlot", ".png"); 
                boxPlotTempFile.deleteOnExit();
                serverR.downloadFile(boxPlotFileName, boxPlotTempFile.getAbsolutePath());
                
                // Create the image
                m_image[0] = ImageIO.read(boxPlotTempFile);
                
            } catch (RServerManager.RServerException | java.io.IOException ex) {
                m_taskError = new TaskError(ex);
                return false;
            }


            return true;
        }

        @Override
        public AbstractServiceTask.ServiceState getServiceState() {
            // always returns STATE_DONE because it is a synchronous service
            return AbstractServiceTask.ServiceState.STATE_DONE;
        }
    }
    
    
}
