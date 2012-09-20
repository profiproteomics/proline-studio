package fr.proline.studio.rsmexplorer.node;


import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ResultSummaryData;
import java.awt.Image;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Node for a ResultSummary
 * @author JM235353
 */
public class RSMResultSummaryNode extends RSMNode {

    private static Image icon = ImageUtilities.loadImage("fr/proline/studio/rsmexplorer/images/resultSummary.png");

    public RSMResultSummaryNode(Children children, Lookup lookup, AbstractData data) {
        super(children, lookup, NodeTypes.RESULT_SUMMARY, data);
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canRename() {
        return true;
    }
    
    public ResultSummary getResultSummary() {
        return ((ResultSummaryData) data).getResultSummary();
    }
}
