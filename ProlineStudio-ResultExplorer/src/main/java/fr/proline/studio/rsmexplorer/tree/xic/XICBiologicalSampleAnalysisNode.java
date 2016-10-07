package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openide.nodes.Sheet;

/**
 * Biological Sample Analysis Node used in XIC Design Tree
 * @author JM235353
 */
public class XICBiologicalSampleAnalysisNode extends DataSetNode {

    String m_qcName ; //Name of the quantChannel associated to this BiologicalSampleAnalysis
           
    public XICBiologicalSampleAnalysisNode(AbstractData data) {
        super(AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS, data);
        m_qcName = ((DataSetData) data).getName();        
    }

    
    public void setQuantChannelName(String qcName) {
        m_qcName = qcName;
    }
    
    public String getQuantChannelName(){
        return m_qcName;
    }
    
    @Override
    public String getToolTipText() {
        int nbChild = getChildCount();
        if (nbChild == 1) {
            AbstractNode childNode = (AbstractNode) getChildAt(0);
            if (childNode.getType() == AbstractNode.NodeTypes.RUN) {
                String peakList = ((XICRunNode)childNode).getPeakListPath();
                if (peakList == null) {
                    return null;
                }
                return "PeakList : "+peakList;
            }
        }
        return null;
    }
    
    @Override
    public boolean hasResultSummary() {
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSummaryId() != null);
    }

    @Override
    public Long getResultSummaryId() {
        return ((DataSetData) getData()).getDataset().getResultSummaryId();
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.DATASET_RSM);
    }

    @Override
    public Sheet createSheet() {
        return super.createSheet();
    }

    @Override
    public AbstractNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
        super.loadDataForProperties(callback);
    }
    
    @Override
    public boolean canBeDeleted() {
        return true;
    }
    
    @Override
    public String toString() {
        //display Quant Chanel Name
        if(m_qcName != null && !m_qcName.trim().isEmpty())
            return m_qcName;
        return super.toString();
    }
    
}
