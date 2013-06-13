package fr.proline.studio.rsmexplorer.actions;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.pattern.DataParameter;
import fr.proline.studio.rsmexplorer.gui.dialog.DataBoxChooserDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class DisplayUserWindowAction extends AbstractRSMAction {
    
    public DisplayUserWindowAction() {
        super(NbBundle.getMessage(DisplayRsmProteinSetsAction.class, "CTL_DisplayUserWindowAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
        // only one node selected for this action
        RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[0];
        
        ArrayList<DataParameter> outParameters = new ArrayList<>();
        if (dataSetNode.hasResultSet()) {
            DataParameter outParameter = new DataParameter();
            outParameter.addParameter(ResultSet.class, false);
            outParameters.add(outParameter);
        }
        if (dataSetNode.hasResultSummary()) {
            DataParameter outParameter = new DataParameter();
            outParameter.addParameter(ResultSummary.class, false);
            outParameters.add(outParameter);
        }

        final Dataset dataset = ((DataSetData) dataSetNode.getData()).getDataset();

        DataBoxChooserDialog chooser = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), outParameters);
        //JPM.TODO chooser.
        
        //JPM.TODO if (chooser)
        
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
         int nbSelectedNodes = selectedNodes.length;

        // only one node can be selected
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
         RSMNode node = selectedNodes[0];
        if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }
        
        setEnabled(true);
    }
    
}
