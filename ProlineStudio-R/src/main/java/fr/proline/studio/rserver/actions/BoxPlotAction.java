package fr.proline.studio.rserver.actions;


import fr.proline.studio.rserver.command.*;
import fr.proline.studio.rserver.data.RGraphicData;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import fr.proline.studio.rserver.node.RGraphicNode;
import fr.proline.studio.rserver.node.RNode;



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
        
        
        RScenario scenario = new RScenario();

        PlotCommand plotCommand = new PlotCommand("Box Plot", "Box Plot", "BoxPlot(" + AbstractCommand.PREVIOUS_NODE + ")", "GetGraphicsAsPng('" + AbstractCommand.FILE_ON_SERVER + "', boxPlotEDyP(" + AbstractCommand.IN_VARIABLE + ", '" + "BoxPlot(" + AbstractCommand.PREVIOUS_LONG_DISPLAY_NAME + ")" + "'), 500, 500)");
        scenario.addCommand(plotCommand);

        scenario.play(selectedNodes[0]);



        
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        
        RNode parentNode = selectedNodes[0];
        
        setEnabled(parentNode.getType() == RNode.NodeTypes.MSN_SET);
    }
    
    
 
}
