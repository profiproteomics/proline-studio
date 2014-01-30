package fr.proline.studio.rserver.actions;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rserver.command.*;
import fr.proline.studio.rserver.data.RGraphicData;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import fr.proline.studio.rserver.node.RGraphicNode;
import fr.proline.studio.rserver.node.RMsnSetNode;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import java.awt.Image;
import java.awt.image.BufferedImage;

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

        final RVar inVar = parentNode.getVar(); 
        final RVar outVar = new RVar();

        final PlotCommand plotCommand = new PlotCommand("Box Plot", "Box Plot", "BoxPlot("+AbstractCommand.PEVIOUS_NODE+")", "GetGraphicsAsPng('"+AbstractCommand.FILE_ON_SERVER+"', boxPlotEDyP("+AbstractCommand.IN_VARIABLE+", 'testBoxplot'), 500, 500)");
        
        

        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    
                    Image img = (Image) outVar.getAttachedData();
                    
                    resultNode.setCommand(plotCommand);
                    resultNode.setIsChanging(false);
                    ((RGraphicData)resultNode.getData()).setImage(img);
                    treeModel.nodeChanged(resultNode);
                    
                    ImageViewerTopComponent win = new ImageViewerTopComponent(resultNode.getLongDisplayName(), img);
                    win.open();
                    win.requestActive();
                    
                } else {
                    treeModel.removeNodeFromParent(resultNode);
                }
            }
        };

        CommandTask task = new CommandTask(callback, outVar, inVar, plotCommand);
        AccessServiceThread.getAccessServiceThread().addTask(task);
        
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        
        RNode parentNode = selectedNodes[0];
        
        setEnabled(parentNode.getType() == RNode.NodeTypes.MSN_SET);
    }
    
    
 
}
