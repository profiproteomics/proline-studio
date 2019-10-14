/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import javax.swing.tree.MutableTreeNode;
import org.openide.nodes.Sheet;

/**
 * Biological Sample Analysis Node used in XIC Design Tree
 *
 * @author JM235353
 */
public class XICBiologicalSampleAnalysisNode extends DataSetNode {

    public enum SpectrumVerificationStatus {

        NOT_VERIFIED, SUCCESSFULLY_VERIFIED, UNSUCCESSFULLY_VERIFIED
    }

    private String m_qcName; //Name of the quantChannel associated to this BiologicalSampleAnalysis
    private XICRunNode m_xicRunNode;
    private SpectrumVerificationStatus m_verificationStatus;

    public XICBiologicalSampleAnalysisNode(AbstractData data) {
        super(AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS, data);
        m_qcName = ((DataSetData) data).getName();
        m_verificationStatus = SpectrumVerificationStatus.NOT_VERIFIED;
    }

    public void setVerificationStatus(SpectrumVerificationStatus status) {
        m_verificationStatus = status;
    }

    public SpectrumVerificationStatus getVerificationStatus() {
        return m_verificationStatus;
    }

    //HACK so that we can access potential raw files when needed.
    public void addXicRunNode(XICRunNode xicRunNode, boolean addToChild) {
        m_xicRunNode = xicRunNode;
        if(addToChild)
            super.add(m_xicRunNode);
    }
   
    @Override
    public void add(MutableTreeNode newChild) {
        if(!XICRunNode.class.isInstance(newChild))
            throw new IllegalArgumentException("BiologicalSampleAnalysisNode only accept RunNode as child. (invalid "+newChild+" node )");
        m_xicRunNode = (XICRunNode) newChild;
        super.add(m_xicRunNode);    
    }
   
    public XICRunNode getXicRunNode() {
        return m_xicRunNode;
    }

    public void setQuantChannelName(String qcName) {
        m_qcName = qcName;
    }

    public String getQuantChannelName() {
        return m_qcName;
    }

    @Override
    public String getToolTipText() {
        int nbChild = getChildCount();
        if (nbChild == 1) {
            AbstractNode childNode = (AbstractNode) getChildAt(0);
            if (childNode.getType() == AbstractNode.NodeTypes.RUN) {
                String peakList = ((XICRunNode) childNode).getPeakListPath();
                if (peakList == null) {
                    return null;
                }
                return "PeakList : " + peakList;
            }
        }
        return null;
    }

    @Override
    public boolean hasResultSummary() {
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        if (dataSet == null)
            return false;
        return (dataSet.getResultSummaryId() != null);
    }

    @Override
    public Long getResultSummaryId() {
        return ((DataSetData) getData()).getDataset().getResultSummaryId();
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {
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
        if (m_qcName != null && !m_qcName.trim().isEmpty()) {
            return m_qcName;
        }
        return super.toString();
    }

}
