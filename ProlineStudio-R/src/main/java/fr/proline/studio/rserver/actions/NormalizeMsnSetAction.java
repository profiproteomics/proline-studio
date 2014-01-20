package fr.proline.studio.rserver.actions;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rserver.data.MsnSetData;
import fr.proline.studio.rserver.data.RExpression;
import fr.proline.studio.rserver.node.RMsnSetNode;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class NormalizeMsnSetAction extends AbstractRAction {

    public NormalizeMsnSetAction() {
        super("Normalize");
    }
    
    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        
        RTree tree = RTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        final RMsnSetNode parentNode = (RMsnSetNode) selectedNodes[0];
        final RMsnSetNode resultNode = new RMsnSetNode(new MsnSetData("Normalized"));
        final RExpression expression = new RExpression();
        resultNode.setRExpression(expression);
        resultNode.getData().setLongDisplayName("Normalize("+parentNode.getLongDisplayName()+")");
        resultNode.setIsChanging(true);
        treeModel.insertNodeInto(resultNode, parentNode, parentNode.getChildCount());
        // expand parent node if needed
        final TreePath pathToExpand = new TreePath(parentNode.getPath());
        if (!tree.isExpanded(pathToExpand)) {
            tree.expandPath(pathToExpand);
        }
        
        
        
        final String[] RVariable = new String[1];

        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    resultNode.setIsChanging(false);
                    treeModel.nodeChanged(resultNode);
                } else {
                    treeModel.removeNodeFromParent(resultNode);
                }
            }
        };

        NormalizeMsnSetTask task = new NormalizeMsnSetTask(callback, parentNode.toString(), parentNode.getRExpression().getRVariable(), expression);
        AccessServiceThread.getAccessServiceThread().addTask(task);
        
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        
        RNode parentNode = selectedNodes[0];
        
        setEnabled(parentNode.getType() == RNode.NodeTypes.MSN_SET);
    }
    
    
    
       
    public class NormalizeMsnSetTask extends AbstractServiceTask {

        private RExpression m_RExpression;
        private String m_srcRVariable;

        public NormalizeMsnSetTask(AbstractServiceCallback callback, String name, String srcRVariable, RExpression expression) {
            super(callback, true /** synchronous */, new TaskInfo("Normalize MsnSet " + name, false, TASK_LIST_INFO));

            m_srcRVariable = srcRVariable;
            m_RExpression = expression;
        }

        @Override
        public boolean askService() {

            RServerManager serverR = RServerManager.getRServerManager();

            try {

                String var = serverR.getNewVariableName("msnSet");
                m_RExpression.setRVariable(var);
                String code = var+" <- NormalizeCenterReduction(" + m_srcRVariable + ")";
                m_RExpression.setRExpression(code);
                serverR.eval(code);
                
            } catch (RServerManager.RServerException ex) {
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
