package fr.proline.studio.rsmexplorer.node;


import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ResultSummaryData;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * Node for a ResultSummary
 * @author JM235353
 */
public class RSMResultSummaryNode extends RSMNode {

    private static ImageIcon icon = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/resultSummary.png", false);

    public RSMResultSummaryNode(AbstractData data) {
        super(NodeTypes.RESULT_SUMMARY, data);
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }

    /*@Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canRename() {
        return true;
    }*/
    
 
    public ResultSummary getResultSummary() {
        return ((ResultSummaryData) getData()).getResultSummary();
    }
}
