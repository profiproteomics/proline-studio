package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openide.nodes.Sheet;

/**
 * Biological Sample Analysis Node used in XIC Design Tree
 * @author JM235353
 */
public class XICBiologicalSampleAnalysisNode extends AbstractNode {

    boolean m_hasError = true;
    
    public XICBiologicalSampleAnalysisNode(AbstractData data) {
        super(AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS, data);
    }
    
    public void setChildError(DefaultTreeModel m_treeModel, boolean error) {
        if (m_hasError ^ error) {
            m_hasError = error;
            m_treeModel.nodeChanged(this);
        }
        
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
        if (m_hasError) {
            return getIcon(IconManager.IconType.RSM_ERROR);
        } else {
            return getIcon(IconManager.IconType.RSM);
        }
    }

    @Override
    public Sheet createSheet() {
        return null;
    }

    @Override
    public AbstractNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }
    
}
