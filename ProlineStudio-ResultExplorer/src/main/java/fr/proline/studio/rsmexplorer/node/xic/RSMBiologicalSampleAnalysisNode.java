package fr.proline.studio.rsmexplorer.node.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Biological Sample Analysis Node used in XIC Design Tree
 * @author JM235353
 */
public class RSMBiologicalSampleAnalysisNode extends RSMNode {

    public RSMBiologicalSampleAnalysisNode(AbstractData data) {
        super(RSMNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS, data);
    }
    
    public boolean hasResultSummary() {
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSummaryId() != null);
    }

    public Long getResultSummaryId() {
        return ((DataSetData) getData()).getDataset().getResultSummaryId();
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.RSM);
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
