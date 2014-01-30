package fr.proline.studio.rserver.command;

import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.rserver.data.MsnSetData;
import fr.proline.studio.rserver.data.RGraphicData;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import fr.proline.studio.rserver.node.RGraphicNode;
import fr.proline.studio.rserver.node.RMsnSetNode;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class RScenario {
    
    private ArrayList<AbstractCommand> m_commandList = new ArrayList<>();
    
    private String m_name;

    
    public RScenario(String name) {
        m_name = name;
    }
    
    public RScenario() {
        
    }
    
    public void addCommand(AbstractCommand cmd) {
        m_commandList.add(cmd);
    }
    
    public void write(StringBuilder sb) {
        
        sb.append(m_name);
        sb.append(AbstractCommand.CMD_SEPARATOR);
        int nbCommands = m_commandList.size();
        sb.append(nbCommands);
        sb.append(AbstractCommand.CMD_SEPARATOR);
        for (int i=0;i<nbCommands;i++) {
            AbstractCommand cmd = m_commandList.get(i);
            cmd.write(sb);
        }
    }

    public void read(String script) {

        int indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR);
        if (indexOfSeparator == -1) {
            // should not happen
            return;
        }
        m_name = script.substring(0, indexOfSeparator);
        
        int indexStart = indexOfSeparator+AbstractCommand.CMD_SEPARATOR.length();
        
        indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        String nbCommandsS =  script.substring(indexStart, indexOfSeparator);
        
        indexStart = indexOfSeparator+AbstractCommand.CMD_SEPARATOR.length();
        
        int nbCommands = Integer.valueOf(nbCommandsS);
        
        if (nbCommands == 0) {
            // should not happen
            return;
        }
        
        for (int i = 0; i < nbCommands; i++) {
            indexStart = AbstractCommand.read(this, script, indexStart);
        }

    }
    
    
    public void play(RNode parentNode) {
        executeCmd(parentNode, 0);
    }
    
    private void executeCmd(RNode parentNode, final int cmdIndex) {
        
        if (cmdIndex>=m_commandList.size()) {
            return;
        }
        
        final AbstractCommand cmd = m_commandList.get(cmdIndex);
        
        int resultType = cmd.getResultType();
        
        RNode resultNode = null;
        switch (resultType) {
            case AbstractCommand.CMD_GENERIC:
                resultNode = new RMsnSetNode(new MsnSetData(cmd.getNodeName()), false);
                break;
            case AbstractCommand.CMD_PLOT:
                resultNode = new RGraphicNode(new RGraphicData(cmd.getNodeName()));
                break;
        }
        final RNode _resultNode = resultNode;
        
        RTree tree = RTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        
        //resultNode.getData().setLongDisplayName("Normalize("+parentNode.getLongDisplayName()+")");
        resultNode.setIsChanging(true);
        treeModel.insertNodeInto(resultNode, parentNode, parentNode.getChildCount());
        // expand parent node if needed
        final TreePath pathToExpand = new TreePath(parentNode.getPath());
        if (!tree.isExpanded(pathToExpand)) {
            tree.expandPath(pathToExpand);
        }
        
        
        final RVar inVar = parentNode.getVar(); 
        final RVar outVar = new RVar();


        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    _resultNode.setIsChanging(false);
                    _resultNode.setCommand(cmd);
                    _resultNode.setVar(outVar);

                    if (outVar.getType() == RVar.GRAPHIC) {
                        Image img = (Image) outVar.getAttachedData();
                        ((RGraphicData) _resultNode.getData()).setImage(img);
                    }
                    
                    outVar.setFullDisplay(cmd.getLongDisplayName(_resultNode.getParent().toString()));
                    _resultNode.setLongDisplayName(cmd.getLongDisplayName(_resultNode.getParent().toString()));
                    //_resultNode.getData().setName(cmd.getNodeName());
                    
                    treeModel.nodeChanged(_resultNode);
                    
                    if (_resultNode.getType() == RNode.NodeTypes.GRAPHIC) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                RVar var = _resultNode.getVar();
                                Image img = ((RGraphicData)_resultNode.getData()).getImage();
                                ImageViewerTopComponent win = new ImageViewerTopComponent(var.getFullDisplay(), img);
                                win.open();
                                win.requestActive();
                            }
                        });
                    }
                        
                    executeCmd(_resultNode, cmdIndex+1);
                    
                } else {
                    treeModel.removeNodeFromParent(_resultNode);
                }
            }
        };

        CommandTask task = new CommandTask(callback, outVar, inVar, cmd );
        
        //NormalizeMsnSetTask task = new NormalizeMsnSetTask(callback, parentNode.toString(), parentNode.getRExpression().getRVariable(), expression);
        AccessServiceThread.getAccessServiceThread().addTask(task);
    }
}
